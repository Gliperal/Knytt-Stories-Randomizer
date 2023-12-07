package map;

public class LoadException extends Exception {
	private static final long serialVersionUID = 1L;

	public LoadException()
	{
		super("Failed to load Knytt Stories files.");
	}

	public LoadException(String message)
	{
		super(message);
	}
}
