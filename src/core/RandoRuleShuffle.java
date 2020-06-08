package core;

import java.text.ParseException;

public class RandoRuleShuffle extends RandoRule
{
	public final static char ID = 'S';
	protected char getID() { return ID; }
	
	public RandoRuleShuffle(String key, ObjectClassesFile classData) throws ParseException
	{
		super.readKey(key, classData);
		// TODO Auto-generated constructor stub
	}
	
	// TODO things get interesting if input != output
	
	public String toString()
	{
		return "Shuffle " + super.toString();
	}
}
