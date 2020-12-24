import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class Part1
{
  private final LinkedList<Integer> cups;
  private final LinkedList<Integer> pickedUps;

  private Part1()
  {
    cups = new LinkedList<Integer>();
    pickedUps = new LinkedList<Integer>();
  }

  private void printCups(final int currentCup)
  {
    final StringBuffer s = new StringBuffer("cups:");
    for (int i = 0; i < cups.size(); i++) {
      s.append(" ");
      final int cup = cups.get(i);
      if (cup == currentCup) s.append("(");
      s.append(cup);
      if (cup == currentCup) s.append(")");
    }
    System.out.println(s);
  }

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final String input = reader.readLine();
    input.chars().forEach(ch -> cups.add(ch - '0'));
    final int cupsSize = cups.size();
    int currentCup = cups.get(0);
    for (int move = 1; move <= 100; move++) {
      final int currentCupIndex = cups.indexOf(currentCup);
      System.out.println("-- move " + move + "--");
      printCups(currentCup);
      pickedUps.clear();
      for (int i = 0; i < 3; i++) {
        if ((currentCupIndex + 1) < cups.size())
          pickedUps.add(cups.remove(currentCupIndex + 1));
        else
          pickedUps.add(cups.remove(0));
      }
      final StringBuffer strPickUp = new StringBuffer("pick up:");
      for (int i = 0; i < 3; i++) {
        strPickUp.append(" ");
        final int pickUp = pickedUps.get(i);
        strPickUp.append(pickUp);
      }
      System.out.println(strPickUp);
      int nextCupIndex = -1;
      int destinationLabel = currentCup;
      while (nextCupIndex < 0) {
        destinationLabel--;
        if (destinationLabel <= 0) destinationLabel = cupsSize;
        nextCupIndex = cups.indexOf(destinationLabel);
      }
      System.out.println("destination: " + destinationLabel);
      System.out.println();
      cups.addAll(nextCupIndex + 1, pickedUps);
      currentCup = cups.get((cups.indexOf(currentCup) + 1) % cupsSize);
    }
    System.out.println("-- final --");
    printCups(currentCup);
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run("data.txt");
  }
}
