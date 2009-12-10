/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
import org.glassfish.api.deployment.archive.ReadableArchive;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;

/*
 * Represents a deployment of a CDI (Weld) application. 
 */
public class DeploymentImpl implements Deployment {

    private Logger logger = Logger.getLogger(DeploymentImpl.class.getName());

    private static final String WEB_INF_LIB = "WEB-INF/lib";
    private static final String WEB_INF_BEANS_XML = "WEB-INF/beans.xml";
    private static final String META_INF_BEANS_XML = "META-INF/beans.xml";
    private static final String WEB_INF_CLASSES = "WEB-INF/classes";
    private static final String CLASS_SUFFIX = ".class";
    private static final String JAR_SUFFIX = ".jar";
    private static final String EXPLODED_WAR_SUFFIX = "_war/";
    private static final String EXPLODED_JAR_SUFFIX = "_jar/";
    private static final char SEPARATOR_CHAR = '/';

    private List<Class<?>> wbClasses;
    private List<URL> wbUrls;
    private ReadableArchive archive;
    private final List<BeanDeploymentArchive> beanDeploymentArchives;
    private Collection<EjbDescriptor> ejbs;

    private Map<String, BeanDeploymentArchive> idToBeanDeploymentArchive;

    private SimpleServiceRegistry simpleServiceRegistry = null;

    public DeploymentImpl(ReadableArchive archive, Collection<EjbDescriptor> ejbs) {
        this.wbClasses = new ArrayList<Class<?>>();
        this.wbUrls = new ArrayList<URL>();
        this.beanDeploymentArchives = new ArrayList<BeanDeploymentArchive>();
        this.archive = archive;
        this.ejbs = ejbs;
        this.idToBeanDeploymentArchive = new HashMap<String, BeanDeploymentArchive>();
        scan();
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
        idToBeanDeploymentArchive.put(beanClass.getName(), newBda);
        return newBda;
    }

    public ServiceRegistry getServices() {
        if (null == simpleServiceRegistry) {
            simpleServiceRegistry = new SimpleServiceRegistry();
        }
        return simpleServiceRegistry;
    }

    public void scanArchive(ReadableArchive archive, Collection<EjbDescriptor> ejbs) {
        this.wbClasses = new ArrayList<Class<?>>();
        this.wbUrls = new ArrayList<URL>();
        this.ejbs = ejbs;
        this.archive = archive;
        scan();
    }

    public BeanDeploymentArchive getBeanDeploymentArchiveForArchive(String archiveId) {
        return idToBeanDeploymentArchive.get(archiveId);
    }  

    public List<BeanDeploymentArchive> getWarBeanDeploymentArchives() {
        List<BeanDeploymentArchive> warBDAs = new ArrayList<BeanDeploymentArchive>(); 
        Set<Map.Entry<String, BeanDeploymentArchive>> set = idToBeanDeploymentArchive.entrySet();
        for (Map.Entry<String, BeanDeploymentArchive> me : set) {
            if (me.getKey().endsWith(EXPLODED_WAR_SUFFIX)) {
                warBDAs.add(me.getValue());
            }
        }
        return warBDAs;
    }
     
    public List<BeanDeploymentArchive> getJarBeanDeploymentArchives() {
        List<BeanDeploymentArchive> jarBDAs = new ArrayList<BeanDeploymentArchive>();
        Set<Map.Entry<String, BeanDeploymentArchive>> set = idToBeanDeploymentArchive.entrySet();
        for (Map.Entry<String, BeanDeploymentArchive> me : set) {
            if (me.getKey().endsWith(EXPLODED_JAR_SUFFIX)) {
                jarBDAs.add(me.getValue());
            }
        }
        return jarBDAs;
    }
        
    private void scan() {

        try {
            
            // If this archive has WEB-INF/beans.xml entry..
            // Collect all classes in the archive
            // Collect beans.xml in the archive

            if (archive.exists(WEB_INF_BEANS_XML)) {
                Enumeration entries = archive.entries();
                while (entries.hasMoreElements()) {
                    String entry = (String)entries.nextElement();
                    if (entry.endsWith(CLASS_SUFFIX)) {
                        entry = entry.substring(WEB_INF_CLASSES.length()+1);
                        String className = filenameToClassname(entry);
                        wbClasses.add(getClassLoader().loadClass(className));
                    } else if (entry.endsWith("beans.xml")) {
                        URI uri = archive.getURI();
                        File file = new File(uri.getPath() + entry);
                        URL beansXmlUrl = file.toURL();
                        wbUrls.add(beansXmlUrl);
                    }
                }
                archive.close();
            }

            // If this archive has WEB-INF/lib entry..
            // Examine all jars;  If the examined jar has a META_INF/beans.xml:
            //  collect all classes in the jar archive
            //  beans.xml in the jar archive

            if (archive.exists(WEB_INF_LIB)) {
                Enumeration<String> entries = archive.entries(WEB_INF_LIB);
                while (entries.hasMoreElements()) {
                    String entry = (String)entries.nextElement();
                    if (entry.endsWith(JAR_SUFFIX) &&
                        entry.indexOf(SEPARATOR_CHAR, WEB_INF_LIB.length() + 1 ) == -1 ) {
                        ReadableArchive jarArchive = archive.getSubArchive(entry);
                        if (jarArchive.exists(META_INF_BEANS_XML)) {
                            collectJarInfo(jarArchive);
                        }
                    }
               }
            }

            if (archive.exists(META_INF_BEANS_XML)) {
                collectJarInfo(archive);
            }
        
        } catch(IOException e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
        } catch(ClassNotFoundException cne) {
            logger.log(Level.SEVERE, cne.getLocalizedMessage(), cne);
        }

        String archiveId = archive.getURI().getPath();
        BeanDeploymentArchive beanDeploymentArchive = new BeanDeploymentArchiveImpl(archiveId, wbClasses, wbUrls, ejbs);
        idToBeanDeploymentArchive.put(archiveId, beanDeploymentArchive);
        beanDeploymentArchives.add(beanDeploymentArchive);
    }

    private void collectJarInfo(ReadableArchive archive) 
        throws IOException, ClassNotFoundException {
        Enumeration entries = archive.entries();
        while (entries.hasMoreElements()) {
            String entry = (String)entries.nextElement();
            if (entry.endsWith(CLASS_SUFFIX)) {
                String className = filenameToClassname(entry);
                wbClasses.add(getClassLoader().loadClass(className));
            } else if (entry.endsWith("beans.xml")) {
                URL beansXmlUrl = Thread.currentThread().getContextClassLoader().getResource(entry);
                wbUrls.add(beansXmlUrl);
            }
        }
        archive.close();
    }


    private static String filenameToClassname(String filename) {
        String className = null;
        if (filename.indexOf(File.separatorChar) >= 0) {
            className = filename.replace(File.separatorChar, '.');
        } else {
            className = filename.replace(SEPARATOR_CHAR, '.');
        }
        className = className.substring(0, className.length()-6);
        return className;
    }

    private ClassLoader getClassLoader() {
        if (Thread.currentThread().getContextClassLoader() != null) {
            return Thread.currentThread().getContextClassLoader();
        } else {
            return DeploymentImpl.class.getClassLoader();
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
}
