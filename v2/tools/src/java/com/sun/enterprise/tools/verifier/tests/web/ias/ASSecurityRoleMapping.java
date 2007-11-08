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


import com.sun.enterprise.tools.verifier.tests.web.elements.*;
import com.sun.enterprise.tools.verifier.tests.web.WebTest;
import com.sun.enterprise.tools.verifier.tests.web.WebCheck;
import java.util.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.tools.common.dd.webapp.*;
import com.sun.enterprise.tools.common.dd.*;

//<addition author="irfan@sun.com" [bug/rfe]-id="4711198" >
/* Changed the result messages to reflect consistency between the result messages generated 
 * for the EJB test cases for SunONE specific deployment descriptors*/
//</addition>

public class ASSecurityRoleMapping extends WebTest implements WebCheck {



    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	WebComponentNameConstructor compName = new WebComponentNameConstructor(descriptor);
        SecurityRoleMapping[] secRoleMapp = descriptor.getIasWebApp().getSecurityRoleMapping();
        String roleName;
        String prinNames[];
        String grpNames[];
	boolean oneFailed = false;

        //System.out.println(">>>>>>>>>>>>> count of refs are "+ejbRefs.length);
	if (secRoleMapp !=null && secRoleMapp.length > 0) {
	    for (int rep=0; rep<secRoleMapp.length; rep++ ) {

                roleName=secRoleMapp[rep].getRoleName();
                prinNames=secRoleMapp[rep].getPrincipalName();
                grpNames=secRoleMapp[rep].getGroupName();

                if(validRoleName(roleName,descriptor)){

                  result.passed(smh.getLocalString
					  (getClass().getName() + ".passed",
					   "PASSED [AS-WEB security-role-mapping] role-name [ {0} ] properly defined in the war file.",
					   new Object[] {roleName}));

                  }else{

                  result.failed(smh.getLocalString
					   (getClass().getName() + ".failed",
					    "FAILED [AS-WEB security-role-mapping] role-name [ {0} ] is not valid, either empty or not defined in web.xml.",
					    new Object[] {roleName}));
                  oneFailed = true;

                  }

                if (prinNames !=null && prinNames.length > 0) {
                  String prinName;
                  for (int rep1=0; rep1<prinNames.length; rep1++ ) {
                      // <addition> srini@sun.com Bug : 4699658
                      //prinName = prinNames[rep1];
                      prinName = prinNames[rep1].trim();
                      // </addition>
                      if(prinName !=null && ! "".equals(prinName)){

                      result.passed(smh.getLocalString
					  (getClass().getName() + ".passed1",
					   "PASSED [AS-WEB security-role-mapping] principal-name [ {0} ] properly defined in the war file.",
					   new Object[] {prinName}));
                      }else{

                      result.failed(smh.getLocalString
					   (getClass().getName() + ".failed1",
					    "FAILED [AS-WEB security-role-mapping] principal-name [ {0} ] cannot be empty string.",
					    new Object[] {prinName}));
                      oneFailed = true;

                      }
                    }
                }

                if (grpNames !=null && grpNames.length > 0) {
                  String grpName;
                  for (int rep1=0; rep1<grpNames.length; rep1++ ) {
                      // <addition> srini@sun.com Bug : 4699658
                      //grpName =grpNames[rep1];
                      grpName =grpNames[rep1].trim();
                      // </addition>
                      if(grpName !=null && ! "".equals(grpName)){
                      result.passed(smh.getLocalString
					  (getClass().getName() + ".passed2",
					   "PASSED [AS-WEB security-role-mapping] group-name [ {0} ] properly defined in the war file.",
					   new Object[] {grpName}));

                      }else{

                      result.failed(smh.getLocalString
					   (getClass().getName() + ".failed2",
					    "FAILED [AS-WEB security-role-mapping] group-name [ {0} ] cannot be an empty string.",
					    new Object[] {grpName}));
                      oneFailed = true;

                      }
                    }
                }

	      }
         } else {
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "NOT APPLICABLE [AS-WEB sun-web-app] security-role-mapping element not defined in the web archive [ {0} ].",
				  new Object[] {descriptor.getName()}));
	    return result;
	}

	if (oneFailed){
		result.setStatus(Result.FAILED);
        } else {
                result.setStatus(Result.PASSED);
		result.passed
		    (smh.getLocalString
		     (getClass().getName() + ".passed3",
		      "PASSED [AS-WEB sun-web-app] security-role-mapping element(s) are valid within the web archive [ {0} ].",
                            new Object[] {descriptor.getName()} ));
        }
	return result;
    }

    boolean validRoleName(String roleName, WebBundleDescriptor descriptor){
          boolean valid=false;
          if (roleName != null && roleName.length() != 0) {
              Enumeration roles = descriptor.getSecurityRoles();
                    // test the sec roles in this .war
                    while (roles!=null && roles.hasMoreElements()) {
                        SecurityRoleDescriptor roleDesc = (SecurityRoleDescriptor) roles.nextElement();
                        String thisRoleName = roleDesc.getName();
			if (roleName.equals(thisRoleName)) {
                            valid = true;
                            break;
                        }
                    }
                    // to-do vkv#
                    //## roles related to application also needs to be checked, although present application
                    //##descriptor dont have seperate sec roles data-structure, so leaving it for time

          }
          return valid;
    }
}

