package com.iostate.hasta.util;

public interface JoinFunction<K, V1, V2, R> {
  R compute(K key, V1 value1, V2 value2);
}
