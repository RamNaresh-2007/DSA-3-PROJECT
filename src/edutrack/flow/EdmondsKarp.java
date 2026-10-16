package edutrack.flow;

import edutrack.core.CustomArrayList;
import edutrack.core.CustomQueue;

/**
 * Edmonds-Karp Algorithm for Maximum Network Flow.
 *
 * <h3>Complexity & Properties</h3>
 * <ul>
 *   <li>Uses Breadth-First Search (BFS) to find augmenting paths with minimum edge count.</li>
 *   <li>Guaranteed polynomial time complexity: O(V * E^2).</li>
 *   <li>Independent of capacity values (prevents pathological infinite loops with irrational capacities).</li>
 *   <li>Acts as the canonical practical reference implementation.</li>
 * </ul>
 *
 * <p>Strictly conforms to zero {@code java.util.*} imports using {@link CustomQueue}.</p>
 */
public class EdmondsKarp {

    public static class FlowResult {
        public final int maxFlow;
        public final int augmentations;
        public final CustomArrayList<String> pathHistory;
        public final long executionNs;

        public FlowResult(int maxFlow, int augmentations, CustomArrayList<String> pathHistory, long executionNs) {
            this.maxFlow = maxFlow;
            this.augmentations = augmentations;
            this.pathHistory = pathHistory;
            this.executionNs = executionNs;
        }
    }

    public static FlowResult solve(FlowNetwork network, int source, int sink) {
        long t0 = System.nanoTime();
        network.resetFlow();
        int n = network.getNumVertices();
        int maxFlow = 0;
        int augmentations = 0;
        CustomArrayList<String> pathHistory = new CustomArrayList<>();

        FlowNetwork.Edge[] parentEdge = new FlowNetwork.Edge[n];

        while (true) {
            for (int i = 0; i < n; i++) parentEdge[i] = null;

            CustomQueue<Integer> queue = new CustomQueue<>();
            queue.enqueue(source);

            while (!queue.isEmpty()) {
                int u = queue.dequeue();
                if (u == sink) break;

                CustomArrayList<FlowNetwork.Edge> edges = network.getAdj(u);
                for (int i = 0; i < edges.size(); i++) {
                    FlowNetwork.Edge e = edges.get(i);
                    if (e.residualCapacity() > 0 && parentEdge[e.v] == null && e.v != source) {
                        parentEdge[e.v] = e;
                        queue.enqueue(e.v);
                    }
                }
            }

            // No augmenting path found
            if (parentEdge[sink] == null) break;

            // Find bottleneck capacity along BFS path
            int bottleneck = Integer.MAX_VALUE;
            CustomArrayList<Integer> pathNodes = new CustomArrayList<>();
            for (int curr = sink; curr != source; ) {
                pathNodes.add(curr);
                FlowNetwork.Edge e = parentEdge[curr];
                bottleneck = Math.min(bottleneck, e.residualCapacity());
                curr = e.u;
            }
            pathNodes.add(source);

            // Augment flow along the path
            for (int curr = sink; curr != source; ) {
                FlowNetwork.Edge forward = parentEdge[curr];
                forward.flow += bottleneck;
                network.getAdj(forward.v).get(forward.rev).flow -= bottleneck;
                curr = forward.u;
            }

            maxFlow += bottleneck;
            augmentations++;

            if (augmentations <= 8) {
                StringBuilder sb = new StringBuilder("BFS Path: ");
                for (int i = pathNodes.size() - 1; i >= 0; i--) {
                    sb.append(pathNodes.get(i));
                    if (i > 0) sb.append(" -> ");
                }
                sb.append(" (pushed bottleneck: ").append(bottleneck).append(")");
                pathHistory.add(sb.toString());
            }
        }

        long elapsed = System.nanoTime() - t0;
        return new FlowResult(maxFlow, augmentations, pathHistory, elapsed);
    }
}
