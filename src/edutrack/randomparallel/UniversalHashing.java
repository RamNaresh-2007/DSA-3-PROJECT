package edutrack.randomparallel;

import edutrack.core.CustomArrayList;
import edutrack.core.CustomRandom;

/**
 * Universal Hashing using Carter-Wegman Hash Families.
 *
 * <h3>Foundations</h3>
 * <ul>
 *   <li><b>Why Hashing is Probabilistic:</b> For any deterministic hash function h: U -&gt; [0..m-1],
 *       if |U| &gt;= n * m, an adversary can identify n keys mapping to the identical slot, degrading
 *       lookup to Omega(n). Randomizing the hash choice at runtime defeats adversarial inputs.</li>
 *   <li><b>Carter-Wegman Universal Family:</b>
 *       H = { h_{a,b}(x) = ((a * x + b) mod p) mod m | a in [1..p-1], b in [0..p-1] } where p is prime &gt; |U|.</li>
 *   <li><b>Universality Theorem:</b> For any two distinct keys x != y,
 *       Pr_{h in H}[ h(x) == h(y) ] &lt;= 1 / m.</li>
 *   <li>Therefore, expected collision count in any bucket is at most 1 + (n - 1)/m = O(1 + alpha).</li>
 * </ul>
 *
 * <p>Strictly conforms to zero {@code java.util.*} imports.</p>
 */
public class UniversalHashing {

    private final long p; // Prime > max key
    private final int m;  // Table slots
    private final long a;
    private final long b;

    public static final long DEFAULT_PRIME = 1_000_000_007L;

    public UniversalHashing(int m) {
        this(m, DEFAULT_PRIME, new CustomRandom());
    }

    public UniversalHashing(int m, long p, CustomRandom rng) {
        if (m <= 0) throw new IllegalArgumentException("m must be > 0");
        this.m = m;
        this.p = p;
        // a in [1, p - 1], b in [0, p - 1]
        this.a = 1 + (rng.nextLong() >>> 1) % (p - 1);
        this.b = (rng.nextLong() >>> 1) % p;
    }

    public int hash(long key) {
        long nonNegativeKey = (key >= 0) ? key : -key;
        long val = (MillerRabin.mulMod(a, nonNegativeKey % p, p) + b) % p;
        return (int) (val % m);
    }

    public int hash(String key) {
        if (key == null) return 0;
        long h = 0;
        for (int i = 0; i < key.length(); i++) {
            h = (h * 31 + key.charAt(i)) % p;
        }
        return hash(h);
    }

    public int getM() { return m; }
    public long getP() { return p; }
    public long getA() { return a; }
    public long getB() { return b; }

    public static class HashDistribution {
        public final int tableSlots;
        public final int totalKeys;
        public final int maxBucketSize;
        public final double averageBucketSize;
        public final int emptyBuckets;
        public final int[] bucketSizes;

        public HashDistribution(int tableSlots, int totalKeys, int[] bucketSizes) {
            this.tableSlots = tableSlots;
            this.totalKeys = totalKeys;
            this.bucketSizes = bucketSizes;
            int max = 0;
            int empty = 0;
            for (int s : bucketSizes) {
                if (s > max) max = s;
                if (s == 0) empty++;
            }
            this.maxBucketSize = max;
            this.emptyBuckets = empty;
            this.averageBucketSize = (double) totalKeys / tableSlots;
        }
    }

    /** Analyzes bucket collision distribution for a set of integer keys. */
    public HashDistribution analyzeDistribution(CustomArrayList<Long> keys) {
        int[] counts = new int[m];
        for (int i = 0; i < keys.size(); i++) {
            counts[hash(keys.get(i))]++;
        }
        return new HashDistribution(m, keys.size(), counts);
    }
}
