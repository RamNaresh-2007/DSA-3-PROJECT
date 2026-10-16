package edutrack.approx;

import edutrack.core.CustomArrayList;

/**
 * Parameterised Complexity & Fixed-Parameter Tractable (FPT) Vertex Cover.
 *
 * <h3>Foundations</h3>
 * <ul>
 *   <li><b>FPT Definition:</b> An algorithm with running time O(f(k) * n^O(1)), where
 *       the combinatorial explosion is confined strictly to the parameter k, not the input size n.</li>
 *   <li><b>Buss Kernelisation:</b> Reduces the instance to a "kernel" of size at most 2k vertices
 *       and k^2 edges in polynomial time:
 *     <ul>
 *       <li>Rule 1: Any vertex with degree &gt; k MUST be included in the cover (otherwise all its
 *           &gt; k neighbors must be in the cover, exceeding budget k).</li>
 *       <li>Rule 2: Discard isolated vertices (degree 0).</li>
 *       <li>Rule 3: If remaining edges &gt; k^2, output NO immediately.</li>
 *     </ul>
 *   </li>
 *   <li><b>Bounded Search Tree:</b> Branch on an uncovered edge (u, v): either take u or take v.
 *       Tree depth is bounded by k, yielding O(2^k * k^2 + n) runtime!</li>
 * </ul>
 *
 * <p>Strictly conforms to zero {@code java.util.*} imports.</p>
 */
public class ParameterizedVertexCover {

    public static class FptResult {
        public final boolean feasible;
        public final int budgetK;
        public final int kernelVertices;
        public final int kernelEdges;
        public final CustomArrayList<Integer> vertexCover;
        public final String diagnosis;
        public final long executionNs;

        public FptResult(boolean feasible, int budgetK, int kernelVertices, int kernelEdges,
                         CustomArrayList<Integer> vertexCover, String diagnosis, long executionNs) {
            this.feasible = feasible;
            this.budgetK = budgetK;
            this.kernelVertices = kernelVertices;
            this.kernelEdges = kernelEdges;
            this.vertexCover = vertexCover;
            this.diagnosis = diagnosis;
            this.executionNs = executionNs;
        }
    }

    /**
     * Solves k-Vertex Cover using Buss Kernelisation and Bounded Search Tree.
     */
    public static FptResult solve(int numVertices, int[][] edges, int k) {
        long t0 = System.nanoTime();

        // Step 1: Buss Kernelisation
        int[] degrees = new int[numVertices];
        for (int[] e : edges) {
            degrees[e[0]]++;
            degrees[e[1]]++;
        }

        CustomArrayList<Integer> mandatory = new CustomArrayList<>();
        boolean[] inMandatory = new boolean[numVertices];
        int remainingK = k;

        for (int v = 0; v < numVertices; v++) {
            if (degrees[v] > k) {
                mandatory.add(v);
                inMandatory[v] = true;
                remainingK--;
            }
        }

        if (remainingK < 0) {
            long elapsed = System.nanoTime() - t0;
            return new FptResult(false, k, numVertices, edges.length, new CustomArrayList<>(),
                    "Buss Kernelization: More than k vertices had degree > k. Impossible to cover within budget k.", elapsed);
        }

        // Collect remaining edges not covered by mandatory vertices
        CustomArrayList<int[]> kernelEdgesList = new CustomArrayList<>();
        boolean[] inKernel = new boolean[numVertices];

        for (int[] e : edges) {
            if (!inMandatory[e[0]] && !inMandatory[e[1]]) {
                kernelEdgesList.add(e);
                inKernel[e[0]] = true;
                inKernel[e[1]] = true;
            }
        }

        int kernelVerticesCount = 0;
        for (int v = 0; v < numVertices; v++) {
            if (inKernel[v]) kernelVerticesCount++;
        }

        // Kernel size check: max k^2 edges allowed in kernel
        if (kernelEdgesList.size() > remainingK * remainingK) {
            long elapsed = System.nanoTime() - t0;
            return new FptResult(false, k, kernelVerticesCount, kernelEdgesList.size(), new CustomArrayList<>(),
                    "Kernelization bound exceeded: " + kernelEdgesList.size() + " edges > k^2 (" + (remainingK * remainingK)
                            + "). No vertex cover of size " + k + " exists.", elapsed);
        }

        // Step 2: Bounded Search Tree on the Kernel
        CustomArrayList<Integer> chosen = new CustomArrayList<>();
        boolean found = searchTree(kernelEdgesList, remainingK, chosen);

        long elapsed = System.nanoTime() - t0;
        if (found) {
            CustomArrayList<Integer> fullCover = new CustomArrayList<>();
            for (int i = 0; i < mandatory.size(); i++) fullCover.add(mandatory.get(i));
            for (int i = 0; i < chosen.size(); i++) fullCover.add(chosen.get(i));
            return new FptResult(true, k, kernelVerticesCount, kernelEdgesList.size(), fullCover,
                    "FPT Bounded Search Tree verified: Found cover of size " + fullCover.size() + " within budget " + k + ".", elapsed);
        } else {
            return new FptResult(false, k, kernelVerticesCount, kernelEdgesList.size(), new CustomArrayList<>(),
                    "Search tree exhausted: No vertex cover of size <= " + k + " exists.", elapsed);
        }
    }

    private static boolean searchTree(CustomArrayList<int[]> edges, int k, CustomArrayList<Integer> currentCover) {
        // Find first uncovered edge
        int[] uncovered = null;
        for (int i = 0; i < edges.size(); i++) {
            int[] e = edges.get(i);
            boolean uIn = false, vIn = false;
            for (int j = 0; j < currentCover.size(); j++) {
                if (currentCover.get(j) == e[0]) uIn = true;
                if (currentCover.get(j) == e[1]) vIn = true;
            }
            if (!uIn && !vIn) {
                uncovered = e;
                break;
            }
        }

        if (uncovered == null) return true; // All edges covered!
        if (k <= 0) return false;

        // Branch 1: include u
        currentCover.add(uncovered[0]);
        if (searchTree(edges, k - 1, currentCover)) return true;
        currentCover.remove(currentCover.size() - 1);

        // Branch 2: include v
        currentCover.add(uncovered[1]);
        if (searchTree(edges, k - 1, currentCover)) return true;
        currentCover.remove(currentCover.size() - 1);

        return false;
    }
}
