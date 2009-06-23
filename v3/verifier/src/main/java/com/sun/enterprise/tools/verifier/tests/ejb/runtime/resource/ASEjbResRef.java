package com.sun.enterprise.tools.verifier.tests.ejb.runtime.resource;

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
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;

import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;

import com.sun.enterprise.deployment.ResourceReferenceDescriptor;

/** ejb [0,n]
 *  resource-ref [0,n]
 *      res-ref-name [String]
 *      jndi-name [String]
 *      default-resource-principal ?
 *          name [String]
 *          password [String]
 *
 * The resource-ref element holds the runtime bindings for a resource
 * reference declared in the ejb-jar.xml
 * @author Irfan Ahmed
 */

public class ASEjbResRef extends EjbTest implements EjbCheck { 

    public Result check(EjbDescriptor descriptor) 
    {
	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        Set resRef = descriptor.getResourceReferenceDescriptors();
        boolean oneFailed = false;
        if(!(resRef.isEmpty()))
        {
            Iterator it = resRef.iterator();
            while (it.hasNext())
            {
                ResourceReferenceDescriptor resDesc = (ResourceReferenceDescriptor)it.next();
                String refName = resDesc.getName();
                
             try
                    {
                        descriptor.getResourceReferenceByName(refName);
                        addGoodDetails(result, compName);
                        result.passed(smh.getLocalString(getClass().getName()+".passed",
                            "PASSED [AS-EJB resource-ref] : res-ref-name {0} is verified with ejb-jar.xml",
                            new Object[]{refName}));
                    }
                    catch(IllegalArgumentException iaex)
                    {
                        Verifier.debug(iaex);
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString(getClass().getName()+".failed",
                            "FAILED [AS-EJB resource-ref] : The res-ref-name {0} is not defined in ejb-jar.xml for this bean",
                            new Object[]{refName}));
                    }
             
            }
                
        }
        else
        {
            addNaDetails(result, compName);
            result.notApplicable(smh.getLocalString
                (getClass().getName() + ".notApplicable",
                    "{0} Does not define any resource-ref Elements",
                    new Object[] {descriptor.getName()}));
        }
    return result;
    }
}
