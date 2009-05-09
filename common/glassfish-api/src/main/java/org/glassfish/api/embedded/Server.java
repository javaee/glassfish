/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.api.embedded;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.annotations.Contract;

import java.util.Collection;

/**
 * Defines a embedded Server, capable of attaching containers (entities running
 * users applications).
 *
 * @author Jerome Dochez
 */
@Contract
public interface Server {

    /**
     * Creates a new embedded container configuration of a type.
     *
     * @param configType the type of the embedded container configuration
     * @param <T> type of the embedded container
     * @return the configuration to configure a container of type <T>
     */
    public <T extends EmbeddedContainerInfo> T createConfig(Class<T> configType);

    /**
     * Adds a container to this server.
     *
     * Uing the configuration instance for the container of type <T>,
     * creating the container from that configuration and finally adding the
     * container instance to the list of managed containers
     *
     * @param info the configuration for the container
     * @param <T> type of the container
     * @return instance of the container <T>
     */
    public <T extends EmbeddedContainer> T addContainer(EmbeddedContainerInfo<T> info);

    /**
     * Returns a list of the currently managed containers
     *
     * @return the containers list
     */
    public Collection<EmbeddedContainer> getContainers();

    /**
     * Creates a port to attach to embedded containers. Ports can be attached to many
     * embedded containers and containers may accept more than one port.
     *
     * @param port port number for this port
     * @return a new port abstraction.
     */
    public Port createPort(int port);

    /**
     * Returns the configured habitat for this server.
     *
     * @return the habitat
     */
    public Habitat getHabitat();

    /**
     * Returns the container name, as specified in {@link ServerInfo#ServerInfo(String)}
     *
     * @return container name
     */
    public String getName();

    /**
     * Starts the server
     */
    public void start();

    /**
     * Stops the container
     */
    public void stop();    

}
