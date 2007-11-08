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
//import com.sun.enterprise.config.serverbeans.WebModule;

//JMX imports
import javax.management.Attribute;

//Admin imports
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.common.EntityStatus;
import com.sun.enterprise.admin.common.constant.ConfigAttributeName;
import com.sun.enterprise.admin.common.exception.J2EEWebModuleException;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;

//for jsr77
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.admin.event.AdminEventCache;
import com.sun.enterprise.admin.event.AdminEventResult;
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
import com.sun.enterprise.management.StateManageable;


/**
    A class that represents a Standalone Managed J2EE Web module. Note that this is a
    standalone J2EE module, because it is deployed independently and not
    as a part of an application. Such a Web Module has certain additional
    manageable interface.
    <p>
    When a Web ARchive is deployed, an instance of this MBean is created in
    the MBeanServer. The MBean is deregistered when the module is removed.
    <p>
    ObjectName of this MBean is:
        ias:type=StandaloneJ2EEWebModule, name=<moduleName>
*/

public class ManagedStandaloneJ2EEWebModule extends ConfigMBeanBase implements ConfigAttributeName.StandaloneWebModule
{
    /**
     * Methods start and stop will use this attribute to store
     * the current state, RUNNING or STOPPED
     * These states correspond to jsr77 states
     * RUNNING_STATE (1) and STOPPED_STATE (3)
     */
    private int state = StateManageable.RUNNING_STATE;

    private static final String[][] MAPLIST    = 
    {
        {kName            , ATTRIBUTE + ServerTags.NAME},
        {kContextRoot     , ATTRIBUTE + ServerTags.CONTEXT_ROOT},
        {kLocation        , ATTRIBUTE + ServerTags.LOCATION},
        {kVirtualServers  , ATTRIBUTE + ServerTags.VIRTUAL_SERVERS},
//ms1        {kEnabled         , ATTRIBUTE + ServerTags.ENABLED},
        {kDescription     , ATTRIBUTE + PSEUDO_ATTR_DESCRIPTION},
    };

    private static final String[]   ATTRIBUTES  =
    {
        kName            + ", String,     R" ,
        kContextRoot     + ", String,     RW" ,
        kLocation        + ", String,     RW" ,
        kVirtualServers  + ", String,     RW" ,
//ms1        kEnabled         + ", boolean,    RW" ,
        kDescription     + ", String,     RW" ,
    };

    private static final String[]   OPERATIONS  = 
    {
        "getStatus(),   INFO",
        "enable(),      ACTION",
        "disable(),     ACTION",
        "start(),       ACTION",
        "stop(),        ACTION",
        "getState(),    INFO"
    };

    /**
        Default constructor sets MBean description tables
    */
    public ManagedStandaloneJ2EEWebModule() throws MBeanConfigException
    {
        this.setDescriptions(MAPLIST, ATTRIBUTES, OPERATIONS);
    }

    public ManagedStandaloneJ2EEWebModule(String instanceName, String moduleName)
        throws MBeanConfigException
    {
        this(); //set description tables
        initialize(ObjectNames.kStandaloneWebModule, new String[]{instanceName, moduleName});
    }

    /**
     * Returns the Status of this module.
     * @throws J2EEEjbJarModuleException if the status can't be retrieved.
    */
    public EntityStatus getStatus() throws J2EEWebModuleException
    {
        EntityStatus status = null;
        try
        {
            boolean isModuleEnabled = true; //FIXME for RI only
//            boolean isModuleEnabled = ((Boolean)this.getAttribute(kEnabled)).booleanValue();
            status = new EntityStatus();
            if (isModuleEnabled)
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
            throw new J2EEWebModuleException(e.getMessage());
        }
        return status;
    }

    /**
     * Disables this module.
     * @throws J2EEEjbJarModuleException if there is some error during 
     * disabling.
    */
    public void disable() throws J2EEWebModuleException
    {
        return; //FIXME for RI only;
/*        try
        {
            this.setAttribute(new Attribute(kEnabled, new Boolean(false)));
            super.getConfigContext().flush();
        }
        catch (Exception e)
        {
            sLogger.throwing(getClass().getName(), "disable", e);
            throw new J2EEWebModuleException(e.getMessage());
        }
*/
    }


    /**
     * Disables the application. Difference between this method
     * and disable is persistence of the state. Disable method persists 
     * the state and this method does not persist the state.
     */
    public void stop() throws J2EEWebModuleException {
	state = StateManageable.STOPPING_STATE;
        try {
            String moduleName = (String)this.getAttribute(kName);
            multicastAdminEvent(moduleName, BaseDeployEvent.DISABLE);
	    state = StateManageable.STOPPED_STATE;
        } catch (Exception e) {
            sLogger.throwing(getClass().getName(), "stop", e);
	    state = StateManageable.FAILED_STATE;
            throw new J2EEWebModuleException(e.getMessage());
        }
    }

    /**
     * Gets the jsr77 state corresponding to this module
     */
    public Integer getState() throws J2EEWebModuleException {

        return (new Integer(state));

	/**
	 * Commented the following since Tomcat container is 
	 * not ready with state management functionality
	 * Until such time it will return the value for state
	 * set by start and stop.
	 *
	 * Once the Tomcat functionality is ready this needs to be modified
	 *
	 */

	/*
        try {
            MBeanServer mbs = MBeanServerFactory.getMBeanServer();
            ServerContext serverContext = ApplicationServer.getServerContext();
            ObjectName objName = new ObjectName(
                serverContext.getDefaultDomainName() + ":" + 
                "j2eeType=WebModule," +
                "name=" + ((String)this.getAttribute(kName)) + "," + 
		"J2EEApplication=" + "null" + "," + 
		"J2EEServer=" + serverContext.getInstanceName());
            Integer intObj = (Integer) mbs.getAttribute(objName, "state");
	    return intObj;
        } catch (Exception e) {
            sLogger.throwing(getClass().getName(), "getState", e);
            throw new J2EEWebModuleException(e.getMessage());
        }
	*/
    }


    /**
     * Enables this module.
     * @throws J2EEEjbJarModuleException if there is some error during 
     * enablement.
    */
    public void enable() throws J2EEWebModuleException
    {
        return; //FIXME for RI only;
/*        try
        {
            this.setAttribute(new Attribute(kEnabled, new Boolean(true)));
            super.getConfigContext().flush();
        }
        catch (Exception e)
        {
            sLogger.throwing(getClass().getName(), "enable", e);
            throw new J2EEWebModuleException(e.getMessage());
        }
*/
    }

    /**
     * Enables the application. Difference between this method
     * and enable is persistence of the state. Enable method persists 
     * the state and this method does not persist the state.
     */
    public void start() throws J2EEWebModuleException {
	state = StateManageable.STARTING_STATE;
        try {
            String moduleName = (String)this.getAttribute(kName);
            multicastAdminEvent(moduleName, BaseDeployEvent.ENABLE);
	    state = StateManageable.RUNNING_STATE;
        } catch (Exception e) {
            sLogger.throwing(getClass().getName(), "start", e);
	    state = StateManageable.FAILED_STATE;
            throw new J2EEWebModuleException(e.getMessage());
        }
    }

    public String[] getServlets() throws J2EEWebModuleException
    {
        try
        {
            String location = (String)this.getAttribute(kLocation);
            return ModulesXMLHelper.getServletsForWebModule(location, null);
        }
        catch (Exception e)
        {
            sLogger.throwing(getClass().getName(), "getServlets", e);
            throw new J2EEWebModuleException(e.getMessage());
        }
    }


    /*
        all the other ias-ejb-jar.xml parameters that are exposed are
        taken from the super class ManagedJ2EEEjbJarModule
    */

    /**
     * Multicasts the admin event so that the application gets loaded
     * dynamically without the need for reconfig. 
     */
    private void multicastAdminEvent(String entityName, String actionCode) 
                        throws J2EEWebModuleException {
        String instanceName = super.getServerInstanceName();
        InstanceEnvironment instEnv = new InstanceEnvironment(instanceName);
        /*
        try {
                instEnv.applyServerXmlChanges(false);
        } catch (Exception e) {
            sLogger.throwing(getClass().getName(), "getAttr", e);
            throw new J2EEWebModuleException(e.getMessage());
        }
         **/
        AdminEvent event = new ModuleDeployEvent(instanceName, entityName, "web", actionCode);
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
