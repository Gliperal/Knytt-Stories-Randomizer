package core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Random;

import map.KSMap;
import map.MapObject;

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
		output.populateWithMapObjects(map);
		for (MapObject target : targets)
			target.replace(output.randomObject(rand));
	}

	public String toString()
	{
		return "True Random " + super.toDisplayString();
	}
}
