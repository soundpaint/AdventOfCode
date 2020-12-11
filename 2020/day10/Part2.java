import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;

public class Part2
{
  private int countCombinations(final boolean[] bits,
                                final int pos, final boolean value)
  {
    if (pos >= bits.length) return 1;
    bits[pos] = value;
    if (bits[pos] ||
        pos == 0 ||
        bits[pos -1] ||
        pos == 1 ||
        bits[pos - 2])
    {
      return
        countCombinations(bits, pos + 1, false) +
        countCombinations(bits, pos + 1, true);
    }
    return 0;
  }

  private int countCombinations(final int seqLen) {
    final boolean[] bits = new boolean[seqLen];
    return
      (countCombinations(bits, 0, false) +
       countCombinations(bits, 0, true)) >> 1;
  }

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new ArrayList<String>();
    String line;
    while ((line = reader.readLine()) != null) {
      values.add(line);
    }
    final int[] joltages = new int[values.size() + 2];
    joltages[0] = 0;
    for (int i = 0; i < values.size(); i++) {
      joltages[i + 1] = Integer.parseInt(values.get(i));
    }
    Arrays.sort(joltages, 0, joltages.length - 1);
    joltages[joltages.length - 1] = joltages[joltages.length - 2] + 3;
    int seqLen = 0;
    int prev = 0;
    long result = 1;
    for (int i = 0; i < joltages.length; i++) {
      final int curr = joltages[i];
      if (curr - prev == 1) {
        seqLen++;
      } else {
        if (seqLen >= 2) {
          result *= countCombinations(seqLen - 1);
        }
        seqLen = 0;
      }
      prev = curr;
    }
    if (seqLen >= 2) {
      result *= countCombinations(seqLen - 1);
    }
    System.out.println(result);
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part2().run("data.txt");
  }
}
