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
	
	public static void printWarning(String str)
	{
		System.out.println("WARNING: \033[33m" + str + "\033[0m");
	}
	
	public static void printRed(String err)
	{
		System.out.println("\033[31m" + err + "\033[0m");
	}
	
	public static void printError(String err)
	{
		printRed("ERROR: " + err);
	}
}
