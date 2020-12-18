import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

public class Part1
{
  private static final int EOF = -1;
  private static final int PLUS = -2;
  private static final int MUL = -3;
  private static final int LEFT_PAREN = -4;
  private static final int RIGHT_PAREN = -5;

  private final Lexer lexer;
  private final Stack<Long> pendingOperations;
  private final Stack<Long> pendingOperands;

  private static class Lexer
  {
    private String input;
    private int pos;

    public void setInput(final String input)
    {
      this.input = input;
      pos = 0;
    }

    public int nextSymbol()
    {
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

  private Part1()
  {
    lexer = new Lexer();
    pendingOperations = new Stack<Long>();
    pendingOperands = new Stack<Long>();
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
      sum += eval();
      if (lexer.nextSymbol() != EOF)
        throw new RuntimeException("parser: unexpected trailing characters");
    }
    System.out.println(sum);
  }

  private long applyPendingOperation(final long pendingOperation,
                                     final long lhs, final long rhs)
  {
    switch ((int)pendingOperation) {
    case PLUS:
      return lhs + rhs;
    case MUL:
      return lhs * rhs;
    default:
      throw new RuntimeException("parser: unexpected operation: " +
                                 pendingOperation);
    }
  }

  private long eval()
  {
    long lhs = 0;
    long pendingOperation = EOF;
    while (true) {
      final int symbol = lexer.nextSymbol();
      if (symbol >= 0) {
        if (pendingOperation != EOF) {
          lhs = applyPendingOperation(pendingOperation, lhs, symbol);
          pendingOperation = EOF;
        } else {
          lhs = symbol;
        }
        continue;
      }
      switch (symbol) {
      case PLUS:
      case MUL:
        pendingOperation = symbol;
        break;
      case LEFT_PAREN:
        pendingOperands.push(lhs);
        pendingOperations.push(pendingOperation);
        pendingOperation = EOF;
        break;
      case RIGHT_PAREN:
        if (pendingOperation != EOF)
          throw new RuntimeException("parser: expected operand, but got ')'");
        pendingOperation = pendingOperations.pop();
        if (pendingOperation == EOF) {
          pendingOperands.pop();
        } else {
          lhs =
            applyPendingOperation(pendingOperation, pendingOperands.pop(), lhs);
          pendingOperation = EOF;
        }
        break;
      case EOF:
        if (!pendingOperations.empty())
          throw new RuntimeException("parser: mismatching parens; " +
                                     "left operations: " + pendingOperations);
        return lhs;
      default:
        throw new RuntimeException("parser: unexpected symbol: " + symbol);
      }
    }
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run("data.txt");
  }
}
