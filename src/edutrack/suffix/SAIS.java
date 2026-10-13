package edutrack.suffix;

/**
 * SA-IS (Suffix Array by Induced Sorting) — linear-time O(n) suffix array construction.
 *
 * <h3>Algorithm overview</h3>
 * <ul>
 *   <li>Classifies each suffix as S-type or L-type based on lexicographic comparison
 *       with the next suffix.</li>
 *   <li>Identifies LMS (Left-Most S-type) suffixes — the boundary positions.</li>
 *   <li>Recursively sorts LMS suffixes, then induces the full suffix array.</li>
 *   <li>Total time: O(n), total space: O(n).</li>
 * </ul>
 *
 * <h3>EduTrack use-case</h3>
 * Efficient large-document indexing — when the O(n log² n) approach is too slow
 * for very large textbooks or reference materials.
 */
public class SAIS {

    private final int[] suffixArray;

    public SAIS(String text) {
        this.suffixArray = buildSuffixArray(text);
    }

    public int[] getSuffixArray() {
        return suffixArray;
    }

    /**
     * Builds the suffix array for the given string in O(n) time.
     *
     * @param text the input string
     * @return the suffix array
     */
    public static int[] buildSuffixArray(String text) {
        if (text == null || text.isEmpty()) return new int[0];

        int n = text.length();
        // Convert to integer array (with sentinel 0)
        int[] s = new int[n + 1];
        for (int i = 0; i < n; i++) s[i] = text.charAt(i) + 1; // shift so sentinel is smallest
        s[n] = 0; // sentinel

        int alphabetSize = 256 + 2; // ASCII + shift + sentinel
        int[] sa = new int[n + 1];
        sais(s, sa, n + 1, alphabetSize);

        // Remove sentinel (first entry of sa is always the sentinel)
        int[] result = new int[n];
        for (int i = 0; i < n; i++) result[i] = sa[i + 1];
        return result;
    }

    /**
     * Core SA-IS algorithm operating on integer arrays.
     * Recursive; each level reduces the problem to ~n/2 LMS suffixes.
     */
    private static void sais(int[] s, int[] sa, int n, int alphabetSize) {
        // Step 1: Classify each position as S-type or L-type
        boolean[] isS = new boolean[n];
        isS[n - 1] = true; // sentinel is S-type
        for (int i = n - 2; i >= 0; i--) {
            if (s[i] < s[i + 1]) isS[i] = true;
            else if (s[i] > s[i + 1]) isS[i] = false;
            else isS[i] = isS[i + 1];
        }

        // Step 2: Find bucket boundaries
        int[] bucketSizes = new int[alphabetSize];
        for (int i = 0; i < n; i++) bucketSizes[s[i]]++;

        int[] bucketHeads = new int[alphabetSize];
        int[] bucketTails = new int[alphabetSize];
        getBuckets(bucketSizes, bucketHeads, bucketTails, alphabetSize);

        // Step 3: Place LMS suffixes into their buckets (at the tail)
        for (int i = 0; i < n; i++) sa[i] = -1;
        for (int i = 1; i < n; i++) {
            if (isS[i] && !isS[i - 1]) { // LMS position
                sa[bucketTails[s[i]]--] = i;
            }
        }

        // Step 4: Induce L-type suffixes
        getBuckets(bucketSizes, bucketHeads, bucketTails, alphabetSize);
        for (int i = 0; i < n; i++) {
            if (sa[i] > 0 && !isS[sa[i] - 1]) {
                sa[bucketHeads[s[sa[i] - 1]]++] = sa[i] - 1;
            }
        }

        // Step 5: Induce S-type suffixes
        getBuckets(bucketSizes, bucketHeads, bucketTails, alphabetSize);
        for (int i = n - 1; i >= 0; i--) {
            if (sa[i] > 0 && isS[sa[i] - 1]) {
                sa[bucketTails[s[sa[i] - 1]]--] = sa[i] - 1;
            }
        }

        // Step 6: Compact LMS suffixes and name them
        int lmsCount = 0;
        for (int i = 1; i < n; i++) {
            if (isS[i] && !isS[i - 1]) lmsCount++;
        }

        if (lmsCount > 0) {
            // Collect sorted LMS suffixes
            int[] sortedLMS = new int[lmsCount];
            int idx = 0;
            for (int i = 0; i < n; i++) {
                if (sa[i] > 0 && isS[sa[i]] && !isS[sa[i] - 1]) {
                    sortedLMS[idx++] = sa[i];
                }
                if (sa[i] == 0) {
                    // sentinel: LMS by convention only if n > 1
                }
            }

            // Name each LMS substring
            int[] names = new int[n];
            for (int i = 0; i < n; i++) names[i] = -1;
            int nameCount = 0;
            int prev = -1;
            for (int i = 0; i < lmsCount; i++) {
                int pos = sortedLMS[i];
                if (prev == -1 || !lmsEqual(s, isS, prev, pos, n)) {
                    nameCount++;
                }
                names[pos] = nameCount - 1;
                prev = pos;
            }

            // If names are not unique, recurse
            if (nameCount < lmsCount) {
                int[] reduced = new int[lmsCount + 1];
                int[] lmsPositions = new int[lmsCount];
                int j = 0;
                for (int i = 0; i < n; i++) {
                    if (names[i] >= 0) {
                        reduced[j] = names[i];
                        lmsPositions[j] = i;
                        j++;
                    }
                }
                reduced[lmsCount] = 0; // sentinel for reduced string

                int[] reducedSA = new int[lmsCount + 1];
                sais(reduced, reducedSA, lmsCount + 1, nameCount + 1);

                // Use recursive result to sort LMS suffixes correctly
                for (int i = 0; i < n; i++) sa[i] = -1;
                getBuckets(bucketSizes, bucketHeads, bucketTails, alphabetSize);

                for (int i = lmsCount; i >= 1; i--) {
                    int lmsIdx = lmsPositions[reducedSA[i]];
                    sa[bucketTails[s[lmsIdx]]--] = lmsIdx;
                }

                // Re-induce
                getBuckets(bucketSizes, bucketHeads, bucketTails, alphabetSize);
                for (int i = 0; i < n; i++) {
                    if (sa[i] > 0 && !isS[sa[i] - 1]) {
                        sa[bucketHeads[s[sa[i] - 1]]++] = sa[i] - 1;
                    }
                }

                getBuckets(bucketSizes, bucketHeads, bucketTails, alphabetSize);
                for (int i = n - 1; i >= 0; i--) {
                    if (sa[i] > 0 && isS[sa[i] - 1]) {
                        sa[bucketTails[s[sa[i] - 1]]--] = sa[i] - 1;
                    }
                }
            }
        }
    }

    /** Checks if two LMS substrings are equal. */
    private static boolean lmsEqual(int[] s, boolean[] isS, int a, int b, int n) {
        int i = 0;
        while (true) {
            boolean aLMS = (i > 0) && isS[a + i] && (a + i == 0 || !isS[a + i - 1]);
            boolean bLMS = (i > 0) && isS[b + i] && (b + i == 0 || !isS[b + i - 1]);
            if (aLMS && bLMS) return true;
            if (aLMS != bLMS) return false;
            if (s[a + i] != s[b + i]) return false;
            i++;
            if (a + i >= n || b + i >= n) return false;
        }
    }

    /** Computes bucket head and tail pointers from bucket sizes. */
    private static void getBuckets(int[] sizes, int[] heads, int[] tails, int alphabetSize) {
        int sum = 0;
        for (int i = 0; i < alphabetSize; i++) {
            heads[i] = sum;
            sum += sizes[i];
            tails[i] = sum - 1;
        }
    }
}
