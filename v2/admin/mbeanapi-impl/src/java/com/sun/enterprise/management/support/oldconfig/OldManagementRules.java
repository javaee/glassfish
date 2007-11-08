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
package com.sun.enterprise.management.support.oldconfig;

import javax.management.ObjectName;
import javax.management.AttributeList;

import java.util.Set;
import java.util.List;
import java.util.Properties;

public interface OldManagementRules 
{
    /**
        Taken from com.sun.enterprise.admin.mbeans.ManagementRulesMBean
     
        Create new self management rule with contained event and action(optional)
        @param ruleName          name of the management rule. Required.
        @param bRuleEnabled      enabled/disabled rule state.  
        @param ruleDescription   textual decription of the rule.
        @param eventType         one of the predefined event types. Required. 
        @param eventLevel        events severety level (default "INFO")
        @param eventDescription  textual decription of the evant
        @param eventRecordEvent  whether the event is to be logged or not ("true")
        @param eventProperties   event's properties
        @param actionMbeanName   actionMbeanNames associated with rule 

        @returns ObjectName of created management-rule
        @throws Exception
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
        );


    ObjectName createManagementRule(AttributeList a);
    void addActionToManagementRule(String ruleName,String actionMBeanName);
    List getAllActionMBeans(boolean p);
    List getDottedNames(String p);
    List getEventProperties(String p);
    List getEventPropertyValues(String p1,String p2);
    List getEventTypes(boolean p);
    List getMBeanAttributes(String p);
    ObjectName[] getManagementRule();
    ObjectName getManagementRuleByName(String p);
    String[] getManagementRuleNamesList();
    List getNotificationTypes(String p);
    List getNotificationTypes(ObjectName p);
    Set getRegisteredMBeans(String p);
    void removeManagementRuleByName(String p);
    
};




