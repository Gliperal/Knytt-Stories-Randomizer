package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

// TODO Make this into a singleton?
public class ObjectClassesFile
{
	private String fileName;
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
	
	private ObjectClass getObjectClass(char id)
	{
		for (ObjectClass oc : classes)
			if (oc.hasID(id))
				return oc;
		return null;
	}
	
	private ObjectClass parseHeader(String header, int line) throws Exception
	{
		// Read until the first non-whitespace
		int i = 0;
		while (Character.isWhitespace(header.charAt(i)))
		{
			if (header.charAt(i) == '\n')
				line++;
			i++;
			if (i == header.length())
				throw new Exception(fileName + " Line " + line + ": Expected id for object class.");
		}
		
		// Next character should be a char ID for the object class
		char id = header.charAt(i);
		
		// Check that no other object classes have the same id
		for (ObjectClass oc : classes)
			if (oc.hasID(id))
				throw new Exception(fileName + " Line " + line + ": Multiple object classes with id " + id + ".");
		
		// Read until the first non-whitespace (if none, the object class only has an id and no name)
		i++;
		while (Character.isWhitespace(header.charAt(i)))
		{
			if (header.charAt(i) == '\n')
				line++;
			i++;
			if (i == header.length())
			{
				System.out.println("id = " + id);
				return new ObjectClass(id, null); // TODO make sure name can be null in ObjectClass
			}
		}
		
		// The next character should be a :
		if (header.charAt(i) != ':')
			throw new Exception(fileName + " Line " + line + ": Expected \":\" after object class id.");
		
		// Read until the first non-whitespace
		i++;
		while (Character.isWhitespace(header.charAt(i)))
		{
			if (header.charAt(i) == '\n')
				line++;
			i++;
			if (i == header.length())
				throw new Exception(fileName + " Line " + line + ": Expected name for object class after :");
		}
		
		// Read object class name
		String name = header.substring(i).trim();
		
		// Create a new object class
		return new ObjectClass(id, name);
	}
	
	private enum TokenType { group, and, minus, plus, openParen, closeParen };
	private class Token
	{
		public TokenType type;
		public ObjectClass group;
		public int lineNumber;
		
		public Token(TokenType type, int lineNumber)
		{
			this.type = type;
		}
		
		public Token(ObjectClass group, int lineNumber)
		{
			this.type = TokenType.group;
			this.group = group;
		}
	}
	
	public void evaluateTokens(ArrayList<Token> tokens, int start) throws Exception
	{
		// Evaluate parenthesis
		for (int i = start; i < tokens.size(); i++)
		{
			TokenType type = tokens.get(i).type;
			if (type == TokenType.closeParen)
				break;
			if (type == TokenType.openParen)
			{
				if (i == tokens.size() - 1)
					throw new Exception(fileName + " Line " + tokens.get(i).lineNumber + ": Unclosed parenthesis.");
				evaluateTokens(tokens, i + 1);
				tokens.remove(i);		// remove open parenthesis (closing will be removed by evaluation)
			}
		}
		
		// Resolve all adjacencies
		for (int i = start; i < tokens.size() - 1;)
		{
			Token token = tokens.get(i);
			Token next = tokens.get(i + 1);
			if (token.type == TokenType.closeParen)
				break;
			if (token.type == TokenType.group && next.type == TokenType.group)
			{
				token.group = token.group.combineWith(next.group);
				tokens.remove(i + 1);
			}
			else
				i++;
		}
		
		// Resolve all ANDs
		for (int i = start; i < tokens.size();)
		{
			Token token = tokens.get(i);
			if (token.type == TokenType.closeParen)
				break;
			if (token.type == TokenType.and)
			{
				if (i == start || tokens.get(i - 1).type != TokenType.group)
					throw new Exception(fileName + " Line " + token.lineNumber + ": & with no left side");
				if (i == tokens.size() - 1 || tokens.get(i + 1).type != TokenType.group)
					throw new Exception(fileName + " Line " + token.lineNumber + ": & with no right side");
				Token left = tokens.get(i - 1);
				Token right = tokens.get(i + 1);
				left.group = left.group.overlapWith(right.group);
				tokens.remove(i + 1);
				tokens.remove(i);
			}
			else
				i++;
		}
		
		// Resolve all subtractions
		for (int i = start; i < tokens.size();)
		{
			Token token = tokens.get(i);
			if (token.type == TokenType.closeParen)
				break;
			if (token.type == TokenType.minus)
			{
				if (i == start || tokens.get(i - 1).type != TokenType.group)
					throw new Exception(fileName + " Line " + token.lineNumber + ": Minus with no left side");
				if (i == tokens.size() - 1 || tokens.get(i + 1).type != TokenType.group)
					throw new Exception(fileName + " Line " + token.lineNumber + ": Minus with no right side");
				Token left = tokens.get(i - 1);
				Token right = tokens.get(i + 1);
				left.group = left.group.eliminateFrom(right.group);
				tokens.remove(i + 1);
				tokens.remove(i);
			}
			else
				i++;
		}
		
		// Resolve all additions
		for (int i = start; i < tokens.size();)
		{
			Token token = tokens.get(i);
			if (token.type == TokenType.closeParen)
				break;
			if (token.type == TokenType.plus)
			{
				if (i == start || tokens.get(i - 1).type != TokenType.group)
					throw new Exception(fileName + " Line " + token.lineNumber + ": + with no left side");
				if (i == tokens.size() - 1 || tokens.get(i + 1).type != TokenType.group)
					throw new Exception(fileName + " Line " + token.lineNumber + ": + with no right side");
				Token left = tokens.get(i - 1);
				Token right = tokens.get(i + 1);
				left.group = left.group.combineWith(right.group);
				tokens.remove(i + 1);
				tokens.remove(i);
			}
			else
				i++;
		}
		
		// Error checking before return
		if (tokens.size() == 0)
			throw new Exception(fileName + ": Unknown error processing group.");
		Token first = tokens.get(0);
		if (first.type != TokenType.group) // then it has to be a closing parenthesis
			throw new Exception(fileName + " Line " + first.lineNumber + ": Empty group.");
		if (tokens.size() == 1 || tokens.get(1).type != TokenType.closeParen)
			throw new Exception(fileName + " Line " + first.lineNumber + ": Unbalanced parenthesis.");
		
		// Remove ) and return
		tokens.remove(1);
	}
	
	// TODO refactor: replace base with id and name
	public ObjectClass buildObjectClass(ObjectClass base, String str, int line) throws Exception
	{
		// Tokenize
		int i = 0;
		ArrayList<Token> tokens = new ArrayList<Token>();
		while (i < str.length())
		{
			char c = str.charAt(i);
			if (c == '\n')
			{
				line++;
				i++;
			}
			else if (c == ',' || Character.isWhitespace(c))
			{
				i++;
			}
			else if (c == '&')
			{
				tokens.add(new Token(TokenType.and, line));
				i++;
			}
			else if (c == '-')
			{
				tokens.add(new Token(TokenType.minus, line));
				i++;
			}
			else if (c == '+')
			{
				tokens.add(new Token(TokenType.plus, line));
				i++;
			}
			else if (c == '(')
			{
				tokens.add(new Token(TokenType.openParen, line));
				i++;
			}
			else if (c == ')')
			{
				tokens.add(new Token(TokenType.closeParen, line));
				i++;
			}
			else if (Character.isDigit(c))
			{
				try
				{
					int j = str.indexOf(':', i);
					int bank = Integer.parseInt(str.substring(i, j));
					int k = j + 1;
					while (Character.isDigit(str.charAt(k)) && k < str.length())
						k++;
					int object = Integer.parseInt(str.substring(j + 1, k));
					ObjectClass oc = new ObjectClass(' ', "auto-generated object class");
					oc.add((byte) bank, (byte) object);
					tokens.add(new Token(oc, line));
					i = k;
				}
				catch (Exception e)
				{
					throw new Exception(fileName + " Line " + line + ": Couldn't parse object format near \"" + str.substring(i) + "\""); // TODO this gets quite unruly for strings with newlines in them
				}
			}
			else
			{
				ObjectClass oc = getObjectClass(c);
				if (oc == null)
					throw new Exception(fileName + " Line " + line + ": Couldn't parse object format near \"" + str.substring(i) + "\"");
				tokens.add(new Token(oc, line));
				i++;
			}
		}
		if (tokens.isEmpty())
			throw new Exception(fileName + " Line " + line + ": Empty object class.");
		
		tokens.add(new Token(TokenType.closeParen, line)); // add ) to indicate end of expression
		evaluateTokens(tokens, 0);
		if (tokens.size() > 1)
			throw new Exception(fileName + " Line " + tokens.get(0).lineNumber + ": Unbalanced parenthesis.");
		
		// Finish the object class and move on to the next
		base = base.combineWith(tokens.get(0).group);
		base.sort();
		return base;
	}
	
	public ObjectClassesFile(Path ocFile) throws Exception
	{
		classes = new ArrayList<ObjectClass>();
		fileName = ocFile.getFileName().toString();
		BufferedReader br = Files.newBufferedReader(ocFile);
		lineNumber = -1;
		String line = "";
		int i;
		int startLine;
		while (true)
		{
			// Read until the next {
			String header = line;
			startLine = lineNumber;
			while (header.indexOf('{') == -1)
			{
				line = readLine(br);
				if (line == null)
					break;
				header += "\n" + line;
			}
			
			// End of file
			if (line == null)
			{
				if (header.matches("\\s*"))
					break;
				else
					throw new Exception(fileName + " Line " + lineNumber + ": Unexpected end of file.");
			}
			
			// Parse the header
			i = header.indexOf('{');
			ObjectClass oc = parseHeader(header.substring(0, i), startLine);

			// Read until the next {
			String body = header.substring(i + 1);
			startLine = lineNumber;
			while (body.indexOf('}') == -1)
			{
				line = readLine(br);
				if (line == null)
					throw new Exception(fileName + " Line " + startLine + ": Expected \"}\" to match \"}\"");
				body += "\n" + line;
			}
			
			// Parse the body
			i = body.indexOf('}');
			classes.add(buildObjectClass(oc, body.substring(0, i), startLine));
			line = body.substring(i + 1);
		}
		// Sort the groups alphabetically
		sort();
		
		// Close the reader
		br.close();
		printEverything();
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
