package core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class WeightedObjectGroup
{
	private String creationKey;
	private ArrayList<ObjectGroup> groups;
	private ArrayList<Double> weights;
	
	private WeightedObjectGroup()
	{
		groups = new ArrayList<ObjectGroup>();
		weights = new ArrayList<Double>();
	}
	
	public WeightedObjectGroup(ObjectGroup group)
	{
		creationKey = group.getCreationKey();
		groups.add(group);
		weights.add(1.0);
	}
	
	public WeightedObjectGroup(ObjectClassesFile classData, String creationKey) throws ParseException
	{
		this();
		this.creationKey = creationKey;
		
		// Standard object group
		if (!creationKey.contains("<"))
		{
			groups.add(classData.buildObjectGroup(creationKey, -1));
			weights.add(1.0);
			return;
		}

		// Parse creation key into groups and weights
		String key = creationKey.trim();
		while (key.length() > 0)
		{
			int i = key.indexOf('<');
			if (i == -1) {
				if (key != "")
					throw new ParseException("Missing percent for final group \"" + key + "\"", -1);
				break;
			}
			String groupKey = key.substring(0, i).trim();
			key = key.substring(i + 1);
			groups.add(classData.buildObjectGroup(groupKey, -1));
			i = key.indexOf('>');
			if (i == -1)
				throw new ParseException("Unmatched <", -1);
			String groupWeight = key.substring(0, i).trim();
			key = key.substring(i + 1);
			try 
			{
				weights.add(Double.parseDouble(groupWeight));
			}
			catch (NumberFormatException e)
			{
				throw new ParseException("Could not interpret group weight \"" + groupWeight + "\" as a number.", -1);
			}
		}
		
		// Normalize weights
		float total = 0;
		for (Double weight : weights)
			total += weight;
		for (int i = 0; i < weights.size(); i++)
			weights.set(i, weights.get(i) / total);
	}
	
	public String getCreationKey()
	{
		return creationKey;
	}
	
	public boolean hasObject(int bankObj)
	{
		for (ObjectGroup group : groups)
			if (group.hasObject(bankObj))
				return true;
		return false;
	}
	
	public int size()
	{
		ObjectGroup all = new ObjectGroup();
		for (ObjectGroup group : groups)
			all = all.combineWith(group);
		return all.size();
	}
	
	public WeightedObjectGroup overlapWith(ObjectGroup mask)
	{
		WeightedObjectGroup ret = new WeightedObjectGroup();
		for (ObjectGroup group : groups)
			ret.groups.add(group.overlapWith(mask));
		ret.weights = weights;
		return ret;
	}
	
	public int randomObject(Random rand)
	{
		double roll = rand.nextDouble();
		double total = 0;
		for (int i = 0; i < weights.size(); i++)
		{
			total += weights.get(i);
			if (total > roll)
				return groups.get(i).randomObject(rand);
		}
		return groups.get(groups.size() - 1).randomObject(rand);
	}
	
	public ArrayList<Integer> randomlyFillList(int length, Random rand)
	{
		// Calculate scale required on the weights to ensure every object appears at least once
		double scale = 1.0;
		for (int i = 0; i < weights.size(); i++)
		{
			double s = ((double) groups.get(i).size()) / weights.get(i);
			if (s > scale)
				scale = s;
		}
		
		// Generate weighted array of objects
		ArrayList<Integer> weightedObjects = new ArrayList<Integer>();
		for (int i = 0; i < weights.size(); i++)
		{
			int size = (int) Math.round(weights.get(i) * scale);
			weightedObjects.addAll(groups.get(i).randomlyFillList(size, rand));
		}
		
		// Fill array of desired length
		ArrayList<Integer> ret = new ArrayList<Integer>();
		while (ret.size() < length)
		{
			Collections.shuffle(weightedObjects);
			ret.addAll(weightedObjects);
		}
		return ret;
	}
}
