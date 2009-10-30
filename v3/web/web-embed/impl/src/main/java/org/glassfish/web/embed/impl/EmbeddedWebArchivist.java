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

package org.glassfish.web.embed.impl;

import org.jvnet.hk2.annotations.Inject;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.embedded.web.WebBuilder;
import org.glassfish.api.embedded.*;
import org.glassfish.apf.ProcessingResult;
import org.glassfish.apf.AnnotationProcessorException;

import java.net.URL;
import java.io.IOException;
import java.io.File;
import java.util.Set;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.logging.Level;

import com.sun.enterprise.deployment.archivist.WebArchivist;
import com.sun.enterprise.deployment.annotation.impl.ModuleScanner;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;

/**
 * @author Jerome Dochez
 */
public class EmbeddedWebArchivist extends WebArchivist {

    @Inject
    WebBuilder builder;

    private final ModuleScanner scanner = new ModuleScanner() {

            final Set<Class> elements = new HashSet<Class>();

            @Override
            public void process(ReadableArchive archiveFile, Object bundleDesc, ClassLoader classLoader) throws IOException {
                // in embedded mode, we don't scan archive, we just process all classes.
                Enumeration<String> entries = archiveFile.entries();
                while (entries.hasMoreElements()) {
                    String entry = entries.nextElement();
                    if (entry.endsWith(".class")) {
                        try {
                            elements.add(classLoader.loadClass(toClassName(entry)));
                        } catch (ClassNotFoundException e) {
                            logger.log(Level.FINER, "Cannot load class " + entry, e);
                        }
                    }
                }

            }

            private String toClassName(String entryName) {
                String name = entryName.substring("WEB-INF/classes/".length(), entryName.length()-".class".length());
                return name.replaceAll("/",".");

            }

            public void process(File archiveFile, Object bundleDesc, ClassLoader classLoader) throws IOException {

            }

            @Override
            public Set getElements() {
                return elements;
            }
        };

    @Override
    protected URL getDefaultWebXML() throws IOException {
        if (builder.getDefaultWebXml()!=null) {
            return builder.getDefaultWebXml();
        }
        return super.getDefaultWebXML();
    }

    @Override
    protected ProcessingResult processAnnotations(RootDeploymentDescriptor bundleDesc,
                                               ModuleScanner scanner,
                                               ReadableArchive archive)
            throws AnnotationProcessorException, IOException {

        // in embedded mode, I ignore all scanners and parse all possible classes.
        if (archive instanceof ScatteredArchive) {
            return super.processAnnotations(bundleDesc, this.scanner, archive);
        } else {
            return super.processAnnotations(bundleDesc, scanner, archive);
        }
    }
}
