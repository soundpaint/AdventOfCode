import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class Part2
{
  private static final boolean DEBUG = false;
  private final Log log;
  private final HashMap<Integer, Rule> id2rule;

  private static class Log
  {
    static final String indentPattern = ".+*";
    int indent;
    String indentStr = "";

    public void indent()
    {
      indent++;
      indentStr = indentStr +
        indentPattern.charAt(indent % indentPattern.length()) + " ";
    }

    public void unindent()
    {
      if (indent == 0)
        throw new RuntimeException("nothing left for unindent");
      indent--;
      indentStr = indentStr.substring(0, indentStr.length() - 2);
    }

    public void log(String message)
    {
      if (DEBUG) System.out.println(indentStr + message);
    }
  }

  private static class Expansion
  {
    private final Log log;
    private final ArrayList<Integer> ruleRefs;
    private final ArrayList<Rule> rules;

    private Expansion() {
      throw new UnsupportedOperationException("unsupported constructor");
    }

    public Expansion(final Log log, final String unparsed) {
      this.log = log;
      ruleRefs = new ArrayList<Integer>();
      rules = new ArrayList<Rule>();
      final String[] tokens = unparsed.split(" ");
      for (final String token : tokens) {
        final int ruleRef = Integer.parseInt(token.trim());
        ruleRefs.add(ruleRef);
      }
    }

    public void resolve(final HashMap<Integer, Rule> id2rule)
    {
      for (final Integer ruleRef : ruleRefs) {
        rules.add(id2rule.get(ruleRef));
      }
    }

    private TreeSet<Integer> accept(final String input, final int startPos,
                                    final int ruleIndex)
    {
      log.indent();
      log.log("expansion[" + ruleIndex + "] " + " accepts " +
              input + "@" + startPos + "?");
      final TreeSet<Integer> result = new TreeSet<Integer>();
      if (ruleIndex < rules.size()) {
        final Rule rule = rules.get(ruleIndex);
        final TreeSet<Integer> possiblePositions = rule.accept(input, startPos);
        for (final Integer pos : possiblePositions) {
          result.addAll(accept(input, pos, ruleIndex + 1));
        }
      } else {
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

    public TreeSet<Integer> accept(final String input, final int startPos)
    {
      log.indent();
      log.log("expansion " + this + " accepts " +
              input + "@" + startPos + "?");
      final TreeSet<Integer> result = accept(input, startPos, 0);
      log.log((result.size() > 0 ? "yes" : "no") +
              ": maxpos=" + (result.size() > 0 ? result.last() : -1));
      log.unindent();
      return result;
    }

    public String toString()
    {
      final StringBuffer s = new StringBuffer();
      for (final Integer ruleRef : ruleRefs) {
        if (s.length() > 0) s.append(" ");
        s.append(ruleRef);
      }
      return s.toString();
    }
  }

  private static abstract class Rule
  {
    protected final Log log;
    protected final int id;

    private Rule() {
      throw new UnsupportedOperationException("unsupported constructor");
    }

    protected Rule(final Log log, final int id)
    {
      this.log = log;
      this.id = id;
    }

    abstract void resolve(final HashMap<Integer, Rule> id2rule);

    /**
     * @return Updated parse positions for any possible derivation.
     */
    abstract TreeSet<Integer> accept(String input, int startPos);

    public static Rule parseRule(final Log log, final String unparsed)
    {
      final String[] tokens = unparsed.split(": ");
      final int id = Integer.parseInt(tokens[0].trim());
      final String expansions = tokens[1].trim();
      if (expansions.startsWith("\"")) {
        return TerminalRule.parseRule(log, id, expansions);
      } else {
        return ExpansionRule.parseRule(log, id, expansions);
      }
    }
  }

  private static class ExpansionRule extends Rule
  {
    private final ArrayList<Expansion> expansions;

    private ExpansionRule(final Log log, final int id,
                          final ArrayList<Expansion> expansions)
    {
      super(log, id);
      this.expansions = expansions;
    }

    public void resolve(final HashMap<Integer, Rule> id2rule)
    {
      for (final Expansion expansion : expansions) {
        expansion.resolve(id2rule);
      }
    }

    public TreeSet<Integer> accept(final String input, final int startPos)
    {
      log.indent();
      log.log("rule " + this);
      final TreeSet<Integer> result = new TreeSet<Integer>();
      for (final Expansion expansion : expansions) {
        final TreeSet<Integer> possiblePositions =
          expansion.accept(input, startPos);
        result.addAll(possiblePositions);
      }
      log.unindent();
      return result;
    }

    public static Rule parseRule(final Log log, final int id,
                                 final String unparsed)
    {
      final String[] tokens = unparsed.split(" \\| ");
      final ArrayList<Expansion> expansions = new ArrayList<Expansion>();
      for (final String token : tokens) {
        final Expansion expansion = new Expansion(log, token.trim());
        expansions.add(expansion);
      }
      return new ExpansionRule(log, id, expansions);
    }

    public String toString()
    {
      final StringBuffer s = new StringBuffer();
      for (final Expansion expansion : expansions) {
        if (s.length() > 0) s.append(" | ");
        s.append(expansion);
      }
      return id + ": " + s;
    }
  }

  private static class TerminalRule extends Rule
  {
    final char terminal;

    private TerminalRule(final Log log, final int id, final char terminal)
    {
      super(log, id);
      this.terminal = terminal;
    }

    public static Rule parseRule(final Log log, final int id,
                                 final String terminalExpansion)
    {
      if (terminalExpansion.charAt(0) != '"')
        throw new RuntimeException("parse error: terminal rule: '\"' expected");
      final char terminalChar = terminalExpansion.charAt(1);
      if ((terminalChar < 'a') || (terminalChar > 'b'))
        throw new RuntimeException("parse error: terminal rule: " +
                                   "unexpected terminal char: " + terminalChar);
      if (terminalExpansion.charAt(2) != '"')
        throw new RuntimeException("parse error: terminal rule: '\"' expected");
      if (terminalExpansion.length() > 3)
        throw new RuntimeException("parse error: terminal rule: " +
                                   "unexpected trailing characters");
      return new TerminalRule(log, id, terminalChar);
    }

    public void resolve(final HashMap<Integer, Rule> id2rule)
    {
      // nothing to resolve for terminals
    }

    public TreeSet<Integer> accept(final String input, final int startPos)
    {
      final TreeSet<Integer> result = new TreeSet<Integer>();
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

  private Part2()
  {
    log = new Log();
    id2rule = new HashMap<Integer, Rule>();
  }

  private void printGrammar()
  {
    for (final Rule rule : id2rule.values()) {
      System.out.println(rule);
    }
  }

  private void parseGrammar(final ArrayList<String> unparsedRules)
  {
    for (final String unparsedRule : unparsedRules) {
      final Rule rule = Rule.parseRule(log, unparsedRule);
      id2rule.put(rule.id, rule);
    }
    for (final Rule rule : id2rule.values()) {
      rule.resolve(id2rule);
    }
  }

  private void loadGrammar(final BufferedReader reader) throws IOException
  {
    String line;
    final var values = new ArrayList<String>();
    while ((line = reader.readLine()) != null) {
      if (line.isEmpty()) break;
      values.add(line);
    }
    parseGrammar(values);
  }

  private void parseMessages(final BufferedReader reader) throws IOException
  {
    String line;
    int sum = 0;
    final Rule startRule = id2rule.get(0);
    while ((line = reader.readLine()) != null) {
      final TreeSet<Integer> result = startRule.accept(line, 0);
      log.log(line + " -> " +
              (result.size() > 0 ? result.last() : "-1") +
              "(" + line.length() + ")");
      if (result.contains(line.length())) sum++;
    }
    System.out.println(sum);
  }

  private void modifiyRulesForPart2()
  {
    final Expansion expansionForRule8 = new Expansion(log, "42 8");
    expansionForRule8.resolve(id2rule);
    final ExpansionRule rule8 = (ExpansionRule)id2rule.get(8);
    rule8.expansions.add(expansionForRule8);

    final Expansion expansionForRule11 = new Expansion(log, "42 11 31");
    expansionForRule11.resolve(id2rule);
    final ExpansionRule rule11 = (ExpansionRule)id2rule.get(11);
    rule11.expansions.add(expansionForRule11);
  }

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    loadGrammar(reader);
    modifiyRulesForPart2();
    // printGrammar();
    parseMessages(reader);
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part2().run("data.txt");
  }
}
