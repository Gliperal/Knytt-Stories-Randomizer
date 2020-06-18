package core;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Random;

import map.KSMap;
import util.Util;

public class RandoRuleTransform extends RandoRule
{
	public final static char ID = 'T';
	protected char getID() { return ID; }
	
	public RandoRuleTransform(String key, ObjectClassesFile classData) throws ParseException
	{
		super.readKey(key, classData);
	}
	
	public void randomize(KSMap map, Random rand)
	{
		// Get a list of the input objects, and sort for later
		int[] inObjs = input.toList();
		Arrays.sort(inObjs);
		
		// Shuffle the output objects as many times as needed to match the size of input
		int[] outObjs = {};
		while (outObjs.length < inObjs.length)
			outObjs = Util.mergeArrays(outObjs, output.toShuffle(rand));
		
		// Randomize
		boolean includeEmpty = input.hasObject(0);
		int[] mapObjects = map.exportObjects(includeEmpty);
		for (int i = 0; i < mapObjects.length; i++)
		{
			int obj = mapObjects[i];
			int objIndex = Arrays.binarySearch(inObjs, obj);
			if (objIndex >= 0)
				mapObjects[i] = outObjs[objIndex];
		}
		map.importObjects(mapObjects, includeEmpty);
	}
	
	public String toString()
	{
		return "Transform " + super.toString();
	}
}
