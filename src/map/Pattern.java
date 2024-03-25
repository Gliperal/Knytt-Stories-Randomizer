package map;

public interface Pattern
{
	public boolean matches(Screen s, int layer, int x, int y);
	/**
	 * Returns a new ObjectPattern that contains the essence of this pattern. For example, simplifying a subtract CombinedPattern that deducts an airborne OffetPattern from an enemy ObjectPattern will return an ObjectPattern containing only enemy types that never appear in the air.
	 * @param map The map on which to apply the Pattern.
	 * @return The simplified ObjectPattern.
	 */
	public ObjectPattern simplify(KSMap map);

	public Pattern and(Pattern that);
	public Pattern or(Pattern that);
	public Pattern subtract(Pattern that);
}
