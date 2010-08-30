package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

@RunLevelTen
@Service
public class RunLevelServiceA extends RunLevelServiceBase implements ServiceA {

  @Inject
  ServiceB serviceB;
  
}
