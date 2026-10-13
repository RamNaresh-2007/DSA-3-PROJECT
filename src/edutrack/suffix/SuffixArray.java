package edutrack.suffix;

import edutrack.core.CustomArrayList;

/**
 * Suffix Array construction and search — O(n log² n) construction.
 *
 * <h3>Algorithm overview</h3>
 * <ul>
 *   <li>A suffix array SA for a string S of length n is a permutation of [0..n-1]
 *       such that S[SA[0]..] &lt; S[SA[1]..] &lt; ... (lexicographic order).</li>
 *   <li>Construction uses doubling (rank pairs of length 1, 2, 4, ...) with
 *       radix/merge sort at each level → O(n log² n) or O(n log n) with radix.</li>
 *   <li>Binary search on the suffix array gives O(m log n) substring queries.</li>
 * </ul>
 *
 * <h3>EduTrack use-case</h3>
 * Indexes textbooks, notes, and academic documents for fast substring lookup.
 */
public class SuffixArray {

    private final String text;
    private final int[] sa;     // the suffix array
    private final int n;

    /**
     * Constructs the suffix array for the given text in O(n log² n).
     *
     * @param text the input string
     */
    public SuffixArray(String text) {
        this.text = text;
        this.n = text.length();
        this.sa = build(text);
    }

    /** Returns the underlying suffix array. */
    public int[] getSuffixArray() { return sa; }

    /** Returns the original text. */
    public String getText() { return text; }

    /**
     * Searches for a pattern in the indexed text using binary search on the suffix array.
     * Time: O(m log n) where m = pattern.length().
     *
     * @param pattern the substring to search for
     * @return list of starting positions where pattern occurs
     */
    public CustomArrayList<Integer> search(String pattern) {
        CustomArrayList<Integer> result = new CustomArrayList<>();
        if (pattern == null || pattern.isEmpty() || n == 0) return result;

        int m = pattern.length();

        // Find the leftmost position where the suffix >= pattern
        int lo = 0, hi = n - 1;
        int left = n; // first index in SA where suffix starts with pattern
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            int cmp = compareSuffix(sa[mid], pattern, m);
            if (cmp >= 0) { left = mid; hi = mid - 1; }
            else lo = mid + 1;
        }

        // Find the rightmost position where the suffix starts with pattern
        lo = left; hi = n - 1;
        int right = left - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            int cmp = compareSuffix(sa[mid], pattern, m);
            if (cmp <= 0) { right = mid; lo = mid + 1; }
            else hi = mid - 1;
        }

        for (int i = left; i <= right; i++) {
            result.add(sa[i]);
        }
        return result;
    }

    /**
     * Compares the suffix starting at pos with the pattern (first m chars).
     * Returns negative if suffix < pattern, 0 if prefix matches, positive if suffix > pattern.
     */
    private int compareSuffix(int suffixStart, String pattern, int m) {
        int len = Math.min(n - suffixStart, m);
        for (int i = 0; i < len; i++) {
            char sc = text.charAt(suffixStart + i);
            char pc = pattern.charAt(i);
            if (sc != pc) return sc - pc;
        }
        // If we've compared all of pattern, it's a match (suffix may be longer)
        if (len >= m) return 0;
        // Suffix is shorter than pattern and all chars matched so far → suffix < pattern
        return -1;
    }

    /**
     * Builds the suffix array using the prefix-doubling approach.
     * O(n log² n) with built-in merge sort at each doubling step.
     */
    private int[] build(String s) {
        int len = s.length();
        if (len == 0) return new int[0];

        // Initial ranks based on character values
        int[] rank = new int[len];
        int[] order = new int[len];
        for (int i = 0; i < len; i++) {
            rank[i] = s.charAt(i);
            order[i] = i;
        }

        for (int gap = 1; gap < len; gap *= 2) {
            // Sort by (rank[i], rank[i+gap]) pairs
            final int g = gap;
            final int[] r = rank;
            // Simple merge sort on order array using the rank pairs
            mergeSortByRank(order, r, g, len);

            // Recompute ranks
            int[] newRank = new int[len];
            newRank[order[0]] = 0;
            for (int i = 1; i < len; i++) {
                newRank[order[i]] = newRank[order[i - 1]];
                int ra1 = r[order[i - 1]];
                int ra2 = (order[i - 1] + g < len) ? r[order[i - 1] + g] : -1;
                int rb1 = r[order[i]];
                int rb2 = (order[i] + g < len) ? r[order[i] + g] : -1;
                if (ra1 != rb1 || ra2 != rb2) {
                    newRank[order[i]]++;
                }
            }
            rank = newRank;

            // Early termination: if all ranks are unique
            if (rank[order[len - 1]] == len - 1) break;
        }
        return order;
    }

    /** Merge sort the suffix indices by their (rank[i], rank[i+gap]) pair. */
    private void mergeSortByRank(int[] order, int[] rank, int gap, int len) {
        if (len <= 1) return;
        int[] aux = new int[len];
        mergeSortHelper(order, aux, rank, gap, len, 0, len - 1);
    }

    private void mergeSortHelper(int[] order, int[] aux, int[] rank, int gap, int len, int lo, int hi) {
        if (lo >= hi) return;
        int mid = lo + (hi - lo) / 2;
        mergeSortHelper(order, aux, rank, gap, len, lo, mid);
        mergeSortHelper(order, aux, rank, gap, len, mid + 1, hi);
        // merge
        for (int k = lo; k <= hi; k++) aux[k] = order[k];
        int i = lo, j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid) order[k] = aux[j++];
            else if (j > hi) order[k] = aux[i++];
            else {
                int cmp = compareRankPair(aux[i], aux[j], rank, gap, len);
                if (cmp <= 0) order[k] = aux[i++];
                else order[k] = aux[j++];
            }
        }
    }

    private int compareRankPair(int a, int b, int[] rank, int gap, int len) {
        if (rank[a] != rank[b]) return rank[a] - rank[b];
        int ra = (a + gap < len) ? rank[a + gap] : -1;
        int rb = (b + gap < len) ? rank[b + gap] : -1;
        return ra - rb;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SuffixArray for \"" + text + "\":\n");
        for (int i = 0; i < n; i++) {
            sb.append(String.format("  SA[%2d] = %2d  ->  \"%s\"\n", i, sa[i], text.substring(sa[i])));
        }
        return sb.toString();
    }
}
