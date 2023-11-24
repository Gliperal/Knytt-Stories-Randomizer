package core;

import java.text.ParseException;
import java.util.Random;

import map.KSMap;

public class RandoRuleTrueRandom extends RandoRule
{
	public final static char ID = 'R';
	protected char getID() { return ID; }

	public RandoRuleTrueRandom(String key, ObjectClassesFile classData) throws ParseException
	{
		super.readKey(key, classData);
	}

	public void randomize(KSMap map, Random rand)
	{
		boolean includeEmpty = input.hasObject(0);
		int[] mapObjects = map.exportObjects(includeEmpty);
		for (int i = 0; i < mapObjects.length; i++)
			if (input.hasObject(mapObjects[i]))
				mapObjects[i] = output.randomObject(rand);
		map.importObjects(mapObjects, includeEmpty);
	}

	public String toString()
	{
		return "True Random " + super.toDisplayString();
	}
}
