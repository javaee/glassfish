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

import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

import java.lang.reflect.Method;

/**
 * If the stateless session bean instance has an ejbCreate method, the container
 * treats the ejbCreate method as the instance’s PostConstruct method, and, in
 * this case, the PostConstruct annotation (or deployment descriptor metadata)
 * can only be applied to the bean’s ejbCreate method.
 * 
 * @author Vikas Awasthi
 */
public class EJBCreatePostConstruct extends SessionBeanTest {

    public Result check(EjbSessionDescriptor descriptor) {

        if(descriptor.isStateless() && 
                descriptor.hasPostConstructMethod() && 
                hasEJBCreateMethod(descriptor.getEjbClassName())) {
            for (LifecycleCallbackDescriptor callbackDesc : 
                    descriptor.getPostConstructDescriptors()) {
                String cmName = callbackDesc.getLifecycleCallbackMethod();
                if(!cmName.contains("ejbCreate")) {
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                            (getClass().getName()+".failed",
                                    "Wrong postconstruct method [ {0} ]",
                                    new Object[] {cmName}));
                }
            }
        }
        
        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                            (getClass().getName()+".passed",
                            "Valid postcontruct method(s) in Bean"));
        }
        
        return result;
    }
    
    private boolean hasEJBCreateMethod(String beanClassName) {
        try {
            ClassLoader jcl = getVerifierContext().getClassLoader();
            Class bean = Class.forName(beanClassName, false, jcl);
            Method[] methods = bean.getMethods();
            for (Method method : methods) 
                if(method.getName().contains("ejbCreate"))
                    return true;
        } catch (ClassNotFoundException e) {}// will be caught in other tests
        return false;
    }
}
