package com.iostate.hasta.util;

import java.lang.reflect.*;
import java.util.*;

import com.iostate.hasta.util.exception.BeanAnalysisException;
import com.iostate.hasta.util.exception.BeanCopyException;

import static com.iostate.hasta.util.Utils.*;

public class BeanCopier implements Copier {
  private Field fromField;
  private Field toField;
  private Class<?> fromCls;
  private Class<?> toCls;
  private Constructor constructor;
  /** No need to be volatile */
  private List<Copier> copiers;

  /** Top bean */
  public BeanCopier(Class<?> fromCls, Class<?> toCls) {
    this.fromCls = fromCls;
    this.toCls = toCls;
    try {
      constructor = toCls.getDeclaredConstructor();
      constructor.setAccessible(true);
    } catch (NoSuchMethodException e) {
      throw new BeanAnalysisException(e);
    }
  }

  /** Referenced bean */
  public BeanCopier(Field fromField, Field toField) {
    this.fromField = fromField;
    this.toField = toField;
    this.fromCls = fromField.getType();
    this.toCls = toField.getType();
    try {
      constructor = toCls.getDeclaredConstructor();
      constructor.setAccessible(true);
    } catch (NoSuchMethodException e) {
      throw new BeanAnalysisException(e);
    }
  }

  /** Defer after construction to avoid cyclic reference */
  public void ensureAnalyzed() {
    // DCL without volatile
    if (copiers == null) {
      synchronized (this) {
        if (copiers == null) {
          copiers = analyze(fromCls, toCls);
        }
      }
    }
  }

  /** Top bean */
  public Object topCopy(Object source) {
    Object target;
    try {
      target = constructor.newInstance();
    } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
      throw new BeanCopyException(e);
    }
    topCopy(source, target);
    return target;
  }

  /** Top bean */
  public void topCopy(Object source, Object target) {
    ensureAnalyzed();
    for (Copier copier : copiers) {
      copier.copy(source, target);
    }
  }

  /** Referenced bean */
  @Override
  public void copy(Object source, Object target) {
    ensureAnalyzed();
    Object from, to;
    try {
      from = fromField.get(source);
      to = toField.get(target);
      if (from == null) {
        toField.set(target, null);
        return;
      }
      if (to == null) {
        to = constructor.newInstance();
        toField.set(target, to);
      }
    } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
      throw new BeanCopyException(e);
    }
    for (Copier copier : copiers) {
      copier.copy(from, to);
    }
  }

  private static List<Copier> analyze(Class<?> sourceCls, Class<?> targetCls) {
    Map<String, Field> fromFieldsMap = new HashMap<>();
    for (Field field : allNonStaticFields(sourceCls)) {
      fromFieldsMap.put(field.getName(), field);
    }

    List<Copier> copiers = new ArrayList<>();
    for (Field toField : allNonStaticFields(targetCls)) {
      Field fromField = fromFieldsMap.get(toField.getName());
      if (fromField == null) {
        continue;
      }
      Type toFieldType = toField.getGenericType();
      if (toFieldType instanceof ParameterizedType) {
        Type[] toEtalTypes = ((ParameterizedType) toFieldType).getActualTypeArguments();
        Type[] fromEtalTypes = ((ParameterizedType) fromField.getGenericType()).getActualTypeArguments();

        Class<?> fieldCls = toField.getType();
        if (Set.class.isAssignableFrom(fieldCls)) {
          copiers.add(new CollectionCopier(fromField, toField, nameOf(fromEtalTypes[0]), nameOf(toEtalTypes[0]), true));
        } else if (Collection.class.isAssignableFrom(fieldCls)) {
          copiers.add(new CollectionCopier(fromField, toField, nameOf(fromEtalTypes[0]), nameOf(toEtalTypes[0]), false));
        } else if (Map.class.isAssignableFrom(fieldCls)) {
          copiers.add(new MapCopier(fromField, toField, nameOf(fromEtalTypes[1]), nameOf(toEtalTypes[1])));
        } else {
          throw new BeanAnalysisException("Custom generic type is not supported!");
        }
      } else {
        if (isBuiltin(fromField.getType()) || isBuiltin(toField.getType())) {
          copiers.add(new SingleCopier(fromField, toField));
        } else {
          copiers.add(BeanCopierRegistry.findOrCreate(fromField, toField));
        }
      }
    }

    return copiers;
  }

  private static List<Field> allNonStaticFields(Class<?> cls) {
    List<Field> all = new ArrayList<>();
    Class<?> cur = cls;
    do {
      for (Field each : cur.getDeclaredFields()) {
        if (!Modifier.isStatic(each.getModifiers())) {
          all.add(each);
        }
      }
    } while ((cur = cur.getSuperclass()) != null);
    return all;
  }

  @Override
  public String toString() {
    ensureAnalyzed();
    return "BeanCopier{" +
        "fromField=`" + fromField +
        "`, toField=`" + toField +
        "`, fromCls=`" + fromCls +
        "`, toCls=`" + toCls +
        "`, copiers=[" + join(copiers) +
        "]}";
  }

  private String join(Collection<Copier> items) {
    StringBuilder sb = new StringBuilder();
    for (Object item : items) {
      sb.append("\n  ");
      String str = (item instanceof BeanCopier) ? item.getClass().getSimpleName() : item.toString();
      sb.append(str);
    }
    return sb.toString();
  }
}