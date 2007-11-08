package com.sun.enterprise.tools.verifier.tests.ejb.ias;

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
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;

import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;

import com.sun.enterprise.tools.common.dd.ejb.SunEjbJar;
import com.sun.enterprise.tools.common.dd.ejb.Ejb;
import com.sun.enterprise.tools.common.dd.ejb.Cmp;
import com.sun.enterprise.tools.common.dd.ejb.Finder;
import com.sun.enterprise.tools.common.dd.ejb.OneOneFinders;
import java.io.*;
import java.util.jar.*;
import java.util.zip.*;

/** ejb [0,n]
 *      cmp ?
 *          mapping-properties ? [String]
 *          is-one-one-cmp ? [String]
 *          one-one-finders ?
 *              finder [1,n]
 *                  method-name [String]
 *                  query-params ? [String]
 *                  query-filter ? [String]
 *                  query-variables ? [String]
 *
 * The cmp element describes the runtime information for a CMP enitity bean.
 * mapping-properties - The vendor specific O/R mapping file
 * is-one-one-cmp - boolean field used to identify CMP 1.1 descriptors
 * one-one-finders - The finders for CMP 1.1
 *
 * @author Irfan Ahmed
 */
public class ASEjbCMP extends EjbTest implements EjbCheck {

    public boolean oneFailed = false;
    public boolean oneWarning = false;
    public Result check(EjbDescriptor descriptor)
    {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = new ComponentNameConstructor(descriptor);

        SunEjbJar ejbJar = descriptor.getEjbBundleDescriptor().getIasEjbObject();
        Ejb testCase = null;
        if(ejbJar!=null)
        {
            testCase = super.getEjb(descriptor.getName(),ejbJar);
            Cmp cmpBean = testCase.getCmp();
            if(cmpBean != null)
            {
                //mapping-properties
                String mappingProps = cmpBean.getMappingProperties();
                if(mappingProps == null)
                {
				    oneWarning = true;
                    result.warning(smh.getLocalString(getClass().getName()+".warning",
                        "WARNING [AS-EJB cmp] : mapping-properties Element is not defined"));
                }
                else
                {
                    if(mappingProps.length()==0) 
                    {
                        oneFailed = true;
                        result.failed(smh.getLocalString(getClass().getName()+".failed",
                            "FAILED [AS-EJB cmp] : mapping-properties field must contain a vaild non-empty value"));
                    }
                    else//4690436
                    {   File f = Verifier.getJarFile(((EjbBundleArchivist) descriptor.getEjbBundleDescriptor().getArchivist()).getEjbJarFile().getName());
                        JarFile jarFile = null;
                        ZipEntry deploymentEntry=null;
                        try {
                              jarFile = new JarFile(f);
                              if(jarFile!=null)
                              deploymentEntry = jarFile.getEntry(mappingProps);
                              

                        }
                        catch(IOException e){}
                        finally{
                           try{  if(jarFile!=null) jarFile.close();} 
                           catch(IOException e){}
                        }

                        if(deploymentEntry !=null){
                        result.passed(smh.getLocalString(getClass().getName()+".passed",
                            "PASSED [AS-EJB cmp] : mapping-properties file is {0}",
                            new Object[]{mappingProps}));
                        }
                        else{
                        //invalid entry
                        result.failed(smh.getLocalString(getClass().getName()+".failed",
                            "FAILED [AS-EJB cmp] : mapping-properties field must contain a vaild non-empty value"));


                        }
                    }
                }

                //is-one-one-cmp
                String isOneOne = cmpBean.getIsOneOneCmp();
                if(isOneOne == null)
                {
					oneWarning = true;
                    result.warning(smh.getLocalString(getClass().getName()+".warning1",
                        "WARNING [AS-EJB cmp] : is-one-one-cmp Element is not defined"));
                }
                else
                {
                    if(isOneOne.length()==0)
                    {
                        oneFailed = true;
                        result.failed(smh.getLocalString(getClass().getName()+".failed1",
                            "FAILED [AS-EJB cmp] : is-one-one-cmp cannot be an empty string. It can be either true or false"));
                    }
                    else
                    {
                        if(!isOneOne.equals("true") && !isOneOne.equals("false"))
                        {
                            oneFailed = true;
                            result.failed(smh.getLocalString(getClass().getName()+".failed2",
                                "FAILED [AS-EJB cmp] : is-one-one-cmp cannot be {0}. It can either be true or false",
                                new Object[]{isOneOne}));
                        }
                        else
                        {
                            result.passed(smh.getLocalString(getClass().getName()+".passed1",
                                "PASSED [AS-EJB cmp] : is-one-one-cmp is {0}",
                                new Object[]{isOneOne}));
                        }
                    }
                }

                //one-one-finders
                OneOneFinders finders = cmpBean.getOneOneFinders();
                if(finders!=null)
                {
                    testFinders(finders,result);
                }
                else
                {
					oneWarning = true;
                    result.warning(smh.getLocalString(getClass().getName()+".warning2",
                        "WARNING [AS-EJB cmp] : one-one-finders Element is not defined"));
                }

                if(oneFailed)
                    result.setStatus(Result.FAILED);
				else if(oneWarning)
				    result.setStatus(Result.WARNING);
            }
            else
                result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                    "NOT APPLICABLE [AS-EJB cmp] : {0} is not a CMP Entity Bean.",
                    new Object[] {testCase.getEjbName()}));
        }
        else
        {
            result.failed(smh.getLocalString(getClass().getName()+".notRun",
                "NOT RUN [AS-EJB cmp] Could not create SunEjbJar Object."));
        }
        return result;
    }

    public void testFinders(OneOneFinders finders, Result result)
    {
        Finder finder[] = finders.getFinder();
        for (int i=0;i<finder.length;i++)
        {
            //method-name
            String methodName = finder[i].getMethodName();
            if(methodName.length()==0)
            {
                oneFailed = true;
                result.failed(smh.getLocalString(getClass().getName()+".failed3",
                    "FAILED [AS-EJB finder] : method-name cannot be an empty string."));
            }
            else
            {
                //TODO Should the method name exist in Home interfaces
                result.passed(smh.getLocalString(getClass().getName()+".passed2",
                    "PASSED [AS-EJB finder] : method-name is {0}",
                    new Object[]{methodName}));
            }

            //query-params
            String value = finder[i].getQueryParams();
            testQuery(value,result,"finder","query-params");

            //query-filter
            value = finder[i].getQueryFilter();
            testQuery(value,result,"finder","query-filter");

            //query-variables
            value = finder[i].getQueryVariables();
            testQuery(value,result,"finder","query-variables");
        }

    }

    public void testQuery(String value, Result result,String parent, String child)
    {
        if(value == null)
        {
			oneWarning = true;
            result.warning(smh.getLocalString(getClass().getName()+".warning3",
                "WARNING [AS-EJB {0}] : {1} Element is not defined",
                new Object[]{parent,child}));
        }
        else
        {
            if(value.length()==0)
            {
                oneFailed = true;
                result.failed(smh.getLocalString(getClass().getName()+".failed4",
                    "FAILED [AS-EJB {0}] : {1} cannot be an empty string",
                    new Object[]{parent,child}));
            }
            else
            {
                result.passed(smh.getLocalString(getClass().getName()+".passed3",
                    "PASSED [AS-EJB {0}] : {1} is/are {2}",
                    new Object[]{parent, child, value}));
            }
        }
    }
}
