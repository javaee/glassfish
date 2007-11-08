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

/* StatsHolderMBean.java
 * $Id: StatsHolderMBean.java,v 1.3 2005/12/25 03:43:36 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:43:36 $
 * Indentation Information:
 * 0. Please (try to) preserve these settings.
 * 1. Tabs are preferred over spaces.
 * 2. In vi/vim -
 *		:set tabstop=4 :set shiftwidth=4 :set softtabstop=4
 * 3. In S1 Studio -
 *		1. Tools->Options->Editor Settings->Java Editor->Tab Size = 4
 *		2. Tools->Options->Indentation Engines->Java Indentation Engine->Expand Tabs to Spaces = False.
 *		3. Tools->Options->Indentation Engines->Java Indentation Engine->Number of Spaces per Tab = 4.
 */

package com.sun.enterprise.admin.monitor.registry.spi;


/**
 * Provides the management interface for a StatsHolder that represents a 
 * node in the arbitrary monitoring node hierarchy. It is purposely separated
 * from the {@link javax.management.DynamicMBean} because the implementing class
 * may use a Standard MBean instead, to get various attributes.
 * @author  <a href="mailto:Kedar.Mhaswade@sun.com">Kedar Mhaswade</a>
 * @since S1AS8.0
 * @version $Revision: 1.3 $
 */
public interface StatsHolderMBean {
	/**
	 * Returns the {@link javax.management.ObjectName}s of all the children of associated 
	 * {@link com.sun.enterprise.admin.monitor.registry.StatsHolder} in an
	 * arbitrary hierarchy. A management application should be able to build a
	 * hierarchical view using on this method. Implementing class should always
	 * return an empty array in case there are no children.
	 *
	 * @return		an array of ObjectNames representing this StatsHolder's children
	 */
	javax.management.ObjectName[] getChildren();
	
	/**
	 * Returns the name of the StatsHolder node, that the MBean is exposing
	 * This usually refers to a specific name such as HelloWorldServlet or
	 * the name of the MonitoredObjectType such as TransactionService, 
	 * when a specific name does not exist. Does not return a null.
	 *
	 * @see com.sun.enterprise.admin.monitor.registry.MonitoredObjectType
	 * @return name of the node
	 */
	String getName();
	
	/**
	 * Returns the type of the StatsHolder Node. This refers to one of several
	 * monitored object types, such as ejb-methods, bean-pool.
	 * Does not return a null.
	 *
	 * @see com.sun.enterprise.admin.monitor.registry.MonitoredObjectType
	 * @return type of the node
	 */
	String getType();
		
	/**
	 * Returns the result of executing the getXXX methods on the associated
	 * Stats object. The result is an array of serialized Statistic objects.
	 *
	 * @return array of Statistic objects
	 */
	javax.management.j2ee.statistics.Statistic[] getStatistics();
	
	/**
	 * Returns an array containing the names of the Statistics, for a given
	 * Stats object
	 *
	 * @return  array of statistic names
	 */
	String[] getStatisticNames();
	
}
