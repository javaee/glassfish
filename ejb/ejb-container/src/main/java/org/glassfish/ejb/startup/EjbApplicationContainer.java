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
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.jvnet.hk2.annotations.Service;


/**
 * Ejb container service
 *
 * @author Mahesh Kannan
 */
@Service(name="ejb")
public class EjbApplicationContainer
    implements ApplicationContainer<EjbBundleDescriptor> {

    Object containerFactory;
    EjbBundleDescriptor bundleDesc;

    public EjbApplicationContainer(Object containerFactory, EjbBundleDescriptor bundleDesc) {
        this.containerFactory = containerFactory;
        this.bundleDesc = bundleDesc;
    }

    public boolean start(ApplicationContext startupContext) {
        return true;
    }

    public boolean stop(ApplicationContext stopContext) {
        return false;
    }

    /**
     * Suspends this application container.
     *
     * @return true if suspending was successful, false otherwise.
     */
    public boolean suspend() {
        // Not (yet) supported
        return false;
    }

    /**
     * Resumes this application container.
     *
     * @return true if resumption was successful, false otherwise.
     */
    public boolean resume() {
        // Not (yet) supported
        return false;
    }

    public EjbBundleDescriptor getDescriptor() {
        return bundleDesc;
    }

    /**
     * Returns the class loader associated with this application
     *
     * @return ClassLoader for this app
     */
    public ClassLoader getClassLoader() {
        return bundleDesc.getClassLoader();
    }

}
