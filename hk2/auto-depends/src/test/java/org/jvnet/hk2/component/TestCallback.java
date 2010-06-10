package org.jvnet.hk2.component;

import org.junit.Ignore;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.InhabitantTracker;
import org.jvnet.hk2.component.InhabitantTracker.Callback;

/**
 * A test friendly callback.
 * 
 * @author Jeff Trent
 */
@Ignore
public class TestCallback implements Callback {

  public int calls;

  @Override
  public void updated(InhabitantTracker t, Habitat h, boolean initial) {
    calls++;
    if (null == t || null == h) {
      throw new IllegalArgumentException();
    }
  }

}
