package core;

public abstract class RandoRule
{
	private ObjectClass input;
	private ObjectClass output;
	
	protected void readKey(String key, ObjectClassesFile classData) throws Exception
	{
		String[] split = key.split("->");
		if (split.length == 1)
		{
			input = new ObjectClass('.', "auto-generated object class");
			input = classData.buildObjectClass(input, key, -1);
			output = input;
		}
		else if (split.length == 2)
		{
			input = new ObjectClass('.', "auto-generated object class");
			input = classData.buildObjectClass(input, split[0], -1);
			output = new ObjectClass('.', "auto-generated object class");
			output = classData.buildObjectClass(output, split[1], -1);
		}
		else
			throw new Exception("More than one -> in randomization rule.");
	}
}
