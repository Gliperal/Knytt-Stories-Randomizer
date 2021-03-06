package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import util.Util;

// TODO Make this into a singleton?
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
	
	private ObjectClass getObjectClass(char id)
	{
		for (ObjectClass oc : classes)
			if (oc.hasID(id))
				return oc;
		return null;
	}
	
	private ObjectClassMetadata parseHeader(String header, int line) throws ParseException
	{
		// Read until the first non-whitespace
		int i = 0;
		while (Character.isWhitespace(header.charAt(i)))
		{
			if (header.charAt(i) == '\n')
				line++;
			i++;
			if (i == header.length())
				throw new ParseException("Expected id for object class.", line);
		}
		
		// Next character should be a char ID for the object class
		char id = header.charAt(i);
		
		// Check that no other object classes have the same id
		for (ObjectClass oc : classes)
			if (oc.hasID(id))
				throw new ParseException("Multiple object classes with id " + id + ".", line);
		
		// Read until the first non-whitespace (if none, the object class only has an id and no name)
		i++;
		while (Character.isWhitespace(header.charAt(i)))
		{
			if (header.charAt(i) == '\n')
				line++;
			i++;
			if (i == header.length())
				return new ObjectClassMetadata(id, null);
		}
		
		// The next character should be a :
		if (header.charAt(i) != ':')
			throw new ParseException("Expected \":\" after object class id.", line);
		
		// Read until the first non-whitespace
		i++;
		while (Character.isWhitespace(header.charAt(i)))
		{
			if (header.charAt(i) == '\n')
				line++;
			i++;
			if (i == header.length())
				throw new ParseException("Expected name for object class after :", line);
		}
		
		// Split on the next : (if it exists), separating name and category
		int split = header.indexOf(':', i);
		if (split != -1)
		{
			String name = header.substring(i, split).trim();
			String category = header.substring(split + 1).trim();
			return new ObjectClassMetadata(id, name, category);
		}
		
		// If not, then just read the name
		String name = header.substring(i).trim();
		return new ObjectClassMetadata(id, name);
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
		
		public String toString()
		{
			switch(type)
			{
			case group:
				return "G";
			case and:
				return "&";
			case minus:
				return "-";
			case plus:
				return "+";
			case openParen:
				return "(";
			case closeParen:
				return ")";
			default:
				return ".";
			}
		}
	}
	
	private void evaluateTokens(ArrayList<Token> tokens, int start) throws ParseException
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
					throw new ParseException("Unclosed parenthesis.", tokens.get(i).lineNumber);
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
					throw new ParseException("& with no left side", token.lineNumber);
				if (i == tokens.size() - 1 || tokens.get(i + 1).type != TokenType.group)
					throw new ParseException("& with no right side", token.lineNumber);
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
					throw new ParseException("Minus with no left side", token.lineNumber);
				if (i == tokens.size() - 1 || tokens.get(i + 1).type != TokenType.group)
					throw new ParseException("Minus with no right side", token.lineNumber);
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
					throw new ParseException("+ with no left side", token.lineNumber);
				if (i == tokens.size() - 1 || tokens.get(i + 1).type != TokenType.group)
					throw new ParseException("+ with no right side", token.lineNumber);
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
		if (tokens.size() <= start)
			throw new ParseException("Unknown error processing group.", -1);
		Token first = tokens.get(start);
		if (first.type != TokenType.group) // then it has to be a closing parenthesis
			throw new ParseException("Empty group.", first.lineNumber);
		if (tokens.size() == start + 1 || tokens.get(start + 1).type != TokenType.closeParen)
			throw new ParseException("Unbalanced parenthesis.", first.lineNumber);
		
		// Remove ) and return
		tokens.remove(start + 1);
	}
	
	public ObjectClass buildObjectClass(String key, int line) throws ParseException
	{
		return buildObjectClass(new ObjectClassMetadata(' ', "auto-generated object class"), key, line);
	}
	
	public ObjectClass buildObjectClass(ObjectClassMetadata metadata, String key, int line) throws ParseException
	{
		// Tokenize
		int i = 0;
		ArrayList<Token> tokens = new ArrayList<Token>();
		while (i < key.length())
		{
			char c = key.charAt(i);
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
					int j = key.indexOf(':', i);
					int bank = Integer.parseInt(key.substring(i, j));
					int k = j + 1;
					while (k < key.length() && Character.isDigit(key.charAt(k)))
						k++;
					int object = Integer.parseInt(key.substring(j + 1, k));
					// assign the same id and name to all the tokens (eventually they'll be merged down into one)
					ObjectClass oc = new ObjectClass(' ', "temporary partial class");
					oc.add((byte) bank, (byte) object);
					tokens.add(new Token(oc, line));
					i = k;
				}
				catch (Exception e)
				{
					String snippet = Util.trimStringForPrinting(key.substring(i));
					throw new ParseException("Couldn't parse object format near \"" + snippet + "\"", line);
				}
			}
			else
			{
				ObjectClass oc = getObjectClass(c);
				if (oc == null)
				{
					String snippet = Util.trimStringForPrinting(key.substring(i));
					throw new ParseException("Couldn't parse object format near \"" + snippet + "\"", line);
				}
				tokens.add(new Token(oc, line));
				i++;
			}
		}
		if (tokens.isEmpty())
			throw new ParseException("Empty object class.", line);
		
		tokens.add(new Token(TokenType.closeParen, line)); // add ) to indicate end of expression
		evaluateTokens(tokens, 0);
		if (tokens.size() > 1)
			throw new ParseException("Unbalanced parenthesis.", tokens.get(0).lineNumber);
		
		// The last remaining token has our finished object class
		ObjectClass ret = tokens.get(0).group;
		ret = new ObjectClass(metadata.id, metadata.name, metadata.category).combineWith(ret);
		ret.sort();
		return ret;
	}
	
	public ObjectClassesFile(Path ocFile) throws IOException, ParseException
	{
		classes = new ArrayList<ObjectClass>();
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
					throw new ParseException("Unexpected end of file.", lineNumber);
			}
			
			// Parse the header
			i = header.indexOf('{');
			ObjectClassMetadata groupID = parseHeader(header.substring(0, i), startLine);
			
			// Read until the next {
			String body = header.substring(i + 1);
			startLine = lineNumber;
			while (body.indexOf('}') == -1)
			{
				line = readLine(br);
				if (line == null)
					throw new ParseException("Expected \"}\" to match \"}\"", startLine);
				body += "\n" + line;
			}
			
			// Parse the body
			i = body.indexOf('}');
			classes.add(buildObjectClass(groupID, body.substring(0, i), startLine));
			line = body.substring(i + 1);
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
		
		return result.addCreationKey(groupStr);
	}
	
	public void printEverything()
	{
		for (ObjectClass oc : classes)
			Console.printString(oc.toString());
	}
	
	public void tabPrintClasses()
	{
		Map<String, ArrayList<ObjectClass>> categories = new HashMap<String, ArrayList<ObjectClass>>();
		for (ObjectClass oc : classes)
		{
			String category = oc.getCategory();
			if (categories.containsKey(category))
				categories.get(category).add(oc);
			else
			{
				ArrayList<ObjectClass> categoryObjs = new ArrayList<ObjectClass>();
				categoryObjs.add(oc);
				categories.put(category, categoryObjs);
			}
		}
		for (String category : categories.keySet())
		{
			ArrayList<ObjectClass> categoryObjs = categories.get(category);
			if (category == null)
				category = "MISCELLANEOUS";
			String str = "-- " + category.toUpperCase() + " --";
			while (str.length() < Console.MAX_LENGTH)
				str += '-';
			Console.printString(str);
			for (ObjectClass oc : categoryObjs)
				Console.printString("\t" + oc.indentifier());
		}
	}
}
