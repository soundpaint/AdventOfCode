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

  private HashMap<Long, Long> memory;
  private long maskAnd;
  private long maskOr;

  private String mask;
  //private ArrayList<Long> masks;

  private void handleMask(String mask)
  {
    String[] tokens = mask.split(" ");
    String strMask = tokens[2];
    maskOr = Long.parseLong(strMask.replace("X", "0"), 2);
    maskAnd = Long.parseLong(strMask.replace("X", "1"), 2);
  }

  /*
  private void handleMask2(String mask, int pos)
  {
    if (pos >= mask.length()) {
      long value = Long.parseLong(mask);
      masks.add(value);
    } else {
      if (mask.charAt(pos) == 'X') {
        mask = mask.substring(0, pos) + '0' + mask.substring(pos + 1);
        handleMask2(mask, pos + 1);
        mask = mask.substring(0, pos) + '1' + mask.substring(pos + 1);
        handleMask2(mask, pos + 1);
      } else {
        handleMask2(mask, pos + 1);
      }
    }
  }

  private void handleMask2(String mask)
  {
    String[] tokens = mask.split(" ");
    String strMask = tokens[2];
    masks.clear();
    handleMask2(String mask, 0);
  }
  */

  private void handleMem(String value, boolean doMask)
  {
    String[] tokens = value.substring(4).split(" ");
    long adr = Long.parseLong(tokens[0].replaceFirst("]", ""));
    long val = Long.parseLong(tokens[2]);
    if (true) {
      val = (val & maskAnd) | maskOr;
    }
    memory.put(adr, val);
  }

  private String adr2bits(long adr)
  {
    StringBuffer bits = new StringBuffer();
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

  private void handleMem2(String value)
  {
    String[] tokens = value.substring(4).split(" ");
    long adr = Long.parseLong(tokens[0].replaceFirst("]", ""));
    long val = Long.parseLong(tokens[2]);
    String adrBits = adr2bits(adr);
    System.out.println("adrBits=" + adrBits);
    StringBuffer floatingAddress = new StringBuffer();
    for (int i = 0; i < mask.length(); i++) {
      char bit = mask.charAt(i);
      if (bit == 'X') {
        floatingAddress.append('X');
      } else if (bit == '0') {
        floatingAddress.append(adrBits.charAt(i));
      } else {
        floatingAddress.append('1');
      }
    }
    System.out.println("floatingAddress=" + floatingAddress);
    handleMem2(val, floatingAddress.toString(), 0);
  }

  private void handleMem2(long val, String floatingAddress, int pos)
  {
    if (pos >= mask.length()) {
      long adr = Long.parseLong(floatingAddress, 2);
      memory.put(adr, val);
      System.out.println("writing " + val + " into " + floatingAddress);
    } else {
      if (floatingAddress.charAt(pos) == 'X') {
        floatingAddress =
          floatingAddress.substring(0, pos) + '0' +
          floatingAddress.substring(pos + 1);
        handleMem2(val, floatingAddress, pos + 1);
        floatingAddress =
          floatingAddress.substring(0, pos) + '1' +
          floatingAddress.substring(pos + 1);
        handleMem2(val, floatingAddress, pos + 1);
      } else {
        handleMem2(val, floatingAddress, pos + 1);
      }
    }
  }

  private void run1(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new ArrayList<String>();
    String line;
    while ((line = reader.readLine()) != null) {
      values.add(line);
    }
    memory = new HashMap<Long, Long>();
    maskAnd = 0x3ffffL;
    maskOr = 0x0;
    boolean doMask = false;
    for (final var value : values) {
      if (value.startsWith("mask")) {
        handleMask(value);
        doMask = true;
      } else {
        handleMem(value, doMask);
        doMask = false;
      }
    }
    long sum = 0;
    for (final Long adr : memory.keySet()) {
      Long value = memory.get(adr);
      if (value != null) {
        sum += value;
      }
    }
    System.out.println(sum);
  }

  private void run3(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new ArrayList<String>();
    String line;
    while ((line = reader.readLine()) != null) {
      values.add(line);
    }
    memory = new HashMap<Long, Long>();
    //masks = new ArrayList<Long>();
    for (final var value : values) {
      if (value.startsWith("mask")) {
        String[] tokens = value.split(" ");
        mask = tokens[2];
      } else {
        handleMem2(value);
      }
    }
    long sum = 0;
    for (final Long adr : memory.keySet()) {
      Long value = memory.get(adr);
      if (value != null) {
        sum += value;
      }
    }
    System.out.println(sum);
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
    new Part1().run3("data.txt");
  }
}
