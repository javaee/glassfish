package org.jvnet.hk2.test.impl;

import org.jvnet.hk2.annotations.FactoryFor;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Factory;
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
@FactoryFor(value=TestingInfoService.class)
public class ServiceByFactory implements Factory<TestingInfoService>, PostConstruct {

  public static boolean returnNullNextTime;
  
  
  // presence of this verifies that the inner workings of injection manager doesn't fail
  @Inject(optional=true)
  private DummyContract dummy;
  
  // presence of this verifies that the inner workings of injection manager doesn't fail
  @Inject
  private Holder<TestingInfoService2> dummy2;
  
  
  @Override
  public TestingInfoService get() throws ComponentException {
    if (returnNullNextTime) {
      returnNullNextTime = false;
      return null;
    }

    return new TIS();
  }

  
  @Override
  public void postConstruct() {
    assert(null == dummy);
    assert(null != dummy2);
  }

  
  public static class TIS implements TestingInfoService {
    @Override
    public boolean isPreDestroyed() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public void preDestroy() {
      // TODO Auto-generated method stub
    }
  }

}
