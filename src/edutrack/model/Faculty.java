package edutrack.model;

/**
 * Represents a faculty member.
 */
public class Faculty {
    private String facultyId;
    private String name;
    private String email;
    private String department;
    private String specialization;

    public Faculty(String facultyId, String name, String email, String department, String specialization) {
        this.facultyId = facultyId;
        this.name = name;
        this.email = email;
        this.department = department;
        this.specialization = specialization;
    }

    public String getFacultyId()      { return facultyId; }
    public String getName()           { return name; }
    public String getEmail()          { return email; }
    public String getDepartment()     { return department; }
    public String getSpecialization() { return specialization; }

    public void setName(String name)                     { this.name = name; }
    public void setEmail(String email)                   { this.email = email; }
    public void setDepartment(String department)         { this.department = department; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    @Override
    public String toString() {
        return "Faculty{id='" + facultyId + "', name='" + name +
               "', dept='" + department + "', spec='" + specialization + "'}";
    }
}
