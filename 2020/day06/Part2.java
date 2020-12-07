import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Part2
{
  private Part2() {}

  private long countAllYesAnswersForGroup(final ArrayList<String> group)
  {
    final int answers[] = new int[26];
    group.forEach(person ->
                  person.chars().forEach(answer -> answers[answer - 'a']++));
    return
      Arrays.stream(answers).boxed().mapToInt(Integer::intValue).
      filter(yesCount -> yesCount == group.size()).count();
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
        sum += countAllYesAnswersForGroup(groupYesAnswers);
        groupYesAnswers.clear();
      } else {
        groupYesAnswers.add(personYesAnswers);
      }
    }
    sum += countAllYesAnswersForGroup(groupYesAnswers);
    System.out.println(sum);
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part2().run("data.txt");
  }
}
