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

package org.glassfish.ejb.embedded;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ejb.embeddable.EJBContainer;
import javax.ejb.EJBException;

import com.sun.logging.LogDomains;
import com.sun.ejb.containers.EjbContainerUtilImpl;
import com.sun.enterprise.util.io.FileUtils;
import org.glassfish.api.embedded.EmbeddedDeployer;
import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.LifecycleException;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.deployment.common.ModuleExploder;
import org.glassfish.ejb.embedded.EmbeddedEjbContainer;

/**
 * GlassFish implementation of the EJBContainer.
 *
 * @author Marina Vatkina
 */
public class EJBContainerImpl extends EJBContainer {

    // Use Bundle from another package
    private static final Logger _logger =
            LogDomains.getLogger(EjbContainerUtilImpl.class, LogDomains.EJB_LOGGER);

    private final Server server;
    
    private final EmbeddedEjbContainer ejbContainer;

    private final EmbeddedDeployer deployer;

    private String deployedAppName;

    private boolean open = true;

    /**
     * Construct new EJBContainerImpl instance 
     */                                               
    EJBContainerImpl(Server server, EmbeddedEjbContainer ejbContainer, EmbeddedDeployer deployer) {
        this.server = server;
        this.ejbContainer = ejbContainer;
        this.deployer = deployer;
    }

    /**
     * Construct new EJBContainerImpl instance and deploy found modules.
     */
    void deploy(Map<?, ?> properties, Set<File> modules) throws EJBException {
        File app = null;
        try {
            app = getOrCreateApplication(modules);
            
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("[EJBContainerImpl] Deploying app: " + app);
            }
            DeployCommandParameters dp = new DeployCommandParameters(app);
            if (properties != null) {
                dp.name = (String)properties.get(EJBContainer.APP_NAME);
            }

            if (dp.name == null) {
                dp.name = app.getName();
            }
            deployedAppName = deployer.deploy(app, dp);
        } catch (IOException e) {
            throw new EJBException("Failed to deploy EJB modules", e);
        }

        if (deployedAppName == null) {
            throw new EJBException("Failed to deploy EJB modules - see log for details");
        }
    }

    /**
     * Retrieve a naming context for looking up references to session beans
     * executing in the embeddable container.
     *
     * @return naming context
     */
    public Context getContext() { 
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("IN getContext()");
        }
        try {
            return new InitialContext();
        } catch (Exception e) {
            throw new EJBException(_logger.getResourceBundle().getString(
                    "ejb.embedded.cannot_create_context"), e);
        }
    }

    /**
     * Shutdown an embeddable EJBContainer instance.
     */
    public void close() {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("IN close()");
        }

        if (deployedAppName != null) {
            try {
                deployer.undeploy(deployedAppName, null);
            } catch (Exception e) {
                _logger.warning("Cannot undeploy deployed modules: " + e.getMessage());
            }
        }

        try {
            server.stop();
        } catch (LifecycleException e) {
            _logger.warning("Cannot stop embedded container " + e.getMessage());
        } finally {
            open = false;
        }
    }

    /**
     * Returns true if there are deployed modules associated with this container.
     */
    boolean isOpen() {
        return open;
    }

    /** 
     */
    private File getOrCreateApplication(Set<File> modules)
            throws EJBException, IOException {
        File result = null;
        if (modules == null || modules.size() == 0) {
            _logger.info("[EJBContainerImpl] No modules found");
        } else if (modules.size() == 1) {
            result = modules.iterator().next();
        } else {
            // Create a temp dir by creating a temp file first, then 
            // delete the file and create a directory in its place.
            result = File.createTempFile("ejb-app", "");
            if (result.delete() && result.mkdirs()) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.fine("[EJBContainerImpl] temp dir created at " + result.getAbsolutePath());
                }
            } else {
                throw new EJBException("Not able to create temp dir " + result.getAbsolutePath ());
            }
//            result.deleteOnExit();

            // Copy module directories and explode module jars
            for (File f : modules) {
                String filename = f.toURI().getSchemeSpecificPart();
                if (filename.endsWith(File.separator) || filename.endsWith("/")) {
                    int length = filename.length();
                    filename = filename.substring(0, length - 1);
                }

                int lastpart = filename.lastIndexOf(File.separatorChar);
                if (lastpart == -1) {
                    lastpart = filename.lastIndexOf('/');
                }
                String name = filename.substring(lastpart + 1);
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.fine("[EJBContainerImpl] Converted file name: " + filename + " to " + name);
                }
                if (f.isDirectory()) {
                    File out = new File(result, name + "_jar");
                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.fine("[EJBContainerImpl] Copying directory to: " + out);
                    }
                    FileUtils.copy(f, out);
                } else {
                    File out = new File(result, FileUtils.makeFriendlyFilename(name));
                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.fine("[EJBContainerImpl] Exploding jar to: " + out);
                    }
                    ModuleExploder.explodeJar(f, out);
                }
            }
        }
        return result;
    }
}
