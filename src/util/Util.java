package util;

import java.util.ArrayList;

// TODO delete this file too pls
public class Util
{
	@Deprecated
	// Deprecated: use Arrays.copyOf instead
	public static byte[] truncate(byte[] array, int newLength)
	{
		if (newLength >= array.length)
			return array;
		byte[] newArray = new byte[newLength];
		for (int i = 0; i < newLength; i++)
			newArray[i] = array[i];
		return newArray;
	}
	
	public static ArrayList<Integer> keywordMatch(ArrayList<String> list, String[] keywords)
	{
		ArrayList<Integer> matches = new ArrayList<Integer>();
		for (int i = 0; i < list.size(); i++)
		{
			String worldString = list.get(i);
			boolean match = true;
			for (String keyword : keywords)
				if (!worldString.toLowerCase().contains(keyword))
				{
					match = false;
					break;
				}
			if (match)
				matches.add(i);
		}
		return matches;
	}
}
