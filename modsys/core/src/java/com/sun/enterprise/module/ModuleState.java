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
 * ModuleState define the state of a Module instance. 
 *
 * @author Jerome Dochez
 */
public enum ModuleState {
    
    /**
     * a Module is in NEW state when the module object is constructed but not 
     * initialized
     */
    NEW,
    /**
     * a Module is in PREPARING state when the module looks for its import 
     * policy class if any or use the default import policy to construct the 
     * network of dependency modules.
     */
    PREPARING,
    /**
     * a Module is in VALIDATING state when the system ensures that all 
     * declared dependencies are statisfied (all used Modules in RESOLVED, 
     * READY state
     */ 
    VALIDATING,
    /**
     * a Module is in RESOLVED state when the validation is finished and 
     * successful and before the module is started
     */
    RESOLVED,
    /**
     * the Module has been started as all its dependencies were satisfied
     */
    READY,
    /**
     * a Module is in ERROR state when its class loader cannot be constructed 
     * or when any of its dependent module is in ERROR state.
     */
    ERROR
    
}
