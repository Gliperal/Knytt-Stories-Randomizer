package core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import map.KSMap;
import map.MapObject;
import util.Util;

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
		ArrayList<MapObject> outObjects = output.findAll(map);
		ArrayList<Integer> outShuffle = new ArrayList<Integer>();
		for (MapObject obj : outObjects)
			outShuffle.add(Util.combineBankObj(obj.bank, obj.object));

		// Shuffle said list
		Collections.shuffle(outShuffle, rand);

		// If there are no objects that match, just make it the deletion object.
		if (outShuffle.isEmpty())
			outShuffle.add(0);

		// Randomize
		ArrayList<MapObject> targets = map.find(input);
		int next = 0;
		for (int i = 0; i < targets.size(); i++)
		{
			// If we reach the end of the shuffled output items, shuffle again and start from the beginning
			if (next >= outShuffle.size())
			{
				Collections.shuffle(outShuffle, rand);
				next = 0;
			}
			targets.get(i).replace(outShuffle.get(next));
			next++;
		}
	}

	public String toString()
	{
		return "Shuffle " + super.toDisplayString();
	}
}
