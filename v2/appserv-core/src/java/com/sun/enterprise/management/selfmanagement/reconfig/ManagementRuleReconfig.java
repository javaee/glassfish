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

package com.sun.enterprise.management.selfmanagement.reconfig;

import com.sun.enterprise.management.selfmanagement.SelfManagementService;
import com.sun.enterprise.admin.event.selfmanagement.ManagementRuleEvent;
import com.sun.enterprise.admin.event.selfmanagement.ManagementRuleEventListener;

import com.sun.enterprise.admin.event.AdminEventListener;
import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.admin.event.selfmanagement.ManagementRuleEvent;
import com.sun.enterprise.admin.event.selfmanagement.ManagementRuleEventListener;
import com.sun.enterprise.config.ConfigAdd;
import com.sun.enterprise.config.ConfigSet;
import com.sun.enterprise.config.ConfigDelete;
import com.sun.enterprise.config.ConfigChange;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigUpdate;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.ManagementRules;
import com.sun.enterprise.config.serverbeans.ManagementRule;
import com.sun.enterprise.config.serverbeans.Action;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.Server;
import  com.sun.enterprise.admin.server.core.AdminService;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

/**
 * This class handles dynamic reconfiguration notification related to 
 * management-rule creation, update and deletion.
 *
 * @author Pankaj Jairath
 */ 
public final class ManagementRuleReconfig implements ManagementRuleEventListener{
    
    private static final SelfManagementService service = 
                                 SelfManagementService.getService();
    
    /** Logger for self management service */
    private static final Logger _logger = LogDomains.getLogger(LogDomains.SELF_MANAGEMENT_LOGGER);
    
    /**
     * Create a new management-rule 
     */
    public synchronized void handleCreate(ManagementRuleEvent event)
                                            throws AdminEventListenerException {
        _logger.log(Level.FINE,"sgmt.reconfig_handlecreatereceived");
        try{             
            ConfigContext configContext = event.getConfigContext();
            Config config = ServerBeansFactory.getConfigBean(configContext);
            ConfigAdd configAdd = null;
            ConfigChange configChange = null;
            ArrayList<ConfigChange> configChangeList = event.getConfigChangeList();
            ManagementRule rule = null;
            String xpath = null;
            String pXPath = null;
            Object object;
            String ruleName = null;
            for (int i=0; i < configChangeList.size(); i++){
                configChange = configChangeList.get(i);
                if (configChange instanceof ConfigAdd) {
                    _logger.log(Level.INFO,"sgmt.reconfig_handlecreaterulereceived");
                    xpath = configChange.getXPath();
                    if (xpath != null) {
                        _logger.log(Level.INFO, "sgmt.reconfig_handlexpath",xpath);
                        rule = (ManagementRule)configContext.exactLookup(xpath);
                        if (rule.isEnabled())
                            service.addRule(rule, configContext); 
                        else
                            service.addDisabledRule(rule);
                       
                    }
                } else if(configChange instanceof ConfigSet) {
                    //handle action addition
		    pXPath = configChange.getParentXPath();
		    String name = configChange.getName();
		    if (name.equals(ManagementRule.ACTION)) {
		        _logger.log(Level.INFO,"smgt.handleactionadd",pXPath);
		        rule = (ManagementRule)configContext.exactLookup(pXPath);
		        service.handleActionAdd(rule, configContext);
                    }
		}
            }
        } catch (Exception ex) {
           throw new AdminEventListenerException(ex);
        }  
    }
    
    
    /**
     * Update an existing management-rule.
     */
    public synchronized void handleUpdate(ManagementRuleEvent event)
                                            throws AdminEventListenerException {   
        _logger.log(Level.INFO,"sgmt.reconfig_handleupdatereceived");
        try {
            ConfigContext configContext = event.getConfigContext();
            ConfigContext adminConfigCtx =
                        AdminService.getAdminService().
                        getAdminContext().getAdminConfigContext();
            Config config = ServerBeansFactory.getConfigBean(configContext);
            ConfigUpdate configUp = null;
            ArrayList<ConfigUpdate> configChangeList = event.getConfigChangeList();
            String xPath = null;
            Object object;

            for (int i=0; i < configChangeList.size(); i++){
	        if (configChangeList.get(i) instanceof ConfigAdd ||
		    configChangeList.get(i) instanceof ConfigSet ||
		    configChangeList.get(i) instanceof ConfigDelete) {
		    ConfigChange change = configChangeList.get(i);
		    xPath =  change.getXPath();
	        } else {
                    configUp = configChangeList.get(i);
                    xPath = configUp.getXPath();
	        }
                if (xPath!=null) {
                    _logger.log(Level.INFO,"sgmt.handleupdatexpath", xPath);
                    object = configContext.exactLookup(xPath);
                    if (object instanceof ManagementRules) {
                        ManagementRules rules = (ManagementRules)object;
                        service.handleRulesUpdate(configContext,configUp);
                    } else if(object instanceof ManagementRule) {
                        ManagementRule rule = (ManagementRule)configContext.exactLookup(xPath); 
                        service.handleRuleUpdate(rule,configUp,configContext);
                    } else {
                        String pXPath = null;
                        if(object instanceof ElementProperty ||
			   xPath.lastIndexOf("element-property") >= 0  ) {
                            pXPath = xPath.substring(0,xPath.lastIndexOf("/"));
                            if(pXPath != null) {
                                pXPath = pXPath.substring(0,pXPath.lastIndexOf("/"));
                            }
                        } else {
                            pXPath = xPath.substring(0,xPath.lastIndexOf("/"));
                        }
                        if(pXPath != null) {
                            ManagementRule rule = (ManagementRule)configContext.exactLookup(pXPath); 
                            if(rule != null) {
                                String ruleName = rule.getName();

                                //Delete the rule (will delete the rule runtime)
                                service.deleteRule(ruleName);

                                //Recreate the rule
                                if (rule.isEnabled()) {
                                    service.addRule(rule, adminConfigCtx);
                                } else {
                                    service.addDisabledRule(rule);
                                }
                            }
                        }
                        break;
                    }
                 }
            }
                     
       
        } catch (Exception ex) {
           throw new AdminEventListenerException(ex);
        }
 
    }
    
    
    /**
     * Delete an existing management-rule. 
     */
    public synchronized void handleDelete(ManagementRuleEvent event) 
                                           throws AdminEventListenerException {
        _logger.log(Level.INFO,"sgmt.reconfig_handledeletereceived");                         
        try{
            ConfigContext oldConfigContext = event.getOldConfigContext();
			ConfigContext configContext = event.getConfigContext();
            ConfigContext adminConfigCtx =
                 AdminService.getAdminService().
                 getAdminContext().getAdminConfigContext();
            Config config = ServerBeansFactory.getConfigBean(adminConfigCtx);
            ConfigDelete configDel = null;
            ArrayList<ConfigDelete> configChangeList = event.getConfigChangeList();
            ManagementRule rule = null;
            String xpath = null;
            Object object;

            for (int i=0; i < configChangeList.size(); i++){
                configDel = configChangeList.get(i);

                xpath = configDel.getXPath();
                if (xpath != null){
                    _logger.log(Level.INFO, "sgmt.reconfig_handledelxpath",xpath);
		    object = oldConfigContext.exactLookup(xpath); 
                    if (object instanceof Action) {
                        //handle action removal - noaction
                        String realXPath = xpath.substring(0,xpath.lastIndexOf("/"));
                        rule = (ManagementRule) adminConfigCtx.exactLookup(realXPath);
                        String ruleName = rule.getName();
                        service.deleteAction(ruleName);
                    } else if (object instanceof ManagementRule) {
                        rule = (ManagementRule)object;
                        String ruleName = rule.getName();
                        service.deleteRule(ruleName);
                    } else {
                        String realXPath = xpath.substring(0,xpath.lastIndexOf("/"));
                        rule = (ManagementRule) adminConfigCtx.exactLookup(realXPath);
                        String ruleName = rule.getName();
                        service.deleteAction(ruleName);
		    }
                }
            }
        }  catch( Exception ex){
           throw new AdminEventListenerException(ex);
        }      
    }     
}
