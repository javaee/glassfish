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

import java.util.HashMap;
import com.sun.enterprise.config.serverbeans.ServerTags;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 * MonitoredObjectType represents the type of a monitored object. Many of
 * monitored objects have same properties, even while they monitor different
 * user objects. For example - application and standalone ejb module are both
 * nothing more than containers of other objects -- application contains web
 * module and ejb modules, whereas standalone ejb module contains beans of
 * various types. This object facilitates use of same GenericMonitorMBean
 * as MBean for both of them, but still distinguishes them.
 * @see com.sun.enterprise.admin.monitor.GenericMonitorMBean
 */
public class MonitoredObjectType {

    /**
     * A map to store all objects of type MonitoredObjectType using their string
     * representation as key. 
     */
    private static final HashMap objectMap = new HashMap();

    /**
     * A root monitored object type
     */
    public static final MonitoredObjectType ROOT =
            new MonitoredObjectType("root", true);

    /**
     * A monitored object type of Application
     */
    public static final MonitoredObjectType APPLICATION =
            new MonitoredObjectType("application");

    /**
     * A monitored object type of EJB Module. This denotes ejb modules within
     * an application. For stand alone ejb modules, please use the type
     * MonitoredObjectType.STANDALONE_EJBMODULE
     */
    public static final MonitoredObjectType EJBMODULE =
            new MonitoredObjectType("ejb-module");

    /**
     * A monitored object type of standalone EJB Module. This denotes ejb
     * modules not deployed as part of any application. For ejb modules deployed
     * as part of applications, please use the type MonitoredObjectType.EJBMODULE
     */
    public static final MonitoredObjectType STANDALONE_EJBMODULE =
            new MonitoredObjectType("standalone-ejb-module");

    /**
     * A monitored object type of Web Module. This denotes web module within
     * an application. For stand alone web modules, please use the type
     * MonitoredObjectType.STANDALONE_WEBMODULE
     */
    public static final MonitoredObjectType WEBMODULE =
            new MonitoredObjectType("web-module");

    /**
     * A monitored object type of standalone Web Module. This denotes web
     * modules not deployed as part of any application. For web modules deployed
     * as part of applications, please use the type MonitoredObjectType.WEBMODULE
     */
    public static final MonitoredObjectType STANDALONE_WEBMODULE =
            new MonitoredObjectType("standalone-web-module");

    /**
     * A monitored object type of stateless session bean
     */
    public static final MonitoredObjectType STATELESS_BEAN =
            new MonitoredObjectType("stateless-session-bean");

    /**
     * A monitored object type of stateful session bean
     */
    public static final MonitoredObjectType STATEFUL_BEAN =
            new MonitoredObjectType("stateful-session-bean");

    /**
     * A monitored object type of entity bean
     */
    public static final MonitoredObjectType ENTITY_BEAN =
            new MonitoredObjectType("entity-bean");

    /**
     * A monitored object type of message driven bean
     */
    public static final MonitoredObjectType MESSAGE_DRIVEN_BEAN =
            new MonitoredObjectType("message-driven-bean");

    /**
     * A monitored object type of bean pool. For every bean, there can be atmost
     * one object of type bean pool monitor.
     */
    public static final MonitoredObjectType BEAN_POOL =
            new MonitoredObjectType("bean-pool", true);

    /**
     * A monitored object type of bean cache. For every bean, there can be
     * atmost one object of type bean cache monitor.
     */
    public static final MonitoredObjectType BEAN_CACHE =
            new MonitoredObjectType("bean-cache", true);

    /**
     * A monitored object type of bean method
     */
    public static final MonitoredObjectType BEAN_METHOD =
            new MonitoredObjectType("bean-method");


    /**
     * A monitored object type of http-server (core)
     */
    public static final MonitoredObjectType HTTP_SERVER =
            new MonitoredObjectType("http-server", true);


    /**
     * A monitored object type of virtual-server
     */
    public static final MonitoredObjectType VIRTUAL_SERVER =
            new MonitoredObjectType("virtual-server", true);


    /**
     * A monitored object type of process
     */
    public static final MonitoredObjectType PROCESS =
            new MonitoredObjectType("process", true);

    /**
     * A monitored object type of jts monitor. There is only one instance of
     * jts monitor object.
     */
    public static final MonitoredObjectType TXNMGR =
            new MonitoredObjectType("transaction-service", true);

    /**
     * A monitored object type of iiop service
     */
    public static final MonitoredObjectType IIOP_SERVICE =
            new MonitoredObjectType("iiop-service", true);

    /**
     * A monitored object type of orb. 
     */
    public static final MonitoredObjectType ORB = new MonitoredObjectType("orb");

    /**
     * A monitored object type of orb connection
     */
    public static final MonitoredObjectType ORB_CONNECTION =
            new MonitoredObjectType("orb-connection", true);

    /**
     * A monitored object type of orb thread pool
     */
    public static final MonitoredObjectType ORB_THREAD_POOL =
            new MonitoredObjectType("orb-thread-pool", true);
			
    /**
     * A monitored object type of resources
     */
    public static final MonitoredObjectType RESOURCES =
            new MonitoredObjectType("resources", true);
			
    /**
     * A monitored object type of jdbc-connection-pool
     */
    public static final MonitoredObjectType JDBC_CONN_POOL =
            new MonitoredObjectType("jdbc-connection-pool", false);

    /**
     * value of this object as a string
     */
    private String typeName;

    /**
     * Denotes whether this type allows more than one instance at any level.
     */
    private boolean isSingleton;

    /**
     * Number of components that need this type to be enabled. If the number is
     * more than zero then monitoring is started on MBeans of this type.
     */
    private int enableCount = 0;

    /**
     * Creates a new instance of MonitoredObjectType using specified string type
     * @param type string representing the name of monitored object type 
     */
    private MonitoredObjectType(String type) {
        this(type, false);
    }

    /**
     * Creates a new instance of MonitoredObjectType using specified string type
     * and specified flag for singleton
     * @param type string representing the name of monitored object type 
     * @param isSingleton denotes whether this type of monitored object has
     *      only one instance (in its context) 
     */
    private MonitoredObjectType(String type, boolean isSingleton) {
        this.typeName = type;
        this.isSingleton = isSingleton;
        objectMap.put(this.typeName, this);
    }

    /**
     * Get type of this "MonitoredObjectType" as string
     * @return Monitored object type as string
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Is instance of this type of MonitorMBean singleton. For example, there
     * can only be one pool for every stateless session bean, so a
     * MonitoredObjectType of type MonitoredObjectType.BEAN_POOL is a singleton.
     * @return true if this type of object can have atmost one instance within
     *      its context, false otherwise.
     */
    public boolean isSingleton() {
        return isSingleton;
    }

    /**
     * A string representation. The return value of this method is
     * same as that of method getTypeName.
     * @return A string representation of this MonitoredObjectType
     */
    public String toString() {
        return typeName;
    }

    /**
     * Is monitoring enabled for this type. 
     */
    public boolean isMonitoringEnabled() {
        return (enableCount > 0);
    }

    /**
     * Get a MonitoredObjectType instance for the specified string type.
     * @param typeName string representing MonitoredObjectType
     * @throws IllegalArgumentException if the specified type name is not
     *     known.
     */
    public static MonitoredObjectType getMonitoredObjectType(String typeName) {
        MonitoredObjectType type = getMonitoredObjectTypeOrNull(typeName);
        if (type == null) {
			String msg = localStrings.getString( "admin.monitor.unknown_type_name", typeName );
            throw new IllegalArgumentException( msg );
        }
        return type;
    }

    /**
     * Get a MonitoredObjectType instance for the specified string type. If the
     * specified type is not known, the method returns null.
     * @param typeName string representing MonitoredObjectType
     */
    static MonitoredObjectType getMonitoredObjectTypeOrNull(String typeName) {
        MonitoredObjectType type = null;
        if (objectMap != null && typeName != null) {
            type = (MonitoredObjectType)objectMap.get(typeName);
        }
        return type;
    }

    /**
     * List of monitored object types for ejb container
     */
    public static final MonitoredObjectType[] EJB_TYPES =
            new MonitoredObjectType[] {
                    MonitoredObjectType.ROOT,
                    MonitoredObjectType.APPLICATION,
                    MonitoredObjectType.EJBMODULE,
                    MonitoredObjectType.STANDALONE_EJBMODULE,
                    MonitoredObjectType.STATELESS_BEAN,
                    MonitoredObjectType.STATEFUL_BEAN,
                    MonitoredObjectType.ENTITY_BEAN,
                    MonitoredObjectType.BEAN_POOL,
                    MonitoredObjectType.BEAN_CACHE,
                    MonitoredObjectType.BEAN_METHOD};

    /**
     * List of monitored object types for mdb container
     */
    public static final MonitoredObjectType[] MDB_TYPES =
            new MonitoredObjectType[] {
                    MonitoredObjectType.ROOT,
                    MonitoredObjectType.APPLICATION,
                    MonitoredObjectType.EJBMODULE,
                    MonitoredObjectType.STANDALONE_EJBMODULE,
                    MonitoredObjectType.MESSAGE_DRIVEN_BEAN,
                    MonitoredObjectType.BEAN_POOL,
                    MonitoredObjectType.BEAN_CACHE,
                    MonitoredObjectType.BEAN_METHOD};

    /**
     * List of monitored object types for orb
     */
    public static final MonitoredObjectType[] ORB_TYPES =
            new MonitoredObjectType[] {
                    MonitoredObjectType.ROOT,
                    MonitoredObjectType.IIOP_SERVICE,
                    MonitoredObjectType.ORB,
                    MonitoredObjectType.ORB_CONNECTION,
                    MonitoredObjectType.ORB_THREAD_POOL};

    /**
     * List of monitored object types for JTS
     */
    public static final MonitoredObjectType[] JTS_TYPES =
            new MonitoredObjectType[] {
                    MonitoredObjectType.ROOT,
                    MonitoredObjectType.TXNMGR};

    /**
     * Enable monitoring for specified types. Note that a type can be enabled
     * more than once as it can be used by different monitorable components.
     */
    private static synchronized void enableTypes(MonitoredObjectType[] types) {
        int size = types.length;
        for (int i = 0; i < size; i++) {
            types[i].enableCount++;
        }
    }

    /**
     * Disble monitoring for specified types. Note that a type can be disbled
     * more than once as it can be used by different monitorable components.
     */
    private static synchronized void disableTypes(MonitoredObjectType[] types) {
        int size = types.length;
        for (int i = 0; i < size; i++) {
            if (types[i].enableCount > 0) {
                types[i].enableCount--;
            } else {
				String msg = localStrings.getString( "admin.monitor.monitored_object_type_already_disabled", types[i].toString() );
                throw new IllegalStateException( msg );
            }
        }
    }

    /**
     * Tracks whether monitoring is enabled on ejb container
     */
    private static boolean ejbMonitoringEnabled = false;

    /**
     * Tracks whether monitoring is enabled on mdb container
     */
    private static boolean mdbMonitoringEnabled = false;

    /**
     * Tracks whether monitoring is enabled on orb
     */
    private static boolean orbMonitoringEnabled = false;

    /**
     * Tracks whether monitoring is enabled on JTS
     */
    private static boolean jtsMonitoringEnabled = false;

	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( MonitoredObjectType.class );

    /**
     * Set monitoring enabled or disabled for ejb container
     * @param enable if true monitoring is enabled, otherwise it is disabled
     */
    public static void setEjbMonitoringEnabled(boolean enable) {
        ejbMonitoringEnabled = setMonitoringEnabled(EJB_TYPES,
                ejbMonitoringEnabled, enable);
    }

    /**
     * Set monitoring enabled or disabled for mdb container
     * @param enable if true monitoring is enabled, otherwise it is disabled
     */
    public static void setMdbMonitoringEnabled(boolean enable) {
        mdbMonitoringEnabled = setMonitoringEnabled(MDB_TYPES,
                mdbMonitoringEnabled, enable);
    }

    /**
     * Set monitoring enabled or disabled for orb
     * @param enable if true monitoring is enabled, otherwise it is disabled
     */
    public static void setOrbMonitoringEnabled(boolean enable) {
        orbMonitoringEnabled = setMonitoringEnabled(ORB_TYPES,
                orbMonitoringEnabled, enable);
    }

    /**
     * Set monitoring enabled or disabled for jts
     * @param enable if true monitoring is enabled, otherwise it is disabled
     */
    public static void setJtsMonitoringEnabled(boolean enable) {
        jtsMonitoringEnabled = setMonitoringEnabled(JTS_TYPES,
                jtsMonitoringEnabled, enable);
    }

    /**
     * Set monitoring enabled or disabled on specified component.
     * @param comp the component name - this is the xpath of the component
     *     that has monitoring enabled.
     * @param enable turn monitoring on (if true) or off (if false)
     */
    static void setMonitoringEnabled(String comp, boolean enable) {
        if (ServerTags.EJB_CONTAINER.equals(comp)) {
            setEjbMonitoringEnabled(enable);
        } else if (ServerTags.MDB_CONTAINER.equals(comp)) {
            setMdbMonitoringEnabled(enable);
        } else if (ServerTags.ORB.equals(comp)) {
            setOrbMonitoringEnabled(enable);
        } else if (ServerTags.TRANSACTION_SERVICE.equals(comp)) {
            setJtsMonitoringEnabled(enable);
        }
    }

    /**
     * Enable or Disable monitoring for specified monitored object types. If
     * oldEnabled and newEnabled are same then this method is a NO-OP.
     * @param types list of monitored object types
     * @param oldEnabled old value of monitoring enabled
     * @param newEnabled new value of monitoring enabled
     * @return new value of monitoring enabled.
     */
    private static boolean setMonitoringEnabled(MonitoredObjectType[] types,
            boolean oldEnabled, boolean newEnabled) {
        if (newEnabled != oldEnabled) {
            oldEnabled = newEnabled;
            if (oldEnabled) {
                enableTypes(types);
            } else {
                disableTypes(types);
            }
        }
        return newEnabled;
    }
}
