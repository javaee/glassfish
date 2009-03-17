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
package org.glassfish.javaee.core.deployment;

import java.io.OutputStream;
import org.glassfish.api.deployment.*;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.container.Container;
import org.glassfish.api.ActionReport;
import org.glassfish.api.event.Events;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.deployment.common.DownloadableArtifacts;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.internal.data.*;
import org.glassfish.deployment.common.DeploymentContextImpl;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PerLookup;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.util.XModuleType;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.deploy.shared.OutputJarArchive;
import com.sun.enterprise.deployment.deploy.shared.Util;
import com.sun.enterprise.universal.io.FileUtils;
import com.sun.logging.LogDomains;

import java.util.*;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.glassfish.deployment.common.DeploymentException;

/**
 * EarDeployer to deploy composite Java EE applications.
 * todo : could be generified into any composite applications.
 *
 * @author Jerome Dochez
 */
@Service
@Scoped(PerLookup.class)
public class EarDeployer implements Deployer {

//    private static final Class GLASSFISH_APPCLIENT_GROUP_FACADE_CLASS =
//            org.glassfish.appclient.client.AppClientGroupFacade.class;
// Currently using a string instead of a Class constant to avoid a circular
// dependency.  
    private static final String GLASSFISH_APPCLIENT_GROUP_FACADE_CLASS_NAME =
            "org.glassfish.appclient.client.AppClientGroupFacade";

    private static final Attributes.Name GLASSFISH_APPCLIENT_GROUP = new Attributes.Name("GlassFish-AppClient-Group");

    @Inject
    Habitat habitat;

    @Inject
    Deployment deployment;

    @Inject
    ServerEnvironment env;

    @Inject
    ApplicationRegistry appRegistry;

    @Inject
    ArchiveFactory archiveFactory;

    @Inject
    Events events;

    @Inject
    private DownloadableArtifacts artifacts;

    final static Logger logger = LogDomains.getLogger(EarDeployer.class, LogDomains.DPL_LOGGER);
    
    public MetaData getMetaData() {
        return new MetaData(false, null, new Class[] { Application.class});
    }

    public Object loadMetaData(Class type, DeploymentContext context) {
        return null;
    }

    public boolean prepare(final DeploymentContext context) {

        Application application = context.getModuleMetaData(Application.class);

        DeployCommandParameters deployParams = context.getCommandParameters(DeployCommandParameters.class);
        final String appName = deployParams.name();
        
        final ApplicationInfo appInfo = new CompositeApplicationInfo(context.getSource(), appName);
        for (Object m : context.getModuleMetadata()) {
            appInfo.addMetaData(m);
        }

        final Map<ModuleDescriptor, ExtendedDeploymentContext> contextPerModules =
                this.initSubContext(application, context);

        try {
            doOnAllBundles(application, new BundleBlock<ModuleInfo>() {
                public ModuleInfo doBundle(ModuleDescriptor bundle) throws Exception {
                    ModuleInfo info = prepareBundle(bundle, contextPerModules.get(bundle));
                    appInfo.addModule(info);
                    return info;
                }

            });
        } catch(Exception e) {

        }

        context.addModuleMetaData(appInfo);
        generateArtifacts(context);
        return true;
    }

    protected void generateArtifacts(final DeploymentContext context) throws DeploymentException {
        /*
         * For EARs, currently only nested app clients will generate artifacts.
         */
        final Application application = context.getModuleMetaData(Application.class);
        final Collection<ModuleDescriptor<BundleDescriptor>> appClients =
                application.getModuleDescriptorsByType(XModuleType.CAR);

        final StringBuilder appClientGroupListSB = new StringBuilder();

        /*
         * For each app client, get its URI to include in the generated facade's
         * client group listing.
         */
        for (Iterator<ModuleDescriptor<BundleDescriptor>> it = appClients.iterator(); it.hasNext(); ) {
            ModuleDescriptor<BundleDescriptor> md = it.next();
            appClientGroupListSB.append((appClientGroupListSB.length() > 0) ? " " : "").append(md.getArchiveUri());
        }

        try {
            generateAndRecordEARFacade(
                    application.getRegistrationName(),
                    context.getScratchDir("xml"),
                    generatedEARFacadeName(application.getRegistrationName()), appClientGroupListSB.toString());
        } catch (Exception e) {
            throw new DeploymentException(e);
        }


    }

    private String generatedEARFacadeName(final String earName) {
        return earName + "Client.jar";
    }

    private void generateAndRecordEARFacade(final String earName,
            final File appScratchDir,
            final String facadeFileName,
            final String appClientGroupList) throws IOException {

        File generatedJar = new File(appScratchDir, facadeFileName);
        OutputJarArchive facadeArchive = new OutputJarArchive();
        facadeArchive.create(generatedJar.toURI());

        Manifest manifest = facadeArchive.getManifest();
        Attributes mainAttrs = manifest.getMainAttributes();

        mainAttrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        mainAttrs.put(Attributes.Name.MAIN_CLASS, GLASSFISH_APPCLIENT_GROUP_FACADE_CLASS_NAME);
        mainAttrs.put(GLASSFISH_APPCLIENT_GROUP, appClientGroupList);


        //Now manifest is ready to be written into the facade jar
        OutputStream os = facadeArchive.putNextEntry(JarFile.MANIFEST_NAME);
        manifest.write(os);
        facadeArchive.closeEntry();

        final String mainClassResourceName =
                GLASSFISH_APPCLIENT_GROUP_FACADE_CLASS_NAME.replace('.', '/') +
                ".class";
        os = facadeArchive.putNextEntry(mainClassResourceName);

        try {
            InputStream is = openByteCodeStream("/" + mainClassResourceName);
            FileUtils.copyStream(is, os);
            is.close();
        } catch (Exception e) {
            throw new DeploymentException(e);
        }

        Set<DownloadableArtifacts.FullAndPartURIs> downloads =
                    new HashSet<DownloadableArtifacts.FullAndPartURIs>();
        downloads.add(new DownloadableArtifacts.FullAndPartURIs(generatedJar.toURI(), facadeFileName));
        artifacts.addArtifacts(earName, downloads);

    }

    private InputStream openByteCodeStream(final String classResourceName) throws URISyntaxException, MalformedURLException, IOException {
        URI currentModule = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
        URI classURI = currentModule.resolve("gf-client-module.jar!" + classResourceName);
        return new URI("jar", classURI.toASCIIString(), null).toURL().openStream();
//        return getClass().getResourceAsStream(classResourceName);
    }

    private class CompositeApplicationInfo extends ApplicationInfo {

        Application application=null;

        private CompositeApplicationInfo(ReadableArchive source, String name) {
            super(events, source, name);
        }

        @Override
        public void load(ExtendedDeploymentContext context, ActionReport report, ProgressTracker tracker) throws Exception {

            application = context.getModuleMetaData(Application.class);
            final Map<ModuleDescriptor, ExtendedDeploymentContext> contextPerModules =
                    initSubContext(application, context);
            
            for (ModuleInfo module : super.getModuleInfos()) {
                final ModuleDescriptor md = application.getModuleDescriptorByUri(module.getName());
                module.load(contextPerModules.get(md), report, tracker);
            }
        }

        @Override
        public void start(DeploymentContext context, ActionReport report, ProgressTracker tracker) throws Exception {

            if (application==null) {
                return;
            }
            
            final Map<ModuleDescriptor, ExtendedDeploymentContext> contextPerModules =
                    initSubContext(application, context);

            for (ModuleInfo module : super.getModuleInfos()) {
                final ModuleDescriptor md = application.getModuleDescriptorByUri(module.getName());
                module.start(contextPerModules.get(md), report, tracker);
            }
        }

        @Override
        public void unload(ExtendedDeploymentContext context, ActionReport report) {

            if (application==null) {
                return;
            }
            
            final Map<ModuleDescriptor, ExtendedDeploymentContext> contextPerModules =
                    initSubContext(application, context);

            for (ModuleInfo module : super.getModuleInfos()) {
                final ModuleDescriptor md = application.getModuleDescriptorByUri(module.getName());
                module.unload(contextPerModules.get(md), report);
            }

        }

        @Override
        public void clean(ExtendedDeploymentContext context) throws Exception {

            if (application==null) {
                return;
            }
            
            final Map<ModuleDescriptor, ExtendedDeploymentContext> contextPerModules =
                    initSubContext(application, context);

            for (ModuleInfo module : super.getModuleInfos()) {
                final ModuleDescriptor md = application.getModuleDescriptorByUri(module.getName());
                module.clean(contextPerModules.get(md));
            }            
        }
    }

    
    private Collection<ModuleDescriptor<BundleDescriptor>>
                doOnAllTypedBundles(Application application, XModuleType type, BundleBlock runnable)
                    throws Exception {

        final Collection<ModuleDescriptor<BundleDescriptor>> typedBundles = application.getModuleDescriptorsByType(type);
        for (ModuleDescriptor module : typedBundles) {
            runnable.doBundle(module);
        }
        return typedBundles;
    }

    private void doOnAllBundles(Application application, BundleBlock runnable) throws Exception {

        Collection<ModuleDescriptor> bundles = new HashSet<ModuleDescriptor>();
        bundles.addAll(application.getModules());

        // first we take care of the connectors
        bundles.removeAll(doOnAllTypedBundles(application, XModuleType.RAR, runnable));

        // now the EJBs
        bundles.removeAll(doOnAllTypedBundles(application, XModuleType.EJB, runnable));

        // finally the war files.
        bundles.removeAll(doOnAllTypedBundles(application, XModuleType.WAR, runnable));

        // now ther remaining bundles
        for (final ModuleDescriptor bundle : bundles) {
            runnable.doBundle(bundle);
        }
        
    }

    private ModuleInfo prepareBundle(final ModuleDescriptor md, final ExtendedDeploymentContext bundleContext)
        throws Exception {

        LinkedList<EngineInfo> orderedContainers = null;

        ActionReport report = habitat.getComponent(ActionReport.class, "hk2-agent");
        ProgressTracker tracker = new ProgressTracker() {
            public void actOn(Logger logger) {
                for (EngineRef module : get("prepared", EngineRef.class)) {
                    module.clean(bundleContext, logger);
                }

            }

        };

        try {
            // let's get the list of containers interested in this module
            orderedContainers = deployment.setupContainerInfos(bundleContext, report);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return deployment.prepareModule(orderedContainers, md.getArchiveUri(), bundleContext, report, tracker);
    }

    public ApplicationContainer load(Container container, DeploymentContext context) {

        // this will never be called.
        return null;
    }

    public void unload(ApplicationContainer appContainer, DeploymentContext context) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void clean(DeploymentContext context) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private interface BundleBlock<T> {

        public T doBundle(ModuleDescriptor bundle) throws Exception;
    }

    public Map<ModuleDescriptor, ExtendedDeploymentContext> initSubContext(final Application application, final DeploymentContext context) {

        Map<ModuleDescriptor, ExtendedDeploymentContext> results = new HashMap<ModuleDescriptor, ExtendedDeploymentContext>();

        for (final BundleDescriptor bd : application.getBundleDescriptors()) {
            if (!results.containsKey(bd.getModuleDescriptor())) {
                final String moduleUri = 
                    bd.getModuleDescriptor().getArchiveUri();
                final ReadableArchive subArchive;
                try {
                    subArchive = context.getSource().getSubArchive(moduleUri);
                    subArchive.setParentArchive(context.getSource());
                } catch(IOException ioe) {
                    ioe.printStackTrace();
                    return null;
                }
                
                final Properties moduleProps = 
                    getModuleProps(context, moduleUri);

                ExtendedDeploymentContext subContext = new DeploymentContextImpl(logger, context.getSource(),
                        context.getCommandParameters(OpsParams.class), env) {

                    @Override
                    public ClassLoader getClassLoader() {
                        try {
                            EarClassLoader appCl = EarClassLoader.class.cast(context.getClassLoader());
                            return appCl.getModuleClassLoader(moduleUri);
                        } catch (ClassCastException e) {
                            return context.getClassLoader();
                        }                        
                    }

                    @Override
                    public ClassLoader getFinalClassLoader() {
                        return context.getFinalClassLoader();
                    }

                    @Override
                    public ReadableArchive getSource() {
                        return subArchive;
                    }

                    @Override
                    public Properties getProps() {
                        return moduleProps;
                    }

                    @Override
                    public ReadableArchive getOriginalSource() {
                        try {
                            File appRoot = context.getSourceDir();
                            File origModuleFile = new File(appRoot, moduleUri); 
                            return archiveFactory.openArchive(
                                origModuleFile);
                        } catch (IOException ioe) {
                            return null;
                        }
                    }

                    @Override
                    public File getScratchDir(String subDirName) {
                        String modulePortion = Util.getURIName(
                            getSource().getURI());
                        return (new File(super.getScratchDir(subDirName), 
                            modulePortion));
                    }

                    @Override
                    public <T> T getModuleMetaData(Class<T> metadataType) {
                        try {
                            return metadataType.cast(bd);
                        } catch (Exception e) {
                            // let's first try the extensions mechanisms...
                            if (RootDeploymentDescriptor.class.isAssignableFrom(metadataType)) {
                                for (RootDeploymentDescriptor extension  : bd.getExtensionsDescriptors((Class<RootDeploymentDescriptor>) metadataType)) {
                                    // we assume there can only be one type of
                                    if (extension!=null) {
                                        try {
                                            return metadataType.cast(extension);
                                        } catch (Exception e1) {
                                            // next one...
                                        }
                                    }
                                }
                                
                            }

                            return context.getModuleMetaData(metadataType);
                        }
                    }
                };
                results.put(bd.getModuleDescriptor(), subContext);
            }
        }
        return results;
    }

    private Properties getModuleProps(DeploymentContext context, 
        String moduleUri) {
        Map<String, Properties> modulePropsMap = context.getModulePropsMap();
        Properties moduleProps = modulePropsMap.get(moduleUri);
        if (moduleProps == null) {
            moduleProps = new Properties();
            modulePropsMap.put(moduleUri, moduleProps);
        }

        // add the properties from parent context if such property does not
        // exist in current context properties
        for (Enumeration e = context.getProps().keys(); e.hasMoreElements();) {
            String propName = (String)e.nextElement();
            if (moduleProps.contains(propName)) {
                continue;
            }
            moduleProps.put(propName, context.getProps().get(propName));
        }

        return moduleProps;
    }
}
