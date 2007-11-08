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

import com.sun.enterprise.admin.selfmanagement.event.Event;

/**
 * Denotes the configured rule in the server instance. It abstracts all the
 * Rule relevent information in it.
 *
 * @author Pankaj Jairath
 */
class Rule {    
    /** One of the states of the rule */
    final static int ENABLED = 0;
    
    final static int INACTIVE = 1;
    
    final static int DISABLED = 2;
    
    final static int NOACTION = 3;
    
    private String name = null;
    
    private String description = null;
    
    private int state = -1;
    
    private Event event = null;
    
    private Object actionMBean = null;

    private boolean isEnabled = false;
    
    /** Creates a new instance of Rule */
    Rule(String ruleName, String ruleDescription) {
        name = ruleName;
        description  = ruleDescription;
    }

    /** Sets the name of the configured Rule */
    void setName(String ruleName) {
        name = ruleName;
    }
 
    
    /** Retrieves name of the configured rule */
    String getName() {
        return name;
    }
    
    /** Retrieves the description of the configured rule */
    String getDescription() {
        return description;
    }
    
    /** Sets the current state of the configured Rule */
    void setState(int ruleState) {
        state = ruleState;
    }       
    
    /** Retrieves the current state of the configured Rule */
    int getState() {
        return state;
    }
    
    /** Associates the event with the Rule */
    void setEvent(Event ruleEvent) {
        event = ruleEvent;
    }
    
    /** Retrieves the Event associated with the Rule */
    Event getEvent() {
        return event;
    }
    
    /** Associates the Action with the configured Rule */
    void setAction(Object actionInstance) {
        actionMBean = actionInstance;
    }
    
    /** Retrieves the Action associated with the Rule */
    Object getAction() {
        return actionMBean;
    }

    /** Set the enabled status */
    void setEnabled(boolean status) {
        isEnabled = status;
    }

    /** Provides the enabled status of the rule */
    boolean isEnabled() {
        return isEnabled;
    }
}
