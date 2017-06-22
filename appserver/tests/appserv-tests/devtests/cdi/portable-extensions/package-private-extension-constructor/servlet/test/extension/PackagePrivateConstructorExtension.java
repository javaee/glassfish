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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;

import test.beans.DuplicateTestBean;

public class PackagePrivateConstructorExtension implements Extension{
    public static boolean beforeBeanDiscoveryCalled = false;
    public static boolean afterBeanDiscoveryCalled = false;
    public static boolean processAnnotatedTypeCalled = false;
    public static boolean packagePrivateConstructorCalled = false;
    private Collection<Class<?>> enabledInterceptors;
    /* package private */ PackagePrivateConstructorExtension(){
        System.out.println("In MyExtension ctor");
        PackagePrivateConstructorExtension.packagePrivateConstructorCalled = true;
        this.enabledInterceptors = Collections.synchronizedSet(new
                HashSet<Class<?>>());
    }
    
   @SuppressWarnings("unused")
   void observeInterceptors(@Observes ProcessBean<?> pmb)
   {
      if (pmb.getBean() instanceof Interceptor<?>)
      {
         this.enabledInterceptors.add(pmb.getBean().getBeanClass());
      }
   }
   
   Collection<Class<?>> getEnabledInterceptors()
   {
      return enabledInterceptors;
   }
    
    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bdd){
        System.out.println("MyExtension::beforeBeanDiscovery" + bdd);
        beforeBeanDiscoveryCalled = true;
    }
    
    <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat){
        System.out.println("MyExtension:Process annotated type" + pat.getAnnotatedType().getBaseType());
        processAnnotatedTypeCalled = true;
        //Vetoing the processing of DuplicateTestBean
        //If this is not vetoed, at the InjectionPoint in Servlet, there would be
        //an ambiguous dependency due to TestBean and DuplicateTestBean
        if (pat.getAnnotatedType().getBaseType().equals(DuplicateTestBean.class)){
            pat.veto();
        }
    }
    
    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm){
        System.out.println("MyExtension: abd: " + abd + " BeanManager: " + bm);
        
        if (bm != null) {
            //ensure a valid BeanManager is injected
            afterBeanDiscoveryCalled = true;
        }
    }
}
