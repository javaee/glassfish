package org.jvnet.hk2.component;

import java.lang.ref.WeakReference;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;

/**
 * A proxy where the underlying HabitatListener is held as a weak
 * proxy and is automatically removed from the habitat if the proxy
 * is GC'ed.
 *
 * @author Jeff Trent
 * 
 * @since 3.1
 */
public class HabitatListenerWeakProxy implements HabitatListener {

  private final WeakReference<HabitatListener> proxy;
  
  public HabitatListenerWeakProxy(HabitatListener proxy) {
    if (null == proxy) throw new IllegalArgumentException();
    this.proxy = new WeakReference<HabitatListener>(proxy);
  }
  
  @Override
  public synchronized boolean inhabitantChanged(EventType eventType, Habitat habitat,
      Inhabitant<?> inhabitant) {
    HabitatListener listener = proxy.get();
    if (null == listener) return false;
    return listener.inhabitantChanged(eventType, habitat, inhabitant);
  }

  @Override
  public boolean inhabitantIndexChanged(EventType eventType, Habitat habitat,
      Inhabitant<?> inhabitant, String index, String name, Object service) {
    HabitatListener listener = proxy.get();
    if (null == listener) return false;
    return listener.inhabitantIndexChanged(eventType, habitat, inhabitant, index, name, service);
  }

}
