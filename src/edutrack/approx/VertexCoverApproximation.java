package edutrack.approx;

import edutrack.core.CustomArrayList;

/**
 * 2-Approximation Algorithm for Minimum Vertex Cover via Maximal Matching.
 *
 * <h3>Theoretical Proof of Ratio Guarantee</h3>
 * <ul>
 *   <li>Let M be the maximal matching collected by the algorithm.</li>
 *   <li>Because no two edges in M share any endpoints, any valid vertex cover must pick
 *       at least one distinct endpoint from each edge in M: OPT &gt;= |M|.</li>
 *   <li>The algorithm adds both endpoints of each edge in M to the cover C: |C| = 2 * |M|.</li>
 *   <li>Therefore: |C| = 2 * |M| &lt;= 2 * OPT. The approximation ratio is guaranteed to be &lt;= 2.0.</li>
 * </ul>
 *
 * <p>Strictly conforms to zero {@code java.util.*} imports.</p>
 */
public class VertexCoverApproximation {

    public static class ApproxResult {
        public final CustomArrayList<Integer> approxCover;
        public final int approxSize;
        public final CustomArrayList<int[]> maximalMatching;
        public final int exactOptSize;
        public final double empiricalRatio;
        public final double theoreticalGuarantee;
        public final long executionNs;

        public ApproxResult(CustomArrayList<Integer> approxCover, CustomArrayList<int[]> maximalMatching,
                            int exactOptSize, long executionNs) {
            this.approxCover = approxCover;
            this.approxSize = approxCover.size();
            this.maximalMatching = maximalMatching;
            this.exactOptSize = exactOptSize;
            this.theoreticalGuarantee = 2.0;
            this.empiricalRatio = (exactOptSize > 0) ? ((double) approxSize / exactOptSize) : 1.0;
            this.executionNs = executionNs;
        }
    }

    /**
     * Computes a 2-approximation for Vertex Cover using maximal matching.
     * Also computes exact OPT if graph size <= 20 for ratio comparison.
     */
    public static ApproxResult solve(int numVertices, int[][] edges) {
        long t0 = System.nanoTime();

        boolean[] covered = new boolean[numVertices];
        CustomArrayList<int[]> matching = new CustomArrayList<>();
        CustomArrayList<Integer> cover = new CustomArrayList<>();

        // Greedy maximal matching
        for (int[] e : edges) {
            int u = e[0];
            int v = e[1];
            if (!covered[u] && !covered[v]) {
                matching.add(new int[]{u, v});
                covered[u] = true;
                covered[v] = true;
                cover.add(u);
                cover.add(v);
            }
        }

        // Exact solver for small graphs
        int opt = -1;
        if (numVertices <= 20) {
            opt = solveExactOpt(numVertices, edges);
        }

        long elapsed = System.nanoTime() - t0;
        return new ApproxResult(cover, matching, opt, elapsed);
    }

    private static int solveExactOpt(int numVertices, int[][] edges) {
        for (int k = 1; k <= numVertices; k++) {
            if (hasCoverOfSize(numVertices, edges, k, 0, 0, new boolean[numVertices])) {
                return k;
            }
        }
        return numVertices;
    }

    private static boolean hasCoverOfSize(int n, int[][] edges, int k, int start, int chosenCount, boolean[] inCover) {
        if (chosenCount == k) {
            for (int[] e : edges) {
                if (!inCover[e[0]] && !inCover[e[1]]) return false;
            }
            return true;
        }
        for (int v = start; v < n; v++) {
            inCover[v] = true;
            if (hasCoverOfSize(n, edges, k, v + 1, chosenCount + 1, inCover)) return true;
            inCover[v] = false;
        }
        return false;
    }
}
