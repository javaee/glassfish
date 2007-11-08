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

import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.JmsHost;
import com.sun.enterprise.config.serverbeans.JmsService;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.validation.GenericValidator;
import com.sun.enterprise.config.serverbeans.validation.Result;
import com.sun.enterprise.config.serverbeans.validation.ValidationDescriptor;

/**
   Custom Test for JMS Host Test which calls the Generic Validation before performing custom tests

   @author Sreenivas Munnangi
   @version 1.0
*/

public class JmsHostTest extends GenericValidator {
    
    private static final char [] illegalChars = {'*', ',', ':'};

    public JmsHostTest (ValidationDescriptor desc) {
        super(desc);
    }

    public Result validate(ConfigContextEvent cce) {

        Result result = super.validate(cce);
        try {
            if (cce.getChoice().equals(StaticTest.DELETE)){
                if (isReferencedByParent(cce)){
                    result.failed(
                        smh.getLocalString(
                            getClass().getName() + ".cannotDeleteReferencedJmsHost",
                            "Jms host (name={0}) can not be removed. It is referenced by its parent jms-service",
                            new Object[]{getJmsHost(cce).getName()}));
                }
            } else {
                if (cce.getName().equals(ServerTags.ADMIN_USER_NAME)) {
                    validateAdminUserName((String) cce.getObject(), result);
                } else if (cce.getName().equals(ServerTags.ADMIN_PASSWORD)) {
                    validateAdminUserPassword((String) cce.getObject(), result);
                } 
            }
        } catch(Exception e) {
            _logger.log(Level.FINE, "domainxmlverifier.exception", e);
        }
        return result;
    }

    private final boolean isReferencedByParent(final ConfigContextEvent cce) throws ConfigException {
        final JmsHost host = getJmsHost(cce);
        final JmsService parent = (JmsService) host.parent();
        return parent.getDefaultJmsHost() != null && parent.getDefaultJmsHost().equals(host.getName());
    }

    private void validateAdminUserName(String name, Result result) {

        if ((name == null) || (name.equals("")) || (name.length() < 1)) {
            result.failed(smh.getLocalString(getClass().getName() +
                                             ".blankOrNullString",
                                             "Attribute {0} : cannot be null or blank",
                                             new Object[] {ServerTags.ADMIN_USER_NAME}));
            return;
        }

        if (hasIllegalChar(name.toCharArray(), illegalChars)) {
            result.failed(smh.getLocalString(getClass().getName() +
                                             ".stringHasIllegalChars", 
                                             "Attribute {0} cannot have any of illegal characters {1}",
                                             new Object[] {ServerTags.ADMIN_USER_NAME, new String(illegalChars)}));
        }
    }

    private void validateAdminUserPassword(String name, Result result) {

        if ((name == null) || (name.equals("")) || (name.length() < 1)) {
            result.failed(smh.getLocalString(getClass().getName() +
                                             ".blankOrNullString",
                                             "Attribute {0} : cannot be null or blank",
                                             new Object[] {ServerTags.ADMIN_PASSWORD}));
            return;
        }

        if (hasIllegalChar(name.toCharArray(), illegalChars)) {
            result.failed(smh.getLocalString(getClass().getName() +
                                             ".stringHasIllegalChars", 
                                             "Attribute {0} cannot have any of illegal characters {1}",
                                             new Object[] {ServerTags.ADMIN_PASSWORD, new String(illegalChars)}));
        }
    }

    private boolean hasIllegalChar (char[] inputCharArr, char[] illegalCharArr) {

        int inputCharArrLen = inputCharArr.length;
        int illegalCharArrLen = illegalCharArr.length;

        for (int i=0; i<illegalCharArrLen; i++) {
            for (int j=0; j<inputCharArrLen; j++) {
                if (inputCharArr[j] == illegalCharArr[i]) {
                    return true;
                }
            }
        }

        return false;
    }

    private final JmsHost getJmsHost(final ConfigContextEvent cce) throws ConfigException{
        return (JmsHost) cce.getValidationTarget();
    }
    
}
