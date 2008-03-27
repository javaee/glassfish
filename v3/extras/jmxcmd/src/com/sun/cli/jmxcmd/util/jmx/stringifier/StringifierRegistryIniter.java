/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/stringifier/StringifierRegistryIniter.java,v 1.6 2005/11/15 20:21:50 llc Exp $
 * $Revision: 1.6 $
 * $Date: 2005/11/15 20:21:50 $
 */
 
package com.sun.cli.jmxcmd.util.jmx.stringifier;

import javax.management.*;
import javax.management.modelmbean.*;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;

import com.sun.cli.jmxcmd.util.jmx.stringifier.*;


import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.Statistic;
import com.sun.cli.jmxcmd.util.j2ee.stringifier.*;

import com.sun.cli.jcmd.util.stringifier.StringifierRegistry;

/**
	Registers all included stringifiers with the default registry.
 */
public class StringifierRegistryIniter extends
	com.sun.cli.jcmd.util.stringifier.StringifierRegistryIniterImpl
{
		public
	StringifierRegistryIniter( StringifierRegistry registry )
	{
		super( registry );
		
		add( ObjectName.class, ObjectNameStringifier.DEFAULT );
		add( MBeanInfo.class, MBeanInfoStringifier.DEFAULT );
		add( ModelMBeanInfo.class, ModelMBeanInfoStringifier.DEFAULT );
		
		add( MBeanOperationInfo.class, MBeanOperationInfoStringifier.DEFAULT );
		add( ModelMBeanOperationInfo.class, ModelMBeanOperationInfoStringifier.DEFAULT );
		
		add( MBeanAttributeInfo.class, MBeanAttributeInfoStringifier.DEFAULT );
		add( ModelMBeanAttributeInfo.class, ModelMBeanAttributeInfoStringifier.DEFAULT );
		
		add( MBeanParameterInfo.class, MBeanParameterInfoStringifier.DEFAULT );
		
		add( MBeanNotificationInfo.class, MBeanNotificationInfoStringifier.DEFAULT );
		add( ModelMBeanNotificationInfo.class, ModelMBeanNotificationInfoStringifier.DEFAULT );
		
		add( MBeanConstructorInfo.class, MBeanConstructorInfoStringifier.DEFAULT );
		add( ModelMBeanConstructorInfo.class, ModelMBeanConstructorInfoStringifier.DEFAULT );
		
		add( Attribute.class, AttributeStringifier.DEFAULT );
		add( AttributeList.class, AttributeListStringifier.DEFAULT );
		
		add( Notification.class, NotificationStringifier.DEFAULT );
		add( AttributeChangeNotification.class, AttributeChangeNotificationStringifier.DEFAULT );
		add( MBeanServerNotification.class, MBeanServerNotificationStringifier.DEFAULT );
		
		
		add( CompositeData.class, CompositeDataStringifier.DEFAULT );
		add( CompositeDataSupport.class, CompositeDataStringifier.DEFAULT );
		add( TabularData.class, TabularDataStringifier.DEFAULT );
		add( TabularDataSupport.class, TabularDataStringifier.DEFAULT );
		
		try
		{
		    add( Stats.class, StatsStringifier.DEFAULT );
		    add( Statistic.class, StatisticStringifier.DEFAULT );
		}
		catch( Throwable t )
		{
		}
	}
}



