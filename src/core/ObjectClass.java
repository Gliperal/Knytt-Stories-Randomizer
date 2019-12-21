package core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import util.BankObjectArrayUtil;

public class ObjectClass
{
	private char id;
	private String name;
	private byte[] objects;
	Map<Short, byte[]> objectShuffle;
	private boolean isSorted = false;
	
	private ObjectClass() {}
	
	public ObjectClass(char id, String name)
	{
		this.id = Character.toUpperCase(id);
		this.name = name;
		objects = new byte[0];
	}
	
	public boolean hasID(char id)
	{
		return this.id == Character.toUpperCase(id);
	}
	
	public void add(byte bank, byte obj)
	{
		// Check if object already exists in this class
		if (hasObject(bank, obj))
			return;
		
		// Copy data to new array
		int oldLength = objects.length;
		byte[] updatedObjects = new byte[oldLength + 2];
		for (int i = 0; i < oldLength; i++)
			updatedObjects[i] = objects[i];
		
		// Add new data
		updatedObjects[oldLength] = bank;
		updatedObjects[oldLength + 1] = obj;
		
		// Copy back
		objects = updatedObjects;
	}
	
	public boolean add(String objectStr)
	{
		String[] split = objectStr.split(":", 2);
		if (split.length == 1)
			return false;
		try
		{
			// Retrieve data
			byte bank = (byte) Integer.parseInt(split[0]);
			byte obj = (byte) Integer.parseInt(split[1]);
			
			// Add to objects
			add(bank, obj);
			return true;
		}
		catch(NumberFormatException e)
		{
			return false;
		}
	}
	
	public void sort()
	{
		if (!isSorted)
		{
			objects = BankObjectArrayUtil.sort(objects);
			isSorted = true;
		}
	}
	
	public ObjectClass combineWith(ObjectClass that)
	{
		// Sort both classes if they aren't already, so that we can perform a kind of simple merge sort
		sort();
		that.sort();
		
		// Create result group
		ObjectClass group = new ObjectClass();
		group.id = id;
		group.name = name;
		group.objects = BankObjectArrayUtil.combineSortedArrays(this.objects, that.objects);
		
		// Return
		return group;
	}
	
	public void trim(ObjectClass that)
	{
		// Sort both classes if they aren't already
		sort();
		that.sort();
		
		// Trim
		objects = BankObjectArrayUtil.overlapSortedArrays(objects, that.objects);
	}
	
	public String toString()
	{
		String result = "ObjectClass(" + id + ", " + name + ")[";
		for (int i = 0; i < objects.length/2; i++)
			result += objects[i*2] + ":" + objects[i*2 + 1] + ",";
		return result + "]";
	}

	public boolean hasObject(byte bank, byte obj)
	{
		for (int i = 0; i < objects.length; i += 2)
			if (objects[i] == bank && objects[i+1] == obj)
				return true;
		return false;
	}

	public byte[] randomObject(Random rand)
	{
		int index = 2 * rand.nextInt(objects.length / 2);
		return new byte[] {objects[index], objects[index+1]};
	}
	
	@Deprecated
	public void shuffle(Random rand)
	{
		int numSpots = objects.length / 2;
		ArrayList<Integer> availableSpots = new ArrayList<Integer>();
		for (int i = 0; i < numSpots; i++)
			availableSpots.add(i*2);
		
		byte[] shuffled = new byte[objects.length];
		int shuffledIndex = 0;
		while (numSpots > 0)
		{
			int i = rand.nextInt(numSpots);
			int spot = availableSpots.remove(i);
			shuffled[shuffledIndex] = objects[spot];
			shuffled[shuffledIndex+1] = objects[spot+1];
			numSpots--;
			shuffledIndex += 2;
		}
		
		objects = shuffled;
	}
	
	public void shuffleInit(Random rand)
	{
		int numObjects = objects.length / 2;
		ArrayList<Integer> available = new ArrayList<Integer>();
		for (int i = 0; i < numObjects; i++)
			available.add(i*2);
		
		objectShuffle = new HashMap<Short, byte[]>();
		for (int i = 0; i < objects.length; i += 2)
		{
			short key = (short) (objects[i]*256 + objects[i+1]);
			int r = available.remove(rand.nextInt(numObjects));
			numObjects--;
			byte[] value = new byte[]
			{
					objects[r],
					objects[r+1]
			};
			objectShuffle.put(key, value);
		}
	}
	
	@Deprecated
	public byte[] objectAfter(byte bank, byte obj)
	{
		for (int index = 0; index < objects.length; index += 2)
			if (objects[index] == bank && objects[index+1] == obj)
			{
				int indexAfter = (index + 2) % objects.length;
				return new byte[] {objects[indexAfter], objects[indexAfter+1]};
			}
		
		// Object not in this class
		return null;
	}
	
	public byte[] shuffleObject(byte bank, byte obj)
	{
		short key = (short) (bank*256 + obj);
		return objectShuffle.get(key);
	}
	
	public String indentifier()
	{
		return Character.toUpperCase(id) + ": " + name;
	}
	
	public byte[] hasACommonObject(ObjectClass that)
	{
		for (int i = 0; i < objects.length; i += 2)
			for (int j = 0; j < that.objects.length; j += 2)
				if (objects[i] == that.objects[j] && objects[i+1] == that.objects[j+1])
					return new byte[] {objects[i], objects[i+1]};
		return null;
	}
	
	public static class ObjectClassComparator implements Comparator<ObjectClass>
	{
		@Override
		public int compare(ObjectClass a, ObjectClass b)
		{
			return a.cmp(b);
		}
	}

	public int cmp(ObjectClass b)
	{
		return id - b.id;
	}
}
