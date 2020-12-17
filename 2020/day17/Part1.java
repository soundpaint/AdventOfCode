import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;

public class Part1
{
  private Part1() {}
  private int cycle;

  char[][][] createCube(int d)
  {
    char[][][] cube = new char[d][][];
    for (int i = 0; i < d; i++) {
      cube[i] = new char[d][];
      for (int j = 0; j < d; j++) {
        cube[i][j] = new char[d];
        for (int k = 0; k < d; k++) {
          cube[i][j][k] = '.';
        }
      }
    }
    return cube;
  }

  void printCube(char[][][] cube)
  {
    for (int z = 0; z < cube.length; z++) {
      System.out.println();
      System.out.println("z=" + z);
      for (int y = 0; y < cube.length; y++) {
        for (int x = 0; x < cube.length; x++) {
          System.out.print(cube[x][y][z]);
        }
        System.out.println();
      }
    }
  }

  void countActive(char[][][] cube)
  {
    int count = 0;
    for (int z = 0; z < cube.length; z++) {
      for (int y = 0; y < cube.length; y++) {
        for (int x = 0; x < cube.length; x++) {
          if (cube[x][y][z] == '#') count++;
        }
      }
    }
    System.out.println(count);
  }

  private boolean isActive(char[][][] cube, int x, int y, int z)
  {
    if ((x < 0) || (x >= cube.length)) return false;
    if ((y < 0) || (y >= cube.length)) return false;
    if ((z < 0) || (z >= cube.length)) return false;
    return cube[x][y][z] == '#';
  }

  private int countActiveNeighbours(char[][][] cube, int x0, int y0, int z0)
  {
    int count = 0;
    for (int x = x0 - 1; x <= x0 + 1; x++) {
      for (int y = y0 - 1; y <= y0 + 1; y++) {
        for (int z = z0 - 1; z <= z0 + 1; z++) {
          if ((x == x0) && (y == y0) && (z == z0)) continue;
          if (isActive(cube, x, y, z)) {
            count++;
          }
        }
      }
    }
    return count;
  }

  private void update(char[][][] cubeSource, char[][][] cubeDest)
  {
    for (int x = 0; x < cubeSource.length; x++) {
      for (int y = 0; y < cubeSource.length; y++) {
        for (int z = 0; z < cubeSource.length; z++) {
          int activeNeighbours = countActiveNeighbours(cubeSource, x, y, z);
          boolean active = cubeSource[x][y][z] == '#';
          if (active && ((activeNeighbours == 2) || (activeNeighbours == 3))) {
            cubeDest[x][y][z] = '#';
          } else if (!active && (activeNeighbours == 3)) {
            cubeDest[x][y][z] = '#';
          } else {
            cubeDest[x][y][z] = '.';
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
    char[][][] cube1 = createCube(d + 2 * cycles);
    int y = 0;
    for (final var value : values) {
      for (int x = 0; x < value.length(); x++) {
        cube1[offs + x][offs + y][offs]  = value.charAt(x);
      }
      y++;
    }
    System.out.println("Before any cycles:");
    printCube(cube1);
    char[][][] cube2 = createCube(d + 2 * cycles);
    for (cycle = 0; cycle < cycles; cycle++) {
      update(cube1, cube2);
      char[][][] swap = cube1;
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
    new Part1().run1("data.txt");
  }
}
