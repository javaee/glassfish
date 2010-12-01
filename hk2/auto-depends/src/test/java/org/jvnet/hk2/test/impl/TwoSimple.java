package org.jvnet.hk2.test.impl;

import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantRequested;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.test.contracts.Simple;

/**
 * A PerLookup Service
 *   
 * @author Jeff Trent
 */
@Service(name="two")
@Scoped(PerLookup.class)
public class TwoSimple implements Simple, PostConstruct, PreDestroy, InhabitantRequested {
  public static int constructs;
  public static int destroys;
  
  public Inhabitant<?> self;
  
  @Override
  public void postConstruct() {
    constructs++;
    if (null == self) {
      throw new IllegalStateException();
    }
  }

  @Override
  public void preDestroy() {
    destroys++;
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
  public String get() {
    return "two";
  }

}
