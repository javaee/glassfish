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
import org.jvnet.hk2.component.Inhabitant;

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
            // which is in <Root Installation>/modules/containerName
            String root = System.getProperty("com.sun.aas.installRoot");
            File location = new File(root);
            location = new File(location, "modules");
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

        // first the right container from that module.
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            //Thread.currentThread().setContextClassLoader(glueModule.getClassLoader());
            Inhabitant<? extends ContainerProvider> provider = habitat.getInhabitant(ContainerProvider.class, sniffer.getModuleType());
            if (provider==null) {
                logger.severe("Cannot find ContainerProvider named " + sniffer.getModuleType());
                logger.severe("Cannot start " + sniffer.getModuleType() + " container");
                return null;
            }
            Thread.currentThread().setContextClassLoader(provider.type().getClassLoader());
            ContainerProvider container = provider.get();
            //ContainerProvider container = habitat.getComponent(ContainerProvider.class, sniffer.getModuleType());
            if (container!=null) {
                ContainerInfo info = new ContainerInfo(container, sniffer);

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
        
        return new ContainerInfo(null, sniffer);
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
