package edutrack.randomparallel;

import edutrack.core.CustomRandom;

/**
 * Las Vegas Randomised QuickSort with Rigorous Expected-Time Analysis.
 *
 * <h3>Theoretical Foundations</h3>
 * <ul>
 *   <li><b>Las Vegas Property:</b> Always produces the correct sorted output; running time is a random variable.</li>
 *   <li><b>Rigorous Expected Comparisons:</b> Let z_1, ..., z_n be the elements in sorted order.
 *       Two elements z_i and z_j are compared if and only if one of them is chosen as pivot before any element
 *       between them in the sorted sequence. The probability is exactly 2 / (j - i + 1).</li>
 *   <li>Summing over all pairs: E[Comparisons] = sum_{i=1}^{n-1} sum_{j=i+1}^n (2 / (j - i + 1))
 *       = 2n ln(n) + O(n) approx 1.386 * n * log_2(n).</li>
 *   <li><b>Adversary Defeat:</b> Deterministic quicksort (e.g. first-element pivot) degrades to Omega(n^2)
 *       on sorted/reverse-sorted inputs. Random pivot selection renders worst-case inputs impossible to engineer.</li>
 * </ul>
 *
 * <p>Strictly conforms to zero {@code java.util.*} imports.</p>
 */
public class RandomizedQuickSort {

    private static final CustomRandom rng = new CustomRandom(42);

    public static class SortMetrics {
        public final String variant;
        public final int elementCount;
        public final long comparisons;
        public final int maxRecursionDepth;
        public final double theoreticalExpectedComparisons;
        public final long executionNs;
        public final boolean verifiedSorted;

        public SortMetrics(String variant, int elementCount, long comparisons, int maxRecursionDepth,
                           long executionNs, boolean verifiedSorted) {
            this.variant = variant;
            this.elementCount = elementCount;
            this.comparisons = comparisons;
            this.maxRecursionDepth = maxRecursionDepth;
            this.theoreticalExpectedComparisons = (elementCount > 1)
                    ? (2.0 * elementCount * Math.log(elementCount))
                    : 0;
            this.executionNs = executionNs;
            this.verifiedSorted = verifiedSorted;
        }
    }

    public static SortMetrics sortRandomized(int[] arr) {
        if (arr == null || arr.length == 0) {
            return new SortMetrics("Randomized (Las Vegas)", 0, 0, 0, 0, true);
        }
        int[] copy = arr.clone();
        long t0 = System.nanoTime();
        long[] counter = new long[2]; // [comparisons, maxDepth]
        quickSortRandomized(copy, 0, copy.length - 1, 1, counter);
        long elapsed = System.nanoTime() - t0;
        return new SortMetrics("Randomized (Las Vegas)", copy.length, counter[0], (int) counter[1], elapsed, isSorted(copy));
    }

    public static SortMetrics sortDeterministic(int[] arr) {
        if (arr == null || arr.length == 0) {
            return new SortMetrics("Deterministic (First-Element Pivot)", 0, 0, 0, 0, true);
        }
        int[] copy = arr.clone();
        long t0 = System.nanoTime();
        long[] counter = new long[2];
        quickSortDeterministic(copy, 0, copy.length - 1, 1, counter);
        long elapsed = System.nanoTime() - t0;
        return new SortMetrics("Deterministic (First-Element Pivot)", copy.length, counter[0], (int) counter[1], elapsed, isSorted(copy));
    }

    private static void quickSortRandomized(int[] a, int low, int high, int depth, long[] counter) {
        if (depth > counter[1]) counter[1] = depth;
        if (low < high) {
            // Pick random pivot in [low, high] and swap to low
            int pivotIdx = low + rng.nextInt(high - low + 1);
            int tmp = a[low]; a[low] = a[pivotIdx]; a[pivotIdx] = tmp;

            int p = partition(a, low, high, counter);
            quickSortRandomized(a, low, p - 1, depth + 1, counter);
            quickSortRandomized(a, p + 1, high, depth + 1, counter);
        }
    }

    private static void quickSortDeterministic(int[] a, int low, int high, int depth, long[] counter) {
        if (depth > counter[1]) counter[1] = depth;
        if (low < high) {
            int p = partition(a, low, high, counter);
            quickSortDeterministic(a, low, p - 1, depth + 1, counter);
            quickSortDeterministic(a, p + 1, high, depth + 1, counter);
        }
    }

    private static int partition(int[] a, int low, int high, long[] counter) {
        int pivot = a[low];
        int i = low + 1;
        int j = high;

        while (true) {
            while (i <= high) {
                counter[0]++;
                if (a[i] > pivot) break;
                i++;
            }
            while (j >= low + 1) {
                counter[0]++;
                if (a[j] <= pivot) break;
                j--;
            }
            if (i < j) {
                int tmp = a[i]; a[i] = a[j]; a[j] = tmp;
            } else {
                break;
            }
        }
        int tmp = a[low]; a[low] = a[j]; a[j] = tmp;
        return j;
    }

    private static boolean isSorted(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) return false;
        }
        return true;
    }
}
