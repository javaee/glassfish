/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

package test.extension;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

import test.fwk.FrameworkService;
import test.fwk.SomeFwkServiceImpl;
import test.fwk.SomeFwkServiceInterface;

public class FrameworkServiceFactory {
    public static Object getService(final Type type, final FrameworkService fs){
        //NOTE:hard-coded for this test, but ideally should get the
        //service implementation from the service registry
        SomeFwkServiceInterface instance = 
            (SomeFwkServiceInterface) lookupService(type, fs.waitTimeout());
        
        if (fs.dynamic()) {
            InvocationHandler ih = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args)
                        throws Throwable {
                    System.out.println("****************** Method " + method + " called on proxy");
                    return method.invoke(lookupService(type, fs.waitTimeout()), args);
                }
            };
            instance = (SomeFwkServiceInterface) 
            Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), 
                    new Class[]{SomeFwkServiceInterface.class}, ih); 
        }
        return instance;
    }
    
    private static Object lookupService(Type type, int waitTimeout) {
        if (type.equals(SomeFwkServiceInterface.class)){ 
            return new SomeFwkServiceImpl("test");
        }
        return null;
    }

    public static void ungetService(Object serviceInstance){
        //unget the service instance from the service registry
    }

}
