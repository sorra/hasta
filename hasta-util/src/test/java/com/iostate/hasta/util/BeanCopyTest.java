package com.iostate.hasta.util;

import java.util.*;

import com.iostate.hasta.util.beans.Site;
import com.iostate.hasta.util.beans.SiteView;
import com.iostate.hasta.util.beans.User;
import com.iostate.hasta.util.beans.UserView;
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
    SiteView siteView = new SiteView(null, null);
    BeanCopy.copy(site, siteView);

    Assert.assertEquals("SiteView{name='MySite', admin=UserView{name='Admin', underHands=null}, users={CEO=UserView{name='CEO', underHands=[UserView{name='Admin', underHands=null}]}}}", siteView.toString());
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
    System.out.println(siteViews);
  }

  @Test
  public void testConverter() throws Exception {
    BeanCopierRegistry.clear();
    ConverterRegistry.clear();

    // Add converter before creating copiers
    ConverterRegistry.put(User.class.getName(), UserView.class.getName(), new Converter() {
      @Override
      public Object convert(Object from) {
        User user = (User) from;
        return new UserView(user.getName()+"-View", new ArrayList<UserView>());
      }
    });

    Assert.assertNotNull(ConverterRegistry.find(User.class.getName(), UserView.class.getName()));

    Site site = new Site("MySite", new HashMap<String, User>());
    User admin = new User("Admin", null);
    site.setAdmin(admin);
    site.getUsers().put("CEO", new User("CEO", singletonList(admin)));
    SiteView siteView = new SiteView(null, null);
    BeanCopy.copy(site, siteView);

    Assert.assertEquals("SiteView{name='MySite', admin=UserView{name='Admin-View', underHands=[]}, users={CEO=UserView{name='CEO-View', underHands=[]}}}", siteView.toString());

    BeanCopierRegistry.clear();
    ConverterRegistry.clear();
  }
}
