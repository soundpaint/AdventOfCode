import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Part1
{
  private Part1() {}

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final int cardPublicKey = Integer.parseInt(reader.readLine());
    final int doorPublicKey = Integer.parseInt(reader.readLine());
    int subjectNumber = 7;
    long loopSize;
    long value;
    for (loopSize = 0, value = 1; value != cardPublicKey; loopSize++) {
      value = (value * subjectNumber) % 20201227;
    }
    final long cardSecretLoopSize = loopSize;
    for (loopSize = 0, value = 1; value != doorPublicKey; loopSize++) {
      value = (value * subjectNumber) % 20201227;
    }
    final long doorSecretLoopSize = loopSize;
    subjectNumber = doorPublicKey;
    for (loopSize = 0, value = 1; loopSize < cardSecretLoopSize; loopSize++) {
      value = (value * subjectNumber) % 20201227;
    }
    System.out.println(value);
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run("data1.txt");
  }
}
