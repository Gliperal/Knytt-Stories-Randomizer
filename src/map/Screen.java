package map;

import java.io.IOException;
import java.util.ArrayList;

import core.ObjectGroup;
import util.Util;

public class Screen
{
	@SuppressWarnings("serial")
	public class NotAScreenException extends Exception {}

	private String location;
	private byte[] data;

	public Screen(String location, byte[] data) throws NotAScreenException
	{
		if (!location.matches("x-?\\d+y-?\\d+"))
			throw new NotAScreenException();
		this.location = location;
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
		System.out.println("Screen " + location);
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

	public void writeTo(MMFBinaryArray mapData) throws IOException
	{
		mapData.set(location, data);
	}

	public void collectObjects(ObjectGroup objects, boolean includeEmpty)
	{
		for (int layer = 4; layer < 8; layer++)
		{
			int bankOffset = layer*500 - 750;
			int objOffset = layer*500 - 1000;
			for (int tile = 0; tile < 250; tile++)
			{
				byte bank = data[bankOffset + tile];
				byte obj = data[objOffset + tile];
				if (obj != 0 || includeEmpty)
					objects.add(bank, obj);
			}
		}
	}

	public void exportObjects(ArrayList<Integer> list, boolean includeEmpty)
	{
		for (int layer = 4; layer < 8; layer++)
		{
			int bankOffset = layer*500 - 750;
			int objOffset = layer*500 - 1000;
			for (int tile = 0; tile < 250; tile++)
			{
				byte bank = data[bankOffset + tile];
				byte obj = data[objOffset + tile];
				if (obj != 0 || includeEmpty)
					list.add(Util.combineBankObj(bank, obj));
			}
		}
	}

	public int importObjects(int[] arr, int offset, boolean includeEmpty)
	{
		for (int layer = 4; layer < 8; layer++)
		{
			int bankOffset = layer*500 - 750;
			int objOffset = layer*500 - 1000;
			for (int tile = 0; tile < 250; tile++)
			{
				if (data[objOffset + tile] != 0 || includeEmpty)
				{
					data[bankOffset + tile] = Util.separateBank(arr[offset]);
					data[objOffset + tile] = Util.separateObj(arr[offset]);
					offset++;
				}
			}
		}
		return offset;
	}

	public byte getMusic()
	{
		return data[3004];
	}

	public void setMusic(byte value)
	{
		data[3004] = value;
	}

	public int countObject(byte bank, byte obj)
	{
		int count = 0;
		for (int layer = 4; layer < 8; layer++)
		{
			int bankOffset = layer*500 - 750;
			int objOffset = layer*500 - 1000;
			for (int tile = 0; tile < 250; tile++)
			{
				if ((int)data[bankOffset + tile] == bank && (int)data[objOffset + tile] == obj)
					count++;
			}
		}
		return count;
	}

	public String toString()
	{
		return "Screen[" + location + "]";
	}
}
