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

import java.util.*;

import org.junit.Assert;
import org.junit.Test;

public class JoinTest {
  List<String> people = Arrays.asList("1-Ann", "2-Ben", "3-Cecilia");
  List<Pair<Long, Integer>> ages = Arrays.asList(Pair.of(1L, 17), Pair.of(2L, 20), Pair.of(3L, 21));

  KeyExtractor<Long, String> keyExtractor1 = new KeyExtractor<Long, String>() {
    @Override
    public Long extract(String value) {
      return Long.valueOf(value.split("-")[0]);
    }
  };

  KeyExtractor<Long, Pair<Long, Integer>> keyExtractor2 = new KeyExtractor<Long, Pair<Long, Integer>>() {
    @Override
    public Long extract(Pair<Long, Integer> value) {
      return value.getA();
    }
  };

  JoinFunction<Long, String, Pair<Long, Integer>, String> joinFunction = new JoinFunction<Long, String, Pair<Long, Integer>, String>() {
    @Override
    public String compute(Long key, String person, Pair<Long, Integer> agePair) {
      return person.split("-")[1] + " is " + agePair.getB() + " years old";
    }
  };

  private void checkResults(Map<Long, String> statements) {
    Assert.assertEquals("Ann is 17 years old", statements.get(1L));
    Assert.assertEquals("Ben is 20 years old", statements.get(2L));
    Assert.assertEquals("Cecilia is 21 years old", statements.get(3L));
  }

  @Test
  public void testLoopJoin() {
    Map<Long, String> statements = Join.loopJoin(people, ages, keyExtractor1, keyExtractor2, joinFunction);
    checkResults(statements);
  }

  @Test
  public void testHashJoin() {
    Map<Long, String> statements = Join.hashJoin(people, ages, keyExtractor1, keyExtractor2, joinFunction);
    checkResults(statements);
  }

  @Test
  public void testMapsJoin() {
    Map<Long, String> peopleMap = new HashMap<>();
    for (int i = 0; i < people.size(); i++) {
      peopleMap.put((long) i+1, people.get(i));
    }

    Map<Long, Pair<Long, Integer>> agesMap = new HashMap<>();
    for (int i = 0; i < ages.size(); i++) {
      agesMap.put((long) i+1, ages.get(i));
    }

    Map<Long, String> statements = Join.mapsJoin(peopleMap, agesMap, joinFunction);
    checkResults(statements);
  }
}
