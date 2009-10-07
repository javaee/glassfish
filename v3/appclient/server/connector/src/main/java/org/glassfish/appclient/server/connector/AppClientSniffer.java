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
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 */
package org.glassfish.appclient.server.connector;

import java.util.jar.Manifest;
import org.glassfish.internal.deployment.GenericSniffer;

import org.glassfish.api.deployment.archive.ReadableArchive;

import org.glassfish.api.container.Sniffer;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.List;
import java.util.ArrayList;

@Service(name = "AppClient")
@Scoped(Singleton.class)
public class AppClientSniffer extends GenericSniffer implements Sniffer {
    private static final String[] stigmas = {
        "META-INF/application-client.xml", "META-INF/sun-application-client.xml"
    };

    private static final String[] containers = {"appclient"};

    public AppClientSniffer() {
        this(containers[0], stigmas[0], null);
    }

    public AppClientSniffer(String containerName, String appStigma, String urlPattern) {
        super(containerName, appStigma, urlPattern);
    }

    public String[] getContainersNames() {
        return containers;
    }

    /**
     * Returns true if the passed file or directory is recognized by this
     * instance.                   
     *
     * @param location the file or directory to explore
     * @param loader class loader for this application
     * @return true if this sniffer handles this application type
     */
    @Override
    public boolean handles(ReadableArchive location, ClassLoader loader) {
        for (String s : stigmas) {
            try {
                if (location.exists(s)) {
                    return true;
                }
            } catch (IOException ignore) {
            }
        }

        try {
            Manifest manifest = location.getManifest();
            if (manifest != null && 
                manifest.getMainAttributes().containsKey(Attributes.Name.MAIN_CLASS)) {
                return true;
            }
        } catch (IOException ignore) {
        }
        return false;
    }

    /**
     * @return whether this sniffer should be visible to user
     */
    @Override
    public boolean isUserVisible() {
        return true;
    }

    /**
     * @return the set of the sniffers that should not co-exist for the
     * same module. For example, ejb and appclient sniffers should not
     * be returned in the sniffer list for a certain module.
     * This method will be used to validate and filter the retrieved sniffer
     * lists for a certain module
     *
     */
    public String[] getIncompatibleSnifferTypes() {
        return new String[] {"ejb"};
    }

    private static final List<String> deploymentConfigurationPaths =
            initDeploymentConfigurationPaths();

    private static List<String> initDeploymentConfigurationPaths() {
        final List<String> result = new ArrayList<String>();
        result.add("META-INF/application-client.xml");
        result.add("META-INF/sun-application-client.xml");
        return result;
    }

    /**
     * Returns the descriptor paths that might exist in an appclient app.
     *
     * @return list of the deployment descriptor paths
     */
    @Override
    protected List<String> getDeploymentConfigurationPaths() {
        return deploymentConfigurationPaths;
    }

}
