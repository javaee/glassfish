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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.enterprise.deploy.shared.ModuleType;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.common.constant.ConfigAttributeName;
import com.sun.enterprise.admin.common.constant.DeploymentConstants;
import com.sun.enterprise.admin.common.exception.AFException;
import com.sun.enterprise.admin.common.exception.AFJDBCResourceException;
import com.sun.enterprise.admin.common.exception.AFResourceException;
import com.sun.enterprise.admin.common.exception.AFRuntimeStoreException;
import com.sun.enterprise.admin.common.exception.ControlException;
import com.sun.enterprise.admin.common.exception.DeploymentException;
import com.sun.enterprise.admin.common.exception.IllegalStateException;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;
import com.sun.enterprise.admin.common.exception.PortInUseException;
import com.sun.enterprise.admin.common.exception.ServerInstanceException;
import com.sun.enterprise.admin.common.InitConfFileBean;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.common.ObjectNameHelper;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.common.RequestID;
import com.sun.enterprise.admin.common.ServerInstanceStatus;
import com.sun.enterprise.admin.common.Status;
import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.admin.event.AdminEventCache;
import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.admin.event.AdminEventMulticaster;
import com.sun.enterprise.admin.event.AdminEventResult;
import com.sun.enterprise.admin.event.ApplicationDeployEvent;
import com.sun.enterprise.admin.event.BaseDeployEvent;
import com.sun.enterprise.admin.event.ConfigChangeEvent;
import com.sun.enterprise.admin.event.EventBuilder;
import com.sun.enterprise.admin.event.EventContext;
import com.sun.enterprise.admin.event.EventStack;
import com.sun.enterprise.admin.event.ModuleDeployEvent;
import com.sun.enterprise.admin.event.ResourceDeployEvent;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.server.core.channel.AdminChannel;
import com.sun.enterprise.admin.server.core.channel.RMIClient;
import com.sun.enterprise.admin.server.core.ManualChangeManager;
import com.sun.enterprise.admin.server.core.mbean.config.Domain2ServerTransformer;
import com.sun.enterprise.admin.util.ArgChecker;
import com.sun.enterprise.admin.util.Assert;
import com.sun.enterprise.admin.util.ExceptionUtil;
import com.sun.enterprise.admin.util.HostAndPort;
import com.sun.enterprise.admin.util.StringValidator;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.util.JmsRaMapping;
import com.sun.enterprise.instance.InstanceDefinition;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.instance.ServerManager;
import com.sun.enterprise.security.auth.realm.BadRealmException;
import com.sun.enterprise.security.auth.realm.file.FileRealm;
import com.sun.enterprise.security.auth.realm.NoSuchRealmException;
import com.sun.enterprise.security.auth.realm.NoSuchUserException;
import com.sun.enterprise.security.util.IASSecurityException;
import com.sun.enterprise.server.Constants;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.net.NetUtils;

import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.AdminContext;

/**TBD
*/

public class DeploymentNotificationHelper 
{
     public static final Logger sLogger =
            Logger.getLogger(AdminConstants.kLoggerName);

     private static final int CONFIG_CHANGED             = 7;

    // i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( DeploymentNotificationHelper.class );

    /**
        Convenience method to multicast events. This method is being used to
        events other than ModuleDeployEvent.
    */

        /*
    private static boolean multicastEvent(int eventType, String entityName) 
        throws DeploymentException
    {
        return multicastEvent(eventType, entityName, null);
    }

         */
        
        /*
    public static boolean multicastEvent(int eventType, String entityName,
            String moduleType) throws DeploymentException {
        return multicastEvent(eventType, entityName, moduleType, false);
    }

         */
        
    /**
        Multicasts the event to the respective listeners. The listeners are
        multicast from here even if the instance is not running. The
        AdminEventMulticaster should take care of it.
        @return true if the instance is up and event was sent and successfully
            handled or if the instance is down, false otherwise.
    */
        /*
    public boolean multicastEvent(int eventType, String entityName,
          String moduleType , boolean cascade) throws DeploymentException {
	    return multicastEvent(eventType, entityName, moduleType, cascade, false, null);
    }
         */

                                                                                                                                               
    /**
     *  Multicasts the event to the respective listeners. The listeners are
     *  multicast from here even if the instance is not running. The
     *  AdminEventMulticaster should take care of it.
     *  @return true if the instance is up and event was sent and successfully
     *      handled or if the instance is down, false otherwise.
     */
    public static boolean multicastEvent(int eventType, String entityName,
           String moduleType , boolean cascade, boolean forceDeploy, int loadUnloadAction, String targetName) throws DeploymentException
    {
        //String name = getInstanceName();
        AdminEvent event = null;
        EventBuilder builder = new EventBuilder();

        //XXX Can we put the following 4 lines be done in the EventBuilder?
        EventStack stack = EventContext.getEventStackFromThreadLocal();
        ConfigContext ctx = stack.getConfigContext();
        stack.setTarget(targetName);
        stack.setConfigChangeList(ctx.getConfigChangeList());

        try{
            if (eventType == BaseDeployEvent.APPLICATION_DEPLOYED)
            {
                event = builder.createApplicationDeployEvent(
                    BaseDeployEvent.DEPLOY, entityName, false, forceDeploy, 
                    loadUnloadAction);
            }
            else if (eventType == BaseDeployEvent.APPLICATION_UNDEPLOYED)
            {
                event = builder.createApplicationDeployEvent(
                    BaseDeployEvent.UNDEPLOY, entityName, cascade, forceDeploy,                     loadUnloadAction);
            }
            else if (eventType == BaseDeployEvent.APPLICATION_REDEPLOYED)
            {
                event = builder.createApplicationDeployEvent(
                    BaseDeployEvent.REDEPLOY, entityName, false, forceDeploy,
                    loadUnloadAction);
            }
            else if (eventType == BaseDeployEvent.MODULE_DEPLOYED)
            {
                event = builder.createModuleDeployEvent(
                    BaseDeployEvent.DEPLOY, entityName, moduleType, cascade, forceDeploy);
            }
            else if (eventType == BaseDeployEvent.MODULE_UNDEPLOYED)
            {
                event = builder.createModuleDeployEvent(
                    BaseDeployEvent.UNDEPLOY, entityName, moduleType, cascade, forceDeploy);
            }
            else if (eventType == BaseDeployEvent.MODULE_REDEPLOYED)
            {
                event = builder.createModuleDeployEvent(
                    BaseDeployEvent.REDEPLOY, entityName, moduleType);
            }
            else if (eventType == BaseDeployEvent.APPLICATION_ENABLE)
            {
                event = builder.createApplicationDeployEvent(
                    BaseDeployEvent.ENABLE, entityName, false, forceDeploy, 
                    loadUnloadAction);
            }
            else if (eventType == BaseDeployEvent.APPLICATION_DISABLE)
            {
                event = builder.createApplicationDeployEvent(
                    BaseDeployEvent.DISABLE, entityName, false, forceDeploy,
                    loadUnloadAction);
            }
            else if(eventType == BaseDeployEvent.MODULE_ENABLE)
            {
                event = builder.createModuleDeployEvent(
                    BaseDeployEvent.ENABLE, entityName, moduleType, false, forceDeploy);
            }
            else if(eventType == BaseDeployEvent.MODULE_DISABLE)
            {
                event = builder.createModuleDeployEvent(
                    BaseDeployEvent.DISABLE, entityName, moduleType, false, forceDeploy);
            } 
            else if(eventType == BaseDeployEvent.APPLICATION_REFERENCED)
            {
                      event = builder.createApplicationDeployEvent(
                    BaseDeployEvent.ADD_REFERENCE, entityName, false, forceDeploy, loadUnloadAction);
            }
            else if(eventType == BaseDeployEvent.APPLICATION_UNREFERENCED)
            {
                    event = builder.createApplicationDeployEvent(
                    BaseDeployEvent.REMOVE_REFERENCE, entityName, false, forceDeploy, loadUnloadAction);
            }
            else if (eventType == CONFIG_CHANGED)
            {
                event = builder.createConfigChangeEvent(targetName, null);
            }
            else
            {
                String msg = 
                    localStrings.getString( "admin.server.core.mbean.config.no_such_event", 
                                            new Integer(eventType) );
                throw new RuntimeException( msg );
            }
        } catch (ConfigException ex) {
            DeploymentException de = new DeploymentException(ex.getMessage());
            de.initCause(ex);
            throw de;
        }

        //set target destination for the event
        if (targetName != null) {
            event.setTargetDestination(targetName);
        }

        if (event instanceof ApplicationDeployEvent
                || event instanceof ModuleDeployEvent) {
            AdminEventCache.populateConfigChange(getConfigContext(), event);
        }
        
        if (sLogger.isLoggable(Level.FINEST)) {
            sLogger.log(Level.FINEST, "mbean.event_sent", event.getEventInfo());
        } else {
            sLogger.log(Level.FINE, "mbean.send_event", event.toString());
        }

        AdminEventResult multicastResult =
                AdminEventMulticaster.multicastEvent(event);
        sLogger.log(Level.FINE, "mbean.event_res",
                multicastResult.getResultCode());
        sLogger.log(Level.FINE, "mbean.event_reply",
                multicastResult.getAllMessagesAsString());
        boolean eventSuccess = true;
            //ALREADY SET in Admin Event Multicaster
            //AdminEventCache cache =
            //        AdminEventCache.getInstance(mInstanceName);
            //cache.setRestartNeeded(true);

            // if there is an exception thrown when loading modules
            // rethrow the exception
            AdminEventListenerException ale = null;
            ale = multicastResult.getFirstAdminEventListenerException();
            if (ale != null) {
                sLogger.log(Level.WARNING, "mbean.event_failed", 
                    ale.getMessage());
                DeploymentException de = 
                    new DeploymentException(ale.getMessage());
                de.initCause(ale);
                throw de;
            }
        return eventSuccess;
    }

    private static ConfigContext getConfigContext() {
         AdminContext adminContext = 
                        AdminService.getAdminService().getAdminContext();
         return adminContext.getAdminConfigContext();
    }
}
