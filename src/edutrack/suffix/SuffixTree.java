package edutrack.suffix;

import edutrack.core.CustomArrayList;

/**
 * Suffix Tree built using Ukkonen's algorithm — O(n) online construction.
 *
 * <h3>Algorithm overview</h3>
 * <ul>
 *   <li>Ukkonen's algorithm extends the suffix tree one character at a time
 *       using three key rules: Rule 1 (leaf extension), Rule 2 (new branch),
 *       Rule 3 (already present — do nothing).</li>
 *   <li>Suffix links allow O(1) jumps between internal nodes.</li>
 *   <li>Active point (active_node, active_edge, active_length) tracks the
 *       current insertion point.</li>
 *   <li>Total O(n) construction time and space.</li>
 * </ul>
 *
 * <h3>EduTrack use-case</h3>
 * Substring-based document queries: "find all documents containing substring X"
 * in O(|X|) time after O(n) build.
 */
public class SuffixTree {

    private static final int ALPHABET_SIZE = 128;

    /** Represents an edge/node in the suffix tree. */
    static class Node {
        int[] children;
        int suffixLink;
        int start;      // start index in text
        int end;        // end index in text (-1 means leaf — use global 'leafEnd')
        int suffixIndex; // for leaves, the starting position of this suffix

        Node(int start, int end) {
            children = new int[ALPHABET_SIZE];
            for (int i = 0; i < ALPHABET_SIZE; i++) children[i] = -1;
            suffixLink = -1;
            this.start = start;
            this.end = end;
            suffixIndex = -1;
        }

        int edgeLength(int leafEnd) {
            return (end == -1 ? leafEnd : end) - start + 1;
        }
    }

    private Node[] nodes;
    private int nodeCount;
    private final String text;
    private int leafEnd;

    // Active point
    private int activeNode;
    private int activeEdge;
    private int activeLength;
    private int remaining;

    private int lastNewInternalNode;

    /**
     * Builds a suffix tree for the given text using Ukkonen's algorithm.
     *
     * @param text the input string (a sentinel '$' is appended internally)
     */
    public SuffixTree(String text) {
        this.text = text + "$";
        int n = this.text.length();
        nodes = new Node[2 * n + 10];
        nodeCount = 0;
        leafEnd = -1;
        activeNode = 0;
        activeEdge = -1;
        activeLength = 0;
        remaining = 0;
        lastNewInternalNode = -1;

        // Create root
        nodes[nodeCount++] = new Node(-1, -1);
        nodes[0].suffixLink = 0;

        for (int i = 0; i < n; i++) {
            extendTree(i);
        }
        setSuffixIndices(0, 0);
    }

    /** Phase i: extend tree by character text[pos]. */
    private void extendTree(int pos) {
        leafEnd = pos;
        remaining++;
        lastNewInternalNode = -1;

        while (remaining > 0) {
            if (activeLength == 0) {
                activeEdge = pos;
            }

            int c = text.charAt(activeEdge);
            if (nodes[activeNode].children[c] == -1) {
                // Rule 2: create new leaf
                int leaf = newNode(pos, -1);
                nodes[activeNode].children[c] = leaf;
                addSuffixLink(activeNode);
            } else {
                int next = nodes[activeNode].children[c];
                int edgeLen = nodes[next].edgeLength(leafEnd);

                // Walk down if activeLength >= edge length
                if (activeLength >= edgeLen) {
                    activeEdge += edgeLen;
                    activeLength -= edgeLen;
                    activeNode = next;
                    continue; // restart this phase step
                }

                // Rule 3: character already in tree
                if (text.charAt(nodes[next].start + activeLength) == text.charAt(pos)) {
                    activeLength++;
                    addSuffixLink(activeNode);
                    break; // nothing to do
                }

                // Rule 2: split edge and create new branch
                int split = newNode(nodes[next].start, nodes[next].start + activeLength - 1);
                nodes[activeNode].children[c] = split;
                int leaf = newNode(pos, -1);
                nodes[split].children[text.charAt(pos)] = leaf;
                nodes[next].start += activeLength;
                nodes[split].children[text.charAt(nodes[next].start)] = next;
                addSuffixLink(split);
            }

            remaining--;
            if (activeNode == 0 && activeLength > 0) {
                activeLength--;
                activeEdge = pos - remaining + 1;
            } else if (nodes[activeNode].suffixLink != -1 && nodes[activeNode].suffixLink != 0) {
                activeNode = nodes[activeNode].suffixLink;
            } else {
                activeNode = 0;
            }
        }
    }

    private int newNode(int start, int end) {
        nodes[nodeCount] = new Node(start, end);
        return nodeCount++;
    }

    private void addSuffixLink(int node) {
        if (lastNewInternalNode != -1 && lastNewInternalNode != node) {
            nodes[lastNewInternalNode].suffixLink = node;
        }
        lastNewInternalNode = node;
    }

    /** DFS to set suffix indices on leaf nodes. */
    private void setSuffixIndices(int node, int labelHeight) {
        boolean isLeaf = true;
        for (int i = 0; i < ALPHABET_SIZE; i++) {
            int child = nodes[node].children[i];
            if (child != -1) {
                isLeaf = false;
                setSuffixIndices(child, labelHeight + nodes[child].edgeLength(leafEnd));
            }
        }
        if (isLeaf) {
            nodes[node].suffixIndex = text.length() - labelHeight;
        }
    }

    /**
     * Searches for a pattern in the suffix tree in O(m) time.
     *
     * @param pattern the substring to search for
     * @return list of starting positions in the original text
     */
    public CustomArrayList<Integer> search(String pattern) {
        CustomArrayList<Integer> result = new CustomArrayList<>();
        if (pattern == null || pattern.isEmpty()) return result;

        int node = 0;
        int i = 0;
        while (i < pattern.length()) {
            int c = pattern.charAt(i);
            if (nodes[node].children[c] == -1) return result; // not found

            int child = nodes[node].children[c];
            int edgeLen = nodes[child].edgeLength(leafEnd);
            int j = 0;
            while (j < edgeLen && i < pattern.length()) {
                if (text.charAt(nodes[child].start + j) != pattern.charAt(i)) {
                    return result; // mismatch on edge
                }
                i++;
                j++;
            }
            node = child;
        }

        // Collect all leaf suffix indices under this subtree
        collectLeaves(node, result);
        return result;
    }

    /** Collects all suffix indices from leaves under the given node. */
    private void collectLeaves(int node, CustomArrayList<Integer> result) {
        if (nodes[node].suffixIndex >= 0) {
            // Filter out the sentinel suffix
            if (nodes[node].suffixIndex < text.length() - 1) {
                result.add(nodes[node].suffixIndex);
            }
            return;
        }
        for (int i = 0; i < ALPHABET_SIZE; i++) {
            if (nodes[node].children[i] != -1) {
                collectLeaves(nodes[node].children[i], result);
            }
        }
    }

    /**
     * Checks whether the pattern exists as a substring.
     */
    public boolean contains(String pattern) {
        return !search(pattern).isEmpty();
    }
}
