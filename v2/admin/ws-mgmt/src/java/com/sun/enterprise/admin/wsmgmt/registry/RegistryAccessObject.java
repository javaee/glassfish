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

package com.sun.enterprise.admin.wsmgmt.registry;

/**
 ** RegistryAccessObject is based on DataAccessObject pattern. It facilitates
 * access to the registry operations. A new RegistryAccessObject is created
 * for each publishToRegistry, unpublishFromRegistry and listRegistryLocations
 * operation from asadmin.
 * 
 * A RAO encapsulates connection objects to each of the listed registry locations
 * specificed by registryJndiNames
 *
 * @author Harpreet Singh
 */
public interface RegistryAccessObject {
     
     /**
     * publish a web service to a set of registries
     * @param String[] list of registry-locations specified by the jndi name 
     * of the connector-connection-pool. The list of names can be obtained by 
     * using the listRegistryLocations method
     * @param String web service name 
     * @param String load balancer host where this webservice is (optionally)
     * hosted. A null string signifies that the host is optional and is to be 
     * ignored while publishing this web service
     * @param int load balancer port where this webservice is (optionally) 
     * hosted. A value of -1 indicates that this is optional and is to be 
     * ignored
     * @param int load balancer ssl port where this webservice is (optionally) 
     * hosted. A value of -1 indicates that this is optional and is to be 
     * ignored
     * @param String[] an (optional) list of categories that can qualify this 
     * webservice. A null value indicates that this optional and is to be 
     * ignored
     * @param String description an (optional) description. A null value 
     * indicates that this is to be ignored
     * @param String wsdl WSDL File to publish
     * @return boolean true if published, false otherwise.
     */
    public boolean publish(String[] registryLocations, String webServiceName, 
            String lbhost, int lbport, int lbsslport, String[] categories, String organization, 
            String description, String wsdl);
    
    /**
     * Unpublishes a web service wsdl from a list of registries
     * @param String[] list of registry-locations
     * @param String web service name whose wsdl needs to be unpublised
     * @return boolean true if unpublished successfully
     */   
    public boolean unpublishFromRegistry(String[] registryJndiNames, 
           String webServiceName);
       
    /**
     * List the RegistryLocations. A registry location is the jndi name of a
     * connection pool that points to a registry determined by the
     * connector connection definition of the type JAXR_REGISTRY_TYPE
     * @return String[] list of registry-location
     */
    public String[] listRegistryLocations ();
    
}
