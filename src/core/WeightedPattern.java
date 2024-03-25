package core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import map.KSMap;
import map.MapObject;
import map.ObjectPattern;
import map.Pattern;

public class WeightedPattern
{
	public ArrayList<Pattern> patterns;
	private ArrayList<ObjectPattern> simplifiedPatterns;
	private ArrayList<Double> weights;
	private double totalWeight = 0.0;

	private WeightedPattern()
	{
		patterns = new ArrayList<Pattern>();
		weights = new ArrayList<Double>();
	}

	public WeightedPattern(Pattern group)
	{
		this();
		patterns.add(group);
		weights.add(1.0);
		totalWeight = 1.0;
	}

	public WeightedPattern(ObjectClassesFile classData, String creationKey) throws ParseException
	{
		this();

		// Standard object group
		if (!creationKey.contains("<"))
		{
			patterns.add(classData.buildObjectGroup(creationKey));
			weights.add(1.0);
			totalWeight = 1.0;
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
			patterns.add(classData.buildObjectGroup(groupKey));
			i = key.indexOf('>');
			if (i == -1)
				throw new ParseException("Unmatched <", -1);
			String groupWeight = key.substring(0, i).trim();
			key = key.substring(i + 1);
			try
			{
				double weight = Double.parseDouble(groupWeight);
				weights.add(weight);
				totalWeight += weight;
			}
			catch (NumberFormatException e)
			{
				throw new ParseException("Could not interpret group weight \"" + groupWeight + "\" as a number.", -1);
			}
		}
	}

	public WeightedPattern overlapWith(ObjectPattern mask)
	{
		WeightedPattern ret = new WeightedPattern();
		for (Pattern group : patterns)
			ret.patterns.add(group.and(mask));
		ret.weights = weights;
		return ret;
	}

	public ArrayList<MapObject> findAll(KSMap map)
	{
		// TODO Weight this somehow (for shuffle)?
		Pattern allPatterns = patterns.get(0);
		for (int i = 1; i < patterns.size(); i++)
			allPatterns = allPatterns.and(patterns.get(i));
		return map.find(allPatterns);
	}

	/**
	 * Simplifies all of the Patterns into ObjectPatterns. Must be called before randomObject or randomlyFillList.
	 * @param map The map to use for the pattern simplification.
	 * @return 0 if successful, 1 if successful but some empty groups were deleted, -1 if all groups were deleted
	 */
	public int prepForRandomization(KSMap map)
	{
		simplifiedPatterns = new ArrayList<ObjectPattern>();
		boolean groupsDeleted = false;
		for (int i = 0; i < patterns.size(); i++)
		{
			ObjectPattern x = patterns.get(i).simplify(map);
			if (x.size() == 0)
				simplifiedPatterns.add(x);
			else
			{
				// Delete any groups that become empty
				weights.remove(i);
				i--;
				groupsDeleted = true;
				// Hack to prevent from being used for any other purposes in the future, since groups and weights might be mismatched
				patterns = null;
			}
		}
		// Return 1 if some groups were deleted or -1 if all were deleted
		if (simplifiedPatterns.size() == 0)
			return -1;
		return groupsDeleted ? 1 : 0;
	}

	public int randomObject(Random rand)
	{
		double roll = rand.nextDouble() * totalWeight;
		double total = 0;
		for (int i = 0; i < weights.size(); i++)
		{
			total += weights.get(i);
			if (total > roll)
				return simplifiedPatterns.get(i).randomObject(rand);
		}
		return simplifiedPatterns.get(simplifiedPatterns.size() - 1).randomObject(rand);
	}

	public ArrayList<Integer> randomlyFillList(int length, Random rand)
	{
		// Calculate scale required on the weights to ensure every object appears at least once
		double scale = 1.0;
		for (int i = 0; i < weights.size(); i++)
		{
			double s = ((double) simplifiedPatterns.get(i).size()) / weights.get(i);
			if (s > scale)
				scale = s;
		}

		// Generate weighted array of objects
		ArrayList<Integer> weightedObjects = new ArrayList<Integer>();
		for (int i = 0; i < weights.size(); i++)
		{
			int size = (int) Math.round(weights.get(i) * scale);
			weightedObjects.addAll(simplifiedPatterns.get(i).randomlyFillList(size, rand));
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
