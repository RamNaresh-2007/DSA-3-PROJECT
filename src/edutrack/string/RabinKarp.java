package edutrack.string;

import edutrack.core.CustomArrayList;

/**
 * Rabin-Karp rolling-hash pattern matching algorithm.
 *
 * <h3>Algorithm overview</h3>
 * <ul>
 *   <li>Uses polynomial rolling hash: H(s) = Σ s[i] · BASE^(m-1-i)  mod MOD.</li>
 *   <li>Slides a window of length m across text, updating the hash in O(1).</li>
 *   <li>Average-case O(n + m); worst-case O(nm) on hash collisions.</li>
 *   <li>Double-hashing with two distinct moduli virtually eliminates false positives.</li>
 * </ul>
 *
 * <h3>EduTrack use-case</h3>
 * Searches assignment and course codes efficiently; multi-pattern variant
 * quickly locates any of several code prefixes.
 */
public class RabinKarp {

    // Two large primes for double-hashing collision avoidance
    private static final long MOD1 = 1_000_000_007L;
    private static final long MOD2 = 998_244_353L;
    private static final long BASE1 = 31L;
    private static final long BASE2 = 37L;

    /**
     * Finds all occurrences of pattern in text using double-hash Rabin-Karp.
     *
     * @param text    the text to search in
     * @param pattern the pattern to search for
     * @return list of starting indices (0-based)
     */
    public static CustomArrayList<Integer> search(String text, String pattern) {
        CustomArrayList<Integer> result = new CustomArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty() || text.length() < pattern.length()) {
            return result;
        }

        int n = text.length();
        int m = pattern.length();

        // Compute hash of pattern
        long patHash1 = 0, patHash2 = 0;
        long txtHash1 = 0, txtHash2 = 0;
        long pow1 = 1, pow2 = 1; // BASE^(m-1) mod MOD

        for (int i = 0; i < m; i++) {
            patHash1 = (patHash1 * BASE1 + pattern.charAt(i)) % MOD1;
            patHash2 = (patHash2 * BASE2 + pattern.charAt(i)) % MOD2;
            txtHash1 = (txtHash1 * BASE1 + text.charAt(i)) % MOD1;
            txtHash2 = (txtHash2 * BASE2 + text.charAt(i)) % MOD2;
            if (i > 0) {
                pow1 = (pow1 * BASE1) % MOD1;
                pow2 = (pow2 * BASE2) % MOD2;
            }
        }

        for (int i = 0; i <= n - m; i++) {
            // Check double-hash match, then verify characters
            if (txtHash1 == patHash1 && txtHash2 == patHash2) {
                if (verifyMatch(text, i, pattern)) {
                    result.add(i);
                }
            }
            // Roll the hash window forward
            if (i + m < n) {
                txtHash1 = ((txtHash1 - text.charAt(i) * pow1 % MOD1 + MOD1) * BASE1 + text.charAt(i + m)) % MOD1;
                txtHash2 = ((txtHash2 - text.charAt(i) * pow2 % MOD2 + MOD2) * BASE2 + text.charAt(i + m)) % MOD2;
            }
        }
        return result;
    }

    /**
     * Searches for multiple patterns simultaneously in a single text pass.
     * Useful for scanning academic keywords or course-code prefixes.
     *
     * @param text     the text to search in
     * @param patterns array of patterns to find
     * @return list of Pair(patternIndex, startPosition)
     */
    public static CustomArrayList<int[]> multiPatternSearch(String text, String[] patterns) {
        CustomArrayList<int[]> result = new CustomArrayList<>();
        if (text == null || patterns == null) return result;

        for (int p = 0; p < patterns.length; p++) {
            CustomArrayList<Integer> positions = search(text, patterns[p]);
            for (int j = 0; j < positions.size(); j++) {
                result.add(new int[]{p, positions.get(j)});
            }
        }
        return result;
    }

    /** Character-by-character verification to guard against hash collisions. */
    private static boolean verifyMatch(String text, int start, String pattern) {
        for (int j = 0; j < pattern.length(); j++) {
            if (text.charAt(start + j) != pattern.charAt(j)) return false;
        }
        return true;
    }

    /**
     * Counts total occurrences of pattern in text.
     */
    public static int countOccurrences(String text, String pattern) {
        return search(text, pattern).size();
    }
}
