/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.ejb.startup;

import com.sun.ejb.ContainerFactory;
import com.sun.ejb.Container;
import com.sun.ejb.containers.ContainerFactoryImpl;
import com.sun.ejb.containers.BaseContainer;
import com.sun.enterprise.deployment.EjbDescriptor;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.DeploymentContext;
import org.jvnet.hk2.annotations.Service;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Ejb container service
 *
 * @author Mahesh Kannan
 */
@Service(name = "ejb")
public class EjbApplication
        implements ApplicationContainer<Collection<EjbDescriptor>> {

    String appName;
    ContainerFactory ejbContainerFactory;
    Collection<EjbDescriptor> ejbs;
    Collection<Container> containers = new ArrayList();
    ClassLoader ejbAppClassLoader;
    DeploymentContext dc;

    // TODO: move restoreEJBTimers to correct location
    private static boolean restored = false;
    private static Object lock = new Object();
    // TODO: move restoreEJBTimers to correct location

    public EjbApplication(
            Collection<EjbDescriptor> bundleDesc, DeploymentContext dc,
            ClassLoader cl) {
        this.ejbContainerFactory = new ContainerFactoryImpl();
        this.ejbs = bundleDesc;
        this.ejbAppClassLoader = cl;
        this.appName = ""; //TODO
        this.dc = dc;
    }

    public Collection<EjbDescriptor> getDescriptor() {
        return ejbs;
    }

    public boolean start(ClassLoader cl) {
        /*
        Set<EjbDescriptor> descs = (Set<EjbDescriptor>) bundleDesc.getEjbs();

        long appUniqueID = ejbs.getUniqueId();
        */
        long appUniqueID = 0;
        if (appUniqueID == 0) {
            appUniqueID = (System.currentTimeMillis() & 0xFFFFFFFF) << 16;
        }

        //System.out.println("**CL => " + bundleDesc.getClassLoader());
        int counter = 0;
        for (EjbDescriptor desc : ejbs) {
            desc.setUniqueId(appUniqueID + (counter++));
            try {
                Container container = ejbContainerFactory.createContainer(desc, ejbAppClassLoader,
                    null, dc);
                containers.add(container);
            } catch (Throwable th) {
                throw new RuntimeException("Error during EjbApplication.start() ", th);
            }
        }

        for (Container container : containers) {
            container.doAfterApplicationDeploy();
        }

        // TODO: move restoreEJBTimers to correct location
        synchronized(lock) {
            System.out.println("==> Restore Timers? == " + restored);
            if (!restored) {
                com.sun.ejb.containers.EJBTimerService ejbTimerService = 
                    com.sun.ejb.containers.EjbContainerUtilImpl.getInstance().getEJBTimerService();
                if (ejbTimerService != null) {
                    restored = ejbTimerService.restoreEJBTimers();
                    System.out.println("==> Restored Timers? == " + restored);
                }
            }
        }
        // TODO: move restoreEJBTimers to correct location

        return true;
    }

    public boolean stop() {
        return false;
    }

    /**
     * Returns the class loader associated with this application
     *
     * @return ClassLoader for this app
     */
    public ClassLoader getClassLoader() {
        return ejbAppClassLoader;
    }

}
