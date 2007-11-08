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

package com.sun.enterprise.tools.verifier.tests.web.ias;

import java.util.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.tools.verifier.tests.web.*;

import com.sun.enterprise.tools.common.dd.ResourceEnvRef;
import com.sun.enterprise.tools.common.dd.webapp.*;



public class IASResourceEnvRef extends WebTest implements WebCheck {

    public Result check(WebBundleDescriptor descriptor) {

        String resName;
        String resType;
	Result result = getInitializedResult();
	WebComponentNameConstructor compName = new WebComponentNameConstructor(descriptor);
        String jndiName;
        boolean oneFailed = false;
        boolean notApp = false;
        boolean chkOk=true;
        ResourceEnvRef[] envRefs= descriptor.getIasWebApp().getResourceEnvRef();

	if (envRefs.length > 0) {

             boolean isValidResRefName;
	     for (int rep=0; rep<envRefs.length; rep++ ) {
                isValidResRefName=false;
		resName = envRefs[rep].getResourceEnvRefName();
                jndiName = envRefs[rep].getJndiName();

                if (validResEnvRefName(resName,descriptor)) {

                    isValidResRefName=true;
		    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                    result.addGoodDetails(smh.getLocalString
					  (getClass().getName() + ".passed",
					   "Proper resource env reference name  [ {0} ] defined in the war file.",
					   new Object[] {resName}));
                } else {
                    if (!oneFailed)
                        oneFailed = true;
		    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                    result.addErrorDetails(smh.getLocalString
                                        (getClass().getName() + ".failed",
                                        "Error: Resource env reference name [ {0} ] is not valid, either empty or not defined in web.xml.",
                                        new Object[] {resName}));
                }

                if (isValidResRefName && validJndiName(jndiName, resName,descriptor)){
                    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                    result.addGoodDetails(smh.getLocalString
					  (getClass().getName() + ".passed",
					   "Proper jndi name  [ {0} ] defined in the war file.",
					   new Object[] {jndiName}));
                } else {
                    if (!oneFailed)
                        oneFailed = true;
		    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                    result.addErrorDetails(smh.getLocalString
                                        (getClass().getName() + ".failed",
                                        "Error: Jndi name [ {0} ] is not valid, either empty or not starts with \"jms/\".",
                                        new Object[] {resName}));
                }

            }

        } else {
            //System.out.println("There are no resource env references defined within the ias-web archive");
            notApp = true;
	    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
            result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There is no resource env reference element present in  this web archive [ {0} ].",
				  new Object[] {descriptor.getName()}));
        }
        if (oneFailed) {
            result.setStatus(Result.FAILED);
        } else if(notApp) {
            result.setStatus(Result.NOT_APPLICABLE);
        }else {
            result.setStatus(Result.PASSED);
            result.addGoodDetails(smh.getLocalString
                    ("tests.componentNameConstructor",
                    "For [ {0} ]",
                    new Object[] {compName.toString()}));
            result.passed
		    (smh.getLocalString
		     (getClass().getName() + ".passed",
		      "All resource env reference elements are valid within the web archive [ {0} ].",
                      new Object[] {descriptor.getName()} ));
        }
	return result;
    }

     boolean validResEnvRefName(String name,WebBundleDescriptor descriptor){
        boolean valid =true;
        if(name !=null && name.length()!=0) {
            try{
              descriptor.getJmsDestinationReferenceByName(name);
            }
            catch(IllegalArgumentException e){
            valid=false;
            }
        }  else{
         valid=false;

        }
        return valid;
    }

    boolean validJndiName(String refJndiName, String refName,WebBundleDescriptor descriptor){
        boolean valid =true;
        JmsDestinationReferenceDescriptor resDesc;

        if(refJndiName !=null && refJndiName.length()!=0) {
          //descriptor.getResourceReferenceByName(name);
          resDesc = descriptor.getJmsDestinationReferenceByName(refName);
                        String type = resDesc.getRefType();
                        if(type.indexOf("javax.jms")>-1) //jms resource
                        {
                            if(!refJndiName.startsWith("jms/"))
                              valid=false;

                        }

                        else
                            valid=false;
         }
        else{
        valid=false;
        }

        return valid;
    }



}
