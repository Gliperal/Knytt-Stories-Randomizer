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

import core.ObjectClass;
import core.ObjectShuffle;
import core.RandoKey;

public class KSMap
{
	// The byte sequence 0 -66 11 0 0 denotes end of header
	// public static final String endOfHeader = "\u0000\uFFBE\u000B\u0000\u0000";
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
	
	@Deprecated
	/**
	 * Uses the true random algorithm
	 */
	public void randomize(RandoKey randoKey, Random rand)
	{
		for(Screen s : screens)
			s.randomize(randoKey, rand);
	}

	@Deprecated
	/**
	 * Uses the transform algorithm
	 */
	public void randomize(RandoKey randoKey)
	{
		for(Screen s : screens)
			s.randomize(randoKey);
	}
	
	public void printScreens()
	{
		for(Screen s : screens)
			s.println();
	}

	public ObjectClass allObjects()
	{
		ObjectClass objects = new ObjectClass('.', null);
		for(Screen s : screens)
			s.collectObjects(objects);
		return objects;
	}
	
	public void populateShuffle(ObjectShuffle shuffle)
	{
		for(Screen s : screens)
			s.populateShuffle(shuffle);
	}
	
	public void shuffle(ObjectShuffle shuffle)
	{
		for(Screen s : screens)
			s.shuffle(shuffle);
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
}
