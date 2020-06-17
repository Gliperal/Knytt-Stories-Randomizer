package core;

import java.io.BufferedReader;
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
import util.Util;

public class KSFiles
{
	private static Path ksDir = null;
	private static Path worldFolder = null;
	private static Path mapFile = null;
	private static Path originalMapFile = null;
	
	public static boolean loadKSDirectory()
	{
		Path workingDir = Paths.get(System.getProperty("user.dir"));
		
		// If the jar is being run from a subfolder of the KS directory
		System.out.print("Checking local files...");
		ksDir = workingDir.getParent();
		if (isKSDirectory(ksDir))
		{
			System.out.println(" Yes.");
			return true;
		}
		System.out.println(" No.");
		
		// If there is a symbolic link in the working directory called "KS"
		System.out.print("Checking for a KS symbolic link...");
		ksDir = workingDir.resolve("KS");
		if (isKSDirectory(ksDir))
		{
			System.out.println(" Yes.");
			return true;
		}
		System.out.println(" No.");
		
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
					System.out.println(" Yes.");
					return true;
				}
			} catch (Exception e) {}
		}
		System.out.println(" No.");
		
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
					System.out.println(" Yes.");
					return true;
				}
			} catch (IOException e) {}
		}
		System.out.println(" No.");
		return false;
	}
	
	private static boolean isKSDirectory(Path ksDir)
	{
		if (!Files.isDirectory(ksDir))
			return false;
		return
				Files.exists(ksDir.resolve("Knytt Stories.exe")) ||
				Files.exists(ksDir.resolve("Knytt Stories Plus.exe")) ||
				Files.exists(ksDir.resolve("knytt stories ex.exe")) ||
				Files.exists(ksDir.resolve("Knytt Stories Speedrun Edition 0.2.2.exe")) ||
				Files.exists(ksDir.resolve("Knytt Stories Speedrun Edition 0.3.1.exe"));
	}
	
	// TODO replace print statements with exception throwing
	public static String haveUserSelectWorld(Scanner input, String prompt)
	{
		// Load worlds directory
		Path worldsDir = ksDir.resolve("Worlds");
		if (!Files.exists(worldsDir))
		{
			System.out.println("Missing Worlds directory.");
			return null;
		}
		
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
			System.out.println("IOException loading worlds: " + e.getMessage());
			return null;
		}
		
		// Get user choice
		int worldID;
		while (true)
		{
			System.out.print(prompt);
			System.out.println(" Enter world ID, or enter a string to search.");
			// TODO Enter ##-## to see all the worlds in a range
			String inputStr = input.nextLine();
			try
			{
				worldID = Integer.parseInt(inputStr);
				if (worldID >= 0 && worldID < worldStrings.size())
					break;
				System.out.println(worldID + "is out of the range of available options.");
			}
			catch (NumberFormatException e)
			{
				String[] keywords = inputStr.split("\\s+");
				ArrayList<Integer> matches = Util.keywordMatch(worldStrings, keywords);
				if (matches.size() == 0)
					System.out.println("No worlds found for search query \"" + inputStr + "\"");
				else
				{
					System.out.println("Found " + matches.size() + " worlds matching \"" + inputStr + "\":");
					for (int i : matches)
						System.out.println("   " + i + "\t" + worldStrings.get(i));
				}
			}
		}
		worldFolder = worlds.get(worldID);
		return worldFolder.getFileName().toString();
	}
	
	public static void specifyWorld(String worldName) throws Exception
	{
		// Load worlds directory
		Path worldsDir = ksDir.resolve("Worlds");
		if (!Files.exists(worldsDir))
			throw new Exception("Missing Worlds directory.");
		
		// TODO Do a thing...
		worldFolder = worldsDir.resolve(worldName);
		// TODO Throw exception if world doesn't exist
	}
	
	private static Path getMapFile()
	{
		if (mapFile == null)
		{
			// Load map file
			System.out.println("Loading map file...");
			mapFile = worldFolder.resolve("Map.bin");
			if (!Files.exists(mapFile))
			{
				System.out.println("Error: Unable to locate file " + mapFile);
				return null;
			}
		}
		return mapFile;
	}
	
	public static boolean backupMapFile(Scanner input)
	{
		// Make sure we have a map loaded
		if (mapFile == null && getMapFile() == null)
			return false;
		
		// Back it up (the backup is also the file we'll be reading in)
		originalMapFile = worldFolder.resolve("MapBackup.rando.bin");
		if (!Files.exists(originalMapFile))
		{
			try
			{
				Files.copy(mapFile, originalMapFile);
			} catch (IOException e)
			{
				if (UserInput.getBooleanInput(input, "Unable to make a backup of the map file. The original map will be overwritten. Are you sure you wish to continue? [Y/N]"))
					originalMapFile = mapFile;
				else
					return false;
			}
		}
		return true;
	}
	
	public static ArrayList<String> backedUpWorlds()
	{
		// Load worlds directory
		Path worldsDir = ksDir.resolve("Worlds");
		if (!Files.exists(worldsDir))
		{
			System.out.println("Missing Worlds directory.");
			return null;
		}
		
		// Collect worlds
		ArrayList<String> result = new ArrayList<String>();
		try
		{
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
		} catch (IOException e)
		{
			System.out.println("Unable to process Worlds directory.");
			return null;
		}
	}
	
	public static boolean restoreMapFile(String worldName)
	{
		// Load worlds directory
		Path worldsDir = ksDir.resolve("Worlds");
		if (!Files.exists(worldsDir))
		{
			System.out.println("Missing Worlds directory.");
			return false;
		}
		
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
	
	public static KSMap readMap()
	{
		try
		{
			return new KSMap(originalMapFile);
		} catch (NoSuchFileException e)
		{
			System.out.println("Error: Unable to open file " + mapFile);
			return null;
		} catch (IOException e)
		{
			System.out.println("Error: IOException reading file " + mapFile);
			return null;
		} catch (Exception e)
		{
			System.out.println("Error parsing map file: " + e.getMessage());
			return null;
		}
	}
	
	public static boolean writeMap(KSMap map)
	{
		try
		{
			map.saveToFile(mapFile);
			return true;
		} catch (NoSuchFileException e)
		{
			System.out.println("Error: Unable to open file " + mapFile);
			return false;
		} catch (IOException e)
		{
			System.out.println("Error: IOException writing to file " + mapFile);
			return false;
		} catch (Exception e)
		{
			System.out.println("Error writing screen data: " + e.getMessage());
			return false;
		}
	}
}
