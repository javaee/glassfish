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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class EJBBundle extends OSGiJavaEEArchive {

    public EJBBundle(Bundle[] fragments, Bundle host) {
        super(fragments, host);
    }

    @Override
    protected void init() {
        // Very important implementation note:
        //
        // Now we need to merge individual namespaces represented in each of the the subarchives
        // corresponding to entries in the effetcive bundle classpath.
        // During merging of namespaces, collision can be expected. e.g.:
        // a bundle with BCP: bin, . and having a content tree like this:
        // p/A.class, bin/p/A.class.
        // Actually, there is only name here which is p/A.class, but it appears in both the namespaces.
        // Our collision avoidance strategy is based on how Bundle.getResource() behaves. Since bin
        // appears ahead of . in BCP, bundle.getResource(p/A.class) will return bin/p/A.class.
        // So,our merged namespace must also contain bin/p/A.class.
        // The simplest way to achieve this is to collect entries from the subarchives in the reverse
        // order of bundle classpath and put them into a hasmap with entry name being the key.
        // See https://glassfish.dev.java.net/issues/show_bug.cgi?id=14268
        final EffectiveBCP bcp = getEffectiveBCP();
        List<BCPEntry> bcpEntries = new ArrayList(bcp.getBCPEntries());
        Collections.reverse(bcpEntries);
        for (BCPEntry bcpEntry : bcpEntries) {
            bcpEntry.accept(new BCPEntry.BCPEntryVisitor() {

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
}
