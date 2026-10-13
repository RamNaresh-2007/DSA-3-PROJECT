package edutrack.core;

/**
 * Hand-built min-heap priority queue — replaces java.util.PriorityQueue.
 * O(log n) insert and extractMin, O(1) peekMin.
 *
 * @param <T> element type (must be comparable via the supplied comparator)
 */
public class CustomPriorityQueue<T> {

    private Object[] heap;
    private int size;
    private final CustomArrayList.CustomComparator<T> comparator;
    private static final int DEFAULT_CAPACITY = 16;

    public CustomPriorityQueue(CustomArrayList.CustomComparator<T> comparator) {
        this.comparator = comparator;
        heap = new Object[DEFAULT_CAPACITY];
        size = 0;
    }

    /** Inserts an element. O(log n). */
    public void insert(T element) {
        if (size == heap.length) grow();
        heap[size] = element;
        siftUp(size);
        size++;
    }

    /** Returns the minimum element without removing. O(1). */
    @SuppressWarnings("unchecked")
    public T peekMin() {
        if (size == 0) throw new RuntimeException("Priority queue is empty");
        return (T) heap[0];
    }

    /** Removes and returns the minimum element. O(log n). */
    @SuppressWarnings("unchecked")
    public T extractMin() {
        if (size == 0) throw new RuntimeException("Priority queue is empty");
        T min = (T) heap[0];
        heap[0] = heap[--size];
        heap[size] = null;
        if (size > 0) siftDown(0);
        return min;
    }

    public int size()        { return size; }
    public boolean isEmpty() { return size == 0; }

    @SuppressWarnings("unchecked")
    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (comparator.compare((T) heap[i], (T) heap[parent]) < 0) {
                swap(i, parent);
                i = parent;
            } else break;
        }
    }

    @SuppressWarnings("unchecked")
    private void siftDown(int i) {
        while (true) {
            int left = 2 * i + 1, right = 2 * i + 2, smallest = i;
            if (left < size && comparator.compare((T) heap[left], (T) heap[smallest]) < 0)
                smallest = left;
            if (right < size && comparator.compare((T) heap[right], (T) heap[smallest]) < 0)
                smallest = right;
            if (smallest != i) { swap(i, smallest); i = smallest; }
            else break;
        }
    }

    private void swap(int i, int j) {
        Object tmp = heap[i]; heap[i] = heap[j]; heap[j] = tmp;
    }

    private void grow() {
        Object[] newHeap = new Object[heap.length * 2];
        for (int i = 0; i < size; i++) newHeap[i] = heap[i];
        heap = newHeap;
    }
}
