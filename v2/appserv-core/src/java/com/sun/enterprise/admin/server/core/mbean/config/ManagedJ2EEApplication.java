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

package com.sun.enterprise.admin.server.core.mbean.config;

//Config imports
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.config.serverbeans.ServerTags;
//import com.sun.enterprise.config.serverbeans.J2eeApplication;

//JMX imports
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;

//Admin Imports
import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.common.EntityStatus;
import com.sun.enterprise.admin.common.constant.ConfigAttributeName;
import com.sun.enterprise.admin.common.exception.J2EEApplicationException;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;

//for jsr77
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.admin.event.AdminEventCache;
import com.sun.enterprise.admin.event.AdminEventResult;
import com.sun.enterprise.admin.event.ApplicationDeployEvent;
import com.sun.enterprise.admin.event.ModuleDeployEvent;
import com.sun.enterprise.admin.event.BaseDeployEvent;
import com.sun.enterprise.admin.event.AdminEventMulticaster;
import com.sun.enterprise.admin.server.core.channel.RMIClient;
import com.sun.enterprise.admin.server.core.channel.AdminChannel;
import com.sun.enterprise.admin.common.Status;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.Switch;


/**
    A class that represents any deployed J2EE application to a Server Instance.
    In other words it exposes all the parameters of an application that can be
    managed and all the operations that can be invoked on it. Whenever
    a J2EE application is deployed to a Server Instance, a corresponding instance
    of this class is registered in the MBeanServer. On removal of the application,
    the MBean will be deregistered.
    <p>
    There will be as many instances of this MBean as there are deployed
    J2EE applications in the Server Instance. Note that not all deployed
        applications are running (or enabled).
    <p>
    ObjectName of this MBean is: 
                ias:type=J2EEApplication, name=<appName>, InstanceName=<instanceName>
*/

public class ManagedJ2EEApplication extends ConfigMBeanBase implements ConfigAttributeName.J2EEApplication
{
    private static final String[][] MAPLIST    = 
    {
        {kName            , ATTRIBUTE + ServerTags.NAME},
        {kLocation        , ATTRIBUTE + ServerTags.LOCATION},
        {kVirtualServers  , ATTRIBUTE + ServerTags.VIRTUAL_SERVERS},
//        {kEnabled         , ATTRIBUTE + ServerTags.ENABLED},
        {kDescription     , ATTRIBUTE + PSEUDO_ATTR_DESCRIPTION},
    };

    private static final String[]   ATTRIBUTES  =
    {
        kName            + ", String,     R" ,
        kLocation        + ", String,     RW" ,
        kVirtualServers  + ", String,     RW" ,
//        kEnabled         + ", boolean,    RW" ,
        kDescription     + ", String,     RW" ,
    };
    

    private static final String[]   OPERATIONS  = 
    {
        "getModules(), INFO",
        "getEjbModules(), INFO",
        "getWebModules(), INFO",
        "getStatus(), INFO",
        "enable(), ACTION",
        "disable(), ACTION",
        "start(),     ACTION",
        "stop(),      ACTION",
        "getState(),   INFO"
    };

    /**
        Default constructor sets MBean description tables
    */
    public ManagedJ2EEApplication() throws MBeanConfigException
    {
        this.setDescriptions(MAPLIST, ATTRIBUTES, OPERATIONS);
    }


    public ManagedJ2EEApplication(String instanceName, String appName)
        throws MBeanConfigException
    {
        this(instanceName, appName, null);
    }

    public ManagedJ2EEApplication(String instanceName, String appName,
        AdminContext adminContext)
        throws MBeanConfigException
    {
        this(); //set description tables
        setAdminContext(adminContext);
        initialize(ObjectNames.kApplication, new String[]{instanceName, appName});
    }

    public String[] getModules() throws J2EEApplicationException
    {
        return getModulesByType(ModulesXMLHelper.MODULE_TYPE_ALL);
    }

    public String[] getEjbModules() throws J2EEApplicationException
    {
        return getModulesByType(ModulesXMLHelper.MODULE_TYPE_EJB);
    }

    public String[] getWebModules() throws J2EEApplicationException
    {
        return getModulesByType(ModulesXMLHelper.MODULE_TYPE_WEB);
    }

    private String[] getModulesByType(int moduleTypes) throws J2EEApplicationException
    {
        try
        {
            String location = (String)this.getAttribute(kLocation);
            return ModulesXMLHelper.getModulesFromApplicationLocation(location, moduleTypes);
        }
        catch (Exception e)
        {
            sLogger.throwing(getClass().getName(), "getModulesByType", e);
            throw new J2EEApplicationException(e.getMessage());
        }
    }
        
    
    /**
            Returns the Status of this Application.

            @throws J2EEApplicationException if the status can't be retrieved.
    */
    public EntityStatus getStatus() throws J2EEApplicationException
    {
        EntityStatus status = null;

        try
        {
            boolean isAppEnabled = true; //FIXME for RI only
//            boolean isAppEnabled = ((Boolean)this.getAttribute(kEnabled)).booleanValue();
            status = new EntityStatus();
            if (isAppEnabled)
            {
                status.setEnabled();
            }
            else
            {
                status.setDisabled();
            }
        }
        catch (Exception e)
        {
            sLogger.throwing(getClass().getName(), "getStatus", e);
            throw new J2EEApplicationException(e.getMessage());
        }
        return status;
    }

    /**
            Disables this application. The application should be enabled to begin
            with. There will
            be some time elapsed till the application is disabled. This method
            will return asynchronously. This is to exhibit the quiece capabilities.

            @throws J2EEApplicationException if the application is already disabled
                    or there is some error during disablement
    */
    public void disable() throws J2EEApplicationException
    {
        return; //FIXME: for RI only
/*        try
        {
            this.setAttribute(new Attribute(kEnabled, new Boolean(false)));
            super.getConfigContext().flush();
        }
        catch (Exception e)
        {
            sLogger.throwing(getClass().getName(), "disable", e);
            throw new J2EEApplicationException(e.getMessage());
        }
*/
    }

    /**
     * Disables the application. Difference between this method
     * and disable is persistence of the state. Disable method persists 
     * the state and this method does not persist the state.
     */
    public void stop() throws J2EEApplicationException {
        try {
            String appName = (String)this.getAttribute(kName);
            multicastAdminEvent(appName, BaseDeployEvent.DISABLE);
        } catch (Exception e) {
            sLogger.throwing(getClass().getName(), "stop", e);
            throw new J2EEApplicationException(e.getMessage());
        }
    }

    /**
     * Gets the jsr77 state corresponding to this module
     */

    public Integer getState() throws J2EEApplicationException {
        try {
            ServerContext serverContext = ApplicationServer.getServerContext();
            String namePattern = (
                serverContext.getDefaultDomainName() + ":" + 
                "j2eeType=J2EEApplication," + 
		"name=" + ((String)this.getAttribute(kName)) + "," + 
		"J2EEServer=" + serverContext.getInstanceName() + "," +
		"*");
            Integer intObj = (Integer)
	    	Switch.getSwitch().getManagementObjectManager().getState(namePattern);
	    return intObj;
        } catch (Exception e) {
            sLogger.throwing(getClass().getName(), "getState", e);
            throw new J2EEApplicationException(e.getMessage());
        }
    }

    /*
    public Integer getState() throws J2EEApplicationException {
        try {
            MBeanServer mbs = MBeanServerFactory.getMBeanServer();
            ServerContext serverContext = ApplicationServer.getServerContext();
            ObjectName objName = new ObjectName(
                serverContext.getDefaultDomainName() + ":" + 
                "j2eeType=J2EEApplication," + 
		"name=" + ((String)this.getAttribute(kName)) + "," + 
		"J2EEApplication=" + ((String)this.getAttribute(kName)) + "," + 
		"J2EEServer=" + serverContext.getInstanceName());
            Integer intObj = (Integer) mbs.getAttribute(objName, "state");
	    return intObj;
        } catch (Exception e) {
            sLogger.throwing(getClass().getName(), "getState", e);
            throw new J2EEApplicationException(e.getMessage());
        }
    }
    */


    /**
            Enables this application. The application should be disabled to begin 
            with. There will
            be some time elapsed till the application is enabled. This method
            will return asynchronously.

            @throws J2EEApplicationException if the application is already enabled
                    or there is some error during enablement
    */
    public void enable() throws J2EEApplicationException
    {
        return; //FIXME: for RI only
/*        try
        {
             this.setAttribute(new Attribute(kEnabled, new Boolean(true)));
            super.getConfigContext().flush();
        }
        catch (Exception e)
        {
            sLogger.throwing(getClass().getName(), "enable", e);
            throw new J2EEApplicationException(e.getMessage());
        }
*/
    }

    
    /**
     * Enables the application. Difference between this method
     * and enable is persistence of the state. Enable method persists 
     * the state and this method does not persist the state.
     */
    public void start() throws J2EEApplicationException {
        try {
            String appName = (String)this.getAttribute(kName);
            multicastAdminEvent(appName, BaseDeployEvent.ENABLE);
        } catch (Exception e) {
            sLogger.throwing(getClass().getName(), "start", e);
            throw new J2EEApplicationException(e.getMessage());
        }
    }

    public static final void main(String[] args) throws Exception
    {
        System.setProperty("com.sun.aas.instanceRoot", "e:\\tmp");
        ManagedJ2EEApplication appMBean = 
            new ManagedJ2EEApplication("adminapp", "admserv");
        EntityStatus status = appMBean.getStatus();
        sLogger.info("======== Status = " + status.getStatusString());
        if (status.isDisabled())
        {
            sLogger.info("======== Enabling app");
            appMBean.enable();
            status = appMBean.getStatus();
            sLogger.info("======== Status = " + status.getStatusString());
        }
        else
        {
            sLogger.info("======== Disabling app");
            appMBean.disable();
            status = appMBean.getStatus();
            sLogger.info("======== Status = " + status.getStatusString());
        }
    }

    /**
     * Multicasts the admin event so that the application gets loaded
     * dynamically without the need for reconfig. 
     */
    private void multicastAdminEvent(String entityName, String actionCode) 
                        throws J2EEApplicationException {
        String instanceName = super.getServerInstanceName();
        InstanceEnvironment instEnv = new InstanceEnvironment(instanceName);
        try {
                instEnv.applyServerXmlChanges(false);
        } catch (Exception e) {
            sLogger.throwing(getClass().getName(), "getAttr", e);
            throw new J2EEApplicationException(e.getMessage());
        }
        AdminEvent event = new ApplicationDeployEvent(instanceName, entityName, actionCode);
        //AdminEventCache.populateConfigChange(super.getConfigContext(), event);
        RMIClient serverInstancePinger = AdminChannel.getRMIClient(instanceName);
        if (serverInstancePinger.getInstanceStatusCode() != Status.kInstanceRunningCode) {
                    return;
        }
        AdminEventResult multicastResult = AdminEventMulticaster.multicastEvent(event);
        if (!AdminEventResult.SUCCESS.equals(multicastResult.getResultCode())) {
                AdminEventCache cache = AdminEventCache.getInstance(instanceName);
                    cache.setRestartNeeded(true);
        }
    }
}
