package com.sun.enterprise.tools.verifier.tests.ejb.runtime;

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
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;

import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;

import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;

import com.sun.enterprise.deployment.runtime.IASEjbExtraDescriptors;

/** ejb [0,n]
 *    jms-max-messages-load ? [String]
 *
 * The jms-max-messages-load specifies the maximum number of messages to
 * load into a JMS Session.
 * It is valid only for MDBs
 * The value should be between 1 and MAX_INT
 * @author Irfan Ahmed
 */
public class ASEjbJMSMaxMessagesLoad extends EjbTest implements EjbCheck { 

    public Result check(EjbDescriptor descriptor) 
    {
	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        
        try{
            IASEjbExtraDescriptors iasEjbDescriptor = descriptor.getIASEjbExtraDescriptors();
            int value = iasEjbDescriptor.getJmsMaxMessagesLoad();
            Integer jmsMaxMsgs = new Integer(value);
            if (jmsMaxMsgs != null){
                if(value<1 || value>Integer.MAX_VALUE){
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString(getClass().getName()+".failed",
                        "FAILED [AS-EJB ejb] : {0} is not a valid value for jms-max-messages-load. It should be " + '\n' + 
                        "between 0 and MAX_INT", new Object[]{new Integer(value)}));
                }else{
                    addGoodDetails(result, compName);
                    result.passed(smh.getLocalString(getClass().getName()+".passed",
                        "PASSED [AS-EJB ejb] : jms-max-messages-load is {0}", new Object[]{jmsMaxMsgs}));
                }
            }else{
                if(descriptor instanceof EjbMessageBeanDescriptor){
                    //<addition author="irfan@sun.com" [bug/rfe]-id="4724447" >
                    //Change in message output ms->jms //
                    addWarningDetails(result, compName);
                    result.warning(smh.getLocalString(getClass().getName()+".warning",
                        "WARNING [AS-EJB ejb] : jms-max-messages-load should be defined for MDBs"));
                }else{
                    addNaDetails(result, compName);
                    result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                        "NOT APPLICABLE [AS-EJB ejb] : jms-max-messages-load element is not defined"));
                }
            }
        }catch(Exception ex){
            addErrorDetails(result, compName);
            result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun",
                  "NOT RUN [AS-EJB] : Could not create a descriptor object"));
        }
        return result;
    }
}
