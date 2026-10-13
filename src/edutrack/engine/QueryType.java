package edutrack.engine;

/**
 * Academic services provided by the EduTrack platform.
 * All operations are named in natural human language, with algorithm
 * topic names placed strictly inside brackets.
 */
public enum QueryType {

    PATTERN_SEARCH_KMP,
    REPEATED_PHRASE_DETECTION,
    CODE_SEARCH_RABIN_KARP,
    MULTI_KEYWORD_SEARCH,
    DOCUMENT_SUBSTRING_SEARCH,
    LARGE_DOCUMENT_INDEX,
    CROSS_DOCUMENT_SIMILARITY,
    ADVANCED_SUBSTRING_QUERY,
    FUZZY_MATCH_LEVENSHTEIN,
    FUZZY_MATCH_DAMERAU,
    GLOBAL_ALIGNMENT,
    LOCAL_ALIGNMENT,
    MATRIX_CHAIN_OPTIMIZATION,
    OPTIMAL_KEY_LOOKUP,
    COURSE_COMBINATION_SEARCH,
    PREREQUISITE_ANALYSIS,
    ENROLLMENT_ANALYTICS;

    /**
     * Operation name in pure human language.
     */
    public String description() {
        switch (this) {
            case PATTERN_SEARCH_KMP:       return "Search course catalog by topic or keyword";
            case REPEATED_PHRASE_DETECTION: return "Check assignment submission for repeated phrases";
            case CODE_SEARCH_RABIN_KARP:   return "Search course codes and department identifiers";
            case MULTI_KEYWORD_SEARCH:     return "Scan study materials for multiple syllabus terms";
            case DOCUMENT_SUBSTRING_SEARCH:return "Search exact phrases inside lecture notes";
            case LARGE_DOCUMENT_INDEX:     return "Index long textbooks and reference books";
            case CROSS_DOCUMENT_SIMILARITY:return "Check for copied content between student submissions";
            case ADVANCED_SUBSTRING_QUERY: return "Analyze vocabulary and common text passages";
            case FUZZY_MATCH_LEVENSHTEIN:  return "Look up students and professors with typo tolerance";
            case FUZZY_MATCH_DAMERAU:      return "Correct misspelled names and swapped letters";
            case GLOBAL_ALIGNMENT:         return "Compare two syllabus versions line by line";
            case LOCAL_ALIGNMENT:          return "Find matching sections between homework and slides";
            case MATRIX_CHAIN_OPTIMIZATION:return "Optimize database join order for grade reports";
            case OPTIMAL_KEY_LOOKUP:       return "Speed up quick-search for popular courses";
            case COURSE_COMBINATION_SEARCH:return "Plan the shortest route between campus lab rooms";
            case PREREQUISITE_ANALYSIS:    return "Check degree prerequisites and elective conflicts";
            case ENROLLMENT_ANALYTICS:     return "Analyze student enrollment numbers across courses";
            default: return "Academic portal service";
        }
    }

    /**
     * Algorithm topic name enclosed in brackets only.
     */
    public String topicBracket() {
        switch (this) {
            case PATTERN_SEARCH_KMP:       return "[KMP]";
            case REPEATED_PHRASE_DETECTION: return "[Z-Function]";
            case CODE_SEARCH_RABIN_KARP:   return "[Rabin-Karp]";
            case MULTI_KEYWORD_SEARCH:     return "[Aho-Corasick]";
            case DOCUMENT_SUBSTRING_SEARCH:return "[Suffix Array]";
            case LARGE_DOCUMENT_INDEX:     return "[SA-IS]";
            case CROSS_DOCUMENT_SIMILARITY:return "[Kasai LCP]";
            case ADVANCED_SUBSTRING_QUERY: return "[Suffix Tree & Suffix Automaton]";
            case FUZZY_MATCH_LEVENSHTEIN:  return "[Levenshtein]";
            case FUZZY_MATCH_DAMERAU:      return "[Damerau-Levenshtein]";
            case GLOBAL_ALIGNMENT:         return "[Needleman-Wunsch]";
            case LOCAL_ALIGNMENT:          return "[Smith-Waterman]";
            case MATRIX_CHAIN_OPTIMIZATION:return "[Matrix-Chain Multiplication]";
            case OPTIMAL_KEY_LOOKUP:       return "[Optimal BST]";
            case COURSE_COMBINATION_SEARCH:return "[Bitmask DP]";
            case PREREQUISITE_ANALYSIS:    return "[Tree DP]";
            case ENROLLMENT_ANALYTICS:     return "[SOS DP]";
            default: return "[Custom Algorithm]";
        }
    }

    /**
     * Full menu display line: human language with topic name in brackets only.
     */
    public String menuDisplay() {
        return description() + " " + topicBracket();
    }

    /**
     * Engine algorithm detail string for reports.
     */
    public String engineAlgorithm() {
        return topicBracket();
    }

    /**
     * General functional area in human language.
     */
    public String algorithmFamily() {
        switch (this) {
            case PATTERN_SEARCH_KMP:
            case REPEATED_PHRASE_DETECTION:
            case CODE_SEARCH_RABIN_KARP:
            case MULTI_KEYWORD_SEARCH:
                return "Course Catalog & Search";
            case DOCUMENT_SUBSTRING_SEARCH:
            case LARGE_DOCUMENT_INDEX:
            case CROSS_DOCUMENT_SIMILARITY:
            case ADVANCED_SUBSTRING_QUERY:
                return "Digital Library & Plagiarism Checking";
            default:
                return "Student Records & Degree Planning";
        }
    }
}
