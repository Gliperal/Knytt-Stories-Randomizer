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

import map.KSFiles;
import map.KSMap;
import map.LoadException;
import map.OffsetPattern;
import map.World;
import util.Console;
import util.UserInput;
import util.Util;

public class Main
{
	public enum ExitBehavior {
		ASK,
		NONE,
		KS,
	}

	private static final String VERSION_NUMBER = "3.1.0";
	private static final String RANDOMIZER_HEADER =
			"                          ,--------------------------,\n" +
			"                          | KNYTT STORIES RANDOMIZER |\n" +
			"                          |      version  " + VERSION_NUMBER + "      |\n" +
			"                          '--------------------------'";

	public static void main(String[] args)
	{
		// Get user input
		Scanner input = new Scanner(System.in);

		// Do everything in a sub method, so if an error gets free we can catch it before the program exits
		try
		{
			boolean restore = false;
			boolean rerun = false;
			ExitBehavior exitBehavior = ExitBehavior.ASK;
			for (int i = 0; i < args.length; i++)
			{
				String arg = args[i];
				if (arg.equals("--restore"))
					restore = true;
				else if (arg.equals("--exit=none"))
					exitBehavior = ExitBehavior.NONE;
				else if (arg.equals("--exit=ks"))
					exitBehavior = ExitBehavior.KS;
				else if (arg.equals("--rerun"))
					rerun = true;
			}
			int exitCode;
			if (restore)
				exitCode = restoreMain(input);
			else
				exitCode = randoMain(input, rerun);

			// Launch Knytt Stories
			switch (exitBehavior)
			{
			case ASK:
				Console.printString("Press enter to exit. Type any letter to launch Knytt Stories and exit.");
				if (input.nextLine().isEmpty())
					break;
			case KS:
				try
				{
					KSFiles.launchKS();
				} catch (IOException e)
				{
					Console.printError("Could not launch Knytt Stories: " + e.getMessage());
					System.exit(1);
				}
			case NONE:
			}
			System.exit(exitCode);
		} catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static int randoMain(Scanner input, boolean rerun)
	{
		// Load Knytt Stories location
		Console.printString("Loading Knytt Stories directory...");
		try
		{
			KSFiles.init();
		} catch (LoadException e)
		{
			Console.printError("Unable to locate Knytt Stories directory.");
			return 1;
		}

		// Load object classes
		ObjectClassesFile classData;
		try
		{
			classData = loadObjectClasses();
		} catch (Exception e)
		{
			Console.printError(e.getMessage());
			return 1;
		}

		// Display header
		Console.printString(RANDOMIZER_HEADER);

		// Obtain user settings
		UserSettings settings = new UserSettings(Paths.get("resources", "UserSettings.txt"), classData);
		if (rerun) {}
		else if (settings.firstLaunch() && UserInput.getBooleanInput(input, "It looks like it's your first time using the randomizer. Would you like to be guided through it?"))
			settings.ezEdit(input, classData);
		else
			settings.edit(input, classData);

		// Load world
		World world;
		try
		{
			world = KSFiles.getWorld(settings.getWorld());
		} catch (FileNotFoundException | LoadException e)
		{
			Console.printError(e.getMessage());
			return 1;
		}

		// Backup map file
		try
		{
			world.backupMapFile();
		} catch (IOException e)
		{
			if (!UserInput.getBooleanInput(input, "Unable to make a backup of the map file. The original map will be overwritten. Are you sure you wish to continue?"))
				return 1;
		}

		// Load map
		KSMap map;
		try
		{
			map = world.readOriginalMap();
		} catch (Exception e)
		{
			Console.printError(e.getMessage());
			return 1;
		}

		// Begin randomization
		Console.printString("Randomizing...");
		Random rand = settings.getRandomFromSeed();
		ArrayList<RandoRule> rules = settings.getRandoRules();
		for (RandoRule rule : rules)
			rule.randomize(map, rand);

		// Randomize music
		map.randomizeMusic(rand);

		// Save map
		Console.printString("Saving...");
		try
		{
			map.addAdditionalInfo("Randomizer Version", VERSION_NUMBER, true);
			map.addAdditionalInfo("Randomizer Seed", settings.getSeed().toString(), true);
			map.addAdditionalInfo("Randomizer Rules", Util.join("\n", rules), true);
			map.addAdditionalInfo("Randomizer Hash", Util.condenseHash(map.hash(), 8), true);
			world.writeMap(map);
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
		return 0;
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

	private static int restoreMain(Scanner input)
	{
		// Confirm
		if (!UserInput.getBooleanInput(input, "This will restore all your randomized levels to their original states. Are you sure you wish to continue?"))
			return 0;

		// Load Knytt Stories location
		Console.printString("Loading Knytt Stories directory...");
		try
		{
			KSFiles.init();
		} catch (LoadException e)
		{
			Console.printError("Unable to locate Knytt Stories directory.");
			return 1;
		}

		// Load restorable worlds
		ArrayList<String> restorableWorlds;
		try
		{
			restorableWorlds = KSFiles.backedUpWorlds();
		} catch (IOException e)
		{
			Console.printError("Unable to load worlds: " + e.getMessage());
			return 1;
		}
		if (restorableWorlds.isEmpty())
		{
			Console.printString("Found nothing to restore.");
			return 0;
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
			return 1;
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
		return 0;
	}
}
