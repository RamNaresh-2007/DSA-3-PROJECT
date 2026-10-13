package edutrack.core;

/**
 * Hand-built ordered map backed by a balanced BST (AVL tree) — replaces java.util.TreeMap.
 * O(log n) put/get/remove, in-order traversal for sorted key iteration.
 *
 * @param <K> key type (compared via supplied comparator)
 * @param <V> value type
 */
public class CustomTreeMap<K, V> {

    private static class Node<K, V> {
        K key; V value;
        Node<K, V> left, right;
        int height;
        Node(K key, V value) { this.key = key; this.value = value; height = 1; }
    }

    private Node<K, V> root;
    private int size;
    private final CustomArrayList.CustomComparator<K> comparator;

    public CustomTreeMap(CustomArrayList.CustomComparator<K> comparator) {
        this.comparator = comparator;
        root = null;
        size = 0;
    }

    /** Inserts or updates a key-value pair. O(log n). */
    public void put(K key, V value) {
        root = insert(root, key, value);
    }

    /** Returns the value for the given key, or null. O(log n). */
    public V get(K key) {
        Node<K, V> node = find(root, key);
        return node == null ? null : node.value;
    }

    /** Returns true if the key is present. */
    public boolean containsKey(K key) { return find(root, key) != null; }

    /** Removes a key. O(log n). */
    public void remove(K key) {
        if (containsKey(key)) { root = delete(root, key); size--; }
    }

    /** Returns all keys in sorted order. */
    public CustomArrayList<K> keys() {
        CustomArrayList<K> result = new CustomArrayList<>();
        inorder(root, result);
        return result;
    }

    public int size()        { return size; }
    public boolean isEmpty() { return size == 0; }

    /* ---------- AVL internals ---------- */

    private int height(Node<K, V> n) { return n == null ? 0 : n.height; }
    private int balance(Node<K, V> n) { return n == null ? 0 : height(n.left) - height(n.right); }

    private void updateHeight(Node<K, V> n) {
        n.height = 1 + Math.max(height(n.left), height(n.right));
    }

    private Node<K, V> rotateRight(Node<K, V> y) {
        Node<K, V> x = y.left;
        y.left = x.right;
        x.right = y;
        updateHeight(y); updateHeight(x);
        return x;
    }

    private Node<K, V> rotateLeft(Node<K, V> x) {
        Node<K, V> y = x.right;
        x.right = y.left;
        y.left = x;
        updateHeight(x); updateHeight(y);
        return y;
    }

    private Node<K, V> rebalance(Node<K, V> n) {
        updateHeight(n);
        int bal = balance(n);
        if (bal > 1) {
            if (balance(n.left) < 0) n.left = rotateLeft(n.left);
            return rotateRight(n);
        }
        if (bal < -1) {
            if (balance(n.right) > 0) n.right = rotateRight(n.right);
            return rotateLeft(n);
        }
        return n;
    }

    private Node<K, V> insert(Node<K, V> n, K key, V value) {
        if (n == null) { size++; return new Node<>(key, value); }
        int cmp = comparator.compare(key, n.key);
        if (cmp < 0)      n.left  = insert(n.left, key, value);
        else if (cmp > 0)  n.right = insert(n.right, key, value);
        else                n.value = value; // update
        return rebalance(n);
    }

    private Node<K, V> delete(Node<K, V> n, K key) {
        if (n == null) return null;
        int cmp = comparator.compare(key, n.key);
        if (cmp < 0)      n.left  = delete(n.left, key);
        else if (cmp > 0)  n.right = delete(n.right, key);
        else {
            if (n.left == null) return n.right;
            if (n.right == null) return n.left;
            Node<K, V> successor = minNode(n.right);
            n.key = successor.key;
            n.value = successor.value;
            n.right = delete(n.right, successor.key);
        }
        return rebalance(n);
    }

    private Node<K, V> minNode(Node<K, V> n) {
        while (n.left != null) n = n.left;
        return n;
    }

    private Node<K, V> find(Node<K, V> n, K key) {
        if (n == null) return null;
        int cmp = comparator.compare(key, n.key);
        if (cmp < 0) return find(n.left, key);
        if (cmp > 0) return find(n.right, key);
        return n;
    }

    private void inorder(Node<K, V> n, CustomArrayList<K> result) {
        if (n == null) return;
        inorder(n.left, result);
        result.add(n.key);
        inorder(n.right, result);
    }
}
