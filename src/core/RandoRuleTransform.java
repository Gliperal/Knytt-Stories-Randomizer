package core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Random;

import map.KSMap;
import map.MapObject;
import util.Console;
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
		ArrayList<MapObject> targets = map.find(input);
		ArrayList<Integer> inObjs = new ArrayList<Integer>();
		for (MapObject mo : targets)
		{
			int object = Util.combineBankObj(mo.bank, mo.object);
			if (inObjs.indexOf(object) == -1)
				inObjs.add(object);
		}
		inObjs.sort(null);

		// Shuffle the output objects as many times as needed to match the size of input
		int status = output.prepForRandomization(map);
		if (status == 1)
			Console.printWarning("Map contains no objects for certain groups in " + outputCreationKey + ". Deleting those groups from rule.");
		if (status == -1)
		{
			Console.printWarning("Map contains no objects of the type " + outputCreationKey + ". Skipping rule.");
			return;
		}
		ArrayList<Integer> outObjs = output.randomlyFillList(inObjs.size(), rand);

		// Randomize
		for (MapObject target : targets)
		{
			int obj = Util.combineBankObj(target.bank, target.object);
			int objIndex = inObjs.indexOf(obj);
			target.replace(outObjs.get(objIndex));
		}
	}

	public String toString()
	{
		return "Transform " + super.toDisplayString();
	}
}
