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
//Admin imports
import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.common.ObjectNameHelper;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;
import com.sun.enterprise.admin.util.HostAndPort;
//import com.sun.enterprise.admin.server.core.mbean.config.*;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.server.core.mbean.config.GenericConfigurator;
import com.sun.enterprise.admin.server.core.mbean.config.ServerController;
import com.sun.enterprise.admin.server.core.mbean.config.ManagedAdminServerInstance;
import com.sun.enterprise.admin.server.core.mbean.config.ManagedServerInstance;
import com.sun.enterprise.admin.server.core.mbean.config.naming.ConfigMBeanNamingInfo;

//Other imports
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.instance.ServerManager;

import com.sun.enterprise.admin.meta.MBeanRegistryEntry;
//server
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.instance.InstanceEnvironment;

import java.util.logging.Level;
import java.util.logging.Logger;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;


/**
	A class to construct proper type of MBean from its object name. This
	class gives the instances of mbeans that would be registered as given
	object names.
*/

public class MBeanManufacturer
{
    private ObjectName  mObjectName = null;
    private	Object      mConfigBean = null;
    private AdminContext mAdminContext;

    private static final Logger _logger = Logger.getLogger(AdminConstants.kLoggerName);
	
    // i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( MBeanManufacturer.class );
	

    public MBeanManufacturer (ObjectName oName, Object configBean) 
    {
        if (oName == null || configBean == null)
        {
			String msg = localStrings.getString( "admin.server.core.jmx.storage.null_object_name_or_config_bean" );
            throw new IllegalArgumentException( msg );
        }
        mObjectName = oName;
        mConfigBean = configBean;
    }

    public void setAdminContext(AdminContext ctx) {
        mAdminContext = ctx;
    }

    /**
        Returns an instance of proper MBean. This is required, when somebody
        wants to create the MBean for the first time in MBean Repository.
        The necessary parameters for MBean construction are derived from the
        configBean passed during consruction of this class.\
        @return instance of proper MBean corresponding to ObjectName. ObjectName
        is passed during the construction of this class. May not be null.
    */
    public Object createMBeanInstance()
    {
        Object mbeanInstance = null;
        String instanceName = ApplicationServer.getServerContext().getInstanceName();
        
        //8.0 first 
        if(mConfigBean instanceof MBeanRegistryEntry)
        {
            ConfigContext configContext;
            try
            {
                if (mAdminContext != null) {
                    configContext = mAdminContext.getAdminConfigContext();
                } else {
                    InstanceEnvironment instanceEnvironment = new InstanceEnvironment(instanceName);
                    //String fileUrl  = instanceEnvironment.getConfigFilePath();
                    /*Everything should be set in the backup file*/
                    String fileUrl  = instanceEnvironment.getBackupConfigFilePath();
                    configContext   = ConfigFactory.createConfigContext(fileUrl);
                }
                return ((MBeanRegistryEntry)mConfigBean).instantiateMBean(mObjectName,null, configContext);
            }
            catch(Exception e)
            {
                return null;
            }
        }
        String type         = ObjectNameHelper.getType(mObjectName);
        //String instanceName = ObjectNameHelper.getServerInstanceName(mObjectName);
        Level logLevel = Level.SEVERE;

        try
        {
            if (type.equals(ObjectNames.kController))
            {
                mbeanInstance = new ServerController(mAdminContext);
            }
            else if (type.equals(ObjectNames.kGenericConfigurator))
            {
                mbeanInstance = new GenericConfigurator();
            }
            else if (type.equals(ObjectNames.kServerInstance))
            {
                mbeanInstance = createServerInstanceMBean(instanceName);
/* *************** **
                if (instanceName.equals(ServerManager.ADMINSERVER_ID))
                {
                    mbeanInstance = createAdminServerInstance();
                }
                else
                {
                    mbeanInstance = createServerInstanceMBean(instanceName);
                }
** *************** */
            }
            else 
            {
                logLevel = Level.FINE;
                mbeanInstance = createGenericConfigMBean(mObjectName);
            }
        }
        catch(Exception e)
        {
            _logger.log(logLevel, "mbean.config.admin.create_mbean_instance_failed",  e );
        }
        
        return ( mbeanInstance );
    }
	
    private ManagedServerInstance createServerInstanceMBean(String instanceName) throws Exception
    {
        Server server = (Server)mConfigBean;
//patch for ms1
ConfigContext ctx = server.getConfigContext();
Config          config  = (Config) ConfigBeansFactory.getConfigBeanByXPath(ctx, ServerXPathHelper.XPATH_CONFIG);
        HttpService https = config.getHttpService();
        
        HttpListener[] hlArray = https.getHttpListener();
        //check not needed since there should always be atleast 1 httplistener
        //if you don't find one, use first one.
        HttpListener ls = hlArray[0];  
        //default is the first one that is enabled.
        for(int i = 0;i<hlArray.length;i++) {
            if(hlArray[i].isEnabled()) {
                ls = hlArray[i];
                break;
            }
        }
        String port = new PropertyResolver(ctx, instanceName).
                resolve(ls.getPort());
        int intPort = Integer.parseInt (port);
        HostAndPort hp = new HostAndPort("localhost", intPort);
        return new ManagedServerInstance(instanceName, hp, false, mAdminContext);
    }

    private ManagedAdminServerInstance createAdminServerInstance() 
        throws Exception
    {
        return new ManagedAdminServerInstance();
    }

    private Object createGenericConfigMBean(ObjectName objectName) throws MBeanConfigException
    {
        ConfigMBeanNamingInfo mbeanInfo = new ConfigMBeanNamingInfo(objectName);
        mbeanInfo.setAdminContext(mAdminContext);
        return mbeanInfo.constructConfigMBean();
    }

}
