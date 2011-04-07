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

package org.glassfish.osgiejb;

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import org.glassfish.api.ActionReport;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.osgijavaeebase.*;
import org.glassfish.server.ServerEnvironmentImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiEJBDeployer extends AbstractOSGiDeployer {

    private static Logger logger = Logger.getLogger(OSGiEJBDeployer.class.getPackage().getName());

    private EJBTracker ejbTracker;

    private final InitialContext ic;

    public OSGiEJBDeployer(BundleContext ctx) {
        super(ctx, Integer.MIN_VALUE);
        try {
            ic = new InitialContext();
        } catch (NamingException e) {
            throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
        }
        ejbTracker = new EJBTracker();
        ejbTracker.open(true);
    }

    public OSGiUndeploymentRequest createOSGiUndeploymentRequest(Deployment deployer, ServerEnvironmentImpl env, ActionReport reporter, OSGiApplicationInfo osgiAppInfo) {
        return new OSGiEJBUndeploymentRequest(deployer, env, reporter, osgiAppInfo);
    }

    public OSGiDeploymentRequest createOSGiDeploymentRequest(Deployment deployer, ArchiveFactory archiveFactory, ServerEnvironmentImpl env, ActionReport reporter, Bundle b) {
        return new OSGiEJBDeploymentRequest(deployer, archiveFactory, env, reporter, b);
    }

    public boolean handles(Bundle bundle) {
        return isEJBBundle(bundle);
    }

    /**
     * Determines if a bundle represents a EJB application or not.
     * We determine this by looking at presence of Application-Type manifest header.
     *
     * @param b
     * @return
     */
    private boolean isEJBBundle(Bundle b) {
        final Dictionary headers = b.getHeaders();
        return headers.get(Constants.EXPORT_EJB) != null &&
                headers.get(org.osgi.framework.Constants.FRAGMENT_HOST) == null;
    }

    /**
     * An EJBTracker is responsible for registering the desired EJBs in OSGi service registry.
     * It is only applicable for OSGi enabled EJB bundles. Everytime such a bundle gets
     * deployed, OSGiContainer registers an {@link org.glassfish.osgijavaeebase.OSGiApplicationInfo}.
     * This class tracks such an object and queries its manifest for {@link Constants#EXPORT_EJB} header.
     * Based on the value of the header, it selects EJBs to be registered as OSGi services. To keep the
     * implementation simple at this point, we only support mapping of stateless EJBs with local business interface
     * views to OSGi services. When an EJB is registered as service, the service properties include
     * the portable JNDI name of the EJB in a service property names {@link #JNDI_NAME_PROP}.
     * All the services are registered under the bundle context of the OSGi/EJB bundle which hosts the EJBs.
     * While registering the EJBs, thread's context class loader is also set to the application class loader of
     * the OSGi/EJB bundle application so that any service tracker (like CDI producer methods) listening
     * to service events will get called in an appropriate context.
     */
    class EJBTracker extends ServiceTracker {
        // TODO(Sahoo): More javadoc needed about service properties and service registration
        private final String JNDI_NAME_PROP = "jndi-name";

        /**
         * Maps bundle id to service registrations
         */
        private Map<Long, Collection<ServiceRegistration>> b2ss =
                new ConcurrentHashMap<Long, Collection<ServiceRegistration>>();
        private ServiceRegistration reg;

        EJBTracker() {
            super(getBundleContext(), OSGiApplicationInfo.class.getName(), null);
        }

        @Override
        public Object addingService(ServiceReference reference) {
            OSGiApplicationInfo osgiApplicationInfo = OSGiApplicationInfo.class.cast(context.getService(reference));
            String exportEJB = (String) osgiApplicationInfo.getBundle().getHeaders().get(Constants.EXPORT_EJB);
            if (exportEJB != null) {
                // remove spaces. I once spent 1 hour trying to debug why EJB was not getting registered
                // and it turned out that user had specified "ALL " in the manifest.
                exportEJB = exportEJB.trim();
                ApplicationInfo ai = osgiApplicationInfo.getAppInfo();
                Application app = ai.getMetaData(Application.class);
                Collection<EjbDescriptor> ejbs = app.getEjbDescriptors();
                logger.info("addingService: Found " + ejbs.size() + " no. of EJBs");
                Collection<EjbDescriptor> ejbsToBeExported = new ArrayList<EjbDescriptor>();
                if (Constants.EXPORT_EJB_ALL.equals(exportEJB)) {
                    ejbsToBeExported = ejbs;
                } else {
                    StringTokenizer st = new StringTokenizer(exportEJB, ",");
                    while (st.hasMoreTokens()) {
                        String next = st.nextToken();
                        for (EjbDescriptor ejb : ejbs) {
                            if (next.equals(ejb.getName())) {
                                ejbsToBeExported.add(ejb);
                            }
                        }
                    }
                }
                b2ss.put(osgiApplicationInfo.getBundle().getBundleId(), new ArrayList<ServiceRegistration>());
                ClassLoader oldTCC = switchTCC(osgiApplicationInfo);
                try {
                    for (EjbDescriptor ejb : ejbsToBeExported) {
                        registerEjbAsService(ejb, osgiApplicationInfo.getBundle());
                    }
                } finally {
                    Thread.currentThread().setContextClassLoader(oldTCC);
                }
            }
            return osgiApplicationInfo;
        }

        /**
         *
         * @param osgiApplicationInfo application which just got deployed
         * @return the old thread context classloader
         */
        private ClassLoader switchTCC(OSGiApplicationInfo osgiApplicationInfo) {
            ClassLoader newTCC = osgiApplicationInfo.getClassLoader();
            final Thread thread = Thread.currentThread();
            ClassLoader oldTCC = thread.getContextClassLoader();
            thread.setContextClassLoader(newTCC);
            return oldTCC;
        }

        private void registerEjbAsService(EjbDescriptor ejb, Bundle bundle) {
            System.out.println(ejb);
            try {
                if (EjbSessionDescriptor.TYPE.equals(ejb.getType())) {
                    EjbSessionDescriptor sessionBean = EjbSessionDescriptor.class.cast(ejb);
                    if (EjbSessionDescriptor.STATEFUL.equals(sessionBean.getSessionType())) {
                        logger.warning("Stateful session bean can't be registered as OSGi service");
                    } else {
                        final BundleContext ejbBundleContext = bundle.getBundleContext();
                        for (String lbi : sessionBean.getLocalBusinessClassNames()) {
                            String jndiName = sessionBean.getPortableJndiName(lbi);
                            Object service = null;
                            try {
                                service = ic.lookup(jndiName);
                            } catch (NamingException e) {
                                e.printStackTrace();
                            }
                            Properties props = new Properties();
                            props.put(JNDI_NAME_PROP, jndiName);

                            // Note: we register using the bundle context of the bundle containing the EJB.
                            reg = ejbBundleContext.registerService(lbi, service, props);
                            b2ss.get(bundle.getBundleId()).add(reg);
                        }
                    }
                } else {
                    logger.warning("Only stateless bean or singleton beans can be registered as OSGi service");
                }
            } catch (Exception e) {
                logger.logp(Level.SEVERE, "OSGiEJBDeployer$EJBTracker", "registerEjbAsService", "Exception registering service for ejb by name", e);
            }
        }

        @Override
        public void removedService(ServiceReference reference, Object service) {
            /*
             * When the OSGi-EJB container goes away, the ejb bundle remains in ACTIVE state, so
             * we must unregister the services that OSGi-EJB container has registered on that bundle's behalf.
             */

            OSGiApplicationInfo osgiApplicationInfo = OSGiApplicationInfo.class.cast(service);
            ApplicationInfo ai = osgiApplicationInfo.getAppInfo();
            Application app = ai.getMetaData(Application.class);
            Collection<EjbDescriptor> ejbs = app.getEjbDescriptors();
            logger.info("removedService: Found " + ejbs.size() + " no. of EJBs");
            final Collection<ServiceRegistration> regs = b2ss.get(osgiApplicationInfo.getBundle().getBundleId());
            if (regs != null) { // it can be null if this bundle is not an OSGi-EJB bundle.
                for (ServiceRegistration reg : regs) {
                    if (reg != null) {
                        try {
                            reg.unregister();
                        } catch (Exception e) {
                            // If the underlying bundle is stopped, then the services registered for that context
                            // would have already been unregistered, so an IllegalStateException can be raised here.
                            // log it in FINE level and ignore.
                            logger.logp(Level.FINE, "OSGiEJBDeployer$EJBTracker", "removedService",
                                    "Exception unregistering " + reg, e);
                        }
                    }
                }
            }
            super.removedService(reference, service);
        }
    }
}
