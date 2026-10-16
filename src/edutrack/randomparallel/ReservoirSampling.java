package edutrack.randomparallel;

import edutrack.core.CustomArrayList;
import edutrack.core.CustomRandom;

/**
 * Reservoir Sampling for Streaming Big Data (Algorithm R).
 *
 * <h3>Theoretical Proof of Uniform Sampling</h3>
 * <ul>
 *   <li><b>Goal:</b> Select k items uniformly at random from a continuous stream of unknown/infinite size N,
 *       maintaining only O(k) memory.</li>
 *   <li><b>Algorithm R:</b>
 *     <ol>
 *       <li>Fill reservoir with the first k items from the stream.</li>
 *       <li>For the i-th item (i &gt; k), generate a random integer j in [0, i-1].
 *           If j &lt; k, replace reservoir[j] with item i.</li>
 *     </ol>
 *   </li>
 *   <li><b>Induction Proof:</b>
 *     <ul>
 *       <li>Base case: When i = k, every item is in reservoir with probability k/k = 1.</li>
 *       <li>Inductive step: Suppose after i-1 items, every item is in reservoir with prob k / (i-1).</li>
 *       <li>Item i is selected with probability k / i.</li>
 *       <li>Any previous item stays in reservoir if either (a) item i is not chosen (prob 1 - k/i),
 *           or (b) item i is chosen but replaces another slot (prob (k/i) * (1 - 1/k) = (k-1)/i).</li>
 *       <li>Total probability of staying = (1 - k/i) + (k-1)/i = (i-1)/i.</li>
 *       <li>Overall probability = [k / (i-1)] * [(i-1) / i] = <b>k / i</b>.</li>
 *       <li>Hence, every item in the stream has an exactly equal probability <b>k / N</b> of inclusion!</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p>Strictly conforms to zero {@code java.util.*} imports.</p>
 */
public class ReservoirSampling<T> {

    private final int k;
    private final CustomArrayList<T> reservoir;
    private long totalSeen;
    private final CustomRandom rng;

    public ReservoirSampling(int k) {
        this(k, new CustomRandom());
    }

    public ReservoirSampling(int k, CustomRandom rng) {
        if (k <= 0) throw new IllegalArgumentException("k must be > 0");
        this.k = k;
        this.reservoir = new CustomArrayList<>();
        this.totalSeen = 0;
        this.rng = rng;
    }

    /**
     * Processes next element in the streaming sequence.
     */
    public void feed(T item) {
        totalSeen++;
        if (reservoir.size() < k) {
            reservoir.add(item);
        } else {
            // Pick random j in [0, totalSeen - 1]
            long j = (rng.nextLong() >>> 1) % totalSeen;
            if (j < k) {
                reservoir.set((int) j, item);
            }
        }
    }

    public CustomArrayList<T> getReservoir() {
        return reservoir;
    }

    public long getTotalSeen() {
        return totalSeen;
    }

    public int getK() {
        return k;
    }

    public double getInclusionProbability() {
        return totalSeen > 0 ? Math.min(1.0, (double) k / totalSeen) : 1.0;
    }

    public static class SimulationResult {
        public final int sampleK;
        public final int streamSize;
        public final double expectedProb;
        public final int[] hitFrequencies;
        public final double chiSquareDeviation;

        public SimulationResult(int sampleK, int streamSize, double expectedProb,
                                int[] hitFrequencies, double chiSquareDeviation) {
            this.sampleK = sampleK;
            this.streamSize = streamSize;
            this.expectedProb = expectedProb;
            this.hitFrequencies = hitFrequencies;
            this.chiSquareDeviation = chiSquareDeviation;
        }
    }

    /** Runs an empirical uniform verification test across multiple stream runs. */
    public static SimulationResult verifyUniformity(int streamSize, int k, int trials) {
        int[] counts = new int[streamSize];
        CustomRandom rng = new CustomRandom(777);

        for (int t = 0; t < trials; t++) {
            ReservoirSampling<Integer> sampler = new ReservoirSampling<>(k, rng);
            for (int i = 0; i < streamSize; i++) {
                sampler.feed(i);
            }
            CustomArrayList<Integer> res = sampler.getReservoir();
            for (int idx = 0; idx < res.size(); idx++) {
                counts[res.get(idx)]++;
            }
        }

        double expected = (double) trials * k / streamSize;
        double chiSq = 0;
        for (int c : counts) {
            double diff = c - expected;
            chiSq += (diff * diff) / expected;
        }

        return new SimulationResult(k, streamSize, (double) k / streamSize, counts, chiSq);
    }
}
