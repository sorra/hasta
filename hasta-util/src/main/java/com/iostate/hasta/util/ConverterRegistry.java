package com.iostate.hasta.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.Pair;

class ConverterRegistry {
  private static Map<Pair<String, String>, Converter> converterMap = new ConcurrentHashMap<>();

  static Converter find(String fromType, String toType) {
    return converterMap.get(Pair.of(fromType, toType));
  }
}
