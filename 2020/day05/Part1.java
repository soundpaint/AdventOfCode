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
    final var values = new ArrayList<String>();
    String line;
    while ((line = reader.readLine()) != null) {
      values.add(line);
    }
    int maxId = -1;
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
      if (id > maxId) maxId = id;
    }
    System.out.println(maxId);
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run("data.txt");
  }
}
