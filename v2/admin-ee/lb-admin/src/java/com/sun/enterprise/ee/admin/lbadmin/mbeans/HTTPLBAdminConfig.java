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
package com.sun.enterprise.ee.admin.lbadmin.mbeans;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.config.serverbeans.ElementProperty;
	
import com.sun.enterprise.config.serverbeans.LbConfigs;
import com.sun.enterprise.config.serverbeans.LbConfig;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ClusterRef;
import com.sun.enterprise.config.serverbeans.ServerRef;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.HealthChecker;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.deployment.phasing.ApplicationReferenceHelper;
import com.sun.enterprise.ee.admin.lbadmin.writer.LbConfigPublisher;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.util.SystemPropertyConstants;

import com.sun.logging.ee.EELogDomains;
import java.util.logging.Logger;
import java.util.logging.Level; 
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Enumeration;

import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;

import com.sun.enterprise.ee.admin.lbadmin.writer.LbConfigWriter;

/**
 *  MBean representing configuration for the lb-config element.
 *
 * @author Satish Viswanatham
 */
public final class HTTPLBAdminConfig extends BaseConfigMBean 
                        implements HTTPLBAdminConfigMBean {

    public HTTPLBAdminConfig() {
    }     
    
    // --- BEGIN OF LBCONFIG MGR FUNCTIONS -----

    public String[] listLBConfigs(String target) throws MBeanException {

        _logger.log(Level.FINE, "[LBAdminMBean] listLBConfigs called" );

        LbConfigs lbConfigs = getLbConfigs();
        if (lbConfigs == null ) {
            _logger.log(Level.INFO, _sMgr.getString(
                    "http_lb_admin.NoLbConfigs"));
            return null;
        }

        String[] names = null;
        if (target == null) {
            LbConfig[] lbConfigArray = lbConfigs.getLbConfig();
            if (lbConfigArray.length == 0) {
                _logger.log(Level.INFO, _sMgr.getString(
                        "http_lb_admin.NoLbConfigs"));
                return null;
            }
            names = new String [lbConfigArray.length];
            for (int i=0; i < lbConfigArray.length; i++) {
                names[i] = lbConfigArray[i].getName();    
            }
        } else {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();

            try {
                // target is a cluster
                if (ClusterHelper.isACluster(ctx, target)) {
                    names = getLBConfigsForCluster(target);

                // target is a server
                } else if (ServerHelper.isAServer(ctx, target)) {
                    names = getLBConfigsForServer(target);

                } else {

                    LbConfig lbConfig = lbConfigs.getLbConfigByName(target);

                    // target is a lb config
                    if (lbConfig != null) {
                        String[] clusters = getClustersInLBConfig(target);
                        String[] servers = getServersInLBConfig(target);

                        // add the two array lists
                        List list = new ArrayList();
                        if (clusters != null) {
                            String cPrefix = _strMgr.getString("ClusterPrefix");
                            for (int i=0; i<clusters.length; i++) {
                                list.add(  cPrefix+ clusters[i]);
                            }
                        }
                        if (servers != null) {
                            String sPrefix = _strMgr.getString("ServerPrefix");
                            for (int i=0; i<servers.length; i++) {
                                list.add( sPrefix + servers[i]);
                            }
                        }

                        names = new String [list.size()];
                        names = (String[]) list.toArray(names);
                    }
                }
            } catch(ConfigException ce) {
                throw new MBeanException(ce);
            }
        }
        return names;
    }

    public String[] getLBConfigsForServer(String serverName) 
            throws MBeanException 
    {
        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();
            return LbConfigHelper.getLBsForStandAloneServer(ctx, serverName);
        } catch(ConfigException ce) {
            throw new MBeanException(ce);
        }
    }

    public String[] getLBConfigsForCluster(String clusterName)
            throws MBeanException 
    {
        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();
            return LbConfigHelper.getLBsForCluster(ctx, clusterName);
        } catch(ConfigException ce) {
            throw new MBeanException(ce);
        }
    }

    public String[] getServersInLBConfig(String configName)  
        throws MBeanException 
    {
        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();
            return LbConfigHelper.getServersInLB(ctx, configName);           
        } catch(ConfigException ce) {
            throw new MBeanException(ce);
        }
    }

    public String[] getClustersInLBConfig(String configName) 
            throws MBeanException 
    {
        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();
            return LbConfigHelper.getClustersInLB(ctx, configName);           
        } catch(ConfigException ce) {
            throw new MBeanException(ce);
        }
    }

    public ObjectName getHTTPLBConfigObjectName (String name) 
    {
        _logger.log(Level.FINE,
            "[LBAdminMBean] getHttpLbConfigObjectName called for configname " 
            + name);

        LbConfigs lbConfigs = getLbConfigs();

        if (lbConfigs == null ) {
            _logger.log(Level.FINE,_sMgr.getString(
                    "http_lb_admin.NoLbConfigs"));
            return null;
        }
        LbConfig lbConfig = lbConfigs.getLbConfigByName(name);
        if (lbConfig == null ) {
            _logger.log(Level.FINE,_sMgr.getString(
                    "http_lb_admin.NoLbConfigs"));
            return null;
        }
        // XXX get its objectName
        return null;
    }
 
    public void createLBConfig(String configName, String responseTimeout, 
            String httpsRouting, String reloadInterval, String monitor, 
            String routeCookie, String target, Properties props)
            throws MBeanException
    {
        LbConfigs lbConfigs = null;
        LbConfig newConfig = null;
        try {
            lbConfigs = getLbConfigs();

            newConfig = new LbConfig();
            if (configName == null) {

                if (target == null) {
                    target = 
                        SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;
                }

                // lb config name is derived by adding a suffix to the target
                configName = target + "-http-lb-config";
            }
            newConfig.setName(configName);
            newConfig.setResponseTimeoutInSeconds(
                new Integer(responseTimeout).toString());
            newConfig.setReloadPollIntervalInSeconds(
                new Integer(reloadInterval).toString());
            newConfig.setMonitoringEnabled(
                Boolean.valueOf(monitor).booleanValue());
            newConfig.setRouteCookieEnabled(
                Boolean.valueOf(routeCookie).booleanValue());
            newConfig.setHttpsRouting(
                Boolean.valueOf(httpsRouting).booleanValue());

            ElementProperty [] eProps = getElementProperty(props);
            if (eProps != null) {
                newConfig.setElementProperty(eProps);
            }

            // XXX do I need to use setConfig instead?
            lbConfigs.addLbConfig(newConfig);
       } catch(ConfigException ce) {
            throw new MBeanException(ce);
       }

        try {
            // creates a reference to the target
            if (target != null) {
                createLBRef(target, configName);
            }
            _logger.log(Level.INFO,_sMgr.getString(
                    "http_lb_admin.LbConfigCreated", configName));
        } catch (MBeanException me) {
            // remove lb config if created
            try {
                if ( newConfig != null) {  
                    lbConfigs.removeLbConfig(newConfig);
                }
            } catch (Throwable t) {
            }
            throw me;
        }
    }

    private ElementProperty[] getElementProperty(Properties props) {
        ElementProperty[] eprops = null;
        String name = null;
        if ((props == null) || (props.size() == 0)){
            return null;
        } else {
            int i =0;
            eprops = new ElementProperty [props.size()]; 
            for(Enumeration names = props.propertyNames(); 
                    names.hasMoreElements(); ){
                name = (String)names.nextElement();                        
                eprops[i] = new ElementProperty();
                eprops[i].setName(name);
                eprops[i].setValue(props.getProperty(name));
                i++;
            }
        }
        return eprops;
    }

    public void deleteLBConfig(String name) throws MBeanException 
    {
        LbConfigs lbConfigs = getLbConfigs();
        if (lbConfigs == null ) {
            String msg =  _strMgr.getString("NoLbConfigsElement");
            throw new MBeanException (new ConfigException(msg));
        }
        LbConfig lbConfig = lbConfigs.getLbConfigByName(name);
        if (lbConfig == null) {
            // Nothing to be deleted
            String msg =  _strMgr.getString("InvalidLbConfigName",name);
            throw new MBeanException (new ConfigException(msg));
        }
        if ( (lbConfig.getServerRef() == null || 
             lbConfig.getServerRef().length == 0 ) && 
                ( lbConfig.getClusterRef() == null || 
                    lbConfig.getClusterRef() .length == 0 ) ) {
            lbConfigs.removeLbConfig(lbConfig);
            _logger.log(Level.INFO,_sMgr.getString(
                    "http_lb_admin.LbConfigDeleted", name));
        } else {
            String msg =  _strMgr.getString("LbConfigNotEmpty", name);
            throw new MBeanException (new ConfigException(msg));
        }
    }

    public void disableServer(String target, String time) 
            throws MBeanException
    {          
        _logger.log(Level.FINE,"[LBAdminMBean] disableServer - Target "
                    + target + ", Timeout " + time);

        int timeout = Integer.parseInt(time);
        if (timeout < 0) {
            String msg = _strMgr.getString("InvalidNumber");
            throw new MBeanException (new ConfigException(msg));
        }

        ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                .getAdminConfigContext();

        try {
            // disables cluster if target is a cluster
            if (ClusterHelper.isACluster(ctx, target)) {
                disableCluster(target, timeout);
                _logger.log(Level.INFO,_sMgr.getString(
                    "http_lb_admin.ClusterDisabled", target));

            } else { // target is a server

                boolean foundTarget = false;
                LbConfig[] lbConfigs = getLbConfigs().getLbConfig();
                for (int i =0; i < lbConfigs.length; i ++ ) {
                    ServerRef  sRef = lbConfigs[i].getServerRefByRef(target);
                    if (sRef == null) {
                        _logger.log(Level.FINEST," server " + target +
                            " does not exist in " + lbConfigs[i]);
                    } else {
                        foundTarget = true;
                        boolean enabled = sRef.isLbEnabled();
                        int curTout = Integer.parseInt(
                            sRef.getDisableTimeoutInMinutes());
                        if ((enabled == false) && (curTout == timeout)) {
                            String msg = _strMgr.getString("ServerDisabled",
                                sRef.getRef());
                            throw new MBeanException(new ConfigException(msg));
                        }
                        sRef.setLbEnabled(false);
                        sRef.setDisableTimeoutInMinutes(
                            new Integer(timeout).toString());
                        _logger.log(Level.INFO,_sMgr.getString(
                        "http_lb_admin.ServerDisabled", target));
                    }
                }
                // did not find server target
                if (!foundTarget) {
                    ServerRef sRef = getServerRefFromCluster(ctx, target);
                    if (sRef == null) {
                        _logger.log(Level.FINEST," server " + target +
                        " does not exist in any cluster in the domain");
                        String msg = _strMgr.getString("ServerNotDefined", 
                                            target);
                        throw new MBeanException (new RuntimeException(msg));
                    } else {
                        int curTout = Integer.parseInt(
                            sRef.getDisableTimeoutInMinutes());
                        boolean enabled = sRef.isLbEnabled();
                        if ((enabled == false) && (curTout == timeout)) {
                            String msg = _strMgr.getString("ServerDisabled",
                                sRef.getRef());
                            throw new MBeanException(new ConfigException(msg));
                        }
                        sRef.setLbEnabled(false);
                        sRef.setDisableTimeoutInMinutes(
                            new Integer(timeout).toString());
                        _logger.log(Level.INFO,_sMgr.getString(
                            "http_lb_admin.ServerDisabled", target));
                    }

                }
            }
        } catch(ConfigException ce) {
            throw new MBeanException(ce);
        }
    }
    
    public void enableServer(String target) throws MBeanException 
    {          
        _logger.log(Level.FINE,"[LBAdminMBean] enableServer called for target "
                    + target);

        ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                .getAdminConfigContext();

        try {
            // target is a cluster
            if (ClusterHelper.isACluster(ctx, target)) {
                enableCluster(target);
                _logger.log(Level.INFO,_sMgr.getString(
                        "http_lb_admin.ClusterEnabled", target));

            } else { // target is a server

                boolean foundTarget = false;
                LbConfig[] lbConfigs = getLbConfigs().getLbConfig();
                for (int i =0; i < lbConfigs.length; i ++ ) {
                    ServerRef  sRef = lbConfigs[i].getServerRefByRef(target);
                    if (sRef == null) {
                        _logger.log(Level.FINEST," server " + target +
                            " does not exist in " + lbConfigs[i]);
                    } else {
                        boolean enabled = sRef.isLbEnabled();
                        if (enabled == true) {
                            String msg = _strMgr.getString("ServerEnabled",
                                sRef.getRef());
                            throw new MBeanException(new ConfigException(msg));
                        }
                        sRef.setLbEnabled(true);
                        foundTarget = true;
                        _logger.log(Level.INFO,_sMgr.getString(
                            "http_lb_admin.ServerEnabled", target));
                    }
                }

                // did not find server target
                if (!foundTarget) {
                    ServerRef sRef = getServerRefFromCluster(ctx, target);
                    if (sRef == null) {
                        _logger.log(Level.FINEST," server " + target +
                        " does not exist in any cluster in the domain");
                        String msg = _strMgr.getString("ServerNotDefined", 
                                            target);
                        throw new MBeanException (new RuntimeException(msg));
                    } else {
                        boolean enabled = sRef.isLbEnabled();
                        if (enabled == true) {
                            String msg = _strMgr.getString("ServerEnabled",
                                sRef.getRef());
                            throw new MBeanException(new ConfigException(msg));
                        }
                        sRef.setLbEnabled(true);
                        _logger.log(Level.INFO,_sMgr.getString(
                            "http_lb_admin.ServerEnabled", target));
                    }
                }
            }
        } catch(ConfigException ce) {
            throw new MBeanException(ce);
        }
    }

    private ServerRef getServerRefFromCluster(ConfigContext ctx,String target) 
                throws MBeanException
    {
        // check if this server is part of cluster, then
        // turn on lb-enable flag in the cluster.

        Cluster c= null;
        try {
            c = ClusterHelper.getClusterForInstance(ctx, target);  
        } catch(ConfigException ce){
        }
                    
        if (c== null) {
            String msg = _strMgr.getString("ServerNotDefined", target);
            throw new MBeanException (new RuntimeException(msg));
        } else {
            return c.getServerRefByRef(target);
        }
    }

    private void disableCluster(String clusterName, int timeout) 
                throws MBeanException
    {          
        if ( timeout < 0 ) {
            String msg = _strMgr.getString("InvalidNumber");
            throw new MBeanException ( new ConfigException(msg));
        }
        Cluster c = null;
        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();
            c = ClusterHelper.getClusterByName(ctx, clusterName);
        } catch(ConfigException ce) {
            throw new MBeanException(ce);
        }
        if ( c == null ) {
            String msg =_strMgr.getString("ClusterNotDefined", clusterName);
            throw new MBeanException( new ConfigException(msg));
        }
        ServerRef[] sRefs = c.getServerRef();
        for (int i=0; i < sRefs.length; i++) {
            sRefs[i].setLbEnabled(false);
            sRefs[i].setDisableTimeoutInMinutes(
                    new Integer(timeout).toString());
        }
    }

    private void enableCluster(String clusterName) throws MBeanException
    {          
        Cluster c = null;
        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();
            c= ClusterHelper.getClusterByName(ctx, clusterName);
        } catch(ConfigException ce) {
            throw new MBeanException(ce);
        }
        if ( c == null ) {
            String msg = _strMgr.getString("ClusterNotDefined", clusterName);
            throw new MBeanException( new ConfigException(msg));
        }
        ServerRef[] sRefs = c.getServerRef();
        for (int i=0; i < sRefs.length; i++) {
            sRefs[i].setLbEnabled(true);
        }
    }
    
    private void disableApplicationForServer(String appName,String serverName,
            int timeout) throws MBeanException
    {          
        if ( timeout < 0 ) {
            String msg = _strMgr.getString("InvalidNumber");
            throw new MBeanException ( new ConfigException(msg));
        }

        Server s = null;
        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();
            s = ServerHelper.getServerByName(ctx, serverName);
        } catch(ConfigException ce) {
            throw new MBeanException(ce);
        }
        if (s == null ) {
            String msg = _strMgr.getString("ServerNotDefined", serverName);
            throw new MBeanException ( new ConfigException(msg));
        }
        ApplicationRef appRef = s.getApplicationRefByRef(appName);
        if (appRef == null ) {
            String msg = _strMgr.getString("AppRefNotDefined", appName, 
                            serverName);
            throw new MBeanException ( new ConfigException(msg));
        }
        int curTout = Integer.parseInt(appRef.getDisableTimeoutInMinutes());
        boolean enabled = appRef.isLbEnabled();
        if ((enabled == false) && (curTout == timeout)) {
            String msg = _strMgr.getString("AppDisabledOnServer", 
                appRef.getRef(), serverName);
            throw new MBeanException(new ConfigException(msg));
        }
        appRef.setLbEnabled(false);
        appRef.setDisableTimeoutInMinutes(new Integer(timeout).toString());
        _logger.log(Level.INFO,_sMgr.getString(
                "http_lb_admin.ApplicationDisabled", appName,serverName));

    }
    
    private void enableApplicationForServer(String appName,String serverName) 
            throws MBeanException
    {          
        Server s = null;
        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();
            s = ServerHelper.getServerByName(ctx, serverName);
        } catch(ConfigException ce) {
            throw new MBeanException(ce);
        }
        if (s == null ) {
            String msg = _strMgr.getString("ServerNotDefined", serverName);
            throw new MBeanException ( new ConfigException(msg));
        }
        ApplicationRef appRef = s.getApplicationRefByRef(appName);
        if (appRef == null ) {
            String msg = _strMgr.getString("AppRefNotDefined", appName);
            throw new MBeanException ( new ConfigException(msg));
        }
        boolean enabled = appRef.isLbEnabled();
        if (enabled == true) {
            String msg = _strMgr.getString("AppEnabledOnServer", 
                appRef.getRef(), serverName);
            throw new MBeanException(new ConfigException(msg));
        }
        appRef.setLbEnabled(true);
        _logger.log(Level.INFO,_sMgr.getString(
                        "http_lb_admin.ApplicationEnabled",appName,serverName));
   }
    
    public void disableApplication(String target, String timeout,
            String appName) throws MBeanException
    {
        _logger.log(Level.FINE,
            "[LBAdminMBean] disableApplication called - Target " 
            + target + ", App Name " + appName + ", Timeout " 
            + timeout); 

        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                .getAdminConfigContext();

            int iTimeout = Integer.parseInt(timeout);

            // target is a cluster 
            if (ClusterHelper.isACluster(ctx, target)) {
                disableApplicationForCluster(appName, target, iTimeout);

            // target is a server
            } else if (ServerHelper.isAServer(ctx, target)) {
                disableApplicationForServer(appName,target, iTimeout);

            // unknown target 
            } else {
                String msg = _strMgr.getString("InvalidTarget", target);
                throw new MBeanException (new RuntimeException(msg));
            }
        } catch (ConfigException ce) {
            throw new MBeanException (ce);
        }
    }

    private void disableApplicationForCluster(String appName,String clusterName,
            int timeout) throws MBeanException
    {          
        if ( timeout < 0 ) {
            String msg = _strMgr.getString("InvalidNumber");
            throw new MBeanException ( new ConfigException(msg));
        }

        Cluster c = null;
        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();
            c = ClusterHelper.getClusterByName(ctx, clusterName);
        } catch (ConfigException ce) {
            throw new MBeanException(ce);
        }
        if (c == null ) {
            String msg = _strMgr.getString("ClusterNotDefined", clusterName);
            throw new MBeanException ( new ConfigException(msg));
        }
        ApplicationRef appRef = c.getApplicationRefByRef(appName);
        if (appRef == null ) {
            String msg = _strMgr.getString("AppRefNotDefined", 
                                            appName, clusterName);
            throw new MBeanException (new ConfigException(msg));
        }
        int curTout = Integer.parseInt(appRef.getDisableTimeoutInMinutes());
        boolean enabled = appRef.isLbEnabled();
        if ((enabled == false) && (curTout == timeout)) {
            String msg = _strMgr.getString("AppDisabledOnCluster", 
                appRef.getRef(), clusterName);
            throw new MBeanException(new ConfigException(msg));
        }
        appRef.setLbEnabled(false);
        appRef.setDisableTimeoutInMinutes(new Integer(timeout).toString());
        _logger.log(Level.INFO,_sMgr.getString(
                    "http_lb_admin.ApplicationDisabled", appName,clusterName));

    }

    public void enableApplication(String target, String appName)
            throws MBeanException
    {
        _logger.log(Level.FINE,
            "[LBAdminMBean] enableApplication called - Target " 
            + target + ", App Name " + appName);

        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                .getAdminConfigContext();

            // target is a cluster 
            if (ClusterHelper.isACluster(ctx, target)) {
                enableApplicationForCluster(appName, target);

            // target is a server
            } else if (ServerHelper.isAServer(ctx, target)) {
                enableApplicationForServer(appName, target); 

            // unknown target 
            } else {
                String msg = _strMgr.getString("InvalidTarget", target);
                throw new MBeanException (new RuntimeException(msg));
            }
        } catch (ConfigException ce) {
            throw new MBeanException (ce);
        }
    }

    private void enableApplicationForCluster(String appName,String clusterName)
            throws MBeanException
    {          
        _logger.log(Level.FINE,
            "[LBAdminMBean] disableApplication called for target " 
            + clusterName + " app name is " + appName );

        Cluster c = null;
        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();
            c = ClusterHelper.getClusterByName(ctx, clusterName);
        } catch (ConfigException ce) {
            throw new MBeanException(ce);
        }
        if (c == null ) {
            String msg = _strMgr.getString("ClusterNotDefined", clusterName);
            throw new MBeanException ( new ConfigException(msg));
        }
        ApplicationRef appRef = c.getApplicationRefByRef(appName);
        if (appRef == null ) {
            String msg = 
                _strMgr.getString("AppRefNotDefined", appName, clusterName);
            throw new MBeanException ( new ConfigException(msg));
        }
        boolean enabled = appRef.isLbEnabled();
        if (enabled == true) {
            String msg = _strMgr.getString("AppEnabledOnCluster", 
                appRef.getRef(), clusterName);
            throw new MBeanException(new ConfigException(msg));
        }
        appRef.setLbEnabled(true);
        _logger.log(Level.INFO,_sMgr.getString(
                    "http_lb_admin.ApplicationEnabled", appName,clusterName));
    }

   public void createHealthChecker(String url, String interval,
            String timeout, String lbConfigName, String target) 
            throws MBeanException
    {
        if (lbConfigName != null) {
            LbConfig lbConfig = getLbConfigs().getLbConfigByName(lbConfigName);
            createHealthCheckerInternal(url,interval,timeout,lbConfig, 
            lbConfigName ,target, false);
        } else {
            LbConfig[] lbConfigs = getLbConfigs().getLbConfig();   
            if (lbConfigs == null) {
                String msg = _strMgr.getString("NoLbConfigsElement");
                throw new MBeanException ( new ConfigException(msg));
            }

            LbConfig[] match = null;
            try {
                match = matchLbConfigToTarget(lbConfigs, target);
            } catch (ConfigException ce) {
                String msg = _strMgr.getString("UnassociatedTarget", target);
                throw new MBeanException(ce, msg);
            }

            if ( (match == null) || (match.length == 0) ) {
                String msg = _strMgr.getString("UnassociatedTarget", target);
                throw new MBeanException(new ConfigException(msg));
            }

            for (int lbIdx =0; lbIdx < match.length; lbIdx++) {
                createHealthCheckerInternal(url,interval,timeout,
                    match[lbIdx], lbConfigName, target, false);
            }
        }
    }

    /**
     * Returns an array of LbConfigs that has a reference to the target
     * server or cluster. If there are no references found for the 
     * target or the arguments are null, this method returns null.
     *
     * @param  lbConfigs  array of existing LbConfigs in the system
     * @param  target     name of server or cluster 
     *
     * @return array of LbConfigs that has a ref to the target server
     * @throws ConfigException  if an error with configuration parsing
     */
    private LbConfig[] matchLbConfigToTarget(LbConfig[] lbConfigs, 
            String target) throws ConfigException
    {
        List list = null;

        // bad target
        if (target == null) {
            String msg = _strMgr.getString("NullTarget");
            throw new ConfigException(msg);
        }

        // admin config context 
        ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                .getAdminConfigContext();

        // system has lb configs defined
        if (lbConfigs != null) {
            list = new ArrayList();

            for (int i=0; i<lbConfigs.length; i++) {

                // target is a cluster
                if (ClusterHelper.isACluster(ctx, target)) {
                    ClusterRef  cRef = lbConfigs[i].getClusterRefByRef(target);

                    // this lb config has a reference to the target cluster
                    if (cRef != null) {
                        list.add(lbConfigs[i]);
                    }

                // target is a server
                } else if (ServerHelper.isAServer(ctx, target)) {
                    ServerRef sRef = lbConfigs[i].getServerRefByRef(target);

                    // this lb config has a reference to the target server
                    if (sRef != null) {
                        list.add(lbConfigs[i]);
                    }
                }
            }
        }

        // converts the list to an array
        LbConfig[] lbcArray = null;
        if (list != null) {
            lbcArray = new LbConfig[list.size()];
            lbcArray = (LbConfig[]) list.toArray(lbcArray);
        } 

        return lbcArray;
    }

    /**
     * This is to create a health checker to a cluster configuration. By 
     * default the healh checker will be configured.  This applies only 
     * to our native load balancer.
     *
     * @param   url   the URL to ping so as to determine the health state
     *   of a listener.
     *
     * @param   interval   specifies the interval in seconds at which health 
     *   checks of unhealthy instances carried out to check if the instances
     *   has turned healthy. Default value is 30 seconds. A value of 0 would
     *   imply that health check is disabled.
     *
     * @param   timeout    timeout interval in seconds within which response 
     *   should be obtained for a health check request; else the instance would
     *   be considered unhealthy.Default value is 10 seconds.
     *
     * @param   lbConfig    the load balancer configuration bean
     * @param   lbConfigName    the load balancer configuration's name
     *
     * @param   target      name of the target - cluster or stand alone 
     *  server instance
     *
     * @param   ignoreError if ignoreError is true, exceptions are not thrown in
     *                      the following cases 
     *                      1). The specified server instance or cluster 
     *                      does not exist in the LB config.
     *                      2). The target  already contains the health checker.
     *
     * @throws MBeanException   If the operation is failed
     */
    private void createHealthCheckerInternal(String url, String interval,
            String timeout,LbConfig lbConfig, String lbConfigName, String target, 
            boolean ignoreError) throws MBeanException
    {
        // invalid lb config name
        if (lbConfig == null) {
            String msg = _strMgr.getString("InvalidLbConfigName", lbConfigName);
            throw new MBeanException ( new ConfigException(msg));
        }

        lbConfigName = lbConfig.getName();
        // print diagnostics msg
        _logger.log(Level.FINE,
            "[LBAdminMBean] createHealthChecker called - URL " 
            + url + ", Interval " + interval + ", Time out " 
            + timeout + ", LB Config  " + lbConfigName 
            + ", Target " + target);

        // null target 
        if (target == null) {
            String msg = _strMgr.getString("NullTarget");
            throw new MBeanException ( new RuntimeException(msg));
        }

        // admin config context
        ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                .getAdminConfigContext();
        try { 
            HealthChecker hc = new HealthChecker();
            if (url != null)       { hc.setUrl(url);                    }
            if (interval != null)  { hc.setIntervalInSeconds(interval); }
            if (timeout != null)   { hc.setTimeoutInSeconds(timeout);   }

            // target is a cluster
            if (ClusterHelper.isACluster(ctx, target)) {
                ClusterRef  cRef = lbConfig.getClusterRefByRef(target);

                // cluster is not associated to this lb config
                if ((cRef == null) && (ignoreError == false)){
                    String msg = _strMgr.getString("UnassociatedCluster", 
                                                    lbConfigName, target);
                    throw new MBeanException (new ConfigException(msg));
                }

                if ( (cRef != null) && (cRef.getHealthChecker() == null) ){
                    cRef.setHealthChecker(hc);
                        _logger.log(Level.INFO,_sMgr.getString(
                            "http_lb_admin.HealthCheckerCreated", target));
                } else {
                    if (ignoreError == false) {
                        String msg = _strMgr.getString("HealthCheckerExists", 
                                             cRef.getRef());
                        throw new MBeanException (new ConfigException(msg));
                    }
                }

            // target is a server
            } else if (ServerHelper.isAServer(ctx, target)) {
                ServerRef sRef = lbConfig.getServerRefByRef(target);

                // server is not associated to this lb config
                if ((sRef == null) && (ignoreError == false)){
                    String msg = _strMgr.getString("UnassociatedServer", 
                                                    lbConfigName, target);
                    throw new MBeanException (new ConfigException(msg));
                }

                if ((sRef != null) && (sRef.getHealthChecker() == null) ){
                    sRef.setHealthChecker(hc);
                    _logger.log(Level.INFO,_sMgr.getString(
                            "http_lb_admin.HealthCheckerCreated", target));
                } else {
                    if (ignoreError == false) {
                        String msg = _strMgr.getString("HealthCheckerExists", 
                                             sRef.getRef());
                        throw new MBeanException (new ConfigException(msg));
                    }
                }


            // unknown target 
            } else {
                String msg = _strMgr.getString("InvalidTarget", target);
                throw new MBeanException ( new RuntimeException(msg));
            }
        } catch (ConfigException ce) {
            String msg = _strMgr.getString("ConfigErrorInHealthChecker", 
                            lbConfigName, target);
            throw new MBeanException (ce, msg);
        }
    }
    public void deleteHealthChecker(String lbConfigName, String target) 
                throws MBeanException
    {
        if (lbConfigName != null) {
            LbConfig lbConfig = getLbConfigs().getLbConfigByName(lbConfigName);
            deleteHealthCheckerInternal(lbConfig, target, false);
        } else {
            LbConfig[] lbConfigs = getLbConfigs().getLbConfig();   
            if (lbConfigs == null) {
                String msg = _strMgr.getString("NoLbConfigsElement");
                throw new MBeanException ( new ConfigException(msg));
            }
            for (int lbIdx =0; lbIdx < lbConfigs.length; lbIdx++) {
                deleteHealthCheckerInternal(lbConfigs[lbIdx], target, true);
            }
        }
    }

    /**
     * Deletes a health checker from a load balancer configuration.  
     *
     * @param   lbConfig        Http load balancer configuration bean
     * @param   target          Name of a cluster or stand alone server instance
     * @param   ignoreFailure   if ignoreError is true, exceptions are not 
     *                          thrown in the following cases 
     *                          1). The specified server instance or cluster 
     *                          does not exist in the LB config.
     *                          2).The target already contains the health checker
     *
     * @throws MBeanException   If the operation is failed
     */
    private void deleteHealthCheckerInternal(LbConfig lbConfig, String target, 
        boolean ignoreFailure) 
                throws MBeanException
    {          

        // invalid lb config name
        if (lbConfig == null) {
            String msg = _strMgr.getString("InvalidLbConfigName", target);
            throw new MBeanException ( new ConfigException(msg));
        }

        String lbConfigName = lbConfig.getName();

        _logger.log(Level.FINE,
            "[LBAdminMBean] deleteHealthChecker called - LB Config Name: " 
            + lbConfigName + ", Target: " + target);


        // null target 
        if (target == null) {
            String msg = _strMgr.getString("NullTarget");
            throw new MBeanException ( new RuntimeException(msg));
        }

        // admin config context
        ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                .getAdminConfigContext();

        try { 
            // target is a cluster
            if (ClusterHelper.isACluster(ctx, target)) {
                ClusterRef  cRef = lbConfig.getClusterRefByRef(target);

                // cluster is not associated to this lb config
                if ((cRef == null) && (ignoreFailure == false)){
                    String msg = _strMgr.getString("UnassociatedCluster", 
                                                    lbConfigName, target);
                    throw new MBeanException (new ConfigException(msg));
                }

                HealthChecker hc = cRef.getHealthChecker();
                if (hc != null) {
                    //cRef.removeHealthCheker(hc);
                    cRef.removeChild(hc, true);
                    _logger.log(Level.INFO,_sMgr.getString(
                            "http_lb_admin.HealthCheckerDeleted", target));
                } else {
                   if (ignoreFailure == false) {
                       String msg = _strMgr.getString("HealthCheckerDoesNotExist", 
                                                   target,lbConfigName);
                        throw new MBeanException (new ConfigException(msg));
                    }
                }

            // target is a server
            } else if (ServerHelper.isAServer(ctx, target)) {
                ServerRef  sRef   = lbConfig.getServerRefByRef(target);

                // server is not associated to this lb config
                if ((sRef == null) && (ignoreFailure == false)){
                    String msg = _strMgr.getString("UnassociatedServer", 
                                                    lbConfigName, target);
                    throw new MBeanException (new ConfigException(msg));
                }

                HealthChecker hc  = sRef.getHealthChecker();
                if (hc != null) {
                    //sRef.removeHealthCheker(hc);
                    sRef.removeChild(hc, true);
                    _logger.log(Level.INFO,_sMgr.getString(
                            "http_lb_admin.HealthCheckerDeleted", target));
                } else {
                    if (ignoreFailure == false) {
                       String msg = _strMgr.getString("HealthCheckerDoesNotExist", 
                                                    target,lbConfigName);
                        throw new MBeanException (new ConfigException(msg));
                    }
                }

            } else {
                String msg = _strMgr.getString("InvalidTarget", target);
                throw new MBeanException ( new RuntimeException(msg));
            }
        } catch (ConfigException ce) {
            String msg = _strMgr.getString("ConfigErrorInHealthChecker", 
                            lbConfigName, target);
            throw new MBeanException (ce, msg);
        }
    }
    
    protected LbConfigs getLbConfigs() {
        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();
            Domain domain = (Domain)ctx.getRootConfigBean();
            LbConfigs cfgs = domain.getLbConfigs();
            if (cfgs == null) {
                cfgs = new LbConfigs();
                domain.setLbConfigs(cfgs);
            }
            return cfgs;
        } catch( ConfigException ce) {
            return null;
        }
    }

    protected LbConfig getLbConfig(String configName) throws MBeanException {
        LbConfigs lbConfigs = getLbConfigs();

        if (lbConfigs == null ) {
            _logger.log(Level.FINE,_sMgr.getString(
                    "http_lb_admin.NoLbConfigs"));
            String msg = _strMgr.getString("NoLbConfigsElement");
            throw new MBeanException ( new ConfigException(msg));
        }
        LbConfig lbConfig = lbConfigs.getLbConfigByName(configName);
        if (lbConfig == null ) {
            _logger.log(Level.FINE,_sMgr.getString(
                    "http_lb_admin.NoLbConfigs"));
            String msg = _strMgr.getString("InvalidLbConfigName", configName);
            throw new MBeanException (new ConfigException(msg));
        }
        return lbConfig;
    }

    /// ----- END OF LB CONFIG MGR FUNCTIONS ----- 

    /**
     * This is used to add an existing server to an existing load balancer 
     * configuration. In addition, this method creates health checker, set the
     * lb policy,policy module , and enables all instances and applications
     *
     * @param   target      Name of the server or cluster
     * @param   config      Name of the config
     * @param   lbPolicy    load balancer policy
     * @param   lbPolicyModule path to the shared library implementing the user-defined
     *          load balancing policy
     * @param   hcURL       health checker url
     * @param   hcInterval  interval in seconds for the health checker
     * @param   hcTimeout   timeout in seconds for the health checker
     * @param   enableInstances enable all instances in the target
     * @param   enableApps  enable all user applications in the target
     *
     * @throws MBeanException   If the operation is failed
     */
    public void createLBRef(String target, String configName, String lbPolicy,
            String lbPolicyModule, String hcURL, String  hcInterval, String hcTimeout,
            boolean enableInstances, boolean enableApps)
        throws MBeanException
    {
        ConfigContext ctx =AdminService.getAdminService().getAdminContext()
        .getAdminConfigContext();
        try{
            if((lbPolicy != null) || (lbPolicyModule != null)){
                if (!ClusterHelper.isACluster(ctx, target)) {
                    String msg = _strMgr.getString("NotCluster", target);
                    throw new MBeanException( new ConfigException(msg));
                }
            }
            Cluster c = null;
            Server s = null;
            if (ClusterHelper.isACluster(ctx, target)) {
                c = ClusterHelper.getClusterByName(ctx, target);
                if (c == null ) {
                    String msg = _strMgr.getString("ClusterNotDefined", target);
                    throw new MBeanException( new ConfigException(msg));
                }
            }else{
                s = ServerHelper.getServerByName(ctx, target);
                if (s == null ) {
                    String msg = _strMgr.getString("ServerNotDefined", target);
                    throw new MBeanException( new ConfigException(msg));
                }
            }
            createLBRef(target,configName);
            if(hcURL != null ){
                try{
                    createHealthChecker(hcURL, hcInterval, hcTimeout, configName, target);
                }catch(MBeanException e){
                    String msg = _strMgr.getString("HealthCheckerExists",target);
                    _logger.log(Level.WARNING, msg);
                }
            }
            if(enableInstances) {
                enableServer(target);
            }
            if(enableApps || lbPolicy != null || lbPolicyModule != null ) {
                ApplicationRef[] appRefs = null;
                LbConfig lbConfig = getLbConfig(configName);
                if (ClusterHelper.isACluster(ctx, target)) {
                    appRefs = c.getApplicationRef();
                    
                }else{
                    appRefs = s.getApplicationRef();
                }
                if (appRefs != null && enableApps ) {
                    for(ApplicationRef ref:appRefs) {
                        //enable only user applications
                        if(!ApplicationHelper.isSystemApp(ctx, ref.getRef()))
                            ref.setLbEnabled(enableApps);
                    }
                }
                if(lbPolicy != null) {
                    ClusterRef cRef = lbConfig.getClusterRefByRef(target);
                    cRef.setLbPolicy(lbPolicy);
                }
                if(lbPolicyModule != null){
                    ClusterRef cRef = lbConfig.getClusterRefByRef(target);
                    cRef.setLbPolicyModule(lbPolicyModule);
                }
                
            }
        } catch(ConfigException ce) {
            throw new MBeanException(ce);
        }

            
    }
    public void createLBRef(String target, String configName)
        throws MBeanException
    {
        _logger.log(Level.FINE,
            "[LBAdminMbean] createLBRef called for target " + target);

        ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                .getAdminConfigContext();

        try { 
            // target is a cluster
            if (ClusterHelper.isACluster(ctx, target)) {
                addClusterToLbConfig(configName, target);
                _logger.log(Level.INFO, _sMgr.getString(
                    "http_lb_admin.AddClusterToConfig", target, configName));

            // target is a server
            } else if (ServerHelper.isAServer(ctx, target)) {
                addServerToLBConfig(configName, target);  
                _logger.log(Level.INFO, _sMgr.getString(
                    "http_lb_admin.AddServerToConfig", target, configName));

            } else {
                String msg = _strMgr.getString("InvalidTarget", target);
                throw new MBeanException ( new RuntimeException(msg));
            }
        } catch (ConfigException ce) {
            throw new MBeanException (ce);
        }
    }

    private void addServerToLBConfig(String configName,String serverName)  
            throws MBeanException
    {          
        LbConfig lbConfig = getLbConfig(configName);

        ServerRef sRef = lbConfig.getServerRefByRef(serverName);
        if (sRef != null) {
            // already exists
            return;
        }
        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();

            if (!ServerHelper.isServerStandAlone(ctx, serverName)) {
                String msg = _strMgr.getString("NotStandAloneInstance", 
                                                serverName);
                throw new MBeanException (new ConfigException(msg));
            }
            sRef = new ServerRef();
            sRef.setRef(serverName);
            lbConfig.addServerRef(sRef);
        } catch( ConfigException ce) {
            throw new MBeanException(ce);
        }
    }
    
    private void addClusterToLbConfig(String configName, String clusterName)  
            throws MBeanException
    {          
        LbConfig lbConfig = getLbConfig(configName);

        ClusterRef cRef = lbConfig.getClusterRefByRef(clusterName);
        if (cRef != null) {
            // already exists
            return;
        }
        cRef = new ClusterRef();
        cRef.setRef(clusterName);
        try {
            lbConfig.addClusterRef(cRef);
        } catch (ConfigException ce) {
            throw new MBeanException (ce);
        }
    }

    public void deleteLBRef(String target, String configName) 
            throws MBeanException
    {
        _logger.log(Level.FINE,
            "[LBAdminMBean] deleteLBRef called for target " + target);

        ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                .getAdminConfigContext();

        try { 
            // target is a cluster
            if (ClusterHelper.isACluster(ctx, target)) {
                deleteClusterFromLBConfig(configName, target); 
                _logger.log(Level.INFO, _sMgr.getString(
                    "http_lb_admin.DeleteClusterFromConfig",target,configName));

            // target is a server
            } else if (ServerHelper.isAServer(ctx, target)) {
                deleteServerFromLBConfig(configName, target);
                _logger.log(Level.INFO, _sMgr.getString(
                    "http_lb_admin.DeleteServerFromConfig",target,configName));

            } else {
                String msg = _strMgr.getString("InvalidTarget", target);
                throw new MBeanException ( new RuntimeException(msg));
            }
        } catch (ConfigException ce) {
            throw new MBeanException (ce);
        }
    }

    private void deleteServerFromLBConfig(String configName,String serverName) 
            throws MBeanException
    {          
        LbConfig lbConfig = getLbConfig(configName);

        ServerRef  sRef = lbConfig.getServerRefByRef(serverName);
        if (sRef == null) {
            // does not exist, just return from here
            _logger.log(Level.FINEST," server " + serverName +
                    " does not exist in any cluster in the domain");
            String msg = _strMgr.getString("ServerNotDefined",
                    serverName);
            throw new MBeanException(new RuntimeException(msg));
        }
        if (sRef.isLbEnabled()) {
            String msg = _strMgr.getString("ServerNeedsToBeDisabled", serverName);
            throw new MBeanException ( new ConfigException(msg));
        }
        // check if its applications are LB disabled.
        Server s = null;
        try {
            ConfigContext ctx =AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();
            s = ServerHelper.getServerByName(ctx, sRef.getRef());
        } catch(ConfigException ce) {
            throw new MBeanException(ce);
        }
        if (s == null ) {
            String msg = _strMgr.getString("ServerNotDefined", serverName);
            throw new MBeanException ( new ConfigException(msg));
        }
        ApplicationRef[] appRef = s.getApplicationRef();
        if (appRef == null ) {
            String msg = _strMgr.getString("AppRefsNotDefined", serverName);
            throw new MBeanException ( new ConfigException(msg));
        }
        int i =0;
        for(i=0; i < appRef.length; i++) {
            if (appRef[i].isLbEnabled()) {
                break;
            }
        }
        if ( i < appRef.length) {
            String msg = _strMgr.getString("AppsNotDisabled");
            throw new MBeanException ( new ConfigException(msg));
        }

        lbConfig.removeServerRef(sRef);
    }
    
    private void deleteClusterFromLBConfig(String configName, 
            String clusterName) throws MBeanException
    {          
        LbConfig lbConfig = getLbConfig(configName);

        ClusterRef cRef = lbConfig.getClusterRefByRef(clusterName);
        if (cRef == null) {
            // does not exist, just return from here
            return;
        }
        Cluster c = null;
        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();
            c = ClusterHelper.getClusterByName(ctx, clusterName);
        } catch (ConfigException ce) {
            throw new MBeanException(ce);
        }
        if ( c == null ) {
            String msg = _strMgr.getString("ClusterNotDefined", clusterName);
            throw new MBeanException (new ConfigException(msg));
        }
        ServerRef[] sRefs = c.getServerRef();
        for (int i=0; i < sRefs.length; i++) {
            if (sRefs[i].isLbEnabled()) {
                String msg = _strMgr.getString("ServerNeedsToBeDisabled", clusterName);
                throw new MBeanException (new ConfigException(msg));
            }
        }
        lbConfig.removeClusterRef(cRef);
    }

    public String exportLBConfig(String configName, String filePath)
        throws MBeanException
    {          
        _logger.log(Level.FINE, 
            "[LBAdminMBean] exportLbConfig called - LB Config " 
            + configName + ", File Path " + filePath);

        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();
            LbConfigWriter lbw = new LbConfigWriter(ctx, configName, filePath);
            String fName = lbw.write();
            _logger.log(Level.INFO, _sMgr.getString(
                    "http_lb_admin.ExportConfig", configName, fName));
            return fName;
        } catch (Exception e) {
            String msg = _strMgr.getString("LbExportFailed", 
                        configName, filePath);
            throw new MBeanException(e, msg);
        }
    }
    
    /**
    * Applies changes to the specified lb.
    *
    * @param  lbName  name of the loadbalancer
    */
    public void applyLBChanges(String configName, String lbName) throws MBeanException {
        if(lbName == null || lbName.equals("") || lbName.equals("null")) {
            applyLBChanges(configName);
            return;
        }
        _logger.log(Level.FINE, 
            "[LBAdminMBean] exportLbConfig called - LB Config " 
            + configName + " "+lbName);

        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();
            if(configName == null || configName.equals("") || configName.equals("null")) {
                com.sun.enterprise.config.serverbeans.LoadBalancers lbs =
                ((Domain)ctx.getRootConfigBean()).
                   getLoadBalancers();
                if(lbs == null)
                    throw new Exception("No Loadbalancers configured");
                com.sun.enterprise.config.serverbeans.LoadBalancer lb =
                   lbs.getLoadBalancerByName(lbName);   
                if(lb == null)
                    throw new Exception("No Loadbalancer configured by name " + lbName);
                configName = lb.getLbConfigName();
            }
            LbConfigPublisher lbp = new LbConfigPublisher(ctx, configName, lbName);
            lbp.publish();
            _logger.log(Level.INFO, _sMgr.getString(
                    "http_lb_admin.ExportConfig", configName, lbName));
        } catch (Exception e) {
            String msg = _strMgr.getString("LbExportFailed", 
                        configName, lbName);
            throw new MBeanException(e, msg);
        }
        
    }

    /**
     * Applies changes to all associated loadbalancers.
     */
    public void applyLBChanges(String configName) throws MBeanException {
        _logger.log(Level.FINE, 
            "[LBAdminMBean] exportLbConfig called - LB Config " 
            + configName );

        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();
            com.sun.enterprise.config.serverbeans.LoadBalancers loadbalancers = 
                ((Domain)ctx.getRootConfigBean()).
                   getLoadBalancers();
            if(loadbalancers == null)
                throw new Exception("No Loadbalancers configured");
            com.sun.enterprise.config.serverbeans.LoadBalancer[] lbs = 
                   loadbalancers.getLoadBalancer();
            if(lbs == null)
                throw new Exception("No Loadbalancers configured");
            for(com.sun.enterprise.config.serverbeans.LoadBalancer lb : lbs){
                if(lb == null)
                    throw new Exception("No Loadbalancers configured");
                if(lb.getLbConfigName().equals(configName)) {
                    LbConfigPublisher lbp = new LbConfigPublisher(ctx, configName, lb.getName());
                    lbp.publish();
                    _logger.log(Level.INFO, _sMgr.getString(
                            "http_lb_admin.ExportConfig", configName, lb.getName()));
                }
            }
        } catch (Exception e) {
            String msg = _strMgr.getString("LbExportFailed", 
                        configName, "");
            throw new MBeanException(e, msg);
        }
        
    }
    
    
    //------- PRIVATE VARIABLES --------------------
    private static final StringManager _strMgr = 
        StringManager.getManager(HTTPLBAdminConfigMBean.class);

    private static Logger _logger = Logger.getLogger(
			EELogDomains.EE_ADMIN_LOGGER);
    
    private static final StringManagerBase _sMgr =
        StringManagerBase.getStringManager(_logger.getResourceBundleName());

}
