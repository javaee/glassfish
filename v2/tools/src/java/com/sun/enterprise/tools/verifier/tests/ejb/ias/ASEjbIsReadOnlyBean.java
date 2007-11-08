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
import com.sun.enterprise.tools.common.dd.ejb.EnterpriseBeans;
import com.sun.enterprise.tools.common.dd.ejb.Ejb;
import com.sun.enterprise.deployment.EjbEntityDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;
import java.lang.reflect.*;
/** ejb [0,n]
 *   is-read-only-bean ? [String]
 *
 * The <B>is-read-only-bean</B> should be defined for entity beans only.
 * Also the implementation should not define ant create and remove methods.
 * This is recommended and not required
 * @author Irfan Ahmed
 */
public class ASEjbIsReadOnlyBean extends EjbTest implements EjbCheck { 

    boolean oneFailed= false;
	boolean oneWarning = false;

    /**
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = new ComponentNameConstructor(descriptor);

        SunEjbJar ejbJar = descriptor.getEjbBundleDescriptor().getIasEjbObject();
        String ejbName = null;
        Ejb testCase = null;
        if(ejbJar!=null)
        {
            testCase = getEjb(descriptor.getName(),ejbJar);
            String isReadOnlyBean = testCase.getIsReadOnlyBean();
            if(isReadOnlyBean != null)
            {
                if(isReadOnlyBean.length()==0)
                {
                    oneFailed = true;
                     result.failed(smh.getLocalString(getClass().getName()+".failed",
                        "FAILED [AS-EJB ejb] : is-read-only-bean cannot be an empty string"));
                }
                else
                {
                    if(!isReadOnlyBean.equals("true") && !isReadOnlyBean.equals("false"))
                    {
                        oneFailed = true;
                        result.failed(smh.getLocalString(getClass().getName()+".failed1",
                            "FAILED [AS-EJB ejb] : is-read-only-bean cannot be {0}. It can only be true or false",
                            new Object[]{isReadOnlyBean}));
                    }
                    else
                    {
                        if(isReadOnlyBean.equals("true"))
                            testROBSpecific(descriptor,testCase,result);
                        else
                        {
                            result.passed(smh.getLocalString(getClass().getName()+".passed",
                            "PASSED [AS-EJB ejb] is-read-only-bean is {0}",
                            new Object[]{isReadOnlyBean}));
                        }
                    }
                }
                if(oneFailed)
                    result.setStatus(Result.FAILED);
			    else if(oneWarning)
				    result.setStatus(Result.WARNING);
            }
            else
            {
                result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                    "NOT APPLICABLE [AS-EJB ejb] is-read-only-bean Element not defined"));
            }
        }
        else
        {
            result.addErrorDetails(smh.getLocalString
                                   ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
            result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun",
                  "NOT RUN [AS-EJB] : Could not create an SunEjbJar object"));
        }
        return result;
    }
    
    public void testROBSpecific(EjbDescriptor descriptor, Ejb testCase,Result result)
    {
        //Read only Beans can only be Entity Beans
        if(descriptor instanceof EjbEntityDescriptor)
        {
            result.passed(smh.getLocalString(getClass().getName()+".passed1", 
                                    "PASSED [AS-EJB ejb] : Read Only Beans can only be Entity Beans"));
        }
        else if(descriptor instanceof EjbSessionDescriptor)
        {
            oneFailed = true;
            result.failed(smh.getLocalString(getClass().getName()+".failed2",
                "FAILED [AS-EJB ejb] : Read Only Beans cannot be Session Beans. They can only be Entity Beans"));
            return;
        }
        else if(descriptor instanceof EjbMessageBeanDescriptor)
        {
            oneFailed = true;
            result.failed(smh.getLocalString(getClass().getName()+".failed3",
                "FAILED [AS-EJB ejb] : Read Only Beans cannot be Message Driven Beans. They can only be Entity Beans"));
            return;
        }

        //Only Container Managed Transactions are Allowed
        String txnType = descriptor.getTransactionType();
        if(txnType.equals(descriptor.CONTAINER_TRANSACTION_TYPE))
        {
            result.passed(smh.getLocalString(getClass().getName()+".passed2", 
                                    "PASSED [AS-EJB ejb] : Read Only Beans can only have Container Managed Transactions"));
        }
        else
        {
            oneFailed = true;
            result.failed(smh.getLocalString(getClass().getName()+".failed4",
                "FAILED [AS-EJB ejb] : Read Only Beans cannot have Bean Managed Transactions"));
        }

        //Encourage not to have create/remove methods in Home Interface
        String homeName = descriptor.getHomeClassName();
        Class homeClass = null;
        boolean foundCreateMethod = false;
        boolean foundRemoveMethod = false;
        try
        {
            homeClass = getVerifierContext().getClassLoader().loadClass(homeName);
            Method methods[] = homeClass.getMethods();
            for(int i=0;i<methods.length;i++)
            {
                if(methods[i].getName().startsWith("create"))
                {
                    foundCreateMethod = true;
                    break;
                }
            }

            for(int i=0;i<methods.length;i++)
            {
               if(methods[i].getName().startsWith("remove"))
               {
                    foundRemoveMethod = true;
                    break;
               }
            }
            if(foundCreateMethod)
            {
			    oneWarning = true;
                result.addWarningDetails(smh.getLocalString(getClass().getName()+".warning1",
                    "WARNING [AS-EJB ejb] : Read Only Beans should have zero create Methods"));
            }
            else
                result.passed(smh.getLocalString(getClass().getName()+".passed3",
                    "PASSED [AS-EJB ejb] : Read Only Bean has zero create Methods"));

            if(foundRemoveMethod)
            {
			    oneWarning = true;
                result.addWarningDetails(smh.getLocalString(getClass().getName()+".warning2",
                    "WARNING [AS-EJB ejb] : Read Only Beans should have zero remove Methods"));
            }
            else
            {
                result.passed(smh.getLocalString(getClass().getName()+".passed4",
                    "PASSED [AS-EJB ejb] : Read Only Bean has zero remove Methods"));
            }

            //Refresh Period Test
            String refreshPeriod = testCase.getRefreshPeriodInSeconds();
            if(refreshPeriod!=null)
            {
                if(refreshPeriod.length()==0)
                {
                    oneFailed = true;
                    result.failed(smh.getLocalString(getClass().getName()+".failed5",
                        "FAILED [AS-EJB ejb] : refresh-period-in-seconds is empty. It should be between 0 and {0}",new Object[]{new Long( Long.MAX_VALUE)}));
                }
                else
                {
                    try
                    {
                        long refValue = Long.valueOf(refreshPeriod).longValue();
                        if(refValue <0 || refValue > Long.MAX_VALUE)
                        {
                            result.failed(smh.getLocalString(getClass().getName()+".failed6",
                                "FAILED [AS-EJB ejb] : refresh-period-in-seconds is invalid. It should be between 0 and {0}." ,new Object[]{new Long( Long.MAX_VALUE)}));
                        }
                        else
                            result.passed(smh.getLocalString(getClass().getName()+".passed5",
                                "PASSED [AS-EJB ejb] : refresh-period-in-seconds is {0}",new Object[]{new Long(refValue)}));
                    }
                    catch(NumberFormatException nfex)
                    {
                        Verifier.debug(nfex);
                        result.failed(smh.getLocalString(getClass().getName()+".failed6",
                            "FAILED [AS-EJB ejb] : refresh-period-in-seconds is invalid. It should be between 0 and {0}", new Object[]{new Long(Long.MAX_VALUE)} ));
                    }
                }
            }

            if(oneFailed)
                result.setStatus(Result.FAILED);
        }
        catch(ClassNotFoundException cfne)
        {
            Verifier.debug(cfne);
            result.addErrorDetails(smh.getLocalString
                           ("tests.componentNameConstructor",
                            "For [ {0} ]",
                            new Object[] {(new ComponentNameConstructor(descriptor)).toString()}));
            result.failed(smh.getLocalString
                  (getClass().getName() + ".failed7",
                   "Error: Home interface [ {0} ] does not exist or is not loadable within bean [ {1} ]",
                   new Object[] {homeName, descriptor.getName()}));
        }
    }
}

