package map;

import java.util.ArrayList;

public class CombinedPattern implements Pattern
{
	private ArrayList<Pattern> patterns;
	private char operator;

	public CombinedPattern(char operator, Pattern left, Pattern right)
	{
		this.operator = operator;
		patterns = new ArrayList<Pattern>();
		patterns.add(left);
		patterns.add(right);
	}

	public Pattern subtract(Pattern that)
	{
		return new CombinedPattern('-', this, that);
	}

	// TODO Simplify if one of the patterns is already &
	public Pattern and(Pattern that)
	{
		return new CombinedPattern('&', this, that);
	}

	// TODO Simplify if one of the patterns is already |
	public Pattern or(Pattern that)
	{
		return new CombinedPattern('|', this, that);
	}

	public boolean matches(Screen s, int layer, int x, int y)
	{
		switch (operator)
		{
		case '|':
			for (Pattern p : patterns)
				if (p.matches(s, layer, x, y))
					return true;
			return false;
		case '&':
			for (Pattern p : patterns)
				if (!p.matches(s, layer, x, y))
					return false;
			return true;
		case '-':
			return patterns.get(0).matches(s, layer, x, y) && !patterns.get(1).matches(s, layer, x, y);
		default:
			return false;
		}
	}

	public ObjectPattern simplify(KSMap map)
	{
		ObjectPattern p = patterns.get(0).simplify(map);
		switch (operator)
		{
		case '|':
			for (int i = 1; i < patterns.size(); i++)
				p = p.or(patterns.get(i).simplify(map));
			return p;
		case '&':
			for (int i = 1; i < patterns.size(); i++)
				p = p.and(patterns.get(i).simplify(map));
			return p;
		case '-':
			return p.subtract(patterns.get(1).simplify(map));
		default:
			return null;
		}
	}
}
