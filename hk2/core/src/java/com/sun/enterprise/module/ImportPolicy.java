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

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;

/**
 * Modules that wish to programmatically control their list of imports can 
 * implement this interface. Implementation of this interface will be called 
 * when the module is in {@link ModuleState#PREPARING PREPARING} state. 
 *
 * <p>
 * To define an implementation of this in a module, write a class
 * that implements this interface and puts {@link Service} on it.
 * Maven will take care of the rest.
 * 
 * @author Jerome Dochez
 */
@Contract
public interface ImportPolicy {
    
    /**
     * callback from the module loading system when the module enters the 
     * {@link ModuleState#PREPARING PREPARING} phase.
     * @param module the module instance
     */
    public void prepare(Module module);
}
