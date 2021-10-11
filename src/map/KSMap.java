package map;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

import core.ObjectGroup;

public class KSMap
{
	private ArrayList<Screen> screens;
	
	public KSMap()
	{
		screens = new ArrayList<Screen>();
	}
	
	private static String readScreenHeader(GZIPInputStream gis) throws Exception
	{
		byte[] buffer = new byte[1];
		int bytesRead;
		String header = "";
		while (true)
		{
			// Read next character
			bytesRead = gis.read(buffer);
			if (bytesRead == -1)
				return null;
			
			// null character denotes end of header
			if (buffer[0] == 0)
				return header;
			
			// Append character
			header += (char) buffer[0];
			if (header.length() > 20) // No header should realistically be this long
				throw new Exception("Unable to parse headers.");
		}
	}
	
	private static boolean discardBytes(GZIPInputStream gis, int bytesToDiscard) throws IOException
	{
		byte[] dump = new byte[1024];
		int bytesRead;
		while (bytesToDiscard > 1024)
		{
			bytesRead = gis.read(dump, 0, 1024);
			if (bytesRead == -1)
				return false;
			bytesToDiscard -= bytesRead;
		}
		while (bytesToDiscard > 0)
		{
			bytesRead = gis.read(dump, 0, bytesToDiscard);
			if (bytesRead == -1)
				return false;
			bytesToDiscard -= bytesRead;
		}
		return true;
	}
	
	private static byte[] readScreenData(GZIPInputStream gis) throws Exception
	{
		byte[] screenData = new byte[3006];
		byte[] buffer = new byte[1];
		int bytesSoFar;
		int bytesRead;
		
		// Read size of screen data
		int size = 0;
		int currentPlace = 0;
		boolean lastByteWasZero = false;
		while (true)
		{
			// Read next character
			bytesRead = gis.read(buffer);
			if (bytesRead == -1)
				throw new Exception("Unexpected end of file (2).");
			
			// two zeros in a row denotes end of size
			if (buffer[0] == 0)
			{
				if (lastByteWasZero)
					break;
				lastByteWasZero = true;
			}
			else
				lastByteWasZero = false;
			
			// Add to the size
			int unsignedValue = (buffer[0] < 0) ? buffer[0] + 256 : buffer[0];
			size += Math.pow(256, currentPlace) * unsignedValue;
			currentPlace++;
		}
		
		// Evaluate the size: 3006 is the normal size of a screen
		if (size != 3006)
		{
			// Discard erroneous screens
			boolean status = discardBytes(gis, size);
			if (!status)
				throw new Exception("Unexpected end of file.");
			return null;
		}
		else
		{
			// Read screen data
			bytesSoFar = 0;
			while (bytesSoFar < 3006)
			{
				bytesRead = gis.read(screenData, bytesSoFar, 3006 - bytesSoFar);
				if (bytesRead == -1)
					throw new Exception("Unexpected end of file.");
				bytesSoFar += bytesRead;
			}
			return screenData;
		}
	}
	
	public KSMap(Path mapFile) throws Exception
	{
		screens = new ArrayList<Screen>();
		
		// Setup GZip for map reading
		InputStream is = Files.newInputStream(mapFile);
		byte[] ba = IOUtils.toByteArray(is);
		ByteArrayInputStream bis = new ByteArrayInputStream(ba);
		GZIPInputStream gis = new GZIPInputStream(bis);
		
		// Read map data, one screen at a time
		String header;
		byte[] screenData;
		while(true)
		{
			// Read screen header (and write screen header)
			header = readScreenHeader(gis);
			if (header == null)
				break;
			
			// Read screen data (will always be the next 3006 bytes)
			screenData = readScreenData(gis);
			if (screenData == null)
				continue;
			
			// Package information into screen
			Screen s = new Screen(header, screenData);
			screens.add(s);
		}
		
		// Close resources
		bis.close();
		is.close();
		gis.close();
	}
	
	public void saveToFile(Path mapFile) throws IOException
	{
		// Setup GZip for map writing
		OutputStream os = Files.newOutputStream(mapFile);
		GZIPOutputStream gos = new GZIPOutputStream(os);
		
		// Write data one screen at a time
		for (Screen screen : screens)
			screen.writeTo(gos);
		
		// Finalize
		gos.close();
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
		Map<Byte, Byte> musicRando = new HashMap<Byte, Byte>();
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
