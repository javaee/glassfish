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

package com.sun.enterprise.ee.admin.mbeans;

import com.sun.enterprise.admin.mbeans.ConfigsMBean;
import com.sun.enterprise.admin.mbeans.ConfigMBeanUtil;
import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;


import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.ServerTags;


import com.sun.enterprise.ee.admin.ExceptionHandler;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;

import com.sun.enterprise.ee.admin.configbeans.ConfigsConfigBean;

import com.sun.logging.ee.EELogDomains;

import java.util.Vector;
import java.util.Properties;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;

import javax.management.MBeanException;     
import javax.management.ObjectName;   
import javax.management.Attribute;   
import javax.management.AttributeList;   

/**
 *
 * @author  kebbs
 */
public class ConfigsConfigMBean extends ConfigsMBean 
    implements IAdminConstants, com.sun.enterprise.ee.admin.mbeanapi.ConfigsConfigMBean
{

    /**
     * The default acceptor threads value for EE.
     *
     */
    public static final String DEFAULT_HTTP_LISTENER_ACCEPTOR_THREADS = "1";

    
    private static final StringManager _strMgr = 
        StringManager.getManager(ConfigsConfigMBean.class);

    private static Logger _logger = null;
        
    private static Logger getLogger() 
    {
        if (_logger == null) {
            _logger = Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
        }
        return _logger;
    }    
    
    private static ExceptionHandler _handler = null;
    
    //The exception handler is used to parse and log exceptions
    protected static ExceptionHandler getExceptionHandler() 
    {
        if (_handler == null) {
            _handler = new ExceptionHandler(getLogger());
        }
        return _handler;
    }           

    /** Creates a new instance of ConfigsConfigMBean */
    public ConfigsConfigMBean() {
        super();
    }
   
    private ConfigsConfigBean getConfigsConfigBean() 
    {
        return new ConfigsConfigBean(getConfigContext());        
    }
    
    /**
     * copyConfiguration can be passed a configContext so that the caller can 
     * provide the configContext.
     */
    public ObjectName copyConfiguration(String sourceConfigName,
        String newConfigName, Properties props) throws ConfigException, MBeanException
    {                    
        getConfigsConfigBean().copyConfiguration(sourceConfigName, newConfigName, props);
        return getConfigurationObjectName(newConfigName);        
    }
        
    public void deleteConfiguration(String configName)
        throws ConfigException 
    {
        getConfigsConfigBean().deleteConfiguration(configName);
    }                  
    
    public ObjectName[] listConfigurations(String targetName)    
        throws ConfigException, MBeanException
    {        
        return toConfigurationONArray(getConfigsConfigBean().listConfigurationsAsString(
            targetName));
    }

    /**
     * If includeDAS is false, omits the das config ObjectName from the config
     * list.
     * @param targetName
     * @param includeDAS
     * @throws ConfigException
     */
    public ObjectName[] listConfigurations(String targetName, 
            boolean includeDAS) throws ConfigException, MBeanException
    {
        ObjectName[] configs = listConfigurations(targetName);
        try {
            if (!includeDAS) {
                final ObjectName dasConfigPattern = new ObjectName(
                    IAdminConstants.DAS_CONFIG_OBJECT_NAME_PATTERN);
                final ArrayList al = new ArrayList();
                for (int i = 0; i < configs.length; i++) {
                    if (!dasConfigPattern.apply(configs[i])) {
                        al.add(configs[i]);
                    }
                }
                configs = (ObjectName[])al.toArray(new ObjectName[0]);
            }
        } catch (Exception ex) {
            throw getExceptionHandler().handleConfigException(
                    ex, "eeadmin.listConfigurations.Exception", targetName);
        }
        return configs;
    }

    /**
     * Returns the combined runtime status for the servers
     * refering to the config
     * 
     * @param   configName      Name of the config
     *
     * @return  boolean         Restart required status
     */
    boolean isRestartRequired(String configName) throws InstanceException
    {
        return getConfigsConfigBean().isRestartRequired(configName);
    }   


    /**
     *
     *
     */
    protected String getDefaultHTTPListenerAcceptorThreads() {
        return DEFAULT_HTTP_LISTENER_ACCEPTOR_THREADS;
    }

}
