package com.sun.enterprise.tools.verifier.tests.ejb.ias;

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

import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import java.util.*;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;

import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;

import com.sun.enterprise.tools.common.dd.ejb.*;
import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;

/** ejb [0,n]
 *    jndi-name ? [String]
 *
 * The jndi-name of an ejb is valid for MDBs.
 * The jndi-name should not be an empty string.
 * @author Irfan Ahmed
 */
public class ASEjbJndiName extends EjbTest implements EjbCheck { 

    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = new ComponentNameConstructor(descriptor);

        SunEjbJar ejbJar = descriptor.getEjbBundleDescriptor().getIasEjbObject();
        String ejbName = null, jndiName=null;
        boolean oneFailed = false;
        boolean oneWarning = false;
        if(ejbJar!=null)
        {
            Ejb ejbs[] = ejbJar.getEnterpriseBeans().getEjb();
            Ejb testCase = null;
            for(int i=0;i<ejbs.length;i++)
            {
                if(ejbs[i].getEjbName().equals(descriptor.getName()))
                {
                    testCase = ejbs[i];
                    break;
                }
            }
            
            ejbName = testCase.getEjbName();
            jndiName = testCase.getJndiName();
            if(jndiName != null)
            {
                if(jndiName.trim().length()==0)
                {
                    oneFailed = true;
                    result.failed(smh.getLocalString
                         (getClass().getName() + ".failed",
                          "FAILED [AS-EJB ejb] : jndi-name cannot be an empty value.",
                          new Object[] {ejbName}));
                }
                else
                {
                    if(descriptor instanceof EjbMessageBeanDescriptor)
                    {
                        if(jndiName.startsWith("jms/"))
                        {
                            result.passed(smh.getLocalString(getClass().getName()+".passed",
                                "PASSED [AS-EJB ejb] jndi-name is {0}", new Object[]{jndiName}));
                        }
                        else
                        {
                            oneWarning = true;
                            result.warning(smh.getLocalString(getClass().getName() + ".warning1",
                                "WARNING [AS-EJB ejb] jndi-name for an MDB should start with jms/"));
                        }
                    }
                    else
                    {
                        result.passed(smh.getLocalString(getClass().getName()+".passed",
                            "PASSED [AS-EJB ejb] : jndi-name is {0}", new Object[]{jndiName}));
                    }
                }
            }
            else
            {
                oneWarning = true;
                result.warning(smh.getLocalString(getClass().getName() + ".warning",
                    "WARNING [AS-EJB ejb] : jndi-name should be defined for the EJB {0}",
                    new Object[]{descriptor.getName()})); 
            }
        }
        else
        {
            result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun",
                  "NOT RUN [AS-EJB] : Could not create an SunEjbJar object"));
        }
        if(oneFailed)
            result.setStatus(Result.FAILED);
        else if(oneWarning)
            result.setStatus(Result.WARNING);
        return result;
    }
}
