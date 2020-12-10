import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Part2
{
  private Part2() {}

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new ArrayList<Long>();
    String line;
    while ((line = reader.readLine()) != null) {
      final var value = Long.parseLong(line);
      if (value < 0L) throw new RuntimeException("< 0");
      values.add(value);
    }

    // result from part 1, may be different for your input data
    final Long totalSum = 530627549L;

    for (int indexFirst = 0; indexFirst < values.size(); indexFirst++) {
      long sum = values.get(indexFirst);
      int indexLast = indexFirst + 1;
      long min = sum;
      long max = sum;
      while ((sum < totalSum) && (indexLast < values.size())) {
        final long add = values.get(indexLast);
        if (add > max) max = add;
        if (add < min) min = add;
        sum += add;
        if (sum == totalSum) {
          System.out.println(min + max);
          return;
        }
        indexLast++;
      }
    }
    System.out.println("no match");
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part2().run("data.txt");
  }
}
