package com.sun.enterprise.tools.verifier.tests.ejb.ias.resource;
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
import com.sun.enterprise.tools.common.dd.ejb.Ejb;
import com.sun.enterprise.tools.common.dd.ResourceRef;
import com.sun.enterprise.tools.common.dd.DefaultResourcePrincipal;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;

/** ejb [0,n]
 *   resource-ref [0,n]
 *       res-ref-name [String]
 *       jndi-name [String]
 *       default-resource-principal ?
 *           name [String]
 *           password [String]
 *
 * The jndi-name specifies the JNDI name to which this resource is binded
 * The jndi-name should not be null.
 * The jndi-name should map to the correct subcontext and hence start with the
 * valid subcontext
 *    URL url/
 *    Mail mail/
 *    JDBC jdbc/
 *    JMS jms/
 *
 * @author Irfan Ahmed
 */
public class ASEjbRRefJndiName extends ASEjbResRef { 

    public Result check(EjbDescriptor descriptor) 
    {
	Result result = getInitializedResult();
	ComponentNameConstructor compName = new ComponentNameConstructor(descriptor);

        SunEjbJar ejbJar = descriptor.getEjbBundleDescriptor().getIasEjbObject();
        boolean oneFailed = false;
        boolean oneWarning = false;
        
        if(ejbJar!=null)
        {
            Ejb ejbs[] = ejbJar.getEnterpriseBeans().getEjb();
            Ejb testCase = null;
            for(int i=0;i<ejbs.length;i++)
            {
                if(ejbs[i].getEjbName().equals(descriptor.getName()))
                {
                    testCase = ejbs[i];
                    break;
                }
            }
            
            ResourceRef resRefs[] = testCase.getResourceRef();
            ResourceReferenceDescriptor resDesc;
            if(resRefs.length > 0)
            {
                for(int i=0;i<resRefs.length;i++)
                {
                    String refName = resRefs[i].getResRefName();
                    String refJndiName = resRefs[i].getJndiName();
                    try
                    {
                        resDesc = descriptor.getResourceReferenceByName(refName);
                        String type = resDesc.getType();
						if(refJndiName.equals(""))
						{
                            oneFailed = true;
						    result.failed(smh.getLocalString(getClass().getName()+".failed1","FAILED [AS-EJB resource-ref]: jndi-name is blank string"));
						}
						else
						{
                        if(type.indexOf("javax.jms")>-1) //jms resource
                        {
                            if(refJndiName.startsWith("jms/"))
                                result.passed(smh.getLocalString(getClass().getName()+".passed1",
                                    "PASSED [AS-EJB resource-ref] : jndi-name {0} is valid", new Object[]{refJndiName}));
                            else
							{
							    oneWarning = true;
                                result.warning(smh.getLocalString(getClass().getName()+".warning1",
                                    "WARNING [AS-EJB resource-ref] : jndi-name is \"{0}\" for resource type \"{1}\"." + 
                                    "The preferred jndi-name for JMS resources should start with jms/",
                                    new Object[]{refJndiName,type}));
							}
                        }
                        else if(type.indexOf("javax.sql")>-1) //jdbc resource
                        {
                            if(refJndiName.startsWith("jdbc/"))
                                result.passed(smh.getLocalString(getClass().getName()+".passed1",
                                    "PASSED [AS-EJB resource-ref] : jndi-name {0} is valid", new Object[]{refJndiName}));
                            else
                            {
                                oneWarning = true;
                                result.warning(smh.getLocalString(getClass().getName()+".warning2",
                                    "WARNING [AS-EJB resource-ref] : jndi-name is \"{0}\" for resource type \"{1}\"." + 
                                    "The preferred jndi-name for JDBC resources should start with jdbc/",
                                    new Object[]{refJndiName,type}));
                            }
                        }
                        else if(type.indexOf("java.net")>-1) //url resource
                        {
                            if(refJndiName.startsWith("http://"))//FIX should it start with http:// or url/http://
                                result.passed(smh.getLocalString(getClass().getName()+".passed1",
                                    "PASSED [AS-EJB resource-ref] : jndi-name {0} is valid", new Object[]{refJndiName}));
                            else
                            {
                                oneWarning = true;
                                result.warning(smh.getLocalString(getClass().getName()+".warning3",
                                    "WARNING [AS-EJB resource-ref] : jndi-name is \"{0}\" for resource type \"{1}\". " + 
                                    "The preferred jndi-name for URL resources should start with a url",
                                    new Object[]{refJndiName,type}));
                            }
                        }
                        else if(type.indexOf("javax.mail")>-1) //jms resource
                        {
                            if(refJndiName.startsWith("mail/"))
                                result.passed(smh.getLocalString(getClass().getName()+".passed1",
                                    "PASSED [AS-EJB resource-ref] : jndi-name {0} is valid", new Object[]{refJndiName}));
                            else
                            {
                                oneWarning = true;
                                result.warning(smh.getLocalString(getClass().getName()+".warning4",
                                    "WARNING [AS-EJB resource-ref] : jndi-name is \"{0}\" for resource type \"{1}\"." + 
                                    "The preferred jndi-name for MAIL resources should start with mail/",
                                    new Object[]{refJndiName,type}));
                            }
                        }
                        else
						{
					        result.passed(smh.getLocalString(getClass().getName()+".passed1","PASSED [AS-EJB resource-ref]: jndi-name {0} is valid",new Object[]{refJndiName}));
                        }
                        }
                    }
                    catch(IllegalArgumentException iex)
                    {
					    oneFailed = true;
                        result.failed(smh.getLocalString(getClass().getName()+".failed2",
                            "FAILED [AS-EJB resource-ref] : res-ref with res-ref-name {0} is not defined in the ejb-jar.xml",
                            new Object[]{refName}));
                    }
                }
            }
            else
            {
                 result.notApplicable(smh.getLocalString
                         (getClass().getName() + ".notApplicable",
                          "NOT APPLICABLE [AS-EJB] : {0} Does not define any resource-ref Elements",
                          new Object[] {descriptor.getName()}));
            }
        }
        else
        {
            result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun",
                  "NOT RUN [AS-EJB] : Could not create a SunEjbJar object"));
        }
		if(oneWarning)
            result.setStatus(Result.WARNING);
		if(oneFailed)
            result.setStatus(Result.FAILED);
        return result;
    }
}

