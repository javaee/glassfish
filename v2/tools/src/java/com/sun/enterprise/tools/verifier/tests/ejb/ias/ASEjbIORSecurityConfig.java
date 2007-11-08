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
import com.sun.enterprise.tools.common.dd.ejb.Ejb;
import com.sun.enterprise.tools.common.dd.ejb.TransportConfig;
import com.sun.enterprise.tools.common.dd.ejb.AsContext;
import com.sun.enterprise.tools.common.dd.ejb.SasContext;
import com.sun.enterprise.tools.common.dd.ejb.IorSecurityConfig;

/** ejb [0,n]
 *    ior-security-config ?
 *        transport-config?
 *            integrity [String]
 *            confidentiality [String]
 *            establish-trust-in-client [String]
 *            establish-trust-in-target [String]
 *        as-context?
 *            auth-method [String]
 *            realm [String]
 *            required [String]
 *        sas-context?
 *            caller-propagation [String]
 *
 * The tag describes the security configuration for the IOR
 * @author Irfan Ahmed
 */
public class ASEjbIORSecurityConfig extends EjbTest implements EjbCheck { 
    boolean oneFailed = false;

    /** The function that performs the test.
     *
     * @param descriptor EjbDescriptor object representing the bean.
     */    
    public Result check(EjbDescriptor descriptor) 
    {
	Result result = getInitializedResult();
	ComponentNameConstructor compName = new ComponentNameConstructor(descriptor);

        SunEjbJar ejbJar = descriptor.getEjbBundleDescriptor().getIasEjbObject();
        
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
            
            IorSecurityConfig iorSec = testCase.getIorSecurityConfig();
            if(iorSec == null)
            {
                result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                    "NOT APPLICABLE [AS-EJB ior-security-config] : ior-security-config Element not defined"));
            }
            else
            {
                TransportConfig tranConfig = iorSec.getTransportConfig();
                if(tranConfig != null)
                    testTranConfig(tranConfig,result);
                else
                {
                    result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                        "NOT APPLICABLE [AS-EJB ior-security-config] : transport-config Element not defined"));
                }
                
                AsContext asContext = iorSec.getAsContext();
                if(asContext != null)
                    testAsContext(asContext,result);
                else
                {
                    result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                        "NOT APPLICABLE [AS-EJB ior-security-config] : as-context Element not defined"));
                }
                
                SasContext sasContext = iorSec.getSasContext();
                if(sasContext != null)
                    testSasContext(sasContext,result);
                else
                {
                    result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                        "NOT APPLICABLE [AS-EJB ior-security-config] : sas-context Element not defined"));
                }
                
            }
            if(oneFailed)
                result.setStatus(Result.FAILED);
        }
        else
        {
            result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun",
                  "NOT RUN [AS-EJB] : Could not create an SunEjbJar object"));
        }
        return result;
    }
    
    /** This function tests the <transport-config> tag for valid values
     *
     * @param tConfig TransportConfig - The object representing the <transport-config> tag
     * @param result Result - The Result object
     */    
    protected void testTranConfig(TransportConfig tConfig , Result result)
    {
        //integrity
        String integrity = tConfig.getIntegrity();
        testMsgs(integrity,result,"transport-config","integrity");
        
        //confidentiality
        String confdn = tConfig.getConfidentiality();
        testMsgs(confdn,result,"transport-config","confidentiality");
        
        //establish-trust-in-target
        String trustTarget = tConfig.getEstablishTrustInTarget();
        testMsgs(trustTarget,result,"transport-config","extablish-trust-in-target");
        
        //establish-trust-in-client
        String trustClient = tConfig.getEstablishTrustInClient();
        testMsgs(trustClient,result,"transport-config","establish-trust-in-client");
    }
    
    /** The function tests the <as-context> tag in <transport-config> for
     * valid values
     * @param aContext AsContext object representing the <as-context> tag
     * @param result Result object
     */    
    protected void testAsContext(AsContext aContext, Result result)
    {
        //auth-method
        String value = aContext.getAuthMethod();
        if(value.length()==0)
        {
            oneFailed = true;
            result.failed(smh.getLocalString(getClass().getName()+".failedAsContextAuthMethod",
                "FAILED [AS-EJB as-context] : auth-method cannotb be an empty string"));
        }
        else
        {
            if(value.equals("USERNAME_PASSWORD"))
            {
                result.passed(smh.getLocalString(getClass().getName()+".passedAsContextAuthMethod",
                    "PASSED [AS-EJB as-context] : auth-method is {0}", new Object[] {value}));
            }
            else
            {
                oneFailed = true;
                result.failed(smh.getLocalString(getClass().getName()+".failedAsContextAuthMethod1",
                    "FAILED [AS-EJB as-context] : auth-method cannot be {0}. It can only be USERNAME_PASSWORD"
                    ,new Object[]{value}));
            }
        }
        
        //realm
        value = aContext.getRealm();
        if(value.length()==0)
        {
            oneFailed = true;
            result.failed(smh.getLocalString(getClass().getName()+".failedAsContextRealm",
                "FAILED [AS-EJB as-context] : realm cannot be an empty string"));
        }
        else
        {
            result.passed(smh.getLocalString(getClass().getName()+".passedAsContextRealm",
                "PASSED [AS-EJB as-context] : realm is {0}", new Object[] {value}));
        }
        
        //required
        value = aContext.getRequired();
        if(value.length()==0)
        {
            oneFailed = true;
            result.failed(smh.getLocalString(getClass().getName()+".failedAsContextRequired",
                "FAILED [AS-EJB as-context] : required cannot be an empty string"));
        }
        else
        {
            if(value.equals("true") || value.equals("false"))
            {
                result.passed(smh.getLocalString(getClass().getName()+".passedAsContextRequired",
                    "PASSED [AS-EJB as-context] : required is {0}", new Object[] {value}));
            }
            else
            {
                oneFailed = true;
                result.failed(smh.getLocalString(getClass().getName()+".failedAsContextRequired1",
                    "FAILED [AS-EJB as-context] : required cannot be {0}. It can only be true or false"
                    ,new Object[]{value}));
            }
        }
        
    }
    
    /**
     * @param sContext
     * @param result  */    
    protected void testSasContext(SasContext sContext, Result result)
    {
        String caller = sContext.getCallerPropagation();
        testMsgs(caller,result,"sas-context","caller-propagation");
    }
    
    private void testMsgs(String tCase, Result result, String parentElement, String testElement)
    {
        if(tCase.length()==0)
        {
            oneFailed = true;
            result.failed(smh.getLocalString(getClass().getName()+".failedTestMsg",
                "FAILED [AS-EJB " + parentElement + "] : " + testElement + " cannot be an empty String"));
        }
        else
        {
            if(!tCase.equals("NONE") && !tCase.equals("SUPPORTED") 
                && !tCase.equals("REQUIRED"))
            {
                oneFailed = true;
                result.failed(smh.getLocalString(getClass().getName()+".failedTestMsg",
                    "FAILED [AS-EJB " + parentElement+"] : "+testElement+" cannot be {0}. It can be either NONE, SUPPORTED or REQUIRED",
                    new Object[]{tCase}));
            }
            else
                result.passed(smh.getLocalString(getClass().getName()+".passedTestMsg",
                    "PASSED [AS-EJB "+ parentElement+"] : " + testElement +" is {0}", new Object[]{tCase}));
        }
    }
    
}
