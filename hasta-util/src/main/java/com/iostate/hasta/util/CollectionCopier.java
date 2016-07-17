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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.iostate.hasta.util.exception.BeanCopyException;

class CollectionCopier implements Copier {
  private Field fromField;
  private Field toField;
  private boolean isSet;
  private Converter converter = null;

  CollectionCopier(Field fromField, Field toField, String fromEtalType, String toEtalType, boolean isSet) {
    this.fromField = fromField;
    this.toField = toField;
    this.isSet = isSet;
    fromField.setAccessible(true);
    toField.setAccessible(true);
    converter = Utils.findOrCreateConverter(fromEtalType, toEtalType);
  }

  @Override @SuppressWarnings("unchecked")
  public void copy(Object source, Object target) {
    try {
      Collection fromColl = (Collection) fromField.get(source);
      if (fromColl == null) {
        toField.set(target, null);
        return;
      }
      Collection toColl = (Collection) toField.get(target);
      if (toColl == null) {
        if (isSet) {toColl = new HashSet();}
        else {toColl = new ArrayList();}
        toField.set(target, toColl);
      }

      if (converter == null) {
        toColl.addAll(fromColl);
      } else {
        for (Object elem : fromColl) {
          toColl.add(converter.convert(elem));
        }
      }
    } catch (IllegalAccessException e) {
      throw new BeanCopyException(e);
    }
  }

  @Override
  public String toString() {
    return "CollectionCopier{" +
        "fromField=" + fromField +
        ", toField=" + toField +
        ", converter=" + converter +
        ", isSet=" + isSet +
        '}';
  }
}
