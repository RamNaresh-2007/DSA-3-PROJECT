package edutrack.randomparallel;

import edutrack.core.CustomArrayList;

/**
 * Parallel Algorithm Primitives, Work-Span Analysis & Brent's Theorem.
 *
 * <h3>Foundations</h3>
 * <ul>
 *   <li><b>Work (T_1):</b> The total number of operations executed on a single processor.</li>
 *   <li><b>Span (T_infty):</b> The length of the critical path / longest sequence of dependent operations.</li>
 *   <li><b>Parallelism:</b> T_1 / T_infty (the maximum possible speedup on infinitely many cores).</li>
 *   <li><b>Brent's Theorem:</b> On p processors, running time T_p &lt;= (T_1 - T_infty) / p + T_infty.
 *       Speedup ceiling: S_p = T_1 / T_p &lt;= min(p, T_1 / T_infty).</li>
 *   <li><b>Blelloch Work-Efficient Parallel Scan (Prefix Sum):</b>
 *     <ul>
 *       <li>Up-sweep (reduce tree): Computes sum of subtrees in O(n) work and O(log n) span.</li>
 *       <li>Down-sweep: Traverses tree downward distributing prefix sums in O(n) work and O(log n) span.</li>
 *       <li>Total Work: O(n) (work-efficient!), Span: O(log n).</li>
 *     </ul>
 *   </li>
 *   <li><b>Domains:</b> Data-parallel (SIMD, map-reduce) vs Task-parallel; embarrassingly parallel.</li>
 * </ul>
 *
 * <p>Strictly conforms to zero {@code java.util.*} imports.</p>
 */
public class ParallelPrimitives {

    public static class ParallelMetrics {
        public final String primitive;
        public final int inputSize;
        public final long workT1;
        public final int spanTInfinity;
        public final double theoreticalParallelism; // T1 / T_inf
        public final double[] brentSpeedupCores;   // Speedup for 2, 4, 8, 16, 32, 64 cores
        public final long executionNs;

        public ParallelMetrics(String primitive, int inputSize, long workT1, int spanTInfinity, long executionNs) {
            this.primitive = primitive;
            this.inputSize = inputSize;
            this.workT1 = workT1;
            this.spanTInfinity = spanTInfinity;
            this.theoreticalParallelism = (spanTInfinity > 0) ? ((double) workT1 / spanTInfinity) : 1.0;
            this.executionNs = executionNs;

            int[] cores = {2, 4, 8, 16, 32, 64};
            this.brentSpeedupCores = new double[cores.length];
            for (int i = 0; i < cores.length; i++) {
                int p = cores[i];
                double tp = ((double) (workT1 - spanTInfinity) / p) + spanTInfinity;
                this.brentSpeedupCores[i] = (tp > 0) ? (workT1 / tp) : 1.0;
            }
        }
    }

    /**
     * Parallel Reduction (Tree sum).
     * Work: O(n), Span: O(log n).
     */
    public static ParallelMetrics parallelReduce(int[] data) {
        long t0 = System.nanoTime();
        int n = data.length;
        if (n == 0) return new ParallelMetrics("Parallel Reduce", 0, 0, 0, 0);

        long work = n - 1;
        int span = (int) Math.ceil(Math.log(n) / Math.log(2));
        if (span < 1) span = 1;

        // Simulate reduction tree
        long sum = 0;
        for (int v : data) sum += v;

        long elapsed = System.nanoTime() - t0;
        return new ParallelMetrics("Parallel Reduce", n, work, span, elapsed);
    }

    public static class ScanResult {
        public final int[] exclusivePrefixSum;
        public final ParallelMetrics metrics;
        public final int totalSum;

        public ScanResult(int[] exclusivePrefixSum, ParallelMetrics metrics, int totalSum) {
            this.exclusivePrefixSum = exclusivePrefixSum;
            this.metrics = metrics;
            this.totalSum = totalSum;
        }
    }

    /**
     * Blelloch Work-Efficient Parallel Prefix Sum (Exclusive Scan).
     * Work: O(n), Span: O(log n).
     */
    public static ScanResult blellochParallelScan(int[] input) {
        long t0 = System.nanoTime();
        int origLen = input.length;

        // Next power of 2
        int n = 1;
        while (n < origLen) n <<= 1;
        int[] tree = new int[n];
        for (int i = 0; i < origLen; i++) tree[i] = input[i];

        long workOps = 0;
        int spanLevels = 0;

        // 1. Up-sweep (Reduce) phase
        for (int d = 0; (1 << d) < n; d++) {
            spanLevels++;
            int stride = 1 << (d + 1);
            int half = 1 << d;
            for (int k = 0; k < n; k += stride) {
                tree[k + stride - 1] += tree[k + half - 1];
                workOps++;
            }
        }

        int totalSum = tree[n - 1];
        tree[n - 1] = 0; // Clear root for down-sweep

        // 2. Down-sweep phase
        for (int d = (int) (Math.log(n) / Math.log(2)) - 1; d >= 0; d--) {
            spanLevels++;
            int stride = 1 << (d + 1);
            int half = 1 << d;
            for (int k = 0; k < n; k += stride) {
                int t = tree[k + half - 1];
                tree[k + half - 1] = tree[k + stride - 1];
                tree[k + stride - 1] += t;
                workOps += 2;
            }
        }

        int[] result = new int[origLen];
        for (int i = 0; i < origLen; i++) result[i] = tree[i];

        long elapsed = System.nanoTime() - t0;
        ParallelMetrics pm = new ParallelMetrics("Blelloch Work-Efficient Scan", origLen, workOps, spanLevels, elapsed);
        return new ScanResult(result, pm, totalSum);
    }
}
