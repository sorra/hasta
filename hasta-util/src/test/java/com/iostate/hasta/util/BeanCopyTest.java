package com.iostate.hasta.util;

import java.util.*;

import com.iostate.hasta.util.beans.Site;
import com.iostate.hasta.util.beans.SiteView;
import com.iostate.hasta.util.beans.User;
import com.iostate.hasta.util.beans.UserView;
import org.junit.Assert;
import org.junit.Test;

public class BeanCopyTest {
  @Test
  public void testCopySingle() throws Exception {
    BeanCopierRegistry.findOrCreate(User.class, UserView.class);
    BeanCopier beanCopier = BeanCopierRegistry.findOrCreate(Site.class, SiteView.class);

    System.out.println(beanCopier);
    System.out.println();

    Site site = new Site("MySite", new HashMap<String, User>());
    site.getUsers().put("CEO", new User("CEO", null));
    SiteView siteView = new SiteView(null, null);
    BeanCopy.copy(site, siteView);

    Assert.assertEquals("SiteView{name='MySite', users={CEO=User{name='CEO', underHands=null}}}", siteView.toString());
  }

  @Test
  public void testCopyCollection() throws Exception {
    Site site = new Site("MySite", new HashMap<String, User>());
    site.getUsers().put("CEO", new User("CEO", null));
    Collection<Site> sites = Arrays.asList(site, site);
    Collection<SiteView> siteViews = new HashSet<>();
    BeanCopy.copy(sites, siteViews, Site.class, SiteView.class);

    Assert.assertEquals(
        "[SiteView{name='MySite', users={CEO=User{name='CEO', underHands=null}}}, SiteView{name='MySite', users={CEO=User{name='CEO', underHands=null}}}]",
        siteViews.toString());
  }

  @Test
  public void testCopyMap() throws Exception {
    Site site = new Site("MySite", new HashMap<String, User>());
    site.getUsers().put("CEO", new User("CEO", null));
    Map<String, Site> sites = new HashMap<>();
    sites.put("Site1", site);
    sites.put("Site2", site);
    Map<String, SiteView> siteViews = new TreeMap<>();
    BeanCopy.copy(sites, siteViews, Site.class, SiteView.class);

    Assert.assertEquals(
        "{Site1=SiteView{name='MySite', users={CEO=User{name='CEO', underHands=null}}}, Site2=SiteView{name='MySite', users={CEO=User{name='CEO', underHands=null}}}}",
        siteViews.toString());
    System.out.println(siteViews);
  }
}
