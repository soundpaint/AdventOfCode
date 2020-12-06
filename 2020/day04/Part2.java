import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Part2
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

  private abstract static class YearParser extends ValueParser
  {
    private final int minYear;
    private final int maxYear;

    public YearParser(final String id, final int minYear, final int maxYear)
    {
      super(id);
      this.minYear = minYear;
      this.maxYear = maxYear;
    }

    public void parseValue(final String value)
    {
      checkAlreadyParsed();
      final int year = Integer.parseInt(value);
      if ((year >= minYear) && (year <= maxYear)) {
        status = Status.SUCCEEDED;
      } else {
        System.out.println("error: " + getId() +
                           ": year " + value + " not in range " +
                           minYear + ".." + maxYear);
        status = Status.FAILED;
      }
    }
  }

  private static class Passport
  {
    private ValueParser parsers[] = {
      // birth year
      new YearParser("byr", 1920, 2020) {},

      // issue year
      new YearParser("iyr", 2010, 2020) {},

      // expiration year
      new YearParser("eyr", 2020, 2030) {},

      // height
      new ValueParser("hgt") {
        private void parseCentimeters(final String value)
        {
          int centimeters = Integer.parseInt(value);
          if ((centimeters >= 150) && (centimeters <= 193)) {
            status = Status.SUCCEEDED;
          } else {
            System.out.println("error: " + getId() + ": " +
                               "height (cm) out of range: " + value);
            status = Status.FAILED;
          }
        }

        private void parseInches(final String value)
        {
          int inches = Integer.parseInt(value);
          if ((inches >= 59) && (inches <= 76)) {
            status = Status.SUCCEEDED;
          } else {
            System.out.println("error: " + getId() + ": " +
                               "height (in) out of range: " + value);
            status = Status.FAILED;
          }
        }

        public void parseValue(final String value)
        {
          checkAlreadyParsed();
          if (value.endsWith("cm")) {
            parseCentimeters(value.substring(0, value.length() - 2));
          } else if (value.endsWith("in")) {
            parseInches(value.substring(0, value.length() - 2));
          } else {
            System.out.println("error: " + getId() + ": " +
                               "could not determine unit of height");
            status = Status.FAILED;
          }
        }
      },

      // hair color
      new ValueParser("hcl") {
        private boolean isSmallLetterHexDigit(final char ch)
        {
          return
            ((ch >= '0') && (ch <= '9')) ||
            ((ch >= 'a') && (ch <= 'z'));
        }

        public void parseValue(final String value)
        {
          checkAlreadyParsed();
          if (value.startsWith("#")) {
            final String color = value.substring(1);
            if (color.length() == 6) {
              int count = 0;
              for (int i = 0; i < 6; i++) {
                if (isSmallLetterHexDigit(color.charAt(i)))
                  count++;
              }
              if (count == 6) {
                status = Status.SUCCEEDED;
              } else {
                System.out.println("error: " + getId() + ": " +
                                   "expected 6 hex digits as color value");
                status = Status.FAILED;
              }
            } else {
              System.out.println("error: " + getId() + ": " +
                                 "expected 6 color digits after '#'");
              status = Status.FAILED;
            }
          } else {
            System.out.println("error: " + getId() + ": " +
                               "expected '#'");
            status = Status.FAILED;
          }
        }
      },

      // eye color
      new ValueParser("ecl") {
        final List<String> validEyeColors =
          Arrays.asList("amb", "blu", "brn", "gry", "grn", "hzl", "oth");

        public void parseValue(final String value)
        {
          checkAlreadyParsed();
          if (validEyeColors.stream().anyMatch(s -> s.equals(value))) {
            status = Status.SUCCEEDED;
          } else {
            System.out.println("error: " + getId() + ": " +
                               "unknown eye color: " + value);
            status = Status.FAILED;
          }
        }
      },

      // passport ID
      new ValueParser("pid") {
        public void parseValue(final String value)
        {
          checkAlreadyParsed();
          if (value.length() == 9) {
            int count = 0;
            for (int i = 0; i < 9; i++) {
              char ch = value.charAt(i);
              if ((ch >= '0') && (ch <= '9'))
                count++;
            }
            if (count == 9) {
              status = Status.SUCCEEDED;
            } else {
              System.out.println("error: " + getId() + ": " +
                                 "passport ID: expected 9 digits: " + value);
              status = Status.FAILED;
            }
          } else {
            System.out.println("error: " + getId() + ": " +
                               "passport ID: expected 9 characters: " + value);
            status = Status.FAILED;
          }
        }
      },

      // country ID
      new ValueParser("cid") {
        @Override
        public boolean parsesOptionalKey() { return true; }

        public void parseValue(final String value)
        {
          checkAlreadyParsed();
          status = Status.SUCCEEDED;
        }
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

  private Part2() {}

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
    new Part2().run("data.txt");
  }
}
