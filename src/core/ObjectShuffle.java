package core;

import java.util.Random;

import util.BankObjectArrayUtil;

public class ObjectShuffle
{
	private RandoKey randoGroups;
	int numGroups;
	private byte[][] objects;
	private int[] markers;
	private boolean ready = false;
	
	public ObjectShuffle(RandoKey randoGroups)
	{
		this.randoGroups = randoGroups;
		numGroups = randoGroups.numberOfGroups();
		objects = new byte[numGroups][];
		for (int i = 0; i < numGroups; i++)
		{
			objects[i] = new byte[0];
		}
	}
	
	public void count(byte bank, byte obj)
	{
		if (ready)
			return;
		int groupIndex = randoGroups.groupIndexForObject(bank, obj);
		if (groupIndex == -1)
			return;
		objects[groupIndex] = BankObjectArrayUtil.appendToArray(objects[groupIndex], bank, obj);
	}
	
	public void generateShuffle(Random rand)
	{
		// Shuffle the elements in each group
		for (int i = 0; i < objects.length; i++)
			objects[i] = BankObjectArrayUtil.shuffle(objects[i], rand);
		
		// Initialize the position markers and ready state
		markers = new int[numGroups];
		for (int i = 0; i < numGroups; i++)
			markers[i] = 0;
		ready = true;
	}
	
	public byte[] popShuffledItem(byte bank, byte obj)
	{
		// Make sure we're in the shuffled state
		if (!ready)
			return null;
		
		// Collect some information
		int groupIndex = randoGroups.groupIndexForObject(bank, obj);
		if (groupIndex == -1)
			return null;
		byte[] shuffleGroup = objects[groupIndex];
		int indexInGroup = markers[groupIndex];
		if (indexInGroup >= shuffleGroup.length)
			return null;
		
		// Pop
		byte newBank = shuffleGroup[indexInGroup];
		byte newObj = shuffleGroup[indexInGroup+1];
		markers[groupIndex] = indexInGroup + 2;
		return new byte[] {newBank, newObj};
	}
	
	public void debugPrint()
	{
		for (int i = 0; i < numGroups; i++)
		{
			System.out.print("[");
			byte[] array = objects[i];
			for (int j = 0; j < array.length; j += 2)
				System.out.print(array[j] + ":" + array[j+1] + ", ");
			System.out.println("]");
		}
	}
}
