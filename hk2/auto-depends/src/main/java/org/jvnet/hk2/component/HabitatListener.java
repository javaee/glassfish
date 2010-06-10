package org.jvnet.hk2.component;

import java.util.EventListener;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;

/**
 * For Listening to the Habitat.
 * 
 * @see org.jvnet.hk2.component.Habitat#addHabitatListener(HabitatListener)
 * @see org.jvnet.hk2.component.Habitat#removeHabitatListener(HabitatListener)
 * 
 * @author Jeff Trent
 * 
 * @since 3.1
 */
@Contract
public interface HabitatListener extends EventListener {

  public enum EventType {
    INHABITANT_ADDED,
    INHABITANT_MODIFIED,
    INHABITANT_REMOVED,
    INHABITANT_INDEX_ADDED,
    INHABITANT_INDEX_REMOVED,
  };
  
  /**
   * Called when the habitat has changed.
   * 
   * @param eventType
   * @param habitat
   * @param inhabitant
   * @return callee should return true to continue receiving notification, false otherwise
   */
  public boolean inhabitantChanged(EventType eventType,
      Habitat habitat, Inhabitant<?> inhabitant);
  
  /**
   * Called when the habitat index has changed.
   * 
   * @param eventType
   * @param habitat
   * @param inhabitant
   * @return callee should return true to continue receiving notification, false otherwise
   */
  public boolean inhabitantIndexChanged(EventType eventType,
      Habitat habitat, Inhabitant<?> inhabitant, String index, String name, Object service);

}
