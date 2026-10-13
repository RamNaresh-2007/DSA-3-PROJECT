package edutrack.dp;

/**
 * Damerau-Levenshtein distance — extends Levenshtein with transpositions.
 *
 * <h3>Algorithm overview</h3>
 * <ul>
 *   <li>Adds a fourth edit operation: transposition of two adjacent characters.</li>
 *   <li>dp[i][j] considers insertion, deletion, substitution, <b>and</b>
 *       transposition (swap s1[i-1] with s1[i-2] when they match s2[j-2] and s2[j-1]).</li>
 *   <li>Time: O(nm), Space: O(nm).</li>
 * </ul>
 *
 * <h3>EduTrack use-case</h3>
 * Handles transposed characters in student search queries (e.g. "aglorithm" → "algorithm").
 */
public class DamerauLevenshtein {

    /**
     * Computes the Damerau-Levenshtein distance (restricted edit distance).
     * This is the "optimal string alignment" variant — each substring is
     * edited at most once.
     *
     * @param s1 first string
     * @param s2 second string
     * @return the minimum edit distance (with transpositions)
     */
    public static int distance(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return (s1 == null && s2 == null) ? 0 :
                    (s1 == null ? s2.length() : s1.length());
        }

        int n = s1.length();
        int m = s2.length();
        int[][] dp = new int[n + 1][m + 1];

        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;

                dp[i][j] = Math.min(
                        dp[i - 1][j] + 1,           // deletion
                        Math.min(
                                dp[i][j - 1] + 1,       // insertion
                                dp[i - 1][j - 1] + cost  // substitution
                        )
                );

                // Transposition: swap two adjacent characters
                if (i > 1 && j > 1
                        && s1.charAt(i - 1) == s2.charAt(j - 2)
                        && s1.charAt(i - 2) == s2.charAt(j - 1)) {
                    dp[i][j] = Math.min(dp[i][j], dp[i - 2][j - 2] + cost);
                }
            }
        }
        return dp[n][m];
    }

    /**
     * Computes similarity ratio: 1 - (distance / max(|s1|, |s2|)).
     */
    public static double similarity(String s1, String s2) {
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;
        return 1.0 - (double) distance(s1, s2) / maxLen;
    }

    /**
     * Returns true if the distance is within the given threshold.
     * Useful for "fuzzy match" filtering.
     */
    public static boolean isFuzzyMatch(String s1, String s2, int threshold) {
        return distance(s1, s2) <= threshold;
    }
}
