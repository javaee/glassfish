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

package com.sun.enterprise.management.ext.wsmgmt;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

import java.io.Serializable;

import javax.management.ObjectName;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;

import com.sun.appserv.management.j2ee.J2EETypes;

import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.TypeCast;

import com.sun.appserv.management.ext.wsmgmt.WebServiceMgr;
import com.sun.appserv.management.ext.wsmgmt.WebServiceEndpointInfo;

import com.sun.enterprise.management.support.AMXImplBase;

import com.sun.enterprise.admin.wsmgmt.WebServiceMgrBackEnd;



/**
 * Manager MBean for web services. This enumerates the list of the web services
 * deployed in the domain. For each web service, detailed information can be
 * obtained.
 */
public class WebServiceMgrImpl extends AMXImplBase
        // implements WebServiceMgr
{
    /**
     * Default constructor for WebServiceMgrImpl
     */
    public WebServiceMgrImpl() {
    }
    
    /**
     * Returns the Group information of this MBean.
     *
     * @return the group name (AMX.GROUP_OTHER)
     */
    public final String getGroup() {
        return( AMX.GROUP_OTHER );
    }
    
    /**
     * Returns a Map containing web services and the fully qualified name for
     * each web service. This fully qualified name must be used to get more
     * details about this web service.
     *
     * @return Map of web service name and its fully qualified name
     */
    public Map<Object,String> getWebServiceEndpointKeys()
    {
        final Map<?,?>   m = WebServiceMgrBackEnd.getManager().getWebServicesMap();
        
        final Map<Object,String> result =
            TypeCast.checkMap( m, Object.class, String.class );
        
        return result;
    }
    
    /**
     * Return WebServiceInfo for a web service.
     *
     * @param name  Fully qualified name of the web service
     *
     * @return WebServiceInfo for a web service
     */
        public Map<String, Serializable>
    getWebServiceEndpointInfo(Object name)
    {
        if ( name instanceof String)
        {
            return TypeCast.asMap(
                WebServiceMgrBackEnd.getManager().getWebServiceInfoMap((String)name) );
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }
    
    
    
    /**
     * Returns the set of WebServiceEndpoint runtime mbeans for the specified
     * Web Service Endpoint name on the specified server instance.
     *
     * @param webServiceInfoKey     Web Service Info Key
     * @param sName            Name of the server instance
     *
     * @return Set of WebServiceEndpoint runtime mbeans
     */
    public Set<ObjectName> getWebServiceEndpointObjectNameSet(
            Object webServiceInfoKey , String sName) {
        
        String objNamePat = WebServiceMgrBackEnd.getManager().
                getWebServiceEndpointObjectNames(webServiceInfoKey, sName);
        if (objNamePat == null) {
            return new HashSet<ObjectName>();
        } else {
            return getQueryMgr().queryPropsObjectNameSet(objNamePat);
        }
    }
    
    /**
     * Returns list of configured web service registry access points.
     */
    public String[] listRegistryLocations() {
        return WebServiceMgrBackEnd.getManager().listRegistryLocations();
    }
    
    /**
     * Publishes the WSDL of webService to the registries specified by the
     * JNDI Names
     *
     * @param registryLocations  array of jndi names of the connector resources
     * pointing to different registries
     *
     * @param webServiceEndpointKey web service endpoint(key) whose WSDL has to
     * be published;
     * format is appName#moduleName#webserviceName
     * This is opaque, as returned by the method getWebServiceEndpointKeys
     *
     * @param optional  optional map contains the following:
     * <pre>
     *    lbhost, host name or ip address of the loadbalancer where this
     *            service is publicly exposed
     *    lbport, loadbalancer port used to expose this service
     *    lbsslport, secure port in loadbalancer used to expose this service
     *    categories, Categories under which this web service endpoint should be
     *                published. This is an array of Strings.
     *    description, Description of the web service endpoint
     * </pre>
     */
    public void publishToRegistry(String[] registryLocations,
            Object webServiceEndpointKey, Map<String,String> optional) {
        WebServiceMgrBackEnd.getManager().
                publishToRegistry(registryLocations, webServiceEndpointKey,
                optional);
    }
    
    /**
     * Unpublish WSDL of the specified webservice from the registries specified
     * via their JNDI names
     * @param registryLocations   array of jndi names of the connector resources
     * pointing to different registries
     * @param webServiceEndpointKey web service endpoint(key) whose WSDL has to
     * be published;
     * format is appName#moduleName#webserviceName
     * This is opaque, as returned by the method getWebServiceEndpointKeys
     */
    public void unpublishFromRegistry(String[] registryLocations,
            Object webServiceEndpointKey) {
        WebServiceMgrBackEnd.getManager().
                unpublishFromRegistry(registryLocations,
                webServiceEndpointKey);
    }
    /**
     * Removes the registry specific resources  from the domain.
     * Peeks at the connector-resource element to obtain the
     * connector-connection-pool name. Using this pool name, removes the
     * connector-connection-pool, proceeds further to remove the
     * connector-resource
     * @param jndiNameOfRegistry whose resources are to be removed from the domain
     */
    public void removeRegistryConnectionResources(String jndiNameOfRegistry){
        WebServiceMgrBackEnd.getManager().removeRegistryConnectionResources(jndiNameOfRegistry);
    }
    /**
     * Adds registry specific resources to the domain.
     * Adds a connector connection pool and then proceeds to add a connector
     * resource
     *
     * @param jndiName of the connector-resource that points to the registry
     *
     * @param description of the connector-resource and the connector-connection
     * -pool name
     *
     * @param type type of registry. 
     *
     * {@link com.sun.appserv.management.ext.wsmgmt.WebServiceMgr#UDDI_KEY}
     * {@link com.sun.appserv.management.ext.wsmgmt.WebServiceMgr#EBXML_KEY}
     *
     * @param properties a map of key, value pair that encapsulate the properties
     * of the connection pool that connects to the registry.  Properties are
     *
     * {@link WebServiceMgr#PUBLISH_URL_KEY}
     * {@link WebServiceMgr#QUERY_URL_KEY}
     * {@link WebServiceMgr#USERNAME_KEY}
     * {@link WebServiceMgr#PASSWORD_KEY}
     */
    public void addRegistryConnectionResources(String jndiName,
            String description, String type , Map<String, String> properties) {
        WebServiceMgrBackEnd.getManager().
                addRegistryConnectionResources(jndiName, description, type, properties);
    }    
}

