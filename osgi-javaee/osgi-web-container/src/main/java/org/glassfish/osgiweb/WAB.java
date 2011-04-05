/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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


package org.glassfish.osgiweb;

import org.glassfish.api.deployment.archive.Archive;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.osgijavaeebase.OSGiBundleArchive;
import org.glassfish.osgijavaeebase.OSGiJavaEEArchive;
import org.osgi.framework.Bundle;
import com.sun.enterprise.deploy.shared.AbstractReadableArchive;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Servlet spec, the spec which defines the term Web Application, defines the overall structure of a
 * Web Application as a hierrachical arrangement of files (and directories), but does not mandate them to be
 * available in a hierarchical file system per se. See section #10.4 of Servlet 3.0 spec, which mentions
 * the following:
 * This specification defines a hierarchical structure used for deployment and
 * packaging purposes that can exist in an open file system, in an archive file, or in
 * some other form. It is recommended, but not required, that servlet containers
 * support this structure as a runtime representation.
 * <p/>
 * A WAB provides such a view of web application which is actually composed of a host OSGi bundle and zero or
 * more attached fragment bundles.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class WAB extends OSGiJavaEEArchive {
    // Implementation Notes:
    // We don't create virtual jar from directory type Bundle-ClassPath entry, because rfc #66 says that
    // such entries should be treated like WEB-INF/classes/, which means, they must not be searched for
    // web-fragments.xml.

    /**
     * All Bundle-ClassPath entries of type jars are represented as WEB-INF/lib/{N}.jar,
     * where N is a number starting with 0.
     */
    private final static String LIB_DIR = "WEB-INF/lib/";
    private final static String CLASSES_DIR = "WEB-INF/classes/";

    public WAB(Bundle host, Bundle[] fragments) {
        super(fragments, host);
    }


    @Override
    protected synchronized void init() {
        List<Bundle> bundles = new ArrayList(Arrays.asList(fragments));
        bundles.add(0, host);
        for(Bundle b : bundles) {
            final OSGiBundleArchive archive = getArchive(b);
            for(final String entry : Collections.list(archive.entries())) {
                if(getEntries().containsKey(entry)) continue; // encountering second time - ignore
                ArchiveEntry archiveEntry = new ArchiveEntry() {
                    public String getName() {
                        return entry;
                    }

                    public URI getURI() throws URISyntaxException {
                        return archive.getEntryURI(entry);
                    }

                    public InputStream getInputStream() throws IOException {
                        return archive.getEntry(entry);
                    }
                };
                getEntries().put(entry, archiveEntry);
            }
        }

        final EffectiveBCP bcp = getEffectiveBCP();
        bcp.accept(new BCPEntry.BCPEntryVisitor() {
            private int i = 0;

            public void visitDir(final DirBCPEntry bcpEntry) {
                try {
                    // do special processing if the dir name is not WEB-INF/classes/
                    if (bcpEntry.getName().equals(CLASSES_DIR)) return;
                    final Archive subArchive = getArchive(bcpEntry.getBundle()).getSubArchive(bcpEntry.getName());
                    for (final String subEntry : Collections.list(subArchive.entries())) {
                        ArchiveEntry archiveEntry = new ArchiveEntry() {
                            public String getName() {
                                return CLASSES_DIR + subEntry;
                            }

                            public URI getURI() throws URISyntaxException {
                                return bcpEntry.getBundle().getEntry(bcpEntry.getName() + subEntry).toURI();
                            }

                            public InputStream getInputStream() throws IOException {
                                try {
                                    return getURI().toURL().openStream();
                                } catch (URISyntaxException e) {
                                    throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
                                }
                            }
                        };
                        getEntries().put(archiveEntry.getName(), archiveEntry);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
                }
            }

            public void visitJar(final JarBCPEntry bcpEntry) {
                // do special processing if the jar does not belong to WEB-INF/lib/
                if (bcpEntry.getName().startsWith(LIB_DIR) && bcpEntry.getName().endsWith(JAR_EXT)) {
                    String jarName = bcpEntry.getName().substring(LIB_DIR.length());
                    if(jarName.indexOf("/") == -1) {
                        return; // This jar is already first level jar in WEB-INF/lib
                    }
                }

                // do special processing for Bundle-ClassPath DOT
                if (bcpEntry.getName().equals(DOT)) {
                    final String newJarName = LIB_DIR + "Bundle" + bcpEntry.getBundle().getBundleId() + JAR_EXT;
                    getEntries().put(newJarName, new ArchiveEntry(){
                        public String getName() {
                            return newJarName;
                        }

                        public URI getURI() throws URISyntaxException {
                            return getArchive(bcpEntry.getBundle()).getURI();
                        }

                        public InputStream getInputStream() throws IOException {
                            return getArchive(bcpEntry.getBundle()).getInputStream();
                        }
                    });
                } else {
                    final String newJarName = LIB_DIR + "Bundle" + bcpEntry.getBundle().getBundleId() + "-" +
                            bcpEntry.getName().replace('/', '-') + JAR_EXT;
                    getEntries().put(newJarName, new ArchiveEntry() {
                        public String getName() {
                            return newJarName;
                        }

                        public URI getURI() throws URISyntaxException {
                            return bcpEntry.getBundle().getEntry(bcpEntry.getName()).toURI();
                        }

                        public InputStream getInputStream() throws IOException {
                            try {
                                return getURI().toURL().openStream();
                            } catch (URISyntaxException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            }
        });

    }

}
