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

package com.sun.enterprise.admin.server.core.jmx.storage;

//JMX imports
import javax.management.ObjectName;
import javax.management.InstanceNotFoundException;
//Admin imports
import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.common.ObjectNameHelper;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.server.core.mbean.config.GenericConfigurator;
import com.sun.enterprise.admin.server.core.mbean.config.ServerController;
import com.sun.enterprise.admin.server.core.mbean.config.naming.ConfigMBeanNamingInfo;
//Other imports
import com.sun.enterprise.instance.ServerManager;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.serverbeans.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
import com.sun.enterprise.admin.meta.MBeanRegistry;

/**
    A class that acts as the bridge between persistent store and
    MBean Repository. Uses the conversion between ObjectName and
    identification into Config API and carries out the search.
*/

public class PersistenceChecker
{
    private static final Logger _logger = Logger.getLogger(
            AdminConstants.kLoggerName);

    private AdminContext mAdminContext;

    /** Creates new PersistenceChecker */
    public PersistenceChecker () 
    {
        //for future use.
    }

    public void setAdminContext(AdminContext ctx) {
        mAdminContext = ctx;
    }

    private Object findElement_8_0(ObjectName objectName) throws InstanceNotFoundException
    {
        try
        {
            MBeanRegistry registry  = MBeanRegistryFactory.getAdminMBeanRegistry();
            return (Object)registry.findMBeanRegistryEntry(objectName);
        }
        catch(Exception e)
        {
            return null;
        }
    }
    public Object findElement(ObjectName objectName) throws InstanceNotFoundException
    {
        Object      match   = null;
        
        match= findElement_8_0(objectName);
        if(match!=null)
            return match;
        
        //Now try the old 7.0 namespace
        // which's still alive 
        String type         = ObjectNameHelper.getType(objectName);
        String instanceName = ObjectNameHelper.getServerInstanceName(objectName);
        
        if (type != null)
        {
            if (type.equals(ObjectNames.kController))
            {
                match = new ServerController();
            }
            else if (type.equals(ObjectNames.kGenericConfigurator))
            {
                match = new GenericConfigurator();
            }
            else if (type.equals(ObjectNames.kServerInstance))
            {
                match = findServerInstance(instanceName);
                if(match==null)
                    throw new InstanceNotFoundException(objectName.toString());
            }
            else  if (type.equals(ObjectNames.kDeployment))
            {
               match = findServerInstance(instanceName);
               if(match==null)
                   throw new InstanceNotFoundException(objectName.toString());
            }
            else
            {
                /* Ordinary Config Mbean - use ConfigMBeanNaming */
                match = findGenericConfigBean(objectName, instanceName);
            }
        }
        else
        {
            /* unknown type */
            _logger.log(Level.FINE, "mbean.config.admin.unknown_mbean_type", objectName.toString() );
        }
        return ( match );
    }

    private Object findServerInstance(String instanceName)
    {
        ServerManager sm = ServerManager.instance();
	Server serverInstance = null;
/*        boolean isAdminServer = instanceName.equals(
                                    ServerManager.ADMINSERVER_ID); */
        if (! sm.instanceExists(instanceName) /*&& !isAdminServer*/)
        {
            return null;
        }

        try
        {
            ConfigContext cc = getConfigContext(instanceName);
            serverInstance = ServerBeansFactory.getServerBean(cc);
        }
        catch(Exception e)
        {
            e.printStackTrace(); //no harm in squelching
        }
        return ( serverInstance );
    }
    
    private Object findGenericConfigBean(ObjectName objectName, String instanceName)
    {
        /* Ordinary Config Mbean - use ConfigMBeanNaming */
        Object bean = null;
        ConfigMBeanNamingInfo mbeanInfo = null;
        try
        {
            mbeanInfo = new ConfigMBeanNamingInfo(objectName);
        }
        catch (MBeanConfigException mce)
        {
            _logger.log(Level.FINE, "mbean.config.admin.naming_not_found", 
                        new Object[]{objectName.toString(), mce.getLocalizedMessage()} );
        }
        if(mbeanInfo!=null)
        {
            String xPath = mbeanInfo.getXPath();
            try
            {
                ConfigContext ctx = getConfigContext(instanceName);
                bean = ConfigBeansFactory.getConfigBeanByXPath(ctx, xPath);                
            }
            catch(Exception e)
            {
                _logger.log(Level.FINE, "mbean.config.admin.config_bean_not_found", 
                            new Object[]{xPath, e.getLocalizedMessage()} );
            }
        }
        return bean;
    }
    private ConfigContext getConfigContext(String instanceName) throws ConfigException
    {
        if (mAdminContext != null) {
            return mAdminContext.getAdminConfigContext();
        } else {
            String backupServerXmlPath = new InstanceEnvironment(instanceName).getBackupConfigFilePath();
            return (ConfigFactory.createConfigContext(backupServerXmlPath) );
        }
    }
    
}
