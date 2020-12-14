import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Part1
{
  private Part1()
  {
    memory = new HashMap<Long, Long>();
  }

  private final HashMap<Long, Long> memory;
  private long maskAnd;
  private long maskOr;

  private void parseMask(final String mask)
  {
    maskOr = Long.parseLong(mask.replace("X", "0"), 2);
    maskAnd = Long.parseLong(mask.replace("X", "1"), 2);
  }

  private void executeWriteAccess(final String value)
  {
    final String[] tokens = value.substring(4).split(" ");
    final long adr = Long.parseLong(tokens[0].replaceFirst("]", ""));
    final long val = (Long.parseLong(tokens[2]) & maskAnd) | maskOr;
    memory.put(adr, val);
  }

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new ArrayList<String>();
    String line;
    while ((line = reader.readLine()) != null) {
      values.add(line);
    }
    maskAnd = 0x3ffffL;
    maskOr = 0x0;
    for (final var instruction : values) {
      if (instruction.startsWith("mask = ")) {
        parseMask(instruction.substring(7));
      } else {
        executeWriteAccess(instruction);
      }
    }
    long sum = 0;
    for (final Long adr : memory.keySet()) {
      final Long value = memory.get(adr);
      if (value != null) sum += value;
    }
    System.out.println(sum);
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run("data.txt");
  }
}
