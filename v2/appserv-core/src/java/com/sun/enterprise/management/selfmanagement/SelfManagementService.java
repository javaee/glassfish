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
package com.sun.enterprise.management.selfmanagement;

import com.sun.appserv.server.ServerLifecycle;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.enterprise.server.ServerContext;

import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.ManagementRules;
import com.sun.enterprise.config.serverbeans.ManagementRule;
import com.sun.enterprise.config.serverbeans.Event;
import com.sun.enterprise.config.serverbeans.Action;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.ConfigUpdate;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;

import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.event.AdminEventListenerRegistry;
import com.sun.enterprise.admin.event.selfmanagement.ManagementRuleEvent;
import com.sun.enterprise.management.selfmanagement.reconfig.ManagementRuleReconfig;

import javax.management.MBeanServer;
import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;

//import com.sun.enterprise.admin.selfmanagement.event.Event;
import com.sun.enterprise.admin.selfmanagement.event.EventBuilder;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.sun.logging.LogDomains;

/**
 * The core class for the self management service. As part of
 * server startup, it reads in the instance management rules
 * and sets them up.
 *
 * @author Pankaj Jairath
 */
public class SelfManagementService implements ServerLifecycle {
    
    static private final Logger _logger = LogDomains.getLogger(
                                            LogDomains.SELF_MANAGEMENT_LOGGER);
    
    private static SelfManagementService serviceInstance = null;

    private RuleManager rulesManager = null;
    
    private EventBuilder eventBuilder = null;
    
    private boolean isServiceEnabled = false;
    
              
    /** Creates a new instance of SelfManagementService */
    public SelfManagementService() {
        
    }
 
    /**
     * Server is initializing subsystems and setting up the runtime environment.
     * Prepare for the beginning of active use of the public methods of this
     * subsystem. This method is called before any of the public methods of 
     * this subsystem are utilized.  
     *
     * @param sc ServerContext the server runtime context.
     *
     * @exception IllegalStateException if this subsystem has already been
     *  started
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onInitialization(ServerContext sc) 
                        throws ServerLifecycleException {
        /*
         *would need support from domain.xml management_rules to support
         *service properties.
         */
        final String RULES_MAIL_SMTP_HOST = "mail.smtp.host";
        final String RULES_ALERT_RECIPIENTS = "rules.recipients";
                
        // initialize the Rules Manager  
        rulesManager = new RuleManager(this);
        eventBuilder = EventBuilder.getInstance();
        _logger.log(Level.FINE, "smgt.service_init");
        
        String mailSMTPHost = java.lang.System.getProperty(RULES_MAIL_SMTP_HOST);
        String alertRecip = java.lang.System.getProperty(RULES_ALERT_RECIPIENTS);
        rulesManager.setMailSMTPHost(mailSMTPHost);
        rulesManager.setRecipients(alertRecip);
        serviceInstance = this;

        _logger.log(Level.FINE, "smgt.service_initialized");
    }

    /**
     * Server is starting up applications
     *
     * @param sc ServerContext the server runtime context.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onReady(ServerContext sc) 
                        throws ServerLifecycleException {        
                
        try {
            ConfigContext instanceConfigContext = sc.getConfigContext();
            Config instanceConfig = ServerBeansFactory.getConfigBean(
                                                         instanceConfigContext);
            ManagementRules selfManagementRules = instanceConfig.getManagementRules();
       
            if ( selfManagementRules != null ) {
                if ( selfManagementRules.isEnabled() ) {
                    _logger.log(Level.INFO, "smgt.service_enabled");               
                    isServiceEnabled = true;
                    
                    ManagementRule[] rules = selfManagementRules.getManagementRule(); 
                    if (rules != null) {
                        for (int i = 0; i < rules.length; i++) {
                            ManagementRule rule = rules[i];              
                            String ruleName = rule.getName();
                            String ruleDescription = rule.getDescription();
                        
                            //check if rule is enabled then obtain event else skip
                           if (rule.isEnabled()) {
                               // statically read ctx is used
                               addRule(rule,null);
                           } else {
                               _logger.log(Level.FINE, "smgt.service_rule_disabled",
                                       new Object[]{ruleName, ruleDescription});       
                               //addDisabledRule(ruleName, ruleDescription);
                               addDisabledRule(rule);
                           }
                       }
                   } else {
                        _logger.log(Level.FINE,"smgt.service_no_rules_defined"); 
                   }    
                } else {
                      _logger.log(Level.INFO,"smgt.service_disabled");
                }
            } else {
                  _logger.log(Level.INFO, "smgt.service_not_configured");
            }

            //activate this after the admin backend support is achieved 
            AdminEventListenerRegistry.addEventListener(ManagementRuleEvent.eventType,
                                                    new ManagementRuleReconfig());
        } catch (ConfigException ex) {
            //just log, core can still come up, just warn at severe                        
            _logger.log(Level.SEVERE, "smgt.config_error", ex);  
            //throw new ServerLifeCycleException(ex.getMessage(), ex);
        } catch (Exception ex) {
            _logger.log(Level.SEVERE, "sgmt.config_error, ex");
        }
    }

    /**
     * Server has complted loading the applications and is ready to serve requests.
     *
     * @param sc ServerContext the server runtime context.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onStartup(ServerContext sc) throws ServerLifecycleException {
        _logger.log(Level.FINE, "smgt.service_onready");

    }

    /**
     * Server is shutting down applications
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onShutdown() 
                        throws ServerLifecycleException {
        _logger.log(Level.FINE, "smgt.service_ontermination");
    }

    /**
     * Server is terminating the subsystems and the runtime environment.
     * Gracefully terminate the active use of the public methods of this
     * subsystem.  This method should be the last one called on a given
     * instance of this subsystem.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onTermination() 
                        throws ServerLifecycleException {
        _logger.log(Level.FINE, "smgt.service_onshutdown");
    }    
    
    /**
     * Checks if the event has been already defined for the management
     * rules.
     *
     * @return Event The existing event or null if no such event exists.
     */
    private com.sun.enterprise.admin.selfmanagement.event.Event checkEventExist(
                                    String type, ElementProperty[] eventProps) {
        //pass the invocation to the contained core instance
        com.sun.enterprise.admin.selfmanagement.event.Event event = 
                               rulesManager.checkEventExists(type, eventProps);
                                                   
        return event;      
    }
    
    /**
     * Invokes the rule managers add rule to form the runtime binding
     */
    public void addRule(ManagementRule rule, ConfigContext ctxToUse) {
                  
        String actionMBeanName = null;
        boolean isSkip = false;
        String ruleName = rule.getName();
        String ruleDescription = rule.getDescription();
        
        try {
            final int ATTRIB_COUNT = 3;
            com.sun.enterprise.config.serverbeans.Event event = null;
            com.sun.enterprise.admin.selfmanagement.event.Event ruleEvent = null;  
            Map logHandback = new HashMap<String,String>(ATTRIB_COUNT);
            
            _logger.log(Level.INFO, "smgt.service_rule_enabled", 
                        new Object[]{ruleName, ruleDescription});
                        
            //getEvent                       
            event = rule.getEvent();
            String type = event.getType();
            String description = event.getDescription();
                                                             
            logHandback.put(ServerTags.TYPE, type);                 
            logHandback.put(ServerTags.RECORD_EVENT, String.valueOf(event.isRecordEvent()));
            logHandback.put(ServerTags.LEVEL,event.getLevel());
            
            //Properties eventProperities = new Properties();
                       
            ElementProperty[] eventProps = event.getElementProperty();
                                                      
            /*activate after dependency support provided*/
            //Event event = checkEventExists(type, eventProps);
                                
            //EventBuilder eventBuilder = EventBuilder.getInstance();
            ruleEvent = getEvent(type, eventProps,description, ruleName);
            if (ruleEvent == null) {
                //skip this rule, errorenous event props
                return ;
            }
        
            Action ruleAction = rule.getAction(); 
            if (ruleAction != null) {
                actionMBeanName = ruleAction.getActionMbeanName();
            }
       
            // pass the invocation to the contained core instance
            rulesManager.addRule(ruleName, description, ruleEvent,
                                 actionMBeanName, logHandback, ctxToUse);
        } catch (InstanceNotFoundException ex) {
            _logger.log(Level.INFO, "smgt.internal_error", ex);
        }
    }

    /** 
     * Deletes a configured rule
     *
     * @param name: Name of the rule to delete
     *
     */
    public void deleteRule(String rule) {
         //delegate it to the rule manager
         rulesManager.deleteRule(rule);
    }

    
    /**
     * Adds to the list of disabled rules
     */
    public void addDisabledRule(ManagementRule rule) {
        //pass the invocation to rule manager
        String ruleName = rule.getName();
        String ruleDescription = rule.getDescription();
        rulesManager.addDisabledRule(ruleName, ruleDescription);
    
    }
    
    /**
     * Obtains the configured event
     *
     * @return The event instance
     */
    private com.sun.enterprise.admin.selfmanagement.event.Event getEvent(
                                                  String type,
                                                  ElementProperty[] eventProps,
                                                  String description, 
                                                  String ruleName) {
        com.sun.enterprise.admin.selfmanagement.event.Event eventInstance = null;
        
        try {
            eventInstance = eventBuilder.getEvent(type, eventProps, description);            
        } catch( IllegalArgumentException ex) {
            // some issue with configuring the event props
            _logger.log(Level.INFO, "smgt.errorenous_event", new Object[] 
                         {ruleName, type});                        
        }
        
        return eventInstance;
    }
    
    /** Obtain instance to the service */
    public static SelfManagementService getService() {
        return serviceInstance;
    }

    /**
     * handle Update for parent node - Management Rules
     *
     * @param configUpdate for the node
     */
    public void handleRulesUpdate(ConfigContext cfgCtx, ConfigUpdate configUp)
                        throws Exception {
       Set<String> attributesChg = configUp.getAttributeSet();
       for (String attribute : attributesChg) {
           if (attribute.equals(ServerTags.ENABLED)) {
               String newValue = configUp.getNewValue(attribute); 
               if (isServiceEnabled && newValue.equals("false")) {
                   _logger.log(Level.INFO,"sgmt.disableservice");
                   synchronized(this) {
                       isServiceEnabled = false;
                       rulesManager.disableService();
                   }
               } else if (!isServiceEnabled && newValue.equals("true")) {
                   _logger.log(Level.INFO,"sgmt.enableservice");
                   synchronized(this) {
                       isServiceEnabled = true;
                       handleEnableService(cfgCtx);
                   }
               }
           }
       }  
    }

  /**
   * Handle Action addition
   *
   * @param rule - ManagementRule for which action is being added
   * @param action - Action being added
   */
   public void handleActionAdd(ManagementRule rule, ConfigContext ctxToUse)
                       throws Exception {
       String ruleName = rule.getName();
       String description = rule.getDescription();
       Action actionToAdd = rule.getAction();
       //Should be the case with validator
       if (actionToAdd!=null) {
           String actionName = actionToAdd.getActionMbeanName();
           //validator? 
           if (isServiceEnabled && rule.isEnabled())  {
               if (actionName!=null) {

                   rulesManager.addAction(ruleName,actionName,ctxToUse);
               }
           } else {
               _logger.log(Level.INFO, "smgt.erroraddaction_notenabled",
                           new Object[]{ruleName,actionName});
           }
       } else {
           _logger.log(Level.SEVERE,"smgt.error_add_noactionreceived",  
                       new Object[]{ruleName,description});
           throw new RuntimeException();
       }
   }

   /**
    * Handle Action upate
    *
    * @param rule : ManagementRule element that has been updated
    *        configUp : The set of config changes on the child Action element 
    */
   public void handleActionUpdate(ManagementRule rule, ConfigUpdate configUp) {
       //not yet supported
   }

   /**
    * Handle ManagementRule update
    *
    * @param rule : ManagementRule being updated
    *        configUp : The set of config changes on the rule.
    */
   public void handleRuleUpdate(ManagementRule rule, ConfigUpdate configUp,
                                ConfigContext ctxToUse)
                       throws Exception {
       //pass on to the delegate 
       rulesManager.updateRule(rule, configUp, ctxToUse); 
   }

   /**
    * handles the management rules being enabled
    *
    * @param configCtx The instance config context containing the management
    *                  rules
    */
   void handleEnableService(ConfigContext configCtx) {
       try {
           Config instanceConfig = ServerBeansFactory.getConfigBean(configCtx);
           ManagementRules selfManagementRules = instanceConfig.getManagementRules();
           ManagementRule[] rules = selfManagementRules.getManagementRule();

           for (ManagementRule rule : rules) {
               if (rule.isEnabled()) {
                   addRule(rule, configCtx);
               } else {
                   addDisabledRule(rule);
               }
           }
       } catch (ConfigException ex) {
           _logger.log(Level.WARNING,"sgmt.error_enableservice", ex);           
       } 
   }

   boolean isServiceEnabled() {
       return isServiceEnabled;
   }

   /**
    * Deletes an action associated with the configured rule
    *
    * @param name: Name of the rule for which action is to be deleted
    *
    **/
   public void deleteAction(String rule) {
       rulesManager.deleteAction(rule);
   }
}
