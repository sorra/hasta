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
