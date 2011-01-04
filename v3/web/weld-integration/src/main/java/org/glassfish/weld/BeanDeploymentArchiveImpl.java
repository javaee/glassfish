/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2011 Oracle and/or its affiliates. All rights reserved.
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
 * file and include the License file at packager/legal/LICENSE.txt.
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static java.util.logging.Level.*;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionTarget;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.weld.ejb.EjbDescriptorImpl;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.ejb.spi.EjbDescriptor;


/*
 * The means by which Weld Beans are discovered on the classpath. 
 */
public class BeanDeploymentArchiveImpl implements BeanDeploymentArchive {
   
    private Logger logger = Logger.getLogger(BeanDeploymentArchiveImpl.class.getName());

    private static final char SEPARATOR_CHAR = '/';
    private static final String WEB_INF = "WEB-INF";
    private static final String WEB_INF_CLASSES = WEB_INF + SEPARATOR_CHAR + "classes";
    private static final String WEB_INF_LIB = WEB_INF + SEPARATOR_CHAR + "lib";
    private static final String WEB_INF_BEANS_XML = "WEB-INF" + SEPARATOR_CHAR + "beans.xml";
    private static final String META_INF_BEANS_XML = "META-INF" + SEPARATOR_CHAR + "beans.xml";
    private static final String CLASS_SUFFIX = ".class";
    private static final String JAR_SUFFIX = ".jar";
    private static final String RAR_SUFFIX = ".rar";
    private static final String EXPANDED_RAR_SUFFIX = "_rar";

    private ReadableArchive archive;
    private String id;
    private List<Class<?>> wClasses = null;
    private List<URL> wUrls = null;
    private final Collection<EjbDescriptor<?>> ejbDescImpls;
    private List<BeanDeploymentArchive> beanDeploymentArchives;

    private SimpleServiceRegistry simpleServiceRegistry = null;

    public static final String WAR = "WAR";
    public static final String JAR = "JAR";
    public String bdaType;

    private DeploymentContext context;
    private final Map<AnnotatedType<?>, InjectionTarget<?>> itMap 
                    = new HashMap<AnnotatedType<?>, InjectionTarget<?>>();
    
    //workaround
    private ClassLoader moduleClassLoaderForBDA = null;


    /**
     * Produce a <code>BeanDeploymentArchive</code> form information contained 
     * in the provided <code>ReadableArchive</code>.
     * @param context 
     */
    public BeanDeploymentArchiveImpl(ReadableArchive archive,
        Collection<com.sun.enterprise.deployment.EjbDescriptor> ejbs, DeploymentContext ctx) {
        this.wClasses = new ArrayList<Class<?>>();
        this.wUrls = new ArrayList<URL>();
        this.archive = archive;
        this.id = archive.getName(); 
        this.ejbDescImpls = new HashSet<EjbDescriptor<?>>();
        this.beanDeploymentArchives = new ArrayList<BeanDeploymentArchive>();
        this.context = ctx;

        for(com.sun.enterprise.deployment.EjbDescriptor next : ejbs) {
            EjbDescriptorImpl wbEjbDesc = new EjbDescriptorImpl(next);
            ejbDescImpls.add(wbEjbDesc);
        }
        populate();
        try {
            this.archive.close();
        } catch (Exception e) {
        }
        this.archive = null;
        
        //set to the current TCL
        this.moduleClassLoaderForBDA = Thread.currentThread().getContextClassLoader();

    }

    //These are for empty BDAs that do not model Bean classes in the current 
    //deployment unit -- for example: BDAs for portable Extensions.
    public BeanDeploymentArchiveImpl(String id, List<Class<?>> wClasses, List<URL> wUrls,
        Collection<com.sun.enterprise.deployment.EjbDescriptor> ejbs, DeploymentContext ctx) {
        this.id = id;
        this.wClasses = wClasses;
        this.wUrls = wUrls;
        this.ejbDescImpls = new HashSet<EjbDescriptor<?>>();
        this.beanDeploymentArchives = new ArrayList<BeanDeploymentArchive>();
        this.context = ctx;

        for(com.sun.enterprise.deployment.EjbDescriptor next : ejbs) {
            EjbDescriptorImpl wbEjbDesc = new EjbDescriptorImpl(next);
            ejbDescImpls.add(wbEjbDesc);
        }
        
        //set to the current TCL
        this.moduleClassLoaderForBDA = Thread.currentThread().getContextClassLoader();
    }


    public Collection<BeanDeploymentArchive> getBeanDeploymentArchives() {
        return beanDeploymentArchives;
    }

    public Collection<String> getBeanClasses() {
        List<String> s  = new ArrayList<String>();
        for (Iterator<Class<?>> iterator = wClasses.iterator(); iterator.hasNext();) {
            String classname = iterator.next().getName();
            s.add(classname);
        }
        //This method is called during BeanDeployment.deployBeans, so this would
        //be the right time to place the module classloader for the BDA as the TCL
        logger.log(FINER, "set TCL for " + this.id + " to " + this.moduleClassLoaderForBDA);
        Thread.currentThread().setContextClassLoader(this.moduleClassLoaderForBDA);
        //The TCL is unset at the end of deployment of CDI beans in WeldDeployer.event 
        return s;
    }
    
    public Collection<Class<?>> getBeanClassObjects(){
        return wClasses;
    }

    public BeansXml getBeansXml() {
        WeldBootstrap wb =  context.getTransientAppMetaData(WeldDeployer.WELD_BOOTSTRAP, WeldBootstrap.class);
        return wb.parse(wUrls);
    }

    /**
    * Gets a descriptor for each EJB
    *
    * @return the EJB descriptors
    */
    public Collection<EjbDescriptor<?>> getEjbs() {

       return ejbDescImpls;
    }

    public EjbDescriptor getEjbDescriptor(String ejbName) {
        EjbDescriptor match = null;

        for(EjbDescriptor next : ejbDescImpls) {
            if( next.getEjbName().equals(ejbName) ) {
                match = next;
                break;
            }
        }

        return match;
    }

    public ServiceRegistry getServices() {
        if (simpleServiceRegistry == null) {
            simpleServiceRegistry = new SimpleServiceRegistry();
        }
        return simpleServiceRegistry;
    }

    public String getId() {
        return id;
    }

    //A graphical representation of the BDA hierarchy
    public String toString() {
        String val = "|ID: " + getId() + ", bdaType= " + bdaType 
                        +  ", Bean Classes #: " + getBeanClasses().size() + "\n";
        Collection<BeanDeploymentArchive> bdas = getBeanDeploymentArchives();
        Iterator<BeanDeploymentArchive> iter = bdas.iterator();
        while (iter.hasNext()) {
            BeanDeploymentArchive bda = (BeanDeploymentArchive) iter.next();
            String embedBDAType = ""; 
            if (bda instanceof BeanDeploymentArchiveImpl) {
                embedBDAType = ((BeanDeploymentArchiveImpl)bda).getBDAType();
            }
            val += "|---->ID: " + bda.getId() + ", bdaType= " + embedBDAType 
                +  ", Bean Classes #: " + bda.getBeanClasses().size() + "\n";
        }
        return val;
    }

    public String getBDAType() {
        return bdaType;
    }

    private void populate() {
        try {
            if (archive.exists(WEB_INF_BEANS_XML)) {
                logger.log(FINE, "-processing " + archive.getURI() 
                                        + " as it has WEB-INF/beans.xml");
                bdaType = WAR;
                Enumeration<String> entries = archive.entries();
                while (entries.hasMoreElements()) {
                    String entry = entries.nextElement();
                    if (entry.endsWith(CLASS_SUFFIX)) {
                        entry = entry.substring(WEB_INF_CLASSES.length()+1);
                        String className = filenameToClassname(entry);
                        wClasses.add(getClassLoader().loadClass(className));
                    } else if (entry.endsWith("beans.xml")) {
                        URI uri = archive.getURI();
                        File file = new File(uri.getPath() + entry);
                        URL beansXmlUrl = file.toURI().toURL();
                        wUrls.add(beansXmlUrl);
                    }
                }
                archive.close();
            }

            // If this archive has WEB-INF/lib entry..
            // Examine all jars;  If the examined jar has a META_INF/beans.xml:
            //  collect all classes in the jar archive
            //  beans.xml in the jar archive

            if (archive.exists(WEB_INF_LIB)) {
                logger.log(FINE, "-processing WEB-INF/lib in " 
                        + archive.getURI());
                bdaType = WAR;
                Enumeration<String> entries = archive.entries(WEB_INF_LIB);
                while (entries.hasMoreElements()) {
                    String entry = (String)entries.nextElement();
                    if (entry.endsWith(JAR_SUFFIX) &&
                        entry.indexOf(SEPARATOR_CHAR, WEB_INF_LIB.length() + 1 ) == -1 ) {
                        ReadableArchive jarArchive = archive.getSubArchive(entry);
                        if (jarArchive.exists(META_INF_BEANS_XML)) {
                            logger.log(FINE, "-WEB-INF/lib: considering " + entry 
                                    + " as a bean archive as it has beans.xml");
                            collectJarInfo(jarArchive);
                        } else {
                            logger.log(FINE, "-WEB-INF/lib: skipping " + archive.getName() 
                                                + " as it doesn't have beans.xml");
                        }
                    }
               }
            }

            //Handle RARs. RARs are packaged differently from EJB-JARs or WARs.
            //see 20.2 of Connectors 1.6 specification
            //The resource adapter classes are in a jar file within the
            //RAR archive
            if (archive.getName().endsWith(RAR_SUFFIX) || archive.getName().endsWith(EXPANDED_RAR_SUFFIX)) {
                collectRarInfo(archive);
            }
            
            if (archive.exists(META_INF_BEANS_XML)) {
                logger.log(FINE, "-JAR processing: " + archive.getURI() 
                        + " as a jar since it has META-INF/beans.xml");
                bdaType = JAR;
                collectJarInfo(archive);
            } 
            
        } catch(IOException e) {
            logger.log(SEVERE, e.getLocalizedMessage(), e);
        } catch(ClassNotFoundException cne) {
            logger.log(SEVERE, cne.getLocalizedMessage(), cne);
        }
    }   

    private void collectJarInfo(ReadableArchive archive) 
                        throws IOException, ClassNotFoundException {
        logger.log(FINE, "-collecting jar info for " + archive.getURI());
        Enumeration<String> entries = archive.entries();
        while (entries.hasMoreElements()) {
            String entry = entries.nextElement();
            handleEntry(entry);
        }
    }

    private void handleEntry(String entry) throws ClassNotFoundException {
        if (entry.endsWith(CLASS_SUFFIX)) {
            String className = filenameToClassname(entry);
            wClasses.add(getClassLoader().loadClass(className));
        } else if (entry.endsWith("beans.xml")) {
            URL beansXmlUrl = Thread.currentThread().getContextClassLoader().getResource(entry);
            wUrls.add(beansXmlUrl);
        }
    }

    private void collectRarInfo(ReadableArchive archive) throws IOException,
            ClassNotFoundException {
        logger.log(FINE, "-collecting rar info for " + archive.getURI());
        Enumeration<String> entries = archive.entries();
        while (entries.hasMoreElements()) {
            String entry = entries.nextElement();
            if (entry.endsWith(JAR_SUFFIX)){
                ReadableArchive jarArchive = archive.getSubArchive(entry);
                collectJarInfo(jarArchive);
            } else {
                handleEntry(entry);
            }
        }
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
        ClassLoader cl;
        if (this.context.getClassLoader() != null) {
            cl = this.context.getClassLoader();
        } else if (Thread.currentThread().getContextClassLoader() != null) {
            logger.log(FINE, "Using TCL");
            cl = Thread.currentThread().getContextClassLoader();
        } else {
            logger.log(FINE, "TCL is null. Using DeploymentImpl's classloader");
            cl = BeanDeploymentArchiveImpl.class.getClassLoader();
        }
        
        //cache the moduleClassLoader for this BDA
        this.moduleClassLoaderForBDA = cl; 
        return cl;
    }

    public InjectionTarget<?> getInjectionTarget(AnnotatedType<?> annotatedType) {
        return itMap.get(annotatedType);
    }

    void putInjectionTarget(AnnotatedType<?> annotatedType, InjectionTarget<?> it) {
        itMap.put(annotatedType, it);
    }

}
