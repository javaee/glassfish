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
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */
package com.sun.enterprise.admin.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.server.core.AdminService;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Helper class to manage monitoring data provider object. This class has helper
 * methods to register and unregister monitoring MBeans and implementors
 * of monitorable interface (IMonitorable).
 */
public class MonitoringHelper {

	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( MonitoringHelper.class );

    /**
     * A reference to logger object
     */
    static Logger logger = Logger.getLogger(AdminConstants.kLoggerName);

    /**
     * Register a monitoring MBean for a ejb that is part of specified
     * module in the specified application.
     *
     * @param appName name of the application
     * @param moduleName name of the module
     * @param ejbName name of the ejb
     * @param type type of ejb - entity, stateless or stateful session, mdb
     * @param mbean the monitorable mbean
     *
     * @returns name under which the mbean is registered in MBeanServer
     *
     * @throws InstanceAlreadyExistsException A MBean for specified parameter(s)
     *         is already under the control of the MBean server.
     * @throws MBeanRegistrationException preRegister (MBeanRegistration
     *         interface) method of the MBean has thrown an exception. The MBean
     *         will not be registered.
     */
    public static ObjectName registerEJBMonitoringMBean(String appName,
            String moduleName, String ejbName, MonitoredObjectType type,
            BaseMonitorMBean mbean)
            throws InstanceAlreadyExistsException, MBeanRegistrationException {
        BaseMonitorMBean app = getLevelOneMBeanForSure(
                MonitoredObjectType.APPLICATION, appName);
        BaseMonitorMBean module = getMBeanForSure(app,
                MonitoredObjectType.EJBMODULE, moduleName);
        module.addChild(ejbName, type, mbean);
        return mbean.getObjectName();
    }

    /**
     * Get name of the monitoring MBean (in MBeanServer) for a ejb that is part
     * of specified module in the specified application.
     *
     * @param appName name of the application
     * @param moduleName name of the module
     * @param ejbName name of the ejb
     *
     * @throws InstanceNotFoundException There is no MBean registered for
     *         specified bean, module and application in the MBean server.
     */
    public static ObjectName getEJBMonitoringMBeanName(String appName,
            String moduleName, String ejbName)
            throws InstanceNotFoundException {
        return getEJBMonitoringMBean(appName, moduleName, ejbName).getObjectName();
    }

    /**
     * Unregister a monitoring MBean for a ejb that is part of specified
     * module in the specified application.
     *
     * @param appName name of the application
     * @param moduleName name of the module
     * @param ejbName name of the ejb
     *
     * @throws InstanceNotFoundException There is no MBean registered for
     *         specified bean, module and application in the MBean server.
     * @throws MBeanRegistrationException - The preDeregister (MBeanRegistration
     *         interface) method of the MBean has thrown an exception.
     */
    public static void unregisterEJBMonitoringMBean(String appName,
            String moduleName, String ejbName)
            throws InstanceNotFoundException, MBeanRegistrationException {
        BaseMonitorMBean module = getEJBModuleMBean(appName, moduleName);
        BaseMonitorMBean ejb = module.getFirstChildByName(ejbName);
        module.removeChild(ejb);
        checkAndPurgeUnusedAppAndModule(appName, module);
    }

    /**
     * Register a monitoring MBean for a ejb that is part of specified
     * stand alone ejb module.
     *
     * @param standaloneModuleName name of the stand alone ejb module.
     * @param ejbName name of the ejb
     * @param type type of ejb - entity, stateless or stateful session, mdb
     * @param mbean the monitorable mbean
     *
     * @returns name under which the mbean is registered in MBeanServer
     *
     * @throws InstanceAlreadyExistsException A MBean for specified parameter(s)
     *         is already under the control of the MBean server.
     * @throws MBeanRegistrationException preRegister (MBeanRegistration
     *         interface) method of the MBean has thrown an exception. The MBean
     *         will not be registered.
     */
    public static ObjectName registerEJBMonitoringMBean(String standaloneModuleName,
            String ejbName, MonitoredObjectType type, BaseMonitorMBean mbean)
            throws InstanceAlreadyExistsException, MBeanRegistrationException {
        BaseMonitorMBean standaloneModule = getLevelOneMBeanForSure(
                MonitoredObjectType.STANDALONE_EJBMODULE, standaloneModuleName);
        standaloneModule.addChild(ejbName, type, mbean);
        return mbean.getObjectName();
    }

    /**
     * Get name of the monitoring MBean (in MBeanServer) for a ejb that is part
     * of specified stand alone ejb module.
     *
     * @param standaloneModuleName name of the stand alone ejb module.
     * @param ejbName name of the ejb
     *
     * @throws InstanceNotFoundException There is no MBean registered for
     *         specified bean, and stand alone ejb module in the MBean server.
     */
    public static ObjectName getEJBMonitoringMBeanName(String standaloneModuleName,
            String ejbName)
            throws InstanceNotFoundException {
        return getEJBMonitoringMBean(standaloneModuleName, ejbName).getObjectName();
    }

    /**
     * Unregister a monitoring MBean for a ejb that is part of specified
     * stand alone ejb module.
     *
     * @param standaloneModuleName name of the stand alone ejb module.
     * @param ejbName name of the ejb
     *
     * @throws InstanceNotFoundException There is no MBean registered for
     *         specified bean, and stand alone ejb module in the MBean server.
     * @throws MBeanRegistrationException - The preDeregister (MBeanRegistration
     *         interface) method of the MBean has thrown an exception.
     */
    public static void unregisterEJBMonitoringMBean(String standaloneModuleName,
            String ejbName)
            throws InstanceNotFoundException, MBeanRegistrationException {
        BaseMonitorMBean module = getEJBModuleMBean(standaloneModuleName);
        BaseMonitorMBean ejb = module.getFirstChildByName(ejbName);
        module.removeChild(ejb);
        checkAndPurgeUnusedLevelOneMBean(module);
    }

    /**
     * @throws InstanceNotFoundException There is no MBean registered for
     *         specified bean, and stand alone ejb module in the MBean server.
     * @throws InstanceAlreadyExistsException A MBean for specified parameter(s)
     *         is already under the control of the MBean server.
     * @throws MBeanRegistrationException preRegister (MBeanRegistration
     *         interface) method of the MBean has thrown an exception. The MBean
     *         will not be registered.
     */
    public static void registerEJBMethodMonitoringMBean(ObjectName ejbMBeanName,
            String methodName, BaseMonitorMBean mbean)
            throws InstanceNotFoundException, InstanceAlreadyExistsException,
            MBeanRegistrationException {
        BaseMonitorMBean ejb = getMonitorMBean(ejbMBeanName);
        ejb.addChild(methodName, MonitoredObjectType.BEAN_METHOD, mbean);
    }

    /**
     * Register a monitoring MBean under specified name. name is dotted
     * representation that denotes the position of MBean within the current
     * context (this instance). [TBD: Dotted notation]
     *
     * @param name name of the MBean in dotted notation
     * @param mbean monitoring mbean object
     *
     * @return name under which the mbean is registered in MBeanServer
     *
     * @throws InstanceAlreadyExistsException A MBean for specified parameter(s)
     *         is already under the control of the MBean server.
     * @throws MBeanRegistrationException preRegister (MBeanRegistration
     *         interface) method of the MBean has thrown an exception. The MBean
     *         will not be registered.
     * @deprecated
     */
    public static ObjectName registerMonitoringMBean(String name,
            BaseMonitorMBean mbean)
            throws InstanceNotFoundException, MBeanRegistrationException {
        return null;
    }

    /**
     * Unregister a monitoring MBean under specified name. name is dotted
     * representation that denotes the position of MBean within the current
     * context (this instance). [TBD: Dotted notation]
     *
     * @param name name of the MBean in dotted notation
     *
     * @throws InstanceNotFoundException The MBean specified is not registered
     *         in the MBean server.
     * @throws MBeanRegistrationException - The preDeregister (MBeanRegistration
     *         interface) method of the MBean has thrown an exception.
     * @deprecated
     */
    public static void unregisterMonitoringMBean(String name)
            throws InstanceNotFoundException, MBeanRegistrationException {
    }

    /**
     * Name of system orb. This string can not be used for naming any user orbs.
     */
    public static final String SYSTEM_ORB_NAME = "system";

    /**
     * Register a mbean to monitor system ORB. System ORB is created at server
     * startup.
     * @param mbean the mbean to be registered.
     * @throws IllegalStateException if the method is called when admin service
     *     has not been initialized (for example, a call by application client
     *     container, which does not ever initialize admin service)
     * @throws InstanceAlreadyExistsException A MBean for system orb
     *         is already under the control of the MBean server.
     * @throws MBeanRegistrationException preRegister (MBeanRegistration
     *         interface) method of the MBean has thrown an exception. The MBean
     *         will not be registered.
     */
    public static void registerSystemORBMonitoringMBean(BaseMonitorMBean mbean)
            throws InstanceAlreadyExistsException, MBeanRegistrationException {
        registerORBMonitoringMBean(SYSTEM_ORB_NAME, mbean);
    }

    /**
     * Register a mbean to monitor user created ORB.
     * @param hint a hint for the name of mbean. The hint will be used as name
     *     if it is valid and has not been used earlier. A hint is valid if it
     *     is not null and does not contain any of following characters -
     *     comma, space, equals sign or colon. If hint is invalid a name derived
     *     from the constant DEFAULT_USER_ORB_HINT will be used for the mbean.
     * @throws IllegalArgumentException if the specified hint is reserved for
     *     use by system. The reserved name is value of constant SYSTEM_ORB_NAME
     * @throws IllegalStateException if the method is called when admin service
     *     has not been initialized (for example, a call by application client
     *     container, which does not ever initialize admin service)
     * @throws InstanceAlreadyExistsException A MBean for system orb
     *         is already under the control of the MBean server.
     * @throws MBeanRegistrationException preRegister (MBeanRegistration
     *         interface) method of the MBean has thrown an exception. The MBean
     *         will not be registered.
     */
    public static void registerUserORBMonitoringMBean(String hint,
            BaseMonitorMBean mbean)
            throws InstanceAlreadyExistsException, MBeanRegistrationException {
        if (SYSTEM_ORB_NAME.equalsIgnoreCase(hint)) {
            String msg = localStrings.getString(
                    "admin.monitor.system_orb_name_used", hint);
            throw new IllegalArgumentException(msg);
        }
        registerORBMonitoringMBean(getUserOrbMBeanName(hint), mbean);
    }

    /**
     * Register a ORB monitoring MBean. ORB monitoring MBeans are registered
     * under the node iiop-service (which is directly under root node) in the
     * tree of monitoring mbeans. 
     * @param name name of the mbean to be registered
     * @param mbean the mbean to be registered
     * @throws IllegalStateException if the method is called when admin service
     *     has not been initialized (for example, a call by application client
     *     container, which does not ever initialize admin service)
     * @throws InstanceAlreadyExistsException A MBean for user orb
     *         is already under the control of the MBean server.
     * @throws MBeanRegistrationException preRegister (MBeanRegistration
     *         interface) method of the MBean has thrown an exception. The MBean
     *         will not be registered.
     */
    private static void registerORBMonitoringMBean(String name,
            BaseMonitorMBean mbean)
            throws InstanceAlreadyExistsException, MBeanRegistrationException {
        if (AdminService.getAdminService() == null) {
            String msg = localStrings.getString(
                    "admin.monitor.admin_service_not_inited");
            throw new IllegalStateException(msg);
        }
        BaseMonitorMBean iiop = getLevelOneMBeanForSure(
                MonitoredObjectType.IIOP_SERVICE,
                MonitoredObjectType.IIOP_SERVICE.getTypeName());
        iiop.addChild(name, MonitoredObjectType.ORB, mbean);
        logger.log(Level.FINEST, ORB_MBEAN_REGISTERED, name);
    }

    /**
     * A map to keep track of user orb names already in use.
     */
    private static final HashMap userOrbNames = new HashMap();

    /**
     * An int that is used to make user orb names unique.
     */
    private static int orbNameUniquifier = 0;

    /**
     * Default prefix for user orbs.
     */
    public static final String DEFAULT_USER_ORB_HINT = "user";

    /**
     * Get a unique name for user orb. It tries to uniquify the name by adding
     * integers to the end of the hint.
     * @param hint hint for orb name, if the name is not in use, it is used as
     *     is, otherwise integers are appended to it till it is unique. If hint
     *     is null, empty or any form (lowercase or uppercase) of constant
     *     DEFAULT_USER_ORB_HINT, the method uses constant DEFAULT_USER_ORB_HINT
     *     as hint.
     * @return a name that is derived from the specified hint and is not in use
     */
    private static String getUserOrbMBeanName(String hint) {
        if (hint == null || hint.trim().equals("")
                 || hint.equalsIgnoreCase(DEFAULT_USER_ORB_HINT)
                 || (hint.indexOf(COMMA) != -1)
                 || (hint.indexOf(SPACE) != -1)
                 || (hint.indexOf(COLON) != -1)
                 || (hint.indexOf(EQUALS) != -1)) {
            logger.log(Level.FINEST, INVALID_USER_ORB_NAME_HINT, hint);
            hint = DEFAULT_USER_ORB_HINT + (++orbNameUniquifier);
        }
        synchronized (userOrbNames) {
            while (userOrbNames.containsKey(hint)) {
                logger.log(Level.FINEST, USER_ORB_MBEAN_NAME_USED, hint);
                hint = hint + (++orbNameUniquifier);
            }
            userOrbNames.put(hint, hint);
        }
        return hint;
    }

    /**
     * Register a monitorable object under specified name. A MBean is created
     * from the monitorable and then registered to MBean server. name is dotted
     * representation that denotes the position of MBean within the current
     * context (this instance). [TBD: Dotted notation]
     *
     * @param name name of the MBean in dotted notation
     * @param monitorable monitorable object
     *
     * @return name under which the mbean is registered in MBeanServer
     *
     * @throws InstanceAlreadyExistsException A MBean for specified parameter(s)
     *         is already under the control of the MBean server.
     * @throws MBeanRegistrationException preRegister (MBeanRegistration
     *         interface) method of the MBean has thrown an exception. The MBean
     *         will not be registered.
     * @deprecated 
     */
    public static ObjectName registerMonitorable(String name,
            IMonitorable monitorable)
            throws InstanceNotFoundException, MBeanRegistrationException {
        return null;
    }

    /**
     * Unregister a monitorable under specified name. A MBean is created
     * from the monitorable and then registered to MBean server. name is dotted
     * representation that denotes the position of MBean within the current
     * context (this instance). [TBD: Dotted notation]
     *
     * @param name name of the MBean in dotted notation
     *
     * @throws InstanceNotFoundException The MBean specified is not registered
     *         in the MBean server.
     * @throws MBeanRegistrationException - The preDeregister (MBeanRegistration
     *         interface) method of the MBean has thrown an exception.
     * @deprecated
     */
    public static void unregisterMonitorable(String name)
            throws InstanceNotFoundException, MBeanRegistrationException {
    }

    /**
     * Get monitoring mBean registered in MBean Server with specified name.
     * @param name registered name of the MBean in MBeanServer
     * @throws InstanceNotFoundException if the specified name is not registered
     *     in the MBean server.
     */
    public static BaseMonitorMBean getMonitorMBean(ObjectName name)
            throws InstanceNotFoundException {
        BaseMonitorMBean mbean = getMonitorMBeanOrNull(name);
        if (mbean == null) {
			String msg = localStrings.getString( "admin.monitor.mbean_with_name_not_found", name );
            throw new InstanceNotFoundException( msg );
        }
        return mbean;
    }

    /**
     * Get monitoring mBean registered in MBean Server with specified name. If
     * the mbean with specified name is not found this method returns null.
     * @param name registered name of the MBean in MBeanServer
     * @return mbean of specified name or null if it is not found
     */
    private static BaseMonitorMBean getMonitorMBeanOrNull(ObjectName name) {
        BaseMonitorMBean mbean =
                (BaseMonitorMBean)BaseMonitorMBean.objectNameMap.get(name);
        return mbean;
    }

    /**
     * Get monitoring MBean (in MBeanServer) for a ejb that is part
     * of specified module in the specified application.
     *
     * @param appName name of the application
     * @param moduleName name of the module
     * @param ejbName name of the ejb
     *
     * @throws InstanceNotFoundException There is no MBean registered for
     *         specified bean, module and application in the MBean server.
     */
    private static BaseMonitorMBean getEJBMonitoringMBean(String appName,
            String moduleName, String ejbName)
            throws InstanceNotFoundException {
        BaseMonitorMBean mbean = null;
        BaseMonitorMBean app = getLevelOneMBean(MonitoredObjectType.APPLICATION,
                appName);
        if (app != null) {
            BaseMonitorMBean module = app.getChildOrNull(
                    MonitoredObjectType.EJBMODULE, moduleName);
            if (module != null) {
                ArrayList list = module.getChildList(ejbName);
                if (!list.isEmpty()) {
                    mbean = (BaseMonitorMBean)list.get(0);
                }
            }
        }
        if (mbean == null) {
			String msg = localStrings.getString( "admin.monitor.no_mbean_for_appname_modulename_ejbname", appName, moduleName, ejbName );
            throw new InstanceNotFoundException( msg );
        }
        return mbean;
    }

    /**
     * Get MBean for specified application and ejb module. If the MBean for
     * application or module does not exist, it returns null.
     * @param appName name of the application
     * @param moduleName name of the ejb module
     * @return MBean for the ejb module if it exists, null otherwise
     */
    private static BaseMonitorMBean getEJBModuleMBean(String appName,
            String moduleName) {
        BaseMonitorMBean module = null;
        BaseMonitorMBean app = getLevelOneMBean(MonitoredObjectType.APPLICATION,
                appName);
        if (app != null) {
            module = app.getChildOrNull(
                    MonitoredObjectType.EJBMODULE, moduleName);
        }
        return module;
    }

    /**
     * Get monitoring MBean (in MBeanServer) for a ejb that is part
     * of specified stand alone ejb module.
     *
     * @param standaloneModuleName name of the stand alone ejb module
     * @param ejbName name of the ejb
     *
     * @throws InstanceNotFoundException There is no MBean registered for
     *         specified bean, and stand alone ejb module in the MBean server.
     */
    private static BaseMonitorMBean getEJBMonitoringMBean(
            String standaloneModuleName, String ejbName)
            throws InstanceNotFoundException {
        BaseMonitorMBean mbean = null;
        BaseMonitorMBean module = getLevelOneMBean(
                MonitoredObjectType.STANDALONE_EJBMODULE, standaloneModuleName);
        if (module != null) {
            ArrayList list = module.getChildList(ejbName);
            if (!list.isEmpty()) {
                mbean = (BaseMonitorMBean)list.get(0);
            }
        }
        if (mbean == null) {
			String msg = localStrings.getString( "admin.monitor.no_mbean_for_standalonemodulename_ejbname", standaloneModuleName, ejbName );
            throw new InstanceNotFoundException( msg );
        }
        return mbean;
    }

    /**
     * Get MBean for specified stand alone ejb module. If the MBean for
     * stand alone ejb module does not exist, it returns null.
     * @param standaloneModuleName name of the stand alone ejb module
     * @return MBean for the stand alone ejb module if it exists, null otherwise
     */
    private static BaseMonitorMBean getEJBModuleMBean(
            String standaloneModuleName) {
        return getLevelOneMBean(
                MonitoredObjectType.STANDALONE_EJBMODULE, standaloneModuleName);
    }

    /**
     * Get a monitoring mbean from top level (immediate child of root monitor
     * mbean). If the mbean does not exist, it returns null.
     * @param type type of the mbean
     * @param name name of the mbean
     * @return mbean representing specified type and name, if it exists, null
     *     otherwise.
     */
    private static BaseMonitorMBean getLevelOneMBean(
            MonitoredObjectType type, String name) {
        GenericMonitorMBean root = GenericMonitorMBean.getRoot();
        return root.getChildOrNull(type, name);
    }

    /**
     * Get a monitoring mbean from top level (immediate child of root monitor
     * mbean). If the mbean does not exist, a generic mbean is created and
     * added.
     * @param type type of the mbean
     * @param name name of the mbean
     * @return mbean representing specified type and name
     */
    private static BaseMonitorMBean getLevelOneMBeanForSure(
            MonitoredObjectType type, String name) {
        GenericMonitorMBean root = GenericMonitorMBean.getRoot();
        return getMBeanForSure(root, type, name);
    }

    /**
     * Get a MBean of specified type and name contained within specified
     * parent mbean. If a MBean representing the child does not exist, an
     * instance of generic mbean is created and added to the parent.
     * @param parent the parent MBean
     * @param type type of the child MBean
     * @param name name of the child MBean
     * @return an MBean representing specified child.
     */
    private static BaseMonitorMBean getMBeanForSure(BaseMonitorMBean parent,
            MonitoredObjectType type, String name) {
        BaseMonitorMBean mbean = parent.getChildOrNull(type, name);
        if (mbean == null) {
            mbean = new GenericMonitorMBean();
            try {
                parent.addChild(name, type, mbean);
            } catch (InstanceAlreadyExistsException iae) {
                // Narrow window between getChildOrNull() and addChild() call
                // allows this possibility. So get the child again
                mbean = parent.getChildOrNull(type, name);
                if (mbean == null) {
                    // If this is null, don't deal with it (removed?). If this
                    // exception shows up, synchronize calls to getChildOrNull()
                    // and addChild() on parent
					String msg = localStrings.getString( "admin.monitor.unable_getting_mbean", type, name, parent.getObjectName() );
                    throw new RuntimeException( msg );
                }
            } catch (MBeanRegistrationException mbr) {
                // This is thrown by errors during execution of method in
                // MBeanRegistration interface. GenericMonitorMBean does not
                // implement this interace, so if it happens throw a
                // RuntimeException
				String msg = localStrings.getString( "admin.monitor.rootcause_unable_to_register_mbean", type, name, mbr.getMessage() );
                throw new RuntimeException( msg );
            }
        }
        return mbean;
    }

    /**
     * Register a monitoring MBean for transaction service.
     *
     * @param mbean the monitorable mbean
     *
     * @returns name under which the mbean is registered in MBeanServer
     *
     * @throws InstanceAlreadyExistsException A MBean for specified parameter(s)
     *         is already under the control of the MBean server.
     * @throws MBeanRegistrationException preRegister (MBeanRegistration
     *         interface) method of the MBean has thrown an exception. The MBean
     *         will not be registered.
     */
    public static ObjectName registerTxnMonitoringMBean(BaseMonitorMBean mbean)
            throws InstanceAlreadyExistsException, MBeanRegistrationException {
        GenericMonitorMBean.getRoot().addChild(MonitoredObjectType.TXNMGR.getTypeName(), MonitoredObjectType.TXNMGR, mbean);
        return mbean.getObjectName();
    }
    
    public static ObjectName registerJdbcPoolMonitoringMBean(String poolName, MonitoredObjectType type, BaseMonitorMBean mbean)
            throws InstanceAlreadyExistsException, MBeanRegistrationException
    {
	    if(AdminService.getAdminService() != null){
		BaseMonitorMBean resMBean = getLevelOneMBeanForSure(MonitoredObjectType.RESOURCES,"resources");
       resMBean.addChild(poolName, type, mbean);
       return mbean.getObjectName();
        }
		return null;

    }

    public void unregisterJdbcPoolMonitoringMBean(MonitoredObjectType type,String poolName)
        throws InstanceNotFoundException, MBeanRegistrationException
    {

      BaseMonitorMBean resMBean = getLevelOneMBean(MonitoredObjectType.RESOURCES,"resources");
	   if(resMBean != null)
           resMBean.removeChild(type,poolName);
    }

    /**
     * Purge module mbean if it does not have any child mbean. If module mbean
     * is purged then also purge application mbean if there are no more modules.
     * @param appName name of the application
     * @param moduleMBean module mbean
     */
    private static void checkAndPurgeUnusedAppAndModule(String appName,
            BaseMonitorMBean moduleMBean) {
        if (moduleMBean == null) {
            return;
        }
        BaseMonitorMBean appMBean = getLevelOneMBean(
                MonitoredObjectType.APPLICATION, appName);
        boolean purged = checkAndPurgeMBean(appMBean, moduleMBean);
        if (purged) {
            checkAndPurgeUnusedLevelOneMBean(appMBean);
        }
        return;
    }

    /**
     * Purge unused mbean at the top level (below root) in the monitoring mbean
     * tree. Any mbean that does not have children is considered unused.
     * @param levelOneMBean the mbean that needs to checked and purged
     */
    private static void checkAndPurgeUnusedLevelOneMBean(
            BaseMonitorMBean levelOneMBean) {
        if (levelOneMBean == null) {
            return;
        }
        GenericMonitorMBean root = GenericMonitorMBean.getRoot();
        boolean purged = checkAndPurgeMBean(root, levelOneMBean);
        return;
    }

    /**
     * Purge an mbean if the mbean does not have any child mbean. This method
     * tries to do 
     * @param parent the parent mbean of the mbean that needs to purged.
     * @param mbean the mbean that needs to be purged
     * @returns true if the mbean was purged, false otherwise
     */
    private static boolean checkAndPurgeMBean(BaseMonitorMBean parent,
            BaseMonitorMBean mbean) {
        boolean purged = false;
        if (mbean != null && parent != null) {
            ArrayList childList = mbean.getChildList();
            if (childList == null || childList.size() == 0) {
                try {
                    parent.removeChild(mbean);
                    purged = true;
                } catch (Throwable t) {
                    // Failure in deletion of mbean is not fatal, it will have
                    // undesirable effect of not removing the module mbean even
                    // when the application is undeployed, but won't cause any
                    // harm. So just log the error.
                    logger.log(Level.FINE, PURGE_MBEAN_FAILED,
                            mbean.getNodeType() + "/" + mbean.getNodeName());
                    logger.log(Level.FINEST, PURGE_MBEAN_FAILED_TRACE, t);
                }
            }
        }
        return purged;
    }

    /*
     * Invalid characters for MBean name follow. 
     */
    private static final char COMMA = ',';
    private static final char SPACE = ' ';
    private static final char COLON = ':';
    private static final char EQUALS = '=';

    /*
     * Constants for logging keys.
     */
    private static final String PURGE_MBEAN_FAILED =
            "monitor.purge_mbean_failed";
    private static final String PURGE_MBEAN_FAILED_TRACE =
            "monitor.purge_mbean_failed_trace";
    private static final String USER_ORB_MBEAN_NAME_USED =
            "monitor.user_orb_mbean_name_used";
    private static final String ORB_MBEAN_REGISTERED =
            "monitor.orb_mbean_registered";
    private static final String INVALID_USER_ORB_NAME_HINT =
            "monitor.invalid_user_orb_name_hint";
}
