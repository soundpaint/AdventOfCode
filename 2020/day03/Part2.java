import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Part2
{
  private final ArrayList<String> lines;

  private Part2() {
    lines = new ArrayList<String>();
  }

  private boolean isTree(final int posX, final int posY)
  {
    final String line = lines.get(posY);
    return line.charAt(posX % line.length()) == '#';
  }

  private int countForSlope(final int stepsX, final int stepsY)
  {
    int count = 0;
    int posX = 0;
    int posY = 0;
    do {
      if (isTree(posX, posY)) count++;
      posX += stepsX;
      posY += stepsY;
    } while (posY < lines.size());
    return count;
  }

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    lines.clear();
    String line;
    while ((line = reader.readLine()) != null) {
      lines.add(line);
    }
    final int count1 = countForSlope(1, 1);
    final int count2 = countForSlope(3, 1);
    final int count3 = countForSlope(5, 1);
    final int count4 = countForSlope(7, 1);
    final int count5 = countForSlope(1, 2);
    System.out.println(((long)count1) * count2 * count3 * count4 * count5);
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part2().run("data.txt");
  }
}
