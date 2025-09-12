package dev.openfeature.sdk;

class Pair<K, V> {
    private final K left;
    private final V right;

    private Pair(K left, V value) {
        this.left = left;
        this.right = value;
    }

    public K getLeft() {
        return left;
    }

    public V getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "Pair{" + "key=" + left + ", value=" + right + '}';
    }

    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }
}
