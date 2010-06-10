package org.jvnet.hk2.component;

import org.jvnet.hk2.annotations.Contract;

/**
 * Contract used to determine if an inhabitant matches some
 * criteria determined by the implementation.
 *
 * @see InhabitantTrackerContext
 * 
 * @author Jeff Trent
 * 
 * @since 3.1
 */
@Contract
public interface InhabitantFilter {

  /**
   * @return true; if the inhabitant matches filter criteria
   */
  public boolean matches(Inhabitant<?> i);
  
}
