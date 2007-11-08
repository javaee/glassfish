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

import com.sun.enterprise.admin.server.core.CustomMBeanException;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Mbean;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Clusters;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ManagementRule;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.ServerTags;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigUpdate;

import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.selfmanagement.event.*;
import com.sun.enterprise.management.selfmanagement.LogMgmtEventsNotificationListener;
import com.sun.enterprise.admin.server.core.CustomMBeanRegistration;

import com.sun.appserv.management.alert.MailAlert;        
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.NotificationFilter;
import javax.management.NotificationFilterSupport;
import javax.management.InstanceNotFoundException;
import javax.management.RuntimeOperationsException;
import javax.management.MalformedObjectNameException;
import javax.management.InstanceAlreadyExistsException;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.reflect.Constructor;
import java.lang.String;        

import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * This is the core class, which manages the runtime of the
 * configured management_rules.
 *
 * @author Pankaj Jairath
 */
public final class RuleManager {
    
    /** Logger for self management service */
    private static final Logger _logger =  LogDomains.getLogger(LogDomains.SELF_MANAGEMENT_LOGGER);
    
    /** Local Strings manager for the class */
    private static final StringManager localStrings = StringManager.getManager(RuleManager.class);
    
    /** A HashMap that contains all enabled and active rules */
    private final Map<String,Rule> activeRules;
    
    /** HashMap that contains all enabled but inactive rules */    
    private final Map<String,Rule> inActiveRules;
    
    /** HashMap that contains all the rules that have no actions. These
     * would have their configured events logged.
     */
    private final Map<String,Rule> noActionRules;
     
    /** A HashMap that contains all the disabled rules */
    private final Map<String,Rule> disabledRules;
    
    /** */
    private final String USER_DEFINED_TYPE = "user";
       
    /** */
    private ConfigContext configCtx = null;
    /** */
    private final String instanceName;

    /** Instance MBeanServer */
    private MBeanServer instanceMbs = null;
    
    /** SMTP host for service alert mails */
    private String mailSMTPHost = null;
    
    /** Recipients of service alert messages */
    private String srvAlertRecipients = null;    

    /** Self management service instance */
    private SelfManagementService service = null;
       
    /** Creates a new instance of RuleManager */
    public RuleManager(SelfManagementService service) {
        this.service = service;
        instanceName = (ApplicationServer.getServerContext()).getInstanceName();
        AdminService adSrv = AdminService.getAdminService();
        instanceMbs = (adSrv.getAdminContext()).getMBeanServer();
        configCtx = (ApplicationServer.getServerContext()).getConfigContext();
    
        activeRules = Collections.synchronizedMap(new HashMap<String,Rule>());
        inActiveRules = Collections.synchronizedMap(new HashMap<String,Rule>());
        noActionRules = Collections.synchronizedMap(new HashMap<String, Rule>());
        disabledRules = Collections.synchronizedMap(new HashMap<String,Rule>());
    }
    
    /**
     *
     */
    public void addRule(String ruleName, String description, Event ruleEvent,
                      String actionMBeanName, Object handback,
                      ConfigContext ctxToUse) throws InstanceNotFoundException {
        ObjectName actionObjName = null;
        Object alertAction = null;
        Rule ruleInstance = new Rule(ruleName, description);
        //rule.setRuleEvent(ruleEvent);
        if (ctxToUse!=null) {
            configCtx = ctxToUse;
        }
        
        try {
            if (actionMBeanName != null) {
            
                Domain domain = ServerBeansFactory.getDomainBean(configCtx);
                ApplicationRef appRef = verifyActionMBean(actionMBeanName, domain,
                                                          ctxToUse);
       
       
                if (appRef != null) {
                    Applications apps = domain.getApplications();
                    Mbean definedMBean = apps.getMbeanByName(actionMBeanName);    
                
                   
                    if (appRef.isEnabled() && definedMBean.isEnabled() ) {
                        if ((definedMBean.getObjectType()).equals(USER_DEFINED_TYPE)) {
                            actionObjName = registerAction(ruleEvent,actionMBeanName,
                                                           definedMBean,false, handback);
                        } else {
                            // system defined, need to explictly load
                            actionObjName = registerAction(ruleEvent,actionMBeanName,
                                                           definedMBean,true, handback); 
                        }
                        //obtain the action instance from name
                        setupRuleBinding(Rule.ENABLED, ruleInstance, ruleEvent,
                                         (Object)actionObjName);
                        //rule.setActionMBean(actionInstance);
                        //rule.setState(Rule.ENABLED);
                        //addToActiveRuleMap(ruleName,rule);
                    } else {
                        /* action is disabled, but rule is active; Add a default mail
                         * alert
                         */
                        alertAction = registerAlertAction(ruleEvent, actionMBeanName,
                                                          ruleName, handback);
                        _logger.log(Level.WARNING,"sgmt.ruleactiondisabled",
                                    new Object[]{ruleName,actionMBeanName});
                        
                        setupRuleBinding(Rule.INACTIVE, ruleInstance, ruleEvent,
                                         alertAction);
                        //rule.setActionMbean(actionInstance);
                        //rule.setState(Rule.INACTIVE);
                        //addToInActiveRuleSet(ruleName,rule);
                    }
                 } else {
                     //log error, invalid action MBean provided for this rule
                     _logger.log(Level.SEVERE, "smgt.invalid_action", new Object[]{
                                 ruleName, actionMBeanName});
                     //config error
                 }
            } else {
                //no action rule, event would simply record itself
                setupRuleBinding(Rule.NOACTION, ruleInstance, ruleEvent, null);
            }
        } catch (ConfigException ex) {
            _logger.log(Level.INFO, "config_error", ex);
        } catch (InstanceNotFoundException ex) {
            //log it, 
            _logger.log(Level.INFO, "smgt.error_reg_action", new Object[]{
                        ruleName, actionMBeanName});
            registerAlertAction(ruleEvent, actionMBeanName, ruleName, handback);            
        } catch (MalformedObjectNameException ex) {
            _logger.log(Level.WARNING, "sgmt.error_reg_action", new Object[]{
                        ruleName, actionMBeanName});
        } catch (RuntimeOperationsException ex) {
            //log it, actionMBean is not a listener type, add to inactive rules
            _logger.log(Level.WARNING, "smgt.error_reg_action", new Object[]{
                        ruleName,actionMBeanName});
            registerAlertAction(ruleEvent, actionMBeanName, ruleName, handback);
        } catch (CustomMBeanException cmbe) {
            /**  
	     * log it, as thrown from customRegistration, add to inactive rules
	     */
	    _logger.log(Level.WARNING, "smgt.error_reg_action", new Object[] {
			ruleName, actionMBeanName});
	    registerAlertAction(ruleEvent, actionMBeanName, ruleName, handback);
        } catch (RuntimeException ex) {
            /*
             *log it, as thrown from customRegistration, add to inactive rules
             */
            _logger.log(Level.WARNING, "smgt.error_reg_action", new Object[] {
                        ruleName, actionMBeanName});
            registerAlertAction(ruleEvent, actionMBeanName, ruleName, handback);
       }
    }
        
    /**
     * Verify's whether the action MBean is a referenced application
     * by the server instance and also has a valid definition at the domain
     * level
     *
     * @param actionMBeanName
     * @param domain
     * @return ApplicationRef
     */
    private ApplicationRef verifyActionMBean(String actionMBeanName,
                                             Domain domain,
                                             ConfigContext ctxToUse) {
        ApplicationRef appRef = null;
        if (ctxToUse != null) {
           configCtx = ctxToUse;
        }
        
        try {
            Server instanceBean = ServerBeansFactory.getServerBean(configCtx);
            appRef = instanceBean.getApplicationRefByRef(actionMBeanName);
        
            if (appRef == null) {
                //check at cluster level, if this instance is part of cluster
                Clusters clusters = domain.getClusters();
                Cluster[] cluster = clusters.getCluster();
                for (Cluster val : cluster) {
                    if ((val.getServerRefByRef(instanceName)) != null) {
                        appRef = val.getApplicationRefByRef(actionMBeanName);
                        break;
                    } 
                }
            }
        
            //should have obtained the app reference, if all well
            if (appRef != null) {
                //check there exists a definition
                Applications apps = domain.getApplications();
                Mbean definedMBean = apps.getMbeanByName(actionMBeanName); 
                if (definedMBean == null ) {
                    appRef = null;
                }
            }
        } catch (ConfigException ex) {
            _logger.log(Level.INFO, "smgt.config_error", ex);
        }
        
        return appRef;
    }

    /** 
     * Handles deletion of an existing rule 
     *
     * @param name The Rule name which needs to be deleted
     */
    public void deleteRule(String name) {
        Event ruleEvent = null;
        String description = null;
        Rule ruleInstance = null;
        
        try {
            if (activeRules.containsKey(name)) {
                //remove from the active list and its binding
                ruleInstance = activeRules.remove(name);
                ruleEvent = ruleInstance.getEvent();
                NotificationFilter filter = ruleEvent.getNotificationFilter();
                ObjectName eventObjName = ruleEvent.getObjectName();

                ObjectName actionObjName = (ObjectName)ruleInstance.getAction();
				if(actionObjName != null) {
                    instanceMbs.removeNotificationListener(eventObjName,
									actionObjName, filter, null);
				}
                ruleEvent.destroy();
            } else if (inActiveRules.containsKey(name)) {
                //remove from in active set and its binding
                ruleInstance = inActiveRules.remove(name);
                //alert action ?
                ruleEvent = ruleInstance.getEvent();
                ruleEvent.destroy();
            } else if (noActionRules.containsKey(name)) {
                //remove from no action set and its binding
                ruleInstance = noActionRules.remove(name);
                ruleEvent = ruleInstance.getEvent();
                ruleEvent.destroy();
            } else if (disabledRules.containsKey(name)) {
                //remove from disabled set
                ruleInstance = disabledRules.remove(name);
            } else {
                //log error
                _logger.log(Level.WARNING,"sgmt.delete_invalidname", name);
                return;
            }
        } catch (Exception ex) {
            _logger.log(Level.WARNING, "sgmt.error_delete_rule", 
                        new Object[]{name,ex.toString()});
            return;
        }    
       
        description = ruleInstance.getDescription();

        _logger.log(Level.INFO,"sgmt.deleted_rule",
                    new Object[] {name,description});

        //ensure gc of contained event
        ruleInstance = null;
    }

    
    /**
     * Handles addition of disabled rules under management_rules
     * as configured in domain.xml
     *
     * @param ruleName
     *        Identifies the defined management rule.
     * @param description
     *        Associates message with the intent of the defined rule.
     */
    public void addDisabledRule(String ruleName, String ruleDescription) {
        Rule ruleInstance = new Rule(ruleName, ruleDescription);
        setupRuleBinding(Rule.DISABLED, ruleInstance, null, null);
    }
    
    /**
     * Determines if an event already exists which matches the 
     * details of the passed type and configured event properties
     *
     *@return Event : The already existing event instance
     *        null  : If no such event has been configured
     */         
    Event checkEventExists(String type, ElementProperty[] eventProps) {
        //placeHolder
        return null;
        
    }
    
    /**
     * For valid and enabled rule definitions registers the action to the
     * defined event.
     *
     * @param ruleEvent
     * @param actionMBeanName
     * @param definedActionMBean
     * @param isSystemDefined
     * @param handback
     * @return ObjectName
     */
    private ObjectName registerAction(Event ruleEvent, String actionMBeanName,
            Mbean definedActionMBean, boolean isSystemDefined, Object handback) 
	    throws InstanceNotFoundException, MalformedObjectNameException, 
            CustomMBeanException {
       ObjectName actionObjName = null;
       ObjectName eventObjName = ruleEvent.getObjectName();
       NotificationFilter filter = ruleEvent.getNotificationFilter();

       try { 
            //check if this is a system defined rule then explictly load the MBean       
            if (isSystemDefined) {
                CustomMBeanRegistration obj = getCustomMBeanRegistration();
                actionObjName = obj.registerMBean(definedActionMBean);
                instanceMbs.addNotificationListener(eventObjName, actionObjName,
                                                    filter, null);    
            } else {
                //user defined should have been loaded by adminservice
                //if mbean has defined object name, use that to obtain
                //obj instance else use objectnames static call for apps
                String objName = definedActionMBean.getObjectName();
                if (objName == null) {
                    //should have been assigned implict obj name
                    actionObjName = ObjectNames.getApplicationObjectName(
                                                                   instanceName,
                                                                   actionMBeanName);
                } else {
                    //actionObjName = new ObjectName(objName);
                    actionObjName = getCascadedObjectName(objName);
                }

                instanceMbs.addNotificationListener(eventObjName, actionObjName,
                                                    filter, null);
                if (handback!=null) {
	            createLoggingListener (ruleEvent, handback);
                }
            }  
        } catch (CustomMBeanException ex) {
            if ( ex.getCause() instanceof InstanceAlreadyExistsException) {
                String objName = definedActionMBean.getObjectName();
                actionObjName = getCascadedObjectName(objName); 
            } else {
                throw ex;
            }
        }

      return actionObjName;
    }
    
    /**
     * Registers MailAlert listener for the defined enabled rule incase the
     * there has been error loading/registering the action when loading
     * the system applications.
     *
     * @param ruleEvent
     * @param actionMBeanName
     * @param ruleName
     * @param handback
     */
    private Object registerAlertAction(Event ruleEvent, String actionMBeanName,
                                       String ruleName, Object handback) 
                                       throws InstanceNotFoundException {
        NotificationFilterSupport filter = null;
        if (srvAlertRecipients == null) {
            //disable the event in filter
            filter = new NotificationFilterSupport();
            filter.disableAllTypes();
        }
        String subject = localStrings.getString("ruleMgr.alert_action",
                                                    ruleName, actionMBeanName);
        MailAlert alertAction = new MailAlert();
        alertAction.setSubject(subject);
        alertAction.setRecipients(srvAlertRecipients);
        

        ObjectName eventObjName = ruleEvent.getObjectName();
        // instanceMbs.addNotificationListener(eventObjName, alertAction, filter, null);
	createLoggingListener (ruleEvent, handback);

        return alertAction; 
    }    
    /**
     * Setup the the bindings for the RuelManager for defined rules in 
     * domain.xml
     *
     * @param ruleState
     * @param ruleInstance
     * @param ruleEvent
     * @param action
     *        Incase of enabled rule, this would be it's object name
     *        Incase of inactive rule, this would be mail alert obj listener.
     */
    private void setupRuleBinding(int ruleState, Rule ruleInstance, 
                                  Event ruleEvent, Object action) {
        
        String ruleName = ruleInstance.getName();
        
	Map ruleMap = getRuleMap(ruleName);
	if (ruleMap != null) 
	    ruleMap.remove(ruleName);
        ruleInstance.setState(ruleState);
        if (ruleState != Rule.DISABLED) {
            ruleInstance.setEvent(ruleEvent);
            ruleInstance.setAction(action);
            ruleInstance.setEnabled(true);
        }
        
        switch (ruleState) {
        case Rule.ENABLED:           
            activeRules.put(ruleName, ruleInstance);
            break;
             
        case Rule.INACTIVE:
            inActiveRules.put(ruleName, ruleInstance);
            break;
        
        case Rule.NOACTION:
            noActionRules.put(ruleName, ruleInstance);
            break;
            
        case Rule.DISABLED:
            //event and action would be null by default during rule creation
            disabledRules.put(ruleName, ruleInstance);
            break;
        }
    }

    /**
     * Retrives the custom MBean interface responsible for loading/registering
     * the custom defined MBeans. 
     */
    private CustomMBeanRegistration getCustomMBeanRegistration() {

        //This a temp support, till appropriate provider is provided later
        final String CUSTOM_REGRISTRATION_IMPL_CLASS = 
          "com.sun.enterprise.admin.mbeans.custom.loading.CustomMBeanRegistrationImpl";
        CustomMBeanRegistration customObj = null; 

        Constructor ctor = null;
	Object[] args =  null;

        try {
            Class c = Class.forName(CUSTOM_REGRISTRATION_IMPL_CLASS);
            Class[] params = new Class[]{javax.management.MBeanServer.class};
            ctor = c.getConstructor(params);
            args = new Object[]{instanceMbs};

            customObj = (CustomMBeanRegistration) ctor.newInstance(args); 
        } catch (Exception ex) {
            _logger.log(Level.FINE,"smgt.internal_error", ex);
        }

        return customObj;
    } 
    
    /**
     * Sets the service property which defines the mail SMTP host to be
     * used for sending the service alert messages
     *
     * @param value
     *        SMTP host
     */
    public void setMailSMTPHost(String value) {
        if ( mailSMTPHost != null) {
            mailSMTPHost = value;
        } else {
            mailSMTPHost = "localhost";
        }
    }
    
    /**
     * Sets the service property which defines the recipients of alert
     * messages from the service
     *
     * @param value
     *        Recipients of alert messages
     */
    public void setRecipients(String value) {
        srvAlertRecipients = value;
    }
    
    /*
     * Provides the cascading aware object name
     */
    private ObjectName getCascadedObjectName(String actionMBeanName) 
                                           throws MalformedObjectNameException{
        ObjectName configObjName = new ObjectName(actionMBeanName);
        String serverNameKey  = ServerTags.SERVER;
        Hashtable properties  = configObjName.getKeyPropertyList();
        properties.put(serverNameKey, instanceName);
        ObjectName casdObjName = new ObjectName(configObjName.getDomain(),
                                                     properties);
        return (casdObjName);
    }

	
    private void createLoggingListener (Event event, Object handback) 
		throws InstanceNotFoundException{
	NotificationListener logListener = LogMgmtEventsNotificationListener.getInstance(); 
	instanceMbs.addNotificationListener (event.getObjectName(), logListener, event.getNotificationFilter(), handback);
	return;
    }

   /**
    * Handles an update to a configured rule.
    *
    * @param rule - The config bean for the rule from the old config 
    *               context
    * @param configUp - Contains the config changes for the rule 
    * @param ctxToUse - The updated config context
    */
   void updateRule(ManagementRule rule, ConfigUpdate configUp,
                   ConfigContext ctxToUse) throws Exception {
       String name = rule.getName();
       Rule ruleInstance = getRuleInstance(name);

       // null should not be the case with validator check
       if (ruleInstance!=null) {
           synchronized(ruleInstance) {
               String description = ruleInstance.getDescription();
               Set<String> attributesChg = configUp.getAttributeSet();
               for (String attribute : attributesChg) {
                   if (attribute.equals(ServerTags.ENABLED)) {
                       String oldValue = configUp.getOldValue(attribute);
                       String newValue = configUp.getNewValue(attribute);
                       _logger.log(Level.INFO,"sgmt.updateruleattribute",
                                   new Object[]{attribute,oldValue,newValue});
                       if (ruleInstance.isEnabled() && newValue.equals("false")) {
                           if (service.isServiceEnabled()) {
                               //disable the rule
                               disableRule(ruleInstance,false);
                               _logger.log(Level.INFO,"smgt.disabledrule",
                                           new Object[]{name,description});
                            } else {
                               //with validator, may not need this 
                               _logger.log(Level.WARNING,"sgmt.error_disablerule_noservice",
                                           new Object[]{name,description});
                            }
                       } else if (!ruleInstance.isEnabled() && newValue.equals("true")) {
                           //enabled the rule
                           if (service.isServiceEnabled()) {
                               service.addRule(rule,ctxToUse);
                               _logger.log(Level.INFO,"smgt.enabledrule", 
                                           new Object[]{name,description});
                           } else {
                               //with validator, may not need this 
                               _logger.log(Level.WARNING,"sgmt.error_enablerule_noservice",
                                          new Object[]{name,description});
                           }
                       } else {
                          _logger.log(Level.INFO,"smgt.cannot_changesstate",
                                      new Object[]{name,description});
                       }
                   }
               }
           }
       }
   }

   private Rule getRuleInstance(String ruleName) {
       Rule rule = null; 
       if (activeRules.containsKey(ruleName)) {
           rule = activeRules.get(ruleName);
       } else if (disabledRules.containsKey(ruleName)) {
           rule = disabledRules.get(ruleName);
       } else if (inActiveRules.containsKey(ruleName)) {
           rule = inActiveRules.get(ruleName);
       } else if (noActionRules.containsKey(ruleName)) {
           rule = noActionRules.get(ruleName);
       }

       return rule;
   }

   private Map getRuleMap(String ruleName) {
       Map ruleMap = null;
  
       if (activeRules.containsKey(ruleName)) {
           ruleMap = activeRules;
       } else if (disabledRules.containsKey(ruleName)) {
           ruleMap = disabledRules;
       } else if (inActiveRules.containsKey(ruleName)) {
           ruleMap = inActiveRules;
       } else if (noActionRules.containsKey(ruleName)) {
           ruleMap = noActionRules;
       }

       return ruleMap;
   }

   private void disableRule(Rule ruleInstance, boolean disableService) throws Exception {
       /*
        * The disableService param determines whether the call is due
        * to service being disabled and thus the rule would get implicitly
        * /internally disabled or due to an explict call to disable the 
        * configured rule.
        */
       String ruleName = ruleInstance.getName();
       Event ruleEvent = ruleInstance.getEvent();
     
 
       try {
           if (ruleInstance.getState() == Rule.ENABLED) {
               //remove the event action binding
               NotificationFilter filter = ruleEvent.getNotificationFilter();
               ObjectName eventObjName = ruleEvent.getObjectName();
               ObjectName actionObjName = (ObjectName)ruleInstance.getAction();

               instanceMbs.removeNotificationListener(eventObjName,actionObjName,
                                                      filter, null);
               ruleInstance.setAction(null);
               if (!disableService) { 
                   activeRules.remove(ruleName);
               }
           } else if (ruleInstance.getState() == Rule.INACTIVE) {
               //remove event binding, default action (when supported)   
               ruleInstance.setAction(null);
               if (!disableService) { 
                   inActiveRules.remove(ruleName);
               }
           } else if (ruleInstance.getState() == Rule.NOACTION) {
               ruleInstance.setAction(null);
               if (!disableService) { 
                   noActionRules.remove(ruleName);
               }
           } 

           //notificationfiltersupport?
           ruleEvent.destroy();
           ruleInstance.setEvent(null);
           if (!disableService) {
               //call is for explict rule disable
               ruleInstance.setEnabled(false);
               ruleInstance.setState(Rule.DISABLED);
               disabledRules.put(ruleName,ruleInstance);    
               
           }
       } catch (Exception ex) {
           _logger.log(Level.WARNING,"sgmt.error_disablerule", 
                       new Object[]{ruleName,ex});
           throw ex;
       }               

   }

  /**
   * Handles disabling the service. Would internally disable all
   * enabled rules.
   */
  synchronized void disableService() throws Exception {
      int size = activeRules.size() + inActiveRules.size() + noActionRules.size(); 
      Map<String,Rule> completeMap = (Map<String,Rule>) new HashMap(size);
      completeMap.putAll(activeRules);
      completeMap.putAll(inActiveRules);
      completeMap.putAll(noActionRules);

      Set<String> rules = completeMap.keySet();
      for (String name : rules) {
          Rule ruleInstance = completeMap.get(name);
          disableRule(ruleInstance,true);
      }
  }

  /**
   * Handles addition of action
   *
   * @param ruleName : Identifies the rule to which action is being added
   * @param actionName : Identifies the action MBean name
   */ 
  void addAction(String ruleName, String actionName, ConfigContext ctxToUse) {
       Rule ruleInstance = getRuleInstance(ruleName);
       if (ruleInstance!=null) {
           synchronized(ruleInstance) {
               bindAction(ruleInstance,actionName,ctxToUse);
           }
       }
   }

  private void bindAction(Rule ruleInstance, String actionMBeanName,
                          ConfigContext ctxToUse) {
      String ruleName = ruleInstance.getName();
      Event ruleEvent = ruleInstance.getEvent();
      ObjectName actionObjName = null;
      Object alertAction = null;	  

      try {
          Domain domain = ServerBeansFactory.getDomainBean(ctxToUse);
          ApplicationRef appRef = verifyActionMBean(actionMBeanName, domain, ctxToUse);


          if (appRef != null) {
              Applications apps = domain.getApplications();
              Mbean definedMBean = apps.getMbeanByName(actionMBeanName);


              if (appRef.isEnabled() && definedMBean.isEnabled() ) {
                  if ((definedMBean.getObjectType()).equals(USER_DEFINED_TYPE)) {
                      actionObjName = registerAction(ruleEvent,actionMBeanName,
                                                     definedMBean,false,null);
                  } else {
                      // system defined, need to explictly load
                      actionObjName = registerAction(ruleEvent,actionMBeanName,
                                                     definedMBean,true,null);
                  }
                  //update the binding
                  ruleInstance.setAction((Object)actionObjName);
		  //Added to set up the rule binding
		  setupRuleBinding(Rule.ENABLED, ruleInstance, ruleEvent,
                                      (Object)actionObjName);
                  _logger.log(Level.INFO,"smgt.successaddaction", 
                               new Object[]{ruleName,actionMBeanName});
              } else {
                  /* action is disabled, but rule is active; Add a default mail
                   * alert
                   */
                  //alertAction = registerAlertAction(ruleEvent, actionMBeanName,
                  //                                   ruleName, null);
                  _logger.log(Level.WARNING,"sgmt.ruleactiondisabled",
                              new Object[]{ruleName,actionMBeanName});
                  //update the binding
                  ruleInstance.setState(Rule.INACTIVE);
				  //Added to set up the rule binding 
		  setupRuleBinding(Rule.INACTIVE, ruleInstance, ruleEvent,
                                         alertAction);
              }
           } else {
               //log error, invalid action MBean provided for this rule
               _logger.log(Level.SEVERE, "smgt.invalid_action", new Object[]{
                           ruleName, actionMBeanName});
               //config error
           }
        } catch (ConfigException ex) {
            _logger.log(Level.INFO, "config_error", ex);
        } catch (InstanceNotFoundException ex) {
            //log it,
            _logger.log(Level.INFO, "smgt.error_reg_action", new Object[]{
                        ruleName, actionMBeanName});
            //registerAlertAction(ruleEvent, actionMBeanName, ruleName, null);
        } catch (MalformedObjectNameException ex) {
            _logger.log(Level.WARNING, "sgmt.error_reg_action", new Object[]{
                        ruleName, actionMBeanName});
        } catch (RuntimeOperationsException ex) {
            //log it, actionMBean is not a listener type, add to inactive rules
            _logger.log(Level.WARNING, "smgt.error_reg_action", new Object[]{
                        ruleName,actionMBeanName});
            //registerAlertAction(ruleEvent, actionMBeanName, ruleName, null);
        } catch (CustomMBeanException cmbe) {
            /**  
	     * log it, as thrown from customRegistration, add to inactive rules
	     */
	    _logger.log(Level.WARNING, "smgt.error_reg_action", new Object[] {
			ruleName, actionMBeanName});
	    // registerAlertAction(ruleEvent, actionMBeanName, ruleName, handback);
        } catch (RuntimeException ex) {
            /*
             *log it, as thrown from customRegistration, add to inactive rules
             */
            _logger.log(Level.WARNING, "smgt.error_reg_action", new Object[] {
                        ruleName, actionMBeanName});
           //registerAlertAction(ruleEvent, actionMBeanName, ruleName, null);
       }
   }

    public void deleteAction(String name) {
        Event ruleEvent = null;
        Rule ruleInstance = null;
        try {
            ruleInstance = getRuleInstance(name);
            ruleEvent = ruleInstance.getEvent();
            NotificationFilter filter = ruleEvent.getNotificationFilter();
            ObjectName eventObjName = ruleEvent.getObjectName();
            ObjectName actionObjName = (ObjectName)ruleInstance.getAction();
            instanceMbs.removeNotificationListener(eventObjName,actionObjName,
                                                   filter, null);
            setupRuleBinding(Rule.NOACTION, ruleInstance, ruleEvent, null);
        } catch (Exception ex) {
            _logger.log(Level.WARNING, "sgmt.error_delete_action",
                new Object[]{name,ex.toString()});
        }

        //ensure gc of contained event
        ruleInstance = null;
    }

}
