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
import com.sun.enterprise.v3.data.ContainerInfo;
import com.sun.enterprise.v3.data.ContainerRegistry;
import com.sun.enterprise.util.StringUtils;
import org.glassfish.api.container.Container;
import org.glassfish.api.container.Sniffer;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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

    public Collection<ContainerInfo> startContainer(Sniffer sniffer, Module snifferModule) {

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
        String containerHome = StringUtils.getProperty(containerName + ".home");
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


        Module mainModule = null;
        // I do the container setup first so the code has a chance to set up
        // repositories which would allow access to the connector module.
        try {

            mainModule = sniffer.setup(containerHome, logger);
            if (mainModule!=null) {
                snifferModule.addImport(mainModule);
            }
        } catch(FileNotFoundException fnf) {
            logger.log(Level.SEVERE, fnf.getMessage());
            return null;
        } catch(IOException ioe) {
            logger.log(Level.SEVERE, ioe.getMessage(), ioe);
            return null;

        }

        // first the right container from that module.
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        List<ContainerInfo> containers = new ArrayList<ContainerInfo>();
        for (String name : sniffer.getContainersNames()) {

            try {
                Inhabitant<? extends Container> provider = habitat.getInhabitant(Container.class, name);
                if (provider==null) {
                    logger.severe("Cannot find Container named " + sniffer.getModuleType());
                    logger.severe("Cannot start " + sniffer.getModuleType() + " container");
                    return null;
                }
                Thread.currentThread().setContextClassLoader(snifferModule.getClassLoader());
                if (provider!=null) {
                    ContainerInfo info = new ContainerInfo(provider, sniffer);

                    ContainerRegistry registry = habitat.getComponent(ContainerRegistry.class);
                    registry.addContainer(name, info);
                    containers.add(info);

                    if (mainModule==null) {
                        info.setMainModule(snifferModule);
                    } else {
                        info.setMainModule(mainModule);
                    }
                }
            } catch (ComponentException e) {
                logger.log(Level.SEVERE, "Cannot create or inject Container", e);
                return null;
            } finally {
                Thread.currentThread().setContextClassLoader(cl);
            }

        }                                       
        return containers;
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
