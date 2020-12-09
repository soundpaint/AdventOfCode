import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Part2
{
  private class Rule
  {
    private final String id;
    private final HashMap<String, Integer> contains;

    private Rule() {
      throw new UnsupportedOperationException("empty constructor");
    }

    public Rule(final String unparsed)
    {
      contains = new HashMap<String, Integer>();
      final String[] tokens = unparsed.split(" bags contain ");
      id = tokens[0].trim();
      parseContents(tokens[1].trim());
    }

    public String getId() { return id; }

    private void addContent(final String id, final int count)
    {
      contains.put(id, count);
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

    public long countContains()
    {
      long sum = 0;
      for (final String id : contains.keySet()) {
        final Rule other = rules.get(id);
        final int count = contains.get(id);
        final long otherContains = other.countContains();
        sum += count * (otherContains + 1);
      }
      return sum;
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

  private Part2() {
    rules = new HashMap<String, Rule>();
  }

  private void parseRule(final String value)
  {
    final Rule rule = new Rule(value);
    final String id = rule.getId();
    rules.put(id, rule);
  }

  public long getContains(final String id) {
    Rule rule = rules.get(id);
    return rule.countContains();
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
    System.out.println(getContains("shiny gold"));
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part2().run("data.txt");
  }
}
