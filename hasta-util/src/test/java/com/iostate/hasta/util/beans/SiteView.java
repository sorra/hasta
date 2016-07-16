package com.iostate.hasta.util.beans;

import java.util.List;
import java.util.Map;

public class SiteView {
  private String name;
  private Map<String, User> users;

  SiteView() {}

  public SiteView(String name, Map<String, User> users) {
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
    return "SiteView{" +
        "name='" + name + '\'' +
        ", users=" + users +
        '}';
  }
}
