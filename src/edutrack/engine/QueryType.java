package edutrack.engine;

/**
 * Academic & Advanced-Algorithm services provided by the EduTrack / TextHack platform.
 * Covers all 6 modules of the Advanced DSA-3 Canon:
 * <ul>
 *   <li>Module 1: TextHack as a System (Indic Wikipedia Question Bank)</li>
 *   <li>Module 2: String Algorithms & Suffix Structures (CO1, CO2)</li>
 *   <li>Module 3: Advanced Dynamic Programming (CO3)</li>
 *   <li>Module 4: Network Flow (Ford-Fulkerson, Edmonds-Karp, Dinic, Min-Cut, Matching, Reductions)</li>
 *   <li>Module 5: NP-Completeness & Approximation (Reductions, 2-Approx, FPTAS, FPT Kernelization)</li>
 *   <li>Module 6: Randomised & Parallel Algorithms (Miller-Rabin, Universal/Perfect Hashing, Reservoir, Blelloch)</li>
 * </ul>
 */
public enum QueryType {

    // Module 2 — String Algorithms (CO1)
    PATTERN_SEARCH_KMP,
    REPEATED_PHRASE_DETECTION,
    CODE_SEARCH_RABIN_KARP,
    MULTI_KEYWORD_SEARCH,

    // Module 2 — Suffix Structures (CO2)
    DOCUMENT_SUBSTRING_SEARCH,
    LARGE_DOCUMENT_INDEX,
    CROSS_DOCUMENT_SIMILARITY,
    ADVANCED_SUBSTRING_QUERY,

    // Module 3 — Advanced DP (CO3)
    FUZZY_MATCH_LEVENSHTEIN,
    FUZZY_MATCH_DAMERAU,
    GLOBAL_ALIGNMENT,
    LOCAL_ALIGNMENT,
    MATRIX_CHAIN_OPTIMIZATION,
    OPTIMAL_KEY_LOOKUP,
    COURSE_COMBINATION_SEARCH,
    PREREQUISITE_ANALYSIS,
    ENROLLMENT_ANALYTICS,

    // Module 4 — Network Flow
    MAX_FLOW_EDMONDS_KARP,
    MAX_FLOW_DINIC,
    MIN_CUT_PARTITION,
    BIPARTITE_MATCHING_ASSIGNMENT,
    MIN_COST_MAX_FLOW_ASSIGNMENT,
    PROJECT_SELECTION_REDUCTION,

    // Module 5 — NP-Completeness & Approximation
    VERTEX_COVER_2_APPROX,
    KNAPSACK_FPTAS_APPROX,
    FPT_VERTEX_COVER_KERNEL,
    REDUCTION_ZOO_VERIFICATION,

    // Module 6 — Randomised & Parallel Algorithms
    MILLER_RABIN_PRIMALITY,
    RANDOMIZED_QUICKSORT_ANALYSIS,
    PERFECT_HASHING_FKS,
    RESERVOIR_STREAM_SAMPLING,
    PARALLEL_SCAN_BLELLOCH,

    // Module 1 — TextHack Indic Question Bank
    INDIC_WIKIPEDIA_QUERY;

    /**
     * Operation name in pure human language.
     */
    public String description() {
        switch (this) {
            case PATTERN_SEARCH_KMP:            return "Search course catalog by topic or keyword";
            case REPEATED_PHRASE_DETECTION:     return "Check assignment submission for repeated phrases";
            case CODE_SEARCH_RABIN_KARP:        return "Search course codes and department identifiers";
            case MULTI_KEYWORD_SEARCH:          return "Scan study materials for multiple syllabus terms";
            case DOCUMENT_SUBSTRING_SEARCH:     return "Search exact phrases inside lecture notes";
            case LARGE_DOCUMENT_INDEX:          return "Index long textbooks and reference books";
            case CROSS_DOCUMENT_SIMILARITY:     return "Check for copied content between student submissions";
            case ADVANCED_SUBSTRING_QUERY:      return "Analyze vocabulary and common text passages";
            case FUZZY_MATCH_LEVENSHTEIN:       return "Look up students and professors with typo tolerance";
            case FUZZY_MATCH_DAMERAU:           return "Correct misspelled names and swapped letters";
            case GLOBAL_ALIGNMENT:              return "Compare two syllabus versions line by line";
            case LOCAL_ALIGNMENT:               return "Find matching sections between homework and slides";
            case MATRIX_CHAIN_OPTIMIZATION:     return "Optimize database join order for grade reports";
            case OPTIMAL_KEY_LOOKUP:            return "Speed up quick-search for popular courses";
            case COURSE_COMBINATION_SEARCH:     return "Plan the shortest route between campus lab rooms";
            case PREREQUISITE_ANALYSIS:         return "Check degree prerequisites and elective conflicts";
            case ENROLLMENT_ANALYTICS:          return "Analyze student enrollment numbers across courses";
            case MAX_FLOW_EDMONDS_KARP:         return "Calculate university network bandwidth and student flow";
            case MAX_FLOW_DINIC:                return "Compute fast blocking flow over lab network";
            case MIN_CUT_PARTITION:             return "Identify network bottleneck and partition campus nodes";
            case BIPARTITE_MATCHING_ASSIGNMENT: return "Optimal conflict-free student lab/project allocation";
            case MIN_COST_MAX_FLOW_ASSIGNMENT:  return "Assign student preferences under course capacity limits";
            case PROJECT_SELECTION_REDUCTION:   return "Select maximum profit research projects with prerequisites";
            case VERTEX_COVER_2_APPROX:         return "Deploy minimum campus monitoring checkpoints";
            case KNAPSACK_FPTAS_APPROX:         return "Optimize research funding allocation under budget";
            case FPT_VERTEX_COVER_KERNEL:       return "Fixed-parameter tractable conflict resolution";
            case REDUCTION_ZOO_VERIFICATION:    return "Formal polynomial-time reduction pipeline step-through";
            case MILLER_RABIN_PRIMALITY:        return "Cryptographic large-prime verification for academic security";
            case RANDOMIZED_QUICKSORT_ANALYSIS: return "Las Vegas randomized record sorting and expected comparisons";
            case PERFECT_HASHING_FKS:           return "Instantaneous zero-collision O(1) student ID lookup";
            case RESERVOIR_STREAM_SAMPLING:     return "Real-time uniform audit sampling from active engagement stream";
            case PARALLEL_SCAN_BLELLOCH:        return "Blelloch work-efficient parallel prefix sum and Brent speedup";
            case INDIC_WIKIPEDIA_QUERY:         return "Query Indian-language Wikipedia articles and heritage corpus";
            default: return "Academic portal service";
        }
    }

    /**
     * Algorithm topic name enclosed in brackets.
     */
    public String topicBracket() {
        switch (this) {
            case PATTERN_SEARCH_KMP:            return "[KMP]";
            case REPEATED_PHRASE_DETECTION:     return "[Z-Function]";
            case CODE_SEARCH_RABIN_KARP:        return "[Rabin-Karp]";
            case MULTI_KEYWORD_SEARCH:          return "[Aho-Corasick]";
            case DOCUMENT_SUBSTRING_SEARCH:     return "[Suffix Array]";
            case LARGE_DOCUMENT_INDEX:          return "[SA-IS]";
            case CROSS_DOCUMENT_SIMILARITY:     return "[Kasai LCP]";
            case ADVANCED_SUBSTRING_QUERY:      return "[Suffix Tree & Suffix Automaton]";
            case FUZZY_MATCH_LEVENSHTEIN:       return "[Levenshtein]";
            case FUZZY_MATCH_DAMERAU:           return "[Damerau-Levenshtein]";
            case GLOBAL_ALIGNMENT:              return "[Needleman-Wunsch]";
            case LOCAL_ALIGNMENT:               return "[Smith-Waterman]";
            case MATRIX_CHAIN_OPTIMIZATION:     return "[Matrix-Chain Multiplication]";
            case OPTIMAL_KEY_LOOKUP:            return "[Optimal BST]";
            case COURSE_COMBINATION_SEARCH:     return "[Bitmask DP]";
            case PREREQUISITE_ANALYSIS:         return "[Tree DP]";
            case ENROLLMENT_ANALYTICS:          return "[SOS DP]";
            case MAX_FLOW_EDMONDS_KARP:         return "[Edmonds-Karp O(VE^2)]";
            case MAX_FLOW_DINIC:                return "[Dinic O(V^2 E)]";
            case MIN_CUT_PARTITION:             return "[Min-Cut Duality]";
            case BIPARTITE_MATCHING_ASSIGNMENT: return "[Bipartite Matching & König's Theorem]";
            case MIN_COST_MAX_FLOW_ASSIGNMENT:  return "[Min-Cost Max-Flow SSP]";
            case PROJECT_SELECTION_REDUCTION:   return "[Project Selection Min-Cut]";
            case VERTEX_COVER_2_APPROX:         return "[Vertex Cover 2-Approximation]";
            case KNAPSACK_FPTAS_APPROX:         return "[Knapsack FPTAS & Greedy]";
            case FPT_VERTEX_COVER_KERNEL:       return "[FPT Buss Kernelisation]";
            case REDUCTION_ZOO_VERIFICATION:    return "[NP Reduction Zoo 3-SAT->Clique->IS->VC]";
            case MILLER_RABIN_PRIMALITY:        return "[Miller-Rabin Primality Test]";
            case RANDOMIZED_QUICKSORT_ANALYSIS: return "[Randomized QuickSort Expected 2n ln n]";
            case PERFECT_HASHING_FKS:           return "[FKS 2-Level Perfect Hashing O(1)]";
            case RESERVOIR_STREAM_SAMPLING:     return "[Reservoir Sampling Algorithm R]";
            case PARALLEL_SCAN_BLELLOCH:        return "[Blelloch Parallel Scan & Brent's Theorem]";
            case INDIC_WIKIPEDIA_QUERY:         return "[TextHack Indic Corpus Engine]";
            default: return "[Custom Algorithm]";
        }
    }

    public String menuDisplay() {
        return description() + " " + topicBracket();
    }

    public String engineAlgorithm() {
        return topicBracket();
    }

    public String algorithmFamily() {
        switch (this) {
            case PATTERN_SEARCH_KMP:
            case REPEATED_PHRASE_DETECTION:
            case CODE_SEARCH_RABIN_KARP:
            case MULTI_KEYWORD_SEARCH:
                return "Module 2: String Algorithms";
            case DOCUMENT_SUBSTRING_SEARCH:
            case LARGE_DOCUMENT_INDEX:
            case CROSS_DOCUMENT_SIMILARITY:
            case ADVANCED_SUBSTRING_QUERY:
                return "Module 2: Suffix Structures";
            case FUZZY_MATCH_LEVENSHTEIN:
            case FUZZY_MATCH_DAMERAU:
            case GLOBAL_ALIGNMENT:
            case LOCAL_ALIGNMENT:
            case MATRIX_CHAIN_OPTIMIZATION:
            case OPTIMAL_KEY_LOOKUP:
            case COURSE_COMBINATION_SEARCH:
            case PREREQUISITE_ANALYSIS:
            case ENROLLMENT_ANALYTICS:
                return "Module 3: Advanced Dynamic Programming";
            case MAX_FLOW_EDMONDS_KARP:
            case MAX_FLOW_DINIC:
            case MIN_CUT_PARTITION:
            case BIPARTITE_MATCHING_ASSIGNMENT:
            case MIN_COST_MAX_FLOW_ASSIGNMENT:
            case PROJECT_SELECTION_REDUCTION:
                return "Module 4: Network Flow";
            case VERTEX_COVER_2_APPROX:
            case KNAPSACK_FPTAS_APPROX:
            case FPT_VERTEX_COVER_KERNEL:
            case REDUCTION_ZOO_VERIFICATION:
                return "Module 5: NP-Completeness & Approximation";
            case MILLER_RABIN_PRIMALITY:
            case RANDOMIZED_QUICKSORT_ANALYSIS:
            case PERFECT_HASHING_FKS:
            case RESERVOIR_STREAM_SAMPLING:
            case PARALLEL_SCAN_BLELLOCH:
                return "Module 6: Randomised & Parallel Algorithms";
            default:
                return "Module 1: TextHack Indic Corpus";
        }
    }
}
