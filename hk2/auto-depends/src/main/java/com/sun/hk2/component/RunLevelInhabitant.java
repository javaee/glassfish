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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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

  /*
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

    @Override
    public <U> U getByType(Class<U> type) {
        if (isStrict()) {
            if (type.isInterface()) {
                // try to activate to ensure a predictable activation order between dependant services
                attemptToActivate();

                return (U) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type},
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] objects)
                                    throws Throwable {

                                if (isActive()) {
                                    Object o = get();
                                    return o == null ? null : method.invoke(o, objects);
                                }
                                throw new RunLevelException(getFailedActivationMessage(
                                        getPlannedRunLevel(), getCurrentRunLevel()));
                            }
                        });
            }

            throw new RunLevelException("can't create proxy of type "
                    + type + " for " + this );
        }
        return super.getByType(type);
    }

    /**
     * Verifies that the state of the RunLevelService is appropriate for this
     * instance activation.
     *
     * @throws ComponentException if not in an appropriate state for activation
     */
    protected void verifyState() throws ComponentException {
        if (enabled && !isActive()){
            Integer planned = getPlannedRunLevel();
            Integer current = getCurrentRunLevel();

            if (!isValidActiveState(planned, current)) {
                throw new RunLevelException(getFailedActivationMessage(planned, current));
            }
        }
    }

    /**
     * Attempt to activate this instance.  First check that the state of
     * the RunLevelService is appropriate for this instance activation.
     */
    private void attemptToActivate() {
        if (isValidActiveState(getPlannedRunLevel(), getCurrentRunLevel())) {
            get();
        }
    }

    /**
     * Determine whether or not the state of the RunLevelService is appropriate
     * for this instance activation with the given planned/current run levels.
     *
     * @param planned  the planned run level
     * @param current  the current run level
     *
     * @return true if the state of the RunLevelService is appropriate for
     *         this instance activation
     */
    private boolean isValidActiveState(Integer planned, Integer current) {
        return !isStrict() ||
                ((runLevel <= planned) && (runLevel <= (current + 1)));
    }

    /**
     * Get the text for a failed activation of the run level service.
     *
     * @param planned  the planned run level
     * @param current  the current run level
     *
     * @return  the message
     */
    private String getFailedActivationMessage(Integer planned, Integer current) {
        return "unable to activate " + this +
                "; minimum expected RunLevel is: " + runLevel +
                "; planned is: " + planned +
                "; current is: " + current;
    }

    /**
     * Check whether the strict constraint rules should be followed.
     *
     * @return true if strict constraint rules should be followed
     */
    private boolean isStrict() {
        RunLevel rl = type().getAnnotation(RunLevel.class);
        return rl == null || rl.strict();
    }

    /**
     * Get the planned run level from the associated run level state.
     *
     * @return the planned run level
     */
    private Integer getPlannedRunLevel() {
        Integer planned = state.getPlannedRunLevel();
        return planned == null ? RunLevel.KERNEL_RUNLEVEL : planned;
    }

    /**
     * Get the current run level of the associated run level state.
     *
     * @return the current run level
     */
    private Integer getCurrentRunLevel() {
        Integer current = state.getCurrentRunLevel();
        return current == null ? RunLevel.KERNEL_RUNLEVEL : current;
    }

    /**
     * Get the run level state that is associated with this inhabitant.
     *
     * @return  the run level state.
     */
    public RunLevelState<?> getState() {
        return state;
    }
}
