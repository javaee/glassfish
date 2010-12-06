package org.jvnet.hk2.test.impl;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.test.contracts.Simple;
import org.jvnet.hk2.test.contracts.SimpleGetter;

/**
 * A RandomContract service that also has a Simple @Inject non-
 * optional dependency.
 * 
 * @author Jeff Trent
 */
@Service
public class RandomSimpleService extends RandomService 
    implements SimpleGetter, PreDestroy {
  
  private boolean destroyed;
  
  @Inject(name="one")
  Simple simple;

  @Override
  public Simple getSimple() {
    return simple;
  }

  @Override
  public void preDestroy() {
    destroyed = true;
  }

  @Override
  public boolean isPreDestroyed() {
    return destroyed;
  }
}
