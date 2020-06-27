package core;

public class ObjectClassMetadata
{
	public char id;
	public String name;
	public String category;
	
	public ObjectClassMetadata(char id, String name)
	{
		this.id = id;
		this.name = name;
	}
	
	public ObjectClassMetadata(char id, String name, String category)
	{
		this(id, name);
		this.category = category;
	}
}
