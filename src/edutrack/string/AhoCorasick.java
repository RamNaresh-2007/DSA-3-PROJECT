package edutrack.string;

import edutrack.core.CustomArrayList;
import edutrack.core.CustomQueue;

/**
 * Aho-Corasick multi-pattern matching automaton.
 *
 * <h3>Algorithm overview</h3>
 * <ul>
 *   <li>Builds a trie of all k patterns (total length M) in O(M).</li>
 *   <li>Computes failure (suffix) links via BFS in O(M).</li>
 *   <li>Scans text of length n, following goto and failure links, reporting
 *       all matches in O(n + M + z) where z is the number of matches.</li>
 * </ul>
 *
 * <h3>EduTrack use-case</h3>
 * Searches multiple academic keywords (e.g. "algorithm", "data structure",
 * "complexity", "graph") simultaneously across documents and assignments.
 */
public class AhoCorasick {

    private static final int ALPHABET_SIZE = 128; // ASCII

    /** A single node in the Aho-Corasick trie/automaton. */
    private static class ACNode {
        int[] children;
        int failureLink;       // suffix link
        int dictionaryLink;    // link to the next node that is a terminal
        int patternIndex;      // index of the pattern ending here (-1 if none)
        int depth;             // depth in the trie (= length of the string to this node)

        ACNode() {
            children = new int[ALPHABET_SIZE];
            for (int i = 0; i < ALPHABET_SIZE; i++) children[i] = -1;
            failureLink = 0;
            dictionaryLink = -1;
            patternIndex = -1;
            depth = 0;
        }
    }

    private ACNode[] nodes;
    private int nodeCount;
    private String[] patterns;
    private boolean built;

    /** Creates an Aho-Corasick automaton for the given patterns. */
    public AhoCorasick(String[] patterns) {
        this.patterns = patterns;
        // Upper bound on number of nodes: total characters + 1 (root)
        int totalChars = 0;
        for (String p : patterns) totalChars += p.length();
        nodes = new ACNode[totalChars + 2];
        nodes[0] = new ACNode(); // root
        nodeCount = 1;
        built = false;

        // Insert all patterns into the trie
        for (int i = 0; i < patterns.length; i++) {
            insertPattern(patterns[i], i);
        }
        // Build failure and dictionary links
        buildLinks();
        built = true;
    }

    /** Inserts a pattern into the trie. */
    private void insertPattern(String pattern, int index) {
        int cur = 0;
        for (int i = 0; i < pattern.length(); i++) {
            int c = pattern.charAt(i);
            if (c < 0 || c >= ALPHABET_SIZE) c = '?'; // safety
            if (nodes[cur].children[c] == -1) {
                nodes[nodeCount] = new ACNode();
                nodes[nodeCount].depth = nodes[cur].depth + 1;
                nodes[cur].children[c] = nodeCount++;
            }
            cur = nodes[cur].children[c];
        }
        nodes[cur].patternIndex = index;
    }

    /** Builds failure and dictionary links via BFS from the root. */
    private void buildLinks() {
        CustomQueue<Integer> queue = new CustomQueue<>();

        // All children of root have failure link to root (0)
        for (int c = 0; c < ALPHABET_SIZE; c++) {
            if (nodes[0].children[c] != -1) {
                nodes[nodes[0].children[c]].failureLink = 0;
                queue.enqueue(nodes[0].children[c]);
            } else {
                nodes[0].children[c] = 0; // point missing transitions back to root
            }
        }

        while (!queue.isEmpty()) {
            int u = queue.dequeue();
            // Dictionary link: first terminal reachable via failure chain
            int f = nodes[u].failureLink;
            nodes[u].dictionaryLink = (nodes[f].patternIndex >= 0) ? f : nodes[f].dictionaryLink;

            for (int c = 0; c < ALPHABET_SIZE; c++) {
                int v = nodes[u].children[c];
                if (v != -1) {
                    nodes[v].failureLink = nodes[nodes[u].failureLink].children[c];
                    queue.enqueue(v);
                } else {
                    // Automaton transition for missing edges
                    nodes[u].children[c] = nodes[nodes[u].failureLink].children[c];
                }
            }
        }
    }

    /**
     * Searches the text for all pattern occurrences.
     *
     * @param text the text to scan
     * @return list of matches, each as {patternIndex, startPositionInText}
     */
    public CustomArrayList<int[]> search(String text) {
        CustomArrayList<int[]> results = new CustomArrayList<>();
        if (text == null || text.isEmpty()) return results;

        int cur = 0;
        for (int i = 0; i < text.length(); i++) {
            int c = text.charAt(i);
            if (c < 0 || c >= ALPHABET_SIZE) c = '?';
            cur = nodes[cur].children[c];

            // Report all patterns ending at this position
            int temp = cur;
            while (temp > 0) {
                if (nodes[temp].patternIndex >= 0) {
                    int pIdx = nodes[temp].patternIndex;
                    int startPos = i - patterns[pIdx].length() + 1;
                    results.add(new int[]{pIdx, startPos});
                }
                temp = nodes[temp].dictionaryLink;
                if (temp == -1) break;
            }
        }
        return results;
    }

    /**
     * Returns a human-readable summary of all matches.
     */
    public CustomArrayList<String> searchReadable(String text) {
        CustomArrayList<int[]> matches = search(text);
        CustomArrayList<String> readable = new CustomArrayList<>();
        for (int i = 0; i < matches.size(); i++) {
            int[] m = matches.get(i);
            readable.add("Pattern '" + patterns[m[0]] + "' found at position " + m[1]);
        }
        return readable;
    }
}
