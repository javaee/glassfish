package org.jvnet.hk2.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.junit.Hk2Runner;
import org.jvnet.hk2.test.contracts.DummyContract;
import org.jvnet.hk2.test.contracts.ErrorThrowingContract;
import org.jvnet.hk2.test.contracts.Simple;
import org.jvnet.hk2.test.contracts.SimpleGetter;
import org.jvnet.hk2.test.impl.OneSimple;
import org.jvnet.hk2.test.impl.PerLookupService;
import org.jvnet.hk2.test.impl.PerLookupServiceNested1;
import org.jvnet.hk2.test.impl.PerLookupServiceNested2;
import org.jvnet.hk2.test.impl.PerLookupServiceNested3;
import org.jvnet.hk2.test.impl.RandomService;
import org.jvnet.hk2.test.impl.RandomSimpleService;
import org.jvnet.hk2.test.impl.TwoSimple;

import com.sun.hk2.component.Holder;
import com.sun.hk2.component.InjectInjectionResolver;
import com.sun.hk2.component.LazyInhabitant;

/**
 * Tests Extended Injection point capabilities.
 * 
 * @author Jeff Trent
 */
@RunWith(Hk2Runner.class)
public class ExtendedInjectTest {

  @Inject
  Habitat h;
  
  // injection by type - #1a
  @Inject
  PerLookupService perLookupService;

  // injection by type - #1b
  @Inject(optional = true)
  PerLookupService perLookupServiceOptional;

  // injection by contract - #1a
  @Inject
  RandomService singletonService;

  // injection by type - #1b
  @Inject(optional = true)
  RandomService singletonServiceOptional;

  @Inject
  Holder<RandomService> singletonServiceHolder;

  @Inject(optional = true)
  DummyContract neverService;

  // injection by name and contract - #1a
  @Inject(name = "one")
  Simple oneSingletonService;

  // injection by name and contract - #1b
  @Inject(name = "one")
  Simple oneSingletonServiceCopy;

  // injection by name and contract - #2a
  @Inject(name = "two")
  Simple twoPerLookupService;

  // injection by name and contract - #2b
  @Inject(name = "two")
  Simple twoPerLookupServiceCopy;

  @Inject(optional = true)
  SimpleGetter simpleOptionalServiceWithADependency;

  // These are NOT annotated at field level, but is at method level
  Simple setterOneSimple;
  Simple setterOtherSimple;
  
  @Inject(name = "one")
  void setOneSimple(Simple simple) {
    assertNull(setterOneSimple);
    setterOneSimple = simple;
  }

  @SuppressWarnings("unused")
  @Inject(name = "other")
  private void setOtherSimple(Simple simple) {
    assertNull(setterOtherSimple);
    setterOtherSimple = simple;
  }

  @BeforeClass
  public static void reset() {
    PerLookupService.constructs = 0;
    PerLookupService.destroys = 0;
    
    PerLookupServiceNested1.constructs = 0;
    PerLookupServiceNested1.destroys = 0;
    
    PerLookupServiceNested2.constructs = 0;
    PerLookupServiceNested2.destroys = 0;
    
    PerLookupServiceNested3.constructs = 0;
    PerLookupServiceNested3.destroys = 0;
    
    OneSimple.constructs = 0;
    OneSimple.destroys = 0;

    TwoSimple.constructs = 0;
    TwoSimple.destroys = 0;

    RandomSimpleService.constructs = 0;
    RandomSimpleService.destroys = 0;
  }
  
  @Test
  public void byType_perLookupScoped() {
    assertNotNull(perLookupService);
    assertNotNull(perLookupServiceOptional);
    assertNotSame(perLookupService, perLookupServiceOptional);
  }

  @Test
  public void byType_perSingletonScoped() {
    assertNotNull(singletonService);
    assertSame(singletonService, singletonServiceOptional);
  }

  @Test
  public void byContract_perLookupScoped() {
    assertNotNull(twoPerLookupService);
    assertNotNull(twoPerLookupServiceCopy);
    assertNotSame(twoPerLookupService, twoPerLookupServiceCopy);
  }

  @Test
  public void byContract_perSingletonScoped() {
    assertNotNull(oneSingletonService);
    assertSame(oneSingletonService, oneSingletonServiceCopy);
  }

  @Test
  public void simpleOptionalServiceHavingADependent() {
    assertNotNull(simpleOptionalServiceWithADependency);
    assertEquals(RandomSimpleService.class, simpleOptionalServiceWithADependency.getClass());
    assertNotNull(simpleOptionalServiceWithADependency.getSimple());
    assertEquals("one", simpleOptionalServiceWithADependency.getSimple().get());
  }
  
  @Test
  public void byContract_neverSatisfied() {
    assertNull(neverService);
  }

  // TODO: see simpleGetterServiceHolderOptional above
  @Test
  public void holder() {
    assertNotNull(singletonServiceHolder);
    assertNotNull(singletonServiceHolder.get());
    assertSame(singletonService, singletonServiceHolder.get());
  }
  
  @Test
  public void setterMethod() {
    assertNotNull(setterOneSimple);
    assertEquals(setterOneSimple.get(), "one");

    assertNotNull(setterOtherSimple);
    assertEquals(setterOtherSimple.get(), "other");
  }

  /**
   * Verifies the affect of exceptions during the injection phase.  The
   * expectation is that inhabitant should NOT be activated in such
   * circumstances.
   */
  @Test
  public void errorThrowingSetterMethod() {
    Collection<Inhabitant<?>> iColl = 
        h.getAllInhabitantsByContract(ErrorThrowingContract.class.getName());
    assertNotNull(iColl);
    assertEquals(1, iColl.size());
    Inhabitant<?> i = iColl.iterator().next();
    
    LogHandler handler = new LogHandler();
    Logger logger = Logger.getLogger(LazyInhabitant.class.getName());
    logger.addHandler(handler);
    try {
      Object obj = i.get();
      fail("Exception expected but instead got: " + obj);
    } catch (ComponentException e) {
      // expected
//      e.printStackTrace();
    } finally {
      logger.removeHandler(handler);
    }
    assertFalse("shouldn't be instantiated - it's in a bad state", i.isInstantiated());

    // TODO: do logging (in the dynamic work)
//    assertEquals("log records: " + handler.publishedRecords, 1, handler.publishedRecords.size());
//    LogRecord lr = handler.publishedRecords.get(0);
//    assertEquals("log record: " + lr, Level.WARNING, lr.getLevel());
//    assertTrue("log record: " + lr, lr.getMessage().contains("Failed to activate inhabitant"));
//    assertNotNull("log record: " + lr, lr.getThrown());
  }

  /**
   * Verifies the affect of exceptions during the injection phase.  The
   * expectation is that the dependent inhabitant should NOT be 
   * wired / activated in such circumstances.
   */
  @Test
  public void errorThrowingInDI() {
    Inhabitant<?> iets = 
      h.getInhabitant(ErrorThrowingContract.class, null);
    assertNotNull(iets);
    assertFalse(iets.isInstantiated());
    
    Inhabitant<?> ietds = 
      h.getInhabitant(Simple.class, "ErrorThrowingDependentService");
    assertNotNull(ietds);
    assertNotSame(iets, ietds);
    assertFalse(ietds.isInstantiated());
  
    LogHandler handler = new LogHandler();
    Logger logger = Logger.getLogger(LazyInhabitant.class.getName());
    logger.addHandler(handler);
    
    try {
      Simple simple = h.getComponent(Simple.class, "ErrorThrowingDependentService");
      fail("Expected unsatisfied dependencies exception but got: " + simple);
    } catch (Exception e) {
      // expected
      assertEquals("exception type", ComponentException.class, e.getClass());
      assertEquals("message", "injection failed on org.jvnet.hk2.test.impl.ErrorThrowingDependentService.errorThrowing with interface org.jvnet.hk2.test.contracts.ErrorThrowingContract", e.getLocalizedMessage());
      Throwable e2 = e.getCause();
      assertEquals("exception 2 type", ComponentException.class, e2.getClass());
      assertEquals("message 2", "injection failed on void org.jvnet.hk2.test.impl.ErrorThrowingService.fakeRandomContractThrowingUp(org.jvnet.hk2.test.runlevel.RandomContract)", e2.getLocalizedMessage());
    }
    
    assertFalse(iets.isInstantiated());
    assertFalse(ietds.isInstantiated());

    // TODO: do logging (in the dynamic work)
//    assertEquals("log records: " + handler.publishedRecords, 2, handler.publishedRecords.size());
//    for (LogRecord lr : handler.publishedRecords) {
//      assertEquals("log record: " + lr, Level.WARNING, lr.getLevel());
//      assertTrue("log record: " + lr, lr.getMessage().contains("Failed to activate inhabitant"));
//      assertNotNull("log record: " + lr, lr.getThrown());
//    }
  }

  /**
   * Testing the nature of the scoped clones on PerLookup, byType services wrt PostConstruct and PreDestroy
   */
//  @Ignore
  @Test
  public void managedip_perLookupByType_scopedClone_PostConstructAndPreDestroys() {
    assertEquals("PostConstruct(s)", 2, PerLookupService.constructs);
    assertEquals("PostConstruct(s)", 2, PerLookupServiceNested1.constructs);
    assertEquals("PostConstruct(s)", 2, PerLookupServiceNested2.constructs);
    assertEquals("PostConstruct(s)", 2, PerLookupServiceNested3.constructs);
    assertEquals("PreDestroy(s)", 0, PerLookupService.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested1.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested2.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested3.destroys);

    Inhabitant<PerLookupService> i = h.getInhabitantByType(PerLookupService.class).scopedClone();
    assertEquals("PostConstruct(s)", 2, PerLookupService.constructs);
    assertEquals("PostConstruct(s)", 2, PerLookupServiceNested1.constructs);
    assertEquals("PostConstruct(s)", 2, PerLookupServiceNested2.constructs);
    assertEquals("PostConstruct(s)", 2, PerLookupServiceNested3.constructs);
    
    PerLookupService pls = i.get(); // refCount now 1
    assertNotNull(pls);
    assertEquals("PostConstruct(s)", 3, PerLookupService.constructs);
    assertEquals("PostConstruct(s)", 3, PerLookupServiceNested1.constructs);
    assertEquals("PostConstruct(s)", 3, PerLookupServiceNested2.constructs);
    assertEquals("PostConstruct(s)", 3, PerLookupServiceNested3.constructs);
    assertEquals("PreDestroy(s)", 0, PerLookupService.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested1.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested2.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested3.destroys);

    PerLookupService pls2 = i.get(); // refCount now 2
    assertNotNull(pls2);
    assertSame(pls, pls2);
    assertEquals("PostConstruct(s) should not change since this is the same inhabitant", 3, PerLookupService.constructs);
    assertEquals("PostConstruct(s)", 3, PerLookupServiceNested1.constructs);
    assertEquals("PostConstruct(s)", 3, PerLookupServiceNested2.constructs);
    assertEquals("PostConstruct(s)", 3, PerLookupServiceNested3.constructs);
    assertEquals("PreDestroy(s)", 0, PerLookupService.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested1.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested2.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested3.destroys);

    i.release(); // refCount now 1
    assertEquals("PostConstruct(s)", 3, PerLookupService.constructs);
    assertEquals("PostConstruct(s)", 3, PerLookupServiceNested1.constructs);
    assertEquals("PostConstruct(s)", 3, PerLookupServiceNested2.constructs);
    assertEquals("PostConstruct(s)", 3, PerLookupServiceNested3.constructs);
    assertEquals("PreDestroy(s) after 1 release", 0, PerLookupService.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested1.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested2.destroys);
    assertEquals("PreDestroy(s)", 0, PerLookupServiceNested3.destroys);

    i.release(); // refCount now 0
    assertEquals("PostConstruct(s)", 3, PerLookupService.constructs);
    assertEquals("PostConstruct(s)", 3, PerLookupServiceNested1.constructs);
    assertEquals("PostConstruct(s)", 3, PerLookupServiceNested2.constructs);
    assertEquals("PostConstruct(s)", 3, PerLookupServiceNested3.constructs);
    assertEquals("PreDestroy(s) after 2 releases", 1, PerLookupService.destroys);
    if (InjectInjectionResolver.MANAGED_ENABLED) {
      assertEquals("PreDestroy(s) after 2 releases (perLookup scoped should be released)", 1, PerLookupServiceNested1.destroys);
      assertEquals("PreDestroy(s) after 2 releases (perLookup scoped should be released)", 1, PerLookupServiceNested2.destroys);
      assertEquals("PreDestroy(s) after 2 releases (perLookup scoped should be released)", 1, PerLookupServiceNested3.destroys);
    } else {  // old behavior
      assertEquals("PreDestroy(s) after 2 releases", 0, PerLookupServiceNested1.destroys);
      assertEquals("PreDestroy(s) after 2 releases", 0, PerLookupServiceNested2.destroys);
      assertEquals("PreDestroy(s) after 2 releases", 0, PerLookupServiceNested3.destroys);
    }
  }
  
  /**
   * Testing the nature of the scoped clones on Singleton, byName services wrt PostConstruct and PreDestroy
   */
  @Test
  @SuppressWarnings("unchecked")
  public void managedip_singletonByName_scopedClone_PostConstructAndPreDestroys() {
    assertEquals("PostConstruct(s)", 1, OneSimple.constructs);
    assertEquals("PostConstruct(s) of a nested PerLookup", 3, TwoSimple.constructs);
    assertEquals("PreDestroy(s)", 0, OneSimple.destroys);
    assertEquals("PreDestroy(s) of a nested PerLookup", 0, TwoSimple.destroys);

    Inhabitant<Simple> i = h.getInhabitantByContract(Simple.class.getName(), "one").scopedClone();
    assertEquals("PostConstruct(s)", 1, OneSimple.constructs);
    assertEquals("PostConstruct(s) of a nested PerLookup", 3, TwoSimple.constructs);
    
    Simple pls = i.get();
    assertNotNull(pls);
    assertEquals("PostConstruct(s)", 1, OneSimple.constructs);
    assertEquals("PostConstruct(s) of a nested PerLookup", 3, TwoSimple.constructs);
    assertEquals("PreDestroy(s)", 0, OneSimple.destroys);
    assertEquals("PreDestroy(s) of a nested PerLookup", 0, TwoSimple.destroys);

    Simple pls2 = i.get();
    assertNotNull(pls2);
    assertSame(pls, pls2);
    assertEquals("PostConstruct(s) should not change since this is the same inhabitant", 1, OneSimple.constructs);
    assertEquals("PostConstruct(s) of a nested PerLookup", 3, TwoSimple.constructs);
    assertEquals("PreDestroy(s)", 0, OneSimple.destroys);
    assertEquals("PreDestroy(s) of a nested PerLookup", 0, TwoSimple.destroys);

    i.release();
    assertEquals("PostConstruct(s)", 1, OneSimple.constructs);
    assertEquals("PostConstruct(s) of a nested PerLookup", 3, TwoSimple.constructs);
    assertEquals("PreDestroy(s) after 1 release", 0, OneSimple.destroys);
    assertEquals("PreDestroy(s) of a nested PerLookup", 0, TwoSimple.destroys);

    i.release();
    assertEquals("PostConstruct(s)", 1, OneSimple.constructs);
    assertEquals("PostConstruct(s) of a nested PerLookup", 3, TwoSimple.constructs);
    assertEquals("PreDestroy(s) after 2 releases (Singleton scoped should not be released)", 0, OneSimple.destroys);
    assertEquals("PreDestroy(s) after 2 releases", 0, TwoSimple.destroys);
  }

  /**
   * Testing the nature of the scoped clones on Singleton, byContract services wrt PostConstruct and PreDestroy
   */
  @Test
  @SuppressWarnings("unchecked")
  public void managedip_singletonByContract_scopedClone_PostConstructAndPreDestroys() {
    assertEquals("PostConstruct(s)", 1, RandomSimpleService.constructs);
    assertEquals("PreDestroy(s)", 0, RandomSimpleService.destroys);

    // TODO: we should be able to use RandomContract here as well since it's in the parent chain!! Bug?
//    Inhabitant<RandomContract> i = h.getInhabitantByContract(RandomContract.class.getName(), null).scopedClone();
    Inhabitant<SimpleGetter> i = h.getInhabitantByContract(SimpleGetter.class.getName(), null).scopedClone();
    assertEquals("PostConstruct(s)", 1, RandomSimpleService.constructs);
    
//    RandomContract pls = i.get();
    SimpleGetter pls = i.get();
    assertNotNull(pls);
    assertEquals("PostConstruct(s)", 1, RandomSimpleService.constructs);
    assertEquals("PreDestroy(s)", 0, RandomSimpleService.destroys);

//    RandomContract pls2 = i.get();
    SimpleGetter pls2 = i.get();
    assertNotNull(pls2);
    assertSame(pls, pls2);
    assertEquals("PostConstruct(s) should not change since this is the same inhabitant", 1, RandomSimpleService.constructs);
    assertEquals("PreDestroy(s)", 0, RandomSimpleService.destroys);

    i.release();
    assertEquals("PostConstruct(s)", 1, RandomSimpleService.constructs);
    assertEquals("PreDestroy(s) after 1 release", 0, RandomSimpleService.destroys);

    i.release();
    assertEquals("PostConstruct(s)", 1, RandomSimpleService.constructs);
    assertEquals("PreDestroy(s) after 1 release", 0, RandomSimpleService.destroys);
  }
  
  /**
   * Continued testing after Hk2Runner releases us 
   */
  @AfterClass
  public static void managedip_perLookupByType_PreDestroysShouldBeCalledInAfterClass() {
    if (InjectInjectionResolver.MANAGED_ENABLED) {
      assertEquals("PostConstruct(s)", 3, PerLookupService.constructs);
      assertEquals("PostConstruct(s)", 3, PerLookupServiceNested1.constructs);
      assertEquals("PostConstruct(s)", 3, PerLookupServiceNested2.constructs);
      assertEquals("PostConstruct(s)", 3, PerLookupServiceNested3.constructs);
      assertEquals("PreDestroy(s)", 3, PerLookupService.destroys);
      assertEquals("PreDestroy(s)", 3, PerLookupServiceNested1.destroys);
      assertEquals("PreDestroy(s)", 3, PerLookupServiceNested2.destroys);
      assertEquals("PreDestroy(s)", 3, PerLookupServiceNested3.destroys);
    } else {
      assertEquals("PostConstruct(s)", 3, PerLookupService.constructs);
      assertEquals("PreDestroy(s) after 2 releases", 3, PerLookupServiceNested1.constructs);
      assertEquals("PreDestroy(s)", 1, PerLookupService.destroys);
      assertEquals("PreDestroy(s)", 0, PerLookupServiceNested1.destroys);
      assertEquals("PreDestroy(s)", 0, PerLookupServiceNested2.destroys);
      assertEquals("PreDestroy(s)", 0, PerLookupServiceNested3.destroys);
    }
  }
  
  /**
   * Continued testing after Hk2Runner releases us 
   */
  @AfterClass
  public static void managedip_singletonByName_PreDestroysShouldBeCalledInAfterClass() {
    assertEquals("PostConstruct(s)", 1, OneSimple.constructs);
    assertEquals("PostConstruct(s) of a nested PerLookup", 3, TwoSimple.constructs);
    assertEquals("PreDestroy(s)", 0, OneSimple.destroys);
    if (InjectInjectionResolver.MANAGED_ENABLED) {
      assertEquals("PreDestroy(s) of a nested PerLookup", 2, TwoSimple.destroys);
    } else {
      assertEquals("PreDestroy(s) of a nested PerLookup", 0, TwoSimple.destroys);
    }
  }

  /**
   * Continued testing after Hk2Runner releases us 
   */
  @AfterClass
  public static void managedip_singletonByContract_PreDestroysShouldBeCalledInAfterClass() {
    assertEquals("PostConstruct(s)", 1, RandomSimpleService.constructs);
    assertEquals("PreDestroy(s)", 0, RandomSimpleService.destroys);
  }
  
  
  static class LogHandler extends Handler {
    final ArrayList<LogRecord> publishedRecords = new ArrayList<LogRecord>();
    
    @Override
    public void close() throws SecurityException {
    }

    @Override
    public void flush() {
    }

    @Override
    public void publish(LogRecord lr) {
      publishedRecords.add(lr);
    }
  }

}
