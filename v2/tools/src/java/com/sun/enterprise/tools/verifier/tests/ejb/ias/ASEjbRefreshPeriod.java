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
import com.sun.enterprise.tools.common.dd.ResourceRef;
import com.sun.enterprise.tools.common.dd.DefaultResourcePrincipal;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.EjbEntityDescriptor;

/** ejb [0,n]
 *    refresh-period-in-seconds ? [String]
 *
 * The refresh-period-in-seconds denotes the rate at which a read-only-bean
 * is refreshed.
 * Is applicable only if it is a ROB.
 * The value should be between 0 and MAX_INT
 * @author Irfan Ahmed
 */
public class ASEjbRefreshPeriod extends EjbTest implements EjbCheck { 

    public Result check(EjbDescriptor descriptor) 
    {
	Result result = getInitializedResult();
	ComponentNameConstructor compName = new ComponentNameConstructor(descriptor);

        SunEjbJar ejbJar = descriptor.getEjbBundleDescriptor().getIasEjbObject();
        boolean oneWarning = false;
        boolean oneFailed = false;
        
        if(ejbJar!=null)
        {
            Ejb testCase = getEjb(descriptor.getName(), ejbJar);
            String refreshPeriod = testCase.getRefreshPeriodInSeconds();
            
            boolean isReadOnly = false;
            String beanType = testCase.getIsReadOnlyBean();
            if(beanType!=null && beanType.equals("true"))
                isReadOnly = true;
            else
                isReadOnly=false;
            
            if(refreshPeriod!=null)
            {
                if(refreshPeriod.length()==0)
                {
                    oneFailed = true;
                    result.failed(smh.getLocalString(getClass().getName()+".failed3",
                        "FAILED [AS-EJB ejb] : refresh-period-in-seconds is invalid. It shoud be between 0 and " + Integer.MAX_VALUE));
                }
                else
                {
                    if(!(descriptor instanceof EjbEntityDescriptor 
                            && isReadOnly))
                    {
                        oneWarning = true;
                        result.warning(smh.getLocalString(getClass().getName()+".warning",
                        "WARNING [AS-EJB ejb] : refresh-period-in-seconds should be defined for Read Only Beans."));
                    }
                    try
                    {
                        int refValue = Integer.parseInt(refreshPeriod);
                        if(refValue<0 || refValue>Integer.MAX_VALUE)
                        {
                            oneFailed = true;
                            result.failed(smh.getLocalString(getClass().getName()+".failed4",
                                "FAILED [AS-EJB ejb] : refresh-period-in-seconds cannot be greater than " + Integer.MAX_VALUE + 
                                " or less than 0"));
                        }
                        else
                            result.passed(smh.getLocalString(getClass().getName()+".passed4",
                                "PASSED [AS-EJB ejb] : refresh-period-in-seconds is {0}",new Object[]{new Integer(refValue)}));
                    }
                    catch(NumberFormatException nfex)
                    {
                        oneFailed = true;
                        Verifier.debug(nfex);
                        result.failed(smh.getLocalString(getClass().getName()+".failed5",
                            "FAILED [AS-EJB ejb] : refresh-period-in-seconds is invalid. It should be between 0 and "+Integer.MAX_VALUE ));
                    }
                }
            }
            else
            {
                if((descriptor instanceof EjbEntityDescriptor) 
                        && (isReadOnly))
                {
                    result.warning(smh.getLocalString(getClass().getName()+".warning1",
                    "WARNING [AS-EJB ejb] : refresh-period-in-seconds should be defined for Read Only Beans"));
                }
                else
                {
                    result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                        "NOT APPLICABLE [AS-EJB ejb] : refresh-period-in-seconds is not defined"));
                }
            }
            if(oneWarning)
                result.setStatus(Result.WARNING);
            if(oneFailed)
                result.setStatus(Result.FAILED);
        }
        else
        {
            result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun",
                  "NOT RUN [AS-EJB] : Could not create an SunEjbJar object"));
        }
        return result;
    }
}
