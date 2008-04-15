/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.Repository;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.common_impl.DirectoryBasedRepository;
import com.sun.enterprise.module.common_impl.AbstractRepositoryImpl;

import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.io.FileFilter;
import java.io.FileNotFoundException;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class ASMainHK2 extends com.sun.enterprise.module.bootstrap.Main {

    Logger logger;
    ASMainHelper helper;

    public ASMainHK2(Logger logger) {
        this.logger = logger;
        helper = new ASMainHelper(logger);
    }

    // Constructor used by RunMojo (gf:run)
    public ASMainHK2() {
        this(Logger.getAnonymousLogger());
    }

    protected void setParentClassLoader(StartupContext context, ModulesRegistry mr) throws BootException {

        ClassLoader cl = this.getClass().getClassLoader();
        mr.setParentClassLoader(cl);

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
            mr.setParentClassLoader(cl);
        }

        // now install the java-ee APIs. this has to be at a very high level in the hierarchy
        Module parentModule = mr.makeModuleFor("org.glassfish:javax.javaee", null);
        if(parentModule!=null) {
            cl = parentModule.getClassLoader();
        }

        helper.parseAsEnv(context.getRootDirectory().getParentFile());
        File domainRoot = helper.getDomainRoot(context);
        helper.verifyDomainRoot(domainRoot);

        List<Repository> libs = new ArrayList<Repository>();
        //add jdk tools.jar
        Repository jdkToolsRepo = helper.getJDKToolsRepo();
        if (jdkToolsRepo!=null) {
            libs.add(jdkToolsRepo);
        }
        // do we have a lib ?
        Repository lib = mr.getRepository("lib");
        if (lib!=null) {
            libs.add(lib);

            // do we have a domain lib ?
            File domainlib = new File(domainRoot, "lib");
            if (domainlib.exists()) {
                Repository domainLib = new DirectoryBasedRepository("domnainlib", domainlib);
                try {
                    domainLib.initialize();
                    mr.addRepository(domainLib);
                    libs.add(domainLib);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error while initializing domain lib repository", e);
                }

            }
        }
        if (libs.size() > 0) {
            cl = helper.setupSharedCL(cl, libs);
        }

        // finally
        mr.setParentClassLoader(cl);

    }

    /**
     * Gets the shared repository and add all subdirectories as Repository
     *
     * @param root installation root
     * @param bootstrapJar
     *      The file from which manifest entries are loaded. Used for error reporting
     * @param mf main module manifest
     * @param mr modules registry
     * @throws BootException
     */
    @Override
    protected void createRepository(File root, File bootstrapJar, Manifest mf, ModulesRegistry mr) throws BootException {

        super.createRepository(root, bootstrapJar, mf, mr);
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

}
