import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Part1
{
  private class Rule
  {
    private final String id;
    private final HashMap<String, Integer> contains;
    private final HashSet<Rule> containedBy;

    private Rule() {
      throw new UnsupportedOperationException("empty constructor");
    }

    public Rule(final String unparsed)
    {
      contains = new HashMap<String, Integer>();
      containedBy = new HashSet<Rule>();
      final String[] tokens = unparsed.split(" bags contain ");
      id = tokens[0].trim();
      parseContents(tokens[1].trim());
    }

    public String getId() { return id; }

    private void addContent(final String id, final int count)
    {
      contains.put(id, count);
    }

    private void addContainedBy(final Rule rule)
    {
      containedBy.add(rule);
    }

    private void parseContent(final String content)
    {
      if (("no other bags.").equals(content)) {
        return;
      }
      final String[] tokens =
        content.split("bag[s]?\\.?$")[0].trim().split(" ", 2);
      final String count = tokens[0].trim();
      final String id = tokens[1].trim();
      addContent(id, Integer.parseInt(count));
    }

    private void parseContents(final String contents)
    {
      for (final String content : contents.split(",")) {
        parseContent(content);
      }
    }

    public void resolveOther()
    {
      for (final String id : contains.keySet()) {
        final Rule other = rules.get(id);
        if (other == null) {
          throw new RuntimeException("unknown bags type: " + id);
        }
        other.addContainedBy(this);
      }
    }

    public void getContainedByClosure(final HashSet<Rule> closure)
    {
      for (final Rule rule : containedBy) {
        if (!closure.contains(rule)) {
          closure.add(rule);
          rule.getContainedByClosure(closure);
        }
      }
    }

    public String toString()
    {
      final StringBuffer s = new StringBuffer();
      if (contains.size() == 0) {
        s.append("no other bags");
      } else {
        for (final String id : contains.keySet()) {
          if (s.length() > 0) { s.append(", "); }
          final int count = contains.get(id);
          final Rule rule = rules.get(id);
          s.append(count + " " + id + " bag" + (count != 1 ? "s" : ""));
        }
      }
      return id + " bags contain " + s + ".";
    }
  }

  private final HashMap<String, Rule> rules;

  private Part1() {
    rules = new HashMap<String, Rule>();
  }

  private void parseRule(final String value)
  {
    final Rule rule = new Rule(value);
    final String id = rule.getId();
    rules.put(id, rule);
  }

  private void resolveContainedBy()
  {
    for (final String id : rules.keySet()) {
      final Rule rule = rules.get(id);
      rule.resolveOther();
    }
  }

  public HashSet<Rule> getContainedByClosure(final String id)
  {
    final Rule rule = rules.get(id);
    final HashSet<Rule> closure = new HashSet<Rule>();
    rule.getContainedByClosure(closure);
    return closure;
  }

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new ArrayList<String>();
    String line;
    while ((line = reader.readLine()) != null) {
      values.add(line);
    }
    for (final var value : values) {
      parseRule(value);
    }
    resolveContainedBy();
    System.out.println(getContainedByClosure("shiny gold").size());
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run("data.txt");
  }
}
