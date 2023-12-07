package map;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import core.ObjectGroup;

public class KSMap
{
	private ArrayList<Screen> screens;
	private HashMap<String, String> additionalInfo;

	public KSMap()
	{
		screens = new ArrayList<Screen>();
		additionalInfo = new HashMap<String, String>();
	}

	public KSMap(Path mapFile) throws IOException
	{
		this();

		// Unpack binary file
		MMFBinaryArray mapData = new MMFBinaryArray(mapFile);

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
		MMFBinaryArray mapData = new MMFBinaryArray(mapFile);

		// Write data one screen at a time
		for (Screen screen : screens)
			screen.writeTo(mapData);

		// Write additional information
		for (Entry<String, String> entry : additionalInfo.entrySet())
		{
			String info = entry.getValue();
			int size = info.length();
			byte[] bytes = new byte[size < 3006 ? 3006 : size + 1];
			for (int i = 0; i < size; i++)
				bytes[i] = (byte) info.charAt(i);
			mapData.set(entry.getKey(), bytes);
		}

		// Finalize
		mapData.writeToFile(mapFile);
	}

	public void addAdditionalInfo(String key, String info)
	{
		additionalInfo.put(key, info);
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

	public ObjectGroup allObjects(boolean includeEmpty)
	{
		ObjectGroup objects = new ObjectGroup();
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

	public void findObject(byte bank, byte obj)
	{
		for(Screen s : screens)
		{
			int count = s.countObject(bank, obj);
			if (count > 0)
				// TODO return something instead
				System.out.println(count + " on screen " + s.toString());
		}
	}
}
