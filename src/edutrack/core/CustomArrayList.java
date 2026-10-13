package edutrack.core;

/**
 * Hand-built dynamic array — replaces java.util.ArrayList.
 * Amortised O(1) append, O(1) random access, O(n) search.
 *
 * @param <T> element type
 */
public class CustomArrayList<T> {

    private Object[] data;
    private int size;
    private static final int DEFAULT_CAPACITY = 16;

    public CustomArrayList() {
        data = new Object[DEFAULT_CAPACITY];
        size = 0;
    }

    public CustomArrayList(int initialCapacity) {
        if (initialCapacity < 1) initialCapacity = DEFAULT_CAPACITY;
        data = new Object[initialCapacity];
        size = 0;
    }

    /** Appends element to the end. Amortised O(1). */
    public void add(T element) {
        ensureCapacity();
        data[size++] = element;
    }

    /** Inserts element at the given index. O(n) worst case. */
    public void add(int index, T element) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException("Index: " + index);
        ensureCapacity();
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
        }
        data[index] = element;
        size++;
    }

    /** Returns element at index. O(1). */
    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("Index: " + index);
        return (T) data[index];
    }

    /** Sets element at index. O(1). */
    public void set(int index, T element) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("Index: " + index);
        data[index] = element;
    }

    /** Removes element at index. O(n). */
    @SuppressWarnings("unchecked")
    public T remove(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("Index: " + index);
        T removed = (T) data[index];
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
        }
        data[--size] = null;
        return removed;
    }

    /** Returns the number of elements. */
    public int size() {
        return size;
    }

    /** Returns true if empty. */
    public boolean isEmpty() {
        return size == 0;
    }

    /** Returns true if the list contains the element. O(n). */
    public boolean contains(T element) {
        return indexOf(element) >= 0;
    }

    /** Returns the index of the first occurrence, or -1. O(n). */
    public int indexOf(T element) {
        for (int i = 0; i < size; i++) {
            if (element == null ? data[i] == null : element.equals(data[i])) {
                return i;
            }
        }
        return -1;
    }

    /** Clears all elements. */
    public void clear() {
        for (int i = 0; i < size; i++) data[i] = null;
        size = 0;
    }

    /** Converts to a plain Object array. */
    public Object[] toArray() {
        Object[] result = new Object[size];
        for (int i = 0; i < size; i++) result[i] = data[i];
        return result;
    }

    /** Sorts this list using the provided comparator (merge sort). O(n log n). */
    @SuppressWarnings("unchecked")
    public void sort(CustomComparator<T> comparator) {
        if (size <= 1) return;
        Object[] aux = new Object[size];
        mergeSort(data, aux, 0, size - 1, comparator);
    }

    @SuppressWarnings("unchecked")
    private void mergeSort(Object[] arr, Object[] aux, int lo, int hi, CustomComparator<T> cmp) {
        if (lo >= hi) return;
        int mid = lo + (hi - lo) / 2;
        mergeSort(arr, aux, lo, mid, cmp);
        mergeSort(arr, aux, mid + 1, hi, cmp);
        merge(arr, aux, lo, mid, hi, cmp);
    }

    @SuppressWarnings("unchecked")
    private void merge(Object[] arr, Object[] aux, int lo, int mid, int hi, CustomComparator<T> cmp) {
        for (int k = lo; k <= hi; k++) aux[k] = arr[k];
        int i = lo, j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid) arr[k] = aux[j++];
            else if (j > hi) arr[k] = aux[i++];
            else if (cmp.compare((T) aux[i], (T) aux[j]) <= 0) arr[k] = aux[i++];
            else arr[k] = aux[j++];
        }
    }

    private void ensureCapacity() {
        if (size == data.length) {
            Object[] newData = new Object[data.length * 2];
            for (int i = 0; i < size; i++) newData[i] = data[i];
            data = newData;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            if (i > 0) sb.append(", ");
            sb.append(data[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /** Functional interface for comparison — avoids java.util.Comparator. */
    public interface CustomComparator<T> {
        int compare(T a, T b);
    }
}
