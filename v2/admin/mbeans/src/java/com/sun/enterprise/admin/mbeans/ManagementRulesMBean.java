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
 * $Id: ManagementRulesMBean.java,v 1.4 2005/12/25 03:42:24 tcfujii Exp $
 */

package com.sun.enterprise.admin.mbeans;

//jdk imports
import java.util.Properties;
import java.util.Enumeration;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

//JMX imports
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MalformedObjectNameException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;


//config imports
import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.admin.config.ConfigMBeanHelper;

//core imports
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.ManagementRules;
import com.sun.enterprise.config.serverbeans.ManagementRule;
import com.sun.enterprise.config.serverbeans.Event;
import com.sun.enterprise.config.serverbeans.Action;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.admin.selfmanagement.event.ManagementRulesMBeanHelper;


public class ManagementRulesMBean extends BaseConfigMBean
{
    
    /**
     * Create new self management rule with contained event and action(optional)
     * @param ruleName          name of the management rule. Required.
     * @param bRuleEnabled      enabled/disabled rule state.  
     * @param ruleDescription   textual decription of the rule.
     * @param eventType         one of the predefined event types. Required. 
     * @param eventLevel        events severety level (default "INFO")
     * @param eventDescription  textual decription of the evant
     * @param eventRecordEvent  whether the event is to be logged or not ("true")
     * @param eventProperties   event's properties
     * @param actionMbeanName   actionMbeanNames associated with rule 
     *
     * @returns ObjectName of created management-rule
     * @throws Exception
     */
public ObjectName createManagementRule(
            String  ruleName,           //required
            Boolean bRuleEnabled,
            String  ruleDescription,
            String  eventType,          //required
            String  eventLevel,         
            Boolean eventRecordEvent,   
            String  eventDescription,   
            Properties eventProperties,
            String  actionMbeanName
        )  throws Exception
{
    ManagementRule  newRule = new ManagementRule();
    //Rule attrs
    newRule.setName(ruleName);
    if(ruleDescription!=null)
       newRule.setDescription(ruleDescription);
    if(bRuleEnabled!=null)
       newRule.setEnabled(bRuleEnabled.booleanValue());
    //Event
    Event event = new Event();
    event.setType(eventType);
    if(eventRecordEvent!=null)
       event.setRecordEvent(eventRecordEvent.booleanValue());
    if(eventLevel!=null)
       event.setLevel(eventLevel);
    if(eventDescription!=null)
       event.setDescription(eventDescription);
    //Event properties
    if (null != eventProperties)
        {
            Enumeration keys = eventProperties.keys();
            while (keys.hasMoreElements())
            {
                final String key = (String)keys.nextElement();
                ElementProperty prop = new ElementProperty();
                prop.setName(key);
                prop.setValue((String)eventProperties.get(key));
                event.addElementProperty(prop);
            }
        }
    newRule.setEvent(event);
    
    //Action
    if(actionMbeanName!=null)
    {
        Action action = new Action();
        action.setActionMbeanName(actionMbeanName);
        newRule.setAction(action);
        
    }
    // insert new rule to config tree
    ManagementRules rules = (ManagementRules)getBaseConfigBean();
    rules.addManagementRule(newRule);
    return ConfigMBeanHelper.getChildObjectName(super.m_registry, super.info, newRule);
}

    /**
     * Add Action element to maagement rule
     * @param ruleName          name of the management rule
     * @param actionMbeanNames  actionMbeanNames associated with rule 
     *
     * @throws ConfigException
     */
public void addActionToManagementRule(
            String  ruleName,           //required
            String  actionMbeanName)  throws ConfigException
{
    ManagementRules rules = (ManagementRules)getBaseConfigBean();
    ManagementRule  rule = rules.getManagementRuleByName(ruleName);
    Action action = new Action();
    action.setActionMbeanName(actionMbeanName);
    rule.setAction(action);
}


    /**
     * Gets the registred actions in the domain.
     * @param enabled          if true, gets only enabled actions, otherwise all actions.
     *
     * @returns registered actions
     * @throws ConfigException
     */
public List<String> getAllActionMBeans(boolean enabled) throws ConfigException {
    return ManagementRulesMBeanHelper.getAllActionMBeans(enabled);
}

    /**
     * Gets the list of event types.
     * @param isEE  if true, gets events for EE,  otherwise gets events for PE.
     *
     * @returns list of event types
     */
public List<String> getEventTypes(boolean isEE) {
    return ManagementRulesMBeanHelper.getEventTypes(isEE);
}

    /**
     * Gets the associated propertied for a given event.
     * @param eventType  for a given event type, gets the associated property names.
     *
     * @returns list of property names associated with an event type
     */
public List<String> getEventProperties(String eventType) {
    return ManagementRulesMBeanHelper.getEventProperties(eventType);
}


    /**
     * Gets the possible values for a given event type and property name.
     * @param eventType  event type to which the propertyName belongs.
     * @param propertyName name of the property
     *
     * @returns list of property values associated with a property name.
     */
public List<String> getEventPropertyValues(String eventType, String propertyName)
                    throws ConfigException {
    return ManagementRulesMBeanHelper.getEventPropertyValues(eventType, propertyName);
}

    /**
     * Gets the registred custom mbeans which can be notification emitters.
     * @param enabled   if true, gets only enabled emitters, otherwise all emitters.
     *
     * @returns custom mbeans which are notification emitters
     * @throws ConfigException
     */
List<String> getAllNotificationEmitterMbeans(boolean enabled) throws ConfigException {
    return ManagementRulesMBeanHelper.getAllNotificationEmitterMbeans(enabled);
}


    /**
     * Gets the registred MBeans registered in the server's MBean server.
     * @param filter  ObjectName filter for quering MBean server.
     *
     * @returns list of registered mbeans 
     * @throws MalformedObjectNameException 
     */
public Set<ObjectName> getRegisteredMBeans(String filter) throws 
                                  MalformedObjectNameException {
    return ManagementRulesMBeanHelper.getRegisteredMBeans(filter);
}

    /**
     * Gets the attributes for a given ObjectName.
     * @param objName  ObjectName for which the attributes are required.
     *
     * @returns list of attributes 
     * @throws InstanceNotFoundException,IntrospectionException, ReflectionException
     */
public List<String> getAttributes(ObjectName objName) throws 
                              InstanceNotFoundException, 
                              IntrospectionException,
                              ReflectionException {
    return ManagementRulesMBeanHelper.getAttributes(objName);
}

    /**
     * Gets the attributes for a given ObjectName in a string form.
     * @param objNameStr  ObjectName for which the attributes are required.
     *
     * @returns list of attributes 
     * @throws InstanceNotFoundException,IntrospectionException, ReflectionException
     * @throws MalformedObjectNameException
     */
public List<String> getMBeanAttributes(String objectNameStr) 
                         throws MalformedObjectNameException,
                         InstanceNotFoundException, IntrospectionException,
                         ReflectionException {
    return ManagementRulesMBeanHelper.getMBeanAttributes(objectNameStr);
}

    /**
     * Gets the Notifications for a given ObjectName.
     * @param objName  ObjectName for which the Notifications are required.
     *
     * @returns list of Notifications 
     * @throws InstanceNotFoundException,IntrospectionException, ReflectionException
     */
public List<String> getNotificationTypes(ObjectName objName) throws 
                                          InstanceNotFoundException, 
                                          IntrospectionException,
                                          ReflectionException {
    return ManagementRulesMBeanHelper.getNotificationTypes(objName);
}

    /**
     * Gets the notifications for a given ObjectName in a string form.
     * @param objectNameStr  objectNameStr for which the notifications are required.
     *
     * @returns list of notifications 
     * @throws InstanceNotFoundException,IntrospectionException, ReflectionException
     * @throws MalformedObjectNameException
     */
public List<String> getNotificationTypes(String objectNameStr) throws 
                     MalformedObjectNameException,
                     InstanceNotFoundException, IntrospectionException,
                     ReflectionException {
    return ManagementRulesMBeanHelper.getNotificationTypes(objectNameStr);
}


public List<String> getAttributes(String dottedName) { return null; }

public List<String> getDottedNames(String dottedName) { return null; }


}

