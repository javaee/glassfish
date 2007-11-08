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
 * $Id: BaseMonitorMBean.java,v 1.5 2007/05/05 05:33:43 tcfujii Exp $
 */
package com.sun.enterprise.admin.monitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.monitor.types.MonitoredAttributeType;
import com.sun.enterprise.admin.common.MBeanServerFactory;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Monitoring MBean in iAS. This is the superclass of all monitoring MBeans.
 * <p>
 * All monitoring MBeans are registered to MBeanServer. Even though MBeanServer
 * does not enforce any hierarchy of MBeans, the implementation within iAS is
 * of a monitoring MBean hierarchy (tree). Therefore, MonitoringMBean for a j2ee
 * application contains monitoring MBeans for all ejb modules and web modules;
 * a monitoring MBean for a ejb module contains monitoring MBeans for all beans
 * (stateless and stateful session beans, entity beans and message driven beans).
 * </p>
 * <p>
 * A monitoring MBean is fully identified within its context by a type and a
 * name. A monitoring MBean exposed by the method getRootMonitorMBean in
 * com.sun.enterprise.admin.server.AdminService is the root of the monitoring MBean
 * hierarchy.
 * </p>
 * <p>
 * The methods <code>getNodeName, setNodeName, getNodeType and setNodeType,
 * addChild, removeChild, getChild</code> can be used to manage/access heirarchy
 * of MBeans. However, the properties and operations represented by those methods
 * is not available in mBeanInfo, so the heirarchy can not be managed/accessed
 * through MBeanServer interface.
 * </p>
 */
public abstract class BaseMonitorMBean implements DynamicMBean, IMonitorable {

    /**
     * A reference to logger object
     */
    static Logger logger = Logger.getLogger(AdminConstants.kLoggerName);

    /**
     * A map of object names and MBean instance
     */
    static final HashMap objectNameMap = new HashMap();

    /**
     * Names of all attributes exposed by this MBean
     */
    private String[] attrNames = null;

    /**
     * Object name of this MBean as registered in MBean server
     */
    private ObjectName objectName;

    /**
     * Name of this MBean at its level in MBean heirarchy.
     */
    private String nodeName;

    /**
     * Type of this MBean as registered in MBean heirarchy.
     */
    private String nodeType;

    /**
     * List of child.
     */
    protected Vector childList = new Vector();

    /**
     * Set object name for this MBean. This should be same as the name used
     * to register this MBean to MBeanServer.
     * @param objName name of this MBean as registered in MBean Server
     */
    protected void setObjectName(ObjectName objName) {
        objectName = objName;
    }

    /**
     * Get name of this MBean as registered in MBean server.
     */
    public ObjectName getObjectName() {
        return objectName;
    }

    /**
     * Get name of this MBean. A typical example is - for a stateless session
     * bean called fortune, this method will return fortune. The MBean has
     * a fully qualified name in the MBeanServer, but this method only returns
     * name from the current context.
     * @return name of the MBean within its context.
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * Set name of the node to specified value. Invoked from addChild method of
     * the parent of this MBean. So, this method need not be called elsewhere.
     * A call to this method does not affect the name in MBeanServer, so the
     * caller of this method should also handle naming within MBeanServer.
     * @param name name of this MBean
     */
    protected void setNodeName(String name) {
        nodeName = name;
    }

    /**
     * Get type of this MBean. This is the type used in the hierarchy of
     * monitoring MBeans.
     * @return type of this MBean.
     */
    public String getNodeType() {
        return nodeType;
    }

    /**
     * Set type of this MBean to specified value. This method is invoked from
     * the method addChild of the parent of this MBean. A call to this method
     * does not affect the naming within MBeanServer, so the caller of this
     * method should also handle naming within MBeanServer.
     * @param type type of the MBean
     */
    protected void setNodeType(String type) {
        nodeType = type;
    }

    /**
     * Add an MBean to the MBean tree below this MBean. This method also adds
     * the specified child MBean to the MBeanServer. Adding monitoring MBean to
     * MBeanServer requires object name and can be derived for the child, if
     * this MBean (parent) was already added to MBeanServer. 
     * @param name name of the MBean
     * @param type type of the MBean
     * @param mBean the new MBean
     * @throws InstanceAlreadyExistsException if an MBean of this name and type
     *     already exists or if this type is a singleton and one instance
     *     already exists.
     * @throws MBeanRegistrationException preRegister (MBeanRegistration
     *         interface) method of the MBean has thrown an exception.
     * @throws IllegalArgumentException if any of name, type or mBean is null
     * @throws IllegalStateException if this MBean has not yet been added to
     *         tree of monitoring MBeans. 
     * @return name of the child MBean in MBeanServer.
     */
    public synchronized ObjectName addChild(String name, MonitoredObjectType type,
            BaseMonitorMBean mBean)
            throws InstanceAlreadyExistsException, MBeanRegistrationException {
        if (name == null || type == null || mBean == null) {
            String str1 = (name == null) ? "Child Name (name); " : "";
            String str2 = (type == null) ? "Child Type (type); " : "";
            String str3 = (mBean == null) ? "MBean (mBean); " : "";

			String msg = localStrings.getString( "admin.monitor.null_arguments", str1, str2, str3 );
            throw new IllegalArgumentException( msg );
        }
        if (objectName == null || nodeName == null || nodeType == null) {
			String msg = localStrings.getString( "admin.monitor.monitoring_mbean_not_added_to_mbeantree" );
            throw new IllegalStateException( msg );
        }
        boolean exists = false;
        int size = childList.size();
        for (int i = 0; i < size; i++) {
            BaseMonitorMBean m = (BaseMonitorMBean)childList.elementAt(i);
            if (type.isSingleton()) {
                if (m.nodeType.equals(type.getTypeName())) {
                    exists = true;
                    break;
                }
            } else {
                if (m.nodeType.equals(type.getTypeName())
                        && m.nodeName.equals(name)) {
                    exists = true;
                    break;
                }
            }
        }
        if (exists) {
			String msg = localStrings.getString( "admin.monitor.mbean_already_exists", type, name );
            throw new InstanceAlreadyExistsException( msg );
        }
        if (type.isSingleton()) {
            name = type.getTypeName();
        }
        childList.add(mBean);
        mBean.setNodeName(name);
        mBean.setNodeType(type.getTypeName());
        ObjectName childObjName = getChildObjectName(objectName, type, name);
        mBean.setObjectName(childObjName);
        MBeanServer mbs = getMBeanServer();
       /**
        * Commenting this block for now
        try {
            mbs.registerMBean(mBean, childObjName);
        } catch (NotCompliantMBeanException ncme) {
            logger.log(Level.WARNING, NON_COMPLIANT_MBEAN, childObjName);
            throw new MBeanRegistrationException(ncme);
        }
        */
        objectNameMap.put(childObjName, mBean);
        if (type.isMonitoringEnabled()) {
            mBean.startMonitoring();
        }
        return childObjName;
    }

    /**
     * Derive Object Name for the child MBean.
     */
    private static ObjectName getChildObjectName(ObjectName parent,
            MonitoredObjectType childType, String childName)
            throws MBeanRegistrationException {
        Hashtable props = parent.getKeyPropertyList();
        Hashtable newProps = (Hashtable)props.clone();
        String type = (String)props.get(MON_OBJTYPE);
        String name = (String)props.get(MON_OBJNAME);
        newProps.put(type, name);
        newProps.put(MON_OBJTYPE, childType.getTypeName());
        newProps.put(MON_OBJNAME, childName);
        ObjectName newName = null;
        try {
            newName = new ObjectName(parent.getDomain(), newProps);
        } catch (MalformedObjectNameException mone) {
            throw new MBeanRegistrationException(mone, mone.getMessage());
        }
        return newName;
    }

    /**
     * Get list of children of this MBean. This method returns all children of
     * this MBean. If there is no child, the method returns an empty list.
     * @return an arraylist containing all children
     */
    public ArrayList getChildList() {
        ArrayList list = new ArrayList();
        list.addAll(childList);
        return list;
    }

    /**
     * Get list of children of specified type for this MBean. This method
     * returns all children of specified type for this MBean. If there is no
     * child of specified type, the method returns an empty list.
     * @param type type of the child MBean
     * @throws IllegalArgumentException if type is null
     * @return an arraylist containing all children
     */
    public ArrayList getChildList(MonitoredObjectType type) {
        if (type == null) {
			String msg = localStrings.getString( "admin.monitor.null_argument_mbean_type" );
            throw new IllegalArgumentException( msg );
        }
        ArrayList list = new ArrayList();
        Iterator iter = childList.iterator();
        while (iter.hasNext()) {
            BaseMonitorMBean mBean = (BaseMonitorMBean)iter.next();
            if (mBean.nodeType.equals(type.getTypeName())) {
                list.add(mBean);
            }
        }
        return list;
    }

    /**
     * Get list of children of specified name for this MBean. This method
     * returns all children of specified name for this MBean. If there is no
     * child of specified name, the method returns an empty list.
     * @param name name of the child MBean
     * @return an arraylist containing all children
     */
    public ArrayList getChildList(String name) {
        ArrayList list = new ArrayList();
        Iterator iter = childList.iterator();
        while (iter.hasNext()) {
            BaseMonitorMBean mBean = (BaseMonitorMBean)iter.next();
            if (mBean.nodeName.equals(name)) {
                list.add(mBean);
            }
        }
        return list;
    }

    /**
     * Get specified child MBean. If type is singleton, then name parameter is
     * not used for searching.
     * @param type type of the child MBean
     * @param name name of the child MBean
     * @return the child MBean
     * @throws InstanceNotFoundException if child MBean of specified type and
     *      name is not found.
     * @throws IllegalArgumentException if specified type is null.
     */
    public BaseMonitorMBean getChild(MonitoredObjectType type, String name)
            throws InstanceNotFoundException {
        BaseMonitorMBean found = getChildOrNull(type, name);
        if (found == null) {
			String msg = localStrings.getString( "admin.monitor.mbean_not_found", type, name );
            throw new InstanceNotFoundException( msg );
        }
        return found;
    }

    /**
     * Get specified child mbean or null if there is no such child.
     * @param type type of the child MBean
     * @param name name of the child MBean
     * @throws IllegalArgumentException if specified type is null.
     * @return the child MBean if it exists, null otherwise
     */
    BaseMonitorMBean getChildOrNull(MonitoredObjectType type, String name) {
        if (type == null) {
			String msg = localStrings.getString( "admin.monitor.monitored_object_type_null" );
            throw new IllegalArgumentException( msg );
        }
        BaseMonitorMBean found = null;
        Iterator iter = childList.iterator();
        while (iter.hasNext()) {
            BaseMonitorMBean mBean = (BaseMonitorMBean)iter.next();
            if (type.isSingleton()) {
                if (mBean.nodeType.equals(type.getTypeName())) {
                    found = mBean;
                }
            } else {
                if (mBean.nodeType.equals(type.getTypeName())
                        && mBean.nodeName.equals(name)) {
                    found = mBean;
                }
            }
        }
        return found;
    }

    /**
     * Get first child MBean with the specified name.
     * @throws InstanceNotFoundException if no child MBean of specified name
     *         exists.
     * @return child MBean with specified name
     */
    BaseMonitorMBean getFirstChildByName(String name)
            throws InstanceNotFoundException {
        ArrayList list = getChildList(name);
        if (list.isEmpty()) {
			String msg = localStrings.getString( "admin.monitor.child_mbean_not_available", name, objectName );
            throw new InstanceNotFoundException( msg );
        }
        return ((BaseMonitorMBean)list.get(0));
    }

    /**
     * Remove specified monitoring MBean from MBean tree. This method will
     * remove the specified MBean from MBeanServer as well. If the specified
     * MBean has any child(ren) they are also removed from the MBean tree and
     * MBeanServer.
     * @param type the type of MBean
     * @param name the name of MBean
     * @throws InstanceNotFoundException if the specified MBean (name and type)
     *      is not a child of this MBean.
     * @throws MBeanRegistrationException preDeregister (MBeanRegistration
     *         interface) method of the MBean has thrown an exception
     * @throws IllegalArgumentException if specified type is null.
     */
    public synchronized void removeChild(MonitoredObjectType type, String name)
            throws InstanceNotFoundException, MBeanRegistrationException {
        BaseMonitorMBean mBean = getChild(type, name);
        removeChild(mBean);
    }

    /**
     * Remove specified child MBean from MBean tree. This method will
     * remove the specified MBean from MBeanServer as well. If the specified
     * MBean has any child(ren) they are also removed from the MBean tree and
     * MBeanServer.
     * @param child the child MBean
     * @throws InstanceNotFoundException if the specified MBean is not a child
     *         of this MBean.
     * @throws MBeanRegistrationException preDeregister (MBeanRegistration
     *         interface) method of the MBean has thrown an exception
     */
    synchronized void removeChild(BaseMonitorMBean child)
            throws InstanceNotFoundException, MBeanRegistrationException {
        removeAllChild(child);
        childList.remove(child);
        objectNameMap.remove(child.objectName);
        MBeanServer mbs = getMBeanServer();
        // Commenting this line for now
        // mbs.unregisterMBean(child.objectName);
    }

    /**
     * Remove all child MBeans of specified mbean. This methos also unregisters
     * all child Mbeans from MBean server. It recusrively calls itself if the
     * child mbeans also have child(ren).
     * @throws InstanceNotFoundException if any of the child MBean is not
     *         registered to MBeanSever.
     * @throws MBeanRegistrationException preDeregister (MBeanRegistration
     *         interface) method of any of the child MBean throws an exception
     */
    private static void removeAllChild(BaseMonitorMBean mbean)
            throws InstanceNotFoundException, MBeanRegistrationException {
        Iterator iter = mbean.childList.iterator();
        while (iter.hasNext()) {
            BaseMonitorMBean child = (BaseMonitorMBean)iter.next();
            removeAllChild(child);
            objectNameMap.remove(child.objectName);
            MBeanServer mbs = getMBeanServer();
            // Commenting this line for now
            // mbs.unregisterMBean(child.objectName);
        }
        mbean.childList.removeAllElements();
    }

    /**
     * Get MBeanServer. If MBeanServer is not found then this method throws
     * RuntimeException.
     * @throws RuntimeException if there is any error in getting MBeanServer
     * @return an implementation of MBeanServer interface
     */
    private static MBeanServer getMBeanServer() {
        return MBeanServerFactory.getMBeanServer();
    }

    /**
     * Obtains the value of a specific monitored attribute.
     * @param attribute The name of the attribute to be retrieved
     * @returns The value of the attribute retrieved.
     * @throws AttributeNotFoundException if attribute name is not valid
     */
    public abstract Object getAttribute(String attribute)
            throws AttributeNotFoundException;

    /**
     * Get the values of several attributes of the monitoring MBean.
     * @param attributes A list of the attributes to be retrieved.
     * @returns The list of attributes retrieved.
     */
    public abstract AttributeList getAttributes(String[] attributes);

    /**
     * Provides the exposed attributes and actions of the monitoring MBean using
     * an MBeanInfo object. The implementation for this method should ensure
     * that the returned value is always same (not necessarily same reference,
     * but same contained values).
     * @returns An instance of MBeanInfo with all attributes and actions exposed
     *          by this monitoring MBean.
     */
    public abstract MBeanInfo getMBeanInfo();

    /**
     * Invoke a operation on this MBean.
     */
    public Object invoke(String str, Object[] obj, String[] str2)
            throws MBeanException, ReflectionException {
        throw new UnsupportedOperationException(
                getLocalString(UNSUPPORTED_ERRCODE, UNSUPPORTED_ERRMSG));
    }

    /**
     * Set an attribute on this MBean.
     */
    public final void setAttribute(Attribute attribute)
            throws AttributeNotFoundException, InvalidAttributeValueException,
            MBeanException, ReflectionException {
        throw new UnsupportedOperationException(
                getLocalString(UNSUPPORTED_ERRCODE, UNSUPPORTED_ERRMSG));
    }

    /**
     * Set specified attributes on this MBean
     */
    public final AttributeList setAttributes(AttributeList attributeList) {
        throw new UnsupportedOperationException(
                getLocalString(UNSUPPORTED_ERRCODE, UNSUPPORTED_ERRMSG));
    }

    /**
     * Get type of the specified monitored attribute.
     */
    public abstract MonitoredAttributeType getAttributeType(String attrName);

    /**
     * Start monitoring on this component. This will be called when monitoring
     * is enabled on this component (or the group containing this component)
     * through user interface.
     * @see stopMonitoring
     */
    public void startMonitoring() {
    }

    /**
     * Stop monitoring on this component. Called when monitoring is disabled on
     * user interface.
     */
    public void stopMonitoring() {
    }

    /**
     * Get a map of monitored attribute names and their types. The keys in
     * the map are names of the attribute and the values are their types. The
     * type value are instances of class
     * com.iplanet.ias.monitor.type.MonitoredAttributeType (or its sub-classes)
     *
     * @return map of names and types of all monitored attributes
     */
    public abstract Map getMonitoringMetaData();

    /**
     * Get value of specified monitored attribute. This method is from interface
     * IMonitorable and is implemented to use method getAttribute from
     * DynamicMBean interface.
     * @param monitorAttributeName name of the monitored attribute
     * @return value of the specified monitored attribute, if it exists, null
     *        otherwise.
     */
    public final Object getMonitoredAttributeValue(String monitorAttributeName) {
        Object result = null;
        try {
            result = getAttribute(monitorAttributeName);
        } catch (AttributeNotFoundException anfe) {
        }
        return result;
    }

    /**
     * Get values of specified monitored attributes. This method returns a
     * map of monitored attribute names and their corresponding values. This
     * method is from interface IMonitorable and is implemented to use method
     * getAttributes from DynamicMBean interface.
     *
     * @param monitorAttributeNameSet set of monitored attribute names
     *
     * @return map of attribute names and their values
     */
    public final Map getMonitoredAttributeValues(Set monitorAttributeNameSet) {
        return null;
    }

    /**
     * Convenience method to create MBeanInfo from a HashMap of attribute
     * names and attribute types. This method can be called from static
     * initializer of sub-classes to initialize a static variable with
     * MBeanInfo (which can then be returned by the method getMBeanInfo). This
     * method assumes that sub-classes of BaseMonitorMBean do not implement any
     * additional JMX operations or notifications. If the sub-classes need to
     * implement their own operations and/or notifications the MBeanInfo
     * returned from this method should be updated to add those.
     *
     * @param map of attribute names (instances of String) and attribute
     *        types (instances of MonitoredAttributeTypes)
     *
     * @return info for this MBean.
     */
    protected static MBeanInfo createMBeanInfo(Map attrNameTypeMap) {
        return (createMBeanInfo(attrNameTypeMap, new MBeanOperationInfo[0]));
    }

    /**
     * Convenience method to create MBeanInfo from a HashMap of attribute
     * names and attribute types. This method can be called from static
     * initializer of sub-classes to initialize a static variable with
     * MBeanInfo (which can then be returned by the method getMBeanInfo). This
     * method assumes that sub-classes of BaseMonitorMBean do not implement any
     * additional JMX operations or notifications. If the sub-classes need to
     * implement their own operations and/or notifications the MBeanInfo
     * returned from this method should be updated to add those.
     *
     * @param map of attribute names (instances of String) and attribute
     *        types (instances of MonitoredAttributeTypes)
     *
     * @return info for this MBean.
     */
    protected static MBeanInfo createMBeanInfo(Map attrNameTypeMap, 
                            MBeanOperationInfo[] operationInfoArray) {
        String className = GenericMonitorMBean.class.getName();
        String description = "Generic Monitoring MBean";
        attrNameTypeMap = convertKeysToCamelCase(attrNameTypeMap);
        Set keys = attrNameTypeMap.keySet();
        Iterator names = keys.iterator();
        MBeanAttributeInfo[] attrList = new MBeanAttributeInfo[keys.size()];
        int i = 0;
        while (names.hasNext()) {
            String name = (String)names.next();
            String type = ((MonitoredAttributeType)attrNameTypeMap.get(name)).getJavaTypeName();
            String desc = "Monitored attribute " + name;
            attrList[i] = new MBeanAttributeInfo(name, type, desc, true, false, false);
            i++;
        }
        MBeanConstructorInfo[] constructorList = new MBeanConstructorInfo[0];
        //MBeanOperationInfo[] operationList = new MBeanOperationInfo[0];
        MBeanNotificationInfo[] notificationList = new MBeanNotificationInfo[0];
        return new MBeanInfo(className, description, attrList, constructorList,
               operationInfoArray, notificationList);
    }
    
    /**
     * Get name of all attributes exposed by this MBean. The method returns
     * a string array of length zero or more. The default implementation
     * uses MBeanInfo to derive list of attribute names and throws an
     * IllegalStateException if MBeanInfo is null. 
     * @return a string array containing names of all exposed attributes.
     */
    public String[] getAllAttributeNames() {
        return getAllAttributeNamesFromMBeanInfo();
    }

    /**
     * Get name of all attributes exposed by this MBean. This method uses
     * MBeanInfo to derive list of all attributes and caches it. Therefore,
     * if MBeanInfo is changed after a call to this method it will not affect
     * the return value of this method. JMX specification requires that
     * MBeanInfo returned by DynamicMBean implementation does not change once
     * the MBean has been registered.
     * @throws IllegalStateException if MBeanInfo returned by the method
     *     <code>getMBeanInfo</code> is null.
     */
    private String[] getAllAttributeNamesFromMBeanInfo() {
        if (attrNames == null) {
            MBeanInfo info = this.getMBeanInfo();
            if (info == null) {
				String msg = localStrings.getString( "admin.monitor.null_mbean_info", this.getClass() );
                throw new IllegalStateException( msg );
            }
            MBeanAttributeInfo[] attrInfoList = info.getAttributes();
            if (attrInfoList == null) {
				String msg = localStrings.getString( "admin.monitor.null_attribute_info_in_mbeaninfo", this.getClass() );
                throw new IllegalStateException( msg );
            }
            int size = attrInfoList.length;
            attrNames = new String[size];
            for (int i = 0; i < size; i++) {
                attrNames[i] = attrInfoList[i].getName();
            }
        }
        return attrNames;
    }

    /**
     * Convenience method to create a map of monitorable attribute names and
     * their types from a 2-d array of attribute names and attribute types.
     * Attribute types are instances of class MonitoredAttributeType in the
     * package com.sun.enterprise.admin.monitor.types. This method can be called
     * from static initializer of sub-classes to initialize a static variable
     * with a Map of attribute names and types (which can then be returned by
     * the method getMonitoringMetaData)
     *
     * @param attrNameTypeArray 2-d array of objects containing lists of
     *        atttribute names (instances of String) and their corresponding
     *        types (instances of MonitoredAttributeTypes)
     *
     * @return map of attribute names and their corresponding types.
     */
    protected static Map createAttrNameTypeMap(Object[][] attrNameTypeArray) {
        HashMap map = new HashMap();
        for (int i = attrNameTypeArray.length; i > 0; i--) {
            map.put(attrNameTypeArray[i-1][0], attrNameTypeArray[i-1][1]);
        }
        return map;
    }

    private static String getLocalString(String key, String dflt) {
        return dflt;
    }

    /**
        Converts the keys in the passed map into camel-case strings.
        The given map must not be null.
        @return Map that contains keys as valid java identifiers.
        @param attrNameTypeMap a map with keys being invalid java-identifiers,
            an empty map in case of empty map passed.
    */
    private static Map convertKeysToCamelCase(Map attrNameTypeMap) {
        Iterator keys = attrNameTypeMap.keySet().iterator();
        Iterator values = attrNameTypeMap.values().iterator();
        Map newMap = new HashMap();
        while(keys.hasNext()) {
            String key = (String)keys.next();
            String camelCaseKey = toCamelCase(key);
            newMap.put(camelCaseKey, values.next());
        }
        return newMap;
    }
    
    /**
        Converts the given string with characters that are illegal identifier parts and converts it
        into a String with camel-case. The entire BNF is out of the scope.
        Following are the basic rules:
        <li> gets rid of all the illegal characters in given string.
        <li> all consecutive illegal characters are collapsed.
        <li> leading and trailing illegal characters are ignored.
        <li> given string should have no other illegal characters (that make it
            an illegal java-identifier). This is asserted.
        <li> The case of an already upper-case character is NOT changed.
       @throws NullPointerException in case of null string.
       @returns the String with camel-case version of passed string.
    */
    private static String toCamelCase(String illegalIdentifier) {
        final StringBuffer from = new StringBuffer(illegalIdentifier);
        final StringBuffer to = new StringBuffer();
        final int length = from.length();
	    boolean illegalFound = false;
        for (int i = 0 ; i < length ; i++) {
	    char currentChar = from.charAt(i);
	    /* First char should be valid start and char at any other position
             should be valid part, otherwise ignore it. */
            if (i == 0 && !Character.isJavaIdentifierStart(currentChar) ||
		        i > 0  && !Character.isJavaIdentifierPart(currentChar)) {
                illegalFound = true;
                continue;
            }
            if (illegalFound) {
                to.append(Character.toUpperCase(currentChar));
                illegalFound = false;
            }
            else {
                to.append(currentChar);
            }
        }
	    return (to.toString());
    }

	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( BaseMonitorMBean.class );

    protected static final String UNSUPPORTED_ERRCODE = localStrings.getString( "admin.monitor.unsupported_mbean_monitor" );
    protected static final String UNSUPPORTED_ERRMSG = localStrings.getString( "admin.monitor.unsupported_action_on_monitoring_mbean" );

    private static final String MON_OBJTYPE = ObjectNames.kMonitoringClassName;
    private static final String MON_OBJNAME = ObjectNames.kNameKeyName;

    private static final String NON_COMPLIANT_MBEAN = "monitor.non_compliant_mbean";
    private static final String MBS_INIT_ERROR = "monitor.mbs_init_error";

}
