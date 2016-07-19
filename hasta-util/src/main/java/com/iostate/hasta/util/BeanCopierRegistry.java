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

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of all generated bean copiers.
 */
public class BeanCopierRegistry {
  private static Map<Pair<Class, Class>, BeanCopier> topBeanCopierMap = new ConcurrentHashMap<>();
  private static Map<Pair<Field, Field>, BeanCopier> refBeanCopierMap = new ConcurrentHashMap<>();

  /** Prepare a bean copier before using, in order to check correctness and warm-up cache in advance */
  public static BeanCopier prepare(Class sourceCls, Class targetCls) {
    Pair<Class, Class> pair = Pair.of(sourceCls, targetCls);
    BeanCopier beanCopier = topBeanCopierMap.get(pair);
    if (beanCopier == null) {
      beanCopier = new BeanCopier(sourceCls, targetCls);
      topBeanCopierMap.put(pair, beanCopier);
      beanCopier.ensureAnalyzed();
    }
    return beanCopier;
  }

  static BeanCopier findOrCreate(Field fromField, Field toField) {
    Pair<Field, Field> pair = Pair.of(fromField, toField);
    BeanCopier beanCopier = refBeanCopierMap.get(pair);
    if (beanCopier == null) {
      beanCopier = new BeanCopier(fromField, toField);
      refBeanCopierMap.put(pair, beanCopier);
    }
    return beanCopier;
  }

  static void clear() {
    topBeanCopierMap.clear();
    refBeanCopierMap.clear();
  }
}
