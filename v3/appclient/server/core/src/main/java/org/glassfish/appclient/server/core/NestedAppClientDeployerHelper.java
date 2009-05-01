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

import com.sun.enterprise.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.archivist.AppClientArchivist;
import com.sun.enterprise.deployment.deploy.shared.InputJarArchive;
import com.sun.enterprise.deployment.deploy.shared.OutputJarArchive;
import com.sun.enterprise.deployment.deploy.shared.Util;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DownloadableArtifacts;
import org.glassfish.deployment.common.DownloadableArtifacts.FullAndPartURIs;

class NestedAppClientDeployerHelper extends AppClientDeployerHelper {

    private Set<FullAndPartURIs> libraryAndClassPathJARs = new HashSet<FullAndPartURIs>();

    private StringBuilder classPathForFacade = new StringBuilder();

    private final URI earURI;

    /**
     * records the downloads needed to support this app client,
     * including the app client JAR itself, the facade, and the transitive
     * closure of any library JARs from the EAR's lib directory or from the
     * app client's class path
     */
    private final Set<FullAndPartURIs> downloads = new HashSet<FullAndPartURIs>();

    /** recognizes expanded directory names for submodules */
    private static final Pattern submoduleURIPattern = Pattern.compile("(.*)_([wcrj]ar)$");

    NestedAppClientDeployerHelper(
            final DeploymentContext dc,
            final ApplicationClientDescriptor bundleDesc,
            final AppClientArchivist archivist) throws IOException {
        super(dc, bundleDesc, archivist);
        earURI = dc.getSource().getParentArchive().getURI();
        processJARDependencies();
    }

    private void processJARDependencies() throws IOException {

        /*
         * Init the class path for the facade so it refers to the developer's app client,
         * relative to where the facade will be.  (The facade and the original
         * app client share the same directory.)
         */
        URI appClientURI = URI.create(Util.getURIName(appClientUserURI(dc())));
//        classPathForFacade.append(appClientURI);
        classPathForFacade.append(appClientURI);

        /*
         * For a nested app client, the required downloads include the
         * developer's original app client JAR, the generated facade JAR,
         * the generated EAR-level facade, and
         * the transitive closure of all JARs in the app client's Class-Path
         * and the JARs in the EAR's library-directory.
         *
         * Note that the EAR deployer will add the EAR-level facade as a download
         * for each of its submodule app clients.
         */
//        downloads.add(new DownloadableArtifacts.FullAndPartURIs(
//                appClientServerURI(dc()),
//                appClientUserURI(dc())));
        downloads.add(new DownloadableArtifacts.FullAndPartURIs(
                facadeServerURI(dc()),
                facadeUserURI(dc())));

        /*
         * jarURIsProcessed records URIs, relative to the original JAR as it will
         * reside in the user's download directory, that have already been
         * processed.  This allows us to avoid processing the same JAR more
         * than once if more than one JAR depends on it.
         */
        Set<URI> jarURIsProcessed = new HashSet<URI>();

        processJARDependencies(appClientURIWithinApp(dc()), downloads, jarURIsProcessed);

        /*
         * Now incorporate the library JARs.
         */
        final String libDir = appClientDesc().getApplication().getLibraryDirectory();
        if (libDir != null) {
            File libDirFile = new File(new File(earURI), libDir);
            if (libDirFile.exists() && libDirFile.isDirectory()) {
                for (File libJar : libDirFile.listFiles(new FileFilter() {
                    public boolean accept(File pathname) {
                        return pathname.getName().endsWith(".jar") && ! pathname.isDirectory();
                    }
                })) {
                    final URI libJarURI = facadeServerURI(dc()).relativize(libJar.toURI());
                    /*
                     * Add a relative URI from the facade to this library JAR
                     * to the class path.
                     */
                    classPathForFacade.append(' ').append(libJarURI.toASCIIString());

                    /*
                     * Process this library JAR to record the need to download it
                     * and any JARs it depends on.
                     */
                    URI jarURI = URI.create("jar:" + libJar.toURI().getRawSchemeSpecificPart());
                    processJARDependencies(jarURI, downloads, jarURIsProcessed);
                }
            }
        }
    }

    /**
     * Processes a JAR URI on which the developer's app client depends, adding
     * the JAR to the set of JARs for download.
     * <p>
     * If the URI actually maps to an expanded directory for a submodule, this
     * method makes a copy of the submodule as a JAR so it will be available
     * after deployment has finished (which is not the case for uploaded EARs)
     * and can be downloaded as a JAR (which is not the case for submodules
     * in directory-deployed EARs.
     *
     * @param jarURI
     * @param jarDependencies
     * @param jarURIsProcessed
     * @throws java.io.IOException
     */
    private void processJARDependencies(
            final URI dependencyURI,
            final Set<FullAndPartURIs> downloads,
            final Set<URI> jarURIsProcessed) throws IOException {

        if (jarURIsProcessed.contains(dependencyURI)) {
            return;
        }

        URI dependencyFileURI = earURI.resolve(dependencyURI);

        /*
         * Get the dependency for a safe, JAR copy of an expanded directory if
         * that's what this URI is.
         */
        DownloadableArtifacts.FullAndPartURIs jarFileDependency =
                safeCopyOfExpandedSubmodule(dependencyURI);
        if (jarFileDependency != null) {
            dependencyFileURI = jarFileDependency.getFull();
        } else {
            /*
             * Get a URI of the same scheme as the EAR directory URI for the
             * client-side directory so relativize will work correctly.
             */

            
//            if (scheme != null && scheme.equals("jar")) {
//                dependencyFileURI = URI.create("file:" + dependencyURI.getRawSchemeSpecificPart());
//            } else {
//                if (scheme == null) {
//                    scheme = "file:";
//                }
//                dependencyFileURI = URI.create(scheme + dependencyURI.getRawSchemeSpecificPart());
//            }

            /*
             * The app might specify non-existent JARs in its Class-Path.
             */
            if ( ! new File(dependencyFileURI).exists()) {
                return;
            }
            jarFileDependency = new FullAndPartURIs(dependencyFileURI,
                    earDirUserURI(dc()).resolve(earURI.relativize(dependencyFileURI)));
        }
        downloads.add(jarFileDependency);

        /*
         * Process any JARs in this JAR's class path by opening it as an
         * archive and getting its manifest and processing the Class-Path
         * entry from there.
         */
        URI jarURI = URI.create("jar:" + dependencyFileURI.getRawSchemeSpecificPart());
        ReadableArchive dependentJar = new InputJarArchive();
        dependentJar.open(jarURI);

        Manifest jarManifest = dependentJar.getManifest();
        dependentJar.close();

        Attributes mainAttrs = jarManifest.getMainAttributes();

        String jarClassPath = mainAttrs.getValue(Attributes.Name.CLASS_PATH);
        if (jarClassPath != null) {
            for (String elt : jarClassPath.split(" ")) {
                processJARDependencies(jarFileDependency.getFull().resolve(elt),
                        downloads, jarURIsProcessed);
            }
        }
    }

    /**
     * If the specified URI is for an expanded submodule, makes a copy of
     * the submodule as a JAR and returns the URI for the copy.
     * @param classPathElement
     * @return URI to the safe copy of the submodule, relative to the top level
     * if the classPathElement is for a submodule; null otherwise
     */
    private FullAndPartURIs safeCopyOfExpandedSubmodule(final URI candidateSubmoduleURI) throws IOException {
        for (ModuleDescriptor<BundleDescriptor> desc : appClientDesc().getApplication().getModules()) {
            if (matchesSubmoduleURI(candidateSubmoduleURI, desc.getArchiveUri())) {
                if ( ! dc().getSource().getParentArchive().exists(candidateSubmoduleURI.getPath())) {
                    ReadableArchive source = new FileArchive();
                    source.open(dc().getSource().getParentArchive().getURI().resolve(expandedDirURI(candidateSubmoduleURI)));
                    OutputJarArchive target = new OutputJarArchive();
                    target.create(dc().getScratchDir("xml").toURI().resolve(candidateSubmoduleURI));
                    /*
                     * Copy the manifest explicitly because the ReadableArchive
                     * entries() method omits it.
                     */
                    Manifest mf = source.getManifest();
                    OutputStream os = target.putNextEntry(JarFile.MANIFEST_NAME);
                    mf.write(os);
                    target.closeEntry();
                    ClientJarMakerUtils.copyArchive(source, target, Collections.EMPTY_SET);
                    target.close();
                    return new FullAndPartURIs(target.getURI(), 
                            earDirUserURI(dc()).resolve(earURI.relativize(candidateSubmoduleURI)));
                }
            }
        }
        /*
         * The class path element does not seem to be a submodule.
         */
        return null;
    }

    /**
     * Checks whether the candidate URI matches the given submodule URI.
     * Either URI could be to a directory, perhaps because the user has
     * directory-deployed the EAR or perhaps because the app server has expanded
     * submodule JARs into directories in the server's repository.
     * 
     * @param candidateURI possible submodule URI
     * @param submoduleURIText submodule URI text to compare to
     * @return true if the candiateURI matches the submoduleURI, accounting for
     * either or both being directories; false otherwise
     */
    private boolean matchesSubmoduleURI(final URI candidateURI, final String submoduleURIText) {
        Matcher candidateMatcher = submoduleURIPattern.matcher(candidateURI.getPath());
        URI normalizedCandidateURI = (candidateMatcher.matches()
                ? URI.create(candidateMatcher.group(1) + "." + candidateMatcher.group(2))
                : candidateURI);
        candidateMatcher.reset(submoduleURIText);
        URI normalizedSubmoduleURI = (candidateMatcher.matches()
                ? URI.create(candidateMatcher.group(1) + "." + candidateMatcher.group(2))
                : URI.create(submoduleURIText));

        return normalizedCandidateURI.equals(normalizedSubmoduleURI);
    }

    private URI expandedDirURI(final URI submoduleURI) {
        final String uriText = submoduleURI.toString();
        int lastDot = uriText.lastIndexOf('.');
        return URI.create(uriText.substring(0, lastDot) + "_" + uriText.substring(lastDot + 1));
    }
    
    private URI convertExpandedDirToJarURI(final String submoduleURI) {
        URI result = null;
        Matcher m = submoduleURIPattern.matcher(submoduleURI);
        if (m.matches()) {
            result = URI.create(m.group(1) + "." + m.group(2));
        }
        return result;
    }

    @Override
    protected URI facadeServerURI(DeploymentContext dc) {
        File genXMLDir = dc.getScratchDir("xml");
        return genXMLDir.toURI().resolve(relativeFacadeURI(dc));
    }

    @Override
    protected Set<FullAndPartURIs> downloads() {
        return downloads;
    }

    @Override
    protected String facadeClassPath() {
        return classPathForFacade.toString();
    }

    @Override
    protected void prepareJARs() throws IOException, URISyntaxException {
        generateAppClientFacade();
    }

    private String appName(final DeploymentContext dc) {
        DeployCommandParameters params = dc.getCommandParameters(DeployCommandParameters.class);
        return params.name();
    }

    @Override
    protected URI facadeUserURI(DeploymentContext dc) {
        return URI.create(appName(dc) + "Client/" + relativeFacadeURI(dc));
    }

    private URI relativeFacadeURI(DeploymentContext dc) {
        return moduleURI().resolve(facadeFileNameAndType(dc));
    }

    @Override
    protected String facadeFileNameAndType(DeploymentContext dc) {
        return moduleNameOnly() + "Client.jar";
    }

    @Override
    protected URI appClientUserURI(DeploymentContext dc) {
        return earDirUserURI(dc).resolve(moduleURI());
    }

    @Override
    protected URI appClientUserURIForFacade(DeploymentContext dc) {
        return URI.create(Util.getURIName(appClientUserURI(dc)));
    }


    private URI earDirUserURI(final DeploymentContext dc) {
        return URI.create(appName(dc) + "Client/");
    }

    @Override
    protected URI appClientServerURI(DeploymentContext dc) {
        URI result;
        String appClientURIWithinEAR = appClientDesc().getModuleDescriptor().getArchiveUri();
        Matcher m = submoduleURIPattern.matcher(appClientURIWithinEAR);
        if (m.matches()) {
            result = new File(dc.getScratchDir("xml"), m.group(1) + "." + m.group(2)).toURI();
        } else {
            result = new File(new File(earURI), appClientURIWithinEAR).toURI();
        }
        return result;
    }

    @Override
    protected URI appClientURIWithinApp(DeploymentContext dc) {
        return URI.create(appClientDesc().getModuleDescriptor().getArchiveUri());
    }

    private URI moduleURI() {
        return URI.create(appClientDesc().getModuleDescriptor().getArchiveUri());
    }

    private String moduleNameAndType() {
        return Util.getURIName(moduleURI());
    }

    private String moduleNameOnly() {
        String nameAndType = moduleNameAndType();
        return nameAndType.substring(0, nameAndType.lastIndexOf(".jar"));
    }
}
