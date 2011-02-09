package org.jvnet.hk2.test.impl;

import java.security.AccessControlContext;

import org.jvnet.hk2.annotations.FactoryFor;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.ContextualFactory;
import org.jvnet.hk2.component.InjectionPoint;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.test.contracts.DummyContract;
import org.jvnet.hk2.test.contracts.TestingInfoService;
import org.jvnet.hk2.test.contracts.TestingInfoService2;

import com.sun.hk2.component.Holder;

/**
 * See ExtendedInjectTest
 * @author Jeff Trent
 */
@Service
@FactoryFor(value=TestingInfoService2.class)
public class ServiceByContextualFactory implements ContextualFactory<TestingInfoService2>, PostConstruct {

  public static boolean returnNullNextTime;

  // presence of this verifies that the inner workings of injection manager doesn't fail
  @Inject(optional=true)
  private DummyContract dummy;
  
  // presence of this verifies that the inner workings of injection manager doesn't fail
  @Inject
  private Holder<TestingInfoService> dummy2;
  
  
  @Override
  public TestingInfoService2 getObject() throws ComponentException {
    assert returnNullNextTime : "see ExtendedInjectTest";
    return null;
  }

  @Override
  public TestingInfoService2 getObject(InjectionPoint ip, AccessControlContext acc) {
    if (returnNullNextTime) {
      returnNullNextTime = false;
      return null;
    }
    
    return new TIS(ip, acc);
  }
  

  @Override
  public void postConstruct() {
    assert(null == dummy);
    assert(null != dummy2);
  }

  
  public static class TIS implements TestingInfoService2, PostConstruct {
    
    private final InjectionPoint ip;
    private final AccessControlContext acc;
    int preDestroyCount;
    int postConstructCount;

    TIS(InjectionPoint ip, AccessControlContext acc) {
      this.ip = ip;
      this.acc = acc;
    }
    
    @Override
    public boolean isPreDestroyed() {
      return (preDestroyCount > 0);
    }

    @Override
    public void preDestroy() {
      preDestroyCount++;
    }

    @Override
    public InjectionPoint getInjectionPoint() {
      return ip;
    }

    @Override
    public AccessControlContext getAccessControlContext() {
      return acc;
    }
    
    @Override
    public int getPostContructCount() {
      return postConstructCount;
    }

    @Override
    public void postConstruct() {
      postConstructCount++;
    }
  }
}
