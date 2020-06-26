package core;

public class Console
{
	public static void putString(String string)
	{
		System.out.print(string);
	}
	
	public static void printString(String str)
	{
		System.out.println(str);
	}
	
	public static void printf(String format, Object... args)
	{
		System.out.printf(format, args);
	}
	
	public static void printWarning(String str)
	{
		System.out.println("WARNING: " + str);
	}
	
	public static void printRed(String err)
	{
		System.out.println(err);
	}
	
	public static void printError(String err)
	{
		printRed("ERROR: " + err);
	}
}
