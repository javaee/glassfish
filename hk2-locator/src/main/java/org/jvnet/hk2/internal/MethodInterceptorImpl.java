/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package org.jvnet.hk2.internal;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ProxyCtl;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;

/**
 * @author jwells
 * @author mtaube
 */
public class MethodInterceptorImpl implements MethodHandler {
    private final static String PROXY_MORE_METHOD_NAME = "__make";
    
    private final ServiceLocatorImpl locator;
    private final ActiveDescriptor<?> descriptor;
    /** Original root node, needed for proper destruction */
    private final ServiceHandleImpl<?> root;
    /** Actual injectee, needed for InstantiationService */
    private final WeakReference<Injectee> myInjectee;
    
    /* package */ MethodInterceptorImpl(ServiceLocatorImpl sli,
            ActiveDescriptor<?> descriptor,
            ServiceHandleImpl<?> root,
            Injectee injectee) {
        this.locator = sli;
        this.descriptor = descriptor;
        this.root = root;
        if (injectee != null) {
          this.myInjectee = new WeakReference<Injectee>(injectee);
        }
        else {
            this.myInjectee = null;
        }
    }
    
    private Object internalInvoke(Object target, Method method, Method proceed, Object[] params) throws Throwable {
        Context<?> context;
        Object service;

        context = locator.resolveContext(descriptor.getScopeAnnotation());
        service = context.findOrCreate(descriptor, root);

        if (service == null) {
            throw new MultiException(new IllegalStateException("Proxiable context " +
                    context + " findOrCreate returned a null for descriptor " + descriptor +
                    " and handle " + root));
        }

        if (method.getName().equals(PROXY_MORE_METHOD_NAME)) {
            // We did what we came here to do
            return service;
        }
        
        if (isEquals(method) && (params.length == 1) && (params[0] != null) && (params[0] instanceof ProxyCtl)) {
            ProxyCtl equalsProxy = (ProxyCtl) params[0];
            
            params = new Object[1];
            params[0] = equalsProxy.__make();
        }

        return ReflectionHelper.invoke(service, method, params, locator.getNeutralContextClassLoader());
        
    }

    @Override
    public Object invoke(Object target, Method method, Method proceed, Object[] params) throws Throwable {
        boolean pushed = false;
        if (root != null && myInjectee != null) {
            Injectee ref = myInjectee.get();
            if (ref != null) {
                root.pushInjectee(ref);
                pushed = true;
            }
        }
        
        try {
            return internalInvoke(target, method, proceed, params);
        }
        finally {
            if (pushed) {
                root.popInjectee();
            }
        }

    }
    
    private final static String EQUALS_NAME = "equals";
    
    private static boolean isEquals(Method m) {
        if (!m.getName().equals(EQUALS_NAME)) return false;
        Class<?>[] params = m.getParameterTypes();
        if (params == null || params.length != 1) return false;
        
        if (!Object.class.equals(params[0])) return false;
        return true;
    }
}
