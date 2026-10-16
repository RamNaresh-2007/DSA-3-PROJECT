package edutrack.flow;

import edutrack.core.CustomArrayList;

/**
 * Residual Flow Network representation for network flow algorithms.
 *
 * <p>Strictly conforms to project constraints: zero {@code java.util.*} imports.
 * Maintains directed edges and their reverse residual counterparts using
 * index pointers for instantaneous residual capacity updates in O(1).</p>
 */
public class FlowNetwork {

    public static class Edge {
        public final int u;
        public final int v;
        public final int capacity;
        public int flow;
        public final int cost; // For min-cost max-flow
        public final int rev;  // Index of reverse edge in adj[v]

        public Edge(int u, int v, int capacity, int cost, int rev) {
            this.u = u;
            this.v = v;
            this.capacity = capacity;
            this.cost = cost;
            this.flow = 0;
            this.rev = rev;
        }

        public int residualCapacity() {
            return capacity - flow;
        }

        @Override
        public String toString() {
            return "(" + u + "->" + v + ", cap=" + capacity + ", flow=" + flow + (cost != 0 ? ", cost=" + cost : "") + ")";
        }
    }

    private final int numVertices;
    private final CustomArrayList<Edge>[] adj;
    private final CustomArrayList<Edge> allEdges;

    @SuppressWarnings("unchecked")
    public FlowNetwork(int numVertices) {
        if (numVertices <= 0) throw new IllegalArgumentException("Vertices must be positive");
        this.numVertices = numVertices;
        this.adj = new CustomArrayList[numVertices];
        for (int i = 0; i < numVertices; i++) {
            adj[i] = new CustomArrayList<>();
        }
        this.allEdges = new CustomArrayList<>();
    }

    /**
     * Adds a directed edge with capacity and zero cost.
     * Automatically inserts the reverse residual edge with zero capacity.
     */
    public void addEdge(int u, int v, int capacity) {
        addEdge(u, v, capacity, 0);
    }

    /**
     * Adds a directed edge with capacity and per-unit cost.
     */
    public void addEdge(int u, int v, int capacity, int cost) {
        if (u < 0 || u >= numVertices || v < 0 || v >= numVertices) {
            throw new IndexOutOfBoundsException("Vertex indices out of range: " + u + " -> " + v);
        }
        int aIndex = adj[u].size();
        int bIndex = adj[v].size();
        Edge forward = new Edge(u, v, capacity, cost, bIndex);
        Edge backward = new Edge(v, u, 0, -cost, aIndex);
        adj[u].add(forward);
        adj[v].add(backward);
        allEdges.add(forward);
    }

    public int getNumVertices() {
        return numVertices;
    }

    public CustomArrayList<Edge>[] getAdj() {
        return adj;
    }

    public CustomArrayList<Edge> getAdj(int u) {
        return adj[u];
    }

    public CustomArrayList<Edge> getAllEdges() {
        return allEdges;
    }

    /** Resets all edge flows back to zero. */
    public void resetFlow() {
        for (int i = 0; i < numVertices; i++) {
            for (int j = 0; j < adj[i].size(); j++) {
                adj[i].get(j).flow = 0;
            }
        }
    }
}
