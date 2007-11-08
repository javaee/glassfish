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

package com.sun.enterprise.web;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.webservice.monitoring.*;
import org.jvnet.hk2.component.Habitat;

import java.util.Iterator;


/**
 * Listener for EJB webservice endpoint registrations and unregistrations.
 *
 * Upon receiving an EJB webservice endpoint registration event, this
 * listener will register the EJB webservice endpoint's path as an ad-hoc
 * path with the web container, along with information about the 
 * ad-hoc servlet responsible for servicing any requests on this path.
 *
 * Upon receiving an EJB webservice endpoint unregistration event, this
 * listener will unregister the EJB webservice endpoint's path as an 
 * ad-hoc path from the web container.
 *
 * @author Jan Luehe
 */
public class EjbWebServiceRegistryListener
        implements EndpointLifecycleListener {


    private static final EjbWebServiceServletInfo
        EJB_WEB_SERVICE_SERVLET_INFO = new EjbWebServiceServletInfo();


    private WebContainer webContainer;

    /**
     * Constructor.
     *
     * @param webContainer The web container with which this 
     * EjbWebServiceRegistryListener is associated
     */
    public EjbWebServiceRegistryListener(WebContainer webContainer) {
        this.webContainer = webContainer;
    }


    /**
     * Registers this EjbWebServiceRegistryListener with the EJB webservice
     * registry.
     */
    public void register(Habitat habitat) {

        WebServiceEngine wsEngine = habitat.getByContract(WebServiceEngine.class);
        if (wsEngine==null) {
            // no web service in this container...
            return;
        }

        wsEngine.addLifecycleListener(this);

        /*
         * Process any webservice endpoints that had been added before we've
         * started listening for registration events.
         */
        Iterator<Endpoint> endpoints = wsEngine.getEndpoints();
        if (endpoints != null) {
            while (endpoints.hasNext()) {
                endpointAdded(endpoints.next());
            }
        }
    }


    /**
     * Unregisters this EjbWebServiceRegistryListener from the EJB webservice
     * registry.
     */
    public void unregister(Habitat habitat) {
        
        WebServiceEngine wsEngine = habitat.getByContract(WebServiceEngine.class);
        if (wsEngine==null) {
            // no web service in this container...
            return;
        }
        wsEngine.removeLifecycleListener(this);
    }


    /**
     * Receives and processes notification of a new webservice endpoint
     * installation in the appserver.
     *
     * This method extracts the context root from the endpoint's URL, at
     * which it registers an EjbWebServiceWebModule which handles all HTTP
     * requests for the endpoint.
     *
     * @param endpoint The EJB webservice endpoint that was added
     */
    public void endpointAdded(Endpoint endpoint) {

        if (!TransportType.HTTP.equals(endpoint.getTransport())) {
            return;
        }

        if (!EndpointType.EJB_ENDPOINT.equals(endpoint.getEndpointType())) {
            return;
        }

        String epURI = endpoint.getEndpointSelector();
        String epCtxRoot = null;
        String epPath = null;

        int index = epURI.indexOf('/', 1);
        if (index < 0) {
            epCtxRoot = epURI;
            epPath = "";
        } else {
            epCtxRoot = epURI.substring(0, index);
            epPath = epURI.substring(index);
        }

        String epSubtree = epPath + "/__container$publishing$subctx/";

        String epAppName = null;
        WebServiceEndpoint wse = endpoint.getDescriptor();
        if (wse != null) {
            BundleDescriptor bd = wse.getBundleDescriptor();
            if (bd != null) {
                Application app = bd.getApplication();
                if (app != null) {
                    epAppName = app.getRegistrationName();
                }
            }
        }

        webContainer.registerAdHocPathAndSubtree(
            epPath,
            epSubtree,
            epCtxRoot,
            epAppName,
            EJB_WEB_SERVICE_SERVLET_INFO);
    }
    
    /**
     * Receives and processes notification of a webservice endpoint
     * removal from the appserver.
     *
     * This method extracts the context root from the endpoint's URL, and
     * unregisters the corresponding EjbWebServiceWebModule from the web
     * container
     *
     * @param endpoint The EJB webservice endpoint that was removed
     */
    public void endpointRemoved(Endpoint endpoint) {
  
        if (!TransportType.HTTP.equals(endpoint.getTransport())) {
            return;
        }

        if (!EndpointType.EJB_ENDPOINT.equals(endpoint.getEndpointType())) {
            return;
        }

        String epURI = endpoint.getEndpointSelector();
        String epCtxRoot = null;
        String epPath = null;
        int index = epURI.indexOf('/', 1);
        if (index < 0) {
            epCtxRoot = epURI;
            epPath = "";
        } else {
            epCtxRoot = epURI.substring(0, index);
            epPath = epURI.substring(index);
        }

        String epSubtree = epPath + "/__container$publishing$subctx/";
        webContainer.unregisterAdHocPathAndSubtree(epPath, epSubtree,
                                                   epCtxRoot);
    }

}
