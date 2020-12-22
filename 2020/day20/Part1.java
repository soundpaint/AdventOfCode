import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Part1
{
  private Part1()
  {
    id2tile = new TreeMap<Integer, Tile>();
  }

  Map<Integer, Tile> id2tile;

  private enum Direction
  {
    EAST, NORTH, WEST, SOUTH;

    private Direction turnLeft() { return turnLeft(1); }

    private Direction turnLeft(int n)
    {
      int modSum = (ordinal() + n) % 4;
      return Direction.values()[modSum < 0 ? modSum + 4 : modSum];
    }

    private Direction turnRight() { return turnRight(1); }

    private Direction turnRight(int n)
    {
      return turnLeft(-n);
    }

    private Direction turnOpposite()
    {
      return turnLeft(2);
    }

    private boolean isHorizontalDirection()
    {
      switch (this) {
      case EAST:
      case WEST:
        return true;
      case NORTH:
      case SOUTH:
        return false;
      default: throw new RuntimeException("unexpected case");
      }
    }

    private int ccwDiffTo(Direction other)
    {
      int diff = other.ordinal() - ordinal();
      return diff >= 0 ? diff : diff + 4;
    }

    private int cwDiffTo(Direction other)
    {
      return other.ccwDiffTo(this);
    }

    private int getDX()
    {
      switch (this) {
      case EAST:
        return +1;
      case WEST:
        return -1;
      case NORTH:
      case SOUTH:
        return 0;
      default: throw new RuntimeException("unexpected case");
      }
    }

    private int getDY()
    {
      switch (this) {
      case EAST:
      case WEST:
        return 0;
      case NORTH:
        return -1;
      case SOUTH:
        return +1;
      default: throw new RuntimeException("unexpected case");
      }
    }
  }

  private static class Pair
  {
    int x;
    int y;
    Pair(int x, int y)
    {
      this.x = x;
      this.y = y;
    }
    public String toString() { return "(" + x + "," + y + ")"; }
  }

  private enum Transform
  {
    CCW0,
    CCW0_HORIZ_FLIP,
    CCW1,
    CCW1_HORIZ_FLIP,
    CCW2,
    CCW2_HORIZ_FLIP,
    CCW3,
    CCW3_HORIZ_FLIP;

    private Transform[] CONCAT;

    private Transform getReverse()
    {
      switch (this) {
      case CCW0: return Transform.CCW0;
      case CCW0_HORIZ_FLIP: return Transform.CCW0_HORIZ_FLIP;
      case CCW1: return Transform.CCW3;
      case CCW1_HORIZ_FLIP: return Transform.CCW1_HORIZ_FLIP;
      case CCW2: return Transform.CCW2;
      case CCW2_HORIZ_FLIP: return Transform.CCW2_HORIZ_FLIP;
      case CCW3: return Transform.CCW1;
      case CCW3_HORIZ_FLIP: return Transform.CCW3_HORIZ_FLIP;
      default: throw new RuntimeException("unexpected case");
      }
    }

    private boolean isUpsideDown()
    {
      switch (this) {
      case CCW0:
      case CCW1:
      case CCW2:
      case CCW3:
        return false;
      case CCW0_HORIZ_FLIP:
      case CCW1_HORIZ_FLIP:
      case CCW2_HORIZ_FLIP:
      case CCW3_HORIZ_FLIP:
        return true;
      default: throw new RuntimeException("unexpected case");
      }
    }

    private Direction applyOnDirection(Direction direction)
    {
      switch (this) {
      case CCW0:
        return direction;
      case CCW1:
        return direction.turnLeft(1);
      case CCW2:
        return direction.turnLeft(2);
      case CCW3:
        return direction.turnLeft(3);
      case CCW0_HORIZ_FLIP:
        return direction.isHorizontalDirection() ?
          direction.turnOpposite() : direction;
      case CCW1_HORIZ_FLIP:
        return direction.isHorizontalDirection() ?
          direction.turnLeft() : direction.turnRight();
      case CCW2_HORIZ_FLIP:
        return !direction.isHorizontalDirection() ?
          direction.turnOpposite() : direction;
      case CCW3_HORIZ_FLIP:
        return !direction.isHorizontalDirection() ?
          direction.turnLeft() : direction.turnRight();
      default: throw new RuntimeException("unexpected case");
      }
    }

    private Direction getDirection()
    {
      switch (this) {
      case CCW0:
      case CCW2_HORIZ_FLIP:
        return Direction.EAST;
      case CCW1:
      case CCW1_HORIZ_FLIP:
        return Direction.NORTH;
      case CCW2:
      case CCW0_HORIZ_FLIP:
        return Direction.WEST;
      case CCW3:
      case CCW3_HORIZ_FLIP:
        return Direction.SOUTH;
      default: throw new RuntimeException("unexpected case");
      }
    }

    private void initCache()
    {
      CONCAT = new Transform[Transform.values().length];
      for (Transform t : Transform.values()) {
        CONCAT[t.ordinal()] = uncachedConcat(t);
      }
    }

    private static Transform ident()
    {
      return CCW0;
    }

    private static Transform fromCCW(int n)
    {
      int modN = n % 4;
      if (modN < 0) modN += 4;
      switch (modN) {
      case 0: return CCW0;
      case 1: return CCW1;
      case 2: return CCW2;
      case 3: return CCW3;
      default: throw new RuntimeException("unexpected case");
      }
    }

    private static Transform fromCW(int n)
    {
      return fromCCW(-n);
    }

    private Transform turnLeft(int n)
    {
      return concat(fromCCW(n));
    }

    private Transform turnRight(int n)
    {
      return concat(fromCW(n));
    }

    private Transform turnOpposite()
    {
      return turnLeft(2);
    }

    private Transform flipHorizontally()
    {
      return concat(CCW0_HORIZ_FLIP);
    }

    private Transform flipVertically()
    {
      return concat(CCW2_HORIZ_FLIP);
    }

    private Transform concat(Transform t)
    {
      if (CONCAT == null) {
        initCache();
      }
      return CONCAT[t.ordinal()];
    }

    private Transform uncachedConcat(Transform t)
    {
      if (this == Transform.CCW0)
        return t;
      if (t == Transform.CCW0)
        return this;
      if (t == Transform.CCW1) {
        switch (this) {
        case CCW1: return Transform.CCW2;
        case CCW2: return Transform.CCW3;
        case CCW3: return Transform.CCW0;
        case CCW0_HORIZ_FLIP: return Transform.CCW3_HORIZ_FLIP;
        case CCW1_HORIZ_FLIP: return Transform.CCW0_HORIZ_FLIP;
        case CCW2_HORIZ_FLIP: return Transform.CCW1_HORIZ_FLIP;
        case CCW3_HORIZ_FLIP: return Transform.CCW2_HORIZ_FLIP;
        default:
        }
      }
      if (t == Transform.CCW0_HORIZ_FLIP) {
        switch (this) {
        case CCW1: return Transform.CCW1_HORIZ_FLIP;
        case CCW2: return Transform.CCW2_HORIZ_FLIP;
        case CCW3: return Transform.CCW3_HORIZ_FLIP;
        case CCW0_HORIZ_FLIP: return Transform.CCW0;
        case CCW1_HORIZ_FLIP: return Transform.CCW1;
        case CCW2_HORIZ_FLIP: return Transform.CCW2;
        case CCW3_HORIZ_FLIP: return Transform.CCW3;
        default:
        }
      }
      if (t == Transform.CCW2)
        return concat(Transform.CCW1).concat(Transform.CCW1);
      if (t == Transform.CCW3)
        return concat(Transform.CCW2).concat(Transform.CCW1);
      if (t == Transform.CCW1_HORIZ_FLIP)
        return concat(Transform.CCW1).concat(Transform.CCW0_HORIZ_FLIP);
      if (t == Transform.CCW2_HORIZ_FLIP)
        return concat(Transform.CCW2).concat(Transform.CCW0_HORIZ_FLIP);
      if (t == Transform.CCW3_HORIZ_FLIP)
        return concat(Transform.CCW3).concat(Transform.CCW0_HORIZ_FLIP);
      throw new RuntimeException("unexpected case");
    }

    Pair translate(Pair p, int size) {
      switch(this) {
      case CCW0:
        return new Pair(p.x, p.y);
      case CCW0_HORIZ_FLIP:
        return new Pair(size - 1 - p.x, p.y);
      case CCW1:
        return new Pair(p.y, size - 1 - p.x);
      case CCW1_HORIZ_FLIP:
        return new Pair(size - 1 - p.y, size - 1 - p.x);
      case CCW2:
        return new Pair(size - 1 - p.x, size - 1 - p.y);
      case CCW2_HORIZ_FLIP:
        return new Pair(p.x, size - 1 - p.y);
      case CCW3:
        return new Pair(size - 1 - p.y, p.x);
      case CCW3_HORIZ_FLIP:
        return new Pair(p.y, p.x);
      default:
        throw new RuntimeException("unexpected case");
      }
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
            s.append(tile.id + " (");
            switch (tile.onBoardWithTransform) {
            case CCW0:
              s.append("0-");
              break;
            case CCW0_HORIZ_FLIP:
              s.append("0H");
              break;
            case CCW1:
              s.append("1-");
              break;
            case CCW1_HORIZ_FLIP:
              s.append("1H");
              break;
            case CCW2:
              s.append("2-");
              break;
            case CCW2_HORIZ_FLIP:
              s.append("2H");
              break;
            case CCW3:
              s.append("3-");
              break;
            case CCW3_HORIZ_FLIP:
              s.append("3H");
              break;
            default: throw new RuntimeException("unexpected case");
            }
            s.append(")");
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
    if (directions.contains(Direction.EAST) &&
        directions.contains(Direction.SOUTH))
      return Transform.CCW0;
    switch (directions.last()) {
    case NORTH:
      return Transform.CCW3;
    case WEST:
      return Transform.CCW2;
    default:
      return Transform.CCW1;
    }
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
    System.out.println(id2tile.get(1171));
    System.out.println(image);
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run("data.txt");
  }
}
