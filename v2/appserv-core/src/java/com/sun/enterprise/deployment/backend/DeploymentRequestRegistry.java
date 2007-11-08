/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.deployment.backend;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import com.sun.enterprise.util.i18n.StringManager;

/**
 * This registry singleton class will maintain a list of 
 * oustanding DeploymentRequest instances keyed by the 
 * DeploymentID they are serving. 
 *
 * @author Jerome Dochez
 */
public class DeploymentRequestRegistry {
    
    // singleton instance
    static DeploymentRequestRegistry instance=null;
    
    // the map of deployment id to deployment requests
    Map idToRequest = Collections.synchronizedMap(new HashMap());    
    
    /** string manager */
    private static StringManager localStrings =
        StringManager.getManager( DeploymentRequestRegistry.class );

    /** Creates a new instance of DeploymentRequestRegistry */
    private DeploymentRequestRegistry() {
    }
    
    /**
     * @return the singleton instance of this registry
     */ 
    public static DeploymentRequestRegistry getRegistry() {
        if (instance==null) {
            synchronized(DeploymentRequestRegistry.class) {
                if (instance==null) {
                    instance = new DeploymentRequestRegistry();
                }
            };
        }
        return instance;
    }
    
    /**
     * add new new request to the registry
     * @param the module identifier
     * @param the deployment request
     */
    public void addDeploymentRequest(String id, 
        DeploymentRequest request) throws IASDeploymentException { 
        synchronized(DeploymentRequestRegistry.class) {
            // if there is another thread operating on the same module
            // at the same time, we need to abort this deployment
            if (idToRequest.containsKey(id)) {
                String msg = localStrings.getString(
                    "another_thread_access_same_module",
                    new Object[]{ id });
                throw new IASDeploymentException(msg);
            }
            idToRequest.put(id, request);
        }
    }

    /**
     * remove request from the registry
     * @param the module identifier
     */
    public void removeDeploymentRequest(String id) { 
        idToRequest.remove(id);
    }

    
    /** 
     * @return the deployment request associated with the passed module ID
     */
    public DeploymentRequest getDeploymentRequest(String id) {
        return (DeploymentRequest) idToRequest.get(id);
    }
}
