package map;

import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import util.Console;

public class KSMap
{
	private ArrayList<Screen> screens;
	private HashMap<String, byte[]> additionalInfo;

	public KSMap()
	{
		screens = new ArrayList<Screen>();
		additionalInfo = new HashMap<String, byte[]>();
	}

	public KSMap(Path mapFile) throws IOException
	{
		this();

		// Unpack binary file
		MMFBinaryArray mapData = new MMFBinaryArray(mapFile, 3006);
		if (mapData.countTruncatedScreens() > 0)
			Console.printWarning("One or more oversize screens were truncated.");

		// Read map data, one screen at a time
		for (String key : mapData.keys())
		{
			try
			{
				// Package information into screen
				Screen s = new Screen(key, mapData.get(key));
				screens.add(s);
			}
			catch (Screen.NotAScreenException e) {}
		}
	}

	public void saveToFile(Path mapFile) throws IOException
	{
		MMFBinaryArray mapData = new MMFBinaryArray();

		// Write data one screen at a time
		for (Screen screen : screens)
			screen.writeTo(mapData);

		// Write additional information
		for (Entry<String, byte[]> entry : additionalInfo.entrySet())
			mapData.set(entry.getKey(), entry.getValue());

		// Finalize
		mapData.writeToFile(mapFile);
	}

	public void addAdditionalInfo(String key, byte[] info)
	{
		for (int i = 1;; i++)
		{
			String incrementedKey = i > 1 ? key + " " + i : key;
			additionalInfo.put(incrementedKey, Arrays.copyOf(info, 3006));
			if (info.length <= 3006)
				break;
			info = Arrays.copyOfRange(info, 3006, info.length);
		}
	}

	public void addAdditionalInfo(String key, String info)
	{
		addAdditionalInfo(key, info.getBytes());
	}

	public boolean addAdditionalInfo(String key, byte[] info, boolean truncate)
	{
		if (info.length > 3006 && !truncate)
			return false;
		additionalInfo.put(key, Arrays.copyOf(info, 3006));
		return true;
	}

	public boolean addAdditionalInfo(String key, String info, boolean truncate)
	{
		if (info.length() >= 3006)
		{
			if (!truncate)
				return false;
			info = info.substring(0, 3002) + "...";
		}
		additionalInfo.put(key, Arrays.copyOf(info.getBytes(), 3006));
		return true;
	}

	public void clearAdditionalInfo(String key)
	{
		additionalInfo.remove(key);
	}

	public void printScreens()
	{
		for(Screen s : screens)
			s.println();
	}

	public ObjectPattern allObjects(boolean includeEmpty)
	{
		ObjectPattern objects = new ObjectPattern();
		for(Screen s : screens)
			s.collectObjects(objects, includeEmpty);
		return objects;
	}

	/**
	 * Creates an array containing the integer representation of every object in the level, in the order they are stored in the file.
	 * @param includeEmpty True if the empty space object (0:0) should be included in the list. As many maps have a lot of empty space, this can significantly increase memory use.
	 * @return An array of every object contained in the level.
	 */
	public int[] exportObjects(boolean includeEmpty)
	{
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(Screen s : screens)
			s.exportObjects(list, includeEmpty);
		int len = list.size();
		int[] ret = new int[len];
		for (int i = 0; i < len; i++)
			ret[i] = list.get(i);
		return ret;
	}

	/**
	 * Replaces all the objects in the level with those from an array. Ideally this array should match one from a call to {@link #exportObjects(boolean) exportObjects}, using the same value of includeEmpty.
	 * @param arr An array of objects to replace the existing objects in the level.
	 * @param includeEmpty True if empty space (0:0) should be considered an object.
	 * @throws ArrayIndexOutOfBoundsException If arr contains less objects than exist in the level
	 */
	public void importObjects(int[] arr, boolean includeEmpty) throws ArrayIndexOutOfBoundsException
	{
		int offset = 0;
		for(Screen s : screens)
			offset = s.importObjects(arr, offset, includeEmpty);
	}

	public void randomizeMusic(Random rand)
	{
		// Collect music
		ArrayList<Byte> musics = new ArrayList<Byte>();
		for(Screen s : screens)
		{
			Byte music = s.getMusic();
			if (music != 0 && !musics.contains(music))
				musics.add(music);
		}

		// Create randomization hashmap
		ArrayList<Byte> remainingMusics = (ArrayList<Byte>) musics.clone();
		int numRemaining = remainingMusics.size();
		HashMap<Byte, Byte> musicRando = new HashMap<Byte, Byte>();
		for (Byte music : musics)
		{
			int i = rand.nextInt(numRemaining);
			musicRando.put(music, remainingMusics.remove(i));
			numRemaining--;
		}

		// Randomize the music
		for(Screen s : screens)
		{
			Byte key = s.getMusic();
			Byte value = musicRando.get(key);
			if (value == null)
				continue;
			s.setMusic(value);
		}
	}

	public ArrayList<MapObject> find(Pattern p)
	{
		ArrayList<MapObject> result = new ArrayList<MapObject>();
		for(Screen s : screens)
			s.find(p, result);
		return result;
	}

	public void hash(MessageDigest md)
	{
		// order screens before hashing?
		for (Screen s : screens)
			s.hash(md);
	}

	public byte[] hash()
	{
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			hash(md);
			return md.digest();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
}
