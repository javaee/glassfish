/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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


package org.glassfish.weld.services;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;

import java.util.Collection;

import com.sun.enterprise.container.common.spi.JCDIService;

import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;

import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.WeldBootstrap;
import javax.enterprise.inject.spi.BeanManager;

import org.glassfish.api.invocation.ComponentInvocation;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import org.glassfish.api.invocation.InvocationManager;

import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import org.jboss.weld.manager.api.WeldManager;

import org.glassfish.weld.WeldDeployer;


@Service
public class JCDIServiceImpl implements JCDIService
{
    @Inject
    private WeldDeployer weldDeployer;

    @Inject
    private Habitat h;

    @Inject
    private ComponentEnvManager compEnvManager;

    @Inject
    private InvocationManager invocationManager;


    public boolean isCurrentModuleJCDIEnabled() {

        BundleDescriptor bundle = null;

        ComponentInvocation inv = invocationManager.getCurrentInvocation();

        if( inv == null ) {
            return false;
        }
        
        JndiNameEnvironment componentEnv =
            compEnvManager.getJndiNameEnvironment(inv.getComponentId());

        if( componentEnv != null ) {

            if( componentEnv instanceof BundleDescriptor ) {
                bundle = (BundleDescriptor) componentEnv;
            } else if( componentEnv instanceof EjbDescriptor ) {
                bundle = ((EjbDescriptor) componentEnv).getEjbBundleDescriptor();
            }
        }

        return (bundle != null) ? isJCDIEnabled(bundle) : false;

    }

    public boolean isJCDIEnabled(BundleDescriptor bundle) {

        // Get the top-level bundle descriptor from the given bundle.
        // E.g. allows EjbBundleDescriptor from a .war to be handled correctly.
        BundleDescriptor topLevelBundleDesc = (BundleDescriptor) bundle.getModuleDescriptor().getDescriptor();

        return weldDeployer.is299Enabled(topLevelBundleDesc);

    }

    public JCDIInjectionContext injectEJBInstance(EjbDescriptor ejb, Object instance) {

        BundleDescriptor topLevelBundleDesc = (BundleDescriptor)
                ejb.getEjbBundleDescriptor().getModuleDescriptor().getDescriptor();

        // First get BeanDeploymentArchive for this ejb
        BeanDeploymentArchive bda = weldDeployer.getBeanDeploymentArchiveForBundle(topLevelBundleDesc);
     
        WeldBootstrap bootstrap = weldDeployer.getBootstrapForApp(ejb.getEjbBundleDescriptor().getApplication());
        WeldManager weldManager = bootstrap.getManager(bda);

        org.jboss.weld.ejb.spi.EjbDescriptor ejbDesc = weldManager.getEjbDescriptor(ejb.getName());

        // Get an the Bean object
        Bean<?> bean = weldManager.getBean(ejbDesc);

        // Create the injection target
        InjectionTarget it = weldManager.createInjectionTarget(ejbDesc);

        // Per instance required, create the creational context
        CreationalContext<?> cc = weldManager.createCreationalContext(bean);

        // Perform injection and call initializers
        it.inject(instance, cc);

        // NOTE : PostConstruct is handled by ejb container


        return new JCDIInjectionContextImpl(it, cc, instance);

    }

    public JCDIInjectionContext createManagedObject(Class managedClass, BundleDescriptor bundle) {
        return createManagedObject(managedClass, bundle, true);
    }


    public JCDIInjectionContext createManagedObject(Class managedClass, BundleDescriptor bundle,
                                                    boolean invokePostConstruct) {

        Object managedObject = null;

        BundleDescriptor topLevelBundleDesc = (BundleDescriptor) bundle.getModuleDescriptor().getDescriptor();

        // First get BeanDeploymentArchive for this ejb
        BeanDeploymentArchive bda = weldDeployer.getBeanDeploymentArchiveForBundle(topLevelBundleDesc);

        WeldBootstrap bootstrap = weldDeployer.getBootstrapForApp(bundle.getApplication());

        BeanManager beanManager = bootstrap.getManager(bda);

        AnnotatedType annotatedType = beanManager.createAnnotatedType(managedClass);
        
        InjectionTarget it = beanManager.createInjectionTarget(annotatedType);

        CreationalContext cc = beanManager.createCreationalContext(null);

        managedObject = it.produce(cc);

        it.inject(managedObject, cc);

        if( invokePostConstruct ) {
            it.postConstruct(managedObject);
        }

        return new JCDIInjectionContextImpl(it, cc, managedObject);

    }

    private class JCDIInjectionContextImpl implements JCDIInjectionContext {

        private InjectionTarget it;
        private CreationalContext cc;
        private Object instance;

        JCDIInjectionContextImpl(InjectionTarget it, CreationalContext cc, Object i) {
            this.it = it;
            this.cc = cc;
            this.instance = i;
        }


        public Object getInstance() {
            return instance;
        }

        public void cleanup(boolean callPreDestroy) {

            if( callPreDestroy ) {
                it.preDestroy(instance);
            }

            it.dispose(instance);
            cc.release();

        }

    }


}


