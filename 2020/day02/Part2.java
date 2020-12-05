import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Part2
{
  private static class ParseError extends RuntimeException
  {
    final int line;
    final String message;

    private ParseError() { throw new UnsupportedOperationException(); }

    public ParseError(final int line, final String message)
    {
      this.line = line;
      this.message = message;
    }

    public String toString()
    {
      return "parse error in line " + line + ": " + message;
    }
  }

  private static class Range
  {
    final int lowerValue;
    final int upperValue;

    private Range() { throw new UnsupportedOperationException(); }

    public Range(final int line, final String unparsedText)
    {
      if (!unparsedText.contains("-")) {
        throw new ParseError(line, "missing '-' separator");
      }
      final String tokens[] = unparsedText.split("-");
      if (tokens.length > 2) {
        throw new ParseError(line, "multiple '-' separators");
      }
      lowerValue = Integer.parseInt(tokens[0]);
      upperValue = Integer.parseInt(tokens[1]);
    }

    public int getLowerValue() { return lowerValue; }

    public int getUpperValue() { return upperValue; }
  }

  private static class Policy
  {
    private final Range range;
    private final char character;

    private Policy() { throw new UnsupportedOperationException(); }

    public Policy(final int line, final String unparsedText)
    {
      if (!unparsedText.contains(" ")) {
        throw new ParseError(line, "missing ' ' separator");
      }
      final String tokens[] = unparsedText.split(" ");
      if (tokens.length > 2) {
        throw new ParseError(line, "multiple ' ' separators");
      }
      range = new Range(line, tokens[0].trim());
      final String strCharacter = tokens[1].trim();
      if (strCharacter.isEmpty()) {
        throw new ParseError(line, "missing letter");
      }
      if (strCharacter.length() > 1) {
        throw new ParseError(line, "multiple letters");
      }
      character = strCharacter.charAt(0);
    }

    public char getCharacter() { return character; }

    public Range getRange() { return range; }
  }

  private static class PasswordEntry
  {
    private final Policy policy;
    private final String password;

    private PasswordEntry() { throw new UnsupportedOperationException(); }

    public PasswordEntry(final int line, final String unparsedText)
    {
      if (!unparsedText.contains(":")) {
        throw new ParseError(line, "missing ':' separator");
      }
      final String tokens[] = unparsedText.split(":");
      if (tokens.length > 2) {
        throw new ParseError(line, "multiple ':' separators");
      }
      policy = new Policy(line, tokens[0].trim());
      password = tokens[1].trim();
    }

    public boolean isValid()
    {
      final char character = policy.getCharacter();
      final Range range = policy.getRange();
      final int lowerValue = range.getLowerValue();
      final int upperValue = range.getUpperValue();
      int count = 0;
      if (character == password.charAt(lowerValue - 1)) {
        count++;
      }
      if (character == password.charAt(upperValue - 1)) {
        count++;
      }
      return count == 1;
    }
  }

  private Part2() {}

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new ArrayList<String>();
    String inputLine;
    while ((inputLine = reader.readLine()) != null) {
      values.add(inputLine);
    }
    int sum = 0, line = 0;
    for (final var value : values) {
      final PasswordEntry entry = new PasswordEntry(++line, value);
      if (entry.isValid()) {
        sum++;
      }
    }
    System.out.println(sum);
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part2().run("data.txt");
  }
}
