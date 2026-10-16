package edutrack.engine;

import edutrack.core.CustomArrayList;
import edutrack.core.CustomHashMap;
import edutrack.model.*;
import edutrack.string.*;
import edutrack.suffix.*;
import edutrack.dp.*;
import edutrack.flow.*;
import edutrack.approx.*;
import edutrack.randomparallel.*;
import edutrack.corpus.*;

/**
 * EduTrack Engine — the central text-analytics and academic-data engine.
 *
 * <p>This engine ties together every algorithm from Modules 1-6 with the
 * university data model and Indic Wikipedia question bank. No {@code java.util.*}
 * imports are used inside the engine — only the hand-built data structures from {@code edutrack.core}.</p>
 *
 * <h3>Query families supported across all 6 Modules</h3>
 * <ul>
 *   <li><b>Module 1 (TextHack Indic Corpus):</b> Indian-Language Wikipedia Question Bank</li>
 *   <li><b>Module 2 (String & Suffix):</b> KMP, Z-Function, Rabin-Karp, Aho-Corasick, SA-IS, Kasai LCP, Suffix Tree/Automaton</li>
 *   <li><b>Module 3 (Advanced DP):</b> Levenshtein, Damerau, Needleman-Wunsch, Smith-Waterman, MCM, Optimal BST, Bitmask TSP, Tree DP, SOS DP</li>
 *   <li><b>Module 4 (Network Flow):</b> Ford-Fulkerson, Edmonds-Karp, Dinic, Min-Cut Duality, Bipartite Matching, Min-Cost Flow, Reductions</li>
 *   <li><b>Module 5 (NP-Completeness & Approx):</b> Reduction Zoo, Vertex Cover 2-Approx, Knapsack FPTAS, FPT Buss Kernel, NP Advisor</li>
 *   <li><b>Module 6 (Randomised & Parallel):</b> Miller-Rabin, Randomized QuickSort, Universal/Perfect Hashing, Reservoir Sampling, Blelloch Scan</li>
 * </ul>
 */
public class EduTrackEngine {

    // ── Data stores ─────────────────────────────────────────────────────
    private final CustomArrayList<Student> students;
    private final CustomArrayList<Faculty> faculty;
    private final CustomArrayList<Admin> admins;
    private final CustomArrayList<Course> courses;
    private final CustomArrayList<Assignment> assignments;
    private final CustomArrayList<Document> documents;
    private final CustomArrayList<Schedule> schedules;

    // ── Index caches (built lazily) ─────────────────────────────────────
    private CustomHashMap<String, SuffixArray> documentSuffixArrays;
    private CustomHashMap<String, SuffixTree> documentSuffixTrees;
    private CustomHashMap<String, SuffixAutomaton> documentSuffixAutomata;
    private final TextHackEngine textHackEngine;

    public EduTrackEngine() {
        students    = new CustomArrayList<>();
        faculty     = new CustomArrayList<>();
        admins      = new CustomArrayList<>();
        courses     = new CustomArrayList<>();
        assignments = new CustomArrayList<>();
        documents   = new CustomArrayList<>();
        schedules   = new CustomArrayList<>();

        documentSuffixArrays   = new CustomHashMap<>();
        documentSuffixTrees    = new CustomHashMap<>();
        documentSuffixAutomata = new CustomHashMap<>();
        textHackEngine         = new TextHackEngine();
    }

    public TextHackEngine getTextHackEngine() {
        return textHackEngine;
    }

    // ====================================================================
    //  DATA MANAGEMENT — add / retrieve records
    // ====================================================================

    public void addStudent(Student s)       { students.add(s); }
    public void addFaculty(Faculty f)       { faculty.add(f); }
    public void addAdmin(Admin a)           { admins.add(a); }
    public void addCourse(Course c)         { courses.add(c); }
    public void addAssignment(Assignment a) { assignments.add(a); }
    public void addSchedule(Schedule s)     { schedules.add(s); }

    /** Adds a document and eagerly builds its suffix structures for fast queries. */
    public void addDocument(Document d) {
        documents.add(d);
        String content = d.getContent();
        if (content != null && !content.isEmpty()) {
            documentSuffixArrays.put(d.getDocumentId(), new SuffixArray(content));
            documentSuffixTrees.put(d.getDocumentId(), new SuffixTree(content));
            documentSuffixAutomata.put(d.getDocumentId(), new SuffixAutomaton(content));
        }
    }

    public CustomArrayList<Student>    getStudents()    { return students; }
    public CustomArrayList<Faculty>    getFaculty()     { return faculty; }
    public CustomArrayList<Admin>      getAdmins()      { return admins; }
    public CustomArrayList<Course>     getCourses()     { return courses; }
    public CustomArrayList<Assignment> getAssignments() { return assignments; }
    public CustomArrayList<Document>   getDocuments()   { return documents; }
    public CustomArrayList<Schedule>   getSchedules()   { return schedules; }

    public Admin findAdmin(String id) {
        for (int i = 0; i < admins.size(); i++) {
            if (admins.get(i).getAdminId().equalsIgnoreCase(id)) return admins.get(i);
        }
        return null;
    }

    public Student findStudent(String id) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getStudentId().equalsIgnoreCase(id)) return students.get(i);
        }
        return null;
    }

    public Course findCourse(String code) {
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).getCourseCode().equalsIgnoreCase(code) ||
                (courses.get(i).getCourseId() != null && courses.get(i).getCourseId().equalsIgnoreCase(code))) {
                return courses.get(i);
            }
        }
        return null;
    }

    public Faculty findFaculty(String id) {
        for (int i = 0; i < faculty.size(); i++) {
            if (faculty.get(i).getFacultyId().equalsIgnoreCase(id)) return faculty.get(i);
        }
        return null;
    }

    // --- Convenience Service Aliases ---
    public QueryResult searchCoursesByKeyword(String keyword) { return searchCoursesByKMP(keyword); }
    public QueryResult searchByCode(String code)              { return searchAssignmentsByCode(code); }
    public QueryResult searchDocumentSubstring(String sub)   {
        String docId = documents.isEmpty() ? "" : documents.get(0).getDocumentId();
        return searchDocumentSubstring(docId, sub);
    }
    public QueryResult fuzzySearchCourse(String query, int maxDist) { return fuzzyMatchLevenshtein(query, maxDist); }
    public QueryResult fuzzySearchWithTranspositions(String query, int maxDist) { return fuzzyMatchDamerau(query, maxDist); }
    public QueryResult optimizeMatrixChain(int[] dims, String[] names) { return matrixChainOptimization(dims); }
    public QueryResult buildOptimalBST(String[] keys, double[] freqs)  { return optimalKeyLookup(keys, freqs); }
    public QueryResult buildOptimalLookup(String[] keys, double[] freqs) { return optimalKeyLookup(keys, freqs); }
    public QueryResult findOptimalCourseCombination(String[] names, int[][] matrix) { return courseCombinationTSP(matrix, names); }
    public QueryResult analyzePrerequisiteTree(String[] names, int[][] edges) { return prerequisiteAnalysis(names.length, edges); }
    public QueryResult analyzeEnrollments(String[] names, int[] enrollments)  { return enrollmentAnalytics(enrollments, names.length, names); }


    // ====================================================================
    //  MODULE 2 — STRING ALGORITHMS  (CO1)
      public QueryResult searchCoursesByKMP(String keyword) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        for (int i = 0; i < courses.size(); i++) {
            Course c = courses.get(i);
            if (KMP.contains(c.getCourseName().toLowerCase(), keyword.toLowerCase())) {
                results.add(c.getCourseCode() + ": " + c.getCourseName() + " [KMP]");
            }
        }

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.PATTERN_SEARCH_KMP, keyword, results, elapsed,
                "Found " + results.size() + " course catalog item(s) matching '" + keyword + "' [KMP]");
    }

    /**
     * Z-Function repeated-phrase detection in a single assignment.
     */
    public QueryResult detectRepeatedPhrases(String assignmentId, int minLength) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        Assignment target = findAssignment(assignmentId);
        if (target != null) {
            CustomArrayList<int[]> reps = ZFunction.findRepeatedPhrases(target.getContent(), minLength);
            for (int i = 0; i < reps.size(); i++) {
                int[] r = reps.get(i);
                String phrase = target.getContent().substring(r[0], r[0] + r[1]);
                results.add("Repeated sentence at character " + r[0] + " (" + r[1] + " characters long): \"" + truncate(phrase, 60) + "\" [Z-Function]");
            }
        }

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.REPEATED_PHRASE_DETECTION,
                "assignmentId=" + assignmentId + ", minLen=" + minLength,
                results, elapsed,
                "Detected " + results.size() + " repeated phrase(s) of length >= " + minLength + " characters [Z-Function]");
    }

    /**
     * Rabin-Karp search for a course code pattern across all assignments.
     */
    public QueryResult searchAssignmentsByCode(String codePattern) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        for (int i = 0; i < assignments.size(); i++) {
            Assignment a = assignments.get(i);
            CustomArrayList<Integer> hits = RabinKarp.search(a.getContent(), codePattern);
            if (!hits.isEmpty()) {
                results.add(a.getTitle() + " (ID: " + a.getAssignmentId() + ", Course: " + a.getCourseCode() + ") - found " + hits.size() + " time(s) [Rabin-Karp]");
            }
        }

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.CODE_SEARCH_RABIN_KARP, codePattern, results, elapsed,
                "Found code pattern '" + codePattern + "' in " + results.size() + " assignment(s) [Rabin-Karp]");
    }

    /**
     * Aho-Corasick simultaneous multi-keyword search across all documents.
     */
    public QueryResult multiKeywordSearch(String[] keywords) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();
        AhoCorasick ac = new AhoCorasick(keywords);

        for (int i = 0; i < documents.size(); i++) {
            Document d = documents.get(i);
            CustomArrayList<int[]> matches = ac.search(d.getContent());
            if (!matches.isEmpty()) {
                results.add(d.getTitle() + " - " + matches.size() + " syllabus term hit(s) [Aho-Corasick]");
            }
        }

        long elapsed = System.nanoTime() - t0;
        String kw = join(keywords, ", ");
        return new QueryResult(QueryType.MULTI_KEYWORD_SEARCH, kw, results, elapsed,
                "Scanned " + documents.size() + " document(s) for " + keywords.length + " syllabus term(s) [Aho-Corasick]");
    }

    // ====================================================================
    //  MODULE 2 — SUFFIX STRUCTURES  (CO2)
    // ====================================================================

    /**
     * Suffix-Array-based substring search inside a specific document.
     */
    public QueryResult searchDocumentSubstring(String documentId, String pattern) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        SuffixArray sa = documentSuffixArrays.get(documentId);
        if (sa != null) {
            CustomArrayList<Integer> positions = sa.search(pattern);
            for (int i = 0; i < positions.size(); i++) {
                results.add("Matched at text character position " + positions.get(i) + " [Suffix Array]");
            }
        }

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.DOCUMENT_SUBSTRING_SEARCH,
                pattern + " in doc:" + documentId, results, elapsed,
                "Found '" + pattern + "' at " + results.size() + " position(s) [Suffix Array]");
    }

    /**
     * SA-IS large-document indexing demonstration.
     * Builds the suffix array using the SA-IS O(n) algorithm and reports its size.
     */
    public QueryResult indexDocumentSAIS(String documentId) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        Document doc = findDocument(documentId);
        if (doc != null) {
            SAIS sais = new SAIS(doc.getContent());
            int[] sa = sais.getSuffixArray();
            results.add("Document '" + doc.getTitle() + "' indexed: " + sa.length + " sorted index entries [SA-IS]");
        }

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.LARGE_DOCUMENT_INDEX,
                "doc:" + documentId, results, elapsed,
                "Created fast search index for document [SA-IS]");
    }

    /**
     * Kasai LCP cross-document similarity: finds common substrings between two documents.
     */
    public QueryResult crossDocumentSimilarity(String docId1, String docId2, int minLength) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        Document d1 = findDocument(docId1);
        Document d2 = findDocument(docId2);
        if (d1 != null && d2 != null) {
            CustomArrayList<String> common = KasaiLCP.findCommonSubstrings(
                    d1.getContent(), d2.getContent(), minLength);
            for (int i = 0; i < common.size(); i++) {
                results.add("Shared text passage: \"" + truncate(common.get(i), 80) + "\" [Kasai LCP]");
            }
        }

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.CROSS_DOCUMENT_SIMILARITY,
                docId1 + " vs " + docId2 + " (minLen=" + minLength + ")",
                results, elapsed,
                "Found " + results.size() + " shared passage(s) [Kasai LCP]");
    }

    /**
     * Suffix Tree / Automaton advanced substring query — counts occurrences
     * and distinct substrings.
     */
    public QueryResult advancedSubstringQuery(String documentId, String pattern) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        SuffixTree st = documentSuffixTrees.get(documentId);
        SuffixAutomaton sam = documentSuffixAutomata.get(documentId);

        if (st != null) {
            CustomArrayList<Integer> stPositions = st.search(pattern);
            results.add("Locations where phrase appears: found at " + stPositions.size() + " place(s) [Suffix Tree]");
        }
        if (sam != null) {
            long occurrences = sam.countOccurrences(pattern);
            long distinctSubs = sam.countDistinctSubstrings();
            results.add("Total occurrences of phrase in document: " + occurrences + " [Suffix Automaton]");
            results.add("Total distinct words and phrases in document: " + distinctSubs + " [Suffix Automaton]");
        }

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.ADVANCED_SUBSTRING_QUERY,
                pattern + " in doc:" + documentId, results, elapsed,
                "Completed text vocabulary and passage analysis [Suffix Tree & Suffix Automaton]");
    }

    // ====================================================================
    //  MODULE 3 — ADVANCED DP  (CO3)
    // ====================================================================

    /**
     * Levenshtein fuzzy match: finds courses closest to a (possibly misspelled) query.
     */
    public QueryResult fuzzyMatchLevenshtein(String query, int maxResults) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        // Compute distance to every course name, collect best matches
        CustomArrayList<int[]> distances = new CustomArrayList<>(); // {index, distance}
        for (int i = 0; i < courses.size(); i++) {
            int d = Levenshtein.distance(query.toLowerCase(), courses.get(i).getCourseName().toLowerCase());
            distances.add(new int[]{i, d});
        }
        // Sort by distance (simple selection sort for small n)
        sortBySecond(distances);

        int limit = Math.min(maxResults, distances.size());
        for (int i = 0; i < limit; i++) {
            int[] entry = distances.get(i);
            Course c = courses.get(entry[0]);
            double sim = Levenshtein.similarity(query.toLowerCase(), c.getCourseName().toLowerCase());
            results.add(c.getCourseCode() + ": " + c.getCourseName() + " (edit differences: " + entry[1]
                    + ", match confidence: " + String.format("%.1f%%", sim * 100) + ") [Levenshtein]");
        }

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.FUZZY_MATCH_LEVENSHTEIN, query, results, elapsed,
                "Found " + limit + " closest course recommendation(s) for '" + query + "' [Levenshtein]");
    }

    /**
     * Damerau-Levenshtein fuzzy match — handles transposed characters.
     */
    public QueryResult fuzzyMatchDamerau(String query, int maxResults) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        CustomArrayList<int[]> distances = new CustomArrayList<>();
        for (int i = 0; i < courses.size(); i++) {
            int d = DamerauLevenshtein.distance(query.toLowerCase(), courses.get(i).getCourseName().toLowerCase());
            distances.add(new int[]{i, d});
        }
        sortBySecond(distances);

        int limit = Math.min(maxResults, distances.size());
        for (int i = 0; i < limit; i++) {
            int[] entry = distances.get(i);
            Course c = courses.get(entry[0]);
            results.add(c.getCourseCode() + ": " + c.getCourseName() + " (letter swaps/typos: " + entry[1] + ") [Damerau-Levenshtein]");
        }

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.FUZZY_MATCH_DAMERAU, query, results, elapsed,
                "Found " + limit + " closest course recommendation(s) with transposition tolerance [Damerau-Levenshtein]");
    }

    /**
     * Needleman-Wunsch global alignment between two assignment texts.
     */
    public QueryResult globalAlignment(String assignId1, String assignId2) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        Assignment a1 = findAssignment(assignId1);
        Assignment a2 = findAssignment(assignId2);
        if (a1 != null && a2 != null) {
            // Use a representative snippet (first 200 chars) for demo purposes
            String s1 = truncate(a1.getContent(), 200);
            String s2 = truncate(a2.getContent(), 200);
            NeedlemanWunsch nw = new NeedlemanWunsch();
            NeedlemanWunsch.AlignmentResult ar = nw.align(s1, s2);
            results.add("Overall syllabus alignment score: " + ar.score + " [Needleman-Wunsch]");
            results.add("Matches: " + ar.matches + " | Mismatches: " + ar.mismatches + " | Gaps: " + ar.gaps);
            results.add("Aligned text 1: " + truncate(ar.alignedSeq1, 75));
            results.add("Aligned text 2: " + truncate(ar.alignedSeq2, 75));
        }

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.GLOBAL_ALIGNMENT,
                assignId1 + " vs " + assignId2, results, elapsed,
                "Full document alignment completed [Needleman-Wunsch]");
    }

    /**
     * Smith-Waterman local alignment for partial plagiarism detection.
     */
    public QueryResult localAlignment(String assignId1, String assignId2) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        Assignment a1 = findAssignment(assignId1);
        Assignment a2 = findAssignment(assignId2);
        SmithWaterman.LocalAlignmentResult lar = null;
        if (a1 != null && a2 != null) {
            String s1 = truncate(a1.getContent(), 200);
            String s2 = truncate(a2.getContent(), 200);
            SmithWaterman sw = new SmithWaterman();
            lar = sw.align(s1, s2);
            results.add("Most similar matching section score: " + lar.score + " [Smith-Waterman]");
            results.add("Matching text from first submission : " + truncate(lar.alignedSeq1, 75));
            results.add("Matching text from second submission: " + truncate(lar.alignedSeq2, 75));
        }

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.LOCAL_ALIGNMENT,
                assignId1 + " vs " + assignId2, results, elapsed,
                "Highest matching section score: " + (lar != null ? String.valueOf(lar.score) : "0") + " [Smith-Waterman]");
    }

    /**
     * Matrix-chain multiplication optimisation for data processing pipelines.
     */
    public QueryResult matrixChainOptimization(int[] dimensions) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        MatrixChainMultiplication.MCMResult mcm = MatrixChainMultiplication.solve(dimensions);
        results.add("Fewest database calculations needed: " + mcm.minCost + " operations [Matrix-Chain Multiplication]");
        results.add("Optimal join order: " + mcm.optimalParenthesisation);

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.MATRIX_CHAIN_OPTIMIZATION,
                "dims[" + dimensions.length + "]", results, elapsed,
                "Calculated optimal table join order with minimum cost of " + mcm.minCost + " operations [Matrix-Chain Multiplication]");
    }

    /**
     * Optimal BST for frequently accessed academic keys.
     */
    public QueryResult optimalKeyLookup(String[] keys, double[] frequencies) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        OptimalBST.OBSTResult obst = OptimalBST.solve(keys, frequencies);
        results.add("Average lookup steps: " + String.format("%.2f", obst.minCost) + " comparisons [Optimal BST]");
        results.add("Optimal search tree layout:\n" + obst.treeDescription);

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.OPTIMAL_KEY_LOOKUP,
                keys.length + " key(s)", results, elapsed,
                "Optimal search tree constructed with average lookup cost of " + String.format("%.2f", obst.minCost) + " comparisons [Optimal BST]");
    }

    /**
     * Bitmask DP — explores feasible course combinations.
     * Models courses as nodes, inter-building distances as edges, finds the shortest
     * visitation tour (TSP) for a day of classes across buildings.
     */
    public QueryResult courseCombinationTSP(int[][] distanceMatrix, String[] buildingNames) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        BitmaskDP.TSPResult tsp = BitmaskDP.solveTSP(distanceMatrix);
        results.add("Shortest complete walking route: " + tsp.minCost + " distance units [Bitmask DP]");
        StringBuilder tour = new StringBuilder("Optimal route: ");
        for (int i = 0; i < tsp.tour.length; i++) {
            if (i > 0) tour.append(" -> ");
            if (buildingNames != null && tsp.tour[i] < buildingNames.length) {
                tour.append(buildingNames[tsp.tour[i]]);
            } else {
                tour.append(tsp.tour[i]);
            }
        }
        results.add(tour.toString());

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.COURSE_COMBINATION_SEARCH,
                distanceMatrix.length + " buildings", results, elapsed,
                "Calculated shortest walking tour of " + tsp.minCost + " distance units across " + distanceMatrix.length + " buildings [Bitmask DP]");
    }

    /**
     * Bitmask DP — set cover: find minimum elective courses that cover all skill areas.
     */
    public QueryResult courseSetCover(int[] courseBitmasks, int[] costs,
                                      int numSkillAreas, String[] courseLabels) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        BitmaskDP.SetCoverResult sc = BitmaskDP.solveSetCover(courseBitmasks, costs, numSkillAreas);
        results.add("Fewest courses needed to cover all requirements: " + sc.minCost + " [Bitmask DP]");
        StringBuilder sel = new StringBuilder("Selected courses: ");
        for (int i = 0; i < sc.selectedSets.size(); i++) {
            if (i > 0) sel.append(", ");
            int idx = sc.selectedSets.get(i);
            if (courseLabels != null && idx < courseLabels.length) {
                sel.append(courseLabels[idx]);
            } else {
                sel.append("Set-" + idx);
            }
        }
        results.add(sel.toString());

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.COURSE_COMBINATION_SEARCH,
                numSkillAreas + " skill areas", results, elapsed,
                "Found optimal course combination covering " + numSkillAreas + " requirements [Bitmask DP]");
    }

    /**
     * Tree DP — prerequisite chain analysis.
     * Models course prerequisites as a tree and computes the longest chain (diameter).
     */
    @SuppressWarnings("unchecked")
    public QueryResult prerequisiteAnalysis(int numCourses, int[][] prerequisites) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        // Build adjacency list
        CustomArrayList<Integer>[] adj = new CustomArrayList[numCourses];
        for (int i = 0; i < numCourses; i++) adj[i] = new CustomArrayList<>();
        for (int[] edge : prerequisites) {
            adj[edge[0]].add(edge[1]);
            adj[edge[1]].add(edge[0]);
        }

        int diameter = TreeDP.treeDiameter(adj, numCourses);
        results.add("Longest sequence of prerequisites: " + diameter + " consecutive courses in a row [Tree DP]");

        // Also compute sum of distances (rerooting)
        long[] sumDist = TreeDP.sumOfDistancesAllRoots(adj, numCourses);
        int bestRoot = 0;
        for (int i = 1; i < numCourses; i++) {
            if (sumDist[i] < sumDist[bestRoot]) bestRoot = i;
        }
        results.add("Most central course in curriculum: Course " + bestRoot
                + " (total path distance: " + sumDist[bestRoot] + ") [Tree DP]");

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.PREREQUISITE_ANALYSIS,
                numCourses + " courses", results, elapsed,
                "Degree prerequisite analysis completed [Tree DP]");
    }

    /**
     * Tree DP — maximum independent set on the prerequisite tree.
     * Models course credits as weights and finds the maximum credit subset
     * where no two adjacent (prerequisite-linked) courses are selected.
     */
    @SuppressWarnings("unchecked")
    public QueryResult maxIndependentCourseSet(int numCourses, int[][] prerequisites, int[] credits) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        CustomArrayList<Integer>[] adj = new CustomArrayList[numCourses];
        for (int i = 0; i < numCourses; i++) adj[i] = new CustomArrayList<>();
        for (int[] edge : prerequisites) {
            adj[edge[0]].add(edge[1]);
            adj[edge[1]].add(edge[0]);
        }

        int maxCredits = TreeDP.maxIndependentSet(adj, credits, numCourses);
        results.add("Maximum conflict-free elective credits you can take together: " + maxCredits + " credits [Tree DP]");

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.PREREQUISITE_ANALYSIS,
                numCourses + " courses", results, elapsed,
                "Maximum conflict-free elective credits: " + maxCredits + " [Tree DP]");
    }

    /**
     * SOS DP — enrollment analytics.
     * Computes, for each combination of courses, how many students are enrolled in
     * at least those courses.
     */
    public QueryResult enrollmentAnalytics(int[] studentEnrollments, int numCourses, String[] courseNames) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        long[] analysis = SOSDP.courseEnrollmentAnalysis(studentEnrollments, numCourses);
        results.add("Total students: " + studentEnrollments.length);
        results.add("Courses analysed: " + numCourses);

        // Report some interesting combinations
        for (int mask = 1; mask < (1 << numCourses) && mask < 32; mask++) {
            if (analysis[mask] > 0) {
                StringBuilder courses_str = new StringBuilder();
                for (int bit = 0; bit < numCourses; bit++) {
                    if ((mask & (1 << bit)) != 0) {
                        if (courses_str.length() > 0) courses_str.append(" + ");
                        if (courseNames != null && bit < courseNames.length) {
                            courses_str.append(courseNames[bit]);
                        } else {
                            courses_str.append("C" + bit);
                        }
                    }
                }
                results.add(courses_str + ": " + analysis[mask] + " student(s) [SOS DP]");
            }
        }

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.ENROLLMENT_ANALYTICS,
                numCourses + " courses", results, elapsed,
                "Analyzed enrollment numbers for " + studentEnrollments.length + " students across " + numCourses + " courses [SOS DP]");
    }

    /**
     * SOS DP — inclusion-exclusion union size.
     */
    public QueryResult inclusionExclusionAnalytics(int[] setBitmasks, int universeSize) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        int unionSize = SOSDP.inclusionExclusionUnion(setBitmasks, universeSize);
        results.add("Total distinct students across all selected courses: " + unionSize + " [SOS DP]");
        results.add("Universe size: " + universeSize + ", Sets: " + setBitmasks.length);

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.ENROLLMENT_ANALYTICS,
                setBitmasks.length + " sets", results, elapsed,
                "Total distinct students across selected courses: " + unionSize + " [SOS DP]");
    }

    // ====================================================================
    //  MODULE 4 — NETWORK FLOW
    // ====================================================================

    /**
     * Edmonds-Karp maximum flow in O(VE^2).
     */
    public QueryResult computeEdmondsKarpFlow(FlowNetwork network, int s, int t) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        EdmondsKarp.FlowResult res = EdmondsKarp.solve(network, s, t);
        results.add("Maximum flow transmitted: " + res.maxFlow + " units [Edmonds-Karp]");
        results.add("Total BFS path augmentations: " + res.augmentations);
        for (int i = 0; i < res.pathHistory.size(); i++) {
            results.add(res.pathHistory.get(i));
        }

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.MAX_FLOW_EDMONDS_KARP,
                "s=" + s + ", t=" + t, results, elapsed,
                "Computed maximum flow of " + res.maxFlow + " units across " + network.getNumVertices() + " nodes [Edmonds-Karp]");
    }

    /**
     * Dinic's blocking flow algorithm in O(V^2 E) / O(E sqrt(V)).
     */
    public QueryResult computeDinicFlow(FlowNetwork network, int s, int t) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        Dinic.DinicResult res = Dinic.solve(network, s, t);
        results.add("Maximum blocking flow: " + res.maxFlow + " units [Dinic]");
        results.add("Level-graph phases: " + res.levelGraphPhases);
        results.add(res.complexityAnalysis);

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.MAX_FLOW_DINIC,
                "s=" + s + ", t=" + t, results, elapsed,
                "Dinic algorithm transmitted " + res.maxFlow + " units in " + res.levelGraphPhases + " phases [Dinic]");
    }

    /**
     * Min-Cut partition as the dual of maximum flow.
     */
    public QueryResult computeMinCut(FlowNetwork network, int s, int t) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        MinCut.CutResult res = MinCut.computeMinCut(network, s, t);
        results.add("Minimum cut capacity: " + res.minCutCapacity + " [Min-Cut]");
        results.add("Dual max flow value: " + res.maxFlowValue + " (Duality verified: " + res.dualityVerified + ")");
        results.add("Set S (reachable from source): " + res.setS.size() + " vertices");
        results.add("Set T (sink partition): " + res.setT.size() + " vertices");
        results.add("Bottleneck cut edges crossing S -> T: " + res.cutEdges.size() + " edge(s)");

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.MIN_CUT_PARTITION,
                "s=" + s + ", t=" + t, results, elapsed,
                "Min-Cut partition completed with capacity " + res.minCutCapacity + " [Min-Cut Duality]");
    }

    /**
     * Maximum bipartite matching & König's theorem student-lab assignment.
     */
    public QueryResult assignStudentsBipartite(int numStudents, int numProjects, CustomArrayList<int[]> edges) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        BipartiteMatching.MatchingResult res = BipartiteMatching.solve(numStudents, numProjects, edges);
        results.add("Maximum matched pairs: " + res.matchingSize + " assignment(s) [Bipartite Matching]");
        results.add("König's theorem duality: Min Vertex Cover size = " + (res.minVertexCoverLeft.size() + res.minVertexCoverRight.size())
                + " (Matches matching size: " + res.konigTheoremSatisfied + ")");

        for (int i = 0; i < Math.min(10, res.matchedPairs.size()); i++) {
            int[] p = res.matchedPairs.get(i);
            results.add("Assigned: Student " + p[0] + " -> Lab Project " + p[1]);
        }

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.BIPARTITE_MATCHING_ASSIGNMENT,
                numStudents + " students, " + numProjects + " projects", results, elapsed,
                "Assigned " + res.matchingSize + " students to projects under König's theorem [Bipartite Matching]");
    }

    /**
     * Min-Cost Max-Flow (SSP with SPFA) optimal assignment.
     */
    public QueryResult assignMinCostMaxFlow(FlowNetwork network, int s, int t) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        MinCostMaxFlow.MCMFResult res = MinCostMaxFlow.solve(network, s, t);
        results.add("Maximum flow achieved: " + res.maxFlow + " units [MCMF]");
        results.add("Minimum total cost: " + res.minCost + " preference cost units");
        results.add("Shortest-path augmentations: " + res.augmentations);

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.MIN_COST_MAX_FLOW_ASSIGNMENT,
                "s=" + s + ", t=" + t, results, elapsed,
                "Achieved flow of " + res.maxFlow + " at minimum cost of " + res.minCost + " [Min-Cost Max-Flow]");
    }

    /**
     * Project selection problem reduced to Min-Cut.
     */
    public QueryResult solveProjectSelection(int[] profits, CustomArrayList<int[]> dependencies) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        FlowReductions.ProjectSelectionResult res = FlowReductions.solveProjectSelection(profits, dependencies);
        results.add("Optimal net profit: " + res.maxNetProfit + " [Project Selection Min-Cut]");
        results.add("Selected projects count: " + res.selectedProjects.size() + " / " + profits.length);
        results.add("Min-Cut bottleneck penalty: " + res.minCutCost);
        results.add(res.explanation);

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.PROJECT_SELECTION_REDUCTION,
                profits.length + " projects", results, elapsed,
                "Optimal research project selection yielded net profit of " + res.maxNetProfit + " [Min-Cut Reduction]");
    }

    // ====================================================================
    //  MODULE 5 — NP-COMPLETENESS & APPROXIMATION
    // ====================================================================

    /**
     * Vertex Cover 2-approximation via maximal matching with ratio guarantee.
     */
    public QueryResult approximateVertexCover(int numVertices, int[][] edges) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        VertexCoverApproximation.ApproxResult res = VertexCoverApproximation.solve(numVertices, edges);
        results.add("2-Approximation cover size: " + res.approxSize + " checkpoints [Vertex Cover 2-Approx]");
        results.add("Maximal matching edges: " + res.maximalMatching.size() + " (lower bound OPT >= " + res.maximalMatching.size() + ")");
        results.add("Guaranteed approximation ratio: <= " + res.theoreticalGuarantee);
        if (res.exactOptSize > 0) {
            results.add("Exact OPT cover size: " + res.exactOptSize + " (Empirical ratio: " + String.format("%.2f", res.empiricalRatio) + ")");
        }

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.VERTEX_COVER_2_APPROX,
                numVertices + " vertices, " + edges.length + " edges", results, elapsed,
                "Calculated 2-approximation cover of size " + res.approxSize + " [Vertex Cover 2-Approx]");
    }

    /**
     * 0/1 Knapsack FPTAS value scaling with (1 - epsilon) guarantee.
     */
    public QueryResult solveKnapsackFptas(int[] weights, int[] values, int capacity, double epsilon) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        KnapsackApproximation.KnapsackResult greedy = KnapsackApproximation.solveGreedy2Approx(weights, values, capacity);
        KnapsackApproximation.KnapsackResult fptas = KnapsackApproximation.solveFPTAS(weights, values, capacity, epsilon);

        results.add("Greedy 2-Approx Value: " + greedy.totalValue + " (Weight: " + greedy.totalWeight + "/" + capacity + ") [Greedy 2-Approx]");
        results.add("FPTAS (" + String.format("%.0f%%", (1.0 - epsilon) * 100) + ") Value: " + fptas.totalValue
                + " (Weight: " + fptas.totalWeight + "/" + capacity + ") [Knapsack FPTAS]");
        results.add("Selected items count: " + fptas.selectedItemIds.size() + " items");

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.KNAPSACK_FPTAS_APPROX,
                weights.length + " items, cap=" + capacity, results, elapsed,
                "FPTAS computed solution with value " + fptas.totalValue + " [Knapsack FPTAS]");
    }

    /**
     * Parameterized Vertex Cover via Buss Kernelization & Bounded Search Tree.
     */
    public QueryResult solveParameterizedVertexCover(int numVertices, int[][] edges, int k) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        ParameterizedVertexCover.FptResult res = ParameterizedVertexCover.solve(numVertices, edges, k);
        results.add("Feasible within budget k=" + k + ": " + (res.feasible ? "YES" : "NO") + " [FPT Kernelization]");
        results.add("Kernel size after Buss reduction: " + res.kernelVertices + " vertices, " + res.kernelEdges + " edges");
        results.add(res.diagnosis);
        if (res.feasible) {
            results.add("Chosen vertex cover: " + res.vertexCover.size() + " nodes");
        }

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.FPT_VERTEX_COVER_KERNEL,
                "k=" + k + " on " + numVertices + " nodes", results, elapsed,
                "FPT Buss kernelization " + (res.feasible ? "found" : "proved no") + " cover of size <= " + k + " [FPT]");
    }

    /**
     * Reduction Zoo step-through (3-SAT to CLIQUE to IS to VC).
     */
    public QueryResult verifyReductionZoo(CustomArrayList<ReductionZoo.Clause> formula, boolean[] assignment) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        ReductionZoo.ReductionResult res = ReductionZoo.reduce3SatToClique(formula, assignment);
        results.add("From: " + res.fromProblem + " -> To: " + res.toProblem + " [Reduction Zoo]");
        results.add(res.reductionDetails);
        results.add("Witness verified: " + res.witnessValid);
        results.add(res.certificateAnalysis);

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.REDUCTION_ZOO_VERIFICATION,
                formula.size() + " clauses", results, elapsed,
                "Verified polynomial-time reduction from 3-SAT to CLIQUE [Reduction Zoo]");
    }

    // ====================================================================
    //  MODULE 6 — RANDOMISED & PARALLEL ALGORITHMS
    // ====================================================================

    /**
     * Miller-Rabin probabilistic primality test with witness construction.
     */
    public QueryResult testMillerRabinPrimality(long candidate, int rounds) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        MillerRabin.PrimalityResult res = MillerRabin.testProbabilistic(candidate, rounds);
        results.add("Candidate: " + candidate + " -> " + (res.isPrime ? "PRIME (Probabilistic)" : "COMPOSITE (Definite)") + " [Miller-Rabin]");
        if (res.witness != null) {
            results.add("Non-trivial composite witness: base a = " + res.witness);
        }
        for (int i = 0; i < res.roundDetails.size(); i++) {
            results.add(res.roundDetails.get(i));
        }

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.MILLER_RABIN_PRIMALITY,
                "candidate=" + candidate, results, elapsed,
                "Miller-Rabin confirmed " + candidate + " is " + (res.isPrime ? "prime" : "composite") + " [Miller-Rabin]");
    }

    /**
     * Randomized QuickSort vs Deterministic QuickSort comparison.
     */
    public QueryResult analyzeRandomizedQuickSort(int[] arr) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        RandomizedQuickSort.SortMetrics rand = RandomizedQuickSort.sortRandomized(arr);
        RandomizedQuickSort.SortMetrics det = RandomizedQuickSort.sortDeterministic(arr);

        results.add("Array size: " + arr.length + " elements");
        results.add("Randomized QuickSort: " + rand.comparisons + " comparisons (Recursion depth: " + rand.maxRecursionDepth + ") [Las Vegas]");
        results.add("Theoretical Expected 2n ln(n): " + String.format("%.0f", rand.theoreticalExpectedComparisons) + " comparisons");
        results.add("Deterministic QuickSort: " + det.comparisons + " comparisons (Recursion depth: " + det.maxRecursionDepth + ")");

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.RANDOMIZED_QUICKSORT_ANALYSIS,
                arr.length + " elements", results, elapsed,
                "Randomized QuickSort sorted array in " + rand.comparisons + " comparisons [Randomized QuickSort]");
    }

    /**
     * FKS Two-Level Perfect Hashing lookup with guaranteed O(1) worst-case time.
     */
    public QueryResult lookupFksPerfectHashing(CustomArrayList<String> keys, CustomArrayList<String> values, String queryKey) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        PerfectHashing<String> ph = PerfectHashing.build(keys, values);
        PerfectHashing.PerfectHashMetrics m = ph.getMetrics();
        results.add("FKS Perfect Hash Table constructed: " + m.totalKeys + " keys into " + m.totalSecondarySlots + " slots [FKS O(1)]");
        results.add("Space overhead ratio: " + String.format("%.2f", m.spaceOverheadRatio) + " (Linear space O(n))");
        results.add("Primary rehashes needed: " + m.primaryRehashes);

        String val = ph.get(queryKey);
        results.add("Query lookup '" + queryKey + "': " + (val != null ? ("Found -> " + val) : "Key not found") + " in guaranteed O(1) time!");

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.PERFECT_HASHING_FKS,
                "key=" + queryKey, results, elapsed,
                "FKS Perfect Hash Table executed O(1) lookup for '" + queryKey + "' [FKS Perfect Hashing]");
    }

    /**
     * Reservoir sampling audit on continuous event/log stream.
     */
    public QueryResult auditReservoirStream(CustomArrayList<String> stream, int sampleSize) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        ReservoirSampling<String> sampler = new ReservoirSampling<>(sampleSize);
        for (int i = 0; i < stream.size(); i++) {
            sampler.feed(stream.get(i));
        }

        results.add("Total stream elements processed: " + sampler.getTotalSeen() + " [Reservoir Sampling]");
        results.add("Reservoir capacity: " + sampleSize);
        results.add("Inclusion probability per item: " + String.format("%.4f", sampler.getInclusionProbability()) + " (k / N)");

        CustomArrayList<String> res = sampler.getReservoir();
        for (int i = 0; i < Math.min(8, res.size()); i++) {
            results.add("Sample #" + (i + 1) + ": " + res.get(i));
        }

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.RESERVOIR_STREAM_SAMPLING,
                sampleSize + " samples from " + stream.size() + " items", results, elapsed,
                "Reservoir sampling collected " + sampleSize + " uniform audit items [Algorithm R]");
    }

    /**
     * Blelloch work-efficient parallel scan and Brent's theorem analysis.
     */
    public QueryResult executeParallelScanBlelloch(int[] input) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();

        ParallelPrimitives.ScanResult scan = ParallelPrimitives.blellochParallelScan(input);
        ParallelPrimitives.ParallelMetrics m = scan.metrics;

        results.add("Blelloch Parallel Scan: Total sum = " + scan.totalSum + " [Blelloch Scan]");
        results.add("Total Work T_1: " + m.workT1 + " ops, Span T_inf: " + m.spanTInfinity + " levels");
        results.add("Theoretical Parallelism (T_1 / T_inf): " + String.format("%.1f", m.theoreticalParallelism) + "x");
        results.add("Brent's Theorem Speedup: 8 cores -> " + String.format("%.2f", m.brentSpeedupCores[2]) + "x, 16 cores -> "
                + String.format("%.2f", m.brentSpeedupCores[3]) + "x, 64 cores -> " + String.format("%.2f", m.brentSpeedupCores[5]) + "x");

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.PARALLEL_SCAN_BLELLOCH,
                input.length + " elements", results, elapsed,
                "Blelloch work-efficient parallel prefix sum completed in O(log n) span [Parallel Scan]");
    }

    // ====================================================================
    //  MODULE 1 — TEXTHACK INDIC WIKIPEDIA CORPUS
    // ====================================================================

    /**
     * Queries the Wikipedia corpus via KMP pattern matching.
     */
    public QueryResult queryIndicCorpusPattern(String pattern) {
        TextHackEngine.TextHackQueryResult res = textHackEngine.queryPatternSearch(pattern);
        return new QueryResult(QueryType.INDIC_WIKIPEDIA_QUERY,
                pattern, res.answers, res.executionNs,
                "Found " + res.answers.size() + " matches across English Wikipedia corpus [TextHack Engine]");
    }

    /**
     * Queries the Wikipedia corpus via Levenshtein fuzzy match.
     */
    public QueryResult queryIndicCorpusFuzzy(String query, int maxDist) {
        TextHackEngine.TextHackQueryResult res = textHackEngine.queryFuzzyMatch(query, maxDist);
        return new QueryResult(QueryType.INDIC_WIKIPEDIA_QUERY,
                query, res.answers, res.executionNs,
                "Found " + res.answers.size() + " fuzzy matches across English Wikipedia titles [TextHack Engine]");
    }

    // ====================================================================
    //  CONVENIENCE — General fuzzy search across everything
    // ====================================================================

    /**
     * Universal fuzzy search: searches the query across all course names,
     * student names, and document titles, returning the best matches.
     */
    public QueryResult universalFuzzySearch(String query, int maxResults) {
        long t0 = System.nanoTime();
        CustomArrayList<String> results = new CustomArrayList<>();
        CustomArrayList<int[]> scored = new CustomArrayList<>(); // {sourceType, index, distance}

        // Search courses
        for (int i = 0; i < courses.size(); i++) {
            int d = Levenshtein.distance(query.toLowerCase(), courses.get(i).getCourseName().toLowerCase());
            scored.add(new int[]{0, i, d});
        }
        // Search students
        for (int i = 0; i < students.size(); i++) {
            int d = Levenshtein.distance(query.toLowerCase(), students.get(i).getName().toLowerCase());
            scored.add(new int[]{1, i, d});
        }
        // Search document titles
        for (int i = 0; i < documents.size(); i++) {
            int d = Levenshtein.distance(query.toLowerCase(), documents.get(i).getTitle().toLowerCase());
            scored.add(new int[]{2, i, d});
        }

        // Sort by distance
        sortByThird(scored);

        int limit = Math.min(maxResults, scored.size());
        for (int i = 0; i < limit; i++) {
            int[] entry = scored.get(i);
            String label;
            switch (entry[0]) {
                case 0:  label = "Course: "   + courses.get(entry[1]).getCourseName(); break;
                case 1:  label = "Student: "  + students.get(entry[1]).getName(); break;
                case 2:  label = "Document: " + documents.get(entry[1]).getTitle(); break;
                default: label = "Record: "; break;
            }
            results.add(label + " (edit differences: " + entry[2] + ") [Levenshtein]");
        }

        long elapsed = System.nanoTime() - t0;
        return new QueryResult(QueryType.FUZZY_MATCH_LEVENSHTEIN, query, results, elapsed,
                "Found " + limit + " closest matching academic record(s) [Levenshtein]");
    }

    // ====================================================================
    //  INTERNAL HELPERS
    // ====================================================================

    private Assignment findAssignment(String id) {
        for (int i = 0; i < assignments.size(); i++) {
            if (assignments.get(i).getAssignmentId().equals(id)) return assignments.get(i);
        }
        return null;
    }

    private Document findDocument(String id) {
        for (int i = 0; i < documents.size(); i++) {
            if (documents.get(i).getDocumentId().equals(id)) return documents.get(i);
        }
        return null;
    }

    /** Simple selection sort for small lists of {index, distance} pairs. */
    private void sortBySecond(CustomArrayList<int[]> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(j)[1] < list.get(minIdx)[1]) minIdx = j;
            }
            if (minIdx != i) {
                int[] tmp = list.get(i);
                list.set(i, list.get(minIdx));
                list.set(minIdx, tmp);
            }
        }
    }

    /** Sort by third element in each int[] entry. */
    private void sortByThird(CustomArrayList<int[]> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(j)[2] < list.get(minIdx)[2]) minIdx = j;
            }
            if (minIdx != i) {
                int[] tmp = list.get(i);
                list.set(i, list.get(minIdx));
                list.set(minIdx, tmp);
            }
        }
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 3) + "...";
    }

    private static String join(String[] arr, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(separator);
            sb.append(arr[i]);
        }
        return sb.toString();
    }
}
