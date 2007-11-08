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

package com.sun.enterprise.appclient.jws;

import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.runtime.JavaWebStartAccessDescriptor;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.server.event.ApplicationClientEvent;
import com.sun.enterprise.server.event.ApplicationClientLoaderEventListener;
import com.sun.enterprise.server.event.ApplicationEvent;
import com.sun.enterprise.server.event.ApplicationLoaderEventListener;
import com.sun.enterprise.server.event.ApplicationLoaderEventNotifier;
import com.sun.logging.LogDomains;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;

/**
 *Singleton that keeps the AppclientJWSSupportInfo up-to-date as applications and app clients
 *are loaded and unloaded and as the administrator enables and disables
 *Java Web Start access to them.
 *
 * @author tjquinn
 */
public class AppclientJWSSupportManager implements ApplicationLoaderEventListener, ApplicationClientLoaderEventListener {
    
    private static Logger _logger=LogDomains.getLogger(LogDomains.SERVER_LOGGER);

    /** the singleton instance */
    private static AppclientJWSSupportManager instance;
    
    /** the data structure shared with the system web app */
    private AppclientJWSSupportInfo jwsInfo;
    
    /** system property name used to turn off JWS feature */
    private static final String JWS_FEATURE_ON_PROPERTY_NAME = "com.sun.aas.jws.featureon";
    
    /** indicates if JWS handling is turned on or off via property */
    private final boolean isJWSFeatureOn = Boolean.valueOf(System.getProperty(JWS_FEATURE_ON_PROPERTY_NAME, "true"));
    
    /**
     *Returns the singleton instance.
     *@return the instance of the manager
     */
    public synchronized static AppclientJWSSupportManager getInstance() {
        if (instance == null) {
            instance = new AppclientJWSSupportManager();
            if (instance.isJWSFeatureOn) {
                try {
                    /*
                     *Obtain the data structure object.
                     */
                    instance.jwsInfo = AppclientJWSSupportInfo.getInstance();
                } catch (IOException ioe) {
                    _logger.log(Level.SEVERE, "Error initializing Java Web Start support information", ioe);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return instance;
    }

     /**
      *Register all deployed appclients with JWS service. This will be called by
      *OnDemand initialization framework, when webcontainer starts.
      */
     public void startJWSServicesForDeployedAppclients() {
         jwsInfo.startJWSServicesForDeployedAppclients();
     }
    
    
    /** Creates a new instance of AppclientJWSSupportManager */
    private AppclientJWSSupportManager() {
        /**
         *If the JWS feature is turned on (the default), register this object
         *as a listener for app loader and app client loader events.
         */
        if (isJWSFeatureOn) {
            ApplicationLoaderEventNotifier.getInstance().addListener((ApplicationLoaderEventListener) this);
            ApplicationLoaderEventNotifier.getInstance().addListener((ApplicationClientLoaderEventListener) this);
        } else {
            _logger.info("Java Web Start support turned off by " + JWS_FEATURE_ON_PROPERTY_NAME);
        }
    }

    /**
     *Responds to an application-related event broadcast by the instance.
     *@param the ApplicationEvent describing what has happened
     */
    public void handleApplicationEvent(ApplicationEvent event) {
        /*
         *Respond to after-load or before-unload events.
         */
        int eventType = event.getEventType();
        if ((eventType == event.BEFORE_APPLICATION_LOAD) ||
            (eventType == event.AFTER_APPLICATION_UNLOAD) ) {
            return;
        }
        
        /*
         *Get the module descriptors (if any) for eligible nested app clients.
         *If there are any, start Java Web Start services for this app and
         *those eligible app clients.
         */
        Application app = event.getApplication();
        ModuleDescriptor [] mds = NamingConventions.getEligibleAppclientModuleDescriptors(app);
        if (mds.length > 0) {
            try {
                if (eventType == event.AFTER_APPLICATION_LOAD) {
                    jwsInfo.startJWSServicesForApplication(app, mds, event.getConfigContext());
                } else if (eventType == event.BEFORE_APPLICATION_UNLOAD) {
                    jwsInfo.endJWSServicesForApplication(app, mds, event.getConfigContext());
                }
            } catch (Throwable thr) {
                _logger.log(Level.SEVERE, "Error updating Java Web Start information for application " + app.getRegistrationName(), thr);
            }            
        }

    }

    /**
     *No-op implementation needed to conform to the interface.
     */
    public void handleEjbContainerEvent(com.sun.enterprise.server.event.EjbContainerEvent ejbContainerEvent) {
    }
    
    /**
     *Responds to app client events broadcast by the instance.
     *@param the event describing what has happened
     */
    public void handleApplicationClientEvent(ApplicationClientEvent event) {
        /*
         *Respond to after-load or before-unload events.
         */
        int eventType = event.getEventType();
        if ((eventType == event.BEFORE_APPLICATION_CLIENT_LOAD) ||
            (eventType == event.AFTER_APPLICATION_CLIENT_UNLOAD) ) {
            return;
        }
        
        /*
         *Find out if this app client is eligible for Java Web Start access.
         */
        Application app = event.getApplication();
        
        /*
         *The Application object wraps the app client, so there should be only
         *a single nested module (at most) representing the app client itself.
         */
        ModuleDescriptor [] mds = NamingConventions.getEligibleAppclientModuleDescriptors(app);
        
        if (mds.length > 1) {
            _logger.warning("During app client loading, expected exactly one app client module in the wrapping application but found more; using the first one and ignoring the others");
        } else if (mds.length == 0) {
            _logger.warning("During app client loading, expected exactly one app client module in the wrapping application but found none; ignoring this app client and continuing");
            return;
        }
        
        try {
            if (eventType == event.AFTER_APPLICATION_CLIENT_LOAD) {
                jwsInfo.startJWSServicesForAppclient(app, mds[0], event.getConfigContext());
            } else if (eventType == event.BEFORE_APPLICATION_CLIENT_UNLOAD) {
                jwsInfo.endJWSServicesForAppclient(app, mds[0], event.getConfigContext());
            }
        } catch (Throwable thr) {
            _logger.log(Level.SEVERE, "Error updating Java Web Start information for app client " + app.getRegistrationName(), thr);
        }
    }
}
