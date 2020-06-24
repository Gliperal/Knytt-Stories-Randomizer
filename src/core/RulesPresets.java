package core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import util.AlphabeticalComparator;
import util.Util;

public class RulesPresets
{
	private HashMap<String, JSONArray> presets;
	
	public RulesPresets()
	{
		presets = new HashMap<String, JSONArray>();
	}
	
	public RulesPresets(JSONObject data) throws JSONException
	{
		this();
		for (String key : data.keySet())
			presets.put(key, data.getJSONArray(key));
	}
	
	private String selectPreset(Scanner input, String prompt)
	{
		ArrayList<String> presetNames = new ArrayList<String>(presets.keySet());
		presetNames.sort(new AlphabeticalComparator());
		Console.printString("Available presets:");
		Util.displayListConsicesly(presetNames, 8, 6);
		while (true)
		{
			Console.printString(prompt);
			String search = input.nextLine();
			if (search.isEmpty())
			{
				Console.printString("Cancelled.");
				return null;
			}
			// TODO containsKey case insensitive
			if (presets.containsKey(search))
				return search;
			ArrayList<Integer> matches = Util.keywordMatch(presetNames, search.split("\\s+"));
			if (matches.size() == 0)
			{
				Console.printString("Found no presets matching \"" + search + "\"");
				continue;
			}
			Console.printString("Presets matching \"" + search + "\"");
			for (int i : matches)
				Console.printString(presetNames.get(i));
		}
	}
	
	public boolean addPreset(Scanner input, ArrayList<RandoRule> rules)
	{
		String name;
		while (true)
		{
			Console.printString("Enter a name for this preset, or nothing to cancel.");
			name = input.nextLine().trim();
			if (name.isEmpty())
				return false;
			if (!presets.containsKey(name))
				break;
			if (UserInput.getBooleanInput(input, "Preset \"" + name + "\" already exists. Overwrite?"))
				break;
		}
		JSONArray preset = new JSONArray();
		for (RandoRule rule : rules)
			preset.put(rule.saveToString());
		presets.put(name, preset);
		return true;
	}
	
	public ArrayList<RandoRule> loadPreset(Scanner input, ObjectClassesFile classData) throws ParseException
	{
		if (presets.isEmpty())
		{
			Console.printString("No presets available.");
			return null;
		}
		String name = selectPreset(input, "Enter the name of a preset to load, a string to search, or nothing to cancel.");
		if (name == null)
			return null;
		JSONArray preset = presets.get(name);
		ArrayList<RandoRule> rules = new ArrayList<RandoRule>();
		for (int i = 0; i < preset.length(); i++)
		{
			String ruleStr = preset.getString(i);
			RandoRule rule = RandoRule.loadFromString(ruleStr, classData);
			rules.add(rule);
		}
		Console.printString("Preset loaded.");
		return rules;
	}
	
	public JSONObject toJSON()
	{
		JSONObject presetsJSON = new JSONObject();
		for (String key : presets.keySet())
			presetsJSON.put(key, presets.get(key));
		return presetsJSON;
	}

	public boolean deletePreset(Scanner input)
	{
		if (presets.isEmpty())
		{
			Console.printString("No presets to delete.");
			return false;
		}
		String name = selectPreset(input, "Enter the name of a preset to delete, a string to search, or nothing to cancel.");
		if (name == null)
			return false;
		presets.remove(name);
		return true;
	}
	
	public boolean isEmpty()
	{
		return presets.isEmpty();
	}
}
