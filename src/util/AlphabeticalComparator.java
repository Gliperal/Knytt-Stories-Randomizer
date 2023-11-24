package util;

import java.util.Comparator;

public class AlphabeticalComparator implements Comparator<String>
{
	public int compare(String o1, String o2)
	{
		return o1.compareTo(o2);
	}
}
