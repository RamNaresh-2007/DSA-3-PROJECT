package edutrack.flow;

import edutrack.core.CustomArrayList;
import edutrack.core.CustomQueue;

/**
 * Min-Cost Max-Flow (MCMF) via Successive Shortest Path (SSP) with SPFA.
 *
 * <h3>Foundations</h3>
 * <ul>
 *   <li><b>Goal:</b> Transmit the maximum feasible flow from source to sink while minimizing the total cost:
 *       min sum(flow(e) * cost(e)).</li>
 *   <li><b>Successive Shortest Path (SSP):</b> Iteratively finds the augmenting path of minimum unit cost
 *       in the residual graph using Shortest Path Faster Algorithm (SPFA / queue-optimized Bellman-Ford).</li>
 *   <li><b>Cycle Cancelling:</b> An alternative view: a flow is minimum-cost if and only if
 *       its residual network contains no negative-cost directed cycles.</li>
 *   <li><b>University Use-Case:</b> Assigning students to internship companies or elective tracks
 *       matching preferences (low cost = high student preference rank) under department quota limits.</li>
 * </ul>
 *
 * <p>Strictly conforms to zero {@code java.util.*} imports.</p>
 */
public class MinCostMaxFlow {

    public static class MCMFResult {
        public final int maxFlow;
        public final int minCost;
        public final int augmentations;
        public final long executionNs;

        public MCMFResult(int maxFlow, int minCost, int augmentations, long executionNs) {
            this.maxFlow = maxFlow;
            this.minCost = minCost;
            this.augmentations = augmentations;
            this.executionNs = executionNs;
        }
    }

    public static MCMFResult solve(FlowNetwork network, int source, int sink) {
        long t0 = System.nanoTime();
        network.resetFlow();
        int n = network.getNumVertices();
        int totalFlow = 0;
        int totalCost = 0;
        int augmentations = 0;

        int[] dist = new int[n];
        FlowNetwork.Edge[] parentEdge = new FlowNetwork.Edge[n];
        boolean[] inQueue = new boolean[n];

        final int INF = 1_000_000_000;

        while (true) {
            for (int i = 0; i < n; i++) {
                dist[i] = INF;
                parentEdge[i] = null;
                inQueue[i] = false;
            }

            dist[source] = 0;
            CustomQueue<Integer> q = new CustomQueue<>();
            q.enqueue(source);
            inQueue[source] = true;

            // SPFA (Bellman-Ford variant) to find minimum-cost path in residual graph
            while (!q.isEmpty()) {
                int u = q.dequeue();
                inQueue[u] = false;

                CustomArrayList<FlowNetwork.Edge> edges = network.getAdj(u);
                for (int i = 0; i < edges.size(); i++) {
                    FlowNetwork.Edge e = edges.get(i);
                    if (e.residualCapacity() > 0 && dist[e.v] > dist[u] + e.cost) {
                        dist[e.v] = dist[u] + e.cost;
                        parentEdge[e.v] = e;
                        if (!inQueue[e.v]) {
                            q.enqueue(e.v);
                            inQueue[e.v] = true;
                        }
                    }
                }
            }

            // If sink cannot be reached, we have achieved maximum flow at minimum cost
            if (dist[sink] == INF) break;

            // Find bottleneck capacity along the shortest-cost path
            int bottleneck = Integer.MAX_VALUE;
            for (int curr = sink; curr != source; ) {
                FlowNetwork.Edge e = parentEdge[curr];
                bottleneck = Math.min(bottleneck, e.residualCapacity());
                curr = e.u;
            }

            // Augment flow
            for (int curr = sink; curr != source; ) {
                FlowNetwork.Edge forward = parentEdge[curr];
                forward.flow += bottleneck;
                network.getAdj(forward.v).get(forward.rev).flow -= bottleneck;
                curr = forward.u;
            }

            totalFlow += bottleneck;
            totalCost += bottleneck * dist[sink];
            augmentations++;
        }

        long elapsed = System.nanoTime() - t0;
        return new MCMFResult(totalFlow, totalCost, augmentations, elapsed);
    }
}
