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

package com.sun.enterprise.deployment.archivist;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.DescriptorConstants;
import com.sun.enterprise.deployment.io.WebDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.runtime.WebRuntimeDDFile;
import com.sun.enterprise.deployment.node.web.WebBundleNode;
import com.sun.enterprise.deployment.util.ApplicationValidator;
import com.sun.enterprise.deployment.util.ModuleContentValidator;
import com.sun.enterprise.deployment.util.WebBundleVisitor;
import org.glassfish.api.deployment.archive.Archive;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.xml.sax.SAXParseException;

import javax.enterprise.deploy.shared.ModuleType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;


/**
 * This module is responsible for reading and write web applications
 * archive files (war).
 *
 * @author  Jerome Dochez
 * @version 
 */
@Service
@Scoped(PerLookup.class)
public class WebArchivist extends Archivist<WebBundleDescriptor> 
    implements PrivateArchivist {
    
    /** 
     * The DeploymentDescriptorFile handlers we are delegating for XML i/o
     */
    DeploymentDescriptorFile standardDD = new WebDeploymentDescriptorFile();

    private WebBundleDescriptor defaultBundleDescriptor = null;

    /**
     * @return the  module type handled by this archivist
     * as defined in the application DTD
     *
     */
    @Override
    public ModuleType getModuleType() {
        return ModuleType.WAR;
    }        
    
    /**
     * Archivist read XML deployment descriptors and keep the
     * parsed result in the DOL descriptor instances. Sets the descriptor
     * for a particular Archivst type
     */
    public void setDescriptor(Application descriptor) {
        java.util.Set webBundles = descriptor.getWebBundleDescriptors();
        if (webBundles.size()>0) {
            this.descriptor = (WebBundleDescriptor) webBundles.iterator().next();
            if (this.descriptor.getModuleDescriptor().isStandalone())
                return;
            else
                this.descriptor=null;
        }
    }  
    
    /** 
     * @return the location of the web services related deployment 
     * descriptor file inside this archive or null if this archive
     * does not support webservices implementation.
     */
    @Override
    public String getWebServicesDeploymentDescriptorPath() {
        return DescriptorConstants.WEB_WEBSERVICES_JAR_ENTRY;
    }    
    
    /**
     * @return the DeploymentDescriptorFile responsible for handling
     * standard deployment descriptor
     */
    @Override
    public DeploymentDescriptorFile getStandardDDFile() {
        return standardDD;
    }
    
    /**
     * @return if exists the DeploymentDescriptorFile responsible for
     * handling the configuration deployment descriptors
     */
    @Override
    public DeploymentDescriptorFile getConfigurationDDFile() {
        return new WebRuntimeDDFile();
    }      
    
    /**
     * @return a default BundleDescriptor for this archivist
     */
    @Override
    public WebBundleDescriptor getDefaultBundleDescriptor() {
        return defaultBundleDescriptor;
    }

    /**
     * set a default BundleDescriptor for this archivist
     */
    @Override
    public void setDefaultBundleDescriptor(WebBundleDescriptor defaultWbd) {
        defaultBundleDescriptor = defaultWbd;
        WebBundleNode.setDefaultBundleDescriptor(defaultBundleDescriptor);
    }

    /**
     * perform any post deployment descriptor reading action
     *
     * @param descriptor the deployment descriptor for the module
     * @param archive the module archive
     */
    @Override
    protected void postOpen(WebBundleDescriptor descriptor, ReadableArchive archive)
        throws IOException
    {
        super.postOpen(descriptor, archive);
        WebBundleDescriptor webBundle = (WebBundleDescriptor) descriptor;
        ModuleContentValidator mdv = new ModuleContentValidator(archive);
        webBundle.visit(mdv);
    }

    /**
     * validates the DOL Objects associated with this archivist, usually
     * it requires that a class loader being set on this archivist or passed
     * as a parameter
     */
    @Override
    public void validate(ClassLoader aClassLoader) {
        ClassLoader cl = aClassLoader;
        if (cl==null) {
            cl = classLoader;
        }
        if (cl==null) {
            return;
        }
        descriptor.setClassLoader(cl);
        descriptor.visit((WebBundleVisitor) new ApplicationValidator());        
    }            

    /**
     * In the case of web archive, the super handles() method should be able 
     * to make a unique identification.  If not, then the archive is definitely 
     * not a war.
     */
    @Override
    protected boolean postHandles(ReadableArchive abstractArchive)
            throws IOException {
        return DeploymentUtils.isWebArchive(abstractArchive);
    }

    @Override
    protected String getArchiveExtension() {
        return WEB_EXTENSION;
    }
    
    /**
     * @return a list of libraries included in the archivist
     */
    public Vector getLibraries(Archive archive) {
        
        Enumeration<String> entries = archive.entries();
        if (entries==null)
            return null;
        
        Vector libs = new Vector();        
        while (entries.hasMoreElements()) {
            
            String entryName = entries.nextElement();
            if (!entryName.startsWith("WEB-INF/lib")) {
                continue; // not in WEB-INF...
            }
            if (entryName.endsWith(".jar")) {
                libs.add(entryName);
            }            
        }
        return libs;
    }

    @Override
    public void readPersistenceDeploymentDescriptors(
            ReadableArchive archive, WebBundleDescriptor descriptor) throws IOException, SAXParseException {
        if(logger.isLoggable(Level.FINE)) {
            logger.logp(Level.FINE, "WebArchivist",
                    "readPersistenceDeploymentDescriptors", "archive = {0}",
                    archive.getURI());
        }
        Map<String, ReadableArchive> subArchives = new HashMap<String, ReadableArchive>();
        Enumeration entries = archive.entries();
        final String CLASSES_DIR = "WEB-INF/classes/";
        final String LIB_DIR = "WEB-INF/lib/";
        final String JAR_EXT = ".jar";
        try {
            ReadableArchive libArchive = archive.getSubArchive(LIB_DIR);
            if (libArchive!=null) {
                Enumeration<String> libEntries = libArchive.entries();
                while(libEntries.hasMoreElements()) {
                    String path = libEntries.nextElement();
                    if (path.endsWith(JAR_EXT)) {
                        if(path.indexOf('/') == -1) { // to avoid WEB-INF/lib/foo/bar.jar
                            // this jarFile is directly inside WEB-INF/lib directory
                            try {
                                subArchives.put(LIB_DIR+"/"+path, libArchive.getSubArchive(path));
                            } catch (IOException ioe) {
                                // if there is any problem in opening the 
                                // library jar, log the exception and proceed 
                                // to the next jar
                                logger.log(Level.SEVERE, ioe.getMessage(), ioe);
                            }
                        } else {
                            if(logger.isLoggable(Level.FINE)) {
                                logger.logp(Level.FINE, "WebArchivist",
                                        "readPersistenceDeploymentDescriptors",
                                        "skipping {0} as it exists inside a directory in {1}.",
                                        new Object[]{path, LIB_DIR});
                            }
                            continue;
                        }

                    }

                }
            }
            
            final String pathOfPersistenceXMLInsideClassesDir =
                    CLASSES_DIR+DescriptorConstants.PERSISTENCE_DD_ENTRY;
            InputStream is = archive.getEntry(pathOfPersistenceXMLInsideClassesDir);
            if (is!=null) {
                is.close();
                subArchives.put(CLASSES_DIR, archive.getSubArchive(CLASSES_DIR));
            }

            /** ToDo : mitesh, debug replacement code above and clean this old version
            while(entries.hasMoreElements()){
                final String nextEntry = String.class.cast(entries.nextElement());
                if(pathOfPersistenceXMLInsideClassesDir.equals(nextEntry)) {
                    subArchives.put(CLASSES_DIR, archive.getSubArchive(CLASSES_DIR));
                } else if (nextEntry.startsWith(LIB_DIR) && nextEntry.endsWith(JAR_EXT)) {
                    String jarFile = nextEntry.substring(LIB_DIR.length(), nextEntry.length()-JAR_EXT.length());
                    if(jarFile.indexOf('/') == -1) { // to avoid WEB-INF/lib/foo/bar.jar
                        // this jarFile is directly inside WEB-INF/lib directory
                        subArchives.put(nextEntry, archive.getSubArchive(nextEntry));
                    } else {
                        if(logger.isLoggable(Level.FINE)) {
                            logger.logp(Level.FINE, "WebArchivist",
                                    "readPersistenceDeploymentDescriptors",
                                    "skipping {0} as it exists inside a directory in {1}.",
                                    new Object[]{nextEntry, LIB_DIR});
                        }
                        continue;
                    }
                }
            }
             */
            for(Map.Entry<String, ReadableArchive> pathToArchiveEntry : subArchives.entrySet()) {
                readPersistenceDeploymentDescriptor(pathToArchiveEntry.getValue(), pathToArchiveEntry.getKey(), descriptor);
            }
        } finally {
            for(Archive subArchive : subArchives.values()) {
                subArchive.close();
            }
        }
    }

}
