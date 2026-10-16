package edutrack.approx;

import edutrack.core.CustomArrayList;

/**
 * The Practical Engineer's Checklist for Recognising NP-Hardness.
 *
 * <h3>The Canonical NP-Hard Structures</h3>
 * <ol>
 *   <li><b>Partitioning:</b> Dividing items into subsets with balanced sums (Partition, Subset-Sum, Bin Packing, Multiprocessor Scheduling).</li>
 *   <li><b>Permutation / Sequencing / Routing:</b> Finding an order of elements minimizing travel/time (TSP, Hamiltonian Path, Flow Shop).</li>
 *   <li><b>Subgraph Selection:</b> Finding densely connected or mutually independent node subsets (Clique, Independent Set, Max Cut).</li>
 *   <li><b>Subset Packing & Covering:</b> Covering elements using minimal containers (Vertex Cover, Set Cover, Feedback Vertex Set).</li>
 *   <li><b>Constraint Satisfaction:</b> Assigning boolean or multi-value states to variables subject to conflicting constraints (3-SAT, Graph Coloring).</li>
 * </ol>
 *
 * <p>Strictly conforms to zero {@code java.util.*} imports.</p>
 */
public class NPHardnessAdvisor {

    public static class Diagnosis {
        public final String problemType;
        public final boolean isNPHard;
        public final String canonicalProblem;
        public final String polynomialReductionPath;
        public final String recommendedStrategy;
        public final String asymptoticCost;

        public Diagnosis(String problemType, boolean isNPHard, String canonicalProblem,
                         String polynomialReductionPath, String recommendedStrategy, String asymptoticCost) {
            this.problemType = problemType;
            this.isNPHard = isNPHard;
            this.canonicalProblem = canonicalProblem;
            this.polynomialReductionPath = polynomialReductionPath;
            this.recommendedStrategy = recommendedStrategy;
            this.asymptoticCost = asymptoticCost;
        }
    }

    /**
     * Diagnoses problem structure based on engineer's questions.
     */
    public static Diagnosis diagnose(String feature) {
        if (feature == null) feature = "";
        String f = feature.toLowerCase();

        if (f.contains("tour") || f.contains("visit every") || f.contains("tsp") || f.contains("route")) {
            return new Diagnosis("Permutation / Routing", true, "Travelling Salesperson Problem (TSP)",
                    "HAM-CYCLE <=_p TSP",
                    "Use Bitmask DP O(2^n * n^2) for n <= 20, or Christofides 1.5-Approximation for metric TSP",
                    "Exponential O(2^n * n^2) / Poly-approx O(n^3)");
        }

        if (f.contains("cover all edges") || f.contains("vertex cover") || f.contains("camera placement")) {
            return new Diagnosis("Subset Covering", true, "Minimum Vertex Cover",
                    "3-SAT <=_p CLIQUE <=_p INDEPENDENT-SET <=_p VERTEX-COVER",
                    "Use 2-Approximation via Maximal Matching in O(V + E), or FPT Kernelization in O(2^k * k^2 + n) for small k",
                    "2-Approx O(V + E) / FPT O(2^k * k^2 + n)");
        }

        if (f.contains("conflict-free") || f.contains("independent") || f.contains("no two adjacent")) {
            return new Diagnosis("Mutual Independence", true, "Maximum Independent Set",
                    "CLIQUE <=_p INDEPENDENT-SET",
                    "If graph is bipartite or a tree, solve in polynomial time O(V + E) via Tree DP / Hopcroft-Karp; otherwise NP-hard",
                    "Tree DP O(V) / General NP-hard O(2^n)");
        }

        if (f.contains("pack") || f.contains("knapsack") || f.contains("budget") || f.contains("weight limit")) {
            return new Diagnosis("Subset Packing / Resource Allocation", true, "0/1 Knapsack Problem",
                    "SUBSET-SUM <=_p 0/1 KNAPSACK",
                    "Use Pseudo-polynomial DP O(nW) if capacity W is moderate, or FPTAS O(n^3 / eps) for (1 - eps) guarantee",
                    "Pseudo-poly O(nW) / FPTAS O(n^3 / eps)");
        }

        if (f.contains("flow") || f.contains("bipartite match") || f.contains("capacity assignment")) {
            return new Diagnosis("Network Capacity Assignment", false, "Max-Flow / Min-Cut",
                    "Adheres to Max-Flow Min-Cut Theorem (Polynomial Solvable)",
                    "Use Dinic's Algorithm O(V^2 * E) or Edmonds-Karp O(V * E^2); NOT NP-hard!",
                    "Polynomial O(V^2 * E)");
        }

        return new Diagnosis("General Academic Scheduling", true, "Multi-Resource Constrained Timetabling",
                "3-SAT <=_p Graph 3-Coloring <=_p Timetabling",
                "Reduce to Integer Linear Programming (ILP) or Flow with Relaxations",
                "NP-Hard in general");
    }

    public static CustomArrayList<Diagnosis> getChecklist() {
        CustomArrayList<Diagnosis> list = new CustomArrayList<>();
        list.add(diagnose("tsp"));
        list.add(diagnose("vertex cover"));
        list.add(diagnose("independent"));
        list.add(diagnose("knapsack"));
        list.add(diagnose("capacity assignment"));
        return list;
    }
}
