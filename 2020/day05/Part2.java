import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class Part2
{
  private Part2() {}

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new ArrayList<String>();
    String line;
    while ((line = reader.readLine()) != null) {
      values.add(line);
    }
    int minId = 0b10000000000;
    int maxId = -1;
    final HashSet<Integer> ids = new HashSet<Integer>();
    for (final var value : values) {
      final String rowLetterRepresentation = value.substring(0, 7);
      final String rowBinRepresentation =
        rowLetterRepresentation.replace('F', '0').replace('B', '1');
      final Integer row = Integer.parseInt(rowBinRepresentation, 2);
      final String colLetterRepresentation = value.substring(7, 10);
      final String colBinRepresentation =
        colLetterRepresentation.replace('L', '0').replace('R', '1');
      final Integer col = Integer.parseInt(colBinRepresentation, 2);
      final Integer id = row * 8 + col;
      ids.add(id);
      if (id > maxId) maxId = id;
      if (id < minId) minId = id;
    }
    // left-most seat of second row
    final int firstValidId = (minId & 0b1111111000) + 0b1000;

    // right-most seat of last but one row
    final int lastValidId = (maxId & 0b1111111000) - 0b0001;

    int maxMatch = -1;
    for (Integer id = firstValidId; id <= lastValidId; id++) {
      if (!ids.contains(id)) {
        System.out.println(id);
      }
    }
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part2().run("data.txt");
  }
}
