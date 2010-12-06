package org.jvnet.hk2.test.impl;

import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.test.contracts.Simple;

/**
 * A PerLookup Service
 *   
 * @author Jeff Trent
 */
@Service(name="two")
@Scoped(PerLookup.class)
public class TwoSimple implements Simple {

  @Override
  public String get() {
    return "two";
  }

}
