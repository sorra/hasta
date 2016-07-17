package com.iostate.hasta.util;

import java.util.*;

import com.iostate.hasta.util.beans.*;
import org.junit.Assert;
import org.junit.Test;

import static java.util.Collections.singletonList;

public class BeanCopyTest {
  @Test
  public void testCopySingle() throws Exception {
    Site site = new Site("MySite", new HashMap<String, User>());
    User admin = new User("Admin", null);
    site.setAdmin(admin);
    site.getUsers().put("CEO", new User("CEO", singletonList(admin)));
    SiteView siteView = BeanCopy.copy(site, SiteView.class);

    // If there is a converter of SiteView->SiteView, this converter has no effect for siteView0
    SiteView siteView0 = new SiteView(null, null);
    BeanCopy.copy(site, siteView0);

    Assert.assertEquals("SiteView{name='MySite', admin=UserView{name='Admin', underHands=null}, users={CEO=UserView{name='CEO', underHands=[UserView{name='Admin', underHands=null}]}}}", siteView.toString());
    Assert.assertEquals(siteView.toString(), siteView0.toString());
  }

  @Test
  public void testCopyCollection() throws Exception {
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
  public void testCopyMap() throws Exception {
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
  public void testConverter() throws Exception {
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
}
