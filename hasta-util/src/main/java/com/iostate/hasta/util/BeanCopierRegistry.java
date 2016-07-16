package com.iostate.hasta.util;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeanCopierRegistry {
  private static Map<Pair<Class, Class>, BeanCopier> topBeanCopierMap = new ConcurrentHashMap<>();
  private static Map<Pair<Field, Field>, BeanCopier> refBeanCopierMap = new ConcurrentHashMap<>();

  public static BeanCopier findOrCreate(Class sourceCls, Class targetCls) {
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
