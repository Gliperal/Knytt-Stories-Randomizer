package map;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class World
{
	public static final String BACKUPFILE = "MapBackup.bin";
	private Path folder;
	private Path mapFile = null;
	private Path originalMapFile = null;

	public World(Path worldsDir, String worldName) throws FileNotFoundException
	{
		// Load world folder
		Path worldFolder = worldsDir.resolve(worldName);
		if (!Files.exists(worldFolder.resolve("Map.bin")))
			throw new FileNotFoundException(worldName + " is not a valid world.");
		folder = worldFolder;

		// Load map file
		mapFile = folder.resolve("Map.bin");
		if (!Files.exists(mapFile))
			throw new FileNotFoundException("Unable to locate file " + mapFile);

		// Specify backup file
		originalMapFile = folder.resolve(BACKUPFILE);
	}

	public void backupMapFile() throws IOException
	{
		if (!Files.exists(originalMapFile))
			Files.copy(mapFile, originalMapFile);
	}

	public boolean mapIsBackedUp()
	{
		return Files.exists(originalMapFile);
	}

	public void restoreMapFile() throws IOException
	{
		if (!Files.exists(originalMapFile))
			return;
		Files.move(originalMapFile, mapFile, StandardCopyOption.REPLACE_EXISTING);
	}

	private KSMap readMap(Path file) throws Exception
	{
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

	public KSMap readMap() throws Exception
	{
		return readMap(mapFile);
	}

	public KSMap readOriginalMap() throws Exception
	{
		Path file = (originalMapFile != null) ? originalMapFile : mapFile;
		return readMap(file);
	}

	public void writeMap(KSMap map) throws Exception
	{
		try
		{
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
}
