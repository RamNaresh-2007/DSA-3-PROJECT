package edutrack.app;

import edutrack.core.CustomArrayList;
import edutrack.model.Student;
import edutrack.dp.Levenshtein;
import edutrack.dp.DamerauLevenshtein;

/**
 * Service layer for student operations.
 * Provides search and analytics using the algorithm engine.
 */
public class StudentService {

    private final CustomArrayList<Student> students;

    public StudentService(CustomArrayList<Student> students) {
        this.students = students;
    }

    /** Exact search by student ID. */
    public Student findById(String studentId) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getStudentId().equals(studentId)) return students.get(i);
        }
        return null;
    }

    /** Fuzzy search by name using Levenshtein distance. */
    public CustomArrayList<Student> fuzzySearchByName(String query, int maxDistance) {
        CustomArrayList<Student> results = new CustomArrayList<>();
        String q = query.toLowerCase();
        for (int i = 0; i < students.size(); i++) {
            Student s = students.get(i);
            int dist = Levenshtein.distance(q, s.getName().toLowerCase());
            if (dist <= maxDistance) results.add(s);
        }
        return results;
    }

    /** Fuzzy search with transposition support. */
    public CustomArrayList<Student> fuzzySearchWithTranspositions(String query, int maxDistance) {
        CustomArrayList<Student> results = new CustomArrayList<>();
        String q = query.toLowerCase();
        for (int i = 0; i < students.size(); i++) {
            Student s = students.get(i);
            int dist = DamerauLevenshtein.distance(q, s.getName().toLowerCase());
            if (dist <= maxDistance) results.add(s);
        }
        return results;
    }

    /** Filter by department. */
    public CustomArrayList<Student> filterByDepartment(String department) {
        CustomArrayList<Student> results = new CustomArrayList<>();
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getDepartment().equalsIgnoreCase(department))
                results.add(students.get(i));
        }
        return results;
    }

    /** Get top N students by CGPA. */
    public CustomArrayList<Student> topByCgpa(int n) {
        // Copy and sort
        CustomArrayList<Student> sorted = new CustomArrayList<>();
        for (int i = 0; i < students.size(); i++) sorted.add(students.get(i));
        sorted.sort((a, b) -> Double.compare(b.getCgpa(), a.getCgpa())); // descending
        CustomArrayList<Student> top = new CustomArrayList<>();
        for (int i = 0; i < Math.min(n, sorted.size()); i++) top.add(sorted.get(i));
        return top;
    }
}
