package edutrack.dp;

/**
 * Sum Over Subsets (SOS) DP — O(n · 2ⁿ) enumeration.
 *
 * <h3>Algorithm overview</h3>
 * <ul>
 *   <li>Given f[mask] for all 2ⁿ masks, compute
 *       g[mask] = Σ f[sub] for all sub ⊆ mask (submask sum).</li>
 *   <li>Naïve approach is O(3ⁿ) (iterate all submask pairs). SOS DP reduces
 *       this to O(n · 2ⁿ) by processing one bit dimension at a time.</li>
 *   <li>Applications: inclusion-exclusion, convolution over subset lattice,
 *       Möbius inversion.</li>
 * </ul>
 *
 * <h3>EduTrack use-case</h3>
 * Inclusion-exclusion analytics — e.g., counting students enrolled in
 * various combinations of courses, computing "at least k of these courses"
 * statistics efficiently.
 */
public class SOSDP {

    /**
     * Computes the subset-sum transform (zeta / SOS):
     * result[mask] = Σ f[sub] for all sub that are submasks of mask.
     *
     * @param f    input array of size 2^n
     * @param n    number of bits
     * @return the SOS-transformed array
     */
    public static long[] subsetSum(long[] f, int n) {
        int size = 1 << n;
        long[] dp = new long[size];
        for (int i = 0; i < size; i++) dp[i] = f[i];

        for (int bit = 0; bit < n; bit++) {
            for (int mask = 0; mask < size; mask++) {
                if ((mask & (1 << bit)) != 0) {
                    dp[mask] += dp[mask ^ (1 << bit)];
                }
            }
        }
        return dp;
    }

    /**
     * Computes the superset-sum transform:
     * result[mask] = Σ f[sup] for all sup that are supermasks of mask.
     *
     * @param f input array of size 2^n
     * @param n number of bits
     * @return the superset-sum transformed array
     */
    public static long[] supersetSum(long[] f, int n) {
        int size = 1 << n;
        long[] dp = new long[size];
        for (int i = 0; i < size; i++) dp[i] = f[i];

        for (int bit = 0; bit < n; bit++) {
            for (int mask = 0; mask < size; mask++) {
                if ((mask & (1 << bit)) == 0) {
                    dp[mask] += dp[mask | (1 << bit)];
                }
            }
        }
        return dp;
    }

    /**
     * Möbius inversion (inverse of subset sum):
     * Recovers f from g where g[mask] = Σ f[sub] for sub ⊆ mask.
     *
     * @param g the SOS-transformed array
     * @param n number of bits
     * @return the original f array
     */
    public static long[] mobiusInversion(long[] g, int n) {
        int size = 1 << n;
        long[] f = new long[size];
        for (int i = 0; i < size; i++) f[i] = g[i];

        for (int bit = 0; bit < n; bit++) {
            for (int mask = 0; mask < size; mask++) {
                if ((mask & (1 << bit)) != 0) {
                    f[mask] -= f[mask ^ (1 << bit)];
                }
            }
        }
        return f;
    }

    /**
     * Inclusion-exclusion via SOS DP.
     * Given sets S₁, S₂, ..., Sₖ (each represented as a bitmask of elements),
     * and a universe of n elements, computes the number of elements in
     * the union |S₁ ∪ S₂ ∪ ... ∪ Sₖ|.
     *
     * @param sets   array of bitmasks, each representing a set
     * @param n      number of elements in the universe
     * @return size of the union
     */
    public static int inclusionExclusionUnion(int[] sets, int n) {
        int k = sets.length;
        int total = 0;

        // Iterate over all non-empty subsets of {sets}
        for (int mask = 1; mask < (1 << k); mask++) {
            int intersection = (1 << n) - 1; // full universe
            int setBits = 0;
            for (int i = 0; i < k; i++) {
                if ((mask & (1 << i)) != 0) {
                    intersection &= sets[i];
                    setBits++;
                }
            }

            int count = Integer.bitCount(intersection);
            if (setBits % 2 == 1) total += count;
            else total -= count;
        }
        return total;
    }

    /**
     * Demonstrates SOS DP with a practical example:
     * Given enrollment data (each student's course bitmask), compute
     * for each course combination how many students take at least those courses.
     *
     * @param enrollments array where enrollments[i] = bitmask of courses student i takes
     * @param numCourses  number of distinct courses (≤ 20)
     * @return array where result[mask] = number of students enrolled in all courses in mask
     */
    public static long[] courseEnrollmentAnalysis(int[] enrollments, int numCourses) {
        int size = 1 << numCourses;
        long[] f = new long[size];

        // f[mask] = number of students whose exact enrollment is mask
        for (int enrollment : enrollments) {
            if (enrollment < size) f[enrollment]++;
        }

        // After superset sum: result[mask] = students enrolled in ALL courses in mask
        return supersetSum(f, numCourses);
    }
}
