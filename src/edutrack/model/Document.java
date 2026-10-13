package edutrack.model;

/**
 * Represents an academic document or learning resource in the EduTrack platform,
 * mapped to EduTrack.csv resource records.
 * Full content and query text are stored for suffix-structure indexing and similarity analysis.
 */
public class Document {
    private String documentId;
    private String title;
    private String author;
    private String courseCode;
    private String content;    // full text for indexing
    private String type;       // Book, PDF, Video, Question Bank, Lab Manual, Lecture Notes
    private String queryText;  // search query context

    public Document(String documentId, String title, String author,
                    String courseCode, String content, String type, String queryText) {
        this.documentId = documentId;
        this.title = title;
        this.author = author;
        this.courseCode = courseCode;
        this.content = content;
        this.type = type;
        this.queryText = queryText;
    }

    public Document(String documentId, String title, String author,
                    String courseCode, String content, String type) {
        this(documentId, title, author, courseCode, content, type, content);
    }

    public String getDocumentId() { return documentId; }
    public String getTitle()      { return title; }
    public String getAuthor()     { return author; }
    public String getCourseCode() { return courseCode; }
    public String getContent()    { return content; }
    public String getType()       { return type; }
    public String getQueryText()  { return queryText; }

    public void setTitle(String title)         { this.title = title; }
    public void setAuthor(String author)       { this.author = author; }
    public void setContent(String content)     { this.content = content; }
    public void setType(String type)           { this.type = type; }
    public void setQueryText(String queryText) { this.queryText = queryText; }

    @Override
    public String toString() {
        return "Document{id='" + documentId + "', title='" + title +
               "', course='" + courseCode + "', type='" + type + "'}";
    }
}
