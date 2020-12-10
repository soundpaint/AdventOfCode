import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class Part1
{
  private Part1() {}

  private boolean check(final ArrayDeque<Long> previousNumbers, final long sum)
  {
    for (var number1 : previousNumbers) {
      for (var number2 : previousNumbers) {
        if (sum == number1 + number2) {
          return true;
        }
      }
    }
    return false;
  }

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new ArrayList<Long>();
    String line;
    while ((line = reader.readLine()) != null) {
      final var value = Long.parseLong(line);
      values.add(value);
    }
    final ArrayDeque<Long> previousNumbers = new ArrayDeque<Long>();
    for (final var value : values) {
      if (previousNumbers.size() < 25) {
        previousNumbers.add(value);
      } else {
        if (!check(previousNumbers, value)) {
          System.out.println(value);
          return;
        }
        previousNumbers.remove();
        previousNumbers.add(value);
      }
    }
    System.out.println("no match");
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run("data.txt");
  }
}
