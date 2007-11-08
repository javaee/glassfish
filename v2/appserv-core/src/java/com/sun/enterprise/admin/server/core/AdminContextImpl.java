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
package com.sun.enterprise.admin.server.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.net.URL;

import javax.management.MBeanServer;

import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.meta.MBeanRegistry;
import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
import com.sun.enterprise.admin.meta.AdminConfigEventListener;
import com.sun.enterprise.admin.util.proxy.Interceptor;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigContextEventListener;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContextFactory;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.server.ServerContextImpl;
import com.sun.enterprise.config.pluggable.ConfigEnvironment;
import com.sun.enterprise.config.pluggable.EnvironmentFactory;
import com.sun.enterprise.config.serverbeans.ServerHelper;

/**
 *
 */
public class AdminContextImpl implements AdminContext {

    private ServerContextImpl serverContext;
    private ConfigContext runtimeConfigContext;
    private ConfigContext adminConfigContext;
    private MBeanServer mbeanServer;
    private String domainName;
    private String serverName;
    private Logger adminLogger;
    private Interceptor configInterceptor;
    private URL    adminMBeanRegistryURL;
    private URL    runtimeMBeanRegistryURL;
    
    private static final String ADMIN_DESCRIPTORS_FILENAME   = "/admin-mbeans-descriptors.xml";
    private static final String RUNTIME_DESCRIPTORS_FILENAME = "/runtime-mbeans-descriptors.xml";

    /**
     * Creates a new instance of AdminContextImpl
     */
    public AdminContextImpl() {
        String loggerName = System.getProperty("com.sun.aas.admin.logger.name");
        if (loggerName != null) {
            adminLogger = Logger.getLogger(loggerName);
        } else {
            adminLogger = Logger.getAnonymousLogger();
        }
        //default PE settings 
        domainName = "com.sun.appserv";
        //notify MBeanRegistryFactory
        MBeanRegistryFactory.setAdminContext(this);
    }

    public AdminContextImpl(ServerContextImpl srvCtx) {
        this();
        setServerContext(srvCtx);
    }


    public void setServerContext(ServerContextImpl srvCtx) {
        serverContext = srvCtx;
        runtimeConfigContext = serverContext.getConfigContext();
        String configFileName = serverContext.getServerConfigURL();
        try {
            adminConfigContext = ConfigContextFactory.createConfigContext(
                    getAdminConfigEnvironment(configFileName));
            this.setServerName(srvCtx.getInstanceName());
            // Registering the config validator
            if (ServerHelper.isDAS(srvCtx.getConfigContext(), srvCtx.getInstanceName())) {
                registerValidator();
                registerConfigAdjustmentListener();
            }
        } catch (ConfigException ce) {
            adminLogger.log(Level.SEVERE, "core.determining_server_instance_failed",
                   ce);
        } catch (RuntimeException ce) {
            adminLogger.log(Level.SEVERE, "core.admin_config_read_error",
                   ce.getMessage());
            adminLogger.log(Level.WARNING, "core.admin_config_read_error_trace",
                   ce);
            throw ce;
        }
              
        adminLogger.log(Level.FINEST, "core.log_config_id_runtime",
                new Long(runtimeConfigContext.hashCode()));
        adminLogger.log(Level.FINEST, "core.log_config_is_admin",
                new Long(adminConfigContext.hashCode()));
        serverName = serverContext.getInstanceName();
    }

    protected void registerConfigAdjustmentListener() {
	
        // Added mechanism to avoid unadjusted domain.xml.<->MBeans<->dotted_names
             adminConfigContext.addConfigContextEventListener((ConfigContextEventListener)(new AdminConfigEventListener()));
    }
    protected void registerValidator() {
	
        // Added reflection mechanism to avoid compile time error
	try {
             Class cl = Class.forName("com.sun.enterprise.config.serverbeans.validation.DomainMgr");
             adminConfigContext.addConfigContextEventListener((ConfigContextEventListener)cl.newInstance());
        } catch (Exception ex) {
             adminLogger.log(Level.WARNING, "core.admin_validator_not_registered", "Error registering validator, config validator will not be available");
             adminLogger.log(Level.FINE, "core.admin_validator_register_error", ex.getMessage());
        }
    }

    public ConfigContext getAdminConfigContext() {
        return adminConfigContext;
    }
   
    public String getDomainName() {
        return domainName;
    }

    public MBeanServer getMBeanServer() {
        return mbeanServer;
    }

    public ConfigContext getRuntimeConfigContext() {
        return runtimeConfigContext;
    }
    
    public String getServerName() {
        return serverName;
    }
    
    public URL getAdminMBeanRegistryURL() {
        if(adminMBeanRegistryURL==null) {
            try { 
                //PE standard 
                adminMBeanRegistryURL  = MBeanRegistry.class.getResource(ADMIN_DESCRIPTORS_FILENAME);
            } catch (Throwable t) {
                adminLogger.log(Level.WARNING, "core.adminconfig_adminregistry_not_found",
                       ADMIN_DESCRIPTORS_FILENAME);
            }
        }
        return adminMBeanRegistryURL;
    }
    
    public URL getRuntimeMBeanRegistryURL() {
        if(runtimeMBeanRegistryURL==null) {
            try { 
                //PE standard 
                runtimeMBeanRegistryURL  = MBeanRegistry.class.getResource(RUNTIME_DESCRIPTORS_FILENAME);
            } catch (Throwable t) {
                adminLogger.log(Level.WARNING, "core.adminconfig_runtimeregistry_not_found",
                       RUNTIME_DESCRIPTORS_FILENAME);
            }
        }
        return runtimeMBeanRegistryURL;
    }
    
    public void setAdminConfigContext(ConfigContext ctx) {
    }
    
    public void setDomainName(String name) {
//        domainName = name;
    }
    
    public void setMBeanServer(MBeanServer mbs) {
        mbeanServer = mbs;
    }

    public void setRuntimeConfigContext(ConfigContext ctx) {
        if (serverContext != null) {
            serverContext.setConfigContext(ctx);
        }
    }
    
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setAdminMBeanRegistryURL(URL url) {
        adminMBeanRegistryURL = url;
    }
    
    public void setRuntimeMBeanRegistryURL(URL url) {
        runtimeMBeanRegistryURL = url;
    }
    
    public Logger getAdminLogger() {
        return adminLogger;
    }

    public void setAdminLogger(Logger logger) {
        adminLogger = logger;
    }

    public Interceptor getMBeanServerInterceptor() {
        if (configInterceptor == null) {
            configInterceptor = new ConfigInterceptor(this);
        }
        return configInterceptor;
    }

    public void setMBeanServerInterceptor(Interceptor interceptor) {
    }

    private ConfigEnvironment getAdminConfigEnvironment(String configFileName) {
        ConfigEnvironment ce = EnvironmentFactory.getEnvironmentFactory().
                                getConfigEnvironment();
        ce.setUrl(configFileName);
        ce.setReadOnly(false);
        ce.setCachingEnabled(false);
        ce.setRootClass("com.sun.enterprise.config.serverbeans.Domain");
        ce.setHandler("com.sun.enterprise.config.serverbeans.ServerValidationHandler");
        ce.getConfigBeanInterceptor().setResolvingPaths(false);
        return ce;
    }
    public String getDottedNameMBeanImplClassName(){
        return  "com.sun.enterprise.admin.mbeans.DottedNameGetSetMBeanImpl";
    }
}
