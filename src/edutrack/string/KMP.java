package edutrack.string;

import edutrack.core.CustomArrayList;

/**
 * Knuth-Morris-Pratt (KMP) pattern matching algorithm.
 *
 * <h3>Algorithm overview</h3>
 * <ul>
 *   <li>Pre-processes the pattern P of length m to compute the failure function
 *       (longest proper prefix that is also a suffix) in O(m).</li>
 *   <li>Scans text T of length n in a single left-to-right pass, never
 *       retreating the text pointer, yielding O(n) matching.</li>
 *   <li>Total complexity: <b>O(n + m)</b> time, O(m) space.</li>
 * </ul>
 *
 * <h3>EduTrack use-case</h3>
 * Searches course names, student queries, and academic keywords.
 */
public class KMP {

    /**
     * Computes the failure function (partial-match / prefix table) for the pattern.
     *
     * failure[i] = length of the longest proper prefix of pattern[0..i]
     *              that is also a suffix of pattern[0..i].
     *
     * @param pattern the pattern string
     * @return failure array of length pattern.length()
     */
    public static int[] computeFailure(String pattern) {
        int m = pattern.length();
        int[] failure = new int[m];
        failure[0] = 0;
        int j = 0; // length of previous longest prefix suffix

        for (int i = 1; i < m; i++) {
            // Fall back along the failure chain until a match or j == 0
            while (j > 0 && pattern.charAt(i) != pattern.charAt(j)) {
                j = failure[j - 1];
            }
            if (pattern.charAt(i) == pattern.charAt(j)) {
                j++;
            }
            failure[i] = j;
        }
        return failure;
    }

    /**
     * Finds all occurrences of pattern in text using KMP.
     *
     * @param text    the text to search in
     * @param pattern the pattern to search for
     * @return list of starting indices (0-based) where pattern occurs in text
     */
    public static CustomArrayList<Integer> search(String text, String pattern) {
        CustomArrayList<Integer> result = new CustomArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty() || text.length() < pattern.length()) {
            return result;
        }

        int n = text.length();
        int m = pattern.length();
        int[] failure = computeFailure(pattern);

        int j = 0; // number of characters matched so far
        for (int i = 0; i < n; i++) {
            while (j > 0 && text.charAt(i) != pattern.charAt(j)) {
                j = failure[j - 1];
            }
            if (text.charAt(i) == pattern.charAt(j)) {
                j++;
            }
            if (j == m) {
                result.add(i - m + 1);   // match found at index (i - m + 1)
                j = failure[j - 1];       // look for next overlapping match
            }
        }
        return result;
    }

    /**
     * Checks whether pattern occurs anywhere in text.
     *
     * @param text    the text to search in
     * @param pattern the pattern to search for
     * @return true if pattern is found
     */
    public static boolean contains(String text, String pattern) {
        return !search(text, pattern).isEmpty();
    }

    /**
     * Returns the index of the first occurrence, or -1.
     */
    public static int firstOccurrence(String text, String pattern) {
        CustomArrayList<Integer> matches = search(text, pattern);
        return matches.isEmpty() ? -1 : matches.get(0);
    }

    /**
     * Counts total occurrences (including overlapping) of pattern in text.
     */
    public static int countOccurrences(String text, String pattern) {
        return search(text, pattern).size();
    }
}
