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
package com.sun.enterprise.admin.wsmgmt;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import javax.management.ObjectName;

import com.sun.appserv.management.ext.wsmgmt.WebServiceEndpointInfo;
import com.sun.appserv.management.ext.wsmgmt.WebServiceEndpointInfoImpl;
import com.sun.appserv.management.ext.wsmgmt.WebServiceMgr;
import com.sun.enterprise.admin.wsmgmt.registry.RegistryAccessObject;
import com.sun.enterprise.admin.wsmgmt.registry.RegistryAccessObjectImpl;
import com.sun.enterprise.admin.wsmgmt.registry.ConfigHelper;

import com.sun.enterprise.admin.wsmgmt.repository.spi.RepositoryFactory;
import com.sun.enterprise.admin.wsmgmt.repository.spi.RepositoryProvider;
import com.sun.enterprise.admin.wsmgmt.repository.spi.WebServiceInfoProvider;

import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.EjbModule;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.admin.server.core.AdminService;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.j2ee.J2EETypes;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.util.jmx.JMXUtil;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.Application;

import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import com.sun.enterprise.config.serverbeans.TransformationRule;
import com.sun.enterprise.config.serverbeans.WebServiceEndpoint;
import com.sun.enterprise.webservice.monitoring.Endpoint;

import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import java.util.regex.*;

/**
 * Manager MBean for web services. This enumerates the list of the web services
 * deployed in the domain. For each web service, detailed information can be
 * obtained.
 */
public class WebServiceMgrBackEnd {
    /**
     * Default constructor for WebServiceMgrBackEnd
     */
    private WebServiceMgrBackEnd() {
        try {
            repPvdr = RepositoryFactory.getRepositoryProvider();
            wsInfoPvdr = RepositoryFactory.getWebServiceInfoProvider();
        } catch (Exception e) {
            _logger.fine("WebServiceInfoProvider could not be instantiated: " +
                    e.getMessage());
            // log warning
        }
        wsInfoMap = new WeakHashMap();
        try {
            InstanceEnvironment ienv =
                    ApplicationServer.getServerContext().getInstanceEnvironment();
            appsMgr = new AppsManager(ienv);
        } catch (Exception e) {
            _logger.fine("AppsManager could not be instantiated: " +
                    e.getMessage());
            // log exception in FINE level. This excpetion should never occur.
        }
    }
    
    public synchronized static WebServiceMgrBackEnd getManager() {
        if ( mgrImpl == null) {
            mgrImpl = new WebServiceMgrBackEnd();
        }
        return mgrImpl;
    }
    
    /**
     * Returns a Map containing web services and the fully qualified name for
     * each web service. This fully qualified name must be used to get more
     * details about this web service.
     *
     * @return Map of web service name and its fully qualified name
     */
    public Map getWebServicesMap() {
        Map wsNameMap = new HashMap();
        
        // Getting all the modules containing web service end points
        Map wsModMap = repPvdr.getWebServiceModules();
        // no modules found, return empty map
        if ( wsModMap == null ){
            return wsNameMap;
        }
        
        // If underlying Set can not be retrieved return
        // return empty map
        Set wsModSet = wsModMap.entrySet();
        if ( wsModSet == null) {
            return wsNameMap;
        }
        
        // iterate through each module and add web services to the map
        Iterator wsModItr = wsModSet.iterator();
        while (wsModItr.hasNext() ) {
            Map.Entry wsMapEntry = (Map.Entry) wsModItr.next();
            String descLoc = (String) wsMapEntry.getKey();
            Map propMap = (Map) wsMapEntry.getValue();
            List wsInfoListInMod = null;
            String appName = (String)
            propMap.get(WebServiceInfoProvider.APP_ID_PROP_NAME);
            String bundleName = (String)
            propMap.get(WebServiceInfoProvider.BUNDLE_NAME_PROP_NAME);
            
            Map nMap = getFromCache(appName, bundleName);
            if (nMap != null) {
                wsNameMap.putAll(nMap);
                continue;
            }
            
            try {
                wsInfoListInMod = wsInfoPvdr.getWebServiceInfo(descLoc,
                        propMap);
            } catch ( Exception e) {
                // log warnin
                String msg =_stringMgr.getString("ModInfoNotFound",appName
                        + " : " + e.getMessage());
                _logger.log(Level.WARNING, msg);
            }
            
            
            if ( wsInfoListInMod != null ) {
                Iterator wsInfoItrInMod = wsInfoListInMod.iterator();
                
                while (wsInfoItrInMod.hasNext() ) {
                    WebServiceEndpointInfo wsInfo =
                            (WebServiceEndpointInfo) wsInfoItrInMod.next();
                    
                    // add this web service name and its fully qualified name
                    // the map that is being returned
                    
                    String wsFQN =  getFullyQualifiedName(appName, bundleName,
                            wsInfo.isAppStandaloneModule(), wsInfo.getName());
                    wsNameMap.put(wsFQN, wsInfo.getName());
                    
                    // update the internal weak hashmap
                    updateCache(wsFQN, wsInfo);
                }
            }
        }
        return wsNameMap;
    }
    
    private synchronized void updateCache( String wsFQN,
            WebServiceEndpointInfo wsInfo) {
        wsInfoMap.put(wsFQN, wsInfo);
    }
    
    /**
     * Removes the web service end point informationt that is cached by the
     * Web Services Management module. This should be only used during
     * un-deploy. The only expeced user of this method is AppServDELImpl java
     * class.
     *
     * @param appName application this being un-deployed
     */
    public synchronized void removeFromCache(String appName) {
        
        Iterator itr = wsInfoMap.entrySet().iterator();
        
        if (itr == null) {
            return;
        }
        while(itr.hasNext()) {
            Map.Entry entry = (Map.Entry) itr.next();
            String fqn = (String) entry.getKey();
            WebServiceEndpointInfo wsInfo =
                    (WebServiceEndpointInfo) entry.getValue();
            if ( isFQNMatch( fqn, appName) ) {
                itr.remove();
            }
        }
    }
    
    private Map getFromCache(String appName, String bundleName) {
        Iterator itr = wsInfoMap.entrySet().iterator();
        
        Map nMap = null;
        if (itr == null) {
            return null;
        }
        while(itr.hasNext()) {
            Map.Entry entry = (Map.Entry) itr.next();
            String fqn = (String) entry.getKey();
            WebServiceEndpointInfo wsInfo = (WebServiceEndpointInfo) entry.getValue();
            if ( isFQNMatch( fqn, appName, bundleName) ) {
                if ( nMap == null) {
                    nMap = new HashMap();
                }
                nMap.put(fqn, wsInfo.getName());
            }
        }
        return nMap;
    }
    
    private boolean isFQNMatch(String fqn, String appName, String bundleName) {
        String matchFqn;
        
        if ( bundleName != null) {
            matchFqn = appName + NAME_SEPERATOR + bundleName + NAME_SEPERATOR;
        } else {
            matchFqn = appName + NAME_SEPERATOR ;
        }
        
        return ( fqn.startsWith(matchFqn));
    }
    
    private boolean isFQNMatch(String fqn, String appName) {
        
        String matchFqn = appName + NAME_SEPERATOR ;
        
        return ( fqn.startsWith(matchFqn));
    }
    
    public String getFullyQualifiedName( String moduleID, String wsName) {
        return   moduleID + NAME_SEPERATOR + wsName;
    }
    
    public String getModuleName( String epName) {
        String[] sStrs = epName.split(NAME_SEPERATOR);
        if ( sStrs != null) {
            return sStrs[0];
        } else {
            return null;
        }
    }
    
    public String getEndpointName( String epName) {
        String[] sStrs = epName.split(NAME_SEPERATOR);
        if ( sStrs != null) {
            return sStrs[sStrs.length -1];
        } else {
            return null;
        }
    }
    
    public String getFullyQualifiedName( String appName, String bundleName,
            boolean isStandAlone, String wsName) {
        
        if (isStandAlone) {
            return   appName + NAME_SEPERATOR +
                    wsName;
        } else {
            return appName + NAME_SEPERATOR + bundleName + NAME_SEPERATOR +
                    wsName;
        }
    }
    
    public String getFullyQualifiedName( String appName, String bundleName,
            String wsName) {
        
        if (appName == null) {
            return  bundleName + NAME_SEPERATOR +
                    wsName;
        } else {
            return appName + NAME_SEPERATOR + bundleName + NAME_SEPERATOR +
                    wsName;
        }
    }
    
    public String getFullyQualifiedName(Endpoint ep) {
        
        com.sun.enterprise.deployment.WebServiceEndpoint wse=ep.getDescriptor();
        String epName = null;
        if (wse != null) {
            epName = wse.getEndpointName();
        }
        BundleDescriptor bundle = wse.getBundleDescriptor();
        Application app = bundle.getApplication();
        
        String fqn = getFullyQualifiedName( app.getRegistrationName() ,
                bundle.getModuleDescriptor().getArchiveUri(),
                app.isVirtual(), epName);
        return fqn;
    }
    
    /**
     * Return WebServiceEndpointInfo for a web service.
     *
     * @param name  Fully qualified name of the web service
     *
     * @return WebServiceEndpointInfo for a web service
     */
    public Map getWebServiceInfoMap(String name) {
        WebServiceEndpointInfo wsInfo = getWebServiceInfo(name);
        if (wsInfo == null) {
            return null;
        } else {
            return ((WebServiceEndpointInfoImpl)wsInfo).asMap();
        }
    }
    /**
     * Return WebServiceEndpointInfo for a web service.
     *
     * @param name  Fully qualified name of the web service
     *
     * @return EndpointURI for a endpoint
     */
    
    public String getEndpointURI(String name) {
        java.util.Map endpointInfoMap = getWebServiceInfoMap(name);
        if(endpointInfoMap != null)
            return (String)endpointInfoMap.get(WebServiceEndpointInfo.END_POINT_URI_KEY);
        else
            return null;
    }
    
    private WebServiceEndpointInfo getWebServiceInfo(String name) {
        WebServiceEndpointInfo wsInfo = (WebServiceEndpointInfo) wsInfoMap.get(name);
        // call expired, load its descriptors
        // XXX this should be optimized
        if (wsInfo == null) {
            getWebServicesMap();
            wsInfo = (WebServiceEndpointInfo) wsInfoMap.get(name);
        }
        return wsInfo;
    }
    
    /**
     * Returns RegistryAccessObject; RegistryAccessObject encapsulates access
     * to the underlying registries
     * @return RegistryAccessObject
     */
    public RegistryAccessObject getRegistryAccessObject() {
        return new RegistryAccessObjectImpl();
    }
    
    /**
     * Returns the object names of the mbeans matching the criteria specified
     * for WebServiceEndpoint MBeans.
     *
     * @param endpointKey    Fully qualified key for web service endpoint
     * @param serverName     Name of the server instance
     *
     * @return Properties of WebServiceEndpoint MBean object names.
     */
    public String getWebServiceEndpointObjectNames(Object key,
            String serverName) {
        if ((key == null) ||  !( key instanceof String) ) {
            String msg =
                    "Key passed to WebServiceEndpointObjectNames must be of type String";
            _logger.log(Level.FINE, msg);
            return null;
        }
        
        WebServiceEndpointInfo wsInfo = getWebServiceInfo( (String)key);
        if ( wsInfo == null) {
            return null;
        } else {
            String implType = wsInfo.getServiceImplType();
            boolean isEjb = false;
            if ( implType.equals("EJB")) {
                isEjb = true;
            }
            String appName = wsInfo.getAppID();
            String modName = wsInfo.getBundleName();
            if(wsInfo.isAppStandaloneModule() == true) {
                // appId is the module name
                modName = appName;
                // J2EE application name is null for stand alone modules
                appName = null;
            }
            return getWSObjectNames(appName,
                    modName, wsInfo.getName(),
                    getWebServiceEndpointContextRoot( (String)key),isEjb, serverName);
        }
    }
    
    private String getWebServiceEndpointContextRoot(String wsFQN) {
        
        String[] fqns = wsFQN.split(NAME_SEPERATOR);
        ConfigContext configCtx = AdminService.getAdminService().
                getAdminContext().getAdminConfigContext();
        ConfigBean cb = null;
        
        try {
            cb = ApplicationHelper.findApplication(configCtx, fqns[0]);
        } catch( Exception e) {
            String msg = "Could not find a deployed application/module by name "
                    + fqns[0] + " : " + e.getMessage();
            _logger.log(Level.FINE, msg);
            return null;
        }
        
        boolean isStandalone = false, isEjb = false;
        String appId = null, modId = null, epName = null,ctxRoot = null;
        
        if (cb instanceof  J2eeApplication) {
            assert( fqns.length == 3);
            return null;
        } else if (cb instanceof EjbModule) {
            assert( fqns.length == 2);
            return null;
        } else if (cb instanceof WebModule) {
            assert( fqns.length == 2);
            return ((WebModule)cb).getContextRoot();
        }  else {
            return null;
        }
    }
    
    private String getWSObjectNames(String appId, String modId,
            String epName, String ctxRoot, boolean isEjb, String serverName) {
        
        String requiredProps      =
                Util.makeRequiredProps( J2EETypes.WEB_SERVICE, epName  );
        
        String vsProps = null;
        if ( !isEjb) {
            String secPart = ctxRoot;
            if ( secPart == null) {
                Application app = null;
                try {
                    app = appsMgr.getDescriptor(appId);
                } catch (ConfigException ce) {
                    // log a warning
                    _logger.fine("The descriptor for application " + appId +
                            " could not be loaded: " + ce.getMessage());
                }
                if (app != null) {
                    WebBundleDescriptor wbd
                            = app.getWebBundleDescriptorByUri(modId);
                    if (wbd != null) {
                        secPart = wbd.getContextRoot();
                    }
                }
            }
            if (secPart == null) {
                // not getting the context root from the descriptor
                // Descriptor must not be loaded
                return null;
            }
            if (secPart.charAt(0) != '/') {
                secPart = "/" + secPart;
            }
            String compositeName="//"+ DEFAULT_VIRTUAL_SERVER + secPart;
            vsProps  = Util.makeProp( J2EETypes.WEB_MODULE, compositeName );
        } else {
            vsProps      = Util.makeProp( J2EETypes.EJB_MODULE, modId );
        }
        
        requiredProps = Util.concatenateProps(requiredProps, vsProps);
        
        String serverProp = null;
        
        if (( serverName != null) &&  ( !serverName.equals("*"))) {
            serverProp = Util.makeProp( J2EETypes.J2EE_SERVER, serverName );
        }
        
        String  props   = serverProp;
        
        if ( appId == null ) {
            appId = AMX.NULL_NAME;
        }
        final String    applicationProp = Util.makeProp(
                J2EETypes.J2EE_APPLICATION, appId );
        
        if ( props != null) {
            props   = Util.concatenateProps( props, applicationProp );
        } else {
            props = applicationProp;
        }
        
        String finalProps = Util.concatenateProps(requiredProps, props);
        
        return finalProps;
    }
    
    public String[] listRegistryLocations(){
        RegistryAccessObject rao = this.getRegistryAccessObject();
        return rao.listRegistryLocations();
    }
    
    public void publishToRegistry(String[] registryLocations,
            Object webServiceEndpointKey, Map optional){
        
        String webServiceName = (String)webServiceEndpointKey;  
        
        boolean published = false;
        try{
            RegistryAccessObject rao = this.getRegistryAccessObject();
            _logger.fine("WebServiceMgrBackEnd.publishToRegistry: publishing" +
                    "WebService "+webServiceName);
            
            String lbhost = (String)optional.get(WebServiceMgr.LB_HOST_KEY);
            
            if (lbhost == null){
                lbhost = getHostAddress();
                _logger.fine("WebServiceMgrBackend.publishToRegistry: Load " +
                        "Balancer Host is unspecified setting to "+ lbhost);
            }
            String wsdl = getWSDL (lbhost, webServiceName);
            if(wsdl == null){
                _logger.log(Level.SEVERE,
                        "registry.wsdl_absent_publish_failure",
                        webServiceName);
                            
                throw new RuntimeException (" Could not retreive WSDL for "+
                        webServiceName);
            }
            
            String lbport = (String)optional.get(WebServiceMgr.LB_PORT_KEY);
            String lbsslport = (String)optional.get(WebServiceMgr.LB_SECURE_PORT);
            
            if (lbport == null && lbsslport == null) {
                lbport = getPort(webServiceName, true, wsdl);
                if (lbport == null)
                    lbport = MINUS_ONE;
                lbsslport = getPort(webServiceName, false, wsdl);
                if (lbsslport == null)
                    lbsslport = MINUS_ONE;
                _logger.fine("WebServiceMgrBackend.publishToRegistry: Load " +
                        "Balancer Port Unspecified setting to default values  " +
                        "LoadBalancer Port = "+ lbport +
                        " LoadBalancer SSL Port = "+lbsslport);
            } else if (lbsslport == null){
                _logger.fine("WebServiceMgrBackend.publishToRegistry: Load " +
                        "Balancer SSL Port is unspecified setting to "+ MINUS_ONE);
                lbsslport = MINUS_ONE;
            } else if (lbport == null){
                _logger.fine("WebServiceMgrBackend.publishToRegistry: Load " +
                        "Balancer Host is unspecified setting to "+ MINUS_ONE);
                lbport = MINUS_ONE;
            }
            int ilbport = -1;
            int ilbsslport = -1;
            try{
                ilbport = Integer.valueOf(lbport);
            } catch  (NumberFormatException e){
                _logger.fine("WebServiceMgrBackend.publishToRegistry: Load " +
                        "Balancer Port is not a number. Setting to "+ MINUS_ONE);
                ilbport = -1;
            }
            try{
                if(lbsslport !=null)
                    ilbsslport = Integer.valueOf(lbsslport);
            }catch (NumberFormatException e){
                _logger.fine("WebServiceMgrBackend.publishToRegistry: Load " +
                        "Balancer SSL Port is not a number. Setting to "+ MINUS_ONE);
                ilbsslport = -1;
            }
            String categoriesList = (String)optional.get(WebServiceMgr.CATEGORIES_KEY);
            String[] categories = null;
            if (categoriesList != null){
                java.util.StringTokenizer tokenizer =
                        new java.util.StringTokenizer(categoriesList, ",");
                List<String> list = new ArrayList<String>();
                while (tokenizer.hasMoreElements()){
                    list.add((String)tokenizer.nextToken());
                }
                categories = new String[list.size()];
                categories = list.toArray(categories);
            }
            String organization = (String) optional.get(WebServiceMgr.ORGANIZATION_KEY);
            if (organization == null){
                _logger.fine("WebServiceMgrBackend.publishToRegistry: Organization " +
                        " unspecified. Setting a default organization "+
                        DEFAULT_ORGANIZATION);
                organization = DEFAULT_ORGANIZATION;
            }
            String description = (String) optional.get(WebServiceMgr.DESCRIPTION_KEY);
            if (description == null){
                _logger.fine("WebServiceMgrBackend.publishToRegistry: Web Service " +
                        " Description unspecified. Setting a default description: "+
                        DEFAULT_DESCRIPTION);
                description = DEFAULT_DESCRIPTION;
            }
            published = rao.publish(registryLocations, webServiceName, lbhost,
                    ilbport, ilbsslport, categories, organization, description, 
                    wsdl);
            if (published == false){
                String errorMessage =
                        _stringMgr.getString("WebServiceMgrBackend.PublishFailure",
                        webServiceName);
                _logger.log(Level.SEVERE, "registry.publish_failure", webServiceName);
                throw new RuntimeException(errorMessage);
            } else {
                String message =
                        _stringMgr.getString("WebServiceMgrBackend.PublishSuccess",
                        webServiceName);
                _logger.log(Level.INFO, message);
            }
        } catch (RuntimeException re){
            _logger.log(Level.SEVERE, "registry.publish_failure_exception", re);
            throw re;
        } catch (Exception e) {
            _logger.log(Level.SEVERE, "registry.publish_failure", webServiceName);
            _logger.log(Level.SEVERE, "registry.publish_failure_exception", e);
            RuntimeException r =  new RuntimeException(e);
            throw r;
        }
        
    }
    /*
     * boolean unsecure if true, will search for http address in the webservice
     * else will search for https in the webservice
     */
    String getPort(String webServiceName,  boolean unsecure, String wsdlFile) {
        
        String port = DEFAULT_PORT;
        // Extract web service port from the WSDL file.
        String http = (unsecure == true)? "http":"https";
        String pattern = "soap:address.*location.*"+http+":.*:[0-9]+";
        Pattern soap_address_pattern = Pattern.compile(pattern);
        Matcher soap_address_matcher = soap_address_pattern.matcher(wsdlFile);
        boolean soapAddress = soap_address_matcher.find();
        if (soapAddress){
            String soap_address = soap_address_matcher.group();
            Pattern port_pattern = Pattern.compile(":[0-9]+");
            Matcher port_matcher = port_pattern.matcher(soap_address);
            port_matcher.find();
            String portWithColon = port_matcher.group();
            port = portWithColon.substring(1);
        } else
            port =  null;
       
        return port;
    }
    
    String getWSDL(String host, String webServiceName){
        String port = DEFAULT_PORT;
        ConfigHelper ch = ConfigHelper.getInstanceToQueryRegistryLocations();
        port = ch.getInstancePort ();
        if (port == null)
            port = DEFAULT_PORT; // sanity
        
        String wsdl = null;
        String uri = null;
        WebServiceEndpointInfo wsInfo = getWebServiceInfo(webServiceName);
        
        if (wsInfo == null) {
            // should never happen.
            throw new RuntimeException("Could not retreive WebService" +
                    "Information. Cannot publish web service  ");
        } else      {
            uri = wsInfo.getEndpointURI();
        }
        String wsdlurl = "http://" + host + ":"+ port +"/"+ uri+"?wsdl";
        try{
            URL url = new URL(wsdlurl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.connect();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer wsdlBuf = new StringBuffer();
            String line = null;
            while((line=reader.readLine())!=null){
                wsdlBuf.append(line);
            }
            reader.close();
            wsdl = wsdlBuf.toString();
        }catch (MalformedURLException mue){
            _logger.log (Level.WARNING, " Malformed URL for Web Service"+
                    webServiceName, mue);
        }catch (IOException ioe){
            _logger.log(Level.WARNING, " Cannot retrieve WSDL for Web Service "+
                    webServiceName, ioe);
        }
        return wsdl;
    }
        
    private String getHostAddress() {
        try {
            java.net.InetAddress iaddr = java.net.InetAddress.getLocalHost();
            return iaddr.getHostAddress();
        } catch (Exception e) {
            return LOCAL_HOST;
        }
    }
    public void unpublishFromRegistry(String[] registryLocations,
            Object webServiceEndpointKey){
        
        String webServiceName = (String)webServiceEndpointKey;
        boolean unpublished = false;
        try{
            RegistryAccessObject rao = this.getRegistryAccessObject();
            _logger.fine("WebServiceMgrBackEnd.unpublishFromRegistry:" +
                    "unpublishing web service = "+ webServiceName);
            unpublished = rao.unpublishFromRegistry(registryLocations, webServiceName);
            if(unpublished == false){
                String errorMessage =
                        _stringMgr.getString("WebServiceMgrBackend.UnpublishFailure",
                        webServiceName);
                
                _logger.log(Level.SEVERE, "registry.unpublish_failure", webServiceName);
                throw new RuntimeException(errorMessage);
            } else{
                _logger.log(Level.INFO, "registry.unpublish_success", webServiceName);
            }
        } catch (RuntimeException re){
            _logger.log(Level.SEVERE, "registry.unpublish_failure_exception", re);
            throw re;
        }catch (Exception e){
            String errorMessage =
                    _stringMgr.getString("WebServiceMgrBackend.UnpublishFailure",
                    webServiceName);
            
            _logger.log(Level.SEVERE, "registry.unpublish_failure", webServiceName);
            _logger.log(Level.SEVERE, "registry.unpublish_failure_exception",e);
            throw new RuntimeException(e);
        }
    }
    /**
     * Removes the registry specific resources  from the domain.
     * Peeks at the connector-resource element to obtain the
     * connector-connection-pool name. Using this pool name, removes the
     * connector-connection-pool, proceeds further to remove the
     * connector-resource
     * @param jndiNameOfRegistry whose resources are to be removed from the domain
     */
    public void removeRegistryConnectionResources(String jndiNameOfRegistry) {
        ConfigHelper configHelper = ConfigHelper.getInstanceToDeleteRegistryResources();
        configHelper.removeRegistryConnectionResources(jndiNameOfRegistry);
        return;
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
     * @param type of the registry
     * {@link com.sun.appserv.management.WebServiceMgr#UDDI_KEY}
     * {@link com.sun.appserv.management.WebServiceMgr#EBXML_KEY}
     *
     * @param properties a map of key, value pair that encapsulate the properties
     * of the connection pool that connects to the registry.  Properties are
     *
     * {@link com.sun.appserv.management.WebServiceMgr#PUBLISH_URL_KEY}
     * {@link com.sun.appserv.management.WebServiceMgr#QUERY_URL_KEY}
     * {@link com.sun.appserv.management.WebServiceMgr#USERNAME_KEY}
     * {@link com.sun.appserv.management.WebServiceMgr#PASSWORD_KEY}
     *
     */
    public void addRegistryConnectionResources(String jndiName,
            String description, String type, Map<String, String> properties){
        ConfigHelper configHelper = ConfigHelper.getInstanceToDeleteRegistryResources();
        configHelper.addRegistryConnectionResources(jndiName, description, type,
                properties);
    }
    
    
    /**
     * This method removes the transformation rule file from the repository
     * directory.
     *
     * @param appId         application or module Id
     * @param epName        End point's name
     * @param ruleName      Transformation rule's name
     *
     */
    public void removeFileFromRepository(String appId,String epName, String
            ruleName) {
        
        if (( appId == null) || ( epName == null) || (ruleName == null)) {
            String msg = "Either appId or epName or ruleName passed is null "
                    + appId;
            _logger.log(Level.FINE, msg);
            throw new IllegalArgumentException();
        }
        
        ConfigContext configCtx = AdminService.getAdminService().
                getAdminContext().getAdminConfigContext();
        
        TransformationRule tRule = null;
        try {
            tRule = ApplicationHelper.findTransformationRule(configCtx,
                    appId,epName, ruleName);
        } catch( Exception e) {
            String msg = "Could not find a deployed application/module type "
                    + appId;
            _logger.log(Level.FINE, msg);
            throw new RuntimeException(e);
        }
        
        File ruleFile = new File(tRule.getRuleFileLocation());
        FileUtils.liquidate(ruleFile);
    }
    
    /**
     * This method moves the transformation rule file from the curLocation
     * (in some temp dir location) to generated/xml/<appormod>/<app_or_mod_name
     * directory.
     *
     * @param curLocation   current temp location of rule file
     * @param appId         application or module Id
     *
     * @return the new location of the rule file.
     */
    public String moveFileToRepository(String curLocation, String appId)
    throws IOException {
        
        if (( curLocation == null) || ( appId == null)) {
            String msg = "Either rule location or appId passed is null "
                    + appId;
            _logger.log(Level.FINE, msg);
            throw new IllegalArgumentException();
        }
        
        String appType = null;
        ConfigContext configCtx = AdminService.getAdminService().
                getAdminContext().getAdminConfigContext();
        
        try {
            appType = ApplicationHelper.getApplicationType(configCtx, appId);
        } catch( Exception e) {
            String msg = "Could not find a deployed application/module type "
                    + appId;
            _logger.log(Level.FINE, msg);
            throw new RuntimeException(e);
        }
        
        String rootDir = System.getProperty(
                SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);
        String finalLoc = null;
        
        File fin = new File(curLocation);
        if ( appType.equals(Applications.J2EE_APPLICATION) ) {
            finalLoc = rootDir + File.separator + PEFileLayout.GENERATED_DIR +
                    File.separator + PEFileLayout.XML_DIR + File.separator +
                    PEFileLayout.J2EE_APPS_DIR +
                    File.separator + appId + File.separator ;
        } else if (( appType.equals(Applications.EJB_MODULE)) ||
                ( appType.equals(Applications.WEB_MODULE) )) {
            finalLoc = rootDir + File.separator + PEFileLayout.GENERATED_DIR +
                    File.separator + PEFileLayout.XML_DIR + File.separator +
                    PEFileLayout.J2EE_MODULES_DIR +
                    File.separator + appId + File.separator;
        }
        
        
        File fout = new File(finalLoc + fin.getName());
        
        // check if the named file already exists, then choose a different name
        int fileIdx = 0;
        while ( fout.exists() ) {
            fout = new File( finalLoc + fileIdx + fin.getName());
            fileIdx++;
        }
        
        FileUtils.copy(fin, fout);
        FileUtils.liquidate(fin);
        
        return finalLoc + fin.getName();
    }
    
    public List getTransformationRuleConfigObjectNameList(
            String appId, String wsepName,Map<String,ObjectName> oNameMap) {
        
        if (oNameMap == null) {
            return null;
        }
        
        ArrayList retList = new ArrayList(oNameMap.size());
        ConfigContext configCtx = AdminService.getAdminService().
                getAdminContext().getAdminConfigContext();
        ConfigBean cb = null;
        
        try {
            cb = ApplicationHelper.findApplication(configCtx, appId);
        } catch( Exception e) {
            String msg = "Could not find a deployed application/module by name "
                    + appId;
            _logger.log(Level.FINE, msg);
            return null;
        }
        
        TransformationRule[] tRules = null;
        WebServiceEndpoint wsep = null;
        if (cb instanceof  J2eeApplication) {
            wsep = ((J2eeApplication)cb).getWebServiceEndpointByName(wsepName);
        } else if (cb instanceof EjbModule) {
            wsep = ((EjbModule)cb).getWebServiceEndpointByName(wsepName);
        } else if (cb instanceof WebModule) {
            wsep = ((WebModule)cb).getWebServiceEndpointByName(wsepName);
        }  else {
            return null;
        }
        if (wsep != null) {
            tRules = wsep.getTransformationRule();
        }
        for(int index=0; index < tRules.length; index++) {
            retList.add(oNameMap.get(tRules[index].getName()));
        }
        return retList;
    }
    
    /** PUBLIC VARIABLES */
    
    /**
     * Default virtual server, under which all the web service monitoring stats
     * are maintained
     **/
    public static final String DEFAULT_VIRTUAL_SERVER = "server";
    
    /** PRIVATE VARIABLES */
    
    private AppsManager appsMgr = null;
    private RepositoryProvider repPvdr;
    private WebServiceInfoProvider wsInfoPvdr;
    private WeakHashMap wsInfoMap;
    private final static String NAME_SEPERATOR = "#";
    private static WebServiceMgrBackEnd mgrImpl = null;
    
    private static final Logger _logger =
            Logger.getLogger(LogDomains.ADMIN_LOGGER);
    
    private static final StringManager _stringMgr =
            StringManager.getManager(WebServiceMgrBackEnd.class);
    
    private static final String LOCAL_HOST = "localhost";
    private static final String MINUS_ONE = "-1";
    private static final String DEFAULT_ORGANIZATION = "Sun Microsystems Inc";
    private static final String DEFAULT_DESCRIPTION = "Sun Java Application Server Web Service Default Description";
    private static final String DEFAULT_PORT = "8080";
    
    /**
     * WebServices backend has changed and the WSDL file is no longer available
     * Breaking Registry code that parses the WSDL file to locate the port
     * This is a workaround for BETA to specify the port as -D option. 
     * This should be fixed by FCS 
     **/
    private static final String WEB_SERVICES_WORKAROUND_PORT = 
            "com.sun.enterprise.admin.wsmgt.registry.workaroundPort";
}
