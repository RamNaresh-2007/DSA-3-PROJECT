package edutrack.dp;

import edutrack.core.CustomArrayList;

/**
 * Bitmask DP — exponential-time exact algorithms on subsets.
 *
 * <h3>Algorithm overview</h3>
 * <ul>
 *   <li>Represents subsets of n items as n-bit integers (bitmask).</li>
 *   <li>TSP: dp[mask][i] = shortest path visiting exactly the cities in mask,
 *       ending at city i. O(2ⁿ · n²).</li>
 *   <li>Set cover: dp[mask] = minimum number of sets covering mask. O(2ⁿ · m).</li>
 *   <li>Practical limit: n ≤ 20 (since 2²⁰ ≈ 10⁶).</li>
 * </ul>
 *
 * <h3>EduTrack use-case</h3>
 * Explores feasible combinations of courses/resources subject to constraints
 * (e.g., "cover all required topics using the fewest elective slots").
 */
public class BitmaskDP {

    /**
     * Solves the Travelling Salesman Problem (TSP) using bitmask DP.
     * Finds the shortest Hamiltonian cycle visiting all n cities.
     *
     * @param dist n×n distance matrix (dist[i][j] = cost from city i to j)
     * @return TSPResult with minimum cost and tour order
     */
    public static TSPResult solveTSP(int[][] dist) {
        int n = dist.length;
        int FULL = (1 << n) - 1;
        int[][] dp = new int[1 << n][n];
        int[][] parent = new int[1 << n][n];

        // Initialise with infinity
        for (int mask = 0; mask <= FULL; mask++) {
            for (int i = 0; i < n; i++) {
                dp[mask][i] = Integer.MAX_VALUE / 2;
                parent[mask][i] = -1;
            }
        }

        // Start from city 0
        dp[1][0] = 0;

        for (int mask = 1; mask <= FULL; mask++) {
            for (int u = 0; u < n; u++) {
                if ((mask & (1 << u)) == 0) continue;
                if (dp[mask][u] >= Integer.MAX_VALUE / 2) continue;

                for (int v = 0; v < n; v++) {
                    if ((mask & (1 << v)) != 0) continue; // already visited
                    int newMask = mask | (1 << v);
                    int newCost = dp[mask][u] + dist[u][v];
                    if (newCost < dp[newMask][v]) {
                        dp[newMask][v] = newCost;
                        parent[newMask][v] = u;
                    }
                }
            }
        }

        // Find minimum cost to return to city 0
        int minCost = Integer.MAX_VALUE;
        int lastCity = -1;
        for (int u = 1; u < n; u++) {
            int cost = dp[FULL][u] + dist[u][0];
            if (cost < minCost) {
                minCost = cost;
                lastCity = u;
            }
        }

        // Reconstruct tour
        int[] tour = new int[n + 1];
        tour[n] = 0;
        int mask = FULL;
        for (int i = n - 1; i >= 1; i--) {
            tour[i] = lastCity;
            int prevCity = parent[mask][lastCity];
            mask ^= (1 << lastCity);
            lastCity = prevCity;
        }
        tour[0] = 0;

        return new TSPResult(minCost, tour);
    }

    /**
     * Result of the TSP solution.
     */
    public static class TSPResult {
        public final int minCost;
        public final int[] tour;

        public TSPResult(int minCost, int[] tour) {
            this.minCost = minCost;
            this.tour = tour;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("TSP Min Cost: " + minCost + "\nTour: ");
            for (int i = 0; i < tour.length; i++) {
                if (i > 0) sb.append(" -> ");
                sb.append(tour[i]);
            }
            return sb.toString();
        }
    }

    /**
     * Solves the weighted set cover problem using bitmask DP.
     * Given m sets over n elements, find the minimum-cost collection of sets
     * that covers all n elements.
     *
     * @param sets   sets[i] = bitmask of elements covered by set i
     * @param costs  costs[i] = cost of selecting set i
     * @param n      number of elements (n ≤ 20)
     * @return SetCoverResult with minimum cost and selected sets
     */
    public static SetCoverResult solveSetCover(int[] sets, int[] costs, int n) {
        int FULL = (1 << n) - 1;
        int[] dp = new int[1 << n];
        int[] lastSet = new int[1 << n]; // which set was used to reach this mask

        for (int mask = 0; mask <= FULL; mask++) {
            dp[mask] = Integer.MAX_VALUE / 2;
            lastSet[mask] = -1;
        }
        dp[0] = 0;

        for (int mask = 0; mask < FULL; mask++) {
            if (dp[mask] >= Integer.MAX_VALUE / 2) continue;
            for (int s = 0; s < sets.length; s++) {
                int newMask = mask | sets[s];
                if (dp[mask] + costs[s] < dp[newMask]) {
                    dp[newMask] = dp[mask] + costs[s];
                    lastSet[newMask] = s;
                }
            }
        }

        // Reconstruct selected sets
        CustomArrayList<Integer> selected = new CustomArrayList<>();
        int mask = FULL;
        while (mask != 0 && lastSet[mask] != -1) {
            int s = lastSet[mask];
            selected.add(s);
            mask = mask & ~sets[s]; // might overshoot, so we track differently
            // Actually, we need to reconstruct properly
        }
        // Simpler reconstruction
        selected = new CustomArrayList<>();
        mask = FULL;
        while (mask != 0) {
            int s = lastSet[mask];
            if (s == -1) break;
            selected.add(s);
            // Remove the bits that this set covers, keeping only bits we had
            int prevMask = mask & ~sets[s];
            // But prevMask might not be exact — walk back through DP
            // Better: track the exact previous mask
            mask = findPrevMask(dp, sets, costs, mask, s);
        }

        return new SetCoverResult(dp[FULL], selected);
    }

    /** Finds the previous mask that led to the current mask via set s. */
    private static int findPrevMask(int[] dp, int[] sets, int[] costs, int mask, int s) {
        // The previous mask is any submask of mask that, when unioned with sets[s], gives mask
        // and has dp[prev] + costs[s] == dp[mask]
        int covered = mask & sets[s];
        // Iterate over submasks of covered
        int sub = covered;
        while (true) {
            int prev = mask ^ sub; // OR: mask without some of covered bits
            if ((prev | sets[s]) == mask && dp[prev] + costs[s] == dp[mask]) {
                return prev;
            }
            if (sub == 0) break;
            sub = (sub - 1) & covered;
        }
        return 0; // fallback
    }

    /**
     * Result of the set cover solution.
     */
    public static class SetCoverResult {
        public final int minCost;
        public final CustomArrayList<Integer> selectedSets;

        public SetCoverResult(int minCost, CustomArrayList<Integer> selectedSets) {
            this.minCost = minCost;
            this.selectedSets = selectedSets;
        }

        @Override
        public String toString() {
            return "Set Cover Min Cost: " + minCost + "\nSelected sets: " + selectedSets;
        }
    }

    /**
     * Detects whether a Hamiltonian path exists in a graph using bitmask DP.
     *
     * @param adj adjacency matrix (adj[i][j] = true if edge i→j exists)
     * @return true if a Hamiltonian path exists
     */
    public static boolean hasHamiltonianPath(boolean[][] adj) {
        int n = adj.length;
        boolean[][] dp = new boolean[1 << n][n];

        // Base: paths of length 1 starting at each vertex
        for (int i = 0; i < n; i++) {
            dp[1 << i][i] = true;
        }

        for (int mask = 1; mask < (1 << n); mask++) {
            for (int u = 0; u < n; u++) {
                if (!dp[mask][u]) continue;
                for (int v = 0; v < n; v++) {
                    if ((mask & (1 << v)) != 0) continue;
                    if (adj[u][v]) {
                        dp[mask | (1 << v)][v] = true;
                    }
                }
            }
        }

        int FULL = (1 << n) - 1;
        for (int u = 0; u < n; u++) {
            if (dp[FULL][u]) return true;
        }
        return false;
    }
}
