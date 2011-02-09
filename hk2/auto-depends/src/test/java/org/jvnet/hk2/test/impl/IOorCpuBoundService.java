package org.jvnet.hk2.test.impl;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.test.contracts.Simple;

/**
 * We simulate that "one" and "two" are expensive to postConstruct
 * 
 * @see InjectionManagerPerfTest
 * @author Jeff Trent
 */
@Service
public class IOorCpuBoundService extends PerLookupService implements PostConstruct {

  public static final int DELAY = 50;

  @Inject(name="one")
  Simple one;
  
  @Inject(name="two")
  Simple two;
  
  @Override
  public void postConstruct() {
    assert(one != two);
    super.postConstruct();
  }

  @Inject(name="one")
  public void setOneSimple(Simple one) throws InterruptedException {
    Thread.sleep(DELAY);
    this.one = one;
  }
  
  @Inject(name="two")
  public void setTwoSimple(Simple two) throws InterruptedException {
    Thread.sleep(DELAY);
    this.two = two;
  }

}
