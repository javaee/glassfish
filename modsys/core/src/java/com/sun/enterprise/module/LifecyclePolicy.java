/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.module;

/**
 * Modules that wish to invoke special logic when it is loaded, started 
 * or stopped should implement this interface.
 *
 * @author dochez
 */
public interface LifecyclePolicy {
    
    /**
     * Callback when the module enters the {@link ModuleState#RESOLVED RESOLVED}
     * state (all classloaders dependencies are resolved). 
     * Each submodule is guaranteed to be in at least
     * {@link ModuleState#RESOLVED RESOLVED} state. 
     * @param module the module instance
     */
    public void load(Module module);
    
    /** 
     * Callback when the module enters the {@link ModuleState#READY READY} state.
     * This is a good time to do any type of one time initialization 
     * or set up access to resources
     * @param module the module instance
     */
    public void start(Module module);
    
    /** 
     * Callback before the module starts being unloaded. The runtime will 
     * free all the module resources and returned to a {@link ModuleState#NEW NEW} state.
     * @param module the module instance
     */
    public void stop(Module module);
    
    /**
     * Requests a service offered by this module. 
     * The module's user can pass a context defining the 
     * requested service characteristics. A service initialization routine
     * can also update the context to add accessors to get the 
     * service specific hooks.
     *
     * @param serviceContext the context information
     * @return a flag with the service's initialization success 
     * 
     */ 
    public boolean getService(Object serviceContext);
   
}
