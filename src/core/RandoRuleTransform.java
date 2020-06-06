package core;

import java.text.ParseException;

public class RandoRuleTransform extends RandoRule
{
	public RandoRuleTransform(String key, ObjectClassesFile classData) throws ParseException
	{
		super.readKey(key, classData);
		// TODO Auto-generated constructor stub
	}
	
	public String toString()
	{
		return "Transform " + super.toString();
	}
}
