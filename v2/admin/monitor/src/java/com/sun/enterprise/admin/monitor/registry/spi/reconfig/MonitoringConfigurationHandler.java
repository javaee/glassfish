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

/* MonitoringConfigurationHandler.java
 * $Id: MonitoringConfigurationHandler.java,v 1.3 2005/12/25 03:43:40 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:43:40 $
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

package com.sun.enterprise.admin.monitor.registry.spi.reconfig;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevel;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevelListener;
import com.sun.enterprise.admin.monitor.registry.MonitoredObjectType;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import java.util.logging.Logger;
//old way of getting the config information
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.config.ConfigContext;
//old way of getting the config information

import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.util.i18n.StringManager;
/**
 * @author  <a href="mailto:Kedar.Mhaswade@sun.com">Kedar Mhaswade</a>
 * @since S1AS8.0
 * @version $Revision: 1.3 $
 */
public class MonitoringConfigurationHandler {
	
	private static Logger logger = Logger.getLogger(AdminConstants.kLoggerName);
	private static StringManager sm = StringManager.getManager(MonitoringConfigurationHandler.class);
	private MonitoringConfigurationHandler() {
	}
	
	public static MonitoringLevel getLevel(MonitoredObjectType objType) {
		MonitoringLevel level = null;
		if (objType == MonitoredObjectType.APPLICATION	||
		objType == MonitoredObjectType.EJBMODULE	||
		objType == MonitoredObjectType.ROOT		||
		objType == MonitoredObjectType.STANDALONE_EJBMODULE ||
		objType == MonitoredObjectType.STANDALONE_WEBMODULE)
			level = MonitoringLevel.HIGH; //actually does not matter
		else if (objType == MonitoredObjectType.ENTITY_BEAN	||
		objType == MonitoredObjectType.BEAN_CACHE	||
		objType == MonitoredObjectType.BEAN_METHOD	||
		objType == MonitoredObjectType.BEAN_POOL	||
		objType == MonitoredObjectType.STATEFUL_BEAN	||
		objType == MonitoredObjectType.STATELESS_BEAN	||
		objType == MonitoredObjectType.MESSAGE_DRIVEN_BEAN ||
		objType == MonitoredObjectType.BEAN_METHODS ||
        objType == MonitoredObjectType.SESSION_STORE ||
        objType == MonitoredObjectType.TIMERS)
			level = getLevelFor(MonitoringConfigChangeListener.EJB_CONTAINER); // depends on config of ejb-container
		else if (objType == MonitoredObjectType.HTTP_LISTENER	||
		objType == MonitoredObjectType.VIRTUAL_SERVER ||
        objType == MonitoredObjectType.REQUEST ||
        objType == MonitoredObjectType.FILE_CACHE ||
        objType == MonitoredObjectType.PWC_THREAD_POOL ||
        objType == MonitoredObjectType.KEEP_ALIVE ||
        objType == MonitoredObjectType.DNS ||
        objType == MonitoredObjectType.CONNECTION_QUEUE ||
		objType == MonitoredObjectType.HTTP_SERVICE)
			return getLevelFor(MonitoringConfigChangeListener.HTTP_SERVICE); // depends on config of http-service
		else if (objType == MonitoredObjectType.SERVLET)
			level = getLevelFor(MonitoringConfigChangeListener.WEB_CONTAINER); // depends on config of web-container
		else if (objType == MonitoredObjectType.WEBMODULE)
			level = getLevelFor(MonitoringConfigChangeListener.WEB_CONTAINER); // depends on config of web-container
		else if (objType == MonitoredObjectType.CONNECTOR_CONN_POOL ||
                 objType == MonitoredObjectType.CONNECTOR_WORKMGMT ||
                 objType == MonitoredObjectType.JMS_SERVICE)
			level = getLevelFor(MonitoringConfigChangeListener.CONNECTOR_SERVICE); // depends on config connector-service
		else if (objType == MonitoredObjectType.JDBC_CONN_POOL)
			level = getLevelFor(MonitoringConfigChangeListener.JDBC_CONN_POOL); // depends on config of jdbc-pool
		else if (objType == MonitoredObjectType.JVM)
			level = getLevelFor(MonitoringConfigChangeListener.JVM);
		else if (objType == MonitoredObjectType.ORB ||
		objType == MonitoredObjectType.CONNECTION_MANAGERS ||
		objType == MonitoredObjectType.CONNECTION_MANAGER)
			level = getLevelFor(MonitoringConfigChangeListener.ORB); // depends on config of orb
		else if (objType == MonitoredObjectType.THREAD_POOL)
			level = getLevelFor(MonitoringConfigChangeListener.THREAD_POOL); // depends on config of threa-pool
		else if (objType == MonitoredObjectType.TRANSACTION_SERVICE)
			level = getLevelFor(MonitoringConfigChangeListener.TRANSACTION_SERVICE); // depends on config
        else if (objType == MonitoredObjectType.JNDI)
			level = getLevelFor(MonitoringConfigChangeListener.JNDI);
        else if (objType == MonitoredObjectType.WEBSERVICE_ENDPOINT)
			level =
            getLevelFor(MonitoringConfigChangeListener.WEBSERVICE_ENDPOINT);
		else
			logger.finer("Received  a MonitoringObjectType: " + objType + " for which there is no configuration in domain.xml - Returning NULL");
		return ( level );
	}

	public static boolean shouldRegisterMBean(MonitoredObjectType type) {
		final MonitoringLevel levelFromConfig = getLevel(type);

		return ( levelFromConfig == MonitoringLevel.LOW || levelFromConfig == MonitoringLevel.HIGH );
	}
	/** 
	 * Gets the MonitoringLevel the "old way" i.e. from Config Beans. This will
	 * have to change.
	 */
	private static MonitoringLevel getLevelFor(String name) {
		if (MonitoringConfigChangeListener.EJB_CONTAINER.equals(name))
			return ( ConfigGetter.getEjbContainerLevel() );
		else if (MonitoringConfigChangeListener.WEB_CONTAINER.equals(name))
			return ( ConfigGetter.getWebContainerLevel() );
		else if (MonitoringConfigChangeListener.HTTP_SERVICE.equals(name))
			return ( ConfigGetter.getHttpServiceLevel() );
		else if (MonitoringConfigChangeListener.TRANSACTION_SERVICE.equals(name))
			return ( ConfigGetter.getTransactionServiceLevel() );
		else if (MonitoringConfigChangeListener.ORB.equals(name))
			return ( ConfigGetter.getOrbLevel() );
		else if (MonitoringConfigChangeListener.THREAD_POOL.equals(name))
			return ( ConfigGetter.getThreadPoolLevel() );
		else if (MonitoringConfigChangeListener.JDBC_CONN_POOL.equals(name))
			return ( ConfigGetter.getJdbcConnectionPoolLevel() );
		else if (MonitoringConfigChangeListener.CONNECTOR_CONN_POOL.equals(name))
			return ConfigGetter.getConnectorServiceLevel();
        else if (MonitoringConfigChangeListener.CONNECTOR_SERVICE.equals(name))
			return ConfigGetter.getConnectorServiceLevel();
        else if (MonitoringConfigChangeListener.JMS_SERVICE.equals(name))
			return ConfigGetter.getConnectorServiceLevel();
        else if (MonitoringConfigChangeListener.JVM.equals(name))
            return ConfigGetter.getJvmLevel();
        else if (MonitoringConfigChangeListener.WEBSERVICE_ENDPOINT.equals(name)) {
            // XXX read the config for this particular web service endpoint
            // and return its monitoring level.
            return MonitoringLevel.LOW;
        }
		logger.finer("No configuration in domain.xml for the string: " + name);
		return ( null );
	}
	
	/* Remove this entire inner class later - when (and if) we don't get the config from ConfigBeans */
	private static final class ConfigGetter {
		private static com.sun.enterprise.config.serverbeans.Config getCfg() {
			try {
                AdminEvent event = 
                    MonitoringThreadContext.getEventFromThreadLocal();
                ConfigContext ctx = null;
                if (event != null) {
                    ctx = event.getConfigContext();
                } else {
                    ctx=ApplicationServer.getServerContext().getConfigContext();
                }
				final com.sun.enterprise.config.serverbeans.Config cfg = 
                                        ServerBeansFactory.getConfigBean(ctx);
				return ( cfg );
			}
			catch(Exception e) {
				return ( null );
				//squelch it?
			}
		}
		static MonitoringLevel string2Level(String s) {
			final MonitoringLevel l = MonitoringLevel.instance(s);
			if (s == null) {
				final String msg = sm.getString("mch.invalid_monitoring_level", s);
				throw new RuntimeException(msg);
			}
			return ( l );
		}
		static MonitoringLevel getEjbContainerLevel() {
			final String l = getCfg().getMonitoringService().getModuleMonitoringLevels().getEjbContainer();
			logger.finer("EjbContainer monitoring-level = " + l);
			return ( string2Level(l) );
		}
		static MonitoringLevel getWebContainerLevel() {
			final String l = getCfg().getMonitoringService().getModuleMonitoringLevels().getWebContainer();
			logger.finer("WebContainer monitoring-level = " + l);
			return ( string2Level(l) );
		}
		static MonitoringLevel getJdbcConnectionPoolLevel() {
			final String l = getCfg().getMonitoringService().getModuleMonitoringLevels().getJdbcConnectionPool();
			logger.finer("JdbcConnectionPoolLevel monitoring-level = " + l);
			return ( string2Level(l) );
		}
		static MonitoringLevel getConnectorConnectionPoolLevel() {
			final String l = getCfg().getMonitoringService().getModuleMonitoringLevels().getConnectorConnectionPool();
			logger.finer("ConnectorConnectionPoolLevel monitoring-level = " + l);
			return ( string2Level(l) );
		}
		static MonitoringLevel getTransactionServiceLevel() {
			final String l = getCfg().getMonitoringService().getModuleMonitoringLevels().getTransactionService();
			logger.finer("TransactionService monitoring-level = " + l);
			return ( string2Level(l) );
		}
		static MonitoringLevel getHttpServiceLevel() {
			final String l = getCfg().getMonitoringService().getModuleMonitoringLevels().getHttpService();
			logger.finer("HttpService monitoring-level = " + l);
			return ( string2Level(l) );
		}
		static MonitoringLevel getOrbLevel() {
			final String l = getCfg().getMonitoringService().getModuleMonitoringLevels().getOrb();
			logger.finer("Orb monitoring-level = " + l);
			return ( string2Level(l) );
		}
		static MonitoringLevel getThreadPoolLevel() {
			final String l = getCfg().getMonitoringService().getModuleMonitoringLevels().getThreadPool(); 
			logger.finer("Thread pool monitoring-level = " + l);
			return ( string2Level(l) );
		}
        static MonitoringLevel getConnectorServiceLevel() {
			final String l = getCfg().getMonitoringService().getModuleMonitoringLevels().getConnectorService();
			logger.finer("ConnectorServiceLevel monitoring-level = " + l);
			return ( string2Level(l) );
		}
        static MonitoringLevel getJvmLevel() {
            final String l = getCfg().getMonitoringService().getModuleMonitoringLevels().getJvm();
			logger.finer("Jvm monitoring-level = " + l);
			return ( string2Level(l) );
        }
	}
	/* Remove this entire inner class later - when (and if) we don't get the config from ConfigBeans */
}
