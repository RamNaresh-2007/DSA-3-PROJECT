package edutrack.randomparallel;

import edutrack.core.CustomArrayList;
import edutrack.core.CustomRandom;

/**
 * Fredman-Komlós-Szemerédi (FKS) Two-Level Perfect Hashing.
 *
 * <h3>Theoretical Foundations</h3>
 * <ul>
 *   <li><b>Guaranteed O(1) Worst-Case Lookup:</b> Unlike standard separate chaining with O(1) expected time,
 *       FKS guarantees O(1) worst-case lookup time with ZERO collisions!</li>
 *   <li><b>Linear O(n) Space:</b>
 *     <ul>
 *       <li>Level 1: Hash n keys into m = n primary slots using universal hashing.
 *           If sum(s_i^2) &gt; 4n, pick a fresh primary hash (expected trials &lt; 2).</li>
 *       <li>Level 2: For each slot with s_i keys, allocate a secondary table of size S_i = s_i^2.
 *           Pick a secondary universal hash until all s_i keys map to distinct slots with zero collisions!
 *           By the birthday paradox, Pr[zero collisions] &gt;= 1/2.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p>Strictly conforms to zero {@code java.util.*} imports.</p>
 */
public class PerfectHashing<V> {

    private static class Entry<V> {
        final String key;
        final V value;
        Entry(String key, V value) { this.key = key; this.value = value; }
    }

    private static class SecondaryTable<V> {
        UniversalHashing hasher;
        Entry<V>[] table;
        int capacity;
    }

    private final int n;
    private UniversalHashing primaryHasher;
    private SecondaryTable<V>[] secondaryTables;
    private final CustomRandom rng = new CustomRandom(999);

    public static class PerfectHashMetrics {
        public final int totalKeys;
        public final int primarySlots;
        public final int totalSecondarySlots;
        public final double spaceOverheadRatio; // totalSecondarySlots / n
        public final int primaryRehashes;
        public final long constructionNs;

        public PerfectHashMetrics(int totalKeys, int primarySlots, int totalSecondarySlots,
                                  int primaryRehashes, long constructionNs) {
            this.totalKeys = totalKeys;
            this.primarySlots = primarySlots;
            this.totalSecondarySlots = totalSecondarySlots;
            this.spaceOverheadRatio = (totalKeys > 0) ? ((double) totalSecondarySlots / totalKeys) : 0;
            this.primaryRehashes = primaryRehashes;
            this.constructionNs = constructionNs;
        }
    }

    @SuppressWarnings("unchecked")
    public static <V> PerfectHashing<V> build(CustomArrayList<String> keys, CustomArrayList<V> values) {
        long t0 = System.nanoTime();
        int n = keys.size();
        if (n == 0) throw new IllegalArgumentException("Keys cannot be empty");

        PerfectHashing<V> ph = new PerfectHashing<>(n);
        int primaryRehashes = 0;

        // Level 1: Find primary hash where sum(s_i^2) <= 4n
        CustomArrayList<Entry<V>>[] primaryBuckets = new CustomArrayList[n];
        while (true) {
            for (int i = 0; i < n; i++) primaryBuckets[i] = new CustomArrayList<>();
            ph.primaryHasher = new UniversalHashing(n, UniversalHashing.DEFAULT_PRIME, ph.rng);

            for (int i = 0; i < n; i++) {
                int slot = ph.primaryHasher.hash(keys.get(i));
                primaryBuckets[slot].add(new Entry<>(keys.get(i), values.get(i)));
            }

            long sumSq = 0;
            for (int i = 0; i < n; i++) {
                long s = primaryBuckets[i].size();
                sumSq += s * s;
            }

            if (sumSq <= 4L * n) break;
            primaryRehashes++;
        }

        // Level 2: For each bucket, build collision-free secondary table of size s_i^2
        ph.secondaryTables = new SecondaryTable[n];
        int totalSecondary = 0;

        for (int i = 0; i < n; i++) {
            CustomArrayList<Entry<V>> b = primaryBuckets[i];
            int s = b.size();
            ph.secondaryTables[i] = new SecondaryTable<>();
            if (s == 0) {
                ph.secondaryTables[i].capacity = 0;
                ph.secondaryTables[i].table = new Entry[0];
                continue;
            }

            int secCap = s * s;
            totalSecondary += secCap;
            ph.secondaryTables[i].capacity = secCap;

            while (true) {
                UniversalHashing secHash = new UniversalHashing(secCap, UniversalHashing.DEFAULT_PRIME, ph.rng);
                Entry<V>[] secTable = new Entry[secCap];
                boolean collision = false;

                for (int j = 0; j < s; j++) {
                    Entry<V> e = b.get(j);
                    int secSlot = secHash.hash(e.key);
                    if (secTable[secSlot] != null) {
                        collision = true;
                        break;
                    }
                    secTable[secSlot] = e;
                }

                if (!collision) {
                    ph.secondaryTables[i].hasher = secHash;
                    ph.secondaryTables[i].table = secTable;
                    break;
                }
            }
        }

        long elapsed = System.nanoTime() - t0;
        ph.metrics = new PerfectHashMetrics(n, n, totalSecondary, primaryRehashes, elapsed);
        return ph;
    }

    private PerfectHashMetrics metrics;

    private PerfectHashing(int n) {
        this.n = n;
    }

    public PerfectHashMetrics getMetrics() {
        return metrics;
    }

    /**
     * Guaranteed O(1) worst-case lookup. Exactly two hash calculations and array lookups!
     */
    public V get(String key) {
        if (key == null || secondaryTables == null) return null;
        int pSlot = primaryHasher.hash(key);
        SecondaryTable<V> sec = secondaryTables[pSlot];
        if (sec == null || sec.capacity == 0) return null;

        int sSlot = sec.hasher.hash(key);
        Entry<V> entry = sec.table[sSlot];
        if (entry != null && entry.key.equals(key)) {
            return entry.value;
        }
        return null;
    }

    public boolean containsKey(String key) {
        return get(key) != null;
    }
}
