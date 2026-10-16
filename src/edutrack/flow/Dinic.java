package edutrack.flow;

import edutrack.core.CustomArrayList;
import edutrack.core.CustomQueue;

/**
 * Dinic's Algorithm for Maximum Network Flow.
 *
 * <h3>Complexity & Mechanics</h3>
 * <ul>
 *   <li><b>Level Graph:</b> Constructed in O(E) via BFS, enforcing edges with level[v] == level[u] + 1.</li>
 *   <li><b>Blocking Flows:</b> Pushed via DFS with current-arc pointer optimization (ptr[]) in O(VE).</li>
 *   <li><b>Total Time Complexity:</b> O(V^2 * E) for general graphs; O(E * sqrt(V)) for unit networks and bipartite graphs.</li>
 *   <li>Near-optimal performance on realistic academic and capacity allocation workloads.</li>
 * </ul>
 *
 * <p>Strictly conforms to zero {@code java.util.*} imports.</p>
 */
public class Dinic {

    public static class DinicResult {
        public final int maxFlow;
        public final int levelGraphPhases;
        public final long executionNs;
        public final String complexityAnalysis;

        public DinicResult(int maxFlow, int levelGraphPhases, long executionNs, String complexityAnalysis) {
            this.maxFlow = maxFlow;
            this.levelGraphPhases = levelGraphPhases;
            this.executionNs = executionNs;
            this.complexityAnalysis = complexityAnalysis;
        }
    }

    public static DinicResult solve(FlowNetwork network, int source, int sink) {
        long t0 = System.nanoTime();
        network.resetFlow();
        int n = network.getNumVertices();
        int maxFlow = 0;
        int phases = 0;

        int[] level = new int[n];
        int[] ptr = new int[n];

        while (bfsLevelGraph(network, source, sink, level)) {
            phases++;
            for (int i = 0; i < n; i++) ptr[i] = 0;

            while (true) {
                int pushed = dfsBlockingFlow(network, source, sink, Integer.MAX_VALUE, level, ptr);
                if (pushed <= 0) break;
                maxFlow += pushed;
            }
        }

        long elapsed = System.nanoTime() - t0;
        String analysis = "Dinic completed " + phases + " level-graph phase(s) in O(V^2 E) / O(E sqrt(V)) time.";
        return new DinicResult(maxFlow, phases, elapsed, analysis);
    }

    private static boolean bfsLevelGraph(FlowNetwork network, int source, int sink, int[] level) {
        for (int i = 0; i < level.length; i++) level[i] = -1;
        level[source] = 0;

        CustomQueue<Integer> queue = new CustomQueue<>();
        queue.enqueue(source);

        while (!queue.isEmpty()) {
            int u = queue.dequeue();
            CustomArrayList<FlowNetwork.Edge> edges = network.getAdj(u);
            for (int i = 0; i < edges.size(); i++) {
                FlowNetwork.Edge e = edges.get(i);
                if (e.residualCapacity() > 0 && level[e.v] == -1) {
                    level[e.v] = level[u] + 1;
                    queue.enqueue(e.v);
                }
            }
        }
        return level[sink] != -1;
    }

    private static int dfsBlockingFlow(FlowNetwork network, int u, int sink, int flowIn, int[] level, int[] ptr) {
        if (u == sink || flowIn == 0) return flowIn;

        CustomArrayList<FlowNetwork.Edge> edges = network.getAdj(u);
        for (; ptr[u] < edges.size(); ptr[u]++) {
            FlowNetwork.Edge e = edges.get(ptr[u]);
            if (level[e.v] == level[u] + 1 && e.residualCapacity() > 0) {
                int nextFlow = Math.min(flowIn, e.residualCapacity());
                int pushed = dfsBlockingFlow(network, e.v, sink, nextFlow, level, ptr);
                if (pushed > 0) {
                    e.flow += pushed;
                    network.getAdj(e.v).get(e.rev).flow -= pushed;
                    return pushed;
                }
            }
        }
        return 0;
    }
}
