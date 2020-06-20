package core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import map.KSMap;

public class RandoRuleShuffle extends RandoRule
{
	public final static char ID = 'S';
	protected char getID() { return ID; }
	
	public RandoRuleShuffle(String key, ObjectClassesFile classData) throws ParseException
	{
		super.readKey(key, classData);
	}
	
	public void randomize(KSMap map, Random rand)
	{
		// Collect a list of all the output objects in the map (including duplicates)
		int[] outObjects = map.exportObjects(output.hasObject(0));
		ArrayList<Integer> outShuffle = new ArrayList<Integer>();
		for (int obj : outObjects)
			if (output.hasObject(obj))
				outShuffle.add(obj);
		
		// Shuffle said list
		Collections.shuffle(outShuffle, rand);
		
		// If there are no objects that match, just make it the deletion object.
		if (outShuffle.isEmpty())
			outShuffle.add(0);
		
		// Randomize
		boolean includeEmpty = input.hasObject(0);
		int[] mapObjects = map.exportObjects(includeEmpty);
		int next = 0;
		for (int i = 0; i < mapObjects.length; i++)
			if (input.hasObject(mapObjects[i]))
			{
				// If we reach the end of the shuffled output items, shuffle again and start from the beginning
				if (next >= outShuffle.size())
				{
					Collections.shuffle(outShuffle, rand);
					next = 0;
				}
				mapObjects[i] = outShuffle.get(next);
				next++;
			}
		map.importObjects(mapObjects, includeEmpty);
	}
	
	public String toString()
	{
		return "Shuffle " + super.toDisplayString();
	}
}
