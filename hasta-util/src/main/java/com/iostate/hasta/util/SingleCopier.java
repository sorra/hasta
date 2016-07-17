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

import com.iostate.hasta.util.exception.BeanAnalysisException;
import com.iostate.hasta.util.exception.BeanCopyException;

class SingleCopier implements Copier {
  private Field fromField;
  private Field toField;
  private Converter converter = null;

  SingleCopier(Field fromField, Field toField) {
    this.fromField = fromField;
    this.toField = toField;
    fromField.setAccessible(true);
    toField.setAccessible(true);
    Class<?> fromCls = fromField.getType();
    Class<?> toCls = toField.getType();
    if (!toCls.isAssignableFrom(fromCls)) {
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
