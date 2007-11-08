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

import java.util.Locale;
import java.util.Map;

import com.sun.enterprise.config.serverbeans.validation.GenericValidator;
import com.sun.enterprise.config.serverbeans.validation.ValidationDescriptor;
import com.sun.enterprise.config.serverbeans.validation.ValidationContext;
import com.sun.enterprise.config.serverbeans.validation.PropertyHelper;
import com.sun.enterprise.config.serverbeans.validation.Result;

import com.sun.enterprise.config.serverbeans.JdbcConnectionPool;
import com.sun.enterprise.config.serverbeans.ElementProperty;

import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;


import java.util.logging.Level;

/**
    Custom Test for Jdbc Connection Pool Test which calls the Generic Validation before performing custom tests

    @author Srinivas Krishnan
    @version 2.0
*/

public class JdbcConnectionPoolTest extends GenericValidator {
    
    static boolean checked = false;
    int maxPoolSize = 0;
    
    public JdbcConnectionPoolTest(ValidationDescriptor desc) {
        super(desc);
    } 
    
    public void validate(ValidationContext valCtx) {
        super.validate(valCtx); // Before doing custom validation do basic validation
        
        if(valCtx.isVALIDATE() || valCtx.isADD() || valCtx.isSET())
        {
            JdbcConnectionPool pool = (JdbcConnectionPool)valCtx.getTargetBean();
            if(pool.isIsConnectionValidationRequired() && pool.getConnectionValidationMethod().equals("table")) {
                if(pool.getValidationTableName() == null || pool.getValidationTableName().equals(""))
                    reportValidationError(valCtx, "requiredTableName", 
                    "Table Name is required Jdbc Connection Pool if Connection validation method is Table",
                    null);
            }
            checkLazyConnectionProps(valCtx);
        }
    }
    
    //property changes reaction
    static final String LAZY_CONN_ASSOCIATION = "LazyConnectionAssociation";
    static final String LAZY_CONN_ENLISTMENT = "LazyConnectionEnlistment";
    
    public void validatePropertyChanges(ValidationContext propValCtx) 
    {
        
        if(propValCtx.isDELETE() || propValCtx.isVALIDATE()) //no validation
            return;
        
        if(!PropertyHelper.isPropertyChanged(propValCtx, LAZY_CONN_ASSOCIATION) &&
           !PropertyHelper.isPropertyChanged(propValCtx, LAZY_CONN_ENLISTMENT))
            return;
        
        //here we are only if some changes with testing props occured
        checkLazyConnectionProps(propValCtx);
    }
    
    private void checkLazyConnectionProps(ValidationContext valCtx)
    {
        Map map;
        ConfigBean targetBean = valCtx.getTargetBean();
        if(targetBean==null)
            return;
        if(targetBean instanceof ElementProperty)
            map = PropertyHelper.getFuturePropertiesMap(valCtx);
        else
            map = PropertyHelper.getPropertiesMap(targetBean);
        String newAsso = (String)map.get(LAZY_CONN_ASSOCIATION);
        String newEnlist = (String)map.get(LAZY_CONN_ENLISTMENT);
        if(newAsso!=null &&  newEnlist!=null &&
           Boolean.parseBoolean(newAsso) && !Boolean.parseBoolean(newEnlist))
        {
            reportValidationError(valCtx, "PropsConflict", 
                "Combination of properties {0}={1} and {2}={3} is not allowed.",
                new Object[]{LAZY_CONN_ASSOCIATION, newAsso,
                             LAZY_CONN_ENLISTMENT, newEnlist});
        }
    }
}
