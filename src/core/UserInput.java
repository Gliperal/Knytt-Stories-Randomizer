package core;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class UserInput
{
	public static char getCharInput(Scanner input, String prompt, char[] chars, char ifBlank)
	{
		for (int i = 0; i < chars.length; i++)
			chars[i] = Character.toUpperCase(chars[i]);
		
		while (true)
		{
			if (prompt != null)
				Console.printString(prompt);
			String inputStr = input.nextLine().toUpperCase();
			if (inputStr.isEmpty())
				return ifBlank;
			char inputChar = inputStr.charAt(0);
			if (inputStr.length() == 1)
				for (char c : chars)
					if (c == inputChar)
						return c;
			
			Console.putString("Please respond with ");
			for (int i = 0; i < chars.length; i++)
			{
				if (i != 0)
					Console.putString(", ");
				if (i == chars.length - 1)
					Console.putString("or ");
				Console.putString(Character.toString(chars[i]));
			}
			Console.printString(".");
		}
	}
	
	public static boolean getBooleanInput(Scanner input, String prompt)
	{
		while (true)
		{
			Console.printString(prompt + " [Y/N]");
			switch(input.nextLine().toLowerCase())
			{
			case "y":
			case "yes":
				return true;
			case "n":
			case "no":
				return false;
			default:
				Console.printString("Please respond with either Y or N.");
				continue;
			}
		}
	}
	
	public static Long getSeedInput(Scanner input, String prompt)
	{
		// Get user input
		Console.printString(prompt);
		String rawSeed = input.nextLine();
		
		// Empty string is a null seed (will be generated later)
		if (rawSeed.isEmpty())
			return null;
		
		// Numerical string is a raw seed
		try
		{
			return Long.parseLong(rawSeed.substring(1));
		}
		catch (NumberFormatException e) {}
		
		// Convert string seed into a long
		long seed = 0;
		for (char c : rawSeed.toCharArray())
		{
			seed *= 127;
			seed += c;
		}
		
		// Generate a seed for the map based on the user's seed.
		// (so that short strings won't generate low seed numbers)
		Random rand = new Random(seed);
		long mapSeed = rand.nextLong();
		return mapSeed;
	}
	
	public static void waitForEnter(Scanner input, String prompt)
	{
		Console.printString(prompt);
		input.nextLine();
	}
	
	public static int getInputFromList(Scanner input, String prompt, ArrayList<String> list)
	{
		String inputStr;
		int offset = 0;
		int pageSize = 10;
		int numOptions = list.size();
		while (true)
		{
			Console.printString(prompt);
			if (offset > 0)
				Console.printString("u\tScroll up");
			for (int i = offset; i < offset + pageSize && i < list.size(); i++)
				Console.printString(i + "\t" + list.get(i));
			if (offset + pageSize < numOptions)
				Console.printString("d\tScroll down");
			
			inputStr = input.nextLine();
			if (inputStr.toLowerCase().equals("u"))
			{
				offset -= 5;
				if (offset < 0)
					offset = 0;
			}
			else if (inputStr.toLowerCase().equals("d"))
			{
				offset += 5;
				if (offset + pageSize > numOptions)
					offset = numOptions - pageSize;
			}
			else
			{
				try
				{
					int choice = Integer.parseInt(inputStr);
					if (choice < 0 || choice > numOptions - 1)
						Console.printString(choice + "is out of the range of available options.");
					return choice;
				}
				catch (NumberFormatException e)
				{
					Console.printString("Please enter a number, or u or d to scroll");
				}
			}
		}
	}
}
