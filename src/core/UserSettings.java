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
	private String world;
	private long seed;
	private RandoKey randoKey;
	private int randoType;
	
	private boolean loadFromFile(Path settingsFile, ObjectClassesFile classData) throws IOException
	{
		List<String> lines = Files.readAllLines(settingsFile);
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
		
		// Loaded successfully
		return true;
	}
	
	public UserSettings(Scanner input, ObjectClassesFile classData) throws Exception
	{
		// Get seed
		seed = UserInput.getSeedInput(input, "Enter seed or leave blank for random.");
		
		// Try to load settings file
		Path settingsFile = Paths.get("resources", "UserSettings.txt");
		if (Files.exists(settingsFile))
		{
			if (UserInput.getBooleanInput(input, "Use settings from last randomization?"))
			{
				try
				{
					if (loadFromFile(settingsFile, classData))
						return;
					else
						System.out.println("Error parsing UserSettings.txt... Skipping...");
				} catch (IOException e)
				{
					System.out.println("Error reading from UserSettings.txt... Skipping...");
				}
			}
		}
		else
			System.out.println("Could not load UserSettings.txt... Skipping...");
		
		// Specify world
		world = KSFiles.haveUserSelectWorld(input, "Select the world to randomize");
		if (world == null)
			throw new Exception();
		System.out.println("World specified: " + world);
		
		// Get other user input
		if (UserInput.getBooleanInput(input, "Would you like to see the object groups that are available for randomizing? [Y/N]"))
		{
			System.out.println("Randomizable groups:");
			classData.tabPrintClasses();
		}
		randoKey = UserInput.getRandoKeyInput(input, "Enter a key for the randomization classes (leave blank if unsure).", classData);
		randoType = UserInput.getCharInput(input, "Enter type of randomization (leave blank if unsure).\n\t0: Permute\n\t1: Shuffle\n\t2: Transform\n\t3: True random", new char[] {'0', '1', '2', '3'}, '0') - '0';
	}
	
	public Random getRandomFromSeed()
	{
		return new Random(seed);
	}

	public int randoType()
	{
		return randoType;
	}

	public RandoKey randoKey()
	{
		return randoKey;
	}
	
	public void saveSettingsToFile() throws IOException
	{
		Path settingsFile = Paths.get("resources", "UserSettings.txt");
		ArrayList<String> settingsLines = new ArrayList<String>();
		settingsLines.add(world);
		settingsLines.add(randoKey.keyString());
		settingsLines.add("" + randoType);
		settingsLines.add("m" + seed);
		Files.write(settingsFile, settingsLines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}
}
