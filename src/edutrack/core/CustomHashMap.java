package edutrack.core;

/**
 * Hand-built hash map — replaces java.util.HashMap.
 * Separate chaining with linked buckets.
 * Average O(1) put/get/remove, worst-case O(n) with degenerate hashing.
 *
 * @param <K> key type
 * @param <V> value type
 */
public class CustomHashMap<K, V> {

    /** A single key-value entry in a bucket chain. */
    private static class Entry<K, V> {
        K key;
        V value;
        Entry<K, V> next;

        Entry(K key, V value, Entry<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private Entry<K, V>[] buckets;
    private int size;
    private int capacity;
    private static final int DEFAULT_CAPACITY = 64;
    private static final double LOAD_FACTOR = 0.75;

    @SuppressWarnings("unchecked")
    public CustomHashMap() {
        capacity = DEFAULT_CAPACITY;
        buckets = new Entry[capacity];
        size = 0;
    }

    @SuppressWarnings("unchecked")
    public CustomHashMap(int initialCapacity) {
        capacity = Math.max(initialCapacity, 16);
        buckets = new Entry[capacity];
        size = 0;
    }

    /** Associates key with value. If key exists, replaces the old value. */
    public void put(K key, V value) {
        if ((double) size / capacity >= LOAD_FACTOR) rehash();

        int idx = bucketIndex(key);
        Entry<K, V> curr = buckets[idx];
        while (curr != null) {
            if (keysEqual(curr.key, key)) {
                curr.value = value;
                return;
            }
            curr = curr.next;
        }
        buckets[idx] = new Entry<>(key, value, buckets[idx]);
        size++;
    }

    /** Returns the value for the given key, or null if absent. */
    public V get(K key) {
        int idx = bucketIndex(key);
        Entry<K, V> curr = buckets[idx];
        while (curr != null) {
            if (keysEqual(curr.key, key)) return curr.value;
            curr = curr.next;
        }
        return null;
    }

    /** Returns value for key if present, otherwise returns defaultValue. */
    public V getOrDefault(K key, V defaultValue) {
        V val = get(key);
        return val != null || containsKey(key) ? val : defaultValue;
    }

    /** Returns true if the map contains the given key. */
    public boolean containsKey(K key) {
        int idx = bucketIndex(key);
        Entry<K, V> curr = buckets[idx];
        while (curr != null) {
            if (keysEqual(curr.key, key)) return true;
            curr = curr.next;
        }
        return false;
    }

    /** Removes the entry for the given key. Returns old value or null. */
    public V remove(K key) {
        int idx = bucketIndex(key);
        Entry<K, V> prev = null;
        Entry<K, V> curr = buckets[idx];
        while (curr != null) {
            if (keysEqual(curr.key, key)) {
                if (prev == null) buckets[idx] = curr.next;
                else prev.next = curr.next;
                size--;
                return curr.value;
            }
            prev = curr;
            curr = curr.next;
        }
        return null;
    }

    /** Returns the number of key-value pairs. */
    public int size() { return size; }

    /** Returns true if the map is empty. */
    public boolean isEmpty() { return size == 0; }

    /** Returns all keys as a CustomArrayList. */
    public CustomArrayList<K> keys() {
        CustomArrayList<K> list = new CustomArrayList<>();
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> curr = buckets[i];
            while (curr != null) {
                list.add(curr.key);
                curr = curr.next;
            }
        }
        return list;
    }

    /** Returns all values as a CustomArrayList. */
    public CustomArrayList<V> values() {
        CustomArrayList<V> list = new CustomArrayList<>();
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> curr = buckets[i];
            while (curr != null) {
                list.add(curr.value);
                curr = curr.next;
            }
        }
        return list;
    }

    /** Returns all entries as a CustomArrayList of Pair<K,V>. */
    public CustomArrayList<Pair<K, V>> entries() {
        CustomArrayList<Pair<K, V>> list = new CustomArrayList<>();
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> curr = buckets[i];
            while (curr != null) {
                list.add(new Pair<>(curr.key, curr.value));
                curr = curr.next;
            }
        }
        return list;
    }

    /** Clears all entries. */
    @SuppressWarnings("unchecked")
    public void clear() {
        buckets = new Entry[capacity];
        size = 0;
    }

    /* ---------- internal helpers ---------- */

    private int bucketIndex(K key) {
        if (key == null) return 0;
        return (key.hashCode() & 0x7FFFFFFF) % capacity;
    }

    private boolean keysEqual(K a, K b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    @SuppressWarnings("unchecked")
    private void rehash() {
        int newCapacity = capacity * 2;
        Entry<K, V>[] newBuckets = new Entry[newCapacity];
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> curr = buckets[i];
            while (curr != null) {
                Entry<K, V> next = curr.next;
                int idx = (curr.key == null ? 0 : (curr.key.hashCode() & 0x7FFFFFFF) % newCapacity);
                curr.next = newBuckets[idx];
                newBuckets[idx] = curr;
                curr = next;
            }
        }
        buckets = newBuckets;
        capacity = newCapacity;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> curr = buckets[i];
            while (curr != null) {
                if (!first) sb.append(", ");
                sb.append(curr.key).append("=").append(curr.value);
                first = false;
                curr = curr.next;
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
