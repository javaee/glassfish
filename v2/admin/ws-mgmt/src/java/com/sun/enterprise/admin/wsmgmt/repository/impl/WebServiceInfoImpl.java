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
package com.sun.enterprise.admin.wsmgmt.repository.impl;

import com.sun.enterprise.admin.wsmgmt.repository.spi.WebServiceInfo;

/**
 * This is the mechanism to provide web service information for a given module. 
 * A WebServiceInfoProvider implementation is a class that extends the 
 * WebServiceInfoProvider abstract class. Some WebServiceInfoProvider can deal
 * with ejb and web module. Some only deal with web modules.
 * <br>
 */
public class WebServiceInfoImpl implements WebServiceInfo {

    /**
     * Public constructor
     * 
     * @param name            Name of the web service end point
     * @param uri             URI of the web service end point
     * @param appid           Application Id of this web service end point
     * @param bundleName      Bundle name (module name) of this web service end
     *                        point
     * @param isStandAlone    true, if the module (ejb or web) of this web 
     *                        service end point is standalone
     * @param wsdl            WSDL file location for this web service end point
     * @param mapFile         Mapping file location for this web service end 
     *                        point
     * @param webservicesFile webservices.xml file location for this web 
     *                        service end point
     * @param implType        Implementation type of this web service end point
     * @param implName        Implemenation Ejb or Servlet name
     * @param implClass       Implementation Ejb or Servlet class name
     */

    public WebServiceInfoImpl( String name, String uri, String appid, 
        String bundleName, boolean isStandAlone, String wsdl, String mapfile, 
        String webservicesFile, String implType, String implName, 
        String implClass) {
            wsName = name;
            wsUri = uri;
            wsAppId = appid;
            wsAppStandAloneModule = isStandAlone;
            wsBundleName = bundleName;
            wsWSDL = wsdl;
            wsWebServices = webservicesFile;
            wsMapping = mapfile;
            wsImplType = implType;
            wsImplName = implName;
            wsImplClass = implClass;
        }

    /**
     * Returns the name of the WebService.
     *
     * @return fully qualified name of this WebService
     */
    public String getName() {
        return wsName;
    }

    /**
     * Returns the relative URI of this endpoint.
     *
     * @return the uri string
     */
    public String getEndpointUri() {
        return wsUri;
    }

    /**
     * Returns the name of the application or stand alone module that has 
     * this web service. 
     *
     * @return the Application Id
     */
    public String getAppId() {
        return wsAppId;
    }

    /**
     * Returns true if this web service belongs to a stand alone module.
     *
     * @return true if this web service belongs to a stand alone module 
     */
    public boolean isAppStandAloneModule() {
        return wsAppStandAloneModule;
    }
   
    /**
     * Returns the module name within a EAR or null if it is a stand alone 
     * module. 
     *
     * @return the bundle name
     */
    public String getBundleName() {
        return wsBundleName;
    }

    /**
     * Returns the implementation type of this web service.
     *
     * @return either "EJB" or "SERVLET"
     */
    public String getServiceImplType() {
        return wsImplType;
    }

    /**
     * Returns the implementation Ejb or Servlet name of this web service.
     *
     * @return the Ejb or Servlet name
     */
    public String getServiceImplName() {
        return wsImplName;
    }

    /**
     * Returns the implementation Ejb or Servlet class name of this web service.
     *
     * @return the Ejb or Servlet class name
     */
    public String getServiceImplClass() {
        return wsImplClass;
    }

    /**
     * Returns the WSDL file location. 
     *
     * @return the WSDL file location
     */
    public String getWSDLFileLocation() {
        return wsWSDL;
    }
     
    /**
     * Returns the webservices.xml file location.
     *
     * @return the Web services file location
     */ 
    public String getWebservicesFileLocation() {
        return wsWebServices;
    }
 
    /**
     * Returns the mapping file location. 
     *
     * @return the mapping file location
     */
    public String getMappingFileLocation() {
        return wsMapping;
    }

    /**
     * Returns the descriptor locations. The descriptors include WSDL, 
     * webservices.xml and mapping file. If the alt WSDL location is specified
     * in the archive, the overridden (correct) WSDL file location is returned.
     *
     * @return the descriptor locations
     */
    public String[] getDescriptorLocations() {
        String [] list = new String[3];
        list[0] = wsWSDL;
        list[1] = wsWebServices;
        list[2] = wsMapping;
        return list;
    }

    /**
     * Provides the string representation for this web sevice end point.
     *
     * @return string representation.
     */
    public String toString() {
        return "Web Service Name: " + wsName + " URI: " + wsUri  + 
        " Application Id " + wsAppId + " isStandAlone?: " + 
        wsAppStandAloneModule + " Bundle Name: " + wsBundleName + 
        " WSDL location " + wsWSDL + " Mapping file location: " + 
        wsMapping + " webservices.xml location: " + wsWebServices + 
        " impl Type " + wsImplType + " impl Name " + wsImplName + 
        " impl Class " + wsImplClass;
    }

    private String wsName;
    private String wsUri;
    private String wsAppId;
    private boolean wsAppStandAloneModule;
    private String wsBundleName;
    private String wsWSDL;
    private String wsWebServices;
    private String wsMapping;
    private String wsImplName;
    private String wsImplType;
    private String wsImplClass;

}
