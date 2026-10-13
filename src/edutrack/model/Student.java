package edutrack.model;

/**
 * Represents a student in the university system, fully mapped to EduTrack.csv.
 * Stores comprehensive demographic, course enrollment, continuous assessment,
 * and academic performance indicators.
 */
public class Student {
    private String studentId;
    private String name;
    private int age;
    private String gender;
    private String department;
    private int year;
    private int semester;
    private String courseId;
    private String courseCode;
    private String courseName;
    private int credits;
    private String facultyId;
    private String facultyName;
    private double attendance;
    private double studyHours;
    private double assignmentScore;
    private double midtermScore;
    private double examScore;
    private double cgpa;
    private String grade;
    private String academicStatus;
    private String activityId;
    private String activityType;
    private int activityScore;
    private String activityDate;
    private String assignmentId;
    private String assignmentText;
    private String academicKeywords;
    private String examId;
    private String examDate;
    private String roomId;
    private String scheduleDay;
    private String scheduleTime;
    private String resourceId;
    private String resourceType;
    private String resourceTitle;
    private String queryText;
    private int conflictGroup;
    private String email;

    /**
     * Minimal constructor for backwards compatibility with tests and services.
     */
    public Student(String studentId, String name, String email, String department, int semester, double cgpa) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.department = department;
        this.semester = semester;
        this.cgpa = cgpa;
        this.year = Math.max(1, (semester + 1) / 2);
        this.grade = cgpa >= 9.0 ? "A+" : cgpa >= 8.0 ? "A" : cgpa >= 7.0 ? "B" : cgpa >= 6.0 ? "C" : cgpa >= 5.0 ? "D" : "F";
        this.academicStatus = cgpa >= 5.0 ? "Pass" : "Fail";
    }

    /**
     * Full constructor mapping every attribute from EduTrack.csv.
     */
    public Student(String studentId, String name, int age, String gender, String department,
                   int year, int semester, String courseId, String courseCode, String courseName,
                   int credits, String facultyId, String facultyName, double attendance,
                   double studyHours, double assignmentScore, double midtermScore, double examScore,
                   double cgpa, String grade, String academicStatus, String activityId,
                   String activityType, int activityScore, String activityDate, String assignmentId,
                   String assignmentText, String academicKeywords, String examId, String examDate,
                   String roomId, String scheduleDay, String scheduleTime, String resourceId,
                   String resourceType, String resourceTitle, String queryText, int conflictGroup) {
        this.studentId = studentId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.department = department;
        this.year = year;
        this.semester = semester;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.attendance = attendance;
        this.studyHours = studyHours;
        this.assignmentScore = assignmentScore;
        this.midtermScore = midtermScore;
        this.examScore = examScore;
        this.cgpa = cgpa;
        this.grade = grade;
        this.academicStatus = academicStatus;
        this.activityId = activityId;
        this.activityType = activityType;
        this.activityScore = activityScore;
        this.activityDate = activityDate;
        this.assignmentId = assignmentId;
        this.assignmentText = assignmentText;
        this.academicKeywords = academicKeywords;
        this.examId = examId;
        this.examDate = examDate;
        this.roomId = roomId;
        this.scheduleDay = scheduleDay;
        this.scheduleTime = scheduleTime;
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.resourceTitle = resourceTitle;
        this.queryText = queryText;
        this.conflictGroup = conflictGroup;
        this.email = studentId.toLowerCase() + "@univ.edu";
    }

    // --- Getters ---
    public String getStudentId()        { return studentId; }
    public String getName()             { return name; }
    public int    getAge()              { return age; }
    public String getGender()           { return gender; }
    public String getDepartment()       { return department; }
    public int    getYear()             { return year; }
    public int    getSemester()         { return semester; }
    public String getCourseId()         { return courseId; }
    public String getCourseCode()       { return courseCode; }
    public String getCourseName()       { return courseName; }
    public int    getCredits()          { return credits; }
    public String getFacultyId()        { return facultyId; }
    public String getFacultyName()      { return facultyName; }
    public double getAttendance()       { return attendance; }
    public double getStudyHours()       { return studyHours; }
    public double getAssignmentScore()  { return assignmentScore; }
    public double getMidtermScore()     { return midtermScore; }
    public double getExamScore()        { return examScore; }
    public double getCgpa()             { return cgpa; }
    public String getGrade()            { return grade; }
    public String getAcademicStatus()   { return academicStatus; }
    public String getActivityId()       { return activityId; }
    public String getActivityType()     { return activityType; }
    public int    getActivityScore()    { return activityScore; }
    public String getActivityDate()     { return activityDate; }
    public String getAssignmentId()     { return assignmentId; }
    public String getAssignmentText()   { return assignmentText; }
    public String getAcademicKeywords() { return academicKeywords; }
    public String getExamId()           { return examId; }
    public String getExamDate()         { return examDate; }
    public String getRoomId()           { return roomId; }
    public String getScheduleDay()      { return scheduleDay; }
    public String getScheduleTime()     { return scheduleTime; }
    public String getResourceId()       { return resourceId; }
    public String getResourceType()     { return resourceType; }
    public String getResourceTitle()    { return resourceTitle; }
    public String getQueryText()        { return queryText; }
    public int    getConflictGroup()    { return conflictGroup; }
    public String getEmail()            { return email != null ? email : (studentId.toLowerCase() + "@univ.edu"); }

    // --- Setters ---
    public void setName(String name)             { this.name = name; }
    public void setEmail(String email)           { this.email = email; }
    public void setDepartment(String department) { this.department = department; }
    public void setSemester(int semester)         { this.semester = semester; }
    public void setCgpa(double cgpa)              { this.cgpa = cgpa; }
    public void setGrade(String grade)           { this.grade = grade; }
    public void setAttendance(double attendance) { this.attendance = attendance; }

    @Override
    public String toString() {
        return "Student{id='" + studentId + "', name='" + name +
               "', dept='" + department + "', sem=" + semester + ", cgpa=" + cgpa +
               ", grade='" + grade + "', status='" + academicStatus + "'}";
    }
}
