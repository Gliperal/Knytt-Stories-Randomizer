package core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Scanner;

import org.stackoverflowusers.file.WindowsShortcut;

import map.KSMap;

public class KSFiles
{
	private static final String[] EXECUTABLES = {
			"Knytt Stories Plus.exe",
			"knytt stories ex.exe",
			"Knytt Stories Speedrun Edition 0.2.2.exe",
			"Knytt Stories Speedrun Edition 0.3.1.exe",
			"Knytt Stories.exe"
	};
	private static Path ksDir = null;
	private static Path worldFolder = null;
	private static Path mapFile = null;
	private static Path originalMapFile = null;

	public static boolean loadKSDirectory()
	{
		Path workingDir = Paths.get(System.getProperty("user.dir"));

		// If the jar is being run from a subfolder of the KS directory
		Console.printString("Checking local files...");
		ksDir = workingDir.getParent();
		if (isKSDirectory(ksDir))
		{
			Console.printString(" Yes.");
			return true;
		}
		Console.printString(" No.");

		// If there is a symbolic link in the working directory called "KS"
		System.out.print("Checking for a KS symbolic link...");
		ksDir = workingDir.resolve("KS");
		if (isKSDirectory(ksDir))
		{
			Console.printString(" Yes.");
			return true;
		}
		Console.printString(" No.");

		// If there is a Windows link in the working directory called "KS"
		System.out.print("Checking for a KS.lnk Windows link...");
		ksDir = workingDir.resolve("KS.lnk");
		if (Files.exists(ksDir))
		{
			try
			{
				WindowsShortcut shortcut = new WindowsShortcut(ksDir.toFile());
				ksDir = Paths.get(shortcut.getRealFilename());
				if (isKSDirectory(ksDir))
				{
					Console.printString(" Yes.");
					return true;
				}
			} catch (Exception e) {}
		}
		Console.printString(" No.");

		// If there is a text file in the working directory called KS.txt, containing the Knytt Stories directory
		System.out.print("Checking in KS.txt...");
		Path ksTxt = workingDir.resolve("KS.txt");
		if (Files.exists(ksTxt))
		{
			try
			{
				BufferedReader br = Files.newBufferedReader(ksTxt);
				String line = br.readLine().trim();
				ksDir = Paths.get(line);
				if (isKSDirectory(ksDir))
				{
					Console.printString(" Yes.");
					return true;
				}
			} catch (IOException e) {}
		}
		Console.printString(" No.");
		return false;
	}

	private static Path getExecutable(Path dir)
	{
		if (!Files.isDirectory(dir))
			return null;
		for (String name : EXECUTABLES)
		{
			Path exe = dir.resolve(name);
			if (Files.exists(exe))
				return exe;
		}
		return null;
	}

	private static boolean isKSDirectory(Path dir)
	{
		return getExecutable(dir) != null;
	}

	public static String haveUserSelectWorld(Scanner input, String prompt) throws FileNotFoundException, IOException
	{
		// Load worlds directory
		Path worldsDir = ksDir.resolve("Worlds");
		if (!Files.exists(worldsDir))
			throw new FileNotFoundException("Missing Worlds directory.");

		// Collect worlds
		ArrayList<Path> worlds = new ArrayList<Path>();
		ArrayList<String> worldStrings = new ArrayList<String>();
		try
		{
			for (Path world : Files.newDirectoryStream(worldsDir))
			{
				if (!Files.isDirectory(world))
					continue;
				worlds.add(world);
				worldStrings.add(world.getFileName().toString());
			}
		} catch (IOException e)
		{
			throw new IOException("IOException loading worlds: " + e.getMessage());
		}

		// Get user choice
		int worldID = UserInput.getInputFromList(input, prompt, "world", worldStrings);
		if (worldID == -1)
			return null;
		worldFolder = worlds.get(worldID);
		return worldFolder.getFileName().toString();
	}

	public static void specifyWorld(String worldName) throws FileNotFoundException
	{
		// Load worlds directory
		Path worldsDir = ksDir.resolve("Worlds");
		if (!Files.exists(worldsDir))
			throw new FileNotFoundException("Missing Worlds directory.");

		// Attempt to set world
		worldFolder = worldsDir.resolve(worldName);
		if (!Files.exists(worldFolder.resolve("Map.bin")))
		{
			worldFolder = null;
			throw new FileNotFoundException(worldName + " is not a valid world.");
		}
	}

	private static void getMapFile() throws FileNotFoundException
	{
		if (mapFile == null)
		{
			// Load map file
			Console.printString("Loading map file...");
			mapFile = worldFolder.resolve("Map.bin");
			if (!Files.exists(mapFile))
				throw new FileNotFoundException("Unable to locate file " + mapFile);
		}
	}

	public static boolean backupMapFile(Scanner input) throws FileNotFoundException
	{
		// Make sure we have a map loaded
		if (mapFile == null)
			getMapFile();

		// Back it up (the backup is also the file we'll be reading in)
		originalMapFile = worldFolder.resolve("MapBackup.rando.bin");
		if (!Files.exists(originalMapFile))
		{
			try
			{
				Files.copy(mapFile, originalMapFile);
			} catch (IOException e)
			{
				if (UserInput.getBooleanInput(input, "Unable to make a backup of the map file. The original map will be overwritten. Are you sure you wish to continue?"))
					originalMapFile = mapFile;
				else
					return false;
			}
		}
		return true;
	}

	public static ArrayList<String> backedUpWorlds() throws IOException
	{
		// Load worlds directory
		Path worldsDir = ksDir.resolve("Worlds");
		if (!Files.exists(worldsDir))
			throw new FileNotFoundException("Missing Worlds directory.");

		// Collect worlds
		ArrayList<String> result = new ArrayList<String>();
		for (Path world : Files.newDirectoryStream(worldsDir))
		{
			// If the world is not a folder, automatically ignore
			if (!Files.isDirectory(world))
				continue;
			// If the world contains a rando backup file, it's backed up
			Path mapBackup = world.resolve("MapBackup.rando.bin");
			if (Files.exists(mapBackup))
				result.add(world.getFileName().toString());
		}
		return result;
	}

	public static boolean restoreMapFile(String worldName) throws FileNotFoundException
	{
		// Load worlds directory
		Path worldsDir = ksDir.resolve("Worlds");
		if (!Files.exists(worldsDir))
			throw new FileNotFoundException("Missing Worlds directory.");

		// Load paths
		Path world = worldsDir.resolve(worldName);
		Path originalMap = world.resolve("MapBackup.rando.bin");
		Path mapLocation = world.resolve("Map.bin");

		// Attempt to restore map file
		try
		{
			// Successfully restored map
			Files.move(originalMap, mapLocation, StandardCopyOption.REPLACE_EXISTING);
			return true;
		}
		catch(IOException e)
		{
			// Failed to restore map
			return false;
		}
	}

	public static KSMap readMap() throws Exception
	{
		if (mapFile == null)
			getMapFile();
		Path file = (originalMapFile != null) ? originalMapFile : mapFile;
		try
		{
			return new KSMap(file);
		} catch (NoSuchFileException e)
		{
			throw new Exception("Unable to open file " + file);
		} catch (IOException e)
		{
			throw new Exception("IOException reading file " + file);
		} catch (Exception e)
		{
			throw new Exception("Unable to parse map file: " + e.getMessage());
		}
	}

	public static void writeMap(KSMap map) throws Exception
	{
		try
		{
			if (mapFile == null)
				getMapFile();
			map.saveToFile(mapFile);
		} catch (NoSuchFileException e)
		{
			throw new Exception("Unable to open file " + mapFile);
		} catch (IOException e)
		{
			throw new Exception("IOException writing to file " + mapFile);
		} catch (Exception e)
		{
			throw new Exception("Unable to write screen data: " + e.getMessage());
		}
	}

	public static void launchKS() throws IOException
	{
		Runtime run = Runtime.getRuntime();
		Path exe = getExecutable(ksDir);
		if (exe == null)
			throw new FileNotFoundException("Unable to locate Knytt Stories executable.");
		run.exec(exe.toString());
	}
}
