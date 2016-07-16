package com.iostate.hasta.util;

import java.lang.reflect.Field;

import com.iostate.hasta.util.exception.BeanAnalysisException;
import com.iostate.hasta.util.exception.BeanCopyException;

public class SingleCopier implements Copier {
  private Field fromField;
  private Field toField;
  private Converter converter = null;

  public SingleCopier(Field fromField, Field toField) {
    this.fromField = fromField;
    this.toField = toField;
    fromField.setAccessible(true);
    toField.setAccessible(true);
    Class<?> fromCls = fromField.getType();
    Class<?> toCls = toField.getType();
    if (!toCls.equals(fromCls) && !toCls.isAssignableFrom(fromCls)) {
      converter = ConverterRegistry.find(fromCls.getName(), toCls.getName());
      if (converter == null) {
        throw new BeanAnalysisException(String.format("Converter not found. from: %s, to: %s", fromCls.getName(), toCls.getName()));
      }
    }
  }

  @Override
  public void copy(Object source, Object target) {
    try {
      Object value = fromField.get(source);
      if (value == null) {
        toField.set(target, null);
        return;
      }
      if (converter == null) {
        toField.set(target, value);
      } else {
        toField.set(target, converter.convert(value));
      }
      toField.set(target, value);
    } catch (IllegalAccessException e) {
      throw new BeanCopyException(e);
    }
  }

  @Override
  public String toString() {
    return "SingleCopier{" +
        "fromField=" + fromField +
        ", toField=" + toField +
        ", converter=" + converter +
        '}';
  }
}
