package map;

import java.io.IOException;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

import core.ObjectClass;
import core.ObjectShuffle;
import core.RandoKey;

public class Screen
{
	private String header;
	private byte[] data;
	// Corresponds to the byte sequence 0 190 11 0 0, or 0 [3006 in little endian] 0 0
	private static final String endOfHeader = "\u0000\uFFBE\u000B\u0000\u0000";
	
	public Screen(String location, byte[] data)
	{
		this.header = location;
		this.data = new byte[3006];
		for (int i = 0; i < 3006; i++)
			this.data[i] = data[i];
	}
	
	public byte tileAt(int layer, int x, int y)
	{
		return data[layer*250 + y*25 + x];
	}
	
	public byte[] objectAt(int layer, int x, int y)
	{
		byte obj = data[layer*500 - 1000 + y*25 + x];
		byte bank = data[layer*500 - 750 + y*25 + x];
		return new byte[] {bank, obj};
	}
	
	public void println()
	{
		System.out.println("Screen " + header);
		for(int y = 0; y < 10; y++)
		{
			for (int x = 0; x < 25; x++)
			{
				char c = ' ';
				for (int l = 0; l < 3; l++)
					if (tileAt(l, x, y) != 0)
						c = '.';
				if (tileAt(3, x, y) != 0)
					c = '#';
				for (int l = 4; l < 8; l++)
					if (objectAt(l, x, y)[1] != 0)
						c = '*';
				System.out.print(c);
			}
			System.out.println();
		}
	}
	
	/**
	 * Uses the true random algorithm
	 */
	public void randomize(RandoKey randoKey, Random rand)
	{
		// Iterate through all the tiles
		for (int layer = 4; layer < 8; layer++)
		{
			int bankOffset = layer*500 - 750;
			int objOffset = layer*500 - 1000;
			for (int tile = 0; tile < 250; tile++)
			{
				// Collect bank and object
				byte bank = data[bankOffset + tile];
				byte obj = data[objOffset + tile];
				// Send to RandoKey to randomize
				byte[] replacement = randoKey.randomize(bank, obj, rand);
				if (replacement != null)
				{
					// If valid object for randomizing, replace it
					data[bankOffset + tile] = replacement[0];
					data[objOffset + tile] = replacement[1];
				}
			}
		}
	}
	
	public void randomize(RandoKey randoKey)
	{
		// Iterate through all the tiles
		for (int layer = 4; layer < 8; layer++)
		{
			int bankOffset = layer*500 - 750;
			int objOffset = layer*500 - 1000;
			for (int tile = 0; tile < 250; tile++)
			{
				// Collect bank and object
				byte bank = data[bankOffset + tile];
				byte obj = data[objOffset + tile];
				// Send to RandoKey to randomize
				byte[] replacement = randoKey.randomize(bank, obj);
				if (replacement != null)
				{
					// If valid object for randomizing, replace it
					data[bankOffset + tile] = replacement[0];
					data[objOffset + tile] = replacement[1];
				}
			}
		}
	}
	
	public void writeTo(GZIPOutputStream gos) throws IOException
	{
		// Write header
		String combinedHeader = header + endOfHeader;
		int headerSize = combinedHeader.length();
		byte[] headerBytes = new byte[headerSize];
		for (int i = 0; i < headerSize; i++)
			headerBytes[i] = (byte) combinedHeader.charAt(i);
		gos.write(headerBytes, 0, headerSize);
		
		// Write data
		gos.write(data, 0, 3006);
	}
	
	public void collectObjects(ObjectClass objects)
	{
		for (int layer = 4; layer < 8; layer++)
		{
			int bankOffset = layer*500 - 750;
			int objOffset = layer*500 - 1000;
			for (int tile = 0; tile < 250; tile++)
			{
				byte bank = data[bankOffset + tile];
				byte obj = data[objOffset + tile];
				if (obj != 0)
					objects.add(bank, obj);
			}
		}
	}
	
	public void populateShuffle(ObjectShuffle shuffle)
	{
		for (int layer = 4; layer < 8; layer++)
		{
			int bankOffset = layer*500 - 750;
			int objOffset = layer*500 - 1000;
			for (int tile = 0; tile < 250; tile++)
			{
				byte bank = data[bankOffset + tile];
				byte obj = data[objOffset + tile];
				if (obj != 0)
					shuffle.count(bank, obj);
			}
		}
	}

	public void shuffle(ObjectShuffle shuffle)
	{
		// Iterate through all the tiles
		for (int layer = 4; layer < 8; layer++)
		{
			int bankOffset = layer*500 - 750;
			int objOffset = layer*500 - 1000;
			for (int tile = 0; tile < 250; tile++)
			{
				// Collect bank and object
				byte bank = data[bankOffset + tile];
				byte obj = data[objOffset + tile];
				// Send to ObjectShuffle to randomize
				byte[] replacement = shuffle.popShuffledItem(bank, obj);
				if (replacement != null)
				{
					// If valid object for randomizing, replace it
					data[bankOffset + tile] = replacement[0];
					data[objOffset + tile] = replacement[1];
				}
			}
		}
	}
	
	public byte getMusic()
	{
		return data[3004];
	}
	
	public void setMusic(byte value)
	{
		data[3004] = value;
	}
}
