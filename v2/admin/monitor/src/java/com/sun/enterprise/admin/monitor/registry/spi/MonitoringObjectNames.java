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

/* MonitoringObjectNames.java
 * $Id: MonitoringObjectNames.java,v 1.12 2006/11/17 22:07:19 llc Exp $
 * $Revision: 1.12 $
 * $Date: 2006/11/17 22:07:19 $
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

import java.util.Hashtable;
import java.util.logging.Logger;
import javax.management.ObjectName;
import com.sun.enterprise.admin.monitor.registry.MonitoredObjectType;
import com.sun.enterprise.admin.common.constant.AdminConstants; // for logger name
import com.sun.enterprise.server.ApplicationServer; // for instance name
import com.sun.enterprise.admin.server.core.AdminService; // for domain
import com.sun.enterprise.admin.AdminContext; // for domain

/**
 * A class to provide the ObjectNames of various monitoring entities. It has a bunch
 * of static methods to generate the ObjectName.
 * @author  <a href="mailto:Kedar.Mhaswade@sun.com">Kedar Mhaswade</a>
 * @since S1AS8.0
 * @version $Revision: 1.12 $
 */
final class MonitoringObjectNames {
	
	private static final String SERVER_ID = ApplicationServer.getServerContext().getInstanceName();
    private static final Logger logger = Logger.getLogger(AdminConstants.kLoggerName);
	private static final String CATEGORY_PROPERTY		= "category";
	private static final String CATEGORY_PROPERTY_VAL	= "monitor";
	private static final String SERVER_PROPERTY			= "server";
	
	private MonitoringObjectNames() {
		//disallow
	}
	private static ObjectName formObjectName(Hashtable t) {	

		final String domain = getDomainName();
		t.put(CATEGORY_PROPERTY,  CATEGORY_PROPERTY_VAL);
		t.put(SERVER_PROPERTY,  SERVER_ID);
		
		ObjectName on = null;
		try {
			on = new ObjectName(domain, t);
			logger.fine("MonitoringObjectNames:formObjectName - ObjectName = " + on.toString());
		}
		catch (Exception cause) {
			//actually give one more chance if cause is an instance of MalformedObjectNameException - TBD
			throw new RuntimeException(cause);
		}
		return ( on ); // may not return null
	}
	
	private static String getDomainName() {
		final AdminContext ac = AdminService.getAdminService().getAdminContext();
		return ( ac.getDomainName() );
	}
	
	static ObjectName getRootObjectName() {
		final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.ROOT.getTypeName());
		return ( formObjectName(t) );
	}
	
	static ObjectName getEjbObjectName( MonitoredObjectType type, String ejb, String module, String app) {
		final Hashtable t = new Hashtable();
		t.put("type", type.getTypeName());
		t.put("name", ejb);
		if (app != null) {
			t.put(MonitoredObjectType.APPLICATION.getTypeName(), app);
			t.put(MonitoredObjectType.EJBMODULE.getTypeName(), module);
		}
		else {
			t.put(MonitoredObjectType.STANDALONE_EJBMODULE.getTypeName(), module);
		}
		return ( formObjectName(t) );
	}

/*
	static ObjectName getWebServiceObjectNameForWeb(
	    String endpoint, 
        String module,
        String app) { 
		final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.WEBSERVICE_ENDPOINT.getTypeName());
		t.put("name", endpoint);
		if (app != null) {
			t.put(MonitoredObjectType.APPLICATION.getTypeName(), app);
			t.put(MonitoredObjectType.WEBMODULE.getTypeName(), module);
		}
		else {
			t.put(MonitoredObjectType.STANDALONE_WEBMODULE.getTypeName(), 
                    module);
		}
		return ( formObjectName(t) );
	}
*/
	    static ObjectName
	getWebServiceObjectNameForWeb(
	    final String endpoint, 
        final String app,
        final String ctxRoot,
        final String virtualServer ) { 
		final Hashtable t = new Hashtable();
		
		// this is the webmodule-virtual-server it belongs to
		final ObjectName virtualServerObjectName   =
		    getVirtualServerObjectName( app, ctxRoot, virtualServer );
		
		t.put("type", MonitoredObjectType.WEBSERVICE_ENDPOINT.getTypeName());
		t.put("name", endpoint);
		
		// form pair based on virtual server type/name
        t.put(
            virtualServerObjectName.getKeyProperty( "type" ),
            virtualServerObjectName.getKeyProperty( "name" ) );
            
        final String appPropName = MonitoredObjectType.APPLICATION.getTypeName();
        String appName = virtualServerObjectName.getKeyProperty( appPropName );
        if (appName == null) {
            appName = "null";
        }
        t.put(appPropName, appName);

		return formObjectName(t);
	}

	static ObjectName getWebServiceObjectNameForEjb(String endpoint, 
        String module, String app) { 
		final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.WEBSERVICE_ENDPOINT.getTypeName());
		t.put("name", endpoint);
		if (app != null) {
			t.put(MonitoredObjectType.APPLICATION.getTypeName(), app);
			t.put(MonitoredObjectType.EJBMODULE.getTypeName(), module);
		}
		else {
			t.put(MonitoredObjectType.STANDALONE_EJBMODULE.getTypeName(), 
                    module);
		}
		return ( formObjectName(t) );
	}

	
	static ObjectName getEjbMethodsObjectName(String app, String module, MonitoredObjectType ejbType, String ejb) {
		final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.BEAN_METHODS.getTypeName());
		t.put( ejbType.getTypeName(), ejb);
		if (app != null) {
			t.put(MonitoredObjectType.APPLICATION.getTypeName(), app);
			t.put(MonitoredObjectType.EJBMODULE.getTypeName(), module);
		}
		else {
			t.put(MonitoredObjectType.STANDALONE_EJBMODULE.getTypeName(), module);
		}
		return ( formObjectName(t) );
	}

	static ObjectName getEjbMethodObjectName(String method, MonitoredObjectType ejbType, String ejb, String module, String app) {
		final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.BEAN_METHOD.getTypeName());
		t.put("name", method);
		t.put( ejbType.getTypeName(), ejb);
		if (app != null) {
			t.put(MonitoredObjectType.APPLICATION.getTypeName(), app);
			t.put(MonitoredObjectType.EJBMODULE.getTypeName(), module);
		}
		else {
			t.put(MonitoredObjectType.STANDALONE_EJBMODULE.getTypeName(), module);
		}
		return ( formObjectName(t) );
	}
	static ObjectName getEjbPoolObjectName(MonitoredObjectType ejbType, String ejb, String module, String app) {
		final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.BEAN_POOL.getTypeName());
		t.put("name", MonitoredObjectType.BEAN_POOL.getTypeName());
		t.put( ejbType.getTypeName(), ejb);
		if (app != null) {
			t.put(MonitoredObjectType.APPLICATION.getTypeName(), app);
			t.put(MonitoredObjectType.EJBMODULE.getTypeName(), module);
		}
		else {
			t.put(MonitoredObjectType.STANDALONE_EJBMODULE.getTypeName(), module);
		}
		return ( formObjectName(t) );
	}
	static ObjectName getEjbCacheObjectName(MonitoredObjectType ejbType, String ejb, String module, String app) {
		final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.BEAN_CACHE.getTypeName());
		t.put("name", MonitoredObjectType.BEAN_CACHE.getTypeName());
		t.put( ejbType.getTypeName(), ejb);
		if (app != null) {
			t.put(MonitoredObjectType.APPLICATION.getTypeName(), app);
			t.put(MonitoredObjectType.EJBMODULE.getTypeName(), module);
		}
		else {
			t.put(MonitoredObjectType.STANDALONE_EJBMODULE.getTypeName(), module);
		}
		return ( formObjectName(t) );
	}
	static ObjectName getConnectionPoolObjectName(String name, MonitoredObjectType type) {
		final Hashtable t = new Hashtable();
		if (type == MonitoredObjectType.CONNECTOR_CONN_POOL)
			t.put("type", MonitoredObjectType.CONNECTOR_CONN_POOL.getTypeName());
		else
			t.put("type", MonitoredObjectType.JDBC_CONN_POOL.getTypeName());
		t.put("name", name);
		return ( formObjectName(t) );
	}
	
	static ObjectName getOrbConnectionManagerObjectName(String name) {
		final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.CONNECTION_MANAGER.getTypeName());
		t.put("name", name);
		return ( formObjectName(t) );
	}
    static ObjectName getThreadPoolObjectName(String name) {
		final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.THREAD_POOL.getTypeName());
		t.put("name", name);
		return ( formObjectName(t) );
	}
	static ObjectName getTransactionServiceObjectName() {
		final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.TRANSACTION_SERVICE.getTypeName());
		return ( formObjectName(t) );
	}
	static ObjectName getJvmObjectName() {
		final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.JVM.getTypeName());
		return ( formObjectName(t) );
	}
        static ObjectName getJndiObjectName() {
                final Hashtable t = new Hashtable();
                t.put("type", MonitoredObjectType.JNDI.getTypeName());
                return ( formObjectName(t) );
        }
	static ObjectName getApplicationsObjectName() {
		final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.APPLICATIONS.getTypeName());
		return ( formObjectName(t) );
	}
	static ObjectName getThreadPoolsObjectName() {
		final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.THREAD_POOLS.getTypeName());
		return ( formObjectName(t) );
	}
	static ObjectName getOrbObjectName() {
		final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.ORB.getTypeName());
		return ( formObjectName(t) );
	}
	static ObjectName getHttpServiceObjectName() {
		final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.HTTP_SERVICE.getTypeName());
		return ( formObjectName(t) );
	}
	static ObjectName getResourcesObjectName() {
		final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.RESOURCES.getTypeName());
		return ( formObjectName(t) );
	}
	static ObjectName getConnectionManagersObjectName() {
		final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.CONNECTION_MANAGERS.getTypeName());
		return ( formObjectName(t) );
	}
	static ObjectName getVirtualServerObjectName(String vs) {
		final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.VIRTUAL_SERVER.getTypeName());
		t.put("name", vs);
		return ( formObjectName(t) );
	}
	static ObjectName getHttpListenerObjectName(String vs, String ls) {
		final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.HTTP_LISTENER.getTypeName());
		t.put(MonitoredObjectType.VIRTUAL_SERVER.getTypeName(), vs);
		t.put("name", ls);
		return ( formObjectName(t) );
	}
	static ObjectName getApplicationObjectName(String app) {
		final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.APPLICATION.getTypeName());
		t.put("name", app);
		return ( formObjectName(t) );
	}
	static ObjectName getEjbModuleObjectName(String app, String module) {
		final Hashtable t = new Hashtable();
		t.put("name", module);
		if (app != null) {
			t.put(MonitoredObjectType.APPLICATION.getTypeName(), app);
			t.put("type", MonitoredObjectType.EJBMODULE.getTypeName());
		}
		else
			t.put("type", MonitoredObjectType.STANDALONE_EJBMODULE.getTypeName());
		return ( formObjectName(t) );
	}
	static ObjectName getWebModuleObjectName(String app, String ctxRoot,
                                                 String vs) {

                String module = "//" + ((vs==null) ? "DEFAULT" : vs) +
                    (("".equals(ctxRoot)) ? "/" : ctxRoot);

		final Hashtable t = new Hashtable();
		t.put("name", module);

		if (app != null) {
			t.put(MonitoredObjectType.APPLICATION.getTypeName(), app);
			t.put("type", MonitoredObjectType.WEBMODULE.getTypeName());
		}
		else
			t.put("type", MonitoredObjectType.STANDALONE_WEBMODULE.getTypeName());
		return ( formObjectName(t) );
	}
	
	static ObjectName getVirtualServerObjectName(String app,
                                                     String ctxRoot,
                                                     String vs) {

        String module = "//" + ((vs==null) ? "DEFAULT" : vs) +
            (("".equals(ctxRoot)) ? "/" : ctxRoot);

		final Hashtable t = new Hashtable();
		t.put("name", module);
        t.put("type", MonitoredObjectType.WEBAPP_VIRTUAL_SERVER.getTypeName());

		if (app != null) {
			t.put(MonitoredObjectType.APPLICATION.getTypeName(), app);
		}
		return ( formObjectName(t) );
	}
	
	static ObjectName getServletObjectName(String app, String ctxRoot,
                                               String vs, String servlet) {
        
        final ObjectName    virtualServerObjectName =
            getVirtualServerObjectName( app, ctxRoot, vs );

		final Hashtable t = new Hashtable();
		t.put("name", servlet);
		t.put("type", MonitoredObjectType.SERVLET.getTypeName());
        t.put(MonitoredObjectType.WEBAPP_VIRTUAL_SERVER.getTypeName(),
            virtualServerObjectName.getKeyProperty( "name" ));

		if (app != null) {
			t.put(MonitoredObjectType.APPLICATION.getTypeName(), app);
		}
		return ( formObjectName(t) );
	}
    
    // Connector modules, related changes
    static ObjectName getConnectorServiceObjectName() {
        
        final Hashtable t = new Hashtable();
        t.put("type",MonitoredObjectType.CONNECTOR_SERVICE.getTypeName());
        return (formObjectName(t));
    }
    
    static ObjectName getJmsServiceObjectName() {
        
        final Hashtable t = new Hashtable();
        t.put("type",MonitoredObjectType.JMS_SERVICE.getTypeName());
        return (formObjectName(t));
    }
    
    static ObjectName getConnectorModuleObjectName(String j2eeAppName, String moduleName) {
        
        final Hashtable t = new Hashtable();
        
        if (j2eeAppName != null) {
            t.put("name", j2eeAppName + "#" + moduleName);
            t.put("type", MonitoredObjectType.CONNECTOR_MODULE.getTypeName());
        }
        else {
            t.put("name", moduleName);
            t.put("type", MonitoredObjectType.STANDALONE_CONNECTOR_MODULE.getTypeName());
        }
        return( formObjectName(t));
    }
    
    static ObjectName getConnectorWorkMgmtObjectName(String j2eeAppName, String moduleName, boolean isJms) {
        
        final Hashtable t = new Hashtable();
        
        t.put("type", MonitoredObjectType.CONNECTOR_WORKMGMT.getTypeName());
        if(!isJms) {
            if(j2eeAppName != null) {
                t.put(MonitoredObjectType.CONNECTOR_MODULE.getTypeName(), j2eeAppName + "#" + moduleName);
            } else
            {
                t.put(MonitoredObjectType.STANDALONE_CONNECTOR_MODULE.getTypeName(), moduleName);
            }
        }
        return (formObjectName(t));
    }
    
    static ObjectName getConnectionFactoriesObjectName() {
        final Hashtable t = new Hashtable();
        t.put("type", MonitoredObjectType.CONNECTION_FACTORIES.getTypeName());
        return(formObjectName(t));
    }
    
    static ObjectName getConnectionFactoryObjectName(String factoryName) {
        final Hashtable t = new Hashtable();
        t.put("type", MonitoredObjectType.CONNECTION_FACTORY.getTypeName());
        t.put("name", factoryName);
        return(formObjectName(t));
    }
    
    static ObjectName getConnectionPoolsObjectName(String j2eeAppName, String moduleName) {
        final Hashtable t = new Hashtable();
        t.put("type", MonitoredObjectType.CONNECTION_POOLS.getTypeName());
        if(j2eeAppName != null) {
            t.put(MonitoredObjectType.CONNECTOR_MODULE.getTypeName(), j2eeAppName + "#" + moduleName);
        } else
        {
            t.put(MonitoredObjectType.STANDALONE_CONNECTOR_MODULE.getTypeName(), moduleName);
        }
        return(formObjectName(t));
    }
    
    static ObjectName getConnectionPoolObjectName(String poolName, String j2eeAppName, String moduleName) {
        final Hashtable t = new Hashtable();
        t.put("type", MonitoredObjectType.CONNECTOR_CONN_POOL.getTypeName());
        t.put("name", poolName);
        if(j2eeAppName != null) {
            t.put(MonitoredObjectType.CONNECTOR_MODULE.getTypeName(), j2eeAppName + "#" + moduleName);
        } else
        {
            t.put(MonitoredObjectType.STANDALONE_CONNECTOR_MODULE.getTypeName(), moduleName);
        }
        return(formObjectName(t));
    }
    
    // PWC integration related changes
    static ObjectName getConnectionQueueObjectName() {
        
        final Hashtable t = new Hashtable();
        t.put("type", MonitoredObjectType.CONNECTION_QUEUE.getTypeName());
        return(formObjectName(t));
    }
    
    static ObjectName getDnsObjectName() {
        
        final Hashtable t = new Hashtable();
        t.put("type", MonitoredObjectType.DNS.getTypeName());
        return(formObjectName(t));
    }
    
    static ObjectName getKeepAliveObjectName() {
        
        final Hashtable t = new Hashtable();
        t.put("type", MonitoredObjectType.KEEP_ALIVE.getTypeName());
        return(formObjectName(t));
    }
    
    static ObjectName getPWCThreadPoolObjectName() {
        
        final Hashtable t = new Hashtable();
        t.put("type", MonitoredObjectType.PWC_THREAD_POOL.getTypeName());
        return(formObjectName(t));
    }
    
    static ObjectName getFileCacheObjectName() {
        
        final Hashtable t = new Hashtable();
        t.put("type", MonitoredObjectType.FILE_CACHE.getTypeName());
        return(formObjectName(t));
    }
    
    static ObjectName getRequestObjectName(String vsId) {
        
        final Hashtable t = new Hashtable();
        t.put("type", MonitoredObjectType.REQUEST.getTypeName());
        t.put(MonitoredObjectType.VIRTUAL_SERVER.getTypeName(), vsId);
        return(formObjectName(t));
    }
    
    static ObjectName getStatefulSessionStoreObjectName( MonitoredObjectType ejbType, String ejbName, String moduleName, String j2eeAppName) {
        
        final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.SESSION_STORE.getTypeName());
		t.put("name", MonitoredObjectType.SESSION_STORE.getTypeName());
		t.put( ejbType.getTypeName(), ejbName);
		if (j2eeAppName != null) {
			t.put(MonitoredObjectType.APPLICATION.getTypeName(), j2eeAppName);
			t.put(MonitoredObjectType.EJBMODULE.getTypeName(), moduleName);
		}
		else {
			t.put(MonitoredObjectType.STANDALONE_EJBMODULE.getTypeName(), moduleName);
		}
		return ( formObjectName(t) );
    }
    
    static ObjectName getTimerObjectName(MonitoredObjectType ejbType, String ejbName, String moduleName, String j2eeAppName) {
        
        final Hashtable t = new Hashtable();
		t.put("type", MonitoredObjectType.TIMERS.getTypeName());
		t.put("name", MonitoredObjectType.TIMERS.getTypeName());
		t.put( ejbType.getTypeName(), ejbName);
		if (j2eeAppName != null) {
			t.put(MonitoredObjectType.APPLICATION.getTypeName(), j2eeAppName);
			t.put(MonitoredObjectType.EJBMODULE.getTypeName(), moduleName);
		}
		else {
			t.put(MonitoredObjectType.STANDALONE_EJBMODULE.getTypeName(), moduleName);
		}
		return ( formObjectName(t) );
    }
    
    // JVM1.5 related changes - BEGIN
    static ObjectName getJVMCompilationObjectName() {
        
        final Hashtable t = new Hashtable();
        t.put("type", MonitoredObjectType.JVM_COMPILATION.getTypeName());
        t.put("name", MonitoredObjectType.JVM_COMPILATION.getTypeName());
        return(formObjectName(t));
    }
    
    static ObjectName getJVMClassLoadingObjectName() {
        
        final Hashtable t = new Hashtable();
        t.put("type", MonitoredObjectType.JVM_CLASSLOADING.getTypeName());
        t.put("name", MonitoredObjectType.JVM_CLASSLOADING.getTypeName());
        return(formObjectName(t));
    }
    
    static ObjectName getJVMRuntimeObjectName() {
        
        final Hashtable t = new Hashtable();
        t.put("type", MonitoredObjectType.JVM_RUNTIME.getTypeName());
        t.put("name", MonitoredObjectType.JVM_RUNTIME.getTypeName());
        return(formObjectName(t));
    }
    
    static ObjectName getJVMOSObjectName() {
        
        final Hashtable t = new Hashtable();
        t.put("type", MonitoredObjectType.JVM_OS.getTypeName());
        t.put("name", MonitoredObjectType.JVM_OS.getTypeName());
        return(formObjectName(t));
    }
    
    
    static ObjectName getJVMGCSSObjectName() {
        
        final Hashtable t = new Hashtable();
        t.put("type", MonitoredObjectType.JVM_GCS.getTypeName());
        t.put("name", MonitoredObjectType.JVM_GCS.getTypeName());
        return(formObjectName(t));
    }
    
    
    static ObjectName getJVMGCObjectName(String gcName) {
        
        final Hashtable t = new Hashtable();
        t.put("type", MonitoredObjectType.JVM_GC.getTypeName());
        t.put("name", gcName);
        return(formObjectName(t));
    }
    
    static ObjectName getJVMMemoryObjectName() {
        
        final Hashtable t = new Hashtable();
        t.put("type", MonitoredObjectType.JVM_MEMORY.getTypeName());
        t.put("name", MonitoredObjectType.JVM_MEMORY.getTypeName());
        return(formObjectName(t));
    }
    
    static ObjectName getJVMThreadObjectName() {
        
        final Hashtable t = new Hashtable();
        t.put("type", MonitoredObjectType.JVM_THREAD.getTypeName());
        t.put("name", MonitoredObjectType.JVM_THREAD.getTypeName());
        return(formObjectName(t));
    }
    
    static ObjectName getJVMThreadInfoObjectName(String threadName) {
        
        final Hashtable t = new Hashtable();
        t.put("type", MonitoredObjectType.JVM_THREAD_INFO.getTypeName());
        t.put("name", threadName);
        return(formObjectName(t));
    }
    // JVM1.5 related changes - END
}
