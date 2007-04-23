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
 * A ServiceProviderInfo holds information about a particular service 
 * implementation. A Service implementation is identified by the service 
 * interface it implements, the implementation class of that service interface
 * and the module in which that implementation resides. 
 *
 * <p>
 * Note that since a single {@link ModuleDefinition} is allowed to be used
 * in multiple {@link Module}s, this class may not reference anything {@link Module}
 * specific.
 *
 * @author Jerome Dochez
 */
public class ServiceProviderInfo {
    
    final private String serviceName;
    final private String providerName;

    /** Create a new provider information with the service interface class
     * name as well as its implementation class name
     */
    public ServiceProviderInfo(String serviceName, String providerName) {
        this.serviceName = serviceName;
        this.providerName = providerName;
    }

    /**
     * Returns the service interface full class name
     * @return the service interface full class name
     */
    public String getServiceName() {
        return serviceName;
    }
    
    /**
     * Returns the service implementation full class name
     * @return the service implementation full class name
     */
    public String getProviderName() {
        return providerName;
    }
}
