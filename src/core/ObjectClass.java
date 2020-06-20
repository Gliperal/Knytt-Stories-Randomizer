package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;

import util.Util;

public class ObjectClass
{
	private char id;
	private String name;
	private String creationKey;
	private int[] objects;
	Map<Integer, Integer> objectShuffle;
	private boolean isSorted = false;
	
	private ObjectClass() {}
	
	// all base classes have a character id and optionally a name
	// for combined classes, id is 0 and key is used instead
	public ObjectClass(char id, String name)
	{
		this.id = Character.toUpperCase(id);
		this.name = name;
		objects = new int[0];
	}
	
	public ObjectClass addCreationKey(String key)
	{
		ObjectClass group = new ObjectClass();
		group.id = 0;
		group.creationKey = key.trim();
		group.objects = objects;
		group.objectShuffle = objectShuffle;
		group.isSorted = isSorted;
		return group;
	}
	
	public String getCreationKey()
	{
		return (id == 0) ? creationKey : Character.toString(id);
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
		updatedObjects[oldLength] = Util.combineBankObj(bank, obj);
		
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
	
	/**
	 * Returns a new ObjectClass that contains all of the objects from this ObjectClass and the one passed as an argument. Neither of the inputs are modified. The creation key is taken from the ObjectClass whose method is called.
	 * @param that The ObjectClass to be combined with.
	 * @return The union of the two inputs.
	 */
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
		group.creationKey = creationKey;
		group.objects = Arrays.copyOf(combinedObjects, k);
		return group;
	}
	
	/**
	 * Returns a new ObjectClass that contains all of the objects that are common to both this ObjectClass and the one passed as an argument. Neither of the inputs are modified. The creation key is taken from the ObjectClass whose method is called.
	 * @param that The ObjectClass to be overlapped with.
	 * @return The intersection of the two inputs.
	 */
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
		group.creationKey = creationKey;
		group.objects = Arrays.copyOf(commonObjects, k);
		return group;
	}
	
	/**
	 * Returns a clone of the ObjectClass, but stripped of all objects that are present in the ObjectClass passed as an argument. Neither of the inputs are modified. The creation key is taken from the ObjectClass whose method is called.
	 * @param that The ObjectClass containing forbidden objects.
	 * @return The difference of the two inputs.
	 */
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
		group.creationKey = creationKey;
		group.objects = Arrays.copyOf(uniqueObjects, k);
		return group;
	}
	
	public String toString()
	{
		String result;
		if (id != 0)
			result = "ObjectClass(Base: " + id + ", " + name + ")[";
		else
			result = "ObjectClass(Manufactured: " + creationKey + ")[";
		for (int i = 0; i < objects.length; i++)
			result += (objects[i] >> 8) + ":" + (objects[i] & 0xFF) + ",";
		return result + "]";
	}
	
	public boolean hasObject(int bankObj)
	{
		for (int i = 0; i < objects.length; i++)
			if (objects[i] == bankObj)
				return true;
		return false;
	}
	
	public boolean hasObject(int bank, int obj)
	{
		return hasObject(Util.combineBankObj(bank, obj));
	}
	
	public int randomObject(Random rand)
	{
		return objects[rand.nextInt(objects.length)];
	}
	
	public int[] toList()
	{
		// TODO Not sure if it's safe to return objects
		return Arrays.copyOf(objects, objects.length);
	}
	
	public int[] toShuffle(Random rand)
	{
		int len = objects.length;
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		for (int i = 0; i < len; i++)
			indexes.add(i);
		Collections.shuffle(indexes, rand);
		int[] shuffle = new int[len];
		for (int i = 0; i < len; i++)
			shuffle[i] = objects[indexes.get(i)];
		return shuffle;
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
	
	public int size()
	{
		return objects.length;
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
