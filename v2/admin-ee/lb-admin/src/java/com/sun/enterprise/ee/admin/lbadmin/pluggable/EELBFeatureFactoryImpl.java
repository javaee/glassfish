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

package com.sun.enterprise.ee.admin.lbadmin.pluggable;

import com.sun.enterprise.admin.monitor.stats.lb.LoadBalancerStats;
import com.sun.enterprise.ee.admin.lbadmin.mbeans.HTTPLBAdminConfigMBean;
import com.sun.enterprise.ee.admin.lbadmin.writer.LbConfigExporter;
import com.sun.enterprise.ee.admin.lbadmin.writer.LbConfigPublisher;
import com.sun.enterprise.ee.admin.lbadmin.monitor.LbMonitoringHelper;
import com.sun.enterprise.server.pluggable.LBFeatureFactory;
import com.sun.enterprise.config.ConfigContext;
	
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.admin.monitor.stats.lb.LoadBalancerStatsInterface;

import com.sun.logging.ee.EELogDomains;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.logging.Logger;
import java.util.logging.Level; 
import java.util.List;
import java.util.Date;


import com.sun.enterprise.ee.admin.lbadmin.writer.LbConfigWriter;

/**
 *
 * @author  hr124446
 */
public class EELBFeatureFactoryImpl implements LBFeatureFactory {
    
    /**
     * Creates a new instance of EELBFeatureFactoryImpl 
     */
    public EELBFeatureFactoryImpl() {
    }    

    /**
      Applies changes in the corresponding configuration to this LB Config 
     */
    public void applyChanges(String configName){
        _logger.log(Level.FINE, 
            "[LBAdminMBean] applyChanges called - LB Config " 
            + configName );

        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();
            com.sun.enterprise.config.serverbeans.LoadBalancers loadbalancers = 
                ((Domain)ctx.getRootConfigBean()).
                   getLoadBalancers();
            if(loadbalancers == null)
                throw new Exception(_strMgr.getString("NoLoadbalancersConfigured"));
            com.sun.enterprise.config.serverbeans.LoadBalancer[] lbs = 
                   loadbalancers.getLoadBalancer();
            if(lbs == null)
                throw new Exception(_strMgr.getString("NoLoadbalancersConfigured"));
            for(com.sun.enterprise.config.serverbeans.LoadBalancer lb : lbs){
                if(lb == null)
                    throw new Exception(_strMgr.getString("NoLoadbalancersConfigured"));
                if(lb.getLbConfigName().equals(configName)) {
                    LbConfigPublisher lbp = new LbConfigPublisher(ctx, configName, lb.getName());
                    lbp.publish();
                    _logger.log(Level.INFO, _sMgr.getString(
                            "http_lb_admin.ApplyChanges", lb.getName()));
                }
            }
        } catch (Exception e) {
            String msg = _strMgr.getString("LbApplyChangesFailed", 
                        configName,e.getMessage());
            _logger.log(Level.WARNING, msg);
            //throw new MBeanException(e, msg);
        }
        
    }
    
    /**
      Applies changes in the corresponding configuration to this LB and LB Config
     */    
    public void applyChanges(String configName, String lbName){
         if(lbName == null || lbName.equals("") || lbName.equals("null")) {
            applyChanges(configName);
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
                    throw new Exception(_strMgr.getString("NoLoadbalancersConfigured"));
                com.sun.enterprise.config.serverbeans.LoadBalancer lb =
                   lbs.getLoadBalancerByName(lbName);   
                if(lb == null)
                    throw new Exception( _strMgr.getString("LbDoesNotExist", lbName));
                configName = lb.getLbConfigName();
            }
            LbConfigPublisher lbp = new LbConfigPublisher(ctx, configName, lbName);
            lbp.publish();
            _logger.log(Level.INFO, _sMgr.getString(
                    "http_lb_admin.ApplyChanges", lbName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
       
    }
    
    /**
      @return true if there are pending changes for this LB
     */    
    public boolean isApplyChangeRequired(String lbConfigName,String lbName){
        //TODO
        return true;
    }

    /**
      Exports the corresponding LBConfig information and returns the contents as a string.
      @see com.sun.appserv.management.config.LBConfig
     */        
    public String getLoadBalancerXML(String configName,String lbName){
        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();
            if(configName == null || configName.equals("") || configName.equals("null")) {
                com.sun.enterprise.config.serverbeans.LoadBalancers lbs =
                ((Domain)ctx.getRootConfigBean()).
                   getLoadBalancers();
                if(lbs == null)
                    throw new Exception(_strMgr.getString("NoLoadbalancersConfigured"));
                com.sun.enterprise.config.serverbeans.LoadBalancer lb =
                   lbs.getLoadBalancerByName(lbName);   
                if(lb == null)
                    throw new Exception( _strMgr.getString("LbDoesNotExist", lbName));
                configName = lb.getLbConfigName();
            }
            String xml = LbConfigExporter.getXML(ctx, configName);
            _logger.log(Level.FINE, _sMgr.getString(
                    "http_lb_admin.GetLBXml", lbName));
            return xml;
        } catch (Exception e) {
            String msg = _strMgr.getString("LbGetXMLFailed", 
                        lbName, e.getMessage());
            //throw new MBeanException(e, msg);
            _logger.log(Level.WARNING, msg);
        }
        return "";
    }
    
    public LoadBalancerStatsInterface getLoadBalancerMonitoringStats(String configName,String lbName){
        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();
            if(configName == null || configName.equals("") || configName.equals("null")) {
                com.sun.enterprise.config.serverbeans.LoadBalancers lbs =
                ((Domain)ctx.getRootConfigBean()).
                   getLoadBalancers();
                if(lbs == null)
                    throw new Exception(_strMgr.getString("NoLoadbalancersConfigured"));
                com.sun.enterprise.config.serverbeans.LoadBalancer lb =
                   lbs.getLoadBalancerByName(lbName);   
                if(lb == null)
                    throw new Exception( _strMgr.getString("LbDoesNotExist", lbName));
                configName = lb.getLbConfigName();
            }else if(lbName == null){
                
            }
            LbMonitoringHelper lbMonitoringHelper = new LbMonitoringHelper(ctx, configName, lbName);
            String xml = lbMonitoringHelper.getMonitoringXml();
            LoadBalancerStatsInterface lbstats = LoadBalancerStats.readNoEntityResolver(new ByteArrayInputStream(xml.getBytes()));
            _logger.log(Level.INFO, _sMgr.getString(
                    "http_lb_admin.GetStats",  lbName));
            return lbstats;
        } catch (Exception e) {
            String msg = _strMgr.getString("LbGetStatsFailed", 
                        configName, lbName);
            //throw new MBeanException(e, msg);
            _logger.log(Level.WARNING, msg);
        }
        return null;
    }   
    
    /**
      Test the connection between DAS and LoadBalancer.
     */
    public boolean testConnection(String configName, String lbName){
        boolean result=false;
        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();
            if(configName == null || configName.equals("") || configName.equals("null")) {
                com.sun.enterprise.config.serverbeans.LoadBalancers lbs =
                ((Domain)ctx.getRootConfigBean()).
                   getLoadBalancers();
                if(lbs == null)
                    throw new Exception(_strMgr.getString("NoLoadbalancersConfigured"));
                com.sun.enterprise.config.serverbeans.LoadBalancer lb =
                   lbs.getLoadBalancerByName(lbName);   
                if(lb == null)
                    throw new Exception( _strMgr.getString("LbDoesNotExist", lbName));
                configName = lb.getLbConfigName();
            }
            LbConfigPublisher lbp = new LbConfigPublisher(ctx, configName, lbName);
            result = lbp.ping();
            _logger.log(Level.INFO, _sMgr.getString(
                    "http_lb_admin.TestConnection", Boolean.toString(result), lbName));
        } catch (Exception e) {
            String msg = _strMgr.getString("LbTestConnectionFailed", 
                         lbName);
            //throw new MBeanException(e, msg);
            _logger.log(Level.WARNING, msg);
        }
        return result;
    }
    
    /**
      Reset the monitoring stats on this loadbalancer.
     */
    public void resetStats(String configName, String lbName) {
        try {
            ConfigContext ctx = AdminService.getAdminService().getAdminContext()
                                    .getAdminConfigContext();
            if(configName == null || configName.equals("") || configName.equals("null")) {
                com.sun.enterprise.config.serverbeans.LoadBalancers lbs =
                ((Domain)ctx.getRootConfigBean()).
                   getLoadBalancers();
                if(lbs == null)
                    throw new Exception(_strMgr.getString("NoLoadbalancersConfigured"));
                com.sun.enterprise.config.serverbeans.LoadBalancer lb =
                   lbs.getLoadBalancerByName(lbName);   
                if(lb == null)
                    throw new Exception( _strMgr.getString("LbDoesNotExist", lbName));
                configName = lb.getLbConfigName();
            }
            LbMonitoringHelper lbMonitoringHelper = new LbMonitoringHelper(ctx, configName, lbName);
            lbMonitoringHelper.reset();
            _logger.log(Level.INFO, _sMgr.getString(
                    "http_lb_admin.ResetStats", lbName));
        } catch (Exception e) {
            String msg = _strMgr.getString("LbResetStatsFailed", 
                         lbName);
            _logger.log(Level.WARNING, msg);
            throw new RuntimeException(e);
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
