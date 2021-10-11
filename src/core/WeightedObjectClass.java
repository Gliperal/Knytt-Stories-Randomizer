package core;

import java.util.ArrayList;

public class WeightedObjectClass
{
	private String creationKey;
	private ArrayList<ObjectClass> classes;
	private ArrayList<Float> weights;

	public WeightedObjectClass(ObjectClassesFile classData, String creationKey)
	{
		this.creationKey = creationKey;
	}
}
