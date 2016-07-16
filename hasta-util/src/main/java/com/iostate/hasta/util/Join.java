package com.iostate.hasta.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Join {

  /** Assumption: coll1.size <= coll2.size */
  public static <K, V1, V2, R> Map<K, R> loopJoin(Collection<V1> coll1, Collection<V2> coll2,
                                                  KeyExtractor<K, V1> extractor1, KeyExtractor<K, V2> extractor2,
                                                  JoinFunction<K, V1, V2, R> joinFunction) {
    Map<K, R> results = new HashMap<>();
    for (V2 v2 : coll2) {
      K key = extractor2.extract(v2);

      for (V1 v1 : coll1) {
        if (extractor1.extract(v1).equals(key)) {
          results.put(key, joinFunction.compute(key, v1, v2));
          break;
        }
      }
    }
    return results;
  }

  /** Internal: coll1 is turned into HashMap */
  public static <K, V1, V2, R> Map<K, R> hashJoin(Collection<V1> coll1, Collection<V2> coll2,
                                                  KeyExtractor<K, V1> extractor1, KeyExtractor<K, V2> extractor2,
                                                  JoinFunction<K, V1, V2, R> joinFunction) {
    Map<K, R> results = new HashMap<>();

    Map<K, V1> map1 = new HashMap<>();
    for (V1 v1 : coll1) {
      map1.put(extractor1.extract(v1), v1);
    }

    for (V2 v2 : coll2) {
      K key = extractor2.extract(v2);
      R result = joinFunction.compute(key, map1.get(key), v2);
      results.put(key, result);
    }
    return results;
  }


  /** Assumption: map1.size <= map2.size */
  public static <K, V1, V2, R> Map<K, R> mapsJoin(Map<K, V1> map1, Map<K, V2> map2, JoinFunction<K, V1, V2, R> joinFunction) {
    Map<K, R> results = new HashMap<>();
    for (Map.Entry<K, V1> entry1 : map1.entrySet()) {
      K key = entry1.getKey();
      R result = joinFunction.compute(key, entry1.getValue(), map2.get(key));
      results.put(key, result);
    }
    return results;
  }
}
