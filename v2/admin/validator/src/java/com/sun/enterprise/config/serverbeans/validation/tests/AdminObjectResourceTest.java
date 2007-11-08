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

import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.AdminObjectResource;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.validation.GenericValidator;
import com.sun.enterprise.config.serverbeans.validation.Result;
import com.sun.enterprise.config.serverbeans.validation.ValidationDescriptor;
import com.sun.enterprise.connectors.ConnectorRuntime;
import java.util.Set;

/**
    Custom Test for Admin Object Resource Test which calls the Generic Validation before performing custom tests

    @author Srinivas Krishnan
    @version 2.0
*/

public class AdminObjectResourceTest extends GenericValidator {
    
    static boolean checked = false;
    
    public AdminObjectResourceTest(ValidationDescriptor desc) {
        super(desc);
    } 
    
    public Result validate(ConfigContextEvent cce) {
        Result result = super.validate(cce); // Before doing custom validation do basic validation
        
        if(cce.getChoice().equals(StaticTest.UPDATE)) {
            try {
                ConfigContext context = cce.getConfigContext();
                String name = cce.getName();
                String value = (String) cce.getObject();
                AdminObjectResource admin = (AdminObjectResource)cce.getClassObject();
                validateAttribute(name, value,  admin, result);
            } catch(Exception e) {
                _logger.log(Level.FINE, "domainxmlverifier.exception", e);
            }
        }
        
        if(cce.getChoice().equals(StaticTest.ADD) || cce.getChoice().equals(StaticTest.VALIDATE)) {
            try {
                ConfigContext context = cce.getConfigContext();
                Object value = cce.getObject();
                AdminObjectResource admin = (AdminObjectResource)value;
                validateAttribute(ServerTags.RES_TYPE, admin.getResType(),  admin, result);
            } catch(Exception e) {
                _logger.log(Level.FINE, "domainxmlverifier.exception", e);
            }
        }
        return result;
    }
    
    public void validateAttribute(String name, String value, ConfigBean admin, Result result) {
       
/* 
        if(name.equals(ServerTags.RES_TYPE)) {
            String restype = value;

            String resAdapter = ((AdminObjectResource)admin).getResAdapter();
            String resTypes[] = null;

            try {
                resTypes = ConnectorRuntime.getRuntime().getAdminObjectInterfaceNames(resAdapter);
            } catch(Exception e) {
                       _logger.log(Level.FINE, "domainxmlverifier.exception", e);
            }
            boolean available = false;
            if(resTypes != null) {
                for(int i=0;i<resTypes.length;i++) {
                    if(restype.equals(resTypes[i])) {
                        available = true;
                        break;
                    }
                }
            }
            if(!available)
                    result.failed(smh.getLocalString(getClass().getName() + ".resAdapterNotAvl", 
                        "Attribute(restype={0}) : Invalid Resource Type ", new Object[]{restype}));
        }
*/
    }

}
