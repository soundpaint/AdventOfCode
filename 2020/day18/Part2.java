import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Part2
{
  private static final int EOF = -1;
  private static final int PLUS = -2;
  private static final int MUL = -3;
  private static final int LEFT_PAREN = -4;
  private static final int RIGHT_PAREN = -5;
  private static final int NO_LOOK_AHEAD = -6;

  private final Lexer lexer;

  private static class Lexer
  {
    private String input;
    private int pos;
    private int lookAhead;

    public void setInput(final String input)
    {
      this.input = input;
      pos = 0;
      lookAhead = NO_LOOK_AHEAD;
    }

    private boolean haveLookAhead() {
      return lookAhead != NO_LOOK_AHEAD;
    }

    private int consumeLookAhead() {
      final int symbol = lookAhead;
      lookAhead = NO_LOOK_AHEAD;
      return symbol;
    }

    public void pushBack(final int symbol)
    {
      if (haveLookAhead()) {
        throw new RuntimeException("lexer: multiple push back not supported");
      }
      lookAhead = symbol;
    }

    public int nextSymbol()
    {
      if (haveLookAhead()) {
        return consumeLookAhead();
      }
      int value = 0;
      boolean inValue = false; // lexer state
      while (pos < input.length()) {
        final char ch = input.charAt(pos++);
        if (ch == ' ') continue;
        if ((ch >= '0') && (ch <= '9')) {
          inValue = true;
          value = 10 * value + (ch - '0');
          continue;
        }
        if (inValue) {
          pos--;
          return value;
        }
        switch (ch) {
        case '+':
          return PLUS;
        case '*':
          return MUL;
        case '(':
          return LEFT_PAREN;
        case ')':
          return RIGHT_PAREN;
        default:
          throw new RuntimeException("lexer: unexpected char: '" + ch + "'");
        }
      }
      if (inValue) return value;
      return EOF;
    }
  }

  private Part2()
  {
    lexer = new Lexer();
  }

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var lines = new ArrayList<String>();
    String inputLine;
    while ((inputLine = reader.readLine()) != null) {
      lines.add(inputLine);
    }
    long sum = 0;
    for (final String line : lines) {
      lexer.setInput(line);
      sum += parseExpr();
      if (lexer.nextSymbol() != EOF)
        throw new RuntimeException("parser: unexpected trailing characters");
    }
    System.out.println(sum);
  }

  private long parseExpr()
  {
    // expr ::= factor | factor "*" expr .
    final long expr;
    final long factor = parseFactor();
    final int symbol = lexer.nextSymbol();
    if (symbol == MUL) {
      expr = factor * parseExpr();
    } else {
      lexer.pushBack(symbol);
      expr = factor;
    }
    return expr;
  }

  private long parseFactor()
  {
    // factor ::= term | term "+" factor .
    final long factor;
    final long term = parseTerm();
    final int symbol = lexer.nextSymbol();
    if (symbol == PLUS) {
      factor = term + parseFactor();
    } else {
      lexer.pushBack(symbol);
      factor = term;
    }
    return factor;
  }

  private long parseTerm()
  {
    // term ::= constValue | parenExpr .
    final long term;
    final int symbol = lexer.nextSymbol();
    if (symbol >= 0) {
      term = symbol;
    } else {
      lexer.pushBack(symbol);
      term = parseParenExpr();
    }
    return term;
  }

  private long parseParenExpr()
  {
    // parenExpr ::= '(' expr ')' .
    final long parenExpr;
    int symbol = lexer.nextSymbol();
    if (symbol != LEFT_PAREN) {
      throw new RuntimeException("parser: '(' expected");
    }
    parenExpr = parseExpr();
    symbol = lexer.nextSymbol();
    if (symbol != RIGHT_PAREN)
      throw new RuntimeException("parser: ')' expected");
    return parenExpr;
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part2().run("data.txt");
  }
}
