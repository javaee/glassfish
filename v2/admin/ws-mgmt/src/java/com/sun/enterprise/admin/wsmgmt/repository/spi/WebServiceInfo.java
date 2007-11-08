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
package com.sun.enterprise.admin.wsmgmt.repository.spi;

/**
 * This is the mechanism to provide web service information for a given module.  
 * A WebServiceInfoProvider implementation is a class that extends the 
 * WebServiceInfoProvider abstract class. Some WebServiceInfoProvider can deal
 * with ejb and web module. Some only deal with web modules.
 * <br>
 */
public interface WebServiceInfo {

    /**
     * Returns the name of the WebService.
     *
     * @return fully qualified name of this WebService
     */
    public String getName();

    /**
     * Returns the relative URI of this endpoint.
     *
     * @return the uri string
     */
    public String getEndpointUri();

    /**
     * Returns the name of the application or stand alone module that has 
     * this web service. 
     *
     * @return the Application Id
     */
    public String getAppId();

    /**
     * Returns true if this web service belongs to a stand alone module.
     *
     * @return true, if this web service belongs to a stand alone module
     */
    public boolean isAppStandAloneModule();
   
    /**
     * Returns the module name within a EAR or null if it is a stand alone 
     * module. 
     *
     * @return the bundle name
     */
    public String getBundleName();

    /**
     * Returns the implementation type of this web service.
     *
     * @return either "EJB" or "SERVLET"
     */
    public String getServiceImplType();

    /**
     * Returns the implementation Ejb or Servlet name of this web service.
     *
     * @return the Ejb or Servlet name
     */
    public String getServiceImplName();

    /**
     * Returns the implementation Ejb or Servlet class name of this web service.
     *
     * @return the Ejb or Servlet class name
     */
    public String getServiceImplClass();

    /**
     * Returns the WSDL file location. 
     *
     * @return the WSDL file location
     */
    public String getWSDLFileLocation();
     
    /**
     * Returns the webservices.xml file location.
     *
     * @return the webservices file location
     */ 
    public String getWebservicesFileLocation();
 
    /**
     * Returns the mapping file location. 
     *
     * @return the mapping file location
     */
    public String getMappingFileLocation();

    /**
     * Returns the descriptor locations. The descriptors include WSDL, 
     * webservices.xml and mapping file. If the alt WSDL location is specified
     * in the archive, the overridden (correct) WSDL file location is returned.
     *
     * @return the descriptor locations
     */
    public String[] getDescriptorLocations();
}
