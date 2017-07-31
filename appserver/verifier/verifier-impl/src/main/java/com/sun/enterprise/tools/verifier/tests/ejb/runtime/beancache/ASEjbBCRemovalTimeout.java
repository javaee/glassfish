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

package com.sun.enterprise.tools.verifier.tests.ejb.runtime.beancache;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

/** ejb [0,n]
 *    bean-cache ?
 *        max-cache-size ? [String]
 *        is-cache-overflow-allowed ? [String]
 *        cache-idle-timout-in-seconds ? [String]
 *        removal-timeout-in-seconds ? [String]
 *        victim-selection-policy ? [String]
 *
 * The removal-timeout-in-seconds specifies the amount of time the bean remains
 * passivated.
 * Valid values are between 0 and MAX_LONG
 * @author Irfan Ahmed
 */

public class ASEjbBCRemovalTimeout extends ASEjbBeanCache
{
    public Result check(EjbDescriptor descriptor)
    {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String beanCache = null;
        String removeTime = null;
        try{
            beanCache = getXPathValue("/sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/bean-cache");
            if(beanCache!=null)
            {
                removeTime = getXPathValue("/sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/bean-cache/removal-timeout-in-seconds");
                if (removeTime!=null)
                {
                    try{
                        removeTime = removeTime.trim();
                        if (removeTime.length()==0)
                        {
                            addErrorDetails(result, compName);
                            result.failed(smh.getLocalString(getClass().getName()+".failed",
                                    "FAILED [AS-EJB bean-cache] : removal-timeout-in-seconds cannot be empty. It should be between 0 and {0}",
                                    new Object[]{new Long(Long.MAX_VALUE)}));
                        }else
                        {
                            long value = new Integer(removeTime).longValue();
                            if(value < 0  || value > Long.MAX_VALUE)
                            {
                                addErrorDetails(result, compName);
                                result.failed(smh.getLocalString(getClass().getName()+".failed1",
                                    "FAILED [AS-EJB bean-cache] : removal-timeout-in-seconds cannot be {0}. It should be between 0 and {1}",
                                    new Object[]{new Long(value),new Long(Long.MAX_VALUE)}));
                            }else
                            {
                                addGoodDetails(result, compName);
                                result.passed(smh.getLocalString(getClass().getName()+".passed",
                                    "PASSED [AS-EJB bean-cache] : removal-timeout-in-seconds is {0}",
                                    new Object[]{new Long(value)}));
                            }
                        }
                    }catch(NumberFormatException nfex){
                        Verifier.debug(nfex);
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString(getClass().getName()+".failed2",
                                "FAILED [AS-EJB bean-cache] : [{0}] is not a valid Long number",new Object[]{removeTime}));
                    }
                } else // removal-timeout not defined
                {
                    addNaDetails(result, compName);
                    result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                                "NOT APPLICABLE [AS-EJB bean-cache] : removal-timeout-in-seconds element not defined"));
                }
            }else // bean-cache not defined
            {
                addNaDetails(result, compName);
                result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable1",
                    "NOT APPLICABLE [AS-EJB] : bean-cache element not defined"));
            }

        }catch(Exception ex)
        {
            addErrorDetails(result, compName);
            result.addErrorDetails(smh.getLocalString
                    (getClass().getName() + ".notRun",
                            "NOT RUN [AS-EJB] : Could not create an descriptor object"));
        }
        return result;
    }
}
