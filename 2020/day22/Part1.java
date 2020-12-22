import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class Part1
{
  private static final boolean LOG = false;
  private final LinkedList<Integer> player1;
  private final LinkedList<Integer> player2;

  private static void Log()
  {
    if (LOG) System.out.println();
  }

  private static void Log(final String text)
  {
    if (LOG) System.out.println(text);
  }

  private Part1() {
    player1 = new LinkedList<Integer>();
    player2 = new LinkedList<Integer>();
  }

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new ArrayList<String>();
    String line;
    line = reader.readLine();
    line = reader.readLine();
    while (!line.isEmpty()) {
      player1.add(Integer.parseInt(line));
      line = reader.readLine();
    }
    line = reader.readLine();
    line = reader.readLine();
    while ((line != null) && !line.isEmpty()) {
      player2.add(Integer.parseInt(line));
      line = reader.readLine();
    }
    play(player1, player2);
  }

  private void printDeck(final LinkedList<Integer> player, final int id)
  {
    final StringBuffer s = new StringBuffer();
    for (final Integer card : player) {
      if (s.length() > 0) s.append(", ");
      s.append(card);
    }
    Log("Player " + id + "'s deck: " + s);
  }

  private void play(final LinkedList<Integer> player1,
                    final LinkedList<Integer> player2)
  {
    int round = 1;
    int winner = 1;
    while ((player1.size() > 0) && (player2.size() > 0)) {
      Log();
      Log("-- Round " + round + " --");
      printDeck(player1, 1);
      printDeck(player2, 2);
      final int card1 = player1.poll();
      final int card2 = player2.poll();
      Log("Player 1 plays: " + card1);
      Log("Player 2 plays: " + card2);
      winner = card1 > card2 ? 1 : 2;
      if (winner == 1) {
        Log("Player 1 wins the round!");
        player1.add(card1);
        player1.add(card2);
      } else {
        Log("Player 2 wins the round!");
        player2.add(card2);
        player2.add(card1);
      }
      round++;
    }
    Log();
    printPostGameResults(player1, player2);
    printResult(winner == 1 ? player1 : player2);
  }

  private void printPostGameResults(final LinkedList<Integer> player1,
                                    final LinkedList<Integer> player2)
  {
    Log();
    Log("== Post-game results ==");
    printDeck(player1, 1);
    printDeck(player2, 2);
  }

  private void printResult(final LinkedList<Integer> winner) {
    int sum = 0;
    while (winner.size() > 0) {
      sum += winner.size() * winner.poll();
    }
    System.out.println(sum);
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run("data.txt");
  }
}
