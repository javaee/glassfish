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

import java.util.logging.Level;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.serverbeans.validation.Result;
import com.sun.enterprise.config.serverbeans.validation.ValidationDescriptor;
import com.sun.enterprise.config.serverbeans.validation.GenericValidator;

/**
   Instances of this class represent applications taht can be
   deployed. They provide a deeper validation by supplying the ability
   to validate their object type attribute - updates and deletions are
   only allowed if this attribute has the value 'user'
 */
abstract class DeployableAppTest extends GenericValidator
{

    DeployableAppTest(final ValidationDescriptor desc){
        super(desc);
    }

    public Result validate(ConfigContextEvent cce) {
        Result result = super.validate(cce); // Before doing custom
                                             // validation do basic
                                             // validation
        try{
            if(cce.getChoice().equals(StaticTest.UPDATE) || cce.getChoice().equals(StaticTest.DELETE)) { 
            
                if(!getObjectType(cce).equals("user"))
                    result.failed(smh.getLocalString(getClass().getName()+".systemAppNotChangeable",
                                                     "System Application, Attribute Not Changeable"));
            }
        }
        catch (final ConfigException ce){
            _logger.log(Level.WARNING, "domainxmlverifier.exception", ce);
        }
        return result;
    }

        /**
           Return the object type given the config bean of the
           deployable app.
           @param app the application whose object type we want
           @return the value of the object-type attribute
        */
    protected abstract String getObjectType(final ConfigBean app);
    
        /**
           Return the object type from the receiver's class object.
         */
        // Implementor's note - this is only valid for operations
        // other than SET - if the operation is a SET operation then
        // the value object can be an array of config beans, and this
        // isn't implemented.
    private String getObjectType(final ConfigContextEvent cce) throws ConfigException{
        return getObjectType(getApp(cce));
    }

    private ConfigBean getApp(final ConfigContextEvent cce)  throws ConfigException{
        return (ConfigBean) cce.getValidationTarget();
    }

        
}


        
