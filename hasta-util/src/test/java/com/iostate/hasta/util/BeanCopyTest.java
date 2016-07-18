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

import com.iostate.hasta.util.beans.*;
import com.iostate.hasta.util.exception.BeanAnalysisException;
import org.junit.Assert;
import org.junit.Test;

import static java.util.Collections.singletonList;

public class BeanCopyTest {
  @Test
  public void testCopySingle() {
    Site site = new Site("MySite", new HashMap<String, User>());
    User admin = new User("Admin", null);
    site.setAdmin(admin);
    site.getUsers().put("CEO", new User("CEO", singletonList(admin)));

    // If there is a converter of Site->SiteView, it works
    SiteView siteView = BeanCopy.copy(site, SiteView.class);

    // If there is a converter of Site->SiteView, it doesn't work
    SiteView siteView0 = new SiteView(null, null);
    BeanCopy.copy(site, siteView0);

    Assert.assertEquals("SiteView{name='MySite', admin=UserView{name='Admin', underHands=null}, users={CEO=UserView{name='CEO', underHands=[UserView{name='Admin', underHands=null}]}}}", siteView.toString());
    Assert.assertEquals(siteView.toString(), siteView0.toString());
  }

  @Test
  public void testCopyCollection() {
    Site site = new Site("MySite", new HashMap<String, User>());
    User admin = new User("Admin", null);
    site.setAdmin(admin);
    site.getUsers().put("CEO", new User("CEO", singletonList(admin)));
    Collection<Site> sites = Arrays.asList(site, site);
    Collection<SiteView> siteViews = new HashSet<>();
    BeanCopy.copy(sites, siteViews, Site.class, SiteView.class);

    Assert.assertEquals(
        "[SiteView{name='MySite', admin=UserView{name='Admin', underHands=null}, users={CEO=UserView{name='CEO', underHands=[UserView{name='Admin', underHands=null}]}}}, SiteView{name='MySite', admin=UserView{name='Admin', underHands=null}, users={CEO=UserView{name='CEO', underHands=[UserView{name='Admin', underHands=null}]}}}]",
        siteViews.toString());
  }

  @Test
  public void testCopyMap() {
    Site site = new Site("MySite", new HashMap<String, User>());
    User admin = new User("Admin", null);
    site.setAdmin(admin);
    site.getUsers().put("CEO", new User("CEO", singletonList(admin)));
    Map<String, Site> sites = new HashMap<>();
    sites.put("Site1", site);
    sites.put("Site2", site);
    Map<String, SiteView> siteViews = new TreeMap<>();
    BeanCopy.copy(sites, siteViews, Site.class, SiteView.class);

    Assert.assertEquals(
        "{Site1=SiteView{name='MySite', admin=UserView{name='Admin', underHands=null}, users={CEO=UserView{name='CEO', underHands=[UserView{name='Admin', underHands=null}]}}}, Site2=SiteView{name='MySite', admin=UserView{name='Admin', underHands=null}, users={CEO=UserView{name='CEO', underHands=[UserView{name='Admin', underHands=null}]}}}}",
        siteViews.toString());
  }

  @Test
  public void testConverter() {
    BeanCopierRegistry.clear();
    ConverterRegistry.clear();

    // Add converters before generating any copier, otherwise converters won't take effect.
    ConverterRegistry.put(User.class.getName(), UserView.class.getName(), new Converter() {
      @Override
      public Object convert(Object from) {
        User user = (User) from;
        return new UserView(user.getName()+"-View", new ArrayList<UserView>());
      }
    });
    // This converter handles Mono<T>. Without this, the analyzer will complain it cannot handle custom generic type.
    ConverterRegistry.put(Mono.class.getName(), Mono.class.getName(), new Converter() {
      @Override
      public Object convert(Object from) {
        Mono mono = (Mono) from;
        if (mono.get().getClass().equals(Integer.class)) {
          return new Mono<>("num:" + mono.get());
        } else {
          return null;
        }
      }
    });

    Assert.assertNotNull(ConverterRegistry.find(User.class.getName(), UserView.class.getName()));

    Site site = new Site("MySite", new HashMap<String, User>());
    User admin = new User("Admin", null);
    site.setAdmin(admin);
    site.getUsers().put("CEO", new User("CEO", singletonList(admin)));
    SiteView siteView = BeanCopy.copy(site, SiteView.class);

    Assert.assertEquals("SiteView{name='MySite', admin=UserView{name='Admin-View', underHands=[]}, users={CEO=UserView{name='CEO-View', underHands=[]}}}", siteView.toString());

    Mono<Integer> monoInt = new Mono<>(42);
    Mono<String> monoStr = BeanCopy.copy(monoInt, Mono.class);
    Assert.assertEquals("Mono{t=num:42}", monoStr.toString());

    BeanCopierRegistry.clear();
    ConverterRegistry.clear();
  }

  @Test
  public void testEnumConverter() {
    ConverterRegistry.put(Gender.class.getName(), Integer.class.getName(), new Converter() {
      @Override
      public Object convert(Object from) {
        return ((Enum) from).ordinal();
      }
    });

    List<Integer> ints = new ArrayList<>();
    BeanCopy.copy(Arrays.asList(Gender.UNKNOWN, Gender.MALE, Gender.FEMALE), ints, Gender.class, Integer.class);

    Assert.assertEquals(Arrays.asList(0, 1, 2), ints);
  }

  /**
   * This feature is friendly to Continuous Integration (pre-test correctness in CI).
   */
  @Test
  public void testPreTestSuccess() {
    // Use such calls in unit tests to pre-test correctness.
    BeanCopierRegistry.findOrCreate(Site.class, SiteView.class);
    // Unfortunately this call is successful because Site and User have at least a common field `String name`
    BeanCopierRegistry.findOrCreate(Site.class, User.class);
  }

  @Test(expected = BeanAnalysisException.class)
  public void testPreTestFailure1() {
    // Object has no copyable field
    BeanCopierRegistry.findOrCreate(Object.class, Object.class);
  }

  @Test(expected = BeanAnalysisException.class)
  public void testPreTestFailure2() {
    // Site and Mono have no common fields to copy
    BeanCopierRegistry.findOrCreate(Site.class, Mono.class);
  }

  @Test(expected = BeanAnalysisException.class)
  public void testPreTestFailure3() {
    // For Map<K1, V> and Map<K2, V> , K1 should be the same or subclass of K2
    BeanCopierRegistry.findOrCreate(WrongMapA.class, WrongMapB.class);
  }
}
