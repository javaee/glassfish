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

package com.sun.enterprise.config.serverbeans.validation.tests;

import com.sun.enterprise.config.serverbeans.validation.GenericValidator;
import com.sun.enterprise.config.serverbeans.validation.ValidationDescriptor;
import com.sun.enterprise.config.serverbeans.validation.ValidationContext;
import com.sun.enterprise.config.serverbeans.validation.AttrType;
import com.sun.enterprise.config.serverbeans.validation.AttrString;
import com.sun.enterprise.config.serverbeans.validation.Result;

import com.sun.enterprise.config.serverbeans.J2eeApplication;

import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ManagementRule;
import com.sun.enterprise.config.serverbeans.Event;
import java.util.logging.Level;
import java.util.Arrays;
import java.util.List;

public class ManagementRuleTest extends GenericValidator {
    
    public ManagementRuleTest(ValidationDescriptor desc) {
        super(desc);
    } 
    
    final static List EVENT_TYPES=
         Arrays.asList("log","timer","trace","monitor","cluster", "lifecycle","notification");
    final static List LOG_LEVELS=
         Arrays.asList("FINEST","FINER","FINE","CONFIG","INFO","WARNING","SEVERE","OFF");
    
    public void validateElement(ValidationContext valCtx) 
    {
         super.validateElement(valCtx);
         
         // this is temporaty code which should be removed after
         // "deep" childs validation will be implemented for add/set operations
         if( (valCtx.isADD() || valCtx.isSET()) && 
             valCtx.value instanceof ManagementRule)
         {
             try {
                Event event = ((ManagementRule)valCtx.value).getEvent();
                //eventtypes
                if(!EVENT_TYPES.contains(event.getType()))
                    valCtx.result.failed(smh.getLocalString(getClass().getName(),
                            valCtx.smh.getLocalString(getClass().getName() + ".wrongEventType",
                "Value {0} is not allowed for Event type", new Object[] {event.getType()})));
                //log-levels
                if(!LOG_LEVELS.contains(event.getLevel()))
                    valCtx.result.failed(smh.getLocalString(getClass().getName(),
                            valCtx.smh.getLocalString(getClass().getName() + ".wrongEventLevel",
                "Value {0} is not allowed for Event log level", new Object[] {event.getLevel()})));
             } catch (Exception e) {}
             
         }
    }
    

}
