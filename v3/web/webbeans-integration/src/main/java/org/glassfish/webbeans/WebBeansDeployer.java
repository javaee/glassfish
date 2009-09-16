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
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.Application;
import org.jboss.webbeans.bootstrap.spi.BeanDeploymentArchive;


import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.EventListener;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.SimpleDeployer;
import org.glassfish.ejb.api.EjbContainerServices;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.webbeans.ejb.EjbServicesImpl;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;

import org.jboss.webbeans.bootstrap.WebBeansBootstrap;
import org.jboss.webbeans.bootstrap.api.Environments;
import org.jboss.webbeans.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.servlet.api.ServletServices;

import com.sun.enterprise.container.common.spi.util.InjectionManager;
import org.jboss.webbeans.injection.spi.InjectionServices;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

@Service
public class WebBeansDeployer extends SimpleDeployer<WebBeansContainer, WebBeansApplicationContainer> 
    implements PostConstruct, EventListener {

    /* package */ static final String WEB_BEAN_EXTENSION = "org.glassfish.webbeans";

    /* package */ static final String WEB_BEAN_BOOTSTRAP = "org.glassfish.webbeans.WebBeansBootstrap";

    /* package */ static final String WEB_BEAN_DEPLOYMENT = "org.glassfish.webbeans.WebBeansDeployment";

    private static final String WEB_BEAN_LISTENER = "org.jboss.webbeans.servlet.WebBeansListener";



    @Inject
    private Events events;

    @Inject
    private Habitat habitat;

    private Map<Application, WebBeansBootstrap> appToBootstrap =
            new HashMap<Application, WebBeansBootstrap>();

    private Map<BundleDescriptor, BeanDeploymentArchive> bundleToBeanDeploymentArchive =
            new HashMap<BundleDescriptor, BeanDeploymentArchive>();

    @Override
    public MetaData getMetaData() {
        return new MetaData(true, null, new Class[] {Application.class});
    }

    public void postConstruct() {
        events.register(this);
    }

    /**
     * Specific stages of the Web Beans bootstrapping process will execute across different stages
     * of the deployment process.  Web Beans deployment will happen when the load phase of the 
     * deployment process is complete.
     */
    public void event(Event event) {
        if ( event.is(org.glassfish.internal.deployment.Deployment.APPLICATION_LOADED) ) {
            ApplicationInfo appInfo = (ApplicationInfo)event.hook();
            WebBeansBootstrap bootstrap = (WebBeansBootstrap)appInfo.getTransientAppMetaData(WEB_BEAN_BOOTSTRAP, 
                WebBeansBootstrap.class);
            if( bootstrap != null ) {
                DeploymentImpl deploymentImpl = (DeploymentImpl)appInfo.getTransientAppMetaData(
                    WEB_BEAN_DEPLOYMENT, DeploymentImpl.class);
                bootstrap.startContainer(Environments.SERVLET, deploymentImpl, new ConcurrentHashMapBeanStore());
                bootstrap.startInitialization();
                bootstrap.deployBeans();
            }
        } else if ( event.is(org.glassfish.internal.deployment.Deployment.APPLICATION_STARTED) ) {
            ApplicationInfo appInfo = (ApplicationInfo)event.hook();
            WebBeansBootstrap bootstrap = (WebBeansBootstrap)appInfo.getTransientAppMetaData(WEB_BEAN_BOOTSTRAP, 
                WebBeansBootstrap.class);
            if( bootstrap != null ) {
                bootstrap.validateBeans();
                bootstrap.endInitialization();
            }
        } else if ( event.is(org.glassfish.internal.deployment.Deployment.APPLICATION_STOPPED) ||
                    event.is(org.glassfish.internal.deployment.Deployment.APPLICATION_UNLOADED)) {
            ApplicationInfo appInfo = (ApplicationInfo)event.hook();

            // TODO move bootstrap shutdown logic here

            Application app = appInfo.getMetaData(Application.class);

            if( app != null ) {

                for(BundleDescriptor next : app.getBundleDescriptors()) {
                    if( next instanceof EjbBundleDescriptor || next instanceof WebBundleDescriptor ) {
                        bundleToBeanDeploymentArchive.remove(next);
                    }
                }

                appToBootstrap.remove(app);
            }

        }
    }

    BeanDeploymentArchive getBeanDeploymentArchiveForBundle(BundleDescriptor bundle) {
        return bundleToBeanDeploymentArchive.get(bundle);
    }

    boolean is299Enabled(BundleDescriptor bundle) {
        return bundleToBeanDeploymentArchive.containsKey(bundle);
    }

    WebBeansBootstrap getBootstrapForApp(Application app) {
        return appToBootstrap.get(app);
    }

    protected void generateArtifacts(DeploymentContext dc) throws DeploymentException {

    }

    protected void cleanArtifacts(DeploymentContext dc) throws DeploymentException {

    }

    public <V> V loadMetaData(Class<V> type, DeploymentContext context) {
        return null;
    }


    @Override
    public WebBeansApplicationContainer load(WebBeansContainer container, DeploymentContext context) {

        // TODO *** change this logic to share one instance of web beans bootstrap per application ***

        ReadableArchive archive = context.getSource();

        // See if a WebBeansBootsrap has already been created - only want one per app.

        WebBeansBootstrap bootstrap = (WebBeansBootstrap)context.getTransientAppMetaData(WEB_BEAN_BOOTSTRAP,
                WebBeansBootstrap.class);
        if (null == bootstrap) {
            bootstrap = new WebBeansBootstrap();

            Application app = context.getModuleMetaData(Application.class);
            appToBootstrap.put(app, bootstrap);
        }

        Set<EjbDescriptor> ejbs = new HashSet<EjbDescriptor>();
        EjbBundleDescriptor ejbBundle = getEjbBundleFromContext(context);

        EjbServices ejbServices = null;

        if( ejbBundle != null ) {

            ejbs = ejbBundle.getEjbs();

            ejbServices = new EjbServicesImpl(habitat);

        }

        DeploymentImpl deploymentImpl = new DeploymentImpl(archive, ejbs);

        if( ejbBundle != null ) {

            deploymentImpl.getBeanDeploymentArchives().iterator().next().getServices().add(EjbServices.class, ejbServices);

        }

        // TODO change this -- can't assume that 299-enabled module is a web module
        ServletServices servletServices = new ServletServicesImpl(context);
        deploymentImpl.getServices().add(ServletServices.class, servletServices);

        /**        TODO enable injection manager      when enabled, numberguess app stops working
        InjectionManager injectionMgr = habitat.getByContract(InjectionManager.class);
        InjectionServices injectionServices = new InjectionServicesImpl(injectionMgr);
        deploymentImpl.getServices().add(InjectionServices.class, injectionServices);
         **/




        WebBundleDescriptor wDesc = context.getModuleMetaData(WebBundleDescriptor.class);
        if( wDesc != null) {


            wDesc.setExtensionProperty(WEB_BEAN_EXTENSION, "true");

            // Add the Web Beans Listener if it does not already exist..
            wDesc.addAppListenerDescriptor(new AppListenerDescriptorImpl(WEB_BEAN_LISTENER));
        }

        BundleDescriptor bundle = (wDesc != null) ? wDesc : ejbBundle;
        if( bundle != null ) {
            // TODO change logic to support multiple 299 enabled modules in app
            bundleToBeanDeploymentArchive.put(bundle, deploymentImpl.getBeanDeploymentArchives().iterator().next());
        }
        

        WebBeansApplicationContainer wbApp = new WebBeansApplicationContainer(bootstrap);

        // Stash the WebBeansBootstrap instance, so we may access the WebBeansManager later..
        context.addTransientAppMetaData(WEB_BEAN_BOOTSTRAP, bootstrap);

        context.addTransientAppMetaData(WEB_BEAN_DEPLOYMENT, deploymentImpl);

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

}


