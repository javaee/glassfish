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

package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.module.*;
import com.sun.enterprise.module.impl.DirectoryBasedRepository;
import com.sun.enterprise.module.bootstrap.BootException;

import java.net.URLClassLoader;
import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Tag Main to get the manifest file 
 */
public class Main extends com.sun.enterprise.module.bootstrap.Main {

    final static Logger logger = Logger.getAnonymousLogger();
    
    public static void main(final String args[]) {
        (new Main()).run(args);   
    }


    @Override
    protected void setParentClassLoader(ModulesRegistry mr) throws BootException {

        ClassLoader cl = this.getClass().getClassLoader();
                
        // first we mask JAXB if necessary.
        // mask the JAXB and JAX-WS API in the bootstrap classloader so that
        // we get to load our copies in the modules

        Module shared = mr.makeModuleFor("org.glassfish.external:glassfish-jaxb", null);

        if (shared!=null) {
            List<URL> urls = new ArrayList<URL>();
            for (URI location : shared.getModuleDefinition().getLocations()) {
                try {
                    urls.add(location.toURL());
                } catch (MalformedURLException e) {
                    throw new BootException("Cannot set up masking class loader", e);
                }
            }

            cl = new MaskingClassLoader(
                cl,
                urls.toArray(new URL[urls.size()]),
                "javax.xml.bind.",
                "javax.xml.ws.",
                "com.sun.xml."
            );
        }

        // do we have a lib ?
        Repository lib = mr.getRepository("lib");
        if (lib!=null) {
            cl = setupSharedCL(cl, lib);    
        }

        // finally
        mr.setParentClassLoader(cl);
    }

    /**
     * Gets the shared repository and add all subdirectories as Repository
     *
     * @param root installation root
     * @param mf main module manifest
     * @param mr modules registry
     * @throws BootException
     */
    @Override
    protected void createRepository(File root, Manifest mf, ModulesRegistry mr) throws BootException {

        super.createRepository(root, mf, mr);
        Repository repo = mr.getRepository("shared");
        File repoLocation = new File(repo.getLocation());
        for (File file : repoLocation.listFiles(
                new FileFilter() {
                    public boolean accept(File pathname) {
                        return pathname.isDirectory();
                    }
                }))
        {
            try {
                Repository newRepo = new DirectoryBasedRepository(file.getName(), file);
                newRepo.initialize();
                mr.addRepository(newRepo);
            } catch(FileNotFoundException e) {
                
            } catch(IOException e) {
                logger.log(Level.SEVERE, "Cannot initialize repository at " + file.getAbsolutePath(), e);
            }
        }
    }

    private ClassLoader setupSharedCL(ClassLoader parent, Repository sharedRepo) {


        List<URI> uris = sharedRepo.getJarLocations();
        URL[] urls = new URL[uris.size()];
        int i=0;
        for (URI uri : uris) {
            try {
                urls[i++] = uri.toURL();
            } catch (MalformedURLException e) {
                logger.warning("Error while adding library to shared classpath " + e.getMessage());
            }
        }

        return new ExtensibleClassLoader(urls, parent, sharedRepo);
    }

    private class ExtensibleClassLoader extends URLClassLoader
        implements RepositoryChangeListener {

        public ExtensibleClassLoader(URL[] urls, ClassLoader parent, Repository repo) {
            super(urls, parent);
            repo.addListener(this);
        }

        public void jarAdded(URI uri) {
            try {
                super.addURL(uri.toURL());
                logger.info("Added " + uri + " to shared classpath, no need to restart appserver");
            } catch (MalformedURLException e) {
                logger.log(Level.SEVERE, "Cannot add new added library to shared classpath", e);
            }

        }
        public void jarRemoved(URI uri) {
        }

        public void moduleAdded(ModuleDefinition moduleDefinition) {
        }

        public void moduleRemoved(ModuleDefinition moduleDefinition) {
        }
    }    

}
