package core;

import java.text.ParseException;
import java.util.Random;

import map.KSMap;

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
		ObjectClass mapObjects = map.allObjects(input.hasObject(0));
		mapObjects.sort();
		
		// Trim input and output down to only use those objects
		input = input.overlapWith(mapObjects);
		ObjectClass reducedOutput = output.overlapWith(mapObjects);
		if (reducedOutput.size() == 0)
		{
			Console.printWarning("Map contains no objects of the type " + output.getCreationKey() + ". Skipping rule.");
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
