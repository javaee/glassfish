package org.jvnet.hk2.component;

/**
 * @author Jeff Trent
 */
public interface HabitatFactory {
  
  Habitat newHabitat() throws ComponentException;

}
