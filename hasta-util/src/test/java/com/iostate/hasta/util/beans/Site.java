package com.iostate.hasta.util.beans;

import java.util.Map;

public class Site {
  private String name;
  private Map<String, User> users;

  Site() {}

  public Site(String name, Map<String, User> users) {
    this.name = name;
    this.users = users;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, User> getUsers() {
    return users;
  }

  public void setUsers(Map<String, User> users) {
    this.users = users;
  }

  @Override
  public String toString() {
    return "Site{" +
        "name='" + name + '\'' +
        ", users=" + users +
        '}';
  }
}
