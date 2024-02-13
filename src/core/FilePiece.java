package core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class FilePiece
{
	private String data;
	private int line;
	private int col;

	public FilePiece(Path file) throws IOException
	{
		//data = Files.readString(file).replaceAll("\r\n", "\n");
		data = new String(Files.readAllBytes(file)).replaceAll("\r\n", "\n");
		line = 0;
		col = 0;
	}

	public FilePiece(String contents, int startLine, int startCol)
	{
		data = contents;
		line = startLine;
		col = startCol;
	}

	public FilePiece clone()
	{
		return new FilePiece(data, line, col);
	}

	public int getLine()
	{
		return line;
	}

	public int getCol()
	{
		return col;
	}

	public int countCharOccurances(int ch)
	{
		return countCharOccurances(ch, 0, data.length());
	}

	public int countCharOccurances(int ch, int endIndex)
	{
		return countCharOccurances(ch, 0, endIndex);
	}

	public int countCharOccurances(int ch, int beginIndex, int endIndex)
	{
		int count = 0;
		for (int i = beginIndex; i < endIndex; i++)
			if (data.charAt(i) == ch)
				count++;
		return count;
	}

	public int lineOf(int index)
	{
		return line + countCharOccurances('\n', index);
	}

	public char charAt(int index)
	{
		return data.charAt(index);
	}

	public int indexOf(int ch)
	{
		return data.indexOf(ch);
	}

	public int indexOf(int ch, int fromIndex)
	{
		return data.indexOf(ch, fromIndex);
	}

	public boolean isBlank()
	{
		//return data.isBlank();
		return data.trim().isEmpty();
	}

	public int length()
	{
		return data.length();
	}

	public boolean matches(String regex)
	{
		return data.matches(regex);
	}

	public FilePiece[] split(char ch)
	{
		ArrayList<FilePiece> pieces = new ArrayList<FilePiece>();
		FilePiece fodder = clone();
		while (true)
		{
			int i = fodder.indexOf(ch);
			if (i == -1)
			{
				pieces.add(fodder);
				break;
			}
			pieces.add(fodder.substring(0, i));
			fodder = fodder.substring(i + 1);
		}
		FilePiece[] piecesArray = new FilePiece[pieces.size()];
		for (int i = 0; i < pieces.size(); i++)
			piecesArray[i] = pieces.get(i);
		return piecesArray;
	}

	public boolean startsWith(String prefix)
	{
		return data.startsWith(prefix);
	}

	public boolean startsWith(FilePiece prefix)
	{
		return data.startsWith(prefix.data);
	}

	public Integer toInt()
	{
		return Integer.parseInt(data);
	}

	public FilePiece trim()
	{
		int j = data.length();
		while (j > 0 && Character.isWhitespace(data.charAt(j - 1)))
			j--;
		int i = 0;
		while (i < j && Character.isWhitespace(data.charAt(i)))
			i++;
		return substring(i, j);
	}

	public FilePiece substring(int beginIndex)
	{
		return new FilePiece(
			data.substring(beginIndex),
			lineOf(beginIndex),
			beginIndex - data.substring(0, beginIndex).lastIndexOf('\n') - 1
		);
	}

	public FilePiece substring(int beginIndex, int endIndex)
	{
		return new FilePiece(
			data.substring(beginIndex, endIndex),
			lineOf(beginIndex),
			beginIndex - data.substring(0, beginIndex).lastIndexOf('\n') - 1
		);
	}

	public String toString()
	{
		return data;
	}
}
