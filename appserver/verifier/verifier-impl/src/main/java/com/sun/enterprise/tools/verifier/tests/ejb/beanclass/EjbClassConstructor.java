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

package com.sun.enterprise.tools.verifier.tests.ejb.beanclass;


import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * Enterprise Java Bean class constuctor test.  
 * The class must have a public constructor that takes no parameters.
 */
public class EjbClassConstructor extends EjbTest { 


    /**
     * Enterprise Java Bean class constuctor test.  
     * The class must have a public constructor that takes no parameters.
     *   
     * @param descriptor the Enterprise Java Bean deployment descriptor   
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        Class c = loadEjbClass(descriptor, result);
        if (c!=null) {

            boolean foundOne = false;
            Constructor [] constructors = c.getConstructors();
            for (int i = 0; i < constructors.length; i++) {
                int modifiers = constructors[i].getModifiers();
                if (Modifier.isPublic(modifiers)) {
                    Class [] constructorParameterTypes;
                    constructorParameterTypes = constructors[i].getParameterTypes();
                    if (constructorParameterTypes.length > 0) {
                        continue;
                    } else {
                        foundOne = true;
                        break;
                    }
                }
            }

            if (foundOne) {
		result.addGoodDetails(smh.getLocalString
				      ("tests.componentNameConstructor",
				       "For [ {0} ]",
				       new Object[] {compName.toString()}));
		result.passed(smh.getLocalString
			      (getClass().getName() + ".passed",
			       "Valid: This bean [ {0} ] has a public constructor method with no "
			       + " \n parameters.  Enterprise beans must have a public constructor "
			       + " \n method with no parameters.",
			       new Object[] {descriptor.getEjbClassName()}));
            } else {
		result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                result.failed(smh.getLocalString
                    (getClass().getName() + ".failed",
                    "Error: There is no public constructor method with no parameters"
                    + "\n defined within bean [ {0} ].  Enterprise beans must have a "
                    + "\n public constructor methods with no parameters.",
                    new Object[] {descriptor.getEjbClassName()}));
            }
        }
        return result;

    }
}
