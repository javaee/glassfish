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

package com.sun.enterprise.tools.verifier.tests.connector.ias;

import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.tools.common.dd.connector.ResourceAdapter;
import com.sun.enterprise.tools.common.dd.connector.SunConnector;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.tools.verifier.tests.connector.*;


public class ASConnectorMaxPool extends ConnectorTest implements ConnectorCheck {
 public Result check(ConnectorDescriptor descriptor)
 {
     Result result = getInitializedResult();
     ComponentNameConstructor compName = new ComponentNameConstructor(descriptor);   
	 boolean oneFailed = false;
     SunConnector sc = descriptor.getSunDescriptor();
     if(sc == null)
     {
            result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable1",
			   "NOT APPLICABLE [AS­CONNECTOR]: sun-ra.xml descriptor object could not be obtained"));
							  
     }
     else{
     ResourceAdapter ra = sc.getResourceAdapter();
     String maxPoolSize = ra.getAttributeValue("max-pool-size");
     if(maxPoolSize.length()==0)
     {
         result.failed(smh.getLocalString(getClass().getName()+".failed1",
                    "FAILED [AS-CONNECTOR resource-adapter] : max-pool-size cannot be empty"));
     }
     else
     {
        try
        {
            int value = Integer.valueOf(maxPoolSize).intValue();
            if(value < 0  || value > Integer.MAX_VALUE)
            {
                result.failed(smh.getLocalString(getClass().getName()+".failed2",
                    "FAILED [AS-CONNECTOR resource-adapter] : max-pool-size cannot be {0}. It should be between 0 and {1}",
                new Object[]{new Integer(value),new Integer(Integer.MAX_VALUE)}));
            }
            else
            {
                result.passed(smh.getLocalString(getClass().getName()+".passed",
                   "PASSED [AS-CONNECTOR resource-adapter] : max-pool-size is {0}",
                         new Object[]{new Integer(value)}));
            }
        }
        catch(NumberFormatException nfex)
        {
            Verifier.debug(nfex);
            result.failed(smh.getLocalString(getClass().getName()+".failed3",
            "FAILED [AS-CONNECTOR resource-adapter] : The value {0} for max-pool-size is not a valid Integer number",new Object[]{maxPoolSize}));
        }
     }
     }
        return result;
 }
 }
