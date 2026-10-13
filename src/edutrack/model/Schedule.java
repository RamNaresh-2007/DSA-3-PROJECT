package edutrack.model;

/**
 * Represents an academic schedule entry (class, exam, room allocation),
 * mapped to EduTrack.csv exam schedules and conflict groups.
 */
public class Schedule {
    private String scheduleId;
    private String courseCode;
    private String type;       // EXAM, LECTURE, LAB
    private String day;        // e.g. "Friday"
    private String startTime;  // e.g. "10:00"
    private String endTime;    // e.g. "13:00"
    private String room;       // e.g. "B-408"
    private String facultyId;
    private String examDate;   // e.g. "2026-11-11"
    private int conflictGroup; // e.g. 420

    public Schedule(String scheduleId, String courseCode, String type,
                    String day, String startTime, String endTime, String room,
                    String facultyId, String examDate, int conflictGroup) {
        this.scheduleId = scheduleId;
        this.courseCode = courseCode;
        this.type = type;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.facultyId = facultyId;
        this.examDate = examDate;
        this.conflictGroup = conflictGroup;
    }

    public Schedule(String scheduleId, String courseCode, String type,
                    String day, String startTime, String endTime, String room, String facultyId) {
        this(scheduleId, courseCode, type, day, startTime, endTime, room, facultyId, "2026-11-15", 100);
    }

    public String getScheduleId()    { return scheduleId; }
    public String getCourseCode()    { return courseCode; }
    public String getType()          { return type; }
    public String getDay()           { return day; }
    public String getStartTime()     { return startTime; }
    public String getEndTime()       { return endTime; }
    public String getRoom()          { return room; }
    public String getFacultyId()     { return facultyId; }
    public String getExamDate()      { return examDate; }
    public int    getConflictGroup() { return conflictGroup; }

    public void setDay(String day)                   { this.day = day; }
    public void setStartTime(String startTime)       { this.startTime = startTime; }
    public void setEndTime(String endTime)           { this.endTime = endTime; }
    public void setRoom(String room)                 { this.room = room; }
    public void setExamDate(String examDate)         { this.examDate = examDate; }
    public void setConflictGroup(int conflictGroup) { this.conflictGroup = conflictGroup; }

    @Override
    public String toString() {
        return "Schedule{id='" + scheduleId + "', course='" + courseCode +
               "', day='" + day + "', time='" + startTime + "', room='" + room +
               "', conflictGroup=" + conflictGroup + "}";
    }
}
