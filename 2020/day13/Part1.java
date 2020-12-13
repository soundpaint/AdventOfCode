import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Part1
{
  private Part1() {}

  private void run(final String filePath) throws IOException
  {
    final var reader = new BufferedReader(new FileReader(filePath));
    final var values = new ArrayList<String>();
    final long startTime = Long.parseLong(reader.readLine());
    final String[] strServices = reader.readLine().split(",");
    final long[] services = new long[strServices.length];
    for (int i = 0; i < strServices.length; i++) {
      final String strService = strServices[i];
      if (!"x".equals(strService)) {
        services[i] = Long.parseLong(strServices[i]);
      } else {
        services[i] = 0;
      }
    }
    long bestMatchTime = startTime - 1;
    long bestBusID = 0;
    for (int busIndex = 0; busIndex < services.length; busIndex++) {
      final long busID = services[busIndex];
      if (busID == 0) {
        continue;
      }
      final long nextMatchTime = ((startTime / busID) + 1) * busID;
      if ((bestMatchTime < startTime) || (nextMatchTime < bestMatchTime)) {
        bestMatchTime = nextMatchTime;
        bestBusID = busID;
      }
    }
    System.out.println(bestBusID * (bestMatchTime - startTime));
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part1().run("data.txt");
  }
}
