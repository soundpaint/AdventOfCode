import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Part2
{
  private final HashMap<Long, Long> memory;
  private String mask;

  private Part2()
  {
    memory = new HashMap<Long, Long>();
  }

  private String adr2bits(final long adr)
  {
    final StringBuffer bits = new StringBuffer();
    long mask = 0x800000000L;
    for (int i = 0; i < 36; i++) {
      if ((adr & mask) != 0) {
        bits.append("1");
      } else {
        bits.append("0");
      }
      mask = mask >>> 1;
    }
    return bits.toString();
  }

  private void executeWriteAccess(final long val, final String floatingAddress,
                                  final int pos)
  {
    if (pos >= mask.length()) {
      final long adr = Long.parseLong(floatingAddress, 2);
      memory.put(adr, val);
    } else {
      if (floatingAddress.charAt(pos) == 'X') {
        final String floatingAddressSubInstance1 =
          floatingAddress.substring(0, pos) + '0' +
          floatingAddress.substring(pos + 1);
        executeWriteAccess(val, floatingAddressSubInstance1, pos + 1);
        final String floatingAddressSubInstance2 =
          floatingAddress.substring(0, pos) + '1' +
          floatingAddress.substring(pos + 1);
        executeWriteAccess(val, floatingAddressSubInstance2, pos + 1);
      } else {
        executeWriteAccess(val, floatingAddress, pos + 1);
      }
    }
  }

  private void executeWriteAccess(final String command)
  {
    final String[] tokens = command.substring(4).split(" ");
    final long adr = Long.parseLong(tokens[0].replaceFirst("]", ""));
    final long val = Long.parseLong(tokens[2]);
    final String adrBits = adr2bits(adr);
    final StringBuffer floatingAddress = new StringBuffer();
    for (int i = 0; i < mask.length(); i++) {
      final char bit = mask.charAt(i);
      if (bit == 'X') {
        floatingAddress.append('X');
      } else if (bit == '0') {
        floatingAddress.append(adrBits.charAt(i));
      } else if (bit == '1') {
        floatingAddress.append('1');
      } else {
        throw new RuntimeException("unexpected nit mask character: " + bit);
      }
    }
    executeWriteAccess(val, floatingAddress.toString(), 0);
  }

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new ArrayList<String>();
    String line;
    while ((line = reader.readLine()) != null) {
      values.add(line);
    }
    for (final var instruction : values) {
      if (instruction.startsWith("mask = ")) {
        mask = instruction.substring(7);
      } else {
        executeWriteAccess(instruction);
      }
    }
    long sum = 0;
    for (final Long adr : memory.keySet()) {
      final Long value = memory.get(adr);
      if (value != null) {
        sum += value;
      }
    }
    System.out.println(sum);
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part2().run("data.txt");
  }
}
