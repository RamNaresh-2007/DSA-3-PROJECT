package edutrack.suffix;

import edutrack.core.CustomArrayList;

/**
 * Suffix Automaton (SAM) — O(n) construction.
 *
 * <h3>Algorithm overview</h3>
 * <ul>
 *   <li>A suffix automaton for a string S of length n is the smallest DAWG
 *       (Directed Acyclic Word Graph) that accepts exactly the set of substrings of S.</li>
 *   <li>It has at most 2n - 1 states and 3n - 4 transitions.</li>
 *   <li>Constructed online by extending one character at a time, maintaining
 *       suffix links between endpos-equivalence classes.</li>
 *   <li>Enables O(|P|) substring search, counting distinct substrings,
 *       and shortest non-occurring string queries.</li>
 * </ul>
 *
 * <h3>EduTrack use-case</h3>
 * Advanced document queries that suffix arrays cannot answer efficiently:
 * counting distinct substrings, finding shortest unique substrings.
 */
public class SuffixAutomaton {

    /** A state in the suffix automaton. */
    static class State {
        int len;        // longest suffix in this endpos class
        int link;       // suffix link
        int[] next;     // transitions
        long cnt;       // number of occurrences (filled during topo sort)
        boolean isClone;

        State() {
            len = 0;
            link = -1;
            next = new int[128]; // ASCII
            for (int i = 0; i < 128; i++) next[i] = -1;
            cnt = 0;
            isClone = false;
        }
    }

    private State[] states;
    private int stateCount;
    private int last;
    private final String text;

    /**
     * Builds the suffix automaton for the given text.
     *
     * @param text the input string
     */
    public SuffixAutomaton(String text) {
        this.text = text;
        int n = text.length();
        states = new State[2 * n + 2];
        stateCount = 0;

        // Initial state (empty string)
        states[stateCount] = new State();
        states[stateCount].len = 0;
        states[stateCount].link = -1;
        last = stateCount++;

        for (int i = 0; i < n; i++) {
            extend(text.charAt(i));
        }

        // Count occurrences via topological sort
        computeOccurrences();
    }

    /** Extends the automaton by one character. */
    private void extend(char c) {
        int cur = stateCount;
        states[stateCount] = new State();
        states[cur].len = states[last].len + 1;
        states[cur].cnt = 1;
        stateCount++;

        int p = last;
        while (p != -1 && states[p].next[c] == -1) {
            states[p].next[c] = cur;
            p = states[p].link;
        }

        if (p == -1) {
            states[cur].link = 0; // link to initial state
        } else {
            int q = states[p].next[c];
            if (states[p].len + 1 == states[q].len) {
                states[cur].link = q;
            } else {
                // Clone state q
                int clone = stateCount;
                states[stateCount] = new State();
                states[clone].len = states[p].len + 1;
                states[clone].link = states[q].link;
                states[clone].isClone = true;
                states[clone].cnt = 0;
                // Copy transitions
                for (int i = 0; i < 128; i++) states[clone].next[i] = states[q].next[i];
                stateCount++;

                while (p != -1 && states[p].next[c] == q) {
                    states[p].next[c] = clone;
                    p = states[p].link;
                }
                states[q].link = clone;
                states[cur].link = clone;
            }
        }
        last = cur;
    }

    /** Topological sort by length (descending) and propagate occurrence counts. */
    private void computeOccurrences() {
        // Count sort by length
        int maxLen = 0;
        for (int i = 0; i < stateCount; i++) {
            if (states[i].len > maxLen) maxLen = states[i].len;
        }
        int[] countArr = new int[maxLen + 1];
        for (int i = 0; i < stateCount; i++) countArr[states[i].len]++;
        for (int i = 1; i <= maxLen; i++) countArr[i] += countArr[i - 1];

        int[] order = new int[stateCount];
        for (int i = stateCount - 1; i >= 0; i--) {
            order[--countArr[states[i].len]] = i;
        }

        // Propagate counts in reverse topological order
        for (int i = stateCount - 1; i >= 1; i--) {
            int v = order[i];
            if (states[v].link != -1) {
                states[states[v].link].cnt += states[v].cnt;
            }
        }
    }

    /**
     * Checks whether the pattern exists as a substring. O(|pattern|).
     *
     * @param pattern the substring to look for
     * @return true if found
     */
    public boolean contains(String pattern) {
        int cur = 0;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (states[cur].next[c] == -1) return false;
            cur = states[cur].next[c];
        }
        return true;
    }

    /**
     * Counts the number of occurrences of a pattern. O(|pattern|).
     */
    public long countOccurrences(String pattern) {
        int cur = 0;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (states[cur].next[c] == -1) return 0;
            cur = states[cur].next[c];
        }
        return states[cur].cnt;
    }

    /**
     * Counts the total number of distinct substrings.
     * Each state v contributes (states[v].len - states[states[v].link].len) distinct substrings.
     */
    public long countDistinctSubstrings() {
        long total = 0;
        for (int i = 1; i < stateCount; i++) {
            int linkLen = states[i].link >= 0 ? states[states[i].link].len : 0;
            total += states[i].len - linkLen;
        }
        return total;
    }

    /**
     * Finds the longest common substring between this text and another text. O(|other|).
     */
    public String longestCommonSubstring(String other) {
        int cur = 0, curLen = 0;
        int bestLen = 0, bestPos = 0;

        for (int i = 0; i < other.length(); i++) {
            char c = other.charAt(i);
            while (cur != 0 && states[cur].next[c] == -1) {
                cur = states[cur].link;
                curLen = states[cur].len;
            }
            if (states[cur].next[c] != -1) {
                cur = states[cur].next[c];
                curLen++;
            }
            if (curLen > bestLen) {
                bestLen = curLen;
                bestPos = i;
            }
        }

        if (bestLen == 0) return "";
        return other.substring(bestPos - bestLen + 1, bestPos + 1);
    }
}
