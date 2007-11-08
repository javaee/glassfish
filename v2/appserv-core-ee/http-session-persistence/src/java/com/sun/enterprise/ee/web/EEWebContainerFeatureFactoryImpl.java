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

package com.sun.enterprise.ee.web;

import java.lang.reflect.Method;
import java.io.File;
import com.sun.enterprise.server.pluggable.WebContainerFeatureFactory;
import com.sun.enterprise.web.PEWebContainerFeatureFactoryImpl;
import com.sun.enterprise.admin.monitor.stats.WebModuleStats;
//import com.sun.enterprise.ee.web.stats.EEWebModuleStatsImpl;
import com.sun.enterprise.web.WebContainerAdminEventProcessor;
import com.sun.enterprise.web.EEWebContainerAdminEventProcessor;
import com.sun.enterprise.web.WebContainerStartStopOperation;
import com.sun.enterprise.web.ServerConfigLookup;
import com.sun.enterprise.web.SSOFactory;
//import com.sun.enterprise.ee.web.initialization.EESSOFactory;
import com.sun.enterprise.web.VirtualServer;
//import com.sun.enterprise.ee.web.sessmgmt.EEWebContainerStartStopOperation;
import com.sun.enterprise.web.HealthChecker;
//import com.sun.enterprise.ee.web.sessmgmt.EEHADBHealthChecker;
import com.sun.enterprise.web.ReplicationReceiver;
//import com.sun.enterprise.ee.web.sessmgmt.JxtaReplicationReceiver;
import com.sun.enterprise.web.EmbeddedWebContainer;
import com.sun.enterprise.web.WebContainer;
import com.sun.enterprise.config.serverbeans.AccessLog;
import com.sun.enterprise.util.SystemPropertyConstants;

/**
 * Implementation of WebContainerFeatureFactory which returns web container
 * feature implementations for EE.
 */
public class EEWebContainerFeatureFactoryImpl extends PEWebContainerFeatureFactoryImpl
        implements WebContainerFeatureFactory {

    private boolean isHADBInstalled() {
        ServerConfigLookup lookup = new ServerConfigLookup();
        return lookup.isHADBInstalled();
    }

    private final String EE_WEB_MODULE_STATS_IMPL
        = "com.sun.enterprise.ee.web.stats.EEWebModuleStatsImpl";

    public WebModuleStats getWebModuleStats() {
        if(!isHADBInstalled()) {
            return super.getWebModuleStats();
        }
        WebModuleStats stats = null;
        try {
            stats =
                (WebModuleStats) (Class.forName(EE_WEB_MODULE_STATS_IMPL)).newInstance();
        } catch (Exception ex) {
        }
        if(stats != null) {
            return stats;
        } else {
            return super.getWebModuleStats();
        }
        //return new EEWebModuleStatsImpl();
    }

    /*
    public WebContainerAdminEventProcessor getWebContainerAdminEventProcessor() {
        return new EEWebContainerAdminEventProcessor();
    }
     */   
    
    private final String EE_WEB_CONTAINER_START_STOP_OPERATION_IMPL
        = "com.sun.enterprise.ee.web.sessmgmt.EEWebContainerStartStopOperation";

    public WebContainerStartStopOperation getWebContainerStartStopOperation() {
        if(!isHADBInstalled()) {
            return super.getWebContainerStartStopOperation();
        }
        WebContainerStartStopOperation startStopOperation = null;
        try {
            startStopOperation =
                (WebContainerStartStopOperation) (Class.forName(EE_WEB_CONTAINER_START_STOP_OPERATION_IMPL)).newInstance();
        } catch (Exception ex) {
        }
        if(startStopOperation != null) {
            return startStopOperation;
        } else {
            return super.getWebContainerStartStopOperation();
        }
        //return new EEWebContainerStartStopOperation();
    }
 
    private final String EE_HEALTH_CHECKER_IMPL
        = "com.sun.enterprise.ee.web.sessmgmt.EEHADBHealthChecker";

    public HealthChecker getHADBHealthChecker(WebContainer webContainer) {
        if(!isHADBInstalled()) {
            return super.getHADBHealthChecker(webContainer);
        }
        HealthChecker healthChecker = null;
        Class healthCheckerClass = null;
        try {
            Class[] classParams = new Class[]{WebContainer.class};
            healthCheckerClass =
                Class.forName(EE_HEALTH_CHECKER_IMPL);
            Method myMethod
                = healthCheckerClass.getMethod("createInstance", classParams);
            Object[] params = new Object[]{(WebContainer)webContainer};
            healthChecker =
                (HealthChecker)myMethod.invoke(null, params);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if(healthChecker != null) {
            return healthChecker;
        } else {
            return super.getHADBHealthChecker(webContainer);
        }
        //return EEHADBHealthChecker.createInstance(webContainer);
    }
    
    private final String EE_REPLICATION_RECEIVER_IMPL 
        = "com.sun.enterprise.ee.web.sessmgmt.JxtaReplicationReceiver";    
    
    public ReplicationReceiver getReplicationReceiver(EmbeddedWebContainer embedded) {        
        ReplicationReceiver replicationReceiver = null;
        Class replicationReceiverClass = null;
        try {
            Class[] classParams = new Class[]{EmbeddedWebContainer.class};
            replicationReceiverClass = 
                Class.forName(EE_REPLICATION_RECEIVER_IMPL);
            Method myMethod 
                = replicationReceiverClass.getMethod("createInstance", classParams);
            Object[] params = new Object[]{(EmbeddedWebContainer)embedded};
            replicationReceiver =
                (ReplicationReceiver)myMethod.invoke(null, params);          
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if(replicationReceiver != null) {
            return replicationReceiver;
        } else {
            return super.getReplicationReceiver(embedded);
        }        
        //return JxtaReplicationReceiver.createInstance(embedded);
    }        
 
    private final String EE_SSO_FACTORY_IMPL
        = "com.sun.enterprise.ee.web.initialization.EESSOFactory";

    public SSOFactory getSSOFactory() {
        SSOFactory ssoFactory = null;
        try {
            ssoFactory =
                (SSOFactory) (Class.forName(EE_SSO_FACTORY_IMPL)).newInstance();
        } catch (Exception ex) {
        }
        if(ssoFactory != null) {
            return ssoFactory;
        } else {
            return super.getSSOFactory();
        }
        //return new EESSOFactory();
    }

    public VirtualServer getVirtualServer() {
        
        boolean useWebcore = false;        
        if (System.getProperty("com.sun.enterprise.web.useWebcore") != null){
            useWebcore = Boolean.parseBoolean(
                  System.getProperty("com.sun.enterprise.web.useWebcore"));
        }
            
        VirtualServer vs = null;
        if ( useWebcore ) {    
             /*
             * Ideally, this would be as simple as:
             *
             *     return new com.sun.enterprise.web.HttpServiceVirtualServer();
             * 
             * but importing this class would cause a circular dependency
             * between appserv-core-ee and appserv-pwc-linkage
             */

            try {
                vs = (VirtualServer) Class.forName("com.sun.enterprise.web.HttpServiceVirtualServer").newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            vs = new VirtualServer();
        }   

        return vs;

    }

    /**
     * Return the <code>SSLImplementation</code> used by the WebContainer.
     */
    public String getSSLImplementationName(){
        String sslImplName = null;

        //XXX temp, need to change after profile is done
        String dbDir = System.getProperty(
                SystemPropertyConstants.NSS_DB_PROPERTY);
        if ((new File(dbDir, "key3.db")).exists()) {
            sslImplName = "com.sun.enterprise.ee.security.NssImplementation";
        }
 
        return sslImplName;
    }

    /**
     * Gets the default access log file prefix.
     *
     * @return The default access log file prefix
     */
    public String getDefaultAccessLogPrefix() {
        return "_access_log";
    }

    /**
     * Gets the default access log file suffix.
     *
     * @return The default access log file suffix
     */
    public String getDefaultAccessLogSuffix() {
        return "";
    }

    /**
     * Gets the default datestamp pattern to be applied to access log files.
     *
     * @return The default datestamp pattern to be applied to access log files
     */
    public String getDefaultAccessLogDateStampPattern() {
        return AccessLog.getDefaultRotationSuffix();
    }

    /**
     * Returns true if the first access log file and all subsequently rotated
     * ones are supposed to be date-stamped, and false if datestamp is to be
     * added only starting with the first rotation.
     *
     * @return true if first access log file and all subsequently rotated
     * ones are supposed to be date-stamped, and false if datestamp is to be
     * added only starting with the first rotation. 
     */    
    public boolean getAddDateStampToFirstAccessLogFile() {
        return false;
    }

    /**
     * Gets the default rotation interval in minutes.
     *
     * @return The default rotation interval in minutes
     */
    public int getDefaultRotationIntervalInMinutes() {
        return Integer.parseInt(AccessLog.getDefaultRotationIntervalInMinutes());
    }

}
