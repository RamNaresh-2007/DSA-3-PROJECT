package edutrack.core;

/**
 * Hand-built FIFO queue backed by a circular array — replaces java.util.Queue.
 * All operations are amortised O(1).
 *
 * @param <T> element type
 */
public class CustomQueue<T> {

    private Object[] data;
    private int head;   // index of the front element
    private int tail;   // index of the next insertion point
    private int size;
    private static final int DEFAULT_CAPACITY = 16;

    public CustomQueue() {
        data = new Object[DEFAULT_CAPACITY];
        head = 0;
        tail = 0;
        size = 0;
    }

    /** Enqueues an element at the back. Amortised O(1). */
    public void enqueue(T element) {
        if (size == data.length) grow();
        data[tail] = element;
        tail = (tail + 1) % data.length;
        size++;
    }

    /** Dequeues and returns the front element. O(1). */
    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (size == 0) throw new RuntimeException("Queue is empty");
        T element = (T) data[head];
        data[head] = null;
        head = (head + 1) % data.length;
        size--;
        return element;
    }

    /** Peeks at the front element without removing. O(1). */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (size == 0) throw new RuntimeException("Queue is empty");
        return (T) data[head];
    }

    public int size()        { return size; }
    public boolean isEmpty() { return size == 0; }

    private void grow() {
        Object[] newData = new Object[data.length * 2];
        for (int i = 0; i < size; i++) {
            newData[i] = data[(head + i) % data.length];
        }
        data = newData;
        head = 0;
        tail = size;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Queue[");
        for (int i = 0; i < size; i++) {
            if (i > 0) sb.append(", ");
            sb.append(data[(head + i) % data.length]);
        }
        sb.append("]");
        return sb.toString();
    }
}
