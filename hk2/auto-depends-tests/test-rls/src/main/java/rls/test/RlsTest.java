package rls.test;

import java.util.Collection;

import static junit.framework.Assert.*;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;

import rls.test.infra.MultiThreadedInhabitantActivator;
import rls.test.infra.RandomInhabitantSorter;
import rls.test.model.ContractX;
import rls.test.model.ContractY;
import rls.test.model.ServiceBaseX;
import rls.test.model.ServiceDerivedX;
import rls.test.model.ServiceOtherToY;
import rls.test.model.ServiceZ;

import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.hk2.component.Holder;

@Service
public class RlsTest implements ModuleStartup {

  @Inject static Habitat h;
//  @Inject(optional=true) static ContractY y;
//  @Inject(optional=true) static ServiceOtherToY other;
  @Inject static Holder<ServiceZ> zHolder;
  
  @Override
  public void setStartupContext(StartupContext context) {
  }

  @Override
  public void start() {
    runTests();
  }

  @Override
  public void stop() {
  }

  public static void runTests() {
    assert h.isInitialized() : "Sanity check";
    
    assertTrue("Sorter should be called", RandomInhabitantSorter.called);
    assertTrue("Activator should be called", MultiThreadedInhabitantActivator.called);
    
//    assertNull("can't support dependencies to a non RLS", y);
//    assertNull("can't support dependencies to a non RLS", other);

    try {
      Thread.currentThread().sleep(250);
    } catch (InterruptedException e) {
      fail(e.getMessage());
    }
    
    Collection<Inhabitant<?>> coll = h.getAllInhabitantsByContract(ContractX.class.getName());
    assertEquals("ContractX service count: " + coll, 2, coll.size());
    mustBeActive(coll, true);

    coll = h.getAllInhabitantsByContract(RunLevel.class.getName());
    assertEquals("ContractX service count: " + coll, 5, coll.size());
    mustBeActive(coll, true);

    verifyServiceBaseX();
    verifyServiceDerivedX();
    verifyServiceY();
    verifyServiceZ();
  }

  @SuppressWarnings("static-access")
  private static void verifyServiceBaseX() {
    assertEquals("ctor count (one for base one for derived thru base)", 
        2, ServiceBaseX.ctorCount);
    assertEquals("postConstruct count (one for base one for derived thru base)", 
        2, ServiceBaseX.postConstructCount);
    assertNotNull(ServiceBaseX.y);
    assertNotNull(ServiceBaseX.other);
    assertSame(ServiceBaseX.y, ServiceBaseX.other.y);
  }

  private static void verifyServiceDerivedX() {
    assertEquals("ctor count", 
        1, ServiceDerivedX.ctorCount);
    assertEquals("postConstruct count", 
        1, ServiceDerivedX.postConstructCount);
    assertNotNull(ServiceDerivedX.y);
  }

  private static void verifyServiceY() {
    assertEquals("ctor count", 
        1, ServiceOtherToY.ctorCount);
    assertEquals("postConstruct count", 
        1, ServiceOtherToY.postConstructCount);
    assertNotNull("other.y", ServiceOtherToY.y);
    assertNotNull("other.allY", ServiceOtherToY.allY);
    assertEquals("other.allY count", 3, ServiceOtherToY.allY.length);
    Collection<Inhabitant<?>> coll = h.getAllInhabitantsByContract(ContractY.class.getName());
    assertEquals("ContractY service count: " + coll, 3, coll.size());
    mustBeActive(coll, true);
    assertNotNull("other.zHolder", ServiceOtherToY.zHolder);
  }

  private static void verifyServiceZ() {
    assertNotNull("holder to z", zHolder);
    
    assertEquals("ctor count", 
        0, ServiceZ.ctorCount);
    assertEquals("postConstruct count", 
        0, ServiceZ.postConstructCount);
    assertNotNull(zHolder.get());
    assertSame(zHolder.get(), zHolder.get());
    assertEquals("ctor count", 
        1, ServiceZ.ctorCount);
    assertEquals("postConstruct count", 
        1, ServiceZ.postConstructCount);

    assertSame("other.zHolder", zHolder.get(), ServiceOtherToY.zHolder.get());
  }
  
  private static void mustBeActive(Collection<Inhabitant<?>> coll, boolean expectActive) {
    for (Inhabitant<?> i : coll) {
      if (expectActive) {
        assertTrue("DefaultRunLevelService should have activated: " + i, i.isInstantiated());
      } else {
        assertFalse("should not be active: " + i, i.isInstantiated());
      }
    }
  }

}
