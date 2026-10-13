package edutrack.string;

import edutrack.core.CustomArrayList;

/**
 * Z-function string matching algorithm.
 *
 * <h3>Algorithm overview</h3>
 * <ul>
 *   <li>For a string S of length n, Z[i] = length of the longest substring
 *       starting at position i that matches a prefix of S.</li>
 *   <li>Computed in O(n) using the Z-box [L, R] optimisation.</li>
 *   <li>To find pattern P in text T, we construct S = P + "$" + T and
 *       look for Z[i] == |P|.</li>
 * </ul>
 *
 * <h3>EduTrack use-case</h3>
 * Detects repeated phrases in assignment submissions for plagiarism analysis.
 */
public class ZFunction {

    /**
     * Computes the Z-array for the given string in O(n).
     *
     * @param s input string
     * @return Z-array where Z[i] is the length of the longest substring
     *         starting at i that matches a prefix of s. Z[0] is defined as 0.
     */
    public static int[] computeZ(String s) {
        int n = s.length();
        int[] z = new int[n];
        if (n == 0) return z;

        int l = 0, r = 0; // current Z-box [l, r)
        for (int i = 1; i < n; i++) {
            if (i < r) {
                z[i] = Math.min(r - i, z[i - l]);
            }
            // try to extend z[i]
            while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) {
                z[i]++;
            }
            // update Z-box if we went beyond r
            if (i + z[i] > r) {
                l = i;
                r = i + z[i];
            }
        }
        return z;
    }

    /**
     * Finds all occurrences of pattern in text using the Z-function.
     *
     * @param text    the text to search in
     * @param pattern the pattern to search for
     * @return list of starting indices (0-based) in text
     */
    public static CustomArrayList<Integer> search(String text, String pattern) {
        CustomArrayList<Integer> result = new CustomArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty() || text.length() < pattern.length()) {
            return result;
        }

        // Build concatenated string: pattern + "$" + text
        String concat = pattern + "$" + text;
        int[] z = computeZ(concat);
        int m = pattern.length();

        for (int i = m + 1; i < concat.length(); i++) {
            if (z[i] == m) {
                result.add(i - m - 1);  // map back to index in original text
            }
        }
        return result;
    }

    /**
     * Finds repeated substrings of at least minLength in the text.
     * Returns a list of (position, length) pairs.
     *
     * @param text      the text to analyse
     * @param minLength minimum repetition length to report
     * @return list of Pair(startIndex, matchLength)
     */
    public static CustomArrayList<int[]> findRepeatedPhrases(String text, int minLength) {
        CustomArrayList<int[]> result = new CustomArrayList<>();
        if (text == null || text.length() < minLength) return result;

        int[] z = computeZ(text);
        for (int i = 1; i < z.length; i++) {
            if (z[i] >= minLength) {
                result.add(new int[]{i, z[i]});
            }
        }
        return result;
    }

    /**
     * Counts total occurrences of pattern in text.
     */
    public static int countOccurrences(String text, String pattern) {
        return search(text, pattern).size();
    }
}
