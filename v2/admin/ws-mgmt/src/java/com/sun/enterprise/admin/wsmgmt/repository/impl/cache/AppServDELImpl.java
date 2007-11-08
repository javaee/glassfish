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
package com.sun.enterprise.admin.wsmgmt.repository.impl.cache;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;
import com.sun.enterprise.deployment.backend.DeploymentEventListener;
import com.sun.enterprise.deployment.backend.DeploymentEventInfo;
import com.sun.enterprise.deployment.backend.DeploymentEvent;
import com.sun.enterprise.deployment.backend.DeploymentRequest;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.WebServicesDescriptor;
import com.sun.enterprise.admin.wsmgmt.WebServiceMgrBackEnd;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

/**
 * This class listens to the deploy/undeploy events. On deploy, it 
 * investigates the deployment descriptors for web services. If 
 * web services exist, the application or stand alone module is 
 * demarcated persistently. This cache is used to discover the 
 * deployed web services artifacts in the repository. This removes 
 * the need to read all the deployment descriptors in the repository. 
 * On un-deploy, the information, if any, in the cache is removed. 
 *
 * @author Nazrul Islam
 * @since  J2SE 5.0
 */
public class AppServDELImpl implements DeploymentEventListener {

    /**
     * Default constructor.
     */
    public AppServDELImpl() { 
    }

    /**
     * Listens to DEPLOY and UNDEPLOY events. On deploy, the 
     * cache is updated if the application or stand alone 
     * module has web services in it. For application, the ejb and web 
     * bundles with web services are identified. On un-deploy, the 
     * application or stand alone module is removed from cache. 
     */
    public void notifyDeploymentEvent(DeploymentEvent event) {

        try {
            DeploymentEventInfo info = null;
            if (event !=null) {
                info = event.getEventInfo();
            }
            Application rootDD       = null;
            DeploymentRequest dr     = null;
            if (info != null) {
                rootDD = info.getApplicationDescriptor();
                dr = info.getDeploymentRequest();
            }
            CacheMgr mgr             = CacheMgr.getInstance();

            // post deploy event
            if ((event != null) 
                    && (event.getEventType()==DeploymentEvent.POST_DEPLOY)) {

                // if an ejb module
                if (dr.isEjbModule()) {
                    Set ws = rootDD.getWebServiceDescriptors();
                    if ((ws != null) && !ws.isEmpty()) {
                        mgr.addEjbModule(dr.getName());
                        mgr.save();
                    }

                // if a web module
                } else if (dr.isWebModule()) {
                    Set ws = rootDD.getWebServiceDescriptors();
                    if ((ws != null) && !ws.isEmpty()) {
                        mgr.addWebModule(dr.getName());
                        mgr.save();
                    }

                // if application
                } else if (dr.isApplication()) {
                    List ejb = new ArrayList();
                    Set ejbBundles = rootDD.getEjbBundleDescriptors();

                    for (Iterator iter=ejbBundles.iterator(); iter.hasNext();) {
                        BundleDescriptor bd = (BundleDescriptor) iter.next();
                        WebServicesDescriptor wsDD = bd.getWebServices();
                        if (wsDD.hasWebServices()) {

                            // ejb bundle has web services
                            ejb.add(bd.getModuleDescriptor().getArchiveUri());
                        }
                    }

                    List web = new ArrayList();
                    Set webBundles = rootDD.getWebBundleDescriptors();
                    for (Iterator iter=webBundles.iterator(); iter.hasNext();) {
                        BundleDescriptor bd = (BundleDescriptor) iter.next();
                        WebServicesDescriptor wsDD = bd.getWebServices();
                        if (wsDD.hasWebServices()) {

                            // web bundle has web services
                            web.add(bd.getModuleDescriptor().getArchiveUri());
                        }
                    }

                    if ( (!ejb.isEmpty()) || (!web.isEmpty()) ) {
                        mgr.addJ2eeApplication(dr.getName(), ejb, web);
                        mgr.save();
                    }
                }
                WebServiceMgrBackEnd.getManager().removeFromCache(dr.getName());


            // post undeploy event and pre deploy event
            } else if ((event != null) 
                    && (event.getEventType()==DeploymentEvent.POST_UNDEPLOY || 
                        event.getEventType()==DeploymentEvent.PRE_DEPLOY)) {

                // removes the app entry from cache
                if (dr.isEjbModule()) {
                    mgr.removeEjbModule(dr.getName());
                    mgr.save();
                } else if (dr.isWebModule()) {
                    mgr.removeWebModule(dr.getName());
                    mgr.save();
                } else if (dr.isApplication()) {
                    mgr.removeJ2eeApplication(dr.getName());
                    mgr.save();
                }
                WebServiceMgrBackEnd.getManager().removeFromCache(dr.getName());
            }



        } catch (Exception e) {
            _logger.log(Level.FINE, "Error in deployment event listener", e);
        }
    }

    // ---- VARIABLES - PRIVATE --------------------------------------------
    private static Logger _logger = Logger.getLogger(LogDomains.ADMIN_LOGGER);
}
