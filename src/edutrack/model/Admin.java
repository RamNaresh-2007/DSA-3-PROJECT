package edutrack.model;

/**
 * Represents an administrator or university official in the EduTrack platform.
 * Stores administrative credentials, designated office/department, role, and system access level.
 */
public class Admin {
    private String adminId;
    private String name;
    private String email;
    private String department;
    private String role;
    private String accessLevel;
    private String officeRoom;

    public Admin(String adminId, String name, String email, String department,
                 String role, String accessLevel, String officeRoom) {
        this.adminId = adminId;
        this.name = name;
        this.email = email;
        this.department = department;
        this.role = role;
        this.accessLevel = accessLevel;
        this.officeRoom = officeRoom;
    }

    public Admin(String adminId, String name, String email, String department,
                 String role, String accessLevel) {
        this(adminId, name, email, department, role, accessLevel, "Admin Block");
    }

    // --- Getters ---
    public String getAdminId()     { return adminId; }
    public String getName()        { return name; }
    public String getEmail()       { return email; }
    public String getDepartment()  { return department; }
    public String getRole()        { return role; }
    public String getAccessLevel() { return accessLevel; }
    public String getOfficeRoom()  { return officeRoom; }

    // --- Setters ---
    public void setName(String name)               { this.name = name; }
    public void setEmail(String email)             { this.email = email; }
    public void setDepartment(String department)   { this.department = department; }
    public void setRole(String role)               { this.role = role; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }
    public void setOfficeRoom(String officeRoom)   { this.officeRoom = officeRoom; }

    @Override
    public String toString() {
        return "Admin{id='" + adminId + "', name='" + name +
               "', dept='" + department + "', role='" + role +
               "', access='" + accessLevel + "', office='" + officeRoom + "'}";
    }
}
