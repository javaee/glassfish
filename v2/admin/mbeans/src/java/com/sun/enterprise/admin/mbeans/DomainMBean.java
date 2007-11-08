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
 * $Id: DomainMBean.java,v 1.6 2007/05/15 21:47:52 km Exp $
 */

package com.sun.enterprise.admin.mbeans;

import java.lang.reflect.Method;
import java.io.File;
import java.io.IOException;

//JMX imports
import javax.management.AttributeList;

// commons imports
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.i18n.StringManager;

//config imports
import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.admin.config.ConfigMBeanHelper;
import com.sun.enterprise.admin.config.MBeanConfigException;
import com.sun.enterprise.admin.meta.MBeanRegistryEntry;
import com.sun.enterprise.admin.meta.naming.MBeanNamingDescriptor;

//core imports
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.server.ApplicationServer;

import com.sun.enterprise.admin.mbeanapi.IDomainMBean;
import com.sun.enterprise.config.serverbeans.PropertyResolver;

// Logging
import java.util.logging.Level;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
import com.sun.enterprise.admin.servermgmt.RepositoryConfig;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ConfigAPIHelper;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.JmsHost;
import com.sun.enterprise.config.serverbeans.JmsService;
import com.sun.enterprise.config.serverbeans.JmxConnector;
import com.sun.enterprise.jms.IASJmsUtil;


public class DomainMBean extends BaseConfigMBean
    implements IDomainMBean
{
    private static final StringManager localStrings =
            StringManager.getManager(DomainMBean.class);
    
    /**
     * Resolve tokens of the form ${..} in value.
     * @param value string value to resolve
     * @param instanceName - instance name for obtaining token values. If null, then DAS is used for resolution 
     *
     * @returns resolved string
     *
     */
    public String resolveTokens(String value, String instanceName) throws ConfigException
    {
        return resolveTokens(value, instanceName, false);
    }
    
    /**
     * Resolve tokens of the form ${..} in value.
     * @param value string value to resolve
     * @param instanceName - instance name for obtaining token values. If null, then DAS is used for resolution 
     * @param bResolvePathesAsWell - if true, resolves pathes as well (currently only for DAS!)
     *
     * @returns resolved string
     *
     */
    public String resolveTokens(String value, String instanceName, boolean bResolvePathesAsWell) throws ConfigException
    {
        //FIXME: bResolvePathesAsWell=true is working correct only for DAS 
        if(instanceName==null)
        {
            instanceName=MBeanRegistryFactory.getAdminContext().getServerName();
        }
        PropertyResolver resolver = new PropertyResolver(getConfigContext(), instanceName);
        return resolver.resolve(value, bResolvePathesAsWell);
    }
    /*
     * This operation allows to obtain third part software properties set such as jdbc-resources, connectors etc.
     * Since this set depend on what kind (brand) of software it is for, attributeList parameter defines attributes
     * for undesrstanding what kind of software used.
     * This operation suggests that correspondent MBeans' classes override static getDefaultCustomProperties(attributeList)
     * "mbeanTypeName" - parameter is typeName from mbeans descriptors file.
     */
    public AttributeList getDefaultCustomProperties(String mbeanTypeName, AttributeList attributeList)
    {
        if(mbeanTypeName==null)
            return null;
        try {
            MBeanRegistryEntry entry = m_registry.findMBeanRegistryEntryByType(mbeanTypeName);
            MBeanNamingDescriptor descr = entry.getNamingDescriptor();
            String className = descr.getMBeanClassName();
            Class cl = Class.forName(className);
            Method method = cl.getDeclaredMethod("getDefaultCustomProperties", new Class[]{Class.forName("javax.management.AttributeList")});
            return (AttributeList)method.invoke(null, new Object[]{attributeList});
        } catch (Exception e)
        {
            _sLogger.fine("getDefaultCustomProperties(): Exception for mbeanTypeName:"+mbeanTypeName);
            return null;
        }
            
    }
    
    /*
     * Returns default values for attributes for mbean. 
     * If Custom MBean class implents public static getDefaultAttributeValues(String[]) then it will be called,
     * otherwise returns DTD defined default values (from ConfigBeans)  
     *
     * if attrNames is null - all default attributes values are returning
     */
    public AttributeList getDefaultAttributeValues(String mbeanTypeName, String attrNames[])
    {
        if(mbeanTypeName==null)
            return null;
        try {
            MBeanRegistryEntry entry = m_registry.findMBeanRegistryEntryByType(mbeanTypeName);
            if(attrNames==null)
            {
                attrNames = entry.getAttributeNames();
                if(attrNames==null || attrNames.length<1)
                    return null;
            }
            MBeanNamingDescriptor descr = entry.getNamingDescriptor();
            String className = descr.getMBeanClassName();
            Class cl = Class.forName(className);
            Method method = null;
            try {
                method = cl.getDeclaredMethod("getDefaultAttributeValues", new Class[]{(new String[0]).getClass()});
                return (AttributeList)method.invoke(null, new Object[]{attrNames});
            } catch (Exception e)
            {
                //no custom implementation - just ignore
            }
            // standard 
            return ConfigMBeanHelper.getDefaultAttributeValues(descr, attrNames); 
        } catch (Exception e)
        {
            _sLogger.fine("getDefaultAttributeValues(): Exception for mbeanTypeName:"+mbeanTypeName);
            return null;
        }
    }

     /**
     * Returns the absolute path of the config directory.
     *
     * @returns the absolute path of the config directory.
     */
    public String getConfigDir() {
        InstanceEnvironment env = 
                ApplicationServer.getServerContext().getInstanceEnvironment();
        return env.getConfigDirPath();
    }
  
    private static final String BUNDLED_DOMAINS_ROOT = "/var/appserver/domains";

    private static final String AUTOSTART_FILENAME = "autostart";

    /**
     * Is autostart feature supported for this domain. Enabling autostart will
     * result in a domain being started up at the time of machine startup
     * (boot). The autostart feature is supported only on domains in default
     * domain directory of Solaris bundled release.
     * @returns true if autostart feature is supported for this domain.
     */
    public boolean isAutoStartSupported() {
        if (OS.isUnix()) {
            if (getConfigDir().startsWith(BUNDLED_DOMAINS_ROOT)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is auto start enabled for this domain. 
     * @throw MBeanConfigException if autostart is not supported for this domain
     */
    public boolean isAutoStartEnabled() throws MBeanConfigException {
        checkAutoStartSupported();
        File autoStartFile = getAutoStartFile();
        return autoStartFile.exists();
    }

    /**
     * Set autostart enabled state.
     * @param state if true enables autostart, otherwise disables autostart
     * @throw MBeanConfigException if autostart is not supported or if there
     *         is an error in setting autostart state.
     */
    public void setAutoStartEnabled(boolean state) throws MBeanConfigException {
        checkAutoStartSupported();
        boolean success = (state ? enableAutoStart() : disableAutoStart());
        if (!success) {
            String msg = localStrings.getString(
                    "admin.mbeans.domain.set_autostart_failed");
            throw new MBeanConfigException(msg);
        }
    }

    /**
     * Returns the name of this domain.
     *
     * @return  domain name
     */
    public String getName() throws MBeanConfigException {

        String name = null;

        try {
            name = System.getProperty(SystemPropertyConstants.DOMAIN_NAME);
        } catch (Exception e) {
            String msg = localStrings.getString(
                    "admin.mbeans.domain.get_name_failed")
                    + " " + e.getLocalizedMessage();
            throw new MBeanConfigException(msg);
        }

        if (name == null) {
            String msg = localStrings.getString(
                    "admin.mbeans.domain.get_name_failed");
            throw new MBeanConfigException(msg);
        }

        return name;
    }

    private boolean enableAutoStart() {
        File autoStartFile = getAutoStartFile();
        boolean success = false;
        try {
            if (!autoStartFile.exists()) {
                success = autoStartFile.createNewFile();
            } else {
                success = true;
            }
        } catch (IOException ioe) {
            _sLogger.log(Level.FINE, "mbean.autostart_ioexception", ioe);
            _sLogger.log(Level.WARNING, "mbean.autostart_enable_error",
                    new Object[] {autoStartFile, ioe.getMessage()});
        }
        return success;
    }

    private boolean disableAutoStart() {
        File autoStartFile = getAutoStartFile();
        boolean success = true;
        if (autoStartFile.exists()) {
            success = autoStartFile.delete();
        }
        return success;
    }

    private void checkAutoStartSupported() throws MBeanConfigException {
        if (!isAutoStartSupported()) {
            String msg = localStrings.getString(
                    "admin.mbeans.domain.autostart_not_supported");
            throw new MBeanConfigException(msg);
        }
    }

    private File getAutoStartFile() {
        return new File(getConfigDir(), AUTOSTART_FILENAME);
    }

    public void addClusteringSupportUsingProfile(final String profile) throws ConfigException {
        final ConfigContext acc    = MBeanRegistryFactory.getAdminContext().getAdminConfigContext();
        if (ServerHelper.isClusterAdminSupported(acc)) {
            final String msg = localStrings.getString("domain.supports.cluster");
            throw new ConfigException(msg);
        }
        try {
            final Config defaultConfig = getTemplateConfig(profile);
            addConfig(acc, defaultConfig);
            addClusterSupportElements(acc);
            configurAdminServer(acc);
            
        } catch(final Exception e) {
            throw new ConfigException(e);
        }
    }
    
    private Config getTemplateConfig(final String profile) throws IllegalArgumentException, ConfigException {
        final RepositoryConfig myRepos = new RepositoryConfig(); //all system properties are taken care of.
        final PEFileLayout layout = new PEFileLayout(myRepos);
        final File profileDomainXmlTemplate = layout.getPreExistingDomainXmlTemplateForProfile(profile);
        if (! profileDomainXmlTemplate.exists()) {
            final String msg = localStrings.getString("template.domain.xml.not.found",
                    profileDomainXmlTemplate.getAbsolutePath(), profile);
            throw new IllegalArgumentException(msg);
        }
        final ConfigContext tcc = ConfigFactory.createConfigContext(profileDomainXmlTemplate.getAbsolutePath(), true);
        //this created a config context from which we can just borrow the config element.
        final String tcn = SystemPropertyConstants.TEMPLATE_CONFIG_NAME;
        if (! exists(ConfigAPIHelper.getConfigsInDomain(tcc), tcn)) {
            final String msg = localStrings.getString("template.config.not.found", tcn, profileDomainXmlTemplate.getAbsolutePath());
            throw new IllegalArgumentException(msg);
        }
        final Config tc = ConfigAPIHelper.getConfigByName(tcc, tcn); // this has to exist
        return ( (Config)tc.clone() ); //cloning is required
    }

    private static boolean exists(final Config[] configs, final String configNamed) {
        boolean exists = false;
        for (final Config c : configs) {
            if (c.getName().equals(configNamed)) {
                exists = true;
                break;
            }
        }
        return ( exists );
    }
    private static void addConfig(final ConfigContext acc, final Config dc) throws ConfigException {
        final Configs configs = ConfigAPIHelper.getDomainConfigBean(acc).getConfigs();
        dc.setName(SystemPropertyConstants.TEMPLATE_CONFIG_NAME);
        configs.addConfig(dc);
        configureDefaultJmsHost(dc);
        addClientHostNameProperty2SystemJmxConnector(dc);
    }
    
    private static void addClusterSupportElements(final ConfigContext acc) throws ConfigException {
        final Domain domain = ConfigAPIHelper.getDomainConfigBean(acc);
        domain.setClusters(domain.newClusters());
        domain.setNodeAgents(domain.newNodeAgents());
        domain.setLoadBalancers(domain.newLoadBalancers());
        domain.setLbConfigs(domain.newLbConfigs());
    }
    private static void configurAdminServer(final ConfigContext acc) throws ConfigException {
        final Config dasc   = ServerHelper.getConfigForServer(acc, SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME);
        final JavaConfig jc = dasc.getJavaConfig();
        jc.addJvmOptions("-Djavax.management.builder.initial=com.sun.enterprise.ee.admin.AppServerMBeanServerBuilder");
        jc.addJvmOptions("-Dcom.sun.appserv.pluggable.features=com.sun.enterprise.ee.server.pluggable.EEPluggableFeatureImpl");
        addClientHostNameProperty2SystemJmxConnector(dasc);
    }
    
    private static void configureDefaultJmsHost(final Config tc) {
        //this configures the default_JMS_Host's attributes
        //default JMS Host is pointed to by the default-jms-host attribute of jms-service.
        final JmsService js = tc.getJmsService();
        final String jmshn  = js.getDefaultJmsHost();
        final JmsHost jmsh  = js.getJmsHostByName(jmshn);
        jmsh.setAdminUserName(IASJmsUtil.DEFAULT_USER);
        jmsh.setAdminPassword(IASJmsUtil.DEFAULT_PASSWORD);
        jmsh.setHost(System.getProperty(SystemPropertyConstants.HOST_NAME_PROPERTY));
        //don't set the port as it is "tokenized" appropriately
    }
    
    private static void addClientHostNameProperty2SystemJmxConnector(final Config someConfig) throws ConfigException {
        final com.sun.enterprise.config.serverbeans.AdminService as = someConfig.getAdminService();
        final JmxConnector jc    = as.getJmxConnectorByName(IAdminConstants.SYSTEM_CONNECTOR_NAME);
        final String hostValue   = System.getProperty(SystemPropertyConstants.HOST_NAME_PROPERTY);
        final String hostName    = IAdminConstants.HOST_PROPERTY_NAME;
        ElementProperty ep       = jc.getElementPropertyByName(hostName);

        if (ep == null) {
            ep = new ElementProperty();
            ep.setName(hostName);
            ep.setValue(hostValue);            
            jc.addElementProperty(ep);
        } else {
            ep.setValue(hostValue);
        }
    }
}
