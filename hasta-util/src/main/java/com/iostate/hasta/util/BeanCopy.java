package com.iostate.hasta.util;

import java.util.Collection;
import java.util.Map;

public class BeanCopy {

  public static void copy(Object source, Object target) {
    BeanCopierRegistry.findOrCreate(source.getClass(), target.getClass()).topCopy(source, target);
  }

  public static void copy(Collection<?> sources, Collection<?> targets, Class<?> sourceCls, Class<?> targetCls) {
    Collection<Object> results = (Collection<Object>) targets;
    BeanCopier elemCopier = BeanCopierRegistry.findOrCreate(sourceCls, targetCls);
    for (Object source : sources) {
      results.add(elemCopier.topCopy(source));
    }
  }

  public static <K> void copy(Map<K, ?> sources, Map<K, ?> targets, Class<?> sourceCls, Class<?> targetCls) {
    Map<K, Object> results = (Map<K, Object>) targets;
    BeanCopier elemCopier = BeanCopierRegistry.findOrCreate(sourceCls, targetCls);
    for (Map.Entry<K, ?> source : sources.entrySet()) {
      results.put(source.getKey(), elemCopier.topCopy(source.getValue()));
    }
  }
}
