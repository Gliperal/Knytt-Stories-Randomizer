package core;

import java.text.ParseException;

public class RandoRuleTransform extends RandoRule
{
	public final static char ID = 'T';
	protected char getID() { return ID; }
	
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
