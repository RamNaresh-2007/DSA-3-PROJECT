package edutrack.dp;

/**
 * Matrix-Chain Multiplication — classic Interval DP.
 *
 * <h3>Algorithm overview</h3>
 * <ul>
 *   <li>Given a chain of n matrices A₁(p₀×p₁), A₂(p₁×p₂), ..., Aₙ(pₙ₋₁×pₙ),
 *       find the optimal parenthesisation that minimises total scalar multiplications.</li>
 *   <li>dp[i][j] = minimum cost to multiply matrices Aᵢ..Aⱼ.</li>
 *   <li>Recurrence: dp[i][j] = min_{i ≤ k < j} (dp[i][k] + dp[k+1][j] + pᵢ₋₁ · pₖ · pⱼ)</li>
 *   <li>Time: O(n³), Space: O(n²).</li>
 * </ul>
 *
 * <h3>EduTrack use-case</h3>
 * Optimises the order of academic data processing operations (e.g., joining
 * large relation tables for analytics).
 */
public class MatrixChainMultiplication {

    /**
     * Result of the matrix-chain optimisation.
     */
    public static class MCMResult {
        public final long minCost;
        public final String optimalParenthesisation;

        public MCMResult(long minCost, String parenthesisation) {
            this.minCost = minCost;
            this.optimalParenthesisation = parenthesisation;
        }

        @Override
        public String toString() {
            return "Minimum multiplications: " + minCost +
                   "\nOptimal parenthesisation: " + optimalParenthesisation;
        }
    }

    /**
     * Solves the matrix-chain multiplication problem.
     *
     * @param dims array of dimensions: matrix i has dimensions dims[i-1] x dims[i].
     *             Length = n + 1 for n matrices.
     * @return result with minimum cost and optimal parenthesisation
     */
    public static MCMResult solve(int[] dims) {
        int n = dims.length - 1; // number of matrices
        if (n <= 0) return new MCMResult(0, "");
        if (n == 1) return new MCMResult(0, "A1");

        long[][] dp = new long[n + 1][n + 1];
        int[][] split = new int[n + 1][n + 1];

        // Initialise: cost of single matrix is 0
        for (int i = 1; i <= n; i++) dp[i][i] = 0;

        // Fill for chains of increasing length
        for (int len = 2; len <= n; len++) {
            for (int i = 1; i <= n - len + 1; i++) {
                int j = i + len - 1;
                dp[i][j] = Long.MAX_VALUE;
                for (int k = i; k < j; k++) {
                    long cost = dp[i][k] + dp[k + 1][j]
                            + (long) dims[i - 1] * dims[k] * dims[j];
                    if (cost < dp[i][j]) {
                        dp[i][j] = cost;
                        split[i][j] = k;
                    }
                }
            }
        }

        String paren = buildParenthesisation(split, 1, n);
        return new MCMResult(dp[1][n], paren);
    }

    /** Recursively builds the parenthesisation string from the split table. */
    private static String buildParenthesisation(int[][] split, int i, int j) {
        if (i == j) return "A" + i;
        return "(" + buildParenthesisation(split, i, split[i][j])
                + " x " + buildParenthesisation(split, split[i][j] + 1, j) + ")";
    }

    /**
     * Prints the DP cost table for educational purposes.
     */
    public static String printCostTable(int[] dims) {
        int n = dims.length - 1;
        long[][] dp = new long[n + 1][n + 1];
        for (int len = 2; len <= n; len++) {
            for (int i = 1; i <= n - len + 1; i++) {
                int j = i + len - 1;
                dp[i][j] = Long.MAX_VALUE;
                for (int k = i; k < j; k++) {
                    long cost = dp[i][k] + dp[k + 1][j]
                            + (long) dims[i - 1] * dims[k] * dims[j];
                    if (cost < dp[i][j]) dp[i][j] = cost;
                }
            }
        }
        StringBuilder sb = new StringBuilder("Matrix-Chain DP Cost Table:\n");
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                if (j < i) sb.append(String.format("%10s", "-"));
                else sb.append(String.format("%10d", dp[i][j]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
