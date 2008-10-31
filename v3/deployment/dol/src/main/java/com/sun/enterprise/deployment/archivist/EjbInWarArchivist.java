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

import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.annotation.impl.EjbInWarScanner;
import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.DescriptorConstants;
import com.sun.enterprise.deployment.io.EjbDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.runtime.EjbRuntimeDDFile;
import org.glassfish.apf.Scanner;
import org.glassfish.api.deployment.archive.Archive;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PerLookup;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Mahesh Kannan
 */
@Service
@Scoped(PerLookup.class)
public class EjbInWarArchivist
        extends BaseEjbArchivist {

    @Inject
    Habitat habitat;

    
    /**
     * @return the DeploymentDescriptorFile responsible for handling
     *         standard deployment descriptor
     */
    @Override                                                  
    public DeploymentDescriptorFile getStandardDDFile() {
        return new EjbDeploymentDescriptorFile() {
            public String getDeploymentDescriptorPath() {
                return "WEB-INF/ejb-jar.xml";  //TODO Add this to DescriptorConstants.class
            }
        };
    }

    @Override
    public DeploymentDescriptorFile getConfigurationDDFile() {
        return new EjbRuntimeDDFile() {
            public String getDeploymentDescriptorPath() {
                return "WEB-INF/" + "sun-" + "ejb-jar.xml"; //TODO Add this to DescriptorConstants.class
            }
        };
    }
    @Override
    protected String getArchiveExtension() {
        return WEB_EXTENSION;
    }

    /**
     * Returns the scanner for this archivist, usually it is the scanner regitered
     * with the same module type as this archivist, but subclasses can return a
     * different version
     *
     */
    public Scanner getScanner() {
        return habitat.getComponent(EjbInWarScanner.class);
    }

    @Override
    public void readPersistenceDeploymentDescriptors(
            ReadableArchive archive, EjbBundleDescriptor descriptor) throws IOException, SAXParseException {

        //
        // TODO: Factor out this code that is a duplicate of the same method in WebArchivist
        //
        if(logger.isLoggable(Level.FINE)) {
            logger.logp(Level.FINE, "EjbArchivist",
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
                                logger.logp(Level.FINE, "EjbArchivist",
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
                            logger.logp(Level.FINE, "EjbArchivist",
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

