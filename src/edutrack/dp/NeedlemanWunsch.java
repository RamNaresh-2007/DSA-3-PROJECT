package edutrack.dp;

/**
 * Needleman-Wunsch global sequence alignment algorithm.
 *
 * <h3>Algorithm overview</h3>
 * <ul>
 *   <li>Aligns two sequences end-to-end (global alignment).</li>
 *   <li>Uses a scoring scheme: match (+1 default), mismatch (-1), gap penalty (-2).</li>
 *   <li>dp[i][j] = best alignment score for s1[0..i-1] vs s2[0..j-1].</li>
 *   <li>Backtrack to produce the aligned sequences with gap characters.</li>
 *   <li>Time: O(nm), Space: O(nm).</li>
 * </ul>
 *
 * <h3>EduTrack use-case</h3>
 * Document global alignment — comparing two versions of academic documents
 * to identify overall structural similarity and differences.
 */
public class NeedlemanWunsch {

    private final int matchScore;
    private final int mismatchPenalty;
    private final int gapPenalty;

    public NeedlemanWunsch() {
        this(1, -1, -2);
    }

    public NeedlemanWunsch(int matchScore, int mismatchPenalty, int gapPenalty) {
        this.matchScore = matchScore;
        this.mismatchPenalty = mismatchPenalty;
        this.gapPenalty = gapPenalty;
    }

    /**
     * Result of a Needleman-Wunsch alignment.
     */
    public static class AlignmentResult {
        public final String alignedSeq1;
        public final String alignedSeq2;
        public final int score;
        public final int matches;
        public final int mismatches;
        public final int gaps;

        public AlignmentResult(String a1, String a2, int score, int matches, int mismatches, int gaps) {
            this.alignedSeq1 = a1;
            this.alignedSeq2 = a2;
            this.score = score;
            this.matches = matches;
            this.mismatches = mismatches;
            this.gaps = gaps;
        }

        @Override
        public String toString() {
            return "Score: " + score + " | Matches: " + matches +
                   " | Mismatches: " + mismatches + " | Gaps: " + gaps +
                   "\nSeq1: " + alignedSeq1 +
                   "\nSeq2: " + alignedSeq2;
        }
    }

    /**
     * Performs global alignment of two sequences.
     *
     * @param seq1 first sequence
     * @param seq2 second sequence
     * @return alignment result with aligned strings and statistics
     */
    public AlignmentResult align(String seq1, String seq2) {
        int n = seq1.length(), m = seq2.length();
        int[][] dp = new int[n + 1][m + 1];

        // Initialise base cases
        for (int i = 0; i <= n; i++) dp[i][0] = i * gapPenalty;
        for (int j = 0; j <= m; j++) dp[0][j] = j * gapPenalty;

        // Fill DP table
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int scoreDiag = dp[i - 1][j - 1] +
                        (seq1.charAt(i - 1) == seq2.charAt(j - 1) ? matchScore : mismatchPenalty);
                int scoreUp   = dp[i - 1][j] + gapPenalty;
                int scoreLeft = dp[i][j - 1] + gapPenalty;
                dp[i][j] = Math.max(scoreDiag, Math.max(scoreUp, scoreLeft));
            }
        }

        // Backtrack
        StringBuilder aligned1 = new StringBuilder();
        StringBuilder aligned2 = new StringBuilder();
        int i = n, j = m;
        int matches = 0, mismatches = 0, gaps = 0;

        while (i > 0 || j > 0) {
            if (i > 0 && j > 0) {
                int scoreDiag = dp[i - 1][j - 1] +
                        (seq1.charAt(i - 1) == seq2.charAt(j - 1) ? matchScore : mismatchPenalty);
                if (dp[i][j] == scoreDiag) {
                    aligned1.append(seq1.charAt(i - 1));
                    aligned2.append(seq2.charAt(j - 1));
                    if (seq1.charAt(i - 1) == seq2.charAt(j - 1)) matches++;
                    else mismatches++;
                    i--; j--;
                    continue;
                }
            }
            if (i > 0 && dp[i][j] == dp[i - 1][j] + gapPenalty) {
                aligned1.append(seq1.charAt(i - 1));
                aligned2.append('-');
                gaps++;
                i--;
            } else {
                aligned1.append('-');
                aligned2.append(seq2.charAt(j - 1));
                gaps++;
                j--;
            }
        }

        return new AlignmentResult(
                aligned1.reverse().toString(),
                aligned2.reverse().toString(),
                dp[n][m], matches, mismatches, gaps
        );
    }
}
