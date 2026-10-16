package edutrack.flow;

import edutrack.core.CustomArrayList;

/**
 * Canonical Practical Reductions to Network Flow & Min-Cut.
 *
 * <h3>1. Project Selection Problem (Min-Cut)</h3>
 * <p>Given academic research projects or elective modules where each has a profit p_i
 * (can be positive or negative), and prerequisite dependencies (i requires j),
 * we construct an s-t graph:
 * <ul>
 *   <li>Source s -> project i with capacity p_i (if p_i > 0)</li>
 *   <li>Project i -> Sink t with capacity -p_i (if p_i &lt; 0)</li>
 *   <li>Project i -> Project j with capacity INF (for dependency "i requires j")</li>
 * </ul>
 * The maximum profit subset = (Sum of all positive profits) - (Min-Cut Capacity).
 * </p>
 *
 * <h3>2. Exam Scheduling with Deadlines and Room Capacities</h3>
 * <p>Reduces exam assignment to max-flow: Course exams -> Feasible Time Slots -> Rooms -> Sink.</p>
 *
 * <h3>3. Document Foreground/Background Partitioning (Segmentation as Min-Cut)</h3>
 * <p>Pixels or text paragraphs are partitioned into foreground/background by balancing
 * individual likelihood penalties with boundary smoothness penalties.</p>
 */
public class FlowReductions {

    public static class ProjectSelectionResult {
        public final int maxNetProfit;
        public final CustomArrayList<Integer> selectedProjects;
        public final int minCutCost;
        public final String explanation;

        public ProjectSelectionResult(int maxNetProfit, CustomArrayList<Integer> selectedProjects,
                                     int minCutCost, String explanation) {
            this.maxNetProfit = maxNetProfit;
            this.selectedProjects = selectedProjects;
            this.minCutCost = minCutCost;
            this.explanation = explanation;
        }
    }

    /**
     * Solves Project Selection using Min-Cut reduction.
     *
     * @param profits      profit of each project (positive = gain, negative = cost)
     * @param dependencies pairs [i, j] indicating project i requires prerequisite project j
     */
    public static ProjectSelectionResult solveProjectSelection(int[] profits, CustomArrayList<int[]> dependencies) {
        int n = profits.length;
        int source = 0;
        int sink = n + 1;
        FlowNetwork network = new FlowNetwork(sink + 1);

        final int INF = 1_000_000;
        int sumPositiveProfits = 0;

        for (int i = 0; i < n; i++) {
            int p = profits[i];
            int node = i + 1;
            if (p > 0) {
                sumPositiveProfits += p;
                network.addEdge(source, node, p);
            } else if (p < 0) {
                network.addEdge(node, sink, -p);
            }
        }

        // Dependency edges with infinite capacity (cannot be cut)
        for (int k = 0; k < dependencies.size(); k++) {
            int[] dep = dependencies.get(k);
            int i = dep[0];
            int j = dep[1];
            network.addEdge(i + 1, j + 1, INF);
        }

        MinCut.CutResult cut = MinCut.computeMinCut(network, source, sink);
        int maxProfit = sumPositiveProfits - cut.minCutCapacity;

        CustomArrayList<Integer> selected = new CustomArrayList<>();
        for (int i = 0; i < cut.setS.size(); i++) {
            int node = cut.setS.get(i);
            if (node >= 1 && node <= n) {
                selected.add(node - 1);
            }
        }

        String expl = "Project selection solved via Min-Cut. Total positive profit: "
                + sumPositiveProfits + " - Min-Cut: " + cut.minCutCapacity + " = Net Profit: " + maxProfit;

        return new ProjectSelectionResult(maxProfit, selected, cut.minCutCapacity, expl);
    }

    public static class SchedulingResult {
        public final boolean allExamsScheduled;
        public final int scheduledCount;
        public final int totalExams;
        public final CustomArrayList<String> scheduleAssignments;

        public SchedulingResult(boolean allExamsScheduled, int scheduledCount, int totalExams, CustomArrayList<String> scheduleAssignments) {
            this.allExamsScheduled = allExamsScheduled;
            this.scheduledCount = scheduledCount;
            this.totalExams = totalExams;
            this.scheduleAssignments = scheduleAssignments;
        }
    }

    /**
     * Exam scheduling with deadlines and room capacities as flow.
     *
     * @param examNames       array of exam course names
     * @param roomCapacities  capacity per slot for each exam room
     * @param feasibleSlots   for each exam, array of allowable time slot IDs
     */
    public static SchedulingResult scheduleExams(String[] examNames, int[] roomCapacities, int[][] feasibleSlots, int numSlots) {
        int numExams = examNames.length;
        int numRooms = roomCapacities.length;

        // Node layout:
        // 0: Source
        // 1 .. numExams: Exams
        // numExams + 1 .. numExams + (numSlots * numRooms): (Slot, Room) pairs
        // Sink: numExams + (numSlots * numRooms) + 1
        int slotRoomCount = numSlots * numRooms;
        int sink = numExams + slotRoomCount + 1;
        FlowNetwork network = new FlowNetwork(sink + 1);

        int source = 0;

        // Source -> Exam (capacity 1)
        for (int e = 0; e < numExams; e++) {
            network.addEdge(source, e + 1, 1);
        }

        // Exam -> (Slot, Room) (capacity 1)
        for (int e = 0; e < numExams; e++) {
            int[] slots = feasibleSlots[e];
            for (int s : slots) {
                for (int r = 0; r < numRooms; r++) {
                    int slotRoomNode = numExams + 1 + (s * numRooms + r);
                    network.addEdge(e + 1, slotRoomNode, 1);
                }
            }
        }

        // (Slot, Room) -> Sink (capacity roomCapacities[r])
        for (int s = 0; s < numSlots; s++) {
            for (int r = 0; r < numRooms; r++) {
                int slotRoomNode = numExams + 1 + (s * numRooms + r);
                network.addEdge(slotRoomNode, sink, roomCapacities[r]);
            }
        }

        Dinic.DinicResult dinic = Dinic.solve(network, source, sink);
        int maxScheduled = dinic.maxFlow;

        CustomArrayList<String> assignments = new CustomArrayList<>();
        for (int e = 0; e < numExams; e++) {
            CustomArrayList<FlowNetwork.Edge> adj = network.getAdj(e + 1);
            boolean matched = false;
            for (int i = 0; i < adj.size(); i++) {
                FlowNetwork.Edge edge = adj.get(i);
                if (edge.capacity == 1 && edge.flow == 1 && edge.v > numExams && edge.v <= numExams + slotRoomCount) {
                    int id = edge.v - (numExams + 1);
                    int s = id / numRooms;
                    int r = id % numRooms;
                    assignments.add(examNames[e] + " -> Assigned to Time Slot " + (s + 1) + " in Room " + (r + 1));
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                assignments.add(examNames[e] + " -> UNSCHEDULED (Capacity/Deadline Conflict)");
            }
        }

        return new SchedulingResult(maxScheduled == numExams, maxScheduled, numExams, assignments);
    }
}
