package edutrack.dp;

import edutrack.core.CustomArrayList;

/**
 * Tree DP — dynamic programming on tree structures.
 *
 * <h3>Algorithm overview</h3>
 * <ul>
 *   <li>DP on trees exploits the recursive structure of trees: each subtree
 *       is an independent subproblem once the parent edge is fixed.</li>
 *   <li>Common patterns: compute values bottom-up from leaves to root,
 *       then optionally "re-root" to compute the answer for every node.</li>
 *   <li>Applications: maximum independent set, tree diameter, tree centroid,
 *       subset sums on tree paths.</li>
 * </ul>
 *
 * <h3>EduTrack use-case</h3>
 * Prerequisite tree analysis — modelling course prerequisites as a tree
 * and computing optimal study plans, longest prerequisite chains, etc.
 */
public class TreeDP {

    /**
     * Computes the diameter of an unweighted tree (longest path between any two nodes).
     * Uses two-pass BFS / DFS approach via DP.
     *
     * @param adj adjacency list (adj[i] = list of neighbours of node i)
     * @param n   number of nodes
     * @return the diameter (number of edges on the longest path)
     */
    public static int treeDiameter(CustomArrayList<Integer>[] adj, int n) {
        if (n <= 1) return 0;

        int[] depth = new int[n];
        boolean[] visited = new boolean[n];
        int[] diameter = {0};

        dfsDepth(adj, 0, visited, depth, diameter);
        return diameter[0];
    }

    /** DFS that computes the two longest paths from each node downward. */
    private static int dfsDepth(CustomArrayList<Integer>[] adj, int u, boolean[] visited,
                                int[] depth, int[] diameter) {
        visited[u] = true;
        int max1 = 0, max2 = 0; // two longest downward paths

        for (int i = 0; i < adj[u].size(); i++) {
            int v = adj[u].get(i);
            if (!visited[v]) {
                int childDepth = dfsDepth(adj, v, visited, depth, diameter);
                if (childDepth >= max1) {
                    max2 = max1;
                    max1 = childDepth;
                } else if (childDepth > max2) {
                    max2 = childDepth;
                }
            }
        }

        // The longest path through u is max1 + max2
        diameter[0] = Math.max(diameter[0], max1 + max2);
        depth[u] = max1 + 1;
        return depth[u];
    }

    /**
     * Maximum Independent Set on a tree.
     * dp[u][0] = max weight of independent set in subtree(u) when u is NOT selected.
     * dp[u][1] = max weight when u IS selected.
     *
     * @param adj     adjacency list
     * @param weights node weights
     * @param n       number of nodes
     * @return maximum weight of an independent set
     */
    public static int maxIndependentSet(CustomArrayList<Integer>[] adj, int[] weights, int n) {
        int[][] dp = new int[n][2];
        boolean[] visited = new boolean[n];
        dfsIndependent(adj, 0, visited, dp, weights);
        return Math.max(dp[0][0], dp[0][1]);
    }

    private static void dfsIndependent(CustomArrayList<Integer>[] adj, int u,
                                        boolean[] visited, int[][] dp, int[] weights) {
        visited[u] = true;
        dp[u][0] = 0;          // u not selected
        dp[u][1] = weights[u]; // u selected

        for (int i = 0; i < adj[u].size(); i++) {
            int v = adj[u].get(i);
            if (!visited[v]) {
                dfsIndependent(adj, v, visited, dp, weights);
                dp[u][0] += Math.max(dp[v][0], dp[v][1]); // children can be selected or not
                dp[u][1] += dp[v][0]; // children must NOT be selected
            }
        }
    }

    /**
     * Rerooting technique: computes the answer for every node as root.
     * Demonstrates the pattern where we first compute dp with an arbitrary root,
     * then propagate to compute dp as if each node were the root.
     *
     * Example: for each node, compute the sum of distances to all other nodes.
     *
     * @param adj adjacency list
     * @param n   number of nodes
     * @return sumOfDistances[i] = sum of distances from node i to all others
     */
    public static long[] sumOfDistancesAllRoots(CustomArrayList<Integer>[] adj, int n) {
        int[] subtreeSize = new int[n];
        long[] downDist = new long[n];   // sum of distances to all descendants
        long[] answer = new long[n];
        boolean[] visited = new boolean[n];

        // Pass 1: root at 0, compute subtreeSize and downDist
        dfsDown(adj, 0, visited, subtreeSize, downDist);

        // Pass 2: reroot — propagate from root to children
        for (int i = 0; i < n; i++) visited[i] = false;
        answer[0] = downDist[0];
        dfsReroot(adj, 0, visited, subtreeSize, downDist, answer, n);

        return answer;
    }

    private static void dfsDown(CustomArrayList<Integer>[] adj, int u, boolean[] visited,
                                 int[] subtreeSize, long[] downDist) {
        visited[u] = true;
        subtreeSize[u] = 1;
        downDist[u] = 0;

        for (int i = 0; i < adj[u].size(); i++) {
            int v = adj[u].get(i);
            if (!visited[v]) {
                dfsDown(adj, v, visited, subtreeSize, downDist);
                subtreeSize[u] += subtreeSize[v];
                downDist[u] += downDist[v] + subtreeSize[v];
            }
        }
    }

    private static void dfsReroot(CustomArrayList<Integer>[] adj, int u, boolean[] visited,
                                   int[] subtreeSize, long[] downDist, long[] answer, int n) {
        visited[u] = true;
        for (int i = 0; i < adj[u].size(); i++) {
            int v = adj[u].get(i);
            if (!visited[v]) {
                // When we move root from u to v:
                // answer[v] = answer[u] - subtreeSize[v] + (n - subtreeSize[v])
                answer[v] = answer[u] - subtreeSize[v] + (n - subtreeSize[v]);
                dfsReroot(adj, v, visited, subtreeSize, downDist, answer, n);
            }
        }
    }
}
