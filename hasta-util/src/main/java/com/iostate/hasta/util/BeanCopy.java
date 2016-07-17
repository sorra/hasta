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
import java.util.Map;

/**
 * Copy properties from an instance of AClass to an instance of BClass,
 * even if AClass and BClass have nested structures and collections/maps of instances of other classes.
 *
 * You can also do batch copying on collections or maps.
 *
 * @see ConverterRegistry During setup(before using BeanCopy), you can add custom converters to supply or override the default behavior.
 */
@SuppressWarnings("unchecked")
public class BeanCopy {

  /** Copy properties of source to a new instance of targetCls */
  public static <R> R copy(Object source, Class<R> targetCls) {
    return (R) BeanCopierRegistry.findOrCreate(source.getClass(), targetCls).topCopyWithoutTopConverter(source);
  }

  /** <b>Caution:</b> Ignores the converter of source->target (if any) */
  public static void copy(Object source, Object target) {
    BeanCopierRegistry.findOrCreate(source.getClass(), target.getClass()).topCopyWithoutTopConverter(source, target);
  }

  /** Copy each element of sources and add to targets */
  public static void copy(Collection<?> sources, Collection<?> targets, Class<?> sourceCls, Class<?> targetCls) {
    Collection<Object> results = (Collection<Object>) targets;
    BeanCopier elemCopier = BeanCopierRegistry.findOrCreate(sourceCls, targetCls);
    for (Object source : sources) {
      results.add(elemCopier.topCopyWithoutTopConverter(source));
    }
  }

  /** Copy each element of sources and add to targets */
  public static <K> void copy(Map<K, ?> sources, Map<K, ?> targets, Class<?> sourceCls, Class<?> targetCls) {
    Map<K, Object> results = (Map<K, Object>) targets;
    BeanCopier elemCopier = BeanCopierRegistry.findOrCreate(sourceCls, targetCls);
    for (Map.Entry<K, ?> source : sources.entrySet()) {
      results.put(source.getKey(), elemCopier.topCopyWithoutTopConverter(source.getValue()));
    }
  }
}
