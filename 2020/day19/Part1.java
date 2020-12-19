import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

// actually, now works for both parts
public class Part1
{
  private HashMap<Integer, Rule> id2rule;
  private Log log;

  private static class Log
  {
    static final String pattern = ".+*";
    int indent;
    String indentStr = "";

    void indent()
    {
      indent++;
      indentStr = indentStr + pattern.charAt(indent % pattern.length()) + " ";
    }

    void unindent()
    {
      if (indent == 0) return;
      indent--;
      indentStr = indentStr.substring(0, indentStr.length() - 2);
    }

    void log(String message)
    {
      //System.out.println(indentStr + message);
    }
  }

  private static class Production
  {
    private Log log;
    private ArrayList<Integer> ruleRefs;
    private ArrayList<Rule> rules;

    Production(Log log, String unparsed) {
      this.log = log;
      ruleRefs = new ArrayList<Integer>();
      rules = new ArrayList<Rule>();
      String[] tokens = unparsed.split(" ");
      for (String token : tokens) {
        int ruleRef = Integer.parseInt(token.trim());
        ruleRefs.add(ruleRef);
      }
    }

    void resolve(HashMap<Integer, Rule> id2rule)
    {
      for (Integer ruleRef : ruleRefs) {
        rules.add(id2rule.get(ruleRef));
      }
    }

    private TreeSet<Integer> accept(String input, int startPos, int ruleIndex)
    {
      log.indent();
      log.log("production[" + ruleIndex + "] " + " accepts " +
              input + "@" + startPos + "?");
      TreeSet<Integer> result = new TreeSet<Integer>();
      if (ruleIndex < rules.size()) {
        Rule rule = rules.get(ruleIndex);
        TreeSet<Integer> possiblePositions = rule.accept(input, startPos);
        for (Integer pos : possiblePositions) {
          result.addAll(accept(input, pos, ruleIndex + 1));
        }
      } else {
        result = new TreeSet<Integer>();
        result.add(startPos);
      }
      log.log("result=" +
              result.stream().
              map(Object::toString).
              collect(java.util.stream.Collectors.joining(",")) +
              "(" + result.size() + ")");
      log.unindent();
      return result;
    }

    TreeSet<Integer> accept(String input, int startPos)
    {
      log.indent();
      log.log("production " + this + " accepts " +
              input + "@" + startPos + "?");
      TreeSet<Integer> result = accept(input, startPos, 0);
      log.log((result.size() > 0 ? "yes" : "no") +
              ": maxpos=" + (result.size() > 0 ? result.last() : -1));
      log.unindent();
      return result;
    }

    public String toString()
    {
      StringBuffer s = new StringBuffer();
      for (Integer ruleRef : ruleRefs) {
        if (s.length() > 0) s.append(" ");
        s.append(ruleRef);
      }
      return s.toString();
    }
  }

  private static abstract class Rule
  {
    protected Log log;
    protected int id;
    protected Rule(Log log, int id)
    {
      this.log = log;
      this.id = id;
    }

    abstract void resolve(HashMap<Integer, Rule> id2rule);

    // returns updated positions after parse
    abstract TreeSet<Integer> accept(String input, int startPos);

    public static Rule parseRule(Log log, String unparsed)
    {
      String[] tokens = unparsed.split(": ");
      int id = Integer.parseInt(tokens[0].trim());
      String productions = tokens[1].trim();
      if (productions.startsWith("\"")) {
        return TerminalRule.parseRule(log, id, productions);
      } else {
        return ProductionRule.parseRule(log, id, productions);
      }
    }
  }

  private static class ProductionRule extends Rule
  {
    ArrayList<Production> productions;

    private ProductionRule(Log log, int id, ArrayList<Production> productions)
    {
      super(log, id);
      this.productions = productions;
    }

    public void resolve(HashMap<Integer, Rule> id2rule)
    {
      for (Production production : productions) {
        production.resolve(id2rule);
      }
    }

    TreeSet<Integer> accept(String input, int startPos)
    {
      log.indent();
      log.log("rule " + this);
      TreeSet<Integer> result = new TreeSet<Integer>();
      for (Production production : productions) {
        TreeSet<Integer> possiblePositions =
          production.accept(input, startPos);
        result.addAll(possiblePositions);
      }
      log.unindent();
      return result;
    }

    public static Rule parseRule(Log log, int id, String unparsed)
    {
      String[] tokens = unparsed.split(" \\| ");
      ArrayList<Production> productions = new ArrayList<Production>();
      for (String token : tokens) {
        Production production = new Production(log, token.trim());
        productions.add(production);
      }
      return new ProductionRule(log, id, productions);
    }

    public String toString()
    {
      StringBuffer s = new StringBuffer();
      for (Production production : productions) {
        if (s.length() > 0) s.append(" | ");
        s.append(production);
      }
      return id + ": " + s;
    }
  }

  private static class TerminalRule extends Rule
  {
    char terminal;
    private TerminalRule(Log log, int id, char terminal)
    {
      super(log, id);
      this.terminal = terminal;
    }

    public static Rule parseRule(Log log, int id, String terminalProduction)
    {
      if (terminalProduction.length() != 3) throw new RuntimeException();
      return new TerminalRule(log, id, terminalProduction.charAt(1));
    }

    public void resolve(HashMap<Integer, Rule> id2rule)
    {
      // nothing to resolve for terminals
    }

    TreeSet<Integer> accept(String input, int startPos)
    {
      TreeSet<Integer> result = new TreeSet<Integer>();
      if (startPos < input.length() &&
          (input.charAt(startPos) == terminal)) {
        result.add(startPos + 1);
      }
      return result;
    }

    public String toString()
    {
      return id + ": '" + terminal + "'";
    }
  }

  private Part1()
  {
    id2rule = new HashMap<Integer, Rule>();
  }

  private void parseGrammar(ArrayList<String> rules)
  {
    for (final String ruleText : rules) {
      Rule rule = Rule.parseRule(log, ruleText);
      id2rule.put(rule.id, rule);
    }
    for (final Rule rule : id2rule.values()) {
      rule.resolve(id2rule);
    }
    for (final Rule rule : id2rule.values()) {
      System.out.println(rule);
    }
  }

  private void run1(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new ArrayList<String>();
    String line;
    log = new Log();

    // read grammar
    while ((line = reader.readLine()) != null) {
      if (line.isEmpty()) break;
      values.add(line);
    }
    parseGrammar(values);

    int sum = 0;
    Rule startRule = id2rule.get(0);
    // parse messages
    while ((line = reader.readLine()) != null) {
      TreeSet<Integer> result = startRule.accept(line, 0);
      System.out.println(line + " -> " +
                         (result.size() > 0 ? result.last() : "-1") +
                         "(" + line.length() + ")");
      if (result.contains(line.length())) sum++;
    }
    System.out.println(sum);
    // part 2 wrong answer: 246
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
    new Part1().run1("data2.txt");
  }
}
