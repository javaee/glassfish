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

import java.util.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.deployment.WebComponentDescriptor;

/**
 *  
 *  @author      Arun Jain
 */
public class ServletParamName extends WebTest implements WebCheck {
    

     /** 
     *  Servlet Param Name exists test.
     * 
     * 
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

        Set servlets;
        Iterator servItr;
        String epName = null;
	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        WebComponentDescriptor servlet = null;
        Enumeration en;
        EnvironmentProperty ep = null;
        boolean oneFailed = false;
        boolean duplicate = false;
        
	if (!descriptor.getServletDescriptors().isEmpty()) {
            
	    // get the servlets in this .war
	    servlets = descriptor.getServletDescriptors();
	    servItr = servlets.iterator();
	    // test the servlets in this .war
	    while (servItr.hasNext()) {
		servlet = (WebComponentDescriptor)servItr.next();
                HashSet<String> envSet = new HashSet<String>();            
                for ( en = servlet.getInitializationParameters(); en.hasMoreElements();) {
                    ep = (EnvironmentProperty)en.nextElement();
                    epName = ep.getName();
                    
                    if (epName.length() != 0) {
                        // Do duplicate name test.
                        duplicate = checkDuplicate(epName, envSet);
                        
                    } else {
                        oneFailed = true;
			result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                        result.addErrorDetails(smh.getLocalString
                                               (getClass().getName() + ".failed",
                                                "Error: Param name/value entry should of finite length."));
                    }
                    if ( !duplicate) {
                        envSet.add(epName);
                    }
                    else {
                        oneFailed = true;
			result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                        result.addErrorDetails(smh.getLocalString
                                               (getClass().getName() + ".failed",
                                                "Error: Duplicate param names are not allowed."));
                    }
                }
            }
	    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
            result.addGoodDetails(smh.getLocalString
                                  (getClass().getName() + ".passed",
                                   "Param named/value exists for in the servlet [ {0} ].",
                                   new Object[] {servlet.getName()}));          
            
        } else {
            result.setStatus(Result.NOT_APPLICABLE);
	    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
            result.notApplicable(smh.getLocalString
                                 (getClass().getName() + ".notApplicable",
                                  "There are no initialization parameters for the servlet within the web archive [ {0} ]",
                                  new Object[] {descriptor.getName()}));
            return result;
        }
        
        if (oneFailed) {
            result.setStatus(Result.FAILED);
        } else {
            result.setStatus(Result.PASSED);
        }
        return result;
    }
    
    private boolean checkDuplicate(String epName, HashSet theSet) {
        
        return theSet.contains(epName);
    }
}

