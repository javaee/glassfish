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

package org.glassfish.web.sniffer;

import org.glassfish.internal.deployment.GenericSniffer;
import com.sun.enterprise.module.ModulesRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.container.Sniffer;
import org.glassfish.deployment.common.DeploymentUtils;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Singleton;



/**
 * Implementation of the Sniffer for the web container.
 * 
 * @author Jerome Dochez
 */
@Service(name="web")
@Scoped(Singleton.class)
public class WebSniffer  extends GenericSniffer implements Sniffer {

    @Inject
    ModulesRegistry registry;
    
    public WebSniffer() {
        super("web", "WEB-INF/web.xml", null);
    }

    @Override
    public String[] getURLPatterns() {
        // anything finishing with jsp or jspx
        return new String[] { "*.jsp", "*.jspx" };
    }

    /**
     * Returns true if the passed file or directory is recognized by this
     * instance.
     *
     * @param location the file or directory to explore 
     * @param loader class loader for this application
     * @return true if this sniffer handles this application type
     */
    public boolean handles(ReadableArchive location, ClassLoader loader) {
        return DeploymentUtils.isWebArchive(location);
    }

    final String[] containers = { "com.sun.enterprise.web.WebContainer" };
    public String[] getContainersNames() {
        return containers;
    }

   /**
     * Sets up the container libraries so that any imported bundle from the
     * connector jar file will now be known to the module subsystem
     *
     * This method returns a {@link com.sun.enterprise.module.ModuleDefinition} for the module containing
     * the core implementation of the container. That means that this module
     * will be locked as long as there is at least one module loaded in the
     * associated container.
     *
     * @param containerHome is where the container implementation resides
     * @param logger the logger to use
     * @return the module definition of the core container implementation.
     *
     * @throws java.io.IOException exception if something goes sour
     */
    // Commented out by Sahoo, as in OSGi environment, we can't support
    // Module.addImport. So, temporarily we add webtier to the dependency
    // list of gf-web-sniffer.
//    public Module[] setup(String containerHome, Logger logger) throws IOException {
//       Module[] modules = new Module[1];
//       modules[0] = modulesRegistry.makeModuleFor("org.glassfish.web:webtier", null);
//       if (modules[0]==null) {
//           throw new IOException("Webtier module not found, web container is not installed or found");
//       }
//       return modules;
//    }

    /**
     * @return whether this sniffer should be visible to user
     *
     */
    public boolean isUserVisible() {
        return true;
    }
    
    private static final List<String> deploymentConfigurationPaths = 
            initDeploymentConfigurationPaths();
    
    private static List<String> initDeploymentConfigurationPaths() {
        final List<String> result = new ArrayList<String>();
        result.add("WEB-INF/web.xml");
        result.add("WEB-INF/sun-web.xml");
        return result;
    }
    
    /**
     * Returns the web-oriented descriptor paths that might exist in a web
     * app.
     * 
     * @return list of the deployment descriptor paths
     */
    @Override
    protected List<String> getDeploymentConfigurationPaths() {
        return deploymentConfigurationPaths;
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
        return new String[] {"connector"};
    }
}
