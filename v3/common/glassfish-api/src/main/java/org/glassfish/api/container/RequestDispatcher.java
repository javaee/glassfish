/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

package org.glassfish.api.container;

import org.jvnet.hk2.annotations.Contract;
import org.glassfish.api.deployment.ApplicationContainer;
import com.sun.grizzly.tcp.Adapter;

import java.util.Collection;

/**
 * RequestDispatcher is responsible for dispatching incoming requests.
 *
 * @author Jerome Dochez
 */
@Contract
public interface RequestDispatcher {

    /*
     * Registers a new endpoint (proxy implementation) for a particular
     * context-root. All request coming with the context root will be dispatched
     * to the proxy instance passed in.
     * @param contextRoot for the proxy
     * @param endpointAdapter servicing requests.
     */
    public void registerEndpoint(String contextRoot, Adapter endpointAdapter,
                                 ApplicationContainer container) throws EndpointRegistrationException;


   /*
     * Registers a new endpoint (proxy implementation) for a particular
     * context-root. All request coming with the context root will be dispatched
     * to the proxy instance passed in.
     * @param contextRoot for the proxy
     * @param endpointAdapter servicing requests.
     * @param application container
     * @param virtualServers comma separated list of the virtual servers
     */
    public void registerEndpoint(String contextRoot, Adapter endpointAdapter,
        ApplicationContainer container, String virtualServers) throws EndpointRegistrationException;


    /*
     * Registers a new endpoint (proxy implementation) for a particular
     * context-root. All request coming with the context root will be dispatched
     * to the proxy instance passed in.
     * @param contextRoot for the proxy
     * @param endpointAdapter servicing requests.
     */
    public void registerEndpoint(String contextRoot,
                                 Collection<String> vsServers,
                                 Adapter endpointAdapter,
                                 ApplicationContainer container) throws EndpointRegistrationException;


    /**
     * Registers a new endpoint for the given context root at the given port
     * number.
     */
    public void registerEndpoint(String contextRoot,
                                 int port,
                                 Collection<String> vsServers,
                                 Adapter endpointAdapter,
                                 ApplicationContainer container) throws EndpointRegistrationException;


    /**
     * Removes the contex-root from our list of endpoints.
     */
    public void unregisterEndpoint(String contextRoot) throws EndpointRegistrationException;

    /**
     * Removes the contex-root from our list of endpoints.
     */
    public void unregisterEndpoint(String contextRoot,
            ApplicationContainer app) throws EndpointRegistrationException;
    
}
