package edutrack.core;

/**
 * High-performance 64-bit pseudo-random number generator (SplitMix64 / XorShift*).
 *
 * <p>Strictly conforms to zero {@code java.util.*} imports. Provides uniform
 * pseudo-random integers, doubles, and bounded ranges for randomised algorithms
 * (Miller-Rabin primality, randomised quicksort, reservoir sampling, universal hashing).</p>
 */
public class CustomRandom {

    private long state;

    public CustomRandom() {
        this(System.nanoTime() ^ 0x5DEECE66DL);
    }

    public CustomRandom(long seed) {
        this.state = (seed == 0) ? 0x853c49e6748fea9bL : seed;
    }

    /** Generates 64 bits of pseudo-randomness using SplitMix64. */
    public long nextLong() {
        long z = (state += 0x9e3779b97f4a7c15L);
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >>> 31);
    }

    /** Returns pseudo-random 64-bit integer in range [0, bound). */
    public long nextLong(long bound) {
        if (bound <= 0) throw new IllegalArgumentException("bound must be positive");
        long r = nextLong() >>> 1;
        long m = bound - 1;
        if ((bound & m) == 0) { // power of two
            return (bound * r) >>> 31;
        }
        for (long u = r; u - (r = u % bound) + m < 0; u = nextLong() >>> 1) {}
        return r;
    }

    /** Generates 32-bit pseudo-random integer. */
    public int nextInt() {
        return (int) nextLong();
    }

    /** Generates integer in range [0, bound). */
    public int nextInt(int bound) {
        if (bound <= 0) throw new IllegalArgumentException("bound must be positive");
        int r = (int) (nextLong() >>> 33);
        long m = (long) r * (long) bound;
        return (int) (m >>> 31);
    }

    /** Generates integer in inclusive range [min, max]. */
    public int nextInt(int min, int max) {
        if (min > max) throw new IllegalArgumentException("min must be <= max");
        return min + nextInt(max - min + 1);
    }

    /** Generates pseudo-random double in [0.0, 1.0). */
    public double nextDouble() {
        return (nextLong() >>> 11) * 0x1.0p-53;
    }

    /** In-place Fisher-Yates array shuffle. */
    public void shuffle(int[] arr) {
        if (arr == null) return;
        for (int i = arr.length - 1; i > 0; i--) {
            int j = nextInt(i + 1);
            int tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }

    /** In-place Fisher-Yates list shuffle. */
    public <T> void shuffle(CustomArrayList<T> list) {
        if (list == null) return;
        for (int i = list.size() - 1; i > 0; i--) {
            int j = nextInt(i + 1);
            T tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
    }
}
