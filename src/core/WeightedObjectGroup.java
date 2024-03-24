package core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import map.KSMap;
import map.MapObject;
import map.ObjectPattern;
import map.Pattern;

public class WeightedObjectGroup
{
	public ArrayList<Pattern> groups;
	private ArrayList<ObjectPattern> objectsThatMatchForEachGroup;
	private ArrayList<Double> weights;

	private WeightedObjectGroup()
	{
		groups = new ArrayList<Pattern>();
		weights = new ArrayList<Double>();
	}

	public WeightedObjectGroup(Pattern group)
	{
		this();
		groups.add(group);
		weights.add(1.0);
	}

	public WeightedObjectGroup(ObjectClassesFile classData, String creationKey) throws ParseException
	{
		this();

		// Standard object group
		if (!creationKey.contains("<"))
		{
			groups.add(classData.buildObjectGroup(creationKey));
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
			groups.add(classData.buildObjectGroup(groupKey));
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

	public WeightedObjectGroup overlapWith(ObjectPattern mask)
	{
		WeightedObjectGroup ret = new WeightedObjectGroup();
		for (Pattern group : groups)
			ret.groups.add(group.and(mask));
		ret.weights = weights;
		return ret;
	}

	public ArrayList<MapObject> findAll(KSMap map)
	{
		// TODO Weight this somehow (for shuffle)?
		Pattern allPatterns = groups.get(0);
		for (int i = 1; i < groups.size(); i++)
			allPatterns = allPatterns.and(groups.get(i));
		return map.find(allPatterns);
	}

	/**
	 * Simplifies all of the Patterns into ObjectPatterns. Must be called before randomObject or randomlyFillList.
	 * @param map The map to use for the pattern simplification.
	 * @return 0 if successful, 1 if successful but some empty groups were deleted, -1 if all groups were deleted
	 */
	public int prepForRandomization(KSMap map)
	{
		objectsThatMatchForEachGroup = new ArrayList<ObjectPattern>();
		for (int i = 0; i < groups.size(); i++)
		{
			if (groups.get(i) instanceof ObjectPattern)
			{
				// Object patterns are special: treat them as purely a list of object ids
				objectsThatMatchForEachGroup.add((ObjectPattern) groups.get(i));
				continue;
			}
			// For all other patterns, search the map for all objects that match
			ObjectPattern mapObjects = new ObjectPattern();
			for (MapObject obj : map.find(groups.get(i)))
				mapObjects.add(obj.bank, obj.object);
			objectsThatMatchForEachGroup.add(mapObjects);
		}
		// TODO Delete any groups that become empty. Return 1 if some were deleted or -1 if all were deleted
		return 0;
	}

	public int randomObject(Random rand)
	{
		double roll = rand.nextDouble();
		double total = 0;
		for (int i = 0; i < weights.size(); i++)
		{
			total += weights.get(i);
			if (total > roll)
				return objectsThatMatchForEachGroup.get(i).randomObject(rand);
		}
		return objectsThatMatchForEachGroup.get(objectsThatMatchForEachGroup.size() - 1).randomObject(rand);
	}

	public ArrayList<Integer> randomlyFillList(int length, Random rand)
	{
		// Calculate scale required on the weights to ensure every object appears at least once
		double scale = 1.0;
		for (int i = 0; i < weights.size(); i++)
		{
			double s = ((double) objectsThatMatchForEachGroup.get(i).size()) / weights.get(i);
			if (s > scale)
				scale = s;
		}

		// Generate weighted array of objects
		ArrayList<Integer> weightedObjects = new ArrayList<Integer>();
		for (int i = 0; i < weights.size(); i++)
		{
			int size = (int) Math.round(weights.get(i) * scale);
			weightedObjects.addAll(objectsThatMatchForEachGroup.get(i).randomlyFillList(size, rand));
		}

		// Fill array of desired length
		ArrayList<Integer> ret = new ArrayList<Integer>();
		while (ret.size() < length)
		{
			Collections.shuffle(weightedObjects, rand);
			ret.addAll(weightedObjects);
		}
		return ret;
	}
}
