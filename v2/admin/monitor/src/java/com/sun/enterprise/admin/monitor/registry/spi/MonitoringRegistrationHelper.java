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

/* MonitoringRegistrationHelper.java
 * $Id: MonitoringRegistrationHelper.java,v 1.13 2007/04/24 20:18:02 sirajg Exp $
 * $Revision: 1.13 $
 * $Date: 2007/04/24 20:18:02 $
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

import com.sun.enterprise.admin.monitor.registry.*;
import com.sun.enterprise.admin.monitor.stats.*;
import javax.management.*;
import javax.management.j2ee.statistics.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.enterprise.admin.common.constant.AdminConstants; // for logger name
import com.sun.enterprise.util.i18n.StringManager;
// for config and dynamic reconfig
import com.sun.enterprise.admin.event.AdminEventListenerRegistry;
import com.sun.enterprise.admin.monitor.jndi.JndiMBeanManager;
import com.sun.enterprise.admin.monitor.registry.spi.reconfig.MonitoringConfigurationHandler;
import com.sun.enterprise.admin.monitor.registry.spi.reconfig.DynamicReconfigurator;
import com.sun.enterprise.admin.monitor.registry.spi.reconfig.MonitoringConfigChangeListener;
import com.sun.enterprise.server.stats.JVMStatsImpl;
/**
 * Enables components to register their Stats implementation for monitoring.
 * This implementation provides a way for a JSR77 Managed Component's monitoring 
 * statistics to be presented through JMX's API by means of a Dynamic MBean 
 * created by introspecting the Stats object and registered with the MBeanServer.
 * Also provides ability to unregister and to check if a component is registered 
 * successfully. 
 * As this is a registration facilitator for all components, this is a Singleton
 * @author Shreedhar Ganapathy
 * @author Kedar Mhaswade
 */
public class MonitoringRegistrationHelper implements MonitoringRegistry {
    private static final Logger logger = Logger.getLogger(AdminConstants.kLoggerName);
	private static final StringManager sm = StringManager.getManager(MonitoringRegistrationHelper.class);
    private final MBeanServer mbeanserver;
    Vector listeners = new Vector();
	private final ValueListMap objTypeListeners;
    private static MonitoringRegistrationHelper mrh = new MonitoringRegistrationHelper();
	private final StatsHolder rootStatsHolder = 
		new StatsHolderImpl(MonitoredObjectType.ROOT.getTypeName(), MonitoredObjectType.ROOT);
    
    private MonitoringRegistrationHelper(){
        mbeanserver = getMBeanServer();
		objTypeListeners = new ValueListMap();
		initialize(); //initializes the basic tree.
		AdminEventListenerRegistry.addMonitoringLevelChangeEventListener(
			new MonitoringConfigChangeListener(new DynamicReconfigurator(objTypeListeners)));
    }
    
    /** Returns a created or new instance of MonitoringRegistrationHelper.
     * Initializes MBeanServer to which a DynamicMBean will be registered for 
     * each component registering its Stats object.
     */
    public static MonitoringRegistrationHelper getInstance() {
        return mrh;
    }
    
    private MBeanServer getMBeanServer() {
        MBeanServer server=null;
        ArrayList servers = MBeanServerFactory.findMBeanServer(null);
        if(!servers.isEmpty()){
            server = (MBeanServer)servers.get(0);
        }
        else {
			final String msg = sm.getString("gen.no_mbs");
            throw new NullPointerException(msg);
		}
        return server;
    }
   
    public void registerEJBCacheStats(EJBCacheStats cs,  MonitoredObjectType ejbType, String ejb, 
	String module, String app, MonitoringLevelListener listener) 
	throws MonitoringRegistrationException {
        StatsHolder currChild	= null;
        String dottedName		= null;
		final String name		= MonitoredObjectType.BEAN_CACHE.getTypeName();

		if (cs == null || module == null || ejb == null)  // app can be null
			throw new IllegalArgumentException("MRH:EjbCacheStats, module, ejb can't be null");
		addMonitoringLevelListener(listener); //listener can be null
        currChild = getEjb(app, module, ejbType, ejb);
        currChild = currChild.addChild(name, MonitoredObjectType.BEAN_CACHE);
        currChild.setStats(cs);
		currChild.setStatsClass(com.sun.enterprise.admin.monitor.stats.EJBCacheStats.class);
		final ObjectName on = MonitoringObjectNames.getEjbCacheObjectName( ejbType, ejb, module, app);
		currChild.setObjectName(on);
		currChild.setDottedName(DottedNameFactory.getEJBCacheDottedName(app, module,ejb));
		if (shouldRegisterMBean(MonitoredObjectType.BEAN_CACHE))
			currChild.registerMBean();
	}

    public void unregisterEJBCacheStats(MonitoredObjectType ejbType, String ejb, String module, String app)
    throws MonitoringRegistrationException {

        StatsHolder currChild	= null;
        StatsHolder cacheNode	= null;
		final String name		= MonitoredObjectType.BEAN_CACHE.getTypeName();
        currChild = getEjb(app, module, ejbType, ejb);
		assert (currChild != null) : "MRH:unregisterEJBCacheStats: method registerEJBCacheStats was never called";
        cacheNode = currChild.getChild(name);
        cacheNode.unregisterMBean();
        currChild.removeChild(name);
    }

    
    public void registerEJBPoolStats(EJBPoolStats ps, MonitoredObjectType ejbType, String ejb, 
	String module, String app, MonitoringLevelListener listener) 
    throws MonitoringRegistrationException {

		StatsHolder currChild	= null;
        String dottedName		= null;
		final String name		= MonitoredObjectType.BEAN_POOL.getTypeName();
		
		if (ps == null || module == null || ejb == null)  // app can be null
			throw new IllegalArgumentException("MRH:EjbPoolStats, module, ejb can't be null");
		
		addMonitoringLevelListener(listener); //listener can be null
        currChild = getEjb(app, module, ejbType, ejb);
		currChild = currChild.addChild(name, MonitoredObjectType.BEAN_POOL);
        currChild.setStats(ps);
        currChild.setStatsClass(com.sun.enterprise.admin.monitor.stats.EJBPoolStats.class);
		final ObjectName on = MonitoringObjectNames.getEjbPoolObjectName( ejbType, ejb, module, app);
		currChild.setObjectName(on);
		currChild.setDottedName(DottedNameFactory.getEJBPoolDottedName(app, module,ejb));
		if (shouldRegisterMBean(MonitoredObjectType.BEAN_POOL))
			currChild.registerMBean();
	}

    public void unregisterEJBPoolStats(MonitoredObjectType ejbType, String ejb, String module, String app) 
	throws MonitoringRegistrationException {

        StatsHolder currChild	= null;
        StatsHolder poolNode = null;
		final String name		= MonitoredObjectType.BEAN_POOL.getTypeName();
        currChild = getEjb(app, module, ejbType, ejb);
		assert (currChild != null) : "MRH:unregisterEJBPoolStats: method registerEJBPoolStats was never called";
        poolNode = currChild.getChild(name);
        poolNode.unregisterMBean();
        currChild.removeChild(name);
    }    

    public void registerEJBMethodStats(EJBMethodStats ms, String method, MonitoredObjectType ejbType,
	String ejb, String module, String app, MonitoringLevelListener listener) 
    throws MonitoringRegistrationException {

		StatsHolder currChild	= null;
        String dottedName		= null;
		
		if (ms == null || module == null || ejb == null)  // app can be null
			throw new IllegalArgumentException("MRH:EjbMethodStats, module, ejb can't be null");
		
		addMonitoringLevelListener(listener); //listener can be null
        currChild = getEjbMethods( app, module, ejbType, ejb);
		currChild = currChild.addChild(method, MonitoredObjectType.BEAN_METHOD);
        currChild.setStats(ms);
		currChild.setStatsClass(com.sun.enterprise.admin.monitor.stats.EJBMethodStats.class);
		final ObjectName on = MonitoringObjectNames.getEjbMethodObjectName(method, ejbType, ejb, module, app);
		currChild.setObjectName(on);
		currChild.setDottedName(DottedNameFactory.getEJBMethodDottedName(app, module, ejb, method));
		if (shouldRegisterMBean(MonitoredObjectType.BEAN_METHOD))
			currChild.registerMBean();
	}

    public void unregisterEJBMethodStats(String method, MonitoredObjectType ejbType, String ejb, 
	String module, String app) throws MonitoringRegistrationException {

		if ( method == null || ejb == null || module == null) 
			throw new IllegalArgumentException("MRH: unregister method called with null arguments");
        StatsHolder currChild	= null;
        StatsHolder methodNode	= null;
        currChild = getEjbMethods(app, module, ejbType, ejb);
		assert (currChild != null) : "Serious: EjbMethodsNode is not created at all";
        methodNode = currChild.getChild(method);
		assert (methodNode != null) : "MRH:unregisterEJBMethodStats - methodNode null";
        methodNode.unregisterMBean();
        currChild.removeChild(method);
    }
    
	private void registerAnyEjbStats(EJBStats es, MonitoredObjectType ejbType,
	String ejb, String module, String app, MonitoringLevelListener listener) 
	throws MonitoringRegistrationException {
		StatsHolder currChild	= null;
        String dottedName		= null;
		
		if (es == null || module == null || ejb == null)  // app can be null
			throw new IllegalArgumentException("MRH:registerEntityBeanStats, module, ejb can't be null");
		
		addMonitoringLevelListener(listener); //listener can be null
        currChild = getEjb(app, module, ejbType, ejb);
		assert (currChild != null) : "MRH:registerEntityBeanStats - Serious: EjbNode is null";
        currChild.setStats(es);
		this.setEjbStatsClass(currChild, ejbType);
		currChild.setType(ejbType); // this is the real type
		
		final ObjectName on = MonitoringObjectNames.getEjbObjectName(ejbType, ejb, module, app);
		currChild.setObjectName(on);
		currChild.setDottedName(DottedNameFactory.getEJBDottedName(app, module, ejb));		
		if (shouldRegisterMBean(ejbType))
			currChild.registerMBean();
	}
	private void setEjbStatsClass(StatsHolder s, MonitoredObjectType t) {
		assert (t == MonitoredObjectType.ENTITY_BEAN ||
				t == MonitoredObjectType.STATEFUL_BEAN ||
				t == MonitoredObjectType.STATELESS_BEAN ||
				t == MonitoredObjectType.MESSAGE_DRIVEN_BEAN) : "Invalid Ejb Type: " + t.getTypeName();
		if (t == MonitoredObjectType.ENTITY_BEAN)
			s.setStatsClass(javax.management.j2ee.statistics.EntityBeanStats.class);
		else if (t == MonitoredObjectType.STATEFUL_BEAN)
			s.setStatsClass(javax.management.j2ee.statistics.StatefulSessionBeanStats.class);
		else if (t == MonitoredObjectType.STATELESS_BEAN)
			s.setStatsClass(javax.management.j2ee.statistics.StatelessSessionBeanStats.class);
		else
			s.setStatsClass(javax.management.j2ee.statistics.MessageDrivenBeanStats.class);
	}
	
	private void unregisterAnyEjbStats(String ejb, String module, String app) 
	throws MonitoringRegistrationException {
		if (module == null || ejb == null) //app can be null
			throw new IllegalArgumentException("MRH: UnregisterEntityBeanStats, method and ejb name should be non-null");
        final StatsHolder ejbRoot = getEjbRootNode(app, module);
		assert (ejbRoot != null) : "MRH:unregisterEntityBeanStats: method registerEntityBeanStats was never called";
		final StatsHolder ejbNode = ejbRoot.getChild(ejb);
		assert (ejbNode != null) : "MRH:unregisterEntityBeanStats: null node received for ejb: " + ejb;
		ejbNode.removeAllChildren();
        ejbNode.unregisterMBean();
        ejbRoot.removeChild(ejb);
	}
	
    public void registerEntityBeanStats(EntityBeanStats es, String ejb, 
	String module, String app, MonitoringLevelListener listener) 
	throws MonitoringRegistrationException {
		this.registerAnyEjbStats(es, MonitoredObjectType.ENTITY_BEAN, ejb, module, app, listener);
    }
    
    public void unregisterEntityBeanStats(String ejb, String module,
	String app) throws MonitoringRegistrationException {
		this.unregisterAnyEjbStats(ejb, module, app);
    }

    public void registerStatefulSessionBeanStats(StatefulSessionBeanStats ss, 
    String ejb, String module, String app, MonitoringLevelListener listener) 
	throws MonitoringRegistrationException {
		this.registerAnyEjbStats(ss, MonitoredObjectType.STATEFUL_BEAN, ejb, module, app, listener);
    }

    public void unregisterStatefulSessionBeanStats(String ejb, String module, 
	String app) throws MonitoringRegistrationException {
		this.unregisterAnyEjbStats(ejb, module, app);
	}
    
    public void registerStatelessSessionBeanStats(StatelessSessionBeanStats ss, 
    String ejb, String module, String app, MonitoringLevelListener listener) 
	throws MonitoringRegistrationException {
		this.registerAnyEjbStats(ss, MonitoredObjectType.STATELESS_BEAN, ejb, module, app, listener);
    }

    public void unregisterStatelessSessionBeanStats(String ejb, String module, 
    String app) throws MonitoringRegistrationException {
		this.unregisterAnyEjbStats(ejb, module, app);
    }
   
    public void registerMessageDrivenBeanStats(MessageDrivenBeanStats ms, 
    String ejb, String module, String app, MonitoringLevelListener listener) 
	throws MonitoringRegistrationException {
		this.registerAnyEjbStats(ms, MonitoredObjectType.MESSAGE_DRIVEN_BEAN, ejb, module, app, listener);
    }
    
    public void unregisterMessageDrivenBeanStats(String ejb, String module, 
    String app) throws MonitoringRegistrationException {
		this.unregisterAnyEjbStats(ejb, module, app);
    }

	private void registerConnectionPoolStats(ConnectionPoolStats ps, String name,
	MonitoredObjectType type, MonitoringLevelListener listener) 
	throws MonitoringRegistrationException {
		assert (type == MonitoredObjectType.JDBC_CONN_POOL || type == MonitoredObjectType.CONNECTOR_CONN_POOL) : "MRH:registerConnectionPool - type is invalid";
		if (name == null || ps == null)
			throw new IllegalArgumentException("MRH:registerConnectionPoolStats - null name");
		final StatsHolder resourcesNode = rootStatsHolder.getChild(MonitoredObjectType.RESOURCES.getTypeName());
		assert (resourcesNode != null) : "MRH: registerConnectionPoolStats - Resources top-level node not registered";
		final StatsHolder added = resourcesNode.addChild(name, type);
		assert (added != null) : "Connection Pool Addtion Error: " + name;
		added.setStats(ps);
		if (type == MonitoredObjectType.JDBC_CONN_POOL)
			added.setStatsClass(com.sun.enterprise.admin.monitor.stats.JDBCConnectionPoolStats.class);
		else
			added.setStatsClass(com.sun.enterprise.admin.monitor.stats.ConnectorConnectionPoolStats.class);
		final ObjectName on = MonitoringObjectNames.getConnectionPoolObjectName(name, type);
		added.setObjectName(on);
		added.setDottedName(DottedNameFactory.getConnectionPoolDottedName(name, type.getTypeName()));
		if (shouldRegisterMBean(type))
			added.registerMBean();
	}
	private void unregisterConnectionPoolStats(String name)
	throws MonitoringRegistrationException {
		if (name == null)
			throw new IllegalArgumentException("MRH:unregisterConnectionPoolStats - null name");
		final StatsHolder resourcesNode = rootStatsHolder.getChild(MonitoredObjectType.RESOURCES.getTypeName());
		assert (resourcesNode != null) : "MRH: registerConnectionPoolStats - Resources top-level node not registered";
		final StatsHolder pool = resourcesNode.getChild(name);
		assert (pool != null) : "MRH:unregisterConnectionPoolStats -  null node - Serious";
		pool.unregisterMBean();
		resourcesNode.removeChild(name);
		//assert (pool == null) : "Now the reference should be null";
	}
	
    public void registerJDBCConnectionPoolStats(com.sun.enterprise.admin.monitor.stats.JDBCConnectionPoolStats ps, 
	String name, MonitoringLevelListener listener) throws MonitoringRegistrationException {
		this.registerConnectionPoolStats(ps, name, MonitoredObjectType.JDBC_CONN_POOL, listener);
    }
    
    public void unregisterJDBCConnectionPoolStats(String name) 
    throws MonitoringRegistrationException {
		this.unregisterConnectionPoolStats(name);
    }

    public void registerConnectorConnectionPoolStats(com.sun.enterprise.admin.monitor.stats.ConnectorConnectionPoolStats ps, String name, MonitoringLevelListener listener) throws MonitoringRegistrationException {
		this.registerConnectionPoolStats(ps, name, MonitoredObjectType.CONNECTOR_CONN_POOL, listener);
    }

    public void unregisterConnectorConnectionPoolStats(String name) throws MonitoringRegistrationException {                
		this.unregisterConnectionPoolStats(name);
    }
    
    public void registerOrbConnectionManagerStats(OrbConnectionManagerStats cms, 
	String name, MonitoringLevelListener listener) 
    throws MonitoringRegistrationException {
		if (cms == null || name == null)
			throw new IllegalArgumentException("MRH:registerOrbCMStats - null arguments");
		final StatsHolder orb = rootStatsHolder.getChild(MonitoredObjectType.ORB.getTypeName());
		final StatsHolder managers = orb.getChild(MonitoredObjectType.CONNECTION_MANAGERS.getTypeName());
		assert (managers != null) : "MRH:registerOrbConnectionManagerStats ORB Node not created yet";
		managers.setDottedName(DottedNameFactory.getConnectionManagersDottedName());
		final StatsHolder added = managers.addChild(name, MonitoredObjectType.CONNECTION_MANAGER);
		assert (added != null) : "Addition of Connection Manager failed: " + name;
		added.setStats(cms);
		added.setStatsClass(com.sun.enterprise.admin.monitor.stats.OrbConnectionManagerStats.class);
		final ObjectName on = MonitoringObjectNames.getOrbConnectionManagerObjectName(name);
		added.setObjectName(on);
		added.setDottedName(DottedNameFactory.getOrbConnectionManagerDottedName(name));
		if (shouldRegisterMBean(MonitoredObjectType.CONNECTION_MANAGER))
			added.registerMBean();
    }
    
    public void unregisterOrbConnectionManagerStats(String name) 
    throws MonitoringRegistrationException {
		final StatsHolder orb = rootStatsHolder.getChild(MonitoredObjectType.ORB.getTypeName());
		final StatsHolder managers = orb.getChild(MonitoredObjectType.CONNECTION_MANAGERS.getTypeName());
		assert (managers != null) : "MRH:registerOrbConnectionManagerStats ORB Node not created yet";
		final StatsHolder cm = managers.getChild(name);
		assert ( cm != null) : "Connection Manager to be removed is null";
		cm.unregisterMBean();
		managers.removeChild(name);
    }
    
    public void registerThreadPoolStats(ThreadPoolStats tps, String name, 
    MonitoringLevelListener listener) throws MonitoringRegistrationException {
		if (tps == null || name == null)
			throw new IllegalArgumentException("registerThreadPoolStats - null arguments");
		final StatsHolder pools = rootStatsHolder.getChild(MonitoredObjectType.THREAD_POOLS.getTypeName());
		assert (pools != null) : "ThreadPools Node not initialized";
		final StatsHolder added = pools.addChild(name, MonitoredObjectType.THREAD_POOL);
		assert (added != null) : "No matching thread pool: " + name;
		added.setStats(tps);
		added.setStatsClass(com.sun.enterprise.admin.monitor.stats.ThreadPoolStats.class);
		final ObjectName on = MonitoringObjectNames.getThreadPoolObjectName(name);
		added.setObjectName(on);
		added.setDottedName(DottedNameFactory.getThreadPoolDottedName(name));
		if (shouldRegisterMBean(MonitoredObjectType.THREAD_POOL))
			added.registerMBean();
    }
    
    public void unregisterThreadPoolStats(String name) throws MonitoringRegistrationException {
		final StatsHolder tps = rootStatsHolder.getChild(MonitoredObjectType.THREAD_POOLS.getTypeName());
		assert (tps != null) : "MRH:unregisterThreadPoolStats - null";
		final StatsHolder tp = tps.getChild(name);
		assert ( tp != null) : "Thread Pool to be removed is null";
		tp.unregisterMBean();
		tps.removeChild(name);
    }

    public void registerJTAStats(com.sun.enterprise.admin.monitor.stats.JTAStats js, 
    MonitoringLevelListener listener) throws MonitoringRegistrationException {
		if (js == null)
			throw new IllegalArgumentException("JTA - null stats provided");
		final StatsHolder tsNode = rootStatsHolder.getChild(MonitoredObjectType.TRANSACTION_SERVICE.getTypeName());
		assert (tsNode != null) : "MRH:registerJTAStats - Initialization failed";
		tsNode.setStats(js);
		tsNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.JTAStats.class);
		if(listener != null)
			registerMonitoringLevelListener(listener, MonitoredObjectType.TRANSACTION_SERVICE);
		if (shouldRegisterMBean(MonitoredObjectType.TRANSACTION_SERVICE))
			tsNode.registerMBean();
    }
    
    public void unregisterJTAStats() throws MonitoringRegistrationException {
		final String name = MonitoredObjectType.TRANSACTION_SERVICE.getTypeName();
		final StatsHolder ts = rootStatsHolder.getChild(name);
		assert (ts != null) : "Serious JTS is null";
		ts.unregisterMBean();
		rootStatsHolder.removeChild(name);
    }
    
    public void registerJVMStats(JVMStats js, MonitoringLevelListener listener) 
	throws MonitoringRegistrationException {
		if (js == null)
			throw new IllegalArgumentException("JVM  - null stats provided");
		final StatsHolder vmNode = rootStatsHolder.getChild(MonitoredObjectType.JVM.getTypeName());
		assert (vmNode != null) : "MRH:registerJVMStats - Initialization failed";
		vmNode.setStats(js);
		vmNode.setStatsClass(javax.management.j2ee.statistics.JVMStats.class);
		vmNode.registerMBean();
    }
    
    public void unregisterJVMStats() throws MonitoringRegistrationException {
		final String name = MonitoredObjectType.JVM.getTypeName();
		final StatsHolder js = rootStatsHolder.getChild(name);
		assert (js != null) : "Serious JVM is null";
		js.unregisterMBean();
		rootStatsHolder.removeChild(name);
    }    
    
    public void registerHttpListenerStats(HTTPListenerStats hs, String ls, 
	String vs, MonitoringLevelListener listener) throws MonitoringRegistrationException {
		if (hs == null || ls == null || vs == null)
			throw new IllegalArgumentException("MRH:registerHttpListenerStats - Null argunments");
		StatsHolder added = getRootVs(vs);
		added = added.addChild(ls, MonitoredObjectType.HTTP_LISTENER);
		assert (added != null) : "MRH: Listener is null - " + ls;
		added.setObjectName(MonitoringObjectNames.getHttpListenerObjectName(vs, ls));
		added.setDottedName(DottedNameFactory.getHttpListenerDottedName(ls, vs));
		added.setStats(hs);
		added.setStatsClass(com.sun.enterprise.admin.monitor.stats.HTTPListenerStats.class);
		if (shouldRegisterMBean(MonitoredObjectType.HTTP_LISTENER))
			added.registerMBean();
    }

	public void unregisterHttpListenerStats(String name, String vs) 
	throws MonitoringRegistrationException {
		if (name == null || vs == null)
			throw new IllegalArgumentException("Null args");
		final StatsHolder vsNode = getRootVs(vs);
		final StatsHolder ls = vsNode.getChild(name);
		assert (ls != null) : "Unexpected this http listener should be non null : vs = " + vs  + " ls = " + name;
		ls.unregisterMBean();
		vsNode.removeChild(name);
    }
    
    public void registerServletStats(
            com.sun.enterprise.admin.monitor.stats.ServletStats ss, 
            String app,
            String module,
            String ctxRoot,
            String vs,
            String servlet, 
	    MonitoringLevelListener listener)
                throws MonitoringRegistrationException {

        if ( ss == null || module == null || ctxRoot == null
                    || vs == null || servlet == null) {
	    throw new IllegalArgumentException("Null arguments");
        }

        final StatsHolder sRootNode = getServletRootNode(app, module, ctxRoot, vs);
        final StatsHolder sNode = sRootNode.addChild(servlet, MonitoredObjectType.SERVLET);
        assert (sNode != null) : "Added Servlet Node is not null";
        sNode.setStats(ss);
        sNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.ServletStats.class);
        sNode.setObjectName(MonitoringObjectNames.getServletObjectName(app, ctxRoot, vs, servlet));
        sNode.setDottedName(DottedNameFactory.getServletDottedName(app, module, vs, servlet));

        if (shouldRegisterMBean(MonitoredObjectType.SERVLET)) {
            sNode.registerMBean();
        }
    }

    public void unregisterServletStats(String app,
                                       String module,
                                       String ctxRoot,
                                       String vs,
                                       String servlet)
	    throws MonitoringRegistrationException {

        final StatsHolder sRootNode = getServletRootNode(app, module, ctxRoot, vs);
        final StatsHolder tmp = sRootNode.getChild(servlet);
        assert (tmp != null) : "Registration of this servlet was not called before: " + servlet;
        tmp.unregisterMBean();
        sRootNode.removeChild(servlet);
    }

    /*
     * Registers the given WebModule stats.
     *
     * @param stats The web module statistics related to the web module
     *        deployment on the given virtual server
     * @param appName The app to which the web module belongs
     * @param webModuleName The web module name
     * @param ctxRoot The context root at which the web module has been 
     *        deployed
     * @param vs The id of the virtual server on which the web module has been
     *        deployed
     * @param listener Monitoring listener
     */
    public void registerWebModuleStats(
                com.sun.enterprise.admin.monitor.stats.WebModuleStats stats, 
                String appName,
                String webModuleName,
                String ctxRoot,
                String vs,
                MonitoringLevelListener listener)
            throws MonitoringRegistrationException {

        if (stats == null || webModuleName == null
                || ctxRoot == null || vs == null) {
            throw new IllegalArgumentException("Null arguments");
        }

        StatsHolder sNode = getServletRootNode(appName, webModuleName,
                                               ctxRoot, vs);
        sNode.setStats(stats);
        Class beanMethods =
                com.sun.enterprise.admin.monitor.stats.WebModuleStats.class;
        Class[] interfaces = stats.getClass().getInterfaces();
        for (int i=0; interfaces != null && i<interfaces.length; i++) {
            if (beanMethods.isAssignableFrom(interfaces[i])) {
                // Register under more specialized (e.g., EE) interface
                beanMethods = interfaces[i];
                break;
            }
        }
        sNode.setStatsClass(beanMethods);

        if (shouldRegisterMBean(MonitoredObjectType.WEBMODULE)) {
            sNode.registerMBean();
        }
    }

    /*
     * Unregisters the WebModuleStats associated with the web module whose name
     * is given by <code>webModuleName</code> and that is part of the
     * application named <code>appName</code> and is deployed on the virtual
     * server <code>vs</code>.
     *
     * This method removes the monitoring node that represents deployment
     * of the web module on the specified virtual server. This node is a 
     * child of the monitoring node representing the web module. If the 
     * monitoring node representing the web module has no more children, it
     * is also removed.
     *
     * @param appName The app to which the web module belongs
     * @param webModuleName The web module name
     * @param ctxRoot The context root at which the web module has been 
     *        deployed
     * @param vs The id of the virtual server on which the web module has been
     *        deployed
     */    
    public void unregisterWebModuleStats(String appName,
                                         String webModuleName,
                                         String ctxRoot,
                                         String vs)
        throws MonitoringRegistrationException {

        final StatsHolder sNode = getServletRootNode(appName, webModuleName,
                                                     ctxRoot, vs);
        sNode.unregisterMBean();

        final StatsHolder parent = (null == appName
                                    ? rootStatsHolder.getChild(MonitoredObjectType.APPLICATIONS.getTypeName())
                                    : rootStatsHolder.getChild(MonitoredObjectType.APPLICATIONS.getTypeName()).getChild(appName));
        if (null != parent){
            final StatsHolder webModule = parent.getChild(webModuleName);
            if (null != webModule){
                webModule.removeChild(vs);
                Collection children = webModule.getAllChildren();
                if (children == null || children.isEmpty()) {
                    // The node representing the web module has no more 
                    // virtual server children. Remove it.
                    webModule.unregisterMBean();
                    parent.removeChild(webModuleName);
                }
            }
        }
    }

                


    /**
     * Gets the WebModuleStats associated with the web module whose name
     * is given by <code>webModuleName</code> and that is part of the
     * application named <code>appName</code> and is deployed on the virtual
     * server <code>vs</code>.
     *
     * @param appName Name of the J2EE Application to which
     *        the web module belongs, or null if the web module is stand-alone
     * @param webModuleName The name of the web module from which to retrieve
     *        the stats
     * @param ctxRoot The context root at which the web module has been 
     *        deployed
     * @param vs The id of the virtual-server on which the web module has
     *        been deployed
     *
     * @return The desired WebModuleStats, or null
     */
    public WebModuleStats getWebModuleStats(String appName, 
                                            String webModuleName,
                                            String ctxRoot,
                                            String vs) {
        WebModuleStats stats = null;

        StatsHolder sNode = getServletRootNode(appName, webModuleName,
                                               ctxRoot, vs);
        if (sNode != null) {
            stats = (WebModuleStats) sNode.getStats();
        }

        return stats;
        
    }

    /*
     * Returns the node in the monitoring tree that represents the virtual
     * server (with the given id) to which the web module with the given name
     * has been deployed.
     *
     * The returned node represents the parent node to which the web module's
     * servlet nodes will be attached (as children).
     *
     * @param app The app to which the web module belongs
     * @param module The web module name
     * @param ctxRoot The context root at which the web module has been 
     *        deployed
     * @param vs The id of the virtual server on which the web module has been
     *        deployed
     *
     * @return The node in the monitoring tree that represents the virtual 
     *         server to which the web module has been deployed
     */
    private StatsHolder getServletRootNode(
            String app, String module, String ctxRoot, String vs) {

        StatsHolder tmp = rootStatsHolder.getChild(MonitoredObjectType.APPLICATIONS.getTypeName()); //applications
        if (app != null) {
            tmp = tmp.addChild(app, MonitoredObjectType.APPLICATION);
            tmp.setObjectName(MonitoringObjectNames.getApplicationObjectName(app));
            tmp.setDottedName(DottedNameFactory.getAppDottedName(app));
            tmp.registerMBean();
            tmp = tmp.addChild(module, MonitoredObjectType.WEBMODULE);
            tmp.setDottedName(DottedNameFactory.getAppModuleDottedName(app,module));
	} else {
            tmp = tmp.addChild(module, MonitoredObjectType.STANDALONE_WEBMODULE);
            tmp.setDottedName(DottedNameFactory.getStandAloneWebModuleDottedName(module));
	}
        tmp.setObjectName(MonitoringObjectNames.getWebModuleObjectName(app, ctxRoot, vs));
        tmp.registerMBean();
        tmp = tmp.addChild(vs, MonitoredObjectType.VIRTUAL_SERVER);
        tmp.setObjectName(MonitoringObjectNames.getVirtualServerObjectName(app, ctxRoot, vs));
        tmp.setDottedName(DottedNameFactory.getWebAppsVirtualServerDottedName(app, module, vs));
        return ( tmp );
    }

	private StatsHolder getEjbRootNode(String app, String module) {
		StatsHolder tmp = rootStatsHolder.getChild(MonitoredObjectType.APPLICATIONS.getTypeName()); //applications
		if (app != null) {
			tmp = tmp.addChild(app, MonitoredObjectType.APPLICATION);
			tmp.setObjectName(MonitoringObjectNames.getApplicationObjectName(app));
			tmp.setDottedName(DottedNameFactory.getAppDottedName(app));
			tmp.registerMBean();
			tmp = tmp.addChild(module, MonitoredObjectType.EJBMODULE);
			tmp.setDottedName(DottedNameFactory.getAppModuleDottedName(app, module));
		}
		else {
			tmp = tmp.addChild(module, MonitoredObjectType.STANDALONE_EJBMODULE);
			tmp.setDottedName(DottedNameFactory.getStandAloneEJBModuleDottedName(module));
		}
		tmp.setObjectName(MonitoringObjectNames.getEjbModuleObjectName(app, module));
		tmp.registerMBean();
		return ( tmp );
    }

	private StatsHolder getWebServiceEndpointForWeb(String app,
            String module, String ctxRoot, String vs, String endpoint) {
		final StatsHolder sRoot = getServletRootNode(app, module, ctxRoot, vs);
		assert (sRoot != null) : "MRH:getWebService, unexpectedly sRoot is null: "
            + app + ":" + module;
		final StatsHolder wsNode = sRoot.addChild(endpoint, 
            MonitoredObjectType.WEBSERVICE_ENDPOINT); // type not final yet
		assert (wsNode != null) : "Web Service node should not be null: " +
            endpoint;
		return ( wsNode );
    }

	private StatsHolder getWebServiceEndpointForEjb(String app, String module,
    String endpoint) {
		final StatsHolder ejbRoot = getEjbRootNode(app, module);
		assert (ejbRoot != null) : "MRH:getEjb, unexpectedly ejbRoot is null: "
            + app + ":" + module;
		final StatsHolder wsNode = ejbRoot.addChild(endpoint, 
            MonitoredObjectType.WEBSERVICE_ENDPOINT); // type not final yet
		assert (wsNode != null) : "Web Service node should not be null: " +
            endpoint;
		return ( wsNode );
    }

	private StatsHolder getEjb(String app, String module, MonitoredObjectType ejbType, String ejb) {
		final StatsHolder ejbRoot = getEjbRootNode(app, module);
		assert (ejbRoot != null) : "MRH:getEjb, unexpectedly ejbRoot is null: " + app + ":" + module;
		final StatsHolder ejbNode = ejbRoot.addChild( ejb, ejbType );
		assert (ejbNode != null) : "Ejb node should not be null: " + ejb;
		return ( ejbNode );
    }
	
	private StatsHolder getEjbMethods(String app, String module, MonitoredObjectType ejbType, String ejb) {
		final StatsHolder ejbNode = getEjb(app, module, ejbType, ejb);
		assert (ejbNode != null) : "MRH:getEjbMethods, unexpectedly ejb node is null: " + app + ":" + module + ":" + ejb;
		final String methods = MonitoredObjectType.BEAN_METHODS.getTypeName();
		final StatsHolder added = ejbNode.addChild(methods, MonitoredObjectType.BEAN_METHODS);
		added.setObjectName(MonitoringObjectNames.getEjbMethodsObjectName( app, module, ejbType, ejb));
		added.setDottedName(DottedNameFactory.getEJBMethodsDottedName(app, module, ejb));
		added.registerMBean();
		return ( added );
	}

	private StatsHolder getRootVs(String vs) {
		final StatsHolder https = rootStatsHolder.getChild(MonitoredObjectType.HTTP_SERVICE.getTypeName());
		assert (https != null) : "MRH:registerHttpListener HTTP Service is not initialized - Serious";
		https.setDottedName(DottedNameFactory.getHttpServiceDottedName());
		StatsHolder added = https.addChild(vs, MonitoredObjectType.VIRTUAL_SERVER);
		assert (added != null) : "MRH: VS is null: " + vs;
		added.setObjectName(MonitoringObjectNames.getVirtualServerObjectName(vs));
		added.setDottedName(DottedNameFactory.getHttpSvcVirtualServerDottedName(vs));
		added.registerMBean();
		return ( added );
	}
	
	/* START
	 * Here are some tree-walking methods to walk the StatsHolder tree. I am
	 * unfortunately using them somewhere else also, because it is better that the code
	 * is at one place. Also, these methods are NOT in the interface MonitoringRegistry
	 * and hence calling classes have to cast the class.
	 */
	
	public Collection getThreadPoolNodes() {
		final StatsHolder tps = rootStatsHolder.getChild(MonitoredObjectType.THREAD_POOLS.getTypeName());
		return ( tps.getAllChildren() );
	}
	public Collection getOrbNodes() {
		//currently ALL connection managers are returned.
		StatsHolder tmp = rootStatsHolder.getChild(MonitoredObjectType.ORB.getTypeName());
		tmp = tmp.getChild(MonitoredObjectType.CONNECTION_MANAGERS.getTypeName()); //connection-managers
		return ( tmp.getAllChildren() );
	}

	public Collection getHttpServiceNodes() {
		//currently ALL http listeners are returned
		final ArrayList all = new ArrayList();
		final Collection vss = getVsNodes();
        // Need to add the child nodes first, before adding the
        // grandchildren
        all.addAll(vss);
        // also need to add the http-service node itself.
        // all.add(rootStatsHolder.getChild(MonitoredObjectType.HTTP_SERVICE.getTypeName()));
        
		final Iterator  it = vss.iterator();
		while (it.hasNext()) {
			final StatsHolder vs = (StatsHolder) it.next();
			all.addAll(vs.getAllChildren());
		}
		return ( all );
	}
	private Collection getVsNodes() {
		final StatsHolder https = rootStatsHolder.getChild(MonitoredObjectType.HTTP_SERVICE.getTypeName());
    	return ( https.getAllChildren() );
	}
	
	public Collection getTransactionServiceNodes() {
		//singleton transaction-service node is returned
		final ArrayList all = new ArrayList();
		all.add(rootStatsHolder.getChild(MonitoredObjectType.TRANSACTION_SERVICE.getTypeName()));
		return ( all );
	}
	
	public Collection getConnectionPoolNodes() {
		final StatsHolder res = rootStatsHolder.getChild(MonitoredObjectType.RESOURCES.getTypeName());
		return ( res.getAllChildren() );
	}
	    
    
	public Collection getWebContainerNodes() {
		//All servlet nodes are returned
		final ArrayList all = new ArrayList();
		
		final StatsHolder appsNode = rootStatsHolder.getChild(MonitoredObjectType.APPLICATIONS.getTypeName());
		final Collection apps = appsNode.getAllChildren();
		final Iterator it = apps.iterator();
		while (it.hasNext()) {
			final StatsHolder appOrStandaloneModule = (StatsHolder) it.next();
			if (appOrStandaloneModule.getType() == MonitoredObjectType.APPLICATION) {
				final Collection modulesInApp = appOrStandaloneModule.getAllChildren();
				final Iterator ii = modulesInApp.iterator();
				while (ii.hasNext()) {
					final StatsHolder embedded = (StatsHolder) ii.next();
					if (embedded.getType() == MonitoredObjectType.WEBMODULE) {
						all.addAll(getWebModuleNodes(embedded));
					}
				}
			}
			else if (appOrStandaloneModule.getType() == MonitoredObjectType.STANDALONE_WEBMODULE) {
				final StatsHolder standaloneWebModule = appOrStandaloneModule;
				all.addAll(getWebModuleNodes(standaloneWebModule));
			}
		}
		return ( all );
	}
	
	private Collection getWebModuleNodes(StatsHolder node) {
		final ArrayList nodes = new ArrayList();
		assert (node.getType() == MonitoredObjectType.WEBMODULE || 
				node.getType() == MonitoredObjectType.STANDALONE_WEBMODULE);
		final Collection vss = node.getAllChildren();
		final Iterator it = vss.iterator();
		while (it.hasNext()) {
			final StatsHolder vs = (StatsHolder) it.next();
                        // Add node corresponding to virtual server on which
                        // web module has been deployed
                        nodes.add(vs);
                        // Add servlet nodes
			nodes.addAll(vs.getAllChildren());
		}
		return ( nodes );
	}
	
	public Collection getEjbContainerNodes(boolean includeMethods) {
		//all Ejbs, cache, pool and method nodes are returned
		final Collection all = new ArrayList();
		final StatsHolder appsNode = rootStatsHolder.getChild(MonitoredObjectType.APPLICATIONS.getTypeName());
		final Collection apps = appsNode.getAllChildren();
		final Iterator it = apps.iterator();
		while (it.hasNext()) {
			final StatsHolder appOrStandaloneModule = (StatsHolder) it.next();
			if (appOrStandaloneModule.getType() == MonitoredObjectType.APPLICATION) {
				final Collection modulesInApp = appOrStandaloneModule.getAllChildren();
				final Iterator ii = modulesInApp.iterator();
				while (ii.hasNext()) {
					final StatsHolder embedded = (StatsHolder) ii.next();
					if (embedded.getType() == MonitoredObjectType.EJBMODULE) {
						all.addAll(getEjbNodesInEjbModule(embedded, includeMethods));
					}
				}
			}
			else if (appOrStandaloneModule.getType() == MonitoredObjectType.STANDALONE_EJBMODULE) {
				final StatsHolder standaloneEjbModule = appOrStandaloneModule;
				all.addAll(getEjbNodesInEjbModule(standaloneEjbModule, includeMethods));
			}
		}
		return ( all );
	}
	
	private Collection getEjbNodesInEjbModule(StatsHolder node, boolean includeMethods) {
		final ArrayList ejbNodes = new ArrayList();
		assert (node.getType() == MonitoredObjectType.EJBMODULE || 
				node.getType() == MonitoredObjectType.STANDALONE_EJBMODULE);
		final Collection ejbs = node.getAllChildren();
		ejbNodes.addAll(ejbs);   // added all the ejbs in all the web modules
		// now for each ejb node
		final Iterator it = ejbs.iterator();
		while (it.hasNext()) {
			final StatsHolder ejb = (StatsHolder) it.next();
			ejbNodes.addAll(ejb.getAllChildren()); //EJB_METHODS node will also be added here.
			final StatsHolder methodsNode = ejb.getChild(MonitoredObjectType.BEAN_METHODS.getTypeName());
			if(includeMethods) {				
			if (methodsNode != null)  // required because this may be null, when there are no ejbs registered to begin with
				ejbNodes.addAll(getEjbMethodNodes(methodsNode));
		}
			else {
				ejbNodes.remove(methodsNode);
			}
		}
		return ( ejbNodes );
	}
	
	private Collection getEjbMethodNodes(StatsHolder methodsNode) {
		assert (methodsNode.getType() == MonitoredObjectType.BEAN_METHODS);
		return ( methodsNode.getAllChildren() );
	}
	
	public Collection getEjbMethodNodes() {
		// get all the subtype nodes for the ejb container 
		final ArrayList ejbNodes = new ArrayList();
		final ArrayList ejbMethodNodes = new ArrayList();
		ejbNodes.addAll(getEjbContainerNodes(true));
		final Iterator it = ejbNodes.iterator();
		while (it.hasNext()) {
			StatsHolder ejbNode = (StatsHolder) it.next();
			if((ejbNode.getType() == MonitoredObjectType.BEAN_METHODS) || 
			   (ejbNode.getType() == MonitoredObjectType.BEAN_METHOD))
				ejbMethodNodes.add(ejbNode);
		}
		return ejbMethodNodes;
	}
	
	/* END 
	 * Here are some tree-walking methods to walk the StatsHolder tree. I am
	 * unfortunately using them somewhere else also, because it is better that the code
	 * is at one place. Also, these methods are NOT in the interface MonitoringRegistry
	 * and hence calling classes have to to ClassCast.
	 */
	private void addMonitoringLevelListener(MonitoringLevelListener listener){
        if(listener != null)
            listeners.add(listener);
    }        

    /**
     * this method is intended to be called by the different subsystems to register
     * their MonitoringLevelListener
     */
	public void registerMonitoringLevelListener(MonitoringLevelListener listener, 
	MonitoredObjectType objType) {
		if (listener != null && objType != null) {
			//retain the listener reference for dynamic reconfiguration
			objTypeListeners.put(objType, listener);
			// now give the immediate callback with the "current" monitoring level for this type.
			final MonitoringLevel level = MonitoringConfigurationHandler.getLevel(objType);
			//listener.changeLevel(level, level, objType);
		}
    }
	
    public void unregisterMonitoringLevelListener(MonitoringLevelListener listener) {
		if (listener != null) {
			objTypeListeners.remove(listener);
		}
    }
    
	/** Method to initialize the root hierarchy */
	private void initialize() {
		initializeRoot();
		initializeJvm();
		initializeApplications();
		initializeThreadPools();
		initializeOrb();
		initializeTransactionService();
		initializeHttpService();
		initializeResources();
        initializeConnectorService();
        initializeJmsService();
                initializeJndi();
		//((StatsHolderImpl)rootStatsHolder).write();
	}
	
	private void initializeRoot() {
		rootStatsHolder.setObjectName(MonitoringObjectNames.getRootObjectName());
		rootStatsHolder.setDottedName(DottedNameFactory.getRootDottedName());
		rootStatsHolder.registerMBean();
	}
	private void initializeJvm() {
		final StatsHolder jvmNode = rootStatsHolder.addChild(MonitoredObjectType.JVM.getTypeName(), MonitoredObjectType.JVM);
		assert (jvmNode != null) : "Initialization Error in jvmNodeCreation";
		jvmNode.setObjectName(MonitoringObjectNames.getJvmObjectName());
		jvmNode.setDottedName(DottedNameFactory.getJVMDottedName());
		try {
			this.registerJVMStats(new JVMStatsImpl(), null);
		}
		catch (Exception cause) {
			throw new RuntimeException(cause);
		}
        
           initializeJvmMonitoring(rootStatsHolder);
    }
	private void initializeApplications() {
		final StatsHolder appsNode = rootStatsHolder.addChild(MonitoredObjectType.APPLICATIONS.getTypeName(), MonitoredObjectType.APPLICATIONS);
		assert (appsNode != null) : "Initialization Error in appsNodeCreation";
		appsNode.setObjectName(MonitoringObjectNames.getApplicationsObjectName());
		appsNode.setDottedName(DottedNameFactory.getApplicationsDottedName());
		appsNode.registerMBean();
	}
	private void initializeThreadPools() {
		final StatsHolder tpsNode = rootStatsHolder.addChild(MonitoredObjectType.THREAD_POOLS.getTypeName(), MonitoredObjectType.THREAD_POOLS);
		assert (tpsNode != null) : "Initialization Error in tpsNodeCreation";
		tpsNode.setObjectName(MonitoringObjectNames.getThreadPoolsObjectName());
		tpsNode.setDottedName(DottedNameFactory.getThreadPoolsDottedName());
		tpsNode.registerMBean();
	}
	private void initializeOrb() {
		final StatsHolder orbNode = rootStatsHolder.addChild(MonitoredObjectType.ORB.getTypeName(), MonitoredObjectType.ORB);
		assert (orbNode != null) : "Initialization Error in orbNodeCreation";
		orbNode.setObjectName(MonitoringObjectNames.getOrbObjectName());
		orbNode.setDottedName(DottedNameFactory.getOrbDottedName());
		orbNode.registerMBean();
		final StatsHolder cmsNode = orbNode.addChild(MonitoredObjectType.CONNECTION_MANAGERS.getTypeName(), MonitoredObjectType.CONNECTION_MANAGERS);
		assert (cmsNode != null) : "Initialization Error in Connection Managers Creation";
		cmsNode.setObjectName(MonitoringObjectNames.getConnectionManagersObjectName());
		cmsNode.setDottedName(DottedNameFactory.getConnectionManagersDottedName());
		cmsNode.registerMBean();
	}
	private void initializeTransactionService() {
		final StatsHolder tsNode = rootStatsHolder.addChild(MonitoredObjectType.TRANSACTION_SERVICE.getTypeName(), MonitoredObjectType.TRANSACTION_SERVICE);
		assert (tsNode != null) : "Initialization Error in Transaction Service";
		tsNode.setObjectName(MonitoringObjectNames.getTransactionServiceObjectName());
		tsNode.setDottedName(DottedNameFactory.getTransactionServiceDottedName());
	}
	private void initializeHttpService() {
		final StatsHolder hsNode = rootStatsHolder.addChild(MonitoredObjectType.HTTP_SERVICE.getTypeName(), MonitoredObjectType.HTTP_SERVICE);
		assert (hsNode != null) : "Initialization Error in HttpServiceCreation";
		hsNode.setObjectName(MonitoringObjectNames.getHttpServiceObjectName());
		hsNode.setDottedName(DottedNameFactory.getHttpServiceDottedName());
		hsNode.registerMBean();
	}
	private void initializeResources() {
		final StatsHolder resNode = rootStatsHolder.addChild(MonitoredObjectType.RESOURCES.getTypeName(), MonitoredObjectType.RESOURCES);
		assert (resNode != null) : "Initialization Error in Resources";
		resNode.setObjectName(MonitoringObjectNames.getResourcesObjectName());
		resNode.setDottedName(DottedNameFactory.getResourcesDottedName());
		resNode.registerMBean();
	}

        private void initializeJndi() {
                //if (shouldRegisterMBean(MonitoredObjectType.JNDI)) {
                    JndiMBeanManager mgr = new JndiMBeanManager();
                    mgr.registerMBean(MonitoringObjectNames.getJndiObjectName());
                //}
        }

    private void initializeJvmMonitoring(StatsHolder rootNode) {
        
        final String JMM_CLASS =
        "com.sun.enterprise.admin.monitor.registry.spi.JVMMonitoringManager";
        final String METHOD = "getInstance";
        final String REG_METHOD = "registerStats";
        try {
            final Class c = Class.forName(JMM_CLASS);
            final java.lang.reflect.Method m = c.getMethod(METHOD);
            final Object jmm = m.invoke(c);
            registerMonitoringLevelListener((MonitoringLevelListener)jmm, MonitoredObjectType.JVM);
            final java.lang.reflect.Method m1 = 
                          c.getMethod(REG_METHOD, new Class[]{com.sun.enterprise.admin.monitor.registry.StatsHolder.class});
            m1.invoke(jmm, new Object[]{rootStatsHolder});
            //jmm.registerStats(rootStatsHolder);
        }
        catch(Throwable t) {
            throw new RuntimeException(t);
        }
    }
    
	private boolean shouldRegisterMBean(MonitoredObjectType type) {
		return ( MonitoringConfigurationHandler.shouldRegisterMBean(type) );
        }

    // connector & jms service related changes BEGIN
    /**
     * This creates a connector-service node in the hierarchy, which contains
     * all the connector modules.
     */
    private void initializeConnectorService() {
        final StatsHolder adaptorsNode = rootStatsHolder.addChild(
                                        MonitoredObjectType.CONNECTOR_SERVICE.getTypeName(),
                                        MonitoredObjectType.CONNECTOR_SERVICE);
        assert (adaptorsNode != null) : "connector-service node not created";
        adaptorsNode.setObjectName(MonitoringObjectNames.getConnectorServiceObjectName());
        adaptorsNode.setDottedName(DottedNameFactory.getConnectorServiceDottedName());
        adaptorsNode.registerMBean();
    }
	
    /**
     * This creates a jms-service node in the hierarchy, which contains
     * all the connector modules.
     */

    private void initializeJmsService() {
        final StatsHolder adaptorsNode = rootStatsHolder.addChild(
                                        MonitoredObjectType.JMS_SERVICE.getTypeName(),
                                        MonitoredObjectType.JMS_SERVICE);
        assert (adaptorsNode != null) : "jms-service node not created";
        adaptorsNode.setObjectName(MonitoringObjectNames.getJmsServiceObjectName());
        adaptorsNode.setDottedName(DottedNameFactory.getJmsServiceDottedName());
        adaptorsNode.registerMBean();
    }

    /** 
     * returns the resource adapter root node, whether it is part of the application
     * or standalone.
     */
    private StatsHolder getConnectorRootNode(String j2eeAppName, String moduleName) {
        
        // if the connector-module is embedded in a j2eeApp, then the node for the 
        // connector-module should be called j2eeAppName#moduleName
        StatsHolder tmp = rootStatsHolder.getChild(MonitoredObjectType.CONNECTOR_SERVICE.getTypeName());        
        assert (tmp != null) : "connector-service node not created";
		if (j2eeAppName != null)
            tmp = tmp.addChild(j2eeAppName +"#" + moduleName, MonitoredObjectType.CONNECTOR_MODULE);
        else
            tmp = tmp.addChild(moduleName, MonitoredObjectType.STANDALONE_CONNECTOR_MODULE);
        
        tmp.setDottedName(DottedNameFactory.getConnectorModuleDottedName(j2eeAppName, moduleName));
		tmp.setObjectName(MonitoringObjectNames.getConnectorModuleObjectName(j2eeAppName, moduleName));
		tmp.registerMBean();
		return ( tmp );
    }
    
   // this signature is being preserved for backward compatibility 
    public void registerConnectorWorkMgmtStats(
           com.sun.enterprise.admin.monitor.stats.ConnectorWorkMgmtStats stats,
           String j2eeAppName,
           String moduleName,
           MonitoringLevelListener listener) 
           throws MonitoringRegistrationException {
        
        registerConnectorWorkMgmtStats(stats, j2eeAppName, moduleName, false, listener);
               
    }
   
 
	public void registerConnectorWorkMgmtStats(
	       com.sun.enterprise.admin.monitor.stats.ConnectorWorkMgmtStats stats,
	       String j2eeAppName,
	       String moduleName,
           boolean isJms,
	       MonitoringLevelListener listener) throws MonitoringRegistrationException {
      
        StatsHolder adapterNode = null;
        // for registering the work management stats for jms-service, the j2eeAppName and
        // moduleName are not needed
        if(isJms) {
            if(stats == null)
                throw new IllegalArgumentException("Invalid Arguments for ConnectorWorkManagementStats registration");
            adapterNode = rootStatsHolder.getChild(MonitoredObjectType.JMS_SERVICE.getTypeName());
        }
        else {
            if((stats == null)||(moduleName == null)||(moduleName.equals("")))
                throw new IllegalArgumentException("Invalid Arguments for ConnectorWorkManagementStats registration");
            adapterNode = getConnectorRootNode(j2eeAppName, moduleName);
        }
                    
        assert(adapterNode != null):"connector node is null";
        adapterNode = adapterNode.addChild(MonitoredObjectType.CONNECTOR_WORKMGMT.getTypeName(), 
                                           MonitoredObjectType.CONNECTOR_WORKMGMT);
        adapterNode.setStats(stats);
        adapterNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.ConnectorWorkMgmtStats.class);
        adapterNode.setDottedName(DottedNameFactory.getConnectorWorkMgmtDottedName(j2eeAppName, moduleName, isJms));
        adapterNode.setObjectName(MonitoringObjectNames.getConnectorWorkMgmtObjectName(j2eeAppName, moduleName, isJms));
        adapterNode.registerMBean();
        if (shouldRegisterMBean(MonitoredObjectType.CONNECTOR_WORKMGMT))
            adapterNode.registerMBean();
    }
	
	// Connector WorkMgmt Unregistration
    
    // this signature is being preserved for backward compatibility
    public void unregisterConnectorWorkMgmtStats(String j2eeAppName, String moduleName) 
           throws MonitoringRegistrationException {
        unregisterConnectorWorkMgmtStats(j2eeAppName, moduleName, false);
    }
    
	public void unregisterConnectorWorkMgmtStats(String j2eeAppName, String moduleName, boolean isJms) 
	       throws MonitoringRegistrationException {
        
        StatsHolder connectorNode = null;
        if(isJms) {
            connectorNode = rootStatsHolder.getChild(MonitoredObjectType.JMS_SERVICE.getTypeName());
        }
        else {
            if((moduleName == null)||(j2eeAppName != null && j2eeAppName.equals(""))||(moduleName.equals("")))
                throw new IllegalArgumentException("Invalid Arguments for ConnectorWorkManagementStats unregistration");
            connectorNode = getConnectorRootNode(j2eeAppName, moduleName);
        }
        assert(connectorNode != null): "connector node not initialized";
        StatsHolder workmgmtNode = connectorNode.getChild(MonitoredObjectType.CONNECTOR_WORKMGMT.getTypeName());
        assert(workmgmtNode != null): "Connector Work Management node not initialized";
        workmgmtNode.unregisterMBean();
        connectorNode.removeChild(MonitoredObjectType.CONNECTOR_WORKMGMT.getTypeName());
    }
	
    
    public void registerConnectionFactoryStats(com.sun.enterprise.admin.monitor.stats.ConnectionFactoryStats stats,
                                               String factoryName,
                                               MonitoringLevelListener listener)
                                               throws MonitoringRegistrationException {

        if((stats == null) || (factoryName == null))
            throw new IllegalArgumentException("Invalid arguments for registraion of ConnectionFactory Stats");
        StatsHolder cfNode = getConnectionFactoriesNode();
        assert(cfNode != null): "connection-factories node not initialized correctly";
        cfNode = cfNode.addChild(factoryName, MonitoredObjectType.CONNECTION_FACTORY);
        cfNode.setStats(stats);
        cfNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.ConnectionFactoryStats.class);
        cfNode.setDottedName(DottedNameFactory.getConnectionFactoryDottedName(factoryName));
        cfNode.setObjectName(MonitoringObjectNames.getConnectionFactoryObjectName(factoryName));
        if(shouldRegisterMBean(MonitoredObjectType.CONNECTION_FACTORY))
            cfNode.registerMBean();
    }
    
    private StatsHolder getConnectionFactoriesNode() {
        
        StatsHolder tmpNode = rootStatsHolder.getChild(MonitoredObjectType.JMS_SERVICE.getTypeName());
        assert(tmpNode != null): "jms-service node initialized correctly";
        tmpNode = tmpNode.addChild(MonitoredObjectType.CONNECTION_FACTORIES.getTypeName(), MonitoredObjectType.CONNECTION_FACTORIES);
        tmpNode.setDottedName(DottedNameFactory.getConnectionFactoriesDottedName());
        tmpNode.setObjectName(MonitoringObjectNames.getConnectionFactoriesObjectName());
        tmpNode.registerMBean();
        return tmpNode;
    }
   
    public void unregisterConnectionFactoryStats(String factoryName) throws MonitoringRegistrationException {
        
        if(factoryName == null)
            throw new IllegalArgumentException("Invalid arguments for the unregistration of ConnectionFactory Stats");
        StatsHolder tmpNode = rootStatsHolder.getChild(MonitoredObjectType.JMS_SERVICE.getTypeName());
        assert(tmpNode != null): "jms-service node not initialized correctly";
        tmpNode = tmpNode.getChild(MonitoredObjectType.CONNECTION_FACTORIES.getTypeName());
        assert(tmpNode != null): "connection-factories node not initialized correctly";
        StatsHolder cfNode = tmpNode.getChild(factoryName);
        assert(cfNode != null): "connection-factory node not initialized correctly";
        cfNode.unregisterMBean();
        tmpNode.removeChild(factoryName);
    }    
    
    public void registerConnectorConnectionPoolStats(com.sun.enterprise.admin.monitor.stats.ConnectorConnectionPoolStats stats, 
                                                     String poolName,
                                                     String j2eeAppName,
                                                     String moduleName,
                                                     MonitoringLevelListener listener)
                                                     throws MonitoringRegistrationException {
        if((stats == null)||(moduleName == null)||(poolName == null))
            throw new IllegalArgumentException("Invalid arguments for the registration of connection-pool stats");
        
        StatsHolder poolsNode = getConnectionPoolsNode(j2eeAppName, moduleName);
        assert(poolsNode != null): "connection-pools node not initialized correctly";
        StatsHolder poolNode = poolsNode.addChild(poolName, MonitoredObjectType.CONNECTOR_CONN_POOL);
        poolNode.setStats(stats);
        poolNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.ConnectorConnectionPoolStats.class);
        poolNode.setDottedName(DottedNameFactory.getConnectionPoolDottedName(poolName, j2eeAppName, moduleName));
        poolNode.setObjectName(MonitoringObjectNames.getConnectionPoolObjectName(poolName, j2eeAppName, moduleName));
        if(shouldRegisterMBean(MonitoredObjectType.CONNECTOR_CONN_POOL))
            poolNode.registerMBean();
        this.registerConnectionPoolStats(stats, poolName, MonitoredObjectType.CONNECTOR_CONN_POOL, listener);
    }
    
    private StatsHolder getConnectionPoolsNode(String j2eeAppName, String moduleName) {
        
        StatsHolder connNode = getConnectorRootNode(j2eeAppName, moduleName);
        assert(connNode != null): "connector node initialized correctly";
        connNode = connNode.addChild(MonitoredObjectType.CONNECTION_POOLS.getTypeName(), MonitoredObjectType.CONNECTION_POOLS);
        connNode.setDottedName(DottedNameFactory.getConnectionPoolsDottedName(j2eeAppName, moduleName));
        connNode.setObjectName(MonitoringObjectNames.getConnectionPoolsObjectName(j2eeAppName, moduleName));
        connNode.registerMBean();
        return connNode;
    }
    
    public void unregisterConnectorConnectionPoolStats(String poolName,
                                                       String j2eeAppName,
                                                       String moduleName)
                                                       throws MonitoringRegistrationException {
     
        StatsHolder poolsNode = getConnectionPoolsNode(j2eeAppName, moduleName);
        assert(poolsNode != null): "connection-pools node not initialized";
        StatsHolder poolNode = poolsNode.getChild(poolName);
        assert(poolNode != null): "connection-pool node not initialized";
        poolNode.unregisterMBean();
        poolsNode.removeChild(poolName);
        this.unregisterConnectionPoolStats(poolName);
    }    
    
    /**
     * This will return all the nodes related to the connector-connection-pool,
     * connector-service, jms-service
     */
    
    public Collection getConnectorRelatedNodes() {
        Collection c = getConnectionPoolNodes(MonitoredObjectType.CONNECTOR_CONN_POOL);
        c.addAll(getConnectorServiceChildren());
        c.addAll(getJmsServiceChildren());
        return c;
    }
    
    /**
     * Return the collection of all the connection-pool nodes and the
     * work-management nodes registered under the connector service
     */
    private Collection getConnectorServiceChildren() {
       
        StatsHolder connservNode = rootStatsHolder.getChild(MonitoredObjectType.CONNECTOR_SERVICE.getTypeName());
        assert(connservNode != null): "connector-service node is null";
        // get the children of the connector-service
        ArrayList c = new ArrayList();
        Collection cNodes = connservNode.getAllChildren();
        c.addAll(cNodes);
        // now c should contain, all the connector nodes
        Iterator iter = cNodes.iterator();
        while(iter.hasNext()) {
            StatsHolder connectorNode = (StatsHolder) iter.next();
            c.addAll(connectorNode.getAllChildren()); // adds the connection-pools & work-mgmt nodes
            StatsHolder poolsNode = connectorNode.getChild(MonitoredObjectType.CONNECTION_POOLS.getTypeName());
            c.addAll(poolsNode.getAllChildren());
        }
        return c;
    }
    
    private Collection getJmsServiceChildren() {
        
        StatsHolder jmsservNode = rootStatsHolder.getChild(MonitoredObjectType.JMS_SERVICE.getTypeName());
        assert(jmsservNode != null): "jms-service node is null";
        // get the children of the connector-service
        ArrayList c = new ArrayList();
        Collection jmsNodes = jmsservNode.getAllChildren();
        c.addAll(jmsNodes);
        // now c should contain, connection-factories & work-mgmt nodes
        Iterator it = jmsNodes.iterator();
        while(it.hasNext()) {
            StatsHolder childNode = (StatsHolder) it.next();
            Collection c1 = childNode.getAllChildren();
            if(c1 != null)
                c.addAll(c1);
        }
        return c;
    }
    
    public Collection getConnectionPoolNodes(MonitoredObjectType t) {
        
        StatsHolder resNode = rootStatsHolder.getChild(MonitoredObjectType.RESOURCES.getTypeName());
        Collection c = resNode.getAllChildren();
        Iterator iter = c.iterator();
        ArrayList poolNodes = new ArrayList();
        while(iter.hasNext()) {
            StatsHolder tmp = (StatsHolder)iter.next();
            if(tmp.getType() == t)
                poolNodes.add(tmp);            
        }
        return poolNodes;
    }
    
    // connector & jms service related changes END
    
    
    // PWC related changes BEGIN
	// Registration of PWCConnectionQueueStats 
	public void registerPWCConnectionQueueStats(com.sun.enterprise.admin.monitor.stats.PWCConnectionQueueStats stats, 
                                                MonitoringLevelListener listener) throws MonitoringRegistrationException {
        
        if(stats == null)
            throw new IllegalArgumentException("PWCConnectionQueueStats is null");
        StatsHolder httpsvcNode = rootStatsHolder.getChild(MonitoredObjectType.HTTP_SERVICE.getTypeName());
        assert(httpsvcNode != null): "HttpService is not initialized correctly";
        httpsvcNode = httpsvcNode.addChild(MonitoredObjectType.CONNECTION_QUEUE.getTypeName(), MonitoredObjectType.CONNECTION_QUEUE);
        httpsvcNode.setStats(stats);
        httpsvcNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.PWCConnectionQueueStats.class);
        httpsvcNode.setDottedName(DottedNameFactory.getConnectionQueueDottedName());
        httpsvcNode.setObjectName(MonitoringObjectNames.getConnectionQueueObjectName());
        if(shouldRegisterMBean(MonitoredObjectType.CONNECTION_QUEUE))
            httpsvcNode.registerMBean();
	}
	
	// Unregistration of PWCConnectionQueueStats 
	public void unregisterPWCConnectionQueueStats() throws MonitoringRegistrationException {
        
        StatsHolder httpsvcNode = rootStatsHolder.getChild(MonitoredObjectType.HTTP_SERVICE.getTypeName());
        assert(httpsvcNode != null): "http-service node is null";
        StatsHolder targetNode = httpsvcNode.getChild(MonitoredObjectType.CONNECTION_QUEUE.getTypeName());
        assert(targetNode != null): "connection-queue node is null";
        targetNode.unregisterMBean();
        //httpsvcNode.removeChild(MonitoredObjectType.CONNECTION_QUEUE.getTypeName());
	}

	// Registration of PWCDnsStats 
	public void registerPWCDnsStats(
	       com.sun.enterprise.admin.monitor.stats.PWCDnsStats stats, 
	       MonitoringLevelListener listener) throws MonitoringRegistrationException {
        
        if(stats == null)
            throw new IllegalArgumentException("PWCDnsStats is null");
        StatsHolder httpsvcNode = rootStatsHolder.getChild(MonitoredObjectType.HTTP_SERVICE.getTypeName());
        assert(httpsvcNode != null): "HttpService is not initialized correctly";
        httpsvcNode = httpsvcNode.addChild(MonitoredObjectType.DNS.getTypeName(), MonitoredObjectType.DNS);
        httpsvcNode.setStats(stats);
        httpsvcNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.PWCDnsStats.class);
        httpsvcNode.setDottedName(DottedNameFactory.getDnsDottedName());
        httpsvcNode.setObjectName(MonitoringObjectNames.getDnsObjectName());
        if(shouldRegisterMBean(MonitoredObjectType.DNS))
            httpsvcNode.registerMBean();
	}
	
	// Unregistration of PWCDnsStats 
	public void unregisterPWCDnsStats() throws MonitoringRegistrationException {
        
        StatsHolder httpsvcNode = rootStatsHolder.getChild(MonitoredObjectType.HTTP_SERVICE.getTypeName());
        assert(httpsvcNode != null): "http-service node is null";
        StatsHolder dnsNode = httpsvcNode.getChild(MonitoredObjectType.DNS.getTypeName());
        assert(dnsNode != null): "dns node is null";
        dnsNode.unregisterMBean();
        //httpsvcNode.removeChild(MonitoredObjectType.DNS.getTypeName());
	}
	
	// Registration of PWCFileCacheStats 
	public void registerPWCFileCacheStats(
	       com.sun.enterprise.admin.monitor.stats.PWCFileCacheStats stats, 
	       MonitoringLevelListener listener) throws MonitoringRegistrationException {
        
        if(stats == null)
            throw new IllegalArgumentException("PWCFileCacheStats is null");
        StatsHolder httpsvcNode = rootStatsHolder.getChild(MonitoredObjectType.HTTP_SERVICE.getTypeName());
        assert(httpsvcNode != null): "HttpService is not initialized correctly";
        httpsvcNode = httpsvcNode.addChild(MonitoredObjectType.FILE_CACHE.getTypeName(), MonitoredObjectType.FILE_CACHE);
        httpsvcNode.setStats(stats);
        httpsvcNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.PWCFileCacheStats.class);
        httpsvcNode.setDottedName(DottedNameFactory.getFileCacheDottedName());
        httpsvcNode.setObjectName(MonitoringObjectNames.getFileCacheObjectName());
        if(shouldRegisterMBean(MonitoredObjectType.FILE_CACHE))
            httpsvcNode.registerMBean();
	}
	
	// Unregistration of PWCFileCacheStats 
	public void unregisterPWCFileCacheStats() throws MonitoringRegistrationException {
        
        StatsHolder httpsvcNode = rootStatsHolder.getChild(MonitoredObjectType.HTTP_SERVICE.getTypeName());
        assert(httpsvcNode != null): "http-service node is null";
        StatsHolder fcNode = httpsvcNode.getChild(MonitoredObjectType.FILE_CACHE.getTypeName());
        assert(fcNode != null): "file-cache node is null";
        fcNode.unregisterMBean();
        //httpsvcNode.removeChild(MonitoredObjectType.FILE_CACHE.getTypeName());
	}
	
	// Registration of PWCHttpServiceStats 
	public void registerPWCHttpServiceStats(
	       com.sun.enterprise.admin.monitor.stats.PWCHttpServiceStats stats, 
	       MonitoringLevelListener listener) throws MonitoringRegistrationException {
        
        if(stats == null)
            throw new IllegalArgumentException("PWCHttpServiceStats is null");        
        // the MBean for http-service gets registered in the initializeHttpService(), we need 
        // it preserved for PE case. 
        // To make this work in the SE/EE case, we will need unregister the PE MBean and re-register
        // it with Stats if the monitoring on http-service is enabled, else we just set the Stats
        StatsHolder httpsvcNode = rootStatsHolder.getChild(MonitoredObjectType.HTTP_SERVICE.getTypeName());
        assert(httpsvcNode != null): "HttpService is not initialized correctly";
        httpsvcNode.setStats(stats);
        httpsvcNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.PWCHttpServiceStats.class);
        if(shouldRegisterMBean(MonitoredObjectType.HTTP_SERVICE)) {
            httpsvcNode.unregisterMBean();
            httpsvcNode.registerMBean();
        }
	}
	
	// Unregistration of PWCHttpServiceStats 
	public void unregisterPWCHttpServiceStats() throws MonitoringRegistrationException {
        
        StatsHolder httpsvcNode = rootStatsHolder.getChild(MonitoredObjectType.HTTP_SERVICE.getTypeName());
        assert(httpsvcNode != null): "http-service node is null";
        httpsvcNode.unregisterMBean();
        httpsvcNode.setStats(null);
        // hack to recreate the http-service node.
        // won't accept null as a value for StatsClass
        httpsvcNode.setStatsClass(javax.management.j2ee.statistics.Stats.class);
        httpsvcNode.registerMBean();
	}
	
	// Registration of PWCKeepAliveStats 
	public void registerPWCKeepAliveStats(
	       com.sun.enterprise.admin.monitor.stats.PWCKeepAliveStats stats, 
	       MonitoringLevelListener listener) throws MonitoringRegistrationException {
        
        if(stats == null)
            throw new IllegalArgumentException("PWCHttpServiceStats is null");
        StatsHolder httpsvcNode = rootStatsHolder.getChild(MonitoredObjectType.HTTP_SERVICE.getTypeName());
        assert(httpsvcNode != null): "HttpService is not initialized correctly";
        httpsvcNode = httpsvcNode.addChild(MonitoredObjectType.KEEP_ALIVE.getTypeName(), MonitoredObjectType.KEEP_ALIVE);
        httpsvcNode.setStats(stats);
        httpsvcNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.PWCKeepAliveStats.class);
        httpsvcNode.setDottedName(DottedNameFactory.getKeepAliveDottedName());
        httpsvcNode.setObjectName(MonitoringObjectNames.getKeepAliveObjectName());
        if(shouldRegisterMBean(MonitoredObjectType.KEEP_ALIVE))
            httpsvcNode.registerMBean();
	}
	// Unregistration of PWCKeepAliveStats 
	public void unregisterPWCKeepAliveStats() throws MonitoringRegistrationException {
        
        StatsHolder httpsvcNode = rootStatsHolder.getChild(MonitoredObjectType.HTTP_SERVICE.getTypeName());
        assert(httpsvcNode != null): "http-service node is null";
        StatsHolder kaNode = httpsvcNode.getChild(MonitoredObjectType.KEEP_ALIVE.getTypeName());
        assert(kaNode != null): "keep-alive node is null";
        kaNode.unregisterMBean();
        //httpsvcNode.removeChild(MonitoredObjectType.KEEP_ALIVE.getTypeName());
    }
	
	// Registration of PWCRequestStats 
	public void registerPWCRequestStats(
	       com.sun.enterprise.admin.monitor.stats.PWCRequestStats stats, 
	       String vsId, 
	       MonitoringLevelListener listener) throws MonitoringRegistrationException {
               
        if((stats == null) || (vsId == null) || (vsId.equals("")))
            throw new IllegalArgumentException("Invalid arguments for registration of request stats");
        StatsHolder httpsvcNode = rootStatsHolder.getChild(MonitoredObjectType.HTTP_SERVICE.getTypeName());
        assert(httpsvcNode != null): "http-service node is null";
        StatsHolder vsNode = httpsvcNode.getChild(vsId);
        assert(vsNode != null): "virtual-server is not initialized correctly";
        vsNode = vsNode.addChild(MonitoredObjectType.REQUEST.getTypeName(), MonitoredObjectType.REQUEST);
        vsNode.setStats(stats);
        vsNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.PWCRequestStats.class);
        vsNode.setDottedName(DottedNameFactory.getRequestDottedName(vsId));
        vsNode.setObjectName(MonitoringObjectNames.getRequestObjectName(vsId));
        if(shouldRegisterMBean(MonitoredObjectType.REQUEST))
            vsNode.registerMBean();
	}
	
	// Unregistration of PWCRequestStats 
	public void unregisterPWCRequestStats(String vsId) throws MonitoringRegistrationException {
        
        StatsHolder httpsvcNode = rootStatsHolder.getChild(MonitoredObjectType.HTTP_SERVICE.getTypeName());
        assert(httpsvcNode != null): "http-service node is null";
        StatsHolder vsNode = httpsvcNode.getChild(vsId);
        assert(vsNode != null): "virtual-server node is null";
        StatsHolder requestNode = vsNode.getChild(MonitoredObjectType.REQUEST.getTypeName());
        assert(requestNode != null): "request node is null";
        requestNode.unregisterMBean();
        //vsNode.removeChild(MonitoredObjectType.REQUEST.getTypeName());
	}
	
	// Registration of PWCThreadPoolStats 
	public void registerPWCThreadPoolStats(
	       com.sun.enterprise.admin.monitor.stats.PWCThreadPoolStats stats, 
	       MonitoringLevelListener listener) throws MonitoringRegistrationException {
        
        if(stats == null)
            throw new IllegalArgumentException("PWCThreadPoolStats is null");
        StatsHolder httpsvcNode = rootStatsHolder.getChild(MonitoredObjectType.HTTP_SERVICE.getTypeName());
        assert(httpsvcNode != null): "HttpService is not initialized correctly";
        httpsvcNode = httpsvcNode.addChild(MonitoredObjectType.PWC_THREAD_POOL.getTypeName(), MonitoredObjectType.PWC_THREAD_POOL);
        httpsvcNode.setStats(stats);
        httpsvcNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.PWCThreadPoolStats.class);
        httpsvcNode.setDottedName(DottedNameFactory.getPWCThreadPoolDottedName());
        httpsvcNode.setObjectName(MonitoringObjectNames.getPWCThreadPoolObjectName());
        if(shouldRegisterMBean(MonitoredObjectType.PWC_THREAD_POOL))
            httpsvcNode.registerMBean();
	}
	
	// Unregistration of PWCThreadPoolStats 
	public void unregisterPWCThreadPoolStats() throws MonitoringRegistrationException {
        
        StatsHolder httpsvcNode = rootStatsHolder.getChild(MonitoredObjectType.HTTP_SERVICE.getTypeName());
        assert(httpsvcNode != null): "http-service node is null";
        StatsHolder tpNode = httpsvcNode.getChild(MonitoredObjectType.PWC_THREAD_POOL.getTypeName());
        assert(tpNode != null): "pwc-thread-pool node is null";
        tpNode.unregisterMBean();
        //httpsvcNode.removeChild(MonitoredObjectType.PWC_THREAD_POOL.getTypeName());
	}
	
	// Registration of PWCVirtualServerStats 
	public void registerPWCVirtualServerStats(
	       com.sun.enterprise.admin.monitor.stats.PWCVirtualServerStats stats, 
	       String vsId, 
	       MonitoringLevelListener listener) throws MonitoringRegistrationException {
               
        if((stats == null) || (vsId == null) || (vsId.equals("")))
            throw new IllegalArgumentException("Invalid arguments for registration of vs stats");
        StatsHolder httpsvcNode = rootStatsHolder.getChild(MonitoredObjectType.HTTP_SERVICE.getTypeName());
        assert(httpsvcNode != null): "HttpService is not initialized correctly";
        httpsvcNode = httpsvcNode.addChild(vsId, MonitoredObjectType.VIRTUAL_SERVER);
        httpsvcNode.setStats(stats);
        httpsvcNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.PWCVirtualServerStats.class);
        httpsvcNode.setDottedName(DottedNameFactory.getHttpSvcVirtualServerDottedName(vsId));
        httpsvcNode.setObjectName(MonitoringObjectNames.getVirtualServerObjectName(vsId));
        if(shouldRegisterMBean(MonitoredObjectType.VIRTUAL_SERVER))
            httpsvcNode.registerMBean();
	}

	// Unregistration of PWCVirtualServerStats 
	public void unregisterPWCVirtualServerStats(String vsId) throws MonitoringRegistrationException {
        
        StatsHolder httpsvcNode = rootStatsHolder.getChild(MonitoredObjectType.HTTP_SERVICE.getTypeName());
        assert(httpsvcNode != null): "http-service node is null";
        StatsHolder vsNode = httpsvcNode.getChild(vsId);
        assert(vsNode != null): "virtual-server node is null";
        // need to remove all the children of virtual-server, before it is removed
        Collection c = vsNode.getAllChildren();
        Iterator iter = c.iterator();
        while(iter.hasNext()) {
            StatsHolder childNode = (StatsHolder)iter.next();
            childNode.unregisterMBean();
            //vsNode.removeChild(childNode.getName());
        }
        // now unregister the vsNode itself
        vsNode.unregisterMBean();
        //httpsvcNode.removeChild(vsId);
	}    
    // PWC related changes END
    
    // Sessionstore Monitoring BEGIN
    public void registerStatefulSessionStoreStats(StatefulSessionStoreStats stats,
                                                  MonitoredObjectType ejbType,
                                                  String ejbName,
                                                  String moduleName,
                                                  String j2eeAppName,
                                                  MonitoringLevelListener listener)
                                                  throws MonitoringRegistrationException {
        
        if((stats == null) || (ejbName == null) || (moduleName == null))
            throw new IllegalArgumentException("Invalid arguments for registration of StatefulSessionStore stats");
        
        StatsHolder ejbNode = getEjb(j2eeAppName, moduleName, ejbType, ejbName);
        assert(ejbNode != null): "EJB node is not initialized correctly";
        ejbNode = ejbNode.addChild(MonitoredObjectType.SESSION_STORE.getTypeName(), MonitoredObjectType.SESSION_STORE);
        ejbNode.setStats(stats);
        if(stats instanceof com.sun.enterprise.admin.monitor.stats.HAStatefulSessionStoreStats)
            ejbNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.HAStatefulSessionStoreStats.class);
        else
            ejbNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.StatefulSessionStoreStats.class);
        ejbNode.setDottedName(DottedNameFactory.getStatefulSessionStoreDottedName(ejbName, moduleName, j2eeAppName));
        ejbNode.setObjectName(MonitoringObjectNames.getStatefulSessionStoreObjectName( ejbType, ejbName, moduleName, j2eeAppName));
        addMonitoringLevelListener(listener); //listener can be null
        if(shouldRegisterMBean(MonitoredObjectType.SESSION_STORE))
            ejbNode.registerMBean();
    }
    
    public void unregisterStatefulSessionStoreStats(MonitoredObjectType ejbType,
                                                    String ejbName,
                                                    String moduleName,
                                                    String j2eeAppName) 
                                                    throws MonitoringRegistrationException {
                                                        
        if((ejbName == null)|| (moduleName == null))
            throw new IllegalArgumentException("Invalid arguments for the unregistration of StatefulSessionStore stats");
        
        StatsHolder ejbNode = getEjb(j2eeAppName, moduleName, ejbType, ejbName);
        assert(ejbNode != null): "EJB node is not initialized correctly";
        StatsHolder storeNode = ejbNode.getChild(MonitoredObjectType.SESSION_STORE.getTypeName());
        assert(storeNode != null): "SessionStore node is not initialized correctly";
        storeNode.unregisterMBean();
        ejbNode.removeChild(MonitoredObjectType.SESSION_STORE.getTypeName());
    }
    // Sessionstore Monitoring END
    
    // Timer monitoring BEGIN
    public void registerTimerStats(TimerServiceStats stats,
                                   MonitoredObjectType ejbType,
                                   String ejbName,
                                   String moduleName,
                                   String j2eeAppName,
                                   MonitoringLevelListener listener) 
                                   throws MonitoringRegistrationException {
        
        if((stats == null) || (ejbName == null) || (moduleName == null))
            throw new IllegalArgumentException("Invalid arguments for registration of Timer stats");
        
        StatsHolder ejbNode = getEjb(j2eeAppName, moduleName, ejbType, ejbName);
        assert(ejbNode != null): "EJB node is not initialized correctly";
        ejbNode = ejbNode.addChild(MonitoredObjectType.TIMERS.getTypeName(), MonitoredObjectType.TIMERS);
        ejbNode.setStats(stats);
        ejbNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.TimerServiceStats.class);
        ejbNode.setDottedName(DottedNameFactory.getTimerDottedName(ejbName, moduleName, j2eeAppName));
        ejbNode.setObjectName(MonitoringObjectNames.getTimerObjectName( ejbType, ejbName, moduleName, j2eeAppName));
        addMonitoringLevelListener(listener); //listener can be null
        if(shouldRegisterMBean(MonitoredObjectType.TIMERS))
            ejbNode.registerMBean();
    }    
    
    public void unregisterTimerStats(MonitoredObjectType ejbType,
                                     String ejbName,
                                     String moduleName,
                                     String j2eeAppName) 
                                     throws MonitoringRegistrationException {
        
        if((ejbName == null)|| (moduleName == null))
            throw new IllegalArgumentException("Invalid arguments for the unregistration of Timer stats");
        
        StatsHolder ejbNode = getEjb(j2eeAppName, moduleName, ejbType, ejbName);
        assert(ejbNode != null): "EJB node is not initialized correctly";
        StatsHolder timerNode = ejbNode.getChild(MonitoredObjectType.TIMERS.getTypeName());
        assert(timerNode != null): "Timer node is not initialized correctly";
        timerNode.unregisterMBean();
        ejbNode.removeChild(MonitoredObjectType.TIMERS.getTypeName());
    }
        
    // Timer monitoring END
    
    // Web Services Monitoring BEGIN

    public void registerWSAggregateStatsForWeb(Stats stats,
                                   String endpointName,
                                   String moduleName,
                                   String ctxRoot,
                                   String j2eeAppName,
                                   String vs,
                                   MonitoringLevelListener listener) 
                                   throws MonitoringRegistrationException {
        
        if((stats == null) || (endpointName == null) || (moduleName == null))
            throw new IllegalArgumentException(
                "Invalid arguments for registration of WS aggregate stats");
        
        StatsHolder wsNode = getWebServiceEndpointForWeb(j2eeAppName,
            moduleName, ctxRoot, vs, endpointName);
        assert(wsNode != null): "Web Service node is not initialized correctly";
        wsNode = wsNode.addChild(MonitoredObjectType.WEBSERVICE_ENDPOINT.
                getTypeName(), MonitoredObjectType.WEBSERVICE_ENDPOINT);
        wsNode.setStats(stats);
        wsNode.setStatsClassName("com.sun.appserv.management.monitor.statistics.WebServiceEndpointAggregateStats");
        wsNode.setDottedName( DottedNameFactory.
            getWebServiceAggregateStatsInWebDottedName(
                                endpointName, moduleName, j2eeAppName));
        wsNode.setObjectName(
            MonitoringObjectNames.getWebServiceObjectNameForWeb(
                    endpointName, j2eeAppName, ctxRoot, vs ));
        addMonitoringLevelListener(listener); //listener can be null
        if(shouldRegisterMBean(MonitoredObjectType.WEBSERVICE_ENDPOINT))
            wsNode.registerMBean();
    }    
    
    public void unregisterWSAggregateStatsForWeb(String endpointName,
                                     String moduleName,
                                     String ctxRoot,
                                     String j2eeAppName, 
                                     String vs) 
                                     throws MonitoringRegistrationException {
        
        if((endpointName == null)|| (moduleName == null))
            throw new IllegalArgumentException(
            "Invalid arguments for the unregistration of Web Service stats");
        
        StatsHolder wsNode = getWebServiceEndpointForWeb(j2eeAppName,
            moduleName, ctxRoot, vs, endpointName);
        assert(wsNode != null): "Web Service node is not initialized correctly";
        StatsHolder endpointNode =
        wsNode.getChild(MonitoredObjectType.WEBSERVICE_ENDPOINT.getTypeName());
        assert(endpointNode != null): 
                "Endpoint node is not initialized correctly";
        endpointNode.unregisterMBean();
        wsNode.removeChild(MonitoredObjectType.WEBSERVICE_ENDPOINT.
            getTypeName());
    }

    public void registerWSAggregateStatsForEjb(Stats stats,
                                   String endpointName,
                                   String moduleName,
                                   String j2eeAppName,
                                   MonitoringLevelListener listener) 
                                   throws MonitoringRegistrationException {
        
        if((stats == null) || (endpointName == null) || (moduleName == null))
            throw new IllegalArgumentException(
                "Invalid arguments for registration of WS aggregate stats");
        
        StatsHolder wsNode = getWebServiceEndpointForEjb(j2eeAppName,moduleName,
            endpointName);
        assert(wsNode != null): "Web Service node is not initialized correctly";
        wsNode = wsNode.addChild(MonitoredObjectType.WEBSERVICE_ENDPOINT.
                getTypeName(), MonitoredObjectType.WEBSERVICE_ENDPOINT);
        wsNode.setStats(stats);
        wsNode.setStatsClassName("com.sun.appserv.management.monitor.statistics.WebServiceEndpointAggregateStats");
        wsNode.setDottedName( DottedNameFactory.
            getWebServiceAggregateStatsInEjbDottedName(
                                endpointName, moduleName, j2eeAppName));
        wsNode.setObjectName(
            MonitoringObjectNames.getWebServiceObjectNameForEjb(
                    endpointName, moduleName, j2eeAppName));
        addMonitoringLevelListener(listener); //listener can be null
        if(shouldRegisterMBean(MonitoredObjectType.WEBSERVICE_ENDPOINT))
            wsNode.registerMBean();
    }    
    
    public void unregisterWSAggregateStatsForEjb(String endpointName,
                                     String moduleName,
                                     String j2eeAppName) 
                                     throws MonitoringRegistrationException {
        
        if((endpointName == null)|| (moduleName == null))
            throw new IllegalArgumentException(
            "Invalid arguments for the unregistration of Web Service stats");
        
        StatsHolder wsNode = getWebServiceEndpointForEjb(j2eeAppName,moduleName,                endpointName);

        assert(wsNode != null): "Web Service node is not initialized correctly";
        StatsHolder endpointNode =
        wsNode.getChild(MonitoredObjectType.WEBSERVICE_ENDPOINT.getTypeName());
        assert(endpointNode != null): 
                "Endpoint node is not initialized correctly";
        endpointNode.unregisterMBean();
        wsNode.removeChild(MonitoredObjectType.WEBSERVICE_ENDPOINT.
            getTypeName());
    }

    // Web Services Monitoring END

    // utility methods for JVM 1.5 monitoring
    public Collection getJvmNodes(boolean threadInfo) {

        final Collection all = new ArrayList();
		final StatsHolder jvmNode = rootStatsHolder.getChild(MonitoredObjectType.JVM.getTypeName());
		final Collection systemNodes = jvmNode.getAllChildren();
        if(systemNodes != null) {
            final Iterator it = systemNodes.iterator();
            while (it.hasNext()) {
                StatsHolder childNode = (StatsHolder)it.next();
                all.add(childNode);
                //if the node is a garbage-collectors node add its children too
                if(childNode.getType() == MonitoredObjectType.JVM_GCS)
                    all.addAll(childNode.getAllChildren());
                // Add the threadinfo nodes, only if applicable
                if((childNode.getType() == MonitoredObjectType.JVM_THREAD) && threadInfo)
                    all.addAll(childNode.getAllChildren());
            }
        }
		return all;
    }
    
    public Collection getJvmThreadInfoNodes() {
        
        final Collection all = new ArrayList();
        final StatsHolder jvmNode = rootStatsHolder.getChild(MonitoredObjectType.JVM.getTypeName());
        final StatsHolder threadNode = jvmNode.getChild(MonitoredObjectType.JVM_THREAD.getTypeName());
        if(threadNode != null)
            all.addAll(threadNode.getAllChildren());
        
        return all;
    }
    
    public StatsHolder getRootStatsHolder() {
        return rootStatsHolder;
    }
}
