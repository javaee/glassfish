package org.jvnet.hk2.component;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jvnet.hk2.component.matcher.Constants;

/**
 * Default implementation of InhabitantTracker 
 * 
 * @author Jeff Trent
 * 
 * @since 3.1
 */
/*public*/ class InhabitantTrackerImpl implements InhabitantTracker, HabitatListener {

  protected final Habitat h;
  protected final InhabitantTrackerContext itc;
  protected Callback callback;
  private boolean open;
  private volatile boolean initialized;
  private CopyOnWriteArraySet<Inhabitant<?>> matches;
  
  public InhabitantTrackerImpl(Habitat h, 
      InhabitantTrackerContext itc,
      Callback callback) {
    this.h = h;
    this.itc = itc;
    this.open = true;

    if (null != callback) {
      checkInitializedListener();
  
      // callback should be set last
      this.callback = callback;
    
      if (null != callback && null != matches && !matches.isEmpty() && isDone()) {
        callback.updated(this, h, true);
      }
    }
  }

  protected void checkInitializedListener() {
    if (!initialized) {
      initialized = true;
      h.addHabitatListener(new HabitatListenerWeakProxy(this), itc.getClassNames());
      findInitialMatches();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Inhabitant<T> getInhabitant() throws ComponentException {
    if (!open) {
      return null;
    }
    
    checkInitializedListener();
    
    Inhabitant<?> best = null;
    Long bestSr = null;
    if (null != matches) {
      for (Inhabitant<?> next : matches) {
        if (null == best) {
          best = next;
        } else {
          Long sr = getServiceRanking(next, false);
          if (null != sr) {
            if (null == bestSr) {
              bestSr = getServiceRanking(best, true);
            }
            if (sr > bestSr) {
              best = next;
              bestSr = sr;
            }
          }
        }
      }
    }
    
    return (Inhabitant<T>) best;
  }

  protected Long getServiceRanking(Inhabitant<?> i, boolean wantNonNull) {
    MultiMap<String, String> meta = i.metadata();
    String sr = meta.getOne(Constants.SERVICE_RANKING);
    if (null == sr) {
      return (wantNonNull) ? 0L : null;
    }
    return Long.valueOf(sr);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Inhabitant<?>> getInhabitants() throws ComponentException  {
    checkInitializedListener();
    return (null == matches) ? Collections.EMPTY_SET : Collections.unmodifiableSet(matches);
  }

  @Override
  public void release() {
    if (open) {
      if (initialized) {
        h.removeHabitatListener(this);
        initialized = false;
      }
      open = false;
      matches = null;
      callback = null;
    }
  }

  @Override
  public boolean inhabitantChanged(EventType eventType, Habitat habitat,
      Inhabitant<?> inhabitant) {
    if (open && EventType.INHABITANT_MODIFIED == eventType) {
      if (null != matches && matches.contains(inhabitant)) {
        InhabitantFilter filter = itc.getFilter();
        if (null != filter && !filter.matches(inhabitant)) {
          // stimulate a removal
          updateMatched(EventType.INHABITANT_INDEX_REMOVED, inhabitant);
        } else {
          // at least notify of the update (a change may have occurred in ranking)
          if (null != callback && isDone()) {
            callback.updated(this, h, false);
          }
        }
      } else {
        // check for add
        InhabitantFilter filter = itc.getFilter();
        if (null != filter && filter.matches(inhabitant)) {
          if (null == matches) {
            matches = new CopyOnWriteArraySet<Inhabitant<?>>();
          }
          // stimulate an add
          updateMatched(EventType.INHABITANT_INDEX_ADDED, inhabitant);
        }
      }
    }
    
    return open;
  }

  @Override
  public boolean inhabitantIndexChanged(EventType eventType, Habitat habitat,
      Inhabitant<?> inhabitant, String index, String name, Object service) {
    if (open && 
        (EventType.INHABITANT_INDEX_ADDED == eventType || 
            EventType.INHABITANT_INDEX_REMOVED == eventType)) {
      if (itc.getClassNames().contains(index)) {
        InhabitantFilter filter = itc.getFilter(); 
        if (null == filter || filter.matches(inhabitant)) {
          if (null == matches) {
            matches = new CopyOnWriteArraySet<Inhabitant<?>>();
          }
          updateMatched(eventType, inhabitant);
        }
      }
    }
    return open;
  }

  protected void updateMatched(EventType eventType, Inhabitant<?> inhabitant) {
    boolean updated;
    if (EventType.INHABITANT_INDEX_ADDED == eventType) {
      updated = matches.add(inhabitant);
    } else {
      updated = matches.remove(inhabitant);
    }
    
    if (updated && null != callback && isDone()) {
      callback.updated(this, h, false);
    }
  }

  /**
   * The tracker goes beyond traditional listeners in that it also inventories
   * pre-existing inhabitants matching tracking criteria.
   */
  protected void findInitialMatches() {
    for (String contractName : itc.getClassNames()) {
      Collection<Inhabitant<?>> coll = 
        h.getAllInhabitantsByContract(contractName);
      for (Inhabitant<?> i : coll) {
        inhabitantIndexChanged(EventType.INHABITANT_INDEX_ADDED,
            h, i, contractName, null, null);
      }
    }
  }

  public boolean isDone() {
    Boolean presence = itc.getPresenceFlag();
    if (null == presence) {
      return true;
    } else {
      if (presence) {
        return !getInhabitants().isEmpty();
      } else {
        return getInhabitants().isEmpty();
      }
    }
  }

}
