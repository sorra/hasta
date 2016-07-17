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
import java.util.HashMap;
import java.util.Map;

import com.iostate.hasta.util.exception.BeanCopyException;

class MapCopier implements Copier {
  private Field fromField;
  private Field toField;
  private Converter converter = null;

  MapCopier(Field fromField, Field toField, String fromEtalType, String toEtalType) {
    this.fromField = fromField;
    this.toField = toField;
    fromField.setAccessible(true);
    toField.setAccessible(true);
    converter = Utils.findOrCreateConverter(fromEtalType, toEtalType);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void copy(Object source, Object target) {
    try {
      Map fromMap = (Map) fromField.get(source);
      if (fromMap == null) {
        toField.set(target, null);
        return;
      }
      Map toMap = (Map) toField.get(target);
      if (toMap == null) {
        toMap = new HashMap();
        toField.set(target, toMap);
      }

      if (converter == null) {
        toMap.putAll(fromMap);
      } else {
        for (Object entryObj : fromMap.entrySet()) {
          Map.Entry entry = (Map.Entry) entryObj;
          toMap.put(entry.getKey(), converter.convert(entry.getValue()));
        }
      }
    } catch (IllegalAccessException e) {
      throw new BeanCopyException(e);
    }
  }
}
