import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class Part1
{
  private Part1() {}

  private void run1(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new HashMap<Long, Long>();
    /*
    String line;
    while ((line = reader.readLine()) != null) {
      values.add(line);
    }
    */
    String line = reader.readLine();
    long pos = 1;
    long last = -1;
    long next = -1;
    for (final var value : line.split(",")) {
      System.out.println("=== TURN " + pos + ", LAST=" + next);
      last = Long.parseLong(value);
      values.put(last, pos);
      System.out.println("put (" + last + "," + values.get(last) + ")");
      next = last;
      pos++;
    }
    System.out.println("=== TURN " + pos +
                       ": LAST=" + next + " @ " +
                       (pos - 1) + ", null");
    next = pos - 1 - values.get(last);
    pos++;
    while (pos < 30000005) {
      values.put(last, pos - 2);
      if (pos >= 29999990) {
        System.out.println("=== TURN " + pos +
                           ": LAST=" + next + " @ " +
                           (pos - 1) + ", " + values.get(next));
      }
      long newVal;
      //System.out.println("get(" + next + ")=" + values.get(next));
      if (values.get(next) == null) newVal = 0;
      else newVal = pos - 1 - values.get(next);
      /*
      System.out.println("newVal=" + (pos - 1) + "-" + values.get(next) +
                         "=" + newVal);
      System.out.println("put (" + last + "," + values.get(last) + ")");
      */
      last = next;
      next = newVal;
      if (pos >= 29999990) {
        System.out.println("TURN " + pos + ": " + next);
      }
      pos++;
    }
  }

  private void run2(final String filePath) throws IOException
  {
    final var path = Paths.get(filePath);
    final var data =
      new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    final String values[] = data.trim().split("[,\\s]");
    for (final var value : values) {
      System.out.println("[" + value + "]");
    }
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run1("data.txt");
  }
}
