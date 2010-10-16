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

package org.glassfish.embeddable;

/**
 * This is our primary interface to communicate with GlassFish,
 * All the methods in this interface are applicable to all types of GlassFish runtimes.
 * It provides necessary life cycle operations as well as it acts as a component registry.
 *
 * Concurrency Note:
 * This interface can be used concurrently.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public interface GlassFish {
    /**
     * Start GlassFish.
     * When this method is called, all the lifecycle (aka startup) services are started.
     */
    void start() throws GlassFishException;

    /**
     * Stop GlassFish. When this method is called, all the lifecycle (aka startup) services are stopped.
     * GlassFish can be started again by calling the start method.
     */
    void stop() throws GlassFishException;

    /**
     * Call this method if you don't need this GlassFish instance any more. This method will stop GlassFish
     * if not already stopped. After this method is called, calling any method except {@link #getStatus}
     * on the GlassFish object will cause an IllegalStateException to be thrown. When this method is called,
     * any resource (like temporary files, threads, etc.) is also released.
     */
    void dispose() throws GlassFishException;

    /**
     * @return Status of GlassFish
     */
    Status getStatus() throws GlassFishException;

    /**
     * A service has a service interface and optionally a name. For a service which is just a class with no interface,
     * then the service class is the service interface. This method is used to look up a service.
     * @param serviceType type of component required.
     * @param servicetName name of the component. Pass null if any component will fit the bill.
     * @param <T>
     * @return Return a service matching the requirement, null if no service found.
     */
    <T> T lookupService(Class<T> serviceType, String servicetName) throws GlassFishException;

    /**
     * Gets a Deployer instance to deploy an application.
     * Each invocation of this method returns a new Deployer object.
     * Calling this method is equivalent to calling <code>lookupService(Deployer.class, null)</code>
     *
     * @return A new Deployer instance
     */
    Deployer getDeployer() throws GlassFishException;

    /**
     * Gets a CommandRunner instance, using which the user can run asadmin commands.
     * Calling this method is equivalent to calling <code>lookupService(CommandRunner.class, null)</code>
     * Each invocation of this method returns a new CommandRunner object.
     *
     * @return a new CommandRunner instance
     */
    CommandRunner getCommandRunner() throws GlassFishException;

    /**
     * Status of GlassFish object.
     */
    enum Status {
        // Because GlassFish can sometimes take time to start or stop, we have STARTING and STOPPING states.
        INIT, STARTING, STARTED, STOPPING, STOPPED, DISPOSED
    }
}
