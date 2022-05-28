package dev.openfeature.javasdk;

import lombok.Data;

@Data
/**
 * This is only for testing that object serialization works with both generics and nested classes.
 */
class Node<T> {
    Node<T> left, right;
    T value;
}
