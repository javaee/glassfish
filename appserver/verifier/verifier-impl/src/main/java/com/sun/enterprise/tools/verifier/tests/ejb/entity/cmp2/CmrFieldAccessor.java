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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.ejb.deployment.descriptor.CMRFieldInfo;
import org.glassfish.ejb.deployment.descriptor.RelationRoleDescriptor;

/**
 * Container-managed fields declaration test.
 * CMR fields accessor methods names must the field name with the first
 * letter uppercased and prefixed with get and set
 * 
 * @author  Jerome Dochez
 * @author Sheetal Vartak
 * @version 
 */
public class CmrFieldAccessor extends CmrFieldTest {

    /**
     * run an individual verifier test of a declated cmr field of the class
     *
     * @param entity the descriptor for the entity bean containing the cmp-field    
     * @param rrd the descriptor for the declared cmr field
     * @param c the class owning the cmp field
     * @param result the result object to use to put the test results in
     * 
     * @return true if the test passed
     */            
    protected boolean runIndividualCmrTest(Descriptor entity, RelationRoleDescriptor rrd, Class c, Result result) {
	boolean oneFailed = false;
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
	    CMRFieldInfo info = rrd.getCMRFieldInfo();
	    if (info == null) {
		 addErrorDetails(result, compName);
		result.addErrorDetails(smh.getLocalString
		            ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CMPTest.isAccessorDeclared.failed1",
                            "Error : no CMR Field  declared ",
		            new Object[] {}));  
		return false;
	    }
	    oneFailed = isAccessorDeclared(info.name, info.type, c, result);
	    if (oneFailed == false) {
            // do nothing, appropriate message has been added in
            // isAccessorDeclared().
	    }else {
		 result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.addGoodDetails(smh.getLocalString
		            ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CMPTest.isAccessorDeclared.passed",
                            "CMR Field is properly declared ",
		            new Object[] {}));     
	    }
	    return oneFailed;
    }
}
