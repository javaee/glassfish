package org.jvnet.hk2.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Ignore;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.HabitatListener;
import org.jvnet.hk2.component.Inhabitant;

/**
 * A test friendly habitat listener.
 * 
 * @author Jeff Trent
 */
@Ignore
public class TestHabitatListener implements HabitatListener {

  public final List<Call> calls = Collections.synchronizedList(new ArrayList<Call>());
  public RuntimeException forced; 
  public Integer countDown;
  
  public TestHabitatListener() {
  }
  
  public TestHabitatListener(int countDownBeforeRetFalse) {
    this.countDown = countDownBeforeRetFalse;
  }

  public TestHabitatListener(RuntimeException runtimeException) {
    this.forced = runtimeException;
  }

  @Override
  public boolean inhabitantChanged(EventType eventType, Habitat habitat,
      Inhabitant<?> inhabitant) {
    calls.add(new Call(eventType, habitat, inhabitant));
    if (null != forced) throw forced;
    return (null == countDown || --countDown > 0);
  }

  @Override
  public boolean inhabitantIndexChanged(EventType eventType, Habitat habitat,
      Inhabitant<?> inhabitant, String index, String name, Object service) {
    calls.add(new Call(eventType, habitat, inhabitant, index, name, service));
    if (null != forced) throw forced;
    return (null == countDown || --countDown > 0);
  }

  public static class Call {
    public EventType eventType;
    public Habitat h;
    public Inhabitant<?> obj;
    public String index;
    public String name;
    public Object service;
    
    public Call(EventType eventType, Habitat h, Inhabitant<?> i) {
      this(eventType, h, i, null, null, null);
    }

    public Call(EventType eventType, Habitat h, Inhabitant<?> i, String index, String name, Object service) {
      this.eventType = eventType;
      this.h = h;
      this.obj = i;
      this.index = index;
      this.name = name;
      this.service = service;
    }

    public String toString() {
      StringBuilder b = new StringBuilder();
      b.append("(event: ").append(eventType);
      b.append(";obj: ").append(obj);
      b.append(";name: ").append(name);
      b.append(")");
      return b.toString();
    }
    
  }
}
