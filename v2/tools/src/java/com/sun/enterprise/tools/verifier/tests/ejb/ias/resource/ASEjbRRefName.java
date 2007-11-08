package com.sun.enterprise.tools.verifier.tests.ejb.ias.resource;
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

/** ejb [0,n]
 *   resource-ref [0,n]
 *       res-ref-name [String]
 *       jndi-name [String]
 *       default-resource-principal ?
 *           name [String]
 *           password [String]
 *
 * The res-ref-name specfies the name of the resource in the ejb-jar.xml for which
 * this element shows the jndi binding properties
 *
 * The res-ref-name should exist in the ejb-jar.xml file
 *
 * @author Irfan Ahmed
 */
public class ASEjbRRefName extends ASEjbResRef { 

    public Result check(EjbDescriptor descriptor) 
    {
	Result result = getInitializedResult();
	ComponentNameConstructor compName = new ComponentNameConstructor(descriptor);

        SunEjbJar ejbJar = descriptor.getEjbBundleDescriptor().getIasEjbObject();
        boolean oneFailed = false;
        
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
            
            ResourceRef resRefs[] = testCase.getResourceRef();
            if(resRefs.length > 0)
            {
                for(int j=0;j<resRefs.length;j++)
                {
                    String refName = resRefs[j].getResRefName();
                    if(refName.length()==0)
                    {
                        result.failed(smh.getLocalString(getClass().getName()+".failed",
                            "FAILED [AS-EJB resource-ref] : resource-ref has empty res-ref-name"));
                    }
                    else
                    {
                        result.passed(smh.getLocalString(getClass().getName()+".passed",
                            "PASSED [AS-EJB resource-ref] : res-ref-name is {0}",new Object[]{refName}));
                    }
                }
            }
            else
            {
                 result.notApplicable(smh.getLocalString
                         (getClass().getName() + ".notApplicable",
                          "{0} Does not define any resource-ref Elements",
                          new Object[] {descriptor.getName()}));
            }
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

