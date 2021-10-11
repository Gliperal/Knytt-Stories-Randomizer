package core;

import java.text.ParseException;
import java.util.Random;

public abstract class RandoRule
{
	protected ObjectGroup input;
	protected ObjectGroup output;
	private String creationKey;
	
	protected void readKey(String key, ObjectClassesFile classData) throws ParseException
	{
		String[] split = key.split("->");
		if (split.length == 1)
		{
			input = classData.buildObjectGroup(key, -1);
			output = input;
		}
		else if (split.length == 2)
		{
			input = classData.buildObjectGroup(split[0], -1);
			output = classData.buildObjectGroup(split[1], -1);
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
		int obj = input.firstCommonObject(that.input);
		if (obj == -1)
			obj = input.firstCommonObject(that.output);
		if (obj == -1)
			obj = output.firstCommonObject(that.input);
		if (obj == -1)
			obj = output.firstCommonObject(that.output);
		return obj;
	}
	
	private static String objectGroupString(ObjectGroup group)
	{
		return group.getCreationKey() + " (" + group.size() + " objects)";
	}
	
	public String toDisplayString()
	{
		String ruleStr = objectGroupString(input);
		if (input != output)
			ruleStr += " -> " + objectGroupString(output);
		return "Randomization Rule [" + ruleStr + "]";
	}
}
