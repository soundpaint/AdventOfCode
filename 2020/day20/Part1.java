import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Part1
{
  private Part1()
  {
    id2tile = new HashMap<Integer, Tile>();
  }

  HashMap<Integer, Tile> id2tile;

  private enum Direction
  {
    EAST, NORTH, WEST, SOUTH;

    private Direction turnLeft(int n)
    {
      int modSum = (ordinal() + n) % 4;
      return Direction.values()[modSum < 0 ? modSum + 4 : modSum];
    }

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

    private Direction applyTransform(Transform t)
    {
      Direction d = turnLeft(t.getDirection().ordinal());
      if (t.isUpsideDown() && d.isHorizontalDirection())
        return d.turnOpposite();
      return d;
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

    private static Transform ccw(int n)
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

    private static Transform cw(int n)
    {
      return ccw(-n);
    }

    private Transform turnLeft(int n)
    {
      return concat(ccw(n));
    }

    private Transform turnRight(int n)
    {
      return concat(cw(n));
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

  boolean show;

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

    private boolean[] getBorder(int x0, int y0, int dx, int dy, boolean reverse)
    {
      if (reverse)
        return getBorder(dx != 0 ? (bits.length - 1 - x0) : x0,
                         dy != 0 ? (bits.length - 1 - y0) : y0,
                         -dx, -dy, !reverse);
      boolean[] result = new boolean[bits.length];
      int x = x0;
      int y = y0;
      for (int i = 0; i < bits.length; i++) {
        result[i] = bits[x][y];
        x += dx;
        y += dy;
      }
      return result;
    }

    private boolean[] getBorderCCW(Direction d, boolean reverse)
    {
      int x0;
      int y0;
      int dx;
      int dy;
      switch(d) {
      case EAST:
        x0 = bits.length - 1;
        dx = 0;
        y0 = bits.length - 1;
        dy = -1;
        break;
      case NORTH:
        x0 = bits.length - 1;
        dx = -1;
        y0 = 0;
        dy = 0;
        break;
      case WEST:
        x0 = 0;
        dx = 0;
        y0 = 0;
        dy = 1;
        break;
      case SOUTH:
        x0 = 0;
        dx = 1;
        y0 = bits.length - 1;
        dy = 0;
        break;
      default: throw new RuntimeException("unexpected case");
      }
      return getBorder(x0, y0, dx, dy, reverse);
    }

    /**
     * Elements in CCW order of border in direction d after applying
     * transform t onto this tile.
     */
    private boolean[] getBorder(Direction d, Transform t)
    {
      switch (t) {
      case CCW0:
        return getBorderCCW(d, false);
      case CCW0_HORIZ_FLIP:
        return
          d.isHorizontalDirection() ?
          getBorderCCW(d.turnOpposite(), true) : getBorderCCW(d, true);
      case CCW1:
        return getBorder(d.turnRight(1), Transform.CCW0);
      case CCW1_HORIZ_FLIP:
        return getBorder(d.turnRight(1), Transform.CCW0_HORIZ_FLIP);
      case CCW2:
        return getBorder(d.turnOpposite(), Transform.CCW0);
      case CCW2_HORIZ_FLIP:
        return getBorder(d.turnOpposite(), Transform.CCW0_HORIZ_FLIP);
      case CCW3:
        return getBorder(d.turnLeft(1), Transform.CCW0);
      case CCW3_HORIZ_FLIP:
        return getBorder(d.turnLeft(1), Transform.CCW0_HORIZ_FLIP);
      default: throw new RuntimeException("unexpected case");
      }
    }

    int count = 0;

    void checkLineUpWith_fast(Tile other, Direction d, Transform t)
    {
      int ccwThis = Direction.NORTH.cwDiffTo(d);
      int ccwOther = Direction.NORTH.cwDiffTo(d.turnOpposite());
      Transform tThis = Transform.ccw(ccwThis);
      Transform tOther = t.concat(Transform.ccw(ccwOther));
      for (int x = 0; x < bits.length; x++) {
        boolean bitThis = getBitAfterTransform(tThis, bits.length - 1 - x, 0);
        boolean bitOther = other.getBitAfterTransform(tOther, x, 0);
        if (bitThis != bitOther) return;
      }
      lineUps.add(new LineUpInfo(other, d, t));
    }

    void checkLineUpWith(Tile other, Direction d, Transform t)
    {
      boolean[] thisBorder = getBorder(d, Transform.CCW0);
      boolean[] otherBorder = other.getBorder(d.turnOpposite(), t);
      for (int i = 0; i < bits.length; i++) {
        if (thisBorder[i] != otherBorder[bits.length - 1 - i])
          return;
      }
      lineUps.add(new LineUpInfo(other, d, t));
    }

    void checkLineUpWith(Tile other)
    {
      for (Direction d : Direction.values()) {
        for (Transform t : Transform.values()) {
          checkLineUpWith_fast(other, d, t);
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
    // System.out.print("result part 1 (corner product): ");
    System.out.println(result);
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

  private class DrawCache extends LinkedHashMap<Stack<Draw>, Void>
  {
    private static final int MAX_ENTRIES = 100;

    protected boolean removeEldestEntry(Map.Entry eldest) {
      return size() > MAX_ENTRIES;
    }

    void putSnapshot(Stack<Draw> history)
    {
      put(history, null);
    }

    boolean containsSnapshot(Stack<Draw> history)
    {
      boolean result = containsKey(history);
      if (result) {
        System.out.println("re-entered");
      }
      return result;
    }
  }

  private class Board
  {
    private static final int CACHE_MAX_DRAWS = 70;

    Tile[][] tiles;
    Stack<Draw> history;
    DrawCache drawCache;

    Board(int size)
    {
      tiles = new Tile[size][];
      for (int x = 0; x < size; x++) {
        tiles[x] = new Tile[size];
      }
      history = new Stack<Draw>();
      drawCache = new DrawCache();
    }

    boolean vacant(int x, int y)
    {
      return tiles[x][y] == null;
    }

    int getIdAt(int x, int y)
    {
      return tiles[x][y].id;
    }

    int maxLen = 0;

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
     * @param t2 Transform applied on tile1 when put onto board.
     */
    boolean fits_fast(Tile tile1, Transform t1, Direction d,
                      Tile tile2, Transform t2)
    {
      System.out.println("=== fits_fast: " + tile1.id + " vs. " + tile2.id);
      System.out.println("=== d=" + d);
      for (LineUpInfo lineUp : tile1.lineUps) {
        if (lineUp.other != tile2) continue;
        System.out.println("line-up direction=" + lineUp.direction);
        Direction tile2Direction =
          lineUp.direction.applyTransform(t1);
        System.out.println("tile2Direction=" + tile2Direction);
        if (tile2Direction != d)
          continue;
        if (lineUp.otherTransform.concat(t1) == t2)
          return true;
      }
      System.out.println("=== not fitting: " + tile1.id);
      return false;
    }

    boolean fits_slow(Direction d,
                      Tile tile1, Transform t1,
                      Tile tile2, Transform t2)
    {
      if ((tile1.id == 2473) && (tile2.id == 3079)) {
        System.out.println("+++ id1=" + tile1.id);
        System.out.println("+++ id2=" + tile2.id);
        System.out.println("+++ d=" + d);
        System.out.println("+++ t1=" + t1);
        System.out.println("+++ t2=" + t2);
      }
      boolean[] border1 = tile1.getBorder(d, t1);
      boolean[] border2 = tile2.getBorder(d.turnOpposite(), t2);
      if ((tile1.id == 2473) && (tile2.id == 3079)) {
        System.out.println("+++ b1=" + toBitStr(border1));
        System.out.println("+++ b2=" + toBitStr(border2));
      }
      for (int i = 0; i < border1.length; i++) {
        if (border1[i] != border2[border1.length - 1 - i])
          return false;
      }
      return true;
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
      return fits_fast(tile, t, d,
                       otherTile, otherTile.onBoardWithTransform);
      /*
      return fits_slow(d.turnOpposite(),
                       otherTile, otherTile.onBoardWithTransform, tile, t);
      */
      /*
      System.out.println(tile.id + " fits " + otherTile.id +
                         " for direction " + d);
      */
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
      if (history.size() > maxLen) {
        maxLen = history.size();
        /*
        System.out.println("maxLen=" + maxLen);
        if (maxLen > tiles.length - 20)
          System.out.println(this);
        */
      }
      /*
      if (history.size() <= CACHE_MAX_DRAWS)
        return drawCache.containsSnapshot(history);
      */
      //return drawCache.containsSnapshot(history);
      return false;
    }

    void pop()
    {
      /*
      if (history.size() <= CACHE_MAX_DRAWS)
        drawCache.putSnapshot(history);
      */
      //drawCache.putSnapshot(history);
      Draw draw = history.pop();
      draw.tile.onBoardWithTransform = null;
      tiles[draw.x][draw.y] = null;
      if (maxLen > 44)
        if (history.size() < 44)
          throw new RuntimeException("no solution found");
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
            //s.append(tile.id + " ");
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
            //s.append("---- ");
          }
        }
        s.append("\r\n");
      }
      return s.toString();
    }
  }

  private void printResult(Board board, int minX, int maxX, int minY, int maxY)
  {
    /*
    long result = 1;
    result *= board.getIdAt(minX, minY);
    result *= board.getIdAt(maxX, minY);
    result *= board.getIdAt(maxX, maxY);
    result *= board.getIdAt(minX, maxY);
    System.out.println(result);
    */
    //System.out.println(board);
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
      else if ((x == tilesPerRow - 2) && (dx == 0) && (dy == 1))
        return true;
      else
        return false;
    } else {
      // right-to-left-row
      if ((dx == -1) && (dy == 0))
        return true;
      else if ((x == 1) && (dx == 0) && (dy == 1))
        return true;
      else
        return false;
    }
  }

  private boolean examineDraw(Board board,
                              ArrayList<Tile> sortedTiles, int tilesPerRow,
                              Tile tile, int x, int y, Transform t,
                              int minX, int maxX, int minY, int maxY)
  {
    if (board.history.size() == sortedTiles.size()) {
      // System.out.println(board);
      printResult(board, minX, maxX, minY, maxY);
      return true;
    }
    for (LineUpInfo lineUp : tile.lineUps) {
      if (lineUp.other.onBoardWithTransform != null)
        continue; // already on board
      int dx, dy;
      switch (lineUp.direction) {
      case EAST:
        dx = 1;
        dy = 0;
        break;
      case NORTH:
        dx = 0;
        dy = -1;
        break;
      case WEST:
        dx = -1;
        dy = 0;
        break;
      case SOUTH:
        dx = 0;
        dy = 1;
        break;
      default: throw new RuntimeException("unexpected case");
      }
      int swap;
      switch (t) {
      case CCW0:
        break;
      case CCW0_HORIZ_FLIP:
        dx = -dx;
        break;
      case CCW1:
        swap = dx;
        dx = dy;
        dy = -swap;
        break;
      case CCW1_HORIZ_FLIP:
        swap = -dx;
        dx = dy;
        dy = -swap;
        break;
      case CCW2:
        dx = -dx;
        dy = -dy;
        break;
      case CCW2_HORIZ_FLIP:
        dy = -dy;
        break;
      case CCW3:
        swap = dx;
        dx = -dy;
        dy = swap;
        break;
      case CCW3_HORIZ_FLIP:
        swap = -dx;
        dx = -dy;
        dy = swap;
        break;
      default: throw new RuntimeException("unexpected case");
      }

      // enforce solving the puzzle in circles until reaching the
      // center
      if (!nextPositionOk(tilesPerRow, x, y, dx, dy)) {
        continue;
      }

      Transform concatTransform = t.concat(lineUp.otherTransform);

      int newX = x + dx;
      int newY = y + dy;
      int newMinX = newX < minX ? newX : minX;
      int newMaxX = newX > maxX ? newX : maxX;
      int newMinY = newY < minY ? newY : minY;
      int newMaxY = newY > maxY ? newY : maxY;
      boolean solved =
        rearrangeTiles(board, sortedTiles, tilesPerRow,
                       lineUp.other, newX, newY, concatTransform,
                       newMinX, newMaxX, newMinY, newMaxY);
      if (solved)
        return true;
    }
    return false;
  }

  private boolean rearrangeTiles(Board board,
                                 ArrayList<Tile> sortedTiles, int tilesPerRow,
                                 Tile tile, int x, int y, Transform t,
                                 int minX, int maxX, int minY, int maxY)
  {
    /*
    if (tile.type == TileType.OTHER) {
      return false;
    }
    */
    if (maxX - minX >= tilesPerRow) {
      //System.out.println(tile.id + ": width exceeds square");
      return false;
    }
    if (maxY - minY >= tilesPerRow) {
      //System.out.println(tile.id + ": height exceeds square");
      return false;
    }
    if (!board.vacant(x, y)) {
      //System.out.println(tile.id + ": (" + x + "," + y + ") already occupied");
      return false;
    }
    if (!board.fits(tile, t, x, y)) {
      return false;
    }
    boolean solved = false;
    if (!board.push(tile, t, x, y)) {
      solved =
        examineDraw(board, sortedTiles, tilesPerRow, tile, x, y, t,
                    minX, maxX, minY, maxY);
    }
    if (!solved)
      board.pop();
    return solved;
  }

  private boolean rearrangeTiles(ArrayList<Tile> sortedTiles, int tilesPerRow)
  {
    Board board = new Board(tilesPerRow * 2 - 1);
    int x = tilesPerRow - 1;
    int y = tilesPerRow - 1;
    return
      rearrangeTiles(board, sortedTiles, tilesPerRow,
                     sortedTiles.get(0), x, y, Transform.CCW0,
                     x, x, y, y);
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
    if (board.history.size() == board.tiles.length * 4 - 4) {
      return true;
    }
    for (LineUpInfo lineUp : tile.lineUps) {
      if (lineUp.other.onBoardWithTransform != null)
        continue; // already on board
      /*
      int dx, dy;
      switch (lineUp.direction) {
      case EAST:
        dx = 1;
        dy = 0;
        break;
      case NORTH:
        dx = 0;
        dy = -1;
        break;
      case WEST:
        dx = -1;
        dy = 0;
        break;
      case SOUTH:
        dx = 0;
        dy = 1;
        break;
      default: throw new RuntimeException("unexpected case");
      }
      int swap;
      switch (t) {
      case CCW0:
        break;
      case CCW0_HORIZ_FLIP:
        dx = -dx;
        break;
      case CCW1:
        swap = dx;
        dx = dy;
        dy = -swap;
        break;
      case CCW1_HORIZ_FLIP:
        swap = -dx;
        dx = dy;
        dy = -swap;
        break;
      case CCW2:
        dx = -dx;
        dy = -dy;
        break;
      case CCW2_HORIZ_FLIP:
        dy = -dy;
        break;
      case CCW3:
        swap = dx;
        dx = -dy;
        dy = swap;
        break;
      case CCW3_HORIZ_FLIP:
        swap = -dx;
        dx = -dy;
        dy = swap;
        break;
      default: throw new RuntimeException("unexpected case");
      }
      */
      Direction transformedDirection = lineUp.direction.applyTransform(t);
      int dx = transformedDirection.getDX();
      int dy = transformedDirection.getDY();
      Transform concatTransform = lineUp.otherTransform.concat(t);
      int newX = x + dx;
      int newY = y + dy;
      if (lineUp.other.id == 2473) {
        System.out.println("***** t=" + t);
        System.out.println("***** line-up direction=" + lineUp.direction);
        System.out.println("***** transformed direction=" + transformedDirection);
        System.out.println("***** " + lineUp.other.id);
        System.out.println("***** dx=" + dx);
        System.out.println("***** dy=" + dy);
        System.out.println("***** before tile=" + tile.id);
        System.out.println("***** before t=" + t);
        System.out.println("***** lines up with 3079 by applying " +
                           lineUp.otherTransform);
        System.out.println("***** " + lineUp.other.id + " concat_t=" + concatTransform);
      }
      boolean solved =
        drawEdgeTiles(board, lineUp.other, concatTransform, newX, newY);
      if (solved) {
        return true;
      }
    }
    return false;
  }

  private boolean drawEdgeTiles(Board board,
                                Tile tile, Transform t, int x, int y)
  {
    if (tile.id == 2473) System.out.println("[0] t=" + t);
    if (tile.type == TileType.OTHER) {
      if (tile.id == 2473) System.out.println("[1]");
      return false;
    }
    System.out.println("?tile " + tile.id + ",t=" + t + ",x=" + x + ",y=" + y);
    if ((x == 0) || (x == board.tiles.length - 1)) {
      if ((y < 0) || (y >= board.tiles.length)) {
        if (tile.id == 2473) System.out.println("[2]x=" + x + ",y=" + y);
        return false;
      }
    } else if ((y == 0) || (y == board.tiles.length - 1)) {
      if ((x < 0) || (x >= board.tiles.length)) {
        if (tile.id == 2473) System.out.println("[3]x=" + x + ",y=" + y);
        return false;
      }
    } else {
      if (tile.id == 2473) System.out.println("[4]");
      return false;
    }
    if (!board.vacant(x, y)) {
      if (tile.id == 2473) System.out.println("[5]");
      return false;
    }
    if (!board.fits(tile, t, x, y)) {
      if (tile.id == 2473) {
        System.out.println("[6] t=" + t);
        System.out.println("[6] x=" + x);
        System.out.println("[6] y=" + y);
      }
      return false;
    }
    if (tile.id == 2473) System.out.println("[7]");
    boolean solved = false;
    if (!board.push(tile, t, x, y)) {
      System.out.println(board);
      solved = examineEdgeDraw(board, tile, t, x, y);
    }
    if (!solved)
      board.pop();
    return solved;
  }

  private boolean drawEdgeTiles(Board board,
                                ArrayList<Tile> sortedTiles, int tilesPerRow)
  {
    List<Tile> cornerTiles =
      sortedTiles
      .stream()
      .filter(tile -> tile.type == TileType.CORNER)
      .collect(Collectors.toList());
    List<Tile> edgeTiles =
      sortedTiles
      .stream()
      .filter(tile -> tile.type == TileType.EDGE)
      .collect(Collectors.toList());
    Tile upperLeftCorner = cornerTiles.get(0);
    cornerTiles.remove(0);
    Transform t = getUpperLeftCornerTransform(upperLeftCorner);
    return drawEdgeTiles(board, upperLeftCorner, t, 0, 0);
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
              pixels[tileXBase + x - 1][tileYBase + y - 1] = tile.getTransformedBit(x, y);
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

  private void run1(final String filePath) throws IOException
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
    //System.out.println(tilesPerRow + " tiles per row");
    determineLineUps();
    determineTileTypes();
    //sortLineUps();
    //printLineUps();
    printTileTypeInfo();
    System.exit(0); // STOP HERE FOR PART 1
    ArrayList<Tile> sortedTiles = sortTiles();
    Board board = new Board(tilesPerRow);
    //System.out.println(id2tile.get(2053));
    //System.out.println(id2tile.get(2269));
    /*
    List<Tile> sortedEdgeTiles =
      sortedTiles
      .stream()
      .filter(tile -> tile.type != TileType.OTHER)
      .collect(Collectors.toList());
    */
    //rearrangeTiles(sortedTiles, tilesPerRow);
    System.out.println(drawEdgeTiles(board, sortedTiles, tilesPerRow));
    System.out.println(board);
    if (board.history.size() == 0) {
      System.out.println("no solution found -> stopping");
      return;
    }
    Draw draw = board.history.peek();
    examineDraw(board, sortedTiles, tilesPerRow,
                draw.tile, draw.x, draw.y, draw.tile.onBoardWithTransform,
                0, tilesPerRow - 1, 0, tilesPerRow - 1);
    System.out.println(board);
    Image image = new Image(board);
    System.out.println(id2tile.get(1171));
    System.out.println(image);
  }

  private void run2(final String filePath) throws IOException
  {
    final var path = Paths.get(filePath);
    final var data =
      new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    final String values[] = data.trim().split("[,\\s]");
    for (final var value : values) {
      System.out.println("[" + value + "]");
    }
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run1("data1.txt");
  }
}
