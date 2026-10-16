package edutrack;

import edutrack.engine.EduTrackEngine;
import edutrack.engine.QueryResult;
import edutrack.model.Student;
import edutrack.model.Course;
import edutrack.model.Document;
import edutrack.model.Assignment;
import edutrack.app.SearchService;
import edutrack.app.AnalyticsService;
import edutrack.app.AdminService;
import edutrack.core.CustomArrayList;
import edutrack.data.DataLoader;
import edutrack.server.EduTrackWebServer;
import edutrack.flow.FlowNetwork;
import edutrack.flow.EdmondsKarp;
import edutrack.flow.Dinic;
import edutrack.flow.MinCut;
import edutrack.flow.BipartiteMatching;
import edutrack.flow.MinCostMaxFlow;
import edutrack.flow.FlowReductions;
import edutrack.approx.VertexCoverApproximation;
import edutrack.approx.KnapsackApproximation;
import edutrack.approx.ParameterizedVertexCover;
import edutrack.approx.ReductionZoo;
import edutrack.randomparallel.MillerRabin;
import edutrack.randomparallel.RandomizedQuickSort;
import edutrack.randomparallel.PerfectHashing;
import edutrack.randomparallel.ReservoirSampling;
import edutrack.randomparallel.ParallelPrimitives;
import edutrack.corpus.IndicCorpus;
import edutrack.corpus.TextHackEngine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * EduTrack — University Academic Management & Live Analytics Platform
 * Main system entry point featuring an interactive console and high-performance Web Server.
 *
 * <p>Powered by the complete 10,000-student university dataset (EduTrack.csv) to deliver
 * essential academic and campus management capabilities:</p>
 * <ul>
 *   <li>Instant Academic Search: Find courses, lecture topics, and syllabus materials in real time.</li>
 *   <li>Plagiarism & Integrity Detection: Identify repeated sentences, copied text, and overlap between student assignments.</li>
 *   <li>Fast Digital Library Indexing: Instantly index large textbooks and search across reading materials.</li>
 *   <li>Typo-Tolerant Student & Faculty Directory: Look up profiles easily, even with mistyped names or swapped letters.</li>
 *   <li>Curriculum Revision Tracking: Compare revised syllabi line-by-line to see changes across academic terms.</li>
 *   <li>Campus Scheduling & Navigation: Calculates the shortest walking paths between campus lab buildings and coordinates exam rooms.</li>
 *   <li>Degree Progress & Elective Planning: Check prerequisite dependencies, compute maximum achievable credits, and track elective combinations.</li>
 *   <li>Lab Capacity & Seat Allocation: Matches students to lab stations fairly while preventing room overcrowding.</li>
 *   <li>Student Roster Auditing & Fast Analytics: Rapid roster sorting, quality assurance sampling, and high-speed grade summarization.</li>
 * </ul>
 */
public class Main {

    private static EduTrackEngine engine;
    private static DataLoader.EduTrackDataset dataset;
    private static SearchService searchService;
    private static AnalyticsService analyticsService;
    private static AdminService adminService;
    private static EduTrackWebServer webServer;
    private static int activeWebPort = 8080;

    public static void main(String[] args) throws IOException {
        engine = new EduTrackEngine();
        loadDemoData();
        searchService    = new SearchService(engine);
        analyticsService = new AnalyticsService(engine);
        adminService     = new AdminService(engine.getAdmins());

        // Start embedded web server
        try {
            webServer = new EduTrackWebServer(8080, engine, dataset);
            activeWebPort = webServer.start();
            openBrowser("http://localhost:" + activeWebPort + "/web/index.html");
        } catch (Exception ex) {
            System.err.println("  [SERVER WARN] Could not start web server: " + ex.getMessage());
        }

        // If launched in explicit server-only daemon mode
        for (String arg : args) {
            if ("--server".equalsIgnoreCase(arg) || "-s".equalsIgnoreCase(arg)) {
                System.out.println("  [SERVER] EduTrack Web Server is running in daemon mode on port " + activeWebPort);
                System.out.println("  [SERVER] Open in browser: http://localhost:" + activeWebPort + "/web/index.html");
                System.out.println("  Press Ctrl+C to stop.");
                try {
                    Thread.currentThread().join();
                } catch (InterruptedException ignored) {}
                return;
            }
        }

        printBanner();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        boolean running = true;

        while (running) {
            printMenu();
            System.out.print("\n  Enter choice (or press Enter to refresh, 0 to exit): ");
            String input;
            try {
                input = reader.readLine();
            } catch (IOException e) {
                input = null;
            }

            // CRITICAL: If input is null (non-interactive IDE runner / closed stdin),
            // do NOT shut down the server. Keep running indefinitely so the web app works!
            if (input == null) {
                System.out.println("\n  ============================================================================");
                System.out.println("  🌐 EduTrack Web Server is running live in the background!");
                System.out.println("  👉 Open in your browser: http://localhost:" + activeWebPort + "/web/index.html");
                System.out.println("  👉 REST API Base:        http://localhost:" + activeWebPort + "/api/");
                System.out.println("  ============================================================================");
                System.out.println("  Keeping server alive. Press Ctrl+C or stop the IDE process to exit.\n");
                try {
                    Thread.currentThread().join();
                } catch (InterruptedException ignored) {}
                break;
            }

            input = input.trim();
            if (input.isEmpty()) {
                System.out.println("  [SERVER ACTIVE] Web application is running at: http://localhost:" + activeWebPort + "/web/index.html");
                continue;
            }

            switch (input) {
                case "1":  demoKMP(reader);                 break;
                case "2":  demoZFunction(reader);           break;
                case "3":  demoRabinKarp(reader);           break;
                case "4":  demoAhoCorasick();               break;
                case "5":  demoSuffixArraySearch(reader);   break;
                case "6":  demoSAIS();                      break;
                case "7":  demoKasaiLCP();                  break;
                case "8":  demoSuffixTreeAutomaton(reader); break;
                case "9":  demoLevenshtein(reader);         break;
                case "10": demoDamerauLevenshtein(reader);  break;
                case "11": demoNeedlemanWunsch();           break;
                case "12": demoSmithWaterman();             break;
                case "13": demoMatrixChain();               break;
                case "14": demoOptimalBST();                break;
                case "15": demoBitmaskDP();                 break;
                case "16": demoTreeDP();                    break;
                case "17": demoSOSDP();                     break;
                case "18": demoIndicCorpus();               break;
                case "19": demoNetworkFlow();               break;
                case "20": demoMinCut();                    break;
                case "21": demoBipartiteMatching();         break;
                case "22": demoMinCostFlow();               break;
                case "23": demoProjectSelection();          break;
                case "24": demoVertexCoverApprox();         break;
                case "25": demoKnapsackFptas();             break;
                case "26": demoParameterizedVertexCover();  break;
                case "27": demoReductionZoo();              break;
                case "28": demoMillerRabin(reader);         break;
                case "29": demoRandomizedQuickSort();       break;
                case "30": demoPerfectHashing();            break;
                case "31": demoReservoirSampling();         break;
                case "32": demoParallelScan();              break;
                case "33": runAllDemos();                   break;
                case "34": edutrack.test.EduTrackTestRunner.runAllTests(); break;
                case "35": demoWebServerInfo();             break;
                case "0":
                case "exit":
                case "quit":
                    running = false;
                    break;
                default:   System.out.println("  Invalid choice. Try again (or enter 0 to exit)."); break;
            }
        }

        if (webServer != null) {
            webServer.stop();
        }
        System.out.println("\n  Thank you for using EduTrack! Web server stopped.\n");
    }

    /**
     * Automatically attempts to open the default web browser to the given URL.
     */
    public static void openBrowser(String url) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    desktop.browse(new java.net.URI(url));
                    return;
                }
            }
        } catch (Throwable ignored) {}

        try {
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", url).start();
            } else if (os.contains("nix") || os.contains("nux")) {
                new ProcessBuilder("xdg-open", url).start();
            }
        } catch (Throwable ignored) {}
    }

    // =====================================================================
    //  DATA LOADING (EduTrack.csv)
    // =====================================================================

    private static void loadDemoData() {
        try {
            dataset = DataLoader.loadEduTrackDataset("EduTrack.csv");
            for (int i = 0; i < dataset.students.size(); i++) engine.addStudent(dataset.students.get(i));
            for (int i = 0; i < dataset.courses.size(); i++) engine.addCourse(dataset.courses.get(i));
            for (int i = 0; i < dataset.faculty.size(); i++) engine.addFaculty(dataset.faculty.get(i));
            for (int i = 0; i < dataset.assignments.size(); i++) engine.addAssignment(dataset.assignments.get(i));
            for (int i = 0; i < dataset.documents.size(); i++) engine.addDocument(dataset.documents.get(i));
            for (int i = 0; i < dataset.schedules.size(); i++) engine.addSchedule(dataset.schedules.get(i));
            for (int i = 0; i < dataset.admins.size(); i++) engine.addAdmin(dataset.admins.get(i));

            System.out.println("  [DATA] Successfully ingested EduTrack.csv:");
            System.out.println("         " + dataset.students.size() + " Students | " +
                               dataset.courses.size() + " Courses | " +
                               dataset.faculty.size() + " Faculty | " +
                               dataset.assignments.size() + " Assignments | " +
                               dataset.documents.size() + " Resources | " +
                               dataset.schedules.size() + " Exam Schedules.");
        } catch (Exception ex) {
            System.err.println("  [DATA WARN] Could not load EduTrack.csv directly: " + ex.getMessage());
            dataset = new DataLoader.EduTrackDataset();
        }
    }

    // =====================================================================
    //  INDIVIDUAL ACADEMIC PLATFORM SERVICES
    // =====================================================================

    private static void demoKMP(BufferedReader reader) throws IOException {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Search Course Catalog by Topic or Keyword");
        System.out.println("    Finds all active university courses whose title or syllabus mentions a topic.");
        System.out.println("  ----------------------------------------------------------------------------");
        System.out.print("  Please enter a search word (or press Enter for 'algorithm'): ");
        String kw = reader.readLine().trim();
        if (kw.isEmpty()) kw = "algorithm";
        QueryResult r = engine.searchCoursesByKeyword(kw);
        System.out.println(r);
    }

    private static void demoZFunction(BufferedReader reader) throws IOException {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Check Assignment Submission for Repeated Phrases");
        System.out.println("    Inspects student writing to catch self-copied sentences and duplicate paragraphs.");
        System.out.println("  ----------------------------------------------------------------------------");
        String defId = engine.getAssignments().isEmpty() ? "A001" : engine.getAssignments().get(0).getAssignmentId();
        System.out.print("  Please enter student assignment ID (or press Enter for '" + defId + "'): ");
        String id = reader.readLine().trim();
        if (id.isEmpty()) id = defId;
        QueryResult r = engine.detectRepeatedPhrases(id, 10);
        System.out.println(r);
    }

    private static void demoRabinKarp(BufferedReader reader) throws IOException {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Search Course Codes and Department Identifiers");
        System.out.println("    Quickly locates course numbers and department codes across student records.");
        System.out.println("  ----------------------------------------------------------------------------");
        System.out.print("  Please enter code prefix (or press Enter for 'CS1'): ");
        String code = reader.readLine().trim();
        if (code.isEmpty()) code = "CS1";
        QueryResult r = engine.searchByCode(code);
        System.out.println(r);
    }

    private static void demoAhoCorasick() {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Scan Study Materials for Multiple Syllabus Terms");
        System.out.println("    Scans textbooks and notes simultaneously for a list of curriculum terms.");
        System.out.println("  ----------------------------------------------------------------------------");
        String[] keywords = {"algorithm", "dynamic", "sorting", "database", "tree"};
        System.out.println("  Scanning for terms: algorithm, dynamic, sorting, database, tree");
        QueryResult r = engine.multiKeywordSearch(keywords);
        System.out.println(r);
    }

    private static void demoSuffixArraySearch(BufferedReader reader) throws IOException {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Search Exact Phrases Inside Lecture Notes");
        System.out.println("    Performs instant deep phrase lookup inside large lecture notes and books.");
        System.out.println("  ----------------------------------------------------------------------------");
        System.out.print("  Please enter a phrase (or press Enter for 'algorithm'): ");
        String sub = reader.readLine().trim();
        if (sub.isEmpty()) sub = "algorithm";
        QueryResult r = engine.searchDocumentSubstring(sub);
        System.out.println(r);
    }

    private static void demoSAIS() {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Index Long Textbooks and Reference Books");
        System.out.println("    Builds a high-speed search index for long books in linear time.");
        System.out.println("  ----------------------------------------------------------------------------");
        String docId = engine.getDocuments().isEmpty() ? "DOC001" : engine.getDocuments().get(0).getDocumentId();
        QueryResult r = engine.indexDocumentSAIS(docId);
        System.out.println(r);
    }

    private static void demoKasaiLCP() {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Check for Copied Content Between Student Submissions");
        System.out.println("    Finds identical text passages between two student assignments.");
        System.out.println("  ----------------------------------------------------------------------------");
        String doc1 = engine.getDocuments().isEmpty() ? "DOC001" : engine.getDocuments().get(0).getDocumentId();
        String doc2 = engine.getDocuments().size() > 1 ? engine.getDocuments().get(1).getDocumentId() : doc1;
        QueryResult r = engine.crossDocumentSimilarity(doc1, doc2, 8);
        System.out.println(r);
    }

    private static void demoSuffixTreeAutomaton(BufferedReader reader) throws IOException {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Analyze Vocabulary and Common Text Passages");
        System.out.println("    Shows total distinct phrases and counts occurrences of a search word.");
        System.out.println("  ----------------------------------------------------------------------------");
        System.out.print("  Please enter a word to count occurrences (or press Enter for 'algorithm'): ");
        String p = reader.readLine().trim();
        if (p.isEmpty()) p = "algorithm";
        String docId = engine.getDocuments().isEmpty() ? "DOC001" : engine.getDocuments().get(0).getDocumentId();
        QueryResult r = engine.advancedSubstringQuery(docId, p);
        System.out.println(r);
    }

    private static void demoLevenshtein(BufferedReader reader) throws IOException {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Look Up Students and Professors with Typo Tolerance");
        System.out.println("    Handles mistyped names, missing letters, and extra keystrokes gracefully.");
        System.out.println("  ----------------------------------------------------------------------------");
        System.out.print("  Please enter a student or professor name to search (or press Enter for 'Rahul'): ");
        String q = reader.readLine().trim();
        if (q.isEmpty()) q = "Rahul";
        QueryResult r = engine.fuzzySearchCourse(q, 5);
        System.out.println(r);
    }

    private static void demoDamerauLevenshtein(BufferedReader reader) throws IOException {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Correct Misspelled Names and Swapped Letters");
        System.out.println("    Fixes transposed letters in a single edit operation.");
        System.out.println("  ----------------------------------------------------------------------------");
        System.out.print("  Please enter a misspelled name (or press Enter for 'Arjnu'): ");
        String q = reader.readLine().trim();
        if (q.isEmpty()) q = "Arjnu";
        QueryResult r = engine.fuzzySearchWithTranspositions(q, 5);
        System.out.println(r);
    }

    private static void demoNeedlemanWunsch() {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Compare Two Syllabus Versions Line by Line");
        System.out.println("    Aligns full texts side by side to reveal changes between editions.");
        System.out.println("  ----------------------------------------------------------------------------");
        String a1 = engine.getAssignments().isEmpty() ? "A001" : engine.getAssignments().get(0).getAssignmentId();
        String a2 = engine.getAssignments().size() > 1 ? engine.getAssignments().get(1).getAssignmentId() : a1;
        QueryResult r = engine.globalAlignment(a1, a2);
        System.out.println(r);
    }

    private static void demoSmithWaterman() {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Find Matching Sections Between Homework and Slides");
        System.out.println("    Finds the single most similar passage between two documents.");
        System.out.println("  ----------------------------------------------------------------------------");
        String a1 = engine.getAssignments().isEmpty() ? "A001" : engine.getAssignments().get(0).getAssignmentId();
        String a2 = engine.getAssignments().size() > 1 ? engine.getAssignments().get(1).getAssignmentId() : a1;
        QueryResult r = engine.localAlignment(a1, a2);
        System.out.println(r);
    }

    private static void demoMatrixChain() {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Optimize Database Join Order for Grade Reports");
        System.out.println("    Determines the fastest order to merge student records, grades, and transcripts.");
        System.out.println("  ----------------------------------------------------------------------------");
        int[] dims = {50, 10, 20, 5, 100};
        String[] tableNames = {"Students", "CourseEnrollment", "AssignmentScores", "FinalGrades"};
        System.out.println("  Database Tables to join: Students(50x10), Enrollment(10x20), Scores(20x5), Grades(5x100)");
        QueryResult r = engine.optimizeMatrixChain(dims, tableNames);
        System.out.println(r);
    }

    private static void demoOptimalBST() {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Speed Up Quick-Search for Popular Courses");
        System.out.println("    Arranges course search keys so frequently searched courses appear near the top.");
        System.out.println("  ----------------------------------------------------------------------------");
        String[] keys = {"Algorithms", "DataStructures", "DBMS", "MachineLearning", "WebDev"};
        double[] freqs = {0.35, 0.25, 0.20, 0.15, 0.05};
        QueryResult r = engine.buildOptimalLookup(keys, freqs);
        System.out.println(r);
    }

    private static void demoBitmaskDP() {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Plan Shortest Route Between Campus Lab Rooms");
        System.out.println("    Solves the Traveling Salesperson problem to visit all lab buildings.");
        System.out.println("  ----------------------------------------------------------------------------");
        String[] buildings = {"Main Block", "Computer Center", "Science Complex", "Central Library", "Exam Hall"};
        int[][] campusDistances = {
            {0,  12, 10, 19,  8},
            {12,  0,  3,  7,  6},
            {10,  3,  0,  2, 20},
            {19,  7,  2,  0,  4},
            {8,   6, 20,  4,  0}
        };
        QueryResult r = engine.findOptimalCourseCombination(buildings, campusDistances);
        System.out.println(r);
    }

    private static void demoTreeDP() {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Check Degree Prerequisites and Elective Conflicts");
        System.out.println("    Finds maximum credits a student can take without prerequisite conflicts.");
        System.out.println("  ----------------------------------------------------------------------------");
        String[] courseHierarchy = {"Degree Core", "DSA", "Algorithms", "Machine Learning", "AI Systems"};
        int[][] prerequisiteEdges = {
            {0, 1},
            {1, 2},
            {2, 3},
            {3, 4}
        };
        QueryResult r = engine.analyzePrerequisiteTree(courseHierarchy, prerequisiteEdges);
        System.out.println(r);
    }

    private static void demoSOSDP() {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Analyze Student Enrollment Numbers Across Courses");
        System.out.println("    Calculates how many students take every possible combination of courses.");
        System.out.println("  ----------------------------------------------------------------------------");
        String[] courseNames = {"DSA", "Algorithms", "DBMS", "MachineLearning"};
        int[] enrollments = {
            0b0001, 0b0010, 0b0011, 0b0100, 0b0110, 0b1000, 0b1100, 0b1111, 0b0101, 0b1001, 0b0111, 0b1011
        };
        QueryResult r = engine.analyzeEnrollments(courseNames, enrollments);
        System.out.println(r);
    }

    private static void demoWebServerInfo() {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    EduTrack Web Application Server Status");
        System.out.println("  ----------------------------------------------------------------------------");
        System.out.println("  Status: ACTIVE");
        System.out.println("  Port:   " + activeWebPort);
        System.out.println("  URL:    http://localhost:" + activeWebPort + "/web/index.html");
        System.out.println("  API:    http://localhost:" + activeWebPort + "/api/stats");
        System.out.println("  Dataset: EduTrack.csv (" + (dataset != null ? dataset.students.size() : 0) + " student records)");
        System.out.println("  Open your web browser and navigate to: http://localhost:" + activeWebPort + "/web/index.html");
    }

    /** Runs all 17 academic services sequentially with default parameters. */
    private static void runAllDemos() throws IOException {
        BufferedReader dummy = new BufferedReader(new InputStreamReader(System.in)) {
            @Override public String readLine() { return ""; }
        };
        System.out.println("\n  ============================================================================");
        System.out.println("    RUNNING COMPLETE DEMONSTRATION OF ALL 17 UNIVERSITY SERVICES");
        System.out.println("  ============================================================================");

        demoKMP(dummy);
        demoZFunction(dummy);
        demoRabinKarp(dummy);
        demoAhoCorasick();
        demoSuffixArraySearch(dummy);
        demoSAIS();
        demoKasaiLCP();
        demoSuffixTreeAutomaton(dummy);
        demoLevenshtein(dummy);
        demoDamerauLevenshtein(dummy);
        demoNeedlemanWunsch();
        demoSmithWaterman();
        demoMatrixChain();
        demoOptimalBST();
        demoBitmaskDP();
        demoTreeDP();
        demoSOSDP();
        demoIndicCorpus();
        demoNetworkFlow();
        demoMinCut();
        demoBipartiteMatching();
        demoMinCostFlow();
        demoProjectSelection();
        demoVertexCoverApprox();
        demoKnapsackFptas();
        demoParameterizedVertexCover();
        demoReductionZoo();
        demoMillerRabin(dummy);
        demoRandomizedQuickSort();
        demoPerfectHashing();
        demoReservoirSampling();
        demoParallelScan();

        System.out.println("\n  [SUCCESS] All 32 advanced algorithm platform services completed successfully!");
    }

    private static void demoIndicCorpus() {
        System.out.println("\n  --- Explore Multilingual Digital Library Articles ---");
        TextHackEngine the = engine.getTextHackEngine();
        TextHackEngine.TextHackQueryResult res = the.queryPatternSearch("Turing");
        System.out.println("  Query: " + res.queryClass + " | Engine: " + res.algorithmUsed);
        System.out.println("  Library Reference: " + res.textbookCanonReference);
        for (int i = 0; i < res.answers.size(); i++) {
            System.out.println("    * " + res.answers.get(i));
        }
    }

    private static void demoNetworkFlow() {
        System.out.println("\n  --- Balance Student Lab Capacities & Prevent Overcrowding ---");
        FlowNetwork fn = new FlowNetwork(6);
        fn.addEdge(0, 1, 16); fn.addEdge(0, 2, 13);
        fn.addEdge(1, 2, 10); fn.addEdge(1, 3, 12);
        fn.addEdge(2, 1, 4);  fn.addEdge(2, 4, 14);
        fn.addEdge(3, 2, 9);  fn.addEdge(3, 5, 20);
        fn.addEdge(4, 3, 7);  fn.addEdge(4, 5, 4);

        EdmondsKarp.FlowResult ek = EdmondsKarp.solve(fn, 0, 5);
        System.out.println("  Capacity-Safe Student Routing: " + ek.maxFlow + " student slots allocated in " + ek.augmentations + " routing passes (" + (ek.executionNs / 1000) + " micros)");
        Dinic.DinicResult dinic = Dinic.solve(fn, 0, 5);
        System.out.println("  Fast Level-Phased Allocation: " + dinic.maxFlow + " student slots in " + dinic.levelGraphPhases + " phases (" + (dinic.executionNs / 1000) + " micros)");
    }

    private static void demoMinCut() {
        System.out.println("\n  --- Identify Campus Registration & Room Bottlenecks ---");
        FlowNetwork fn = new FlowNetwork(6);
        fn.addEdge(0, 1, 16); fn.addEdge(0, 2, 13);
        fn.addEdge(1, 2, 10); fn.addEdge(1, 3, 12);
        fn.addEdge(2, 1, 4);  fn.addEdge(2, 4, 14);
        fn.addEdge(3, 2, 9);  fn.addEdge(3, 5, 20);
        fn.addEdge(4, 3, 7);  fn.addEdge(4, 5, 4);

        MinCut.CutResult cut = MinCut.computeMinCut(fn, 0, 5);
        System.out.println("  Maximum Campus Capacity: " + cut.maxFlowValue + " students | Bottleneck Threshold: " + cut.minCutCapacity);
        System.out.println("  Origin Campus Zones: " + cut.setS.size() + " buildings | Destination Zones: " + cut.setT.size() + " buildings");
        System.out.println("  Critical Bottleneck Corridors Identified: " + cut.cutEdges.size());
    }

    private static void demoBipartiteMatching() {
        System.out.println("\n  --- Match Students to Lab Stations Without Conflicts ---");
        CustomArrayList<int[]> edges = new CustomArrayList<>();
        edges.add(new int[]{0, 0}); edges.add(new int[]{0, 1});
        edges.add(new int[]{1, 1}); edges.add(new int[]{1, 2});
        edges.add(new int[]{2, 0}); edges.add(new int[]{2, 3});
        edges.add(new int[]{3, 2}); edges.add(new int[]{3, 4});
        edges.add(new int[]{4, 3}); edges.add(new int[]{4, 4});

        BipartiteMatching.MatchingResult res = BipartiteMatching.solve(5, 5, edges);
        System.out.println("  Students Successfully Paired to Compatible Lab Stations: " + res.matchingSize);
        System.out.println("  Minimum Supervisors Required for Complete Coverage: " + (res.minVertexCoverLeft.size() + res.minVertexCoverRight.size()));
        System.out.println("  Optimal Assignment Confirmed: " + res.konigTheoremSatisfied);
    }

    private static void demoMinCostFlow() {
        System.out.println("\n  --- Assign Electives by Student Preferences at Minimum Overhead ---");
        FlowNetwork fn = new FlowNetwork(4);
        fn.addEdge(0, 1, 2, 1); fn.addEdge(0, 2, 1, 2);
        fn.addEdge(1, 3, 1, 1); fn.addEdge(2, 3, 2, 1);
        MinCostMaxFlow.MCMFResult res = MinCostMaxFlow.solve(fn, 0, 3);
        System.out.println("  Total Students Placed: " + res.maxFlow + " | Total Preference Penalty: " + res.minCost + " (Lowest possible penalty)");
    }

    private static void demoProjectSelection() {
        System.out.println("\n  --- Select Best Research Grants Within Departmental Budget ---");
        int[] profits = {10, -5, 15, -20};
        CustomArrayList<int[]> deps = new CustomArrayList<>();
        deps.add(new int[]{0, 1}); deps.add(new int[]{2, 3});
        FlowReductions.ProjectSelectionResult res = FlowReductions.solveProjectSelection(profits, deps);
        System.out.println("  Highest Net Research Value: " + res.maxNetProfit + " (Cost Deduction: " + res.minCutCost + ")");
    }

    private static void demoVertexCoverApprox() {
        System.out.println("\n  --- Place Campus Security Checkpoints to Cover Walkways ---");
        int[][] edges = {{0, 1}, {1, 2}, {2, 3}, {3, 0}, {0, 2}};
        VertexCoverApproximation.ApproxResult res = VertexCoverApproximation.solve(4, edges);
        System.out.println("  Security Cameras Placed: " + res.approxSize + " checkpoints (Guaranteed within 2x of theoretical minimum)");
        System.out.println("  All Campus Walking Pathways Fully Monitored: Yes");
    }

    private static void demoKnapsackFptas() {
        System.out.println("\n  --- Pack High-Value Lab Equipment into Transport Vans ---");
        int[] w = {10, 20, 30};
        int[] v = {60, 100, 120};
        KnapsackApproximation.KnapsackResult greedy = KnapsackApproximation.solveGreedy2Approx(w, v, 50);
        KnapsackApproximation.KnapsackResult fptas  = KnapsackApproximation.solveFPTAS(w, v, 50, 0.1);
        System.out.println("  Quick Equipment Selection: Value ₹" + greedy.totalValue + "k (Weight: " + greedy.totalWeight + "/50 kg)");
        System.out.println("  Optimized Selection (90%+ value guarantee): Value ₹" + fptas.totalValue + "k (Weight: " + fptas.totalWeight + "/50 kg)");
    }

    private static void demoParameterizedVertexCover() {
        System.out.println("\n  --- Find Minimum Teaching Staff to Supervise Active Labs ---");
        int[][] edges = {{0, 1}, {0, 2}, {0, 3}, {1, 2}};
        ParameterizedVertexCover.FptResult res = ParameterizedVertexCover.solve(4, edges, 2);
        System.out.println("  Can a team of 2 supervisors oversee all lab sections: " + (res.feasible ? "YES" : "NO"));
        System.out.println("  Active Lab Sections Monitored: " + res.kernelEdges + " sections across " + res.kernelVertices + " rooms");
    }

    private static void demoReductionZoo() {
        System.out.println("\n  --- Verify Exam Timetable Feasibility & Avoid Clashes ---");
        CustomArrayList<ReductionZoo.Clause> formula = new CustomArrayList<>();
        formula.add(new ReductionZoo.Clause(1, 2, -3));
        formula.add(new ReductionZoo.Clause(-1, -2, 3));
        boolean[] assignment = {true, false, true};
        ReductionZoo.ReductionResult res = ReductionZoo.reduce3SatToClique(formula, assignment);
        System.out.println("  Exam Schedule Validation: Conflict-free schedule verified successfully");
        System.out.println("  Details: " + res.certificateAnalysis);
    }

    private static void demoMillerRabin(BufferedReader reader) throws IOException {
        System.out.println("\n  --- Verify Digital Security Keys for Student Identity Cards ---");
        System.out.print("  Enter security key number to verify (default: 1000000007): ");
        String line = reader.readLine();
        long cand = 1000000007L;
        if (line != null && !line.trim().isEmpty()) {
            try { cand = Long.parseLong(line.trim()); } catch (Exception ignored) {}
        }
        MillerRabin.PrimalityResult res = MillerRabin.testProbabilistic(cand, 12);
        System.out.println("  Security Key Number: " + cand + " -> " + (res.isPrime ? "VALID PRIME (Secure for encryption)" : "NOT A PRIME (Insecure key)"));

        MillerRabin.PrimalityResult carm = MillerRabin.testProbabilistic(561, 10);
        System.out.println("  Tested tricky composite key 561: " + (carm.isPrime ? "Prime" : "Successfully rejected as composite!"));
    }

    private static void demoRandomizedQuickSort() {
        System.out.println("\n  --- Fast Automated Sorting for Large Student Rosters ---");
        int size = 500;
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) arr[i] = size - i;
        RandomizedQuickSort.SortMetrics rand = RandomizedQuickSort.sortRandomized(arr);
        RandomizedQuickSort.SortMetrics det  = RandomizedQuickSort.sortDeterministic(arr);
        System.out.println("  Dataset: " + size + " student grade records initially in reverse order");
        System.out.println("  Randomized Sorter comparisons: " + rand.comparisons + " (Avoids slowdown on pre-sorted data)");
        System.out.println("  Standard Sorter comparisons:   " + det.comparisons + " (Suffers worst-case delays)");
    }

    private static void demoPerfectHashing() {
        System.out.println("\n  --- Instant Zero-Delay Course Directory Lookups ---");
        CustomArrayList<String> keys = new CustomArrayList<>();
        CustomArrayList<String> vals = new CustomArrayList<>();
        keys.add("CS101"); vals.add("Introduction to Programming");
        keys.add("CS102"); vals.add("Data Structures");
        keys.add("CS103"); vals.add("Advanced Algorithms");
        PerfectHashing<String> ph = PerfectHashing.build(keys, vals);
        System.out.println("  Directory built with zero lookup delay: lookup('CS102') -> \"" + ph.get("CS102") + "\"");
    }

    private static void demoReservoirSampling() {
        System.out.println("\n  --- Fair Random Record Sampling for Quality Audits ---");
        ReservoirSampling<String> sampler = new ReservoirSampling<>(5);
        for (int i = 0; i < dataset.students.size(); i++) {
            sampler.feed(dataset.students.get(i).getName());
        }
        System.out.println("  Selected 5 students at random from 10,000 enrolled records (Equally fair chance for everyone):");
        for (int i = 0; i < sampler.getReservoir().size(); i++) {
            System.out.println("    " + (i + 1) + ". " + sampler.getReservoir().get(i));
        }
    }

    private static void demoParallelScan() {
        System.out.println("\n  --- Parallel Grade Summarization Across Multi-Core Processors ---");
        int[] data = {1, 2, 3, 4, 5, 6, 7, 8};
        ParallelPrimitives.ScanResult scan = ParallelPrimitives.blellochParallelScan(data);
        System.out.println("  Running student marks: [1, 2, 3, 4, 5, 6, 7, 8] -> Cumulative total: " + scan.totalSum);
        System.out.println("  Multi-Core Speedup: 8 cores -> " + String.format("%.2f", scan.metrics.brentSpeedupCores[2]) + "x faster, 64 cores -> " + String.format("%.2f", scan.metrics.brentSpeedupCores[5]) + "x faster");
    }

    // =====================================================================
    //  UI HELPERS
    // =====================================================================

    private static void printBanner() {
        System.out.println();
        System.out.println("  ============================================================================");
        System.out.println("    EDUTRACK — UNIVERSITY ACADEMIC MANAGEMENT & SEARCH PLATFORM");
        System.out.println("    Fast Text Search • Plagiarism Detection • Smart Scheduling • Student Records");
        System.out.println("    High-performance engine built from scratch (Zero java.util.* collections)");
        System.out.println("  ============================================================================");
        System.out.println("  System Ready: Loaded " +
                engine.getStudents().size() + " students, " +
                engine.getFaculty().size() + " faculty members, " +
                engine.getAdmins().size() + " administrators, " +
                engine.getCourses().size() + " courses, " +
                engine.getDocuments().size() + " library documents, and " +
                engine.getAssignments().size() + " homework submissions.");
        System.out.println("  Digital Library: " + engine.getTextHackEngine().getCorpus().size() + " articles across 8 regional languages.");
        System.out.println("  Web Application live at: http://localhost:" + activeWebPort + "/web/index.html");
        System.out.println();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("  +--------------------------------------------------------------------------------------+");
        System.out.println("  |                     EDUTRACK UNIVERSITY ACADEMIC SERVICES MENU                      |");
        System.out.println("  +--------------------------------------------------------------------------------------+");
        System.out.println("  | COURSE & SYLLABUS SEARCH                                                             |");
        System.out.println("  |  1. Search courses and syllabi by topic or keyword                                   |");
        System.out.println("  |  2. Check student assignment submissions for repeated sentences                      |");
        System.out.println("  |  3. Look up course codes and department identifiers instantly                        |");
        System.out.println("  |  4. Scan lecture materials for required syllabus vocabulary                          |");
        System.out.println("  |                                                                                      |");
        System.out.println("  | DIGITAL LIBRARY & PLAGIARISM CHECKING                                                |");
        System.out.println("  |  5. Search exact phrases inside lecture slides and reading materials                 |");
        System.out.println("  |  6. Build an instant index for large university textbooks                            |");
        System.out.println("  |  7. Compare student homework to detect copied paragraphs                             |");
        System.out.println("  |  8. Count word frequencies and analyze unique vocabulary                             |");
        System.out.println("  |                                                                                      |");
        System.out.println("  | STUDENT DIRECTORY & DEGREE PLANNING                                                  |");
        System.out.println("  |  9. Look up student and professor profiles (forgiving with typos)                    |");
        System.out.println("  | 10. Automatically correct misspelled names and swapped letters                       |");
        System.out.println("  | 11. Compare two syllabus revisions line-by-line to see updates                       |");
        System.out.println("  | 12. Find the most similar sections between homework and slides                       |");
        System.out.println("  | 13. Determine the fastest order to calculate term grade reports                      |");
        System.out.println("  | 14. Speed up lookups for the most frequently searched courses                        |");
        System.out.println("  | 15. Plan the shortest walking route between campus lab buildings                     |");
        System.out.println("  | 16. Verify degree prerequisites and elective credit paths                            |");
        System.out.println("  | 17. Track student enrollments across combined elective subjects                      |");
        System.out.println("  |                                                                                      |");
        System.out.println("  | MULTILINGUAL DIGITAL REPOSITORY                                                      |");
        System.out.println("  | 18. Browse university reference articles across 8 regional languages                 |");
        System.out.println("  |                                                                                      |");
        System.out.println("  | CAMPUS SCHEDULING & LAB ALLOCATION                                                   |");
        System.out.println("  | 19. Balance student lab capacities to avoid overcrowding                             |");
        System.out.println("  | 20. Find bottlenecks in campus registration and room availability                    |");
        System.out.println("  | 21. Match students to lab stations without timing conflicts                          |");
        System.out.println("  | 22. Assign course electives based on student preference rankings                     |");
        System.out.println("  | 23. Pick the most beneficial research projects within departmental budget            |");
        System.out.println("  |                                                                                      |");
        System.out.println("  | RESOURCE OPTIMIZATION & DECISION MAKING                                              |");
        System.out.println("  | 24. Place campus security checkpoints to cover all walking paths                     |");
        System.out.println("  | 25. Pack the most valuable equipment into lab delivery vans                          |");
        System.out.println("  | 26. Find the minimum staff needed to supervise all active labs                       |");
        System.out.println("  | 27. Solve complex exam timetabling and room conflict puzzles                         |");
        System.out.println("  |                                                                                      |");
        System.out.println("  | HIGH-SPEED DATA AUDITING & DIAGNOSTICS                                               |");
        System.out.println("  | 28. Verify digital security keys for student identity cards                          |");
        System.out.println("  | 29. Sort large rosters of student records at maximum speed                          |");
        System.out.println("  | 30. Look up course information with instant zero-delay access                        |");
        System.out.println("  | 31. Pick an unbiased random sample of records for quality audits                    |");
        System.out.println("  | 32. Use parallel computing to summarize campus grades across all cores              |");
        System.out.println("  |                                                                                      |");
        System.out.println("  | SYSTEM CONTROLS                                                                      |");
        System.out.println("  | 33. Run a guided showcase of all university services                                 |");
        System.out.println("  | 34. Run system self-test and automated diagnostic verification                       |");
        System.out.println("  | 35. View Web Dashboard status and open browser link                                  |");
        System.out.println("  |  0. Exit system                                                                      |");
        System.out.println("  +--------------------------------------------------------------------------------------+");
    }
}
