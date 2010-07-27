package com.sun.hk2.component;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantListener;

/**
 * Used for testing InhabitantListeners
 * 
 * @author Jeff Trent
 */
@Ignore
public class TestInhabitantListener implements InhabitantListener {

  public final List<Call> calls = new ArrayList<Call>();
  
  
  @Override
  public boolean inhabitantChanged(EventType eventType, Inhabitant<?> inhabitant) {
    calls.add(new Call(eventType, inhabitant));
    return true;
  }
  
  public static class Call {
    public final EventType eventType;
    public final Inhabitant<?> inhabitant;
    
    public Call(EventType eventType, Inhabitant<?> inhabitant) {
      this.eventType = eventType;
      this.inhabitant = inhabitant;
    }
  }

}
