package org.jvnet.hk2.test.impl;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantRequested;
import org.jvnet.hk2.component.PostConstruct;
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
    implements SimpleGetter, PostConstruct, PreDestroy, InhabitantRequested {
  public static int constructs;
  public static int destroys;
  
  public Inhabitant<?> self;
  
  private boolean destroyed;
  
  @Inject(name="one")
  Simple simple;

  @Override
  public Simple getSimple() {
    return simple;
  }

  @Override
  public void postConstruct() {
    constructs++;
    if (null == self) {
      throw new IllegalStateException();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setInhabitant(Inhabitant inhabitant) {
    self = inhabitant;
  }
  
  @Override
  public void preDestroy() {
    destroyed = true;
    destroys++;
    if (null == self) {
      throw new IllegalStateException();
    }
  }

  @Override
  public boolean isPreDestroyed() {
    return destroyed;
  }
}
