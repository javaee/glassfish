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
import com.sun.enterprise.tools.common.dd.webapp.*;

//<addition author="irfan@sun.com" [bug/rfe]-id="4711198" >
/* Changed the result messages to reflect consistency between the result messages generated 
 * for the EJB test cases for SunONE specific deployment descriptors*/
//</addition>


public class ASCacheHelperClass extends ASCache implements WebCheck {


    
    public Result check(WebBundleDescriptor descriptor) {


	Result result = getInitializedResult();
	WebComponentNameConstructor compName = new WebComponentNameConstructor(descriptor);

        boolean oneFailed = false;
        boolean notApp = false;
        boolean oneWarning=false;
        boolean presentHelper=false;

        Cache cache = getCache(descriptor);

        CacheHelper[] helperClasses=null;
        CacheHelper helperClass=null;
        

        WebProperty[] webProps;
        String name=null;
        String classname=null;
        String[] names=null;
        
        //to-do vkv# check for class-name attribute.
        if (cache != null )
          helperClasses=cache.getCacheHelper();
          
        if (cache != null && helperClasses !=null && helperClasses.length >0) {
              names=new String[helperClasses.length];             
               
              for(int rep=0;rep < helperClasses.length;rep++){
                    helperClass=helperClasses[rep]; 
                    if(helperClass==null)
                        continue;
		    name = helperClass.getAttributeValue("name");
                    classname= helperClass.getAttributeValue("class-name");
                    Class hClass=null;
                    names[rep]=name;

                    if (name!=null || name.length() != 0 ||classname!=null || classname.length() != 0){
                        //check if the name already exist 
                        boolean isDuplicate=false;
                        for(int rep1=0;rep1<rep;rep1++)
                        {
                            if(name.equals(names[rep1])){
                                isDuplicate=true;
                                break;
                            }

                        }
                        
                     
                        if(isDuplicate){
                            oneFailed = true;
                            result.failed(smh.getLocalString
                                      (getClass().getName() + ".failed",
                                      "FAILED [AS-WEB cache-helper] name attribute [ {0} ], must be unique in the entire list of cache-helper.",
                                      new Object[] {name}));
                        } else {
                        
                            hClass = loadClass(result,classname);
                       
                            /*if(hClass !=null) { 
                               if(com.sun.appserv.web.CacheHelper.class.isAssignableFrom(hClass))
                                         presentHelper=true ;
                               else
                                         presentHelper=false ;
                            }*/
                            if(hClass !=null) 
                                presentHelper=true ;
                            else
                                presentHelper=false ;
                          
                            if(!presentHelper) {
		                  result.warning(smh.getLocalString(
                                      getClass().getName() + ".error",
                                      "WARNING [AS-WEB cache-helper] name [ {0} ], class not present in the war file.",
                                      new Object[] {name}));
                                  oneFailed = true;
                            } else {
                                result.passed(smh.getLocalString
					  (getClass().getName() + ".passed",
					   "PASSED [AS-WEB cache-helper] name  [ {0} ], helper class is valid.",
					   new Object[] {name}));
                            }
                            
                        }

                    } else {
		        result.failed(smh.getLocalString
                                      (getClass().getName() + ".failed1",
                                      "FAILED [AS-WEB cache-helper] name [ {0} ], either empty or null.",
                                      new Object[] {name}));
		        oneFailed = true;
                   
                    }

                  webProps=helperClass.getWebProperty();

                  if(ASWebProperty.checkWebProperties(webProps,result ,descriptor, this )){
                  oneFailed=true;
                  result.failed(smh.getLocalString
                                (getClass().getName() + ".failed2",
                                "FAILED [AS-WEB cache-helper] Atleast one name/value pair is not valid in helper-class of [ {0} ].",
                                new Object[] {descriptor.getName()}));
                  }
                  
                  
             }//end of for 
                            

        } else {
           
            notApp = true;
	    result.notApplicable(smh.getLocalString
                                 (getClass().getName() + ".notApplicable",
                                  "NOT APPLICABLE [AS-WEB cache-helper] There is no cache-helper element for the web application",
                                  new Object[] {descriptor.getName()}));
        }
        
        
        if (oneFailed) {
            result.setStatus(Result.FAILED);
        } else if(oneWarning){
            result.setStatus(Result.WARNING);
        } else if(notApp) {
            result.setStatus(Result.NOT_APPLICABLE);
        }else {
            result.setStatus(Result.PASSED);
        }
	return result;
    }
    

}
