package edutrack.suffix;

import edutrack.core.CustomArrayList;

/**
 * Kasai's algorithm for computing the LCP (Longest Common Prefix) array
 * from a suffix array in O(n) time.
 *
 * <h3>Algorithm overview</h3>
 * <ul>
 *   <li>Given suffix array SA and its inverse (rank array), the LCP array
 *       stores the length of the longest common prefix between consecutive
 *       suffixes in sorted order: LCP[i] = lcp(S[SA[i]..], S[SA[i-1]..]).</li>
 *   <li>Key insight (Kasai): if lcp(SA[rank[i]-1], SA[rank[i]]) = h, then
 *       lcp(SA[rank[i+1]-1], SA[rank[i+1]]) ≥ h - 1.</li>
 *   <li>This monotone property yields O(n) total comparisons.</li>
 * </ul>
 *
 * <h3>EduTrack use-case</h3>
 * Identifies repeated phrases across assignments for plagiarism detection:
 * high LCP values between suffixes from different documents indicate shared text.
 */
public class KasaiLCP {

    /**
     * Computes the LCP array using Kasai's algorithm.
     *
     * @param text the original text
     * @param sa   the suffix array for text
     * @return the LCP array of length n (LCP[0] is undefined, set to 0)
     */
    public static int[] computeLCP(String text, int[] sa) {
        int n = text.length();
        int[] lcp = new int[n];
        int[] rank = new int[n]; // inverse suffix array

        // Build rank (inverse of SA)
        for (int i = 0; i < n; i++) {
            rank[sa[i]] = i;
        }

        int h = 0; // current LCP length
        for (int i = 0; i < n; i++) {
            if (rank[i] > 0) {
                int j = sa[rank[i] - 1]; // previous suffix in sorted order
                // Compare characters while they match
                while (i + h < n && j + h < n && text.charAt(i + h) == text.charAt(j + h)) {
                    h++;
                }
                lcp[rank[i]] = h;
                if (h > 0) h--; // Kasai's property: next LCP >= h - 1
            } else {
                h = 0;
            }
        }
        return lcp;
    }

    /**
     * Finds all pairs of suffixes with LCP >= minLength.
     * Useful for detecting repeated phrases.
     *
     * @param text      the text
     * @param sa        the suffix array
     * @param minLength minimum LCP to report
     * @return list of {position1, position2, lcpLength}
     */
    public static CustomArrayList<int[]> findRepeatedPhrases(String text, int[] sa, int minLength) {
        int[] lcp = computeLCP(text, sa);
        CustomArrayList<int[]> result = new CustomArrayList<>();

        for (int i = 1; i < lcp.length; i++) {
            if (lcp[i] >= minLength) {
                result.add(new int[]{sa[i - 1], sa[i], lcp[i]});
            }
        }
        return result;
    }

    /**
     * Finds the longest repeated substring in the text using the LCP array.
     *
     * @param text the text
     * @param sa   the suffix array
     * @return the longest repeated substring, or "" if none
     */
    public static String longestRepeatedSubstring(String text, int[] sa) {
        int[] lcp = computeLCP(text, sa);
        int maxLCP = 0;
        int maxIdx = -1;

        for (int i = 1; i < lcp.length; i++) {
            if (lcp[i] > maxLCP) {
                maxLCP = lcp[i];
                maxIdx = i;
            }
        }

        if (maxIdx == -1 || maxLCP == 0) return "";
        return text.substring(sa[maxIdx], sa[maxIdx] + maxLCP);
    }

    /**
     * Finds common substrings between two texts by concatenating them
     * with a separator and analysing the LCP array.
     *
     * @param text1     first document
     * @param text2     second document
     * @param minLength minimum length of common substring to report
     * @return list of common substrings
     */
    public static CustomArrayList<String> findCommonSubstrings(String text1, String text2, int minLength) {
        // Concatenate with a sentinel that appears in neither
        char separator = '\u0001';
        String combined = text1 + separator + text2;
        int boundary = text1.length();

        SuffixArray sa = new SuffixArray(combined);
        int[] saArr = sa.getSuffixArray();
        int[] lcp = computeLCP(combined, saArr);

        CustomArrayList<String> result = new CustomArrayList<>();
        // Track already-seen substrings to avoid duplicates
        for (int i = 1; i < lcp.length; i++) {
            if (lcp[i] >= minLength) {
                // Check that the two suffixes come from different documents
                boolean first1 = saArr[i - 1] < boundary;
                boolean first2 = saArr[i] < boundary;
                if (first1 != first2) {
                    String common = combined.substring(saArr[i], saArr[i] + lcp[i]);
                    if (!common.contains("" + separator)) {
                        result.add(common);
                    }
                }
            }
        }
        return result;
    }
}
