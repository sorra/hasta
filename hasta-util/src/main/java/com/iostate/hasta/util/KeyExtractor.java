package com.iostate.hasta.util;

public interface KeyExtractor<K, V> {
  K extract(V value);
}
