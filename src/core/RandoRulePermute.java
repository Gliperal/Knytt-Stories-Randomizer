package core;

import java.text.ParseException;

public class RandoRulePermute extends RandoRule
{
	public RandoRulePermute(String key, ObjectClassesFile classData) throws ParseException
	{
		super.readKey(key, classData);
		// TODO Auto-generated constructor stub
	}
	
	public String toString()
	{
		return "Permute " + super.toString();
	}
}
