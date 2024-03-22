package map;

public interface Pattern
{
	public boolean matches(Screen s, int layer, int x, int y);

	public Pattern and(Pattern that);
	public Pattern or(Pattern that);
	public Pattern subtract(Pattern that);
}
