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

  static Converter findOrCreateConverter(String fromType, String toType) {
    Converter converter;
    Class<?> fromCls;
    Class<?> toCls;
    try {
      fromCls = Class.forName(fromType);
      toCls = Class.forName(toType);
    } catch (ClassNotFoundException e) {
      throw new BeanAnalysisException(e);
    }
    converter = ConverterRegistry.find(fromType, toType);
    if (converter == null && !toCls.isAssignableFrom(fromCls)) {
      if (!Utils.isBuiltin(fromCls) && !Utils.isBuiltin(toCls)) {
        final BeanCopier beanCopier = BeanCopierRegistry.findOrCreate(fromCls, toCls);
        if (beanCopier != null) {
          converter = new Converter() {
            @Override
            public Object convert(Object from) {
              return beanCopier.topCopyWithoutTopConverter(from);
            }
          };
        }
      }
      if (converter == null) {
        throw new BeanAnalysisException(String.format("Converter not found. from: %s, to: %s", fromType, toType));
      }
    } // else keep null
    return converter;
  }
}
