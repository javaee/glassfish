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

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Singleton;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.j2ee.WebServiceEndpoint;

import java.util.Map;
import java.util.Set;

/**
 * WebServiceMgr is used to obtained information about web service end points 
 * that are deployed. For each web service end point, name, URI, type of web
 * service implementation (EJB or Servlet), EJB or Servlet name, descriptor
 * locations etc. can be obtained.
 * <br>
 * This WebServiceMgr can also be used to create Self Management rules.
 *
 * @since AppServer 9.0
 */
public interface WebServiceMgr extends AMX, Singleton
{
    /** The j2eeType of WebServiceMgr */
    public static final String	J2EE_TYPE	= XTypes.WEB_SERVICE_MGR;

    /**
     * Returns a map of web service endpoint keys and their display names.
     * It returns information for all deployed web service endpoints in the
     * domain.
     * 
     * @return a map of web service endpoint keys and their display names
     *            This returned key is opaque. This is of form
     *            ApplicationID#ModuleID#WebServiceEndpointName or
     *            ModuleID#WebServiceEndpointName
     */
    public Map<Object,String> getWebServiceEndpointKeys();

    /**
     * Returns the {@link WebServiceEndpointInfo} for a web service endpoint.
     * 
     * @param key  The web service endpoint's key.
     *             This is opaque key. This is of form
     *             ApplicationID#ModuleID#WebServiceEndpointName or
     *             ModuleID#WebServiceEndpointName
     *             This is returned from getWebServiceEndpointKeys
     *
     * @return {@link WebServiceEndpointInfo} as Map which is mapped by keys in interface {@link
     * WebServiceEndpointInfo}
     @Deprecated  should not use proprietary types over the wire
     */
    public WebServiceEndpointInfo getWebServiceEndpointInfo(Object key);

    
    /**
     * Returns the set of {@link WebServiceEndpoint} runtime mbeans for 
     * the specified Web Service Endpoint key on the specified server instance.
     *
     * @param webServiceEndpointKey     Web Service Endpoint Key
     * @param serverName  Name of the server instance. To specify 
     *                    all server instances, use
     *                    {@link #ALL_SERVERS}
     *
     * @return Set of WebServiceEndpoint runtime mbeans
     */
    public Set<WebServiceEndpoint> getWebServiceEndpointSet(
        Object webServiceEndpointKey, String serverName);


    /**
     * Returns list of configured web service registry access points.
     *
     * @return  list of configured web service registry access points
     */
    public String[] listRegistryLocations();

    /**
     * Publishes the WSDL of webService to the registries specified by the 
     * JNDI Names
     *
     * @param registryLocations array of jndi names of the connector resources 
     * pointing to different registries
     *
     * @param webServiceEndpointKey web service endpoint(key) whose WSDL has to 
     * be published; 
     * format is appName#moduleName#webserviceName
     * This is opaque, as returned by the method getWebServiceEndpointKeys
     *
     * @param optional map contains the following:
     * <pre>
     *    lbhost, host name or ip address of the loadbalancer where this 
     *            service is publicly exposed
     *    lbport, loadbalancer port used to expose this service
     *    lbsslport, secure port in loadbalancer used to expose this service
     *    categories, Categories under which this web service endpoint should be
     *                published. This is an array of Strings.
     *    description, Description of the web service endpoint
     *    organization, Name of the Organization that the web service should be
     *                  published under.
     * </pre>
     */
    public void publishToRegistry (String[] registryLocations, 
                Object webServiceEndpointKey, Map<String,String> optional);

    /**
     * Unpublish WSDL of the specified webservice from the registries specified 
     * via their JNDI names
     *
     * @param registryLocations  array of jndi names of the connector resources
     * pointing to different registries
     *
     * @param webServiceEndpointKey web service endpoint(key) whose WSDL has to 
     * be published; format is appName#moduleName#webserviceName
     * This is opaque, as returned by the method getWebServiceEndpointKeys
     */
    public void unpublishFromRegistry(String[] registryLocations, 
            Object webServiceEndpointKey);

    /*
     * Given the jndi name of the registry, this undeploys the connection pool created for
     * it. The Resource Adapter associated is not undeployed lest other connection pools 
     * are referencing it.
     */
    public void removeRegistryConnectionResources (String jndiNameOfRegistry);
  
   /**
     * Adds registry specific resources to the domain.
     * Adds a connector connection pool and then proceeds to add a connector
     * resource
     *
     * @param jndiName of the connector-resource that points to the registry
     * @param description of the connector-resource and the connector-connection-pool name
     * @param type type of registry: {@link #UDDI_KEY}, {@link #EBXML_KEY} 
     * @param properties a map of key, value pair that encapsulate the properties
     * of the connection pool that connects to the registry.  Properties are
     * {@link #PUBLISH_URL_KEY}, {@link #QUERY_URL_KEY}, {@link #USERNAME_KEY},{@link #PASSWORD_KEY}
     */
    public void addRegistryConnectionResources(String jndiName,
            String description, String type, Map<String, String> properties);
            
    /**
     * A constant to denote all servers.
     */
    public final static String ALL_SERVERS = "*";

    /**
     * host name or ip address of the loadbalancer where this 
     * service is publicly exposed
     */
     public final static String LB_HOST_KEY = "LBHost";
 
    /**
     * loadbalancer port used to expose this service
     */
     public final static String LB_PORT_KEY = "LBPort";

    /**
     * secure port in loadbalancer used to expose this service
     */
     public final static String LB_SECURE_PORT = "LBSecurePort";

    /**
     * Categories under which this web service endpoint should be published.
     * This is an array of Strings.
     */
     public final static String CATEGORIES_KEY = "Categories";

    /**
     * Description of the web service endpoint
     */
     public final static String DESCRIPTION_KEY = "Description";

    /**
     * Organization Name under which the webservice should be
     * published. Required for a UDDI registry.
     */
     public final static String ORGANIZATION_KEY = "Organization";

     /**
      * URL of the registry where publish requests are dispatched
      * This is converted internally by the connector to 
      * JAXR implementation specific property 
      * <i>javax.xml.registry.lifeCycleManagerURL</i>
      */
     public static final String PUBLISH_URL_KEY = "LifeCycleManagerURL";

     /*
      * URL of the registry where query requests are dispatched
      * This is converted internally by the connector to 
      * JAXR implementation specific property 
      * <i>javax.xml.registry.queryManagerURL</i>
      */
     public static final String QUERY_URL_KEY = "QueryManagerURL";


     /**
      * Username to publish to registry
      */
     public static final String USERNAME_KEY = "username";

     /**
      * Password to publish to registry
      */
     public static final String PASSWORD_KEY = "password";

     /*
      * UDDI Key
      */
     public static final String UDDI_KEY = "uddi";

     /*
      * UDDI Key
      */
     public static final String EBXML_KEY = "ebxml";


     
}
