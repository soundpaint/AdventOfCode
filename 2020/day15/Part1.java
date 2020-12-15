import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Part1
{
  private Part1() {}

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new HashMap<Long, Long>();
    String line = reader.readLine();
    long pos = 1, prev = -1;
    for (final var value : line.split(",")) {
      prev = Long.parseLong(value);
      values.put(prev, pos++);
    }
    long next = pos++ - 1 - values.get(prev);
    for (; pos <= 2020; pos++) {
      values.put(prev, pos - 2);
      prev = next;
      next = values.containsKey(prev) ? pos - 1 - values.get(prev) : 0;
    }
    System.out.println(next);
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run("data.txt");
  }
}
