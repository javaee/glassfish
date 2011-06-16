package org.jvnet.hk2.config.provider.internal;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.HabitatFactory;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantProviderInterceptor;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfiguredBy;
import org.jvnet.hk2.config.provider.BogusService1;
import org.jvnet.hk2.config.provider.ConfigTransaction;
import org.jvnet.hk2.config.provider.ConfigTransactionException;
import org.jvnet.hk2.config.provider.ConfigTransactionFactory;
import org.jvnet.hk2.config.provider.EjbServer;
import org.jvnet.hk2.config.provider.EjbServerConfigBean;
import org.jvnet.hk2.config.provider.JmsServer;
import org.jvnet.hk2.config.provider.JmsServer2;
import org.jvnet.hk2.config.provider.JmsServerConfigBean;
import org.jvnet.hk2.config.provider.JmsServerService;
import org.jvnet.hk2.config.provider.JmsServerService2;
import org.jvnet.hk2.config.provider.ServerService;
import org.jvnet.hk2.config.provider.SomeUnusedConfigBean;
import org.jvnet.hk2.config.provider.WebServer;
import org.jvnet.hk2.config.provider.WebServerConfigBean;
import org.jvnet.hk2.junit.Hk2Runner;
import org.jvnet.hk2.junit.Hk2RunnerOptions;

import com.sun.hk2.component.ExistingSingletonInhabitant;
import com.sun.hk2.component.Holder;

/**
 * 
 * @author Jeff Trent
 */
@RunWith(Hk2Runner.class)
@Hk2RunnerOptions(habitatFactory=ConfigTransactionProviderTest.class)
public class ConfigTransactionProviderTest implements HabitatFactory {

  @Inject
  Habitat habitat;

  @Inject
  ConfigTransactionFactory cfgTxnFactory;
  
  Inhabitant<WebServerConfigBean> webServerConfigBeanInhabitant;
  WebServerConfigBean webServerConfigBean;
  @Inject
  Holder<WebServer> webServiceHolder;

  Inhabitant<JmsServerConfigBean> jmsServerConfigBeanInhabitant;
  JmsServerConfigBean jmsServerConfigBean;
  @Inject
  Holder<JmsServer> jmsServiceHolder;
  
  Inhabitant<EjbServerConfigBean> ejbServerConfigBeanInhabitant;
  EjbServerConfigBean ejbServerConfigBean;
  @Inject
  Holder<JmsServer> ejbServiceHolder;
  
  Inhabitant<BogusService1> bogusService;

  boolean setUpCalled;
  
  
  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    setUpCalled = true;
    
    webServerConfigBeanInhabitant = habitat.getInhabitantByType(WebServerConfigBean.class);
    if (null == webServerConfigBeanInhabitant) {
      webServerConfigBeanInhabitant = habitat.getInhabitantByContract(WebServerConfigBean.class.getName(), null);
    }
    
    jmsServerConfigBeanInhabitant = habitat.getInhabitantByType(JmsServerConfigBean.class);
    if (null == jmsServerConfigBeanInhabitant) {
      jmsServerConfigBeanInhabitant = habitat.getInhabitantByContract(JmsServerConfigBean.class.getName(), null);
    }
    
    ejbServerConfigBeanInhabitant = habitat.getInhabitantByType(EjbServerConfigBean.class);
    if (null == ejbServerConfigBeanInhabitant) {
      ejbServerConfigBeanInhabitant = habitat.getInhabitantByContract(EjbServerConfigBean.class.getName(), null);
    }
    
    bogusService = habitat.getInhabitantByType(BogusService1.class);
    if (null == bogusService) {
      bogusService = habitat.getInhabitantByContract(BogusService1.class.getName(), null);
    }
  }
  
  @Override
  public Habitat newHabitat() throws ComponentException {
    Habitat h = new Habitat();
  
    InhabitantProviderInterceptor interceptor = new ConfigInhabitantProvider(h);
    h.addIndex(new ExistingSingletonInhabitant<InhabitantProviderInterceptor>(
        InhabitantProviderInterceptor.class, interceptor), 
        InhabitantProviderInterceptor.class.getName(), null);
    
    return h;
  }
  
  
  @Test
  public void sanityTest() {
    assertTrue(setUpCalled);
    
    assertEquals(null, cfgTxnFactory.getActiveTransaction(false));
    assertNotNull(cfgTxnFactory.getActiveTransaction(true));
    assertSame(cfgTxnFactory.getActiveTransaction(true), cfgTxnFactory.getActiveTransaction(true));
    assertSame(cfgTxnFactory.getActiveTransaction(true), cfgTxnFactory.getActiveTransaction(false));

    assertNull(webServerConfigBeanInhabitant);
    assertNull(jmsServerConfigBeanInhabitant);
    assertNull(ejbServerConfigBeanInhabitant);

    assertEquals("ConfiguredBy inhabitants", 1,
        habitat.getAllInhabitantsByType(WebServer.class).size());
    assertEquals("ConfiguredBy inhabitants", 1,
        habitat.getAllInhabitantsByType(JmsServer.class).size());
    assertEquals("ConfiguredBy inhabitants", 1,
        habitat.getAllInhabitantsByType(EjbServer.class).size());

    assertEquals("ConfiguredBy inhabitants", 4,
        habitat.getAllInhabitantsByContract(ConfiguredBy.class.getName()).size());
    assertEquals("ConfiguredBy inhabitants", 0,
        habitat.getAllInhabitantsByContract(JmsServerService.class.getName()).size());
    assertEquals("ConfiguredBy inhabitants", 0,
        habitat.getAllInhabitantsByContract(ServerService.class.getName()).size());
    
    // now that we verified the habitat doesn't define these, we can define them locally for testing
    webServerConfigBean = new WebServerConfigBean() {};
    assertNotNull(ReflectionHelper.annotation(webServerConfigBean, Configured.class));
    jmsServerConfigBean = new JmsServerConfigBean("Moe");
    assertNotNull(ReflectionHelper.annotation(jmsServerConfigBean, Configured.class));
    ejbServerConfigBean = createEjbServerConfigBeanForTesting();
    assertNotNull(ReflectionHelper.annotation(ejbServerConfigBean, Configured.class));
    
    try {
      fail("expected exception but got: " + webServiceHolder.get());
    } catch (ComponentException e) {
      assertTrue(e.getMessage(), e.getMessage().startsWith("improper use of " + ConfigByMetaInhabitant.class.getSimpleName()));
    }
    
    try {
      fail("expected exception but got: " + jmsServiceHolder.get());
    } catch (ComponentException e) {
      assertTrue(e.getMessage(), e.getMessage().startsWith("improper use of " + ConfigByMetaInhabitant.class.getSimpleName()));
    }
    
    try {
      fail("expected exception but got: " + ejbServiceHolder.get());
    } catch (ComponentException e) {
      assertTrue(e.getMessage(), e.getMessage().startsWith("improper use of " + ConfigByMetaInhabitant.class.getSimpleName()));
    }
    
    assertNull(bogusService);
  }

  @Test
  public void badTransactionUsage() {
    ConfigTransaction cfgTxn = cfgTxnFactory.getActiveTransaction(true);
    cfgTxn.prepare();
    try {
      cfgTxn.created(webServerConfigBean, null, null);
      fail("exception expected");
    } catch (ConfigTransactionException e) {
    }

    cfgTxn.commit();
    
    try {
      cfgTxn.commit();
      fail("exception expected");
    } catch (ConfigTransactionException e) {
    }
  }
  
  @Test
  public void badTransactionUsage2() {
    ConfigTransaction cfgTxn = cfgTxnFactory.getActiveTransaction(true);
    try {
      Object obj = new SomeUnusedConfigBean() {};

      cfgTxn.created(obj, null, null);

      try {
        cfgTxn.created(obj, null, null);
        fail("exception expected");
      } catch (ConfigTransactionException e) {
      }
      
      try {
        cfgTxn.updated(obj, null);
        fail("exception expected");
      } catch (ConfigTransactionException e) {
      }
      
      try {
        cfgTxn.deleted(obj);
        fail("exception expected");
      } catch (ConfigTransactionException e) {
      }
      
      cfgTxn.prepare();
    } finally {
      cfgTxn.rollback();
    }
  }
  
  @Test
  public void simpleConfigTransaction() {
    ConfigTransaction cfgTxn = cfgTxnFactory.getActiveTransaction(true);
    cfgTxn.created(ejbServerConfigBean, "rc", /*metadata*/null);
    cfgTxn.created(webServerConfigBean, "coke", /*metadata*/null);
    cfgTxn.created(jmsServerConfigBean, null, /*metadata*/null);

    assertEquals("ConfiguredBy inhabitants", 1,
        habitat.getAllInhabitantsByType(WebServer.class).size());
    assertEquals("ConfiguredBy inhabitants", 1,
        habitat.getAllInhabitantsByType(JmsServer.class).size());
    assertEquals("ConfiguredBy inhabitants", 1,
        habitat.getAllInhabitantsByType(EjbServer.class).size());
    
    assertEquals("ConfiguredBy inhabitants", 4,
        habitat.getAllInhabitantsByContract(ConfiguredBy.class.getName()).size());
    assertEquals("ConfiguredBy inhabitants", 0,
        habitat.getAllInhabitantsByContract(JmsServerService.class.getName()).size());
    assertEquals("ConfiguredBy inhabitants", 0,
        habitat.getAllInhabitantsByContract(ServerService.class.getName()).size());
    
    // verify that JmsServer was not yet registered by name
    Inhabitant<JmsServerService> jmsServer = habitat.getInhabitant(JmsServerService.class, "Moe");
    assertNull(jmsServer);

    Inhabitant<JmsServerService2> jmsServer2 = habitat.getInhabitant(JmsServerService2.class, "Moe");
    assertNull(jmsServer2);
    
    assertEquals(0, JmsServer.constructCount);
    assertEquals(0, JmsServer.destroyCount);
    assertEquals(0, JmsServer2.constructCount);
    assertEquals(0, JmsServer2.destroyCount);
    assertEquals(0, WebServer.constructCount);
    assertEquals(0, WebServer.destroyCount);
    
    assertEquals("Configured inhabitants", 0,
        habitat.getAllInhabitantsByContract(EjbServerConfigBean.class.getName()).size());
    assertEquals("Configured inhabitants", 0,
        habitat.getAllInhabitantsByContract(WebServerConfigBean.class.getName()).size());
    assertEquals("Configured inhabitants", 0,
        habitat.getAllInhabitantsByContract(JmsServerConfigBean.class.getName()).size());

    // commit the txn
    cfgTxn.commit();

    //
    // Config instances should not be made available by type!
    //
    assertEquals("ConfiguredBy inhabitants", 1,
        habitat.getAllInhabitantsByType(WebServer.class).size());
    assertEquals("ConfiguredBy inhabitants", 1,
        habitat.getAllInhabitantsByType(JmsServer.class).size());
    assertEquals("ConfiguredBy inhabitants", 1,
        habitat.getAllInhabitantsByType(EjbServer.class).size());
    
    assertEquals("ConfiguredBy inhabitants after commit", 4,
        habitat.getAllInhabitantsByContract(ConfiguredBy.class.getName()).size());
    assertEquals("ConfiguredBy inhabitants after commit", 1,
        habitat.getAllInhabitantsByContract(JmsServerService.class.getName()).size());
    assertEquals("ConfiguredBy inhabitants after commit", 3,
        habitat.getAllInhabitantsByContract(ServerService.class.getName()).size());
    
    cfgTxn = cfgTxnFactory.getActiveTransaction(false);
    assertNull(cfgTxn);
    
    // verify name resolution
    jmsServer = habitat.getInhabitant(JmsServerService.class, "Moe");
    assertNotNull(jmsServer);
    assertTrue(jmsServer.isActive());
    assertNotNull(jmsServer.get());

    jmsServer2 = habitat.getInhabitant(JmsServerService2.class, "Moe");
    assertNotNull(jmsServer2);
    assertTrue(jmsServer2.isActive());
    assertNotNull(jmsServer2.get());
    
    assertNotSame(jmsServer.get(), jmsServer2.get());

    assertEquals(1, JmsServer.constructCount);
    assertEquals(0, JmsServer.destroyCount);
    assertEquals(1, JmsServer2.constructCount);
    assertEquals(0, JmsServer2.destroyCount);
    assertEquals(1, WebServer.constructCount);
    assertEquals(0, WebServer.destroyCount);

    assertEquals("Configured inhabitants", 1,
        habitat.getAllInhabitantsByContract(EjbServerConfigBean.class.getName()).size());
    assertEquals("Configured inhabitants", 1,
        habitat.getAllInhabitantsByContract(WebServerConfigBean.class.getName()).size());
    assertEquals("Configured inhabitants", 1,
        habitat.getAllInhabitantsByContract(JmsServerConfigBean.class.getName()).size());

    assertNotNull("Configured inhabitants", 
        habitat.getInhabitant(JmsServerConfigBean.class, "Moe"));
    assertSame("Configured inhabitants", jmsServerConfigBean,
        habitat.getInhabitant(JmsServerConfigBean.class, "Moe").get());
    
    WebServer webServer = (WebServer) habitat.getComponent(ServerService.class, "coke");
    assertNotNull(webServer);
    assertSame(webServerConfigBean, webServer.webServerConfigBean);
  }
  
  @Test
  public void undoSimpleConfigTransaction() {
    ConfigTransaction cfgTxn = cfgTxnFactory.getActiveTransaction(true);
    cfgTxn.deleted(ejbServerConfigBean);
    cfgTxn.deleted(webServerConfigBean);
    cfgTxn.deleted(jmsServerConfigBean);

    assertEquals("ConfiguredBy inhabitants", 1,
        habitat.getAllInhabitantsByType(WebServer.class).size());
    assertEquals("ConfiguredBy inhabitants", 1,
        habitat.getAllInhabitantsByType(JmsServer.class).size());
    assertEquals("ConfiguredBy inhabitants", 1,
        habitat.getAllInhabitantsByType(EjbServer.class).size());
    
    assertEquals("ConfiguredBy inhabitants", 4,
        habitat.getAllInhabitantsByContract(ConfiguredBy.class.getName()).size());
    assertEquals("ConfiguredBy inhabitants", 1,
        habitat.getAllInhabitantsByContract(JmsServerService.class.getName()).size());
    assertEquals("ConfiguredBy inhabitants", 3,
        habitat.getAllInhabitantsByContract(ServerService.class.getName()).size());
    
    cfgTxn.commit();

    //
    // Config instances should not be made available by type!
    //
    assertEquals("ConfiguredBy inhabitants", 1,
        habitat.getAllInhabitantsByType(WebServer.class).size());
    assertEquals("ConfiguredBy inhabitants", 1,
        habitat.getAllInhabitantsByType(JmsServer.class).size());
    assertEquals("ConfiguredBy inhabitants", 1,
        habitat.getAllInhabitantsByType(EjbServer.class).size());
    
    assertEquals("ConfiguredBy inhabitants after commit", 4,
        habitat.getAllInhabitantsByContract(ConfiguredBy.class.getName()).size());
    assertEquals("ConfiguredBy inhabitants after commit", 0,
        habitat.getAllInhabitantsByContract(JmsServerService.class.getName()).size());
    assertEquals("ConfiguredBy inhabitants after commit", 0,
        habitat.getAllInhabitantsByContract(ServerService.class.getName()).size());
    
    cfgTxn = cfgTxnFactory.getActiveTransaction(false);
    assertNull(cfgTxn);

    // verify name resolution
    Inhabitant<JmsServerService> jmsServer = habitat.getInhabitant(JmsServerService.class, "Moe");
    assertNull(jmsServer);
    
    assertEquals(1, JmsServer.constructCount);
    assertEquals(1, JmsServer.destroyCount);
    assertEquals(1, JmsServer2.constructCount);
    assertEquals(1, JmsServer2.destroyCount);
    assertEquals(1, WebServer.constructCount);
    assertEquals(1, WebServer.destroyCount);
    
    assertEquals("Configured inhabitants", 0,
        habitat.getAllInhabitantsByContract(EjbServerConfigBean.class.getName()).size());
    assertEquals("Configured inhabitants", 0,
        habitat.getAllInhabitantsByContract(WebServerConfigBean.class.getName()).size());
    assertEquals("Configured inhabitants", 0,
        habitat.getAllInhabitantsByContract(JmsServerConfigBean.class.getName()).size());
  }
  
  /**
   * Verifies that a replay of the above undo CL causes an exception since the beans are no longer under management
   */
  @Test
  public void secondUndoSimpleConfigTransaction() {
    ConfigTransaction cfgTxn = cfgTxnFactory.getActiveTransaction(true);
    try {
      cfgTxn.deleted(ejbServerConfigBean);
      fail("expected failure since bean is not under management: " + ejbServerConfigBean);
    } catch (ConfigTransactionException e) {
      assertTrue(e.getMessage(), e.getMessage().contains(" is not being tracked"));
    }

    try {
      cfgTxn.deleted(webServerConfigBean);
      fail("expected failure since bean is not under management: " + webServerConfigBean);
    } catch (ConfigTransactionException e) {
      assertTrue(e.getMessage(), e.getMessage().contains(" is not being tracked"));
    }

    try {
      cfgTxn.deleted(jmsServerConfigBean);
      fail("expected failure since bean is not under management: " + jmsServerConfigBean);
    } catch (ConfigTransactionException e) {
      assertTrue(e.getMessage(), e.getMessage().contains(" is not being tracked"));
    }
  }
  


  private static EjbServerConfigBean createEjbServerConfigBeanForTesting() {
    Class<?> classes[] = new Class<?>[] {EjbServerConfigBean.class};
    final EjbServerConfigBean delegate = new EjbServerConfigBean() {};
    EjbServerConfigBean fake = (EjbServerConfigBean) Proxy.newProxyInstance(
        ConfigTransactionProviderTest.class.getClassLoader(), 
        classes, 
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method m, Object[] margs) throws Throwable {
//            System.out.println(m);
            Object val = m.invoke(delegate, margs);
//            System.out.println("\t" + val);
            return val;
          }
    });
    return fake;
  }

  
}
