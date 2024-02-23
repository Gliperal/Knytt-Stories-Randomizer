package map;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import org.stackoverflowusers.file.WindowsShortcut;

import util.Console;
import util.UserInput;

public class KSFiles
{
	private static final String[] EXECUTABLES = {
			"Knytt Stories Plus 1.3.6 for Randomizer.exe",
			"Knytt Stories Plus.exe",
			"knytt stories ex.exe",
			"Knytt Stories Speedrun Edition 0.3.1.exe",
			"Knytt Stories Speedrun Edition 0.2.2.exe",
			"Knytt Stories.exe"
	};
	private static Path ksDir = null;
	private static Path worldsDir = null;

	public static void init() throws LoadException
	{
		if (ksDir != null)
			return;

		// Load KS directory
		loadKSDirectory();

		// Load worlds directory
		worldsDir = ksDir.resolve("Worlds");
		if (!Files.exists(worldsDir))
			throw new LoadException("Missing Worlds directory.");
	}

	private static void loadKSDirectory() throws LoadException
	{
		Path workingDir = Paths.get(System.getProperty("user.dir"));

		// If the jar is being run from a subfolder of the KS directory
		Console.printString("Checking local files...");
		ksDir = workingDir.getParent();
		if (isKSDirectory(ksDir))
		{
			Console.printString(" Yes.");
			return;
		}
		Console.printString(" No.");

		// If there is a symbolic link in the working directory called "KS"
		System.out.print("Checking for a KS symbolic link...");
		ksDir = workingDir.resolve("KS");
		if (isKSDirectory(ksDir))
		{
			Console.printString(" Yes.");
			return;
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
					return;
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
					return;
				}
			} catch (IOException e) {}
		}
		Console.printString(" No.");
		throw new LoadException();
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

	public static String haveUserSelectWorld(Scanner input, String prompt) throws IOException, LoadException
	{
		init();

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
		Path worldFolder = worlds.get(worldID);
		return worldFolder.getFileName().toString();
	}

	public static World getWorld(String worldName) throws LoadException, FileNotFoundException
	{
		init();
		return new World(worldsDir, worldName);
	}

	public static ArrayList<String> backedUpWorlds() throws IOException
	{
		// Collect worlds
		ArrayList<String> result = new ArrayList<String>();
		for (Path world : Files.newDirectoryStream(worldsDir))
		{
			// If the world is not a folder, automatically ignore
			if (!Files.isDirectory(world))
				continue;
			// If the world contains a rando backup file, it's backed up
			Path mapBackup = world.resolve(World.BACKUPFILE);
			if (Files.exists(mapBackup))
				result.add(world.getFileName().toString());
		}
		return result;
	}

	public static boolean restoreMapFile(String worldName) throws FileNotFoundException
	{
		World world = new World(worldsDir, worldName);
		try
		{
			// Successfully restored map
			world.restoreMapFile();
			return true;
		}
		catch(IOException e)
		{
			// Failed to restore map
			return false;
		}
	}

	public static void launchKS() throws IOException
	{
		Runtime run = Runtime.getRuntime();
		Path exe = getExecutable(ksDir);
		if (exe == null)
			throw new FileNotFoundException("Unable to locate Knytt Stories executable.");
		run.exec("\"" + exe.toString() + "\"");
	}
}
