package core;

import java.util.Comparator;

public class ObjectClass
{
	public String id;
	public String name;
	public String category;
	public ObjectGroup group;
	
	// all base classes have a string id and optionally a name and category
	public ObjectClass(String id, String name, ObjectGroup group)
	{
		this.id = formatID(id);
		this.name = name;
		this.group = group;
	}
	
	public ObjectClass(String id, String name, String category, ObjectGroup group)
	{
		this(id, name, group);
		this.category = category;
	}

	public static String formatID(String id)
	{
		return id.substring(0, 1).toUpperCase() + id.substring(1).toLowerCase();
	}
	
	public boolean hasID(String id)
	{
		return this.id == formatID(id);
	}
	
	public String indentifier()
	{
		return id + ": " + name;
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
		return id.compareTo(b.id);
	}
}
