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
  private static final int NO_PUSH_BACK = -6;

  final Lexer lexer;

  private static class Lexer
  {
    private String input;
    private int pos;
    private int pushBack;

    public void setInput(final String input)
    {
      this.input = input;
      pos = 0;
      pushBack = NO_PUSH_BACK;
    }

    private boolean havePushBack() {
      return pushBack != NO_PUSH_BACK;
    }

    private int consumePushBack() {
      int token = pushBack;
      pushBack = NO_PUSH_BACK;
      return token;
    }

    public void pushBack(final int token)
    {
      if (havePushBack()) {
        throw new RuntimeException("lexer: multiple push back not supported");
      }
      pushBack = token;
    }

    public int nextToken()
    {
      if (havePushBack()) {
        return consumePushBack();
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
          throw new RuntimeException("lexer: unknwon char: '" + ch + "'");
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
      long value = parseExpr();
      if (lexer.nextToken() != EOF)
        throw new RuntimeException("parser: unexpected trailing characters");
      sum += value;
    }
    System.out.println(sum);
  }

  private long parseExpr()
  {
    // expr ::= factor | factor "*" expr .
    final long expr;
    final long factor = parseFactor();
    final int token = lexer.nextToken();
    if (token == MUL) {
      expr = factor * parseExpr();
    } else {
      lexer.pushBack(token);
      expr = factor;
    }
    return expr;
  }

  private long parseFactor()
  {
    // factor ::= term | term "+" factor .
    final long factor;
    final long term = parseTerm();
    final int token = lexer.nextToken();
    if (token == PLUS) {
      factor = term + parseFactor();
    } else {
      lexer.pushBack(token);
      factor = term;
    }
    return factor;
  }

  private long parseTerm()
  {
    // term ::= constValue | parenExpr .
    final long term;
    final int token = lexer.nextToken();
    if (token >= 0) {
      term = token;
    } else {
      lexer.pushBack(token);
      term = parseParenExpr();
    }
    return term;
  }

  private long parseParenExpr()
  {
    // parenExpr ::= '(' expr ')' .
    final long parenExpr;
    int token = lexer.nextToken();
    if (token != LEFT_PAREN) {
      throw new RuntimeException("parser: '(' expected");
    }
    parenExpr = parseExpr();
    token = lexer.nextToken();
    if (token != RIGHT_PAREN)
      throw new RuntimeException("parser: ')' expected");
    return parenExpr;
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part2().run("data.txt");
  }
}
