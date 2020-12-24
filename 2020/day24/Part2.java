import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class Part2
{
  private final ArrayList<Path> paths;

  private Part2()
  {
    paths = new ArrayList<Path>();
  }

  private enum Direction
  {
    E(2, 0), NE(1, 1), NW(-1, 1), W(-2, 0), SW(-1, -1), SE(1, -1);

    private final int dx, dy;

    private Direction(final int dx, final int dy)
    {
      this.dx = dx;
      this.dy = dy;
    }
  }

  private static class Pair
  {
    private final int x;
    private final int y;

    public Pair(final int x, final int y) { this.x = x; this.y = y; }

    @Override
    public boolean equals(final Object obj)
    {
      if (obj == null) return false;
      if (!(obj instanceof Pair)) return false;
      final Pair other = (Pair)obj;
      return (other.x == x) && (other.y == y);
    }

    @Override
    public int hashCode()
    {
      return (Integer.toString(x) + y).hashCode();
    }

    @Override
    public String toString()
    {
      return "(" + x + ", " + y + ")";
    }
  }

  private static class Path
  {
    private final ArrayList<Direction> steps;

    private Path()
    {
      steps = new ArrayList<Direction>();
    }

    private Pair getTranslation()
    {
      int x = 0;
      int y = 0;
      for (final Direction step : steps) {
        x += step.dx;
        y += step.dy;
      }
      return new Pair(x, y);
    }

    private void addStep(final Direction direction)
    {
      steps.add(direction);
    }

    private static void parseError(final String unparsed, final int pos,
                                   final char ch, final String message)
    {
      throw new RuntimeException("parse error at column " + pos +
                                 " ('" + ch + "'): " +
                                 message + ", line: " + unparsed);
    }

    private static Path parse(final String unparsed)
    {
      final Path path = new Path();
      char pushback = 0;
      int pos = 0;
      while (pos < unparsed.length()) {
        final char ch = unparsed.charAt(pos++);
        switch (ch) {
        case 'e':
          if (pushback != 0) {
            if (pushback == 's') {
              path.addStep(Direction.SE);
              pushback = 0;
            } else if (pushback == 'n') {
              path.addStep(Direction.NE);
              pushback = 0;
            } else {
              parseError(unparsed, pos, ch, "expected 's' or 'n'");
            }
          } else {
            path.addStep(Direction.E);
          }
          break;
        case 'w':
          if (pushback != 0) {
            if (pushback == 's') {
              path.addStep(Direction.SW);
              pushback = 0;
            } else if (pushback == 'n') {
              path.addStep(Direction.NW);
              pushback = 0;
            } else {
              parseError(unparsed, pos, ch, "expected 's' or 'n'");
            }
          } else {
            path.addStep(Direction.W);
          }
          break;
        case 'n':
        case 's':
          if (pushback == 0) {
            pushback = ch;
          } else {
            parseError(unparsed, pos, ch, "expected 'e' or 'w'");
          }
          break;
        default:
          parseError(unparsed, pos, ch, "expected 'n', 's', 'e' or 'w'");
          break;
        }
      }
      if (pushback != 0) {
        parseError(unparsed, pos, pushback, "orphaned trailing character");
      }
      return path;
    }

    public String toString()
    {
      final StringBuffer s = new StringBuffer();
      for (final Direction step : steps) {
        if (s.length() > 0) s.append(", ");
        s.append(step);
      }
      return s.toString();
    }
  }

  private HashSet<Pair> applyDaily(final HashSet<Pair> blackTiles)
  {
    final HashSet<Pair> result = new HashSet<Pair>();
    applyRule1(blackTiles, result);
    applyRule2(blackTiles, result);
    return result;
  }

  private void applyRule1(final HashSet<Pair> blackTiles,
                          final HashSet<Pair> result)
  {
    for (final Pair p : blackTiles) {
      int count = 0;
      for (final Direction d : Direction.values()) {
        if (blackTiles.contains(new Pair(p.x + d.dx, p.y + d.dy))) count++;
      }
      if ((count != 0) && (count <= 2))
        result.add(p);
    }
  }

  private void applyRule2(final HashSet<Pair> blackTiles,
                          final HashSet<Pair> result)
  {
    final HashSet<Pair> whiteNeighbourTiles = new HashSet<Pair>();
    for (final Pair p : blackTiles) {
      for (final Direction d : Direction.values()) {
        final Pair neighbour = new Pair(p.x + d.dx, p.y + d.dy);
        if (!blackTiles.contains(neighbour)) {
          whiteNeighbourTiles.add(neighbour);
        }
      }
    }
    final HashSet<Pair> newBlackTiles = new HashSet<Pair>();
    for (final Pair p : whiteNeighbourTiles) {
      int count = 0;
      for (final Direction d : Direction.values()) {
        final Pair neighbour = new Pair(p.x + d.dx, p.y + d.dy);
        if (blackTiles.contains(neighbour)) {
          count++;
        }
      }
      if (count == 2)
        newBlackTiles.add(p);
    }
    result.addAll(newBlackTiles);
  }

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new ArrayList<String>();
    String line;
    while ((line = reader.readLine()) != null) {
      final Path path = Path.parse(line);
      paths.add(path);
    }
    HashSet<Pair> blackTiles = new HashSet<Pair>();
    for (final Path path : paths) {
      final Pair p = path.getTranslation();
      if (blackTiles.contains(p)) {
        blackTiles.remove(p);
      } else {
        blackTiles.add(p);
      }
    }
    for (int count = 0; count < 100; count++) {
      blackTiles = applyDaily(blackTiles);
      // System.out.println("Day " + (count + 1) + ": " + blackTiles.size());
    }
    System.out.println(blackTiles.size());
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part2().run("data.txt");
  }
}
