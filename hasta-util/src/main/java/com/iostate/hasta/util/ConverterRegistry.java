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
