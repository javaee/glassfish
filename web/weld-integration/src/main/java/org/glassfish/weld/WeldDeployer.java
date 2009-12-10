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

package org.glassfish.weld;

import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.AppListenerDescriptorImpl;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;

import com.sun.logging.LogDomains;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.EventListener;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.SimpleDeployer;
import org.glassfish.ejb.api.EjbContainerServices;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.weld.services.EjbServicesImpl;
import org.glassfish.weld.services.InjectionServicesImpl;
import org.glassfish.weld.services.SecurityServicesImpl;
import org.glassfish.weld.services.ServletServicesImpl;
import org.glassfish.weld.services.TransactionServicesImpl;
import org.glassfish.weld.services.ValidationServicesImpl;

import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.injection.spi.InjectionServices;
import org.jboss.weld.security.spi.SecurityServices;
import org.jboss.weld.servlet.api.ServletServices;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.jboss.weld.validation.spi.ValidationServices;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.logging.LogDomains;

import javax.servlet.Servlet;
import javax.servlet.Filter;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionListener;
import javax.enterprise.inject.spi.AnnotatedType;

@Service
public class WeldDeployer extends SimpleDeployer<WeldContainer, WeldApplicationContainer> 
    implements PostConstruct, EventListener {

    private Logger _logger = LogDomains.getLogger(WeldDeployer.class, LogDomains.CORE_LOGGER);
    
    public static final String WELD_EXTENSION = "org.glassfish.weld";

    public static final String WELD_DEPLOYMENT = "org.glassfish.weld.WeldDeployment";

    /* package */ static final String WELD_BOOTSTRAP = "org.glassfish.weld.WeldBootstrap";

    private static final String WELD_LISTENER = "org.jboss.weld.servlet.WeldListener";

    private static final String WELD_SHUTDOWN = "false";

    @Inject
    private Events events;

    @Inject
    private Habitat habitat;

    private Map<Application, WeldBootstrap> appToBootstrap =
            new HashMap<Application, WeldBootstrap>();

    private Map<BundleDescriptor, BeanDeploymentArchive> bundleToBeanDeploymentArchive =
            new HashMap<BundleDescriptor, BeanDeploymentArchive>();

    private static final Class<?>[] NON_CONTEXT_CLASSES = {
            Servlet.class, ServletContextListener.class, Filter.class,
            HttpSessionListener.class, ServletRequestListener.class
            // TODO need to add more classes
    };

    @Override
    public MetaData getMetaData() {
        return new MetaData(true, null, new Class[] {Application.class});
    }

    public void postConstruct() {
        events.register(this);
    }

    /**
     * Specific stages of the Weld bootstrapping process will execute across different stages
     * of the deployment process.  Weld deployment will happen when the load phase of the 
     * deployment process is complete.
     */
    public void event(Event event) {
        if ( event.is(org.glassfish.internal.deployment.Deployment.APPLICATION_LOADED) ) {
            ApplicationInfo appInfo = (ApplicationInfo)event.hook();
            WeldBootstrap bootstrap = (WeldBootstrap)appInfo.getTransientAppMetaData(WELD_BOOTSTRAP, 
                WeldBootstrap.class);
            if( bootstrap != null ) {
                DeploymentImpl deploymentImpl = (DeploymentImpl)appInfo.getTransientAppMetaData(
                    WELD_DEPLOYMENT, DeploymentImpl.class);
                deploymentImpl = buildDeploymentGraph(deploymentImpl);
                System.out.println(deploymentImpl.toString());
                try {
                    bootstrap.startContainer(Environments.SERVLET, deploymentImpl, new ConcurrentHashMapBeanStore());
                    bootstrap.startInitialization();
                    fireProcessInjectionTargetEvents(bootstrap, deploymentImpl);
                    bootstrap.deployBeans();
                } catch (Throwable t) {
                    DeploymentException de = new DeploymentException(t.getMessage());
                    de.initCause(t);
                    throw(de);
                }
            }
        } else if ( event.is(org.glassfish.internal.deployment.Deployment.APPLICATION_STARTED) ) {
            ApplicationInfo appInfo = (ApplicationInfo)event.hook();
            WeldBootstrap bootstrap = (WeldBootstrap)appInfo.getTransientAppMetaData(WELD_BOOTSTRAP, 
                WeldBootstrap.class);
            if( bootstrap != null ) {
                try {
                    bootstrap.validateBeans();
                    bootstrap.endInitialization();
                } catch (Throwable t) {
                    DeploymentException de = new DeploymentException(t.getMessage());
                    de.initCause(t);
                    throw(de);
                }
            }
        } else if ( event.is(org.glassfish.internal.deployment.Deployment.APPLICATION_STOPPED) ||
                    event.is(org.glassfish.internal.deployment.Deployment.APPLICATION_UNLOADED)) {
            ApplicationInfo appInfo = (ApplicationInfo)event.hook();

            Application app = appInfo.getMetaData(Application.class);

            if( app != null ) {

                for(BundleDescriptor next : app.getBundleDescriptors()) {
                    if( next instanceof EjbBundleDescriptor || next instanceof WebBundleDescriptor ) {
                        bundleToBeanDeploymentArchive.remove(next);
                    }
                }
           
                appToBootstrap.remove(app);
            }

            String shutdown = appInfo.getTransientAppMetaData(WELD_SHUTDOWN, String.class);
            if (Boolean.valueOf(shutdown) == Boolean.TRUE) {
                return;
            }
            WeldBootstrap bootstrap = (WeldBootstrap)appInfo.getTransientAppMetaData(WELD_BOOTSTRAP, 
                WeldBootstrap.class);
            if (null != bootstrap) {
                try {
                    bootstrap.shutdown();  
                } catch(Exception e) {
                    _logger.log(Level.WARNING, "JCDI shutdown error", e);
                }
                appInfo.addTransientAppMetaData(WELD_SHUTDOWN, "true");
            }
        }
    }

    /*
     * We are only firing ProcessInjectionTarget<X> for non-contextual EE
     * components and not using the InjectionTarget<X> from the event during
     * instance creation in JCDIServiceImpl.java
     * TODO weld would provide a better way to do this, otherwise we may need
     * TODO to store InjectionTarget<X> to be used in instance creation
     */
    private void fireProcessInjectionTargetEvents(WeldBootstrap bootstrap, DeploymentImpl impl) {
        List<BeanDeploymentArchive> bdaList = impl.getBeanDeploymentArchives();
        for(BeanDeploymentArchive bda : bdaList) {
            Collection<Class<?>> bdaClasses = bda.getBeanClasses();
            for(Class<?> bdaClazz: bdaClasses) {
                for(Class<?> nonClazz : NON_CONTEXT_CLASSES) {
                    if (nonClazz.isAssignableFrom(bdaClazz)) {
                        AnnotatedType at = bootstrap.getManager(bda).createAnnotatedType(bdaClazz);
                        bootstrap.getManager(bda).fireProcessInjectionTarget(at);
                    }
                }
            }
        }
    }

    public BeanDeploymentArchive getBeanDeploymentArchiveForBundle(BundleDescriptor bundle) {
        return bundleToBeanDeploymentArchive.get(bundle);
    }

    public boolean is299Enabled(BundleDescriptor bundle) {
        return bundleToBeanDeploymentArchive.containsKey(bundle);
    }

    public WeldBootstrap getBootstrapForApp(Application app) {
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
    public WeldApplicationContainer load(WeldContainer container, DeploymentContext context) {

        ReadableArchive archive = context.getSource();

        // See if a WeldBootsrap has already been created - only want one per app.

        WeldBootstrap bootstrap = (WeldBootstrap)context.getTransientAppMetaData(WELD_BOOTSTRAP,
                WeldBootstrap.class);
        if (null == bootstrap) {
            bootstrap = new WeldBootstrap();
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

        DeploymentImpl deploymentImpl = (DeploymentImpl)context.getTransientAppMetaData(
            WELD_DEPLOYMENT, DeploymentImpl.class);
        if (null == deploymentImpl) {
            deploymentImpl = new DeploymentImpl(archive, ejbs);
            // Add services

            ServletServices servletServices = new ServletServicesImpl(context);
            deploymentImpl.getServices().add(ServletServices.class, servletServices);

            TransactionServices transactionServices = new TransactionServicesImpl(habitat);
            deploymentImpl.getServices().add(TransactionServices.class, transactionServices);

            ValidationServices validationServices = new ValidationServicesImpl();
            deploymentImpl.getServices().add(ValidationServices.class, validationServices);

            SecurityServices securityServices = new SecurityServicesImpl();
            deploymentImpl.getServices().add(SecurityServices.class, securityServices);

            if( ejbBundle != null ) {
                // EJB Services is registered as a top-level service
                deploymentImpl.getServices().add(EjbServices.class, ejbServices);
            }
        } else {
            deploymentImpl.scanArchive(archive, ejbs);
        }

        WebBundleDescriptor wDesc = context.getModuleMetaData(WebBundleDescriptor.class);
        if( wDesc != null) {
            wDesc.setExtensionProperty(WELD_EXTENSION, "true");
            // Add the Weld Listener if it does not already exist..
            wDesc.addAppListenerDescriptor(new AppListenerDescriptorImpl(WELD_LISTENER));
        }

        BundleDescriptor bundle = (wDesc != null) ? wDesc : ejbBundle;
        if( bundle != null ) {

            BeanDeploymentArchive bda = deploymentImpl.getBeanDeploymentArchiveForArchive(archive.getURI().getPath());

            // Register EE injection manager at the bean deployment archive level.
            // We use the generic InjectionService service to handle all EE-style
            // injection instead of the per-dependency-type InjectionPoint approach.
            // Each InjectionServicesImpl instance knows its associated GlassFish bundle.

            InjectionManager injectionMgr = habitat.getByContract(InjectionManager.class);
            InjectionServices injectionServices = new InjectionServicesImpl(injectionMgr, bundle);

            bda.getServices().add(InjectionServices.class, injectionServices);
            bundleToBeanDeploymentArchive.put(bundle, bda); 
        }
        
        WeldApplicationContainer wbApp = new WeldApplicationContainer(bootstrap);

        // Stash the WeldBootstrap instance, so we may access the WeldManager later..
        context.addTransientAppMetaData(WELD_BOOTSTRAP, bootstrap);

        context.addTransientAppMetaData(WELD_DEPLOYMENT, deploymentImpl);

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

    private DeploymentImpl buildDeploymentGraph(DeploymentImpl deploymentImpl) {
        List<BeanDeploymentArchive> jarBDAs = deploymentImpl.getJarBeanDeploymentArchives();
        ListIterator jarIter = jarBDAs.listIterator();
        while (jarIter.hasNext()) {
            boolean modifiedArchive = false;
            BeanDeploymentArchive jarBDA = (BeanDeploymentArchive)jarIter.next();
            ListIterator jarIter1 = jarBDAs.listIterator();
            while (jarIter1.hasNext()) {
                BeanDeploymentArchive jarBDA1 = (BeanDeploymentArchive)jarIter1.next();
                if (jarBDA1.getId().equals(jarBDA.getId())) {
                    continue;
                }
                jarBDA.getBeanDeploymentArchives().add(jarBDA1);
                modifiedArchive = true;
            }
            if (modifiedArchive) {
                int idx = deploymentImpl.getBeanDeploymentArchives().indexOf(jarBDA);
                if (idx >= 0) {
                    deploymentImpl.getBeanDeploymentArchives().remove(idx);
                    deploymentImpl.getBeanDeploymentArchives().add(jarBDA);
                }
                modifiedArchive = false;
            }
        }            
            
        List<BeanDeploymentArchive> warBDAs = deploymentImpl.getWarBeanDeploymentArchives();
        ListIterator lIter = warBDAs.listIterator();
        boolean modifiedArchive = false;
        while (lIter.hasNext()) {
            BeanDeploymentArchive warBDA = (BeanDeploymentArchive)lIter.next();
            jarBDAs = deploymentImpl.getJarBeanDeploymentArchives();
            ListIterator lIter1 = jarBDAs.listIterator();
            while (lIter1.hasNext()) {
                BeanDeploymentArchive jarBDA = (BeanDeploymentArchive)lIter1.next();
                warBDA.getBeanDeploymentArchives().add(jarBDA);
                modifiedArchive = true;
            }
            if (modifiedArchive) {
                int idx = deploymentImpl.getBeanDeploymentArchives().indexOf(warBDA);
                if (idx >= 0) {
                    deploymentImpl.getBeanDeploymentArchives().remove(idx);
                    deploymentImpl.getBeanDeploymentArchives().add(warBDA);
                }
                modifiedArchive = false;
            }
        }
        return deploymentImpl;
    }
}


