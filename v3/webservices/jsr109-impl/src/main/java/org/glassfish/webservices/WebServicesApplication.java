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

package org.glassfish.webservices;

import com.sun.enterprise.config.serverbeans.Config;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.api.deployment.DeploymentContext;

import org.glassfish.api.admin.ServerEnvironment;

import org.glassfish.api.container.RequestDispatcher;
import org.glassfish.api.container.EndpointRegistrationException;

import java.util.Set;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URL;

import com.sun.logging.LogDomains;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.util.WebServerInfo;
import org.jvnet.hk2.component.Habitat;


/**
 * This class implements the ApplicationContainer and will be used
 * to register endpoints to the grizzly ServletAdapter
 * Thus when a request is received it is directed to our EjbWebServiceServlet
 * so that it can process the request
 *
 * @author Bhakti Mehta
 */

public class WebServicesApplication implements ApplicationContainer {

    private ArrayList<EjbEndpoint> ejbendpoints;

    private com.sun.grizzly.tcp.Adapter adapter;

    private final RequestDispatcher dispatcher;

    private final ServerEnvironment serverEnvironment;

    private DeploymentContext deploymentCtx;

    protected Logger logger = LogDomains.getLogger(this.getClass(),LogDomains.WEBSERVICES_LOGGER);

    private ResourceBundle rb = logger.getResourceBundle();

    private Config config = null;

    private Habitat habitat = null;

    private ClassLoader cl;
    private Application app;


    public WebServicesApplication(DeploymentContext context, ServerEnvironment env, RequestDispatcher dispatcherString, Config config, Habitat habitat){
        this.deploymentCtx = context;
        this.dispatcher = dispatcherString;
        this.serverEnvironment = env;
        this.ejbendpoints = getEjbEndpoints();
        this.adapter = (com.sun.grizzly.tcp.Adapter) new EjbWSAdapter();
        this.config = config;
        this.habitat = habitat;
    }
    
    public Object getDescriptor() {
        return null;
    }

    public boolean start(ApplicationContext startupContext) throws Exception {

        cl = startupContext.getClassLoader();

        try {
            app = deploymentCtx.getModuleMetaData(Application.class);
           if (!isJAXWSbasedApp(app)) {
                Iterator<EjbEndpoint> iter = ejbendpoints.iterator();
                EjbEndpoint ejbendpoint = null;
                while(iter.hasNext()) {
                   ejbendpoint = iter.next();
                   String contextRoot = ejbendpoint.contextRoot;

                   dispatcher.registerEndpoint(contextRoot, (com.sun.grizzly.tcp.Adapter)adapter, this);
                   logger.info(format(rb.getString("enterprise.deployment.ejbendpoint.registration"),
                               app.getAppName(),

                               new WsUtil().getWebServerInfoForDAS().getWebServerRootURL(ejbendpoint.isSecure).toString() + contextRoot)
                       );
               }
            }
        } catch (EndpointRegistrationException e) {
            logger.log(Level.SEVERE,  format(rb.getString("error.registering.endpoint"),e.toString()));
        }
        return true;
    }


    private ArrayList<EjbEndpoint> getEjbEndpoints() {
        ejbendpoints = new ArrayList<EjbEndpoint>();
        
        EjbEndpoint ejbendpoint = null;
        Application app = deploymentCtx.getModuleMetaData(Application.class);
        if (!isJAXWSbasedApp(app)) {
            Set<BundleDescriptor> bundles = app.getBundleDescriptors();
            for(BundleDescriptor bundle : bundles) {
                WebServicesDescriptor wsDesc = bundle.getWebServices();
                for (WebService ws : wsDesc.getWebServices()) {

                    for (WebServiceEndpoint endpoint:ws.getEndpoints() ){
                        //Only add for ejb based endpoints
                        if (endpoint.implementedByEjbComponent()) {
                            ejbendpoint = new EjbEndpoint(endpoint.getEndpointAddressUri(),endpoint.isSecure()) ;
                            ejbendpoints.add(ejbendpoint) ;
                        }
                    }
                }
            }
        }


        return ejbendpoints;
    }

    public boolean stop(ApplicationContext stopContext) {
        try {
            Iterator<EjbEndpoint> iter = ejbendpoints.iterator();
            String contextRoot = null;
            EjbEndpoint endpoint = null;
            while(iter.hasNext()) {
                endpoint = iter.next();
                contextRoot = endpoint.contextRoot;
                dispatcher.unregisterEndpoint(contextRoot);
            }
        } catch (EndpointRegistrationException e) {
            logger.log(Level.SEVERE,  format(rb.getString("error.unregistering.endpoint"),e.toString()));
            return false;
        }
        return true;
    }

    public boolean suspend() {
        return false;
    }

    public boolean resume() throws Exception {
        return false;
    }

    public ClassLoader getClassLoader() {
        return cl;
    }

    Application getApplication() {
        return app;
    }

    private boolean isJAXWSbasedApp(Application app){
        if ((app.getStandaloneBundleDescriptor() instanceof WebBundleDescriptor)
                            &&  ((!app.getStandaloneBundleDescriptor().getSpecVersion().equals("2.5")
                                  || (!app.getStandaloneBundleDescriptor().hasWebServices()) ) )
                            ) {
            //JAXWS based apps
            //do nothing
            return true;
        } else
           return false;
    }

    private String format(String key, String ... values){
        return MessageFormat.format(key, (Object[]) values);
    }

    class EjbEndpoint {
        private final String contextRoot;

        private boolean isSecure;

        EjbEndpoint(String contextRoot,boolean secure){
            this.contextRoot = contextRoot;
            this.isSecure = secure;
        }
    }

}
