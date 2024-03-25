package core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Random;

import map.KSMap;
import map.MapObject;
import util.Console;

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
		ArrayList<MapObject> targets = map.find(input);
		int status = output.prepForRandomization(map);
		if (status == 1)
			Console.printWarning("Map contains no objects for certain groups in " + outputCreationKey + ". Deleting those groups from rule.");
		if (status == -1)
		{
			Console.printWarning("Map contains no objects of the type " + outputCreationKey + ". Skipping rule.");
			return;
		}
		for (MapObject target : targets)
			target.replace(output.randomObject(rand));
	}

	public String toString()
	{
		return "True Random " + super.toDisplayString();
	}
}
