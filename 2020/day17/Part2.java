import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;

public class Part2
{
  private Part2() {}
  private int cycle;

  char[][][][] createCube(int d)
  {
    char[][][][] cube = new char[d][][][];
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

  void printCube(char[][][][] cube)
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

  void countActive(char[][][][] cube)
  {
    int count = 0;
    for (int w = 0; w < cube.length; w++) {
      for (int z = 0; z < cube.length; z++) {
        for (int y = 0; y < cube.length; y++) {
          for (int x = 0; x < cube.length; x++) {
            if (cube[x][y][z][w] == '#') count++;
          }
        }
      }
    }
    System.out.println(count);
  }

  private boolean isActive(char[][][][] cube, int x, int y, int z, int w)
  {
    if ((x < 0) || (x >= cube.length)) return false;
    if ((y < 0) || (y >= cube.length)) return false;
    if ((z < 0) || (z >= cube.length)) return false;
    if ((w < 0) || (w >= cube.length)) return false;
    return cube[x][y][z][w] == '#';
  }

  private int countActiveNeighbours(char[][][][] cube,
                                    int x0, int y0, int z0, int w0)
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

  private void update(char[][][][] cubeSource, char[][][][] cubeDest)
  {
    for (int x = 0; x < cubeSource.length; x++) {
      for (int y = 0; y < cubeSource.length; y++) {
        for (int z = 0; z < cubeSource.length; z++) {
          for (int w = 0; w < cubeSource.length; w++) {
            int activeNeighbours =
              countActiveNeighbours(cubeSource, x, y, z, w);
            boolean active = cubeSource[x][y][z][w] == '#';
            if (active && ((activeNeighbours == 2) || (activeNeighbours == 3))) {
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

  private void run1(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new ArrayList<String>();
    String line;
    while ((line = reader.readLine()) != null) {
      values.add(line);
    }
    int cycles = 6;
    int offs = cycles;
    int d = values.size();
    char[][][][] cube1 = createCube(d + 2 * cycles);
    int y = 0;
    for (final var value : values) {
      for (int x = 0; x < value.length(); x++) {
        cube1[offs + x][offs + y][offs][offs]  = value.charAt(x);
      }
      y++;
    }
    System.out.println("Before any cycles:");
    printCube(cube1);
    char[][][][] cube2 = createCube(d + 2 * cycles);
    for (cycle = 0; cycle < cycles; cycle++) {
      update(cube1, cube2);
      char[][][][] swap = cube1;
      cube1 = cube2;
      cube2 = swap;
      System.out.println("After " + (cycle + 1) + " cycles:");
      printCube(cube1);
    }
    countActive(cube1);
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
    new Part2().run1("data.txt");
  }
}
