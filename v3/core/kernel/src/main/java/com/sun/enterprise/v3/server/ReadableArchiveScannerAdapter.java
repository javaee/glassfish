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

package com.sun.enterprise.v3.server;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.hk2.classmodel.reflect.ArchiveAdapter;
import org.glassfish.hk2.classmodel.reflect.Parser;
import org.glassfish.hk2.classmodel.reflect.util.AbstractAdapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.Manifest;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ArchiveAdapter for DOL readable archive instances
 * 
 * @author Jerome Dochez
 */
public class ReadableArchiveScannerAdapter extends AbstractAdapter {
    final ReadableArchive archive;
    final Parser parser;

    public ReadableArchiveScannerAdapter(Parser parser, ReadableArchive archive) {
        this.archive = archive;
        this.parser = parser;
    }

    @Override
    public URI getURI() {
       URI archiveURI =  archive.getURI();
       if (archiveURI.getScheme().equals("jar")) {
           try {
               // let's use the file scheme for jar files as the J2SE
               // File.toURI always returns the file scheme.
               archiveURI = new URI("file", null /* authority */, archiveURI.getPath(), null /* query */, null /* fragment */);
           } catch (Exception e) {
               Logger.getAnonymousLogger().log(Level.WARNING, "failed to convert URI to use file scheme: ", e);
           }
       }
       return archiveURI;
    }

    @Override
    public Manifest getManifest() throws IOException {
        return archive.getManifest();
    }

    @Override
    public void onSelectedEntries(ArchiveAdapter.Selector selector, EntryTask entryTask, Logger logger ) throws IOException {

        Enumeration<String> entries = archive.entries();
        byte[] bytes = new byte[52000];
        while (entries.hasMoreElements()) {
            String name = entries.nextElement();
            Entry entry = new Entry(name, archive.getEntrySize(name), false);
            if (selector.isSelected(entry)) {
                InputStream is = null;
                try {
                    try {
                        is = archive.getEntry(name);
                        if (bytes.length<entry.size) {
                            bytes = new byte[(int) entry.size];
                        }
                        int read = is.read(bytes, 0, (int) entry.size);
                        if (read!=entry.size) {
                            logger.severe("Incorrect file length while reading " + entry.name +
                                    " inside " + archive.getName() +
                                    " of size " + entry.size + " reported is " + read);

                        }
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Exception while processing " + entry.name
                                + " inside " + archive.getName() + " of size " + entry.size, e);
                    }
                    entryTask.on(entry, bytes);
                } finally {
                    if (is!=null)
                        is.close();
                }
            }
            // check for non exploded jars.
            if (name.endsWith(".jar")) {

                // we need to check that there is no exploded directory by this name.
                String explodedName = name.replaceAll("[/ ]", "__").replace(".jar", "_jar");
                if (!archive.exists(explodedName)) {

                    ReadableArchive subArchive = null;
                    try {
                        subArchive = archive.getSubArchive(name);
                        if (subArchive!=null) {
                            ReadableArchiveScannerAdapter adapter = new ReadableArchiveScannerAdapter(parser, subArchive);
                            parser.parse(adapter, null);
                        }
                    } finally {
                        if (subArchive!=null) {
                            subArchive.close();
                        }
                    }
                }
            }  
        }
    }

    @Override
    public void close() throws IOException {
    }
}
