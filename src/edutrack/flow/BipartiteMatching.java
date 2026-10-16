package edutrack.flow;

import edutrack.core.CustomArrayList;

/**
 * Maximum Cardinality Bipartite Matching and König's Theorem.
 *
 * <h3>Theoretical Foundations</h3>
 * <ul>
 *   <li><b>Flow Reduction:</b> Add source s -> U with capacity 1; U -> V with capacity 1; V -> sink t with capacity 1.</li>
 *   <li><b>Integrality Theorem:</b> Because all capacities are 1, maximum flow is integer, representing a valid 1-to-1 matching.</li>
 *   <li><b>König's Theorem:</b> In any bipartite graph, max matching size == min vertex cover size.</li>
 *   <li><b>University Use-Case:</b> Assigning students/groups to faculty advisors, elective tracks, or lab benches.</li>
 * </ul>
 *
 * <p>Strictly conforms to zero {@code java.util.*} imports.</p>
 */
public class BipartiteMatching {

    public static class MatchingResult {
        public final int matchingSize;
        public final CustomArrayList<int[]> matchedPairs; // {leftIndex, rightIndex}
        public final CustomArrayList<Integer> minVertexCoverLeft;
        public final CustomArrayList<Integer> minVertexCoverRight;
        public final boolean konigTheoremSatisfied;
        public final long executionNs;

        public MatchingResult(int matchingSize, CustomArrayList<int[]> matchedPairs,
                              CustomArrayList<Integer> minVertexCoverLeft,
                              CustomArrayList<Integer> minVertexCoverRight,
                              long executionNs) {
            this.matchingSize = matchingSize;
            this.matchedPairs = matchedPairs;
            this.minVertexCoverLeft = minVertexCoverLeft;
            this.minVertexCoverRight = minVertexCoverRight;
            int coverSize = minVertexCoverLeft.size() + minVertexCoverRight.size();
            this.konigTheoremSatisfied = (coverSize == matchingSize);
            this.executionNs = executionNs;
        }
    }

    /**
     * Solves maximum bipartite matching between leftSet (0 .. numLeft-1)
     * and rightSet (0 .. numRight-1).
     *
     * @param numLeft  number of items in left partition (e.g. students)
     * @param numRight number of items in right partition (e.g. lab projects)
     * @param edges    list of [leftIndex, rightIndex] feasible connections
     */
    public static MatchingResult solve(int numLeft, int numRight, CustomArrayList<int[]> edges) {
        long t0 = System.nanoTime();

        // Node indexing:
        // 0: Source s
        // 1 .. numLeft: Left vertices (u + 1)
        // numLeft + 1 .. numLeft + numRight: Right vertices (v + 1 + numLeft)
        // numLeft + numRight + 1: Sink t
        int source = 0;
        int sink = numLeft + numRight + 1;
        FlowNetwork network = new FlowNetwork(sink + 1);

        // s -> left
        for (int u = 0; u < numLeft; u++) {
            network.addEdge(source, u + 1, 1);
        }

        // left -> right
        for (int i = 0; i < edges.size(); i++) {
            int[] edge = edges.get(i);
            int u = edge[0];
            int v = edge[1];
            if (u >= 0 && u < numLeft && v >= 0 && v < numRight) {
                network.addEdge(u + 1, numLeft + 1 + v, 1);
            }
        }

        // right -> t
        for (int v = 0; v < numRight; v++) {
            network.addEdge(numLeft + 1 + v, sink, 1);
        }

        // Solve via Dinic O(E * sqrt(V)) on unit network
        Dinic.DinicResult dinic = Dinic.solve(network, source, sink);
        int maxMatching = dinic.maxFlow;

        // Extract matched pairs
        CustomArrayList<int[]> pairs = new CustomArrayList<>();
        for (int u = 0; u < numLeft; u++) {
            CustomArrayList<FlowNetwork.Edge> adj = network.getAdj(u + 1);
            for (int i = 0; i < adj.size(); i++) {
                FlowNetwork.Edge e = adj.get(i);
                if (e.capacity > 0 && e.flow == 1 && e.v > numLeft && e.v <= numLeft + numRight) {
                    int v = e.v - (numLeft + 1);
                    pairs.add(new int[]{u, v});
                }
            }
        }

        // Compute Min Vertex Cover via Min-Cut reachability
        MinCut.CutResult cut = MinCut.computeMinCut(network, source, sink);
        boolean[] inS = new boolean[sink + 1];
        for (int i = 0; i < cut.setS.size(); i++) inS[cut.setS.get(i)] = true;

        // In König's theorem: Min Vertex Cover = (Left \ S) union (Right intersection S)
        CustomArrayList<Integer> coverLeft = new CustomArrayList<>();
        CustomArrayList<Integer> coverRight = new CustomArrayList<>();

        for (int u = 0; u < numLeft; u++) {
            if (!inS[u + 1]) coverLeft.add(u);
        }
        for (int v = 0; v < numRight; v++) {
            if (inS[numLeft + 1 + v]) coverRight.add(v);
        }

        long elapsed = System.nanoTime() - t0;
        return new MatchingResult(maxMatching, pairs, coverLeft, coverRight, elapsed);
    }
}
