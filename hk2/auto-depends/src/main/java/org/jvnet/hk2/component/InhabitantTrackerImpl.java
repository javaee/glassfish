/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.jvnet.hk2.component;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArraySet;

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

  @Override
  public String toString() {
    // need to initialize data structures / listeners for proper toString() output
    checkInitializedListener();
    
    String many;
    if (matches.size() > 1) {
      many = ",...";
    } else {
      many = "";
    }
    
    return getClass().getSimpleName() + "-" +
        System.identityHashCode(this) + "(" + getInhabitant() + many + ")";
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
          Long sr = Habitat.getServiceRanking(next, false);
          if (null != sr) {
            if (null == bestSr) {
              bestSr = Habitat.getServiceRanking(best, true);
            }
            
            if (sr > bestSr) {
              best = next;
              bestSr = sr;
            } else if (sr.equals(bestSr)) {
              // favor active inhabitants over inactive ones
              if (!best.isActive() && next.isActive()) {
                best = next;
              }
            }
          }
        }
      }
    }
    
    return (Inhabitant<T>) best;
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
        h.getInhabitantsByContract(contractName);
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
