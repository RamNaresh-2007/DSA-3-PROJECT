package edutrack.corpus;

import edutrack.core.CustomArrayList;
import edutrack.model.Document;

/**
 * English Wikipedia Knowledge Corpus for TextHack (Module 1).
 *
 * <p>Contains real Wikipedia excerpt articles in English focusing on
 * computer science pioneers, algorithms, data structures, complexity theory, and mathematics.</p>
 *
 * <p>Strictly conforms to zero {@code java.util.*} imports.</p>
 */
public class IndicCorpus {

    public static class IndicArticle {
        public final String id;
        public final String title;
        public final String language;
        public final String script;
        public final String content;
        public final String englishSummary;

        public IndicArticle(String id, String title, String language, String script, String content, String englishSummary) {
            this.id = id;
            this.title = title;
            this.language = language;
            this.script = script;
            this.content = content;
            this.englishSummary = englishSummary;
        }

        public Document toDocument() {
            return new Document(id, title + " (" + language + ")", "English Wikipedia", "WIKI-" + language.toUpperCase(),
                    content, "Wikipedia Article", englishSummary);
        }
    }

    public static CustomArrayList<IndicArticle> getArticles() {
        CustomArrayList<IndicArticle> list = new CustomArrayList<>();

        // 1. Alan Turing & Computability
        list.add(new IndicArticle("ENG_ALGO_001",
                "Alan Turing — Computability, Turing Machines & Decidability",
                "English",
                "Latin",
                "Alan Mathison Turing (1912-1954) was an English mathematician, computer scientist, logician, cryptanalyst, philosopher, and theoretical biologist. Turing was highly influential in the development of theoretical computer science, providing a formalisation of the concepts of algorithm and computation with the Turing machine, which can be considered a model of a general-purpose computer. He proved the undecidability of the Halting Problem and laid foundations for modern algorithmic complexity.",
                "Alan Turing formalized the concepts of algorithm, universal computation, Turing machines, and the halting problem undecidability."));

        // 2. Ada Lovelace & The First Algorithm
        list.add(new IndicArticle("ENG_ALGO_002",
                "Ada Lovelace — The Analytical Engine & First Computer Algorithm",
                "English",
                "Latin",
                "Augusta Ada King, Countess of Lovelace (1815-1852) was an English mathematician and writer, chiefly known for her work on Charles Babbage's mechanical general-purpose computer, the Analytical Engine. She was the first to recognize that the machine had applications beyond pure calculation, and created the first algorithm intended to be carried out by such a machine to compute Bernoulli numbers. She is celebrated as the world's first computer programmer.",
                "Ada Lovelace authored the first published algorithm for Charles Babbage's Analytical Engine to compute Bernoulli numbers."));

        // 3. Edsger Dijkstra & Shortest Paths
        list.add(new IndicArticle("ENG_ALGO_003",
                "Edsger Dijkstra — Shortest Paths & Structured Programming",
                "English",
                "Latin",
                "Edsger Wybe Dijkstra (1930-2002) was a Dutch computer scientist and programmer. He received the 1972 Turing Award for fundamental contributions to developing programming languages, shortest path algorithms on graphs, concurrent programming, and software engineering. His shortest-path algorithm, published in 1959, computes the single-source shortest path in weighted graphs in O(V^2) or O((V + E) log V) with priority queues, powering modern network routing.",
                "Edsger Dijkstra invented the foundational shortest path algorithm and semaphore primitives for concurrent synchronization."));

        // 4. Srinivasa Ramanujan & Number Theory
        list.add(new IndicArticle("ENG_ALGO_004",
                "Srinivasa Ramanujan — Partition Functions & Number Theory",
                "English",
                "Latin",
                "Srinivasa Ramanujan (1887-1920) was an Indian mathematician whose work revolutionized mathematical analysis, number theory, infinite series, and continued fractions. Working with G.H. Hardy at Cambridge, he formulated asymptotic partition formulas p(n) ~ exp(pi * sqrt(2n/3)) / (4n * sqrt(3)), mock theta functions, and the famous taxicab number 1729, the smallest number expressible as the sum of two cubes in two different ways (1729 = 1^3 + 12^3 = 9^3 + 10^3).",
                "Srinivasa Ramanujan made monumental contributions to number theory, infinite series, modular forms, and the Hardy-Ramanujan number 1729."));

        // 5. Claude Shannon & Information Theory
        list.add(new IndicArticle("ENG_ALGO_005",
                "Claude Shannon — Information Theory & Digital Circuit Design",
                "English",
                "Latin",
                "Claude Elwood Shannon (1916-2001) was an American mathematician, electrical engineer, and cryptographer known as the father of information theory. In his 1937 master's thesis, he showed that electrical applications of Boolean algebra could construct any numerical logical relationship. In 1948, Shannon published A Mathematical Theory of Communication, defining the bit as a fundamental unit of information, channel capacity, and Shannon entropy.",
                "Claude Shannon founded information theory, channel capacity, and proved Boolean algebra models all digital logic circuits."));

        // 6. John von Neumann & Architecture
        list.add(new IndicArticle("ENG_ALGO_006",
                "John von Neumann — Computer Architecture & Automata Theory",
                "English",
                "Latin",
                "John von Neumann (1903-1957) was a Hungarian-American mathematician, physicist, computer scientist, and polymath. He designed the stored-program computer architecture (von Neumann architecture) separating central processing from memory, invented the merge sort algorithm in 1945, established mathematical foundations for quantum mechanics, developed game theory with the minimax theorem, and proved the existence of self-reproducing cellular automata.",
                "John von Neumann pioneered the stored-program computer architecture, invented merge sort, and founded game theory."));

        // 7. Donald Knuth & Algorithmic Analysis
        list.add(new IndicArticle("ENG_ALGO_007",
                "Donald Knuth — The Art of Computer Programming & String Algorithms",
                "English",
                "Latin",
                "Donald Ervin Knuth (born 1938) is an American computer scientist, mathematician, and professor emeritus at Stanford University. He is the author of the multi-volume work The Art of Computer Programming, one of the most respected references in the field. He pioneered the rigorous mathematical analysis of algorithm running time, co-developed the Knuth-Morris-Pratt (KMP) linear string-matching algorithm, and created the TeX computer typesetting system.",
                "Donald Knuth established the rigorous mathematical analysis of algorithms and co-invented the linear-time KMP string algorithm."));

        // 8. Stephen Cook & Complexity Theory
        list.add(new IndicArticle("ENG_ALGO_008",
                "Stephen Cook — Complexity Theory, P vs NP & Cook-Levin Theorem",
                "English",
                "Latin",
                "Stephen Arthur Cook (born 1939) is an American-Canadian computer scientist and mathematician who formalized the notions of polynomial-time reduction and NP-completeness. In his landmark 1971 paper The Complexity of Theorem Proving Procedures, Cook proved that the Boolean Satisfiability problem (SAT) is NP-complete, introducing the famous P versus NP open problem. This result, now known as the Cook-Levin theorem, forms the bedrock of computational complexity theory.",
                "Stephen Cook proved SAT is NP-complete (Cook-Levin theorem) and established the foundational P versus NP complexity framework."));

        return list;
    }
}
