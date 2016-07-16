package com.iostate.hasta.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of all added converters.
 */
public class ConverterRegistry {
  private static Map<Pair<String, String>, Converter> converterMap = new ConcurrentHashMap<>();

  static Converter find(String fromType, String toType) {
    return converterMap.get(Pair.of(fromType, toType));
  }

  public static void put(String fromType, String toType, Converter converter) {
    converterMap.put(Pair.of(fromType, toType), converter);
  }

  static void clear() {
    converterMap.clear();
  }
}
