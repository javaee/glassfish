/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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


package org.glassfish.osgiejb;

import org.glassfish.osgijavaeebase.OSGiContainer;
import org.glassfish.osgijavaeebase.OSGiUndeploymentRequest;
import org.glassfish.osgijavaeebase.OSGiApplicationInfo;
import org.glassfish.osgijavaeebase.OSGiDeploymentRequest;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ModuleInfo;
import org.glassfish.server.ServerEnvironmentImpl;
import org.glassfish.api.ActionReport;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.*;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiEJBContainer extends OSGiContainer {

    private EJBTracker ejbTracker;

    private final InitialContext ic;

    public OSGiEJBContainer(BundleContext ctx) {
        super(ctx);
        try {
            ic = new InitialContext();
        } catch (NamingException e) {
            throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
        }
        ejbTracker = new EJBTracker();
        ejbTracker.open(true);
    }

    protected OSGiUndeploymentRequest createOSGiUndeploymentRequest(Deployment deployer, ServerEnvironmentImpl env, ActionReport reporter, OSGiApplicationInfo osgiAppInfo) {
        return new OSGiEJBUndeploymentRequest(deployer, env, reporter, osgiAppInfo);
    }

    protected OSGiDeploymentRequest createOSGiDeploymentRequest(Deployment deployer, ArchiveFactory archiveFactory, ServerEnvironmentImpl env, ActionReport reporter, Bundle b) {
        return new OSGiEJBDeploymentRequest(deployer, archiveFactory, env, reporter, b);
    }

    class EJBTracker extends ServiceTracker {
        EJBTracker() {
            super(getBundleContext(), OSGiApplicationInfo.class.getName(), null);
        }

        @Override
        public Object addingService(ServiceReference reference) {
            OSGiApplicationInfo osgiApplicationInfo = OSGiApplicationInfo.class.cast(context.getService(reference));
            String exportEJB = (String)osgiApplicationInfo.getBundle().getHeaders().get(Constants.EXPORT_EJB);
            if (exportEJB != null) {
                ApplicationInfo ai = osgiApplicationInfo.getAppInfo();
                Application app = ai.getMetaData(Application.class);
                Collection<EjbDescriptor> ejbs = app.getEjbDescriptors();
                System.out.println("addingService: Found " + ejbs.size() + " no. of EJBs");
                Collection<EjbDescriptor> ejbsToBeExported = new ArrayList<EjbDescriptor>();
                if (Constants.EXPORT_EJB_ALL.equals(exportEJB)) {
                    ejbsToBeExported = ejbs;
                } else {
                    StringTokenizer st = new StringTokenizer(exportEJB, ",");
                    while(st.hasMoreTokens()) {
                        String next = st.nextToken();
                        for (EjbDescriptor ejb : ejbs) {
                            if (next.equals(ejb.getName())) {
                                ejbsToBeExported.add(ejb);
                            }
                        }
                    }
                }

                for (EjbDescriptor ejb : ejbsToBeExported) {
                    registerEjbAsService(ejb, osgiApplicationInfo.getBundle());
                }
            }
            return osgiApplicationInfo;
        }

        private void registerEjbAsService(EjbDescriptor ejb, Bundle bundle) {
            System.out.println(ejb);
            if (EjbSessionDescriptor.TYPE.equals(ejb.getType())) {
                EjbSessionDescriptor sessionBean = EjbSessionDescriptor.class.cast(ejb);
                if (EjbSessionDescriptor.STATEFUL.equals(sessionBean.getSessionType())) {
                    System.out.println("Stateful session bean can't be registered as OSGi service");
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
                        props.put("jndi-name", jndiName);
                        // Note: we register using the bundle context of the bundle containing the EJB.
                        ejbBundleContext.registerService(lbi, service, props);
                    }
                }
            } else {
                System.out.println("Only stateless bean or singleton beans can be registered as OSGi service");
            }
        }

        @Override
        public void removedService(ServiceReference reference, Object service) {
            OSGiApplicationInfo osgiApplicationInfo = OSGiApplicationInfo.class.cast(context.getService(reference));
            ApplicationInfo ai = osgiApplicationInfo.getAppInfo();
            Application app = ai.getMetaData(Application.class);
            Collection<EjbDescriptor> ejbs = app.getEjbDescriptors();
            System.out.println("removedService: Found " + ejbs.size() + " no. of EJBs");
            for (EjbDescriptor ejb : ejbs) {
            }
        }
    }
}
