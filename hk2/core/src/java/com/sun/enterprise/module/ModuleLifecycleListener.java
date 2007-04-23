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
 * Listener interface that can be registered on the registry to listen to 
 * notification of module startup and shutdown. Modules which are interdependent
 * could use this approach to limit the interdependencies of code. 
 *
 * @author Jerome Dochez
 */
public interface ModuleLifecycleListener {
 
    /**
     * Callback after a module is started. 
     * @param module the module instance
     */
    public void moduleStarted(Module module);
    
    /** 
     * Callback after a module is stopped
     * @param module the module instance
     */
    public void moduleStopped(Module module);
}
