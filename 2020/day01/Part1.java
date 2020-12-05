import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Part1
{
  private Part1() {}

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new ArrayList<Integer>();
    String line;
    while ((line = reader.readLine()) != null) {
      values.add(Integer.parseInt(line));
    }
    outer:
    for (int index1 = 0; index1 < values.size(); index1++) {
      final int value1 = values.get(index1);
      for (int index2 = 0; index2 < values.size(); index2++) {
        if (index2 == index1) continue;
        final int value2 = values.get(index2);
        if (value1 + value2 == 2020) {
          System.out.println((((long)value1) * value2));
          break outer;
        }
      }
    }
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run("data.txt");
  }
}
