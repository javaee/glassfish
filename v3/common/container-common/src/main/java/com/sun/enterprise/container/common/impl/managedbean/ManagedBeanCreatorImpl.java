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
package com.sun.enterprise.container.common.impl.managedbean;

import org.jvnet.hk2.component.Habitat;
import com.sun.enterprise.deployment.ManagedBeanDescriptor;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor.CallbackType;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import java.util.Set;

import org.glassfish.api.interceptor.JavaEEInterceptorBuilder;
import org.glassfish.api.interceptor.InterceptorInfo;
import org.glassfish.api.interceptor.InterceptorInvoker;

/**
 */


public class ManagedBeanCreatorImpl {

    private Habitat habitat;
    private InjectionManager injectionMgr;

    public ManagedBeanCreatorImpl(Habitat h) {
        habitat = h;
        injectionMgr = habitat.getByContract(InjectionManager.class);
    }

    public Object createManagedBean(ManagedBeanDescriptor desc) throws Exception {

        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        // Create managed bean instance
        Class managedBeanClass = loader.loadClass(desc.getBeanClassName());
        Object managedBean = managedBeanClass.newInstance();

        // In the simple case the actual managed bean instance is given to the
        // application.   However, if there are any interceptor classes associated
        // with the managed bean or any around invoke methods defined on the
        // managed bean class, a proxy will be returned to the caller.
        Object callerObject = managedBean;

        Set<String> interceptorClasses = desc.getAllInterceptorClasses();


        if( interceptorClasses.isEmpty() && !desc.hasAroundInvokeMethod()) {
            // Inject instance and have injection manager call PostConstruct
            injectionMgr.injectInstance(managedBean, desc.getGlobalJndiName(), true);

            desc.addBeanInstanceInfo(managedBean);

        } else {
            JavaEEInterceptorBuilder interceptorBuilder = (JavaEEInterceptorBuilder)
                desc.getInterceptorBuilder();

            InterceptorInvoker interceptorInvoker =
                    interceptorBuilder.createInvoker(managedBean);

            callerObject = interceptorInvoker.getProxy();

            Object[] interceptorInstances = interceptorInvoker.getInterceptorInstances();

             // Inject instances, but use injection invoker for PostConstruct
            injectionMgr.injectInstance(managedBean, desc.getGlobalJndiName(), false);

            // Inject interceptor instances
            for(int i = 0; i < interceptorInstances.length; i++) {
                 // Inject instance and call PostConstruct method(s).
                injectionMgr.injectInstance(interceptorInstances[i], desc.getGlobalJndiName(), false);
            }

            interceptorInvoker.invokePostConstruct();

            desc.addBeanInstanceInfo(managedBean, interceptorInvoker);

        }


        // TODO Create proxy if managed bean has interceptors    

        return callerObject;

    }
    
}
