package map;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

public class MMFBinaryArray
{
	private HashMap<String, byte[]> data;

	public MMFBinaryArray()
	{
		this.data = new HashMap<String, byte[]>();
	}

	public MMFBinaryArray(Path file) throws IOException
	{
		this();

		// Setup GZip input stream
		InputStream is = Files.newInputStream(file);
		byte[] ba = IOUtils.toByteArray(is);
		ByteArrayInputStream bis = new ByteArrayInputStream(ba);
		GZIPInputStream gis = new GZIPInputStream(bis);

		// Read data, one chunk at a time
		while(true)
		{
			// Read screen header
			String header = readScreenHeader(gis);
			if (header == null)
				break;

			// Read screen size
			int screenSize = readScreenSize(gis);

			// Read screen data
			byte[] screenData = readScreenData(gis, screenSize);
			if (screenData == null)
				continue;

			// Package information into screen
			data.put(header, screenData);
		}

		// Close resources
		bis.close();
		is.close();
		gis.close();
	}

	private static String readScreenHeader(GZIPInputStream gis) throws IOException
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
		}
	}

	private static int readScreenSize(GZIPInputStream gis) throws IOException
	{
		byte[] buffer = new byte[1];
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
				throw new EOFException();

			// two zeros in a row denotes end of size
			if (buffer[0] == 0)
			{
				if (lastByteWasZero)
					break;
				lastByteWasZero = true;
			}
			else
				lastByteWasZero = false;

			// Add byte to the size (little endian)
			int unsignedValue = (buffer[0] < 0) ? buffer[0] + 256 : buffer[0];
			size += Math.pow(256, currentPlace) * unsignedValue;
			currentPlace++;
		}
		return size;
	}

	private static byte[] readScreenData(GZIPInputStream gis, int size) throws IOException
	{
		byte[] screenData = new byte[size];
		int bytesSoFar;
		int bytesRead;

		// Read screen data
		bytesSoFar = 0;
		while (bytesSoFar < size)
		{
			bytesRead = gis.read(screenData, bytesSoFar, size - bytesSoFar);
			if (bytesRead == -1)
				throw new EOFException();
			bytesSoFar += bytesRead;
		}
		return screenData;
	}

	private void writeScreenHeader(GZIPOutputStream gos, String header) throws IOException
	{
		for (int i = 0; i < header.length(); i++)
			gos.write(header.charAt(i));
		gos.write(0);
	}

	private void writeScreenSize(GZIPOutputStream gos, int size) throws IOException
	{
		while (size > 0)
		{
			gos.write(size % 256);
			size /= 256;
		}
		gos.write(0);
		gos.write(0);
	}

	public void writeToFile(Path file) throws IOException
	{
		// Setup GZip for map writing
		OutputStream os = Files.newOutputStream(file);
		GZIPOutputStream gos = new GZIPOutputStream(os);

		// Write data one screen at a time
		for (Entry<String, byte[]> screen : data.entrySet())
		{
			// Write header
			writeScreenHeader(gos, screen.getKey());

			// Write screen size
			byte[] screenData = screen.getValue();
			writeScreenSize(gos, screenData.length);

			// Write screen data
			gos.write(screenData);
		}

		// Finalize
		gos.close();
	}

	public byte[] get(String key)
	{
		return data.get(key);
	}

	public void set(String key, byte[] value)
	{
		data.put(key, value);
	}

	public Set<String> keys()
	{
		return data.keySet();
	}
}
