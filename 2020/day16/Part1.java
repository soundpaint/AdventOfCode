import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class Part1
{
  private Part1() {}

  private ArrayList<Rule> rules;
  private ArrayList<Ticket> tickets;
  private ArrayList<Rule> sortedRules;

  private class Rule
  {
    String id;
    long range1Min;
    long range1Max;
    long range2Min;
    long range2Max;
    boolean selected;
    boolean applicableForColumn[];

    Rule(String id, long range1Min, long range1Max, long range2Min, long range2Max)
    {
      this.id = id;
      this.range1Min = range1Min;
      this.range1Max = range1Max;
      this.range2Min = range2Min;
      this.range2Max = range2Max;
      selected = false;
    }
    boolean check(long value)
    {
      return
        ((value >= range1Min) && (value <= range1Max)) ||
        ((value >= range2Min) && (value <= range2Max));
    }
    String getDescription() {
      return range1Min + "-" + range1Max + " or " + range2Min + "-" + range2Max;
    }
  }

  private class Ticket
  {
    long[] values;
    Ticket(String[] values)
    {
      this.values = new long[values.length];
      for (int i = 0; i < values.length; i++) {
        this.values[i] = Long.parseLong(values[i]);
      }
    }

    private boolean ruleApplicableForColumn(Rule rule, int column)
    {
      boolean applicable = rule.check(values[column]);
      /*
      System.out.println("checking " + values[column] + " against " +
                         rule.id + ": " + applicable);
      */
      /*
      if (!applicable) {
        System.out.println("rule " + rule.id +
                           " not applicable for column " + column + ": " +
                           values[column] + " not in " + rule.getDescription());
      }
      */
      return applicable;
    }

    private void instantiate()
    {
      long result = 1;
      for (int i = 0; i < values.length; i++) {
        String fieldName = sortedRules.get(i).id;
        long fieldValue = values[i];
        System.out.println(fieldName + "=" + fieldValue);
        if (fieldName.startsWith("departure")) {
          result *= fieldValue;
        }
      }
      System.out.println("result=" + result);
    }
  }

  private Rule check(long value)
  {
    if (value < 0) throw new RuntimeException();
    for (Rule rule : rules) {
      if (rule.check(value)) return rule;
    }
    return null;
  }

  private long check(String[] values)
  {
    long sum = 0;
    for (String value : values) {
      long longValue = Long.parseLong(value);
      Rule rule = check(longValue);
      if (rule == null) {
        sum += longValue;
      }
    }
    return sum;
  }

  private boolean ruleApplicableForAllTickets(Rule rule, int column)
  {
    if (rule.applicableForColumn != null) {
      return rule.applicableForColumn[column];
    }
    for (Ticket ticket : tickets) {
      if (!ticket.ruleApplicableForColumn(rule, column)) {
        return false;
      }
    }
    return true;
  }

  private String unselectedRulesAsString(ArrayList<Rule> rules)
  {
    StringBuffer s = new StringBuffer();
    for (int i = 0; i < rules.size(); i++) {
      if (rules.get(i).selected) continue;
      if (s.length() > 0) s.append(", ");
      s.append(rules.get(i).id);
    }
    return s.toString();
  }

  private String rulesAsString(ArrayList<Rule> rules)
  {
    StringBuffer s = new StringBuffer();
    for (int i = 0; i < rules.size(); i++) {
      if (s.length() > 0) s.append(", ");
      s.append(rules.get(i).id);
    }
    return s.toString();
  }

  private boolean determineRules(int startColumn)
  {
    System.out.println("sorted rules(" + startColumn + "): " +
                       rulesAsString(sortedRules));
    /*
    System.out.println("remaining rules(" + startColumn + "): " +
                       unselectedRulesAsString(rules));
    */
    if (startColumn >= rules.size()) return true;
    for (Rule rule : rules) {
      if (rule.selected) {
        System.out.println("skipping " + rule.id);
        continue;
      } else {
        System.out.println("checking " + rule.id);
      }
      //System.out.println("checking rule " + rule.id);
      if (ruleApplicableForAllTickets(rule, startColumn)) {
        //System.out.println("found rule: " + rule.id);
        sortedRules.add(rule);
        rule.selected = true;
        if (determineRules(startColumn + 1))
          return true;
        rule.selected = false;
        sortedRules.remove(sortedRules.size() - 1);
      }
    }
    System.out.println("sorted rules(" + startColumn + "): " +
                       "no solution found");
    return false;
  }

  private void run1(final String filePath) throws IOException
  {
    rules = new ArrayList<Rule>();
    final var reader = new BufferedReader(new FileReader(filePath));
    String line;
    // rules
    while ((line = reader.readLine()) != null) {
      if (line.isEmpty()) break;
      String[] tokens = line.split(":");
      String id = tokens[0].trim();
      System.out.println("rule id=" + id);
      String ranges = tokens[1].trim();
      tokens = ranges.split(" or ");
      String range1 = tokens[0].trim();
      String range2 = tokens[1].trim();
      tokens = range1.split("-");
      long range1Min = Long.parseLong(tokens[0].trim());
      long range1Max = Long.parseLong(tokens[1].trim());
      tokens = range2.split("-");
      long range2Min = Long.parseLong(tokens[0].trim());
      long range2Max = Long.parseLong(tokens[1].trim());
      Rule rule = new Rule(id, range1Min, range1Max, range2Min, range2Max);
      rules.add(rule);
    }
    System.out.println("unsorted rules: " + rulesAsString(rules));

    String[] values;

    // your ticket
    line = reader.readLine();
    Ticket yourTicket = new Ticket(reader.readLine().split(","));
    line = reader.readLine();

    // nearby tickets
    tickets = new ArrayList<Ticket>();
    line = reader.readLine();
    long sum = 0;
    while ((line = reader.readLine()) != null) {
      values = line.split(",");
      long singleSum = check(values);
      sum += singleSum;
      if (singleSum == 0) {
        tickets.add(new Ticket(values));
      }
    }
    System.out.println(sum);

    for (Rule rule : rules) {
      boolean[] applicableForColumn = new boolean[rules.size()];
      for (int column = 0; column < rules.size(); column++) {
        boolean applicable = ruleApplicableForAllTickets(rule, column);
        applicableForColumn[column] = applicable;
        if (applicable) {
          System.out.println("rule " + rule.id +
                             " applicable for column " + column + ": " +
                             applicable);
        }
      }
      rule.applicableForColumn = applicableForColumn;
    }
    // System.exit(0);

    System.out.println("found " + tickets.size() + " valid tickets");
    sortedRules = new ArrayList<Rule>();
    if (determineRules(0)) {
      System.out.println("sorted rules:");
      for (Rule rule : sortedRules) {
        System.out.println(rule.id);
      }
      System.out.println();
      yourTicket.instantiate();
    } else {
      System.out.println("no solution found");
    }
    // wrong answer: 761724660253
    // wrong answer: 939191599769
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
