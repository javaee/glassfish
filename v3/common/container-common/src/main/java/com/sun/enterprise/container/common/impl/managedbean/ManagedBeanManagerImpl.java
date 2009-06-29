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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;

import org.glassfish.api.managedbean.ManagedBeanManager;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.GlassfishNamingManager;


import com.sun.enterprise.deployment.util.ModuleDescriptor;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.logging.LogDomains;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;

import org.jvnet.hk2.component.PostConstruct;

import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.data.ApplicationInfo;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;

import org.glassfish.api.interceptor.InterceptorInvoker;
import org.glassfish.api.interceptor.InterceptorInfo;
import org.glassfish.api.interceptor.JavaEEInterceptorBuilder;
import org.glassfish.api.interceptor.JavaEEInterceptorBuilderFactory;

/**
 */
@Service
public class ManagedBeanManagerImpl implements ManagedBeanManager, PostConstruct, EventListener {

     private static Logger _logger = LogDomains.getLogger(ManagedBeanManagerImpl.class,
            LogDomains.CORE_LOGGER);

    @Inject
    private ComponentEnvManager compEnvManager;

    @Inject
    private InvocationManager invocationMgr;

    @Inject
    private GlassfishNamingManager namingManager;

    @Inject
    private Habitat habitat;

    @Inject
    private Events events;

    @Inject
    private ProcessEnvironment processEnv;

    private ProcessType processType;

    public void postConstruct() {
        events.register(this);
        processType = processEnv.getProcessType();
    }

    public void event(Event event) {
        
         if (event.is(Deployment.APPLICATION_PREPARED) ) {
             DeploymentContext dc =  Deployment.APPLICATION_PREPARED.getHook(event);
             loadManagedBeans(dc);
         } else if( event.is(Deployment.APPLICATION_UNLOADED) ) {
             ApplicationInfo info =  Deployment.APPLICATION_UNLOADED.getHook(event);
             unloadManagedBeans(info);
         }

    }
    
    private void loadManagedBeans(DeploymentContext dc) {

        Application app = dc.getModuleMetaData(Application.class);

        if( app == null ) {
            return;
        }

        for(BundleDescriptor bundle : app.getBundleDescriptors()) {

            if (!bundleEligible(bundle)) {
                continue;
            }

            for(ManagedBeanDescriptor next : bundle.getManagedBeans()) {

                try {

                    // TODO Should move this to regular DOL processing stage
                    next.validate();

                     Set<String> interceptorClasses = next.getAllInterceptorClasses();


                    if( !interceptorClasses.isEmpty() || next.hasAroundInvokeMethod()) {
                        // Inject instance and have injection manager call PostConstruct

                        Class targetClass = bundle.getClassLoader().loadClass(next.getBeanClassName());
                        InterceptorInfo interceptorInfo = new InterceptorInfo();
                        interceptorInfo.setTargetClass(targetClass);
                        interceptorInfo.setInterceptorClassNames(next.getAllInterceptorClasses());
                        interceptorInfo.setPostConstructInterceptors
                                (next.getCallbackInterceptors(LifecycleCallbackDescriptor.CallbackType.POST_CONSTRUCT));
                          interceptorInfo.setPreDestroyInterceptors
                                (next.getCallbackInterceptors(LifecycleCallbackDescriptor.CallbackType.PRE_DESTROY));
                        if( next.hasAroundInvokeMethod() ) {
                            interceptorInfo.setHasTargetClassAroundInvoke(true);
                        }

                        // TODO handle method-level interceptors
                        interceptorInfo.setAroundInvokeInterceptorChain(next.getAroundInvokeInterceptors(null));

                        JavaEEInterceptorBuilderFactory interceptorBuilderFactory =
                                habitat.getByContract(JavaEEInterceptorBuilderFactory.class);

                        JavaEEInterceptorBuilder builder = interceptorBuilderFactory.createBuilder(interceptorInfo);

                        next.setInterceptorBuilder(builder);   

                    }

                    compEnvManager.bindToComponentNamespace(next);

                    String jndiName = next.getGlobalJndiName();
                    ManagedBeanNamingProxy namingProxy =
                        new ManagedBeanNamingProxy(next, habitat);

                    namingManager.publishObject(jndiName, namingProxy, true);

                } catch(Exception e) {
                    throw new RuntimeException("Error binding ManagedBean " + next.getBeanClassName() +
                    " with name = " + next.getName(), e);
                }
            }
        }

    }

    private void unloadManagedBeans(ApplicationInfo appInfo) {

        Application app = appInfo.getMetaData(Application.class);

        if( app == null ) {
            return;
        }

        for(BundleDescriptor bundle : app.getBundleDescriptors()) {

            if (!bundleEligible(bundle)) {
                continue;
            }

            for(ManagedBeanDescriptor next : bundle.getManagedBeans()) {

                com.sun.enterprise.container.common.spi.util.InjectionManager injectionMgr =
                    habitat.getByContract(com.sun.enterprise.container.common.spi.util.InjectionManager.class);

                for(Object instance : next.getBeanInstances()) {

                    InterceptorInvoker invoker = (InterceptorInvoker)
                            next.getInterceptorInfoForBeanInstance(instance);

                    if( invoker == null ) {
                        try {
                            injectionMgr.invokeInstancePreDestroy(instance, next);
                        } catch(Exception e) {
                            _logger.log(Level.FINE, "Managed bean " + next.getBeanClassName() + " PreDestroy", e);
                        }
                    } else {
                        try {
                            invoker.invokePreDestroy();
                        } catch(Exception e) {
                            _logger.log(Level.FINE, "Managed bean " + next.getBeanClassName() + " PreDestroy", e);
                        }
                    }

                }

                com.sun.enterprise.container.common.spi.util.ComponentEnvManager compEnvManager =
                    habitat.getByContract(com.sun.enterprise.container.common.spi.util.ComponentEnvManager.class);


                try {
                    compEnvManager.unbindFromComponentNamespace(next);
                } catch(javax.naming.NamingException ne) {
                    _logger.log(Level.FINE, "Managed bean " + next.getBeanClassName() + " unbind", ne);
                }

                org.glassfish.api.naming.GlassfishNamingManager namingManager =
                        habitat.getByContract(org.glassfish.api.naming.GlassfishNamingManager.class);
                String jndiName = next.getGlobalJndiName();

                try {
                    namingManager.unpublishObject(jndiName);
                } catch(javax.naming.NamingException ne) {
                    _logger.log(Level.FINE, "Error unpubishing managed bean " +
                           next.getBeanClassName() + " with jndi name " + jndiName, ne);
                }

                next.clearBeanInstanceInfo();

            }
        }

    }

    private boolean bundleEligible(BundleDescriptor bundle) {

        boolean eligible = false;

        if( processType == ProcessEnvironment.ProcessType.Server ) {

            eligible = (bundle instanceof WebBundleDescriptor) ||
                (bundle instanceof EjbBundleDescriptor);

        }

        // TODO handle Application Clients

        return eligible;
    }


}
