package edutrack.flow;

import edutrack.core.CustomArrayList;

/**
 * Ford-Fulkerson Method for Maximum Flow.
 *
 * <h3>Theoretical Foundations</h3>
 * <ul>
 *   <li><b>Flow Conservation:</b> For all u != source, sink: sum(flow into u) == sum(flow out of u).</li>
 *   <li><b>Capacity Constraint:</b> 0 &lt;= flow(e) &lt;= capacity(e).</li>
 *   <li><b>Max-Flow Min-Cut Theorem:</b> The value of the maximum flow equals the capacity of the minimum s-t cut.</li>
 *   <li><b>Integer-Capacity Setup:</b> If all capacities are integers, there exists an integer maximum flow.</li>
 * </ul>
 *
 * <p>Uses DFS to locate augmenting paths in the residual graph until no further
 * path exists from source to sink. Zero {@code java.util.*} imports.</p>
 */
public class FordFulkerson {

    public static class FlowResult {
        public final int maxFlow;
        public final int augmentationCount;
        public final CustomArrayList<String> augmentationLog;
        public final long executionNs;

        public FlowResult(int maxFlow, int augmentationCount, CustomArrayList<String> augmentationLog, long executionNs) {
            this.maxFlow = maxFlow;
            this.augmentationCount = augmentationCount;
            this.augmentationLog = augmentationLog;
            this.executionNs = executionNs;
        }
    }

    public static FlowResult solve(FlowNetwork network, int source, int sink) {
        long t0 = System.nanoTime();
        network.resetFlow();
        int maxFlow = 0;
        int augmentations = 0;
        CustomArrayList<String> log = new CustomArrayList<>();

        boolean[] visited = new boolean[network.getNumVertices()];

        while (true) {
            for (int i = 0; i < visited.length; i++) visited[i] = false;
            int pushed = dfs(network, source, sink, Integer.MAX_VALUE, visited);
            if (pushed <= 0) break;

            maxFlow += pushed;
            augmentations++;
            if (augmentations <= 10) {
                log.add("Augmentation #" + augmentations + ": pushed bottleneck flow = " + pushed + " (cumulative: " + maxFlow + ")");
            }
        }

        long elapsed = System.nanoTime() - t0;
        return new FlowResult(maxFlow, augmentations, log, elapsed);
    }

    private static int dfs(FlowNetwork network, int u, int sink, int bottleneck, boolean[] visited) {
        if (u == sink) return bottleneck;
        visited[u] = true;

        CustomArrayList<FlowNetwork.Edge> edges = network.getAdj(u);
        for (int i = 0; i < edges.size(); i++) {
            FlowNetwork.Edge e = edges.get(i);
            if (!visited[e.v] && e.residualCapacity() > 0) {
                int nextBottleneck = Math.min(bottleneck, e.residualCapacity());
                int pushed = dfs(network, e.v, sink, nextBottleneck, visited);
                if (pushed > 0) {
                    e.flow += pushed;
                    // Reverse edge residual capacity increases by pushed
                    network.getAdj(e.v).get(e.rev).flow -= pushed;
                    return pushed;
                }
            }
        }
        return 0;
    }
}
