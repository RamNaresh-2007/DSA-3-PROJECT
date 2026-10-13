package edutrack;

import edutrack.engine.EduTrackEngine;
import edutrack.engine.QueryResult;
import edutrack.engine.QueryType;
import edutrack.model.*;
import edutrack.app.*;
import edutrack.core.CustomArrayList;
import edutrack.data.DataLoader;
import edutrack.server.EduTrackWebServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * EduTrack — Intelligent University Academic Platform
 * Main entry point with interactive CLI and embedded high-performance Web Server.
 *
 * <p>Ingests the real 10,000-record EduTrack.csv dataset into pure DSA structures
 * and demonstrates all query types across 3 modules:</p>
 * <ul>
 *   <li>Module 2 — String Algorithms (CO1): KMP, Z-function, Rabin-Karp, Aho-Corasick</li>
 *   <li>Module 2 — Suffix Structures (CO2): Suffix Array, SA-IS, Kasai LCP, Suffix Tree, Suffix Automaton</li>
 *   <li>Module 3 — Advanced DP (CO3): Levenshtein, Damerau-Levenshtein, NW, SW, MCM, OBST, Bitmask, Tree, SOS</li>
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
        } catch (Exception ex) {
            System.err.println("  [SERVER WARN] Could not start web server: " + ex.getMessage());
        }

        // If launched in server-only mode (e.g. from headless background task)
        for (String arg : args) {
            if ("--server".equalsIgnoreCase(arg) || "-s".equalsIgnoreCase(arg)) {
                System.out.println("  [SERVER] EduTrack Web Server is running in daemon mode on port " + activeWebPort);
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
            System.out.print("\n  Enter choice: ");
            String input = reader.readLine();
            if (input == null) break;
            input = input.trim();

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
                case "18": runAllDemos();                   break;
                case "19": edutrack.test.EduTrackTestRunner.runAllTests(); break;
                case "20": demoWebServerInfo();             break;
                case "0":  running = false;                 break;
                default:   System.out.println("  Invalid choice. Try again."); break;
            }
        }

        if (webServer != null) {
            webServer.stop();
        }
        System.out.println("\n  Thank you for using EduTrack! Goodbye.\n");
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
        System.out.println("    Search Course Catalog by Topic or Keyword [KMP]");
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
        System.out.println("    Check Assignment Submission for Repeated Phrases [Z-Function]");
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
        System.out.println("    Search Course Codes and Department Identifiers [Rabin-Karp]");
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
        System.out.println("    Scan Study Materials for Multiple Syllabus Terms [Aho-Corasick]");
        System.out.println("    Scans textbooks and notes simultaneously for a list of curriculum terms.");
        System.out.println("  ----------------------------------------------------------------------------");
        String[] keywords = {"algorithm", "dynamic", "sorting", "database", "tree"};
        System.out.println("  Scanning for terms: algorithm, dynamic, sorting, database, tree");
        QueryResult r = engine.multiKeywordSearch(keywords);
        System.out.println(r);
    }

    private static void demoSuffixArraySearch(BufferedReader reader) throws IOException {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Search Exact Phrases Inside Lecture Notes [Suffix Array]");
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
        System.out.println("    Index Long Textbooks and Reference Books [SA-IS]");
        System.out.println("    Builds a high-speed search index for long books in linear time.");
        System.out.println("  ----------------------------------------------------------------------------");
        String docId = engine.getDocuments().isEmpty() ? "DOC001" : engine.getDocuments().get(0).getDocumentId();
        QueryResult r = engine.indexDocumentSAIS(docId);
        System.out.println(r);
    }

    private static void demoKasaiLCP() {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Check for Copied Content Between Student Submissions [Kasai LCP]");
        System.out.println("    Finds identical text passages between two student assignments.");
        System.out.println("  ----------------------------------------------------------------------------");
        String doc1 = engine.getDocuments().isEmpty() ? "DOC001" : engine.getDocuments().get(0).getDocumentId();
        String doc2 = engine.getDocuments().size() > 1 ? engine.getDocuments().get(1).getDocumentId() : doc1;
        QueryResult r = engine.crossDocumentSimilarity(doc1, doc2, 8);
        System.out.println(r);
    }

    private static void demoSuffixTreeAutomaton(BufferedReader reader) throws IOException {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Analyze Vocabulary and Common Passages [Suffix Tree & Suffix Automaton]");
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
        System.out.println("    Look Up Students and Professors with Typo Tolerance [Levenshtein]");
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
        System.out.println("    Correct Misspelled Names and Swapped Letters [Damerau-Levenshtein]");
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
        System.out.println("    Compare Two Syllabus Versions Line by Line [Needleman-Wunsch]");
        System.out.println("    Aligns full texts side by side to reveal changes between editions.");
        System.out.println("  ----------------------------------------------------------------------------");
        String a1 = engine.getAssignments().isEmpty() ? "A001" : engine.getAssignments().get(0).getAssignmentId();
        String a2 = engine.getAssignments().size() > 1 ? engine.getAssignments().get(1).getAssignmentId() : a1;
        QueryResult r = engine.globalAlignment(a1, a2);
        System.out.println(r);
    }

    private static void demoSmithWaterman() {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Find Matching Sections Between Homework and Slides [Smith-Waterman]");
        System.out.println("    Finds the single most similar passage between two documents.");
        System.out.println("  ----------------------------------------------------------------------------");
        String a1 = engine.getAssignments().isEmpty() ? "A001" : engine.getAssignments().get(0).getAssignmentId();
        String a2 = engine.getAssignments().size() > 1 ? engine.getAssignments().get(1).getAssignmentId() : a1;
        QueryResult r = engine.localAlignment(a1, a2);
        System.out.println(r);
    }

    private static void demoMatrixChain() {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Optimize Database Join Order for Grade Reports [Matrix-Chain Multiplication]");
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
        System.out.println("    Speed Up Quick-Search for Popular Courses [Optimal BST]");
        System.out.println("    Arranges course search keys so frequently searched courses appear near the top.");
        System.out.println("  ----------------------------------------------------------------------------");
        String[] keys = {"Algorithms", "DataStructures", "DBMS", "MachineLearning", "WebDev"};
        double[] freqs = {0.35, 0.25, 0.20, 0.15, 0.05};
        QueryResult r = engine.buildOptimalLookup(keys, freqs);
        System.out.println(r);
    }

    private static void demoBitmaskDP() {
        System.out.println("\n  ----------------------------------------------------------------------------");
        System.out.println("    Plan Shortest Route Between Campus Lab Rooms [Bitmask DP]");
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
        System.out.println("    Check Degree Prerequisites and Elective Conflicts [Tree DP]");
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
        System.out.println("    Analyze Student Enrollment Numbers Across Courses [SOS DP]");
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

        System.out.println("\n  [SUCCESS] All 17 university platform services completed successfully!");
    }

    // =====================================================================
    //  UI HELPERS
    // =====================================================================

    private static void printBanner() {
        System.out.println();
        System.out.println("  ============================================================================");
        System.out.println("    EDUTRACK - INTELLIGENT UNIVERSITY ACADEMIC PORTAL");
        System.out.println("    Course Catalog, Library Analytics, Plagiarism Checker & Degree Planning");
        System.out.println("    All algorithms built completely from scratch (Zero java.util.* collections)");
        System.out.println("  ============================================================================");
        System.out.println("  System Ready: Loaded " +
                engine.getStudents().size() + " students, " +
                engine.getFaculty().size() + " faculty members, " +
                engine.getAdmins().size() + " administrators, " +
                engine.getCourses().size() + " courses, " +
                engine.getDocuments().size() + " library documents, and " +
                engine.getAssignments().size() + " homework submissions.");
        System.out.println("  Web Application live at: http://localhost:" + activeWebPort + "/web/index.html");
        System.out.println();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("  +--------------------------------------------------------------------------------------+");
        System.out.println("  |                         EDUTRACK UNIVERSITY ACADEMIC PORTAL                          |");
        System.out.println("  +--------------------------------------------------------------------------------------+");
        System.out.println("  |  1. Search course catalog by topic or keyword [KMP]                                  |");
        System.out.println("  |  2. Check assignment submission for repeated phrases [Z-Function]                    |");
        System.out.println("  |  3. Search course codes and department identifiers [Rabin-Karp]                      |");
        System.out.println("  |  4. Scan study materials for multiple syllabus terms [Aho-Corasick]                  |");
        System.out.println("  |  5. Search exact phrases inside lecture notes [Suffix Array]                         |");
        System.out.println("  |  6. Index long textbooks and reference books [SA-IS]                                 |");
        System.out.println("  |  7. Check for copied content between student submissions [Kasai LCP]                 |");
        System.out.println("  |  8. Analyze vocabulary and common text passages [Suffix Tree & Suffix Automaton]     |");
        System.out.println("  |  9. Look up students and professors with typo tolerance [Levenshtein]                |");
        System.out.println("  | 10. Correct misspelled names and swapped letters [Damerau-Levenshtein]               |");
        System.out.println("  | 11. Compare two syllabus versions line by line [Needleman-Wunsch]                    |");
        System.out.println("  | 12. Find matching sections between homework and slides [Smith-Waterman]              |");
        System.out.println("  | 13. Optimize database join order for grade reports [Matrix-Chain Multiplication]     |");
        System.out.println("  | 14. Speed up quick-search for popular courses [Optimal BST]                          |");
        System.out.println("  | 15. Plan the shortest route between campus lab rooms [Bitmask DP]                    |");
        System.out.println("  | 16. Check degree prerequisites and elective conflicts [Tree DP]                      |");
        System.out.println("  | 17. Analyze student enrollment numbers across courses [SOS DP]                       |");
        System.out.println("  |                                                                                      |");
        System.out.println("  | 18. Run demonstration of all university services                                     |");
        System.out.println("  | 19. Run automated system diagnostics and test suite                                  |");
        System.out.println("  | 20. Web Server Status & Live Website URL                                             |");
        System.out.println("  |  0. Exit system                                                                      |");
        System.out.println("  +--------------------------------------------------------------------------------------+");
    }
}
