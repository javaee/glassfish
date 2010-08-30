/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2010 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

  private final int runLevel;
  private final List<Inhabitant<?>> activations;
  
  Recorder(int runLevel) {
    this(new ArrayList<Inhabitant<?>>(), runLevel);
  }
  
  Recorder(List<Inhabitant<?>> list, int runLevel) {
    this.activations = list;
    this.runLevel = runLevel;
  }

  public int getRunLevel() {
    return runLevel;
  }

  List<Inhabitant<?>> getActivations() {
    return Collections.unmodifiableList(activations);
  }
  
  @Override
  public String toString() {
    return getClass().getSimpleName() + "-" + System.identityHashCode(this) + 
        "(" + getRunLevel() + ", " + activations + ")";
  }
  
  @Override
  public synchronized boolean inhabitantChanged(EventType eventType, Inhabitant<?> inhabitant) {
    if (EventType.INHABITANT_ACTIVATED == eventType) {
      assert(inhabitant.isInstantiated());
      assert(AbstractInhabitantImpl.class.isInstance(inhabitant));
      RunLevel rl = ((AbstractInhabitantImpl<?>)inhabitant).getAnnotation(RunLevel.class);
      // actually, it should really never be null (in real life we could consider tossing an exception)
      if (null != rl) {
        if (null == rl.environment() || Void.class == rl.environment()) {
          // we record it anyway, for good measure during shutdown
          activations.add(inhabitant);
          
          // verify it is not to a bad dependency
          if (rl.value() > runLevel) {
            throw new ComponentException("Invalid RunLevel dependency to: " + inhabitant);
          }
        }
      }
    }
    return true;
  }

  /**
   * Causes release of the entire activationSet.  Release occurs in the inverse
   * order of the recordings.  So A->B->C will have startUp ordering be (C,B,A)
   * because of dependencies.  The shutdown ordering will b (A,B,C).
   */
  public synchronized void release() {
    int pos = activations.size();
    while (--pos >= 0) {
      Inhabitant<?> i = activations.get(pos);
      try {
        i.release();
      } catch (Exception e) {
        // don't percolate the exception since it may negatively impact processing
        Logger.getAnonymousLogger().log(Level.WARNING, "exception caught during release:", e);
      }
    }
    
    activations.clear();
  }

}
