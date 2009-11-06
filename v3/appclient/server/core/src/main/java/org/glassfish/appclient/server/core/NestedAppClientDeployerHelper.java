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
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.archivist.AppClientArchivist;
import com.sun.enterprise.deployment.deploy.shared.InputJarArchive;
import com.sun.enterprise.deployment.deploy.shared.Util;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.util.XModuleType;
import com.sun.logging.LogDomains;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.appclient.server.core.jws.JWSAdapterManager;
import org.glassfish.appclient.server.core.jws.JavaWebStartInfo;
import org.glassfish.appclient.server.core.jws.servedcontent.ASJarSigner;
import org.glassfish.appclient.server.core.jws.servedcontent.DynamicContent;
import org.glassfish.appclient.server.core.jws.servedcontent.FixedContent;
import org.glassfish.appclient.server.core.jws.servedcontent.StaticContent;
import org.glassfish.appclient.server.core.jws.servedcontent.TokenHelper;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.deployment.common.DownloadableArtifacts;
import org.glassfish.deployment.common.DownloadableArtifacts.FullAndPartURIs;
import org.jvnet.hk2.component.Habitat;

public class NestedAppClientDeployerHelper extends AppClientDeployerHelper {

    private static final String V2_COMPATIBILITY = "v2";

    private final static String LIBRARY_SECURITY_PROPERTY_NAME = "library.security";
    private final static String LIBRARY_JARS_PROPERTY_NAME = "library.jars";
    private final static String LIBRARY_JNLP_PATH_PROPERTY_NAME = "library.jnlp.path";

    private static final String LIBRARY_DOCUMENT_TEMPLATE =
            JavaWebStartInfo.DOC_TEMPLATE_PREFIX + "libraryJarsDocumentTemplate.jnlp";

    private StringBuilder classPathForFacade = new StringBuilder();
    private StringBuilder PUScanTargetsForFacade = new StringBuilder();

    private final URI earURI;

    private final ASJarSigner jarSigner;

    private ApplicationSignedJARManager signedJARManager;

    private StringBuilder libExtensionElementsForMainDocument = null;

    private static Logger logger = LogDomains.getLogger(NestedAppClientDeployerHelper.class, LogDomains.ACC_LOGGER);

    /**
     * records the downloads needed to support this app client,
     * including the app client JAR itself, the facade, and the transitive
     * closure of any library JARs from the EAR's lib directory or from the
     * app client's class path
     */
    private final Set<FullAndPartURIs> clientLevelDownloads = new HashSet<FullAndPartURIs>();
    private Set<FullAndPartURIs> earLevelDownloads = null;
    private final static String EAR_LEVEL_DOWNLOADS_KEY = "earLevelDownloads";

    private final Habitat habitat;

    private final AppClientGroupFacadeGenerator groupFacadeGenerator;

    /** recognizes expanded directory names for submodules */
    private static final Pattern submoduleURIPattern = Pattern.compile("(.*)_([wcrj]ar)$");

    NestedAppClientDeployerHelper(
            final DeploymentContext dc,
            final ApplicationClientDescriptor bundleDesc,
            final AppClientArchivist archivist,
            final ClassLoader gfClientModuleClassLoader,
            final Application application,
            final Habitat habitat,
            final ASJarSigner jarSigner) throws IOException {
        super(dc, bundleDesc, archivist, gfClientModuleClassLoader, application, habitat);
        this.habitat = habitat;
        groupFacadeGenerator = habitat.getComponent(AppClientGroupFacadeGenerator.class);
        this.jarSigner = jarSigner;
        earURI = dc.getSource().getParentArchive().getURI();
        processDependencies();
    }

    @Override
    protected void prepareJARs() throws IOException, URISyntaxException {
        super.prepareJARs();

        // In embedded mode, we don't process app clients so far.
        if (habitat.getComponent(ProcessEnvironment.class).getProcessType().isEmbedded()) {
            return;
        }

        groupFacadeGenerator.run(this);

    }


    @Override
    public FixedContent fixedContentWithinEAR(String uriString) {
        return new FixedContent(new File(earDirUserURI(dc()).resolve(uriString)));
    }

    public String appLibraryExtensions() {
        return (libExtensionElementsForMainDocument == null ? 
            "" : libExtensionElementsForMainDocument.toString());
    }

    @Override
    public Map<String,Map<URI,StaticContent>> signingAliasToJar() {
        return signedJARManager.aliasToContent();
    }


    @Override
    public void createAndAddLibraryJNLPs(final AppClientDeployerHelper helper,
            final TokenHelper tHelper, final Map<String,DynamicContent> dynamicContent) throws IOException {


        /*
         * For each group of like-signed library JARs create a separate JNLP for
         * the group and add it to the dynamic content for the client.  Also
         * build up a property to hold the full list of such generated JNLPs
         * so it can be substituted into the generated client JNLP below.
         */

        libExtensionElementsForMainDocument = new StringBuilder();

        for (Map.Entry<String,Map<URI,StaticContent>> aliasToContentEntry : signingAliasToJar().entrySet()) {
            final String alias = aliasToContentEntry.getKey();
            final Map<URI,StaticContent> libURIs = aliasToContentEntry.getValue();

            tHelper.setProperty(LIBRARY_SECURITY_PROPERTY_NAME, librarySecurity(alias));
            tHelper.setProperty(LIBRARY_JNLP_PATH_PROPERTY_NAME, libJNLPRelPath(alias));
            final StringBuilder libJarElements = new StringBuilder();

            for (Map.Entry<URI,StaticContent> entry : libURIs.entrySet()) {
                final URI uri = entry.getKey();
                libJarElements.append("<jar href=\"" + libJARRelPath(uri) + "\"/>");
            }
            tHelper.setProperty(LIBRARY_JARS_PROPERTY_NAME, libJarElements.toString());

            JavaWebStartInfo.createAndAddDynamicContent(
                    tHelper, dynamicContent, libJNLPRelPath(alias),
                LIBRARY_DOCUMENT_TEMPLATE);
            
            libExtensionElementsForMainDocument.append(extensionElement(alias, libJNLPRelPath(alias)));
        }
        
        tHelper.setProperty(JavaWebStartInfo.APP_LIBRARY_EXTENSION_PROPERTY_NAME,
                libExtensionElementsForMainDocument.toString());
    }

    public Set<FullAndPartURIs> earLevelDownloads() {
        if (earLevelDownloads == null) {
            earLevelDownloads = dc().getTransientAppMetaData(EAR_LEVEL_DOWNLOADS_KEY, HashSet.class);
            if (earLevelDownloads == null) {
                earLevelDownloads = new HashSet<FullAndPartURIs>();
                dc().addTransientAppMetaData(EAR_LEVEL_DOWNLOADS_KEY, earLevelDownloads);
            }
        }
        return earLevelDownloads;
    }

    private String libJARRelPath(final URI absURI) {
        return JavaWebStartInfo.relativeURIForProvidedOrGeneratedAppFile(dc(), absURI, this).toASCIIString();
    }

    private String extensionElement(final String alias, final String libURIText) {
        return "<extension name=\"libJars" + (alias == null ? "" : "-" + alias) +
                "\" href=\"" + libURIText + "\"/>";
    }

    private String librarySecurity(final String alias) {
        return (alias == null ? "" : "<security><all-permissions/></security>");
    }

    private String libJNLPRelPath(final String alias) {
        return "___lib/client-libs" + (alias == null ? "" : "-" + alias) + ".jnlp";
    }

    private void processDependencies() throws IOException {


        signedJARManager = new ApplicationSignedJARManager(
                JWSAdapterManager.signingAlias(dc()),
                jarSigner,
                habitat,
                dc(),
                this,
                earURI,
                earDirUserURI(dc()));

        /*
         * Init the class path for the facade so it refers to the developer's app client,
         * relative to where the facade will be.
         */
        URI appClientURI = URI.create(Util.getURIName(appClientUserURI(dc())));
        classPathForFacade.append(appClientURI);

        /*
         * Because the group facade contains generated stubs (if any), add the
         * relative path to the group facade to the facade's Class-Path so those
         * stubs will be accessible via the class path at runtime.
         */

        final URI groupFacadeURIRelativeToFacade =
                facadeUserURI(dc()).relativize(relativeURIToGroupFacade());
        classPathForFacade.append(" ").append(groupFacadeURIRelativeToFacade.toASCIIString());

        /*
         * For a nested app client, the required downloads include the
         * developer's original app client JAR, the generated facade JAR,
         * the generated EAR-level facade, and
         * the transitive closure of all JARs in the app client's Class-Path
         * and the JARs in the EAR's library-directory.
         *
         * If the user has selected compatibility with v2 behavior, then also
         * consider EJB submodules and JARs at the top level of the EAR.
         */
        clientLevelDownloads.add(new DownloadableArtifacts.FullAndPartURIs(
                facadeServerURI(dc()),
                facadeUserURI(dc())));

        /*
         * dependencyURIsProcessed records URIs, relative to the original JAR as it will
         * reside in the user's download directory, that have already been
         * processed.  This allows us to avoid processing the same JAR or dir more
         * than once if more than one JAR depends on it.
         *
         * Note that all dependencies expressed in the client's manifest must
         * resolve to JARs within the EAR, not within the client. So those
         * dependent JARs will be "EAR-level" not client-level.
         */
        Set<URI> dependencyURIsProcessed = new HashSet<URI>();

        String appClientURIWithinEAR = appClientDesc().getModuleDescriptor().getArchiveUri();
        processDependencies(
                earURI,
                URI.create(appClientURIWithinEAR),
                earLevelDownloads(),
                dependencyURIsProcessed,
                appClientURI,
                false /* isDependencyALibrary */);

        /*
         * Now incorporate the library JARs and, if v2 compatibility is chosen,
         * EJB JARs and top level JARs.
         */
        addLibraryJARs(classPathForFacade, PUScanTargetsForFacade,
                dependencyURIsProcessed);

        if (useV2Compatibility() && ! appClientDesc().getApplication().isVirtual()) {
            addEJBJARs(classPathForFacade, dependencyURIsProcessed);
            addTopLevelJARs(classPathForFacade, PUScanTargetsForFacade,
                    dependencyURIsProcessed);
        }
    }

    private boolean useV2Compatibility() {
        final String compat = dc().getAppProps().getProperty(DeploymentProperties.COMPATIBILITY);
        return (compat != null && compat.equals(V2_COMPATIBILITY));
    }

    /**
     * Adds EJB JARs to the download set for this application.  For compat (if
     * selected) with v2.
     * @param cpForFacade accumulated class path for the generated facade
     * @param dependencyURIsProcessed record of what URIs have been processed
     * @throws IOException
     */
    private void addEJBJARs(final StringBuilder cpForFacade, final Set<URI> dependencyURIsProcessed) throws IOException {
        final Application app = appClientDesc().getApplication();
        for (ModuleDescriptor md : app.getModuleDescriptorsByType(XModuleType.EJB)) {
            addJar(cpForFacade, null,
                   new File(new File(earURI), md.getArchiveUri()).toURI(),
                   dependencyURIsProcessed);
        }
    }

    /**
     * Adds top-level JARs in the EAR to the download set for this application.
     * For compatibility with v2 (if selected).
     *
     * @param cpForFacade accumulated class path for the generated facade
     * @param dependencyURIsProcessed record of what URIs have been processed
     * @throws IOException
     */
    private void addTopLevelJARs(final StringBuilder cpForFacade,
            final StringBuilder puScanTargets,
            final Set<URI> dependencyURIsProcessed) throws IOException {
        /*
         * Add top-level JARs only if they are not submodules.
         */
        final Set<URI> submoduleURIs = new HashSet<URI>();
        for (ModuleDescriptor<BundleDescriptor> md : appClientDesc().getApplication().getModules()) {
            submoduleURIs.add(URI.create(md.getArchiveUri()));
        }

        addJARsFromDir(cpForFacade, puScanTargets, dependencyURIsProcessed,
                new File(earURI),
                new FileFilter() {
            @Override
                    public boolean accept(final File pathname) {
                        return pathname.getName().endsWith(".jar") && ! pathname.isDirectory()
                                && ! submoduleURIs.contains(earURI.relativize(pathname.toURI()));
                    }
                  }
                );
    }

    /**
     * Adds all JARs that pass the filter to the download set for the application.
     *
     * @param cpForFacade accumulated class path for the generated facade
     * @param dependencyURIsProcessed record of what URIs have beeen processed
     * @param dirContainingJARs directory to scan for JARs
     * @param filter file filter to apply to limit which JARs to accept
     * @throws IOException
     */
    private void addJARsFromDir(final StringBuilder cpForFacade,
            final StringBuilder puScanTargets,
            final Set<URI> dependencyURIsProcessed,
            final File dirContainingJARs,
            final FileFilter filter) throws IOException {
        if (dirContainingJARs.exists() && dirContainingJARs.isDirectory()) {
            for (File jar : dirContainingJARs.listFiles(filter)) {
                addJar(cpForFacade, puScanTargets, jar.toURI(), dependencyURIsProcessed);
            }
        }

    }

    private void addLibraryJARs(final StringBuilder cpForFacade,
            final StringBuilder puScanTargets,
            final Set<URI> dependencyURIsProcessed) throws IOException {
        final String libDir = appClientDesc().getApplication().getLibraryDirectory();
        if (libDir != null) {
            addJARsFromDir(cpForFacade, puScanTargets, dependencyURIsProcessed,
                new File(new File(earURI), libDir),
                new FileFilter() {
                @Override
                    public boolean accept(File pathname) {
                        return pathname.getName().endsWith(".jar") && ! pathname.isDirectory();
                    }
                }
            );
        }
    }

    /**
     * Adds a JAR to the download set for the app, adjusting the accumulated
     * classpath for the facade in the process.
     * @param cpForFacade accumulated class path for the facade JAR
     * @param jarURI URI of the JAR to be added
     * @param dependencyURIsProcessed record of which URIs have already been added for this app
     * @throws IOException
     */
    private void addJar(
            final StringBuilder cpForFacade,
            final StringBuilder puScanTargets,
            final URI jarURI,
            final Set<URI> dependencyURIsProcessed) throws IOException {
        final URI jarURIForFacade = earURI.relativize(jarURI);
        final URI fileURIForJAR = URI.create("file:" + jarURI.getRawSchemeSpecificPart());
        if (dependencyURIsProcessed.contains(fileURIForJAR)) {
            return;
        }

        /*
         * Add a relative URI from where the facade will be to where
         * this library JAR will be, once they are both downloaded,
         * to the class path for the facade.
         */
        if (cpForFacade.length() > 0) {
            cpForFacade.append(' ');
        }
        cpForFacade.append(jarURIForFacade.toASCIIString());
        if (puScanTargets != null) {
            if (puScanTargets.length() > 0) {
                puScanTargets.append(' ');
            }
            puScanTargets.append(jarURIForFacade.toASCIIString());
        }

        /*
         * Process this library JAR to record the need to download it
         * and any JARs or directories it depends on.
         */
        processDependencies(earURI, fileURIForJAR, earLevelDownloads(), dependencyURIsProcessed,
                jarURIForFacade, true /* isDependencyALibrary */);
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
     * @param baseURI base against which to resolve the dependency URI (could be
     * the EAR's expansion URI, or could be the URI to a JAR containing a
     * Class-Path element, for example)
     * @param dependencyURI the JAR or directory entry representing a dependency
     * @param downloads the full set of items to be downloaded to support the current client
     * @param dependentURIsProcessed JAR and directory URIs already processed (so
     * we can avoid processing the same JAR or directory multiple times)
     * @throws java.io.IOException
     */
    private void processDependencies(
            final URI baseURI,
            final URI dependencyURI,
            final Set<FullAndPartURIs> downloads,
            final Set<URI> dependencyURIsProcessed,
            final URI containingJARURI,
            final boolean isDependencyALibrary) throws IOException {

        if (dependencyURIsProcessed.contains(dependencyURI)) {
            return;
        }

        /*
         * The dependencyURI could be a ghost one - meaning that the descriptor
         * specifies it as a module JAR but a directory deployment is in
         * progress so that JAR is actually an expanded directory.  In that case
         * we need to generate a JAR for download and build a FullAndPartURIs
         * object pointing to that generated JAR.
         */
        URI dependencyFileURI = baseURI.resolve(dependencyURI);

        /*
         * Make sure the URI has the scheme "file" and not "jar" because
         * dependencies from the Class-Path in a JAR's manifest could be
         * "jar" URIs.  We need "file" URIs to check for existence, etc.
         */

        String scheme = dependencyFileURI.getScheme();
        if (scheme != null && scheme.equals("jar")) {
            dependencyFileURI = URI.create("file:" + dependencyFileURI.getRawSchemeSpecificPart());
        } else {
            if (scheme == null) {
                scheme = "file";
            }
            dependencyFileURI = URI.create(scheme + ":" + dependencyFileURI.getRawSchemeSpecificPart());
        }

        File dependentFile = new File(dependencyFileURI);
        if ( ! dependentFile.exists()) {
            if (isSubmodule(dependencyURI)) {
                dependentFile = JAROfExpandedSubmodule(dependencyURI);
                dependencyFileURI = dependentFile.toURI();
            } else {
                /*
                 * A JAR's Class-Path could contain non-existent JARs.  If there
                 * is no JAR then no more needs to be done with this URI.
                 */
                return;
            }
        }

        /*
         * The app might specify non-existent JARs in its Class-Path.
         */
        if ( ! dependentFile.exists()) {
            return;
        }
        
        if (dependentFile.isDirectory() && ! isSubmodule(dependencyURI)) {
            /*
             * Make sure the dependencyURI (which would have come from a JAR's
             * Class-Path) for this directory ends with a slash.  Otherwise
             * the default system class loader, based on URLClassLoader,
             * will NOT treat it as a directory.
             */
            if (! dependencyURI.getPath().endsWith("/")) {
                final String format = logger.getResourceBundle().
                        getString("enterprise.deployment.appclient.dirURLnoSlash");
                final String msg = MessageFormat.format(format, dependencyURI.getPath(),
                            containingJARURI.toASCIIString());
                logger.log(Level.WARNING, msg);
                ActionReport warning = dc().getActionReport();
                warning.setMessage(msg);
                warning.setActionExitCode(ActionReport.ExitCode.WARNING);
            } else {
            /*
             * This is a directory.  Add all files within it to the set to be
             * downloaded but do not traverse the manifest Class-Path of any
             * contained JARs.
             */
            processDependentDirectory(dependentFile, baseURI, 
                    dependencyURIsProcessed, downloads);
            }
        } else {
            processDependentJAR(dependentFile, baseURI, 
                    dependencyURI, dependencyFileURI, dependencyURIsProcessed, 
                    downloads, containingJARURI, isDependencyALibrary);
        }
        
    }
    
    private void processDependentDirectory(
            final File dependentDirFile,
            final URI baseURI,
            final Set<URI> dependencyURIsProcessed,
            final Set<FullAndPartURIs> downloads) throws IOException {
        
        /*
         * Iterate through this directory and its subdirectories, marking
         * each contained file for download.
         */
        for (File f : dependentDirFile.listFiles()) {
            if (f.isDirectory()) {
                processDependentDirectory(f, baseURI, dependencyURIsProcessed, downloads);
            } else {
                URI dependencyFileURI = f.toURI();
                signedJARManager.addJAR(f.toURI());
                DownloadableArtifacts.FullAndPartURIs fileDependency = new FullAndPartURIs(dependencyFileURI,
                    earDirUserURI(dc()).resolve(earURI.relativize(dependencyFileURI)));
                downloads.add(fileDependency);
            }
        }
    }
    
    private void processDependentJAR(final File dependentFile,
            final URI baseURI,
            final URI dependencyURI,
            final URI dependencyFileURI,
            final Set<URI> dependencyURIsProcessed,
            final Set<FullAndPartURIs> downloads,
            final URI containingJARURI,
            final boolean isDependencyALibrary
            ) throws IOException {

        /*
         * On the client we want the directory to look like this after the
         * download has completed (for example):
         *
         *   downloadDir/  (as specified on the deploy command)
         *      generated-dir-for-this-EAR's-artifacts/
         *         clientFacadeJAR.jar
         *         clientJAR.jar
         *         lib/lib1.jar
         *   ...
         *
         * The "part" portion of the FullAndPartURIs object needs to be the
         * path of the downloaded item relative to the downloadDir in the
         * layout above.
         *
         * To compute that:
         *
         * 1. We resolve the URI of the dependency against the base
         * URI first.  (The base URI will be the directory where
         * the EAR has been expanded for the app client JAR,
         * then might be other paths as we traverse Class-Path chains from the
         * manifests of various JARs we process).
         *
         * 2. Then relativize that against the directory on the
         * server where the EAR has been expanded.  That gives
         * us the relative path within this app's download directory on the
         * client.

         * 3. This app's download directory lies within the user-specified
         * download directory (from the command line).  So we relativize
         * the result so far once more, this time against the download
         * directory on the client system.
         */
        if (isDependencyALibrary) {
            signedJARManager.addJAR(dependencyFileURI);
        }
        DownloadableArtifacts.FullAndPartURIs jarFileDependency = new FullAndPartURIs(dependencyFileURI,
                earDirUserURI(dc()).resolve(earURI.relativize(baseURI.resolve(dependencyURI))));

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
                /*
                 * A Class-Path list might have multiple spaces as a separator.
                 * Ignore empty elements.
                 */
                if (elt.trim().length() > 0) {
                    final URI eltURI = URI.create(elt);
                    if ( ! dependencyURIsProcessed.contains(eltURI)) {
                        processDependencies(dependencyFileURI, URI.create(elt),
                                downloads, dependencyURIsProcessed,
                                containingJARURI, true /* isDependencyALibrary */);
                    }
                }
            }
        }
    }

    private boolean isSubmodule(final URI candidateURI) {
        for (ModuleDescriptor<BundleDescriptor> desc : appClientDesc().getApplication().getModules()) {
            if (URI.create(desc.getArchiveUri()).equals(candidateURI)) {
                return true;
            }
        }
        return false;
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

    private URI convertExpandedDirToJarURI(final String submoduleURI) {
        URI result = null;
        Matcher m = submoduleURIPattern.matcher(submoduleURI);
        if (m.matches()) {
            result = URI.create(m.group(1) + "." + m.group(2));
        }
        return result;
    }

    @Override
    public URI facadeServerURI(DeploymentContext dc) {
        File genXMLDir = dc.getScratchDir("xml");
        return genXMLDir.toURI().resolve(relativeFacadeURI(dc));
    }

    @Override
    protected Set<FullAndPartURIs> clientLevelDownloads() throws IOException {
        return clientLevelDownloads;
    }

    @Override
    protected String facadeClassPath() {
        return classPathForFacade.toString();
    }

    @Override
    protected String PUScanTargets() {
        return PUScanTargetsForFacade.toString();
    }



    @Override
    protected void addGroupFacadeToEARDownloads() {
        final DownloadableArtifacts.FullAndPartURIs earFacadeDownload =
                dc().getTransientAppMetaData("earFacadeDownload", DownloadableArtifacts.FullAndPartURIs.class);
        earLevelDownloads.add(earFacadeDownload);
    }

    private String appName(final DeploymentContext dc) {
        DeployCommandParameters params = dc.getCommandParameters(DeployCommandParameters.class);
        return params.name();
    }

    @Override
    public URI facadeUserURI(DeploymentContext dc) {
        return URI.create(appName(dc) + "Client/" + relativeFacadeURI(dc));
    }

    @Override
    public URI groupFacadeUserURI(DeploymentContext dc) {
        return relativeGroupFacadeURI(dc);
    }

    @Override
    public URI groupFacadeServerURI(DeploymentContext dc) {
        File genXMLDir = dc.getScratchDir("xml");
        return genXMLDir.toURI().resolve(relativeGroupFacadeURI(dc));
    }

    private URI relativeGroupFacadeURI(DeploymentContext dc) {
        return URI.create(appName(dc) + "Client.jar");
    }

    private URI relativeFacadeURI(DeploymentContext dc) {
        return moduleURI().resolve(facadeFileNameAndType(dc));
    }

    @Override
    protected String facadeFileNameAndType(DeploymentContext dc) {
        return moduleNameOnly() + "Client.jar";
    }

    @Override
    public URI appClientUserURI(DeploymentContext dc) {
        return earDirUserURI(dc).resolve(moduleURI());
    }

    @Override
    public URI appClientUserURIForFacade(DeploymentContext dc) {
        return URI.create(Util.getURIName(appClientUserURI(dc)));
    }


    private URI earDirUserURI(final DeploymentContext dc) {
        return URI.create(appName(dc) + "Client/");
    }

    @Override
    public URI appClientServerURI(DeploymentContext dc) {
        URI result;
        String appClientURIWithinEAR = appClientDesc().getModuleDescriptor().getArchiveUri();
        Matcher m = submoduleURIPattern.matcher(appClientURIWithinEAR);
        final File userProvidedJarFile = new File(new File(earURI), appClientURIWithinEAR);
        /*
         * If either the URI specifies the expanded directory for a directory-
         * deployed app client or there is no actual JAR file for the app
         * client (meaning it is an expanded directory),
         * the server-side URI for the app client JAR will need to be in
         * the generated directory.
         */
        if (m.matches()) {
            result = new File(dc.getScratchDir("xml"), m.group(1) + "." + m.group(2)).toURI();
        } else if ( ! userProvidedJarFile.exists())  {
            result = new File(dc.getScratchDir("xml"), appClientURIWithinEAR).toURI();
        } else {
            result = userProvidedJarFile.toURI();
        }
        return result;
    }

    @Override
    public URI appClientURIWithinApp(DeploymentContext dc) {
        return URI.create(appClientDesc().getModuleDescriptor().getArchiveUri());
    }

    @Override
    public URI URIWithinAppDir(DeploymentContext dc, URI absoluteURI) {
        return earURI.relativize(absoluteURI);
    }

    @Override
    public String pathToAppclientWithinApp(DeploymentContext dc) {
        return appClientDesc().getModuleDescriptor().getArchiveUri();
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
