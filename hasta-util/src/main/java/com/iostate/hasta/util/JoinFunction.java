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

/**
 * Defines how to compute Value1 and Value2 by their common Key
 * @param <K> Key
 * @param <V1> Value1
 * @param <V2> Value2
 * @param <R> Result
 */
public interface JoinFunction<K, V1, V2, R> {
  R compute(K key, V1 value1, V2 value2);
}
