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

package org.glassfish.ejb.embedded;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.ejb.spi.EJBContainerProvider;
import com.sun.ejb.containers.EjbContainerUtilImpl;

import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.embedded.ContainerBuilder;
import org.glassfish.api.embedded.EmbeddedDeployer;
import org.glassfish.api.embedded.EmbeddedFileSystem;
import org.glassfish.api.embedded.Server;

import org.glassfish.deployment.common.GenericAnnotationDetector;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import org.jvnet.hk2.component.Habitat;

/**
 * GlassFish implementation of the EJBContainerProvider.
 *
 * @author Marina Vatkina
 */
public class EJBContainerProviderImpl implements EJBContainerProvider {

    private static final String GF_PROVIDER_NAME = EJBContainerProviderImpl.class.getName();
    private static final String JAR_FILE_EXT = ".jar";
    private static final Attributes.Name ATTRIBUTE_NAME_SKIP = new Attributes.Name("Bundle-SymbolicName");
    private static final String[] ATTRIBUTE_VALUES_SKIP = 
            {"org.glassfish.", "com.sun.enterprise.", "org.eclipse."};
    private static final String[] ATTRIBUTE_VALUES_OK = {"sample", "test"};


    // Use Bundle from another package
    private static final Logger _logger = 
            LogDomains.getLogger(EjbContainerUtilImpl.class, LogDomains.EJB_LOGGER);
    private static final StringManager localStrings = StringManager.getManager(EJBContainerProviderImpl.class);

    private static final Object lock = new Object();

    private static EJBContainerImpl container;
    private static Server server;
    private static Habitat habitat;
    private static ArchiveFactory archiveFactory;
    private static Class[] ejbAnnotations = null;

    public EJBContainerProviderImpl() {}

    public EJBContainer createEJBContainer(Map<?, ?> properties) throws EJBException {
        if (properties == null || properties.get(EJBContainer.PROVIDER) == null || 
                properties.get(EJBContainer.PROVIDER).equals(GF_PROVIDER_NAME)) {

            if (container != null && container.isOpen()) {
                throw new EJBException(localStrings.getString(
                        "ejb.embedded.exception_exists_container"));
            }

            init(properties);
            try {
                Set<File> modules = new HashSet<File>();
                Set<String> moduleNames = addEJBModules(modules, properties);
                if (modules.isEmpty()) {
                    _logger.log(Level.SEVERE, "No EJB modules found");
                }

                container.deploy(properties, modules, moduleNames);
                // Check again - expected module names list might not have matched the found modules
                if (modules.isEmpty()) {
                    _logger.log(Level.SEVERE, "No EJB modules found");
                }


                return container;
            } catch (Throwable t) {
                // Can't throw an exception - only return null.
                _logger.log(Level.SEVERE, "ejb.embedded.exception_instantiating", t);
            }
        }

        return null;
    }

    private void init(Map<?, ?> properties) throws EJBException {
        synchronized(lock) {
            // if (container == null || !container.isOpen()) {
                Server.Builder builder = new Server.Builder("GFEJBContainerProviderImpl");

                File installed_root = null;
                File domain_file = null;
                if (properties != null) {
                    String gf_root = (String) properties.get(
                            "glassfish.ejb.embedded.glassfish.installation");
System.err.println("+++ gf_root : " + gf_root);
                    if (gf_root != null) {
                        installed_root = new File(gf_root);
                        if (!installed_root.exists()) {
                            _logger.log(Level.SEVERE, "ejb.embedded.location_not_exists", gf_root);
                            installed_root = null;
                        } else {
                            // TODO - support a separate location for the domain
                            String domain_root = gf_root + File.separatorChar 
                                    + "domains" + File.separatorChar 
                                    + "domain1" + File.separatorChar 
                                    + "config" + File.separatorChar 
                                    + "domain.xml";
System.err.println("+++ domain_root : " + domain_root);
                            domain_file = new File(domain_root);
                            if (!domain_file.exists()) {
                                _logger.log(Level.SEVERE, "ejb.embedded.location_not_exists", domain_root);
                                installed_root = null;
                            }
                        }
                    }
                }
System.err.println("+++ installed_root: " + installed_root);
System.err.println("+++ domain_file: " + domain_file);

                if (installed_root == null) {
                    server = builder.build();
                } else {
                    //TODO: Stop previous version?

                    EmbeddedFileSystem.Builder efsb = new EmbeddedFileSystem.Builder();
                    efsb.setInstallRoot(installed_root);
                    efsb.setConfigurationFile(domain_file);

                    builder.setEmbeddedFileSystem(efsb.build());
                    server = builder.build();

                }

                EjbBuilder ejb = server.createConfig(EjbBuilder.class);
                habitat = ejb.habitat;

                archiveFactory = habitat.getComponent(ArchiveFactory.class);

                EmbeddedEjbContainer ejbContainer = server.addContainer(ejb);
                server.addContainer(ContainerBuilder.Type.jpa);
                EmbeddedDeployer deployer = server.getDeployer();

                Sniffer sniffer = habitat.getComponent(Sniffer.class, "Ejb");
                ejbAnnotations = sniffer.getAnnotationTypes();

                container = new EJBContainerImpl(server, ejbContainer, deployer);
            // }
        }
    }

    /**
     * Adds EJB modules for the property in the properties Map and in the future
     * from the System classpath
     */
    private Set<String> addEJBModules(Set<File> modules, Map<?, ?> properties) {
        Object obj = (properties == null)? null : properties.get(EJBContainer.MODULES);
        Set<String> moduleNames = new HashSet<String>();

        // Check EJBContainer.MODULES setting first - it can have an explicit set of files
        if (obj != null) {
            // Process File objects directly
            if (obj instanceof File) {
                addEJBModule(modules, (File)obj);
            } else if (obj instanceof File[]) {
                File[] arr = (File[])obj;
                for (File f : arr) {
                    addEJBModule(modules, f);
                }
            // Check module names separately
            } else if (obj instanceof String) {
                moduleNames.add((String)obj);
            } else if (obj instanceof String[]) {
                String[] arr = (String[])obj;
                for (String s : arr) {
                    moduleNames.add(s);
                }
            }
        } 

        if (modules.isEmpty()) {
            // No file is specified - load from the classpath
            String path = System.getProperty("java.class.path");
            String[] entries = path.split(File.pathSeparator);
            for (String s0 : entries) {
                addEJBModule(modules, new File(s0));
            }
        }

        return moduleNames;
    }

    /**
     * @returns true if this file represents EJB module.
     */
    private boolean isEJBModule(File file) throws IOException {
        System.err.println("... Testing ... " + file.getName());
        ReadableArchive archive = archiveFactory.openArchive(file);

        boolean handles =  archive.exists("META-INF/ejb-jar.xml");
        if (!handles) {
            GenericAnnotationDetector detector =
                new GenericAnnotationDetector(ejbAnnotations);
            handles = detector.hasAnnotationInArchive(archive);
        }
        return handles;
    }

    /**
     * Adds an a File to the Set of EJB modules if it represents an EJB module.
     */
    private void addEJBModule(Set<File> modules, File f) {
        try {
            if (f.exists() && isEJBModule(f) && !skipJar(f)) {
                modules.add(f);
                _logger.info("... Added EJB Module .... " + f.getName());
            } // skip the rest
        } catch (IOException ioe) {
            _logger.log(Level.INFO, "ejb.embedded.io_exception", ioe);
            // skip it
        }
    }

    /**
     * @returns true if this jar is either a GlassFish module jar or one
     * of the other known implementation modules.
     */
    private boolean skipJar(File file) throws IOException {
        if (!file.isFile()) {
            return false; // probably a directory
        }

        JarFile jf = new JarFile(file);
        Manifest m = jf.getManifest();
        if (m != null) {
            java.util.jar.Attributes attributes = m.getMainAttributes();
            String value = attributes.getValue(ATTRIBUTE_NAME_SKIP);
            if (value != null) {
                for (String skipValue : ATTRIBUTE_VALUES_SKIP) {
                    if (value.startsWith(skipValue)) {
                        for (String okValue : ATTRIBUTE_VALUES_OK) {
                            if (value.indexOf(okValue) > 0) {
                                // Still OK
                                return false;
                            }
                        }
                        // Not OK - skip it
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
