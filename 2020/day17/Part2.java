import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class Part2
{
  private Part2() {}

  private char[][][][] createCube(final int d)
  {
    final char[][][][] cube = new char[d][][][];
    for (int x = 0; x < d; x++) {
      cube[x] = new char[d][][];
      for (int y = 0; y < d; y++) {
        cube[x][y] = new char[d][];
        for (int z = 0; z < d; z++) {
          cube[x][y][z] = new char[d];
          for (int w = 0; w < d; w++) {
            cube[x][y][z][w] = '.';
          }
        }
      }
    }
    return cube;
  }

  private void printCube(final char[][][][] cube)
  {
    for (int w = 0; w < cube.length; w++) {
      for (int z = 0; z < cube.length; z++) {
        System.out.println();
        System.out.println("z=" + z + ", w=" + w);
        for (int y = 0; y < cube.length; y++) {
          for (int x = 0; x < cube.length; x++) {
            System.out.print(cube[x][y][z][w]);
          }
          System.out.println();
        }
      }
    }
  }

  private void countActive(final char[][][][] cube)
  {
    int count = 0;
    for (int x = 0; x < cube.length; x++) {
      for (int y = 0; y < cube.length; y++) {
        for (int z = 0; z < cube.length; z++) {
          for (int w = 0; w < cube.length; w++) {
            if (cube[x][y][z][w] == '#') count++;
          }
        }
      }
    }
    System.out.println(count);
  }

  private boolean isActive(final char[][][][] cube,
                           final int x, final int y, final int z, final int w)
  {
    if ((x < 0) || (x >= cube.length)) return false;
    if ((y < 0) || (y >= cube.length)) return false;
    if ((z < 0) || (z >= cube.length)) return false;
    if ((w < 0) || (w >= cube.length)) return false;
    return cube[x][y][z][w] == '#';
  }

  private int countActiveNeighbours(final char[][][][] cube,
                                    final int x0, final int y0,
                                    final int z0, final int w0)
  {
    int count = 0;
    for (int x = x0 - 1; x <= x0 + 1; x++) {
      for (int y = y0 - 1; y <= y0 + 1; y++) {
        for (int z = z0 - 1; z <= z0 + 1; z++) {
          for (int w = w0 - 1; w <= w0 + 1; w++) {
            if ((x == x0) && (y == y0) && (z == z0) && (w == w0)) continue;
            if (isActive(cube, x, y, z, w)) {
              count++;
            }
          }
        }
      }
    }
    return count;
  }

  private void update(final char[][][][] cubeSource,
                      final char[][][][] cubeDest)
  {
    for (int x = 0; x < cubeSource.length; x++) {
      for (int y = 0; y < cubeSource.length; y++) {
        for (int z = 0; z < cubeSource.length; z++) {
          for (int w = 0; w < cubeSource.length; w++) {
            final int activeNeighbours =
              countActiveNeighbours(cubeSource, x, y, z, w);
            final boolean active = cubeSource[x][y][z][w] == '#';
            if (active &&
                ((activeNeighbours == 2) || (activeNeighbours == 3))) {
              cubeDest[x][y][z][w] = '#';
            } else if (!active && (activeNeighbours == 3)) {
              cubeDest[x][y][z][w] = '#';
            } else {
              cubeDest[x][y][z][w] = '.';
            }
          }
        }
      }
    }
  }

  private char[][][][] loadInitialConfiguration(final String filePath,
                                                final int padding)
    throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new ArrayList<String>();
    String line;
    while ((line = reader.readLine()) != null) {
      values.add(line);
    }
    final int d = values.size();
    final char[][][][] cube = createCube(d + 2 * padding);
    int y = 0;
    for (final var value : values) {
      for (int x = 0; x < value.length(); x++) {
        cube[padding + x][padding + y][padding][padding]  = value.charAt(x);
      }
      y++;
    }
    return cube;
  }

  private void run(final String filePath) throws IOException
  {
    final int cycles = 6;
    char[][][][] cube1 = loadInitialConfiguration(filePath, cycles);
    final int d = cube1.length;
    char[][][][] cube2 = createCube(cube1.length);
    for (int cycle = 0; cycle < cycles; cycle++) {
      update(cube1, cube2);
      final char[][][][] swap = cube1;
      cube1 = cube2;
      cube2 = swap;
    }
    countActive(cube1);
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part2().run("data.txt");
  }
}
