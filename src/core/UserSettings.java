package core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import util.Util;

public class UserSettings
{
	private static final String HELP_MESSAGE =
			"==========--  W: Select world    R: Specify rules    S: Enter seed  --=========\n" + 
			"=======================--  P: Preset menu    B: Begin  --======================";
	private static final String PRESET_PROMPT =
			"============--  L: Load preset    A: Add preset to current rules  --===========\r\n" + 
			"===========--  S: Save current rules as preset   D: Delete preset  --==========\r\n" + 
			"==========================-  Leave blank to return.  -=========================";
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
	
	private Path settingsFile;
	private String world;
	private Long seed;
	private ArrayList<RandoRule> randoRules;
	private RulesPresets presets;
	private boolean firstLaunch;
	
	public UserSettings(Path file, ObjectClassesFile classData)
	{
		if (file == null)
			throw new NullPointerException();
		settingsFile = file;
		presets = new RulesPresets();
		
		// Try to load settings file
		String filename = file.toString();
		if (Files.exists(settingsFile))
		{
			// Load settings from last randomization by default
			long time_ago = loadFromFile(classData);
			if (time_ago < 0)
				Console.printWarning("Could not load " + filename + ". Presets and previous randomization will not be available.");
			else
			{
				if (time_ago > 0)
					Console.printString("Loaded last saved settings from " + Util.millisecondsToTimeString(time_ago) + " ago:");
				else if (time_ago == 0)
					Console.printString("Loaded settings from unknown date:");
				displaySettings();
			}
		}
		else
			firstLaunch = true;
	}
	
	/**
	 * 
	 * @param classData
	 * @return The number of milliseconds since the file was saved, or 0 if
	 * such information could not be obtained. Returns -1 on error.
	 */
	private long loadFromFile(ObjectClassesFile classData)
	{
		JSONObject data;
		try
		{
			byte[] settingsBytes = Files.readAllBytes(settingsFile);
			String settingsString = new String(settingsBytes);
			data = new JSONObject(settingsString);
		} catch (IOException e)
		{
			return -1;
		}
		
		// World
		try {
			world = data.getString("world");
			KSFiles.specifyWorld(world);
		} catch(JSONException | FileNotFoundException e) {
			world = null;
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
			Console.printRed("Error parsing rules: " + e.getMessage() + " (line " + e.getErrorOffset() + ")");
			randoRules = null;
		}
		
		// Presets
		try {
			JSONObject presetsJSON = data.getJSONObject("presets");
			presets = new RulesPresets(presetsJSON);
		} catch(JSONException e) {}
		
		// Loaded successfully
		try
		{
			return System.currentTimeMillis() - data.getLong("timestamp");
		}
		catch(JSONException e)
		{
			return 0;
		}
	}
	
	private void setSeed(Scanner input)
	{
		seed = UserInput.getSeedInput(input, "Enter seed or leave blank for random.");
		Console.printString("Seed entered.");
	}
	
	private void setWorld(Scanner input)
	{
		try
		{
			world = KSFiles.haveUserSelectWorld(input, "Select the world to randomize.");
			if (world == null)
				Console.printString("Cancelled.");
			else
				Console.printString("World specified: " + world);
		}
		catch (IOException e)
		{
			Console.printError(e.getMessage());
			Console.printString("Failed to select world.");
			world = null;
		}
	}
	
	private String setRandoTypes(Scanner input, ObjectClassesFile classData)
	{
		String randoTypes;
		while (true)
		{
			Console.printString("Enter type(s) of randomization (leave blank if unsure). Enter H for help. \n\tP: Permute\n\tS: Shuffle\n\tT: Transform\n\tR: True random");
			randoTypes = input.nextLine().replaceAll("\\s+", "").toUpperCase();
			if (randoTypes.startsWith("H"))
			{
				Console.printString(RANDO_TYPE_TABLE);
				continue;
			}
			boolean validTypes = true;
			for (int i = 0; i < randoTypes.length(); i++)
				if ("PSTR".indexOf(randoTypes.charAt(i)) == -1)
				{
					Console.printError(randoTypes.charAt(i) + " was not recognized as a randomization type.");
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
		Console.printString("Randomizable groups:");
		classData.tabPrintClasses();
		randoRules = new ArrayList<RandoRule>();
		for (int i = 0; i < randoTypes.length(); i++)
		{
			char type = randoTypes.charAt(i);
			if (randoTypes.length() == 1)
				Console.printString("Enter rules for the randomization (leave blank if unsure). One rule per line. Leave an empty line when done.");
			else if (type == RandoRulePermute.ID)
				Console.printString("Enter rules for the Permute randomization (leave blank if unsure). One rule per line. Leave an empty line when done.");
			else if (type == RandoRuleShuffle.ID)
				Console.printString("Enter rules for the Shuffle randomization (leave blank if unsure). One rule per line. Leave an empty line when done.");
			else if (type == RandoRuleTransform.ID)
				Console.printString("Enter rules for the Transform randomization (leave blank if unsure). One rule per line. Leave an empty line when done.");
			else
				Console.printString("Enter rules for the True Random randomization (leave blank if unsure). One rule per line. Leave an empty line when done.");
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
					Console.printString("Failed to parse randomization key: " + e.getMessage());
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
					Console.printError("Failed to parse default randomization rules.");
					randoRules = null;
					return;
				}
			}
		}
	}
	
	private void saveRulesAsPreset(Scanner input)
	{
		if (randoRules == null)
			Console.printString("No rules currently specified to save.");
		else if (presets.addPreset(input, randoRules))
		{
			try
			{
				savePresetsToFile();
				Console.printString("Preset saved.");
			}
			catch (IOException e)
			{
				Console.printWarning("Preset added, but unable to access resources/UserSettings.txt to save.");
			}
		}
		else
			Console.printString("Preset saving cancelled.");
	}
	
	private void deletePreset(Scanner input)
	{
		if (presets.deletePreset(input))
		{
			try
			{
				savePresetsToFile();
				Console.printString("Preset deleted.");
			}
			catch (IOException e)
			{
				Console.printWarning("Preset deleted, but unable to access resources/UserSettings.txt to save.");
			}
		}
	}
	
	private void loadRandoPreset(Scanner input, ObjectClassesFile classData, boolean append)
	{
		try
		{
			ArrayList<RandoRule> rules = presets.loadPreset(input, classData);
			if (rules != null)
			{
				if (append)
					randoRules.addAll(rules);
				else
					randoRules = rules;
			}
		}
		catch (ParseException e)
		{
			Console.printRed("Error loading preset: " + e.getMessage() + " (line " + e.getErrorOffset() + ")");
		}
	}
	
	private void presetMenu(Scanner input, ObjectClassesFile classData)
	{
		while (true)
		{
			System.out.println();
			switch (UserInput.getCharInput(input, PRESET_PROMPT, "LADS".toCharArray(), '.'))
			{
			case 'L':
				loadRandoPreset(input, classData, false);
				return;
			case 'A':
				loadRandoPreset(input, classData, true);
				return;
			case 'S':
				saveRulesAsPreset(input);
				return;
			case 'D':
				deletePreset(input);
				return;
			case '.':
				return;
			}
		}
	}
	
	private void displaySettings()
	{
		String worldStr = (world == null) ? "[use W to select world]" : world;
		String seedStr = (seed == null) ? "[will be randomly generated; use S to set seed]" : seed.toString();
		Console.printString("World: " + worldStr + "\nSeed: " + seedStr);
		if (randoRules == null)
			Console.printString("Randomization rules: [use R to set rules]");
		else
		{
			Console.printString("Randomization rules:");
			for (RandoRule r : randoRules)
				Console.printString("\t" + r.toString());
		}
	}
	
	private boolean checkForErrors()
	{
		// Check for fatal errors
		boolean ok = true;
		if (world == null)
		{
			Console.printError("No world specified! Use W to set world.");
			ok = false;
		}
		if (randoRules == null)
		{
			Console.printError("No randomization rules specified! Use R to set rules.");
			ok = false;
		}
		else
		{
			// Check for the same object in multiple rule inputs
			// (TODO update to just the inputs when rules can all happen at once)
			for (int a = 0; a < randoRules.size(); a++)
			{
				RandoRule ruleA = randoRules.get(a);
				for (int b = a + 1; b < randoRules.size(); b++)
				{
					RandoRule ruleB = randoRules.get(b);
					int obj = ruleA.conflictsWith(ruleB);
					if (obj != -1)
						Console.printWarning("Bank " + Util.separateBank(obj) + " object " + Util.separateObj(obj) + " found in more than one randomization rule. Overlap between rules may lead to undefined behavior.");
				}
			}
		}
		return ok;
	}
	
	public void edit(Scanner input, ObjectClassesFile classData)
	{
		// Main input loop
		Console.printString(HELP_MESSAGE);
		while (true)
		{
			String s = input.nextLine().toUpperCase();
			if (s.isEmpty())
				s = ".";
			switch(s.charAt(0))
			{
			case 'W':
				setWorld(input);
				break;
			case 'S':
				setSeed(input);
				break;
			case 'R':
				String randoTypes = setRandoTypes(input, classData);
				setRandoRules(input, classData, randoTypes);
				Console.printString("Rules updated.");
				break;
			case 'P':
				presetMenu(input, classData);
				break;
			case 'D':
				Console.printString("Settings:");
				displaySettings();
				break;
			case 'B':
				if (checkForErrors())
				{
					displaySettings();
					return;
				}
				break;
			default:
				Console.printString(HELP_MESSAGE);
			}
		}
	}
	
	public void ezEdit(Scanner input, ObjectClassesFile classData)
	{
		do
		{
			while (world == null)
				setWorld(input);
			while (randoRules == null)
			{
				String randoTypes = setRandoTypes(input, classData);
				setRandoRules(input, classData, randoTypes);
			}
		}
		while (checkForErrors() == false);
	}
	
	public Random getRandomFromSeed()
	{
		if (seed == null)
			seed = System.nanoTime();
		return new Random(seed);
	}
	
	public ArrayList<RandoRule> getRandoRules()
	{
		return randoRules;
	}
	
	public boolean firstLaunch()
	{
		return firstLaunch;
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
		data.put("presets", presets.toJSON());
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
		if (!presets.isEmpty())
			data.put("presets", presets.toJSON());
		data.put("timestamp", System.currentTimeMillis());
		Files.write(settingsFile, data.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}
}
