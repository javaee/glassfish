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

package com.sun.enterprise.v3.server;

import com.sun.enterprise.module.*;
import com.sun.enterprise.module.impl.CookedModuleDefinition;
import com.sun.enterprise.v3.admin.Utils;
import com.sun.enterprise.v3.data.ContainerInfo;
import com.sun.enterprise.v3.data.ContainerRegistry;
import org.glassfish.api.container.Container;
import org.glassfish.api.container.ContainerProvider;
import org.glassfish.api.container.Sniffer;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is responsible for starting containers, it will look for the container
 * installation location, will eventually download the container and install it locally
 *
 * @author Jerome Dochez
 */
public class ContainerStarter {

    ModulesRegistry modulesRegistry;

    Habitat habitat;

    Logger logger;

    public ContainerStarter(ModulesRegistry modulesRegistry, Habitat habitat, Logger logger) {
        this.habitat = habitat;
        this.modulesRegistry = modulesRegistry;
        this.logger = logger;
    }

    public ContainerInfo startContainer(Sniffer sniffer) {

        assert sniffer!=null;
        String containerName = sniffer.getModuleType();
        assert containerName!=null;
        // version is null so far...
        String version = null;
        
        // now we need to find the glue code which can be packaged inside
        // our glassfish lib directory or within the container implementation
        String bundleName = "gf-" + containerName + "-connector";
        String jarFileName = bundleName + ".jar";

        // get the container installation
        String containerHome = Utils.getProperty(containerName + ".home");
        if (containerHome==null) {
            // the container could be installed at the default location
            // which is in <Root Installation>/lib/containerName
            String root = System.getProperty("com.sun.aas.installRoot");
            File location = new File(root);
            location = new File(location, "lib");
            location = new File(location, containerName);
            containerHome = location.getAbsolutePath();
            System.setProperty(containerName + ".home", containerHome);
        }

        assert containerHome!=null;


        // I do the container setup first so the code has a chance to set up
        // repositories which would allow access to the connector module.
        try {

            sniffer.setup(containerHome, logger);
        } catch(FileNotFoundException fnf) {
            logger.log(Level.SEVERE, fnf.getMessage());
            return null;
        } catch(IOException ioe) {
            logger.log(Level.SEVERE, ioe.getMessage(), ioe);
            return null;

        }

        // first try, we get the glue code from our repositories.
        Module glueModule=null;
        try {
            // TODO : this is really bad hack jerome
            glueModule = modulesRegistry.makeModuleFor("org.glassfish.extras:" + bundleName, version);
            if (glueModule==null) {
                glueModule = modulesRegistry.makeModuleFor("org.glassfish.web:" + bundleName, version); 
            }
        } catch(ResolveError e) {
            logger.log(Level.SEVERE, "Resolution Error ", e);
            return null;
        } catch(Exception e) {
            // various bad things can happen here, log and return
            logger.severe(e.getMessage());
            return null;
        }
        if (glueModule==null) {
            // ok we don't know about the connector module yet, let's try to
            // find it.
            Repository connector = habitat.getComponent(Repository.class, "connectors");

            // now we are looking for the connector module. The connector module
            // will be searched in the following location in the specified order :
            //  1. in the containerHome/lib directory
            //  2. in the connectors directory of the application server
            //  3. in the current list of modules we know about
            //  4. anywhere in the containerHome directory (can be slow)
            //
            // let's find the jar file in the container lib installation file system
            File jarLocation = new File(containerHome, "lib");
            jarLocation = new File(jarLocation, jarFileName);

            ModuleDefinition moduleDef=null;

            try {

                if (jarLocation.exists()) {
                    // option 1
                    moduleDef = new CookedModuleDefinition(jarLocation, null);

                } else {

                    // option 2
                    if (connector!=null) {
                        moduleDef = connector.find(bundleName, version);
                    }
                    if (moduleDef==null) {
                        // option 3
                        glueModule= modulesRegistry.makeModuleFor(bundleName, version);
                        if (glueModule==null) {
                            // option 4
                            jarLocation = findFile(jarFileName, new File(containerHome));
                            if (jarLocation!=null) {
                                moduleDef = new CookedModuleDefinition(jarLocation, null);
                            }
                        }
                    }
                }

            } catch(IOException e) {
                logger.severe("container installation failed, aborting");
                return null;
            }

            if (glueModule==null) {
                // we must have a module definition
                if (moduleDef!=null) {
                    // TODO : we need to compare the moduleDef version and the passed parameter version

                    modulesRegistry.add(moduleDef);
                    glueModule = modulesRegistry.makeModuleFor(moduleDef.getName(), moduleDef.getVersion());
                    if (glueModule==null) {
                        logger.log(Level.SEVERE, "connector module " + bundleName + " not found in " + containerHome);
                         // TODO : throw ResolveError
                         return null;
                    }
                } else {
                    logger.severe("Cannot find connector module " + jarFileName);
                    return null;                    
                }
            }

        }

        // first the right container from that module.
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(glueModule.getClassLoader());
            ContainerProvider container = habitat.getComponent(ContainerProvider.class, sniffer.getModuleType());
            if (container!=null) {
                ContainerInfo info = new ContainerInfo(container, sniffer, glueModule);

                ContainerRegistry registry = habitat.getComponent(ContainerRegistry.class);
                registry.addContainer(info);
                return info;
            }
        } catch (ComponentException e) {
            logger.log(Level.SEVERE, "Cannot create or inject Container", e);
            return null;
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }

        // there is no container implementation satisfying the sniffer's module type.
        // that could be ok as there may not be an application container associated
        // with this module type but only a request handler.
        
        return new ContainerInfo(null, sniffer, glueModule);
    }

    /**
     * Finds a file on the file system starting at a location, this
     * can be potentially slow, do not call this method unless you
     * really have to.
     *
     * @param jarFileName
     * @param location
     * @return  the file location on the file system
     */
    private File findFile(String jarFileName, File location) {
        File[] files = location.listFiles();
        if (files==null) {
            return null;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                File l = findFile(jarFileName, file);
                if (l!=null) {
                    return l;
                }
            } else {
                if (file.getName().equals(jarFileName)) {
                    return file;
                }
            }
        }
        return null;
    }

    private File installContainer(String containerName, ModuleDefinition def) {

        Attributes attr = def.getManifest().getMainAttributes();
        if (attr==null) {
            logger.severe("No container installation attributes provided in manifest");
            return null;
        }


        String remoteRepo = attr.getValue("RemoteRepository");
        if (remoteRepo==null) {
            logger.severe("ContractProvider is not installed and RemoteRepository to download it from is not provided");
            return null;
        }

        URI repoURI;
        try {
            repoURI = new URI(remoteRepo);
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, "Invalid remote repository location : " + e.getMessage(), e );
            return null;
        }
        RepositoryFactory factory = RepositoryFactories.getInstance().getFactoryFor(repoURI.getScheme());
        if (factory==null) {
            logger.log(Level.SEVERE, "Cannot handle repositories of type " + repoURI.getScheme());
            return null;
        }
        URI uri=null;
        try {
            uri = new URI(repoURI.getSchemeSpecificPart());
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, "Invalid repository location " + repoURI.getSchemeSpecificPart(),e);
            return null;
        }

        Repository repo = factory.createRepository(containerName, uri);
        if (repo==null) {
            logger.log(Level.SEVERE, "Cannot instantiate repository " + repoURI.getSchemeSpecificPart());
            return null;
        }
        String remoteBundleName = attr.getValue("RemoteBundleName");
        if (remoteBundleName.indexOf(",")!=-1) {
            // there is more than one dependency from the connector, panic !
            logger.severe(containerName + " connector has too many dependencies " + remoteBundleName);
        }
        String remoteBundleVersion = attr.getValue("RemoteBundleVersion");
        ModuleDefinition remoteModuleDef = repo.find(remoteBundleName, remoteBundleVersion);
        if (remoteModuleDef==null) {
            logger.severe("Cannot find "+ remoteBundleName +" from " + repo.getName());
            return null;
        }
        Manifest remoteManifest = remoteModuleDef.getManifest();
        String installType = remoteManifest.getMainAttributes().getValue("InstallationType");
        if (installType==null) {
            installType="Explode";
        }
        // TODO : have an installation pluggability story
        // TODO : support main-class attribute which would be an installer type ?
        if (installType.equalsIgnoreCase("Explode")) {

            // let's create the target repository
            String root = System.getProperty("com.sun.aas.installRoot");
            File repository = new File(root);
            repository = new File(repository, "lib");
            repository = new File(repository, containerName);

            repository.mkdirs();

            // take the remote module location and exploded it locally
            for (URI location : remoteModuleDef.getLocations()) {
                InputStream is=null;
                FileOutputStream os=null;
                File outFile=null;
                try {
                    is = location.toURL().openStream();
                    ReadableByteChannel channel = Channels.newChannel(is);
                    // this naming scheme may need to be reworked...
                    outFile = new File(System.getProperty("java.io.tmpdir"), containerName + ".jar");
                    os = new FileOutputStream(outFile);
                    FileChannel outChannel = os.getChannel();
                    long bytes;
                    long transferedBytes=0;
                    do {
                        bytes = outChannel.transferFrom(channel,transferedBytes , 4096);
                        transferedBytes+=bytes;
                    } while (bytes==4096);
                } catch(MalformedURLException e) {
                } catch(IOException e) {
                    
                } finally {
                    try {
                        if (is!=null)
                            is.close();
                    } catch(IOException e) {
                        // ignore
                    }
                    try {
                        if (os!=null)
                            os.close();
                    } catch(IOException e) {
                        // ignore
                    }
                }

                // now we have downloaded the remote bundle, explode it locally
                if (outFile!=null) {

                    try {
                        explodeJar(outFile, repository);
                    } catch(IOException e) {
                        logger.log(Level.SEVERE, "Cannot expand downloaded container : ", e);
                        return null;
                    }
                    // delete the downloaded file
                    if (!outFile.delete()) {
                        logger.log(Level.INFO, "Cannot delete the downloaded container file : " + outFile.getAbsolutePath());
                    }
                    return repository;
                }

            }
            
        }
        return null;
    }

    public static void explodeJar(File source, File destination) throws IOException {

        JarFile jarFile = null;
        try {
            jarFile = new JarFile(source);
            Enumeration<JarEntry> e = jarFile.entries();
            while (e.hasMoreElements()) {
                JarEntry entry = e.nextElement();
                String fileSystemName = entry.getName().replace('/', File.separatorChar);
                File out = new File(destination, fileSystemName);

                if (entry.isDirectory()) {
                    out.mkdirs();
                } else {
                    InputStream is = null;
                    FileOutputStream fos = null;
                    try {
                        if (!out.getParentFile().exists()) {
                            out.getParentFile().mkdirs();
                        }
                        is = new BufferedInputStream(jarFile.getInputStream(entry));
                        fos = new FileOutputStream(out);
                        ReadableByteChannel inChannel = Channels.newChannel(is);
                        FileChannel outChannel = fos.getChannel();
                        outChannel.transferFrom(inChannel, 0, entry.getSize());
                    } finally {
                        if (is!=null)
                            is.close();
                        if (fos!=null)
                            fos.close();
                    }
                }
            }
        } finally {
            if (jarFile != null) {
                jarFile.close();
            }
        }
    }
}
