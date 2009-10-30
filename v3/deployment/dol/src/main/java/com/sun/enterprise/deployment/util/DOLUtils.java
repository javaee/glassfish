/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only "GP
 * and Distribution License"CDD
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

package com.sun.enterprise.deployment.util;

import com.sun.logging.LogDomains;

import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;

import org.jvnet.hk2.component.Habitat;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.data.ApplicationInfo;
import com.sun.enterprise.deployment.*;

/**
 * Utility class for convenienve methods
 *
 * @author  Jerome Dochez
 * @version 
 */
public class DOLUtils {
    
    private static Logger logger=null;
    

    /** no need to creates new DOLUtils */
    private DOLUtils() {
    }

    /**
     * @return a logger to use in the DOL implementation classes
     */
    public static Logger getDefaultLogger() {
        if (logger==null) {
            logger = LogDomains.getLogger(DeploymentUtils.class, LogDomains.DPL_LOGGER);
        }
        return logger;
    }

    public static Map<String, String> getSubComponentsOfModule(
        String applicationName, String moduleName, Habitat habitat) {
        ApplicationRegistry appRegistry = habitat.getComponent(
            ApplicationRegistry.class);

        ApplicationInfo appInfo = appRegistry.get(applicationName);
        if (appInfo != null) {
            Application app = appInfo.getMetaData(Application.class);
            if (app != null) {
                BundleDescriptor bundleDesc = app.getModuleByUri(moduleName);
                return getModuleLevelComponents(bundleDesc);
            }
        }
        return Collections.emptyMap();
    }

    private static Map<String, String> getModuleLevelComponents(
        BundleDescriptor bundle) {
        Map<String, String> subComponentsMap = new HashMap<String, String>();
        if (bundle instanceof WebBundleDescriptor) {
            WebBundleDescriptor wbd = (WebBundleDescriptor)bundle;
            // look at ejb in war case
            Collection<EjbBundleDescriptor> ejbBundleDescs =
                wbd.getExtensionsDescriptors(EjbBundleDescriptor.class);
            if (ejbBundleDescs.size() > 0) {
                EjbBundleDescriptor ejbBundle =
                        ejbBundleDescs.iterator().next();
                subComponentsMap.putAll(getModuleLevelComponents(ejbBundle));
            }

            for (WebComponentDescriptor wcd :
                    wbd.getWebComponentDescriptors()) {
                String wcdName = wcd.getCanonicalName();
                String wcdType = wcd.isServlet() ? "Servlet" : "JSP";
                subComponentsMap.put(wcdName, wcdType);
            }
        } else if (bundle instanceof EjbBundleDescriptor)  {
            EjbBundleDescriptor ebd = (EjbBundleDescriptor)bundle;
            for (EjbDescriptor ejbDesc : ebd.getEjbs()) {
                String ejbName = ejbDesc.getName();
                String ejbType = getEjbType(ejbDesc);
                subComponentsMap.put(ejbName, ejbType);
            }
        }

        return subComponentsMap;
    }


    public static String getEjbType(EjbDescriptor ejbDesc) {
        String type = null;
        if (ejbDesc.getType().equals(EjbSessionDescriptor.TYPE)) {
            EjbSessionDescriptor sessionDesc = (EjbSessionDescriptor)ejbDesc;
            if (sessionDesc.isStateful()) {
                type = "StatefulSessionBean";
            } else if (sessionDesc.isStateless()) {
                type = "StatelessSessionBean";
            } else if (sessionDesc.isSingleton()) {
                type = "SingletonSessionBean";
            }
        } else if (ejbDesc.getType().equals(EjbMessageBeanDescriptor.TYPE)) {
            type = "MessageDrivenBean";

        } else if (ejbDesc.getType().equals(EjbEntityDescriptor.TYPE)) {
            type = "EntityBean";
        }

        return type;
    }

}
