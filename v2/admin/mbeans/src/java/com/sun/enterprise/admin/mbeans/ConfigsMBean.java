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

/*
 * $Id: ConfigsMBean.java,v 1.9 2005/12/25 03:42:14 tcfujii Exp $
 */

package com.sun.enterprise.admin.mbeans;

//jdk imports
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collection;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;


//JMX imports
import javax.management.AttributeList;
import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;

//config imports
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.SystemPropertyConstants;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.config.serverbeans.MessageSecurityConfig;
import com.sun.enterprise.config.serverbeans.ProviderConfig;
import com.sun.enterprise.config.serverbeans.RequestPolicy;
import com.sun.enterprise.config.serverbeans.ResponsePolicy;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.IiopService;
import com.sun.enterprise.config.serverbeans.Ssl;
import com.sun.enterprise.config.serverbeans.SslClientConfig;
import com.sun.enterprise.config.serverbeans.AuthRealm;



import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.admin.config.ManagedConfigBean;
import com.sun.enterprise.admin.config.MBeanConfigException;
import com.sun.enterprise.admin.config.ConfigMBeanHelper;
import com.sun.enterprise.admin.meta.MBeanRegistry;
import com.sun.enterprise.admin.meta.MBeanRegistryEntry;
import com.sun.enterprise.admin.meta.naming.MBeanNamingInfo;
import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
import com.sun.enterprise.admin.util.jmx.AttributeListUtils;

import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.ConfigAPIHelper;
import com.sun.enterprise.config.serverbeans.PropertyResolver;

import com.sun.enterprise.admin.mbeanapi.IConfigsMBean;

import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.ConfigTarget;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.admin.target.TargetBuilder;

import com.sun.enterprise.admin.meta.MBeanRegistry;

import com.sun.enterprise.admin.util.JvmOptionsHelper;
import com.sun.enterprise.admin.util.InvalidJvmOptionException;

public class ConfigsMBean extends BaseConfigMBean
    implements IConfigsMBean
{

     /**
     * Default http-acceptor-threads for PE.
     *
     */
    public static final String DEFAULT_HTTP_LISTENER_ACCEPTOR_THREADS = "1";
    

    /**
     * Valid targets for the configuration mbeans are 1) a named configuration,
     * 2) a named server instance 3) a named cluster
     *
     * FIXTHIS: This needs to be made pluggable.
     */
    private static final TargetType[] VALID_CREATE_DELETE_TYPES = new TargetType[] {
        TargetType.CLUSTER, TargetType.CONFIG, TargetType.UNCLUSTERED_SERVER, TargetType.DAS};
        
    private static final TargetType[] VALID_LIST_TYPES = new TargetType[] {
        TargetType.CLUSTER, TargetType.CONFIG, TargetType.SERVER, TargetType.DAS};
        
    /**
     * i18n strings manager object
     */
    private static final StringManager localStrings = 
        StringManager.getManager(ConfigsMBean.class);

    /**
     */
    public ConfigsMBean()
    {
        super();
    }  
    
    /**
     * Resolve target name to associated config name.
     *
     * @param targetName  target name to resolve
     *
     * @returns config name associated with given target
     */
    public String getConfigNameForTarget(String targetName) 
         throws Exception
    {
        final Target        target = getTarget(targetName);
        final ConfigTarget  configTarget = target.getConfigTarget();
        return configTarget.getName();
     }
//HTTP Listener

    public ObjectName createHttpListener(   AttributeList   attrList, 
                                            Properties      props,
                                            String          targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);     
        check1ToN(target);

        //set the default acceptor threads if not specified in attrList
        if(!ConfigMBeanUtil.attributeDefinedInList(attrList,
                ServerTags.ACCEPTOR_THREADS)) {
           attrList.add(new Attribute(ServerTags.ACCEPTOR_THREADS,
                getDefaultHTTPListenerAcceptorThreads()));
        }


        ObjectName httpService = getHttpServiceMBean(target);
        ObjectName mbean = (ObjectName)invoke1(httpService, 
            "createHttpListener", attrList, AttributeList.class.getName());
        setProperties(mbean, props);
        //4954870
        try
        {
            HttpListenerVirtualServerAssociationMgr mgr = 
                new HttpListenerVirtualServerAssociationMgr(getConfigContext(), 
                    target.getConfigRef());
            mgr.addHttpListenerRef(mbean.getKeyProperty("id"));
        }
        catch (ConfigException ce)
        {
            throw MBeanExceptionFormatter.toMBeanException(ce, null);
        }
        //4954870
        return mbean;
    }

    public boolean deleteHttpListener(String listenerId, String targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);
        //4954870
        try
        {
            HttpListenerVirtualServerAssociationMgr mgr = 
                new HttpListenerVirtualServerAssociationMgr(getConfigContext(), 
                    target.getConfigRef());
            mgr.deleteHttpListenerRef(listenerId);
        }
        catch (ConfigException ce)
        {
            throw MBeanExceptionFormatter.toMBeanException(ce, null);
        }
        //4954870
        check1ToN(target);        
        final ObjectName httpService = getHttpServiceMBean(target);
        invoke1(httpService, "removeHttpListenerById", listenerId, 
                String.class.getName());
        return true;
    }

    public ObjectName[] listHttpListeners(String targetName)
        throws MBeanException
    {
        final Target target = getListTarget(targetName);
        ObjectName httpService = getHttpServiceMBean(target);
        ObjectName[] ret = (ObjectName[])invoke0(httpService, 
                                                 "getHttpListener");
        return ret;
    }

//IIOP Listener
    public ObjectName createIiopListener(   AttributeList   attrList, 
                                            Properties      props,
                                            String          targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);     
        check1ToN(target);
        ObjectName iiopService = getIiopServiceMBean(target);
        ObjectName mbean = (ObjectName)invoke1(iiopService, 
            "createIiopListener", attrList, AttributeList.class.getName());
        setProperties(mbean, props);
        return mbean;
    }

    public boolean deleteIiopListener(String listenerId, String targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName); 
        check1ToN(target);
        final ObjectName iiopService = getIiopServiceMBean(target);
        invoke1(iiopService, "removeIiopListenerById", listenerId, 
                String.class.getName());
        return true;
    }

    public ObjectName[] listIiopListeners(String targetName)
        throws MBeanException
    {
        final Target target = getListTarget(targetName);
        ObjectName iiopService = getIiopServiceMBean(target);
        ObjectName[] ret = (ObjectName[])invoke0(iiopService, 
                                                 "getIiopListener");
        return ret;
    }

//SSL
    public ObjectName createSsl(AttributeList   al, 
                                String          id, 
                                String          type, 
                                String          targetName)
        throws Exception
    {
        final Target target = getTarget(targetName);        
        check1ToN(target);
        ObjectName targetON = getSslTargetObjectName(target, id, type);
        if ("iiop-service".equals(type))
        {
            Config config = getConfigBeanForTarget(target);
            IiopService iiopService = config.getIiopService();
            SslClientConfig sslCfg = new SslClientConfig();
            sslCfg.setXPath(sslCfg.getAbsoluteXPath(iiopService.getXPath()));
            ManagedConfigBean mb = new ManagedConfigBean(
                    (ConfigBean)sslCfg, m_registry, getDomainName());
            mb.createChildByType("ssl", al, null, true);
            iiopService.setSslClientConfig(sslCfg);
            return (ObjectName)ConfigMBeanHelper.getConfigBeanObjectName(
                    m_registry, 
                    getDomainName(), sslCfg.getSsl());
        }
        checkElementDoesnotExist(targetON, "getSsl", 
            localStrings.getString("configsMBean.ssl_exists"));
        ObjectName ret = (ObjectName)invoke1(targetON, "createSsl", al, 
                                    AttributeList.class.getName());
        return ret;
    }

    public boolean deleteSsl(String id, String type, String targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);   
        check1ToN(target);
        final ObjectName targetON = getSslTargetObjectName(target, id, type);
        final String opName = "iiop-service".equals(type) ?
            "removeSslClientConfig" : "removeSsl";
        invoke0(targetON, opName);
        return true;
    }

//Virtual servers
    public ObjectName createVirtualServer(  AttributeList al, 
                                            Properties props, 
                                            String targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);       
        check1ToN(target);
        ObjectName httpService = getHttpServiceMBean(target);
        ObjectName mbean = (ObjectName)invoke1(httpService, 
            "createVirtualServer", al, AttributeList.class.getName());
        final Properties nProps = copyProperties(props);
        addMandatoryVirtualServerProperties(al, nProps); //"optional properties"
        setProperties(mbean, nProps);
        return mbean;
    }

    public boolean deleteVirtualServer(String id, String targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);       
        check1ToN(target);
        final ObjectName httpService = (ObjectName)getHttpServiceMBean(target);
        invoke1(httpService, "removeVirtualServerById", id, 
                String.class.getName());
        return true;
    }

    public ObjectName[] listVirtualServers(String targetName)
        throws MBeanException
    {
        final Target target = getListTarget(targetName);
        ObjectName httpService = getHttpServiceMBean(target);
        ObjectName[] ret = (ObjectName[])invoke0(httpService, 
                                                 "getVirtualServer");
        return ret;
    }

//Auth realms 
    public ObjectName createAuthRealm(AttributeList attrs, 
                                      Properties    props, 
                                      String        targetName)
        throws Exception
    {
        final Target target = getTarget(targetName);
        check1ToN(target);
        
        //first, create standalone auth-realm
        Config config = getConfigBeanForTarget(target);
        SecurityService secService = config.getSecurityService();
        ManagedConfigBean mcbSecService = getManagedConfigBean(secService);
        AuthRealm authRealm = (AuthRealm)mcbSecService.createChildByType(
                ServerTags.AUTH_REALM, attrs, props);
        
        // Second, check keyfile
        // we need to create empty keyfile if it's not exist
        //   file should exist already - synchronization problems???
        checkAndCreateAuthRealmKeyFile(authRealm); 
        
        
        // Finally, add auth-realm o config context
        mcbSecService.addChildBean(ServerTags.AUTH_REALM, authRealm, false);
        return getConfigBeanObjectName(authRealm);
    }
    
    
    static final String FILE_REALM_CLASSNAME = "com.sun.enterprise.security.auth.realm.file.FileRealm";
    static final String KEYFILE_PATH_PROPERTY_NAME = "file";
    
    //creates empty keyfile for file-realms if it's not exist
    private void checkAndCreateAuthRealmKeyFile(AuthRealm authRealm) 
        throws MBeanConfigException
    {
        if(!FILE_REALM_CLASSNAME.equals(authRealm.getClassname()))
            return; //only file realms have keyfile reference
        ElementProperty prop = authRealm.getElementPropertyByName(KEYFILE_PATH_PROPERTY_NAME);
        if(prop==null)
        {
//            String msg = localStrings.getString("configsMBean.no_keyfile_name_property");
//            _sLogger.log(Level.WARNING, msg);
//            throw new MBeanConfigException(msg);
            return; //leave validation exception to config validator 
        }
        String path=null;
        try 
        {
                path = resolveTokensForDas(prop.getValue());
                // this is patch for file-realm only
                // we need to create empty keyfile if it's not exist
                File file;
                if((file = new File(path))!=null && !file.exists())
                        file.createNewFile();
        }
        catch (Exception e)
        {
            String msg = localStrings.getString("configsMBean.can_not_create_keyfile", new Object[]{path});
            _sLogger.log(Level.WARNING, msg);
            throw new MBeanConfigException(msg);
        }
    }
    private String resolveTokensForDas(String value) throws ConfigException
    {
        String instanceName=MBeanRegistryFactory.getAdminContext().getServerName();
        PropertyResolver resolver = new PropertyResolver(getConfigContext(), instanceName);
        return resolver.resolve(value, true);
    }
    
    
    public boolean deleteAuthRealm(String name, String targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);       
        check1ToN(target);
        final ObjectName securityService = getSecurityServiceMBean(target);
        invoke1(securityService, "removeAuthRealmByName", name, 
                String.class.getName());
        return true;
    }

    public ObjectName[] listAuthRealms(String targetName)
        throws MBeanException
    {
        final Target target = getListTarget(targetName);
        ObjectName securityService = getSecurityServiceMBean(target);
        ObjectName[] ret = (ObjectName[])invoke0(securityService, 
                                                 "getAuthRealm");
        return ret;
    }

    public void addUser(String      user, 
                        String      password, 
                        String[]    grps, 
                        String      realmName, 
                        String      targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);  
        check1ToN(target);
        realmName = (realmName != null) ? 
                    realmName : getDefaultAuthRealm(target);
        final ObjectName authRealm = getAuthRealmMBean(target, realmName);
        invokeN(authRealm, "addUser", new Object[] {user, password, grps}, 
                new String[] {String.class.getName(), String.class.getName(), 
                              new String[0].getClass().getName()});
    }

    public void updateUser( String      user, 
                            String      password, 
                            String[]    grps, 
                            String      realmName, 
                            String      targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);  
        check1ToN(target);
        realmName = (realmName != null) ? 
                    realmName : getDefaultAuthRealm(target);
        final ObjectName authRealm = getAuthRealmMBean(target, realmName);
        invokeN(authRealm, "updateUser", new Object[] {user, password, grps}, 
                new String[] {String.class.getName(), String.class.getName(), 
                              new String[0].getClass().getName()});
    }

    public void removeUser( String      user, 
                            String      realmName, 
                            String      targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);  
        check1ToN(target);
        realmName = (realmName != null) ? 
                    realmName : getDefaultAuthRealm(target);
        final ObjectName authRealm = getAuthRealmMBean(target, realmName);
        invoke1(authRealm, "removeUser", user, String.class.getName());
    }

    public String[] getUserNames(String realmName, String targetName)
        throws MBeanException
    {
        final Target target = getListTarget(targetName);
        realmName = (realmName != null) ? 
                    realmName : getDefaultAuthRealm(target);
        final ObjectName authRealm = getAuthRealmMBean(target, realmName);
        return (String[])invoke0(authRealm, "getUserNames");
    }

    public String[] getGroupNames(  String user, 
                                    String realmName, 
                                    String targetName)
        throws MBeanException
    {
        final Target target = getListTarget(targetName);
        realmName = (realmName != null) ? 
                    realmName : getDefaultAuthRealm(target);
        final ObjectName authRealm = getAuthRealmMBean(target, realmName);
        return (String[])invoke1(authRealm, "getUserGroupNames", 
                                user, String.class.getName());
    }

//Profiler
    public ObjectName createProfiler(AttributeList  al, 
                                     Properties     props, 
                                     String         targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName); 
        check1ToN(target);
        final ObjectName javaConfig = getJavaConfigMBean(target);
        checkElementDoesnotExist(javaConfig, "getProfiler", 
            localStrings.getString("configsMBean.profiler_exists"));
        final ObjectName on = (ObjectName)invoke1(javaConfig, 
            "createProfiler", al, AttributeList.class.getName());
        setProperties(on, props);
        return on;
    }

    public boolean deleteProfiler(String targetName) throws MBeanException
    {
        final Target target = getTarget(targetName);
        check1ToN(target);
        final ObjectName javaConfig = getJavaConfigMBean(target);
        invoke0(javaConfig, "removeProfiler");
        return true;
    }

//JVM options
    public String[] getJvmOptions(boolean isProfiler, String targetName) 
        throws MBeanException
    {
        return getJvmOptions(isProfiler, getListTarget(targetName));
    }

    /**
         Adds the given jvm options to the existing set of jvm options.
         Excludes the jvm options that already exist.
         @param options jvm options to be added.
         @param isProfiler jvm-options can be added either to the java
         config element or to the profiler element.
         @param targetName
         @return Returns an array of jvm options that could not be added.
         This array will be atleast of 0 length so that callers donot have
         to check for null. A non zero length indicates that some of the
         specified options could not be added successfully.
         @throws MBeanException
         @throws IllegalArgumentException if the options argument is null.
         @throws InvalidJvmOptionException
     */
    public String[] createJvmOptions(String[]   options, 
                                     boolean    isProfiler, 
                                     String     targetName)
        throws MBeanException, InvalidJvmOptionException
    { 
        if (null == options)
        {
            throw new IllegalArgumentException(
                localStrings.getString("configsMBean.null_jvm_options"));
        }
        final Target target = getTarget(targetName);
        quoteOptionsWithSpaces(options);
        check1ToN(target);
        final String[] oldOptions = getJvmOptions(isProfiler, target);
        final JvmOptionsHelper jvmOptions = new JvmOptionsHelper(oldOptions);
        final String[] invalid = jvmOptions.addJvmOptions(options);
        final ObjectName targetON = getJvmOptionsTargetObjectName(
                                        target, isProfiler);
        setAttribute0(targetON, "jvm_options", jvmOptions.getJvmOptionsAsStoredInXml());
        postInvoke("createJvmOptions", null);

        return invalid;
    }
    
    private void quoteOptionsWithSpaces(String[] options)
    {
       for(int i=0; i<options.length; i++)
       {
           if(options[i].indexOf(' ')>=0 &&
              (!options[i].startsWith("\"") || !options[i].endsWith("\"")))
           {
               options[i] = "\"" + options[i] + "\"";
           }
       }
    }

    /**
         Deletes the given jvm options from the existing set of jvm options.
         @param options jvm options to be deleted.
         @param isProfiler jvm-options can exist in either the java config 
         element or the profiler element.
         @param targetName
         @return Returns an array of jvm options that could not be deleted.
         This array will be atleast of 0 length so that callers donot have
         to check for null. A non zero length indicates that some of the
         specified options could not be deleted successfully.
         @throws MBeanException
         @throws IllegalArgumentException if the options argument is null.
     */
    public String[] deleteJvmOptions(String[]   options, 
                                     boolean    isProfiler, 
                                     String     targetName)
        throws MBeanException, InvalidJvmOptionException
    {
        if (null == options)
        {
            throw new IllegalArgumentException(
                localStrings.getString("configsMBean.null_jvm_options"));
        }
        quoteOptionsWithSpaces(options);
        final Target target = getTarget(targetName);
        check1ToN(target);
        final String[] oldOptions = getJvmOptions(isProfiler, target);
        final JvmOptionsHelper jvmOptions = new JvmOptionsHelper(oldOptions);
        final String[] invalid = jvmOptions.deleteJvmOptions(options);
        final ObjectName targetON = getJvmOptionsTargetObjectName(
                                        target, isProfiler);
        setAttribute0(targetON, "jvm_options", jvmOptions.getJvmOptionsAsStoredInXml());
        postInvoke("deleteJvmOptions", null);

        return invalid;
    }

//Audit module
    public ObjectName createAuditModule(AttributeList   attrs, 
                                        Properties      props, 
                                        String          targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);
        check1ToN(target);
        final ObjectName securityService = getSecurityServiceMBean(target);
        final ObjectName on = (ObjectName)invoke1(securityService, 
            "createAuditModule", attrs, AttributeList.class.getName());
        setProperties(on, props);
        return on;
    }

    public ObjectName[] listAuditModules(String targetName) 
        throws MBeanException
    {
        final Target target = getListTarget(targetName);
        final ObjectName securityService = getSecurityServiceMBean(target);
        return (ObjectName[])invoke0(securityService, "getAuditModule");
    }

    public boolean deleteAuditModule(String name, String targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);
        check1ToN(target);
        final ObjectName securityService = getSecurityServiceMBean(target);
        invoke1(securityService, "removeAuditModuleByName", name, 
                String.class.getName());
        return true;
    }

//Jms host
    public ObjectName createJmsHost(AttributeList   attrs, 
                                    Properties      props, 
                                    String          targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);
        check1ToN(target);
        final ObjectName jmsService = getJmsServiceMBean(target, true);
        final ObjectName jmsHost = (ObjectName)invoke1(jmsService, 
            "createJmsHost", attrs, AttributeList.class.getName());
        setProperties(jmsHost, props);
        return jmsHost;
    }

    public boolean deleteJmsHost(String name, String targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);
        check1ToN(target);
        final ObjectName jmsService = getJmsServiceMBean(target, false);
        assertIt((jmsService != null), 
            localStrings.getString("configsMBean.no_jms_service"));

	//START_BUG_FIX: 6178076
	if(name.equals(getAttribute0(jmsService, "default_jms_host"))) {
	    // set it to null since the jms host is being deleted.
	    // runtime has default anyway.
	    setAttribute0(jmsService, "default_jms_host", null);	
            _sLogger.log(Level.FINE, 
		"default-jms-host attribute of jms-service for target " 
		+ targetName
		+ " has been set to null");
	}
	//END_BUG_FIX: 6178076

        invoke1(jmsService, "removeJmsHostByName", name, 
                String.class.getName());
        return true;
    }

    public ObjectName[] listJmsHosts(String targetName) throws MBeanException
    {
        final Target target = getListTarget(targetName);
        final ObjectName jmsService = getJmsServiceMBean(target, false);
        assertIt((jmsService != null), 
            localStrings.getString("configsMBean.no_jms_service"));
        return (ObjectName[])invoke0(jmsService, "getJmsHost");
    }

    //**********************************************************************
    private ElementProperty[] convertPropertiesToElementProperties(Properties props)
    {
        ArrayList list = new ArrayList();
        Enumeration keys = props.keys();
        while (keys.hasMoreElements())
        {
            final String key = (String)keys.nextElement();
            ElementProperty property = new ElementProperty();
            property.setName(key);
            property.setValue((String)props.get(key));
            list.add(property);
        }
        return (ElementProperty[])list.toArray(new ElementProperty[list.size()]);
    }
    
static final String PROVIDER_TYPE_SERVER = "server";
static final String PROVIDER_TYPE_CLIENT = "client";

    //**********************************************************************
    //message-security
    /*
     * Creates the message-security-config and provider-config sub elements 
     * for the security-service. 
     * If the message-layer doesnt exist a new subelement for message-security-config 
     * is created and then the provider-config is created under it. 
     */
    public ObjectName createMessageSecurityProvider(
        String messageLayer,     // auth-layer attribute of message-security-config

        String providerId,        // provider id
        String providerType,      // "client"/"server"/"client-server"
        String providerClassName, // The class-name of java class  of the provider. 
       
        String requestAuthSource,    // requirement for message layer sender authentication (can be null)
        String requestAuthRecipient, // requirement for message layer receiver  authentication (can be null)
 
        String responseAuthSource,    // requirement for message layer sender authentication (can be null)
        String responseAuthRecipient, // requirement for message layer receiver  authentication (can be null)
        
        boolean isDefaultProvider,    // set pro
        Properties      props,        //provider's properties 
        String          targetName    // target name (config, instance, cluster, or 'server')
        )
        throws Exception
    {
        final Target target = getTarget(targetName);
        check1ToN(target);
        final ConfigTarget configTarget = target.getConfigTarget();
        boolean bMessageConfigJustCreated = false;
        Config config = ConfigAPIHelper.getConfigByName(getConfigContext(), configTarget.getName());
        SecurityService securityService = config.getSecurityService();
        
        //message-security-config
        MessageSecurityConfig messageSecurity = securityService.getMessageSecurityConfigByAuthLayer(messageLayer);
        if(messageSecurity==null)
        {
          // we have to create it 
           messageSecurity = new MessageSecurityConfig();
           messageSecurity.setAuthLayer(messageLayer);
           bMessageConfigJustCreated = true;
        }
        
        //provider-config
        ProviderConfig provider = new ProviderConfig();
        provider.setClassName(providerClassName);
        provider.setProviderId(providerId);
        provider.setProviderType(providerType);
        
        //properties
        if (null != props)
        {
            provider.setElementProperty(convertPropertiesToElementProperties(props));
        }

        //request-policy
        if(requestAuthSource!=null || requestAuthRecipient!=null )
        {
            RequestPolicy requestPolicy = new RequestPolicy();
            if(requestAuthSource!=null)
                requestPolicy.setAuthSource(requestAuthSource);
            if(requestAuthRecipient!=null)
                requestPolicy.setAuthRecipient(requestAuthRecipient);
            provider.setRequestPolicy(requestPolicy);
        }

        //response-policy
        if(responseAuthSource!=null || responseAuthRecipient!=null )
        {
            ResponsePolicy responsePolicy = new ResponsePolicy();
            if(responseAuthSource!=null)
                responsePolicy.setAuthSource(responseAuthSource);
            if(responseAuthRecipient!=null)
                responsePolicy.setAuthRecipient(responseAuthRecipient);
            provider.setResponsePolicy(responsePolicy);
        }

        messageSecurity.addProviderConfig(provider);
        //set default provider
        if(isDefaultProvider)
        {
            if(PROVIDER_TYPE_SERVER.equals(providerType))
                messageSecurity.setDefaultProvider(providerId);
            else if(PROVIDER_TYPE_CLIENT.equals(providerType))
                messageSecurity.setDefaultClientProvider(providerId);
            else 
            {
                messageSecurity.setDefaultProvider(providerId);
                messageSecurity.setDefaultClientProvider(providerId);
            }
        }
        if(bMessageConfigJustCreated)
        {
            securityService.addMessageSecurityConfig(messageSecurity);
        }
        
        return getMBeanRegistry().getMbeanObjectName("provider-config", 
                   new String[]{getDomainName(), configTarget.getName(), messageLayer, providerId});
    }

    //**********************************************************************
    /*
     * Deletes the provider-config sub element for the given message-layer. 
     * If this is the last provider for the given message-layer, 
     * then the message-layer subelement is also deleted
     */
    public boolean deleteMessageSecurityProvider(
        String  messageLayer,     // auth-layer attribute of message-security-config
        String  providerId,   // provider id
        String  targetName    // target name (config, instance, cluster, or 'server')
        ) throws Exception
    {
        final Target target = getTarget(targetName);
        check1ToN(target);
        Config config = ConfigAPIHelper.getConfigByName(getConfigContext(), 
                        target.getConfigTarget().getName());
        SecurityService securityService = config.getSecurityService();
        MessageSecurityConfig messageSecurity = 
            securityService.getMessageSecurityConfigByAuthLayer(messageLayer);
        if(messageSecurity==null)
        {
            String msg = localStrings.getString( "admin.mbeans.configs.message_security_config_not_found", 
                    new Object[]{messageLayer, targetName});
            throw new MBeanConfigException(msg);
        }
        ProviderConfig[] providers = messageSecurity.getProviderConfig();
        if(providers.length==1 && providerId.equals(providers[0].getProviderId()))
        {
            securityService.removeMessageSecurityConfig(messageSecurity);
            return true;
        }
        ProviderConfig provider = messageSecurity.getProviderConfigByProviderId(providerId);
        if(provider==null)
        {
            String msg = localStrings.getString( "admin.mbeans.configs.security_provider_not_found", new Object[]{providerId, messageLayer, targetName});
            throw new MBeanConfigException(msg);
        }
        messageSecurity.removeProviderConfig(provider);

        return true;
    }

    //**********************************************************************
    //* Lists all the providers for a given message-layer.
    public ObjectName[] listMessageSecurityProviders(
        String  messageLayer,     // auth-layer attribute of message-security-config
        String  targetName    // target name (config, instance, cluster, or 'server')
        ) throws Exception
    {
        final Target target = getListTarget(targetName);
        Config config = ConfigAPIHelper.getConfigByName(getConfigContext(), 
                        target.getConfigTarget().getName());
        SecurityService securityService = config.getSecurityService();
        if(securityService==null)
            return null;
        if(messageLayer!=null)
        {
            MessageSecurityConfig messageSecurity = 
                   securityService.getMessageSecurityConfigByAuthLayer(messageLayer); 
            if(messageSecurity==null)
            {
                String msg = localStrings.getString( "admin.mbeans.configs.message_security_config_not_found", 
                        new Object[]{messageLayer, targetName});
                throw new MBeanConfigException(msg);
            }
            return getMessageSecurityProviders(messageSecurity);
        }
        //here we are for null-messageLayer only
        MessageSecurityConfig[] messageSecurities = 
               securityService.getMessageSecurityConfig(); 
        ArrayList arr = new ArrayList();
        if(messageSecurities!=null)
            for(int i=0; i<messageSecurities.length; i++)
            {
                ObjectName[] names = getMessageSecurityProviders(messageSecurities[i]);
                if(names!=null)
                    for(int j=0; j<names.length; j++)
                        arr.add(names[j]);
            }
        return (ObjectName[])arr.toArray(new ObjectName[arr.size()]);    
    }

    //**********************************************************************
    //* Lists all the providers for a given message-layer.
    private ObjectName[] getMessageSecurityProviders(
        MessageSecurityConfig messageSecurity
        ) throws Exception
    {
        ProviderConfig[] providers = messageSecurity.getProviderConfig();
        return ConfigMBeanHelper.getConfigBeansObjectNames(
            this.getMBeanRegistry(), this.getDomainName(), providers);
    }
    //**********************************************************************

    //JACC provider
    public ObjectName createJaccProvider(   AttributeList   attrs, 
                                            Properties      props, 
                                            String          targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);
        check1ToN(target);
        final ObjectName securityService = getSecurityServiceMBean(target);
        final ObjectName jaccProvider = (ObjectName)invoke1(securityService, 
            "createJaccProvider", attrs, AttributeList.class.getName());
        setProperties(jaccProvider, props);
        return jaccProvider;
    }

    public boolean deleteJaccProvider(String name, String targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);
        check1ToN(target);
        final ObjectName securityService = getSecurityServiceMBean(target);
        invoke1(securityService, "removeJaccProviderByName", name, 
                String.class.getName());
        return true;
    }

    public ObjectName[] listJaccProviders(String targetName) 
        throws MBeanException
    {
        final Target target = getListTarget(targetName);
        final ObjectName securityService = getSecurityServiceMBean(target);
        return (ObjectName[])invoke0(securityService, "getJaccProvider");
    }

//Thread pool
    public ObjectName createThreadPool(AttributeList   attrs, 
                                       Properties      props, 
                                       String          targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);
        check1ToN(target);
        final ObjectName threadPools = getThreadPoolsMBean(target);
        final ObjectName threadPool = (ObjectName)invoke1(threadPools, 
            "createThreadPool", attrs, AttributeList.class.getName());
        setProperties(threadPool, props);
        return threadPool;
    }

    public boolean deleteThreadPool(String threadPoolId, String targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);
        check1ToN(target);
        final ObjectName threadPools = getThreadPoolsMBean(target);
        invoke1(threadPools, "removeThreadPoolByThreadPoolId", 
            threadPoolId, String.class.getName());
        return true;
    }

    public ObjectName[] listThreadPools(String targetName) 
        throws MBeanException
    {
        final Target target = getListTarget(targetName);
        final ObjectName threadPools = getThreadPoolsMBean(target);
        return (ObjectName[])invoke0(threadPools, "getThreadPool");
    }

//EJB Timer Service
    public ObjectName createEjbTimerService(AttributeList   al, 
                                            Properties      props, 
                                            String          targetName)
        throws MBeanException
    {
        final ObjectName ejbContainer = getEjbContainer(targetName);
        final ObjectName on = (ObjectName)invoke1(ejbContainer, 
            "createEjbTimerService", al, AttributeList.class.getName());
        setProperties(on, props);
        return on;
    }

    public boolean deleteEjbTimerService(String targetName)
        throws MBeanException
    {
        final ObjectName ejbContainer = getEjbContainer(targetName);
        invoke0(ejbContainer, "removeEjbTimerService");
        return true;
    }

//Session Properties
    public ObjectName createSessionProperties(  AttributeList   al, 
                                                Properties      props, 
                                                String          targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);
        check1ToN(target);
        final ObjectName sessionConfig = getSessionConfigMBean(target, true);
        final ObjectName on = (ObjectName)invoke1(sessionConfig, 
            "createSessionProperties", al, AttributeList.class.getName());
        setProperties(on, props);
        return on;
    }

    public boolean deleteSessionProperties(String targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);
        check1ToN(target);
        final ObjectName sessionConfig = getSessionConfigMBean(target, false);
        invoke0(sessionConfig, "removeSessionProperties");
        return true;
    }

//Manager Properties
    public ObjectName createManagerProperties(  AttributeList   al, 
                                                Properties      props, 
                                                String          targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);
        check1ToN(target);
        final ObjectName sessionManager = getSessionManagerMBean(target, true);
        final ObjectName on = (ObjectName)invoke1(sessionManager, 
            "createManagerProperties", al, AttributeList.class.getName());
        setProperties(on, props);
        return on;
    }

    public boolean deleteManagerProperties(String targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);
        check1ToN(target);
        final ObjectName sessionManager = getSessionManagerMBean(target, false);
        invoke0(sessionManager, "removeManagerProperties");
        return true;
    }

//Store Properties
    public ObjectName createStoreProperties(AttributeList   al, 
                                            Properties      props, 
                                            String          targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);
        check1ToN(target);
        final ObjectName sessionManager = getSessionManagerMBean(target, true);
        final ObjectName on = (ObjectName)invoke1(sessionManager, 
            "createStoreProperties", al, AttributeList.class.getName());
        setProperties(on, props);
        return on;
    }

    public boolean deleteStoreProperties(String targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName);   
        check1ToN(target);
        final ObjectName sessionManager = getSessionManagerMBean(target, false);
        invoke0(sessionManager, "removeStoreProperties");
        return true;
    }

//Session Config
    public boolean deleteSessionConfig(String targetName)
        throws MBeanException
    {
        final Target target = getTarget(targetName); 
        check1ToN(target);
        final ObjectName webContainer = getChild("web-container", null, target);
        invoke0(webContainer, "removeSessionConfig");
        return true;
    }

//Get methods
    public ObjectName getHttpService(String targetName) throws MBeanException
    {
        return getChild("http-service", null, targetName);
    }

    public ObjectName getIiopService(String targetName) throws MBeanException
    {
        return getChild("iiop-service", null, targetName);
    }

    public ObjectName getEjbContainer(String targetName) throws MBeanException
    {
        return getChild("ejb-container", null, targetName);
    }

    public ObjectName getWebContainer(String targetName) throws MBeanException
    {
        return getChild("web-container", null, targetName);
    }

    public ObjectName getMdbContainer(String targetName) throws MBeanException
    {
        return getChild("mdb-container", null, targetName);
    }

    public ObjectName getJmsService(String targetName) throws MBeanException
    {
        return getChild("jms-service", null, targetName);
    }

    public ObjectName getLogService(String targetName) throws MBeanException
    {
        return getChild("log-service", null, targetName);
    }

    public ObjectName getSecurityService(String targetName) throws MBeanException
    {
        return getChild("security-service", null, targetName);
    }

    public ObjectName getTransactionService(String targetName)
        throws MBeanException
    {
        return getChild("transaction-service", null, targetName);
    }

    public ObjectName getMonitoringService(String targetName)
        throws MBeanException
    {
        return getChild("monitoring-service", null, targetName);
    }

    public ObjectName getJavaConfig(String targetName) throws MBeanException
    {
        return getChild("java-config", null, targetName);
    }

    public ObjectName getHttpListener(String id, String targetName)
        throws MBeanException
    {
        return getChild("http-listener", new String[]{id}, targetName);
    }

    public ObjectName getVirtualServer(String id, String targetName)
        throws MBeanException
    {
        return getChild("virtual-server", new String[]{id}, targetName);
    }

    public ObjectName getIiopListener(String id, String targetName)
        throws MBeanException
    {
        return getChild("iiop-listener", new String[]{id}, targetName);
    }

    public ObjectName getOrb(String targetName) throws MBeanException
    {
        return getChild("orb", null, targetName);
    }

    public ObjectName getJmsHost(String name, String targetName)
        throws MBeanException
    {
        return getChild("jms-host", new String[]{name}, targetName);
    }

    public ObjectName getAuthRealm(String name, String targetName)
        throws MBeanException
    {
        return getChild("auth-realm", new String[]{name}, targetName);
    }

    public ObjectName getAuditModule(String name, String targetName)
        throws MBeanException
    {
        return getChild("audit-module", new String[]{name}, targetName);
    }

    public ObjectName getJaccProvider(String name, String targetName)
        throws MBeanException
    {
        return getChild("jacc-provider", new String[]{name}, targetName);
    }

    public ObjectName getModuleLogLevels(String targetName) 
        throws MBeanException
    {
        return getChild("module-log-levels", null, targetName);
    }

    public ObjectName getModuleMonitoringLevels(String targetName) 
        throws MBeanException
    {
        return getChild("module-monitoring-levels", null, targetName);
    }

    public ObjectName getThreadPool(String threadPoolId, String targetName)
        throws MBeanException
    {
        return getChild("thread-pool", new String[] {threadPoolId}, targetName);
    }

    public ObjectName getEjbTimerService(String targetName)
        throws MBeanException
    {
        return getChild("ejb-timer-service", null, targetName);
    }

    public ObjectName getProfiler(String targetName) throws MBeanException
    {
        return getChild("profiler", null, targetName);
    }

    public ObjectName getSsl(String type, String id, String targetName)
        throws MBeanException
    {
        final Target target = getListTarget(targetName);
        ObjectName sslTarget = getSslTargetObjectName(target, id, type);
        if ("iiop-service".equals(type))
        {
            sslTarget = (ObjectName)invoke0(sslTarget, "getSslClientConfig");
        }
        return (ObjectName)invoke0(sslTarget, "getSsl");
    }

    public ObjectName getSessionProperties(String targetName)
        throws MBeanException
    {
        return getChild("session-properties", null, targetName);
    }

    public ObjectName getManagerProperties(String targetName)
        throws MBeanException
    {
        return getChild("manager-properties", null, targetName);
    }

    public ObjectName getStoreProperties(String targetName)
        throws MBeanException
    {
        return getChild("store-properties", null, targetName);
    }

    /**
     * A generic operation that returns the ObjectName of a child element
     * given its type & parent's location.
     * eg:- 
     * getChild("http-listener", new String[]{"http-listener-1"}, "server")
     * getChild("ssl", new String[] {"http-listener-1"}, null);
     * getChild("ejb-container", null, null);
     * @type The type of the child elemenet. The types are standardized for all
     * the config elements that are exposed via config mbeans. Please read the 
     * mbean descriptor doc for a list of valid types.
     * @location an array of tokens that will be used to locate the parent.
     * Please note that the order of the tokens is important in order to obtain
     * the ObjectName of the parent mbean.
     * @targetName 
     * @throws MBeanException
     */
    public ObjectName getChild( String      type,
                                String[]    location, 
                                String      targetName) throws MBeanException
    {
        return getChild(type, location, getListTarget(targetName));
    }

    protected ObjectName getChild(String    type,
                                  String[]  location, 
                                  Target    target) throws MBeanException
    {
        final MBeanRegistryEntry entry = getMBeanRegistryEntry(type);
        final MBeanNamingInfo namingInfo = getMBeanNamingInfo(entry, type, 
                    getObjectNameTokens(target, location));
        doPersistenceCheck(namingInfo, getConfigContext());
        final ObjectName on = getObjectName(namingInfo);
        postInvoke("getChild", on);
        return on;
    }

    protected MBeanRegistry getMBeanRegistry() throws MBeanException
    {
        assertIt((super.m_registry != null), 
            localStrings.getString("configsMBean.null_registry"));
        return super.m_registry;
    }
  
    Target getListTarget(String targetName) throws MBeanException
    {        
        try
        {               
            return TargetBuilder.INSTANCE.createTarget(VALID_LIST_TYPES, targetName, 
                getConfigContext());            
        }
        catch (Exception e)
        {
            throw MBeanExceptionFormatter.toMBeanException(e, null);
        }        
    }
    
    Target getTarget(String targetName) throws MBeanException
    {        
        try
        {               
            Target target = TargetBuilder.INSTANCE.createTarget(VALID_CREATE_DELETE_TYPES, targetName, 
                getConfigContext());
            if (target.getType() == TargetType.SERVER || 
                target.getType() == TargetType.DAS) {
                //If we are operating on a server, ensure that the server is the only entity 
                //referencing its config
                String configName = ServerHelper.getConfigForServer(getConfigContext(),
                    target.getName()).getName();
                if (!ConfigAPIHelper.isConfigurationReferencedByServerOnly(getConfigContext(), 
                    configName, target.getName())) {
                        throw new ConfigException(localStrings.getString(
                            "configurationHasMultipleRefs", target.getName(), configName, 
                            ConfigAPIHelper.getConfigurationReferenceesAsString(
                                getConfigContext(), configName)));   
                }                                              
            } else if (target.getType() == TargetType.CLUSTER) {
                //If we are operating on a cluster, ensure that the cluster is the only entity 
                //referencing its config
                String configName = ClusterHelper.getConfigForCluster(getConfigContext(), 
                    target.getName()).getName();
                if (!ConfigAPIHelper.isConfigurationReferencedByClusterOnly(getConfigContext(), 
                    configName, target.getName())) {
                        throw new ConfigException(localStrings.getString(
                            "configurationHasMultipleRefs", target.getName(), configName, 
                            ConfigAPIHelper.getConfigurationReferenceesAsString(
                                getConfigContext(), configName))); 
                }     
            }
            return target;
        }
        catch (Exception e)
        {
            throw MBeanExceptionFormatter.toMBeanException(e, null);
        }        
    }

    
    void check1ToN(Target target) throws MBeanException
    {
         /*
         * Assumption: A domain should not have an associated config.
         * Calling getConfigTarget() on a DomainTarget must raise an
         * exception.
         */
         /* XXX QQ. Comment out to build.  FIXME
        boolean is1ToN = false;
        try
        {
            final ConfigTarget ct = target.getConfigTarget();
            is1ToN = ct.is1ToN(target);
        }
        catch (Exception e)
        {
            throw MBeanExceptionFormatter.toMBeanException(e, null);
        }
        if (is1ToN)
        {
            throw MBeanExceptionFormatter.toMBeanException(null, 
                localStrings.getString("configsMBean.multiple_refs"));
        }
        */
    }
   
    protected ObjectName getConfigMBean(Target target) throws MBeanException
    {
        ObjectName configMBean = null;
        try
        {
            final ConfigTarget ct = target.getConfigTarget();
            configMBean = new ObjectName(ct.getTargetObjectName(
                                         new String[] {getDomainName()}));
            
            postInvoke("getConfigMBean", configMBean);
        }
        catch (Exception e)
        {
            throw MBeanExceptionFormatter.toMBeanException(e, null);
        }
        return configMBean;
    }

    protected ObjectName getHttpServiceMBean(Target target) 
        throws MBeanException
    {
        final ObjectName configMBean = getConfigMBean(target);
        ObjectName ret = (ObjectName)invoke0(configMBean, "getHttpService");
        return ret;
    }

    protected ObjectName getIiopServiceMBean(Target target) 
        throws MBeanException
    {
        final ObjectName configMBean = getConfigMBean(target);
        ObjectName ret = (ObjectName)invoke0(configMBean, "getIiopService");
        return ret;
    }

    protected ObjectName getSecurityServiceMBean(Target target) 
        throws MBeanException
    {
        final ObjectName configMBean = getConfigMBean(target);
        ObjectName ret = (ObjectName)invoke0(configMBean, "getSecurityService");
        return ret;
    }

    protected ObjectName getJavaConfigMBean(Target target) 
        throws MBeanException
    {
        final ObjectName configMBean = getConfigMBean(target);
        final ObjectName ret = (ObjectName)invoke0(
                                    configMBean, "getJavaConfig");
        assertIt((ret != null), 
            localStrings.getString("configsMBean.no_java_config"));
        return ret;
    }

    protected ObjectName getHttpListenerMBean(Target target, String id)
        throws MBeanException
    {
        return (ObjectName)invoke1(getHttpServiceMBean(target), 
            "getHttpListenerById", id, String.class.getName());
    }

    protected ObjectName getIiopListenerMBean(Target target, String id)
        throws MBeanException
    {
        return (ObjectName)invoke1(getIiopServiceMBean(target), 
            "getIiopListenerById", id, String.class.getName());
    }

    protected ObjectName getSslTargetObjectName(Target target, 
                                                String id, 
                                                String type) 
        throws MBeanException
    {
        ObjectName on = null;
        if ("http-listener".equals(type))
        {
            on = getHttpListenerMBean(target, id);
        }
        else if ("iiop-listener".equals(type))
        {
            on = getIiopListenerMBean(target, id);
        }
        else if ("iiop-service".equals(type))
        {
            on = getIiopServiceMBean(target);
        }
        else
        {
            throw MBeanExceptionFormatter.toMBeanException(null, 
                localStrings.getString("configsMBean.invalid_ssl_target_type", 
                                       type));
        }
        assertIt((on != null), 
            localStrings.getString("configsMBean.target_for_ssl_not_found"));
        return on;
    }

    protected ObjectName getAuthRealmMBean(Target target, String name) 
        throws MBeanException
    {
        final ObjectName securityService = getSecurityServiceMBean(target);
        final ObjectName authRealm = (ObjectName)invoke1(securityService, 
            "getAuthRealmByName", name, String.class.getName());
        assertIt((authRealm != null), localStrings.getString(
                                    "configsMBean.auth_realm_not_found", name));
        return authRealm;
    }

    protected ObjectName getJmsServiceMBean(Target target, boolean create) 
        throws MBeanException
    {
        final ObjectName configMBean = getConfigMBean(target);
        ObjectName on = null;
        try
        {
            on = (ObjectName)invoke0(configMBean, "getJmsService");
        }
        catch (Exception e)
        {
            //log
        }
        if ((null == on) && create)
        {
            on = (ObjectName)invoke1(configMBean, 
                "createJmsService", null, AttributeList.class.getName());
        }
        return on;
    }

    protected ObjectName getThreadPoolsMBean(Target target) 
        throws MBeanException
    {
        final ObjectName configMBean = getConfigMBean(target);
        ObjectName ret = (ObjectName)invoke0(configMBean, "getThreadPools");
        return ret;
    }

    protected ObjectName getSessionConfigMBean(Target target, boolean create)
        throws MBeanException
    {
        ObjectName sessionConfig = null;
        try
        {
            sessionConfig = getChild("session-config", null, target);
        }
        catch (MBeanException mbe)
        {
            if (create)
            {
                //Create it
                final ObjectName webContainer = 
                    getChild("web-container", null, target);
                sessionConfig = (ObjectName)invoke1(webContainer, 
                    "createSessionConfig", null, AttributeList.class.getName());
            }
            else
            {
                throw mbe;
            }
        }
        return sessionConfig;
    }

    protected ObjectName getSessionManagerMBean(Target target, boolean create)
        throws MBeanException
    {
        final ObjectName sessionConfig = getSessionConfigMBean(target, create);
        ObjectName sessionManager = null;
        try
        {
            sessionManager = getChild("session-manager", null, target);
        }
        catch (MBeanException mbe)
        {
            if (create)
            {
                //Create it
                sessionManager = (ObjectName)invoke1(sessionConfig, 
                   "createSessionManager", null, AttributeList.class.getName());
            }
            else
            {
                throw mbe;
            }
        }
        return sessionManager;
    }

    protected MBeanServer getMBeanServer() throws MBeanException
    {
        return com.sun.enterprise.admin.common.MBeanServerFactory.getMBeanServer();
    }

    protected void preInvoke(String opName, Object[] params, String[] signature)
        throws MBeanException
    {
    }

    protected void postInvoke(String opName, Object ret) throws MBeanException
    {
    }

    protected String getDefaultAuthRealm(Target target) throws MBeanException
    {
        final ObjectName on = getSecurityServiceMBean(target);
        return (String)getAttribute0(on, "default_realm");
    }

    protected String[] getObjectNameTokens(Target target, String[] location)
        throws MBeanException
    {
        if (null == location) { location = new String[0]; }
        final String[] onTokens = new String[location.length + 2];
        onTokens[0] = getDomainName();
        onTokens[1] = getConfigRef(target);
        for (int i = 0; i < location.length; i++)
        {
            onTokens[i+2] = location[i];
        }
        return onTokens;
    }

    protected String getConfigRef(Target target) throws MBeanException
    {
        String configRef = null;
        try
        {
            configRef = target.getConfigRef();
        }
        catch (Exception e)
        {
            throw MBeanExceptionFormatter.toMBeanException(e, null);
        }
        return configRef;
    }

    protected String[] getJvmOptions(boolean isProfiler, Target target)
        throws MBeanException
    {
        final ObjectName targetON = getJvmOptionsTargetObjectName(
                                        target, isProfiler);
        String[] jvmOptions = (String[])getAttribute0(targetON, "jvm_options");
        if (null == jvmOptions) { jvmOptions = new String[0]; }
        return jvmOptions;
    }

    private ObjectName createSslClientConfig(ObjectName iiopService) 
        throws MBeanException
    {
        ObjectName ret = (ObjectName)invoke1(iiopService, 
                            "createSslClientConfig", null, 
                            AttributeList.class.getName());
        return ret;
    }

    private Object invoke0(ObjectName on, String opName) throws MBeanException
    {
        return invokeN(on, opName, null, null);
    }

    private Object invoke1(ObjectName on, String opName, Object o, String cls) 
        throws MBeanException
    {
        return invokeN( on, opName, new Object[] {o}, new String[] {cls} );
    }

    private Object invokeN(ObjectName   on, 
                           String       opName, 
                           Object[]     params, 
                           String[]     signature) 
        throws MBeanException
    {
        Object ret = null;
        try
        {
            ret = getMBeanServer().invoke(on, opName, params, signature);
            postInvoke(opName, ret);
        }
        catch (Exception e)
        {
            throw MBeanExceptionFormatter.toMBeanException(e, null);
        }
        return ret;
    }

    private Object getPropertyValue0(ObjectName on, String propName)
        throws MBeanException
    {
        Object value = null;
        try
        {
            value = invoke1(on, "getPropertyValue", propName, "java.lang.String");
        }
        catch (Exception e)
        {
            throw MBeanExceptionFormatter.toMBeanException(e, null);
        }
        return value;
    }

    private Object getAttribute0(ObjectName on, String attrName)
        throws MBeanException
    {
        Object value = null;
        try
        {
            value = getMBeanServer().getAttribute(on, attrName);
        }
        catch (Exception e)
        {
            throw MBeanExceptionFormatter.toMBeanException(e, null);
        }
        return value;
    }

    private void setAttribute0(ObjectName on, String attrName, Object value)
        throws MBeanException
    {
        try
        {
            final Attribute attr = new Attribute(attrName, value);
            getMBeanServer().setAttribute(on, attr);
        }
        catch (Exception e)
        {
            throw MBeanExceptionFormatter.toMBeanException(e, null);
        }
    }

    private ObjectName getJvmOptionsTargetObjectName(Target     target, 
                                                     boolean    isProfiler)
        throws MBeanException
    {
        ObjectName on = getJavaConfigMBean(target);
        if (isProfiler)
        {
            on = (ObjectName)invoke0(on, "getProfiler");
        }
        assertIt((on != null), localStrings.getString(
            "configsMBean.target_for_jvm_options_not_found"));
        return on;
    }

    private void setProperties(ObjectName on, Properties props)
        throws MBeanException
    {
        if (null != props)
        {
            Enumeration keys = props.keys();
            while (keys.hasMoreElements())
            {
                final String key = (String)keys.nextElement();
                final Attribute property =  new Attribute(key, props.get(key));
                invoke1(on, "setProperty", property, Attribute.class.getName());
            }
        }
    }

    private void assertIt(boolean b, Object msg)
        throws MBeanException
    {
        if (!b)
        {
            throw MBeanExceptionFormatter.toMBeanException(
                    null, msg.toString());
        }
    }

    private MBeanRegistryEntry getMBeanRegistryEntry(String type)
        throws MBeanException
    {
        
        MBeanRegistryEntry entry = null;
        try
        {
            entry = getMBeanRegistry().findMBeanRegistryEntryByType(type);
        }
        catch (Exception e)
        {
            throw MBeanExceptionFormatter.toMBeanException(e, null);
        }
        assertIt((entry != null), 
            localStrings.getString("configsMBean.no_registry_entry", type));
        return entry;
    }
    
    private MBeanNamingInfo getMBeanNamingInfo(MBeanRegistryEntry entry,
                                                 String             type, 
                                                 String[]           loc)
        throws MBeanException
    {
        
        MBeanNamingInfo namingInfo = null;
        try
        {
            namingInfo = new MBeanNamingInfo(entry.getNamingDescriptor(), 
                            type, loc);
        }
        catch (Exception e)
        {
            throw MBeanExceptionFormatter.toMBeanException(e, null);
        }
        return namingInfo;
    }

    private void doPersistenceCheck(MBeanNamingInfo info, ConfigContext ctx)
        throws MBeanException
    {
        ConfigBean cb = null;
        try
        {
            final String xpath = info.getXPath();
            cb = ConfigBeansFactory.getConfigBeanByXPath(ctx, xpath);
            if (null == cb)
            {
                throw new InstanceNotFoundException(
                    localStrings.getString("configsMBean.mbean_not_found", 
                    info.getObjectName()));
            }
        }
        catch (Exception e)
        {
            throw MBeanExceptionFormatter.toMBeanException(e, null);
        }
    }

    private ObjectName getObjectName(MBeanNamingInfo info)
        throws MBeanException
    {
        ObjectName on = null;
        try
        {
            on = info.getObjectName();
        }
        catch (Exception e)
        {
            throw MBeanExceptionFormatter.toMBeanException(e, null);
        }
        return on;
    }

    private void checkElementDoesnotExist(ObjectName targetON, 
                                          String     getter, 
                                          Object     msg)
        throws MBeanException
    {
       ObjectName on = null;
       try
       {
           on = (ObjectName)invoke0(targetON, getter);
       }
       catch (MBeanException mbe)
       {
           //log??
       }
       if (on != null)
       {
           throw MBeanExceptionFormatter.toMBeanException(
               null, msg.toString());
       }
    }
    /** Adds the properties while creating the virtual server. This method
        is required because of the bug 4963449. Once the dtd is modified,
        we may not use this method and its call.
        <P> Date: Dec 09, 2003
    */
    private void addMandatoryVirtualServerProperties(final AttributeList al,
                                                     final Properties p)
    {
        setProperty(p, false, SystemPropertyConstants.ACCESSLOG_PROPERTY,
                    SystemPropertyConstants.getAccessLogDefaultValue());
        final String ca = 
            AttributeListUtils.toJmx12Attribute(ServerTags.DEFAULT_WEB_MODULE);
        if (! AttributeListUtils.
              containsNamedAttribute(al, ca))
        {
            setProperty(p, false, SystemPropertyConstants.DOCROOT_PROPERTY,
                      SystemPropertyConstants.getDocRootDefaultValue());
        }
        else
        {
            final Attribute defaultWb = 
                (Attribute)AttributeListUtils.asNameMap(al).get(ca);
            final String val = (String)defaultWb.getValue();
            if ((val == null) || "".equals(val))
            {
                setProperty(p, false, SystemPropertyConstants.DOCROOT_PROPERTY,
                      SystemPropertyConstants.getDocRootDefaultValue());
            }
        }
    }
    
    private void setProperty(final Properties table, final boolean force, 
                             final String name, final String value)
    {
        if (force)
            table.setProperty(name, value);
        else
            if (! table.containsKey(name))
                table.setProperty(name, value);
    }
 
    /** Always returns a non null instance of {@link java.util.Properties}.
     * Following rules apply: 
     * <ul>
     * <li> If the passed argument is null a non null empty Properties instance 
     * is returned. </li>
     * <li> All property mappings from the passed argument are copied "as-is" 
     * to the new Properties instance, that is returned. </li>
     * <ul>
     * Note that the constructor {@link Properties#Properties} is not useful for
     * this purpose.
     */
    private Properties copyProperties(final Properties p)
    {
        final Properties nn = new Properties();
        if (p != null)
        {
            final Enumeration e = p.propertyNames();
            while (e.hasMoreElements())
            {
                final String key    = (String)e.nextElement();
                final String value  = p.getProperty(key);
                nn.setProperty(key, value);
            }
        }
        return ( nn );
    }


    /**
     *
     *
     */
    protected String getDefaultHTTPListenerAcceptorThreads() {
        return DEFAULT_HTTP_LISTENER_ACCEPTOR_THREADS;
    } 

    /**
     *
     *
     */
    protected Config getConfigBeanForTarget(Target target)  throws Exception
    {
        final ConfigTarget configTarget = target.getConfigTarget();
        return ConfigAPIHelper.getConfigByName(getConfigContext(), configTarget.getName());
    }
}
