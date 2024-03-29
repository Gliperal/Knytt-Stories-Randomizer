package map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import util.Util;

public class ObjectPattern implements Pattern
{
	private int[] objects;
	private boolean isSorted = false;

	public ObjectPattern()
	{
		objects = new int[0];
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

	public Pattern and(Pattern that)
	{
		if (that instanceof ObjectPattern)
			return and((ObjectPattern)that);
		return new CombinedPattern('&', this, that);
	}

	public Pattern or(Pattern that)
	{
		if (that instanceof ObjectPattern)
			return or((ObjectPattern)that); // there's got to be a better way to do it than this...
		return new CombinedPattern('|', this, that);
	}

	public Pattern subtract(Pattern that)
	{
		if (that instanceof ObjectPattern)
			return subtract((ObjectPattern)that);
		return new CombinedPattern('-', this, that);
	}

	/**
	 * Returns a new ObjectPattern that contains all of the objects from this ObjectPattern and the one passed as an argument. Neither of the inputs are modified.
	 * @param that The ObjectPattern to be combined with.
	 * @return The union of the two inputs.
	 */
	public ObjectPattern or(ObjectPattern that)
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
		ObjectPattern group = new ObjectPattern();
		group.objects = Arrays.copyOf(combinedObjects, k);
		return group;
	}

	/**
	 * Returns a new ObjectPattern that contains all of the objects that are common to both this ObjectPattern and the one passed as an argument. Neither of the inputs are modified.
	 * @param that The ObjectPattern to be overlapped with.
	 * @return The intersection of the two inputs.
	 */
	public ObjectPattern and(ObjectPattern that)
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
		ObjectPattern group = new ObjectPattern();
		group.objects = Arrays.copyOf(commonObjects, k);
		return group;
	}

	/**
	 * Returns a clone of the ObjectPattern, but stripped of all objects that are present in the ObjectPattern passed as an argument. Neither of the inputs are modified.
	 * @param that The ObjectPattern containing forbidden objects.
	 * @return The difference of the two inputs.
	 */
	public ObjectPattern subtract(ObjectPattern that)
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
		ObjectPattern group = new ObjectPattern();
		group.objects = Arrays.copyOf(uniqueObjects, k);
		return group;
	}

	public String toString()
	{
		String result = "ObjectGroup[";
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
		sort();
		return objects[rand.nextInt(objects.length)];
	}

	public int[] toList()
	{
		sort();
		return Arrays.copyOf(objects, objects.length);
	}

	public ArrayList<Integer> randomlyFillList(int length, Random rand)
	{
		sort();
		ArrayList<Integer> shuffledObjects = new ArrayList<Integer>();
		for (int i : objects)
			shuffledObjects.add(i);
		ArrayList<Integer> ret = new ArrayList<Integer>();
		while (ret.size() < length)
		{
			Collections.shuffle(shuffledObjects, rand);
			ret.addAll(shuffledObjects);
		}
		return ret;
	}

	public int firstCommonObject(ObjectPattern that)
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

	public boolean matches(Screen s, int layer, int x, int y)
	{
		sort();
		byte[] bankObj = s.objectAt(layer, x, y);
		int object = Util.combineBankObj(bankObj[0], bankObj[1]);
		return Arrays.binarySearch(objects, object) >= 0;
	}

	public ObjectPattern simplify(KSMap map)
	{
		return this;
	}
}
