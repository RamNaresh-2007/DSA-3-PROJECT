package edutrack.model;

/**
 * Represents a student assignment submission in the EduTrack platform,
 * mapped to EduTrack.csv assignments and coursework.
 */
public class Assignment {
    private String assignmentId;
    private String courseCode;
    private String studentId;
    private String title;
    private String content;   // Assignment_Text (full body for pattern search & analysis)
    private String keywords;  // Academic_Keywords
    private double grade;     // Assignment_Score
    private String submissionDate;

    public Assignment(String assignmentId, String courseCode, String studentId,
                      String title, String content, String keywords, double grade, String submissionDate) {
        this.assignmentId = assignmentId;
        this.courseCode = courseCode;
        this.studentId = studentId;
        this.title = title;
        this.content = content;
        this.keywords = keywords;
        this.grade = grade;
        this.submissionDate = submissionDate;
    }

    public Assignment(String assignmentId, String courseCode, String studentId,
                      String title, String content, double grade, String submissionDate) {
        this(assignmentId, courseCode, studentId, title, content, "general academic keywords", grade, submissionDate);
    }

    public String getAssignmentId()   { return assignmentId; }
    public String getCourseCode()     { return courseCode; }
    public String getStudentId()      { return studentId; }
    public String getTitle()          { return title; }
    public String getContent()        { return content; }
    public String getKeywords()       { return keywords; }
    public double getGrade()          { return grade; }
    public String getSubmissionDate() { return submissionDate; }

    public void setTitle(String title)                   { this.title = title; }
    public void setContent(String content)               { this.content = content; }
    public void setKeywords(String keywords)             { this.keywords = keywords; }
    public void setGrade(double grade)                   { this.grade = grade; }
    public void setSubmissionDate(String submissionDate) { this.submissionDate = submissionDate; }

    @Override
    public String toString() {
        return "Assignment{id='" + assignmentId + "', course='" + courseCode +
               "', student='" + studentId + "', grade=" + grade + "}";
    }
}
