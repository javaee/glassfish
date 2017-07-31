/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
 * CredentialInterface.java
 *
 * Created on October 3, 2000, 2:42 PM
 */

package com.sun.enterprise.tools.verifier.tests.connector;

import java.util.*;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.deployment.AuthMechanism;

/**
 * All Credential interface declared in the DD should be one of the 
 * allowed interface
 *
 * @author  Jerome Dochez
 * @version 
 */
public class CredentialInterface extends ConnectorTest implements ConnectorCheck {


    private static String[] allowedInterfaces = new String[] {
                              "javax.resource.spi.security.PasswordCredential",
                              "org.ietf.jgss.GSSCredential",
                              "javax.resource.spi.security.GenericCredential" };

    /** <p>
     * All Credential interface declared in the DD should be one of the 
     * allowed interface
     * </p>
     *
     * @paramm descriptor deployment descriptor for the rar file
     * @return result object containing the result of the individual test
     * performed
     */
    public Result check(ConnectorDescriptor descriptor) {
        boolean oneFailed = false;
        Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        if(!descriptor.getOutBoundDefined())
        {
          result.addNaDetails(smh.getLocalString
              ("tests.componentNameConstructor",
               "For [ {0} ]",
               new Object[] {compName.toString()}));
          result.notApplicable(smh.getLocalString
              ("com.sun.enterprise.tools.verifier.tests.connector.managed.notApplicableForInboundRA",
               "Resource Adapter does not provide outbound communication"));
          return result;
        }
        Set mechanisms = 
        descriptor.getOutboundResourceAdapter().getAuthMechanisms();
        if (mechanisms.isEmpty()) {
            // passed
	    result.addGoodDetails(smh.getLocalString
				  ("tests.componentNameConstructor",
				   "For [ {0} ]",
				   new Object[] {compName.toString()}));	
	    result.passed(smh.getLocalString
    	        ("com.sun.enterprise.tools.verifier.tests.connector.AuthMechType.nonexist",
                 "No authentication mechanism defined for this resource adapater"));
            return result;
        }
        Iterator mechIterator = mechanisms.iterator();
        while (mechIterator.hasNext()) {
            AuthMechanism am = (AuthMechanism) mechIterator.next();
            String credInterface = am.getCredentialInterface();
            boolean allowedInterface = false;            
            if (credInterface!=null) { 
                for (int i=0;i<allowedInterfaces.length;i++) {
                    if (credInterface.equals(allowedInterfaces[i])) {
                        allowedInterface = true;
                        break;
                    }
                }
            }
            if (!allowedInterface || credInterface == null) {
                // failed
                oneFailed = true;
        	result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.failed(smh.getLocalString
    	            ("com.sun.enterprise.tools.verifier.tests.connector.CredentialInterface.failed",
                    "Authentication mechanism credential interface [ {0} ] defined in the credential-interface tag is not allowed",
                    new Object[] {credInterface}));
            }
        }
        if (!oneFailed) {
	    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));	
		result.passed(smh.getLocalString
    	        ("com.sun.enterprise.tools.verifier.tests.connector.CredentialInterface.passed",
                 "All defined authorization mechanism credential interfaces are allowed"));
        }
        return result;        
    }
}
