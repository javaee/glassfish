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

package com.sun.enterprise.ee.admin.configbeans;

import com.sun.enterprise.ee.admin.PortInUseException;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.config.serverbeans.ConfigAPIHelper;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ClusterHelper;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.TargetBuilder;
import com.sun.enterprise.admin.util.IAdminConstants;

import com.sun.logging.ee.EELogDomains;

import com.sun.enterprise.ee.admin.ExceptionHandler;

import com.sun.enterprise.admin.configbeans.BaseConfigBean;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;        
import java.util.Properties;
import java.util.Enumeration;

public class PropertyConfigBean extends BaseConfigBean implements IAdminConstants
{        
    
    private static final TargetType[] VALID_TYPES = new TargetType[] {
        TargetType.DOMAIN, TargetType.CONFIG, TargetType.CLUSTER, 
        TargetType.SERVER, TargetType.DAS};

    private static final StringManager _strMgr = 
        StringManager.getManager(PropertyConfigBean.class);

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
  
    public PropertyConfigBean(ConfigContext configContext) {
        super(configContext);
    }
    
    private void addProperties(Properties props, SystemProperty[] sysProps) 
    {
        if (sysProps != null) {
            for (int i = 0; i < sysProps.length; i++) {
                props.setProperty(sysProps[i].getName(), sysProps[i].getValue());
            }
        }
    }            
           
    Properties getTargetedProperties(String targetName, boolean inherit) throws ConfigException
    {                
        final ConfigContext configContext = getConfigContext();
        final Target target = TargetBuilder.INSTANCE.createTarget(VALID_TYPES, targetName, 
            configContext);            
        
        final Properties result = new Properties();
        if (target.getType() == TargetType.DOMAIN) {
            //list domain properties
            final Domain domain = ConfigAPIHelper.getDomainConfigBean(configContext);
            addProperties(result, domain.getSystemProperty());
        } else if (target.getType() == TargetType.CONFIG) {
            //list config properties
            if (inherit) {
                final Domain domain = ConfigAPIHelper.getDomainConfigBean(configContext);
                addProperties(result, domain.getSystemProperty());
            }
            final Config config = ConfigAPIHelper.getConfigByName(configContext, 
                target.getName());
            addProperties(result, config.getSystemProperty());            
        } else if (target.getType() == TargetType.CLUSTER) {            
            //list cluster properties
            if (inherit) {
                final Domain domain = ConfigAPIHelper.getDomainConfigBean(configContext);          
                addProperties(result, domain.getSystemProperty());
                final Config config = ClusterHelper.getConfigForCluster(configContext, 
                    target.getName());
                addProperties(result, config.getSystemProperty()); 
            }
            final Cluster cluster = ClusterHelper.getClusterByName(configContext, 
                target.getName());
            addProperties(result, cluster.getSystemProperty());
        } else if (target.getType() == TargetType.SERVER ||
            target.getType() == TargetType.DAS) {            
            //list server properties
            final Server server = ServerHelper.getServerByName(configContext, 
                target.getName());
            if (inherit) {
                final Domain domain = ConfigAPIHelper.getDomainConfigBean(configContext);            
                addProperties(result, domain.getSystemProperty());

                final Config config = ConfigAPIHelper.getConfigByName(configContext, server.getConfigRef());
                addProperties(result, config.getSystemProperty());
                if (ServerHelper.isServerClustered(configContext, target.getName()))  {
                    final Cluster cluster = ClusterHelper.getClusterForInstance(configContext, 
                        target.getName());
                    addProperties(result, cluster.getSystemProperty());
                }        
            }
            addProperties(result, server.getSystemProperty());
        } else {
            //list node agent properties. Shouldnt happen.
            throw new ConfigException(_strMgr.getString("invalidPropertyTarget",
                target.getName()));
        }     
        return result;
    }
                        
    public Properties listSystemProperties(String targetName, boolean inherit) throws ConfigException        
    {
        try {            
            return getTargetedProperties(targetName, inherit);            
        } catch (Exception ex) {
            throw getExceptionHandler().handleConfigException(
                ex, "eeadmin.listProperties.Exception", targetName);
        }
    }
    
    public String[] listSystemPropertiesAsString(String targetName, boolean inherit)
        throws ConfigException
    {    
        try {            
            Properties props = getTargetedProperties(targetName, inherit);
            String[] result = new String[props.size()];
            int i = 0;
            for (Enumeration e = props.propertyNames(); e.hasMoreElements(); i++) {
                String name = (String)e.nextElement();
                String value = (String)props.getProperty(name);
                result[i] = name + "=" + value;
            }           
            return result;
        } catch (Exception ex) {
            throw getExceptionHandler().handleConfigException(
                ex, "eeadmin.listProperties.Exception", targetName);
        }
    }
    
    private SystemProperty getSystemProperty(Properties props, String name)
    {                
        SystemProperty sysProp = new SystemProperty();
        sysProp.setName(name);
        sysProp.setValue((String)props.getProperty(name));
        return sysProp;
    }
    
    private void createServerProperties(Target target, Properties props)
        throws Exception
    {
        final ConfigContext configContext = getConfigContext();
        
        //create instance property                       
        final Server server = ServerHelper.getServerByName(configContext, 
            target.getName());   
        //Add the properties        
        final ArrayList removedProperties = new ArrayList();
        for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
            final String name = (String)e.nextElement();
            //If the property exists, remove it
            final SystemProperty sysProp = server.getSystemPropertyByName(name);
            if (sysProp != null) {                
                //Keep track of the properties we have removed in case we have
                //to "rollback". WARNING: after calling removeSystemProperty, the 
                //sysProp will be side affected (with null contents), so make a 
                //copy of it first.
                SystemProperty newProp = new SystemProperty();
                newProp.setName(sysProp.getName());
                newProp.setValue(sysProp.getValue());                
                server.removeSystemProperty(sysProp, OVERWRITE);
                removedProperties.add(newProp);
            }          
            server.addSystemProperty(getSystemProperty(props, name), 
                OVERWRITE);                    
        }    
        //Check for a port conflict after modifying server. If a port conflict occurs, 
        //we "rollback" the addition of the system properties.
        try {
            PortConflictCheckerConfigBean portChecker = getPortConflictCheckerConfigBean();   
            ServersConfigBean scb = getServersConfigBean();            
            portChecker.checkForPortConflicts(server, props, 
                scb.isRunning(server.getName()));
        } catch (Exception ex2) {                     
            try {
                //Delete all the properties that we have added
                for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
                    final String name = (String)e.nextElement();
                    final SystemProperty sysProp = server.getSystemPropertyByName(name);
                    if (sysProp != null) {
                        server.removeSystemProperty(sysProp, OVERWRITE);  
                    }
                }
                //Re-add any of the overwritten properties that were deleted.
                for (int i = 0; i < removedProperties.size(); i++) {
                    SystemProperty sysProp = (SystemProperty)removedProperties.get(i);                    
                    server.addSystemProperty((SystemProperty)removedProperties.get(i),
                        OVERWRITE);
                }
            } catch (Exception ex3) {
                //Log
                StringManagerBase sm = StringManagerBase.getStringManager(
                    getLogger().getResourceBundleName());            
                getLogger().log(Level.WARNING, 
                    sm.getString("eeadmin.createProperties.Exception", target.getName()), ex3);                    
            }
            //We do not want to propagate the PortInUseException up. It is a protected 
            //class used for internal use only
            if (ex2 instanceof PortInUseException) {
                throw new ConfigException(ex2.getMessage());
            } else {
                throw (ex2);
            }
        }
    }
    
    public void createSystemProperties(Properties props, String targetName)
        throws ConfigException
    {
        try {            
            final ConfigContext configContext = getConfigContext();
            final Target target = TargetBuilder.INSTANCE.createTarget(VALID_TYPES, targetName, 
                configContext);                           
            if (target.getType() == TargetType.DOMAIN) {
                //create domain property
                final Domain domain = ConfigAPIHelper.getDomainConfigBean(configContext);                   
                //Add the properties
                for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
                    final String name = (String)e.nextElement();
                    //If the property exists, remove it
                    final SystemProperty sysProp = domain.getSystemPropertyByName(name);
                    if (sysProp != null) {
                        domain.removeSystemProperty(sysProp, OVERWRITE);
                    }
                    domain.addSystemProperty(getSystemProperty(props, name), 
                        OVERWRITE);
                }      
            } else if (target.getType() == TargetType.CONFIG) {
                final Config config = ConfigAPIHelper.getConfigByName(configContext,  
                    target.getName());                   
                //Add the properties
                for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
                    final String name = (String)e.nextElement();
                    //If the property exists, remove it
                    final SystemProperty sysProp = config.getSystemPropertyByName(name);
                    if (sysProp != null) {
                        config.removeSystemProperty(sysProp, OVERWRITE);
                    }
                    config.addSystemProperty(getSystemProperty(props, name), 
                        OVERWRITE);
                }                      
            } else if (target.getType() == TargetType.CLUSTER) {
                final Cluster cluster = ClusterHelper.getClusterByName(configContext, 
                    target.getName());
                //Add the properties
                for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
                    final String name = (String)e.nextElement();
                    //If the property exists, remove it
                    final SystemProperty sysProp = cluster.getSystemPropertyByName(name);
                    if (sysProp != null) {
                        cluster.removeSystemProperty(sysProp, OVERWRITE);
                    }                                     
                    cluster.addSystemProperty(getSystemProperty(props, name), 
                        OVERWRITE);
                }                 
            } else if (target.getType() == TargetType.SERVER ||
                target.getType() == TargetType.DAS) {
                    createServerProperties(target, props);                
            } else {
                //create node agent property. Shouldnt happen
                throw new ConfigException(_strMgr.getString("invalidPropertyTarget",
                    target.getName()));
            }  
            //FIXTHIS: Not sure why we have to intentionally flush here. The changes get written to 
            //disk, but the runtime config context does not get updated!!! This is probably due to 
            //the fact that the config change was not handled and the server thinks it needs to 
            //restart
            flushAll();
        } catch (Exception ex) {
            throw getExceptionHandler().handleConfigException(
                ex, "eeadmin.createProperties.Exception", targetName);            
        }                
    }
        
    public void deleteSystemProperty(String propertyName, String targetName)
        throws ConfigException
    {
        try {
            final ConfigContext configContext = getConfigContext();
            final Target target = TargetBuilder.INSTANCE.createTarget(VALID_TYPES, targetName, 
                configContext);                           
            if (target.getType().equals(TargetType.DOMAIN)) {
                //create domain property
                final Domain domain = ConfigAPIHelper.getDomainConfigBean(configContext);
                final SystemProperty sysProp = domain.getSystemPropertyByName(propertyName);
                //Ensure that the property specified exists
                if (sysProp == null) {
                    throw new ConfigException(_strMgr.getString("propertyDoesNotExist",
                        propertyName, target.getName()));
                }
                //Remove the property
                domain.removeSystemProperty(sysProp, OVERWRITE);
            } else if (target.getType().equals(TargetType.CONFIG)){
                //create configuration property            
                final Config config = ConfigAPIHelper.getConfigByName(configContext,  
                    target.getName());   
                final SystemProperty sysProp = config.getSystemPropertyByName(propertyName);
                //Ensure that the property specified exists
                if (sysProp == null) {
                    throw new ConfigException(_strMgr.getString("propertyDoesNotExist",
                        propertyName, target.getName()));
                }
                //Remove the property
                config.removeSystemProperty(sysProp, OVERWRITE);
            } else if (target.getType().equals(TargetType.CLUSTER)) {
                //create instance property                       
                final Cluster cluster = ClusterHelper.getClusterByName(configContext, 
                    target.getName());
                final SystemProperty sysProp = cluster.getSystemPropertyByName(propertyName);
                //Ensure that the property specified exists
                if (sysProp == null) {
                    throw new ConfigException(_strMgr.getString("propertyDoesNotExist",
                        propertyName, target.getName()));
                }
                //Remove the property
                cluster.removeSystemProperty(sysProp, OVERWRITE);
            } else if (target.getType().equals(TargetType.SERVER) ||
                target.getType().equals(TargetType.DAS)) {
                //create instance property                       
                final Server server = ServerHelper.getServerByName(configContext, target.getName());
                final SystemProperty sysProp = server.getSystemPropertyByName(propertyName);
                //Ensure that the property specified exists
                if (sysProp == null) {
                    throw new ConfigException(_strMgr.getString("propertyDoesNotExist",
                        propertyName, target.getName()));
                }
                SystemProperty removedProp = new SystemProperty();
                removedProp.setName(sysProp.getName());
                removedProp.setValue(sysProp.getValue());
                //Remove the property
                server.removeSystemProperty(sysProp, OVERWRITE);
                /*
                 *Fix for bug 6303353
                 *We do not have to check if the system-property being deleted in this server-instance 
                 *is also being defined in another instance (on same machine).
                 *Ideally we want to check if this system-property is being referenced in any of
                 *the attributes in the associated config, and this is the only system-property that exists 
                 *to specify that value (not even in the parent config). But this would be an extensive search
                 *to go through the whole config and the attributes of all elements in there, which isn't really
                 *worth, when you have to just delete a system property.
                //Check for a port conflict after modifying server. If a port conflict occurs, 
                //we "rollback" the addition of the system properties.
                try {
                    PortConflictCheckerConfigBean portChecker = getPortConflictCheckerConfigBean();   
                    ServersConfigBean scb = getServersConfigBean();                    
                    portChecker.checkForPortConflicts(server, null, 
                        scb.isRunning(server.getName()));
                } catch (Exception ex2) {                         
                    server.addSystemProperty(removedProp, OVERWRITE);
                    //We do not want to propagate the PortInUseException up. It is a protected 
                    //class used for internal use only
                    if (ex2 instanceof PortInUseException) {
                        throw new ConfigException(ex2.getMessage());
                    } else {
                        throw (ex2);
                    }
                }
                */
            } else {
                //create node agent property. Shouldnt happen.
                throw new ConfigException(_strMgr.getString("invalidPropertyTarget",
                    target.getName()));
            }   
            //FIXTHIS: Not sure why we have to intentionally flush here. The changes get written to 
            //disk, but the runtime config context does not get updated!!! This is probably due to 
            //the fact that the config change was not handled and the server thinks it needs to 
            //restart
            flushAll();
        } catch (Exception ex) {
            throw getExceptionHandler().handleConfigException(
                ex, "eeadmin.deleteProperty.Exception", 
                new String[] {propertyName, targetName});             
        }            
    }
    
    private PortConflictCheckerConfigBean getPortConflictCheckerConfigBean() 
    {
        return new PortConflictCheckerConfigBean(getConfigContext());
    }
    
    private ServersConfigBean getServersConfigBean()
    {      
        return new ServersConfigBean(getConfigContext());        
    }
}
