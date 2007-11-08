package com.sun.enterprise.tools.verifier.tests.ejb.ias.beanpool;


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

import com.sun.enterprise.tools.common.dd.ejb.BeanPool;
/** ejb [0,n]
 *    bean-pool ?
 *        steady-pool-size ? [String]
 *        pool-resize-quantity ? [String]
 *        max-pool-size ? [String]
 *        pool-idle-timeout-in-seconds ? [String]
 *        max-wait-time-in-millis ? [String]
 *
 * The bean-pool element specifies the bean pool properties for the beans
 *
 * The bean-pool is valid only for Stateless Session Beans (SSB) and
 * Message-Driven Beans (MDB).
 * @author Irfan Ahmed
 */
public class ASEjbBeanPool extends EjbTest implements EjbCheck { 

    public BeanPool beanPool;
    public Ejb testCase;
    
    public void getBeanPool(EjbDescriptor descriptor, SunEjbJar ejbJar)
    {
        testCase = getEjb(descriptor.getName(),ejbJar);
        beanPool = testCase.getBeanPool();
    }
    
    public Result check(EjbDescriptor descriptor)
    {
        Result result = getInitializedResult();
	ComponentNameConstructor compName = new ComponentNameConstructor(descriptor);
        
        SunEjbJar ejbJar = descriptor.getEjbBundleDescriptor().getIasEjbObject();
        if(ejbJar!=null)
        {
            getBeanPool(descriptor,ejbJar);
            if(beanPool!=null)
            {
                if(descriptor instanceof EjbSessionDescriptor 
                    && ((EjbSessionDescriptor)descriptor).getSessionTypeString().equals(EjbSessionDescriptor.STATEFUL)
                    || descriptor instanceof EjbEntityDescriptor)
                {
                    result.warning(smh.getLocalString(getClass().getName()+".warning1",
                    "WARNING [AS-EJB ejb] : bean-pool should be defined for Stateless Session Beans or Message Driven Beans"));
                }
            }
            else
            {
                if(descriptor instanceof EjbMessageBeanDescriptor
                    || (descriptor instanceof EjbSessionDescriptor 
                            && ((EjbSessionDescriptor)descriptor).getSessionTypeString().equals(EjbSessionDescriptor.STATELESS)))
                {
                    result.warning(smh.getLocalString(getClass().getName()+".warning",
                        "WARNING [AS-EJB ejb] : bean-pool should be defined for Stateless Session and Message Driven Beans"));
                }
                else
                    result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                        "NOT APPLICABLE [AS-EJB ejb] : bean-pool element not defined"));
            }
            return result;
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
            return result;
        }
    }
}
        
