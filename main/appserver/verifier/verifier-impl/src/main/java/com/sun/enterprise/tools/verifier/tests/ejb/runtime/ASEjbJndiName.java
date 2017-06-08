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

package com.sun.enterprise.tools.verifier.tests.ejb.runtime;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbMessageBeanDescriptor;


/** ejb [0,n]
 *    jndi-name ? [String]
 *
 * The jndi-name of an ejb is valid for MDBs.
 * The jndi-name should not be an empty string.
 * @author Irfan Ahmed
 */
public class ASEjbJndiName extends EjbTest implements EjbCheck {

    boolean oneFailed = false;
    boolean oneWarning = false;
    public Result check(EjbDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String ejbName = null, jndiName=null;
        ejbName = descriptor.getName();     //get ejb-name
        jndiName=getXPathValue("/sun-ejb-jar/enterprise-beans/ejb/jndi-name");
        if(jndiName != null){
            if(jndiName.trim().length()==0){
                check(result, descriptor, compName);
            }else{
                addGoodDetails(result, compName);
                result.passed(smh.getLocalString(getClass().getName()+".passed",
                        "PASSED [AS-EJB ejb] : jndi-name is {0}", new Object[]{jndiName}));
            }
        }else
            check(result, descriptor, compName);

        if(oneFailed)
            result.setStatus(Result.FAILED);
        else if(oneWarning)
            result.setStatus(Result.WARNING);
        return result;

    }

    public void check(Result result, EjbDescriptor descriptor, ComponentNameConstructor compName) {
        if(descriptor instanceof EjbMessageBeanDescriptor) {
            String mdbres = getXPathValue("sun-ejb-jar/enetrprise-beans/ejb/mdb-resource-adapter");
            if (mdbres != null) {
                addGoodDetails(result, compName);
                result.passed(smh.getLocalString(getClass().getName()+".passed1",
                        "mdb-resource-adapter is defined for the EJB {0}", new Object[]{mdbres}));
            }else{
                oneFailed=true;
                addErrorDetails(result, compName);
                result.addErrorDetails(smh.getLocalString(getClass().getName()+".failed",
                        "jndi-name or mdb-resource-adapter should be defined for an MDB"));
            }
        }else if(descriptor.isRemoteInterfacesSupported()) {
         /** Bug#5060283 -- It is possible to use this ejb by referencing thru' ejb-ref/ejb-link.
            * Only thing is, the accessibility of the ejb is reduced.
            * It is only accessible to other clients bundled within this ear file.
            * Hence, report a warning, instead of an error.
            */
//            oneFailed=true;
//            addErrorDetails(result, compName);
//            result.addErrorDetails(smh.getLocalString(getClass().getName()+".failed1",
//                    "jndi-name should be defined for a bean implementing a remote interface"));
            oneWarning = true;
            addWarningDetails(result, compName);
            result.warning(smh.getLocalString(getClass().getName() + ".warning",
                    "WARNING [AS-EJB ejb] : jndi-name is not defined for the EJB {0} although it has a remote interface.",
                    new Object[]{descriptor.getName()}));

        }else {
            result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable", "NOT APPLICABLE"));
        }

    }
}
