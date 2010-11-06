package com.sun.hk2.component;

import static org.junit.Assert.*;

import org.junit.Test;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantListener;
import org.jvnet.hk2.component.InhabitantListener.EventType;

/**
 * Testing for EvetPublishingInhabitant
 * 
 * @author Jeff Trent
 */
@SuppressWarnings("unchecked")
public class EventPublishingInhabitantTest {

  private int count = 0;
  
  @Test
  public void testRemovableDuringIteration() {
    Inhabitant i = new ExistingSingletonInhabitant(new Object());
    EventPublishingInhabitant epi = new EventPublishingInhabitant(i);

    for (int j = 0; j < 100; j++) {
      epi.addInhabitantListener(new Listener());
    }
    
    epi.notify(EventType.INHABITANT_ACTIVATED);
    assertEquals(100, count);
    
    count = 0;
    epi.notify(EventType.INHABITANT_ACTIVATED);
    assertEquals("all listeners are now removed", 0, count);
  }
  
  
  private class Listener implements InhabitantListener {
    @Override
    public boolean inhabitantChanged(EventType eventType,
        Inhabitant<?> inhabitant) {
      count++;
      return false;
    }
  }
  
}
