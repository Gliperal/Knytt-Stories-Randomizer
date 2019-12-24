package core;

import java.util.ArrayList;
import java.util.Random;

public class RandoKey
{
	private String keyString;
	private ArrayList<ObjectClass> randoGroups;
	
	public RandoKey(String keyString, ObjectClassesFile classData) throws Exception
	{
		// Save keyString
		this.keyString = keyString;
		
		// Turn user input into groups
		System.out.println("Organizing rando group data...");
		randoGroups = new ArrayList<ObjectClass>();
		for (String groupStr : keyString.split(","))
		{
			if (groupStr.isEmpty())
				continue;
			
			// Merge relevant object classes into one group
			ObjectClass group = classData.group(groupStr);
			randoGroups.add(group);
		}
		
		// Make sure no objects occur in more than one group
		for (int i = 0; i < randoGroups.size(); i++)
			for (int j = i + 1; j < randoGroups.size(); j++)
			{
				int commonObject = randoGroups.get(i).firstCommonObject(randoGroups.get(j));
				if (commonObject != -1)
					throw new Exception("Error: " + commonObject + ":" + commonObject + " found in more than one randomizer group.");
			}
	}
	
	public int numberOfGroups()
	{
		return randoGroups.size();
	}
	
	public void seed(Random rand)
	{
		// Shuffle each of the rando groups
		for (ObjectClass oc : randoGroups)
			oc.shuffleInit(rand);
	}
	
	@Deprecated
	public void restrict(ObjectClass allowedObjects)
	{
		for (ObjectClass oc : randoGroups)
			oc.trim(allowedObjects);
	}
	
	/**
	 * Using the true random algorithm
	 */
	public byte[] randomize(byte bank, byte obj, Random rand)
	{
		for (ObjectClass oc : randoGroups)
		{
			if (oc.hasObject(bank, obj))
				return oc.randomObject(rand);
		}
		return null;
	}
	
	/**
	 * Using the transform algorithm (assuming the groups have already been seeded)
	 */
	public byte[] randomize(byte bank, byte obj)
	{
		for (ObjectClass oc : randoGroups)
		{
			byte[] result = oc.shuffleObject(bank, obj);
			if (result != null)
				return result;
		}
		return null;
	}

	public int groupIndexForObject(byte bank, byte obj)
	{
		for (int i = 0; i < randoGroups.size(); i++)
			if (randoGroups.get(i).hasObject(bank, obj))
				return i;
		return -1;
	}
	
	public String keyString()
	{
		return keyString;
	}
}
