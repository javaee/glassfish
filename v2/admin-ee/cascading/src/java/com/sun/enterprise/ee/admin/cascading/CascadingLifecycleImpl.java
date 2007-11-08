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

package com.sun.enterprise.ee.admin.cascading;

import com.sun.appserv.server.ServerLifecycle;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.mbeans.DomainStatusHelper;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.jdmk.remote.cascading.BasicMBeanServerConnectionFactory;
import com.sun.jdmk.remote.cascading.CascadingAgent;
import com.sun.jdmk.remote.cascading.MBeanServerConnectionFactory;
import com.sun.logging.ee.EELogDomains;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.QueryExp;
import javax.management.Query;
import com.sun.appserv.management.j2ee.StateManageable;
import com.sun.enterprise.ee.admin.clientreg.InstanceRegistry;
import com.sun.appserv.management.util.misc.RunnableBase;

/**
 * CascadingLifecycle implementation
 *
 * Provides methods for cascading local and remote server 
 * instances as part of life cycle operations
 *
 * @author Sreenivas Munnangi
 */

public class CascadingLifecycleImpl implements ServerLifecycle {

    // Logger and StringManager
    private static final Logger _logger =
	Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
    private static final StringManager _strMgr =
	StringManager.getManager(CascadingLifecycleImpl.class);
    
    // map for storing jmx object names of cascaded instances	
    private static final Map<String,ObjectName> cMap = new HashMap<String,ObjectName>();

    // map for stroing references to ASProxyCascadingAgent Objects
    private static final Map<String,ASProxyCascadingAgent> cagentMap = 
            new HashMap<String,ASProxyCascadingAgent>();

    // map for stroing references to previously stopped threads
    private static final Map<String,Thread> threadMap = new HashMap<String,Thread>();

    // how long the stop thread should wait before continuing with start
    private static final long THREAD_WAIT_TIME = 90000l;

    // instance cascading mBean object name
    private static volatile ObjectName icmbON = null;

    // domain status helper class
    private final DomainStatusHelper dsh = new DomainStatusHelper();

    public CascadingLifecycleImpl() {
	_logger.log(Level.FINE, "CascadingLifecycleImpl.init");
    }

    public void onInitialization(ServerContext sc) 
	throws ServerLifecycleException {
	_logger.log(Level.FINE, "CascadingLifecycleImpl.onInitialization");
    }

    public void onStartup(ServerContext sc) 
	throws ServerLifecycleException {
	_logger.log(Level.FINE, "CascadingLifecycleImpl.onStartup");
    }

    /**
     * Registers das mBean to enable cascading by instances.
     * If it is DAS then it checks for live instances and cascades them.
     * If it is instance then it cascades on to das using the das' mbean.
     */
    public void onReady(final ServerContext sc) 
	throws ServerLifecycleException {

	_logger.log(Level.FINE, "CascadingLifecycleImpl.onReady");

	// vars
	ConfigContext configContext = sc.getConfigContext();
	String instanceName = sc.getInstanceName();

	// determine the instance type for appropriate processing
	try {
            if (ServerHelper.isDAS(configContext, instanceName)) {
                // register InstanceCascadingMBean
                registerInstanceCascadingMBean(sc);
                // instance is DAS so cascade the server instances
                cascadeInstances(sc);
            } else {
                // instance is server instance so invoke the DAS' mbean
                // for cascading this instance on DAS
	        _logger.log(Level.INFO, "cascading.instance.background", instanceName);
                RunnableBase rb = new RunnableBase() {
                    protected void doRun() {
                        invokeDASCascading(sc);
                    }
                };
                rb.submit( RunnableBase.HowToRun.RUN_IN_SEPARATE_THREAD );
            }
        } catch (ConfigException ce) {
            ce.printStackTrace();
            _logger.log(Level.WARNING, "cascading.ServerHelper_isDAS_error", ce);
	} catch (Exception ex) {
            _logger.log(Level.WARNING, "cascading.exception", ex);
        }
    }

    public void onShutdown() 
	throws ServerLifecycleException {
	_logger.log(Level.FINE, "CascadingLifecycleImpl.onShutdown");
    }

    /**
     * cleanup including stopping cascading and unregistering mbeans
     */
    public void onTermination() 
	throws ServerLifecycleException {
	_logger.log(Level.FINE, "CascadingLifecycleImpl.onTermination");

	// vars
	ServerContext sc = ApplicationServer.getServerContext();
	ConfigContext configContext = sc.getConfigContext();
	String instanceName = sc.getInstanceName();

	// cleanup

	// determine the instance type for appropriate processing
	try {
           // removed the processing for das
           // because once the DAS is stopped, the cascaded mBeans
           // for remote instances cannot be accessed
           if (! ServerHelper.isDAS(configContext, instanceName)) {
                stopDASCascading();
            }
        } catch (ConfigException ce) {
            ce.printStackTrace();
            _logger.log(Level.WARNING, "cascading.ServerHelper_isDAS_error", ce);
        }
    }


    //
    // private methods
    //


    /**
     * get all the server instances and cascade each instance
     */

    private void cascadeInstances(ServerContext sc) {

        _logger.log(Level.FINE, "CascadingLifecycleImpl.cascadeInstances");

	// local vars
	ConfigContext configContext = sc.getConfigContext();
	String serverName = null;
	Server [] serverArr = null;
	JMXConnector jmxConnector = null;

	// get the list of all instances for this domain

	try {
	    serverArr = ServerHelper.getServersInDomain(configContext);
        } catch (ConfigException ce) {
            ce.printStackTrace();
            _logger.log(Level.WARNING, "cascading.get_servers_in_domain_error", ce);
        }

	// get the jmx connector for each instance
	// exclude the das' server instance

	for (int i=0; i<serverArr.length; i++) {
	    serverName = serverArr[i].getName();
	    try {
	    	if (! ServerHelper.isDAS(configContext, serverName)) {
		    try {
		        jmxConnector = 
			    ServerHelper.getJMXConnector(configContext, serverName);
			cascadeInstance(sc.getDefaultDomainName(), serverName, jmxConnector);
		    } catch (ConfigException ce1) {
			// connector for this instance not available in the config
            		ce1.printStackTrace();
            		_logger.log(Level.WARNING, "cascading.get_server_connector_config_error", ce1);
			// set server status to StateManageable.STATE_STOPPED
			dsh.setstate(serverName, StateManageable.STATE_STOPPED);
		    } catch (java.io.IOException ioe) {
            		_logger.log(Level.WARNING, "cascadingConnectException", serverName);
			// connection cannot be made to this instance
			// set server status to StateManageable.STATE_STOPPED
			dsh.setstate(serverName, StateManageable.STATE_STOPPED);
		    } catch (com.sun.enterprise.security.LoginException le) {
            		_logger.log(Level.WARNING, "cascadingLoginException", serverName);
			// connection cannot be made to this instance
			// set server status to StateManageable.STATE_STOPPED
			dsh.setstate(serverName, StateManageable.STATE_STOPPED);
		    }
	    	}
	    } catch (ConfigException ce) {
            	ce.printStackTrace();
            	_logger.log(Level.WARNING, "casacding.ServerHelper_isDAS_error", ce);
	    }
	}
    }



    /**
     * get all the server instances and stop cascading for each instance
     */

    private void stopCascadeInstances() {
        _logger.log(Level.FINE, "CascadingLifecycleImpl.stopCascadeInstances");
	Set keySet = cMap.keySet();
	if (keySet != null) {
	    Iterator itr = keySet.iterator();
	    while (itr.hasNext()) {
		stopCascadeInstance((String) itr.next());
	    }
	}
    }

    /**
     * set mbean object name for the casacding agent
     * corresponding to the given instance.
     * @param String serverName for which the mBean object name is registered
     * @param ObjectName cascading agent's mbean object name
     */
    private static void setCMap(
	String serverName, ObjectName on) {

        synchronized (cMap) {
	    if (cMap.containsKey(serverName)) {
	        cMap.remove(serverName);
	    }
	    cMap.put(serverName, on);
        }
    }

    /**
     * start cascading for the given instance and jmx connector
     */
    void cascadeInstanceT(String domain, String serverName, JMXConnector jmxConnector) {

        _logger.log(Level.FINE, "CascadingLifecycleImpl.cascadeInstance.serverName = " +
		serverName);

	// local vars
	ObjectName cascadingAgentName = null;
	MBeanServer mbs = MBeanServerFactory.getMBeanServer();
	boolean mfail = false;

	// cascading for instance
	try {

	    // cascading object name
	    cascadingAgentName = new ObjectName(domain + 
			":type=CascadingAgent,serverName=" + serverName);

            // update cMap
            setCMap(serverName, cascadingAgentName);

	    // create cascading agent
	    MBeanServerConnectionFactory mbscf =
		BasicMBeanServerConnectionFactory.newInstance(jmxConnector);

	    // form the cascading filter

	    /*
	    QueryExp queryExp = Query.or(
				new ObjectName(domain + CascadingConstants.runtimeFilter),
				new ObjectName(domain + CascadingConstants.monitoringFilter));

	    // modified the following query to fix bug# 6191799
            QueryExp queryExp = Query.or(monitorFilterON,
                                        Query.and(runtimeFilterON,
                                                Query.not(runtimeServerFilterON)));
	    */

	    ObjectName runtimeFilterON = 
		new ObjectName(domain + CascadingConstants.runtimeFilter);
	    ObjectName webmoduleFilterON =
                new ObjectName(domain + CascadingConstants.webmoduleFilter);
            ObjectName servletFilterON =
                new ObjectName(domain + CascadingConstants.servletFilter);
	    ObjectName monitorFilterON = 
		new ObjectName(domain + CascadingConstants.monitoringFilter);
	    ObjectName runtimeServerFilterON = 
		new ObjectName(domain + CascadingConstants.runtimeServerFilter);
	    ObjectName jbiFilterON = 
                new ObjectName(CascadingConstants.jbiFilter);
	    ObjectName jbiLogFilterON = 
                new ObjectName(CascadingConstants.jbiLogFilter);
		
		// AMX MBeans are always in the "amx" domain, so the filter includes it
		// don't add 'domain' here
	    final ObjectName amxFilterON = new ObjectName( CascadingConstants.amxFilter );

		// Custom MBeans filter which includes its' domain name
	    final ObjectName customMBeansFilterON = 
			new ObjectName( CascadingConstants.customMBeansFilter );

            /* casacding query filter to fix bug# 6191799
                webmoduleFilterON
                OR
                servletFilterON
                OR
                monitorFilterON
                OR
                (runtimeFilterON  AND !runtimeServerFilterON)
            */
               
            QueryExp queryExp = Query.or( jbiFilterON,
                                  Query.or( jbiLogFilterON,
                                    Query.or( amxFilterON,
                                      Query.or(customMBeansFilterON,
                                        Query.or(webmoduleFilterON,
                                          Query.or(servletFilterON,
                                            Query.or(monitorFilterON,
                                              Query.and(runtimeFilterON,
                                                Query.not(runtimeServerFilterON)))))))));

	    // create cascading agent using the above filter

            CascadingAgent cascadingAgent = 
			new ASProxyCascadingAgent(mbscf, 
			new ObjectName("*:*"), 
			queryExp,
			null);

	    // add notification listener
	    CascadingConnectionNotifListener ccnl = 
		new CascadingConnectionNotifListener(serverName);
            cascadingAgent.addNotificationListener(ccnl, null, null);

	    // register with mBean server
	    ObjectInstance cascadingAgentInstance =
		mbs.registerMBean(cascadingAgent, cascadingAgentName);
            
            synchronized (cagentMap) {
                cagentMap.put(serverName, (ASProxyCascadingAgent)cascadingAgent);
            }

	    // start cascading
	    mbs.invoke(cascadingAgentName, "start", null, null);

	} catch(MalformedObjectNameException e) {
	    e.printStackTrace();
            _logger.log(Level.WARNING, "cascading.cascadeInstance_malformedObjectName", e);
	    mfail = true;
	} catch(javax.management.InstanceAlreadyExistsException iae) {
	    iae.printStackTrace();
            _logger.log(Level.WARNING, "cascading.cascadeInstance_InstanceAlreadyExists", iae);
	    mfail = true;
	} catch(javax.management.InstanceNotFoundException infe) {
	    infe.printStackTrace();
            _logger.log(Level.WARNING, "cascading.cascadeInstance_InstanceNotFound", infe);
	    mfail = true;
	} catch(javax.management.MBeanRegistrationException mbre) {
	    mbre.printStackTrace();
            _logger.log(Level.WARNING, "cascading.cascadeInstance_mBeanRegistrationException", mbre);
	    mfail = true;
	} catch(javax.management.MBeanException mbe) {
	    mbe.printStackTrace();
            _logger.log(Level.WARNING, "cascading.cascadeInstance_mBeanException", mbe);
	    mfail = true;
	} catch(javax.management.NotCompliantMBeanException ncmbe) {
	    ncmbe.printStackTrace();
            _logger.log(Level.WARNING, "cascading.cascadeInstance_NotCompliantMBeanException", ncmbe);
	    mfail = true;
	} catch(javax.management.ReflectionException re) {
	    re.printStackTrace();
            _logger.log(Level.WARNING, "cascading.cascadeInstance_ReflectionException", re);
	    mfail = true;
	}

	if (mfail) {
                synchronized (cMap) {
	            cMap.remove(serverName);
                }
		// set server status to StateManageable.STATE_FAILED
		dsh.setstate(serverName, StateManageable.STATE_FAILED);
	} else {
	    	// set server status to StateManageable.STATE_RUNNING
	    	dsh.setstate(serverName, StateManageable.STATE_RUNNING);
	}
    }



    /**
     * stop cascading for the given instance and jmx connector
     */

    void stopCascadeInstance(final String serverName) {
        StopCascadeInstanceThread scit = new StopCascadeInstanceThread(serverName);
        synchronized (threadMap) {
	    if (threadMap.containsKey(serverName)) {
	        threadMap.remove(serverName);
	    }
	    threadMap.put(serverName, scit);
        }
        scit.start();
    }

    void stopCascadeInstanceT(final String serverName) {

        _logger.log(Level.FINE, "CascadingLifecycleImpl.stopCascadeInstance", serverName);

        ObjectName cascadingAgentName = cMap.remove(serverName);

        ASProxyCascadingAgent aspca = cagentMap.remove(serverName);

	MBeanServer mbs = MBeanServerFactory.getMBeanServer();

        // The stopping of cascading for remote instance
        // is done in a separate thread so that the calling process 
        // is not blocked
	if (cascadingAgentName != null) {
	            boolean mfail = false;
                    try {
	                // stop cascading
	                // mbs.invoke(cascadingAgentName, "stop", null, null);
		        // unregister mbean
                        synchronized (aspca) {
		            mbs.unregisterMBean(cascadingAgentName);
                        }

		    } catch(javax.management.InstanceNotFoundException infe) {
	                _logger.log(Level.FINE, "cascading.cascadeInstance_InstanceNotFound", infe);
		        mfail = true;
		    } catch(javax.management.MBeanRegistrationException mbre) {
	                _logger.log(Level.FINE, 
                            "cascading.cascadeInstance_mBeanRegistrationException", mbre);
		        mfail = true;
		    } catch(javax.management.MBeanException mbe) {
	                _logger.log(Level.FINE, "cascading.cascadeInstance_mBeanException", mbe);
		        mfail = true;
	            }
	            if (mfail) {
		            // set server status to StateManageable.STATE_FAILED
		            dsh.setstate(serverName, StateManageable.STATE_FAILED);
	            } else {
		            // set server status to StateManageable.STATE_STOPPED
		            dsh.setstate(serverName, StateManageable.STATE_STOPPED);
	            }
	}

    }


    /*
     * When a remote instance comes up it invokes this mBean
     * to trigger its' cascading on to DAS
     */
    private void registerInstanceCascadingMBean(ServerContext sc) {

        _logger.log(Level.FINE, "CascadingLifecycleImpl.registerInstanceCascadingMBean");

        String domain = sc.getDefaultDomainName();
	MBeanServer mbs = MBeanServerFactory.getMBeanServer();

	try {
	    icmbON = new ObjectName(
		getInstanceCascadingMBeanObjName(sc, sc.getInstanceName()));
	    InstanceCascadingMBean icmb = new InstanceCascadingMBean();
	    ObjectInstance icmbInstance = mbs.registerMBean(icmb, icmbON);
	} catch(MalformedObjectNameException e) {
            e.printStackTrace();
            _logger.log(Level.WARNING, "cascading.cascadeInstance_malformedObjectName", e);
	} catch(javax.management.InstanceAlreadyExistsException iae) {
            iae.printStackTrace();
            _logger.log(Level.WARNING, "cascading.cascadeInstance_InstanceAlreadyExists", iae);
	} catch(javax.management.MBeanRegistrationException mre) {
            mre.printStackTrace();
            _logger.log(Level.WARNING, "cascading.cascadeInstance_mBeanRegistrationException", mre);
	} catch(javax.management.NotCompliantMBeanException ncmbe) {
	    ncmbe.printStackTrace();
            _logger.log(Level.WARNING, "cascading.cascadeInstance_NotCompliantMBeanException", ncmbe);
	}
    }

    /*
     * When a remote instance stops it invokes this mBean
     * to stop its' cascading on to DAS
     */
    private void unregisterInstanceCascadingMBean() {

        _logger.log(Level.FINE, "CascadingLifecycleImpl.unregisterInstanceCascadingMBean");

	MBeanServer mbs = MBeanServerFactory.getMBeanServer();
	try {
	    if (icmbON != null) {
		// unregister mbean
		mbs.unregisterMBean(icmbON);
	    }
	} catch(javax.management.InstanceNotFoundException iae) {
            iae.printStackTrace();
            _logger.log(Level.FINE, "cascading.cascadeInstance_InstanceAlreadyExists", iae);
	} catch(javax.management.MBeanRegistrationException mre) {
            mre.printStackTrace();
            _logger.log(Level.FINE, "cascading.cascadeInstance_mBeanRegistrationException", mre);
	}
    }


    /*
     * Invoke InstanceCascadingMBean on DAS
     */
    private void invokeDASCascading(ServerContext sc) {

        _logger.log(Level.FINE, "CascadingLifecycleImpl.invokeDASCascading");

	// get the jmx connector for the DAS
	// and invoke InstanceCascadingMBean on DAS

	ConfigContext configContext = sc.getConfigContext();
	if (! isDASRunning(configContext) ) {
		_logger.log(Level.INFO, "cascading.noCascadingNeededStartup");
		return;
	}

	try {

	    // get DAS server instance name
	    Server dasServer = ServerHelper.getDAS(configContext);
	    String dasInstanceName = dasServer.getName();

	    // get MBeanServerConnection
            MBeanServerConnection mbsc = 
		ServerHelper.connect(configContext, dasInstanceName);

            // invoke InstanceCascadingMBean on DAS
	    ObjectName on = new ObjectName(
		getInstanceCascadingMBeanObjName(sc, dasInstanceName));

	    String[] signature = {"java.lang.String"};
	    Object[] param=new Object[1];
	    param[0]=(Object) sc.getInstanceName();

	    mbsc.invoke(on, "cascadeInstance", param, signature);

	} catch(MalformedObjectNameException mone) {
            mone.printStackTrace();
            _logger.log(Level.WARNING, "cascading.cascadeInstance_malformedObjectName", mone);
	} catch (ConfigException ce) {
	    // connector for this instance not available in the config
	    ce.printStackTrace();
	    _logger.log(Level.WARNING, "cascading.get_server_connector_config_error", ce);
	} catch (java.io.IOException ioe) {
	    // connection cannot be made to this instance
	    ioe.printStackTrace();
	    _logger.log(Level.WARNING, "cascading.get_server_connector_config_error", ioe);
	} catch(javax.management.InstanceNotFoundException infe) {
	    infe.printStackTrace();
            _logger.log(Level.WARNING, "cascading.cascadeInstance_InstanceNotFound", infe);
	} catch(javax.management.MBeanException mbe) {
	    mbe.printStackTrace();
            _logger.log(Level.WARNING, "cascading.cascadeInstance_mBeanException", mbe);
	} catch(javax.management.ReflectionException re) {
	    re.printStackTrace();
            _logger.log(Level.WARNING, "cascading.cascadeInstance_ReflectionException", re);
	}

    }


    /*
     * Stop InstanceCascadingMBean on DAS
     */
    private void stopDASCascading() {

        _logger.log(Level.FINE, "CascadingLifecycleImpl.stopDASCascading");

	// get the jmx connector for the DAS
	// and invoke InstanceCascadingMBean on DAS

	ServerContext sc = ApplicationServer.getServerContext();
	ConfigContext configContext = sc.getConfigContext();
	if (! isDASRunning(configContext) ) {
		_logger.log(Level.FINE, "cascading.dasisnotrunning");
		return;
	}    
	try {

	    // get DAS server instance name
	    Server dasServer = ServerHelper.getDAS(configContext);
	    String dasInstanceName = dasServer.getName();

	    // get MBeanServerConnection
            MBeanServerConnection mbsc = 
		ServerHelper.connect(configContext, dasInstanceName);

            // invoke InstanceCascadingMBean on DAS
	    ObjectName on = new ObjectName(
		getInstanceCascadingMBeanObjName(sc, dasInstanceName));

	    String[] signature = {"java.lang.String"};
	    Object[] param=new Object[1];
	    param[0]=(Object) sc.getInstanceName();

	    mbsc.invoke(on, "stopCascadeInstance", param, signature);

	} catch(MalformedObjectNameException mone) {
            mone.printStackTrace();
            _logger.log(Level.WARNING, "cascading.cascadeInstance_malformedObjectName", mone);
	} catch (ConfigException ce) {
	    // connector for this instance not available in the config
	    ce.printStackTrace();
	    _logger.log(Level.WARNING, "cascading.get_server_connector_config_error", ce);
	} catch (java.io.IOException ioe) {
	    // connection cannot be made to this instance
	    ioe.printStackTrace();
	    _logger.log(Level.WARNING, "cascading.get_server_connector_config_error", ioe);
	} catch(javax.management.InstanceNotFoundException infe) {
	    infe.printStackTrace();
            _logger.log(Level.WARNING, "cascading.cascadeInstance_InstanceNotFound", infe);
	} catch(javax.management.MBeanException mbe) {
	    mbe.printStackTrace();
            _logger.log(Level.WARNING, "cascading.cascadeInstance_mBeanException", mbe);
	} catch(javax.management.ReflectionException re) {
	    re.printStackTrace();
            _logger.log(Level.WARNING, "cascading.cascadeInstance_ReflectionException", re);
	}

    }

    /**
     * return mbean object name for InstanceCascadingMBean
     */
    private String getInstanceCascadingMBeanObjName(ServerContext sc, String instanceName) {
	    return (sc.getDefaultDomainName() +
		":type=Cascading,serverName=" +
		instanceName +
		",name=instanceCascadingMBean");
    }

    private boolean isDASRunning(final ConfigContext ctx) {
        boolean running = false;
        try {
            final MBeanServerConnection mbsc = InstanceRegistry.getDASConnection(ctx);
            mbsc.getDefaultDomain(); // not interested in the value
            running = true;
        } catch (final Exception e) {
            //this exception is squelched on purpose and it is assumed that DAS is not running
        }
        return ( running );
    }

    void cascadeInstance(String domain, String serverName, JMXConnector jmxConnector) {

        // check and wait for the completion of previous stop thread
        Thread t = null;
        synchronized (threadMap) {
	    if (threadMap.containsKey(serverName)) {
	        t = threadMap.remove(serverName);
	    }
        }
        if ((t != null)  && (t.isAlive())) {
            try {
                t.join(THREAD_WAIT_TIME);
            } catch (java.lang.InterruptedException ie) {
                ie.printStackTrace();
            }
        }

        // check if the instance is already cascaded
        synchronized (cMap) {
	    if (cMap.containsKey(serverName)) {
                return;
            } else {
                setCMap(serverName, null);
            }
        }

        CascadeInstanceThread cit = new CascadeInstanceThread(domain, serverName, jmxConnector);
        cit.submit(RunnableBase.HowToRun.RUN_IN_SEPARATE_THREAD);
    }

    /**
     * run cascadeinstance in a separate thread
     * to see if it improves performance
     */
    private final class CascadeInstanceThread extends RunnableBase {

        final String mDomain;
        final String mServerName;
        final JMXConnector mJMXConnector;

        public CascadeInstanceThread (String domain, String serverName, JMXConnector jmxConnector) {
            mDomain = domain;
            mServerName = serverName;
            mJMXConnector = jmxConnector;
        }

        protected void doRun() throws Exception {
            cascadeInstanceT(mDomain, mServerName, mJMXConnector);
        }
    }

    /**
     * stop cascadeinstance in a separate thread
     */
    private final class StopCascadeInstanceThread extends Thread {
        final String mServerName;

        public StopCascadeInstanceThread(String serverName) {
            mServerName = serverName;
        }

        public void run() {
            stopCascadeInstanceT(mServerName);
        }
    }

}
