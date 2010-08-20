/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.weld;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.deployment.EjbDescriptor;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.javaee.core.deployment.ApplicationHolder;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;

/*
 * Represents a deployment of a CDI (Weld) application. 
 */
public class DeploymentImpl implements Deployment {

    private Logger logger = Logger.getLogger(DeploymentImpl.class.getName());

    private static final String META_INF_BEANS_XML = "META-INF/beans.xml";
    private static final String JAR_SUFFIX = ".jar";
    private static final char SEPARATOR_CHAR = '/';

    // Keep track of our BDAs for this deployment

    private List<BeanDeploymentArchive> jarBDAs;
    private List<BeanDeploymentArchive> warBDAs;
    private List<BeanDeploymentArchive> libJarBDAs = null;

    private ReadableArchive archive;
    private List<BeanDeploymentArchive> beanDeploymentArchives = null;
    private Collection<EjbDescriptor> ejbs;
    private DeploymentContext context;

    // A convenience Map to get BDA for a given BDA ID

    private Map<String, BeanDeploymentArchive> idToBeanDeploymentArchive;

    private SimpleServiceRegistry simpleServiceRegistry = null;

    /**
     * Produce <code>BeanDeploymentArchive</code>s for this <code>Deployment</code>
     * from information from the provided <code>ReadableArchive</code>. 
     */
    public DeploymentImpl(ReadableArchive archive, Collection<EjbDescriptor> ejbs,
                          DeploymentContext context) {
        this.beanDeploymentArchives = new ArrayList<BeanDeploymentArchive>();
        this.archive = archive;
        this.ejbs = ejbs;
        this.context = context;
        this.idToBeanDeploymentArchive = new HashMap<String, BeanDeploymentArchive>();

        // Collect /lib Jar BDAs (if any) from the parent module.
        // If we've produced BDA(s) from any /lib jars, <code>return</code> as
        // additional BDA(s) will be produced for any subarchives (war/jar).

        if (null == libJarBDAs) {
            libJarBDAs = scanForLibJars(archive, ejbs, context);
            if (null != libJarBDAs && libJarBDAs.size() > 0) {
                return;
            }
        }

        BeanDeploymentArchive bda = new BeanDeploymentArchiveImpl(archive, ejbs);
        this.beanDeploymentArchives.add(bda);
        if (((BeanDeploymentArchiveImpl)bda).getBDAType().equals(BeanDeploymentArchiveImpl.WAR)) {
            if (null == warBDAs) {
                warBDAs = new ArrayList();
            }
            warBDAs.add(bda);
        } else if (((BeanDeploymentArchiveImpl)bda).getBDAType().equals(BeanDeploymentArchiveImpl.JAR)) {
            if (null == jarBDAs) {
                jarBDAs = new ArrayList();
            }
            jarBDAs.add(bda);
        }
        this.idToBeanDeploymentArchive.put(bda.getId(), bda);
    }

    /**
     * Produce <code>BeanDeploymentArchive</code>s for this <code>Deployment</code>
     * from information from the provided <code>ReadableArchive</code>. 
     * This method is called for subsequent modules after This <code>Deployment</code> has
     * been created.
     */
    public void scanArchive(ReadableArchive archive, Collection<EjbDescriptor> ejbs,
                            DeploymentContext context) {

        if (null == libJarBDAs) {
            libJarBDAs = scanForLibJars(archive, ejbs, context);
            if (null != libJarBDAs && libJarBDAs.size() > 0) {
                return;
            }
        }

        BeanDeploymentArchive bda = new BeanDeploymentArchiveImpl(archive, ejbs);

        this.archive = archive;
        this.ejbs = ejbs;
        this.context = context;

        if (null == idToBeanDeploymentArchive) {
            idToBeanDeploymentArchive = new HashMap<String, BeanDeploymentArchive>();
        }

        beanDeploymentArchives.add(bda);
        if (((BeanDeploymentArchiveImpl)bda).getBDAType().equals(BeanDeploymentArchiveImpl.WAR)) {
            if (null == warBDAs) {
                warBDAs = new ArrayList();
            }
            warBDAs.add(bda);
        } else if (((BeanDeploymentArchiveImpl)bda).getBDAType().equals(BeanDeploymentArchiveImpl.JAR)) {
            if (null == jarBDAs) {
                jarBDAs = new ArrayList();
            }
            jarBDAs.add(bda);
        }
        idToBeanDeploymentArchive.put(bda.getId(), bda);

    }

    /**
     * Build the accessibility relationship between <code>BeanDeploymentArchive</code>s
     * for this <code>Deployment</code>.  This method must be called after all <code>Weld</code>
     * <code>BeanDeploymentArchive</code>s have been produced for the 
     * <code>Deployment</code>.
     */
    public void buildDeploymentGraph() {

        // Make jars accessible to each other - Example:
        //    /ejb1.jar <----> /ejb2.jar
        // If there are any application (/lib) jars, make them accessible

        if (null != jarBDAs) {
            ListIterator jarIter = jarBDAs.listIterator();
            while (jarIter.hasNext()) {
                boolean modifiedArchive = false;
                BeanDeploymentArchive jarBDA = (BeanDeploymentArchive)jarIter.next();
                ListIterator jarIter1 = jarBDAs.listIterator();
                while (jarIter1.hasNext()) {
                    BeanDeploymentArchive jarBDA1 = (BeanDeploymentArchive)jarIter1.next();
                    if (jarBDA1.getId().equals(jarBDA.getId())) {
                        continue;
                    }
                    jarBDA.getBeanDeploymentArchives().add(jarBDA1);
                    modifiedArchive = true;
                }

                // Make /lib jars (application) accessible
                if (null != libJarBDAs) {
                    ListIterator libJarIter = libJarBDAs.listIterator();
                    while (libJarIter.hasNext()) {
                        BeanDeploymentArchive libJarBDA = (BeanDeploymentArchive)libJarIter.next();
                        jarBDA.getBeanDeploymentArchives().add(libJarBDA);
                    }
                }

                if (modifiedArchive) {
                    int idx = getBeanDeploymentArchives().indexOf(jarBDA);
                    if (idx >= 0) {
                        getBeanDeploymentArchives().remove(idx);
                        getBeanDeploymentArchives().add(jarBDA);
                    }
                    modifiedArchive = false;
                }
            }
        }

        // Make jars (external to WAR modules) accessible to WAR BDAs - Example:
        //    /web.war ----> /ejb.jar
        // If there are any application (/lib) jars, make them accessible

        if (null != warBDAs) {
            ListIterator warIter = warBDAs.listIterator();
            boolean modifiedArchive = false;
            while (warIter.hasNext()) {
                BeanDeploymentArchive warBDA = (BeanDeploymentArchive)warIter.next();
                if (null != jarBDAs) {
                    ListIterator jarIter = jarBDAs.listIterator();
                    while (jarIter.hasNext()) {
                        BeanDeploymentArchive jarBDA = (BeanDeploymentArchive)jarIter.next();
                        warBDA.getBeanDeploymentArchives().add(jarBDA);
                        modifiedArchive = true;
                    }
                }

                // Make /lib jars (application) accessible

                if (null != libJarBDAs) {
                    ListIterator libJarIter = libJarBDAs.listIterator();
                    while (libJarIter.hasNext()) {
                        BeanDeploymentArchive libJarBDA = (BeanDeploymentArchive)libJarIter.next();
                        warBDA.getBeanDeploymentArchives().add(libJarBDA);
                        modifiedArchive = true;
                    }
                }

                if (modifiedArchive) {
                    int idx = getBeanDeploymentArchives().indexOf(warBDA);
                    if (idx >= 0) {
                        getBeanDeploymentArchives().remove(idx);
                        getBeanDeploymentArchives().add(warBDA);
                    }
                    modifiedArchive = false;
                }
            }
        }

      
    }


    public List<BeanDeploymentArchive> getBeanDeploymentArchives() {
        
        if (beanDeploymentArchives.size() > 0) {
            return beanDeploymentArchives;
        }
        return Collections.emptyList(); 
    }

    public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass) {
        List<BeanDeploymentArchive> beanDeploymentArchives = getBeanDeploymentArchives();
        ListIterator<BeanDeploymentArchive> lIter = beanDeploymentArchives.listIterator(); 
        while (lIter.hasNext()) {
            BeanDeploymentArchive bda = lIter.next();
            if (bda.getBeanClasses().contains(beanClass)) {
                return bda;
            }
        }

        // If the BDA was not found for the Class, create one and add it

        List<Class<?>> wClasses = new ArrayList<Class<?>>();
        List<URL> wUrls = new ArrayList<URL>();
        Set<EjbDescriptor> ejbs = new HashSet<EjbDescriptor>();
        wClasses.add(beanClass);
        BeanDeploymentArchive newBda = new BeanDeploymentArchiveImpl(beanClass.getName(), wClasses, wUrls, ejbs);
        lIter = beanDeploymentArchives.listIterator();
        while (lIter.hasNext()) {
            BeanDeploymentArchive bda = lIter.next();
            bda.getBeanDeploymentArchives().add(newBda);
        }
        return newBda;
    }

    public ServiceRegistry getServices() {
        if (null == simpleServiceRegistry) {
            simpleServiceRegistry = new SimpleServiceRegistry();
        }
        return simpleServiceRegistry;
    }



    public BeanDeploymentArchive getBeanDeploymentArchiveForArchive(String archiveId) {
        return idToBeanDeploymentArchive.get(archiveId);
    }

    public void cleanup() {
        if (null != jarBDAs) {
            jarBDAs.clear();
        }
        if (null != warBDAs) {
            warBDAs.clear();
        }
        if (null != libJarBDAs) {
            libJarBDAs.clear();
        }
        if (null != idToBeanDeploymentArchive) {
            idToBeanDeploymentArchive.clear();
        }
    }

    public String toString() {
        String val = null;
        List<BeanDeploymentArchive> beanDeploymentArchives = getBeanDeploymentArchives();
        ListIterator<BeanDeploymentArchive> lIter = beanDeploymentArchives.listIterator();        
        while (lIter.hasNext()) {
            BeanDeploymentArchive bda = lIter.next();
            val += bda.toString(); 
        }
        return val;
    }

    // This method creates and returns a List of BeanDeploymentArchives for each
    // Weld enabled jar under /lib of an existing Archive.
    
    private List scanForLibJars(ReadableArchive archive, Collection<EjbDescriptor> ejbs,
                                DeploymentContext context) {
        List libJars = null;
        ApplicationHolder holder = context.getModuleMetaData(ApplicationHolder.class);
        if (null != holder && null != holder.app) {
            String libDir = holder.app.getLibraryDirectory();
            if (libDir != null && !libDir.isEmpty()) {
                Enumeration<String> entries = archive.entries(libDir);
                while (entries.hasMoreElements()) {
                    String entryName = entries.nextElement();
                    // if a jar in lib dir and not WEB-INF/lib/foo/bar.jar
                    if (entryName.endsWith(JAR_SUFFIX) &&
                        entryName.indexOf(SEPARATOR_CHAR, libDir.length() + 1 ) == -1 ) {
                        try {
                            ReadableArchive jarInLib = archive.getSubArchive(entryName);
                            if (jarInLib.exists(META_INF_BEANS_XML)) {
                                if (null == libJars) {
                                    libJars = new ArrayList();
                                }
                                libJars.add(jarInLib);
                            }
                        } catch (IOException e) {
                        }
                    }
                }
            }
        }
        if (null != libJars) {
            ListIterator libJarIterator = libJars.listIterator();
            while (libJarIterator.hasNext()) {
                ReadableArchive libJarArchive = (ReadableArchive)libJarIterator.next();
                BeanDeploymentArchive bda = new BeanDeploymentArchiveImpl(libJarArchive, ejbs);
                this.beanDeploymentArchives.add(bda);
                if (null == libJarBDAs) {
                    libJarBDAs = new ArrayList();
                    libJarBDAs.add(bda);
                }
                this.idToBeanDeploymentArchive.put(bda.getId(), bda);
            }

        }
        return libJarBDAs;
    }
}
