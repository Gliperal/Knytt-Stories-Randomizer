package core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import map.KSMap;

public class Main
{
	private static final String RANDOMIZER_HEADER =
			"                          ,--------------------------,\n" + 
			"                          | KNYTT STORIES RANDOMIZER |\n" + 
			"                          |      version  2.0.3      |\n" + 
			"                          '--------------------------'";
	
	public static void main(String[] args)
	{
		// Get user input
		Scanner input = new Scanner(System.in);
		
		// Do everything in a sub method, so if an error gets free we can catch it before the program exits
		try
		{
			if (args.length > 0 && args[0].equals("restore"))
				restoreMain(input);
			else
				randoMain(input);
		} catch(Exception e)
		{
			e.printStackTrace();
			UserInput.waitForEnter(input, null);
		}
	}
	
	public static void randoMain(Scanner input)
	{
		// Load Knytt Stories location
		Console.printString("Loading Knytt Stories directory...");
		boolean validKSDir = KSFiles.loadKSDirectory();
		if (!validKSDir)
		{
			Console.printError("Unable to locate Knytt Stories directory.");
			return;
		}
		
		// Load object classes
		ObjectClassesFile classData;
		try
		{
			classData = loadObjectClasses();
		} catch (Exception e)
		{
			Console.printError(e.getMessage());
			return;
		}
		
		// Display header
		Console.printString(RANDOMIZER_HEADER);
		
		// Obtain user settings
		UserSettings settings = new UserSettings(Paths.get("resources", "UserSettings.txt"), classData);
		if (settings.firstLaunch() && UserInput.getBooleanInput(input, "It looks like it's your first time using the randomizer. Would you like to be guided through it?"))
			settings.ezEdit(input, classData);
		else
			settings.edit(input, classData);
		
		// Make sure the map exists and is backed up
		try
		{
			if (!KSFiles.backupMapFile(input))
				return;
		} catch (FileNotFoundException e)
		{
			Console.printError(e.getMessage());
		}
		
		// Load map
		KSMap map;
		try
		{
			map = KSFiles.readMap();
		} catch (Exception e)
		{
			Console.printError(e.getMessage());
			return;
		}
		
		// Begin randomization
		Console.printString("Randomizing...");
		Random rand = settings.getRandomFromSeed();
		for (RandoRule rule : settings.getRandoRules())
			rule.randomize(map, rand);
		
		// Randomize music
		map.randomizeMusic(rand);
		
		// Save map
		Console.printString("Saving...");
		try
		{
			KSFiles.writeMap(map);
			Console.printString("Saved successfully.");
		} catch (Exception e)
		{
			Console.printError(e.getMessage());
		}
		
		// Save user settings
		try
		{
			settings.saveSettingsToFile();
			Console.printString("Settings saved successfully.");
		} catch (IOException e)
		{
			Console.printError("Failed to save settings: " + e.getMessage());
		}
		
		// Launch Knytt Stories
		Console.printString("Press enter to exit. Type any letter to launch Knytt Stories and exit.");
		if (!input.nextLine().isEmpty())
		{
			try
			{
				KSFiles.launchKS();
			} catch (IOException e)
			{
				Console.printError("Could not launch Knytt Stories: " + e.getMessage());
				UserInput.waitForEnter(input, "Press enter to exit.");
			}
		}
	}
	
	private static ObjectClassesFile loadObjectClasses() throws Exception
	{
		// Load object classes file
		Console.printString("Loading object classes file...");
		Path ocFile = Paths.get("resources", "ObjectClasses.txt");
		if (!Files.exists(ocFile))
			throw new Exception("Unable to open resources/ObjectClasses.txt.");
		
		// Load object classes
		try
		{
			return new ObjectClassesFile(ocFile);
		} catch (IOException e)
		{
			throw new Exception("IOException reading from resources/ObjectClasses.txt.");
		} catch (ParseException e)
		{
			throw new Exception("Failed to parse resources/ObjectClasses.txt: Line " + e.getErrorOffset() + ": " + e.getMessage());
		}
	}
	
	private static void restoreMain(Scanner input)
	{
		// Confirm
		if (!UserInput.getBooleanInput(input, "This will restore all your randomized levels to their original states. Are you sure you wish to continue?"))
			return;
		
		// Load Knytt Stories location
		Console.printString("Loading Knytt Stories directory...");
		boolean validKSDir = KSFiles.loadKSDirectory();
		if (!validKSDir)
		{
			Console.printError("Unable to locate Knytt Stories directory.");
			return;
		}
		
		// Load restorable worlds
		ArrayList<String> restorableWorlds;
		try
		{
			restorableWorlds = KSFiles.backedUpWorlds();
		} catch (IOException e)
		{
			Console.printError("Unable to load worlds: " + e.getMessage());
			return;
		}
		if (restorableWorlds.isEmpty())
		{
			Console.printString("Found nothing to restore.");
			return;
		}
		
		// Iterate over the worlds
		ArrayList<String> restoredWorlds = new ArrayList<String>();
		ArrayList<String> failedWorlds = new ArrayList<String>();
		try
		{
			for (String world : restorableWorlds)
			{
				if (KSFiles.restoreMapFile(world))
					restoredWorlds.add(world);
				else
					failedWorlds.add(world);
			}
		}
		catch(FileNotFoundException e)
		{
			Console.printError(e.getMessage());
			return;
		}
		
		// Status
		if (!restoredWorlds.isEmpty())
		{
			Console.printString("The following worlds were restored correctly:");
			for (String w : restoredWorlds)
				Console.printString(w);
		}
		if (!failedWorlds.isEmpty())
		{
			Console.printString("The following worlds failed to restore:");
			for (String w : failedWorlds)
				Console.printString(w);
		}
	}
}
