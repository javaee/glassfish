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
package org.jvnet.hk2.component.internal.runlevel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.HabitatListener;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.RunLevelService;

/**
 * This serves to provide holders, or proxy instances, for RunLevelServices
 * that are not initially found in the Habitat.
 * 
 * for internal use only.
 * 
 * @author Jeff Trent
 */
public class RunLevelServices {

  private Map<Habitat, Map<String, RunLevelServiceStub>> map =
    new HashMap<Habitat, Map<String, RunLevelServiceStub>>();

  
  /**
   * Find the RunLevelService appropriate for the specified RunLevel
   */
  public synchronized RunLevelService<?> get(Habitat habitat, int rl, Class<?> env) {
    return get(habitat, rl, env.getName());
  }
  
  public synchronized RunLevelService<?> get(Habitat habitat, int rl, String env) {
    assert(null != env);

    // if any inhabitants are already being managed, use same RLS stub.
    // we especially want to do this because we don't want to activate
    // the RunLevelservice before all of the inhabitants are present.
    RunLevelService<?> rls = getFromMap(habitat, env);
    
    // next, check to see if we have it from the habitat
    if (null == rls) {
      rls = getFromHabitat(habitat, env);
    }

    // create the stub if we didn't find it anywhere
    if (null == rls) {
      rls = create(habitat, rl, env);
    }
    
    return rls;
  }

  @SuppressWarnings("unchecked")
  static RunLevelService<?> getFromHabitat(Habitat habitat, String env) {
    if (null == habitat) {
      return null;
    }
    
    Collection<RunLevelService> coll = habitat.getAllByContract(RunLevelService.class);
    RunLevelService<?> theOne = null;
    for (RunLevelService<?> hrls : coll) {
      if (null != hrls.getState() && 
          hrls.getState().getEnvironment().equals(env)) {
        if (null != theOne) {
          throw new ComponentException("constraint violation - competing RunLevelServices: " +
              theOne + " and " + hrls);
        }
        theOne = hrls;
      }
    }
    
    return theOne;
  }


  private RunLevelService<?> getFromMap(Habitat habitat, String env) {
    Map<String, RunLevelServiceStub> envMap;
    synchronized (map) {
      envMap = map.get(habitat);
      if (null == envMap) {
        return null;
      }
    }
    
    RunLevelService<?> rls;
    synchronized (envMap) {
      rls = envMap.get(env);
    }
    
    return rls;
  }
  
  
  private RunLevelService<?> create(Habitat habitat, int rl, String env) {
    initialize(habitat, rl, env);
    
    Map<String, RunLevelServiceStub> envMap;
    synchronized (map) {
      envMap = map.get(habitat);
      if (null == envMap) {
        envMap = new HashMap<String, RunLevelServiceStub>();
        map.put(habitat, envMap);
      }
    }
    
    RunLevelServiceStub rls;
    synchronized (envMap) {
      rls = envMap.get(env);
      if (null == rls) {
        rls = new RunLevelServiceStub(habitat, env);
        envMap.put(env, rls);
      }
    }
    
    return rls;
  }


  private void initialize(Habitat habitat, int rl, String env) {
    if (null == habitat) {
      return;
    }
    
    if (habitat.isInitialized()) {
      throw new ComponentException(
          "no RunLevelService found appropriate for RunLevel: (" + rl + "," + env + ")");
    }
    
    habitat.addHabitatListener(new HabitatListener() {
      @Override
      public boolean inhabitantChanged(EventType eventType, Habitat habitat, Inhabitant<?> inhabitant) {
        if (EventType.HABITAT_INITIALIZED == eventType) {
          Map<String, RunLevelServiceStub> rlss;
          synchronized (map) {
            rlss = map.remove(habitat);
          }
          if (null != rlss) {
            // we must have some runlevel inhabitants under management
            for (RunLevelServiceStub rls : rlss.values()) {
              RunLevelService<?> realRls = getFromHabitat(rls.getHabitat(), rls.getEnvironment());
              rls.activate(realRls);
            }
          }
          return false;
        }
        return true;
      }

      @Override
      public boolean inhabitantIndexChanged(EventType eventType,
          Habitat habitat, Inhabitant<?> inhabitant, String index, String name,
          Object service) {
        return true;
      }
    });
  }

}
