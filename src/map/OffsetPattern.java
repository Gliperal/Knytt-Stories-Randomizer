package map;

public class OffsetPattern implements Pattern
{
	private int xOffset;
	private int yOffset;
	private char special; // S = solid, N = non-solid
	private Pattern objects;

	public OffsetPattern(int xOff, int yOff, char special)
	{
		xOffset = xOff;
		yOffset = yOff;
		this.special = special;
	}

	public OffsetPattern(int xOff, int yOff, Pattern objects)
	{
		xOffset = xOff;
		yOffset = yOff;
		this.objects = objects;
	}

	public boolean matches(Screen s, int layer, int x, int y)
	{
		x += xOffset;
		y += yOffset;
		if (x < 0 || x > 24 || y < 0 || y > 9)
			return false;
		if (special == 'S')
			return (s.tileAt(3, x, y) & 0x7f) != 0;
		if (special == 'N')
			return (s.tileAt(3, x, y) & 0x7f) == 0;
		return objects.matches(s, layer, x, y);
	}

	public ObjectPattern simplify(KSMap map)
	{
		ObjectPattern objects = new ObjectPattern();
		for (MapObject obj : map.find(this))
			objects.add(obj.bank, obj.object);
		return objects;
	}

	public Pattern subtract(Pattern that)
	{
		return new CombinedPattern('-', this, that);
	}

	public Pattern and(Pattern that)
	{
		return new CombinedPattern('&', this, that);
	}

	public Pattern or(Pattern that)
	{
		return new CombinedPattern('|', this, that);
	}
}
