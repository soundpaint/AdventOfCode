import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Part1
{
  private final ArrayList<Rule> rules;
  private final ArrayList<Ticket> tickets;
  private final ArrayList<Rule> sortedRules;

  private Part1() {
    rules = new ArrayList<Rule>();
    tickets = new ArrayList<Ticket>();
    sortedRules = new ArrayList<Rule>();
  }

  private static class Rule
  {
    private final String id;
    private final long range1Min;
    private final long range1Max;
    private final long range2Min;
    private final long range2Max;
    private boolean applicableForColumn[];
    private int applicableForColumns;
    private boolean selected;

    public Rule(final String id,
                final long range1Min, final long range1Max,
                final long range2Min, final long range2Max)
    {
      this.id = id;
      this.range1Min = range1Min;
      this.range1Max = range1Max;
      this.range2Min = range2Min;
      this.range2Max = range2Max;
      selected = false;
    }

    public final boolean satisfiesRangeConstraints(final long value)
    {
      return
        ((value >= range1Min) && (value <= range1Max)) ||
        ((value >= range2Min) && (value <= range2Max));
    }

    public static Rule parse(final String unparsed)
    {
      final String[] ruleTokens = unparsed.split(":");
      final String id = ruleTokens[0].trim();
      final String[] rangesTokens = ruleTokens[1].trim().split(" or ");
      final String[] range1Tokens = rangesTokens[0].trim().split("-");
      final long range1Min = Long.parseLong(range1Tokens[0].trim());
      final long range1Max = Long.parseLong(range1Tokens[1].trim());
      final String[] range2Tokens = rangesTokens[1].trim().split("-");
      final long range2Min = Long.parseLong(range2Tokens[0].trim());
      final long range2Max = Long.parseLong(range2Tokens[1].trim());
      return new Rule(id, range1Min, range1Max, range2Min, range2Max);
    }

    public String toString() {
      return id + ": " +
        range1Min + "-" + range1Max + " or " + range2Min + "-" + range2Max;
    }
  }

  private class Ticket
  {
    private final long[] values;

    public Ticket(final String[] values)
    {
      this.values = new long[values.length];
      for (int i = 0; i < values.length; i++) {
        this.values[i] = Long.parseLong(values[i]);
      }
    }

    private boolean ruleApplicableForColumn(final Rule rule, final int column)
    {
      return rule.satisfiesRangeConstraints(values[column]);
    }

    private long instantiate()
    {
      long product = 1;
      for (int i = 0; i < values.length; i++) {
        String fieldName = sortedRules.get(i).id;
        long fieldValue = values[i];
        if (fieldName.startsWith("departure")) {
          product *= fieldValue;
        }
      }
      return product;
    }
  }

  private boolean isValidForSomeRule(final long value)
  {
    if (value < 0) throw new RuntimeException();
    for (final Rule rule : rules) {
      if (rule.satisfiesRangeConstraints(value)) return true;
    }
    return false;
  }

  private long getTicketScanningErrorRate(final String[] values)
  {
    long sum = 0;
    boolean isValid = true;
    for (final String value : values) {
      final long longValue = Long.parseLong(value);
      if (!isValidForSomeRule(longValue)) {
        sum += longValue;
        isValid = false;
      }
    }
    return isValid ? -1 : sum;
  }

  private boolean ruleApplicableForAllTicketsForColumn(final Rule rule,
                                                       final int column)
  {
    if (rule.applicableForColumn != null) {
      return rule.applicableForColumn[column];
    }
    for (final Ticket ticket : tickets) {
      if (!ticket.ruleApplicableForColumn(rule, column)) {
        return false;
      }
    }
    return true;
  }

  private boolean determineRules(final int startColumn)
  {
    if (startColumn >= rules.size()) return true;
    for (final Rule rule : rules) {
      if (rule.selected) continue;
      if (ruleApplicableForAllTicketsForColumn(rule, startColumn)) {
        sortedRules.add(rule);
        rule.selected = true;
        if (determineRules(startColumn + 1))
          return true;
        rule.selected = false;
        sortedRules.remove(sortedRules.size() - 1);
      }
    }
    return false;
  }

  private void parseRules(final BufferedReader reader) throws IOException
  {
    String line;
    while (!(line = reader.readLine()).isEmpty()) {
      rules.add(Rule.parse(line));
    }
  }

  private Ticket parseYourTicket(final BufferedReader reader) throws IOException
  {
    reader.readLine();
    final Ticket ticket = new Ticket(reader.readLine().split(","));
    reader.readLine();
    return ticket;
  }

  private void parseNearbyTickets(final BufferedReader reader)
    throws IOException
  {
    reader.readLine();
    long sum = 0;
    String line;
    while ((line = reader.readLine()) != null) {
      final String[] values = line.split(",");
      final long singleSum = getTicketScanningErrorRate(values);
      if (singleSum == -1) {
        tickets.add(new Ticket(values));
      } else {
        sum += singleSum;
      }
    }
    System.out.println(sum);
  }

  private void computeWhichRulesApplyOnWhatColumns()
  {
    for (final Rule rule : rules) {
      final boolean[] applicableForColumn = new boolean[rules.size()];
      int count = 0;
      for (int column = 0; column < rules.size(); column++) {
        if (applicableForColumn[column] =
            ruleApplicableForAllTicketsForColumn(rule, column)) {
          count++;
        }
      }
      rule.applicableForColumn = applicableForColumn;
      rule.applicableForColumns = count;
    }
  }

  /**
   * Performance optimization: Rearrange order of rules such that in
   * the resulting search tree for a depth first search, the top level
   * nodes have smallest degree, while the bottom level nodes have
   * highest degree.
   *
   * At least for my input data, this approach reduces runtime costs
   * on my machine from roughly 290s to less than roughly 25ms, that is
   * by a factor of more than 10000.
   */
  private void sortRules()
  {
    Collections.sort(rules, (lhs, rhs) -> {
        return
          lhs.applicableForColumns > rhs.applicableForColumns ? 1 :
          (lhs.applicableForColumns < rhs.applicableForColumns ? -1 : 0);
      });
  }

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    parseRules(reader);
    final Ticket yourTicket = parseYourTicket(reader);
    parseNearbyTickets(reader);
    computeWhichRulesApplyOnWhatColumns();
    sortRules();
    if (determineRules(0)) {
      System.out.println(yourTicket.instantiate());
    } else {
      System.out.println("no solution found");
    }
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run("data.txt");
  }
}
