package util;

public class Console
{
	public static final int MAX_LENGTH = 79;

	public static void putString(String string)
	{
		while (string.length() > MAX_LENGTH)
		{
			int i = string.indexOf('\n');
			if (i == -1 || i > MAX_LENGTH)
				i = Util.lastIndexOf(string, " \t", MAX_LENGTH + 1);
			if (i == -1)
				i = Util.firstIndexOf(string, " \t");
			if (i == -1)
				break;
			System.out.println(string.substring(0, i));
			string = string.substring(i + 1);
		}
		System.out.print(string);
	}

	public static void printString(String str)
	{
		putString(str + "\n");
	}

	public static void printf(String format, Object... args)
	{
		putString(String.format(format, args));
	}

	public static void printWarning(String str)
	{
		printString("WARNING: " + str);
	}

	public static void printRed(String err)
	{
		printString(err);
	}

	public static void printError(String err)
	{
		printRed("ERROR: " + err);
	}

	public static void debug(String str, int foo)
	{
		System.out.println("DEBUG>>> " + str);
	}
}
