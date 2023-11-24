package core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import util.AlphabeticalComparator;

public class RulesPresets
{
	private HashMap<String, JSONArray> presets;

	public RulesPresets()
	{
		presets = new HashMap<String, JSONArray>();
	}

	public RulesPresets(Path file) throws JSONException, IOException
	{
		this();
		byte[] settingsBytes = Files.readAllBytes(file);
		String settingsString = new String(settingsBytes);
		JSONObject data = new JSONObject(settingsString);
		for (String key : data.keySet())
			presets.put(key, data.getJSONArray(key));
	}

	private String selectPreset(Scanner input, String prompt)
	{
		ArrayList<String> presetNames = new ArrayList<String>(presets.keySet());
		presetNames.sort(new AlphabeticalComparator());
		int id = UserInput.getInputFromList(input, prompt, "preset", presetNames);
		if (id == -1)
			return null;
		return presetNames.get(id);
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
		String name = selectPreset(input, "Select a preset to load.");
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

	private JSONObject toJSON()
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
		String name = selectPreset(input, "Choose a preset to delete.");
		if (name == null)
			return false;
		presets.remove(name);
		return true;
	}

	public boolean isEmpty()
	{
		return presets.isEmpty();
	}

	public void saveToFile(Path file) throws IOException
	{
		Files.write(file, toJSON().toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}
}
