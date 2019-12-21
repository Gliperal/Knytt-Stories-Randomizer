package util;

import java.util.ArrayList;
import java.util.Random;

public class BankObjectArrayUtil
{
	public static byte[] appendToArray(byte[] array, byte bank, byte obj)
	{
		int oldSize = array.length;
		byte[] newArray = new byte[oldSize + 2];
		for (int i = 0; i < oldSize; i++)
			newArray[i] = array[i];
		newArray[oldSize] = bank;
		newArray[oldSize + 1] = obj;
		return newArray;
	}
	
	public static byte[] combineSortedArrays(byte[] a, byte[] b)
	{
		// Important numbers
		int aSize = a.length;
		int bSize = b.length;
		int destSize = aSize + bSize;
		
		// Create destination array
		byte[] dest = new byte[destSize];
		
		// Combine object arrays
		int aIndex = 0;
		int bIndex = 0;
		int destIndex = 0;
		byte lastCopiedBank = 0;
		byte lastCopiedObject = 0;
		for (int i = 0; i < destSize; i += 2)
		{
			boolean copyFromA;
			if (aIndex == aSize)
				// A index at an end
				copyFromA = false;
			else if (bIndex == bSize)
				// B index at an end
				copyFromA = true;
			else
			{
				copyFromA = a[aIndex] == b[bIndex] ?
						a[aIndex + 1] < b[bIndex + 1] :
						a[aIndex] < b[bIndex];
			}
			if (copyFromA)
			{
				// Copy from A
				if (a[aIndex] != lastCopiedBank || a[aIndex + 1] != lastCopiedObject)
				{
					dest[destIndex] = a[aIndex];
					dest[destIndex + 1] = a[aIndex + 1];
					lastCopiedBank = a[aIndex];
					lastCopiedObject = a[aIndex + 1];
					destIndex += 2;
				}
				aIndex += 2;
			}
			else
			{
				// Copy from B
				if (b[bIndex] != lastCopiedBank || b[bIndex + 1] != lastCopiedObject)
				{
					dest[destIndex] = b[bIndex];
					dest[destIndex + 1] = b[bIndex + 1];
					lastCopiedBank = b[bIndex];
					lastCopiedObject = b[bIndex + 1];
					destIndex += 2;
				}
				bIndex += 2;
			}
		}
		
		// Truncate in case there were duplicate elements
		return Util.truncate(dest, destIndex);
	}
	
	private static byte[] quicksort(byte[] array, int low, int high)
	{
		// Sorted
		if (low >= high) return array;
		
		// Pivot
		int pivotBank = array[high];
		int pivotObj = array[high + 1];
		
		// Partition
		byte tmp;
		int p = low;
		for (int j = low; j < high; j += 2)
		{
			boolean leftOfPivot = (array[j] == pivotBank) ? array[j+1] < pivotObj : array[j] < pivotBank;
			if (leftOfPivot)
			{
				// Swap bank
				tmp = array[j];
				array[j] = array[p];
				array[p] = tmp;
				// Swap object
				tmp = array[j+1];
				array[j+1] = array[p+1];
				array[p+1] = tmp;
				// Increment pivot location
				p += 2;
			}
		}
		// Place pivot (bank)
		tmp = array[p];
		array[p] = array[high];
		array[high] = tmp;
		// Place pivot (obj)
		tmp = array[p+1];
		array[p+1] = array[high+1];
		array[high+1] = tmp;
		
		// Recurse
		array = quicksort(array, low, p-2);
		array = quicksort(array, p+2, high);

		// Return (because Java can't pass arrays by pointer)
		return array;
	}
	
	public static byte[] sort(byte[] array)
	{
		return quicksort(array, 0, array.length - 2);
	}

	public static byte[] overlapSortedArrays(byte[] a, byte[] b)
	{
		// Important numbers
		int aSize = a.length;
		int bSize = b.length;
		int destSize = Math.max(aSize, bSize);
		
		// Create destination array
		byte[] dest = new byte[destSize];
		
		// Overlap object arrays
		int aIndex = 0;
		int bIndex = 0;
		int destIndex = 0;
		while (true)
		{
			if (aIndex == aSize)
				// A index at an end
				break;
			if (bIndex == bSize)
				// B index at an end
				break;
			
			// Matching object
			if (a[aIndex] == b[bIndex] && a[aIndex + 1] == b[bIndex + 1])
			{
				dest[destIndex] = a[aIndex];
				dest[destIndex + 1] = a[aIndex + 1];
				destIndex += 2;
				aIndex += 2;
				bIndex += 2;
			}
			else if ((a[aIndex] == b[bIndex]) ? a[aIndex + 1] < b[bIndex + 1] : a[aIndex] < b[bIndex])
				// A object is smaller
				aIndex += 2;
			else
				// B object is smaller
				bIndex += 2;
		}
		
		// Truncate in case there were discarded elements in both arrays
		return Util.truncate(dest, destIndex);
	}
	
	public static byte[] shuffle(byte[] array, Random rand)
	{
		int numSpots = array.length / 2;
		ArrayList<Integer> availableSpots = new ArrayList<Integer>();
		for (int i = 0; i < numSpots; i++)
			availableSpots.add(i*2);
		
		byte[] shuffled = new byte[array.length];
		int shuffledIndex = 0;
		while (numSpots > 0)
		{
			int i = rand.nextInt(numSpots);
			int spot = availableSpots.remove(i);
			shuffled[shuffledIndex] = array[spot];
			shuffled[shuffledIndex+1] = array[spot+1];
			numSpots--;
			shuffledIndex += 2;
		}
		
		return shuffled;
	}
}
