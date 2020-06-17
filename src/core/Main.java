package core;

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
		}
		UserInput.waitForEnter(input, "Press enter to exit.");
	}
	
	public static void randoMain(Scanner input)
	{
		// Load Knytt Stories location
		System.out.println("Loading Knytt Stories directory...");
		boolean validKSDir = KSFiles.loadKSDirectory();
		if (!validKSDir)
		{
			System.out.println("Unable to locate Knytt Stories directory.");
			return;
		}
		
		// Load object classes
		ObjectClassesFile classData;
		try
		{
			classData = loadObjectClasses();
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
			return;
		}
		
		// Obtain user settings
		UserSettings settings = new UserSettings(input, classData);
		
		// Do the thing
		UserInput.waitForEnter(input, "Press enter to begin.");
		
		// Make sure the map exists and is backed up
		boolean backedUp = KSFiles.backupMapFile(input);
		if (!backedUp)
			return;
		
		// Load map
		KSMap map = KSFiles.readMap();
		if (map == null)
			return;
		
		// Begin randomization
		System.out.println("Randomizing...");
		Random rand = settings.getRandomFromSeed();
		for (RandoRule rule : settings.getRandoRules())
			rule.randomize(map, rand);
		
		// Randomize music
		map.randomizeMusic(rand);
		
		// Save map
		System.out.println("Saving...");
		boolean saved = KSFiles.writeMap(map);
		if (saved)
			System.out.println("Saved successfully.");
		
		// Save user settings
		if (UserInput.getBooleanInput(input, "Would you like to save these settings for re-randomization?"))
		{
			try
			{
				settings.saveSettingsToFile();
				System.out.println("Settings saved successfully.");
			} catch (IOException e)
			{
				System.out.println("Failed to save settings.");
			}
		}
	}
	
	private static ObjectClassesFile loadObjectClasses() throws Exception
	{
		// Load object classes file
		System.out.println("Loading object classes file...");
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
		if (!UserInput.getBooleanInput(input, "This will restore all your randomized levels to their original states. Are you sure you wish to continue? [Y/N]"))
			return;
		
		// Load Knytt Stories location
		System.out.println("Loading Knytt Stories directory...");
		boolean validKSDir = KSFiles.loadKSDirectory();
		if (!validKSDir)
		{
			System.out.println("Unable to locate Knytt Stories directory.");
			return;
		}
		
		// Load restorable worlds
		ArrayList<String> restorableWorlds = KSFiles.backedUpWorlds();
		if (restorableWorlds == null)
			return;
		if (restorableWorlds.isEmpty())
			System.out.println("Found nothing to restore.");
		
		// Iterate over the worlds
		ArrayList<String> restoredWorlds = new ArrayList<String>();
		ArrayList<String> failedWorlds = new ArrayList<String>();
		for (String world : restorableWorlds)
		{
			if (KSFiles.restoreMapFile(world))
				restoredWorlds.add(world);
			else
				failedWorlds.add(world);
		}
		
		// Status
		if (!restoredWorlds.isEmpty())
		{
			System.out.println("The following worlds were restored correctly:");
			for (String w : restoredWorlds)
				System.out.println(w);
		}
		if (!failedWorlds.isEmpty())
		{
			System.out.println("The following worlds failed to restore:");
			for (String w : failedWorlds)
				System.out.println(w);
		}
	}
}
