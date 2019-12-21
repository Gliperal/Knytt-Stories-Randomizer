package util;

public class Util
{
	public static byte[] truncate(byte[] array, int newLength)
	{
		if (newLength >= array.length)
			return array;
		byte[] newArray = new byte[newLength];
		for (int i = 0; i < newLength; i++)
			newArray[i] = array[i];
		return newArray;
	}
}
