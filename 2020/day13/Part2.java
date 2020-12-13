import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Part2
{
  /**
   * A service cycle represents a busses' driving cycle time length
   * and departure time.  This can be a single bus's cycle, or the
   * cycle derived from a combination of multiple bus's cycles.
   *
   * More formally, a service cycle represents the set of any z ∈ ℤ
   * that fulfills the equation z ≡ a (mod n), where "a" denotes the
   * departure and "n" the length of the cycle.
   */
  private class ServiceCycle {
    public final long departure; // "a"
    public final long length; // "n"

    private ServiceCycle(final long departure, final long length)
    {
      this.departure = departure;
      this.length = length;
    }

    /**
     * Brute force intersection determination of service cycles.  Runs
     * still fast enough (few milliseconds on a typical notebook) for
     * input data like that provided in this competition.
     *
     * This is the intersection algorithm that I actually used for the
     * submission of my result.  For an enhanced alternative
     * implementation, see below method chineseIntersect().
     */
    private ServiceCycle bruteForceIntersect(final long otherDeparture,
                                             final long otherLength)
    {
      if (otherLength == 0) { return this; }
      long t;
      for (t = departure; (t - otherDeparture) % otherLength != 0; t += length);
      final long newDeparture = t;
      t += length;
      for (; (t - otherDeparture) % otherLength != 0; t += length);
      final long newLength = t - newDeparture;
      return new ServiceCycle(newDeparture, newLength);
    }

    /**
     * Greatest common divisor.
     *
     * Used by the chineseIntersection() method only.
     */
    private long gcd(long a, long b)
    {
      while (b > 0) {
        final long c = b;
        b = a % b;
        a = c;
      }
      return a;
    }

    /**
     * Given a value a, compute a return value b (the "inverse"), such
     * that (a * b) mod m = 0.
     *
     * Used by the chineseIntersection() method only.  Inspired by:
     * https://rosettacode.org/wiki/Chinese_remainder_theorem
     */
    private long modInverse(long a, long m)
    {
      if (m == 1) return 0;
      long m0 = m;
      long b0 = 0, b = 1;
      while (a > 1) {
        final long q = a / m;
        final long amb = a % m;
        a = m;
        m = amb;
        final long xqx = b - q * b0;
        b = b0;
        b0 = xqx;
      }
      if (b < 0) b += m0;
      return b;
    }

    /**
     * Intersection determination of service cycles inspired by ideas
     * from the Chinese remainder theorem.
     *
     * This is a much more cleaner and (for big data) faster solution
     * compared to the above brute force algorithm.  I implemented
     * this interection algorithm only after I had submitted the
     * correct result with help of the above brute force algorithm.
     *
     * FIXME: In some corner cases (typically for very large values),
     * this method still computes bad values.  Probably, the reason is
     * an arithmetic overflow, which probably can be fixed by using
     * big integers.  Until then, this class internally still uses the
     * above brute force method, which still works quite fast.
     */
    private ServiceCycle chineseIntersect(final long otherDeparture,
                                          final long otherLength)
    {
      if (otherLength == 0) { return this; }
      final long coPrimeOtherLength = otherLength / gcd(length, otherLength);
      if (coPrimeOtherLength != otherLength) {
        System.out.println("WARN ******** GCD != 1 !!!");
      }
      final long newLength = length * coPrimeOtherLength;
      long newDeparture =
        (((departure - otherDeparture) *
          modInverse(otherLength, length)) % length) * otherLength +
        otherDeparture;
      if (newDeparture < 0) newDeparture += newLength;
      return new ServiceCycle(newDeparture, newLength);
    }

    public String toString()
    {
      return "BusCycle(departure=" + departure + ", length=" + length + ")";
    }
  }

  private Part2() {}

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
    ServiceCycle cycle = new ServiceCycle(0, services[0]);
    for (int i = 1; i < services.length; i++) {
      if (services[i] == 0) continue;
      /*
       * Pass negative departure to intersection algorithm, since we
       * do not want to compute how many steps to wait in the future
       * until a conjunction will happen, but, on the opposite,
       * assuming a conjunction at time step t=0, how long it lasted,
       * until at t=0 we got a conjunction.
       */
      cycle = cycle.bruteForceIntersect(-i, services[i]);

      // FIXME: Even faster, but does not yet fully work:
      //
      // cycle = cycle.chineseIntersect(-i, services[i]);
    }
    System.out.println(cycle.departure);
  }

  public static void main(final String argv[]) throws IOException
  {
    new Part2().run("data.txt");
  }
}
