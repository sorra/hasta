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

import java.lang.reflect.*;
import java.util.*;

import com.iostate.hasta.util.exception.BeanAnalysisException;
import com.iostate.hasta.util.exception.BeanCopyException;

import static com.iostate.hasta.util.Utils.*;

class BeanCopier implements Copier {
  private Field fromField;
  private Field toField;
  private Class<?> fromCls;
  private Class<?> toCls;
  private Constructor constructor;
  private Converter converter = null;
  /** No need to be volatile */
  private List<Copier> copiers = null;

  /** Top bean */
  BeanCopier(Class<?> fromCls, Class<?> toCls) {
    this.fromCls = fromCls;
    this.toCls = toCls;
    converter = ConverterRegistry.find(fromCls.getName(), toCls.getName());
    if (converter == null) {
      try {
        constructor = toCls.getDeclaredConstructor();
        constructor.setAccessible(true);
      } catch (NoSuchMethodException e) {
        throw new BeanAnalysisException(e);
      }
    }
  }

  /** Referenced bean */
  BeanCopier(Field fromField, Field toField) {
    this.fromField = fromField;
    this.toField = toField;
    this.fromCls = fromField.getType();
    this.toCls = toField.getType();
    fromField.setAccessible(true);
    toField.setAccessible(true);
    converter = ConverterRegistry.find(fromCls.getName(), toCls.getName());
    if (converter == null) {
      try {
        constructor = toCls.getDeclaredConstructor();
        constructor.setAccessible(true);
      } catch (NoSuchMethodException e) {
        throw new BeanAnalysisException(e);
      }
    }
  }

  /** Defer after construction to avoid cyclic reference */
  void ensureAnalyzed() {
    if (converter != null) {
      return;
    }
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
  Object topCopyWithoutTopConverter(Object source) {
    if (converter != null) {
      return converter.convert(source);
    }

    Object target;
    try {
      target = constructor.newInstance();
    } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
      throw new BeanCopyException(e);
    }
    topCopyWithoutTopConverter(source, target);
    return target;
  }

  /** Top bean */
  void topCopyWithoutTopConverter(Object source, Object target) {
    ensureAnalyzed();
    for (Copier copier : copiers) {
      copier.copy(source, target);
    }
  }

  /** Referenced bean */
  @Override
  public void copy(Object source, Object target) {
    Object from, to;
    try {
      from = fromField.get(source);
      to = toField.get(target);
      if (from == null) {
        toField.set(target, null);
        return;
      }

      if (converter != null) {
        toField.set(target, converter.convert(from));
        return;
      }

      if (to == null) {
        to = constructor.newInstance();
        toField.set(target, to);
      }
    } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
      throw new BeanCopyException(e);
    }

    ensureAnalyzed();
    for (Copier copier : copiers) {
      copier.copy(from, to);
    }
  }

  private static List<Copier> analyze(Class<?> sourceCls, Class<?> targetCls) {
    Map<String, Field> fromFieldsMap = new HashMap<>();
    for (Field field : allNonStaticFields(sourceCls)) {
      fromFieldsMap.put(field.getName(), field);
    }
    if (fromFieldsMap.isEmpty()) {
      throw new BeanAnalysisException(sourceCls.getName() + " has no copyable field!");
    }

    List<Field> targetFields = allNonStaticFields(targetCls);
    if (targetFields.isEmpty()) {
      throw new BeanAnalysisException(targetCls.getName() + " has no copyable field!");
    }

    List<Copier> copiers = new ArrayList<>();
    for (Field toField : targetFields) {
      Field fromField = fromFieldsMap.get(toField.getName());
      if (fromField == null) {
        continue;
      }
      Type toFieldGType = toField.getGenericType();
      if (toFieldGType instanceof ParameterizedType) {
        Type[] toEtalTypes = ((ParameterizedType) toFieldGType).getActualTypeArguments();
        Type[] fromEtalTypes = ((ParameterizedType) fromField.getGenericType()).getActualTypeArguments();

        Class<?> fieldCls = toField.getType();
        if (Set.class.isAssignableFrom(fieldCls)) {
          copiers.add(new CollectionCopier(fromField, toField, nameOf(fromEtalTypes[0]), nameOf(toEtalTypes[0]), true));
        } else if (Collection.class.isAssignableFrom(fieldCls)) {
          copiers.add(new CollectionCopier(fromField, toField, nameOf(fromEtalTypes[0]), nameOf(toEtalTypes[0]), false));
        } else if (Map.class.isAssignableFrom(fieldCls)) {
          try {
            if (!toEtalTypes[0].equals(fromEtalTypes[0])
                && !Class.forName(nameOf(toEtalTypes[0])).isAssignableFrom(Class.forName(nameOf(fromEtalTypes[0])))) {
              throw new BeanAnalysisException("Key types mismatch: " + fromField + " <-> " + toField);
            }
          } catch (ClassNotFoundException e) {
            throw new BeanAnalysisException(e);
          }
          copiers.add(new MapCopier(fromField, toField, nameOf(fromEtalTypes[1]), nameOf(toEtalTypes[1])));
        } else {
          Converter converter = ConverterRegistry.find(fromField.getType().getName(), toField.getType().getName());
          if (converter != null) {
            copiers.add(new SingleCopier(fromField, toField));
          } else {
            throw new BeanAnalysisException("Custom generic type requires converter, otherwise is not supported!");
          }
        }
      } else {
        if (isBuiltin(fromField.getType()) || isBuiltin(toField.getType())) {
          copiers.add(new SingleCopier(fromField, toField));
        } else {
          copiers.add(BeanCopierRegistry.findOrCreate(fromField, toField));
        }
      }
    }

    if (copiers.isEmpty()) {
      throw new BeanAnalysisException(sourceCls.getName() + " & " + targetCls.getName() + " have no common fields to copy!");
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
        "`, converter=" + converter +
        ", copiers=" + join(copiers) +
        "}";
  }

  private String join(Collection<Copier> items) {
    if (items == null) {
      return "null";
    }
    StringBuilder sb = new StringBuilder("[");
    for (Object item : items) {
      sb.append("\n  ");
      String str = (item instanceof BeanCopier) ? item.getClass().getSimpleName() : item.toString();
      sb.append(str);
    }
    return sb.append("]").toString();
  }
}
