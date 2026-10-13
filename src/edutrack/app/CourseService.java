package edutrack.app;

import edutrack.core.CustomArrayList;
import edutrack.model.Course;
import edutrack.string.KMP;
import edutrack.string.RabinKarp;

/**
 * Service layer for course operations.
 */
public class CourseService {

    private final CustomArrayList<Course> courses;

    public CourseService(CustomArrayList<Course> courses) {
        this.courses = courses;
    }

    /** Exact lookup by course code. */
    public Course findByCode(String courseCode) {
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).getCourseCode().equals(courseCode)) return courses.get(i);
        }
        return null;
    }

    /** KMP search for courses by keyword in name or description. */
    public CustomArrayList<Course> searchByKeyword(String keyword) {
        CustomArrayList<Course> results = new CustomArrayList<>();
        String kw = keyword.toLowerCase();
        for (int i = 0; i < courses.size(); i++) {
            Course c = courses.get(i);
            if (KMP.contains(c.getCourseName().toLowerCase(), kw) ||
                (c.getDescription() != null && KMP.contains(c.getDescription().toLowerCase(), kw))) {
                results.add(c);
            }
        }
        return results;
    }

    /** Rabin-Karp search for course codes matching a prefix. */
    public CustomArrayList<Course> searchByCodePrefix(String prefix) {
        CustomArrayList<Course> results = new CustomArrayList<>();
        for (int i = 0; i < courses.size(); i++) {
            Course c = courses.get(i);
            if (!RabinKarp.search(c.getCourseCode(), prefix).isEmpty()) {
                results.add(c);
            }
        }
        return results;
    }

    /** Filter by department. */
    public CustomArrayList<Course> filterByDepartment(String dept) {
        CustomArrayList<Course> results = new CustomArrayList<>();
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).getDepartment().equalsIgnoreCase(dept))
                results.add(courses.get(i));
        }
        return results;
    }
}
