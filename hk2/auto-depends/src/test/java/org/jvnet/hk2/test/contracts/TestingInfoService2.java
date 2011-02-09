package org.jvnet.hk2.test.contracts;

import java.security.AccessControlContext;

import org.jvnet.hk2.component.InjectionPoint;

/**
 * Used in Testing
 * 
 * @author Jeff Trent
 */
//@Contract // commenting it out requires the use of either @ContractProvided or Factory based creation
public interface TestingInfoService2 extends TestingInfoService {
  
  public InjectionPoint getInjectionPoint();
  
  public AccessControlContext getAccessControlContext();
  
  public int getPostContructCount();
  
}
