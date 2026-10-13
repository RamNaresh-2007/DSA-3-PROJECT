package edutrack.test;

import edutrack.core.*;
import edutrack.string.*;
import edutrack.suffix.*;
import edutrack.dp.*;
import edutrack.engine.*;
import edutrack.model.*;
import edutrack.app.*;
import edutrack.data.DataLoader;

/**
 * Comprehensive Automated Test Suite for EduTrack.
 *
 * <p>Validates all hand-built data structures, algorithms, service layers,
 * and query engines with zero external libraries or java.util.* dependencies.</p>
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
        System.out.println("      EDUTRACK AUTOMATED TEST RUNNER (ALL MODULES 1 - 3)      ");
        System.out.println("===============================================================\n");

        testCoreDataStructures();
        testModule1StringAlgorithms();
        testModule2SuffixStructures();
        testModule3AdvancedDP();
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
}
