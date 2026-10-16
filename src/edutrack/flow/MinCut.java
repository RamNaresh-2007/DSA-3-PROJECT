package edutrack.flow;

import edutrack.core.CustomArrayList;
import edutrack.core.CustomQueue;

/**
 * Min-Cut computation as the dual of Maximum Flow (Max-Flow Min-Cut Theorem).
 *
 * <h3>Theorem & Duality</h3>
 * <ul>
 *   <li>The capacity of any s-t cut is an upper bound on any feasible s-t flow value.</li>
 *   <li>When flow is maximal, the set of vertices S reachable from source in the residual graph
 *       and T = V \ S form a minimum cut whose capacity equals exactly the maximum flow.</li>
 *   <li>Direct applications: network bottleneck identification, project selection,
 *       document/image segmentation, clustering.</li>
 * </ul>
 *
 * <p>Strictly conforms to zero {@code java.util.*} imports.</p>
 */
public class MinCut {

    public static class CutResult {
        public final int minCutCapacity;
        public final int maxFlowValue;
        public final CustomArrayList<Integer> setS;
        public final CustomArrayList<Integer> setT;
        public final CustomArrayList<FlowNetwork.Edge> cutEdges;
        public final boolean dualityVerified;

        public CutResult(int minCutCapacity, int maxFlowValue,
                         CustomArrayList<Integer> setS, CustomArrayList<Integer> setT,
                         CustomArrayList<FlowNetwork.Edge> cutEdges) {
            this.minCutCapacity = minCutCapacity;
            this.maxFlowValue = maxFlowValue;
            this.setS = setS;
            this.setT = setT;
            this.cutEdges = cutEdges;
            this.dualityVerified = (minCutCapacity == maxFlowValue);
        }
    }

    public static CutResult computeMinCut(FlowNetwork network, int source, int sink) {
        // Step 1: Solve Max Flow via Dinic
        Dinic.DinicResult dinic = Dinic.solve(network, source, sink);
        int maxFlow = dinic.maxFlow;

        // Step 2: Find all vertices reachable from source in residual graph
        int n = network.getNumVertices();
        boolean[] reachable = new boolean[n];
        reachable[source] = true;

        CustomQueue<Integer> queue = new CustomQueue<>();
        queue.enqueue(source);

        while (!queue.isEmpty()) {
            int u = queue.dequeue();
            CustomArrayList<FlowNetwork.Edge> edges = network.getAdj(u);
            for (int i = 0; i < edges.size(); i++) {
                FlowNetwork.Edge e = edges.get(i);
                if (e.residualCapacity() > 0 && !reachable[e.v]) {
                    reachable[e.v] = true;
                    queue.enqueue(e.v);
                }
            }
        }

        CustomArrayList<Integer> setS = new CustomArrayList<>();
        CustomArrayList<Integer> setT = new CustomArrayList<>();
        for (int i = 0; i < n; i++) {
            if (reachable[i]) setS.add(i);
            else setT.add(i);
        }

        // Step 3: Find forward edges from S to T
        CustomArrayList<FlowNetwork.Edge> cutEdges = new CustomArrayList<>();
        int cutCapacity = 0;

        for (int i = 0; i < setS.size(); i++) {
            int u = setS.get(i);
            CustomArrayList<FlowNetwork.Edge> edges = network.getAdj(u);
            for (int j = 0; j < edges.size(); j++) {
                FlowNetwork.Edge e = edges.get(j);
                if (e.capacity > 0 && !reachable[e.v]) { // Original forward edge crossing the cut
                    cutEdges.add(e);
                    cutCapacity += e.capacity;
                }
            }
        }

        return new CutResult(cutCapacity, maxFlow, setS, setT, cutEdges);
    }
}
