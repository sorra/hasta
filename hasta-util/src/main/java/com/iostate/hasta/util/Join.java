/* Copyright 2016 the initial author of Hasta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iostate.hasta.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Combine and compute two collections or two maps by their common keys,
 * this is like INNER JOIN because we dislike null values.
 */
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
      V1 v1 = map1.get(key);
      if (v1 != null) {
        R result = joinFunction.compute(key, v1, v2);
        results.put(key, result);
      }
    }
    return results;
  }


  /** Assumption: map1.size <= map2.size */
  public static <K, V1, V2, R> Map<K, R> mapsJoin(Map<K, V1> map1, Map<K, V2> map2, JoinFunction<K, V1, V2, R> joinFunction) {
    Map<K, R> results = new HashMap<>();
    for (Map.Entry<K, V1> entry1 : map1.entrySet()) {
      K key = entry1.getKey();
      V2 v2 = map2.get(key);
      if (v2 != null) {
        R result = joinFunction.compute(key, entry1.getValue(), v2);
        results.put(key, result);
      }
    }
    return results;
  }
}
