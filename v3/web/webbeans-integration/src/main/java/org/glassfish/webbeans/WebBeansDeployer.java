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

package org.glassfish.webbeans;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.AppListenerDescriptorImpl;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.InterceptorDescriptor;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor.CallbackType;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.web.WebComponentInvocation;
import com.sun.enterprise.web.WebModule;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.SimpleDeployer;
import org.glassfish.webbeans.ejb.EjbServicesImpl;
import org.glassfish.ejb.api.EjbContainerServices;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;

import org.jvnet.hk2.component.Habitat;

import org.jboss.webbeans.bootstrap.WebBeansBootstrap;
import org.jboss.webbeans.bootstrap.api.Environments;
import org.jboss.webbeans.bootstrap.spi.Deployment;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.webbeans.ejb.spi.EjbServices;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Service
public class WebBeansDeployer extends SimpleDeployer<WebBeansContainer, WebBeansApplicationContainer> {

    /* package */ static final String WEB_BEAN_EXTENSION = "org.glassfish.webbeans";

    /* package */ static final String WEB_BEAN_BOOTSTRAP = "org.glassfish.webbeans.WebBeansBootstrap";

    private static final String WEB_BEAN_LISTENER = "org.jboss.webbeans.servlet.WebBeansListener";

    @Inject
    private Habitat habitat;


    @Override
    public MetaData getMetaData() {
        return new MetaData(true, null, new Class[] {Application.class});
    }

    protected void generateArtifacts(DeploymentContext dc) throws DeploymentException {

    }

    protected void cleanArtifacts(DeploymentContext dc) throws DeploymentException {

    }

    public <V> V loadMetaData(Class<V> type, DeploymentContext context) {
        return null;
    }

    @Override
    public boolean prepare(DeploymentContext context) {

        // If the app contains any ejbs associate the web beans interceptor
        // with each of the ejbs so it's available during the ejb load phase


        EjbBundleDescriptor ejbBundle = getEjbBundleFromContext(context);

        if( ejbBundle != null ) {

            Set<EjbDescriptor> ejbs = ejbBundle.getEjbs();

            InterceptorDescriptor interceptor = createEjbInterceptor(ejbBundle);

            for(EjbDescriptor next : ejbs) {

                if( next.getType().equals(EjbSessionDescriptor.TYPE) ||
                    next.getType().equals(EjbMessageBeanDescriptor.TYPE) ) {
                    next.addFrameworkInterceptor(interceptor);
                }

            }
        }
        

        return true;
    }


    @Override
    public WebBeansApplicationContainer load(WebBeansContainer container, DeploymentContext context) {

        // TODO *** change this logic to share one instance of web beans bootstrap per application ***

        ReadableArchive archive = context.getSource();
        BeanStore applicationBeanStore = new ConcurrentHashMapBeanStore();

        WebBeansBootstrap bootstrap = new WebBeansBootstrap();

        Set<EjbDescriptor> ejbs = new HashSet<EjbDescriptor>();
        
        
        EjbBundleDescriptor ejbBundle = getEjbBundleFromContext(context);

        if( ejbBundle != null ) {

            ejbs = ejbBundle.getEjbs();

            EjbServices ejbServices = new EjbServicesImpl(habitat, ejbs);
            bootstrap.getServices().add(EjbServices.class, ejbServices);

        }
        
        bootstrap.setEnvironment(Environments.SERVLET);
        bootstrap.getServices().add(Deployment.class, new DeploymentImpl(archive, ejbs) {});
        bootstrap.setApplicationContext(applicationBeanStore);

        WebBundleDescriptor wDesc = context.getModuleMetaData(WebBundleDescriptor.class);
        if( wDesc != null) {
            wDesc.setExtensionProperty(WEB_BEAN_EXTENSION, "true");

            // Add the Web Beans Listener if it does not already exist..
            wDesc.addAppListenerDescriptor(new AppListenerDescriptorImpl(WEB_BEAN_LISTENER));
        }
        

        WebBeansApplicationContainer wbApp = new WebBeansApplicationContainer(bootstrap);


        // Do first stage of web beans initialization.  Note that we delay calling
        // bootstrap.boot() until start phase (see WebBeansApplicationContainer)
        bootstrap.initialize();      

        // Stash the WebBeansBootstrap instance, so we may access the WebBeansManager later..
        context.addTransientAppMetaData(WEB_BEAN_BOOTSTRAP, bootstrap);

        return wbApp; 
    }

    private EjbBundleDescriptor getEjbBundleFromContext(DeploymentContext context) {


        EjbBundleDescriptor ejbBundle = context.getModuleMetaData(EjbBundleDescriptor.class);

        if( ejbBundle == null ) {

            WebBundleDescriptor wDesc = context.getModuleMetaData(WebBundleDescriptor.class);
            if( wDesc != null ) {
                Collection<EjbBundleDescriptor> ejbBundles = wDesc.getExtensionsDescriptors(EjbBundleDescriptor.class);
                if (ejbBundles.iterator().hasNext()) {
                    ejbBundle = ejbBundles.iterator().next();
                }
            }

        }

        return ejbBundle;

    }

    private InterceptorDescriptor createEjbInterceptor(EjbBundleDescriptor ejbBundle) {

        InterceptorDescriptor interceptor = new InterceptorDescriptor();

        Class wbInterceptor = org.jboss.webbeans.ejb.SessionBeanInterceptor.class;
        String wbInterceptorName = wbInterceptor.getName();

        interceptor.setInterceptorClass(wbInterceptor);

        for(Method m : wbInterceptor.getDeclaredMethods() ) {

           if( m.getAnnotation(PostConstruct.class) != null ) {
               LifecycleCallbackDescriptor desc = new LifecycleCallbackDescriptor();
               desc.setLifecycleCallbackClass(wbInterceptorName);
               desc.setLifecycleCallbackMethod(m.getName());
               interceptor.addCallbackDescriptor(CallbackType.POST_CONSTRUCT, desc);
           } else if( m.getAnnotation(PreDestroy.class) != null ) {
               LifecycleCallbackDescriptor desc = new LifecycleCallbackDescriptor();
               desc.setLifecycleCallbackClass(wbInterceptorName);
               desc.setLifecycleCallbackMethod(m.getName());
               interceptor.addCallbackDescriptor(CallbackType.PRE_DESTROY, desc);
           } else if( m.getAnnotation(AroundInvoke.class) != null ) {
               LifecycleCallbackDescriptor desc = new LifecycleCallbackDescriptor();
               desc.setLifecycleCallbackClass(wbInterceptorName);
               desc.setLifecycleCallbackMethod(m.getName());
               interceptor.addAroundInvokeDescriptor(desc);
           } else if( m.getAnnotation(AroundTimeout.class) != null ) {
               LifecycleCallbackDescriptor desc = new LifecycleCallbackDescriptor();
               desc.setLifecycleCallbackClass(wbInterceptorName);
               desc.setLifecycleCallbackMethod(m.getName());
               interceptor.addAroundTimeoutDescriptor(desc);
           }

        }

        return interceptor;

    }

}


