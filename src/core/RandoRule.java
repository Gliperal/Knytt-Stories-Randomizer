package core;

import java.text.ParseException;
import java.util.Random;

import map.Pattern;

public abstract class RandoRule
{
	public Pattern input; // TODO Protected
	protected WeightedObjectGroup output;
	protected String outputCreationKey;
	private String creationKey;

	protected void readKey(String key, ObjectClassesFile classData) throws ParseException
	{
		String[] split = key.split("->");
		if (split.length == 1)
		{
			input = classData.buildObjectGroup(key);
			output = new WeightedObjectGroup(input);
			outputCreationKey = key;
		}
		else if (split.length == 2)
		{
			input = classData.buildObjectGroup(split[0]);
			output = new WeightedObjectGroup(classData, split[1]);
			outputCreationKey = split[1];
		}
		else
			throw new ParseException("More than one -> in randomization rule.", -1);
		creationKey = key;
	}

	public static RandoRule create(char type, String key, ObjectClassesFile classData) throws ParseException
	{
		switch(type)
		{
		case RandoRulePermute.ID:
			return new RandoRulePermute(key, classData);
		case RandoRuleShuffle.ID:
			return new RandoRuleShuffle(key, classData);
		case RandoRuleTransform.ID:
			return new RandoRuleTransform(key, classData);
		case RandoRuleTrueRandom.ID:
			return new RandoRuleTrueRandom(key, classData);
		default:
			return null;
		}
	}

	public static RandoRule loadFromString(String str, ObjectClassesFile classData) throws ParseException
	{
		if (str.length() < 1)
			return null;
		char type = str.charAt(0);
		String key = str.substring(1);
		return create(type, key, classData);
	}

	public abstract void randomize(map.KSMap map, Random rand);

	protected abstract char getID();

	public String saveToString()
	{
		return getID() + creationKey;
	}

	public int conflictsWith(RandoRule that)
	{
		// TODO
		return 0;
		//return input.firstCommonObject(that.input);
	}

	public String toDisplayString()
	{
		return "Randomization Rule [" + creationKey + "]";
	}
}
