package core;

import java.util.Comparator;

public class ObjectClass
{
	public char id;
	public String name;
	public String category;
	public ObjectGroup group;
	
	// all base classes have a character id and optionally a name and category
	// for combined classes, id is 0 and key is used instead
	public ObjectClass(char id, String name, ObjectGroup group)
	{
		this.id = Character.toUpperCase(id);
		this.name = name;
		this.group = group;
	}
	
	public ObjectClass(char id, String name, String category, ObjectGroup group)
	{
		this(id, name, group);
		this.category = category;
	}
	
	public boolean hasID(char id)
	{
		return this.id == Character.toUpperCase(id);
	}
	
	public String indentifier()
	{
		return Character.toUpperCase(id) + ": " + name;
	}
	
	public String getCategory()
	{
		return category;
	}
	
	public String toString()
	{
		String result;
			if (category == null)
				result = "ObjectClass(Base: " + id + ", " + name + ") with ";
			else
				result = "ObjectClass(Base: " + id + ", " + name + ", " + category + ") with ";
		return result + group.toString() + "]";
	}
	
	public static class ObjectClassComparator implements Comparator<ObjectClass>
	{
		@Override
		public int compare(ObjectClass a, ObjectClass b)
		{
			return a.cmp(b);
		}
	}
	
	public int cmp(ObjectClass b)
	{
		return id - b.id;
	}
}
