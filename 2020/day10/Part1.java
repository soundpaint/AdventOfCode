import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;

public class Part1
{
  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new ArrayList<String>();
    String line;
    while ((line = reader.readLine()) != null) {
      values.add(line);
    }
    final int[] joltages = new int[values.size()];
    for (int i = 0; i < values.size(); i++) {
      joltages[i] = Integer.parseInt(values.get(i));
    }
    Arrays.sort(joltages);
    int small = 0;
    int large = 0;
    int prev = 0;
    for (int i = 0; i < joltages.length; i++) {
      final int curr = joltages[i];
      if (curr - prev == 1) {
        small++;
      } else {
        large++;
      }
      prev = curr;
    }
    large++;
    System.out.println(small * large);
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run("data.txt");
  }
}
