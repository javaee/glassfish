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
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;

import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;

import com.sun.enterprise.tools.common.dd.ejb.SunEjbJar;
import com.sun.enterprise.tools.common.dd.ejb.EnterpriseBeans;
import com.sun.enterprise.tools.common.dd.ejb.CmpResource;
import com.sun.enterprise.tools.common.dd.DefaultResourcePrincipal;

/** enterprise-bean
 *    cmp-resource ?
 *        jndi-name [String]
 *        default-resource-principal ?
 *            name [String]
 *            password [String]
 *
 * The cmp-resource contains the database to be used for storing the cmp beans
 * in the ejb-jar.xml
 * The jndi-name should not be null and should start with jdbc/
 * @author Irfan Ahmed
 */
public class ASEntBeanCmpResource extends EjbTest implements EjbCheck { 

    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = new ComponentNameConstructor(descriptor);
        //4698046
        boolean oneFailed=false;
        boolean oneWarning=false;

        SunEjbJar ejbJar = descriptor.getEjbBundleDescriptor().getIasEjbObject();
        String ejbName = null;
        
        if(descriptor.getEjbBundleDescriptor().getTestsDone().contains(getClass().getName()))
        {
            result.setStatus(Result.NOT_RUN);
            result.addGoodDetails(smh.getLocalString(getClass().getName() + ".notRun",
                "NOT RUN [AS-EJB enterprise-beans] name test is a JAR Level Test. This test has already been run once"));
            return result;
        }
        descriptor.getEjbBundleDescriptor().setTestsDone(getClass().getName());
        
        if(ejbJar!=null)
        {
            EnterpriseBeans entBean = ejbJar.getEnterpriseBeans();
            CmpResource cmpResource = entBean.getCmpResource();
            if(cmpResource!=null)
            {
                String jndiName = cmpResource.getJndiName();
                if(jndiName.length()==0)
                {
                    result.failed(smh.getLocalString(getClass().getName()+".failed",
                        "FAILED [AS-EJB cmp-resource] : jndi-name cannot be an empty string"));
                    oneFailed=true;//4698046
                }
                else
                {
                    if(jndiName.startsWith("jdbc/")|| jndiName.startsWith("jdo/"))
                        result.passed(smh.getLocalString(getClass().getName()+".passed",
                            "PASSED [AS-EJB cmp-resource] : jndi-name is {0}",new Object[]{jndiName}));
                    else{
                        result.warning(smh.getLocalString(getClass().getName()+".warning",
                            "WARNING [AS-EJB cmp-resource] : The jndi-name  is {0}, the preferred jndi-name should start with  jdbc/ or jdo/" 
                            , new Object[]{jndiName}));
                            oneWarning=true;//4698046
                    }    
                }
                
                DefaultResourcePrincipal defPrincipal = cmpResource.getDefaultResourcePrincipal();
                if(defPrincipal!=null)
                {
                    String name = defPrincipal.getName();
                    if(name.length()==0)
                    {
                        result.failed(smh.getLocalString(getClass().getName()+".failed2",
                            "FAILED [AS-EJB default-resource-principal] :  name cannot be an empty string"));
                        oneFailed=true; //4698046   
                    }
                    else
                    {
                        result.passed(smh.getLocalString(getClass().getName()+".passed1",
                            "PASSED [AS-EJB default-resource-principal] : name is {0}",new Object[]{name}));
                    }

                    String password = defPrincipal.getPassword();
                    if(password.length()==0)
                    {
                        // <addition> srini@sun.com
                        //result.failed(smh.getLocalString(getClass().getName()+".failed3",
                          //  "FAILED [AS-EJB default-resource-principal] : password cannot be an empty string"));
                        result.warning(smh.getLocalString(getClass().getName()+".warning1",
                            "WARNING [AS-EJB default-resource-principal] : password is an empty string"));
                        // </addition>
                        oneWarning=true;//4698046
                    }
                    else
                    {
                        result.passed(smh.getLocalString(getClass().getName()+".passed2",
                            "PASSED [AS-EJB default-resource-principal] : password is  {0}",new Object[]{password}));
                    }
                }
                else
                    result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                        "NOT APPLICABLE [AS-EJB cmp-resource] : default-resource-principal Element not defined"));
            
                if(oneFailed)//4698046
                    result.setStatus(Result.FAILED);
                else if(oneWarning)
                    result.setStatus(Result.WARNING);
            
            }
            else
                result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable1",
                    "NOT APPLICABLE [AS-EJB enterprise-beans] : cmp-resource element is not defined"));
        }
        else
        {
            result.addErrorDetails(smh.getLocalString
                                   ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
            result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun1",
                  "NOT RUN [AS-EJB] : Could not create an SunEjbJar object"));
        }
        return result;
    }
}

