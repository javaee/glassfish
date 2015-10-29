/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.api;

import java.util.concurrent.Executor;

import org.jvnet.hk2.annotations.Contract;

/**
 * This service is advertised when the Immediate service is put into the
 * registry.  The immediate service by default starts in the SUSPENDED
 * state so that the Executor and other parameters can be set prior to
 * the first Immediate service being started
 * 
 * @author jwells
 *
 */
@Contract
public interface ImmediateController {
    /**
     * Returns the executor that is currently in use by the Immediate subsystem
     * 
     * @return The current executor in use by the controller.  Will not return null
     * since a default executor is used and returned by this call even if setExecutor
     * is called with null
     */
    public Executor getExecutor();
    
    /**
     * Sets the executor to be used by the Immediate subsystem.  If set to
     * null a default executor will be used.  This may only be called when
     * the Immediate service is suspended
     * 
     * @param executor The executor to be used when scheduling work.  If null
     * a default executor implementation will be used
     * @throws IllegalStateException if this is called when the Immediate service
     * is not in suspended state
     */
    public void setExecutor(Executor executor) throws IllegalStateException;
    
    /**
     * Returns the time in milliseconds a thread will wait for new Immediate
     * services before dying
     * 
     * @return The time in milliseconds a thread will wait for new
     * Immediate service before dying
     */
    public long getThreadInactivityTimeout();
    
    /**
     * Sets the time in milliseconds a thread will wait for new Immediate
     * services before dying.  May only be called when the system is in
     * suspended state
     * 
     * @param timeInMillis The time in milliseconds a thread will wait for new
     * Immediate service before dying
     * @throws IllegalStateException if this is called when the Immediate service
     * is not in suspended state
     * @throws IllegalArgumentException if timeInMillis is less than zero
     */
    public void setThreadInactivityTimeout(long timeInMillis) throws IllegalStateException, IllegalArgumentException;
    
    /**
     * Returns the state the system is currently running under
     * 
     * @return The current state of the ImmediateService
     */
    public ImmediateServiceState getImmediateState();
    
    /**
     * Sets the state the system is currently running under
     * 
     * @param state The new state of the ImmediateService
     */
    public void setImmediateState(ImmediateServiceState state);
    
    public enum ImmediateServiceState {
        /**
         * The system will not create new Immediate services when in SUSPENDED state.
         * Suspended state does not imply that Immediate services will be shutdown, but
         * rather that any new Immediate services that come along will not be started
         */
        SUSPENDED,
        
        /**
         * The system will create new Immediate services as soon as they are found
         */
        RUNNING
    }

}
