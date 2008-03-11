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


import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.DeploymentContext;
import org.jvnet.hk2.annotations.Service;

import java.util.Set;

/**
 * Ejb container service
 *
 * @author Mahesh Kannan
 */
@Service(name = "ejb")
public class EjbApplication
        implements ApplicationContainer<EjbBundleDescriptor> {

    Object ejbContainerFactory;
    EjbBundleDescriptor bundleDesc;
    ClassLoader ejbAppClassLoader;

    public EjbApplication(
            EjbBundleDescriptor bundleDesc, DeploymentContext dc,
            ClassLoader cl) {
        this.ejbContainerFactory = ejbContainerFactory;                   
        this.bundleDesc = bundleDesc;
        this.ejbAppClassLoader = cl;
    }

    public EjbBundleDescriptor getDescriptor() {
        return bundleDesc;
    }

    public boolean start() {

        Set<EjbDescriptor> descs = (Set<EjbDescriptor>) bundleDesc.getEjbs();

        long appUniqueID = bundleDesc.getUniqueId();
        if (appUniqueID == 0) {
            System.out.println("*** EjbApp::start() => ASSIGNING RANDOM uniqueID..");
            appUniqueID = (System.currentTimeMillis() & 0xFFFFFFFF) << 16;
        }
        System.out.println("*** EjbApp::start() => " + bundleDesc.getApplication().getName()
                + "; uID => " + appUniqueID);

        //System.out.println("**CL => " + bundleDesc.getClassLoader());
        int counter = 0;
        for (EjbDescriptor desc : descs) {
            desc.setUniqueId(appUniqueID + (counter++));
            System.out.println("==>UniqueID: " + desc.getUniqueId() + " ==> "
                    + desc.getName() + "   isLocal: " + desc.toString());

            //ejbContainerFactory.createContainer(desc, ejbAppClassLoader, (Object) null, (Object) null);
        }

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
