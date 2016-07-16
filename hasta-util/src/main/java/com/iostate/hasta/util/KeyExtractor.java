package com.iostate.hasta.util;

/**
 * Defines how to extract the key from value.
 * @param <K> Key
 * @param <V> Value
 */
public interface KeyExtractor<K, V> {
  K extract(V value);
}
