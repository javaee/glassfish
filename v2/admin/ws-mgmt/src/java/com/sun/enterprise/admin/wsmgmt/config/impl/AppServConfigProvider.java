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
package com.sun.enterprise.admin.wsmgmt.config.impl;

import com.sun.enterprise.admin.wsmgmt.config.spi.ConfigProvider;
import com.sun.enterprise.admin.wsmgmt.config.spi.ConfigFactory;
import com.sun.enterprise.admin.wsmgmt.config.spi.WebServiceConfig;

import java.util.List;
import java.util.ArrayList;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.server.ApplicationServer;

import com.sun.enterprise.config.serverbeans.WebServiceEndpoint;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.EjbModule;
import com.sun.enterprise.config.serverbeans.J2eeApplication;

/**
 * This is the mechanism to access web service management configuration. 
 * <br>
 */
public class AppServConfigProvider implements ConfigProvider 
{

    /**
     * Returns the unique identifier for this RepositoryProvider object.
     *
     * @return fully qualified class name of this RepositoryProvider
     */
    public String getProviderID() {
        return ConfigFactory.CONFIG_DEFAULT_PROVIDER;
    }

    /**
     * Returns the list of Web Service Endpoint config in this application 
     *
     * @param appName   Name of the application
     *
     * @return the array of WebServiceConfig
     */
    public WebServiceConfig[] getWebserviceConfigs(String appId) {
        WebServiceEndpoint[] wsps = getWebServiceEndpoints(appId);

        WebServiceConfig[] wsCfgs = new WebServiceConfig[wsps.length];
        for (int idx =0; idx < wsps.length; idx++) {
           wsCfgs[idx] = new WebServiceConfigImpl(wsps[idx]); 
        }
        return wsCfgs;
    }

    WebServiceEndpoint[] getWebServiceEndpoints(String appId) {
        WebServiceEndpoint[] wsps = null;
        try {
            ConfigContext ctx = ApplicationServer.getServerContext().
                                                getConfigContext();

            ConfigBean cb = ApplicationHelper.findApplication(ctx, appId);

            // check if web service endpoint is configured for this
            // application/module, then ruturn this app

            if ( cb instanceof WebModule) {
                wsps = ( (WebModule) cb).getWebServiceEndpoint();
            } else if ( cb instanceof EjbModule) {
                wsps = ( (EjbModule) cb).getWebServiceEndpoint();
            } else if (cb instanceof J2eeApplication) {
                wsps = ( (J2eeApplication) cb).getWebServiceEndpoint();
            }

        } catch (ConfigException ce) {
            // XX throw exception
        } finally {
            return wsps;
        }
    }

    /**
     * Returns the list of Web Service Endpoint config in this application 
     *
     * @param appName   Name of the application
     *
     * @return the array of WebServiceConfig
     */
    public WebServiceConfig getWebServiceConfig(String appId, String modId,
        boolean isStandalone, String name) {

        WebServiceEndpoint[] wsps = getWebServiceEndpoints(appId);
        if (wsps != null) {
            for (int idx =0; idx < wsps.length; idx++) {
               String sName = null;
               if ( isStandalone ) {
                    sName = name;
               } else {
                    sName = modId + "#" + name;
               }
               if ( wsps[idx].getName().equals(sName) ) {
                return new WebServiceConfigImpl(wsps[idx]); 
               }
            }
        }
        return null;
    }


    /**
     * Returns the Web Service Endpoint config in this endpoint 
     *
     * @param fqn   Fully Qualified Name of the endpoint
     *
     * @return the WebServiceConfig
     */
    public WebServiceConfig getWebServiceConfig(String fqn) {

	if (fqn==null) return null;
	String appId = null;
	int sepIdx = fqn.indexOf("#");
	appId = fqn.substring(0, sepIdx);
	String partialName = fqn.substring(sepIdx +1);

        WebServiceEndpoint[] wsps = getWebServiceEndpoints(appId);

        for (int idx =0; idx < wsps.length; idx++) {
           if ( wsps[idx].getName().equals(partialName) ) {
            return new WebServiceConfigImpl(wsps[idx]); 
           }
        }
        return null;
    }

    /**
     * Returns a list of application or stand alone module ids 
     * currently configured for web service management.
     *
     * @return  managed web service application ids
     */
    public List getManagedWebserviceApplicationIds() {

        List aList = new ArrayList();
        try {
            ConfigContext ctx = ApplicationServer.getServerContext().
                                                getConfigContext();

            String serverName =
            ApplicationServer.getServerContext().getInstanceName();

            ApplicationRef[] appRefs =ServerHelper.getApplicationReferences(ctx,
            serverName);
            for ( int appIdx =0; appIdx < appRefs.length; appIdx++) {
                String appName = appRefs[appIdx].getRef();
                ConfigBean cb = ApplicationHelper.findApplication(ctx, appName);
                // check if web service endpoint is configured for this
                // application/module, then ruturn this app

                int wsSize = 0;

                if ( cb instanceof WebModule) {
                    wsSize = ( (WebModule) cb).sizeWebServiceEndpoint();
                } else if ( cb instanceof EjbModule) {
                    wsSize = ( (EjbModule) cb).sizeWebServiceEndpoint();
                } else if (cb instanceof J2eeApplication) {
                    wsSize = ( (J2eeApplication) cb).sizeWebServiceEndpoint();
                }

                if (wsSize > 0)  { aList.add(appName); }
            }
        } catch (ConfigException ce) {
            // XX throw exception
        } finally {
            return aList;
        }
    }

}
