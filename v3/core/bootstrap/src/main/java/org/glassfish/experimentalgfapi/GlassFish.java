/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.experimentalgfapi;

/**
 * This is our primary interface to communicate with the server,
 * It provides necessary life cycle operations as well as it acts as a component registry.
 *
 * Concurrency Note:
 * This interface can be used concurrently.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public interface GlassFish {
    /**
     * Start the server. When this method is called, all the lifecycle (aka startup) services are started.
     */
    void start();

    /**
     * Stop the server. When this method is called, all the lifecycle (aka startup) services are stopped.
     * After the server is stopped, the server can be started again by calling the start method.
     */
    void stop();

    /**
     * Call this method if you don't need the server object any more. This method will stop the server
     * if not already stopped. After this method is called, calling any method except {@link #getStatus}
     * on the server object will cause an IllegalStateException to be thrown. When this method is called,
     * any resource (like temporary files, threads, etc.) is also released.
     */
    void dispose();

    /**
     * @return Status of GlassFish
     */
    Status getStatus();

    /**
     * Look up a service
     * @param serviceType type of component required.
     * @param servicetName name of the component. Pass null if any component will fit the bill.
     * @param <T>
     * @return Return a component matching the requirement, null if no component found.
     */
    <T> T lookupService(Class<T> serviceType, String servicetName);

    enum Status {
        // Because server can take time to start or stop, we have STARTING and STOPPING states.
        INIT, STARTING, STARTED, STOPPING, STOPPED, DISPOSED
    }
}
