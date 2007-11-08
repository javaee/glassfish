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
 * ConfigChangeListener.java
 * $Id: MonitoringConfigChangeListener.java,v 1.3 2005/12/25 03:43:40 tcfujii Exp $
 * $Date: 2005/12/25 03:43:40 $
 * $Revision: 1.3 $
 */


package com.sun.enterprise.admin.monitor.registry.spi.reconfig;
import com.sun.enterprise.admin.event.MonitoringLevelChangeEvent;
import com.sun.enterprise.admin.event.MonitoringLevelChangeEventListener;
import com.sun.enterprise.admin.event.AdminEventListenerException;

import com.sun.enterprise.admin.monitor.registry.MonitoringLevel;
import com.sun.enterprise.admin.monitor.registry.MonitoredObjectType;
/** Provides for the listener that listens for changes done by administrative interface and
 * notifies them dynamically to core systems.
 * This is the bridge between the notification subsystem and monitoring subsystem.
 * @author  <a href="mailto:Kedar.Mhaswade@sun.com">Kedar Mhaswade</a>
 * @since S1AS8.0
 * @version $Revision: 1.3 $
 */
public class MonitoringConfigChangeListener implements MonitoringLevelChangeEventListener {
	
	private final ChangeHandler delegate;
	public MonitoringConfigChangeListener(ChangeHandler delegate) {
		this.delegate = delegate;
	}
	public synchronized void monitoringLevelChanged(
            MonitoringLevelChangeEvent event) 
            throws AdminEventListenerException {

		//This will be done soon once I start receiving these events. These events have all info.
		//delegate.handleChange (type, from, to);
		final String component	= event.getComponentName();
		final String from		= event.getOldMonitoringLevel();
		final String to			= event.getNewMonitoringLevel();
		
        // sets the event to thread context 
        MonitoringThreadContext.setEventToThreadLocal(event);

		handleAllAffectedTypes(component, from, to);

        // removes the event from thread context
        MonitoringThreadContext.removeEventFromThreadLocal();
	}
	
	private void handleAllAffectedTypes(String component, String from, String to) {
		final MonitoredObjectType[]	types	= name2Types(component);
		final MonitoringLevel		oLevel	= MonitoringLevel.instance(from);
		final MonitoringLevel		nLevel	= MonitoringLevel.instance(to);
		for (int i = 0 ; i < types.length ; i++) {
			delegate.handleChange(types[i], oLevel, nLevel);
		}
	}
	
	private MonitoredObjectType[] name2Types(String component) {
		MonitoredObjectType[] types = new MonitoredObjectType[0]; //empty array
		if (EJB_CONTAINER.equals(component)) {
			types = MonitoredObjectType.EJB_TYPES;
		}
		else if (WEB_CONTAINER.equals(component)) {
			types = new MonitoredObjectType[1];
			types[0] = MonitoredObjectType.SERVLET;
		}
		else if (THREAD_POOL.equals(component)) {
			types = new MonitoredObjectType[1];
			types[0] = MonitoredObjectType.THREAD_POOL;
		}
		else if (ORB.equals(component)) {
			types = new MonitoredObjectType[1];
			types[0] = MonitoredObjectType.ORB;
		}
		else if (HTTP_SERVICE.equals(component)) {
			types = MonitoredObjectType.HTTP_SERVICE_TYPES;
		}
		else if (TRANSACTION_SERVICE.equals(component)) {
			types = new MonitoredObjectType[1];
			types[0] = MonitoredObjectType.TRANSACTION_SERVICE;
		}
		else if (JDBC_CONN_POOL.equals(component)) {
			types = new MonitoredObjectType[1];
			types[0] = MonitoredObjectType.JDBC_CONN_POOL;
		}
        else if (CONNECTOR_SERVICE.equals(component)) {
			types = new MonitoredObjectType[1];
			types[0] = MonitoredObjectType.CONNECTOR_SERVICE;
		}
        else if (JVM.equals(component)) {
			types = new MonitoredObjectType[1];
			types[0] = MonitoredObjectType.JVM;
		} else if (WEBSERVICE_ENDPOINT.equals(component)) {
			types = new MonitoredObjectType[1];
			types[0] = MonitoredObjectType.WEBSERVICE_ENDPOINT;
        }
        /**
         * connector-service level will be modified whenever
         * the connector-connection-pool or jms-service is
         * modified, in addition to changes made directly to the
         * connector-service. It therefore makes sense to handle the
         * event only once i.e for connector-service and let the
         * others go unhandled.
        else if (CONNECTOR_CONN_POOL.equals(component)) {
			types = new MonitoredObjectType[1];
			types[0] = MonitoredObjectType.CONNECTOR_SERVICE;
		}
        else if (JMS_SERVICE.equals(component)) {
			types = new MonitoredObjectType[1];
			types[0] = MonitoredObjectType.CONNECTOR_SERVICE;
		}
         */
		return ( types );
	}
	
	/**
	 * All fields actually refer to their counterparts in domain.xml. Actually
	 * the ServerTags class is to be reused, but it is auto-generated and
	 * there are some optimizations made to it, hence this repetition.
	 */ 
	public static final String EJB_CONTAINER			= "ejb-container";
	public static final String WEB_CONTAINER			= "web-container";
	public static final String THREAD_POOL				= "thread-pool";
	public static final String ORB						= "orb";
	public static final String TRANSACTION_SERVICE		= "transaction-service";
	public static final String HTTP_SERVICE				= "http-service";
	public static final String JDBC_CONN_POOL			= "jdbc-connection-pool";
	public static final String CONNECTOR_CONN_POOL		= "connector-connection-pool";
    public static final String CONNECTOR_SERVICE		= "connector-service";
    public static final String JMS_SERVICE		        = "jms-service";
    public static final String JNDI     		        = "jndi";
    public static final String JVM     		            = "jvm";
    public static final String WEBSERVICE_ENDPOINT      = "webservice_endpoint";

}
