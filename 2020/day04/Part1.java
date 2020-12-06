import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Part1
{
  private static class ParseError extends RuntimeException
  {
    final String message;

    private ParseError() { throw new UnsupportedOperationException(); }

    public ParseError(final String message)
    {
      this.message = message;
    }

    public String toString()
    {
      return "parse error: " + message;
    }
  }

  private abstract static class ValueParser
  {
    private final String id;

    protected enum Status { UNPARSED, SUCCEEDED, FAILED };
    protected Status status = Status.UNPARSED;

    public ValueParser(final String id)
    {
      this.id = id;
    }

    public boolean parsesOptionalKey() { return false; }

    protected void checkAlreadyParsed() {
      if (status != Status.UNPARSED) {
        throw new ParseError(getId() + ": already parsed");
      }
    }

    public boolean isUnparsed() { return status == Status.UNPARSED; }

    public boolean parseSucceeded() { return status == Status.SUCCEEDED; }

    public boolean parseFailed() { return status == Status.FAILED; }

    public String getId() { return id; }

    abstract void parseValue(final String value);
  }

  private abstract static class DontCareValueParser extends ValueParser
  {
    public DontCareValueParser(final String id)
    {
      super(id);
    }

    public void parseValue(final String value)
    {
      checkAlreadyParsed();
      status = Status.SUCCEEDED;
    }
  }

  private static class Passport
  {
    private ValueParser parsers[] = {
      // birth year
      new DontCareValueParser("byr") {},

      // issue year
      new DontCareValueParser("iyr") {},

      // expiration year
      new DontCareValueParser("eyr") {},

      // height
      new DontCareValueParser("hgt") {},

      // hair color
      new DontCareValueParser("hcl") {},

      // eye color
      new DontCareValueParser("ecl") {},

      // passport ID
      new DontCareValueParser("pid") {},

      // country ID
      new DontCareValueParser("cid") {
        @Override
        public boolean parsesOptionalKey() { return true; }
      }
    };

    public Passport() {}

    public boolean parse(final String unparsed) {
      String strKeyValuePairs[] = unparsed.trim().split(" ");
      for (var strKeyValuePair : strKeyValuePairs) {
        String[] keyValuePair = strKeyValuePair.split(":");
        if (keyValuePair.length == 0) {
          throw new ParseError("key-value pair: missing separator ':': " +
                               strKeyValuePair);
        }
        if (keyValuePair.length > 2) {
          throw new ParseError("key-value pair: multiple separators ':': " +
                               strKeyValuePair);
        }
        String key = keyValuePair[0].trim();
        String value = keyValuePair[1].trim();
        dispatch(key).parseValue(value);
      }
      return isValid();
    }

    private ValueParser dispatch(final String key)
    {
      for (final ValueParser parser : parsers) {
        if (parser.getId().equals(key)) {
          return parser;
        }
      }
      throw new ParseError("invalid key: " + key);
    }

    public boolean isValid()
    {
      for (final var parser : parsers) {
        if (parser.parseFailed()) {
          System.out.println("error: failed parsing value for key: " +
                             parser.getId());
          return false;
        }
        if (parser.isUnparsed() && !parser.parsesOptionalKey()) {
          System.out.println("error: missing key / value pair: " +
                             parser.getId());
          return false;
        }
      }
      return true;
    }
  }

  private Part1() {}

  private boolean isValidPassport(final String value, final int line)
  {
    final Passport passport = new Passport();
    final boolean isValid = passport.parse(value.trim());
    if (!isValid) {
      System.out.println("error in passport in line " + line);
    }
    return isValid;
  }

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final ArrayList<String> lines = new ArrayList<String>();
    int count = 0;
    int line = 1;
    int passportLine = 0;
    String inputLine;
    final StringBuffer composedLine = new StringBuffer();
    while ((inputLine = reader.readLine()) != null) {
      inputLine = inputLine.trim();
      line++;
      if (inputLine.isEmpty()) {
        if (isValidPassport(composedLine.toString(), passportLine)) count++;
        composedLine.setLength(0);
        passportLine = line;
      } else {
        composedLine.append(" ");
        composedLine.append(inputLine);
      }
    }
    if (isValidPassport(composedLine.toString(), passportLine)) count++;
    System.out.println(count);
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run("data.txt");
  }
}
