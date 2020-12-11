import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class Part1
{
  private Part1() {}

  private enum Direction {
    EAST(+1, 0),
    NORTH_EAST(+1, +1),
    NORTH(0, +1),
    NORTH_WEST(-1, +1),
    WEST(-1, 0),
    SOUTH_WEST(-1, -1),
    SOUTH(0, -1),
    SOUTH_EAST(+1, -1);

    private final int dx;
    private final int dy;

    private Direction()
    {
      throw new UnsupportedOperationException("empty constructor");
    }

    private Direction(final int dx, final int dy)
    {
      this.dx = dx;
      this.dy = dy;
    }

    public int getDX() { return dx; }

    public int getDY() { return dy; }

    public static Stream<Direction> stream()
    {
      return Arrays.stream(Direction.values());
    }
  }

  private enum CellType {
    OUTSIDE(' '),
    FLOOR('.'),
    EMPTY_SEAT('L'),
    OCCUPIED_SEAT('#');

    private final char symbol;
    private final String symbolAsString;

    private CellType()
    {
      throw new UnsupportedOperationException("empty constructor");
    }

    private CellType(final char symbol)
    {
      this.symbol = symbol;
      this.symbolAsString = String.valueOf(symbol);
    }

    public char getSymbol() { return symbol; }

    public String toString() { return symbolAsString; }
  }

  private CellType getCell(final CellType[][] map, final int x, final int y)
  {
    if ((y < 0) ||
        (y >= map.length) ||
        (x < 0) ||
        (x >= map[0].length))
      return CellType.OUTSIDE;
    return map[y][x];
  }

  private boolean isAdjacentOccupiedSeat(final CellType[][] map,
                                         final int x, final int y,
                                         final int dx, final int dy)
  {
    return getCell(map, x + dx, y + dy) == CellType.OCCUPIED_SEAT;
  }

  private boolean haveAdjacentOccupiedSeats(final CellType[][] map,
                                            final int x, final int y,
                                            final int count)
  {
    return
      Direction.stream().
      filter(d -> isAdjacentOccupiedSeat(map, x, y, d.getDX(), d.getDY())).
      limit(count).
      count() == count;
  }

  private int createNextGen(final CellType[][] originMap,
                            final CellType[][] updateMap,
                            final int sum)
  {
    int new_sum = sum;
    for (int y = 0; y < originMap.length; y++) {
      for (int x = 0; x < originMap[0].length; x++) {
        final CellType originCellType = originMap[y][x];
        final CellType updateCellType;
        if ((originCellType == CellType.EMPTY_SEAT) &&
            !haveAdjacentOccupiedSeats(originMap, x, y, 1))
        {
          updateCellType = CellType.OCCUPIED_SEAT;
          new_sum++;
        } else if ((originCellType == CellType.OCCUPIED_SEAT) &&
                   haveAdjacentOccupiedSeats(originMap, x, y, 4))
        {
          updateCellType = CellType.EMPTY_SEAT;
          new_sum--;
        } else { updateCellType = originCellType; }
        updateMap[y][x] = updateCellType;
      }
    }
    return new_sum;
  }

  private void printMap(final CellType[][] map)
  {
    for (int y = 0; y < map.length; y++) {
      for (int x = 0; x < map[0].length; x++) {
        System.out.print(map[y][x]);
      }
      System.out.println();
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
    CellType[][] originMap =
      new CellType[values.size()][values.get(0).length()];
    CellType[][] updateMap =
      new CellType[values.size()][values.get(0).length()];
    for (int y = 0; y < values.size(); y++) {
      final String row = values.get(y);
      for (int x = 0; x < row.length(); x++) {
        final char cellSymbol = row.charAt(x);
        if (cellSymbol == 'L') {
          originMap[y][x] = CellType.EMPTY_SEAT;
        } else if (cellSymbol == '.') {
          originMap[y][x] = CellType.FLOOR;
        } else {
          throw new RuntimeException("invalid cell symbol: " + cellSymbol);
        }
      }
    }
    int prevSum = -1;
    int sum = 0;
    while (sum != prevSum) {
      prevSum = sum;
      sum = createNextGen(originMap, updateMap, prevSum);
      final CellType[][] swapMap = originMap;
      originMap = updateMap;
      updateMap = swapMap;
    }
    System.out.println(sum);
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run("data.txt");
  }
}
