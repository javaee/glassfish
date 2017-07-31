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
 * TransactionSupport.java
 *
 * Created on September 20, 2000, 9:29 AM
 */

package com.sun.enterprise.tools.verifier.tests.connector;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;

/**
 * Verify that the Transaction Support for the ressource adapter is of an 
 * acceptable value
 *
 * @author  Jerome Dochez
 * @version 
 */
public class TransactionSupport extends ConnectorTest implements ConnectorCheck {

    private final String[] acceptableValues = {
        ConnectorTagNames.DD_NO_TRANSACTION,
        ConnectorTagNames.DD_LOCAL_TRANSACTION,
        ConnectorTagNames.DD_XA_TRANSACTION };
                    
    /** 
     * <p>
     * Verifier test implementation. Check for the transaction-support 
     * deployment field which should be one of the acceptable values :
     *          NoTransaction
     *          LocalTransaction
     *          XATransaction
     * </p>
     *
     * @param <code>ConnectorDescritor</code>The deployment descriptor for
     * the connector.
     * @return <code>Result</code> Code execution result
     */
    public Result check(ConnectorDescriptor descriptor) {
        
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
        String connectorTransactionSupport =
        descriptor.getOutboundResourceAdapter().getTransSupport();
        
        // No transaction support specified, this is an error
        if (connectorTransactionSupport==null) {
	    result.addErrorDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
	    result.failed(smh.getLocalString
			  (getClass().getName() + ".nonexist",
			   "Error: No Transaction support specified for ressource adapter",
			   new Object[] {connectorTransactionSupport}));        
            return result;
        }
        
        // let's loop over all acceptable values to check the declared one is valid
        for (int i=0;i<acceptableValues.length;i++) {
            if (connectorTransactionSupport.equals(acceptableValues[i])) {
                    
                // Test passed, we found an acceptable value
	       result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));	
		result.passed(smh.getLocalString
	            (getClass().getName() + ".passed",
                    "Transaction support [ {0} ] for ressource adapter is supported",
	            new Object[] {connectorTransactionSupport}));
               return result;
            }     
        }
        
        // If we end up here, we haven't found an acceptable transaction support
	result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	result.failed(smh.getLocalString
	       (getClass().getName() + ".failed",
                "Error: Deployment descriptor transaction-support [ {0} ] for ressource adapter is not valid",
		new Object[] {connectorTransactionSupport}));        
        return result;
    }
}
