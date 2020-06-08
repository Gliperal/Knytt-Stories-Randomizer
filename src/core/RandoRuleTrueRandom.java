package core;

import java.text.ParseException;

public class RandoRuleTrueRandom extends RandoRule
{
	public final static char ID = 'R';
	protected char getID() { return ID; }
	
	public RandoRuleTrueRandom(String key, ObjectClassesFile classData) throws ParseException
	{
		super.readKey(key, classData);
		// TODO Auto-generated constructor stub
	}
	
	public String toString()
	{
		return "True Random " + super.toString();
	}
}
