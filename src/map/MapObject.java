package map;

import util.Util;

public class MapObject
{
	public Screen screen;
	public int layer;
	public int x;
	public int y;
	public byte bank;
	public byte object;

	public MapObject(Screen screen, int layer, int x, int y, byte bank, byte object)
	{
		this.screen = screen;
		this.layer = layer;
		this.x = x;
		this.y = y;
		this.bank = bank;
		this.object = object;
	}

	public void replace(byte newBank, byte newObj)
	{
		screen.setObject(layer, x, y, newBank, newObj);
	}

	public void replace(int newObject)
	{
		replace(Util.separateBank(newObject), Util.separateObj(newObject));
	}
}
