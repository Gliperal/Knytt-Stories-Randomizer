package core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class UserSettings
{
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
	private long seed;
	private ArrayList<RandoRule> randoRules;
	
	private boolean loadFromFile(Path settingsFile, ObjectClassesFile classData)
	{
		List<String> lines;
		try
		{
			lines = Files.readAllLines(settingsFile);
		} catch (IOException e)
		{
			return false;
		}
		if (lines.size() != 4)
			return false;
		
		// World
		world = lines.get(0).trim();
		try
		{
			KSFiles.specifyWorld(world);
		} catch (Exception e)
		{
			return false;
		}
		
		/*
		 * TODO
		// Rando Key
		try
		{
			randoKey = new RandoKey(lines.get(1).trim(), classData);
		}
		catch (Exception e)
		{
			return false;
		}
		
		// Rando Types
		try
		{
			randoType = Integer.parseInt(lines.get(2).trim());
		} catch (NumberFormatException e)
		{
			return false;
		}
		*/
		
		// Loaded successfully
		return true;
	}
	
	public UserSettings(Scanner input, ObjectClassesFile classData) throws Exception
	{
		// Try to load settings file
		Path settingsFile = Paths.get("resources", "UserSettings.txt");
		if (!Files.exists(settingsFile))
		{
			settingsFile = null;
			System.out.println("Could not load UserSettings.txt... Skipping...");
		}
		
		// Load seed, and give the option to re-randomize with previous settings.
		// TODO set seed should remove the S part of the prompt
		seed = System.nanoTime();
		while (true)
		{
			char c;
			if (settingsFile == null)
				c = UserInput.getCharInput(input, "Input S to enter seed. Leave blank to proceed with world selection.", "S".toCharArray(), 'W');
			else
				c = UserInput.getCharInput(input, "Input S to enter seed. Input R to re-randomize. Leave blank to proceed with world selection.", "SR".toCharArray(), 'W');
			if (c == 'S')
				seed = UserInput.getSeedInput(input, "Enter seed or leave blank for random.");
			else if (c == 'R')
			{
				if (loadFromFile(settingsFile, classData))
					return;
				else
				{
					System.out.println("Error parsing UserSettings.txt... Skipping...");
					settingsFile = null;
				}
			}
			else
				break;
		}
		
		// Specify world
		world = KSFiles.haveUserSelectWorld(input, "Select the world to randomize.");
		if (world == null)
			throw new Exception();
		System.out.println("World specified: " + world);
		
		// TODO Load presets
		String presets = "L";
		
		// Give the option to use a preset, or get the randomization types to be used (default permute)
		String randoTypes;
		while (true)
		{
			if (presets == null)
				System.out.println("Enter type(s) of randomization (leave blank if unsure). Enter H for help. \n\tP: Permute\n\tS: Shuffle\n\tT: Transform\n\tR: True random");
			else
				System.out.println("Enter L to load a randomization preset, or enter type(s) of randomization (leave blank if unsure). Enter H for help. \n\tP: Permute\n\tS: Shuffle\n\tT: Transform\n\tR: True random");
			randoTypes = input.nextLine().replaceAll("\\s+", "").toUpperCase();
			if (randoTypes.startsWith("H"))
			{
				System.out.println(RANDO_TYPE_TABLE);
				continue;
			}
//			else if (randoTypes.startsWith("L"))
				// TODO load rando types presets
//				if (loadPreset())
//					return;
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
			randoTypes = "P";
		
		// Get the randomization rules for each type of randomization
		System.out.println("Randomizable groups:");
		classData.tabPrintClasses();
		randoRules = new ArrayList<RandoRule>();
		for (int i = 0; i < randoTypes.length(); i++)
		{
			char type = randoTypes.charAt(i);
			if (randoTypes.length() == 1)
				System.out.println("Enter rules for the randomization (leave blank if unsure). Leave an empty line when done.");
			else if (type == 'P')
				System.out.println("Enter rules for the Permute randomization (leave blank if unsure). Leave an empty line when done.");
			else if (type == 'S')
				System.out.println("Enter rules for the Shuffle randomization (leave blank if unsure). Leave an empty line when done.");
			else if (type == 'T')
				System.out.println("Enter rules for the Transform randomization (leave blank if unsure). Leave an empty line when done.");
			else
				System.out.println("Enter rules for the True Random randomization (leave blank if unsure). Leave an empty line when done.");
			while (true)
			{
				String key = input.nextLine();
				if (key.isEmpty())
					break;
				try
				{
					if (type == 'P')
						randoRules.add(new RandoRulePermute(key, classData));
					else if (type == 'S')
						randoRules.add(new RandoRuleShuffle(key, classData));
					else if (type == 'T')
						randoRules.add(new RandoRuleTransform(key, classData));
					else if (type == 'R')
						randoRules.add(new RandoRuleTrueRandom(key, classData));
				}
				catch (Exception e)
				{
					System.out.println("Failed to parse randomization key: " + e.getMessage());
				}
			}
		}
		
		// TODO check for the same object in multiple rule inputs
		
		// TODO do you want to save these settings?
	}
	
	public Random getRandomFromSeed()
	{
		return new Random(seed);
	}
	
	public RandoKey randoKey()
	{
		return null;
	}
	
	public void saveSettingsToFile() throws IOException
	{
		// TODO
		/*
		Path settingsFile = Paths.get("resources", "UserSettings.txt");
		ArrayList<String> settingsLines = new ArrayList<String>();
		settingsLines.add(world);
		settingsLines.add(randoKey.keyString());
		settingsLines.add("" + randoType);
		settingsLines.add("m" + seed);
		Files.write(settingsFile, settingsLines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		*/
	}
}
