package edutrack.app;

import edutrack.core.CustomArrayList;
import edutrack.engine.EduTrackEngine;
import edutrack.engine.QueryResult;

/**
 * Analytics service — demonstrates advanced DP algorithms on academic data.
 */
public class AnalyticsService {

    private final EduTrackEngine engine;

    public AnalyticsService(EduTrackEngine engine) {
        this.engine = engine;
    }

    /** Matrix-chain multiplication optimization. */
    public QueryResult optimizeDataProcessing(int[] dimensions, String[] names) {
        return engine.optimizeMatrixChain(dimensions, names);
    }

    /** Optimal BST for key lookup. */
    public QueryResult buildOptimalLookup(String[] keys, double[] freqs) {
        return engine.buildOptimalBST(keys, freqs);
    }

    /** TSP/Bitmask DP for course combinations. */
    public QueryResult findCourseCombination(String[] names, int[][] matrix) {
        return engine.findOptimalCourseCombination(names, matrix);
    }

    /** Prerequisite tree analysis. */
    public QueryResult analyzePrerequisites(String[] names, int[][] edges) {
        return engine.analyzePrerequisiteTree(names, edges);
    }

    /** Enrollment analytics (SOS DP). */
    public QueryResult analyzeEnrollments(String[] courseNames, int[] enrollments) {
        return engine.analyzeEnrollments(courseNames, enrollments);
    }
}
