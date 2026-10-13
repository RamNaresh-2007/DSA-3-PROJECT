package edutrack.app;

import edutrack.core.CustomArrayList;
import edutrack.engine.EduTrackEngine;
import edutrack.engine.QueryResult;

/**
 * Unified search service that delegates to the EduTrack engine.
 * Provides a clean API for UI and web controller layers.
 */
public class SearchService {

    private final EduTrackEngine engine;

    public SearchService(EduTrackEngine engine) {
        this.engine = engine;
    }

    /** Search courses by keyword (KMP). */
    public QueryResult searchCourses(String keyword) {
        return engine.searchCoursesByKeyword(keyword);
    }

    /** Search by code (Rabin-Karp). */
    public QueryResult searchByCode(String code) {
        return engine.searchByCode(code);
    }

    /** Multi-keyword search (Aho-Corasick). */
    public QueryResult multiSearch(String[] keywords) {
        return engine.multiKeywordSearch(keywords);
    }

    /** Document substring search (Suffix Array). */
    public QueryResult searchInDocuments(String substring) {
        return engine.searchDocumentSubstring(substring);
    }

    /** Fuzzy search (Levenshtein). */
    public QueryResult fuzzySearch(String query, int maxDist) {
        return engine.fuzzySearchCourse(query, maxDist);
    }

    /** Fuzzy search with transpositions (Damerau-Levenshtein). */
    public QueryResult fuzzySearchTranspose(String query, int maxDist) {
        return engine.fuzzySearchWithTranspositions(query, maxDist);
    }
}
