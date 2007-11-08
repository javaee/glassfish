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

package com.sun.enterprise.admin.common;

import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;

//JMX imports
import javax.management.ObjectName;
import javax.management.ObjectInstance;
import javax.management.MBeanServer;

//Admin imports
import com.sun.enterprise.admin.common.ObjectNames;

/**
    A Class to derive cues for Bean in Config API from ObjectName.
*/

public class ObjectNameHelper
{
    /**
        A static method that returns type of the MBean as indicated in
        com.sun.enterprise.admin.common.ObjectNames class. The given ObjectName
        may not be null. Returns null, if the ObjectName does not contain
        a property with name "type" in its property name value set.
    */
    
    public static String getType(ObjectName objectName)
    {
       String type = objectName.getKeyProperty(ObjectNames.kTypeKeyName);
       //type has to one of the declared ones and hence this should be asserted.
       
       return ( type );
    }

    /**
        Returns the name of the MBean denoted by this ObjectName. Name is the
        value of property with name "name" in key properties.
    */
    
    public static String getName(ObjectName objectName)
    {
        String name = objectName.getKeyProperty(ObjectNames.kNameKeyName);
        
        return ( name );
    }
    
    public static String getClassId(ObjectName objectName)
    {
        String classId = objectName.getKeyProperty(ObjectNames.kClassIdKeyName);
        return ( classId );
    }

    public static String getServerId(ObjectName objectName)
    {
        String serverId = objectName.getKeyProperty(ObjectNames.kServerIdKeyName);
        return ( serverId );
    }

    public static String getServerInstanceName(ObjectName objectName)
    {
        String instanceName = null;

        if (getType(objectName) != null) {
		if (getType(objectName).equals(ObjectNames.kServerInstance))
		{
			instanceName = getName(objectName);
		}
		else
		{
			instanceName = objectName.getKeyProperty(ObjectNames.kServerInstanceKeyName);
		}
	}

        return ( instanceName );
    }
	
	public static String getModuleType(ObjectName objectName)
	{
		String moduleType = null;
		
		moduleType = objectName.getKeyProperty(ObjectNames.kModuleTypeKeyName);
		
		return ( moduleType );
	}

	public static String getApplicationName(ObjectName objectName)
	{
		String app = null;
		
		app = objectName.getKeyProperty(ObjectNames.kApplicationNameKeyName);
		
		return ( app );
	}

    public static String getGroupId(ObjectName objectName)
	{
		String moduleType = null;
		
		moduleType = objectName.getKeyProperty(ObjectNames.kGroupIdKeyName);
		
		return ( moduleType );
	}

    public static String getVirtualServerClassId(ObjectName objectName)
    {
        String classId = objectName.getKeyProperty(
                            ObjectNames.kVirtualServerClassIdKeyName);
        return ( classId );
    }

    public static String getVirtualServerId(ObjectName objectName)
    {
        String virtualServerId = objectName.getKeyProperty(
                                    ObjectNames.kVirtualServerIdKeyName);
        return ( virtualServerId );
    }

    public static String getHttpListenerId(ObjectName objectName)
    {
        String listenerId = null;
        listenerId = objectName.getKeyProperty(ObjectNames.kHTTPListenerIdKeyName);
        return ( listenerId );
    }
    
    /**
        Returns true if this MBean is of type monitor.
        Useful to decide whether the persistent storage check should
        be performed.
    */
    public static boolean isMonitorMBean(ObjectName objectName)
    {
        boolean isMonitor = false;
        String monitorKeyValue = null;

        monitorKeyValue = objectName.getKeyProperty(
                ObjectNames.kTypeKeyName);
        if (monitorKeyValue != null)
        {
            isMonitor = monitorKeyValue.
                     equals(ObjectNames.kMonitoringType);
        }
        return ( isMonitor );
    }
    
    /**
        Returns array of config MBeans created for given server instance
    */
    public static ObjectName[] getInstanceRelatedMBeans(MBeanServer mbs, String instanceName) {
        final ObjectName pattern = ObjectNames.getAllObjectNamesPattern();  //aka "ias:*"
        final Set names  = mbs.queryMBeans(pattern, null);
        /* gets all the mbeans in the "ias" domain */
        final Iterator iter = names.iterator();
        final ArrayList arr = new ArrayList();
        while(iter.hasNext()) {
            final ObjectInstance objectInstance = (ObjectInstance) iter.next();
            final ObjectName objectName = objectInstance.getObjectName();
            final String name = ObjectNameHelper.getServerInstanceName(objectName);
            if(instanceName.equals(name)) {
                arr.add(objectName);
            }
        }
        return (ObjectName [])arr.toArray(new ObjectName[arr.size()]);
    }
}
