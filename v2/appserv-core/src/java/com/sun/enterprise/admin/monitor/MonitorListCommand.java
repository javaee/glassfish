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

import javax.management.InstanceNotFoundException;
import javax.management.ObjectName;

/**
 * List command for monitoring. 
 */
public class MonitorListCommand extends MonitorCommand {

    /**
     * Constant to denote - List types of available MBeans
     */
    private static final int LIST_TYPE = 1;

    /**
     * Constant to denote - List available MBean names.
     */
    private static final int LIST_INSTANCE = 3;

    /**
     * Constant to denote - List available MBean types and names.
     */
    private static final int LIST_TYPE_AND_INSTANCE = 5;

    /**
     * Create a MonitorListCommand to list available MBean types and names. This
     * command will produce a list of all child MBeans in the form type.name.
     * @param mbeanName object name of the MBean
     */
    MonitorListCommand(ObjectName mbeanName) {
        this.objectName = mbeanName;
        this.actionCode = LIST_TYPE_AND_INSTANCE;
    }

    /**
     * Create a MonitorListCommand to list available MBean names. This command
     * will produce a list of names of all child MBeans of specified type.
     * @param mbeanName object name of the MBean
     * @param type type of the child MBeans
     */
    MonitorListCommand(ObjectName mbeanName, MonitoredObjectType type) {
        this.objectName = mbeanName;
        this.actionCode = LIST_INSTANCE;
        this.monitoredObjectType = type.getTypeName();
    }

    /**
     * Run the command to produce a list. The method returns a String[] of
     * length 0 or more.
     * @throws InstanceNotFoundException if the MBean is not found.
     */
    Object runCommand() throws InstanceNotFoundException {
        BaseMonitorMBean mbean = MonitoringHelper.getMonitorMBean(objectName);
        ArrayList childList = null;
        if (actionCode == LIST_INSTANCE) {
            childList = mbean.getChildList(
                    MonitoredObjectType.getMonitoredObjectType(monitoredObjectType));
        } else {
            childList = mbean.getChildList();
        }
        String[] result = new String[childList.size()];
        Iterator iter = childList.iterator();
        int i = 0;
        while (iter.hasNext()) {
            BaseMonitorMBean child = (BaseMonitorMBean)iter.next();
            MonitoredObjectType childType =
                    MonitoredObjectType.getMonitoredObjectType(child.getNodeType());
            if (actionCode == LIST_INSTANCE) {
                result[i] = child.getNodeName();
            } else {
                if (childType.isSingleton()) {
                    result[i] = child.getNodeType();
                } else {
                    result[i] = child.getNodeType() + "." + child.getNodeName();
                }
            }
            i++;
        }
        return result;
    }

}
