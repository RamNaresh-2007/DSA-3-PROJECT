package edutrack.dp;

/**
 * Optimal Binary Search Tree (OBST) — Interval DP.
 *
 * <h3>Algorithm overview</h3>
 * <ul>
 *   <li>Given n keys with known search frequencies, construct a BST that
 *       minimises the expected search cost (weighted path length).</li>
 *   <li>dp[i][j] = minimum expected cost of a BST containing keys i..j.</li>
 *   <li>Recurrence: dp[i][j] = min_{i ≤ r ≤ j} (dp[i][r-1] + dp[r+1][j] + W[i][j])</li>
 *   <li>Knuth's optimisation: O(n²) by monotonicity of optimal roots.</li>
 *   <li>Standard version: O(n³).</li>
 * </ul>
 *
 * <h3>EduTrack use-case</h3>
 * Organises frequently accessed academic keys (course codes, student IDs)
 * into a search structure that minimises average lookup time.
 */
public class OptimalBST {

    /**
     * Result of the OBST construction.
     */
    public static class OBSTResult {
        public final double minCost;
        public final int[][] root;  // root[i][j] = optimal root for keys i..j
        public final String treeDescription;

        public OBSTResult(double minCost, int[][] root, String desc) {
            this.minCost = minCost;
            this.root = root;
            this.treeDescription = desc;
        }

        @Override
        public String toString() {
            return "Minimum expected search cost: " + String.format("%.4f", minCost) +
                   "\n" + treeDescription;
        }
    }

    /**
     * Computes the optimal BST using O(n³) DP.
     *
     * @param keys   the sorted keys (for display purposes)
     * @param freq   search frequencies for each key (freq[i] = frequency of keys[i])
     * @return result with minimum cost and tree structure
     */
    public static OBSTResult solve(String[] keys, double[] freq) {
        int n = keys.length;
        double[][] dp = new double[n + 2][n + 1]; // dp[i][j] for keys i..j
        double[][] w  = new double[n + 2][n + 1];  // prefix sums of frequencies
        int[][] root  = new int[n + 1][n + 1];

        // Initialise weight sums
        for (int i = 1; i <= n; i++) {
            w[i][i] = freq[i - 1];
            dp[i][i] = freq[i - 1];
            root[i][i] = i;
        }
        // w[i][j] = sum of freq[i..j]
        for (int len = 2; len <= n; len++) {
            for (int i = 1; i <= n - len + 1; i++) {
                int j = i + len - 1;
                w[i][j] = w[i][j - 1] + freq[j - 1];
            }
        }

        // Fill DP for chains of increasing length
        for (int len = 2; len <= n; len++) {
            for (int i = 1; i <= n - len + 1; i++) {
                int j = i + len - 1;
                dp[i][j] = Double.MAX_VALUE;
                for (int r = i; r <= j; r++) {
                    double cost = (r > i ? dp[i][r - 1] : 0) +
                                  (r < j ? dp[r + 1][j] : 0) +
                                  w[i][j];
                    if (cost < dp[i][j]) {
                        dp[i][j] = cost;
                        root[i][j] = r;
                    }
                }
            }
        }

        String desc = buildTreeDescription(root, keys, 1, n, "");
        return new OBSTResult(dp[1][n], root, desc);
    }

    /** Recursively builds a textual description of the optimal BST. */
    private static String buildTreeDescription(int[][] root, String[] keys, int i, int j, String prefix) {
        if (i > j) return "";
        int r = root[i][j];
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append("Root: ").append(keys[r - 1]).append("\n");
        if (i <= r - 1) sb.append(buildTreeDescription(root, keys, i, r - 1, prefix + "  L-> "));
        if (r + 1 <= j) sb.append(buildTreeDescription(root, keys, r + 1, j, prefix + "  R-> "));
        return sb.toString();
    }
}
