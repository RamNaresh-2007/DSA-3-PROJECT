package edutrack.app;

import edutrack.core.CustomArrayList;
import edutrack.model.Document;
import edutrack.suffix.*;

/**
 * Service layer for document indexing and similarity operations.
 */
public class DocumentService {

    private final CustomArrayList<Document> documents;

    public DocumentService(CustomArrayList<Document> documents) {
        this.documents = documents;
    }

    /** Find document by ID. */
    public Document findById(String documentId) {
        for (int i = 0; i < documents.size(); i++) {
            if (documents.get(i).getDocumentId().equals(documentId)) return documents.get(i);
        }
        return null;
    }

    /** Find documents by course code. */
    public CustomArrayList<Document> findByCourse(String courseCode) {
        CustomArrayList<Document> results = new CustomArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            if (documents.get(i).getCourseCode().equals(courseCode))
                results.add(documents.get(i));
        }
        return results;
    }

    /**
     * Compute similarity between two documents using Suffix Automaton LCS.
     * Returns similarity as ratio of LCS length to average document length.
     */
    public double computeSimilarity(String docId1, String docId2) {
        Document d1 = findById(docId1);
        Document d2 = findById(docId2);
        if (d1 == null || d2 == null) return 0.0;

        SuffixAutomaton sam = new SuffixAutomaton(d1.getContent().toLowerCase());
        String lcs = sam.longestCommonSubstring(d2.getContent().toLowerCase());
        double avgLen = (d1.getContent().length() + d2.getContent().length()) / 2.0;
        return avgLen > 0 ? lcs.length() / avgLen : 0.0;
    }

    /**
     * Count distinct substrings in a document using Suffix Automaton.
     */
    public long countDistinctSubstrings(String docId) {
        Document d = findById(docId);
        if (d == null) return 0;
        SuffixAutomaton sam = new SuffixAutomaton(d.getContent().toLowerCase());
        return sam.countDistinctSubstrings();
    }
}
