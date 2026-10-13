package edutrack.core;

/**
 * Hand-built LIFO stack backed by a dynamic array — replaces java.util.Stack.
 * All operations are amortised O(1).
 *
 * @param <T> element type
 */
public class CustomStack<T> {

    private Object[] data;
    private int size;
    private static final int DEFAULT_CAPACITY = 16;

    public CustomStack() {
        data = new Object[DEFAULT_CAPACITY];
        size = 0;
    }

    /** Pushes an element onto the top. Amortised O(1). */
    public void push(T element) {
        if (size == data.length) grow();
        data[size++] = element;
    }

    /** Pops and returns the top element. O(1). */
    @SuppressWarnings("unchecked")
    public T pop() {
        if (size == 0) throw new RuntimeException("Stack is empty");
        T element = (T) data[--size];
        data[size] = null;
        return element;
    }

    /** Peeks at the top element without removing. O(1). */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (size == 0) throw new RuntimeException("Stack is empty");
        return (T) data[size - 1];
    }

    public int size()        { return size; }
    public boolean isEmpty() { return size == 0; }

    private void grow() {
        Object[] newData = new Object[data.length * 2];
        for (int i = 0; i < size; i++) newData[i] = data[i];
        data = newData;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Stack[top -> ");
        for (int i = size - 1; i >= 0; i--) {
            if (i < size - 1) sb.append(", ");
            sb.append(data[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
