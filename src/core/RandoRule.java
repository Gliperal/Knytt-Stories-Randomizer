package core;

import java.text.ParseException;
import java.util.Random;

public abstract class RandoRule
{
	protected ObjectClass input;
	protected ObjectClass output;
	private String creationKey;
	
	protected void readKey(String key, ObjectClassesFile classData) throws ParseException
	{
		String[] split = key.split("->");
		if (split.length == 1)
		{
			input = classData.buildObjectClass(key, -1).addCreationKey(key);
			output = input;
		}
		else if (split.length == 2)
		{
			input = classData.buildObjectClass(split[0], -1).addCreationKey(split[0]);
			output = classData.buildObjectClass(split[1], -1).addCreationKey(split[1]);
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
	
	private String objectClassString(ObjectClass oc)
	{
		return oc.getCreationKey() + " (" + oc.size() + " objects)";
	}
	
	public String toDisplayString()
	{
		String ruleStr = objectClassString(input);
		if (input != output)
			ruleStr += " -> " + objectClassString(output);
		return "Randomization Rule [" + ruleStr + "]";
	}
}
