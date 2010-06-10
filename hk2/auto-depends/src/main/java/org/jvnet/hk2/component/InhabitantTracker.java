package org.jvnet.hk2.component;

import java.util.Collection;

/**
 * Tracks an inhabitant, or set of inhabitants.
 * 
 * @see Habitat#track(InhabitantTrackerContext, Callback)
 * @see Habitat#trackFuture(InhabitantTrackerContext, Boolean)
 * 
 * @author Jeff Trent
 * 
 * @since 3.1
 */
public interface InhabitantTracker {

  /**
   * Returns a single inhabitant.  If multiple inhabitants qualify,
   * the one selected is based upon the implementation.
   * 
   * @return an inhabitant, or null if no inhabitants match tracker criteria
   */
  public <T> Inhabitant<T> getInhabitant() throws ComponentException;

  /**
   * Returns the set of inhabitants qualifying.
   * 
   * @return the collection of inhabitants matching tracker criteria
   */
  public Collection<Inhabitant<?>> getInhabitants() throws ComponentException;

  /**
   * Releases / closes this tracker. This MUST be called for performance reasons,
   * to cleanup resources.
   */
  public void release();


  /**
   * The callback is called when there is an event changing one of the tracked
   * inhabitants.  The callback may occur on a different thread than the one
   * that originated the change in the habitat.
   */
  public static interface Callback {
    /**
     * Called when there is a modification to the set of inhabitants of some kind.
     * 
     * @param t the tracker
     * @param h the habitat
     * @param initial
     *    true if the updated even happens during initial tracker creation taking
     *    inventory of starting matches
     */
    public void updated(InhabitantTracker t, Habitat h, boolean initial);
  }

}
