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
 * and provides RESTful APIs for real-time DSA query execution (KMP, Z, Rabin-Karp,
 * Aho-Corasick, Suffix Structures, Advanced DP) directly over EduTrack.csv datasets.</p>
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
        try {
            s = HttpServer.create(new InetSocketAddress(selectedPort), 0);
        } catch (IOException ex) {
            // If primary port is busy, fallback to alternate port
            selectedPort = (port == 8080) ? 8000 : 8081;
            s = HttpServer.create(new InetSocketAddress(selectedPort), 0);
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
        if (path.equals("EduTrack.csv") || path.equals("data/EduTrack.csv")) {
            File f = new File(path);
            if (f.exists()) return f;
            return new File("EduTrack.csv");
        }
        if (path.startsWith("web/")) {
            return new File(path);
        }
        // Check in web/ directly
        File f = new File("web/" + path);
        if (f.exists()) return f;
        return new File(path);
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
            int limit = parseInt(params.get("limit"), 50);
            String sortBy = params.get("sortBy");
            boolean desc = "desc".equalsIgnoreCase(params.get("order"));

            if (page < 1) page = 1;
            if (limit < 1) limit = 50;
            if (limit > 500) limit = 500;

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
