package dev.openfeature.sdk.internal;

import java.util.Objects;

/**
 * Like {@link java.util.function.BiConsumer} but with 3 params.
 * 
 * @see java.util.function.BiConsumer
 */
@FunctionalInterface
interface TriConsumer<T, U, V> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @param v the third input argument
     */
    void accept(T t, U u, V v);

    default TriConsumer<T, U, V> andThen(TriConsumer<T, U, V> after) {
        Objects.requireNonNull(after);

        return (t, u, v) -> {
            accept(t, u, v);
            after.accept(t, u, v);
        };
    }
}