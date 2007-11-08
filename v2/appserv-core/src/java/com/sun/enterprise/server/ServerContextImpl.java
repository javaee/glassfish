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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */

package com.sun.enterprise.server;

import java.io.File;
import javax.naming.InitialContext;

import com.sun.enterprise.Switch;
import com.sun.enterprise.NamingManager;
import com.sun.enterprise.InvocationManager;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.server.pluggable.PluggableFeatureFactory;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.admin.monitor.registry.MonitoringRegistry;

/**
 * ServerContext interface: the server-wide runtime environment created by
 * ApplicationServer and shared by its subsystems such as the web container
 * or EJB container.
 */
public class ServerContextImpl implements ServerContext {

    /** XXX: should move these to Config API */

    public final String SERVER_XML = "domain.xml";

    public final String DEFAULT_DOMAIN_NAME = "com.sun.appserv";

    public final String CONFIG_DIR = "config";

    /** server command line arguments */
    private String[] cmdLineArgs;

    /** Pluggable features factory */
    private PluggableFeatureFactory featureFactory;

    /** install root of this server instance */
    private String installRoot;

    /** name of this server instance */
    private String instanceName;

    /** local string manager for i18n */
    private static StringManager localStrings =
                            StringManager.getManager("com.sun.enterprise.server");

    /** common class loader i.e., $instance/lib classloader */
    private static ClassLoader commonClassLoader;

    /** shared class loader i.e., connector class loader */
    private static ClassLoader sharedClassLoader;

    /** parent class loader for the life cycle modules */
    private ClassLoader lifeCycleClassLoader;

    /** server config context */
    private ConfigContext configContext;

    /** server config bean */
    private Server server;

    // path to the server-wide configuration
    private String serverConfigPath = null;

    // server xml file URL
    private String serverConfigURL = null;

    /** environment object for this server instance */
    private InstanceEnvironment instanceEnvironment = null;

    /**
     * public constructor
     */
    public ServerContextImpl() {
      installRoot = System.getProperty(Constants.INSTALL_ROOT);
      instanceName = System.getProperty("com.sun.aas.instanceName");
      getInstanceEnvironment();
    }

    /**
     * Set the server command-line arguments
     *
     * @param cmdLineArgs array of command-line arguments
     */
    protected void setCmdLineArgs(String[] cmdLineArgs) {
        this.cmdLineArgs = cmdLineArgs;
    }

    /**
     * Get the server command-line arguments
     *
     * @return  the server command-line arguments
     */
    public String[] getCmdLineArgs() {
        return cmdLineArgs;
    }

    /**
     * Set pluggable features factory. Initialization of server context
     * must be followed by a call to this method. An easy way to obtain
     * a PluggableFeatureFactory is to call the static method getInstance()
     * on com.sun.enterprise.server.pluggable PluggableFeatureFactoryImpl
     * class.
     */
    public void setPluggableFeatureFactory(PluggableFeatureFactory obj) {
        featureFactory = obj;
    }

    /**
     * Get a factory for obtaining implementation of pluggable features.
     */
    public PluggableFeatureFactory getPluggableFeatureFactory() {
        return featureFactory;
    }

    /**
     * Install root of the server
     *
     * @param installRoot server installation root
     */
    protected void setInstallRoot(String installRoot) {
        this.installRoot = installRoot;
    }

    /**
     * Get server installation root
     *
     * @return    the server installation root
     */
    public String getInstallRoot() {
        return this.installRoot;
    }

    /**
     * Instace name
     *
     * @param instanceName name of this application server instance
     */
    protected void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    /**
     * Get the server instance name
     *
     * @return    the server instance name
     */
    public String getInstanceName() {
        return instanceName;
    }

    /**
     * Get the path to the server configuration directory
     */
    public String getServerConfigPath() {
        return instanceEnvironment.getConfigDirPath();
    }

    /**
     * Get a URL representation of any configuration file in server config path
     */
    public String getConfigURL(String configName) {
        String configURI = instanceEnvironment.getConfigFilePath();

        // XXX: Config API Doesn't expect URLs
        return configURI;
        // if (isWindows())
            // return "file:/" + configURI;
        // else
            // return "file:" + configURI;
    }

    /**
     * Get a URL representation of server configuration
     *
     * @return    the URL to the server configuration
     */
    public String getServerConfigURL() {
            return instanceEnvironment.getConfigFilePath();
    }

    /**
     * Get the initial naming context.
     */
    public InitialContext getInitialContext() {
        // XXX: cleanup static instance variables
        NamingManager mgr = Switch.getSwitch().getNamingManager();
        if (mgr == null) {
            return null;
        }
        return (InitialContext)(mgr.getInitialContext());
    }

    /**
     * Are we running on Windows platform?
     */
    private boolean isWindows() {
        return (File.separatorChar == '\\');
    }

    /**
     * Get the server configuration bean.
     *
     * @return  the server config bean
     */
    public Server getConfigBean() throws ConfigException {
        if (server != null) {
            return server;
        }

        // create the Server config bean
        if (configContext != null) {
            server = ServerBeansFactory.getServerBean(configContext);
        } else {
            // perhaps uninitialized?
            String msg = localStrings.getString(
                            "serverContext.config_context_is_null");
            throw new ConfigException(msg);
        }
        return this.server;
    }

    /**
     * Returns the config context for this server.
     *
     * @return    the config context
     */
    public ConfigContext getConfigContext() {
        return this.configContext;
    }

    /**
     * Sets the given config context.
     *
     * This method gets called from the AdminEventMulticaster to set the
     * config context after event processing is complete.
     *
     * @param    config    config context
     */
    public void setConfigContext(ConfigContext config) {
        this.configContext = config;
    }

    /**
     * Get the classloader that loads .jars in $instance/lib and classes
     * in $instance/lib/classes.
     *
     * @return  the common class loader for this server instance
     */
    public ClassLoader getCommonClassLoader() {
        return this.commonClassLoader;
    }

    /**
     * Set the classloader that loads .jars in $instance/lib and classes
     * in $instance/lib/classes.
     *
     * Only allow classes in this package to set this value.
     */
    protected void setCommonClassLoader(ClassLoader cl) {
        this.commonClassLoader = cl;
    }

    /**
     * Returns the shared class loader of the server instance.
     *
     * @return    the shared class loader
     */
    public ClassLoader getSharedClassLoader() {
        return this.sharedClassLoader;
    }

    /**
     * Sets the shared class loader for the instance.
     * Only allow classes in this package to set this value.
     *
     * @param    cl    shared class loader
     */
    protected void setSharedClassLoader(ClassLoader cl) {
        this.sharedClassLoader = cl;
    }

    /**
     * Returns the parent class loader for the life cycle modules.
     *
     * @return    the parent class loader for the life cycle modules
     */
    public ClassLoader getLifecycleParentClassLoader() {
        return this.lifeCycleClassLoader;
    }

    /**
     * Sets the parent class loader for the life cycle module
     *
     * @param    cl    the parent class loader for the life cycle module
     */
    protected void setLifecycleParentClassLoader(ClassLoader cl) {
        this.lifeCycleClassLoader = cl;
    }

    /**
     * Returns the environment object for this server instance.
     *
     * @return    the environment object for this server instance
     */
    public InstanceEnvironment getInstanceEnvironment() {
        if (this.instanceEnvironment == null) {
            this.instanceEnvironment =
                new InstanceEnvironment(this.instanceName);
        }
        return this.instanceEnvironment;
    }

    /**
     * Sets the instance environment object for this server instance.
     *
     * @param    instanceEnv    environment object for this server instance
     */
    void setInstanceEnvironment(InstanceEnvironment instanceEnv) {
        this.instanceEnvironment = instanceEnv;
    }

    /**
     * Get the J2EE Server invocation manager
     *
     * @return InvocationManager
     */
    public InvocationManager getInvocationManager() {
        return Switch.getSwitch().getInvocationManager();
    }

    /**
     * get the default domain name
     *
     * @return String default domain name
     */
    public String getDefaultDomainName() {
        return DEFAULT_DOMAIN_NAME;
    }
    /**
     * Returns the MonitoringRegistry implementation used for registration of
     * monitoring stats.
     * @return      instance of MonitoringRegistry
     */
    public MonitoringRegistry getMonitoringRegistry() {
        /* Get the class from appserv-rt.jar for now through
         * reflection */
        final String REG_IMPL_CLASS =
        "com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper";
        final String METHOD = "getInstance";
        try {
            final Class c = Class.forName(REG_IMPL_CLASS);
            final java.lang.reflect.Method m = c.getMethod(METHOD, (Class[]) null);
            final MonitoringRegistry r = (MonitoringRegistry) m.invoke(c, (Object[]) null);
            return r;
        }
        catch(Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
