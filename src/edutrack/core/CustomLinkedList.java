package edutrack.core;

/**
 * Hand-built singly-linked list — replaces java.util.LinkedList.
 * O(1) prepend, O(n) search / indexed access.
 *
 * @param <T> element type
 */
public class CustomLinkedList<T> {

    private static class Node<T> {
        T data;
        Node<T> next;
        Node(T data, Node<T> next) { this.data = data; this.next = next; }
    }

    private Node<T> head;
    private int size;

    public CustomLinkedList() {
        head = null;
        size = 0;
    }

    /** Prepends an element. O(1). */
    public void addFirst(T element) {
        head = new Node<>(element, head);
        size++;
    }

    /** Appends an element. O(n). */
    public void addLast(T element) {
        if (head == null) { addFirst(element); return; }
        Node<T> curr = head;
        while (curr.next != null) curr = curr.next;
        curr.next = new Node<>(element, null);
        size++;
    }

    /** Alias for addLast. */
    public void add(T element) { addLast(element); }

    /** Returns element at index. O(n). */
    public T get(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("Index: " + index);
        Node<T> curr = head;
        for (int i = 0; i < index; i++) curr = curr.next;
        return curr.data;
    }

    /** Removes and returns the first element. O(1). */
    public T removeFirst() {
        if (head == null) throw new RuntimeException("List is empty");
        T data = head.data;
        head = head.next;
        size--;
        return data;
    }

    /** Returns true if the list contains the element. O(n). */
    public boolean contains(T element) {
        Node<T> curr = head;
        while (curr != null) {
            if (element == null ? curr.data == null : element.equals(curr.data)) return true;
            curr = curr.next;
        }
        return false;
    }

    public int size()      { return size; }
    public boolean isEmpty() { return size == 0; }

    /** Converts to CustomArrayList for convenience. */
    public CustomArrayList<T> toArrayList() {
        CustomArrayList<T> list = new CustomArrayList<>(size);
        Node<T> curr = head;
        while (curr != null) {
            list.add(curr.data);
            curr = curr.next;
        }
        return list;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node<T> curr = head;
        boolean first = true;
        while (curr != null) {
            if (!first) sb.append(" -> ");
            sb.append(curr.data);
            first = false;
            curr = curr.next;
        }
        sb.append("]");
        return sb.toString();
    }
}
