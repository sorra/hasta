package com.iostate.hasta.util;

import java.lang.reflect.Type;

import com.iostate.hasta.util.exception.BeanAnalysisException;

class Utils {
  static boolean isBuiltin(Class<?> cls) {
    return cls.isPrimitive() || cls.getName().startsWith("java.") || cls.getName().startsWith("javax.");
  }

  static String nameOf(Type type) {
    String str = type.toString();
    int idxOfSpace = str.lastIndexOf(' ');
    if (idxOfSpace >= 0) {
      return str.substring(idxOfSpace+1);
    } else {
      return str;
    }
  }

  static Converter findConverter(String fromType, String toType) {
    Converter converter = null;
    Class<?> fromCls;
    Class<?> toCls;
    try {
      fromCls = Class.forName(fromType);
      toCls = Class.forName(toType);
    } catch (ClassNotFoundException e) {
      throw new BeanAnalysisException(e);
    }
    if (!toCls.equals(fromCls) && !toCls.isAssignableFrom(fromCls)) {
      converter = ConverterRegistry.find(fromType, toType);
      if (converter == null && !Utils.isBuiltin(fromCls) && !Utils.isBuiltin(toCls)) {
        final BeanCopier beanCopier = BeanCopierRegistry.findOrCreate(fromCls, toCls);
        if (beanCopier != null) {
          converter = new Converter() {
            @Override
            public Object convert(Object from) {
              return beanCopier.topCopy(from);
            }
          };
        }
      }
      if (converter == null) {
        throw new BeanAnalysisException(String.format("Converter not found. from: %s, to: %s", fromType, toType));
      }
    }
    return converter;
  }
}
