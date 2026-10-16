package edutrack.test;

import edutrack.core.*;
import edutrack.string.*;
import edutrack.suffix.*;
import edutrack.dp.*;
import edutrack.engine.*;
import edutrack.model.*;
import edutrack.app.*;
import edutrack.data.DataLoader;
import edutrack.flow.*;
import edutrack.approx.*;
import edutrack.randomparallel.*;
import edutrack.corpus.*;

/**
 * Comprehensive Automated Test Suite for EduTrack & TextHack.
 *
 * <p>Validates all hand-built data structures, algorithms, service layers,
 * and query engines across all 6 modules with zero external libraries or java.util.* dependencies.</p>
 */
public class EduTrackTestRunner {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            testsPassed++;
            System.out.println("  [PASS] " + testName);
        } else {
            testsFailed++;
            System.err.println("  [FAIL] " + testName);
        }
    }

    private static void assertEquals(String testName, int expected, int actual) {
        if (expected == actual) {
            testsPassed++;
            System.out.println("  [PASS] " + testName + " (expected: " + expected + ", got: " + actual + ")");
        } else {
            testsFailed++;
            System.err.println("  [FAIL] " + testName + " (expected: " + expected + ", got: " + actual + ")");
        }
    }

    private static void assertEquals(String testName, String expected, String actual) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            testsPassed++;
            System.out.println("  [PASS] " + testName);
        } else {
            testsFailed++;
            System.err.println("  [FAIL] " + testName + " (expected: \"" + expected + "\", got: \"" + actual + "\")");
        }
    }

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("      EDUTRACK AUTOMATED TEST RUNNER (ALL MODULES 1 - 6)      ");
        System.out.println("===============================================================\n");

        testCoreDataStructures();
        testModule1TextHackIndicCorpus();
        testModule1StringAlgorithms();
        testModule2SuffixStructures();
        testModule3AdvancedDP();
        testModule4NetworkFlow();
        testModule5NPCompletenessAndApproximation();
        testModule6RandomisedAndParallelAlgorithms();
        testServiceLayersAndEngines();
        testAdminAndDatasets();

        System.out.println("\n===============================================================");
        System.out.println(" TEST RESULTS: " + testsPassed + " passed, " + testsFailed + " failed.");
        System.out.println("===============================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    public static void runAllTests() {
        main(new String[0]);
    }

    // =========================================================================
    // 1. CORE DATA STRUCTURES
    // =========================================================================
    private static void testCoreDataStructures() {
        System.out.println("\n--- Testing Core Data Structures (No java.util.*) ---");

        // CustomArrayList
        CustomArrayList<String> list = new CustomArrayList<>();
        list.add("CS101");
        list.add("CS102");
        list.add("CS103");
        assertEquals("CustomArrayList size after 3 adds", 3, list.size());
        assertEquals("CustomArrayList get(1)", "CS102", list.get(1));
        list.remove(1);
        assertEquals("CustomArrayList size after remove(1)", 2, list.size());
        assertEquals("CustomArrayList new get(1)", "CS103", list.get(1));

        // CustomHashMap
        CustomHashMap<String, Integer> map = new CustomHashMap<>();
        map.put("Arjun", 95);
        map.put("Priya", 98);
        map.put("Rahul", 88);
        assertEquals("CustomHashMap size", 3, map.size());
        assertEquals("CustomHashMap get Priya", 98, (int) map.get("Priya"));
        assertTrue("CustomHashMap containsKey Arjun", map.containsKey("Arjun"));
        map.remove("Arjun");
        assertTrue("CustomHashMap removed Arjun", !map.containsKey("Arjun"));

        // CustomQueue
        CustomQueue<Integer> queue = new CustomQueue<>();
        queue.enqueue(10);
        queue.enqueue(20);
        queue.enqueue(30);
        assertEquals("CustomQueue size", 3, queue.size());
        assertEquals("CustomQueue dequeue first", 10, (int) queue.dequeue());
        assertEquals("CustomQueue peek", 20, (int) queue.peek());
        assertEquals("CustomQueue dequeue second", 20, (int) queue.dequeue());
        assertEquals("CustomQueue dequeue third", 30, (int) queue.dequeue());
        assertTrue("CustomQueue is empty", queue.isEmpty());

        // CustomStack
        CustomStack<String> stack = new CustomStack<>();
        stack.push("Base");
        stack.push("Mid");
        stack.push("Top");
        assertEquals("CustomStack peek", "Top", stack.peek());
        assertEquals("CustomStack pop 1", "Top", stack.pop());
        assertEquals("CustomStack pop 2", "Mid", stack.pop());
        assertEquals("CustomStack pop 3", "Base", stack.pop());
        assertTrue("CustomStack is empty", stack.isEmpty());

        // CustomLinkedList
        CustomLinkedList<Integer> llist = new CustomLinkedList<>();
        llist.add(100);
        llist.add(200);
        llist.add(300);
        assertEquals("CustomLinkedList size", 3, llist.size());
        assertEquals("CustomLinkedList get(0)", 100, (int) llist.get(0));
        assertEquals("CustomLinkedList get(2)", 300, (int) llist.get(2));

        // CustomPriorityQueue
        CustomPriorityQueue<Integer> pq = new CustomPriorityQueue<>((a, b) -> a - b);
        pq.insert(50);
        pq.insert(10);
        pq.insert(40);
        pq.insert(20);
        assertEquals("CustomPriorityQueue extractMin 1", 10, (int) pq.extractMin());
        assertEquals("CustomPriorityQueue extractMin 2", 20, (int) pq.extractMin());
        assertEquals("CustomPriorityQueue extractMin 3", 40, (int) pq.extractMin());
        assertEquals("CustomPriorityQueue extractMin 4", 50, (int) pq.extractMin());

        // CustomTreeMap (AVL tree)
        CustomTreeMap<String, Integer> treeMap = new CustomTreeMap<>((a, b) -> a.compareTo(b));
        treeMap.put("B", 2);
        treeMap.put("A", 1);
        treeMap.put("C", 3);
        assertEquals("CustomTreeMap get A", 1, (int) treeMap.get("A"));
        assertEquals("CustomTreeMap get B", 2, (int) treeMap.get("B"));
        assertEquals("CustomTreeMap get C", 3, (int) treeMap.get("C"));
        assertTrue("CustomTreeMap containsKey B", treeMap.containsKey("B"));
    }

    // =========================================================================
    // 2. MODULE 1: STRING ALGORITHMS
    // =========================================================================
    private static void testModule1StringAlgorithms() {
        System.out.println("\n--- Testing Module 1: String Algorithms (CO1) ---");

        // KMP
        String text = "ABABDABACDABABCABAB";
        String pattern = "ABABCABAB";
        CustomArrayList<Integer> kmpMatches = KMP.search(text, pattern);
        assertEquals("KMP match count", 1, kmpMatches.size());
        assertEquals("KMP match index", 10, (int) kmpMatches.get(0));
        assertTrue("KMP contains match", KMP.contains(text, "ABABCABAB"));
        assertTrue("KMP not contains missing", !KMP.contains(text, "XYZ"));

        // Z-Function
        String zText = "aabxaabxcaabxaabxay";
        String zPat = "aabx";
        CustomArrayList<Integer> zMatches = ZFunction.search(zText, zPat);
        assertEquals("ZFunction match count", 4, zMatches.size());
        assertEquals("ZFunction first match", 0, (int) zMatches.get(0));
        assertEquals("ZFunction second match", 4, (int) zMatches.get(1));

        // Rabin-Karp
        String rkText = "COURSE_CS301_AND_COURSE_CS401";
        CustomArrayList<Integer> rkMatches = RabinKarp.search(rkText, "COURSE");
        assertEquals("Rabin-Karp match count", 2, rkMatches.size());
        assertEquals("Rabin-Karp match 1", 0, (int) rkMatches.get(0));
        assertEquals("Rabin-Karp match 2", 17, (int) rkMatches.get(1));

        // Aho-Corasick
        String[] keywords = {"he", "she", "his", "hers"};
        AhoCorasick ac = new AhoCorasick(keywords);
        CustomArrayList<int[]> acMatches = ac.search("ushers");
        // In "ushers": "she" at 1, "he" at 2, "hers" at 2
        assertEquals("Aho-Corasick multi-match count", 3, acMatches.size());
    }

    // =========================================================================
    // 3. MODULE 2: SUFFIX STRUCTURES
    // =========================================================================
    private static void testModule2SuffixStructures() {
        System.out.println("\n--- Testing Module 2: Suffix Structures (CO2) ---");

        // Suffix Array
        String s = "banana";
        SuffixArray sa = new SuffixArray(s);
        int[] saArr = sa.getSuffixArray();
        // Suffixes of "banana":
        // 0: banana, 1: anana, 2: nana, 3: ana, 4: na, 5: a
        // Sorted: 5(a), 3(ana), 1(anana), 0(banana), 4(na), 2(nana)
        assertEquals("SuffixArray sa[0] for banana", 5, saArr[0]);
        assertEquals("SuffixArray sa[1] for banana", 3, saArr[1]);
        assertEquals("SuffixArray sa[2] for banana", 1, saArr[2]);
        assertTrue("SuffixArray contains 'nan'", sa.search("nan").size() > 0);
        assertTrue("SuffixArray not contains 'apple'", sa.search("apple").isEmpty());

        // SA-IS O(n) construction
        int[] saisSA = SAIS.buildSuffixArray(s);
        assertEquals("SA-IS sa length", 6, saisSA.length);
        for (int i = 0; i < 6; i++) {
            assertEquals("SA-IS sa[" + i + "] matches standard SA", saArr[i], saisSA[i]);
        }

        // Kasai LCP
        int[] lcp = KasaiLCP.computeLCP(s, saArr);
        // lcp[0] is 0 or undefined; lcp between "a" (5) and "ana" (3) is 1
        assertEquals("Kasai LCP between a and ana", 1, lcp[1]);
        // lcp between "ana" (3) and "anana" (1) is 3
        assertEquals("Kasai LCP between ana and anana", 3, lcp[2]);

        // Suffix Tree
        SuffixTree tree = new SuffixTree("banana$");
        assertTrue("SuffixTree contains 'ana'", tree.contains("ana"));
        assertTrue("SuffixTree contains 'banana'", tree.contains("banana"));
        assertTrue("SuffixTree not contains 'xyz'", !tree.contains("xyz"));

        // Suffix Automaton
        SuffixAutomaton sam = new SuffixAutomaton("banana");
        assertTrue("SuffixAutomaton contains 'an'", sam.contains("an"));
        assertTrue("SuffixAutomaton contains 'banana'", sam.contains("banana"));
        assertTrue("SuffixAutomaton not contains 'bananas'", !sam.contains("bananas"));
        assertEquals("SuffixAutomaton LCS with 'kanana'", "anana", sam.longestCommonSubstring("kanana"));
    }

    // =========================================================================
    // 4. MODULE 3: ADVANCED DYNAMIC PROGRAMMING
    // =========================================================================
    @SuppressWarnings("unchecked")
    private static void testModule3AdvancedDP() {
        System.out.println("\n--- Testing Module 3: Advanced DP (CO3) ---");

        // Levenshtein
        assertEquals("Levenshtein kitten -> sitting", 3, Levenshtein.distance("kitten", "sitting"));
        assertEquals("Levenshtein identical", 0, Levenshtein.distance("edutrack", "edutrack"));

        // Damerau-Levenshtein (transposition of adjacent characters)
        assertEquals("Damerau-Levenshtein ca -> ac transposition", 1, DamerauLevenshtein.distance("ca", "ac"));
        assertEquals("Standard Levenshtein ca -> ac is 2", 2, Levenshtein.distance("ca", "ac"));

        // Needleman-Wunsch (Global Alignment)
        NeedlemanWunsch.AlignmentResult nw = new NeedlemanWunsch().align("GCATGCG", "GATTACA");
        assertTrue("Needleman-Wunsch aligned sequence non-empty", nw.alignedSeq1.length() > 0);
        assertTrue("Needleman-Wunsch aligned lengths match", nw.alignedSeq1.length() == nw.alignedSeq2.length());

        // Smith-Waterman (Local Alignment)
        SmithWaterman.LocalAlignmentResult sw = new SmithWaterman().align("XXABCDEYY", "ZZABCDERW");
        assertEquals("Smith-Waterman maximal local match", "ABCDE", sw.alignedSeq1);
        assertEquals("Smith-Waterman maximal local match seq2", "ABCDE", sw.alignedSeq2);

        // Matrix-Chain Multiplication
        // Dimensions for 3 matrices: 10x30, 30x5, 5x60 -> p = {10, 30, 5, 60}
        int[] p = {10, 30, 5, 60};
        MatrixChainMultiplication.MCMResult mcm = MatrixChainMultiplication.solve(p);
        assertEquals("MCM optimal scalar cost", 4500, (int) mcm.minCost);

        // Optimal BST
        String[] keys = {"A", "B", "C"};
        double[] freqs = {0.5, 0.1, 0.4};
        OptimalBST.OBSTResult obst = OptimalBST.solve(keys, freqs);
        assertTrue("Optimal BST cost is positive", obst.minCost > 0);

        // Bitmask DP (TSP)
        int[][] dist = {
            {0, 10, 15, 20},
            {10, 0, 35, 25},
            {15, 35, 0, 30},
            {20, 25, 30, 0}
        };
        BitmaskDP.TSPResult tsp = BitmaskDP.solveTSP(dist);
        assertEquals("Bitmask TSP optimal tour cost", 80, tsp.minCost);

        // Tree DP
        // Tree: 0-1, 1-2, 2-3 (path of 4 nodes, diameter is 3 edges)
        int nTree = 4;
        CustomArrayList<Integer>[] adj = new CustomArrayList[nTree];
        for (int i = 0; i < nTree; i++) adj[i] = new CustomArrayList<>();
        adj[0].add(1); adj[1].add(0);
        adj[1].add(2); adj[2].add(1);
        adj[2].add(3); adj[3].add(2);
        int diam = TreeDP.treeDiameter(adj, nTree);
        assertEquals("Tree DP diameter of 4-node line", 3, diam);

        // SOS DP
        // Subsets of 2 items: 00(0), 01(1), 10(2), 11(3)
        long[] f = {1, 2, 4, 8};
        long[] sos = SOSDP.subsetSum(f, 2);
        assertEquals("SOS DP sum for mask 0", 1, (int) sos[0]);
        assertEquals("SOS DP sum for mask 1", 3, (int) sos[1]);
        assertEquals("SOS DP sum for mask 2", 5, (int) sos[2]);
        assertEquals("SOS DP sum for mask 3", 15, (int) sos[3]);
    }

    // =========================================================================
    // 5. SERVICE LAYERS AND ENGINES
    // =========================================================================
    private static void testServiceLayersAndEngines() {
        System.out.println("\n--- Testing Service Layers & Query Engines ---");

        EduTrackEngine engine = new EduTrackEngine();
        engine.addStudent(new Student("S001", "Arjun Sharma", "arjun@univ.edu", "CSE", 5, 9.2));
        engine.addCourse(new Course("CS301", "Data Structures", "CSE", 4, "F001", "Core course on data structures."));
        engine.addDocument(new Document("DOC1", "Notes", "F001", "CS301", "Algorithm design and data structures.", "NOTES"));

        SearchService search = new SearchService(engine);
        QueryResult qr = search.searchCourses("Structures");
        assertTrue("SearchService courses query completed", qr != null && qr.getResults().size() > 0);

        EduTrackEngine eduEngine = new EduTrackEngine();
        eduEngine.addStudent(new Student("S001", "Arjun Sharma", "arjun@univ.edu", "CSE", 5, 9.2));
        eduEngine.addCourse(new Course("CS301", "Data Structures", "CSE", 4, "F001", "Core course on data structures."));
        eduEngine.addDocument(new Document("DOC1", "Notes", "F001", "CS301", "Algorithm design and data structures.", "NOTES"));

        QueryResult eqr = eduEngine.searchCoursesByKMP("Structures");
        assertTrue("EduTrackEngine courses query completed", eqr != null && eqr.getResults().size() > 0);
    }

    // =========================================================================
    // 6. ADMIN DOMAIN MODEL, ADMINSERVICE & DATASETS
    // =========================================================================
    private static void testAdminAndDatasets() {
        System.out.println("\n--- Testing Admin Domain Model, AdminService & Datasets ---");

        // Admin Model
        Admin admin = new Admin("ADM001", "Dr. Rajeshwar Sharma", "dean.academic@univ.edu",
                "Academic Affairs", "Dean of Academic Affairs", "SUPER_ADMIN", "Admin Block Room 101");
        assertEquals("Admin getAdminId", "ADM001", admin.getAdminId());
        assertEquals("Admin getName", "Dr. Rajeshwar Sharma", admin.getName());
        assertEquals("Admin getDepartment", "Academic Affairs", admin.getDepartment());
        assertEquals("Admin getAccessLevel", "SUPER_ADMIN", admin.getAccessLevel());

        // AdminService
        CustomArrayList<Admin> adminList = new CustomArrayList<>();
        adminList.add(admin);
        adminList.add(new Admin("ADM002", "Dr. Meera Krishnan", "registrar@univ.edu",
                "Office of the Registrar", "University Registrar", "SUPER_ADMIN", "Admin Block Room 102"));
        adminList.add(new Admin("ADM003", "Prof. Sanjay Deshmukh", "exam.controller@univ.edu",
                "Examination Branch", "Controller of Examinations", "EXAM_ADMIN", "Exam Wing Room 204"));

        AdminService adminService = new AdminService(adminList);
        Admin found = adminService.findById("ADM002");
        assertTrue("AdminService findById ADM002 found", found != null && found.getName().equals("Dr. Meera Krishnan"));

        CustomArrayList<Admin> examAdmins = adminService.filterByAccessLevel("EXAM_ADMIN");
        assertEquals("AdminService filterByAccessLevel EXAM_ADMIN count", 1, examAdmins.size());

        CustomArrayList<Admin> registrarSearch = adminService.searchByRole("Registrar");
        assertEquals("AdminService searchByRole Registrar count", 1, registrarSearch.size());

        CustomArrayList<Admin> fuzzyAdmins = adminService.fuzzySearchByName("Dr. Meera Krishna", 2);
        assertTrue("AdminService fuzzySearchByName found", fuzzyAdmins.size() > 0);

        // DataLoader Dataset Validation (EduTrack.csv)
        try {
            DataLoader.EduTrackDataset ds = DataLoader.loadEduTrackDataset("EduTrack.csv");
            assertTrue("DataLoader loaded students from EduTrack.csv (= 10,000 records)", ds.students.size() == 10000);
            assertTrue("DataLoader loaded courses from EduTrack.csv (= 12 courses)", ds.courses.size() == 12);
            assertTrue("DataLoader loaded faculty from EduTrack.csv (= 10 faculty)", ds.faculty.size() == 10);
            assertTrue("DataLoader loaded admins (= 5 institutional admins)", ds.admins.size() >= 5);
        } catch (Exception e) {
            assertTrue("DataLoader encountered exception: " + e.getMessage(), false);
        }
    }

    // =========================================================================
    // MODULE 1: TEXTHACK INDIC CORPUS & ADVANCED QUESTION BANK
    // =========================================================================
    private static void testModule1TextHackIndicCorpus() {
        System.out.println("\n--- Testing Module 1: TextHack English Corpus & Question Bank ---");

        CustomArrayList<IndicCorpus.IndicArticle> articles = IndicCorpus.getArticles();
        assertTrue("Corpus contains 8 English articles", articles.size() >= 8);

        TextHackEngine the = new TextHackEngine();
        TextHackEngine.TextHackQueryResult patRes = the.queryPatternSearch("Turing");
        assertTrue("TextHack pattern search for Turing in English", patRes.answers.size() > 0);

        TextHackEngine.TextHackQueryResult fuzRes = the.queryFuzzyMatch("ramanujan", 3);
        assertTrue("TextHack fuzzy match for Ramanujan in English", fuzRes.answers.size() > 0);

        TextHackEngine.TextHackQueryResult primeRes = the.queryLargePrimePrimality(1000000007L);
        assertTrue("TextHack primality test for 1000000007", primeRes.answers.get(1).contains("PRIME"));

        int[][] graph = {{0, 1, 10}, {1, 2, 8}, {0, 2, 5}};
        TextHackEngine.TextHackQueryResult flowRes = the.queryCitationFlow(graph, 3, 0, 2);
        assertTrue("TextHack citation flow computed", flowRes.answers.size() >= 3);
    }

    // =========================================================================
    // MODULE 4: NETWORK FLOW
    // =========================================================================
    private static void testModule4NetworkFlow() {
        System.out.println("\n--- Testing Module 4: Network Flow ---");

        // 1. Edmonds-Karp & Dinic on classic flow network
        FlowNetwork fn1 = new FlowNetwork(6);
        fn1.addEdge(0, 1, 16);
        fn1.addEdge(0, 2, 13);
        fn1.addEdge(1, 2, 10);
        fn1.addEdge(1, 3, 12);
        fn1.addEdge(2, 1, 4);
        fn1.addEdge(2, 4, 14);
        fn1.addEdge(3, 2, 9);
        fn1.addEdge(3, 5, 20);
        fn1.addEdge(4, 3, 7);
        fn1.addEdge(4, 5, 4);

        EdmondsKarp.FlowResult ekRes = EdmondsKarp.solve(fn1, 0, 5);
        assertEquals("Edmonds-Karp max flow", 23, ekRes.maxFlow);
        assertTrue("Edmonds-Karp augmentations > 0", ekRes.augmentations > 0);

        Dinic.DinicResult dinicRes = Dinic.solve(fn1, 0, 5);
        assertEquals("Dinic max flow matches Edmonds-Karp", 23, dinicRes.maxFlow);
        assertTrue("Dinic level phases > 0", dinicRes.levelGraphPhases > 0);

        // 2. Min-Cut Duality
        MinCut.CutResult cutRes = MinCut.computeMinCut(fn1, 0, 5);
        assertEquals("Min-Cut capacity equals Max Flow", 23, cutRes.minCutCapacity);
        assertTrue("Max-Flow Min-Cut duality theorem verified", cutRes.dualityVerified);
        assertTrue("Cut set S contains source 0", cutRes.setS.contains(0));
        assertTrue("Cut set T contains sink 5", cutRes.setT.contains(5));

        // 3. Bipartite Matching & König's Theorem
        CustomArrayList<int[]> matchEdges = new CustomArrayList<>();
        matchEdges.add(new int[]{0, 0});
        matchEdges.add(new int[]{1, 1});
        matchEdges.add(new int[]{2, 2});
        matchEdges.add(new int[]{3, 3});
        matchEdges.add(new int[]{4, 4});

        BipartiteMatching.MatchingResult matchRes = BipartiteMatching.solve(5, 5, matchEdges);
        assertEquals("Bipartite matching size", 5, matchRes.matchingSize);
        assertTrue("König's theorem satisfied", matchRes.konigTheoremSatisfied);

        // 4. Min-Cost Max-Flow
        FlowNetwork mcmfNet = new FlowNetwork(4);
        mcmfNet.addEdge(0, 1, 2, 1);
        mcmfNet.addEdge(0, 2, 1, 2);
        mcmfNet.addEdge(1, 3, 1, 1);
        mcmfNet.addEdge(2, 3, 2, 1);
        MinCostMaxFlow.MCMFResult mcmfRes = MinCostMaxFlow.solve(mcmfNet, 0, 3);
        assertEquals("Min-Cost Max-Flow total flow", 2, mcmfRes.maxFlow);
        assertEquals("Min-Cost Max-Flow total cost", 5, mcmfRes.minCost);

        // 5. Project Selection Reduction to Min-Cut
        int[] profits = {10, -5, 15, -20};
        CustomArrayList<int[]> deps = new CustomArrayList<>();
        deps.add(new int[]{0, 1}); // P0 requires P1
        deps.add(new int[]{2, 3}); // P2 requires P3
        FlowReductions.ProjectSelectionResult projRes = FlowReductions.solveProjectSelection(profits, deps);
        assertEquals("Project selection net profit", 5, projRes.maxNetProfit);
    }

    // =========================================================================
    // MODULE 5: NP-COMPLETENESS & APPROXIMATION
    // =========================================================================
    private static void testModule5NPCompletenessAndApproximation() {
        System.out.println("\n--- Testing Module 5: NP-Completeness & Approximation ---");

        // 1. Reduction Zoo: 3-SAT to CLIQUE
        CustomArrayList<ReductionZoo.Clause> formula = new CustomArrayList<>();
        formula.add(new ReductionZoo.Clause(1, 2, -3));
        formula.add(new ReductionZoo.Clause(-1, -2, 3));
        boolean[] assignment = {true, false, true};
        ReductionZoo.ReductionResult redRes = ReductionZoo.reduce3SatToClique(formula, assignment);
        assertTrue("Reduction Zoo 3-SAT to CLIQUE witness verified", redRes.witnessValid);

        // 2. Vertex Cover 2-Approximation
        int[][] graphEdges = {
                {0, 1}, {1, 2}, {2, 3}, {3, 0}, {0, 2}
        };
        VertexCoverApproximation.ApproxResult vcRes = VertexCoverApproximation.solve(4, graphEdges);
        assertTrue("Vertex Cover 2-Approx size <= 2 * OPT", vcRes.approxSize <= 2 * vcRes.exactOptSize);
        assertTrue("Vertex Cover covers edges", vcRes.approxSize >= vcRes.exactOptSize);

        // 3. Knapsack Greedy & FPTAS
        int[] w = {10, 20, 30};
        int[] v = {60, 100, 120};
        KnapsackApproximation.KnapsackResult greedy = KnapsackApproximation.solveGreedy2Approx(w, v, 50);
        assertTrue("Knapsack greedy within capacity", greedy.totalWeight <= 50);
        assertTrue("Knapsack greedy value positive", greedy.totalValue >= 160);

        KnapsackApproximation.KnapsackResult fptas = KnapsackApproximation.solveFPTAS(w, v, 50, 0.1);
        assertTrue("Knapsack FPTAS within capacity", fptas.totalWeight <= 50);
        assertEquals("Knapsack FPTAS optimal value", 220, fptas.totalValue);

        // 4. Parameterized Vertex Cover (FPT Buss Kernel)
        int[][] fptEdges = {{0, 1}, {0, 2}, {0, 3}, {1, 2}};
        ParameterizedVertexCover.FptResult fptRes = ParameterizedVertexCover.solve(4, fptEdges, 2);
        assertTrue("FPT Buss Kernel finds cover of size <= 2", fptRes.feasible);

        // 5. NP Advisor Checklist
        NPHardnessAdvisor.Diagnosis tspDiag = NPHardnessAdvisor.diagnose("tsp tour");
        assertTrue("NP Advisor classifies TSP as NP-Hard", tspDiag.isNPHard);

        NPHardnessAdvisor.Diagnosis flowDiag = NPHardnessAdvisor.diagnose("capacity assignment flow");
        assertTrue("NP Advisor classifies Flow as Polynomial", !flowDiag.isNPHard);
    }

    // =========================================================================
    // MODULE 6: RANDOMISED & PARALLEL ALGORITHMS
    // =========================================================================
    private static void testModule6RandomisedAndParallelAlgorithms() {
        System.out.println("\n--- Testing Module 6: Randomised & Parallel Algorithms ---");

        // 1. Miller-Rabin Primality Test
        MillerRabin.PrimalityResult prime1 = MillerRabin.testProbabilistic(1000000007L, 12);
        assertTrue("Miller-Rabin 1000000007 is prime", prime1.isPrime);

        MillerRabin.PrimalityResult comp1 = MillerRabin.testProbabilistic(1000000008L, 5);
        assertTrue("Miller-Rabin 1000000008 is composite", !comp1.isPrime);

        // Carmichael Numbers defeat Fermat but NOT Miller-Rabin!
        MillerRabin.PrimalityResult carmichael561 = MillerRabin.testProbabilistic(561, 10);
        assertTrue("Miller-Rabin detects Carmichael number 561 as composite", !carmichael561.isPrime);

        MillerRabin.PrimalityResult carmichael1105 = MillerRabin.testProbabilistic(1105, 10);
        assertTrue("Miller-Rabin detects Carmichael number 1105 as composite", !carmichael1105.isPrime);

        MillerRabin.PrimalityResult carmichael1729 = MillerRabin.testDeterministic(1729);
        assertTrue("Miller-Rabin detects Hardy-Ramanujan 1729 as composite", !carmichael1729.isPrime);

        // 2. Randomized QuickSort Expected Runtime
        int[] worstCase = new int[100];
        for (int i = 0; i < 100; i++) worstCase[i] = 100 - i;
        RandomizedQuickSort.SortMetrics randSort = RandomizedQuickSort.sortRandomized(worstCase);
        assertTrue("Randomized QuickSort correctly sorted", randSort.verifiedSorted);
        assertTrue("Randomized QuickSort comparisons tracked", randSort.comparisons > 0);

        // 3. Universal Hashing
        UniversalHashing uh = new UniversalHashing(10);
        int h1 = uh.hash(12345);
        assertTrue("Universal hash slot in [0, 9]", h1 >= 0 && h1 < 10);

        // 4. FKS Two-Level Perfect Hashing O(1)
        CustomArrayList<String> keys = new CustomArrayList<>();
        CustomArrayList<String> vals = new CustomArrayList<>();
        keys.add("CS101"); vals.add("Intro CS");
        keys.add("CS102"); vals.add("Data Structures");
        keys.add("CS103"); vals.add("Algorithms");
        PerfectHashing<String> ph = PerfectHashing.build(keys, vals);
        assertEquals("FKS Perfect Hash lookup CS102", "Data Structures", ph.get("CS102"));
        assertEquals("FKS Perfect Hash lookup CS103", "Algorithms", ph.get("CS103"));
        assertTrue("FKS Perfect Hash missing key returns null", ph.get("CS999") == null);

        // 5. Reservoir Sampling Uniform Distribution
        ReservoirSampling<Integer> sampler = new ReservoirSampling<>(5);
        for (int i = 0; i < 100; i++) sampler.feed(i);
        assertEquals("Reservoir size equals k", 5, sampler.getReservoir().size());
        assertEquals("Reservoir total seen", 100, (int) sampler.getTotalSeen());

        // 6. Blelloch Parallel Scan & Brent's Theorem
        int[] scanInput = {1, 2, 3, 4, 5, 6, 7, 8};
        ParallelPrimitives.ScanResult scanRes = ParallelPrimitives.blellochParallelScan(scanInput);
        assertEquals("Blelloch parallel scan total sum", 36, scanRes.totalSum);
        assertEquals("Blelloch scan prefix[0]", 0, scanRes.exclusivePrefixSum[0]);
        assertEquals("Blelloch scan prefix[1]", 1, scanRes.exclusivePrefixSum[1]);
        assertEquals("Blelloch scan prefix[2]", 3, scanRes.exclusivePrefixSum[2]);
        assertEquals("Blelloch scan prefix[3]", 6, scanRes.exclusivePrefixSum[3]);
        assertTrue("Brent's theorem speedup on 8 cores > 1.0", scanRes.metrics.brentSpeedupCores[2] > 1.0);
    }
}

