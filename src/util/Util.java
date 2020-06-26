package util;

import java.util.ArrayList;

public class Util
{
	public static ArrayList<Integer> keywordMatch(ArrayList<String> list, String[] keywords)
	{
		for (int i = 0; i < keywords.length; i++)
			keywords[i] = keywords[i].toLowerCase();
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
	
	public static String trimStringForPrinting(String msg)
	{
		// replace newlines
		msg = msg.replaceAll("\n", "   ").trim();
		// trim if oversize
		if (msg.length() > 32)
			msg = msg.substring(0, 29).trim() + "...";
		return msg;
	}
	
	public static void displayListConsicesly(ArrayList<String> list, int maxLines, int maxLinesIfNotAll)
	{
		int size = list.size();
		int displaySize = (size <= maxLines) ? size : maxLinesIfNotAll;
		for (int i = 0; i < displaySize; i++)
			System.out.println("\t" + list.get(i));
		if (displaySize < size)
			System.out.println("\t+ " + (size - displaySize) + " more");
	}
	
	public static int combineBankObj(int bank, int obj)
	{
		return ((bank & 255) << 8) | (obj & 255);
	}
	
	public static byte separateBank(int object)
	{
		return (byte) (object >> 8);
	}
	
	public static byte separateObj(int object)
	{
		return (byte) object;
	}
	
	public static int[] mergeArrays(int[] a, int[] b)
	{
		int alen = a.length;
		int blen = b.length;
		int rlen = alen + blen;
		int[] ret = new int[rlen];
		for (int i = 0; i < alen; i++)
			ret[i] = a[i];
		for (int i = 0; i < blen; i++)
			ret[alen + i] = b[i];
		return ret;
	}
	
	public static String millisecondsToTimeString(long time)
	{
		if (time < 1000)
			return time + " milliseconds";
		time /= 1000;
		if (time < 60)
			return time + ((time == 1) ? " second" : " seconds");
		time /= 60;
		if (time < 60)
			return time + ((time == 1) ? " minute" : " minutes");
		time /= 60;
		if (time < 24)
			return time + ((time == 1) ? " hour" : " hours");
		time /= 24;
		if (time < 365)
			return time + ((time == 1) ? " day" : " days");
		time /= 365;
		return time + ((time == 1) ? " year" : " years");
	}
	
	public static Integer stringToInteger(String s)
	{
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}
	
	public static int[] stringToRange(String str)
	{
		String[] split = str.split("-");
		if (split.length != 2)
			return null;
		int start = Integer.parseInt(split[0]);
		int end = Integer.parseInt(split[1]);
		return new int[] {start, end};
	}
}
