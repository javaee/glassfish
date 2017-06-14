/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.s1asdev.ejb.ejb32.mdb.ra;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author David Blevins
 */
public class CommandResourceAdapter implements ResourceAdapter {

    private Map<CommandActivationSpec, ActivatedEndpoint> endpoints = new HashMap<CommandActivationSpec, ActivatedEndpoint>();

    public void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
    }

    public void stop() {
    }

    public void endpointActivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) throws ResourceException {
        final CommandActivationSpec commandActivationSpec = (CommandActivationSpec) activationSpec;
        final ActivatedEndpoint activatedEndpoint = new ActivatedEndpoint(messageEndpointFactory, commandActivationSpec);
        endpoints.put(commandActivationSpec, activatedEndpoint);
        final Thread thread = new Thread(activatedEndpoint);
        thread.setDaemon(true);
        thread.start();
    }

    public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) {
        endpoints.remove((CommandActivationSpec) activationSpec);
    }

    public XAResource[] getXAResources(ActivationSpec[] activationSpecs) throws ResourceException {
        return new XAResource[0];
    }

    private static class ActivatedEndpoint implements Runnable {

        private final MessageEndpointFactory factory;
        private final List<Method> commands = new ArrayList<Method>();

        private ActivatedEndpoint(MessageEndpointFactory factory, CommandActivationSpec spec) {
            this.factory = factory;

            final Method[] methods = factory.getEndpointClass().getMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Command.class)) {
                    commands.add(method);
                }
            }

        }

        public void run() {
            pause();
            try {
                final MessageEndpoint endpoint = factory.createEndpoint(null);
                try {
                    for (Method method : commands) {
                        endpoint.beforeDelivery(method);
                        try {
                            method.invoke(endpoint);
                        } finally {
                            endpoint.afterDelivery();
                        }
                    }
                } finally {
                    endpoint.release();
                }
            } catch (Throwable e) {
                e.printStackTrace();
                // fail
            }
        }

        /**
         *  Have to wait till the application is fully started
         */
        private static void pause() {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
    }
}
