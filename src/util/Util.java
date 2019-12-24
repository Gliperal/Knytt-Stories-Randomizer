package util;

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
}
