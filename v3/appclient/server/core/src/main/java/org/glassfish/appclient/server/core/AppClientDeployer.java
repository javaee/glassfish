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
package org.glassfish.appclient.server.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import org.glassfish.deployment.common.DownloadableArtifacts;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.deploy.shared.OutputJarArchive;
import com.sun.enterprise.deployment.deploy.shared.Util;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.universal.io.FileUtils;
import com.sun.logging.LogDomains;
import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Logger;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.deployment.common.DownloadableArtifacts.FullAndPartURIs;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.DummyApplication;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.javaee.core.deployment.JavaEEDeployer;
import org.glassfish.loader.util.ASClassLoaderUtil;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;

/**
 * AppClient module deployer.
 *
 */
@Service
public class AppClientDeployer
        extends JavaEEDeployer<AppClientContainerStarter, DummyApplication> {

    private static Logger logger = LogDomains.getLogger(AppClientDeployer.class, LogDomains.ACC_LOGGER);

    public static final String APPCLIENT_FACADE_CLASS_FILE = "org/glassfish/appclient/client/AppClientFacade.class";
    public static final String APPCLIENT_COMMAND_CLASS_NAME = "org.glassfish.appclient.client.AppClientFacade";
    public static final String GLASSFISH_APPCLIENT_MAIN_CLASS_KEY = "GlassFish-AppClient-Main-Class";
    public static final String GLASSFISH_APPCLIENT_KEY = "GlassFish-AppClient";
    public static final String SPLASH_SCREEN_IMAGE_KEY = "SplashScreen-Image";

    @Inject
    protected ServerContext sc;
    @Inject
    protected Domain domain;
    @Inject
    protected Habitat habitat;
    @Inject
    private DownloadableArtifacts downloadInfo;

    public AppClientDeployer() {
    }

    protected String getModuleType() {
        return "appclient";
    }

    @Override
    public MetaData getMetaData() {
        return new MetaData(false, null, new Class[]{Application.class});
    }

    @Override
    public DummyApplication load(AppClientContainerStarter containerStarter, DeploymentContext dc) {
        return new DummyApplication();
    }

    public void unload(DummyApplication application, DeploymentContext dc) {
    }

    /**
     * Clean any files and artifacts that were created during the execution
     * of the prepare method.
     *
     * @param dc deployment context
     */
    @Override
    public void clean(DeploymentContext dc) {
        super.clean(dc);
        UndeployCommandParameters params = dc.getCommandParameters(UndeployCommandParameters.class);
        downloadInfo.clearArtifacts(params.name);
    }

    @Override
    protected void generateArtifacts(DeploymentContext dc) throws DeploymentException {
        ApplicationClientDescriptor bundleDesc = dc.getModuleMetaData(ApplicationClientDescriptor.class);
        Application application = bundleDesc.getApplication();
        ModuleDescriptor modDesc = bundleDesc.getModuleDescriptor();
        DeployCommandParameters params = dc.getCommandParameters(DeployCommandParameters.class);
        boolean insideEar = !application.isVirtual();

        String modUri = modDesc.getArchiveUri();
        if(!modUri.endsWith(".jar")) {
            modUri += ".jar";
        }

        try {
            ReadableArchive source = dc.getSource();
            ReadableArchive parentArchive = source.getParentArchive();
            ReadableArchive originalSource = ((ExtendedDeploymentContext) dc).getOriginalSource();
            Set<DownloadableArtifacts.FullAndPartURIs> downloads =
                    new HashSet<DownloadableArtifacts.FullAndPartURIs>();

            URI originalSourceURI = originalSource.getURI();
            String originalJarName = Util.getURIName(originalSourceURI);
            String renamedModUri = renameModUri(modUri, parentArchive);
            String renamedJarName = insideEar ?
                Util.getURIName(URI.create(renamedModUri)) : renamedModUri;

            File appScratchDir = dc.getScratchDir("xml");
            File generatedJar = new File(appScratchDir, originalJarName);
            OutputJarArchive facadeArchive = new OutputJarArchive();
            facadeArchive.create(generatedJar.toURI());

            Manifest manifest = facadeArchive.getManifest();
            Attributes mainAttrs = manifest.getMainAttributes();

            mainAttrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
            mainAttrs.put(Attributes.Name.MAIN_CLASS, APPCLIENT_COMMAND_CLASS_NAME);
            mainAttrs.putValue(GLASSFISH_APPCLIENT_MAIN_CLASS_KEY,
                    getManifestAttributeValue(source, Attributes.Name.MAIN_CLASS.toString()));
            mainAttrs.putValue(GLASSFISH_APPCLIENT_KEY, renamedJarName);

            String splash = getManifestAttributeValue(source, SPLASH_SCREEN_IMAGE_KEY);
            if(splash != null) {
                mainAttrs.putValue(SPLASH_SCREEN_IMAGE_KEY, splash);
                ClientJarMakerUtils.copy(source, facadeArchive, splash);
            }
            
            String classPathVal = renamedJarName;
            if(insideEar) {
                String originalClassPath = getManifestAttributeValue(source, 
                        Attributes.Name.CLASS_PATH.toString());
                if(originalClassPath != null) {
//                    classPathVal += " " + originalClassPath;  not needed
                    for(String p : originalClassPath.split(" ")) {
                        recordClassPathRefs(parentArchive.getURI(), originalSource.getURI(), p, downloads);
                    }
                }

                String libDir = application.getLibraryDirectory();
                if(libDir != null) {
                    String libRelativePath = getRelativePathToLib(libDir, modUri);
                    List<URL> libJars = ASClassLoaderUtil.getAppLibDirLibrariesAsList(
                            new File(parentArchive.getURI()), libDir);
                    for(URL u : libJars) {
                        URI full = u.toURI();
                        String jarName = Util.getURIName(full);
                        String relativeLibJar = libRelativePath + jarName;
                        downloads.add(new DownloadableArtifacts.FullAndPartURIs(full,
                                libDir + "/" + jarName));
                        classPathVal += " " + relativeLibJar;  //attribute values auto wrap
                    }
                }
            }
            mainAttrs.put(Attributes.Name.CLASS_PATH, classPathVal);

            //Now manifest is ready to be written into the facade jar
            OutputStream os = facadeArchive.putNextEntry(JarFile.MANIFEST_NAME);
            manifest.write(os);
            facadeArchive.closeEntry();

            os = facadeArchive.putNextEntry(APPCLIENT_FACADE_CLASS_FILE);
            InputStream is = openByteCodeStream("/" + APPCLIENT_FACADE_CLASS_FILE);
            FileUtils.copyStream(is, os);
            
            try {
                is.close();
                facadeArchive.close();  //may have been closed in copyStream
            } catch (IOException ignore) {
            }

            //TODO generatermistubs, if set to true in deploy request

            downloads.add(new DownloadableArtifacts.FullAndPartURIs(originalSourceURI, renamedModUri));
            downloads.add(new DownloadableArtifacts.FullAndPartURIs(facadeArchive.getURI(), modUri));

            if(insideEar) {
                String modUriNoExt = modUri.substring(0, modUri.lastIndexOf("."));
                String parentName = parentArchive.getName();
                downloadInfo.addArtifacts(parentName + "/" + modUriNoExt, downloads);
                downloadInfo.addArtifacts(parentName, downloads);
            } else {
                downloadInfo.addArtifacts(params.name(), downloads);
            }
        } catch (Exception ex) {
            throw new DeploymentException(ex);
        }
    }

    private InputStream openByteCodeStream(final String resourceName) throws URISyntaxException, MalformedURLException, IOException {
       URI currentModule = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
       URI classURI = currentModule.resolve("gf-client-module.jar!" + resourceName);
       return new URI("jar", classURI.toASCIIString(), null).toURL().openStream();
    }

    /**
     * Records all the jar files that are referenced in Class-Path entries of
     * appclient jars. Referenced jars themselves may also have references.
     * @param earURI  the partURI for downloading should be relative to earURI
     * @param referencing the referencing jar, which may be appclient jar or jars
     *        referenced, directly or indirectly, by appclient jar
     * @param ref the referenced jar
     * @param dlds the set of download info
     */
    private void recordClassPathRefs(URI earURI, URI referencing, String ref, Set<FullAndPartURIs> dlds)
        throws IOException {
        File referencingFile = new File(referencing.getSchemeSpecificPart());
        String parentDir = referencingFile.getParent();
        File refFullFile = new File(parentDir, ref);
        URI refFullURI = refFullFile.toURI().normalize();
        URI refPartURI = earURI.relativize(refFullURI);
        dlds.add(new FullAndPartURIs(refFullURI, refPartURI));

        JarFile jarFile = new JarFile(refFullFile);
        Manifest manifest = jarFile.getManifest();
        jarFile.close();
        String cp = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
        if(cp != null) {
            for(String p : cp.split(" ")) {
                recordClassPathRefs(earURI, refFullURI, p, dlds);
            }
        }
    }

    /**
     * Gets the path to the library directory of the enclosing EAR, relative to
     * the current appclient jar.  The modUri may or may not contain subdirectories,
     * for example, foo.jar, or 1/2/3.jar.
     * @param libDir
     * @param modUri
     * @return
     */
    private String getRelativePathToLib(String libDir, String modUri) {
        String result = "../" + libDir + "/";
        int pos = modUri.indexOf("/", 1);
        while(pos >= 1) {
            result = "../" + result;
            pos = modUri.indexOf("/", pos + 1);
        }
        return result;
    }

    private String getManifestAttributeValue(ReadableArchive archive, String name) throws IOException {
        Manifest manifest = archive.getManifest();
        Attributes attrs = manifest.getMainAttributes();
        return attrs.getValue(name);
    }

    private String renameModUri(String jarName, ReadableArchive parent) throws IOException {
        String ext = ".jar";
        String suffix = ".orig";
        String nameNoExt = jarName.substring(0, jarName.length() - 4); //".jar" length
        String newName = nameNoExt + suffix + ext;
        if (parent != null) {
            for (int i = 1; parent.exists(newName); i++) {
                newName = nameNoExt + suffix + "-" + i + ext;
            }
        }
        return newName;
    }

    /** Determines whether a client jar needs to be generated.  It can be skipped
     * if both conditions are met:
     * a. the deployment request does not request the deployer to generate stubs, and
     * b. there are no library JARs from an EAR that must be available to the client.

     * There will be no library JARs required if

     * (1) the app client being deployed is stand-alone (that is, not nested inside an EAR), or
     * (2) the current deployment is an EAR but:
     *    (a) the application.xml contains &lt;library-directory/&gt;
     *        (which turns off all library directory handling) or
     *    (b) the library directory (either explicit or default) contains no JARs.
     */
    private boolean needToGenerateClientJar(DeploymentContext dc) {
        DeployCommandParameters params = dc.getCommandParameters(DeployCommandParameters.class);
        if (params.generatermistubs) {
            return true;
        }

        ApplicationClientDescriptor bundleDesc = dc.getModuleMetaData(ApplicationClientDescriptor.class);
        Application application = bundleDesc.getApplication();
        if (application.isVirtual()) {
            return false;
        }

        String libraryDirectory = application.getLibraryDirectory();
        if (libraryDirectory == null || libraryDirectory.length() == 0) {
            return false;
        }

        //        the search for *.jar should not be recursive
        File libDir = new File(dc.getSourceDir(), libraryDirectory);
        File[] jarsUnder = libDir.listFiles(new FileFilter() {

            public boolean accept(File f) {
                if (f.isFile() && f.getName().endsWith(".jar")) {
                    return true;
                }
                return false;
            }
        });

        if (jarsUnder == null || jarsUnder.length == 0) {
            return false;
        }

        return true;
    }

}
