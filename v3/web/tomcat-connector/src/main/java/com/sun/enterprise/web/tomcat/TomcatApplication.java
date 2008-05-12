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

package com.sun.enterprise.web.tomcat;

import org.glassfish.api.deployment.ApplicationContainer;
import org.apache.catalina.core.StandardContext;

public class TomcatApplication implements ApplicationContainer {

    TomcatContainer container;
    StandardContext ctx;

    public TomcatApplication(TomcatContainer container, StandardContext ctx) {
        this.container = container;
        this.ctx = ctx;
    }

    public boolean start(ClassLoader cl) {
        return true;
    }

    public boolean stop() {
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

    /**
     * Returns the class loader associated with this application
     *
     * @return ClassLoader for this app
     */
    public ClassLoader getClassLoader() {
        return ctx.getLoader().getClassLoader();
    }

    TomcatContainer getContainer() {
        return container;
    }

    /**
     * Returns the deployment descriptor associated with this application
     *
     * @return deployment descriptor if they exist or null if not
     */
    public Object getDescriptor() {
        return null;
    }
}
