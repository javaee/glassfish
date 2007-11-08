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
 * DottedNameFactory.java
 * $Id: DottedNameFactory.java,v 1.3 2005/12/25 03:43:32 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:43:32 $
 * Indentation Information:
 * 0. Please (try to) preserve these settings.
 * 1. Tabs are preferred over spaces.
 * 2. In vi/vim -
 *		:set tabstop=4 :set shiftwidth=4 :set softtabstop=4
 * 3. In S1 Studio -
 *		1. Tools->Options->Editor Settings->Java Editor->Tab Size = 4
 *		2. Tools->Options->Indentation Engines->Java Indentation Engine->Expand Tabs to Spaces = False.
 *		3. Tools->Options->Indentation Engines->Java Indentation Engine->Number of Spaces per Tab = 4.
 *
 * Created on September 4, 2003, 10:25 AM
 */

package com.sun.enterprise.admin.monitor.registry.spi;

import com.sun.enterprise.admin.monitor.registry.*;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.config.ConfigException;
import java.util.logging.*;
import com.sun.enterprise.admin.common.constant.AdminConstants; // for logger name
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.admin.dottedname.DottedName;

/**
 * Provides methods to get DottedNames pertaining to various 
 * components in the monitoring tree hierarchy
 * @author  Shreedhar Ganapathy<mailto:shreedhar.ganapathy@sun.com>
 * @revision $Revision: 1.3 $
 */
class DottedNameFactory {
    private static final Logger logger = Logger.getLogger(AdminConstants.kLoggerName);	
	private static final StringManager sm = StringManager.getManager(DottedNameFactory.class);
	private static String instanceName =null;
	private static final String DELIMITER = ".";
	
	private static String getInstanceName(){
		if(instanceName == null){
			try{
				instanceName = (ApplicationServer.getServerContext()).getInstanceName();
				if(instanceName==null) {
					final String msg = sm.getString("instance_name_not_found");
					throw new NullPointerException(msg);
				}
			}
			catch(Exception e){
				logger.fine("DottedNameFactory:"+e.getClass().getName());
				throw new RuntimeException(e);
			}
		}
		return instanceName;
	}

	static String getRootDottedName(){
		return getInstanceName();
	}	
	
	static String getApplicationsDottedName(){
		return getInstanceName() + DELIMITER +  MonitoredObjectType.APPLICATIONS;
	}	
	
	static String getResourcesDottedName(){
		return getInstanceName() + DELIMITER + MonitoredObjectType.RESOURCES;
	}
	
	static String getOrbDottedName(){
		return getInstanceName() + DELIMITER + MonitoredObjectType.ORB;	
	}
	
	static String getTransactionServiceDottedName(){
		return getInstanceName() + DELIMITER + MonitoredObjectType.TRANSACTION_SERVICE;
	}
	
	static String getThreadPoolsDottedName(){
		return getInstanceName() + DELIMITER + MonitoredObjectType.THREAD_POOLS;
	}
	
	static String getThreadPoolDottedName(String name){
		name = DottedName.escapePart(name);
		return getThreadPoolsDottedName() + DELIMITER + name;
	}
	
	static String getHttpServiceDottedName(){
		return getInstanceName() + DELIMITER + MonitoredObjectType.HTTP_SERVICE;		
	}
	
	static String getJVMDottedName(){
		return getInstanceName() + DELIMITER + MonitoredObjectType.JVM;		
	}
	
	static String getHttpSvcVirtualServerDottedName(String vs){
		vs = DottedName.escapePart(vs);
		return getHttpServiceDottedName() + DELIMITER + vs;
	}
	
	static String getHttpListenerDottedName(String listenerName, String vs){
		listenerName = DottedName.escapePart(listenerName);
		return getHttpSvcVirtualServerDottedName(vs) + DELIMITER + listenerName;
	}
		
	static String getConnectionManagersDottedName(){
		return getOrbDottedName() + DELIMITER + MonitoredObjectType.CONNECTION_MANAGERS;
	}

	static String getOrbConnectionManagerDottedName(String name){
		name = DottedName.escapePart(name);
		return getConnectionManagersDottedName() + DELIMITER + name;
	}
	
	static String getConnectionPoolDottedName(String poolName, String type){
		poolName = DottedName.escapePart(poolName);
		return getResourcesDottedName() + DELIMITER + poolName;
	}

	static String getStandAloneEJBModuleDottedName(String module){
		module = DottedName.escapePart(module);
		return getApplicationsDottedName() + DELIMITER + module;
	}
	
	static String getStandAloneWebModuleDottedName(String module){
		module = DottedName.escapePart(module);
		return getApplicationsDottedName() + DELIMITER + module;		
	}

	static String getAppDottedName(String app){
		app = DottedName.escapePart(app);
		return getApplicationsDottedName() + DELIMITER + app;
	}
	
	static String getAppModuleDottedName(String app, String module){
		module = DottedName.escapePart(module);
		return getAppDottedName(app) + DELIMITER + module;
	}
	
	static String getWebAppsVirtualServerDottedName(String app, String module,
    String vs){
		vs = DottedName.escapePart(vs);
		if(app == null){
			return getStandAloneWebModuleDottedName(module) + DELIMITER + vs;
		}
		return getAppModuleDottedName(app, module) + DELIMITER + vs;
	}
	
	static String getServletDottedName(String app, String module, 
		String vs, String servlet){		
		servlet = DottedName.escapePart(servlet);
		return getWebAppsVirtualServerDottedName(app, module, vs) + DELIMITER + servlet;
	}
	
	static String getEJBDottedName(String app, String module, String ejb){
		ejb = DottedName.escapePart(ejb);
		if(app == null){
			return getStandAloneEJBModuleDottedName(module) + DELIMITER + ejb;
		}
		return getAppModuleDottedName(app, module) + DELIMITER + ejb;
	}
	
	static String getEJBDottedNameWithType(String app, String module, String ejb, 
		String ejbType){
		ejb = DottedName.escapePart(ejb);
		if(app == null){
			return getStandAloneEJBModuleDottedName(module) + DELIMITER + ejbType + DELIMITER + ejb;
		}
		return getAppModuleDottedName(app, module) + DELIMITER + ejbType + DELIMITER + ejb;
	}
	
	static String getEJBCacheDottedName(String app, String module, String ejb){
		return getEJBDottedName(app, module, ejb) + DELIMITER + MonitoredObjectType.BEAN_CACHE;
	}
	
	static String getEJBPoolDottedName(String app, String module, String ejb){
		return getEJBDottedName(app, module, ejb) + DELIMITER + MonitoredObjectType.BEAN_POOL;
	}

	static String getEJBMethodsDottedName(String app, String module, String ejb){
		return getEJBDottedName(app, module, ejb) + DELIMITER + MonitoredObjectType.BEAN_METHODS;
	}	
	
	static String getEJBMethodDottedName(String app, String module, String ejb, String method){
		return getEJBMethodsDottedName(app,module,ejb) + DELIMITER + DottedName.escapePart(method);
	}	
       
    // connector & jms service, related changes
    static String getConnectorServiceDottedName() {
        return getInstanceName() + DELIMITER + MonitoredObjectType.CONNECTOR_SERVICE;
    }
    
    static String getJmsServiceDottedName() {
        return getInstanceName() + DELIMITER + MonitoredObjectType.JMS_SERVICE;
    }
    
    static String getConnectorModuleDottedName(String j2eeAppName, String moduleName) {
        if(j2eeAppName != null) {
           moduleName = DottedName.escapePart(j2eeAppName) + "#" + DottedName.escapePart(moduleName);
        }
        else 
            moduleName = DottedName.escapePart(moduleName);
		return getConnectorServiceDottedName() + DELIMITER + moduleName;
    }
    
    static String getConnectorWorkMgmtDottedName(String j2eeAppName, String moduleName, boolean isJms) {
        
        String dottedName = null;
        
        if(isJms) {
            dottedName = getJmsServiceDottedName() + DELIMITER + MonitoredObjectType.CONNECTOR_WORKMGMT;
        }
        else {
            dottedName = getConnectorModuleDottedName(j2eeAppName, moduleName) + DELIMITER + MonitoredObjectType.CONNECTOR_WORKMGMT;
        }
        return dottedName;
    }
    
    static String getConnectionFactoriesDottedName() {
        return getJmsServiceDottedName() + DELIMITER + MonitoredObjectType.CONNECTION_FACTORIES;
    }
    
    static String getConnectionFactoryDottedName(String factoryName) {
        return getConnectionFactoriesDottedName() + DELIMITER + DottedName.escapePart(factoryName);
    }
    
    static String getConnectionPoolsDottedName(String j2eeAppName, String moduleName) {
        return getConnectorModuleDottedName(j2eeAppName, moduleName) + DELIMITER + MonitoredObjectType.CONNECTION_POOLS;
    }
    
    static String getConnectionPoolDottedName(String poolName, String j2eeAppName, String moduleName){
        return getConnectionPoolsDottedName(j2eeAppName, moduleName) + DELIMITER + DottedName.escapePart(poolName);
    }
    
    // PWC integration, related changes
    static String getConnectionQueueDottedName() {
        return getHttpServiceDottedName() + DELIMITER + MonitoredObjectType.CONNECTION_QUEUE;
    }
    
    static String getDnsDottedName() {
        return getHttpServiceDottedName() + DELIMITER + MonitoredObjectType.DNS;
    }
    
    static String getKeepAliveDottedName() {
        return getHttpServiceDottedName() + DELIMITER + MonitoredObjectType.KEEP_ALIVE;
    }
    
    static String getPWCThreadPoolDottedName() {
        return getHttpServiceDottedName() + DELIMITER + MonitoredObjectType.PWC_THREAD_POOL;
    }
    
    static String getFileCacheDottedName() {
        return getHttpServiceDottedName() + DELIMITER + MonitoredObjectType.FILE_CACHE;
    }    
    
    static String getRequestDottedName(String vsId) {
        return getHttpSvcVirtualServerDottedName(vsId) + DELIMITER + MonitoredObjectType.REQUEST;
    }
    
    // SessionStore Monitoring related changes
    static String getStatefulSessionStoreDottedName(String ejbName, String moduleName, String j2eeAppName) {
        return getEJBDottedName(j2eeAppName, moduleName, ejbName) + DELIMITER + MonitoredObjectType.SESSION_STORE;
    }
    
    // Timer monitoring related changes
    static String getTimerDottedName(String ejbName, String moduleName, String j2eeAppName) {
        return getEJBDottedName(j2eeAppName, moduleName, ejbName) + DELIMITER + MonitoredObjectType.TIMERS;
    }
    
    // Web Services monitoring related changes
    static String getWebServiceAggregateStatsInEjbDottedName(
        String endpointName, String moduleName, String j2eeAppName) {

        return getWebServiceInEjbDottedName(j2eeAppName,moduleName,
            endpointName) + DELIMITER + MonitoredObjectType.WEBSERVICE_ENDPOINT;

    }

    static String getWebServiceAggregateStatsInWebDottedName(
        String endpointName, String moduleName, String j2eeAppName) {

        return getWebServiceInWebDottedName(j2eeAppName,moduleName,
            endpointName) + DELIMITER + MonitoredObjectType.WEBSERVICE_ENDPOINT;

    }

	static String getWebServiceInWebDottedName(String app,String module, String
        endpoint){

		endpoint = DottedName.escapePart(endpoint);
		if(app == null){
			return getStandAloneWebModuleDottedName(module) + DELIMITER +
            endpoint;
		}
		return getAppModuleDottedName(app, module) + DELIMITER + endpoint;
	}

	static String getWebServiceInEjbDottedName(String app,String module, String
        endpoint){

		endpoint = DottedName.escapePart(endpoint);
		if(app == null){
			return getStandAloneEJBModuleDottedName(module) + DELIMITER +
            endpoint;
		}
		return getAppModuleDottedName(app, module) + DELIMITER + endpoint;
	}
	
    // JVM1.5 related changes - BEGIN
    static String getJVMCompilationDottedName() {
        return getJVMDottedName() + DELIMITER + MonitoredObjectType.JVM_COMPILATION;
    }
    
    static String getJVMClassLoadingDottedName() {
        return getJVMDottedName() + DELIMITER + MonitoredObjectType.JVM_CLASSLOADING;
    }
    
    static String getJVMRuntimeDottedName() {
        return getJVMDottedName() + DELIMITER + MonitoredObjectType.JVM_RUNTIME;
    }
    
    static String getJVMOSDottedName() {
        return getJVMDottedName() + DELIMITER + MonitoredObjectType.JVM_OS;
    }
    
    static String getJVMGCSDottedName() {
        return getJVMDottedName() + DELIMITER + MonitoredObjectType.JVM_GCS;
    }
    
    static String getJVMGCDottedName(String gcName) {
        return getJVMGCSDottedName() + DELIMITER + DottedName.escapePart(gcName);
    }
    
    static String getJVMMemoryDottedName() {
        return getJVMDottedName() + DELIMITER + MonitoredObjectType.JVM_MEMORY;
    }
        
    static String getJVMThreadDottedName() {
        return getJVMDottedName() + DELIMITER + MonitoredObjectType.JVM_THREAD;
    }
    
    static String getJVMThreadInfoDottedName(String threadName) {
        return getJVMThreadDottedName() + DELIMITER + DottedName.escapePart(threadName);
    }

    // JVM1.5 related changes - END
}
