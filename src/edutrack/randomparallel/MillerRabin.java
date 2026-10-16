package edutrack.randomparallel;

import edutrack.core.CustomArrayList;
import edutrack.core.CustomRandom;

/**
 * Miller-Rabin Probabilistic and Deterministic Primality Test.
 *
 * <h3>Foundations</h3>
 * <ul>
 *   <li><b>Monte Carlo Algorithm:</b> Fast, bounded running time with controllable error probability.</li>
 *   <li><b>The Carmichael Number Challenge:</b> Numbers like 561, 1105, 1729 fool Fermat's Little Theorem
 *       (a^(n-1) = 1 mod n for all gcd(a,n)=1) while being composite. Miller-Rabin defeats Carmichael numbers
 *       by checking for non-trivial square roots of 1 modulo n.</li>
 *   <li><b>Witness Construction:</b> Write n - 1 = 2^s * d with d odd.
 *       For random base a, compute x = a^d mod n. If x != 1 and x != n-1, square x up to s-1 times.
 *       If x never becomes n-1, base a is a Miller-Rabin witness to n's compositeness.</li>
 *   <li><b>Error Bound:</b> At most 1/4 of bases can be false witnesses. After k iterations,
 *       error probability &lt;= 4^(-k) &lt;= 10^(-6) for k=10.</li>
 *   <li><b>Deterministic 64-Bit Test:</b> Using the first 12 primes as bases guarantees 100% correctness
 *       for all numbers up to 2^64.</li>
 * </ul>
 *
 * <p>Strictly conforms to zero {@code java.util.*} imports.</p>
 */
public class MillerRabin {

    private static final CustomRandom rng = new CustomRandom(1337);

    // Deterministic base set sufficient for all 64-bit unsigned integers
    private static final long[] DETERMINISTIC_BASES = {
            2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37
    };

    public static class PrimalityResult {
        public final long candidate;
        public final boolean isPrime;
        public final String testType;
        public final int roundsTested;
        public final Long witness;
        public final CustomArrayList<String> roundDetails;
        public final long executionNs;

        public PrimalityResult(long candidate, boolean isPrime, String testType, int roundsTested,
                               Long witness, CustomArrayList<String> roundDetails, long executionNs) {
            this.candidate = candidate;
            this.isPrime = isPrime;
            this.testType = testType;
            this.roundsTested = roundsTested;
            this.witness = witness;
            this.roundDetails = roundDetails;
            this.executionNs = executionNs;
        }
    }

    /**
     * Tests primality of n using k independent randomized Miller-Rabin rounds.
     */
    public static PrimalityResult testProbabilistic(long n, int k) {
        long t0 = System.nanoTime();
        CustomArrayList<String> details = new CustomArrayList<>();

        if (n < 2) return new PrimalityResult(n, false, "Probabilistic (k=" + k + ")", 0, null, details, System.nanoTime() - t0);
        if (n == 2 || n == 3) return new PrimalityResult(n, true, "Probabilistic (k=" + k + ")", 0, null, details, System.nanoTime() - t0);
        if ((n & 1) == 0) {
            details.add("Even number > 2: composite");
            return new PrimalityResult(n, false, "Probabilistic (k=" + k + ")", 1, 2L, details, System.nanoTime() - t0);
        }

        // Factor n - 1 = 2^s * d with d odd
        long d = n - 1;
        int s = 0;
        while ((d & 1) == 0) {
            d >>>= 1;
            s++;
        }

        for (int round = 1; round <= k; round++) {
            // Pick random base a in [2, n - 2]
            long a = 2 + (rng.nextLong() >>> 1) % (n - 3);
            long x = modPow(a, d, n);

            if (x == 1 || x == n - 1) {
                if (round <= 5) details.add("Round " + round + ": Base a=" + a + " passed (x = " + x + ")");
                continue;
            }

            boolean composite = true;
            for (int r = 1; r < s; r++) {
                x = mulMod(x, x, n);
                if (x == n - 1) {
                    composite = false;
                    if (round <= 5) details.add("Round " + round + ": Base a=" + a + " reached n-1 at step r=" + r);
                    break;
                }
                if (x == 1) { // Found non-trivial square root of 1 mod n!
                    composite = true;
                    break;
                }
            }

            if (composite) {
                details.add("Round " + round + ": Found witness a=" + a + " proving " + n + " is composite!");
                long elapsed = System.nanoTime() - t0;
                return new PrimalityResult(n, false, "Probabilistic", round, a, details, elapsed);
            }
        }

        long elapsed = System.nanoTime() - t0;
        details.add("Passed all " + k + " rounds. Probability of false positive < " + String.format("%.2e", Math.pow(0.25, k)));
        return new PrimalityResult(n, true, "Probabilistic", k, null, details, elapsed);
    }

    /**
     * Deterministic primality test for 64-bit integers.
     */
    public static PrimalityResult testDeterministic(long n) {
        long t0 = System.nanoTime();
        CustomArrayList<String> details = new CustomArrayList<>();

        if (n < 2) return new PrimalityResult(n, false, "Deterministic", 0, null, details, System.nanoTime() - t0);
        if (n == 2 || n == 3) return new PrimalityResult(n, true, "Deterministic", 0, null, details, System.nanoTime() - t0);
        if ((n & 1) == 0) return new PrimalityResult(n, false, "Deterministic", 1, 2L, details, System.nanoTime() - t0);

        long d = n - 1;
        int s = 0;
        while ((d & 1) == 0) {
            d >>>= 1;
            s++;
        }

        for (long a : DETERMINISTIC_BASES) {
            if (n <= a) break;
            long x = modPow(a, d, n);
            if (x == 1 || x == n - 1) continue;

            boolean composite = true;
            for (int r = 1; r < s; r++) {
                x = mulMod(x, x, n);
                if (x == n - 1) {
                    composite = false;
                    break;
                }
            }

            if (composite) {
                details.add("Deterministic witness: base a=" + a + " proves " + n + " is composite.");
                return new PrimalityResult(n, false, "Deterministic", 1, a, details, System.nanoTime() - t0);
            }
        }

        details.add("Guaranteed Prime: Checked against complete 64-bit deterministic witness basis.");
        return new PrimalityResult(n, true, "Deterministic", DETERMINISTIC_BASES.length, null, details, System.nanoTime() - t0);
    }

    /** Safe modular multiplication (a * b) mod m without 64-bit overflow. */
    public static long mulMod(long a, long b, long m) {
        long res = 0;
        a %= m;
        b %= m;
        while (b > 0) {
            if ((b & 1) != 0) res = (res + a) % m;
            a = (a + a) % m;
            b >>>= 1;
        }
        return res;
    }

    /** Fast modular exponentiation (base^exp) mod m. */
    public static long modPow(long base, long exp, long m) {
        long res = 1;
        base %= m;
        while (exp > 0) {
            if ((exp & 1) != 0) res = mulMod(res, base, m);
            base = mulMod(base, base, m);
            exp >>>= 1;
        }
        return res;
    }
}
