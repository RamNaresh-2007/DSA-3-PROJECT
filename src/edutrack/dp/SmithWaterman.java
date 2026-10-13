package edutrack.dp;

/**
 * Smith-Waterman local sequence alignment algorithm.
 *
 * <h3>Algorithm overview</h3>
 * <ul>
 *   <li>Finds the highest-scoring local alignment between two sequences.</li>
 *   <li>Differs from Needleman-Wunsch: dp values cannot go below 0 (allows
 *       free starts and ends).</li>
 *   <li>Backtrack from the maximum cell to the first zero cell.</li>
 *   <li>Time: O(nm), Space: O(nm).</li>
 * </ul>
 *
 * <h3>EduTrack use-case</h3>
 * Document local alignment — finding the best-matching region between two
 * assignment submissions for partial plagiarism detection.
 */
public class SmithWaterman {

    private final int matchScore;
    private final int mismatchPenalty;
    private final int gapPenalty;

    public SmithWaterman() {
        this(2, -1, -1);
    }

    public SmithWaterman(int matchScore, int mismatchPenalty, int gapPenalty) {
        this.matchScore = matchScore;
        this.mismatchPenalty = mismatchPenalty;
        this.gapPenalty = gapPenalty;
    }

    /**
     * Result of a Smith-Waterman local alignment.
     */
    public static class LocalAlignmentResult {
        public final String alignedSeq1;
        public final String alignedSeq2;
        public final int score;
        public final int startInSeq1;
        public final int startInSeq2;

        public LocalAlignmentResult(String a1, String a2, int score, int s1, int s2) {
            this.alignedSeq1 = a1;
            this.alignedSeq2 = a2;
            this.score = score;
            this.startInSeq1 = s1;
            this.startInSeq2 = s2;
        }

        @Override
        public String toString() {
            return "Local Alignment Score: " + score +
                   " | Start in Seq1: " + startInSeq1 +
                   " | Start in Seq2: " + startInSeq2 +
                   "\nSeq1: " + alignedSeq1 +
                   "\nSeq2: " + alignedSeq2;
        }
    }

    /**
     * Performs local alignment of two sequences.
     *
     * @param seq1 first sequence
     * @param seq2 second sequence
     * @return local alignment result
     */
    public LocalAlignmentResult align(String seq1, String seq2) {
        int n = seq1.length(), m = seq2.length();
        int[][] dp = new int[n + 1][m + 1];

        int maxScore = 0, maxI = 0, maxJ = 0;

        // Fill DP table — values clamped to >= 0
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int diag = dp[i - 1][j - 1] +
                        (seq1.charAt(i - 1) == seq2.charAt(j - 1) ? matchScore : mismatchPenalty);
                int up   = dp[i - 1][j] + gapPenalty;
                int left = dp[i][j - 1] + gapPenalty;
                dp[i][j] = Math.max(0, Math.max(diag, Math.max(up, left)));

                if (dp[i][j] > maxScore) {
                    maxScore = dp[i][j];
                    maxI = i;
                    maxJ = j;
                }
            }
        }

        // Backtrack from max cell to a zero cell
        StringBuilder aligned1 = new StringBuilder();
        StringBuilder aligned2 = new StringBuilder();
        int i = maxI, j = maxJ;

        while (i > 0 && j > 0 && dp[i][j] > 0) {
            int diag = dp[i - 1][j - 1] +
                    (seq1.charAt(i - 1) == seq2.charAt(j - 1) ? matchScore : mismatchPenalty);
            if (dp[i][j] == diag) {
                aligned1.append(seq1.charAt(i - 1));
                aligned2.append(seq2.charAt(j - 1));
                i--; j--;
            } else if (dp[i][j] == dp[i - 1][j] + gapPenalty) {
                aligned1.append(seq1.charAt(i - 1));
                aligned2.append('-');
                i--;
            } else {
                aligned1.append('-');
                aligned2.append(seq2.charAt(j - 1));
                j--;
            }
        }

        return new LocalAlignmentResult(
                aligned1.reverse().toString(),
                aligned2.reverse().toString(),
                maxScore, i, j
        );
    }
}
