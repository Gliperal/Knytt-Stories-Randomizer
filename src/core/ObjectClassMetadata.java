package core;

public class ObjectClassMetadata
{
	public String id;
	public String name;
	public String category;

	public ObjectClassMetadata(String id, String name)
	{
		this.id = id;
		this.name = name;
	}

	public ObjectClassMetadata(String id, String name, String category)
	{
		this(id, name);
		this.category = category;
	}
}
