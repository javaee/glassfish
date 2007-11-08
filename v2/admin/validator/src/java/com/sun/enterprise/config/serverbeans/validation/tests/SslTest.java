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
import java.util.logging.Level;

import com.sun.enterprise.config.serverbeans.validation.GenericValidator;
import com.sun.enterprise.config.serverbeans.validation.ValidationDescriptor;
import com.sun.enterprise.config.serverbeans.validation.Result;
import com.sun.enterprise.config.serverbeans.Ssl;
import com.sun.enterprise.config.serverbeans.IiopListener;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.validation.tests.StaticTest;

import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigException;

/**
    Custom Test for Ssl Element which calls the Generic Validation before performing custom tests

    @author Srinivas Krishnan
    @version 2.0
*/

public class SslTest extends GenericValidator {
    
    public SslTest(ValidationDescriptor desc) {
        super(desc);
    } 
    
    public Result validate(final ConfigContextEvent cce) {
        _logger.log(Level.CONFIG, "SslTest validation");
        
        final Result result = super.validate(cce); // Before doing custom validation do basic validation
        
        final String choice = cce.getChoice();
        if(choice.equals(StaticTest.UPDATE)) {
            return performUpdateChecks(result, cce);
        } else if (choice.equals(StaticTest.SET)) {
            return performSetChecks(result,cce);
        }
        return result;
    }

    private Result performUpdateChecks(final Result result, final ConfigContextEvent cce){
        final ConfigBean co = (ConfigBean) cce.getClassObject();
        final String parentsDtdName = ((ConfigBean) co.parent()).dtdName();
        _logger.log(Level.FINEST, "SslTest update - parent's DTD name is \""+parentsDtdName+"\"");

        _logger.log(Level.FINEST, "SslTest update - attribute name is \""+cce.getName()+"\"");
        _logger.log(Level.FINEST, "SslTest update - attribute value is \""+cce.getObject()+"\"");

        if (parentsDtdName.equals(ServerTags.IIOP_LISTENER)
            && cce.getName().equals(ServerTags.SSL2_ENABLED)
            && ((String) cce.getObject()).equalsIgnoreCase("true")){
            _logger.log(Level.FINER, "SslTest update check - parent is an iiop-listener, and ssl2enabled attribute is being set to true");
            ssl2NotAllowed(result);
        }
        return result;
    }
    private  Result performSetChecks(final Result result, final ConfigContextEvent cce){
        _logger.log(Level.CONFIG, "SsltTest performing set check");
        _logger.log(Level.FINER, "SsltTest set - parent's class is \""+cce.getClassObject().getClass().getName()+"\"");
        _logger.log(Level.FINER, "SsltTest set - ssl objects ssl2enabled attribute is set: \""+((Ssl) cce.getObject()).isSsl2Enabled()+"\"");
        if (cce.getClassObject() instanceof IiopListener
            && ((Ssl) cce.getObject()).isSsl2Enabled()){
            _logger.log(Level.FINER, "SsltTest set check - parent is an iiop-listener, and ssl2enabled attribute is being set to true");
            ssl2NotAllowed(result);
        }
        return result;
    }

    private  void ssl2NotAllowed(final Result result){
        _logger.log(Level.CONFIG, "SslTest - an invalid attempt to enable ssl2 has been found. Returning an error");
        result.failed(smh.getLocalString(getClass().getName()+".ssl2NotAllowed",
                                         "ssl2 cannot be enabled for an iiop-listener"));
    }
    
}
