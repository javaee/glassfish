package org.jvnet.hk2.test.impl;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantRequested;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.test.contracts.Simple;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: May 17, 2010
 * Time: 12:53:20 PM
 * To change this template use File | Settings | File Templates.
 */
@Scoped(Singleton.class)
@Service(name="one")
public class OneSimple implements Simple, PostConstruct, PreDestroy, InhabitantRequested {
  public static int constructs;
  public static int destroys;
  
  public Inhabitant<?> self;
  
  public Simple twoSimple;
  
  @Inject(name="two")
  public void setTwoSimple(Simple simple) {
    twoSimple = simple;
  }
  
  @Override
  public void postConstruct() {
    constructs++;
    if (null == self) {
      throw new IllegalStateException();
    }
    if (null == twoSimple) {
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

  public String get() {
    return "one";
  }
}
