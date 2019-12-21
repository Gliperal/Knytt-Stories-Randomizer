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
			System.out.println(prompt);
			String inputStr = input.nextLine().toUpperCase();
			if (inputStr.isEmpty())
				return ifBlank;
			char inputChar = inputStr.charAt(0);
			if (inputStr.length() == 1)
				for (char c : chars)
					if (c == inputChar)
						return c;
			
			System.out.print("Please respond with ");
			for (int i = 0; i < chars.length; i++)
			{
				if (i != 0)
					System.out.print(", ");
				if (i == chars.length - 1)
					System.out.print("or ");
				System.out.print(chars[i]);
			}
			System.out.println(".");
		}
	}
	
	public static boolean getBooleanInput(Scanner input, String prompt)
	{
		while (true)
		{
			System.out.println(prompt);
			switch(input.nextLine().toLowerCase())
			{
			case "y":
			case "yes":
				return true;
			case "n":
			case "no":
				return false;
			default:
				System.out.println("Please respond with either Y or N.");
				continue;
			}
		}
	}
	
	public static long getSeedInput(Scanner input, String prompt)
	{
		// Get user input
		System.out.println(prompt);
		String rawSeed = input.nextLine();
		
		// Empty string is a truly pseudorandom seed (using the system clock)
		if (rawSeed.isEmpty())
			return System.nanoTime();
		
		// String of the form m###### is a raw seed
		if (rawSeed.charAt(0) == 'm')
		{
			try
			{
				return Long.parseLong(rawSeed.substring(1, rawSeed.length()));
			}
			catch (NumberFormatException e) {}
		}
		
		// Convert string seed into a long
		long seed = 0;
		for (char c : rawSeed.toCharArray())
		{
			seed *= 127;
			seed += c;
		}
		
		// Generate a seed for the map based on the user's seed.
		Random rand = new Random(seed);
		long mapSeed = rand.nextLong();
		return mapSeed;
	}
	
	public static RandoKey getRandoKeyInput(Scanner input, String prompt, ObjectClassesFile classData)
	{
		while (true)
		{
			System.out.println(prompt);
			String inputStr = input.nextLine();
			try
			{
				if (inputStr.isEmpty())
					return new RandoKey("B,P,E", classData);
				return new RandoKey(inputStr, classData);
			}
			catch (Exception e)
			{
				System.out.println("Key creation failed with the following error(s):\n" + e.getMessage());
			}
		}
	}
	
	public static void waitForEnter(Scanner input, String prompt)
	{
		System.out.println(prompt);
		input.nextLine();
	}
	
	public static int getInputFromList(Scanner input, String prompt, ArrayList<String> list)
	{
		String inputStr;
		int offset = 0;
		int pageSize = 5;
		int numOptions = list.size();
		while (true)
		{
			System.out.println(prompt);
			if (offset > 0)
				System.out.println("u\tScroll up");
			for (int i = offset; i < offset + pageSize && i < list.size(); i++)
				System.out.println(i + "\t" + list.get(i));
			if (offset + pageSize < numOptions)
				System.out.println("d\tScroll down");
			
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
						System.out.println(choice + "is out of the range of available options.");
					return choice;
				}
				catch (NumberFormatException e)
				{
					System.out.println("Please enter a number, or u or d to scroll");
				}
			}
		}
	}
}
