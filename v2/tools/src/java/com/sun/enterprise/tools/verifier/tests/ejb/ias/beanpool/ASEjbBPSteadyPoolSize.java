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

package com.sun.enterprise.tools.verifier.tests.ejb.ias.beanpool;

import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import java.util.*;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;

import com.sun.enterprise.tools.common.dd.ejb.*;


/** ejb [0,n]
 *    bean-pool ?
 *        steady-pool-size ? [String]
 *        pool-resize-quantity ? [String]
 *        max-pool-size ? [String]
 *        pool-idle-timeout-in-seconds ? [String]
 *        max-wait-time-in-millis ? [String]
 *
 * The steady-pool-size element specifies the initial and minimum number of beans
 * that must be maintained in the bean pool.
 *
 * Valid values are from 0 to MAX_INT
 *
 *
 * @author Irfan Ahmed
 */
public class ASEjbBPSteadyPoolSize extends ASEjbBeanPool
{
    
    public Result check(EjbDescriptor descriptor) 
    {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = new ComponentNameConstructor(descriptor);

        SunEjbJar ejbJar = descriptor.getEjbBundleDescriptor().getIasEjbObject();
        String ejbName = null;
        Ejb testCase = null;
        boolean oneFailed = false;
        if(ejbJar!=null)
        {
            getBeanPool(descriptor,ejbJar);
            if(beanPool!=null)
            {
                String steadyPoolSize = beanPool.getSteadyPoolSize();
                if(steadyPoolSize!=null)
                {
                    if(steadyPoolSize.length()==0)
                    {
                        result.failed(smh.getLocalString(getClass().getName()+".failed1",
                            "FAILED [AS-EJB bean-pool] : steady-pool-size cannot be empty"));
                    }
                    else
                    {
                        try
                        {
                            int value = Integer.valueOf(steadyPoolSize).intValue();
                            if(value < 0  || value > Integer.MAX_VALUE)
                            {
                                result.failed(smh.getLocalString(getClass().getName()+".failed2",
                                    "FAILED [AS-EJB bean-pool] : steady-pool-size cannot be {0}. It should be between 0 and {1}",
                                    new Object[]{new Integer(value),new Integer(Integer.MAX_VALUE)}));
                            }
                            else
                            {
							    String maxPoolSize = beanPool.getMaxPoolSize();
								int maxPool = 0;
								if(maxPoolSize == null){
								    result.passed(smh.getLocalString(getClass().getName()+".passed1",
									"PASSED [AS-EJB bean-pool] : steady-pool-size is {0}",new Object[] { new Integer(value)}));
								}
								else
								{
								    try{
								        maxPool = Integer.parseInt(maxPoolSize);
								    }catch(NumberFormatException nfe){
                                        result.failed(smh.getLocalString(getClass().getName()+".failed3",       
                                        "FAILED [AS-EJB bean-pool] : The value {0} for max-pool-size is not a valid Integer number",new Object[]{maxPoolSize}));

										return result;

								    }
                                                                    //<addition author="irfan@sun.com" [bug/rfe]-id="4724439" >
								    //if(value < maxPool){
                                                                    if(value <= maxPool){
                                                                    //</addition>
                                        result.passed(smh.getLocalString(getClass().getName()+".passed2",
                                        "PASSED [AS-EJB bean-pool] : steady-pool-size is {0} and is less-than/equal-to max-pool-size [{1}]",
                                        new Object[]{new Integer(value), new Integer(maxPool)}));
							        }else{
								        result.warning(smh.getLocalString(getClass().getName()+".warning","WARNING [AS-EJB bean-pool] : steady-pool-size [{0}]  is greater than max-pool-size[{1}]", new Object[]{new Integer(value), new Integer(maxPool)}));
								    }
								}
                            }
                        }
                        catch(NumberFormatException nfex)
                        {
                            Verifier.debug(nfex);
                            result.failed(smh.getLocalString(getClass().getName()+".failed4",
                                "FAILED [AS-EJB bean-pool] : The value {0} for steady-pool-size is not a valid Integer number",
                                new Object[]{steadyPoolSize}));
                        }
                    }
                }
                else
                {
                    result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable1",
                    "NOT APPLICABLE [AS-EJB bean-pool] : steady-pool-size element not defined"));
                }
            }
            else
            {
                result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable2",
                    "NOT APPLICABLE [AS-EJB] : bean-pool element not defined"));
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
}

