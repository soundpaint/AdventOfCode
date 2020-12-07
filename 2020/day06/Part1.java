import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Part1
{
  private Part1() {}

  private long countAnyYesAnswersForGroup(final ArrayList<String> group)
  {
    final Boolean yessed[] = new Boolean[26];
    group.forEach(person ->
                  person.chars().forEach(answer ->
                                         yessed[answer - 'a'] = true));
    return Arrays.stream(yessed).filter(yes -> yes == Boolean.TRUE).count();
  }

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var lines = new ArrayList<String>();
    String inputLine;
    while ((inputLine = reader.readLine()) != null) {
      lines.add(inputLine);
    }
    long sum = 0;
    final var groupYesAnswers = new ArrayList<String>();
    for (final String line : lines) {
      final String personYesAnswers = line.trim();
      if (personYesAnswers.isEmpty()) {
        sum += countAnyYesAnswersForGroup(groupYesAnswers);
        groupYesAnswers.clear();
      } else {
        groupYesAnswers.add(personYesAnswers);
      }
    }
    sum += countAnyYesAnswersForGroup(groupYesAnswers);
    System.out.println(sum);
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run("data.txt");
  }
}
