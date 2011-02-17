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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantListener;

import com.sun.hk2.component.AbstractInhabitantImpl;

/**
 * The Recorder is used internally within DefaultRunLevelService.
 * 
 * The implementation keeps an ordered set of activations while
 * performing some RunLevelService operation, that can be undone
 * upon the RunLevelService requesting it.
 * 
 * A single Recorder instance is responsible for a single RunLevel.
 * 
 * @author Jeff Trent
 */
/*public*/ class Recorder implements InhabitantListener {

  private final Logger logger = Logger.getLogger(Recorder.class.getName()); 
  
  private final int runLevel;
  private final Stack<Inhabitant<?>> activations;
  private final String targetEnv;
  
  Recorder(int runLevel, String targetEnv) {
    this(new ArrayList<Inhabitant<?>>(), runLevel, targetEnv);
  }
  
  Recorder(List<Inhabitant<?>> list, int runLevel) {
    this(new ArrayList<Inhabitant<?>>(), runLevel, Void.class.getName());
  }
  
  Recorder(List<Inhabitant<?>> list, int runLevel, String targetEnv) {
    this.activations = new Stack<Inhabitant<?>>();
    this.activations.addAll(list);
    this.runLevel = runLevel;
    this.targetEnv = targetEnv;
  }

  public int getRunLevel() {
    return runLevel;
  }

  List<Inhabitant<?>> getActivations() {
    synchronized (activations) {
      return Collections.unmodifiableList(activations);
    }
  }
  
  void push(Inhabitant<?> inhabitant) {
    logger.log(Level.FINE, "pushing {0}", inhabitant);
    synchronized (activations) {
      activations.add(inhabitant);
    }
  }

  Inhabitant<?> pop() {
    Inhabitant<?> inhabitant;
    synchronized (activations) {
      inhabitant = activations.isEmpty() ? null : activations.pop();
    }
    logger.log(Level.FINE, "popping {0}", inhabitant);
    return inhabitant;
  }
  
  @Override
  public String toString() {
    return getClass().getSimpleName() + "-" + System.identityHashCode(this) + 
        "(" + runLevel + ", " + activations + ")\n";
  }
  
  @Override
  public synchronized boolean inhabitantChanged(EventType eventType, Inhabitant<?> inhabitant) {
    if (EventType.INHABITANT_ACTIVATED == eventType) {
      assert(inhabitant.isInstantiated());
      assert(AbstractInhabitantImpl.class.isInstance(inhabitant));
   
      RunLevel rl = ((AbstractInhabitantImpl<?>)inhabitant).getAnnotation(RunLevel.class);
      // actually, it should really never be null (in real life we could consider tossing an exception)
      if (null != rl) {
        if (targetEnv.equals(rl.environment().getName())) {
          push(inhabitant);
          
          // verify it is not to a bad dependency
          if (rl.value() > runLevel) {
            throw new ComponentException("Invalid RunLevel dependency to: " + inhabitant);
          }
        }
      }
    }
    
    return true;
  }

}
