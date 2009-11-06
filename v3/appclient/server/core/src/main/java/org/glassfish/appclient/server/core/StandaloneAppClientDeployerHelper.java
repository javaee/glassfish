/*
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
package org.glassfish.appclient.server.core;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.archivist.AppClientArchivist;
import com.sun.enterprise.deployment.deploy.shared.OutputJarArchive;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.appclient.server.core.jws.servedcontent.DynamicContent;
import org.glassfish.appclient.server.core.jws.servedcontent.FixedContent;
import org.glassfish.appclient.server.core.jws.servedcontent.TokenHelper;
import org.glassfish.deployment.common.DownloadableArtifacts;
import org.glassfish.deployment.common.DownloadableArtifacts.FullAndPartURIs;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.jvnet.hk2.component.Habitat;

/**
 * Encapsulates logic that is specific to stand-alone app client
 * deployments regarding the generation of app client files.
 * <p>
 * The facade JAR file - ${appName}Client.jar - will reside at the top of
 * the user's local directory and will refer to the developer's original
 * app client JAR which will reside in the ${appName}Client subdirectory
 * within the user's download directory.
 *
 * @author tjquinn
 */
public class StandaloneAppClientDeployerHelper extends AppClientDeployerHelper {

    StandaloneAppClientDeployerHelper(final DeploymentContext dc, 
            final ApplicationClientDescriptor bundleDesc,
            final AppClientArchivist archivist,
            final ClassLoader gfClientModuleClassLoader,
            final Application application,
            final Habitat habitat) throws IOException {
        super(dc, bundleDesc, archivist, gfClientModuleClassLoader, 
                application, habitat);
    }

    /**
     * Returns the name (no path, no type) of the facade JAR.  This is used
     * in both creating the full name and URI of the facade as well as for
     * the name of a subdirectory in the user's download directory.
     * 
     * @param dc
     * @return
     */
    private String facadeNameOnly(DeploymentContext dc) {
        DeployCommandParameters params = dc.getCommandParameters(DeployCommandParameters.class);
        final String appName = params.name();
        return appName + "Client";
    }

    @Override
    protected void prepareJARs() throws IOException, URISyntaxException {
        super.prepareJARs();
        /*
         * The app client JAR will have been expanded and deleted, so create
         * a JAR from the expanded directory.
         */
        copyOriginalAppClientJAR(dc());
    }

    @Override
    public void createAndAddLibraryJNLPs(AppClientDeployerHelper helper, TokenHelper tHelper, Map<String, DynamicContent> dynamicContent) {
    }

    @Override
    public FixedContent fixedContentWithinEAR(String uriString) {
        /*
         * There can be no fixed content within the EAR for a stand-alone
         * app client.
         */
        return null;
    }

    /**
     * Returns the file name and type of the facade.
     * <p>
     * For stand-alone app clients, the facade is ${appName}Client.jar.
     * @param dc
     * @return
     */
    @Override
    protected String facadeFileNameAndType(DeploymentContext dc) {
        return facadeNameOnly(dc) + ".jar";
    }


    /**
     * Returns the URI for the generated facade JAR.
     * <p>
     * The facade is ${appName}Client.jar and for stand-alone app clients
     * is stored at generated/xml/${appName}/${appName}Client.jar.
     * @param dc
     * @return
     */
    @Override
    public URI facadeServerURI(DeploymentContext dc) {
        File genXMLDir = dc.getScratchDir("xml");
        return genXMLDir.toURI().resolve(facadeFileNameAndType(dc));
    }

    /**
     * Returns the URI for the facade within the user's download directory.
     * <p>
     * The facade for a stand-alone app client will reside at the top level
     * of the user's download directory.
     * @param dc
     * @return
     */
    @Override
    public URI facadeUserURI(DeploymentContext dc) {
        return URI.create(facadeFileNameAndType(dc));
    }

    @Override
    public URI groupFacadeUserURI(DeploymentContext dc) {
        return null;
    }

    @Override
    public URI groupFacadeServerURI(DeploymentContext dc) {
        return null;
    }



    /**
     * Returns the URI for the developer's original app client JAR within the
     * user's download directory.
     *
     * @param dc
     * @return
     */
    @Override
    public URI appClientUserURI(DeploymentContext dc) {
        return URI.create(facadeNameOnly(dc) + '/' + appClientURIWithinApp(dc));
    }

    /**
     * Returns the URI to the server 
     * @param dc
     * @return
     */
    @Override
    public URI appClientServerURI(DeploymentContext dc) {
        File genXMLDir = dc.getScratchDir("xml");
        return genXMLDir.toURI().resolve(appClientURIWithinApp(dc));
    }


    /**
     * Returns the URI for the app client within the artificial containing
     * app.  For stand-alone clients the module URI is reported as
     * the directory into which the app client JAR was expanded, without the
     * "_jar" suffix.  To that we add .jar to get a URI at the "top-level"
     * of the pseudo-containing app.
     *
     * @param dc
     * @return
     */
    @Override
    public URI appClientURIWithinApp(DeploymentContext dc) {

        String uriText = appClientDesc().getModuleDescriptor().getArchiveUri();
        if ( ! uriText.endsWith(".jar")) {
            uriText += ".jar";
        }
        return URI.create(uriText);
    }

    @Override
    protected Set<FullAndPartURIs> clientLevelDownloads() throws IOException {
        /*
         * Stand-alone client deployments involve these downloads:
         * 1. the original app client JAR,
         * 2. the facade JAR 
         */
        Set<FullAndPartURIs> downloads = new HashSet<FullAndPartURIs>();
        downloads.add(new DownloadableArtifacts.FullAndPartURIs(
                appClientServerURI(dc()),
                appClientUserURI(dc())));
        downloads.add(new DownloadableArtifacts.FullAndPartURIs(
                facadeServerURI(dc()),
                facadeUserURI(dc())));
        return downloads;
    }

    @Override
    public Set<FullAndPartURIs> earLevelDownloads() throws IOException {
        return Collections.EMPTY_SET;
    }

    @Override
    public URI appClientUserURIForFacade(DeploymentContext dc) {
        return appClientUserURI(dc);
    }

    @Override
    protected void addGroupFacadeToEARDownloads() {
        // no-op
    }

    @Override
    public URI URIWithinAppDir(DeploymentContext dc, URI absoluteURI) {
        return dc.getSource().getURI().relativize(absoluteURI);
    }

    @Override
    public String pathToAppclientWithinApp(DeploymentContext dc) {
        return "";
    }



    @Override
    protected String facadeClassPath() {
        /*
         * For app client deployments, the facade class path refers only
         * to the developer's original JAR, renamed to ${name}.orig.jar
         * (or some similar name using orig-${unique-number} to avoid
         * naming collisions.
         */
        return appClientUserURI(dc()).toASCIIString();
    }
    protected void copyOriginalAppClientJAR(final DeploymentContext dc) throws IOException {
        ReadableArchive originalSource = ((ExtendedDeploymentContext) dc).getOriginalSource();
        originalSource.open(originalSource.getURI());
        OutputJarArchive target = new OutputJarArchive();
        target.create(appClientServerURI(dc));
        /*
         * Copy the manifest explicitly because ReadableArchive.entries()
         * excludes the manifest.
         */
        Manifest originalManifest = originalSource.getManifest();
        OutputStream os = target.putNextEntry(JarFile.MANIFEST_NAME);
        originalManifest.write(os);
        target.closeEntry();
        ClientJarMakerUtils.copyArchive(originalSource, target, Collections.EMPTY_SET);
        target.close();
        originalSource.close();
    }

    @Override
    protected String PUScanTargets() {
        return null;
    }



}
