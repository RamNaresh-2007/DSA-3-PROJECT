package edutrack.model;

/**
 * Represents a university course in the EduTrack platform,
 * mapped to EduTrack.csv course catalog data.
 */
public class Course {
    private String courseId;
    private String courseCode;
    private String courseName;
    private String department;
    private int credits;
    private String facultyId;
    private String facultyName;
    private String description;

    public Course(String courseId, String courseCode, String courseName, int credits,
                  String facultyId, String facultyName, String department, String description) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.department = department;
        this.description = description;
    }

    public Course(String courseCode, String courseName, String department, int credits,
                  String facultyId, String description) {
        this(courseCode, courseCode, courseName, credits, facultyId, "Faculty In-Charge", department, description);
    }

    public String getCourseId()    { return courseId; }
    public String getCourseCode()  { return courseCode; }
    public String getCourseName()  { return courseName; }
    public String getDepartment()  { return department; }
    public int    getCredits()     { return credits; }
    public String getFacultyId()   { return facultyId; }
    public String getFacultyName() { return facultyName; }
    public String getDescription() { return description != null ? description : (courseName + " (" + courseCode + ") - Core university course."); }

    public void setCourseName(String courseName)   { this.courseName = courseName; }
    public void setDepartment(String department)   { this.department = department; }
    public void setCredits(int credits)             { this.credits = credits; }
    public void setFacultyId(String facultyId)     { this.facultyId = facultyId; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "Course{id='" + courseId + "', code='" + courseCode + "', name='" + courseName +
               "', dept='" + department + "', credits=" + credits + ", faculty='" + facultyName + "'}";
    }
}
