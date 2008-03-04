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
package com.sun.appserv.management.ext.wsmgmt;

import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.MapCapableBase;

/**
 * This is the mechanism to provide web service endpoint's information in a
 * given module. 
 *
 * @since AppServer 9.0
 */
public final class WebServiceEndpointInfoImpl
    extends MapCapableBase
    implements WebServiceEndpointInfo {

    /**
     * Public constructor
     * 
     * @param m         Map containing keys as in {@link WebServiceEndpointInfo}
     *                  and their values
     * @param className Class name of the interface , must be
     *                  WebServiceEndpointInfo.CLASS_NAME
     */
    public WebServiceEndpointInfoImpl(
        final Map<String,Serializable> m,
        final String className) {
        super(m, className);
    }

    /**
     * Public constructor
     * 
     * @param name            Name of the web service end point
     * @param uri             URI of the web service end point
     * @param appid           Application Id of this web service end point
     * @param bundleName      Bundle name (module name) of this web service end
     *                        point
     * @param isStandalone    true, if the module (ejb or web) of this web 
     *                        service end point is standalone
     * @param wsdl            WSDL file location for this web service end point
     * @param mapfile         Mapping file location for this web service end 
     *                        point
     * @param webservicesFile webservices.xml file location for this web 
     *                        service end point
     * @param implType        Implementation type of this web service end point
     * @param implName        Implemenation Ejb or Servlet name
     * @param implClass       Implementation Ejb or Servlet class name
     * @param sURL            Service URL defined in WSDL
     * @param isSecure        Secure service or not
     */
    public WebServiceEndpointInfoImpl( String name, String uri, String appid, 
        String bundleName, boolean isStandalone, String wsdl, String mapfile, 

        String webservicesFile, String implType, String implName, 
        String implClass, String sURL, boolean isSecure) {

        super (null, WebServiceEndpointInfo.CLASS_NAME);
        setName(name);
        setEndpointURI(uri);
        setAppID(appid);
        setIsAppStandaloneModule(isStandalone);
        setBundleName(bundleName);
        setWSDLFile(wsdl);
        setWebservicesFile(webservicesFile);
        setMappingFile(mapfile);
        setServiceImplType(implType);
        setServiceImplName(implName);
        setServiceImplClass(implClass);
        setServiceURL(sURL);
        setIsSecure(isSecure);
    }

    protected boolean validate() {
        return (true);
    }

    /**
     * Returns the name of the WebService.
     *
     * @return fully qualified name of this WebService
     */
    public String getName() {
        return (getString(WebServiceEndpointInfo.NAME_KEY));
    }

    /**
     * Sets the name of the WebService.
     */
    public void setName(String name) {
        putField(WebServiceEndpointInfo.NAME_KEY, name);
    }

    /**
     * Returns the service URL of this endpoint.
     *
     * @return the url string
     */
    public String getServiceURL() {
        return (getString(WebServiceEndpointInfo.SERVICE_URL_KEY));
    }

    /**
     * Sets the service URL of this endpoint.
     */
    public void setServiceURL(String url) {
        putField(WebServiceEndpointInfo.SERVICE_URL_KEY, url);
    }

    /**
     * Returns the relative URI of this endpoint.
     *
     * @return the uri string
     */
    public String getEndpointURI() {
        return (getString(WebServiceEndpointInfo.END_POINT_URI_KEY));
    }

    /**
     * Sets the relative URI of this endpoint.
     */
    public void setEndpointURI(String uri) {
        putField(WebServiceEndpointInfo.END_POINT_URI_KEY, uri);
    }

    /**
     * Returns the name of the application or stand alone module that has 
     * this web service. 
     *
     * @return the Application Id
     */
    public String getAppID() {
        return (getString(WebServiceEndpointInfo.APP_ID_KEY));
    }

    /**
     * Sets the name of the application or stand alone module that has 
     * this web service. 
     */
    public void setAppID(String name) {
        putField(WebServiceEndpointInfo.APP_ID_KEY, name);
    }

    /**
     * Returns true if this web service belongs to a stand alone module.
     *
     * @return true if this web service belongs to a stand alone module 
     */
    public boolean isAppStandaloneModule() {
        return getBoolean(WebServiceEndpointInfo.IS_STAND_ALONE_MODULE_KEY).booleanValue();
    }

    /**
     * Sets the boolean indicating web service belongs to a stand alone module.
     *
     * @param isAppStandAloneModule true if this web service belongs to a 
     *                              stand alone module 
     */
    public void setIsAppStandaloneModule(boolean isAppStandAloneModule) {
        putField(WebServiceEndpointInfo.IS_STAND_ALONE_MODULE_KEY, new
        Boolean(isAppStandAloneModule) );
    }

   
    /**
     * Returns the module name within a EAR or null if it is a stand alone 
     * module. 
     *
     * @return the bundle name
     */
    public String getBundleName() {
        return (getString(WebServiceEndpointInfo.BUNDLE_NAME_KEY));
    }

    /**
     * Sets the module name within a EAR or null if it is a stand alone 
     * module. 
     *
     * @param name the bundle name
     */
    public void setBundleName(String name) {
        putField(WebServiceEndpointInfo.BUNDLE_NAME_KEY, name);
    }

    /**
     * Returns the implementation type of this web service.
     *
     * @return either "EJB" or "SERVLET"
     */
    public String getServiceImplType() {
        return (getString(WebServiceEndpointInfo.SERVICE_IMPL_TYPE_KEY));
    }

    /**
     * Sets the implementation type of this web service.
     *
     * @param type either "EJB" or "SERVLET"
     */
    public void setServiceImplType(String type) {
        putField(WebServiceEndpointInfo.SERVICE_IMPL_TYPE_KEY, type);
    }

    /**
     * Returns the implementation Ejb or Servlet name of this web service.
     *
     * @return the Ejb or Servlet name
     */
    public String getServiceImplName() {
        return (getString(WebServiceEndpointInfo.SERVICE_IMPL_NAME_KEY));
    }

    /**
     * Sets the implementation Ejb or Servlet name of this web service.
     *
     * @param name the Ejb or Servlet name
     */
    public void setServiceImplName(String name) {
        putField(WebServiceEndpointInfo.SERVICE_IMPL_NAME_KEY, name);
    }

    /**
     * Returns the implementation Ejb or Servlet class name of this web service.
     *
     * @return the Ejb or Servlet class name
     */
    public String getServiceImplClass() {
        return (getString(WebServiceEndpointInfo.SERVICE_IMPL_CLASS_KEY));
    }

    /**
     * Sets the implementation Ejb or Servlet class name of this web service.
     *
     * @param implClassName the Ejb or Servlet class name
     */
    public void setServiceImplClass(String implClassName) {
        putField(WebServiceEndpointInfo.SERVICE_IMPL_CLASS_KEY, implClassName);
    }

    /**
     * Returns the WSDL file. 
     *
     * @return the WSDL file 
     */
    public String getWSDLFile() {
        return (getString(WebServiceEndpointInfo.WSDL_FILE_KEY));
    }
     
    /**
     * Sets the WSDL file . 
     *
     * @param wsdl the WSDL file 
     */
    public void setWSDLFile(String wsdl) {
        putField(WebServiceEndpointInfo.WSDL_FILE_KEY, wsdl);
    }

    /**
     * Returns the webservices.xml file .
     *
     * @return the Web services file 
     */ 
    public String getWebservicesFile() {
        return (getString(WebServiceEndpointInfo.WEB_SERVICES_FILE_KEY));
    }
 
    /**
     * Sets the webservices.xml file .
     *
     * @param webservicesFile the Web services file 
     */ 
    public void setWebservicesFile(String webservicesFile) {
        putField(WebServiceEndpointInfo.WEB_SERVICES_FILE_KEY, 
                        webservicesFile);
    }

    /**
     * Returns the mapping file . 
     *
     * @return the mapping file 
     */
    public String getMappingFile() {
        return (getString(WebServiceEndpointInfo.MAPPING_FILE_KEY));
    }

    /**
     * Sets the mapping file . 
     *
     * @param mapFile the mapping file 
     */
    public void setMappingFile(String mapFile) {
        putField(WebServiceEndpointInfo.MAPPING_FILE_KEY, mapFile);
    }

    /**
     * Gets the web.xml file . 
     *
     * @return webXML the web.xml file 
     */
    public String getWebXML() {
        return getString(WebServiceEndpointInfo.WEB_XML_KEY);
    }

    /**
     * Sets the web.xml file . 
     *
     * @param webXML the web.xml file 
     */
    public void setWebXML(String webXML) {
        putField(WebServiceEndpointInfo.WEB_XML_KEY, webXML);
    }

    /**
     * Sets the sun-web.xml file . 
     *
     * @param sunWebXML the sun-web.xml file 
     */
    public void setSunWebXML(String sunWebXML) {
        putField(WebServiceEndpointInfo.SUN_WEB_XML_KEY, 
            sunWebXML);
    }

    /**
     * Gets the sun-web.xml file . 
     *
     * @return sunWebXML the sun-web.xml file 
     */
    public String getSunWebXML() {
        return getString(WebServiceEndpointInfo.SUN_WEB_XML_KEY); 
    }

    /**
     * Sets the ejb.xml file . 
     *
     * @param EJBXML the ejb.xml file 
     */
    public void setEJBXML(String EJBXML) {
        putField(WebServiceEndpointInfo.EJB_XML_KEY, EJBXML);
    }

    /**
     * Gets the ejb.xml file . 
     *
     * @return EJBXML the ejb.xml file 
     */
    public String getEJBXML() {
        return getString(WebServiceEndpointInfo.EJB_XML_KEY);
    }

    /**
     * Sets the sun-ejb.xml file . 
     *
     * @param sunEJBXML the sun-ejb.xml file 
     */
    public void setSunEJBXML(String sunEJBXML) {
        putField(WebServiceEndpointInfo.SUN_EJB_XML_KEY, 
                            sunEJBXML);
    }

    /**
     * Gets the sun-ejb.xml file . 
     *
     * @return sunEJBXML the sun-ejb.xml file 
     */
    public String getSunEJBXML() {
        return getString(WebServiceEndpointInfo.SUN_EJB_XML_KEY);
    }

    /**
     * Sets the application.xml file . 
     *
     * @param applicationXML the application.xml file 
     */
    public void setApplicationXML(String applicationXML) {
        putField(WebServiceEndpointInfo.APPLICATION_XML_KEY, 
                            applicationXML);
    }

    /**
     * Gets the application.xml file . 
     *
     * @return applicationXML the application.xml file 
     */
    public String getApplicationXML() {
        return getString(WebServiceEndpointInfo.APPLICATION_XML_KEY); 
    }

    /**
     * Returns true if this web service is secured or not.
     * Corresponds to the {@link #IS_SECURE_KEY} key.
     *
     * @return true, if this web service is secured, false otherwise
     */
    public boolean isSecure() {
        return getBoolean(WebServiceEndpointInfo.IS_SECURE_KEY).booleanValue();
    }

    /**
     * Returns true if this web service is secured or not.
     * Corresponds to the {@link #IS_SECURE_KEY} key.
     *
     * @return true, if this web service is secured, false otherwise
     */
    public void setIsSecure(boolean isSec) {
        putField(WebServiceEndpointInfo.IS_SECURE_KEY, new
        Boolean(isSec) );
    }

    /**
     * Returns the descriptor . The descriptors include WSDL, 
     * webservices.xml and mapping file. If the alt WSDL is specified
     * in the archive, the overridden (correct) WSDL file is returned.
     *
     * @return the descriptors 
     */
    public String[] getDescriptors() {
        String [] list = new String[3];
        list[0] = getString(WebServiceEndpointInfo.WSDL_FILE_KEY);
        list[1] = getString(WebServiceEndpointInfo.WEB_SERVICES_FILE_KEY);
        list[2] = getString(WebServiceEndpointInfo.MAPPING_FILE_KEY);
        return list;
    }
}
