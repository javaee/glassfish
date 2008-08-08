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

package com.sun.appserv.management;

import com.sun.appserv.management.base.*;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.ext.lb.LoadBalancer;
import com.sun.appserv.management.ext.update.UpdateStatus;
import com.sun.appserv.management.ext.wsmgmt.WebServiceMgr;
import com.sun.appserv.management.j2ee.J2EEDomain;
import com.sun.appserv.management.monitor.JMXMonitorMgr;
import com.sun.appserv.management.monitor.MonitoringRoot;

import com.sun.appserv.management.ext.realm.RealmsMgr;
import com.sun.appserv.management.ext.runtime.RuntimeMgr;

import java.util.Map;

/**
	The top-level interface for an appserver domain. Access to all other
	{@link AMX} begins here.
    <p>
    The 'name' property in the ObjectName of DomainRoot is the name of the
    appserver domain.  For example, appserver domains 'domain' and 'domain2' would
    have ObjectNames for DomainRoot as follows:
    <pre>
    amx:j2eeType=X-DomainRoot:name=domain1
    amx:j2eeType=X-DomainRoot:name=domain2
    </pre>
    Of course, these two MBeans would normally be found in different MBeanServers.
 */
public interface DomainRoot extends Container
{
    public final static String		J2EE_TYPE	= XTypes.DOMAIN_ROOT;

    /**
      Return the name of this appserver domain.  Not to be confused with the
      JMX domain name, which may be derived from this name and is
      available from any ObjectName in AMX by calling
      {@link Util#getObjectName}

      The domain name is equivalent to the name of
      the directory containing the domain configuration.  This name
      is not part of the configuration and can only be changed by
      using a different directory to house the configuration for the
      domain.
      @return the name of the Appserver domain
     */
    public String	getAppserverDomainName();


    /**
      @return the JSR 77 J2EEDomain.
     */
    public J2EEDomain		getJ2EEDomain();

    /**
        @return the singleton DomainConfig
     */
    public DomainConfig	getDomainConfig();


    /**
        @return the singleton JMXMonitorMgr
     */
    public JMXMonitorMgr		getJMXMonitorMgr() ;

    /**
            Get the NotificationServiceMgr
     */
    public NotificationServiceMgr	getNotificationServiceMgr();

    /**
        @return the singleton SystemInfo
     */
    public SystemInfo		getSystemInfo();

    /**
        @return the singleton SystemInfo
     */
    public SystemStatus		getSystemStatus();
	
    /**
        @return the singleton KitchenSink
     */
    public KitchenSink		getKitchenSink();
	
    /**
       @return the singleton WebServiceMgr for this domain.
       @since AppServer 9.0
    */
    public WebServiceMgr getWebServiceMgr();

    /**
            Get all {@link NotificationEmitterService} instances.
            Possible kinds include those defined in {@link NotificationEmitterServiceKeys}.
            @since AppServer 9.0
     */
    public Map<String,NotificationEmitterService>
            getNotificationEmitterServiceMap();

    /**
        Get the NotificationEmitterService whose name is 
        {@link NotificationEmitterServiceKeys#DOMAIN_KEY}.  Same
        as calling <code>getNotificationEmitterServiceMap().get( DOMAIN_KEY )</code>.
       @return the singleton {@link NotificationEmitterService}.
     */
    public NotificationEmitterService 	getDomainNotificationEmitterService();


    /**
        @return the singleton {@link QueryMgr}.
     */
    public QueryMgr		getQueryMgr();

    /**
        @return the singleton {@link BulkAccess}.
     */
    public BulkAccess		getBulkAccess();

    /**
       @return the singleton {@link UploadDownloadMgr}.
     */
    public UploadDownloadMgr		getUploadDownloadMgr();

    /**
        @return the singleton {@link Pathnames}.
     */
    public Pathnames		getPathnames();
        
    /**
        @return the singleton {@link MonitoringRoot}.
     */
    public MonitoringRoot		getMonitoringRoot() ;

    /**
        Get all the {@link LoadBalancer} instances
        @return Map of items, keyed by name.
        @see LoadBalancer
        @see com.sun.appserv.management.config.LoadBalancerConfig
        @since AppServer 9.0
     */
    public Map<String,LoadBalancer> getLoadBalancerMap();
    
    
    /**
        Notification type for JMX Notification issued when AMX MBeans are loaded
        and ready for use.  
        @see #getAMXReady
     */
    public static final String  AMX_READY_NOTIFICATION_TYPE =
        AMX.NOTIFICATION_PREFIX + "DomainRoot" + ".AMXReady";
        
    /**
        Poll to see if AMX is ready for use. It is more efficient to instead listen
        for a Notification of type {@link #AMX_READY_NOTIFICATION_TYPE}.  That
        should be done  by first registering the listener, then checking
        just after registration in case the Notification was issued in the ensuing
        interval just before the listener became registered.
        
        @return true if AMX is ready for use, false otherwise.
        @see #AMX_READY_NOTIFICATION_TYPE
     */
    public boolean  getAMXReady();
      
    /**
        Wait (block) until AMX is ready for use. Upon return, AMX is ready for use.
     */
    public void  waitAMXReady();

    /**
       Contacts Update Center Server and get the updates status.
     */
    public UpdateStatus  getUpdateStatus();
    
    /**
      @since Glassfish V3
     */
    public String getDebugPort();
    
    /**
      @since Glassfish V3
     */
    public String getApplicationServerFullVersion();  
      
    
    
    /**
       @since Glassfish V3
     */
    public String getInstanceRoot();

    /**
       @return the directory for the domain
      @since Glassfish V3
     */
    public String getDomainDir();
    
    /**
      @return the configuration directory, typically 'config' subdirectory of {@link #getDomainDir}
      @since Glassfish V3
     */
    public String getConfigDir();

    /**
      @return the installation directory
      @since Glassfish V3
     */
    public String getInstallDir();
    
    public RealmsMgr getRealmsMgr();
    
    public RuntimeMgr getRuntimeMgr();
    
    /**
        Return the time the domain admin server has been running.
        uptime[0] contains the time in milliseconds.  uptime[1] contains a human-readable
        string describing the duration.
     */
    public Object[]     getUptimeMillis();
}














