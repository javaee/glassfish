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
package com.sun.hk2.component;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantListener;
import org.jvnet.hk2.component.RunLevelException;
import org.jvnet.hk2.component.RunLevelState;

/**
 * An inhabitant that prevents activation unless the sufficient RunLevelState
 * has been scheduled.
 * 
 * @author Jeff Trent
 */
public class RunLevelInhabitant<T, V> extends EventPublishingInhabitant<T> {
  private static boolean enabled = true;
  
  /**
   * Serves as the gating runLevel state.
   */
  private final int runLevel;

  /**
   * The RunLevelState that will gate the activation of the underlying
   * inhabitant.
   */
  private final RunLevelState<V> state;
  
  /*public*/ RunLevelInhabitant(Inhabitant<T> delegate,
      int runLevel, RunLevelState<V> state) {
    this(delegate, runLevel, state, null);
  }

  /*public*/ RunLevelInhabitant(Inhabitant<T> delegate,
      int runLevel, RunLevelState<V> state,
      InhabitantListener listener) {
    super(delegate, listener);
    this.runLevel = runLevel;
    this.state = state;
  }

  /**
   * FOR INTERNAL USE ONLY
   */
  public static void enable(boolean enable) {
    enabled = enable;
  }
  
  @Override
  public Class<? extends T> type() {
    boolean wasInstantiated = isActive();
    try {
      return super.type();
    } finally {
      if (isActive() != wasInstantiated) {
        // if we were inadvertently activated, better perform a latent check
        verifyState();
      }
    }
  }

  @SuppressWarnings("rawtypes")
  @Override
  public T get(Inhabitant onBehalfOf) {
    verifyState();
    return super.get(onBehalfOf);
  }

  /**
   * Verifies that the state of the RunLevelService is appropriate
   * for this instance activation.
   * 
   * @throws ComponentException if not in an appropriate state
   */
  protected void verifyState() throws ComponentException {
    if (!enabled) {
      return;
    }
    
    if (!isActive()) {
      Integer planned = state.getPlannedRunLevel();
      planned = (null == planned) ? RunLevel.KERNEL_RUNLEVEL : planned;
      Integer current = state.getCurrentRunLevel();
      current = (null == current) ? RunLevel.KERNEL_RUNLEVEL : current;
      if (null == planned || runLevel > planned || runLevel > current + 1) {
        throw new RunLevelException("unable to activate " + this + "; minimum expected RunLevel is: " + runLevel +
            "; planned is: " + planned + "; current is: " + current);
      }
    }
  }

  public RunLevelState<?> getState() {
    return state;
  }
  
}
