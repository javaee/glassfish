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

package com.sun.enterprise.web;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.enterprise.deployment.*;

import com.sun.enterprise.deployment.web.AppListenerDescriptor;
import com.sun.enterprise.deployment.web.ContextParameter;
import com.sun.enterprise.deployment.web.EnvironmentEntry;
import com.sun.enterprise.deployment.web.InitializationParameter;
import com.sun.enterprise.deployment.web.LoginConfiguration;
import com.sun.enterprise.deployment.web.MimeMapping;
import com.sun.enterprise.deployment.web.ServletFilter;
import com.sun.enterprise.deployment.web.ServletFilterMapping;
import com.sun.enterprise.deployment.web.SecurityConstraint;
import com.sun.enterprise.deployment.web.SecurityRoleReference;
import com.sun.enterprise.deployment.web.WebResourceCollection;

import com.sun.enterprise.web.WebModule;
import com.sun.enterprise.web.deploy.ContextEjbDecorator;
import com.sun.enterprise.web.deploy.ContextEnvironmentDecorator;
import com.sun.enterprise.web.deploy.ContextLocalEjbDecorator;
import com.sun.enterprise.web.deploy.ContextResourceDecorator;
import com.sun.enterprise.web.deploy.ErrorPageDecorator;
import com.sun.enterprise.web.deploy.FilterDefDecorator;
import com.sun.enterprise.web.deploy.LoginConfigDecorator;
import com.sun.enterprise.web.deploy.MessageDestinationDecorator;
import com.sun.enterprise.web.deploy.MessageDestinationRefDecorator;
import com.sun.enterprise.web.deploy.SecurityConstraintDecorator;
import com.sun.enterprise.web.deploy.SecurityCollectionDecorator;

import com.sun.logging.LogDomains;

import org.apache.catalina.Container;
import org.apache.catalina.Globals;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.deploy.ContextEjb;
import org.apache.catalina.deploy.ContextLocalEjb;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.deploy.MessageDestination;
import org.apache.catalina.deploy.MessageDestinationRef;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.util.CharsetMapper;
import org.apache.jasper.compiler.JspConfig;

/**
 * This class decorates all <code>com.sun.enterprise.deployment.*</code>
 * objects in order to make them usuable by the Catalina container. 
 * This avoid having duplicate memory representation of the web.xml (as well
 * as parsing the web.xml twice)
 * 
 * @author Jean-Francois Arcand
 */
public class TomcatDeploymentConfig{

    private static final Logger logger = LogDomains.getLogger(TomcatDeploymentConfig.class, LogDomains.WEB_LOGGER);
    
    
    /**
     * Empty constructor
     */
    public TomcatDeploymentConfig(){
    }
    
    
    /**
     * Configure a <code>WebModule</code> by applying default-web.xml 
     * information contained in the default <code>WebModule</code>. 
     *
     * @param webModule Web Module to be configured with default web module
     * @param defaultWebModule Default web module 
     */
    public static void configureWebModule(WebModule webModule,
                                          WebModule defaultWebModule){ 
                                              
        configureStandardContext(webModule,defaultWebModule);
        
        configureContextParam(webModule,defaultWebModule);

        configureApplicationListener(webModule,defaultWebModule);
        
        configureEjbReference(webModule,defaultWebModule);
 
        configureContextEnvironment(webModule,defaultWebModule);
        
        configureErrorPage(webModule,defaultWebModule);

        configureFilterDef(webModule,defaultWebModule);

        configureFilterMap(webModule,defaultWebModule);
        
        configureLoginConfig(webModule,defaultWebModule);
        
        configureMimeMapping(webModule,defaultWebModule);
        
        configureResourceRef(webModule,defaultWebModule);

        configureMessageDestination(webModule,defaultWebModule); 
        
        configureMessageRef(webModule,defaultWebModule); 
        
 
    }
    
    
    /**
     * Configure a <code>WebModule</code> by applying web.xml information
     * contained in <code>WebBundleDescriptor</code>. This astatic void calling
     * Tomcat 5 internal deployment mechanism by re-using the DOL objects.
     */
    public static void configureWebModule(WebModule webModule, 
                                          WebBundleDescriptor webModuleDescriptor) { 

        // When context root = "/"
        if ( webModuleDescriptor == null ){
            return;
        }
        
        webModule.setDisplayName(webModuleDescriptor.getDisplayName());
        webModule.setDistributable(webModuleDescriptor.isDistributable());
        webModule.setReplaceWelcomeFiles(true);
        
        configureStandardContext(webModule,webModuleDescriptor);
        
        configureContextParam(webModule,webModuleDescriptor);

        configureApplicationListener(webModule,webModuleDescriptor);
        
        configureEjbReference(webModule,webModuleDescriptor);
 
        configureContextEnvironment(webModule,webModuleDescriptor);
        
        configureErrorPage(webModule,webModuleDescriptor);

        configureFilterDef(webModule,webModuleDescriptor);

        configureFilterMap(webModule,webModuleDescriptor);
        
        configureLoginConfig(webModule,webModuleDescriptor);
        
        configureMimeMapping(webModule,webModuleDescriptor);
        
        configureResourceRef(webModule,webModuleDescriptor);

        configureMessageDestination(webModule,webModuleDescriptor);

        configureContextResource(webModule,webModuleDescriptor);

        configureSecurityConstraint(webModule,webModuleDescriptor);
        
        configureJspConfig(webModule,webModuleDescriptor);
        
        configureSecurityRoles(webModule, webModuleDescriptor);

    }

    
    /**
     * Configures EJB resource reference for a web application, as
     * represented in a <code>&lt;ejb-ref&gt;</code> and 
     * <code>&lt;ejb-local-ref&gt;</code>element in the
     * deployment descriptor.
     */
    protected static void configureEjbReference(WebModule webModule,
                                         WebBundleDescriptor wmd) {
                                                        
       Set set = wmd.getEjbReferenceDescriptors();
       Iterator iterator =  set.iterator();
       
       EjbReferenceDescriptor ejbDescriptor;
       while( iterator.hasNext() ){
            
           ejbDescriptor = (EjbReferenceDescriptor)iterator.next();
           if ( ejbDescriptor.isLocal() ){
                configureContextLocalEjb(webModule,ejbDescriptor);
           } else {
                configureContextEjb(webModule,ejbDescriptor);               
           }           
       }                                                        
    }
    
     
    /**:q
     *
     * Configures EJB resource reference for a web application, as
     * represented in a <code>&lt;ejb-ref&gt;</code> and 
     * <code>&lt;ejb-local-ref&gt;</code>element in the
     * deployment descriptor.
     */
    protected static void configureEjbReference(WebModule webModule,
                                                WebModule defaultWebModule) { 
         
        ContextLocalEjb[] localEjbs = (ContextLocalEjb[])
                            defaultWebModule.getCachedFindOperation()
                                        [WebModuleContextConfig.LOCAL_EJBS];   
        for (int i=0; i < localEjbs.length; i++){
            webModule.addLocalEjb(localEjbs[i]);
        }
        
        ContextEjb[] ejbs = (ContextEjb[])
                            defaultWebModule.getCachedFindOperation()
                                                [WebModuleContextConfig.EJBS];
        for (int i=0; i <ejbs.length; i++){
            webModule.addEjb(ejbs[i]);
        }
    }   
    
    
    /**
     * Configures EJB resource reference for a web application, as
     * represented in a <code>&lt;ejb-ref&gt;</code> in the
     * deployment descriptor.
     */    
    protected static void configureContextLocalEjb(WebModule webModule,
                                        EjbReferenceDescriptor ejbDescriptor) {
        ContextLocalEjbDecorator decorator = 
                                new ContextLocalEjbDecorator(ejbDescriptor);
        webModule.addLocalEjb(decorator);
    
    }

    
    /**
     * Configures EJB resource reference for a web application, as
     * represented in a <code>&lt;ejb-local-ref&gt;</code>element in the
     * deployment descriptor.
     */    
    protected static void configureContextEjb(WebModule webModule,
                                       EjbReferenceDescriptor ejbDescriptor) {
                                         
        ContextEjbDecorator decorator = new ContextEjbDecorator(ejbDescriptor);
        webModule.addEjb(decorator);
        
    }

    
    /**
     * Configure application environment entry, as represented in
     * an <code>&lt;env-entry&gt;</code> element in the deployment descriptor.
     */
    protected static void configureContextEnvironment(WebModule webModule,
                                                 WebBundleDescriptor wmd) {
                                                        
        Set set = wmd.getContextParametersSet();
        Iterator iterator = set.iterator();
          
        ContextEnvironmentDecorator decorator;
        EnvironmentProperty envRef;
        while (iterator.hasNext()){
            envRef = (EnvironmentProperty)iterator.next();
            decorator = new ContextEnvironmentDecorator(envRef);
            webModule.addEnvironment(decorator);
        }
    }

    
    /**
     * Configure application environment entry, as represented in
     * an <code>&lt;env-entry&gt;</code> element in the deployment descriptor.
     */
    protected static void configureContextEnvironment(
                                    WebModule webModule,
                                    WebModule defaultWebModule) { 
                                                        
       ContextEnvironment[] contextEnvironment = (ContextEnvironment[])
                    defaultWebModule.getCachedFindOperation()
                                        [WebModuleContextConfig.ENVIRONMENTS];
       for(int i=0; i < contextEnvironment.length; i++){
            webModule.addEnvironment(contextEnvironment[i]);
       }
    }
    
    
    /**
     * Configure error page element for a web application,
     * as represented in a <code>&lt;error-page&gt;</code> element in the
     * deployment descriptor.
     */
    protected static void configureErrorPage(WebModule webModule,
                                      WebBundleDescriptor wmd) {
            
       Enumeration enumeration = wmd.getErrorPageDescriptors();
       
       ErrorPageDescriptor errorPageDesc;
       ErrorPageDecorator decorator;
       while (enumeration.hasMoreElements()){
            errorPageDesc = (ErrorPageDescriptor)enumeration.nextElement();
            decorator = new ErrorPageDecorator(errorPageDesc);
            webModule.addErrorPage(decorator);
       }
                                             
    }
    
    
    /**
     * Configure error page element for a web application,
     * as represented in a <code>&lt;error-page&gt;</code> element in the
     * deployment descriptor.
     */
    protected static void configureErrorPage(WebModule webModule,
                                             WebModule defaultWebModule) { 
            
       ErrorPage[] errorPages = (ErrorPage[])
                        defaultWebModule.getCachedFindOperation()
                                    [WebModuleContextConfig.ERROR_PAGES];
                                                 
       for(int i=0; i <  errorPages.length; i++){
            webModule.addErrorPage(errorPages[i]);
       }
                                             
    }
       
    
    /**
     * Configure filter definition for a web application, as represented
     * in a <code>&lt;filter&gt;</code> element in the deployment descriptor.
     */
    protected static void configureFilterDef(WebModule webModule,
                                      WebBundleDescriptor wmd) {
                                                        
       Vector vector = wmd.getServletFilters();
       
       FilterDefDecorator filterDef;
       ServletFilter servletFilter;
       
       for (int i=0; i < vector.size(); i++)  {
           servletFilter = (ServletFilter)vector.get(i);
           filterDef = new FilterDefDecorator(servletFilter);
          
           webModule.addFilterDef(filterDef);          
       }
                                                                     
    }
    
    
    /**
     * Configure filter definition for a web application, as represented
     * in a <code>&lt;filter&gt;</code> element in the deployment descriptor.
     */
    protected static void configureFilterDef(WebModule webModule,
                                             WebModule defaultWebModule) { 
                                                        
       FilterDef[] filterDefs = (FilterDef[])
            defaultWebModule.getCachedFindOperation()
                                        [WebModuleContextConfig.FILTER_DEFS];
       
       for (int i=0; i < filterDefs.length; i++)  {
           webModule.addFilterDef(filterDefs[i]);          
       }
                                                                     
    }
    
    
    /**
     * Configure filter mapping for a web application, as represented
     * in a <code>&lt;filter-mapping&gt;</code> element in the deployment
     * descriptor.  Each filter mapping must contain a filter name plus either
     * a URL pattern or a servlet name.    
     */
    protected static void configureFilterMap(WebModule webModule,
                                             WebBundleDescriptor wmd) {
                                                        
        Vector vector = wmd.getServletFilterMappingDescriptors();
        for (int i=0; i < vector.size(); i++)  {
            webModule.addFilterMap((ServletFilterMapping)vector.get(i));
        }
    }
    
    
    /**
     * Configure filter mapping for a web application, as represented
     * in a <code>&lt;filter-mapping&gt;</code> element in the deployment
     * descriptor.  Each filter mapping must contain a filter name plus either
     * a URL pattern or a servlet name.    
     */
    protected static void configureFilterMap(WebModule webModule,
                                             WebModule defaultWebModule) { 
                                                        
        FilterMap[] filterMaps = (FilterMap[])
                        defaultWebModule.getCachedFindOperation()
                                        [WebModuleContextConfig.FILTER_MAPS];
       
        for (int i=0; i < filterMaps.length; i++)  {
            webModule.addFilterMap(filterMaps[i]);           
        }                                                    
    }
    
    
    /**
     * Configure context initialization parameter that is configured
     * in the server configuration file, rather than the application deployment
     * descriptor.  This is convenient for establishing default values (which
     * may be configured to allow application overrides or not) without having
     * to modify the application deployment descriptor itself.  
     */             
    protected static void configureApplicationListener( WebModule webModule,
                                                        WebModule defaultWebModule) {  
        
       String[] applicationListeners = (String[])
                     defaultWebModule.getCachedFindOperation()
                                [WebModuleContextConfig.APPLICATION_LISTENERS];
       
       for (int i=0; i < applicationListeners.length ; i++){
            webModule.addApplicationListener(applicationListeners[i]);
       }
         
    }
    
    
    /**
     * Configure context initialization parameter that is configured
     * in the server configuration file, rather than the application deployment
     * descriptor.  This is convenient for establishing default values (which
     * may be configured to allow application overrides or not) without having
     * to modify the application deployment descriptor itself.  
     */             
    protected static void configureApplicationListener( WebModule webModule,
                                                    WebBundleDescriptor wmd) {
        
        Vector vector = wmd.getAppListenerDescriptors();
        for (int i=0; i < vector.size() ; i++){
            webModule.addApplicationListener( 
                        ((AppListenerDescriptor)vector.get(i)).getListener() );
        }
         
    }
    
    
    /**
     * Configure <code>jsp-config</code> element contained in the deployment
     * descriptor
     */
    protected static void configureJspConfig(WebModule webModule,
                                             WebBundleDescriptor wmd) {

        Vector jspProperties = new Vector();

        webModule.setAttribute(Globals.JSP_PROPERTY_GROUPS_CONTEXT_ATTRIBUTE,
                               jspProperties);
        webModule.setAttribute(Globals.WEB_XML_VERSION_CONTEXT_ATTRIBUTE,
                               wmd.getSpecVersion());
                                      
        JspConfigDescriptor jspConfig = wmd.getJspConfigDescriptor();
        if (jspConfig == null) {
            return;
        }

        Enumeration<TagLibConfigurationDescriptor> taglibs
            = jspConfig.getTagLibs();
        if (taglibs != null) {
            while (taglibs.hasMoreElements()) {
                TagLibConfigurationDescriptor taglib = taglibs.nextElement();
                webModule.addTaglib(taglib.getTagLibURI(),
                                    taglib.getTagLibLocation()); 
            }
        }
       
        Collection set = jspConfig.getJspGroupSet();
        if (set.isEmpty()) {
            return;
        }

        Iterator<JspGroupDescriptor> jspPropertyGroups = set.iterator();
        while (jspPropertyGroups.hasNext()){

            JspGroupDescriptor jspGroup = jspPropertyGroups.next();

            Vector urlPatterns = null;
            Vector includePreludes = null;
            Vector includeCodas = null;

            String pageEncoding = jspGroup.getPageEncoding();
            String scriptingInvalid = jspGroup.isScriptingInvalid()?
                                          "true": "false";
            String elIgnored = jspGroup.isElIgnored()? "true": "false";
            String isXml = (jspGroup.getIsXml()==null)? null:
                               jspGroup.getIsXml().toString();
            String trimSpaces = jspGroup.isTrimDirectiveWhitespaces()?
                                    "true": "false";
            String poundAllowed = jspGroup.isDeferredSyntaxAllowedAsLiteral()?
                                    "true": "false";
            String defaultContentType = jspGroup.getDefaultContentType();
            String buffer = jspGroup.getBuffer();
            String errorOnUndeclaredNamespace =
                       jspGroup.isErrorOnUndeclaredNamespace()? "true": "false";
           
            // url-pattern
            Enumeration<String> e = jspGroup.getUrlPatterns();
            if (e != null) {
                while (e.hasMoreElements()) {
                    if (urlPatterns == null) {
                        urlPatterns = new Vector();
                    }
                    String urlPattern = e.nextElement();
                    urlPatterns.addElement(urlPattern);
                    webModule.addJspMapping(urlPattern);
                }
            }
            if (urlPatterns == null || urlPatterns.size() == 0) {
                continue;
            }

            // include-prelude
            e = jspGroup.getIncludePreludes();
            if (e != null) {
                while (e.hasMoreElements()) {
                    if (includePreludes == null) {
                        includePreludes = new Vector();
                    }
                    includePreludes.addElement(e.nextElement());
                }
            }

            // include-coda
            e = jspGroup.getIncludeCodas();
            if (e != null) {
                while (e.hasMoreElements()) {
                    if (includeCodas == null) {
                        includeCodas = new Vector();
                    }
                    includeCodas.addElement(e.nextElement());
                }
            }

            JspConfig.makeJspPropertyGroups(jspProperties,
                                            urlPatterns, 
                                            isXml,
                                            elIgnored,
                                            scriptingInvalid,
                                            trimSpaces,
                                            poundAllowed,
                                            pageEncoding,
                                            includePreludes,
                                            includeCodas,
                                            defaultContentType,
                                            buffer,
                                            errorOnUndeclaredNamespace);
        }
    }

        
    /**
     * Configure a login configuration element for a web application,
     * as represented in a <code>&lt;login-config&gt;</code> element in the
     * deployment descriptor.
     */ 
    protected static void configureLoginConfig(WebModule webModule,
                                               WebModule defaultWebModule) {
        LoginConfig loginConf = defaultWebModule.getLoginConfig();
        if ( loginConf == null ){
            return;
        }                                          
        webModule.setLoginConfig(loginConf);       
        
    }
    
    
    /**
     * Configure a login configuration element for a web application,
     * as represented in a <code>&lt;login-config&gt;</code> element in the
     * deployment descriptor.
     */ 
    protected static void configureLoginConfig(WebModule webModule,
                                        WebBundleDescriptor wmd) {
                                            
        LoginConfiguration loginConf = wmd.getLoginConfiguration();
        if ( loginConf == null ){
            return;
        }

        LoginConfigDecorator decorator = new LoginConfigDecorator(loginConf);
        webModule.setLoginConfig(decorator);         
    }
    
    
    /**
     * Configure mime-mapping defined in the deployment descriptor.
     */
    protected static void configureMimeMapping(WebModule webModule,
                                               WebBundleDescriptor wmd) {
                                                        
        Enumeration enumeration = wmd.getMimeMappings();
        MimeMapping mimeMapping;
        while (enumeration.hasMoreElements()){
            mimeMapping = (MimeMapping)enumeration.nextElement();
            webModule.addMimeMapping(mimeMapping.getExtension(),
                                     mimeMapping.getMimeType());            
        }
                                                        
    }
    
    
    /**
     * Adds the MIME mappings defined in default-web.xml to the given
     * web module.
     *
     * @param webModule Web module to which the MIME mappings specified in
     * default-web.xml are to be added
     * @param defaultWebModule Web Module representing default-web.xml
     */
    protected static void configureMimeMapping(WebModule webModule,
                                               WebModule defaultWebModule) {
        String[] mimeMappings = (String[])
            defaultWebModule.getCachedFindOperation()
                                    [WebModuleContextConfig.MIME_MAPPINGS];
       
        for (int i=0; mimeMappings!=null && i<mimeMappings.length; i++) {
            webModule.addMimeMapping(
                            mimeMappings[i],
                            defaultWebModule.findMimeMapping(mimeMappings[i]));
        }                                                
    }
    
    
    /**
     * Configure resource-reference defined in the deployment descriptor.
     */
    protected static void configureResourceRef(WebModule webModule,
                                               WebBundleDescriptor wmd) {
        Set set = wmd.getEnvironmentProperties();
        if ( set.isEmpty() ){
           return;
        }
        Iterator iterator = set.iterator();
        
        EnvironmentEntry envEntry;
        while(iterator.hasNext()){
            envEntry = (EnvironmentEntry)iterator.next();
            
            webModule.addResourceEnvRef(envEntry.getName(), 
                                        envEntry.getType());
                       
        }                                                                     
    }
    
    /**
     * Configure resource-reference defined in the deployment descriptor.
     */
    protected static void configureResourceRef(WebModule webModule,
                                               WebModule defaultWebModule) { 
        
        ContextResource[] contextResources = (ContextResource[])
                                defaultWebModule.getCachedFindOperation()
                                        [WebModuleContextConfig.RESOURCES];

        for (int i=0; i < contextResources.length; i++){
            webModule.addResource(contextResources[i]);
        }
    }
 
    
    /**
     * Configure context parameter defined in the deployment descriptor.
     */
    protected static void configureContextParam(WebModule webModule,
                                                WebBundleDescriptor wmd) {
        
       Set set = wmd.getContextParametersSet();
       if ( set.isEmpty() ){
           return;
       }
       Iterator iterator = set.iterator();
       
       ContextParameter ctxParam;
       while(iterator.hasNext()){
           ctxParam = (ContextParameter)iterator.next();
           
           webModule.addParameter(ctxParam.getName(), ctxParam.getValue());
       }
    }
    
    
    /**
     * Configure context parameter defined in the deployment descriptor.
     */
    protected static void configureContextParam(WebModule webModule,
                                                WebModule defaultWebModule) { 
        
        ApplicationParameter[] params = (ApplicationParameter[])
                    defaultWebModule.getCachedFindOperation()
                            [WebModuleContextConfig.APPLICATION_PARAMETERS];
       
        for (int i=0 ; i < params.length; i++){
            webModule.addParameter(params[i].getName(), params[i].getValue());
        }
    }
    
    
    /**
     * Configure of a message destination for a web application, as
     * represented in a <code>&lt;message-destination&gt;</code> element
     * in the deployment descriptor.
     */    
    protected static void configureMessageDestination(WebModule webModule,
                                                      WebBundleDescriptor wmd) {
                                                        
        Set set = wmd.getMessageDestinations();
        if ( set.isEmpty() ){
            return;
        }
        Iterator iterator = set.iterator();
        
        MessageDestinationDescriptor msgDrd = null;
        MessageDestinationDecorator decorator;
        while(iterator.hasNext()){
            msgDrd = (MessageDestinationDescriptor)iterator.next();
            decorator = new MessageDestinationDecorator(msgDrd);
            
            webModule.addMessageDestination(decorator);
        }       
                                                       
    }

    
    /**
     * Configure of a message destination for a web application, as
     * represented in a <code>&lt;message-destination&gt;</code> element
     * in the deployment descriptor.
     */    
    protected static void configureMessageDestination(WebModule webModule,
                                                      WebModule defaultWebModule) { 
                                                        
        MessageDestination[] messageDestinations = (MessageDestination[])
                    defaultWebModule.getCachedFindOperation()
                                [WebModuleContextConfig.MESSAGE_DESTINATIONS];
        
        for(int i=0; i < messageDestinations.length; i++){
            webModule.addMessageDestination(messageDestinations[i]);
        }                                                           
    }

    
    
    /**
     * Representation of a message destination reference for a web application,
     * as represented in a <code>&lt;message-destination-ref&gt;</code> element
     * in the deployment descriptor.
     */
    protected static void configureMessageDestinationRef(WebModule webModule,
                                                  WebBundleDescriptor wmd) {
                                                        
        Set set = wmd.getMessageDestinationReferenceDescriptors();
        if ( set.isEmpty() ){
            return;
        }
        Iterator iterator = set.iterator();
        
        MessageDestinationReferenceDescriptor msgDrd = null;
        MessageDestinationRefDecorator decorator;
        while(iterator.hasNext()){
            msgDrd = (MessageDestinationReferenceDescriptor)iterator.next();
            decorator = new MessageDestinationRefDecorator(msgDrd);
            
            webModule.addMessageDestinationRef(decorator);
        }                                                             
    }
    
        
    /**
     * Configure of a message destination for a web application, as
     * represented in a <code>&lt;message-destination&gt;</code> element
     * in the deployment descriptor.
     */    
    protected static void configureMessageRef(WebModule webModule,
                                              WebModule defaultWebModule) { 
                                                        
        MessageDestinationRef[] messageDestinationRefs = (MessageDestinationRef[])
                defaultWebModule.getCachedFindOperation()
                            [WebModuleContextConfig.MESSAGE_DESTINATION_REFS];
        
        for(int i=0; i < messageDestinationRefs.length; i++){
            webModule.addMessageDestinationRef(messageDestinationRefs[i]);
        }                                                           
    }
      
    
    /**
     * Configure a resource reference for a web application, as
     * represented in a <code>&lt;resource-ref&gt;</code> element in the
     * deployment descriptor.
     */    
    protected static void configureContextResource(WebModule webModule,
                                                   WebBundleDescriptor wmd) {
        Set set = wmd.getResourceReferenceDescriptors();
        if ( set.isEmpty() ){
            return;
        }
        Iterator iterator = set.iterator();
        
        ResourceReferenceDescriptor resRefDesc;
        ContextResourceDecorator decorator;
        while(iterator.hasNext()){
            resRefDesc = (ResourceReferenceDescriptor)iterator.next();
            decorator = new ContextResourceDecorator(resRefDesc);
                        
            webModule.addResource(decorator);                      
        }

    }
   
    
    /**
     * Configure the <code>WebModule</code> instance by creating 
     * <code>StandardWrapper</code> using the information contained
     * in the deployment descriptor (Welcome Files, JSP, Servlets etc.)
     */
    protected static void configureStandardContext(WebModule webModule,
                                                   WebBundleDescriptor wmd) {
    
       Set set = wmd.getWebComponentDescriptors();
       StandardWrapper wrapper;    
       WebComponentDescriptor webComponentDesc;
       Enumeration enumeration;
       SecurityRoleReference securityRoleReference;
       
       Set set2;
       Iterator iterator2;
       
       if ( !set.isEmpty() ){
           Iterator iterator = set.iterator();

           while (iterator.hasNext()) {

                webComponentDesc = (WebComponentDescriptor)iterator.next();
                if (!webComponentDesc.isEnabled()) {
                    continue;
                }

                wrapper = (StandardWrapper)webModule.createWrapper();
                wrapper.setName(webComponentDesc.getCanonicalName());
                webModule.addChild(wrapper);

                enumeration = webComponentDesc.getInitializationParameters();
                InitializationParameter initP = null;
                while (enumeration.hasMoreElements()){
                    initP = (InitializationParameter)enumeration.nextElement();
                    wrapper.addInitParameter(initP.getName(), initP.getValue());
                }

                if (webComponentDesc.isServlet()){
                    wrapper.setServletClassName(
                        webComponentDesc.getWebComponentImplementation());
                } else {
                    wrapper.setJspFile(
                        webComponentDesc.getWebComponentImplementation());
                }

                wrapper.setLoadOnStartup(webComponentDesc.getLoadOnStartUp());
                wrapper.setIsAsyncSupported(webComponentDesc.isAsyncSupported());
                wrapper.setAsyncTimeout(webComponentDesc.getAsyncTimeout());

                if (webComponentDesc.getRunAsIdentity() != null)
                    wrapper.setRunAs(webComponentDesc.getRunAsIdentity().getRoleName());


                set2 = webComponentDesc.getUrlPatternsSet();
                iterator2 = set2.iterator();
                while (iterator2.hasNext()){
                    webModule.addServletMapping((String)iterator2.next(),
                                                webComponentDesc.getCanonicalName());
                }

                enumeration = webComponentDesc.getSecurityRoleReferences();
                while (enumeration.hasMoreElements()){
                    securityRoleReference = 
                                (SecurityRoleReference)enumeration.nextElement();
                    wrapper.
                        addSecurityReference(securityRoleReference.getRolename(),
                             securityRoleReference.getSecurityRoleLink().getName());
                }
            }
        }
       
        webModule.setSessionTimeout(wmd.getSessionConfigDescriptor().getSessionTimeout());
           
        enumeration = wmd.getWelcomeFiles();
        while (enumeration.hasMoreElements()){
            webModule.addWelcomeFile((String)enumeration.nextElement());
        }
        
        LocaleEncodingMappingListDescriptor lemds = 
                            wmd.getLocaleEncodingMappingListDescriptor();
        if (lemds != null) {
            set2 = lemds.getLocaleEncodingMappingSet();
            iterator2 = set2.iterator();
            LocaleEncodingMappingDescriptor lemd;
            while (iterator2.hasNext()){
                lemd = (LocaleEncodingMappingDescriptor) iterator2.next();
                webModule.
                    addLocaleEncodingMappingParameter(lemd.getLocale(),
                                                      lemd.getEncoding());
            }
        }
    }

    
    /**
     * Configure security constraint element for a web application,
     * as represented in a <code>&lt;security-constraint&gt;</code> element in 
     * the deployment descriptor.    
     *
     * Configure a web resource collection for a web application's security
     * constraint, as represented in a <code>&lt;web-resource-collection&gt;</code>
     * element in the deployment descriptor.
     *
     */
    protected static void configureSecurityConstraint(WebModule webModule,
                                                      WebBundleDescriptor wmd) {
                                                   
        Enumeration enumeration = wmd.getSecurityConstraints(); 
        SecurityConstraint securityConstraint;
        SecurityConstraintDecorator decorator;
        Enumeration enumeration2;
        SecurityCollectionDecorator secCollDecorator;
        while (enumeration.hasMoreElements()){
            securityConstraint =(SecurityConstraint)enumeration.nextElement();
             
            decorator = new SecurityConstraintDecorator(securityConstraint,
                                                        webModule);
            
            enumeration2 = securityConstraint.getWebResourceCollections();
            while (enumeration2.hasMoreElements()){
                secCollDecorator = new SecurityCollectionDecorator
                            ((WebResourceCollection) enumeration2.nextElement());
                   
                decorator.addCollection(secCollDecorator);           
            }
            webModule.addConstraint(decorator);
        }
                                                
    }
    
    
    /**
     * Validate the usage of security role names in the web application
     * deployment descriptor.  If any problems are found, issue warning
     * messages (for backwards compatibility) and add the missing roles.
     * (To make these problems fatal instead, simply set the <code>ok</code>
     * instance variable to <code>false</code> as well).
     */
    protected static void configureSecurityRoles(WebModule webModule,
                                                 WebBundleDescriptor wmd) {

        Enumeration<SecurityRoleDescriptor> e = wmd.getSecurityRoles();
        if (e != null) {
            while (e.hasMoreElements()){
                webModule.addSecurityRole(e.nextElement().getName());
            }
        }

        // Check role names used in <security-constraint> elements
        org.apache.catalina.deploy.SecurityConstraint 
                            constraints[] = webModule.findConstraints();
        for (int i = 0; i < constraints.length; i++) {
            String roles[] = constraints[i].findAuthRoles();
            for (int j = 0; j < roles.length; j++) {
                if (!"*".equals(roles[j]) &&
                    !webModule.findSecurityRole(roles[j])) {
                    logger.log(
                        Level.WARNING,"tomcatDeploymentConfig.role.auth", roles[j]);
                    webModule.addSecurityRole(roles[j]);
                }
            }
        }

        // Check role names used in <servlet> elements
        Container wrappers[] = webModule.findChildren();
        for (int i = 0; i < wrappers.length; i++) {
            Wrapper wrapper = (Wrapper) wrappers[i];
            String runAs = wrapper.getRunAs();
            if ((runAs != null) && !webModule.findSecurityRole(runAs)) {
                logger.log(
                    Level.WARNING,"tomcatDeploymentConfig.role.runas", runAs);
                webModule.addSecurityRole(runAs);
            }
            String names[] = wrapper.findSecurityReferences();
            for (int j = 0; j < names.length; j++) {
                String link = wrapper.findSecurityReference(names[j]);
                if ((link != null) && !webModule.findSecurityRole(link)) {
                    logger.log(
                        Level.WARNING,"tomcatDeploymentConfig.role.link", link);
                    webModule.addSecurityRole(link);
                }
            }
        }

    }
    
    protected static void configureStandardContext(WebModule webModule,
                                                   WebModule defaultWebModule) { 
                                                      
        // 1. Add the default Wrapper
        Container wrappers[] = (Container[])
                            defaultWebModule.getCachedFindOperation()
                                            [WebModuleContextConfig.CHILDREN];
        
        StandardWrapper wrapper, defaultWrapper;
        for(int i=0; i < wrappers.length; i++){    
            defaultWrapper = (StandardWrapper)wrappers[i];
            wrapper = (StandardWrapper)webModule.createWrapper();
            wrapper.setName(defaultWrapper.getName());
            webModule.addChild(wrapper);
            
            String[] initParams = defaultWrapper.findInitParameters();
            for (int j=0; j < initParams.length; j++){
                wrapper.addInitParameter(
                    initParams[j], defaultWrapper.findInitParameter(initParams[j]));
            }
       
            if (defaultWrapper.getJspFile() == null){
                wrapper.setServletClassName(
                    defaultWrapper.getServletClassName());
            } else {
                wrapper.setJspFile(defaultWrapper.getJspFile());
            }
    
            wrapper.setLoadOnStartup(defaultWrapper.getLoadOnStartup());
            if (defaultWrapper.getRunAs() != null)
                wrapper.setRunAs(defaultWrapper.getRunAs());
        }
                           
        String[] servletMappings = (String[])
                    defaultWebModule.getCachedFindOperation()
                                    [WebModuleContextConfig.SERVLET_MAPPINGS];
        
        String servletName;
        for (int j=0; j < servletMappings.length; j++){
            servletName = 
                defaultWebModule.findServletMapping(servletMappings[j]);
            if (servletName.equals("jsp")){
                webModule.addServletMapping(
                    servletMappings[j], servletName, true);
            } else {
                webModule.addServletMapping(servletMappings[j],servletName);
            }
        }
        
        webModule.setSessionTimeout(defaultWebModule.getSessionTimeout());
           
        String[] welcomeFiles = defaultWebModule.getWelcomeFiles();
        for(int i=0; i < welcomeFiles.length; i++){
            webModule.addWelcomeFile(welcomeFiles[i]);
        }
        
        webModule.setCharsetMapper((CharsetMapper)
            defaultWebModule.getCharsetMapper().clone()); 
    }       
}
