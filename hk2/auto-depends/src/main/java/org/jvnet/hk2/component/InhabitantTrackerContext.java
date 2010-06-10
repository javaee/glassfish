package org.jvnet.hk2.component;

import java.util.Set;

/**
 * The filter that is passed to the InhabitantTracker creation.
 * 
 * @see Habitat#track(InhabitantTrackerContext, org.jvnet.hk2.component.InhabitantTracker.Callback)
 * @see Habitat#trackFuture(InhabitantTrackerContext, Boolean)
 * 
 * @author Jeff Trent
 * 
 * @since 3.1
 */
public interface InhabitantTrackerContext {

  /**
   * The set of contract class names that form the initial filter condition.
   * 
   * @return the Set of contract type names to filter on; must be non-empty set
   */
  public Set<String> getClassNames();

  /**
   * The filter to call for finer-level of matching beyond class/contract name.
   *  
   * @return the Filter matcher
   */
  public InhabitantFilter getFilter();

  /**
   * Presence flag:
   *          true, for tracking positive presence of inhabitants
   *          false, for tracking negative presence (i.e. disappearance) of inhabitants
   *          null, for tracking both cases (any inhabitant changes)
   */
  public Boolean getPresenceFlag();
  
}
