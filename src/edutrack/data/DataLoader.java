package edutrack.data;

import edutrack.core.CustomArrayList;
import edutrack.core.CustomHashMap;
import edutrack.model.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Data Loader for the EduTrack University Dataset (EduTrack.csv).
 *
 * <p>Strictly adheres to project constraints: zero {@code java.util.*} collection imports.
 * Reads EduTrack.csv directly and populates custom collection structures.</p>
 */
public class DataLoader {

    public static final String DEFAULT_EDUTRACK_CSV = "EduTrack.csv";
    public static final String BACKUP_EDUTRACK_CSV  = "data/EduTrack.csv";

    /**
     * Container holding all entities loaded from EduTrack.csv.
     */
    public static class EduTrackDataset {
        public final CustomArrayList<Student> students = new CustomArrayList<>();
        public final CustomArrayList<Course> courses = new CustomArrayList<>();
        public final CustomArrayList<Faculty> faculty = new CustomArrayList<>();
        public final CustomArrayList<Assignment> assignments = new CustomArrayList<>();
        public final CustomArrayList<Document> documents = new CustomArrayList<>();
        public final CustomArrayList<Schedule> schedules = new CustomArrayList<>();
        public final CustomArrayList<AcademicActivity> activities = new CustomArrayList<>();
        public final CustomArrayList<Admin> admins = new CustomArrayList<>();
    }

    /**
     * Resolves the dataset file path (root or data/ folder).
     */
    public static File resolveCsvFile(String preferredPath) {
        if (preferredPath != null && !preferredPath.isEmpty()) {
            File f = new File(preferredPath);
            if (f.exists()) return f;
        }
        String[] candidates = {
            DEFAULT_EDUTRACK_CSV,
            BACKUP_EDUTRACK_CSV,
            "../" + DEFAULT_EDUTRACK_CSV,
            "../" + BACKUP_EDUTRACK_CSV,
            "../../" + DEFAULT_EDUTRACK_CSV
        };
        for (String c : candidates) {
            File f = new File(c);
            if (f.exists()) return f;
        }
        return new File("EduTrack.csv");
    }

    /**
     * Full single-pass ingestion of EduTrack.csv.
     * Populates students, courses, faculty, assignments, documents, schedules, activities, and admins.
     */
    public static EduTrackDataset loadEduTrackDataset(String filePath) throws IOException {
        EduTrackDataset ds = new EduTrackDataset();
        File file = resolveCsvFile(filePath);
        if (!file.exists()) {
            return ds;
        }

        CustomHashMap<String, Boolean> seenCourses = new CustomHashMap<>();
        CustomHashMap<String, Boolean> seenFaculty = new CustomHashMap<>();
        CustomHashMap<String, Boolean> seenResources = new CustomHashMap<>();
        CustomHashMap<String, Boolean> seenExams = new CustomHashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Header line
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = splitCsvLine(line);
                if (parts.length < 38) continue;

                String studentId       = parts[0].trim();
                String studentName     = parts[1].trim();
                int age                = parseIntSafe(parts[2].trim(), 20);
                String gender          = parts[3].trim();
                String department      = parts[4].trim();
                int year               = parseIntSafe(parts[5].trim(), 1);
                int semester           = parseIntSafe(parts[6].trim(), 1);
                String courseId        = parts[7].trim();
                String courseCode      = parts[8].trim();
                String courseName      = parts[9].trim();
                int credits            = parseIntSafe(parts[10].trim(), 3);
                String facultyId       = parts[11].trim();
                String facultyName     = parts[12].trim();
                double attendance      = parseDoubleSafe(parts[13].trim(), 75.0);
                double studyHours      = parseDoubleSafe(parts[14].trim(), 5.0);
                double assignmentScore = parseDoubleSafe(parts[15].trim(), 70.0);
                double midtermScore    = parseDoubleSafe(parts[16].trim(), 70.0);
                double examScore       = parseDoubleSafe(parts[17].trim(), 70.0);
                double cgpa            = parseDoubleSafe(parts[18].trim(), 7.0);
                String grade           = parts[19].trim();
                String academicStatus  = parts[20].trim();
                String activityId      = parts[21].trim();
                String activityType    = parts[22].trim();
                int activityScore      = parseIntSafe(parts[23].trim(), 100);
                String activityDate    = parts[24].trim();
                String assignmentId    = parts[25].trim();
                String assignmentText  = parts[26].trim();
                String academicKeywords= parts[27].trim();
                String examId          = parts[28].trim();
                String examDate        = parts[29].trim();
                String roomId          = parts[30].trim();
                String scheduleDay     = parts[31].trim();
                String scheduleTime    = parts[32].trim();
                String resourceId      = parts[33].trim();
                String resourceType    = parts[34].trim();
                String resourceTitle   = parts[35].trim();
                String queryText       = parts[36].trim();
                int conflictGroup      = parseIntSafe(parts[37].trim(), 100);

                // 1. Student
                Student student = new Student(studentId, studentName, age, gender, department,
                        year, semester, courseId, courseCode, courseName, credits, facultyId,
                        facultyName, attendance, studyHours, assignmentScore, midtermScore,
                        examScore, cgpa, grade, academicStatus, activityId, activityType,
                        activityScore, activityDate, assignmentId, assignmentText, academicKeywords,
                        examId, examDate, roomId, scheduleDay, scheduleTime, resourceId,
                        resourceType, resourceTitle, queryText, conflictGroup);
                ds.students.add(student);

                // 2. Course (deduplicated by Course_ID)
                if (seenCourses.get(courseId) == null) {
                    seenCourses.put(courseId, Boolean.TRUE);
                    String dept = departmentForCourse(courseCode, department);
                    Course course = new Course(courseId, courseCode, courseName, credits,
                            facultyId, facultyName, dept,
                            courseName + " (" + courseCode + ") - Core academic syllabus under " + dept);
                    ds.courses.add(course);
                }

                // 3. Faculty (deduplicated by Faculty_ID)
                if (seenFaculty.get(facultyId) == null) {
                    seenFaculty.put(facultyId, Boolean.TRUE);
                    String cleanName = facultyName.replace("Dr. ", "").trim().toLowerCase().replaceAll("\\s+", ".");
                    String email = cleanName + "@univ.edu";
                    Faculty faculty = new Faculty(facultyId, facultyName, email, department, courseName + " & Systems");
                    ds.faculty.add(faculty);
                }

                // 4. Assignment
                if (ds.assignments.size() < 1000) { // Keep high-frequency index set for fast engine queries
                    Assignment asg = new Assignment(assignmentId, courseCode, studentId,
                            courseName + " Assignment (" + assignmentId + ")",
                            assignmentText, academicKeywords, assignmentScore, activityDate);
                    ds.assignments.add(asg);
                }

                // 5. Document / Resource (deduplicated by Resource_ID)
                if (seenResources.get(resourceId) == null && ds.documents.size() < 500) {
                    seenResources.put(resourceId, Boolean.TRUE);
                    Document doc = new Document(resourceId, resourceTitle, facultyName, courseCode,
                            assignmentText + ". Keywords: " + academicKeywords + ". Query: " + queryText,
                            resourceType, queryText);
                    ds.documents.add(doc);
                }

                // 6. Schedule / Exam (deduplicated by Exam_ID)
                if (seenExams.get(examId) == null && ds.schedules.size() < 500) {
                    seenExams.put(examId, Boolean.TRUE);
                    Schedule sch = new Schedule(examId, courseCode, "EXAM", scheduleDay,
                            scheduleTime, calculateEndTime(scheduleTime), roomId,
                            facultyId, examDate, conflictGroup);
                    ds.schedules.add(sch);
                }

                // 7. Activity
                if (ds.activities.size() < 1000) {
                    AcademicActivity act = new AcademicActivity(activityId, studentId, activityType, activityScore, activityDate);
                    ds.activities.add(act);
                }
            }
        }

        // Add standard institutional administrators
        loadStandardAdmins(ds.admins);

        return ds;
    }

    /**
     * Backwards-compatible loadStudents method reading from EduTrack.csv.
     */
    public static CustomArrayList<Student> loadStudents(String filePath) throws IOException {
        return loadEduTrackDataset(filePath).students;
    }

    /**
     * Backwards-compatible loadFaculty method reading from EduTrack.csv.
     */
    public static CustomArrayList<Faculty> loadFaculty(String filePath) throws IOException {
        return loadEduTrackDataset(filePath).faculty;
    }

    /**
     * Backwards-compatible loadCourses method reading from EduTrack.csv.
     */
    public static CustomArrayList<Course> loadCourses(String filePath) throws IOException {
        return loadEduTrackDataset(filePath).courses;
    }

    /**
     * Backwards-compatible loadAdmins method.
     */
    public static CustomArrayList<Admin> loadAdmins(String filePath) throws IOException {
        CustomArrayList<Admin> list = new CustomArrayList<>();
        loadStandardAdmins(list);
        return list;
    }

    private static void loadStandardAdmins(CustomArrayList<Admin> list) {
        list.add(new Admin("ADM001", "Dr. Rajeshwar Sharma", "dean.academic@univ.edu", "Academic Affairs", "Dean of Academic Affairs", "SUPER_ADMIN", "Admin Block Room 101"));
        list.add(new Admin("ADM002", "Dr. Meera Krishnan",   "registrar@univ.edu",     "Office of the Registrar", "University Registrar", "SUPER_ADMIN", "Admin Block Room 102"));
        list.add(new Admin("ADM003", "Prof. Sanjay Deshmukh","exam.controller@univ.edu","Examination Branch", "Controller of Examinations", "EXAM_ADMIN", "Exam Wing Room 204"));
        list.add(new Admin("ADM004", "Pooja Varma",           "admissions@univ.edu",    "Admissions & Records", "Director of Admissions", "ADMISSIONS_ADMIN", "Admissions Center Room 105"));
        list.add(new Admin("ADM005", "Vikramaditya Bose",     "it.director@univ.edu",   "IT & Infrastructure", "Chief System Administrator", "IT_ADMIN", "Tech Center Server Room A"));
    }

    private static String departmentForCourse(String code, String fallback) {
        if (code.startsWith("CS")) return "CSE";
        if (code.startsWith("AI")) return "AIML";
        if (code.startsWith("IT")) return "IT";
        if (code.startsWith("EC")) return "ECE";
        if (code.startsWith("SE")) return "CSE";
        if (code.startsWith("MA")) return "MATH";
        return fallback;
    }

    private static String calculateEndTime(String startTime) {
        if (startTime == null || !startTime.contains(":")) return "12:00";
        String[] parts = startTime.split(":");
        try {
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            h = (h + 3) % 24;
            return String.format("%02d:%02d", h, m);
        } catch (Exception e) {
            return "12:00";
        }
    }

    private static int parseIntSafe(String val, int def) {
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return def;
        }
    }

    private static double parseDoubleSafe(String val, double def) {
        try {
            return Double.parseDouble(val);
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * Splits a CSV line taking into consideration quoted values.
     */
    public static String[] splitCsvLine(String line) {
        CustomArrayList<String> tokens = new CustomArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());

        String[] result = new String[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            result[i] = tokens.get(i);
        }
        return result;
    }
}
