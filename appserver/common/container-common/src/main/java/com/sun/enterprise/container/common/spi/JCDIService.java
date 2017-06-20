/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.container.common.spi;


import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbInterceptor;
import org.jvnet.hk2.annotations.Contract;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.Interceptor;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import java.util.Set;

/**
 */
@Contract
public interface JCDIService {

    public boolean isCurrentModuleJCDIEnabled();

    public boolean isJCDIEnabled(BundleDescriptor bundle);

    public boolean isCDIScoped(Class<?> clazz);

    public void setELResolver(ServletContext servletContext) throws NamingException;

    public <T> JCDIInjectionContext<T> createManagedObject(Class<T> managedClass, BundleDescriptor bundle);
    public <T> JCDIInjectionContext<T> createManagedObject(Class<T> managedClass, BundleDescriptor bundle,
                                                    boolean invokePostConstruct);

    public void injectManagedObject(Object managedObject, BundleDescriptor bundle);

    /**
     * Create an inteceptor instance for an ejb.
     * @param interceptorClass The interceptor class.
     * @param bundle The ejb bundle.
     * @param ejbContext The ejb context.
     * @param ejbInterceptors All of the ejb interceptors for the ejb.
     *
     * @return The interceptor instance.
     */
    <T> T createInterceptorInstance( Class<T> interceptorClass,
                                     BundleDescriptor bundle,
                                     JCDIService.JCDIInjectionContext ejbContext,
                                     Set<EjbInterceptor> ejbInterceptors );

    public <T> JCDIInjectionContext<T> createJCDIInjectionContext(EjbDescriptor ejbDesc);
    public <T> JCDIInjectionContext<T> createJCDIInjectionContext(EjbDescriptor ejbDesc, T instance);

    public <T> void injectEJBInstance(JCDIInjectionContext<T> injectionCtx);

    public interface JCDIInjectionContext<T> {
        public T getInstance();
        public void cleanup(boolean callPreDestroy);

        /**
         * @return The injection target.
         */
        InjectionTarget<T> getInjectionTarget();

        /**
         * @return The creational context.
         */
        CreationalContext<T> getCreationalContext();

        /**
         * Add a dependent context to this context so that the dependent
         * context can be cleaned up when this one is.
         *
         * @param dependentContext The dependenct context.
         */
        void addDependentContext( JCDIInjectionContext dependentContext );
    }

}
