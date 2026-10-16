# EduTrack — Intelligent University Academic Platform & Live DSA Suite
> **Advanced Data Structures & Algorithms (DSA-3 Project — Modules 1–6 Complete Suite)**
> Single Source of Truth: `EduTrack.csv` (10,000 Academic Records)

---

## 🏛️ Project Overview

**EduTrack** is an intelligent university academic platform engineered from the ground up to power high-performance text retrieval, academic document analytics, plagiarism detection, sequence alignment, and combinatorial scheduling.

### ⚠️ Strict Design Constraint: Zero `java.util.*` Collections
All algorithm engines and core data structures are **100% hand-built from scratch**. No collections, maps, trees, or algorithm utilities from `java.util.*` are imported inside the engine, core, or data loading layers.

### 📊 Dataset Integration: `EduTrack.csv`
All mock and synthetic data files have been removed. The entire backend and frontend are powered exclusively by **`EduTrack.csv`**:
* **10,000 Enrolled Students** across 8 Academic Departments (`CE`, `ECE`, `ME`, `EEE`, `AIML`, `CSE`, `AI&DS`, `IT`)
* **12 Curriculum Courses** (`CS100` through `CS111`)
* **10 Faculty Professors** (`F100` through `F109`)
* **Continuous Assessment Scores**: Assignment, Midterm, and Final Exam marks
* **7 Continuous Engagement Activities**: Lab Activity, Assignment Submission, Library Access, Video Lecture, Login, Quiz, Forum
* **6 Learning Resource Formats**: Book, PDF, Video, Question Bank, Lab Manual, Lecture Notes
* **Master Examination Schedules**: Rooms A/B/C/D blocks, schedule days, time slots, and conflict groups

---

## 🏗️ Architecture & Package Structure

```
edutrack/
├── Main.java                                # Interactive CLI & Server Driver
├── core/                                    # Hand-built Data Structures (Zero java.util.*)
│   ├── CustomArrayList.java                 # Dynamic array with custom growth & sorting
│   ├── CustomHashMap.java                   # Hash table with separate chaining & dynamic rehashing
│   ├── CustomLinkedList.java                # Singly-linked list
│   ├── CustomQueue.java                     # Circular array FIFO queue
│   ├── CustomStack.java                     # Dynamic array LIFO stack
│   ├── CustomPriorityQueue.java             # Binary min-heap priority queue
│   ├── CustomTreeMap.java                   # Balanced AVL/BST ordered key-value map
│   └── Pair.java                            # Immutable generic pair
│
├── string/                                  # Module 2: String Algorithms (CO1)
│   ├── KMP.java                             # Knuth-Morris-Pratt O(n + m) pattern search with π table
│   ├── ZFunction.java                       # Z-algorithm O(n) prefix box [L, R] & repeated phrase detection
│   ├── RabinKarp.java                       # Rolling-hash polynomial pattern matching
│   └── AhoCorasick.java                     # Multi-pattern trie with BFS failure links
│
├── suffix/                                  # Module 2: Suffix Structures (CO2)
│   ├── SuffixArray.java                     # Doubling O(n log² n) suffix array & binary search
│   ├── SAIS.java                            # SA-IS O(n) linear-time Induced Sorting suffix array
│   ├── KasaiLCP.java                        # Kasai's O(n) Longest Common Prefix array
│   ├── SuffixTree.java                      # Compact suffix tree with substring recognition
│   └── SuffixAutomaton.java                 # Directed Acyclic Word Graph (DAWG) LCS & counts
│
├── dp/                                      # Module 3: Advanced Dynamic Programming (CO3)
│   ├── Levenshtein.java                     # Edit distance O(nm) & alignment traceback
│   ├── DamerauLevenshtein.java              # Edit distance with adjacent character transpositions
│   ├── NeedlemanWunsch.java                 # Global sequence alignment
│   ├── SmithWaterman.java                   # Local sequence alignment
│   ├── MatrixChainMultiplication.java       # Interval DP O(n³) optimal parenthesisation
│   ├── OptimalBST.java                      # Interval DP O(n³) expected search cost tree
│   ├── BitmaskDP.java                       # Exponential DP O(2ⁿ · n²) TSP course/building route
│   ├── TreeDP.java                          # Prerequisite tree diameter & max independent credit set
│   └── SOSDP.java                           # Sum Over Subsets O(n · 2ⁿ) fast Walsh-Hadamard
│
├── flow/                                    # Module 4: Network Flow & Matching
│   ├── FlowNetwork.java                     # Custom residual graph representation with forward & backward edges
│   ├── FordFulkerson.java                   # Classical augmenting path max-flow framework
│   ├── EdmondsKarp.java                     # BFS shortest-path augmenting max flow O(V E²)
│   ├── Dinic.java                           # Layered network with blocking flows O(V² E)
│   ├── MinCut.java                          # Min-Cut extraction via residual BFS reachability (Max-Flow Min-Cut theorem)
│   ├── BipartiteMatching.java               # Maximum cardinality bipartite matching & König's vertex cover
│   ├── MinCostMaxFlow.java                  # SPFA / Bellman-Ford successive shortest path with minimum cost
│   └── FlowReductions.java                  # Project selection & maximum profit closure reduction
│
├── approx/                                  # Module 5: NP-Completeness & Approximation
│   ├── ReductionZoo.java                    # Canonical reductions (3-SAT to CLIQUE, Vertex Cover, etc.)
│   ├── VertexCoverApproximation.java        # 2-approximation maximal matching for minimum vertex cover
│   ├── KnapsackApproximation.java           # Greedy 2-approximation & FPTAS (1 - ε) dynamic programming
│   ├── ParameterizedVertexCover.java        # Fixed Parameter Tractability (FPT) Buss kernelization O(2^k · k^2 + n)
│   └── NPHardnessAdvisor.java               # Automated complexity classifier and algorithmic strategy advisor
│
├── randomparallel/                          # Module 6: Randomised & Parallel Algorithms
│   ├── MillerRabin.java                     # Probabilistic primality test with witnesses & Carmichael checks
│   ├── RandomizedQuickSort.java             # Expected O(n log n) sorting with custom RNG pivot selection
│   ├── UniversalHashing.java                # Carter-Wegman universal hash family with low collision guarantee
│   ├── PerfectHashing.java                  # Fredman-Komlós-Szemerédi (FKS) two-level O(1) worst-case hashing
│   ├── ReservoirSampling.java               # Alan Hole's Algorithm R for unbiased streaming random sampling
│   └── ParallelPrimitives.java              # Blelloch parallel prefix scan, work-span analysis & Brent's theorem
│
├── corpus/                                  # TextHack English Corpus & Question Bank
│   ├── IndicCorpus.java                     # Curated academic English articles & multithreaded question banks
│   └── TextHackEngine.java                  # Corpus pattern mining, citation flow & fuzzy concept search
│
├── engine/                                  # Central Query & Analytics Engine
│   ├── EduTrackEngine.java                  # Central academic query engine executing all 40+ query types
│   ├── QueryResult.java                     # Structured query outcome with microsecond/nanosecond timings
│   └── QueryType.java                       # Enumeration of all query classes across Modules 1–6
│
├── server/                                  # Embedded Web Server (Zero external dependencies)
│   └── EduTrackWebServer.java               # Built-in Java HTTP server serving website & REST APIs
│
├── app/                                     # Service Layer
│   ├── SearchService.java                   # Course & document search interface
│   ├── AnalyticsService.java                # Academic DP analytics interface
│   ├── CourseService.java                   # Course catalog operations
│   ├── StudentService.java                  # Student records & typo-tolerant fuzzy search
│   └── AdminService.java                    # Admin directory & permission filters
│
├── data/                                    # Dataset Ingestion (No java.util.*)
│   └── DataLoader.java                      # High-speed CSV parser loading EduTrack.csv
│
├── model/                                   # Domain Entities
│   ├── Student.java                         # Comprehensive student profile with 38 EduTrack attributes
│   ├── Course.java                          # Course specifications & syllabus
│   ├── Faculty.java                         # Faculty profile & department
│   ├── Assignment.java                      # Student assignment submissions & academic keywords
│   ├── Document.java                        # Digital learning resources & query text
│   ├── Schedule.java                        # Exam schedules, rooms & conflict groups
│   ├── AcademicActivity.java                # Continuous engagement activity tracking
│   └── Admin.java                           # University administrator credentials & clearance
│
├── test/                                    # Automated Test Verification
│   └── EduTrackTestRunner.java              # 139 unit and integration tests covering Modules 1–6 (All passing)
│
└── web/                                     # State-of-the-Art Web Application
    ├── index.html                           # Modern responsive university academic dashboard with interactive visualizers
    ├── style.css                            # Glassmorphism dark-mode theme, canvas styling & animations
    └── app.js                               # 10,000-records explorer, visualizers & live DSA playground
```

---

## 🚀 How to Run

### Quick Start (Windows)
Double-click `run.bat` or execute in PowerShell / Terminal:
```bat
run.bat
```
This automatically compiles all 69 Java source files via `build.js` and launches the embedded server.

### Manual Launch
```bash
# Compile all Java sources
node build.js

# Launch Web Server & Interactive CLI
java -cp bin edutrack.Main
```
Then open your browser at:
👉 **`http://localhost:8000/web/index.html`** *(or `http://localhost:8080/web/index.html`)*

### Run the Automated Test Suite (139 Tests)
To verify all hand-built data structures, algorithms, and `EduTrack.csv` ingestion across Modules 1–6:
```bash
java -cp bin edutrack.test.EduTrackTestRunner
```

---

## ⚡ Live DSA Query Families Supported

| Module | Algorithm Family | Algorithm Class | University Academic Use-Case |
|---|---|---|---|
| **CO1** | String Search | `KMP.java` | Scans course syllabi and assignment texts in linear $O(n + m)$ time with failure function $\pi$. |
| **CO1** | String Search | `ZFunction.java` | Detects repeated phrases and self-plagiarism using prefix box $[L, R]$. |
| **CO1** | String Search | `RabinKarp.java` | Scans course numbers and department codes using polynomial rolling hash. |
| **CO1** | String Search | `AhoCorasick.java` | Simultaneously scans academic documents for multiple syllabus keywords using trie failure links. |
| **CO2** | Suffix Structures | `SuffixArray.java` | Lexicographically sorted suffix table with binary search for instant phrase retrieval. |
| **CO2** | Suffix Structures | `SAIS.java` | Linear-time $O(n)$ induced sorting suffix array indexer for library textbooks. |
| **CO2** | Suffix Structures | `KasaiLCP.java` | Computes Longest Common Prefix array in linear time for cross-document similarity. |
| **CO2** | Suffix Structures | `SuffixTree.java` | Compact suffix tree for instantaneous substring recognition and occurrence counts. |
| **CO2** | Suffix Structures | `SuffixAutomaton.java`| Directed Acyclic Word Graph (DAWG) for optimal longest common substring and distinct phrases. |
| **CO3** | Advanced DP | `Levenshtein.java` | Typo-tolerant search over student names and courses with color-coded 2D matrix. |
| **CO3** | Advanced DP | `DamerauLevenshtein.java`| Corrects mistyped records with adjacent letter transpositions in single operations. |
| **CO3** | Advanced DP | `NeedlemanWunsch.java` | Global sequence alignment comparing syllabus revisions with gap penalties. |
| **CO3** | Advanced DP | `SmithWaterman.java` | Local sequence alignment identifying maximal substring similarity. |
| **CO3** | Advanced DP | `MatrixChainMultiplication.java` | Interval DP finding optimal matrix join order for university grade report generation. |
| **CO3** | Advanced DP | `OptimalBST.java` | Minimizes average search cost for popular course queries using key frequency distribution. |
| **CO3** | Advanced DP | `BitmaskDP.java` | Solves the Traveling Salesperson tour visiting university laboratory buildings with minimum distance. |
| **CO3** | Advanced DP | `TreeDP.java` | Computes maximum credits achievable under course prerequisite dependency trees. |
| **CO3** | Advanced DP | `SOSDP.java` | Fast Walsh-Hadamard submask aggregation analyzing student multi-course enrollments. |
| **CO4** | Network Flow | `EdmondsKarp.java` & `Dinic.java` | Computes maximum student enrollment throughput & bottleneck capacities across campus pathways. |
| **CO4** | Min-Cut | `MinCut.java` | Identifies critical bottleneck edges disconnecting campus resource networks (Max-Flow Min-Cut duality). |
| **CO4** | Matching | `BipartiteMatching.java` | Assigns graduate teaching assistants & faculty to laboratory courses via maximum bipartite matching. |
| **CO4** | Min-Cost Flow | `MinCostMaxFlow.java` | Optimizes campus bus routing and dorm room allocations minimizing total transit distance/cost. |
| **CO4** | Closure | `FlowReductions.java` | Solves university project selection and grant allocation under prerequisite dependencies. |
| **CO5** | Reductions | `ReductionZoo.java` | Demonstrates NP-Completeness polynomial reductions (3-SAT to CLIQUE, Independent Set, Vertex Cover). |
| **CO5** | Approx Vertex Cover | `VertexCoverApproximation.java`| 2-Approximation algorithm for optimal campus surveillance camera placement. |
| **CO5** | Approx Knapsack | `KnapsackApproximation.java` | Greedy 2-approximation and fully polynomial-time approximation scheme (FPTAS) for budget planning. |
| **CO5** | FPT Kernel | `ParameterizedVertexCover.java` | Buss kernelization parameterized algorithm solving exact minimum security guard placements. |
| **CO5** | NP Advisor | `NPHardnessAdvisor.java` | Intelligent complexity recommender classifying academic optimization problems. |
| **CO6** | Primality | `MillerRabin.java` | High-confidence randomized primality verification for student credential hashing and crypto tokens. |
| **CO6** | Randomized Sort | `RandomizedQuickSort.java` | Expected $O(n \log n)$ randomized quicksort with recursive recursion depth tracking. |
| **CO6** | Universal Hashing | `UniversalHashing.java` | Carter-Wegman universal hashing family with bounded collision probabilities for student IDs. |
| **CO6** | Perfect Hashing | `PerfectHashing.java` | FKS two-level perfect hash table guaranteeing $O(1)$ worst-case retrieval for master course registries. |
| **CO6** | Streaming Sample | `ReservoirSampling.java` | Unbiased single-pass sampling of size $k$ from continuous live student attendance streams. |
| **CO6** | Parallel Scan | `ParallelPrimitives.java` | Blelloch parallel prefix sum (scan), work-span metrics, and Brent's theorem speedup calculations. |
| **Corpus** | English TextHack | `TextHackEngine.java` | Multi-document English corpus search, fuzzy concept matching, and citation network flow analytics. |
