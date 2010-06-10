package org.jvnet.hk2.component;

import java.util.Map;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;

/**
 * The Handler controls lifecycle events, Inhabitants being added or removed from a Habitat.
 * 
 * A test-only, helper construct.
 * 
 * @author Jeff Trent
 */
public class InhabitantHandlerImpl {

  private final Habitat h;

  public InhabitantHandlerImpl(Habitat h) {
    this.h = h;
  }
  
  public <T> InhabitantHandle<T> create(boolean autoCommit,
      T component, String name,
      Map<String, Object> properties, String... classNames)
      throws ComponentException {
    InhabitantHandle<T> handle = 
      new InhabitantHandleImpl<T>(h, component, name, properties, classNames);
    if (autoCommit) {
      handle.commit();
    }
    return handle;
  }

  public static <T> InhabitantHandle<T> create(Habitat h, boolean autoCommit,
      T component, String name,
      Map<String, Object> properties, String... classNames)
      throws ComponentException {
    return new InhabitantHandlerImpl(h).create(autoCommit,
        component, name, properties, classNames);
  }  
}
