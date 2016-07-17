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

package com.iostate.hasta.util.beans;

import java.util.List;

public class User {
  private String name;
  private List<User> underHands;

  User() {}

  public User(String name, List<User> underHands) {
    this.name = name;
    this.underHands = underHands;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<User> getUnderHands() {
    return underHands;
  }

  public void setUnderHands(List<User> underHands) {
    this.underHands = underHands;
  }

  @Override
  public String toString() {
    return "User{" +
        "name='" + name + '\'' +
        ", underHands=" + underHands +
        '}';
  }
}
