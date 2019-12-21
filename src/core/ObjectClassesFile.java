package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class ObjectClassesFile
{
	private int lineNumber; // For use with the constructor error messages
	private ArrayList<ObjectClass> classes;
	
	private String readLine(BufferedReader br) throws IOException
	{
		String line = "";
		while (line.isEmpty())
		{
			lineNumber++;
			line = br.readLine();
			if (line == null)
				return line;
			line = line.trim();
		}
		return line;
	}
	
	private ObjectClass addObject(ObjectClass oc, String object)
	{
		if (!object.isEmpty())
			if (!oc.add(object))
			{
				// If it's not an object, see if it's an object class ID
				if (object.length() == 1)
					for (ObjectClass otherClass : classes)
						if (otherClass.hasID(object.charAt(0)))
						{
							// if so, add all the objects from that class
							return oc.combineWith(otherClass);
						}
				// If it's not, return null
				return null;
			}
		return oc;
	}
	
	public ObjectClassesFile(Path ocFile) throws Exception
	{
		classes = new ArrayList<ObjectClass>();
		String fileName = ocFile.getFileName().toString();
		BufferedReader br = Files.newBufferedReader(ocFile);
		lineNumber = -1;
		String line;
		int i;
		while (true)
		{
			// First character should be a char ID for the object class
			line = readLine(br);
			if (line == null)
				break;
			char id = line.charAt(0);
			line = line.substring(1);
			
			// Check that no other object classes have the same id
			for (ObjectClass oc : classes)
				if (oc.hasID(id))
					throw new Exception(fileName + " Line " + lineNumber + ": Multiple object classes with id " + id + ".");
			
			// The next character should be a :
			if (line.isEmpty())
				line = readLine(br);
			if (line == null)
				throw new Exception(fileName + " Line " + lineNumber + ": Unexpected end of file.");
			if (line.charAt(0) == ':')
				line = line.substring(1);
			else
				throw new Exception(fileName + " Line " + lineNumber + ": Expected \":\" after object class id.");
			
			// Read object class name
			if (line.isEmpty())
				line = readLine(br);
			if (line == null)
				throw new Exception(fileName + " Line " + lineNumber + ": Unexpected end of file.");
			i = line.indexOf("{");
			String name;
			if (i == -1)
			{
				// if no { is present, it should be the first character on the next line
				name = line;
				line = readLine(br);
				if (line == null)
					throw new Exception(fileName + " Line " + lineNumber + ": Unexpected end of file.");
				if (line.startsWith("{"))
					line = line.substring(1);
				else
					throw new Exception(fileName + " Line " + lineNumber + ": Expected \"{\" after object class name.");
			}
			else
			{
				name = line.substring(0, i).trim();
				line = line.substring(i + 1);
			}
			
			// Create a new object class
			ObjectClass oc = new ObjectClass(id, name);
			
			// Add the objects
			while (true)
			{
				i = line.indexOf(",");
				if (i != -1)
				{
					// Found an object
					String object = line.substring(0, i).trim();
					oc = addObject(oc, object);
					if (oc == null)
						throw new Exception(fileName + " Line " + lineNumber + ": Couldn't parse object format: \"" + object + "\"");
					line = line.substring(i + 1).trim();
					continue;
				}
				// No commas left, so check for end of class
				i = line.indexOf("}");
				if (i != -1)
				{
					// End of class
					String object = line.substring(0, i).trim();
					oc = addObject(oc, object);
					if (oc == null)
						throw new Exception(fileName + " Line " + lineNumber + ": Couldn't parse object format: \"" + object + "\"");
					break;
				}
				// No commas left and no }, so load more data
				String more = readLine(br);
				if (more == null)
					throw new Exception(fileName + " Line " + lineNumber + ": Unexpected end of file.");
				line += more;
			}
			
			// Finish the object class and move on to the next
			oc.sort();
			classes.add(oc);
		}
		// Sort the groups alphabetically
		sort();
		
		// Close the reader
		br.close();
	}
	
	private void sort()
	{
		classes.sort(new ObjectClass.ObjectClassComparator());
	}
	
	public ObjectClass group(String groupStr) throws Exception
	{
		ObjectClass result = null;
		
		for (char classID : groupStr.toCharArray())
		{
			boolean badClassID = true;
			for (ObjectClass oc : classes)
				if (oc.hasID(classID))
				{
					badClassID = false;
					if (result == null)
						result = oc;
					else
						result = result.combineWith(oc);
				}
			if (badClassID)
				throw new Exception("No object class matching ID " + classID);
		}
		
		return result;
	}
	
	public void printEverything()
	{
		for (ObjectClass oc : classes)
			System.out.println(oc);
	}

	public void tabPrintClasses()
	{
		for (ObjectClass oc : classes)
			System.out.println("\t" + oc.indentifier());
	}
}
