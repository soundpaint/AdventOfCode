import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Part1
{
  private Part1() {}

  private enum Direction {
    EAST,
    NORTH,
    WEST,
    SOUTH
  }

  private int east;
  private int north;
  private Direction direction;

  private void forward(final int amount)
  {
    switch (direction) {
    case NORTH:
      north += amount;
      break;
    case SOUTH:
      north -= amount;
      break;
    case EAST:
      east += amount;
      break;
    case WEST:
      east -= amount;
      break;
    default:
      throw new RuntimeException();
    }
  }

  private void left(final int degrees)
  {
    int turns = degrees / 90;
    if (turns * 90 != degrees)
      throw new RuntimeException("unexpected input: " +
                                 "degree is not a multiple of 90");
    if (turns < 0) turns += 4;
    final int newOrdDirection = (direction.ordinal() + turns) % 4;
    direction = Direction.values()[newOrdDirection];
  }

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new ArrayList<String>();
    String line;
    while ((line = reader.readLine()) != null) {
      values.add(line);
    }
    direction = Direction.EAST;
    east = 0;
    north = 0;
    for (final var value : values) {
      final char ch = value.charAt(0);
      final int amount = Integer.parseInt(value.substring(1));
      switch (ch) {
      case 'N':
        north += amount;
        break;
      case 'S':
        north -= amount;
        break;
      case 'E':
        east += amount;
        break;
      case 'W':
        east -= amount;
        break;
      case 'L':
        left(amount);
        break;
      case 'R':
        left(-amount);
        break;
      case 'F':
        forward(amount);
        break;
      default:
        throw new RuntimeException("unexpected command letter: " + ch);
      }
    }
    System.out.println(Math.abs(north) + Math.abs(east));
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run("data.txt");
  }
}
