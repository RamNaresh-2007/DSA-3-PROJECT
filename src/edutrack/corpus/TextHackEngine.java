package edutrack.corpus;

import edutrack.core.CustomArrayList;
import edutrack.dp.Levenshtein;
import edutrack.flow.*;
import edutrack.approx.VertexCoverApproximation;
import edutrack.randomparallel.MillerRabin;
import edutrack.string.KMP;
import edutrack.suffix.KasaiLCP;

/**
 * TextHack Engine — The Advanced-Algorithm Question Bank & Text-Analytics Suite (Module 1).
 *
 * <h3>Day-One Public API</h3>
 * <p>Students issue queries against the Indian-language Wikipedia corpus and observe
 * real results before exploring internals. Each query class maps to an advanced-algorithm family:</p>
 *
 * <table border="1">
 *   <tr><th>Query Class</th><th>Algorithm Family</th><th>Core Algorithm</th><th>Asymptotic Cost</th><th>Canon Chapter</th></tr>
 *   <tr><td>1. Pattern Search</td><td>String Algorithms</td><td>Knuth-Morris-Pratt (KMP)</td><td>O(n + m)</td><td>CLRS Ch 32, Erickson Ch 8</td></tr>
 *   <tr><td>2. Fuzzy Matching</td><td>Dynamic Programming</td><td>Wagner-Fischer Levenshtein</td><td>O(nm)</td><td>KT Ch 6, Erickson Ch 3</td></tr>
 *   <tr><td>3. Document Similarity</td><td>Suffix Structures</td><td>Kasai LCP / Suffix Array</td><td>O(n)</td><td>CLRS Ch 32, Gusfield Ch 7</td></tr>
 *   <tr><td>4. Citation Flow</td><td>Network Flow</td><td>Dinic / Edmonds-Karp Max-Flow</td><td>O(V^2 E) / O(VE^2)</td><td>CLRS Ch 26, KT Ch 7</td></tr>
 *   <tr><td>5. Project Scheduling</td><td>NP-Hard Approximation</td><td>Vertex Cover 2-Approx & FPT</td><td>O(V + E) / O(2^k k^2 + n)</td><td>CLRS Ch 34-35, KT Ch 8-11</td></tr>
 *   <tr><td>6. Primality Testing</td><td>Randomised Algorithms</td><td>Miller-Rabin Test</td><td>O(k log^3 n)</td><td>CLRS Ch 31, KT Ch 13</td></tr>
 * </table>
 *
 * <p>Strictly conforms to zero {@code java.util.*} imports.</p>
 */
public class TextHackEngine {

    private final CustomArrayList<IndicCorpus.IndicArticle> corpus;

    public TextHackEngine() {
        this.corpus = IndicCorpus.getArticles();
    }

    public CustomArrayList<IndicCorpus.IndicArticle> getCorpus() {
        return corpus;
    }

    public static class TextHackQueryResult {
        public final String queryClass;
        public final String algorithmFamily;
        public final String algorithmUsed;
        public final String asymptoticCost;
        public final String textbookCanonReference;
        public final CustomArrayList<String> answers;
        public final long executionNs;

        public TextHackQueryResult(String queryClass, String algorithmFamily, String algorithmUsed,
                                   String asymptoticCost, String textbookCanonReference,
                                   CustomArrayList<String> answers, long executionNs) {
            this.queryClass = queryClass;
            this.algorithmFamily = algorithmFamily;
            this.algorithmUsed = algorithmUsed;
            this.asymptoticCost = asymptoticCost;
            this.textbookCanonReference = textbookCanonReference;
            this.answers = answers;
            this.executionNs = executionNs;
        }
    }

    /** 1. Pattern Search (String Algorithms) -> KMP */
    public TextHackQueryResult queryPatternSearch(String pattern) {
        long t0 = System.nanoTime();
        CustomArrayList<String> hits = new CustomArrayList<>();

        for (int i = 0; i < corpus.size(); i++) {
            IndicCorpus.IndicArticle art = corpus.get(i);
            if (KMP.contains(art.content, pattern) || KMP.contains(art.title, pattern) || KMP.contains(art.englishSummary, pattern)) {
                hits.add("[" + art.language + "] " + art.title + " -> Found pattern '" + pattern + "'");
            }
        }

        long elapsed = System.nanoTime() - t0;
        return new TextHackQueryResult("Pattern Search", "String Algorithms", "Knuth-Morris-Pratt (KMP)",
                "O(n + m)", "CLRS Part VII Ch 32; Erickson Ch 8", hits, elapsed);
    }

    /** 2. Fuzzy Matching (Dynamic Programming) -> Levenshtein Edit Distance */
    public TextHackQueryResult queryFuzzyMatch(String query, int maxDist) {
        long t0 = System.nanoTime();
        CustomArrayList<String> hits = new CustomArrayList<>();
        String q = query.toLowerCase().trim();

        for (int i = 0; i < corpus.size(); i++) {
            IndicCorpus.IndicArticle art = corpus.get(i);
            String titleLower = art.title.toLowerCase();
            int bestDist = Levenshtein.distance(q, titleLower);

            // Also check individual tokens/words (e.g. English transliteration or Indic words)
            String[] tokens = titleLower.split("[\\s(),.।]+");
            for (String tok : tokens) {
                if (!tok.isEmpty()) {
                    int d = Levenshtein.distance(q, tok);
                    if (d < bestDist) bestDist = d;
                }
            }

            if (bestDist <= maxDist) {
                hits.add(art.title + " (" + art.language + ") [Edit Distance: " + bestDist + "]");
            }
        }

        long elapsed = System.nanoTime() - t0;
        return new TextHackQueryResult("Fuzzy Matching with Edit Distance", "Dynamic Programming", "Wagner-Fischer Levenshtein",
                "O(n * m)", "Kleinberg-Tardos Ch 6.6; Erickson Ch 3", hits, elapsed);
    }

    /** 3. Document Similarity (Suffix Structures) -> Kasai LCP */
    public TextHackQueryResult queryDocumentSimilarity(String docId1, String docId2, int minLength) {
        long t0 = System.nanoTime();
        CustomArrayList<String> matches = new CustomArrayList<>();

        IndicCorpus.IndicArticle a1 = findArticle(docId1);
        IndicCorpus.IndicArticle a2 = findArticle(docId2);

        if (a1 != null && a2 != null) {
            CustomArrayList<String> common = KasaiLCP.findCommonSubstrings(a1.content, a2.content, minLength);
            for (int i = 0; i < common.size(); i++) {
                matches.add("Shared substring: \"" + common.get(i) + "\"");
            }
        }

        long elapsed = System.nanoTime() - t0;
        return new TextHackQueryResult("Document Similarity", "Suffix Structures", "Suffix Array & Kasai LCP",
                "O(n) Suffix Array + Kasai", "CLRS Part VII Ch 32; Gusfield Ch 7", matches, elapsed);
    }

    /** 4. Citation-Flow Analysis (Network Flow) -> Dinic / Min-Cut */
    public TextHackQueryResult queryCitationFlow(int[][] citationGraph, int numPapers, int sourcePaper, int targetPaper) {
        long t0 = System.nanoTime();
        CustomArrayList<String> answers = new CustomArrayList<>();

        FlowNetwork fn = new FlowNetwork(numPapers);
        for (int[] edge : citationGraph) {
            fn.addEdge(edge[0], edge[1], edge[2]);
        }

        Dinic.DinicResult dinic = Dinic.solve(fn, sourcePaper, targetPaper);
        MinCut.CutResult cut = MinCut.computeMinCut(fn, sourcePaper, targetPaper);

        answers.add("Maximum citation transfer throughput: " + dinic.maxFlow + " papers/sec");
        answers.add("Min-Cut bottleneck capacity: " + cut.minCutCapacity + " (Duality verified: " + cut.dualityVerified + ")");
        answers.add("Core influence group S size: " + cut.setS.size() + ", Peripheral group T size: " + cut.setT.size());

        long elapsed = System.nanoTime() - t0;
        return new TextHackQueryResult("Citation-Flow Analysis", "Network Flow", "Dinic's Blocking Flow & Min-Cut",
                "O(V^2 E)", "CLRS Ch 26; Kleinberg-Tardos Ch 7", answers, elapsed);
    }

    /** 5. Project Scheduling (NP-Hard with Approximation) -> 2-Approx Vertex Cover */
    public TextHackQueryResult queryProjectScheduling(int numTasks, int[][] conflictEdges) {
        long t0 = System.nanoTime();
        CustomArrayList<String> answers = new CustomArrayList<>();

        VertexCoverApproximation.ApproxResult res = VertexCoverApproximation.solve(numTasks, conflictEdges);
        answers.add("Minimum supervisor checkpoints (2-Approx): " + res.approxSize + " checkpoints");
        answers.add("Maximal matching basis size: " + res.maximalMatching.size() + " conflict pairs");
        answers.add("Guaranteed approximation ratio: <= " + res.theoreticalGuarantee);
        if (res.exactOptSize > 0) {
            answers.add("Exact OPT: " + res.exactOptSize + " (Empirical ratio: " + String.format("%.2f", res.empiricalRatio) + ")");
        }

        long elapsed = System.nanoTime() - t0;
        return new TextHackQueryResult("Project Scheduling Demonstration", "NP-Hard & Approximation", "Vertex Cover 2-Approximation via Maximal Matching",
                "O(V + E) 2-Approximation", "CLRS Ch 34-35; Kleinberg-Tardos Ch 8-11", answers, elapsed);
    }

    /** 6. Large-Prime Primality Testing (Randomised Algorithms) -> Miller-Rabin */
    public TextHackQueryResult queryLargePrimePrimality(long candidate) {
        long t0 = System.nanoTime();
        CustomArrayList<String> answers = new CustomArrayList<>();

        MillerRabin.PrimalityResult res = MillerRabin.testProbabilistic(candidate, 12);
        answers.add("Candidate number: " + candidate);
        answers.add("Classification: " + (res.isPrime ? "PRIME (Probabilistic confidence > 99.9999%)" : "COMPOSITE (Definite)"));
        if (res.witness != null) {
            answers.add("Miller-Rabin witness detected: base a=" + res.witness);
        }
        for (int i = 0; i < res.roundDetails.size(); i++) {
            answers.add(res.roundDetails.get(i));
        }

        long elapsed = System.nanoTime() - t0;
        return new TextHackQueryResult("Large-Prime Primality Testing", "Randomised Algorithms", "Miller-Rabin Monte Carlo Test",
                "O(k log^3 n)", "CLRS Ch 31; Kleinberg-Tardos Ch 13", answers, elapsed);
    }

    private IndicCorpus.IndicArticle findArticle(String id) {
        for (int i = 0; i < corpus.size(); i++) {
            if (corpus.get(i).id.equalsIgnoreCase(id)) return corpus.get(i);
        }
        return null;
    }
}
