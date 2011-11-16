/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.javaee.services;

import com.sun.grizzly.config.ConfigAwareElement;
import com.sun.grizzly.config.dom.NetworkListener;
import com.sun.grizzly.http.ProcessorTaskFactory;
import com.sun.grizzly.http.ajp.AjpConfiguration;
import com.sun.grizzly.http.ajp.ShutdownHandler;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.ContractProvided;
import org.jvnet.hk2.component.Habitat;
/**
 * Native Grizzly AJP service.
 *
 * @author Alexey Stashok
 */
@Service(name="grizzly-ajp")
@ContractProvided(ProcessorTaskFactory.class)
public class AjpProcessorTaskFactoryProvider
        extends com.sun.grizzly.http.ajp.AjpProcessorTaskFactory
        implements ConfigAwareElement<NetworkListener> {

    
    public AjpProcessorTaskFactoryProvider() {
        super(new AjpConfigurationImpl());
    }

    @Override
    public void configure(final Habitat habitat,
            final NetworkListener configuration) {
        final String configFileName = configuration.getJkConfigurationFile();
    }
    
    private final static class AjpConfigurationImpl implements AjpConfiguration {

        private final Queue<ShutdownHandler> shutdownHandlers =
                new ConcurrentLinkedQueue<ShutdownHandler>();
        private String secret;
        private boolean isTomcatAuthentication = true;

        /**
         * {@inheritDoc}
         */
        @Override
        public Queue<ShutdownHandler> getShutdownHandlers() {
            return shutdownHandlers;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isTomcatAuthentication() {
            return isTomcatAuthentication;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setTomcatAuthentication(boolean isTomcatAuthentication) {
            this.isTomcatAuthentication = isTomcatAuthentication;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSecret() {
            return secret;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setSecret(String requiredSecret) {
            this.secret = requiredSecret;
        }
    }
}
