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

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantListener;
import org.jvnet.hk2.component.RunLevelService;
import org.jvnet.hk2.component.RunLevelState;

/**
 * This serves as a holder, or proxy, for RunLevelServices that are not
 * initially found in the Habitat.
 * 
 * @author Jeff Trent
 */
@SuppressWarnings("rawtypes")
/*public*/ class RunLevelServiceStub implements RunLevelService, RunLevelState, InhabitantListener {

  private final Habitat h;
  
  private final String scopeName;
  
  // We are waiting for this guy to come around
  private RunLevelService delegate;
  private InhabitantListener delegateListener;
  
  
  /*public*/ RunLevelServiceStub(Habitat habitat, String scopeName) {
    this.h = habitat;
    this.scopeName = scopeName;
  }

  public RunLevelService getDelegate() {
    return delegate;
  }
  
  /**
   * Called when the habitat backing this RunLevelService has been fully initialized.
   */
  void activate(RunLevelService<?> realRls) {
    delegate = realRls;
    if (InhabitantListener.class.isInstance(delegate)) {
      delegateListener = InhabitantListener.class.cast(delegate);
    }
  }

  Habitat getHabitat() {
    return h;
  }

  @Override
  public RunLevelState getState() {
    return this;
  }

  @Override
  public Integer getCurrentRunLevel() {
    return (null == delegate) ? null : delegate.getState().getCurrentRunLevel();
  }

  @Override
  public Integer getPlannedRunLevel() {
    return (null == delegate) ? null : delegate.getState().getPlannedRunLevel();
  }

  @Override
  public String getScopeName() {
    return scopeName;
  }

  @Override
  public void proceedTo(int runLevel) {
    if (null != delegate) {
      delegate.proceedTo(runLevel);
    }

    // should never be here
    throw new IllegalStateException();
  }

  @Override
  public void interrupt() {
    if (null != delegate) {
      delegate.interrupt();
    }

    // should never be here
    throw new IllegalStateException();
  }
  
  @Override
  public void interrupt(int runLevel) {
    if (null != delegate) {
      delegate.interrupt(runLevel);
    }

    // should never be here
    throw new IllegalStateException();
  }

  @Override
  public synchronized boolean inhabitantChanged(EventType eventType, Inhabitant<?> inhabitant) {
    if (null == delegateListener) {
      if (null == delegate) {
        // we want to keep getting messages for now
        return true;
      } else {
          if (InhabitantListener.class.isInstance(delegate)) {
              delegateListener = InhabitantListener.class.cast(delegate);
          } else {
              // our delegate is not a listener, so we don't care anymore
              return false;
          }
      }
    }

    // refer to the delegate
    return delegateListener.inhabitantChanged(eventType, inhabitant);
  }

}
