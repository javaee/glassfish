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
import java.util.Iterator;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.ObjectName;

/**
 * Get command for monitoring.
 */
public class MonitorGetCommand extends MonitorCommand {

    /**
     * Constant to denote - get specified attribute value from specified MBean
     */
    private static final int GET_ATTR = 2;

    /**
     * Constant to denote - get all attribute values from specified MBean
     */
    private static final int GET_ALL_ATTR = 4;

    /**
     * Constant to denote - get specified attribute value from all child MBeans
     * of specified type.
     */
    private static final int GET_ATTR_FOR_TYPE = 6;

    /**
     * Constant to denote - get all attribute values from all child MBeans of
     * specified type.
     */
    private static final int GET_ALL_ATTR_FOR_TYPE = 8;

    /**
     * Name of the attribute. This is either an attribute name or wildcard
     * character (*) denoting all attributes. Partial wildcard is not supported,
     * so num-invocation can be used but num* can not be.
     */
    private String attrName;

    /**
     * Create a new get command that gets specified attribute from specified
     * MBean. 
     * @param mbeanName object name of the MBean
     * @param attrName name of the attribute. This can be wildcard character
     */
    MonitorGetCommand(ObjectName mbeanName, String attrName) {
        this.objectName = mbeanName;
        this.attrName = attrName;
        if (WILDCARD.equals(attrName)) {
            actionCode = GET_ALL_ATTR;
        } else {
            actionCode = GET_ATTR;
        }
    }

    /**
     * Create a new get command that gets specified attribute from all child
     * of specified type from the specified MBean. 
     * @param mbeanName object name of the MBean
     * @param type the type of child MBeans to search
     * @param attrName name of the attribute. This can be wildcard character
     */
    MonitorGetCommand(ObjectName mbeanName, MonitoredObjectType type,
            String attrName) {
        this.objectName = mbeanName;
        this.monitoredObjectType = type.getTypeName();
        this.attrName = attrName;
        if (WILDCARD.equals(attrName)) {
            actionCode = GET_ALL_ATTR_FOR_TYPE;
        } else {
            actionCode = GET_ATTR_FOR_TYPE;
        }
    }

    /**
     * Run get command. This method returns an instance of jmx AttributeList.
     * @throws InstanceNotFoundException if the MBean is not found
     * @throws AttributeNotFoundException if the attribute is not found
     * @return list of attributes
     */
    Object runCommand() throws InstanceNotFoundException,
            AttributeNotFoundException {
        BaseMonitorMBean mbean = MonitoringHelper.getMonitorMBean(objectName);
        MonitoredObjectType type = null;
        if (monitoredObjectType != null) {
            type = MonitoredObjectType.getMonitoredObjectType(monitoredObjectType);
        }
        ArrayList mbeanList = null;
        if (actionCode == GET_ATTR_FOR_TYPE
                || actionCode == GET_ALL_ATTR_FOR_TYPE) {
            mbeanList = mbean.getChildList(type);
        } else {
            mbeanList = new ArrayList();
            mbeanList.add(mbean);
        }
        AttributeList result = new AttributeList();
        Iterator iter = mbeanList.iterator();
        while (iter.hasNext()) {
            BaseMonitorMBean bmb = (BaseMonitorMBean)iter.next();
            if (actionCode == GET_ATTR || actionCode == GET_ATTR_FOR_TYPE) {
                Object val = bmb.getAttribute(attrName);
                Attribute attr = null;
                if (actionCode == GET_ATTR_FOR_TYPE) {
                    attr = new Attribute(getQualifiedName(bmb, type, attrName),
                            val);
                } else {
                    attr = new Attribute(attrName, val);
                }
                result.add(attr);
            } else if (actionCode == GET_ALL_ATTR
                    || actionCode == GET_ALL_ATTR_FOR_TYPE) {
                String[] attrNames = bmb.getAllAttributeNames();
                AttributeList attrList = bmb.getAttributes(attrNames);
                if (actionCode == GET_ALL_ATTR_FOR_TYPE) {
                    AttributeList newAttrList = new AttributeList();
                    int numAttr = attrList.size();
                    for (int i = 0; i < numAttr; i++) {
                        Attribute attr = (Attribute)attrList.get(i);
                        attr = new Attribute(
                                getQualifiedName(bmb, type, attr.getName()),
                                attr.getValue());
                        newAttrList.add(attr);
                    }
                    attrList = newAttrList;
                }
                result.addAll(attrList);
            }
        }
        return result;
    }

    /**
     * Get qualified name of an attribute. It is possible to get attributes
     * from more than one MBean in same command invocation. In these cases,
     * the attribute names are prefixed with child MBean type and optionally
     * child MBean name to scope the attribute name properly.
     * @param mbean the child mbean
     * @param type type of the child mbean
     * @param attrName name of the attribute in child mbean
     */
    private String getQualifiedName(BaseMonitorMBean mbean,
            MonitoredObjectType type, String attrName) {
        StringBuffer fullName = new StringBuffer();
        fullName.append(mbean.getNodeType());
        if (!type.isSingleton()) {
            fullName.append("." + mbean.getNodeName());
        }
        fullName.append("." + attrName);
        return fullName.toString();
    }

    /**
     * Constant to denote wildcard character (addresses all attributes)
     */
    private static final String WILDCARD = "*";

}
