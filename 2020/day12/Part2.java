import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Part2
{
  private Part2() {}

  private enum Direction {
    EAST,
    NORTH,
    WEST,
    SOUTH
  }

  private int east;
  private int north;
  private int wayEast;
  private int wayNorth;
  private Direction direction;

  private void forward(final int amount)
  {
    north += amount * wayNorth;
    east += amount * wayEast;
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
    int swap;
    switch (turns) {
    case -3:
    case 1:
      swap = wayNorth;
      wayNorth = wayEast;
      wayEast = -swap;
      break;
    case -2:
    case 0:
    case 2:
      wayEast = -wayEast;
      wayNorth = -wayNorth;
      break;
    case -1:
    case 3:
      swap = wayEast;
      wayEast = wayNorth;
      wayNorth = -swap;
      break;
    default:
      assert false: "unexpected case fall-through";
    }
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
    wayEast = 10;
    wayNorth = 1;
    east = 0;
    north = 0;
    for (final var value : values) {
      final char ch = value.charAt(0);
      final int amount = Integer.parseInt(value.substring(1));
      switch (ch) {
      case 'N':
        wayNorth += amount;
        break;
      case 'S':
        wayNorth -= amount;
        break;
      case 'E':
        wayEast += amount;
        break;
      case 'W':
        wayEast -= amount;
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
    new Part2().run("data.txt");
  }
}
