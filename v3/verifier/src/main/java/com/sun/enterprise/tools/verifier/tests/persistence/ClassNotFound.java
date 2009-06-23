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


package com.sun.enterprise.tools.verifier.tests.persistence;

import com.sun.enterprise.tools.verifier.tests.VerifierTest;
import com.sun.enterprise.tools.verifier.tests.VerifierCheck;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.deployment.Descriptor;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;

/**
 * The names of classes specified in persistence.xml must be loadable.
 * TopLink simply ignores if class could not be loaded. So we need this test.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class ClassNotFound extends VerifierTest implements VerifierCheck {
    public Result check(Descriptor descriptor) {
        Result result = getInitializedResult();
        addErrorDetails(result, getVerifierContext().getComponentNameConstructor());
        result.setStatus(Result.PASSED);
        final PersistenceUnitDescriptor pu = PersistenceUnitDescriptor.class.cast(descriptor);
        for(String className : pu.getClasses()) {
            try {
                Class.forName(className, false, getVerifierContext().getClassLoader());
            } catch (ClassNotFoundException e) {
                result.failed(smh.getLocalString(getClass().getName() + "failed1",
                        "Class [ {0} ] could not be loaded", new Object[]{className}));
            } catch (NoClassDefFoundError e) {
                result.failed(smh.getLocalString(getClass().getName() + "failed2",
                        "Class [ {0} ] could not be loaded " +
                        "because a dependent class could not be loaded. See reason:\n [ {1} ]",
                        new Object[]{className,e.getMessage()}));
            }
        }
        return result;
    }
}
