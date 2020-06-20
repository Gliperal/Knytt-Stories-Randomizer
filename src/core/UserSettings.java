package core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import util.AlphabeticalComparator;
import util.Util;

public class UserSettings
{
	private static final Path settingsFile = Paths.get("resources", "UserSettings.txt");
	private static final String RANDOMIZER_HEADER =
			"KNYTT STORIES RANDOMIZER\n" +
			"version 2.0";
	private static final String HELP_MESSAGE =
			"B: Begin randomization.\n" +
			"D: Display current settings.\n" +
			"H: Show help.\n" +
			"P: Load randomization rules from preset.\n" +
			"R: Specify randomization rules.\n" +
			"S: Enter seed.\n" +
			"W: Select world to randomize.";
	public static final String RANDO_TYPE_TABLE = 
			"                          ,---------------------,------------------------,\n" + 
			"                          | Objects of the same | Objects are randomized |\n" + 
			"                          | type get randomized | independent of similar |\n" + 
			"                          | together            | objects                |\n" + 
			",-------------------------+---------------------+------------------------|\n" + 
			"|        Only use objects |       Permute       |        Shuffle         |\n" + 
			"|    present in the level |                     |                        |\n" + 
			"|-------------------------+---------------------+------------------------|\n" + 
			"|    Use any objects from |      Transform      |      True random       |\n" + 
			"| the randomization group |                     |                        |\n" + 
			"'-------------------------'---------------------'------------------------'";
	
	private String world;
	private Long seed;
	private ArrayList<RandoRule> randoRules;
	private HashMap<String, JSONArray> presets;
	
	private boolean loadFromFile(ObjectClassesFile classData)
	{
		JSONObject data;
		try
		{
			byte[] settingsBytes = Files.readAllBytes(settingsFile);
			String settingsString = new String(settingsBytes);
			data = new JSONObject(settingsString);
		} catch (IOException e)
		{
			return false;
		}
		
		// World
		try {
			world = data.getString("world");
			KSFiles.specifyWorld(world);
		} catch(JSONException e) {
			world = null;
		} catch (Exception e) // TODO or whatever exception specifyWorld will eventually throw
		{
			world = null;
		}
		
		// Seed
		try {
			seed = data.getLong("seed");
		} catch(JSONException e) {
			seed = null;
		}
		
		// Rules
		try {
			randoRules = new ArrayList<RandoRule>();
			JSONArray rulesJSON = data.getJSONArray("rules");
			for (int i = 0; i < rulesJSON.length(); i++)
			{
				String rule = rulesJSON.getString(i);
				randoRules.add(RandoRule.loadFromString(rule, classData));
			}
		} catch(JSONException e) {
			randoRules = null;
		} catch (ParseException e)
		{
			System.out.println("Error parsing rules: " + e.getMessage() + " (line " + e.getErrorOffset() + ")");
			randoRules = null;
		}
		
		// Presets
		try {
			presets = new HashMap<String, JSONArray>();
			JSONObject presetsJSON = data.getJSONObject("presets");
			for (String key : presetsJSON.keySet())
				presets.put(key, presetsJSON.getJSONArray(key));
		} catch(JSONException e) {
			presets = null;
		}
		
		// Loaded successfully
		return true;
	}
		
	private void setSeed(Scanner input)
	{
		seed = UserInput.getSeedInput(input, "Enter seed or leave blank for random.");
		System.out.println("Seed entered.");
	}
	
	private void setWorld(Scanner input)
	{
		world = KSFiles.haveUserSelectWorld(input, "Select the world to randomize.");
		if (world == null)
			System.out.println("Failed to select world.");
		else
			System.out.println("World specified: " + world);
	}
	
	private String setRandoTypes(Scanner input, ObjectClassesFile classData)
	{
		String randoTypes;
		while (true)
		{
			System.out.println("Enter type(s) of randomization (leave blank if unsure). Enter H for help. \n\tP: Permute\n\tS: Shuffle\n\tT: Transform\n\tR: True random");
			randoTypes = input.nextLine().replaceAll("\\s+", "").toUpperCase();
			if (randoTypes.startsWith("H"))
			{
				System.out.println(RANDO_TYPE_TABLE);
				continue;
			}
			boolean validTypes = true;
			for (int i = 0; i < randoTypes.length(); i++)
				if ("PSTR".indexOf(randoTypes.charAt(i)) == -1)
				{
					System.out.println("Error: " + randoTypes.charAt(i) + " was not recognized as a randomization type.");
					validTypes = false;
					break;
				}
			if (validTypes)
				break;
		}
		if (randoTypes.isEmpty())
			randoTypes = Character.toString(RandoRulePermute.ID);
		return randoTypes;
	}
	
	private void setRandoRules(Scanner input, ObjectClassesFile classData, String randoTypes)
	{
		System.out.println("Randomizable groups:");
		classData.tabPrintClasses();
		randoRules = new ArrayList<RandoRule>();
		for (int i = 0; i < randoTypes.length(); i++)
		{
			char type = randoTypes.charAt(i);
			if (randoTypes.length() == 1)
				System.out.println("Enter rules for the randomization (leave blank if unsure). One rule per line. Leave an empty line when done.");
			else if (type == RandoRulePermute.ID)
				System.out.println("Enter rules for the Permute randomization (leave blank if unsure). One rule per line. Leave an empty line when done.");
			else if (type == RandoRuleShuffle.ID)
				System.out.println("Enter rules for the Shuffle randomization (leave blank if unsure). One rule per line. Leave an empty line when done.");
			else if (type == RandoRuleTransform.ID)
				System.out.println("Enter rules for the Transform randomization (leave blank if unsure). One rule per line. Leave an empty line when done.");
			else
				System.out.println("Enter rules for the True Random randomization (leave blank if unsure). One rule per line. Leave an empty line when done.");
			boolean noRules = true;
			while (true)
			{
				String key = input.nextLine();
				if (key.isEmpty())
					break;
				try
				{
					randoRules.add(RandoRule.create(type, key, classData));
					noRules = false;
				}
				catch (ParseException e)
				{
					System.out.println("Failed to parse randomization key: " + e.getMessage());
				}
			}
			if (noRules)
			{
				try
				{
					// Try to use default
					randoRules.add(RandoRule.create(type, "E", classData));
					randoRules.add(RandoRule.create(type, "J", classData));
					randoRules.add(RandoRule.create(type, "P", classData));
				}
				catch(ParseException e)
				{
					// If that fails
					System.out.println("Failed to parse default randomization rules.");
					randoRules = null;
					return;
				}
			}
		}
	}
	
	private void saveRulesAsPreset(Scanner input)
	{
		if (presets == null)
			presets = new HashMap<String, JSONArray>();
		String name;
		while (true)
		{
			System.out.println("Enter a name for this preset.");
			name = input.nextLine().trim();
			if (name.isEmpty())
				continue;
			if (!presets.containsKey(name))
				break;
			if (UserInput.getBooleanInput(input, "Preset \"" + name + "\" already exists. Overwrite?"))
				break;
		}
		JSONArray preset = new JSONArray();
		for (RandoRule rule : randoRules)
			preset.put(rule.saveToString());
		presets.put(name, preset);
		try
		{
			savePresetsToFile();
			System.out.println("Preset saved.");
		}
		catch (IOException e)
		{
			System.out.println("Preset added, but unable to access resources/UserSettings.txt to save.");
		}
	}
	
	private void loadRandoPreset(Scanner input, ObjectClassesFile classData)
	{
		if (presets == null || presets.isEmpty())
		{
			System.out.println("No presets available.");
			return;
		}
		ArrayList<String> presetNames = new ArrayList<String>(presets.keySet());
		presetNames.sort(new AlphabeticalComparator());
		System.out.println("Available presets:");
		Util.displayListConsicesly(presetNames, 8, 6);
		JSONArray preset;
		while (true)
		{
			System.out.println("Enter the name of a preset to load or a string to search.");
			String search = input.nextLine();
			// TODO containsKey case insensitive
			if (presets.containsKey(search))
			{
				preset = presets.get(search);
				break;
			}
			ArrayList<Integer> matches = Util.keywordMatch(presetNames, search.split("\\s+"));
			if (matches.size() == 0)
			{
				System.out.println("Found no presets matching \"" + search + "\"");
				continue;
			}
			System.out.println("Presets matching \"" + search + "\"");
			for (int i : matches)
				System.out.println(presetNames.get(i));
		}
		try
		{
			ArrayList<RandoRule> rules = new ArrayList<RandoRule>();
			for (int i = 0; i < preset.length(); i++)
			{
				String ruleStr = preset.getString(i);
				RandoRule rule = RandoRule.loadFromString(ruleStr, classData);
				rules.add(rule);
			}
			// TODO would you like this preset to replace or add onto the existing rules?
			randoRules = rules;
			System.out.println("Preset loaded.");
		}
		catch (ParseException e)
		{
			System.out.println("Error loading preset: " + e.getMessage() + " (line " + e.getErrorOffset() + ")");
		}
	}
	
	private void displaySettings()
	{
		String worldStr = (world == null) ? "[use W to select world]" : world;
		String seedStr = (seed == null) ? "[will be randomly generated; use S to set seed]" : seed.toString();
		System.out.println("Settings:\nWorld: " + worldStr + "\nSeed: " + seedStr);
		if (randoRules == null)
			System.out.println("Randomization rules: [use R to set rules]");
		else
		{
			System.out.println("Randomization rules:");
			for (RandoRule r : randoRules)
				System.out.println("\t" + r.toString());
		}
	}
	
	private boolean checkForErrors()
	{
		boolean ok = true;
		if (world == null)
		{
			System.out.println("No world specified! Use W to set world.");
			ok = false;
		}
		if (randoRules == null)
		{
			System.out.println("No randomization rules specified! Use R to set rules.");
			ok = false;
		}
		return ok;
	}
	
	// TODO exceptions are not being caught and printed properly
	public UserSettings(Scanner input, ObjectClassesFile classData)
	{
		// Try to load settings file
		boolean loaded = true;
		if (Files.exists(settingsFile))
		{
			// Load settings from last randomization by default
			if (loadFromFile(classData))
			{
				// TODO "Loaded last saved settings from __ minutes ago"
			}
			else
				loaded = false;
		}
		else
			loaded = false;
		
		if (!loaded)
			System.out.println("Could not load UserSettings.txt. Presets and previous randomization will not be available.");
		
		// Main input loop
		System.out.println(HELP_MESSAGE);
		while (true)
		{
			char c = UserInput.getCharInput(input, null, "HWSRPDB".toCharArray(), '.');
			if (c == 'H' || c == '.')
				System.out.println(HELP_MESSAGE);
			else if (c == 'W')
				setWorld(input);
			else if (c == 'S')
				setSeed(input);
			else if (c == 'R')
			{
				String randoTypes = setRandoTypes(input, classData);
				setRandoRules(input, classData, randoTypes);
				if (UserInput.getBooleanInput(input, "Would you like to save these randomization rules as a preset?"))
					saveRulesAsPreset(input);
				else
					System.out.println("Rules updated.");
			}
			else if (c == 'P')
				loadRandoPreset(input, classData);
			else if (c == 'D')
				displaySettings();
			else if (c == 'B')
				if (checkForErrors())
					break;
		}
		
		if (seed == null)
			seed = System.nanoTime();
		
		// TODO check for the same object in multiple rule inputs
	}
	
	public Random getRandomFromSeed()
	{
		return new Random(seed);
	}
	
	public ArrayList<RandoRule> getRandoRules()
	{
		return randoRules;
	}
	
	public void savePresetsToFile() throws IOException
	{
		JSONObject data;
		if (Files.exists(settingsFile))
		{
			byte[] settingsBytes = Files.readAllBytes(settingsFile);
			String settingsString = new String(settingsBytes);
			data = new JSONObject(settingsString);
		}
		else
			data = new JSONObject();
		
		JSONObject presetsJSON = new JSONObject();
		for (String key : presets.keySet())
			presetsJSON.put(key, presets.get(key));
		data.put("presets", presetsJSON);
		
		Files.write(settingsFile, data.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}
	
	public void saveSettingsToFile() throws IOException
	{
		JSONObject data = new JSONObject();
		data.put("world", world);
		data.put("seed", (long) seed);
		JSONArray rulesJSON = new JSONArray();
		for (RandoRule r : randoRules)
			rulesJSON.put(r.saveToString());
		data.put("rules", rulesJSON);
		JSONObject presetsJSON = new JSONObject();
		for (String key : presets.keySet())
			presetsJSON.put(key, presets.get(key));
		data.put("presets", presetsJSON);
		Files.write(settingsFile, data.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}
}
