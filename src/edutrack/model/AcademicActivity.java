package edutrack.model;

/**
 * Represents a student's continuous academic engagement activity,
 * mapped to EduTrack.csv activity tracking records.
 */
public class AcademicActivity {
    private String activityId;
    private String studentId;
    private String activityType;
    private int score;
    private String date;

    public AcademicActivity(String activityId, String studentId, String activityType, int score, String date) {
        this.activityId = activityId;
        this.studentId = studentId;
        this.activityType = activityType;
        this.score = score;
        this.date = date;
    }

    public String getActivityId()   { return activityId; }
    public String getStudentId()    { return studentId; }
    public String getActivityType() { return activityType; }
    public int    getScore()        { return score; }
    public String getDate()         { return date; }

    public void setActivityType(String activityType) { this.activityType = activityType; }
    public void setScore(int score)                  { this.score = score; }
    public void setDate(String date)                 { this.date = date; }

    @Override
    public String toString() {
        return "AcademicActivity{id='" + activityId + "', student='" + studentId +
               "', type='" + activityType + "', score=" + score + ", date='" + date + "'}";
    }
}
