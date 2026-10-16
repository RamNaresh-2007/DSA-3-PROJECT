package edutrack.approx;

import edutrack.core.CustomArrayList;

/**
 * Knapsack Approximation Algorithms: Greedy 2-Approximation & FPTAS.
 *
 * <h3>Approximation Hierarchy</h3>
 * <ul>
 *   <li><b>APX:</b> Problems with constant-factor polynomial approximations (e.g. Vertex Cover 2-approx).</li>
 *   <li><b>PTAS:</b> Polynomial-Time Approximation Scheme: (1 + eps) approximation in O(n^(f(1/eps))).</li>
 *   <li><b>FPTAS:</b> Fully Polynomial-Time Approximation Scheme: (1 + eps) approximation in time polynomial in BOTH n and (1/eps).</li>
 *   <li>0/1 Knapsack admits an FPTAS via value scaling and dynamic programming!</li>
 * </ul>
 *
 * <p>Strictly conforms to zero {@code java.util.*} imports.</p>
 */
public class KnapsackApproximation {

    public static class Item {
        public final int id;
        public final int weight;
        public final int value;
        public final double ratio;

        public Item(int id, int weight, int value) {
            this.id = id;
            this.weight = weight;
            this.value = value;
            this.ratio = (weight > 0) ? ((double) value / weight) : 0;
        }
    }

    public static class KnapsackResult {
        public final String method;
        public final int totalValue;
        public final int totalWeight;
        public final CustomArrayList<Integer> selectedItemIds;
        public final double approximationGuarantee;
        public final long executionNs;

        public KnapsackResult(String method, int totalValue, int totalWeight,
                              CustomArrayList<Integer> selectedItemIds,
                              double approximationGuarantee, long executionNs) {
            this.method = method;
            this.totalValue = totalValue;
            this.totalWeight = totalWeight;
            this.selectedItemIds = selectedItemIds;
            this.approximationGuarantee = approximationGuarantee;
            this.executionNs = executionNs;
        }
    }

    /**
     * Greedy 2-Approximation for 0/1 Knapsack.
     * Takes max(Greedy by ratio, Max single item). Guarantees >= 0.5 * OPT.
     */
    public static KnapsackResult solveGreedy2Approx(int[] weights, int[] values, int capacity) {
        long t0 = System.nanoTime();
        int n = weights.length;
        CustomArrayList<Item> items = new CustomArrayList<>();
        int maxSingleVal = 0;
        int maxSingleIdx = -1;

        for (int i = 0; i < n; i++) {
            items.add(new Item(i, weights[i], values[i]));
            if (weights[i] <= capacity && values[i] > maxSingleVal) {
                maxSingleVal = values[i];
                maxSingleIdx = i;
            }
        }

        // Sort items by value/weight ratio descending (selection sort)
        for (int i = 0; i < items.size() - 1; i++) {
            int best = i;
            for (int j = i + 1; j < items.size(); j++) {
                if (items.get(j).ratio > items.get(best).ratio) best = j;
            }
            if (best != i) {
                Item tmp = items.get(i);
                items.set(i, items.get(best));
                items.set(best, tmp);
            }
        }

        int greedyWeight = 0;
        int greedyValue = 0;
        CustomArrayList<Integer> greedySelected = new CustomArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (greedyWeight + item.weight <= capacity) {
                greedyWeight += item.weight;
                greedyValue += item.value;
                greedySelected.add(item.id);
            }
        }

        long elapsed = System.nanoTime() - t0;
        if (maxSingleVal > greedyValue) {
            CustomArrayList<Integer> single = new CustomArrayList<>();
            single.add(maxSingleIdx);
            return new KnapsackResult("Greedy 2-Approximation (Single Max Item)", maxSingleVal, weights[maxSingleIdx], single, 0.5, elapsed);
        } else {
            return new KnapsackResult("Greedy 2-Approximation (Ratio Packing)", greedyValue, greedyWeight, greedySelected, 0.5, elapsed);
        }
    }

    /**
     * FPTAS for 0/1 Knapsack: guarantees >= (1 - eps) * OPT in O(n^3 / eps) time.
     */
    public static KnapsackResult solveFPTAS(int[] weights, int[] values, int capacity, double epsilon) {
        long t0 = System.nanoTime();
        int n = weights.length;
        int maxVal = 0;
        for (int v : values) if (v > maxVal) maxVal = v;

        if (maxVal == 0 || n == 0) {
            return new KnapsackResult("FPTAS (Empty)", 0, 0, new CustomArrayList<>(), 1.0 - epsilon, System.nanoTime() - t0);
        }

        double theta = (epsilon * maxVal) / n;
        if (theta < 1.0) theta = 1.0;

        int[] scaledValues = new int[n];
        int sumScaled = 0;
        for (int i = 0; i < n; i++) {
            scaledValues[i] = (int) Math.floor(values[i] / theta);
            sumScaled += scaledValues[i];
        }

        // DP[v]: min weight to achieve scaled value v
        final int INF = 1_000_000_000;
        int[] dp = new int[sumScaled + 1];
        int[][] parent = new int[n][sumScaled + 1];

        for (int v = 1; v <= sumScaled; v++) dp[v] = INF;
        dp[0] = 0;

        for (int i = 0; i < n; i++) {
            int w = weights[i];
            int v = scaledValues[i];
            for (int val = sumScaled; val >= v; val--) {
                if (dp[val - v] != INF && dp[val - v] + w < dp[val]) {
                    dp[val] = dp[val - v] + w;
                    parent[i][val] = 1;
                }
            }
        }

        // Find max value with weight <= capacity
        int bestScaledVal = 0;
        for (int val = sumScaled; val >= 0; val--) {
            if (dp[val] <= capacity) {
                bestScaledVal = val;
                break;
            }
        }

        // Reconstruct items
        CustomArrayList<Integer> selected = new CustomArrayList<>();
        int currVal = bestScaledVal;
        int totalOriginalValue = 0;
        int totalWeight = 0;

        for (int i = n - 1; i >= 0; i--) {
            if (parent[i][currVal] == 1) {
                selected.add(i);
                currVal -= scaledValues[i];
                totalOriginalValue += values[i];
                totalWeight += weights[i];
            }
        }

        long elapsed = System.nanoTime() - t0;
        return new KnapsackResult("FPTAS (" + String.format("%.0f%%", (1.0 - epsilon) * 100) + " Guarantee)",
                totalOriginalValue, totalWeight, selected, 1.0 - epsilon, elapsed);
    }
}
