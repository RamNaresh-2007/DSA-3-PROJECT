package edutrack.engine;

import edutrack.core.CustomArrayList;

/**
 * Encapsulates the result of an EduTrack academic query.
 * Produces clean, human-readable terminal reports where algorithm topic names
 * appear strictly in brackets.
 */
public class QueryResult {

    private final QueryType queryType;
    private final String query;
    private final CustomArrayList<String> results;
    private final long executionTimeNanos;
    private final String summary;

    public QueryResult(QueryType queryType, String query, CustomArrayList<String> results,
                       long executionTimeNanos, String summary) {
        this.queryType = queryType;
        this.query = query;
        this.results = results;
        this.executionTimeNanos = executionTimeNanos;
        this.summary = summary;
    }

    public QueryType getQueryType()       { return queryType; }
    public String getQuery()              { return query; }
    public CustomArrayList<String> getResults() { return results; }
    public long getExecutionTimeNanos()   { return executionTimeNanos; }
    public String getSummary()            { return summary; }

    /** Execution time in milliseconds. */
    public double getExecutionTimeMs() {
        return executionTimeNanos / 1_000_000.0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n================================================================================\n");
        sb.append("  UNIVERSITY ACADEMIC PORTAL - ACTIVITY REPORT\n");
        sb.append("================================================================================\n");
        sb.append("  What was done    : ").append(queryType.description()).append("\n");
        sb.append("  Algorithm used   : ").append(queryType.topicBracket()).append("\n");
        sb.append("  Category         : ").append(queryType.algorithmFamily()).append("\n");
        sb.append("  What you typed   : ").append(truncate(query, 60)).append("\n");
        sb.append("  Time taken       : ").append(String.format("%.3f ms", getExecutionTimeMs())).append(" (Hand-built, zero java.util.*)\n");
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("  Summary          : ").append(summary).append("\n");
        sb.append("--------------------------------------------------------------------------------\n");

        if (!results.isEmpty()) {
            sb.append("  Results:\n");
            int limit = Math.min(results.size(), 25);
            for (int i = 0; i < limit; i++) {
                sb.append("    [").append(i + 1).append("] ").append(results.get(i)).append("\n");
            }
            if (results.size() > 25) {
                sb.append("    ... and ").append(results.size() - 25).append(" more item(s).\n");
            }
        } else {
            sb.append("  No matching records were found in the database.\n");
        }
        sb.append("================================================================================\n");
        return sb.toString();
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 3) + "...";
    }
}
