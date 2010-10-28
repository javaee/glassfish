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


package org.glassfish.osgiejb;

import org.glassfish.api.deployment.archive.Archive;
import org.glassfish.osgijavaeebase.OSGiBundleArchive;
import org.glassfish.osgijavaeebase.OSGiJavaEEArchive;
import org.glassfish.osgijavaeebase.URIable;
import org.osgi.framework.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class EJBBundle extends OSGiJavaEEArchive {

    public EJBBundle(Bundle[] fragments, Bundle host) {
        super(fragments, host);
    }

    @Override
    protected void init() {
        final EffectiveBCP bcp = getEffectiveBCP();
        bcp.accept(new BCPEntry.BCPEntryVisitor() {
            private int i = 0;

            public void visitDir(final DirBCPEntry bcpEntry) {
                visitBCPEntry(bcpEntry);
            }

            public void visitJar(final JarBCPEntry bcpEntry) {
                // do special processing for Bundle-ClassPath DOT
                if (bcpEntry.getName().equals(DOT)) {
                    OSGiBundleArchive subArchive = getArchive(bcpEntry.getBundle());
                    addEntriesForSubArchive(subArchive);
                } else {
                    visitBCPEntry(bcpEntry);
                }
            }

            private void visitBCPEntry(BCPEntry bcpEntry) {
                try {
                    final Archive subArchive = getArchive(bcpEntry.getBundle()).getSubArchive(bcpEntry.getName());
                    addEntriesForSubArchive(subArchive);
                } catch (IOException e) {
                    throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
                }
            }

            private void addEntriesForSubArchive(Archive subArchive) {
                final URIable uriableArchive = (URIable) subArchive;
                for (final String subEntry : Collections.list(subArchive.entries())) {
                    ArchiveEntry archiveEntry = new ArchiveEntry() {
                        public String getName() {
                            return subEntry;
                        }

                        public URI getURI() throws URISyntaxException {
                            return uriableArchive.getEntryURI(subEntry);
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
            }

        });
    }
}
