package core;

import java.text.ParseException;
import java.util.Random;

import map.KSMap;

import util.Console;

public class RandoRulePermute extends RandoRuleTransform
{
	public final static char ID = 'P';
	protected char getID() { return ID; }

	public RandoRulePermute(String key, ObjectClassesFile classData) throws ParseException
	{
		super(key, classData);
	}

	public void randomize(KSMap map, Random rand)
	{
		// Collect all the relevant objects
		ObjectGroup mapObjects = map.allObjects(true);
		mapObjects.sort();

		// Trim output down to only use those objects
		WeightedObjectGroup reducedOutput = output.overlapWith(mapObjects);
		if (reducedOutput == null)
		{
			Console.printWarning("Map contains no objects of the type " + outputCreationKey + ". Skipping rule.");
			return;
		}
		output = reducedOutput;

		// Continue as with Transform
		super.randomize(map, rand);
	}

	public String toString()
	{
		return "Permute " + super.toDisplayString();
	}
}
