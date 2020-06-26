package core;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import util.Util;

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
	
	public static int getInputFromList(Scanner input, String prompt, String unit, ArrayList<String> list)
	{
		String inputStr;
		int offset = 0;
		int pageSize = 10;
		int numOptions = list.size();
		boolean showDisplay = true;
		while (true)
		{
			// Display page of options
			if (showDisplay)
			{
				Console.printString((offset > 0)
						? "+-- U: scroll up ---------------------------------------------------------------"
						: "+-------------------------------------------------------------------------------");
				for (int i = offset; i < offset + pageSize && i < list.size(); i++)
					Console.printf("|%4d. %s\n", i, list.get(i));
				Console.printString((offset + pageSize < numOptions)
						? "+-- D: scroll down -------------------------------------------------------------"
						: "+-------------------------------------------------------------------------------");
				showDisplay = false;
			}
			Console.printString(prompt + " Enter " + unit + " ID. Enter a string to search or ##-## to see all " + unit + "s in a range. Leave blank to cancel.");
			
			// Get user input
			inputStr = input.nextLine();
			if (inputStr.isEmpty())
				return -1; // TODO catch
			
			// Change page
			if (inputStr.toLowerCase().equals("u"))
			{
				offset -= pageSize;
				if (offset < 0)
					offset = 0;
				showDisplay = true;
				continue;
			}
			else if (inputStr.toLowerCase().equals("d"))
			{
				offset += pageSize;
				if (offset + pageSize > numOptions)
					offset = numOptions - pageSize;
				showDisplay = true;
				continue;
			}
			
			// input = ID (return)
			Integer choice = Util.stringToInteger(inputStr);
			if (choice != null)
			{
				if (choice >= 0 && choice < numOptions)
					return choice;
				else
					Console.printString(choice + " is out of the range of available options.");
				continue;
			}
			
			// input = ##-## (range)
			int[] range = Util.stringToRange(inputStr);
			if (range != null)
			{
				if (range[0] < 0 || range[0] >= numOptions ||
						range[1] < 0 || range[1] >= numOptions ||
						range[1] < range[0])
					Console.printString(inputStr + " is not a valid range.");
				else
					for (int i = range[0]; i <= range[1]; i++)
						Console.printString("\t" + i + ". " + list.get(i));
				continue;
			}
			
			// input = string (search)
			ArrayList<Integer> matches = Util.keywordMatch(list, inputStr.split("\\s+"));
			if (matches.size() == 0)
				Console.printString("Found no " + unit + "s matching \"" + inputStr + "\"");
			else
			{
				Console.printString(unit + "s matching \"" + inputStr + "\"");
				for (int i : matches)
					Console.printString("\t" + i + ". " + list.get(i));
			}
		}
	}
}
