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
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.webbeans;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;

import org.glassfish.api.deployment.archive.ReadableArchive;

import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;

/*
 * The means by which Web Beans are discovered on the classpath. 
 */
public abstract class WebBeanDiscoveryImpl implements WebBeanDiscovery {
   
    private Logger logger = Logger.getLogger(WebBeanDiscoveryImpl.class.getName());

    private static final String WEB_INF_LIB = "WEB-INF/lib";
    private static final String WEB_INF_BEANS_XML = "WEB-INF/beans.xml";
    private static final String META_INF_BEANS_XML = "META-INF/beans.xml";
    private static final String WEB_INF_CLASSES = "WEB-INF/classes";
    private static final String CLASS_SUFFIX = ".class";
    private static final String JAR_SUFFIX = ".jar";
    private static final String SEPERATOR_CHAR = "/";

    private final Set<Class<?>> wbClasses;
    private final Set<URL> wbUrls;
    private final ReadableArchive archive;
   
    public WebBeanDiscoveryImpl(ReadableArchive archive) {
        this.wbClasses = new HashSet<Class<?>>();
        this.wbUrls = new HashSet<URL>();
        this.archive =  archive;
        scan();
    }
   
    public Iterable<Class<?>> discoverWebBeanClasses() {
        return Collections.unmodifiableSet(wbClasses);
    }
   
    public Iterable<URL> discoverWebBeansXml() {
        return Collections.unmodifiableSet(wbUrls);
    }
   
    public Set<Class<?>> getWbClasses() {
        return wbClasses;
    }
   
    public Set<URL> getWbUrls() {
        return wbUrls;
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
                        entry.indexOf(SEPERATOR_CHAR, WEB_INF_LIB.length() + 1 ) == -1 ) {
                        ReadableArchive jarArchive = archive.getSubArchive(entry);
                        if (jarArchive.exists(META_INF_BEANS_XML)) {
                            Enumeration jarEntries = jarArchive.entries();
                            while (jarEntries.hasMoreElements()) {
                                entry = (String)entries.nextElement();
                                if (entry.endsWith(CLASS_SUFFIX)) {
                                    String className = filenameToClassname(entry);
                                    wbClasses.add(getClassLoader().loadClass(className));
                                } else if (entry.endsWith("beans.xml")) {
                                    URL beansXmlUrl = WebBeanDiscoveryImpl.class.getResource(entry);
                                    wbUrls.add(beansXmlUrl);
                                }
                            }
                        }
                        jarArchive.close();
                    }
               } 
            }
        } catch(IOException e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
        } catch(ClassNotFoundException cne) {
            logger.log(Level.SEVERE, cne.getLocalizedMessage(), cne);
        }

    }

    public static String filenameToClassname(String filename) {
        String className = filename.replace(File.separatorChar, '.');
        className = className.substring(0, className.length()-6);
        return className;
    }

    private ClassLoader getClassLoader() {
        if (Thread.currentThread().getContextClassLoader() != null) {
            return Thread.currentThread().getContextClassLoader();
        } else {
            return WebBeanDiscoveryImpl.class.getClassLoader();
        }
    }
   
}
