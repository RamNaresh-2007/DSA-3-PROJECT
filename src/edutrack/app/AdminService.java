package edutrack.app;

import edutrack.core.CustomArrayList;
import edutrack.model.Admin;
import edutrack.dp.Levenshtein;
import edutrack.dp.DamerauLevenshtein;

/**
 * Service layer for administrator operations.
 * Provides exact lookup, department and access level filtering, and typo-tolerant search.
 */
public class AdminService {

    private final CustomArrayList<Admin> admins;

    public AdminService(CustomArrayList<Admin> admins) {
        this.admins = admins;
    }

    /** Exact search by admin ID. */
    public Admin findById(String adminId) {
        for (int i = 0; i < admins.size(); i++) {
            if (admins.get(i).getAdminId().equalsIgnoreCase(adminId)) {
                return admins.get(i);
            }
        }
        return null;
    }

    /** Filter administrators by department. */
    public CustomArrayList<Admin> filterByDepartment(String department) {
        CustomArrayList<Admin> results = new CustomArrayList<>();
        for (int i = 0; i < admins.size(); i++) {
            if (admins.get(i).getDepartment().equalsIgnoreCase(department)) {
                results.add(admins.get(i));
            }
        }
        return results;
    }

    /** Filter administrators by access level (e.g. SUPER_ADMIN, EXAM_ADMIN, IT_ADMIN). */
    public CustomArrayList<Admin> filterByAccessLevel(String accessLevel) {
        CustomArrayList<Admin> results = new CustomArrayList<>();
        for (int i = 0; i < admins.size(); i++) {
            if (admins.get(i).getAccessLevel().equalsIgnoreCase(accessLevel)) {
                results.add(admins.get(i));
            }
        }
        return results;
    }

    /** Search administrators by role title keyword. */
    public CustomArrayList<Admin> searchByRole(String roleKeyword) {
        CustomArrayList<Admin> results = new CustomArrayList<>();
        String kw = roleKeyword.toLowerCase();
        for (int i = 0; i < admins.size(); i++) {
            if (admins.get(i).getRole().toLowerCase().contains(kw)) {
                results.add(admins.get(i));
            }
        }
        return results;
    }

    /** Fuzzy search administrators by name using Levenshtein distance. */
    public CustomArrayList<Admin> fuzzySearchByName(String query, int maxDistance) {
        CustomArrayList<Admin> results = new CustomArrayList<>();
        String q = query.toLowerCase();
        for (int i = 0; i < admins.size(); i++) {
            Admin a = admins.get(i);
            int dist = Levenshtein.distance(q, a.getName().toLowerCase());
            if (dist <= maxDistance) {
                results.add(a);
            }
        }
        return results;
    }

    /** Fuzzy search administrators with transposition support using Damerau-Levenshtein. */
    public CustomArrayList<Admin> fuzzySearchWithTranspositions(String query, int maxDistance) {
        CustomArrayList<Admin> results = new CustomArrayList<>();
        String q = query.toLowerCase();
        for (int i = 0; i < admins.size(); i++) {
            Admin a = admins.get(i);
            int dist = DamerauLevenshtein.distance(q, a.getName().toLowerCase());
            if (dist <= maxDistance) {
                results.add(a);
            }
        }
        return results;
    }
}
