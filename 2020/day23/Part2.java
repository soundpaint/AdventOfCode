import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Part2
{
  private static final int SIZE = 1000000;
  private static final int ROUNDS = 10000000;
  private final HashMap<Integer, Cup> id2cup;
  private Cup cupWithLabel1;

  /**
   * Circular double-linked list of cups.  In contrast to the Java
   * LinkedList implementation, it is circular, and also the Cup
   * object itself contains the links, such that deletion and
   * insertion operation is possible in O(1).  In contrast, the Java
   * implementation first needs to do a scan in O(n) to find the node
   * to delete (since the Java LinkedList cleanly separates node
   * content (any user object) from node structure (the collection
   * object), but resulting in much high performance costs.  With this
   * integrated cup and list implementation, I got a huge speed-up of
   * around 15.000 compared to a separate element class managed by the
   * java.util.LinkedList class.
   */
  private static class Cup
  {
    private final int label;
    private Cup prev;
    private Cup next;
    private Cup lowerLabelCup; // fast link to cup with next-lower label

    private Cup(final int label)
    {
      this.label = label;
    }

    private static Cup createSelfLinkedSeed(final int label)
    {
      final Cup cup = new Cup(label);
      cup.prev = cup;
      cup.next = cup;
      return cup;
    }

    private static Cup createCupAndAppendTo(final int label, final Cup other)
    {
      return
        (other != null) ?
        other.createAndAppendCup(label) :
        createSelfLinkedSeed(label);
    }

    private Cup createAndAppendCup(final int label)
    {
      final Cup cup = new Cup(label);
      insertAfter(cup);
      return cup;
    }

    private void insertAfter(final Cup other)
    {
      if (other.isLinked())
        throw new RuntimeException("cup already linked");
      next.prev = other;
      other.prev = this;
      other.next = next;
      next = other;
    }

    private void remove()
    {
      if (!isLinked())
        throw new RuntimeException("cup not linked");
      prev.next = next;
      next.prev = prev;
      prev = null;
      next = null;
    }

    private boolean isLinked()
    {
      return (prev != null) && (next != null);
    }

    public String toString() { return Integer.toString(label); }
  }

  private Part2()
  {
    id2cup = new HashMap<Integer, Cup>();
  }

  private static int min(final int a, final int b)
  {
    return a < b ? a : b;
  }

  private void printCups(final Cup startCup, final int markLabel)
  {
    final StringBuffer s = new StringBuffer("cups:");
    Cup cup = startCup;
    for (int i = 0; i < min(id2cup.size(), 20); i++) {
      s.append(" ");
      if (cup.label == markLabel) s.append("(");
      s.append(cup);
      if (cup.label == markLabel) s.append(")");
      cup = cup.next;
    }
    if (id2cup.size() > 20) s.append(" ...");
    System.out.println(s);
  }

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final String input = reader.readLine();
    Cup startCup = null;
    Cup parsedCup = null;
    for (final char ch : input.toCharArray()) {
      final int label = ch - '0';
      parsedCup = Cup.createCupAndAppendTo(label, parsedCup);
      id2cup.put(label, parsedCup);
      if (label == 1) cupWithLabel1 = parsedCup;
      if (startCup == null) startCup = parsedCup;
    }
    id2cup.values().stream().forEach(cup -> {
        final int lowerLabel =
          ((cup.label + id2cup.size() - 2) % id2cup.size()) + 1;
        cup.lowerLabelCup =
          id2cup.values().stream()
          .filter(lowerCup -> lowerCup.label == lowerLabel).findAny().get();
      });
    Cup cupWithMaxLabel = id2cup.values().stream()
      .max((cup1, cup2) -> Integer.compare(cup1.label, cup2.label)).get();
    Cup syntheticCup = null;
    for (int label = cupWithMaxLabel.label + 1; label <= SIZE; label++) {
      syntheticCup = parsedCup.createAndAppendCup(label);
      id2cup.put(label, syntheticCup);
      syntheticCup.lowerLabelCup = cupWithMaxLabel;
      cupWithMaxLabel = syntheticCup;
      parsedCup = syntheticCup;
    }
    cupWithLabel1.lowerLabelCup = parsedCup;
    Cup currentCup = startCup;
    int move = 0;
    while (move++ < ROUNDS) {
      if ((move & 0xfffff) == 0)
        System.out.printf("%d%%\r", (move * 100 / ROUNDS));
      final Cup pickUp1 = currentCup.next;
      pickUp1.remove();
      final Cup pickUp2 = currentCup.next;
      pickUp2.remove();
      final Cup pickUp3 = currentCup.next;
      pickUp3.remove();
      Cup destinationCup = currentCup.lowerLabelCup;
      while (!destinationCup.isLinked())
        destinationCup = destinationCup.lowerLabelCup;
      destinationCup.insertAfter(pickUp3);
      destinationCup.insertAfter(pickUp2);
      destinationCup.insertAfter(pickUp1);
      currentCup = currentCup.next;
    }
    System.out.println("-- final --");
    printCups(cupWithLabel1, currentCup.label);
    System.out.println((long)cupWithLabel1.next.label *
                       cupWithLabel1.next.next.label);
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part2().run("data.txt");
  }
}
