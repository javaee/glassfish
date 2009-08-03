/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.deployment.common;

import com.sun.logging.LogDomains;
import java.io.File;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.glassfish.api.deployment.DeploymentContext;

/**
 * Records artifacts generated during deployment that need
 * to be included with each downloaded app client.
 * <p>
 * An example: jaxrpc classes from web services deployment
 * <p>
 * An instance of this class can be stored in the deployment
 * context's transient app metadata so the various deployers can add to the
 * same collection and so the app client deployer can find it and
 * act on its contents.
 * <p>
 * Because other modules should add their artifacts before the app client
 * deployer processes them, the <code>add</code> methods do not permit
 * further additions once the {@link #artifacts} method has been invoked.
 *
 * @author tjuinn
 */
public class ClientArtifactsManager {

    private boolean isArtifactSetConsumed = false;
    
    private static final String CLIENT_ARTIFACTS_KEY = "ClientArtifacts";
    
    private final Logger logger = 
            LogDomains.getLogger(ClientArtifactsManager.class, LogDomains.DPL_LOGGER);
    
    private final Set<DownloadableArtifacts.FullAndPartURIs> artifacts = 
            new HashSet<DownloadableArtifacts.FullAndPartURIs>();
    /**
     * Retreives the client artifacts store from the provided deployment 
     * context, creating one and storing it back into the DC if none is 
     * there yet.
     * 
     * @param dc the deployment context to hold the ClientArtifactsManager object
     * @return the ClientArtifactsManager object from the deployment context (created
     * and stored in the DC if needed)
     */
    public static ClientArtifactsManager get(final DeploymentContext dc) {
        synchronized (dc) {
            ClientArtifactsManager result = dc.getTransientAppMetaData(
                    CLIENT_ARTIFACTS_KEY, ClientArtifactsManager.class);

            if (result == null) {
                result = new ClientArtifactsManager();
                dc.addTransientAppMetaData(CLIENT_ARTIFACTS_KEY, result);
            }
            return result;
        }
    }

    /**
     * Adds a new artifact to the collection of artifacts to be included in the
     * client facade JAR file so they can be delivered to the client during a
     * download.
     *
     * @param baseURI absolute URI of the base directory within which the
     * artifact lies
     * @param artifactURI absolute or relative URI for the artifact itself
     * @throws IllegalStateException if invokes after the accumulated artifacts have been consumed
     */
    public void add(final URI baseURI, final URI artifactURI) {
        URI relativeURI;
        URI absoluteURI;
        if (artifactURI.isAbsolute()) {
            absoluteURI = artifactURI;
            relativeURI = baseURI.relativize(absoluteURI);
        } else {
            relativeURI = artifactURI;
            absoluteURI = baseURI.resolve(relativeURI);
        }
        if (isArtifactSetConsumed) {
            final String format = logger.getResourceBundle().
                    getString("enterprise.deployment.backend.appClientArtifactOutOfOrder");
            throw new IllegalStateException(MessageFormat.format(
                    format, absoluteURI.toASCIIString()));
        } else {
            artifacts.add(new DownloadableArtifacts.FullAndPartURIs(
                    absoluteURI, relativeURI));
        }
    }

    /**
     * Adds a new artifact to the collection of artifacts to be added to the
     * client facade JAR file so they can be delivered to the client during a
     * download.
     *
     * @param baseFile File for the base directory within which the artifact lies
     * @param artifactFile File for the artifact itself
     * @throws IllegalStateException if invoked after the accumulated artifacts have been consumed
     */
    public void add(final File baseFile, final File artifactFile) {
        add(baseFile.toURI(), artifactFile.toURI());
    }

    /**
     * Adds all artifacts in Collection to those to be added to the client
     * facade JAR.
     *
     * @param baseFile File for the base directory within which each artifact lies
     * @param artifactFiles Collection of File objects for the artifacts to be included
     * @throws IllegalStateException if invoked after the accumulated artifacts have been consumed
     */
    public void addAll(final File baseFile, final Collection<File> artifactFiles) {
        for (File f : artifactFiles) {
            add(baseFile, f);
        }
    }

    /**
     * Returns the set (in unmodifiable form) of FullAndPartURIs for the
     * accumulated artifacts.
     * <p>
     * Note: Intended for use only by the app client deployer.
     *
     * @return all client artifacts reported by various deployers
     */
    public Set<DownloadableArtifacts.FullAndPartURIs> artifacts() {
        isArtifactSetConsumed = true;
        return Collections.unmodifiableSet(artifacts);
    }
}
