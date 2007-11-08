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

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
 
/*
 */

package com.sun.enterprise.management.support;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import javax.management.ObjectName;

import com.sun.appserv.management.base.XTypes;

import com.sun.enterprise.admin.monitor.registry.MonitoredObjectType;
 
/**
	Maps an AMX j2eeType to/from and "old" (8.0) monitor type.
	
	See {@link com.sun.appserv.management.base.XTypes}
 */
public final class OldMonitorTypes extends OldTypesBase
{
    private static OldMonitorTypes  INSTANCE;
    
		private
    OldMonitorTypes()
	{
		super();
	}
	
		public static synchronized OldMonitorTypes
	getInstance()
	{
	    if ( INSTANCE == null )
	    {
	        INSTANCE	= new OldMonitorTypes();
	    }

		return( INSTANCE );
	}
	
	/**
		These are delegates that require a config name only, no other keys
		other than category=monitor.
		
		If this list is updated, be sure to update the unit test
		com.sun.enterprise.management.monitor.ComSunAppservMonitorTest.
	 */
		void
	initMap()
	{
		/* monitoring types */
		add(  XTypes.SERVER_ROOT_MONITOR, "root");
		add(  XTypes.SERVER_ROOT_MONITOR, "server");
		add(  XTypes.JVM_MONITOR, "jvm");
		
		add(  XTypes.APPLICATION_MONITOR, "application");
		add(  XTypes.EJB_MODULE_MONITOR, "ejb-module");
		add(  XTypes.EJB_MODULE_MONITOR, "standalone-ejb-module");
		
		add(  XTypes.STATEFUL_SESSION_BEAN_MONITOR, MonitoredObjectType.STATEFUL_BEAN.toString() );
		add(  XTypes.STATELESS_SESSION_BEAN_MONITOR, MonitoredObjectType.STATELESS_BEAN.toString() );
		add(  XTypes.ENTITY_BEAN_MONITOR, MonitoredObjectType.ENTITY_BEAN.toString() );
		add(  XTypes.MESSAGE_DRIVEN_BEAN_MONITOR, MonitoredObjectType.MESSAGE_DRIVEN_BEAN.toString() );
		
		add(  XTypes.BEAN_POOL_MONITOR, "bean-pool");
		add(  XTypes.BEAN_CACHE_MONITOR, "bean-cache");
		add(  XTypes.BEAN_METHOD_MONITOR, "bean-method");
		add(  XTypes.SERVLET_MONITOR, "servlet");
		add(  XTypes.HTTP_SERVICE_MONITOR, "http-service");
		add(  XTypes.FILE_CACHE_MONITOR, "file-cache");

		add(  XTypes.HTTP_SERVICE_VIRTUAL_SERVER_MONITOR, "virtual-server");
		add(  XTypes.WEB_MODULE_VIRTUAL_SERVER_MONITOR, "webmodule-virtual-server");

		add(  XTypes.HTTP_LISTENER_MONITOR, "http-listener");
		add(  XTypes.TRANSACTION_SERVICE_MONITOR, "transaction-service");
		add(  XTypes.THREAD_POOL_MONITOR, "thread-pool");
		add(  XTypes.CONNECTION_MANAGER_MONITOR, "connection-manager");
		add(  XTypes.JDBC_CONNECTION_POOL_MONITOR, "jdbc-connection-pool");
		add(  XTypes.CONNECTOR_CONNECTION_POOL_MONITOR, "connector-connection-pool");
		
		add(  XTypes.KEEP_ALIVE_MONITOR, "keep-alive");
		add(  XTypes.CONNECTION_QUEUE_MONITOR, "connection-queue");

		add(  XTypes.WEBSERVICE_ENDPOINT_MONITOR, "webservice-endpoint");
	}

}








