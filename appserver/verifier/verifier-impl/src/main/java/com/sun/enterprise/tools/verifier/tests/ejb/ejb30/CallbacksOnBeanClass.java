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

package com.sun.enterprise.tools.verifier.tests.ejb.ejb30;

import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.util.Set;

/**
 * If the PostConstruct lifecycle callback interceptor method is the ejbCreate 
 * method, if the PreDestroy lifecycle callback interceptor method is the 
 * ejbRemove method, if the PostActivate lifecycle callback interceptor method 
 * is the ejbActivate method, or if the Pre-Passivate lifecycle callback 
 * interceptor method is the ejbPassivate method, these callback methods must 
 * be implemented on the bean class itself (or on its superclasses).
 * 
 * @author Vikas Awasthi
 */
public class CallbacksOnBeanClass extends EjbTest {

    Result result = null;
    ComponentNameConstructor compName = null;
    
    public Result check(EjbDescriptor descriptor) {
        result = getInitializedResult();
        compName = getVerifierContext().getComponentNameConstructor();
        
        Set<EjbInterceptor> interceptors = descriptor.getInterceptorClasses();
        for (EjbInterceptor interceptor : interceptors) {
            if (interceptor.hasCallbackDescriptor(
                    LifecycleCallbackDescriptor.CallbackType.POST_ACTIVATE)) {
                Set<LifecycleCallbackDescriptor> callBackDescs = 
                        interceptor.getCallbackDescriptors(
                                LifecycleCallbackDescriptor.CallbackType.POST_ACTIVATE);
                reportError(callBackDescs, "ejbActivate",interceptor.getInterceptorClassName());
            }
            if (interceptor.hasCallbackDescriptor(
                    LifecycleCallbackDescriptor.CallbackType.PRE_PASSIVATE)) {
                Set<LifecycleCallbackDescriptor> callBackDescs = 
                        interceptor.getCallbackDescriptors(
                                LifecycleCallbackDescriptor.CallbackType.PRE_PASSIVATE);
                reportError(callBackDescs, "ejbPassivate",interceptor.getInterceptorClassName());
            }
            if (interceptor.hasCallbackDescriptor(
                    LifecycleCallbackDescriptor.CallbackType.POST_CONSTRUCT)) {
                Set<LifecycleCallbackDescriptor> callBackDescs = 
                        interceptor.getCallbackDescriptors(
                                LifecycleCallbackDescriptor.CallbackType.POST_CONSTRUCT);
                reportError(callBackDescs, "ejbCreate",interceptor.getInterceptorClassName());
            }
            if (interceptor.hasCallbackDescriptor(
                    LifecycleCallbackDescriptor.CallbackType.PRE_DESTROY)) {
                Set<LifecycleCallbackDescriptor> callBackDescs = 
                        interceptor.getCallbackDescriptors(
                                LifecycleCallbackDescriptor.CallbackType.PRE_DESTROY);
                reportError(callBackDescs, "ejbRemove",interceptor.getInterceptorClassName());
            }
        }
        
        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                            (getClass().getName()+".passed",
                            "Valid lifecycle callback method(s)"));
        }
        return result;
    }
    
    private void reportError(Set<LifecycleCallbackDescriptor> callBackDescs, 
                             String callbackMethodName,
                             String interceptorClassName) {
        for (LifecycleCallbackDescriptor callbackDesc : callBackDescs) {
            String callbackMethod = callbackDesc.getLifecycleCallbackMethod();
            if(callbackMethod.contains(callbackMethodName)) {
                result.getFaultLocation().setFaultyClassName(interceptorClassName);
                result.getFaultLocation().setFaultyMethodName(callbackMethod);
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString
                        (getClass().getName()+".failed",
                        "Wrong method [ {0} ] in class [ {1} ]",
                        new Object[] {callbackMethod, interceptorClassName}));
            }
        }
    }
}
