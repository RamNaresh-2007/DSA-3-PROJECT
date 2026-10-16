package edutrack.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import edutrack.core.CustomArrayList;
import edutrack.core.CustomHashMap;
import edutrack.data.DataLoader;
import edutrack.dp.*;
import edutrack.engine.EduTrackEngine;
import edutrack.engine.QueryResult;
import edutrack.model.*;
import edutrack.string.*;
import edutrack.suffix.*;
import edutrack.flow.*;
import edutrack.approx.*;
import edutrack.randomparallel.*;
import edutrack.corpus.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.Executors;

/**
 * High-Performance Embedded Web Server for the EduTrack University Academic Platform.
 *
 * <p>Serves the interactive web application frontend (HTML, CSS, JS, EduTrack.csv)
 * and provides RESTful APIs for real-time DSA query execution across all 6 modules:
 * KMP, Z, Rabin-Karp, Aho-Corasick, Suffix Structures, Advanced DP, Network Flow,
 * NP-Completeness & Approximation, Randomised & Parallel, and TextHack Indic Corpus.</p>
 */
public class EduTrackWebServer {

    private final int port;
    private final EduTrackEngine engine;
    private final DataLoader.EduTrackDataset dataset;
    private HttpServer server;

    public EduTrackWebServer(int port, EduTrackEngine engine, DataLoader.EduTrackDataset dataset) {
        this.port = port;
        this.engine = engine;
        this.dataset = dataset;
    }

    public synchronized int start() throws IOException {
        int selectedPort = port;
        HttpServer s = null;
        int[] candidatePorts = { port, 8081, 8082, 8085, 8090, 8888, 0 };
        for (int p : candidatePorts) {
            try {
                s = HttpServer.create(new InetSocketAddress(p), 0);
                selectedPort = s.getAddress().getPort();
                break;
            } catch (IOException ignored) {
            }
        }
        if (s == null) {
            s = HttpServer.create(new InetSocketAddress(0), 0);
            selectedPort = s.getAddress().getPort();
        }
        this.server = s;

        // Context handlers
        server.createContext("/api/stats", new StatsHandler());
        server.createContext("/api/students", new StudentsHandler());
        server.createContext("/api/courses", new CoursesHandler());
        server.createContext("/api/faculty", new FacultyHandler());
        server.createContext("/api/analytics", new AnalyticsHandler());
        server.createContext("/api/dsa/search", new DsaSearchHandler());
        server.createContext("/api/dsa/dp", new DsaDpHandler());
        server.createContext("/api/dsa/flow", new DsaFlowHandler());
        server.createContext("/api/dsa/approx", new DsaApproxHandler());
        server.createContext("/api/dsa/random", new DsaRandomHandler());
        server.createContext("/api/texthack", new TextHackHandler());
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        System.out.println("  [SERVER] EduTrack Web Server is active at: http://localhost:" + selectedPort + "/");
        System.out.println("  [SERVER] Web Application UI: http://localhost:" + selectedPort + "/web/index.html");
        return selectedPort;
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop(1);
            server = null;
        }
    }

    // =========================================================================
    //  STATIC FILE HANDLER (Serves web/ folder and EduTrack.csv)
    // =========================================================================
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/web/index.html";
            }

            File targetFile = resolveStaticFile(path);
            if (targetFile == null || !targetFile.exists() || targetFile.isDirectory()) {
                // Try fallback to index.html if within web directory
                if (path.startsWith("/web/")) {
                    targetFile = new File("web/index.html");
                }
            }

            if (targetFile != null && targetFile.exists() && !targetFile.isDirectory()) {
                byte[] bytes = Files.readAllBytes(targetFile.toPath());
                String mime = getMimeType(targetFile.getName());

                addCorsHeaders(exchange);
                exchange.getResponseHeaders().set("Content-Type", mime + "; charset=utf-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                String notFound = "{\"error\":\"File not found: " + path + "\"}";
                addCorsHeaders(exchange);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
    }

    private File resolveStaticFile(String path) {
        if (path.startsWith("/")) path = path.substring(1);
        String[] candidatePaths = {
            path,
            "web/" + path,
            "../" + path,
            "../web/" + path,
            "../../" + path,
            "../../web/" + path
        };
        for (String c : candidatePaths) {
            File f = new File(c);
            if (f.exists() && !f.isDirectory()) return f;
        }
        if (path.equals("EduTrack.csv") || path.endsWith("EduTrack.csv")) {
            String[] csvCandidates = { "EduTrack.csv", "data/EduTrack.csv", "../EduTrack.csv", "../../EduTrack.csv" };
            for (String c : csvCandidates) {
                File f = new File(c);
                if (f.exists() && !f.isDirectory()) return f;
            }
        }
        return new File("web/" + path);
    }

    private String getMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".html")) return "text/html";
        if (lower.endsWith(".css"))  return "text/css";
        if (lower.endsWith(".js"))   return "application/javascript";
        if (lower.endsWith(".json")) return "application/json";
        if (lower.endsWith(".csv"))  return "text/csv";
        if (lower.endsWith(".svg"))  return "image/svg+xml";
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".ico"))  return "image/x-icon";
        return "text/plain";
    }

    // =========================================================================
    //  API HANDLERS
    // =========================================================================

    /** /api/stats */
    private class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }

            int totalStudents = dataset.students.size();
            int totalCourses = dataset.courses.size();
            int totalFaculty = dataset.faculty.size();
            int passCount = 0;
            int failCount = 0;
            double sumCgpa = 0.0;
            double sumAttendance = 0.0;

            CustomHashMap<String, Integer> gradeCounts = new CustomHashMap<>();
            CustomHashMap<String, Integer> deptCounts = new CustomHashMap<>();
            CustomHashMap<String, Integer> actCounts = new CustomHashMap<>();

            for (int i = 0; i < totalStudents; i++) {
                Student s = dataset.students.get(i);
                if ("Pass".equalsIgnoreCase(s.getAcademicStatus())) passCount++;
                else failCount++;
                sumCgpa += s.getCgpa();
                sumAttendance += s.getAttendance();

                String g = s.getGrade();
                Integer gc = gradeCounts.get(g);
                gradeCounts.put(g, gc == null ? 1 : gc + 1);

                String d = s.getDepartment();
                Integer dc = deptCounts.get(d);
                deptCounts.put(d, dc == null ? 1 : dc + 1);

                String act = s.getActivityType();
                if (act != null) {
                    Integer ac = actCounts.get(act);
                    actCounts.put(act, ac == null ? 1 : ac + 1);
                }
            }

            double avgCgpa = totalStudents > 0 ? (sumCgpa / totalStudents) : 0.0;
            double avgAtt = totalStudents > 0 ? (sumAttendance / totalStudents) : 0.0;

            StringBuilder sb = new StringBuilder("{");
            sb.append("\"totalStudents\":").append(totalStudents).append(",");
            sb.append("\"totalCourses\":").append(totalCourses).append(",");
            sb.append("\"totalFaculty\":").append(totalFaculty).append(",");
            sb.append("\"totalAssignments\":").append(dataset.assignments.size()).append(",");
            sb.append("\"totalDocuments\":").append(dataset.documents.size()).append(",");
            sb.append("\"totalSchedules\":").append(dataset.schedules.size()).append(",");
            sb.append("\"totalAdmins\":").append(dataset.admins.size()).append(",");
            sb.append("\"passCount\":").append(passCount).append(",");
            sb.append("\"failCount\":").append(failCount).append(",");
            sb.append("\"averageCgpa\":").append(String.format("%.2f", avgCgpa)).append(",");
            sb.append("\"averageAttendance\":").append(String.format("%.1f", avgAtt)).append(",");

            // Grade counts
            sb.append("\"gradeDistribution\":{");
            String[] grades = {"A+", "A", "B", "C", "D", "F"};
            for (int i = 0; i < grades.length; i++) {
                String g = grades[i];
                Integer c = gradeCounts.get(g);
                if (i > 0) sb.append(",");
                sb.append("\"").append(g).append("\":").append(c != null ? c : 0);
            }
            sb.append("},");

            // Dept counts
            sb.append("\"departmentDistribution\":{");
            String[] depts = {"CSE", "ECE", "ME", "EEE", "AIML", "AI&DS", "IT", "CE"};
            for (int i = 0; i < depts.length; i++) {
                String d = depts[i];
                Integer c = deptCounts.get(d);
                if (i > 0) sb.append(",");
                sb.append("\"").append(d).append("\":").append(c != null ? c : 0);
            }
            sb.append("},");

            // Activity counts
            sb.append("\"activityDistribution\":{");
            String[] acts = {"Lab Activity", "Assignment Submission", "Library Access", "Video Lecture", "Login", "Quiz", "Forum"};
            for (int i = 0; i < acts.length; i++) {
                String a = acts[i];
                Integer c = actCounts.get(a);
                if (i > 0) sb.append(",");
                sb.append("\"").append(escapeJson(a)).append("\":").append(c != null ? c : 0);
            }
            sb.append("}");

            sb.append("}");
            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    /** /api/students */
    private class StudentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }

            String query = exchange.getRequestURI().getRawQuery();
            CustomHashMap<String, String> params = parseQueryParams(query);

            String search = params.get("search");
            String dept = params.get("dept");
            String yearStr = params.get("year");
            String grade = params.get("grade");
            String status = params.get("status");
            int page = parseInt(params.get("page"), 1);
            String limitStr = params.get("limit");
            int limit = (limitStr != null && !limitStr.isEmpty()) ? parseInt(limitStr, 10000) : 10000;
            String sortBy = params.get("sortBy");
            boolean desc = "desc".equalsIgnoreCase(params.get("order"));

            if (page < 1) page = 1;
            if (limit < 1) limit = 10000;
            if (limit > 50000) limit = 50000;

            int filterYear = parseInt(yearStr, 0);

            // Filter
            CustomArrayList<Student> matched = new CustomArrayList<>();
            String searchLower = (search != null) ? search.trim().toLowerCase() : null;

            for (int i = 0; i < dataset.students.size(); i++) {
                Student s = dataset.students.get(i);
                if (dept != null && !dept.isEmpty() && !dept.equalsIgnoreCase("ALL") && !s.getDepartment().equalsIgnoreCase(dept)) {
                    continue;
                }
                if (filterYear > 0 && s.getYear() != filterYear) {
                    continue;
                }
                if (grade != null && !grade.isEmpty() && !grade.equalsIgnoreCase("ALL") && !s.getGrade().equalsIgnoreCase(grade)) {
                    continue;
                }
                if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("ALL") && !s.getAcademicStatus().equalsIgnoreCase(status)) {
                    continue;
                }
                if (searchLower != null && !searchLower.isEmpty()) {
                    boolean hits = s.getStudentId().toLowerCase().contains(searchLower) ||
                                   s.getName().toLowerCase().contains(searchLower) ||
                                   s.getCourseName().toLowerCase().contains(searchLower);
                    if (!hits) continue;
                }
                matched.add(s);
            }

            // Sorting
            if (sortBy != null && !sortBy.isEmpty()) {
                sortStudents(matched, sortBy, desc);
            }

            int total = matched.size();
            int totalPages = (total + limit - 1) / limit;
            int startIdx = (page - 1) * limit;
            int endIdx = Math.min(startIdx + limit, total);

            StringBuilder sb = new StringBuilder("{");
            sb.append("\"total\":").append(total).append(",");
            sb.append("\"page\":").append(page).append(",");
            sb.append("\"limit\":").append(limit).append(",");
            sb.append("\"totalPages\":").append(totalPages).append(",");
            sb.append("\"students\":[");

            for (int i = startIdx; i < endIdx; i++) {
                if (i > startIdx) sb.append(",");
                Student s = matched.get(i);
                appendStudentJson(sb, s);
            }
            sb.append("]}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    private void sortStudents(CustomArrayList<Student> list, String sortBy, boolean desc) {
        list.sort((a, b) -> {
            int cmp = 0;
            switch (sortBy.toLowerCase()) {
                case "cgpa":
                    cmp = Double.compare(a.getCgpa(), b.getCgpa());
                    break;
                case "attendance":
                    cmp = Double.compare(a.getAttendance(), b.getAttendance());
                    break;
                case "examscore":
                    cmp = Double.compare(a.getExamScore(), b.getExamScore());
                    break;
                case "name":
                    cmp = a.getName().compareToIgnoreCase(b.getName());
                    break;
                case "id":
                case "studentid":
                    cmp = a.getStudentId().compareToIgnoreCase(b.getStudentId());
                    break;
                default:
                    cmp = a.getStudentId().compareToIgnoreCase(b.getStudentId());
                    break;
            }
            return desc ? -cmp : cmp;
        });
    }

    private void appendStudentJson(StringBuilder sb, Student s) {
        sb.append("{");
        sb.append("\"studentId\":\"").append(escapeJson(s.getStudentId())).append("\",");
        sb.append("\"name\":\"").append(escapeJson(s.getName())).append("\",");
        sb.append("\"age\":").append(s.getAge()).append(",");
        sb.append("\"gender\":\"").append(escapeJson(s.getGender())).append("\",");
        sb.append("\"department\":\"").append(escapeJson(s.getDepartment())).append("\",");
        sb.append("\"year\":").append(s.getYear()).append(",");
        sb.append("\"semester\":").append(s.getSemester()).append(",");
        sb.append("\"courseId\":\"").append(escapeJson(s.getCourseId())).append("\",");
        sb.append("\"courseCode\":\"").append(escapeJson(s.getCourseCode())).append("\",");
        sb.append("\"courseName\":\"").append(escapeJson(s.getCourseName())).append("\",");
        sb.append("\"credits\":").append(s.getCredits()).append(",");
        sb.append("\"facultyId\":\"").append(escapeJson(s.getFacultyId())).append("\",");
        sb.append("\"facultyName\":\"").append(escapeJson(s.getFacultyName())).append("\",");
        sb.append("\"attendance\":").append(s.getAttendance()).append(",");
        sb.append("\"studyHours\":").append(s.getStudyHours()).append(",");
        sb.append("\"assignmentScore\":").append(s.getAssignmentScore()).append(",");
        sb.append("\"midtermScore\":").append(s.getMidtermScore()).append(",");
        sb.append("\"examScore\":").append(s.getExamScore()).append(",");
        sb.append("\"cgpa\":").append(s.getCgpa()).append(",");
        sb.append("\"grade\":\"").append(escapeJson(s.getGrade())).append("\",");
        sb.append("\"academicStatus\":\"").append(escapeJson(s.getAcademicStatus())).append("\",");
        sb.append("\"activityId\":\"").append(escapeJson(s.getActivityId())).append("\",");
        sb.append("\"activityType\":\"").append(escapeJson(s.getActivityType())).append("\",");
        sb.append("\"activityScore\":").append(s.getActivityScore()).append(",");
        sb.append("\"activityDate\":\"").append(escapeJson(s.getActivityDate())).append("\",");
        sb.append("\"assignmentId\":\"").append(escapeJson(s.getAssignmentId())).append("\",");
        sb.append("\"assignmentText\":\"").append(escapeJson(s.getAssignmentText())).append("\",");
        sb.append("\"examId\":\"").append(escapeJson(s.getExamId())).append("\",");
        sb.append("\"examDate\":\"").append(escapeJson(s.getExamDate())).append("\",");
        sb.append("\"roomId\":\"").append(escapeJson(s.getRoomId())).append("\",");
        sb.append("\"scheduleDay\":\"").append(escapeJson(s.getScheduleDay())).append("\",");
        sb.append("\"scheduleTime\":\"").append(escapeJson(s.getScheduleTime())).append("\",");
        sb.append("\"resourceType\":\"").append(escapeJson(s.getResourceType())).append("\",");
        sb.append("\"resourceTitle\":\"").append(escapeJson(s.getResourceTitle())).append("\"");
        sb.append("}");
    }

    /** /api/courses */
    private class CoursesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < dataset.courses.size(); i++) {
                if (i > 0) sb.append(",");
                Course c = dataset.courses.get(i);
                sb.append("{");
                sb.append("\"courseId\":\"").append(escapeJson(c.getCourseId())).append("\",");
                sb.append("\"courseCode\":\"").append(escapeJson(c.getCourseCode())).append("\",");
                sb.append("\"courseName\":\"").append(escapeJson(c.getCourseName())).append("\",");
                sb.append("\"credits\":").append(c.getCredits()).append(",");
                sb.append("\"department\":\"").append(escapeJson(c.getDepartment())).append("\",");
                sb.append("\"facultyId\":\"").append(escapeJson(c.getFacultyId())).append("\",");
                sb.append("\"facultyName\":\"").append(escapeJson(c.getFacultyName())).append("\",");
                sb.append("\"description\":\"").append(escapeJson(c.getDescription())).append("\"");
                sb.append("}");
            }
            sb.append("]");
            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    /** /api/faculty */
    private class FacultyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < dataset.faculty.size(); i++) {
                if (i > 0) sb.append(",");
                Faculty f = dataset.faculty.get(i);
                sb.append("{");
                sb.append("\"facultyId\":\"").append(escapeJson(f.getFacultyId())).append("\",");
                sb.append("\"name\":\"").append(escapeJson(f.getName())).append("\",");
                sb.append("\"email\":\"").append(escapeJson(f.getEmail())).append("\",");
                sb.append("\"department\":\"").append(escapeJson(f.getDepartment())).append("\",");
                sb.append("\"specialization\":\"").append(escapeJson(f.getSpecialization())).append("\"");
                sb.append("}");
            }
            sb.append("]");
            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    /** /api/analytics */
    private class AnalyticsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }

            // High-dimensional department aggregation
            CustomHashMap<String, double[]> deptStats = new CustomHashMap<>();
            // { count, sumCgpa, sumAtt, sumExam, passCount }
            for (int i = 0; i < dataset.students.size(); i++) {
                Student s = dataset.students.get(i);
                String d = s.getDepartment();
                double[] st = deptStats.get(d);
                if (st == null) {
                    st = new double[5];
                    deptStats.put(d, st);
                }
                st[0] += 1.0;
                st[1] += s.getCgpa();
                st[2] += s.getAttendance();
                st[3] += s.getExamScore();
                if ("Pass".equalsIgnoreCase(s.getAcademicStatus())) st[4] += 1.0;
            }

            StringBuilder sb = new StringBuilder("{\"departments\":[");
            String[] depts = {"CSE", "ECE", "ME", "EEE", "AIML", "AI&DS", "IT", "CE"};
            boolean first = true;
            for (String d : depts) {
                double[] st = deptStats.get(d);
                if (st == null) continue;
                if (!first) sb.append(",");
                first = false;
                double count = st[0];
                sb.append("{");
                sb.append("\"department\":\"").append(d).append("\",");
                sb.append("\"enrollment\":").append((int) count).append(",");
                sb.append("\"avgCgpa\":").append(String.format("%.2f", count > 0 ? st[1] / count : 0)).append(",");
                sb.append("\"avgAttendance\":").append(String.format("%.1f", count > 0 ? st[2] / count : 0)).append(",");
                sb.append("\"avgExamScore\":").append(String.format("%.1f", count > 0 ? st[3] / count : 0)).append(",");
                sb.append("\"passRate\":").append(String.format("%.1f", count > 0 ? (st[4] * 100.0 / count) : 0));
                sb.append("}");
            }
            sb.append("]}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    /** /api/dsa/search */
    private class DsaSearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed. Use POST.\"}");
                return;
            }

            String body = readRequestBody(exchange);
            CustomHashMap<String, String> json = parseSimpleJson(body);

            String algo = json.get("algorithm");
            String query = json.get("query");
            String target = json.get("target");
            int maxDist = parseInt(json.get("maxDist"), 2);

            if (query == null || query.trim().isEmpty()) {
                query = "algorithm";
            }
            if (algo == null) algo = "kmp";

            long t0 = System.nanoTime();
            CustomArrayList<String> matches = new CustomArrayList<>();
            String details = "";

            switch (algo.toLowerCase()) {
                case "kmp": {
                    String kw = query.toLowerCase();
                    for (int i = 0; i < dataset.students.size(); i++) {
                        Student s = dataset.students.get(i);
                        if (KMP.contains(s.getAssignmentText().toLowerCase(), kw) ||
                            KMP.contains(s.getCourseName().toLowerCase(), kw) ||
                            KMP.contains(s.getName().toLowerCase(), kw)) {
                            matches.add(s.getStudentId() + ": " + s.getName() + " (" + s.getCourseName() + ") -> \"" + s.getAssignmentText() + "\"");
                            if (matches.size() >= 100) break;
                        }
                    }
                    details = "KMP linear prefix-function pattern match over assignment corpus";
                    break;
                }
                case "z": {
                    String kw = query.toLowerCase();
                    for (int i = 0; i < dataset.students.size(); i++) {
                        Student s = dataset.students.get(i);
                        String combined = kw + "$" + s.getAssignmentText().toLowerCase();
                        int[] z = ZFunction.computeZ(combined);
                        for (int k = kw.length() + 1; k < z.length; k++) {
                            if (z[k] >= kw.length()) {
                                matches.add(s.getStudentId() + " (" + s.getCourseName() + "): Match at pos " + (k - kw.length() - 1) + " -> \"" + s.getAssignmentText() + "\"");
                                break;
                            }
                        }
                        if (matches.size() >= 100) break;
                    }
                    details = "Z-Algorithm linear prefix box calculation over assignment texts";
                    break;
                }
                case "rabin-karp": {
                    for (int i = 0; i < dataset.students.size(); i++) {
                        Student s = dataset.students.get(i);
                        CustomArrayList<Integer> hits = RabinKarp.search(s.getAssignmentText(), query);
                        if (!hits.isEmpty()) {
                            matches.add(s.getStudentId() + " [" + s.getCourseCode() + "]: " + hits.size() + " hit(s) in \"" + s.getAssignmentText() + "\"");
                            if (matches.size() >= 100) break;
                        }
                    }
                    details = "Rabin-Karp polynomial rolling hash substring matching";
                    break;
                }
                case "aho-corasick": {
                    String[] kw = query.split("[,\\s]+");
                    AhoCorasick ac = new AhoCorasick(kw);
                    for (int i = 0; i < dataset.students.size(); i++) {
                        Student s = dataset.students.get(i);
                        CustomArrayList<int[]> hits = ac.search(s.getAcademicKeywords());
                        if (!hits.isEmpty()) {
                            matches.add(s.getStudentId() + ": " + hits.size() + " keyword matches in student keywords (" + s.getAcademicKeywords() + ")");
                            if (matches.size() >= 100) break;
                        }
                    }
                    details = "Aho-Corasick multi-pattern trie with failure link state transitions";
                    break;
                }
                case "suffix-array": {
                    for (int i = 0; i < dataset.documents.size(); i++) {
                        Document doc = dataset.documents.get(i);
                        SuffixArray sa = new SuffixArray(doc.getContent().toLowerCase());
                        CustomArrayList<Integer> pos = sa.search(query.toLowerCase());
                        if (!pos.isEmpty()) {
                            matches.add(doc.getTitle() + " (" + doc.getType() + "): " + pos.size() + " match(es) at offsets " + pos.toString());
                        }
                    }
                    details = "Suffix Array binary search over academic resource repository";
                    break;
                }
                case "levenshtein": {
                    String q = query.toLowerCase();
                    for (int i = 0; i < dataset.students.size(); i++) {
                        Student s = dataset.students.get(i);
                        int dist = Levenshtein.distance(q, s.getName().toLowerCase());
                        if (dist <= maxDist) {
                            matches.add(s.getName() + " (" + s.getStudentId() + ", " + s.getDepartment() + ") -> Edit Distance = " + dist);
                            if (matches.size() >= 100) break;
                        }
                    }
                    details = "Levenshtein dynamic programming fuzzy match (Ins/Del/Sub)";
                    break;
                }
                case "damerau": {
                    String q = query.toLowerCase();
                    for (int i = 0; i < dataset.students.size(); i++) {
                        Student s = dataset.students.get(i);
                        int dist = DamerauLevenshtein.distance(q, s.getName().toLowerCase());
                        if (dist <= maxDist) {
                            matches.add(s.getName() + " (" + s.getStudentId() + ", " + s.getDepartment() + ") -> Damerau Distance = " + dist);
                            if (matches.size() >= 100) break;
                        }
                    }
                    details = "Damerau-Levenshtein DP with adjacent character transpositions";
                    break;
                }
                default: {
                    QueryResult qr = engine.searchCoursesByKMP(query);
                    for (int i = 0; i < qr.getResults().size(); i++) matches.add(qr.getResults().get(i));
                    details = qr.getSummary();
                    break;
                }
            }

            long elapsed = System.nanoTime() - t0;
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"algorithm\":\"").append(algo).append("\",");
            sb.append("\"query\":\"").append(escapeJson(query)).append("\",");
            sb.append("\"elapsedNs\":").append(elapsed).append(",");
            sb.append("\"elapsedMicros\":").append(elapsed / 1000).append(",");
            sb.append("\"matchCount\":").append(matches.size()).append(",");
            sb.append("\"details\":\"").append(escapeJson(details)).append("\",");
            sb.append("\"matches\":[");
            for (int i = 0; i < matches.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(escapeJson(matches.get(i))).append("\"");
            }
            sb.append("]}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    /** /api/dsa/dp */
    private class DsaDpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed. Use POST.\"}");
                return;
            }

            String body = readRequestBody(exchange);
            CustomHashMap<String, String> json = parseSimpleJson(body);

            String algo = json.get("algorithm");
            String s1 = json.get("s1");
            String s2 = json.get("s2");

            long t0 = System.nanoTime();
            StringBuilder sb = new StringBuilder("{");

            if ("needleman-wunsch".equalsIgnoreCase(algo)) {
                if (s1 == null || s1.isEmpty()) s1 = "ALGORITHM";
                if (s2 == null || s2.isEmpty()) s2 = "ALLIGATOR";
                NeedlemanWunsch nw = new NeedlemanWunsch(1, -1, -2);
                NeedlemanWunsch.AlignmentResult res = nw.align(s1, s2);
                long elapsed = System.nanoTime() - t0;

                sb.append("\"algorithm\":\"Needleman-Wunsch Global Alignment\",");
                sb.append("\"score\":").append(res.score).append(",");
                sb.append("\"aligned1\":\"").append(res.alignedSeq1).append("\",");
                sb.append("\"aligned2\":\"").append(res.alignedSeq2).append("\",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000).append(",");
                sb.append("\"explanation\":\"Optimal global sequence alignment with score = " + res.score + "\"");
            } else if ("smith-waterman".equalsIgnoreCase(algo)) {
                if (s1 == null || s1.isEmpty()) s1 = "DATASTRUCTURES";
                if (s2 == null || s2.isEmpty()) s2 = "ASTRUCTURE";
                SmithWaterman sw = new SmithWaterman(2, -1, -2);
                SmithWaterman.LocalAlignmentResult res = sw.align(s1, s2);
                long elapsed = System.nanoTime() - t0;

                sb.append("\"algorithm\":\"Smith-Waterman Local Alignment\",");
                sb.append("\"score\":").append(res.score).append(",");
                sb.append("\"aligned1\":\"").append(res.alignedSeq1).append("\",");
                sb.append("\"aligned2\":\"").append(res.alignedSeq2).append("\",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000).append(",");
                sb.append("\"explanation\":\"Optimal local substring alignment with peak score = " + res.score + "\"");
            } else if ("matrix-chain".equalsIgnoreCase(algo)) {
                int[] dims = {10, 30, 5, 60, 20, 15};
                MatrixChainMultiplication.MCMResult res = MatrixChainMultiplication.solve(dims);
                long elapsed = System.nanoTime() - t0;

                sb.append("\"algorithm\":\"Matrix Chain Multiplication\",");
                sb.append("\"minMultiplications\":").append(res.minCost).append(",");
                sb.append("\"parenthesization\":\"").append(escapeJson(res.optimalParenthesisation)).append("\",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000).append(",");
                sb.append("\"explanation\":\"Optimal scalar multiplication sequence: " + res.optimalParenthesisation + "\"");
            } else if ("optimal-bst".equalsIgnoreCase(algo)) {
                String[] keys = {"Algo", "DSA", "DBMS", "ML", "Web"};
                double[] freqs = {0.35, 0.25, 0.20, 0.15, 0.05};
                OptimalBST.OBSTResult res = OptimalBST.solve(keys, freqs);
                long elapsed = System.nanoTime() - t0;

                sb.append("\"algorithm\":\"Optimal Binary Search Tree\",");
                sb.append("\"expectedCost\":").append(String.format("%.4f", res.minCost)).append(",");
                sb.append("\"rootKey\":").append(res.root != null && res.root.length > 1 ? res.root[1][keys.length] : 1).append(",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000).append(",");
                sb.append("\"explanation\":\"Minimum expected search cost tree: " + String.format("%.4f", res.minCost) + "\"");
            } else {
                long elapsed = System.nanoTime() - t0;
                sb.append("\"algorithm\":\"Dynamic Programming\",");
                sb.append("\"status\":\"ready\",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000);
            }
            sb.append("}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    // =========================================================================
    //  MODULE 4: NETWORK FLOW HANDLER (/api/dsa/flow)
    // =========================================================================
    private class DsaFlowHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed. Use POST.\"}");
                return;
            }

            String body = readRequestBody(exchange);
            CustomHashMap<String, String> json = parseSimpleJson(body);
            String algo = json.get("algorithm");
            if (algo == null) algo = "dinic";

            long t0 = System.nanoTime();
            StringBuilder sb = new StringBuilder("{");

            if ("edmonds-karp".equalsIgnoreCase(algo)) {
                FlowNetwork fn = createDefaultCampusFlowNetwork();
                EdmondsKarp.FlowResult res = EdmondsKarp.solve(fn, 0, 5);
                long elapsed = System.nanoTime() - t0;

                sb.append("\"algorithm\":\"Edmonds-Karp Maximum Flow\",");
                sb.append("\"complexity\":\"O(V * E^2) guaranteed polynomial\",");
                sb.append("\"maxFlow\":").append(res.maxFlow).append(",");
                sb.append("\"augmentations\":").append(res.augmentations).append(",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000).append(",");
                sb.append("\"paths\":[");
                for (int i = 0; i < res.pathHistory.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append("\"").append(escapeJson(res.pathHistory.get(i))).append("\"");
                }
                sb.append("],\"explanation\":\"Edmonds-Karp BFS augmented " + res.augmentations + " times to achieve maximum flow of " + res.maxFlow + " units.\"");
            } else if ("dinic".equalsIgnoreCase(algo)) {
                FlowNetwork fn = createDefaultCampusFlowNetwork();
                Dinic.DinicResult res = Dinic.solve(fn, 0, 5);
                long elapsed = System.nanoTime() - t0;

                sb.append("\"algorithm\":\"Dinic's Blocking Flow Algorithm\",");
                sb.append("\"complexity\":\"O(V^2 * E) general / O(E * sqrt(V)) unit network\",");
                sb.append("\"maxFlow\":").append(res.maxFlow).append(",");
                sb.append("\"levelPhases\":").append(res.levelGraphPhases).append(",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000).append(",");
                sb.append("\"explanation\":\"" + escapeJson(res.complexityAnalysis) + "\"");
            } else if ("min-cut".equalsIgnoreCase(algo)) {
                FlowNetwork fn = createDefaultCampusFlowNetwork();
                MinCut.CutResult cut = MinCut.computeMinCut(fn, 0, 5);
                long elapsed = System.nanoTime() - t0;

                sb.append("\"algorithm\":\"Max-Flow Min-Cut Duality\",");
                sb.append("\"minCutCapacity\":").append(cut.minCutCapacity).append(",");
                sb.append("\"maxFlowValue\":").append(cut.maxFlowValue).append(",");
                sb.append("\"dualityVerified\":").append(cut.dualityVerified).append(",");
                sb.append("\"setSSize\":").append(cut.setS.size()).append(",");
                sb.append("\"setTSize\":").append(cut.setT.size()).append(",");
                sb.append("\"cutEdgesCount\":").append(cut.cutEdges.size()).append(",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000).append(",");
                sb.append("\"cutEdges\":[");
                for (int i = 0; i < cut.cutEdges.size(); i++) {
                    if (i > 0) sb.append(",");
                    FlowNetwork.Edge e = cut.cutEdges.get(i);
                    sb.append("{\"from\":").append(e.u).append(",\"to\":").append(e.v).append(",\"capacity\":").append(e.capacity).append("}");
                }
                sb.append("],\"explanation\":\"Max-Flow Min-Cut Theorem confirmed: Capacity(" + cut.minCutCapacity + ") == MaxFlow(" + cut.maxFlowValue + ")\"");
            } else if ("bipartite-matching".equalsIgnoreCase(algo)) {
                CustomArrayList<int[]> edges = new CustomArrayList<>();
                // 5 Students -> 5 Lab Projects
                edges.add(new int[]{0, 0}); edges.add(new int[]{0, 1});
                edges.add(new int[]{1, 1}); edges.add(new int[]{1, 2});
                edges.add(new int[]{2, 0}); edges.add(new int[]{2, 3});
                edges.add(new int[]{3, 2}); edges.add(new int[]{3, 4});
                edges.add(new int[]{4, 3}); edges.add(new int[]{4, 4});

                BipartiteMatching.MatchingResult res = BipartiteMatching.solve(5, 5, edges);
                long elapsed = System.nanoTime() - t0;

                sb.append("\"algorithm\":\"Bipartite Matching & König's Theorem\",");
                sb.append("\"matchingSize\":").append(res.matchingSize).append(",");
                sb.append("\"konigSatisfied\":").append(res.konigTheoremSatisfied).append(",");
                sb.append("\"minVertexCoverSize\":").append(res.minVertexCoverLeft.size() + res.minVertexCoverRight.size()).append(",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000).append(",");
                sb.append("\"pairs\":[");
                for (int i = 0; i < res.matchedPairs.size(); i++) {
                    if (i > 0) sb.append(",");
                    int[] p = res.matchedPairs.get(i);
                    sb.append("{\"student\":\"Student-").append(p[0] + 1).append("\",\"project\":\"LabTrack-").append((char)('A' + p[1])).append("\"}");
                }
                sb.append("],\"explanation\":\"König's theorem satisfied: Max matching (" + res.matchingSize + ") equals min vertex cover.\"");
            } else if ("min-cost-flow".equalsIgnoreCase(algo)) {
                FlowNetwork fn = new FlowNetwork(6);
                fn.addEdge(0, 1, 10, 2);
                fn.addEdge(0, 2, 8, 5);
                fn.addEdge(1, 3, 5, 1);
                fn.addEdge(1, 4, 5, 3);
                fn.addEdge(2, 3, 4, 2);
                fn.addEdge(2, 4, 6, 4);
                fn.addEdge(3, 5, 8, 1);
                fn.addEdge(4, 5, 10, 2);

                MinCostMaxFlow.MCMFResult res = MinCostMaxFlow.solve(fn, 0, 5);
                long elapsed = System.nanoTime() - t0;

                sb.append("\"algorithm\":\"Min-Cost Max-Flow (SSP + SPFA)\",");
                sb.append("\"maxFlow\":").append(res.maxFlow).append(",");
                sb.append("\"minCost\":").append(res.minCost).append(",");
                sb.append("\"augmentations\":").append(res.augmentations).append(",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000).append(",");
                sb.append("\"explanation\":\"Successive Shortest Path reached maximum flow of " + res.maxFlow + " units at minimal cost of " + res.minCost + " units.\"");
            } else if ("project-selection".equalsIgnoreCase(algo)) {
                int[] profits = {10, 20, -15, 30, -25, 40};
                CustomArrayList<int[]> deps = new CustomArrayList<>();
                deps.add(new int[]{0, 2}); // P1 requires P3
                deps.add(new int[]{1, 2}); // P2 requires P3
                deps.add(new int[]{3, 4}); // P4 requires P5
                deps.add(new int[]{5, 4}); // P6 requires P5

                FlowReductions.ProjectSelectionResult res = FlowReductions.solveProjectSelection(profits, deps);
                long elapsed = System.nanoTime() - t0;

                sb.append("\"algorithm\":\"Project Selection via Min-Cut\",");
                sb.append("\"netProfit\":").append(res.maxNetProfit).append(",");
                sb.append("\"minCutCost\":").append(res.minCutCost).append(",");
                sb.append("\"selectedCount\":").append(res.selectedProjects.size()).append(",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000).append(",");
                sb.append("\"selectedProjects\":[");
                for (int i = 0; i < res.selectedProjects.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append("\"Project-").append(res.selectedProjects.get(i) + 1).append("\"");
                }
                sb.append("],\"explanation\":\"" + escapeJson(res.explanation) + "\"");
            } else {
                long elapsed = System.nanoTime() - t0;
                sb.append("\"algorithm\":\"Network Flow\",");
                sb.append("\"status\":\"ready\",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000);
            }
            sb.append("}");

            sendJsonResponse(exchange, 200, sb.toString());
        }

        private FlowNetwork createDefaultCampusFlowNetwork() {
            // 0: Source (Internet/Gateway), 1: Core Router, 2: CS Dept, 3: AI Lab, 4: Library, 5: Sink (Host Dorms)
            FlowNetwork fn = new FlowNetwork(6);
            fn.addEdge(0, 1, 16);
            fn.addEdge(0, 2, 13);
            fn.addEdge(1, 2, 10);
            fn.addEdge(1, 3, 12);
            fn.addEdge(2, 1, 4);
            fn.addEdge(2, 4, 14);
            fn.addEdge(3, 2, 9);
            fn.addEdge(3, 5, 20);
            fn.addEdge(4, 3, 7);
            fn.addEdge(4, 5, 4);
            return fn;
        }
    }

    // =========================================================================
    //  MODULE 5: NP-COMPLETENESS & APPROXIMATION HANDLER (/api/dsa/approx)
    // =========================================================================
    private class DsaApproxHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed. Use POST.\"}");
                return;
            }

            String body = readRequestBody(exchange);
            CustomHashMap<String, String> json = parseSimpleJson(body);
            String algo = json.get("algorithm");
            if (algo == null) algo = "vertex-cover";

            long t0 = System.nanoTime();
            StringBuilder sb = new StringBuilder("{");

            if ("vertex-cover".equalsIgnoreCase(algo)) {
                int numNodes = parseInt(json.get("nodes"), 8);
                int[][] edges = {
                        {0, 1}, {1, 2}, {2, 3}, {3, 0},
                        {0, 4}, {1, 5}, {2, 6}, {3, 7},
                        {4, 5}, {5, 6}, {6, 7}, {7, 4}
                };

                VertexCoverApproximation.ApproxResult res = VertexCoverApproximation.solve(numNodes, edges);
                long elapsed = System.nanoTime() - t0;

                sb.append("\"algorithm\":\"Vertex Cover 2-Approximation\",");
                sb.append("\"approxSize\":").append(res.approxSize).append(",");
                sb.append("\"matchingSize\":").append(res.maximalMatching.size()).append(",");
                sb.append("\"exactOptSize\":").append(res.exactOptSize).append(",");
                sb.append("\"empiricalRatio\":").append(String.format("%.2f", res.empiricalRatio)).append(",");
                sb.append("\"theoreticalGuarantee\":").append(res.theoreticalGuarantee).append(",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000).append(",");
                sb.append("\"coverNodes\":[");
                for (int i = 0; i < res.approxCover.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(res.approxCover.get(i));
                }
                sb.append("],\"explanation\":\"Maximal matching of " + res.maximalMatching.size() + " edges proved OPT >= "
                        + res.maximalMatching.size() + ". Both endpoints added to cover: " + res.approxSize + " <= 2 * OPT.\"");
            } else if ("knapsack".equalsIgnoreCase(algo)) {
                int[] weights = {10, 20, 30, 40, 50, 15, 25};
                int[] values  = {60, 100, 120, 150, 200, 70, 90};
                int capacity  = parseInt(json.get("capacity"), 80);
                double eps    = 0.20; // 80% guarantee

                KnapsackApproximation.KnapsackResult greedy = KnapsackApproximation.solveGreedy2Approx(weights, values, capacity);
                KnapsackApproximation.KnapsackResult fptas  = KnapsackApproximation.solveFPTAS(weights, values, capacity, eps);
                long elapsed = System.nanoTime() - t0;

                sb.append("\"algorithm\":\"Knapsack Approximation (Greedy vs FPTAS)\",");
                sb.append("\"greedyValue\":").append(greedy.totalValue).append(",");
                sb.append("\"greedyWeight\":").append(greedy.totalWeight).append(",");
                sb.append("\"fptasValue\":").append(fptas.totalValue).append(",");
                sb.append("\"fptasWeight\":").append(fptas.totalWeight).append(",");
                sb.append("\"capacity\":").append(capacity).append(",");
                sb.append("\"fptasGuarantee\":").append(String.format("%.2f", fptas.approximationGuarantee)).append(",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000).append(",");
                sb.append("\"explanation\":\"FPTAS value scaling delivered " + fptas.totalValue + " profit, satisfying >= " + String.format("%.0f%%", (1.0 - eps) * 100) + " of OPT.\"");
            } else if ("fpt-kernel".equalsIgnoreCase(algo)) {
                int k = parseInt(json.get("k"), 4);
                int[][] edges = {
                        {0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5}, // node 0 has degree 5 > k
                        {1, 2}, {2, 3}, {3, 4}, {4, 5}
                };

                ParameterizedVertexCover.FptResult res = ParameterizedVertexCover.solve(6, edges, k);
                long elapsed = System.nanoTime() - t0;

                sb.append("\"algorithm\":\"FPT Buss Kernelization (k-Vertex Cover)\",");
                sb.append("\"budgetK\":").append(k).append(",");
                sb.append("\"feasible\":").append(res.feasible).append(",");
                sb.append("\"kernelVertices\":").append(res.kernelVertices).append(",");
                sb.append("\"kernelEdges\":").append(res.kernelEdges).append(",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000).append(",");
                sb.append("\"coverNodes\":[");
                for (int i = 0; i < res.vertexCover.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(res.vertexCover.get(i));
                }
                sb.append("],\"explanation\":\"" + escapeJson(res.diagnosis) + "\"");
            } else if ("reduction-zoo".equalsIgnoreCase(algo)) {
                CustomArrayList<ReductionZoo.Clause> formula = new CustomArrayList<>();
                // (x1 v x2 v ~x3) ^ (~x1 v ~x2 v x3) ^ (x1 v ~x2 v x3)
                formula.add(new ReductionZoo.Clause(1, 2, -3));
                formula.add(new ReductionZoo.Clause(-1, -2, 3));
                formula.add(new ReductionZoo.Clause(1, -2, 3));
                boolean[] assignment = {true, false, true};

                ReductionZoo.ReductionResult res = ReductionZoo.reduce3SatToClique(formula, assignment);
                long elapsed = System.nanoTime() - t0;

                sb.append("\"algorithm\":\"NP Reduction Zoo (3-SAT -> CLIQUE)\",");
                sb.append("\"from\":\"").append(res.fromProblem).append("\",");
                sb.append("\"to\":\"").append(res.toProblem).append("\",");
                sb.append("\"witnessValid\":").append(res.witnessValid).append(",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000).append(",");
                sb.append("\"details\":\"").append(escapeJson(res.reductionDetails)).append("\",");
                sb.append("\"explanation\":\"" + escapeJson(res.certificateAnalysis) + "\"");
            } else if ("advisor".equalsIgnoreCase(algo)) {
                String query = json.get("feature");
                NPHardnessAdvisor.Diagnosis diag = NPHardnessAdvisor.diagnose(query);
                long elapsed = System.nanoTime() - t0;

                sb.append("\"algorithm\":\"NP-Hardness Structural Advisor\",");
                sb.append("\"problemType\":\"").append(diag.problemType).append("\",");
                sb.append("\"isNPHard\":").append(diag.isNPHard).append(",");
                sb.append("\"canonicalProblem\":\"").append(diag.canonicalProblem).append("\",");
                sb.append("\"reductionPath\":\"").append(diag.polynomialReductionPath).append("\",");
                sb.append("\"strategy\":\"").append(diag.recommendedStrategy).append("\",");
                sb.append("\"asymptoticCost\":\"").append(diag.asymptoticCost).append("\",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000);
            } else {
                long elapsed = System.nanoTime() - t0;
                sb.append("\"algorithm\":\"Approximation Algorithms\",");
                sb.append("\"status\":\"ready\",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000);
            }
            sb.append("}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    // =========================================================================
    //  MODULE 6: RANDOMISED & PARALLEL HANDLER (/api/dsa/random)
    // =========================================================================
    private class DsaRandomHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed. Use POST.\"}");
                return;
            }

            String body = readRequestBody(exchange);
            CustomHashMap<String, String> json = parseSimpleJson(body);
            String algo = json.get("algorithm");
            if (algo == null) algo = "miller-rabin";

            long t0 = System.nanoTime();
            StringBuilder sb = new StringBuilder("{");

            if ("miller-rabin".equalsIgnoreCase(algo)) {
                long candidate = 1000000007L;
                try {
                    String cStr = json.get("candidate");
                    if (cStr != null && !cStr.isEmpty()) candidate = Long.parseLong(cStr.trim());
                } catch (Exception ignored) {}

                int rounds = parseInt(json.get("rounds"), 12);
                MillerRabin.PrimalityResult res = MillerRabin.testProbabilistic(candidate, rounds);
                long elapsed = System.nanoTime() - t0;

                sb.append("\"algorithm\":\"Miller-Rabin Primality Test\",");
                sb.append("\"candidate\":").append(candidate).append(",");
                sb.append("\"isPrime\":").append(res.isPrime).append(",");
                sb.append("\"rounds\":").append(res.roundsTested).append(",");
                sb.append("\"witness\":").append(res.witness != null ? res.witness : "null").append(",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000).append(",");
                sb.append("\"details\":[");
                for (int i = 0; i < res.roundDetails.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append("\"").append(escapeJson(res.roundDetails.get(i))).append("\"");
                }
                sb.append("],\"explanation\":\"" + (res.isPrime ? "Candidate " + candidate + " passed all rounds (confidence > 99.9999%)." : "Candidate " + candidate + " proved composite.") + "\"");
            } else if ("randomized-quicksort".equalsIgnoreCase(algo)) {
                int size = parseInt(json.get("size"), 500);
                int[] arr = new int[size];
                for (int i = 0; i < size; i++) arr[i] = size - i; // Worst-case for deterministic!

                RandomizedQuickSort.SortMetrics rand = RandomizedQuickSort.sortRandomized(arr);
                RandomizedQuickSort.SortMetrics det  = RandomizedQuickSort.sortDeterministic(arr);
                long elapsed = System.nanoTime() - t0;

                sb.append("\"algorithm\":\"Randomized QuickSort vs Deterministic\",");
                sb.append("\"arraySize\":").append(size).append(",");
                sb.append("\"randComparisons\":").append(rand.comparisons).append(",");
                sb.append("\"randDepth\":").append(rand.maxRecursionDepth).append(",");
                sb.append("\"detComparisons\":").append(det.comparisons).append(",");
                sb.append("\"detDepth\":").append(det.maxRecursionDepth).append(",");
                sb.append("\"theoreticalExpected\":").append(String.format("%.0f", rand.theoreticalExpectedComparisons)).append(",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000).append(",");
                sb.append("\"explanation\":\"Randomized QuickSort completed in " + rand.comparisons + " comparisons (close to 2n ln n), while deterministic suffered " + det.comparisons + " comparisons.\"");
            } else if ("perfect-hashing".equalsIgnoreCase(algo)) {
                CustomArrayList<String> keys = new CustomArrayList<>();
                CustomArrayList<String> vals = new CustomArrayList<>();
                for (int i = 0; i < Math.min(20, dataset.courses.size()); i++) {
                    Course c = dataset.courses.get(i);
                    keys.add(c.getCourseCode());
                    vals.add(c.getCourseName());
                }

                PerfectHashing<String> ph = PerfectHashing.build(keys, vals);
                PerfectHashing.PerfectHashMetrics m = ph.getMetrics();
                String testKey = json.get("queryKey");
                if (testKey == null || testKey.isEmpty()) testKey = keys.isEmpty() ? "CS101" : keys.get(0);
                String foundVal = ph.get(testKey);
                long elapsed = System.nanoTime() - t0;

                sb.append("\"algorithm\":\"FKS Two-Level Perfect Hashing\",");
                sb.append("\"totalKeys\":").append(m.totalKeys).append(",");
                sb.append("\"primarySlots\":").append(m.primarySlots).append(",");
                sb.append("\"secondarySlots\":").append(m.totalSecondarySlots).append(",");
                sb.append("\"spaceOverhead\":").append(String.format("%.2f", m.spaceOverheadRatio)).append(",");
                sb.append("\"lookupKey\":\"").append(escapeJson(testKey)).append("\",");
                sb.append("\"foundValue\":\"").append(escapeJson(foundVal != null ? foundVal : "NOT_FOUND")).append("\",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000).append(",");
                sb.append("\"explanation\":\"FKS guaranteed O(1) worst-case lookup with zero collisions across all " + m.totalKeys + " keys.\"");
            } else if ("reservoir".equalsIgnoreCase(algo)) {
                int sampleK = parseInt(json.get("k"), 10);
                ReservoirSampling<String> sampler = new ReservoirSampling<>(sampleK);
                for (int i = 0; i < dataset.students.size(); i++) {
                    sampler.feed(dataset.students.get(i).getName() + " (" + dataset.students.get(i).getStudentId() + ")");
                }
                long elapsed = System.nanoTime() - t0;

                sb.append("\"algorithm\":\"Reservoir Sampling (Algorithm R)\",");
                sb.append("\"streamSize\":").append(sampler.getTotalSeen()).append(",");
                sb.append("\"sampleK\":").append(sampleK).append(",");
                sb.append("\"inclusionProbability\":").append(String.format("%.4f", sampler.getInclusionProbability())).append(",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000).append(",");
                sb.append("\"samples\":[");
                CustomArrayList<String> res = sampler.getReservoir();
                for (int i = 0; i < res.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append("\"").append(escapeJson(res.get(i))).append("\"");
                }
                sb.append("],\"explanation\":\"Processed all " + sampler.getTotalSeen() + " student records in a single streaming pass with uniform probability k/N.\"");
            } else if ("parallel-scan".equalsIgnoreCase(algo)) {
                int size = parseInt(json.get("size"), 16);
                int[] data = new int[size];
                for (int i = 0; i < size; i++) data[i] = i + 1;

                ParallelPrimitives.ScanResult scan = ParallelPrimitives.blellochParallelScan(data);
                ParallelPrimitives.ParallelMetrics m = scan.metrics;
                long elapsed = System.nanoTime() - t0;

                sb.append("\"algorithm\":\"Blelloch Work-Efficient Parallel Scan\",");
                sb.append("\"inputSize\":").append(size).append(",");
                sb.append("\"totalSum\":").append(scan.totalSum).append(",");
                sb.append("\"workT1\":").append(m.workT1).append(",");
                sb.append("\"spanTInf\":").append(m.spanTInfinity).append(",");
                sb.append("\"parallelism\":").append(String.format("%.1f", m.theoreticalParallelism)).append(",");
                sb.append("\"brentSpeedup\":{");
                int[] cores = {2, 4, 8, 16, 32, 64};
                for (int i = 0; i < cores.length; i++) {
                    if (i > 0) sb.append(",");
                    sb.append("\"cores_").append(cores[i]).append("\":").append(String.format("%.2f", m.brentSpeedupCores[i]));
                }
                sb.append("},\"elapsedMicros\":").append(elapsed / 1000).append(",");
                sb.append("\"explanation\":\"Blelloch up-sweep/down-sweep achieved O(n) work and O(log n) span with Brent speedup up to " + String.format("%.2f", m.brentSpeedupCores[5]) + "x on 64 cores.\"");
            } else {
                long elapsed = System.nanoTime() - t0;
                sb.append("\"algorithm\":\"Randomised & Parallel Algorithms\",");
                sb.append("\"status\":\"ready\",");
                sb.append("\"elapsedMicros\":").append(elapsed / 1000);
            }
            sb.append("}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    // =========================================================================
    //  MODULE 1: TEXTHACK INDIC CORPUS HANDLER (/api/texthack)
    // =========================================================================
    private class TextHackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }

            TextHackEngine the = engine.getTextHackEngine();
            String method = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(method)) {
                // Return all Indic corpus articles
                CustomArrayList<IndicCorpus.IndicArticle> articles = the.getCorpus();
                StringBuilder sb = new StringBuilder("{\"corpusSize\":").append(articles.size()).append(",\"articles\":[");
                for (int i = 0; i < articles.size(); i++) {
                    if (i > 0) sb.append(",");
                    IndicCorpus.IndicArticle a = articles.get(i);
                    sb.append("{\"id\":\"").append(a.id).append("\",")
                      .append("\"title\":\"").append(escapeJson(a.title)).append("\",")
                      .append("\"language\":\"").append(a.language).append("\",")
                      .append("\"script\":\"").append(a.script).append("\",")
                      .append("\"content\":\"").append(escapeJson(a.content)).append("\",")
                      .append("\"englishSummary\":\"").append(escapeJson(a.englishSummary)).append("\"}");
                }
                sb.append("]}");
                sendJsonResponse(exchange, 200, sb.toString());
                return;
            }

            // POST query handling
            String body = readRequestBody(exchange);
            CustomHashMap<String, String> json = parseSimpleJson(body);
            String qclass = json.get("queryClass");
            String query  = json.get("query");
            if (qclass == null) qclass = "pattern";
            if (query == null) query = "Turing";

            TextHackEngine.TextHackQueryResult res;
            switch (qclass.toLowerCase()) {
                case "pattern":
                    res = the.queryPatternSearch(query);
                    break;
                case "fuzzy":
                    res = the.queryFuzzyMatch(query, parseInt(json.get("maxDist"), 3));
                    break;
                case "similarity":
                    res = the.queryDocumentSimilarity("IND_HI_001", "IND_SA_003", 5);
                    break;
                case "citation-flow": {
                    int[][] citationGraph = {
                            {0, 1, 15}, {0, 2, 10}, {1, 3, 12}, {2, 3, 10}, {3, 4, 25}
                    };
                    res = the.queryCitationFlow(citationGraph, 5, 0, 4);
                    break;
                }
                case "scheduling": {
                    int[][] conflicts = {{0, 1}, {1, 2}, {2, 3}, {3, 0}, {0, 4}, {2, 4}};
                    res = the.queryProjectScheduling(5, conflicts);
                    break;
                }
                case "primality": {
                    long c = 1000000007L;
                    try { c = Long.parseLong(query.trim()); } catch (Exception ignored) {}
                    res = the.queryLargePrimePrimality(c);
                    break;
                }
                default:
                    res = the.queryPatternSearch(query);
                    break;
            }

            StringBuilder sb = new StringBuilder("{");
            sb.append("\"queryClass\":\"").append(escapeJson(res.queryClass)).append("\",");
            sb.append("\"algorithmFamily\":\"").append(escapeJson(res.algorithmFamily)).append("\",");
            sb.append("\"algorithmUsed\":\"").append(escapeJson(res.algorithmUsed)).append("\",");
            sb.append("\"asymptoticCost\":\"").append(escapeJson(res.asymptoticCost)).append("\",");
            sb.append("\"canonReference\":\"").append(escapeJson(res.textbookCanonReference)).append("\",");
            sb.append("\"elapsedMicros\":").append(res.executionNs / 1000).append(",");
            sb.append("\"answers\":[");
            for (int i = 0; i < res.answers.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(escapeJson(res.answers.get(i))).append("\"");
            }
            sb.append("]}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    // =========================================================================
    //  HTTP UTILITIES
    // =========================================================================

    private void handleOptions(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.sendResponseHeaders(204, -1);
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private void sendJsonResponse(HttpExchange exchange, int status, String json) throws IOException {
        addCorsHeaders(exchange);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int read;
        while ((read = is.read(buf)) != -1) {
            baos.write(buf, 0, read);
        }
        return baos.toString(StandardCharsets.UTF_8.name());
    }

    private CustomHashMap<String, String> parseQueryParams(String query) {
        CustomHashMap<String, String> map = new CustomHashMap<>();
        if (query == null || query.isEmpty()) return map;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                if (idx > 0) {
                    String k = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    String v = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                    map.put(k, v);
                } else if (!pair.isEmpty()) {
                    map.put(URLDecoder.decode(pair, "UTF-8"), "");
                }
            } catch (Exception ignored) {}
        }
        return map;
    }

    private CustomHashMap<String, String> parseSimpleJson(String json) {
        CustomHashMap<String, String> map = new CustomHashMap<>();
        if (json == null || json.trim().isEmpty()) return map;

        String content = json.trim();
        if (content.startsWith("{")) content = content.substring(1);
        if (content.endsWith("}")) content = content.substring(0, content.length() - 1);

        String[] tokens = content.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String token : tokens) {
            int colon = token.indexOf(":");
            if (colon > 0) {
                String k = token.substring(0, colon).trim().replaceAll("^\"|\"$", "");
                String v = token.substring(colon + 1).trim().replaceAll("^\"|\"$", "");
                map.put(k, v);
            }
        }
        return map;
    }

    private int parseInt(String s, int def) {
        if (s == null) return def;
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b");  break;
                case '\f': sb.append("\\f");  break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 32) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.toString();
    }
}
