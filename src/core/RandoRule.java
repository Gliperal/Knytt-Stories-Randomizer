package core;

import java.text.ParseException;

public abstract class RandoRule
{
	private ObjectClass input;
	private ObjectClass output;
	
	protected void readKey(String key, ObjectClassesFile classData) throws ParseException
	{
		String[] split = key.split("->");
		if (split.length == 1)
		{
			input = classData.buildObjectClass(key, -1);
			output = input;
		}
		else if (split.length == 2)
		{
			input = classData.buildObjectClass(split[0], -1);
			output = classData.buildObjectClass(split[1], -1);
		}
		else
			throw new ParseException("More than one -> in randomization rule.", -1);
	}
	
	public String toString()
	{
		return "Randomization Rule (" + input.size() + " objects -> " + output.size() + " objects)";
	}
}
