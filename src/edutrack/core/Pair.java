package edutrack.core;

/**
 * Generic immutable pair — replaces java.util.Map.Entry and similar.
 *
 * @param <A> type of the first element
 * @param <B> type of the second element
 */
public class Pair<A, B> {
    private final A first;
    private final B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst()  { return first; }
    public B getSecond() { return second; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair<?, ?> p = (Pair<?, ?>) o;
        boolean eq1 = (first == null ? p.first == null : first.equals(p.first));
        boolean eq2 = (second == null ? p.second == null : second.equals(p.second));
        return eq1 && eq2;
    }

    @Override
    public int hashCode() {
        int h1 = first  == null ? 0 : first.hashCode();
        int h2 = second == null ? 0 : second.hashCode();
        return h1 * 31 + h2;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
