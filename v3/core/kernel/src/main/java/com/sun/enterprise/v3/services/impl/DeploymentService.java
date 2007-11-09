/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.services.impl;

import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.v3.deployment.GenericSniffer;
import com.sun.enterprise.v3.web.WebSniffer;
import org.glassfish.api.Startup;
import org.glassfish.api.container.Sniffer;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Singleton;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DeploymentService is responsible for installing artifacts that will allow 
 * users to deploy and undeploy applications. 
 *
 * @author Jerome Dochez
 
 */
@Service
@Scoped(Singleton.class)
public class DeploymentService implements Startup, PostConstruct {

    @Inject
    ServerContext serverContext;

    @Inject
    Logger logger;

    @Inject
    Habitat habitat;


    public Lifecycle getLifecycle() {
        return Lifecycle.SERVER;
    }

    public void postConstruct() {

        // Read the stock containers configuration
        /*File root = new File(serverContext.getInstallRoot());
        File config = new File(root, "config");
        config = new File(config, "glassfish.container");
        if (config.exists()) {
            Properties containersDef = new Properties();
            try {
                containersDef.load(new BufferedInputStream(new FileInputStream(config)));
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error while reading glassfish containers definition");
            }
            String containers = containersDef.getProperty("containers");
            if (containers==null) {
                return;
            }
            StringTokenizer st = new StringTokenizer(containers, ",");

            // TODO : this should move to a component initialization capabilities in HK2

            while (st.hasMoreTokens()) {
                String container = st.nextToken().trim();
                logger.log(Level.FINE, "ContractProvider " + container);
                String appToken = containersDef.getProperty(container+".application");
                String urlToken = containersDef.getProperty(container+".urlpattern");

                // Hack for web container
                Sniffer sniffer;
                if (container.equals("web")) {
                    sniffer = new WebSniffer(container, appToken, urlToken);
                } else {
                    sniffer = new GenericSniffer(container, appToken, urlToken);
                }
                try {
                    habitat.addComponent(container, sniffer);
                } catch (ComponentException e) {
                    logger.log(Level.SEVERE, sniffer + " Sniffer injection failed ", e);
                }

            }
        } */

        // coded sniffers. just for displaying useful message.
        StringBuffer moduleTypes = new StringBuffer();
        for (Sniffer sniffer : habitat.getAllByContract(Sniffer.class)) {
            moduleTypes.append(sniffer.getModuleType());
            moduleTypes.append(",");
        }
        // delete the last ':'
        if (moduleTypes.length()>0) {
            moduleTypes.deleteCharAt(moduleTypes.length()-1);
            logger.info("Supported containers : " + moduleTypes.toString());
        }

    }
    
    /**
     * Return a meaningful service description
     */
    public String toString() {
        return "Deployment";
    }
}
