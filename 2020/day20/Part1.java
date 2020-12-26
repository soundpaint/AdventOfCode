import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Part1
{
  private final Map<Integer, Tile> id2tile;

  private enum Direction
  {
    EAST(1, 0), NORTH(0, -1), WEST(-1, 0), SOUTH(0, 1);

    private final int dx, dy;
    private final boolean isHorizontal;

    private Direction(final int dx, final int dy)
    {
      this.dx = dx;
      this.dy = dy;
      isHorizontal = dy == 0;
    }

    private Direction turnLeft(final int n)
    {
      final int modSum = (ordinal() + n) % 4;
      return Direction.values()[modSum < 0 ? modSum + 4 : modSum];
    }

    private Direction turnRight(final int n)
    {
      return turnLeft(-n);
    }

    private Direction turnOpposite()
    {
      return turnLeft(2);
    }

    private Direction flipHorizontally()
    {
      return isHorizontal ? this : turnOpposite();
    }

    private boolean isHorizontal() { return isHorizontal; }

    private int ccwDiffTo(final Direction other)
    {
      final int diff = other.ordinal() - ordinal();
      return diff >= 0 ? diff : diff + 4;
    }

    private int cwDiffTo(final Direction other)
    {
      return other.ccwDiffTo(this);
    }

    private int getDX() { return dx; }

    private int getDY() { return dy; }
  }

  private static class Pair
  {
    private final int x, y;

    public Pair(final int x, final int y)
    {
      this.x = x;
      this.y = y;
    }

    public boolean equals(final Object obj)
    {
      if (!(obj instanceof Pair)) return false;
      final Pair other = (Pair)obj;
      return (x == other.x) && (y == other.y);
    }

    public int hashCode() {
      return toString().hashCode();
    }

    public String toString() { return "(" + x + "," + y + ")"; }
  }

  private enum Transform
  {
    CCW0_0("0-", new Pair( 1,  0), new Pair( 0,  1), false),
    CCW1_0("1-", new Pair( 0,  1), new Pair(-1,  0), false),
    CCW2_0("2-", new Pair(-1,  0), new Pair( 0, -1), false),
    CCW3_0("3-", new Pair( 0, -1), new Pair( 1,  0), false),
    CCW0_H("0H", new Pair(-1,  0), new Pair( 0,  1), true),
    CCW1_H("1H", new Pair( 0, -1), new Pair(-1,  0), true),
    CCW2_H("2H", new Pair( 1,  0), new Pair( 0, -1), true),
    CCW3_H("3H", new Pair( 0,  1), new Pair( 1,  0), true);

    private final String mnemonic;
    private final Pair tx, ty;
    private final boolean upsideDown;
    private Transform[] CONCAT;

    private Transform(final String mnemonic,
                      final Pair tx, final Pair ty,
                      final boolean upsideDown)
    {
      this.mnemonic = mnemonic;
      this.tx = tx;
      this.ty = ty;
      this.upsideDown = upsideDown;
    }

    private String getMnemonic() { return mnemonic; }

    private Transform getReverse()
    {
      return concat(concat(this));
    }

    private boolean isUpsideDown() { return upsideDown; }

    private Direction applyOnDirection(final Direction direction)
    {
      return ordinal() < 4 ?
        direction.turnLeft(ordinal()) :
        direction.turnRight(ordinal() + (direction.isHorizontal() ? 2 : 0));
    }

    private void initCache()
    {
      CONCAT = new Transform[Transform.values().length];
      for (Transform t : Transform.values()) {
        CONCAT[t.ordinal()] = uncachedConcat(t);
      }
    }

    private static Transform fromCCW(final int n)
    {
      final int modN = n % 4;
      return Transform.values()[modN >= 0 ? modN : modN + 4];
    }

    private static Transform fromCW(final int n)
    {
      return fromCCW(-n);
    }

    private Transform turnLeft(final int n)
    {
      return concat(fromCCW(n));
    }

    private Transform turnRight(final int n)
    {
      return concat(fromCW(n));
    }

    private Transform turnOpposite()
    {
      return turnLeft(2);
    }

    private Transform flipHorizontally()
    {
      return concat(CCW0_H);
    }

    private Transform flipVertically()
    {
      return concat(CCW2_H);
    }

    private Transform concat(final Transform t)
    {
      if (CONCAT == null) {
        initCache();
      }
      return CONCAT[t.ordinal()];
    }

    private Transform uncachedConcat(final Transform t)
    {
      if (t == Transform.CCW0_0)
        return this;
      if (t == Transform.CCW1_0)
        if (ordinal() < 4)
          return Transform.values()[(ordinal() + 1) % 4];
        else
          return Transform.values()[(ordinal() + 3) % 4 + 4];
      if (t == Transform.CCW0_H)
        return Transform.values()[(ordinal() + 4) % 8];
      if (t.ordinal() < 4)
        return
          uncachedConcat(Transform.values()[t.ordinal() - 1]).
          uncachedConcat(Transform.CCW1_0);
      return
        uncachedConcat(Transform.values()[t.ordinal() - 4]).
        uncachedConcat(Transform.CCW0_H);
    }

    private Pair translate(final Pair p, final int size) {
      return new Pair(tx.x * (p.x + (size - 1) * ((tx.x - 1) >> 1)) +
                      tx.y * (p.y + (size - 1) * ((tx.y - 1) >> 1)),
                      ty.x * (p.x + (size - 1) * ((ty.x - 1) >> 1)) +
                      ty.y * (p.y + (size - 1) * ((ty.y - 1) >> 1)));
    }
  }

  private class LineUpInfo
  {
    /**
     * The other tile that matches this tile.
     */
    Tile other;

    /**
     * The direction where to add the other tile to this tile.
     */
    Direction direction;

    /**
     * The transform to apply on the other tile to match this tile.
     */
    Transform otherTransform;

    public LineUpInfo(Tile other, Direction direction, Transform otherTransform)
    {
      this.other = other;
      this.direction = direction;
      this.otherTransform = otherTransform;
    }
  }

  private enum TileType
  {
    CORNER, EDGE, OTHER;
  }

  private class Tile
  {
    int id;
    boolean[][] bits;
    ArrayList<LineUpInfo> lineUps;
    TileType type;
    Transform onBoardWithTransform;

    public Tile(int id, int size)
    {
      this.id = id;
      bits = new boolean[size][];
      for (int x = 0; x < size; x++) {
        bits[x] = new boolean[size];
      }
      lineUps = new ArrayList<LineUpInfo>();
    }

    void setBit(int x, int y, boolean bit)
    {
      bits[x][y] = bit;
    }

    boolean getBitAfterTransform(Transform t, int x, int y)
    {
      Pair p = t.getReverse().translate(new Pair(x, y), bits.length);
      return bits[p.x][p.y];
    }

    boolean getTransformedBit(int x, int y)
    {
      if (onBoardWithTransform == null) {
        throw new RuntimeException("no transformation set");
      }
      return getBitAfterTransform(onBoardWithTransform, x, y);
    }

    void determineType()
    {
      HashSet<Direction> directions = new HashSet<Direction>();
      for (LineUpInfo lineUp : lineUps) {
        directions.add(lineUp.direction);
      }
      if (directions.size() == 4) {
        type = TileType.OTHER;
      } else if (directions.size() == 3) {
        type = TileType.EDGE;
      } else if (directions.size() == 2) {
        type = TileType.CORNER;
      } else {
        throw new RuntimeException("tile with invalid number of non-matching borders: " + id);
      }
    }

    void checkLineUpWith(Tile other, Direction d, Transform t)
    {
      int ccwThis = Direction.NORTH.cwDiffTo(d);
      int ccwOther = Direction.NORTH.cwDiffTo(d.turnOpposite());
      Transform tThis = Transform.fromCCW(ccwThis);
      Transform tOther = t.concat(Transform.fromCCW(ccwOther));
      for (int x = 0; x < bits.length; x++) {
        boolean bitThis = getBitAfterTransform(tThis, bits.length - 1 - x, 0);
        boolean bitOther = other.getBitAfterTransform(tOther, x, 0);
        if (bitThis != bitOther) return;
      }
      lineUps.add(new LineUpInfo(other, d, t));
    }

    void checkLineUpWith(Tile other)
    {
      for (Direction d : Direction.values()) {
        for (Transform t : Transform.values()) {
          checkLineUpWith(other, d, t);
        }
      }
    }

    void printLineUps()
    {
      for (LineUpInfo lineUp : lineUps) {
        System.out.println("Tile " + id + " lines up " + lineUp.direction +
                           " with tile " + lineUp.other.id + " by applying " +
                           lineUp.otherTransform + " on " + lineUp.other.id);
      }
    }

    /**
     * Sort line ups by the refenced tile's number of line ups.
     */
    private void sortLineUps()
    {
      Collections.sort(lineUps, (lhs, rhs) -> {
          return
            lhs.other.lineUps.size() > rhs.other.lineUps.size() ? 1 :
            (lhs.other.lineUps.size() < rhs.other.lineUps.size() ? -1 : 0);
        });
    }

    public String toString()
    {
      StringBuffer s = new StringBuffer();
      s.append("Tile " + id + ":\r\n");
      for (int y = 0; y < bits.length; y++) {
        for (int x = 0; x < bits.length; x++) {
          s.append(bits[x][y] ? '#' : '.');
        }
        s.append("\r\n");
      }
      return s.toString();
    }
  }

  private Part1()
  {
    id2tile = new TreeMap<Integer, Tile>();
  }

  private ArrayList<Tile> sortTiles()
  {
    ArrayList<Tile> tiles = new ArrayList<Tile>(id2tile.values());
    Collections.sort(tiles, (lhs, rhs) -> {
        return
          lhs.lineUps.size() > rhs.lineUps.size() ? 1 :
          (lhs.lineUps.size() < rhs.lineUps.size() ? -1 : 0);
      });
    return tiles;
  }

  private void determineTileTypes()
  {
    for (Tile tile : id2tile.values()) {
      tile.determineType();
    }
  }

  private void printTileTypeInfo()
  {
    long result = 1;
    int edgesCount = 0;
    for (Tile tile : id2tile.values()) {
      if (tile.type == TileType.CORNER) {
        // System.out.println("corner: " + tile.id);
        result *= tile.id;
      } else if (tile.type == TileType.EDGE) {
        // System.out.println("edge: " + tile.id);
        edgesCount++;
      }
    }
    System.out.println(result); // Part 1 result
    // System.out.println("#non-corner edges: " + edgesCount);
  }

  private void determineLineUps()
  {
    for (Tile tile1 : id2tile.values()) {
      for (Tile tile2 : id2tile.values()) {
        if (tile1 != tile2) {
          tile1.checkLineUpWith(tile2);
        }
      }
    }
  }

  private void sortLineUps()
  {
    for (Tile tile : id2tile.values()) {
      tile.sortLineUps();
    }
  }

  private void printLineUps()
  {
    for (Tile tile : id2tile.values()) {
      tile.printLineUps();
    }
  }

  private void parseTile(int id, ArrayList<String> lines)
  {
    int size = lines.size();
    Tile tile = new Tile(id, size);
    for (int y = 0; y < size; y++) {
      String row = lines.get(y);
      for (int x = 0; x < size; x++) {
        tile.setBit(x, y, row.charAt(x) == '#');
      }
    }
    id2tile.put(id, tile);
  }

  private static boolean isNullOrEmpty(final String s) {
    return (s == null) || s.isEmpty();
  }

  private class Draw
  {
    Tile tile;
    int x;
    int y;

    public Draw(Tile tile, int x, int y)
    {
      this.tile = tile;
      this.x = x;
      this.y = y;
    }
  }

  private class Board
  {
    Tile[][] tiles;
    Stack<Draw> history;

    Board(int size)
    {
      tiles = new Tile[size][];
      for (int x = 0; x < size; x++) {
        tiles[x] = new Tile[size];
      }
      history = new Stack<Draw>();
    }

    boolean vacant(int x, int y)
    {
      return tiles[x][y] == null;
    }

    int getIdAt(int x, int y)
    {
      return tiles[x][y].id;
    }

    String toBitStr(boolean[] bits)
    {
      StringBuffer s = new StringBuffer();
      for (int i = 0; i < bits.length; i++) {
        s.append(bits[i] ? "#" : ".");
      }
      return s.toString();
    }

    /**
     * @param tile1 New tile to draw.
     * @param t1 Transform to be applied on tile1 when putting onto
     * board.
     * @param d From the perspective of the board, direction that
     * points from tile1 to tile2.
     * @param tile2 Existing tile on board.
     * @param t2 Transform applied on tile2 when put onto board.
     */
    boolean fits(Tile tile1, Transform t1, Direction d,
                 Tile tile2, Transform t2)
    {
      for (LineUpInfo lineUp : tile1.lineUps) {
        if (lineUp.other != tile2) continue;
        Direction tile2Direction = t1.applyOnDirection(lineUp.direction);
        if (tile2Direction != d)
          continue;
        if (lineUp.otherTransform.concat(t1) == t2)
          return true;
      }
      return false;
    }

    boolean fits(Tile tile, Transform t, Direction d, int otherX, int otherY)
    {
      if ((otherX < 0) || (otherX >= tiles.length))
        return true;
      if ((otherY < 0) || (otherY >= tiles.length))
        return true;
      if (vacant(otherX, otherY))
        return true;
      Tile otherTile = tiles[otherX][otherY];
      return fits(tile, t, d, otherTile, otherTile.onBoardWithTransform);
    }

    boolean fits(Tile tile, Transform t, int x, int y)
    {
      return
        fits(tile, t, Direction.EAST, x + 1, y) &&
        fits(tile, t, Direction.NORTH, x, y - 1) &&
        fits(tile, t, Direction.WEST, x - 1, y) &&
        fits(tile, t, Direction.SOUTH, x, y + 1);
    }

    boolean push(Tile tile, Transform t, int x, int y)
    {
      if (tiles[x][y] != null) throw new RuntimeException();
      tile.onBoardWithTransform = t;
      tiles[x][y] = tile;
      history.push(new Draw(tile, x, y));
      return false;
    }

    void pop()
    {
      Draw draw = history.pop();
      draw.tile.onBoardWithTransform = null;
      tiles[draw.x][draw.y] = null;
    }

    boolean containsTile(Tile tile)
    {
      return history.contains(tile);
    }

    public String toString()
    {
      StringBuffer s = new StringBuffer();
      for (int y = 0; y < tiles.length; y++) {
        for (int x = 0; x < tiles.length; x++) {
          Tile tile = tiles[x][y];
          if (x > 0) s.append(" ");
          if (tile != null) {
            s.append(tile.id +
                     " (" + tile.onBoardWithTransform.getMnemonic() + ")");
          } else {
            s.append("---- (--)");
          }
        }
        s.append("\r\n");
      }
      return s.toString();
    }
  }

  private boolean nextPositionOk(int tilesPerRow, int x, int y, int dx, int dy)
  {
    if (y == 0) {
      return (x == 1) && (dx == 0) && (dy == 1);
    } else if (x == 0) {
      return (y == 1) && (dx == 1) && (dy == 0);
    }
    if (y % 2 == 1) {
      // left-to-right-row
      if ((dx == 1) && (dy == 0))
        return true;
      return (x == tilesPerRow - 2) && (dx == 0) && (dy == 1);
    } else {
      // right-to-left-row
      if ((dx == -1) && (dy == 0))
        return true;
      return (x == 1) && (dx == 0) && (dy == 1);
    }
  }

  private boolean examineInnerDraw(Board board,
                                   Tile tile, Transform t, int x, int y)
  {
    int tilesPerRow = board.tiles.length;
    if (board.history.size() == tilesPerRow * tilesPerRow)
      return true;
    for (LineUpInfo lineUp : tile.lineUps) {
      if (lineUp.other.onBoardWithTransform != null)
        continue; // already on board
      Direction transformedDirection = t.applyOnDirection(lineUp.direction);
      int dx = transformedDirection.getDX();
      int dy = transformedDirection.getDY();
      if (!nextPositionOk(tilesPerRow, x, y, dx, dy))
        continue;
      Transform concatTransform = lineUp.otherTransform.concat(t);
      boolean solved =
        drawInnerTile(board, lineUp.other, concatTransform, x + dx, y + dy);
      if (solved)
        return true;
    }
    return false;
  }

  private boolean drawInnerTile(Board board,
                                Tile tile, Transform t, int x, int y)
  {
    if (!board.vacant(x, y))
      return false;
    if (!board.fits(tile, t, x, y))
      return false;
    boolean solved = false;
    if (!board.push(tile, t, x, y))
      solved = examineInnerDraw(board, tile, t, x, y);
    if (!solved)
      board.pop();
    return solved;
  }

  private Transform getUpperLeftCornerTransform(Tile corner)
  {
    TreeSet<Direction> directions = new TreeSet<Direction>();
    for (LineUpInfo lineUp : corner.lineUps) {
      directions.add(lineUp.direction);
    }
    for (int ccw = 0; ccw < 4; ccw++) {
      if (directions.contains(Direction.EAST.turnRight(ccw)) &&
          directions.contains(Direction.SOUTH.turnRight(ccw)))
        return Transform.fromCCW(ccw);
    }
    throw new RuntimeException("unexpected case");
  }

  private boolean examineEdgeDraw(Board board,
                                  Tile tile, Transform t, int x, int y)
  {
    if (board.history.size() == board.tiles.length * 4 - 4)
      return true;
    for (LineUpInfo lineUp : tile.lineUps) {
      if (lineUp.other.onBoardWithTransform != null)
        continue; // already on board
      Direction transformedDirection = t.applyOnDirection(lineUp.direction);
      int dx = transformedDirection.getDX();
      int dy = transformedDirection.getDY();
      Transform concatTransform = lineUp.otherTransform.concat(t);
      boolean solved =
        drawEdgeTile(board, lineUp.other, concatTransform, x + dx, y + dy);
      if (solved)
        return true;
    }
    return false;
  }

  private boolean drawEdgeTile(Board board,
                               Tile tile, Transform t, int x, int y)
  {
    if (tile.type == TileType.OTHER)
      return false;
    if ((x == 0) || (x == board.tiles.length - 1)) {
      if ((y < 0) || (y >= board.tiles.length))
        return false;
    } else if ((y == 0) || (y == board.tiles.length - 1)) {
      if ((x < 0) || (x >= board.tiles.length))
        return false;
    } else {
      return false;
    }
    if (!board.vacant(x, y))
      return false;
    if (!board.fits(tile, t, x, y))
      return false;
    boolean solved = false;
    if (!board.push(tile, t, x, y))
      solved = examineEdgeDraw(board, tile, t, x, y);
    if (!solved)
      board.pop();
    return solved;
  }

  private boolean drawEdgeTiles(Board board,
                                ArrayList<Tile> sortedTiles, int tilesPerRow)
  {
    Tile upperLeftCorner =
      sortedTiles
      .stream()
      .filter(tile -> tile.type == TileType.CORNER)
      .findFirst()
      .get();
    Transform t = getUpperLeftCornerTransform(upperLeftCorner);
    return drawEdgeTile(board, upperLeftCorner, t, 0, 0);
  }

  private static final String[] MONSTER_CHARS = {
    "                  # ",
    "#    ##    ##    ###",
    " #  #  #  #  #  #   "
  };

  private static final Boolean[][] MONSTER =
    Arrays.stream(MONSTER_CHARS).map(row -> {
        return row.chars().mapToObj(c -> c == '#')
        .collect(Collectors.toList()).toArray(Boolean[]::new);
      })
    .collect(Collectors.toList()).toArray(Boolean[][]::new);

  private class MonsterOverlay
  {
    ArrayList<Pair> locations;
    Transform t;

    private MonsterOverlay(ArrayList<Pair> locations, Transform t)
    {
      this.locations = locations;
      this.t = t;
    }

    private int countWaves(Boolean[][] monster)
    {
      HashSet<Pair> waves = new HashSet<Pair>();
      for (Pair location : locations) {
        int x0 = location.x;
        int y0 = location.y;
        for (int x = 0; x < monster.length; x++) {
          for (int y = 0; y < monster[0].length; y++) {
            if (monster[x][y]) {
              waves.add(new Pair(x0 + x, y0 + y));
            }
          }
        }
      }
      return waves.size();
    }
  }

  private class Image
  {
    final boolean[][] pixels;

    Image(Board board)
    {
      int tilesPerRow = board.tiles.length;
      int pixelsPerTile = board.tiles[0][0].bits.length;
      int imageSize = tilesPerRow * (pixelsPerTile - 2);
      pixels = new boolean[imageSize][];
      for (int x = 0; x < imageSize; x++) {
        pixels[x] = new boolean[imageSize];
      }
      for (int tileX = 0; tileX < tilesPerRow; tileX++) {
        int tileXBase = tileX * (pixelsPerTile - 2);
        for (int tileY = 0; tileY < tilesPerRow; tileY++) {
          int tileYBase = tileY * (pixelsPerTile - 2);
          Tile tile = board.tiles[tileX][tileY];
          for (int x = 1; x < tile.bits.length - 1; x++) {
            for (int y = 1; y < tile.bits.length - 1; y++) {
              pixels[tileXBase + x - 1][tileYBase + y - 1] =
                tile.getTransformedBit(x, y);
            }
          }
        }
      }
    }

    boolean getBitAfterTransform(Transform t, int x, int y)
    {
      Pair p = t.getReverse().translate(new Pair(x, y), pixels.length);
      return pixels[p.x][p.y];
    }

    boolean monsterAt(Boolean[][] monster, Transform t, int x0, int y0)
    {
      for (int x = 0; x < monster.length; x++) {
        for (int y = 0; y < monster[0].length; y++) {
          if (monster[x][y] && !getBitAfterTransform(t, x0 + x, y0 + y)) {
            return false;
          }
        }
      }
      return true;
    }

    ArrayList<Pair> monstersAt(Boolean[][] monster, Transform t)
    {
      ArrayList<Pair> locations = new ArrayList<Pair>();
      for (int x0 = 0; x0 < pixels.length - monster.length; x0++) {
        for (int y0 = 0; y0 < pixels.length - monster[0].length; y0++) {
          if (monsterAt(monster, t, x0, y0))
            locations.add(new Pair(x0, y0));
        }
      }
      return locations;
    }

    MonsterOverlay searchMonster(Boolean[][] monster)
    {
      ArrayList<Pair> maxLocations = null;
      Transform maxT = null;
      for (Transform t : Transform.values()) {
        ArrayList<Pair> locations = monstersAt(monster, t);
        int size = locations.size();
        System.out.println("found " + size + " monsters for transform " + t);
        if ((maxLocations == null) || (size > maxLocations.size())) {
          maxLocations = locations;
          maxT = t;
        }
      }
      return new MonsterOverlay(maxLocations, maxT);
    }

    int countWaves()
    {
      int imageSize = pixels.length;
      int count = 0;
      for (int y = 0; y < imageSize; y++) {
        for (int x = 0; x < imageSize; x++) {
          if (pixels[x][y]) count++;
        }
      }
      return count;
    }

    public String toString()
    {
      int imageSize = pixels.length;
      StringBuffer s = new StringBuffer();
      s.append("Image (" + imageSize + "x" + imageSize + "):\r\n");
      for (int y = 0; y < imageSize; y++) {
        for (int x = 0; x < imageSize; x++) {
          s.append(pixels[x][y] ? '#' : '.');
        }
        s.append("\r\n");
      }
      return s.toString();
    }
  }

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new ArrayList<String>();
    String line;
    while ((line = reader.readLine()) != null) {
      int id = Integer.parseInt(line.split("Tile ")[1].split(":")[0]);
      values.clear();
      while (!isNullOrEmpty(line = reader.readLine())) {
        values.add(line);
      }
      parseTile(id, values);
    }
    /*
    for (Tile tile : id2tile.values()) {
      System.out.println(tile);
    }
    */
    int tilesPerRow = (int)Math.round(Math.sqrt(id2tile.size()));
    determineLineUps();
    determineTileTypes();
    //sortLineUps();
    printLineUps();
    printTileTypeInfo();
    // System.exit(0); // STOP HERE FOR PART 1
    ArrayList<Tile> sortedTiles = sortTiles();
    Board board = new Board(tilesPerRow);
    if (!drawEdgeTiles(board, sortedTiles, tilesPerRow)) {
      System.out.println("draw edge tiles: no solution found -> giving up");
      return;
    }
    System.out.println(board);
    Draw draw = board.history.peek();
    if (!examineInnerDraw(board,
                          draw.tile,
                          draw.tile.onBoardWithTransform,
                          draw.x, draw.y)) {
      System.out.println("draw inner tiles: no solution found -> giving up");
      return;
    }
    System.out.println(board);
    Image image = new Image(board);
    System.out.println(image);
    int waves = image.countWaves();
    System.out.println("count " + waves + " waves");
    MonsterOverlay monsterOverlay = image.searchMonster(MONSTER);
    int overlayedWaves = monsterOverlay.countWaves(MONSTER);
    System.out.println("result part 2: " + (waves - overlayedWaves));
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run("data.txt");
  }
}
