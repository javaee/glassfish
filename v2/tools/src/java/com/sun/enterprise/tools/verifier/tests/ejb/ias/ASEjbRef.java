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

import com.sun.enterprise.tools.common.dd.ejb.SunEjbJar;
import com.sun.enterprise.tools.common.dd.ejb.Ejb;
import com.sun.enterprise.tools.common.dd.EjbRef;

import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.deployment.EjbEntityDescriptor;
/** ejb [0,n]
 *    ejb-ref [0,n]
 *        ejb-ref-name [String]
 *        jndi-name [String]
 *
 * The ejb-ref is root element that binds and ejb reference to a jndi-name.
 * The ejb-ref-name should have an entry in the ejb-jar.xml
 * The jdi-name should not be empty. It shoudl start with ejb/
 * @author Irfan Ahmed
 */
public class ASEjbRef extends EjbTest implements EjbCheck { 



    /**
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = new ComponentNameConstructor(descriptor);

        SunEjbJar ejbJar = descriptor.getEjbBundleDescriptor().getIasEjbObject();
        String ejbName = null, jndiName=null;
        boolean oneFailed = false;
        boolean notApplicable = false;
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
            EjbRef ejbRefs[] = testCase.getEjbRef();
            if(ejbRefs.length > 0)
            {
                for(int j=0;j<ejbRefs.length;j++)
                {
                    String refName = ejbRefs[j].getEjbRefName();
                    String refJndiName = ejbRefs[j].getJndiName();
                    
                    try
                    {
                        EjbReferenceDescriptor refDesc = descriptor.getEjbReferenceByName(refName);
                        String type = refDesc.getType();
                        if(type.equals(EjbSessionDescriptor.TYPE) ||
                            type.equals(EjbEntityDescriptor.TYPE))
                        {
                            result.passed(smh.getLocalString(getClass().getName() + ".passed2",
                                "PASSED [AS-EJB ejb-ref] ejb-ref-name [{0}] is valid",
                                new Object[]{refName}));
                        }
                        else
                        {
                            oneFailed = true;
                            result.failed(smh.getLocalString(getClass().getName() + ".failed1",
                                "FAILED [AS-EJB ejb-ref] ejb-ref-name has an invalid type in ejb-jar.xml." + 
                                " Type should be Session or Entity only"));
                        }
                    }
                    catch(IllegalArgumentException iex)
                    {
                        oneFailed = true;
                        result.failed(smh.getLocalString(getClass().getName() + ".failed",
                            "FAILED [AS-EJB ejb-ref] ejb-ref-name [{0}] is not defined in the ejb-jar.xml",
                            new Object[]{refName}));
                    }
                        
                    if(refJndiName!=null && refJndiName.length()==0)
                    {                        
                        oneFailed = true;
                        result.addErrorDetails(smh.getLocalString
                             (getClass().getName() + ".failed2",
                              "FAILED [AS-EJB ejb-ref] : jndi-name cannot be an empty string",
                              new Object[] {refName}));
                    }
                    if(refJndiName!=null && !refJndiName.startsWith("ejb/"))
                    {
                        oneWarning = true;
                        result.warning(smh.getLocalString
                             (getClass().getName() + ".warning",
                              "FAILED [AS-EJB ejb-ref] JNDI name should start with ejb/ for an ejb reference",
                              new Object[] {refName}));
                    }
                    
                    result.addGoodDetails(smh.getLocalString(
                                            getClass().getName() + ".passed1",
                              "PASSED [AS-EJB ejb-ref] : ejb-ref-Name is {0} and jndi-name is {1}",
                              new Object[] {refName,refJndiName}));
                }
            }
            else
            {
                 result.notApplicable(smh.getLocalString
                         (getClass().getName() + ".notApplicable",
                          "{0} Does not define any ejb references",
                          new Object[] {ejbName}));
                 return result;
            }
        }
        else
        {
            result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun",
                  "NOT RUN [AS-EJB] : Could not create an SunEjbJar object"));
            return result;
        }
        
	if (oneFailed) 
        {
	    result.setStatus(Result.FAILED);
        }
        else if(oneWarning)
        {
            result.setStatus(Result.WARNING);
        }   
        else
        {
	    result.addGoodDetails(smh.getLocalString
				  ("tests.componentNameConstructor",
				   "For [ {0} ]",
				   new Object[] {compName.toString()}));
	    result.passed
		(smh.getLocalString
		 (getClass().getName() + ".passed",
		  "PASSED [AS-EJB] :  {0} ejb refernce is verified",
		  new Object[] {ejbName, jndiName}));
	}
        return result;
    }
}

