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

package com.sun.enterprise.server.ondemand;


import java.util.logging.Logger;
import java.util.logging.Level;
import javax.management.ObjectName;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.config.*;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.appserv.server.ServerLifecycle;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.enterprise.server.ondemand.entry.EntryContext;
import com.sun.enterprise.server.ondemand.entry.EntryPoint;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.appclient.jws.AppclientJWSSupportManager;

/**
 * Represents the group services needed by web apps. The main components
 * of this servicegroup Webcontainer and admin apps.
 *
 * @author Binod PG
 * @see ServiceGroup
 * @see ServiceGroupBuilder
 */
public class WebServiceGroup extends ServiceGroup {

    /**
     * Triggers the start of the servicegroup. The entry context
     * that caused this startup is used by the servicegroup to obtain
     * any startup information it require.
     * 
     * @param context EntryContext object.
     * @see EntryContext.
     */
    public void start(EntryContext context) throws ServiceGroupException {
        try {
            startLifecycleServices(context.getServerContext());
            loadSystemApps();
            AppclientJWSSupportManager.getInstance().
            startJWSServicesForDeployedAppclients();
            setState(ServiceGroup.STARTED);
        } catch (Exception e) {
            throw new ServiceGroupException (e);
        }
    }

    /**
     * Loads all the system apps belongs to this servicegroup.
     * @see OnDemandServices
     * @see SystemAppLoader
     */
    private void loadSystemApps() {
        SystemAppLoader loader = OnDemandServer.getSystemAppLoader();
        loader.loadSystemApps(loader.getWebServiceGroupSystemApps());
    }

    /**
     * Analyse the entrycontext and specifies whether this servicegroup
     * can be started or not.
     *
     * @return boolean If true is returned, this servicegroup can be started
     * If false is returned, the entrycontext  is not recognized by the 
     * servicegroup.
     */
    public boolean analyseEntryContext( EntryContext context ) {

        if (_logger.isLoggable(Level.FINER)) {
            _logger.log(Level.FINER, 
            "Analysing the context in Web ServiceGroup :" + context);
        }

        if (context.get() == null) {
            return false;
        }

        if ( context.getEntryPointType() == EntryPoint.JNDI ) {
            /*
            no.op. You cant access this servicegroup via JNDI.
            */
            return false;
        }

        boolean result = false;
        try {
            ConfigContext ctxt = context.getServerContext().getConfigContext();
            Config conf = ServerBeansFactory.getConfigBean( ctxt );

            if (context.getEntryPointType() == EntryPoint.APPLOADER ) {
                Descriptor desc = (Descriptor) context.get();
                if (desc instanceof Application) {
                    // has atleast one webcomponent or 
                    // Atleast one component has webservice       
                    result = !((Application) desc).getWebBundleDescriptors().isEmpty() || 
                             !((Application) desc).getWebServiceDescriptors().isEmpty(); 
                } else if (desc instanceof EjbBundleDescriptor) {
                    result = ((EjbBundleDescriptor) desc).hasWebServices();
                } else if (desc instanceof EjbAbstractDescriptor) {
                    result = ((EjbAbstractDescriptor) desc).hasWebServiceEndpointInterface();
                } else {
                    result = desc instanceof WebBundleDescriptor || 
                             desc instanceof WebServicesDescriptor;
                } 
            }

            if ( context.getEntryPointType() == EntryPoint.PORT ) {
  	        // Start HTTP listener ports
	        HttpService httpService = conf.getHttpService();
	        HttpListener[] httpListeners = httpService.getHttpListener();
	        for ( int i=0; i<httpListeners.length; i++ ) {
	            int port = Integer.parseInt(httpListeners[i].getPort());
                    if (port == ((Integer) context.get()).intValue() ) {
                        result = true;
                    }
                }
	    }

            if (context.getEntryPointType() == EntryPoint.MBEAN) {
                result = analyseObjectName((ObjectName) context.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }

        return result;
    }

    // Does the objectname belongs to any of the system apps
    // in this servicegroup.
    private boolean analyseObjectName(ObjectName name) {

        /*
        if (name == null) {
            return true;
        }

        String cat = name.getKeyProperty("category");
        if (cat != null && cat.equals("monitor")) {
            return true;
        }
        */

        String type = name.getKeyProperty("type");
        if ((type != null) && type.equals("Loader")) {
             return true;
        }

        String j2eeType = name.getKeyProperty("j2eeType");
        if ((j2eeType != null) && 
             j2eeType.equals("WebModule")) {
             return true;
        }

        if (name.getKeyProperty("WebModule") != null) {
            return true;
        }

        String nameStr = name.getKeyProperty("name");
        String ref = name.getKeyProperty("ref");
        String app = name.getKeyProperty("J2EEApplication");

        return    belongsToThisServiceGroup(nameStr) || 
                  belongsToThisServiceGroup(ref) || 
                  belongsToThisServiceGroup(app); 


    }

    private boolean belongsToThisServiceGroup(String name) {
      SystemAppLoader appLoader = OnDemandServer.getSystemAppLoader();
        if (appLoader != null) {
            for (Object n : appLoader.getWebServiceGroupSystemApps()) {
                if (((String) n).equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }
 

    /**
     * Start lifecycles belonging to this service group.
     * @see OnDemandServices
     */
    private void startLifecycleServices(ServerContext context) {
        String[][] services = OnDemandServices.getWebServiceGroupServices();
        super.startLifecycleServices(services, context);
    }

    /**
     * Stop the servicegroup. It stops all the lifecycle modules belongs to this
     * servicegroup.
     */
    public void stop(EntryContext context) throws ServiceGroupException {
       super.stopLifecycleServices();
    }

    /**
     * Abort the servicegroup. This is not called from anywhere as of now.
     */
    public void abort(EntryContext context) {
       super.stopLifecycleServices();
    }
}
