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

package com.sun.enterprise.tools.verifier.tests.web;

import com.sun.enterprise.tools.verifier.tests.web.WebTest;
import java.util.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;

/** 
 * The environment entry name must be of finite length.
 */
public class WebEnvEntryName extends WebTest implements WebCheck { 


    /** 
     * The environment entry name must be of finite length.
     *
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        boolean oneFailed = false;
        EnvironmentProperty nextEnvironmentProperty;
        String envName;
	if (!descriptor.getEnvironmentProperties().isEmpty()) {

	    for (Iterator itr = descriptor.getEnvironmentProperties().iterator(); 
		 itr.hasNext();) {
		nextEnvironmentProperty = (EnvironmentProperty) itr.next();
                envName = nextEnvironmentProperty.getName();
		if (envName.length() > 0) {
		    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addGoodDetails
			(smh.getLocalString
			 (getClass().getName() + ".passed",
			  "Environment entry value [ {0} ] has valid name within web archive [ {1} ]",
			  new Object[] {nextEnvironmentProperty.getName(),descriptor.getName()}));
		} else {
		    oneFailed = true;
		    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addErrorDetails
			(smh.getLocalString
			 (getClass().getName() + ".failed",
			  "Error: Environment entry name must be of finite length"));
		} 
	    }
	    if (!oneFailed){
		result.setStatus(Result.PASSED);
	    } else {
		result.setStatus(Result.FAILED);
	    }
	} else {
            result.setStatus(Result.NOT_APPLICABLE);
	    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There are no environment entry elements defined within this web archive [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}

	return result;
    }
}
