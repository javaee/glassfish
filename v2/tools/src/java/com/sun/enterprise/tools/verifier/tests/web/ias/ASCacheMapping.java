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
import com.sun.enterprise.deployment.web.*;
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

public class ASCacheMapping extends ASCache implements WebCheck {


    boolean oneWarning = false;
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	WebComponentNameConstructor compName = new WebComponentNameConstructor(descriptor);

        boolean oneFailed = false;
        boolean notApp = false;
        Cache cache = getCache(descriptor);

        CacheMapping[] cacheMapp=null;

        String servletName=null;
        String urlPattern=null;
        String timeout=null;
        String[] httpMethods;
        //boolean[] keyFields;
        String cacheHelperRef;



        if (cache != null ){
          cacheMapp=cache.getCacheMapping();

          }
                  

         if (cache != null && cacheMapp !=null && cacheMapp.length !=0 ) {
            for(int rep=0;rep < cacheMapp.length;rep++){
		servletName = cacheMapp[rep].getServletName();
                urlPattern = cacheMapp[rep].getUrlPattern();
                timeout = cacheMapp[rep].getTimeout();
                httpMethods = cacheMapp[rep].getHttpMethod();
                cacheHelperRef = cacheMapp[rep].getCacheHelperRef();

                if(servletName !=null){
                      if(validServletName(servletName,descriptor)){

                      result.passed(smh.getLocalString
					  (getClass().getName() + ".passed",
					   "PASSED [AS-WEB cache-mapping] servlet-name  [ {0} ] properly defined.",
					   new Object[] {servletName}));

                      }else{

		        result.failed(smh.getLocalString
					   (getClass().getName() + ".failed",
					    "FAILED [AS-WEB cache-mapping] servlet-name [ {0} ], does not exist in the web application.",
                                            new Object[] {servletName}));
		        oneFailed = true;

                      }

                }
                else if(urlPattern !=null)
                {
                      if(validURL(urlPattern)){

                      result.passed(smh.getLocalString
					  (getClass().getName() + ".passed1",
					   "PASSED [AS-WEB cache-mapping] url-pattern [ {0} ] properly defined.",
					   new Object[] {urlPattern}));

                      }else{

		      result.failed(smh.getLocalString
					   (getClass().getName() + ".failed1",
					    "FAILED [AS-WEB cache-mapping] url-pattern [ {0} ], does not exist in  the web application.",
                                            new Object[] {urlPattern}));
		      oneFailed = true;
                      }

                }
               
                if(cacheHelperRef !=null){
                    //test cache-helper-ref
                    if(validCacheHelperRef(cacheHelperRef,cache)){
                        
                    result.passed(smh.getLocalString
					  (getClass().getName() + ".passed2",
					   "PASSED [AS-WEB cache-mapping] cache-helper-ref element [ {0} ]  defined properly.",
					   new Object[] {cacheHelperRef}));

                    }
                    else{
                    oneFailed = true;
                    result.failed(smh.getLocalString
                                      (getClass().getName() + ".failed2",
                                      "FAILED [AS-WEB cache-mapping] cache-helper-ref [ {0} ] not valid, either empty or  cache-helper not defined for it.",
                                      new Object[] {cacheHelperRef}));

                    }
                    
                } else {
                   
                    if(timeout != null){
                    String timeoutName=cacheMapp[rep].getAttributeValue("Timeout","Name"); 
                    
                    if(validTimeout(timeout,timeoutName)){
                    
		    result.passed(smh.getLocalString
					   (getClass().getName() + ".passed3",
					    "PASSED [AS-WEB cache-mapping] timeout element [ {0} ] properly defined.",
                                            new Object[] {new Integer(timeout)}));

                    }
                    else{
                    oneFailed = true;
                    result.failed(smh.getLocalString
                                      (getClass().getName() + ".failed3",
                                      "FAILED [AS-WEB cache-mapping] timeout element [{0}] must be a Long ( Not less than -1 and not more that MAX_LONG) and its name attribute [{1}] can not be empty/null.",
                                      new Object[] {timeout,timeoutName}));



                    }
                    }
                    //<addition author="irfan@sun.com" [bug/rfe]-id="4706026" >
                    if(cacheMapp[rep].isRefreshField())
                    {
                        String cacheMapName = null;
                        if(cacheMapp[rep].getServletName()!=null)
                            cacheMapName = cacheMapp[rep].getServletName();
                        else
                            cacheMapName = cacheMapp[rep].getUrlPattern();
                        String name = cacheMapp[rep].getAttributeValue(cacheMapp[rep].REFRESH_FIELD,"name");
                        if(name!=null && name.length()==0)
                        {
                            result.failed(smh.getLocalString(getClass().getName()+".failed3a",
                                "FAILED [AS-WEB cache-mapping] for {0}, refresh-field name [{1}] cannot be empty/null string",
                                new Object[]{cacheMapName,name}));
                            oneFailed = true;
                        }
                        else
                        {
                            result.passed(smh.getLocalString(getClass().getName()+".passed3a",
                                "PASSED [AS-WEB cache-mapping] for {0}, refresh-field name [{1}] has been furnished",
                                new Object[]{cacheMapName,name}));
                        }
                    }
                    //</addition>
                    
                    if(checkHTTPMethodList(httpMethods,result,compName,descriptor)){

                    }
                    else{
                    oneFailed = true;
                    result.failed(smh.getLocalString
                        (getClass().getName() + ".failed4",
                        "FAILED [AS-WEB cache-mapping] http-method - List of HTTP methods is not proper, "+
                        " atleast one of the method name in the list is empty/null "));

                    }

                //<addition author="irfan@sun.com" [bug/rfe]-id="4706026" >
                int numKeyFields = cacheMapp[rep].sizeKeyField();
                if(numKeyFields>0)
                    testKeyFields(cacheMapp[rep],result,oneFailed);
                //</addition>
                    
                              
                }

            }     

        } else {
            notApp = true;
	    result.notApplicable(smh.getLocalString
                                 (getClass().getName() + ".notApplicable",
                                  "NOT APPLICABLE [AS-WEB cache-mapping] element not defined",
                                  new Object[] {descriptor.getName()}));
        }
        if (oneFailed) {
            result.setStatus(Result.FAILED);
        }else if(oneWarning) {
            result.setStatus(Result.WARNING);
        } else if(notApp) {
            result.setStatus(Result.NOT_APPLICABLE);
        }else {
            result.setStatus(Result.PASSED);
        }
	return result;
    }


    boolean validURL(String URL){
          boolean valid=false;
          if (URL != null) {
              if ((URL.startsWith("/")) ||
                ((URL.startsWith("/")) && (URL.endsWith("/*"))) ||
                (URL.startsWith("*."))) {
                            valid = true;
              }
          }
          return valid;
    }

    boolean validServletName(String servletName, WebBundleDescriptor descriptor){
          boolean valid=false;
          if (servletName != null && servletName.length() != 0) {
              Set servlets = descriptor.getServletDescriptors();
                    Iterator itr = servlets.iterator();
                    // test the servlets in this .war
                    while (itr.hasNext()) {
                        ServletDescriptor servlet = (ServletDescriptor) itr.next();
                        String thisServletName = servlet.getCanonicalName();
			if (servletName.equals(thisServletName)) {
                            valid = true;
                            break;
                        }
                    }

          }
          return valid;
    }

    boolean validTimeout(String timeout,String timeoutName){
          boolean valid=false;
          if (timeout != null) {
              try{
                  long timeoutValue = Long.parseLong(timeout);
                  if(timeoutValue >= -1 && timeoutValue <= Long.MAX_VALUE){
                    //if(Integer.parseInt(timeout) >= -1)  {      //4705932      
                        //check the name is non-empty      
                        if(timeoutName !=null && timeoutName.length() != 0)
                                valid = true;
                    }
              }  catch(NumberFormatException exception){ 
                  //nothing required
              }
             
          } else {//since optional field
               valid = true;
          }
          return valid;
    }

    boolean checkHTTPMethodList(String[] httpMethods, Result result, WebComponentNameConstructor compName,WebBundleDescriptor descriptor ){

          boolean valid=true;
          if (httpMethods != null) {
             for(int rep=0;rep < httpMethods.length;rep++){
                if(httpMethods[rep]!=null &&  !(httpMethods[rep].trim().equals("")))
                 {
                    if((httpMethods[rep].equalsIgnoreCase("GET")
                      || httpMethods[rep].equalsIgnoreCase("POST") || httpMethods[rep].equalsIgnoreCase("HEAD")))
                    {

                      result.passed(smh.getLocalString
					  (getClass().getName() + ".passed4",
					   "PASSED [AS-WEB cache-mapping ] http-method  [ {0} ] properly defined in the WEB DD.",
					   new Object[] {httpMethods[rep]}));
                    }else{

                    oneWarning = true;
                    result.warning(smh.getLocalString
					   (getClass().getName() + ".warning",
					    "WARNING [AS-WEB cache-mapping] http-method name [ {0} ] present, suggested to be GET | POST | HEAD.",
					    new Object[] {httpMethods[rep]}));



                    }

                 }
                else{

                result.failed(smh.getLocalString
					   (getClass().getName() + ".failed5",
					    "FAILED [AS-WEB cache-mapping] http-method name [ {0} ] is invalid, it should be GET | POST | HEAD.",
					    new Object[] {httpMethods[rep]}));

                valid=false;
                }

             }

          }

          return valid;
    }
    
    boolean validCacheHelperRef(String helperRef, Cache cache){
          boolean valid=false;
          if (helperRef.length() != 0) {
            
            CacheHelper[] helperClasses=null;
            CacheHelper helperClass=null;
            String name=null;
        
            if (cache != null )
            helperClasses=cache.getCacheHelper();
          
            if (cache != null && helperClasses !=null) {
       
                for(int rep=0;rep < helperClasses.length;rep++){
                    helperClass=helperClasses[rep]; 
                    if(helperClass==null)
                        continue;
		    name = helperClass.getAttributeValue("name");
                    //System.out.println("checking ref for  : " +name);
                    if(helperRef.equals(name)){
                        //System.out.println("Valid ref found : " +name);
                        valid=true; 
                        break;
                    }  
                }
            }

          }
          return valid;
    }
    
    //<addition author="irfan@sun.com" [bug/rfe]-id="4706026" >
    public void testKeyFields(CacheMapping cacheMap, Result result, boolean oneFailed)
    {
        String cacheMapName = null;
        if(cacheMap.getServletName()!=null && cacheMap.getServletName().length()!=0)
            cacheMapName = cacheMap.getServletName();
        else 
            cacheMapName = cacheMap.getUrlPattern();
            
        for(int i=0;i<cacheMap.sizeKeyField();i++)
        {
            if(cacheMap.isKeyField(i))
            {
                String name = cacheMap.getAttributeValue(cacheMap.KEY_FIELD,i,"name");
                //testing name attribute
                if(name!=null && name.length()==0)
                {
                    result.failed(smh.getLocalString(getClass().getName()+".failed6",
                       "FAILED [AS-WEB cache-mapping] for {0}, key-field #{1}, name cannot be an empty string",
                        new Object[]{cacheMapName,new Integer(i)}));
                    oneFailed = true;
                }
                else
                {
                    result.passed(smh.getLocalString(getClass().getName()+".passed5",
                        "PASSED [AS-WEB cache-mapping] for {0}, key-field #{1} name value furnished",
                        new Object[]{cacheMapName,new Integer(i)}));
                }
            }
        }
    }
    //</addition>
}

