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
import java.util.Iterator;
import java.util.Map;

import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.ObjectName;

import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.monitor.types.MonitoredAttributeType;
import com.sun.enterprise.admin.server.core.AdminService;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 * A simple monitoring MBean. This monitoring MBean does not expose any
 * monitorable attributes but is used only to group other monitoring MBeans
 * that expose monitorable properties.
 */
public class GenericMonitorMBean extends BaseMonitorMBean {

    /**
     * Root monitoring MBean.
     */
    private static GenericMonitorMBean root;

    /**
     * MBeanInfo for all generic MBeans, exposes no attributes.
     */
    private static MBeanInfo genericMBeanInfo = null;

	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( GenericMonitorMBean.class );

    /**
     * Creates a new instance of GenericMonitorMBean
     */
    public GenericMonitorMBean() {
    }

    /**
     * Get value of specified attribute. As there are no attributes exposed by
     * this object, the method always throws AttributeNotFoundException.
     * @throws AttributeNotFoundException always
     */
    public Object getAttribute(String str) throws AttributeNotFoundException {
		String msg = localStrings.getString( "admin.monitor.unknown_attribute", str );
        throw new AttributeNotFoundException( msg );
    }

    /**
     * Get type of specified monitored attribute. As there are no attributes
     * exposed by this object, the method always throws
     * UnsupportedOperationException.
     * @throws UnsupportedOperationException always.
     */
    public MonitoredAttributeType getAttributeType(String str) {
		String msg = localStrings.getString( "admin.monitor.unsupported_getattributetype" );
        throw new UnsupportedOperationException( msg );
    }

    /**
     * Get the values of several attributes of the monitoring MBean. As there
     * are no attributes exposed by this MBean, the method always returns a
     * empty AttributeList.
     * @param attributes A list of the attributes to be retrieved.
     * @returns The list of attributes retrieved.
     */
    public AttributeList getAttributes(String[] str) {
        return new AttributeList();
    }

    /**
     * Provides the exposed attributes and actions of the monitoring MBean using
     * an MBeanInfo object.
     * @returns An instance of MBeanInfo with all attributes and actions exposed
     *          by this monitoring MBean.
     */
    public MBeanInfo getMBeanInfo() {
        if (genericMBeanInfo == null) {
            genericMBeanInfo = createMBeanInfo(new HashMap());
        }
        return genericMBeanInfo;
    }

    /**
     * Get a map of monitored attribute names and their types. The keys in
     * the map are names of the attribute and the values are their types. The
     * type value are instances of class
     * com.iplanet.ias.monitor.type.MonitoredAttributeType (or its sub-classes).
     * As no attributes are exposed by this MBean, it always returns a empty
     * map.
     *
     * @return map of names and types of all monitored attributes
     */
    public Map getMonitoringMetaData() {
        return new HashMap();
    }

    /**
     * Start monitoring on the resource represented by this MBean. This method
     * starts monitoring on all child monitoring MBeans of this MBean by calling
     * startMonitoring on them one after another.
     */
    public void startMonitoring() {
        Iterator iter = childList.iterator();
        while (iter.hasNext()) {
            BaseMonitorMBean mBean = (BaseMonitorMBean)iter.next();
            mBean.startMonitoring();
        }
    }

    /**
     * Stop monitoring on the resource represented by this MBean. This method
     * stops monitoring on all child monitoring MBeans of this MBean by calling
     * stopMonitoring on them one after another.
     */
    public void stopMonitoring() {
        Iterator iter = childList.iterator();
        while (iter.hasNext()) {
            BaseMonitorMBean mBean = (BaseMonitorMBean)iter.next();
            mBean.stopMonitoring();
        }
    }

    /**
     * Start monitoring on the resource represented by this MBean, if the type
     * of this MBean is included in the type list. The method will also result
     * in call to startMonitoring() of all child MBeans of the type included in
     * the type list.
     * @param typeList list of monitored object types on which monitoring is to
     *     be enabled.
     */
    public void startMonitoring(MonitoredObjectType[] typeList) {
        HashMap typeMap = getMonitoredObjectTypeMap(typeList);
        startMonitoring(typeMap);
    }

    /**
     * Stop monitoring on the resource represented by this MBean, if the type
     * of this MBean is included in the type list. The method will also result
     * in call to startMonitoring() of all child MBeans of the type included in
     * the type list.
     * @param typeList list of monitored object types on which monitoring is to
     *     be enabled.
     */
    public void stopMonitoring(MonitoredObjectType[] typeList) {
        HashMap typeMap = getMonitoredObjectTypeMap(typeList);
        stopMonitoring(typeMap);
    }

    /**
     * Start monitoring on all child mbeans whose type is included in the
     * specified typeMap.
     * @param typeName a map of type string and MonitoredObjectType instance
     */
    private void startMonitoring(HashMap typeMap) {
        Iterator iter = childList.iterator();
        while (iter.hasNext()) {
            BaseMonitorMBean mBean = (BaseMonitorMBean)iter.next();
            if (typeMap.containsKey(mBean.getNodeType())) {
                if (mBean.getClass() == GenericMonitorMBean.class) {
                    ((GenericMonitorMBean)mBean).startMonitoring(typeMap);
                } else {
                    mBean.startMonitoring();
                }
            }
        }
    }

    /**
     * Stop monitoring on all child mbeans whose type is included in the
     * specified typeMap.
     * @param typeName a map of type string and MonitoredObjectType instance
     */
    private void stopMonitoring(HashMap typeMap) {
        Iterator iter = childList.iterator();
        while (iter.hasNext()) {
            BaseMonitorMBean mBean = (BaseMonitorMBean)iter.next();
            if (typeMap.containsKey(mBean.getNodeType())) {
                if (mBean.getClass() == GenericMonitorMBean.class) {
                    ((GenericMonitorMBean)mBean).stopMonitoring(typeMap);
                } else {
                    mBean.stopMonitoring();
                }
            }
        }
    }

    /**
     * Get a map of type and MonitoredObjectType instances from the array of
     * MonitoredObjectType instances.
     */
    private HashMap getMonitoredObjectTypeMap(MonitoredObjectType[] typeList) {
        HashMap map = new HashMap();
        int size = (typeList != null) ? typeList.length : 0;
        for (int i = 0; i < size; i++) {
            map.put(typeList[i].getTypeName(), typeList[i]);
        }
        return map;
    }

    /**
     * Get root monitoring MBean. The root MBean is initialized when the
     * server starts up and is available thereafter.
     */
    public static GenericMonitorMBean getRoot() {
        if (root == null) {
            root = new GenericMonitorMBean();
            root.setNodeName(ObjectNames.kMonitoringRootClass);
            root.setNodeType(ObjectNames.kMonitoringRootClass);
            String instName = AdminService.getAdminService().getInstanceName();
            ObjectName objName = ObjectNames.getRootMonitorMBeanName(instName);
            objectNameMap.put(objName, root);
            root.setObjectName(objName);
        }
        return root;
    }

}
