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

import com.sun.appserv.management.base.MapCapable;

/**
 * WebServiceInfo provides information for a particular web service end point. 
 * Information includes name, URI, descriptor locations, type of implementation
 * (EJB or Servlet), etc.
 *
 * @since AppServer 9.0
 */
public interface WebServiceEndpointInfo extends MapCapable {

    /**
     * Returns the name of the WebService.
     * Corresponds to the {@link #NAME_KEY} key.
     *
     * @return fully qualified name of this WebService
     */
    public String getName();

    /**
     * Returns the relative URI of this endpoint.
     * Corresponds to the {@link #END_POINT_URI_KEY} key.
     *
     * @return the uri string
     */
    public String getEndpointURI();

    /**
     * Returns the name of the application or stand alone module that has 
     * this web service. 
     * Corresponds to the {@link #APP_ID_KEY} key.
     *
     * @return the Application Id
     */
    public String getAppID();

    /**
     * Returns true if this web service belongs to a stand alone module.
     * Corresponds to the {@link #IS_STAND_ALONE_MODULE_KEY} key.
     *
     * @return true, if this web service belongs to a stand alone module
     */
    public boolean isAppStandaloneModule();
   
    /**
     * Returns the module name within a EAR or null if it is a stand alone 
     * module. 
     * Corresponds to the {@link #BUNDLE_NAME_KEY} key.
     *
     * @return the bundle name
     */
    public String getBundleName();

    /**
     * Returns the implementation type of this web service.
     * Corresponds to the {@link #SERVICE_IMPL_TYPE_KEY} key.
     *
     * @return either  @see #EJB_IMPL or @ see #SERVLET_IMPL
     */
    public String getServiceImplType();

    /**
     * Returns the implementation Ejb or Servlet name of this web service.
     * Corresponds to the {@link #SERVICE_IMPL_NAME_KEY} key.
     *
     * @return the Ejb or Servlet name
     */
    public String getServiceImplName();

    /**
     * Returns the implementation Ejb or Servlet class name of this web service.
     * Corresponds to the {@link #SERVICE_IMPL_CLASS_KEY} key.
     *
     * @return the Ejb or Servlet class name
     */
    public String getServiceImplClass();

    /**
     * Returns the WSDL file 
     * Corresponds to the {@link #WSDL_FILE_KEY} key.
     *
     * @return the WSDL file 
     */
    public String getWSDLFile();
     
    /**
     * Returns the webservices.xml file .
     * Corresponds to the {@link #WEB_SERVICES_FILE_KEY} key.
     *
     * @return the webservices file 
     */ 
    public String getWebservicesFile();
 
    /**
     * Returns the mapping file. This is null for JAX-WS applications.
     * Corresponds to the {@link #MAPPING_FILE_KEY} key.
     *
     * @return the mapping file 
     */
    public String getMappingFile();


    /**
     * Gets the web.xml file. This will be null for EJB web service endpoints.
     * Corresponds to the {@link #WEB_XML_KEY} key.
     *
     * @return webXML the web.xml file as String
     */
    public String getWebXML(); 

    /**
     * Gets the sun-web.xml file. This will be null for EJB web service
     * endpoints. 
     * Corresponds to the {@link #SUN_WEB_XML_KEY} key.
     *
     * @return sunWebXML the sun-web.xml file 
     */
    public String getSunWebXML();

    /**
     * Gets the ejb.xml file. This will be null for Servlet web service
     * endpoints. 
     * Corresponds to the {@link #EJB_XML_KEY} key.
     *
     * @return EJBXML the ejb.xml file 
     */
    public String getEJBXML();

    /**
     * Gets the sun-ejb.xml file. This will be null for Servlet web service
     * endpoints. 
     * Corresponds to the {@link #SUN_EJB_XML_KEY} key.
     *
     * @return sunEJBXML the sun-ejb.xml file 
     */
    public String getSunEJBXML();

    /**
     * Gets the service URL of this web service endpoint.
     * Corresponds to the {@link #SERVICE_URL_KEY} key.
     *
     * @return service URL 
     */
    public String getServiceURL();

    /**
     * Gets the application.xml file. This will be null for web service in a
     * standalone module.
     * Corresponds to the {@link #APPLICATION_XML_KEY} key.
     *
     * @return applicationXML the application.xml file 
     */
    public String getApplicationXML();

    /**
     * Returns the descriptor . The descriptors includes:
     * <pre>
     *   WSDL, 
     *   webservices.xml, 
     *   mapping file 
     * </pre>
     * in the above specified order. If the alt WSDL location is specified 
     * in the archive, the overridden (correct) WSDL file is returned.
     *
     * @return the descriptor 
     */
    public String[] getDescriptors();

    /**
     * Returns true if this web service is secured or not.
     * Corresponds to the {@link #IS_SECURE_KEY} key.
     *
     * @return true, if this web service is secured, false otherwise
     */
    public boolean isSecure();
   
    // Keys to be used in Map returned from 
    // {@link WebServiceMgr#getWebServiceEndpointInfo}

    /** Application ID */
    public static final String APP_ID_KEY = "AppID";

    /** Name of the web service */
    public static final String NAME_KEY = "Name";

    /** Relative end point URI */
    public static final String END_POINT_URI_KEY = "EndPointURI";

    /**
     * Decides if this web service end point is in a stand alone
     * Module or not. 
     */
    public static final String IS_STAND_ALONE_MODULE_KEY = 
                "IsAppStandAloneModule";

    /** Bundle (embedded module) name, if applicable */
    public static final String BUNDLE_NAME_KEY = "BundleName";

    /** EJB or Servlet name */
    public static final String SERVICE_IMPL_NAME_KEY = "ServiceImplName";

    /** EJB or Servlet class name */
    public static final String SERVICE_IMPL_CLASS_KEY = "ServiceImplClass";

    /** 
     * Web service implementation type -  @see #EJB_IMPL or @see #SERVLET_IMPL
     */
    public static final String SERVICE_IMPL_TYPE_KEY = "ServiceImplType";

    /** WSDL file */
    public static final String WSDL_FILE_KEY = "WSDLFile";

    /** webservices.xml file */
    public static final String WEB_SERVICES_FILE_KEY =
                                                "WebServicesFile";

    /** WSDL location file */
    public static final String WSDL_FILE_LOCATION_KEY = "WSDLFileLocation";

    /** Mapping file */
    public static final String MAPPING_FILE_KEY ="MappingFile";

    /** Mapping file location */
    public static final String MAPPING_FILE_LOCATION_KEY ="MappingFileLocation";

    /** application.xml file */
    public static final String APPLICATION_XML_KEY =
                                "ApplicationXML";

    /** web.xml file */
    public static final String WEB_XML_KEY =
                                "WebXML";

    /** ejb.xml file */
    public static final String EJB_XML_KEY =
                                "EJBXML";

    /** sun-web.xml file */
    public static final String SUN_WEB_XML_KEY =
                                "SunWebXML";

    /** ejb.xml file */
    public static final String SUN_EJB_XML_KEY =
                                "SunEJBXML";


    /** EJB implementation type name */
    public static final String EJB_IMPL ="EJB";

    /** Servlet implementation type name */
    public static final String SERVLET_IMPL ="SERVLET";

    /** This interface's class name */
    public final static String  CLASS_NAME    =
                "com.sun.appserv.management.ext.wsmgmt.WebServiceEndpointInfo";

    /** Throuput statistic's name */
    public final static String  THROUGHPUT_STATISTIC    =
                "throughput_statistic";

    /** Service URL's name */
    public final static String  SERVICE_URL_KEY    =
                "service_url";
    /** Decides if this web service end point is secured or not.  */
    public static final String IS_SECURE_KEY = "IsSecure";

}
