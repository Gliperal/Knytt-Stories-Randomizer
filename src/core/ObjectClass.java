package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import util.BankObjectArrayUtil;

public class ObjectClass
{
	private char id;
	private String name;
	private int[] objects;
	Map<Integer, Integer> objectShuffle;
	private boolean isSorted = false;
	
	private ObjectClass() {}
	
	public ObjectClass(char id, String name)
	{
		this.id = Character.toUpperCase(id);
		this.name = name;
		objects = new int[0];
	}
	
	public boolean hasID(char id)
	{
		return this.id == Character.toUpperCase(id);
	}
	
	public void add(int bank, int obj)
	{
		// Check if object already exists in this class
		if (hasObject(bank, obj))
			return;
		
		// Copy data to new array
		int oldLength = objects.length;
		int[] updatedObjects = new int[oldLength + 1];
		for (int i = 0; i < oldLength; i++)
			updatedObjects[i] = objects[i];
		
		// Add new data
		updatedObjects[oldLength] = (bank << 8) | obj;
		
		// Copy back
		objects = updatedObjects;
		isSorted = false;
	}
	
	public boolean add(String objectStr)
	{
		String[] split = objectStr.split(":", 2);
		if (split.length == 1)
			return false;
		try
		{
			// Retrieve data
			int bank = Integer.parseInt(split[0]);
			int obj = Integer.parseInt(split[1]);
			
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
			Arrays.sort(objects);
			isSorted = true;
		}
	}
	
	public ObjectClass combineWith(ObjectClass that)
	{
		// Sort both classes if they aren't already, so that we can perform a merge with uniqueness
		sort();
		that.sort();
		
		// Merge object arrays
		int[] combinedObjects = new int[objects.length + that.objects.length];
		int i = 0, j = 0, k = 0;
		while (i < objects.length && j < that.objects.length)
		{
			int next = Integer.min(objects[i], that.objects[j]);
			if (objects[i] == next)
				i++;
			if (that.objects[j] == next)
				j++;
			combinedObjects[k] = next;
			k++;
		}
		while (i < objects.length)
			combinedObjects[k++] = objects[i++];
		while (j < that.objects.length)
			combinedObjects[k++] = that.objects[j++];
		
		// Create result group
		ObjectClass group = new ObjectClass();
		group.id = id;
		group.name = name;
		group.objects = Arrays.copyOf(combinedObjects, k);
		return group;
	}
	
	public ObjectClass overlapWith(ObjectClass that)
	{
		// Sort both classes if they aren't already, so that we can perform a linear search
		sort();
		that.sort();
		
		// Merge object arrays
		int[] commonObjects = new int[Integer.min(objects.length, that.objects.length)];
		int i = 0, j = 0, k = 0;
		while (i < objects.length && j < that.objects.length)
		{
			if (objects[i] == that.objects[j])
			{
				commonObjects[k] = objects[i];
				i++;
				j++;
				k++;
			}
			else if (objects[i] < that.objects[j])
				i++;
			else
				j++;
		}
		
		// Create result group
		ObjectClass group = new ObjectClass();
		group.id = id;
		group.name = name;
		group.objects = Arrays.copyOf(commonObjects, k);
		return group;
	}
	
	public ObjectClass eliminateFrom(ObjectClass that)
	{
		// Sort both classes if they aren't already, so that we can perform a linear search
		sort();
		that.sort();
		
		// Merge object arrays
		int[] uniqueObjects = new int[objects.length];
		int i = 0, j = 0, k = 0;
		while (i < objects.length)
		{
			if (j == that.objects.length || objects[i] < that.objects[j])
			{
				uniqueObjects[k] = objects[i];
				i++;
				k++;
			}
			else if (objects[i] == that.objects[j])
			{
				i++;
				j++;
			}
			else
				j++;
		}
		
		// Create result group
		ObjectClass group = new ObjectClass();
		group.id = id;
		group.name = name;
		group.objects = Arrays.copyOf(uniqueObjects, k);
		return group;
	}
	
	@Deprecated
	public void trim(ObjectClass that)
	{
		// Sort both classes if they aren't already
		sort();
		that.sort();
		
		// Trim
		//objects = BankObjectArrayUtil.overlapSortedArrays(objects, that.objects);
	}
	
	public String toString()
	{
		String result = "ObjectClass(" + id + ", " + name + ")[";
		for (int i = 0; i < objects.length; i++)
			result += (objects[i] >> 8) + ":" + (objects[i] & 0xFF) + ",";
		return result + "]";
	}
	
	public boolean hasObject(int bank, int obj)
	{
		for (int i = 0; i < objects.length; i += 2)
			if (objects[i] == bank && objects[i+1] == obj)
				return true;
		return false;
	}
	
	public byte[] randomObject(Random rand)
	{
		return new byte[] {0, 0};
		// TODO return objects[rand.nextInt(objects.length)];
	}
	
	@Deprecated
	public void shuffle(Random rand)
	{
		/*
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
		*/
	}
	
	public void shuffleInit(Random rand)
	{
		/*
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
		*/
	}
	
	/*
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
	*/
	
	@Deprecated
	public byte[] shuffleObject(byte bank, byte obj)
	{
		return new byte[] {0, 0};
		//short key = (short) (bank*256 + obj);
		//return objectShuffle.get(key);
	}
	
	public String indentifier()
	{
		return Character.toUpperCase(id) + ": " + name;
	}
	
	public int firstCommonObject(ObjectClass that)
	{
		sort();
		that.sort();
		int i = 0, j = 0;
		while (i < objects.length && j < that.objects.length)
		{
			if (objects[i] == that.objects[j])
				return objects[i];
			else if (objects[i] < that.objects[j])
				i++;
			else
				j++;
		}
		return -1;
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
