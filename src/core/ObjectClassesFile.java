package core;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import map.OffsetPattern;
import map.Pattern;
import util.Console;
import util.Util;

// TODO Make this into a singleton?
public class ObjectClassesFile
{
	private ArrayList<ObjectClass> classes;

	private ObjectClass getFirstObjectClass(String str)
	{
		for (ObjectClass oc : classes)
			if (str.startsWith(oc.id))
				return oc;
		return null;
	}

	private ObjectClassMetadata parseHeader(FilePiece header) throws ParseException
	{
		// Strip whitespace
		if (header.isBlank())
			throw new ParseException("Expected id for object class.", header.getLine());
		header = header.trim();

		// Split header into id, name, and category
		FilePiece[] split = header.split(':');
		if (split.length > 3)
			throw new ParseException("Too many \":\" in object class header.", header.getLine());

		// First element: id
		FilePiece id = split[0].trim();
		if (id.isBlank())
			throw new ParseException("Expected object class id.", id.getLine());
		int x = Util.firstIndexOf(id, "\n,&+-()");
		if (x != -1)
			throw new ParseException("Special character found in object class id: \"" + id.charAt(x) + "\"", id.getLine());

		// Check for object class id conflicts
		for (ObjectClass oc : classes)
			if (oc.id.startsWith(id.toString()) || id.startsWith(oc.id))
				throw new ParseException("Object class id begins with the id of another class: " + oc.id + " and " + id + ".", id.getLine());

		// One element: id only and no name
		if (split.length == 1)
			return new ObjectClassMetadata(id.toString(), null);

		// Second element: name
		FilePiece name = split[1].trim();
		if (name.isBlank())
			return new ObjectClassMetadata(id.toString(), null);
		if (split.length == 2)
			return new ObjectClassMetadata(id.toString(), name.toString());

		// Third element: category
		FilePiece category = split[2].trim();
		if (category.isBlank())
			return new ObjectClassMetadata(id.toString(), name.toString());
		else
			return new ObjectClassMetadata(id.toString(), name.toString(), category.toString());
	}

	private enum TokenType { group, and, minus, plus, openParen, closeParen };
	private class Token
	{
		public TokenType type;
		public Pattern group;
		public int lineNumber;

		public Token(TokenType type, int lineNumber)
		{
			this.type = type;
			this.lineNumber = lineNumber;
		}

		public Token(Pattern group, int lineNumber)
		{
			this.type = TokenType.group;
			this.group = group;
			this.lineNumber = lineNumber;
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
				token.group = token.group.or(next.group);
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
				left.group = left.group.and(right.group);
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
				left.group = left.group.subtract(right.group);
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
				left.group = left.group.or(right.group);
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

	public Pattern buildSpecialPattern(String key, int lineNumber) throws ParseException
	{
		JSONObject json;
		try
		{
			json = new JSONObject(key);
		}
		catch (JSONException e)
		{
			throw new ParseException("Invalid JSON for pattern: " + e.getMessage(), lineNumber);
		}

		if (!json.has("type"))
			throw new ParseException("Pattern with missing type", lineNumber);
		String type = json.getString("type");

		switch(type)
		{
		case "tile":
			int x = json.has("x") ? json.getInt("x") : 0;
			int y = json.has("y") ? json.getInt("y") : 0;
			boolean solid = json.has("solid") ? json.getBoolean("solid") : true;
			return new OffsetPattern(x, y, solid ? 'S' : 'N');
		default:
			throw new ParseException("Unrecognized pattern type: \"" + type + "\"", lineNumber);
		}
	}

	public Pattern buildObjectGroup(FilePiece key) throws ParseException
	{
		// Tokenize
		int i = 0;
		ArrayList<Token> tokens = new ArrayList<Token>();
		while (i < key.length())
		{
			char c = key.charAt(i);
			if (c == ',' || Character.isWhitespace(c))
			{
				i++;
			}
			else if (c == '&')
			{
				tokens.add(new Token(TokenType.and, key.lineOf(i)));
				i++;
			}
			else if (c == '-')
			{
				tokens.add(new Token(TokenType.minus, key.lineOf(i)));
				i++;
			}
			else if (c == '+')
			{
				tokens.add(new Token(TokenType.plus, key.lineOf(i)));
				i++;
			}
			else if (c == '(')
			{
				tokens.add(new Token(TokenType.openParen, key.lineOf(i)));
				i++;
			}
			else if (c == ')')
			{
				tokens.add(new Token(TokenType.closeParen, key.lineOf(i)));
				i++;
			}
			else if (c == '{')
			{
				int j = Util.findMatchingBracket(key, i);
				if (j == -1)
					throw new ParseException("Unbalanced brackets", key.lineOf(i));
				String json = key.toString().substring(i, j + 1).replaceAll("\r?\n", "").trim();
				Pattern pattern = buildSpecialPattern(json, key.lineOf(i));
				tokens.add(new Token(pattern, key.lineOf(i)));
				i = j + 1;
			}
			else if (Character.isDigit(c))
			{
				try
				{
					int j = key.indexOf(':', i);
					int bank = key.substring(i, j).toInt();
					int k = j + 1;
					while (k < key.length() && Character.isDigit(key.charAt(k)))
						k++;
					int object = key.substring(j + 1, k).toInt();
					ObjectGroup group = new ObjectGroup();
					group.add((byte) bank, (byte) object);
					tokens.add(new Token(group, key.lineOf(i)));
					i = k;
				}
				catch (Exception e)
				{
					String snippet = Util.trimStringForPrinting(key.substring(i).toString());
					throw new ParseException("Couldn't parse object format near \"" + snippet + "\"", key.lineOf(i));
				}
			}
			else
			{
				ObjectClass oc = getFirstObjectClass(key.substring(i).toString());
				if (oc == null)
				{
					String snippet = Util.trimStringForPrinting(key.substring(i).toString());
					throw new ParseException("Couldn't parse object format near \"" + snippet + "\"", key.lineOf(i));
				}
				tokens.add(new Token(oc.group, key.lineOf(i)));
				i += oc.id.length();
			}
		}
		if (tokens.isEmpty())
			throw new ParseException("Empty object class.", key.getLine());

		tokens.add(new Token(TokenType.closeParen, key.lineOf(i))); // add ) to indicate end of expression
		evaluateTokens(tokens, 0);
		if (tokens.size() > 1)
			throw new ParseException("Unbalanced parenthesis.", tokens.get(0).lineNumber);

		// The last remaining token has our finished pattern
		return tokens.get(0).group;
	}

	public Pattern buildObjectGroup(String key) throws ParseException
	{
		return buildObjectGroup(new FilePiece(key, -1, -1));
	}

	public ObjectClassesFile(Path ocFile) throws IOException, ParseException
	{
		classes = new ArrayList<ObjectClass>();
		FilePiece contents = new FilePiece(ocFile);
		int i;

		while (true)
		{
			// Read until the next {
			i = contents.indexOf('{');
			if (i == -1)
			{
				// End of file
				if (contents.matches("\\s*"))
					break;
				else
					throw new ParseException("Unexpected text after last object group.", contents.getLine());
			}

			// Read until the matching }
			int end = Util.findMatchingBracket(contents, i);
			if (end == -1)
				throw new ParseException("Unmatched \"{\"", contents.getLine());

			// Parse the header
			FilePiece header = contents.substring(0, i);
			ObjectClassMetadata metadata = parseHeader(header);

			// Parse the body
			FilePiece body = contents.substring(i + 1, end);
			Pattern group = buildObjectGroup(body);
			classes.add(new ObjectClass(metadata.id, metadata.name, metadata.category, group));

			// Continue from there
			contents = contents.substring(end + 1);
		}
		// Sort the groups alphabetically
		sort();
	}

	private void sort()
	{
		classes.sort(new ObjectClass.ObjectClassComparator());
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
				Console.printString("\t" + oc.identifier());
		}
	}
}
