package edutrack.dp;

/**
 * Levenshtein Edit Distance — Wagner-Fischer algorithm.
 *
 * <h3>Algorithm overview</h3>
 * <ul>
 *   <li>The Levenshtein distance between two strings is the minimum number of
 *       single-character insertions, deletions, or substitutions to transform
 *       one string into the other.</li>
 *   <li>dp[i][j] = edit distance between s1[0..i-1] and s2[0..j-1].</li>
 *   <li>Recurrence:
 *       <ul>
 *         <li>If s1[i-1] == s2[j-1]: dp[i][j] = dp[i-1][j-1]</li>
 *         <li>Else: dp[i][j] = 1 + min(dp[i-1][j], dp[i][j-1], dp[i-1][j-1])</li>
 *       </ul>
 *   </li>
 *   <li>Time: O(nm), Space: O(nm) or O(min(n,m)) with rolling array.</li>
 * </ul>
 *
 * <h3>EduTrack use-case</h3>
 * Corrects student search queries by finding the closest matching course name,
 * keyword, or student name in the system.
 */
public class Levenshtein {

    /**
     * Computes the Levenshtein edit distance between two strings.
     *
     * @param s1 first string
     * @param s2 second string
     * @return the minimum edit distance
     */
    public static int distance(String s1, String s2) {
        if (s1 == null || s2 == null) return s1 == s2 ? 0 : Math.max(
                s1 == null ? 0 : s1.length(), s2 == null ? 0 : s2.length());

        int n = s1.length();
        int m = s2.length();

        // Space-optimised: only keep two rows
        int[] prev = new int[m + 1];
        int[] curr = new int[m + 1];

        // Base case: transforming empty string to s2[0..j-1]
        for (int j = 0; j <= m; j++) prev[j] = j;

        for (int i = 1; i <= n; i++) {
            curr[0] = i; // transforming s1[0..i-1] to empty string
            for (int j = 1; j <= m; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    curr[j] = prev[j - 1]; // no edit needed
                } else {
                    int insert  = curr[j - 1] + 1;
                    int delete  = prev[j] + 1;
                    int replace = prev[j - 1] + 1;
                    curr[j] = Math.min(insert, Math.min(delete, replace));
                }
            }
            // Swap rows
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }
        return prev[m];
    }

    /**
     * Computes the full DP table (useful for backtracking the edit operations).
     *
     * @param s1 first string
     * @param s2 second string
     * @return the complete dp table of size (n+1) x (m+1)
     */
    public static int[][] distanceTable(String s1, String s2) {
        int n = s1.length(), m = s2.length();
        int[][] dp = new int[n + 1][m + 1];

        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j],
                                   Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
                }
            }
        }
        return dp;
    }

    /**
     * Returns the sequence of edit operations to transform s1 into s2.
     * Operations: MATCH, INSERT, DELETE, REPLACE.
     *
     * @param s1 source string
     * @param s2 target string
     * @return human-readable list of operations
     */
    public static String[] getEditOperations(String s1, String s2) {
        int[][] dp = distanceTable(s1, s2);
        int n = s1.length(), m = s2.length();

        // Backtrace
        int opsCount = 0;
        String[] ops = new String[n + m]; // upper bound
        int i = n, j = m;

        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && s1.charAt(i - 1) == s2.charAt(j - 1)) {
                ops[opsCount++] = "MATCH '" + s1.charAt(i - 1) + "'";
                i--; j--;
            } else if (i > 0 && j > 0 && dp[i][j] == dp[i - 1][j - 1] + 1) {
                ops[opsCount++] = "REPLACE '" + s1.charAt(i - 1) + "' -> '" + s2.charAt(j - 1) + "'";
                i--; j--;
            } else if (j > 0 && dp[i][j] == dp[i][j - 1] + 1) {
                ops[opsCount++] = "INSERT '" + s2.charAt(j - 1) + "'";
                j--;
            } else {
                ops[opsCount++] = "DELETE '" + s1.charAt(i - 1) + "'";
                i--;
            }
        }

        // Reverse the operations
        String[] result = new String[opsCount];
        for (int k = 0; k < opsCount; k++) {
            result[k] = ops[opsCount - 1 - k];
        }
        return result;
    }

    /**
     * Computes similarity ratio: 1 - (distance / max(|s1|, |s2|)).
     */
    public static double similarity(String s1, String s2) {
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;
        return 1.0 - (double) distance(s1, s2) / maxLen;
    }
}
