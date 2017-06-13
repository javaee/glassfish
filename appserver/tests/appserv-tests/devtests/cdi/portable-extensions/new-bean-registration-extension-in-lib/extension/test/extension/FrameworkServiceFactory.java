/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

package test.extension;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

import test.fwk.FrameworkService;


/**
 * A simple Service Factory class that provides the ability to obtain/get 
 * references to a service implementation (obtained from a service registry) 
 * and also provides a mechanism to unget or return a service after its usage
 * is completed.
 * 
 * @author Sivakumar Thyagarajan
 */
public class FrameworkServiceFactory {
    private static final boolean DEBUG_ENABLED = false;

    /**
     * Get a reference to the service of the provided <code>Type</code>
     */
    public static Object getService(final Type type, final FrameworkService fs){
        debug("getService " + type + " FS:" + fs);
        Object instance = lookupService(type, fs.waitTimeout());
        
        //If the service is marked as dynamic, when a method is invoked on a 
        //a service proxy, an attempt is made to get a reference to the service 
        //and then the method is invoked on the newly obtained service.
        //This scheme should work for statless and/or idempotent service 
        //implementations that have a dynamic lifecycle that is not linked to
        //the service consumer [service dynamism]
        if (fs.dynamic()) {
            InvocationHandler proxyInvHndlr = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args)
                        throws Throwable {
                    debug("Method " + method + " called on proxy");
                    //Always lookup the service and invoke the method in this.
                    return method.invoke(lookupService(type, fs.waitTimeout()), args);
                }
            };
            instance =  Proxy.newProxyInstance(
                                Thread.currentThread().getContextClassLoader(), 
                                new Class[]{(Class)type}, 
                                proxyInvHndlr); 
        }
        return instance;
    }

    //NOTE:hard-coded service instantiation for this test, 
    //but ideally should get the
    //service implementation from the framework's service registry
    private static Object lookupService(Type type, int waitTimeout) {
        String clazzName = ((Class)type).getName() + "Impl";
        System.out.println("LOADING " + clazzName);
        try {
            return FrameworkServiceFactory.class.getClassLoader().loadClass(clazzName).newInstance();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Unget the service
     */
    public static void ungetService(Object serviceInstance, 
            Type type, FrameworkService frameworkService){
        //unget the service instance from the service registry
    }
    
    private static void debug(String string) {
        if(DEBUG_ENABLED)
            System.out.println("ServiceFactory:: " + string);
    }
    

}
