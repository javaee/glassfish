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
import java.net.URISyntaxException;
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
    final URI uri;

    public ReadableArchiveScannerAdapter(Parser parser, ReadableArchive archive) {
        this.archive = archive;
        this.parser = parser;
        this.uri = archive.getURI();
    }

    private ReadableArchiveScannerAdapter(Parser parser, ReadableArchive archive, URI uri) {
        this.archive = archive;
        this.parser = parser;
        this.uri = uri==null?archive.getURI():uri;
    }

    @Override
    public URI getURI() {
       return uri;
    }

    @Override
    public Manifest getManifest() throws IOException {
        return archive.getManifest();
    }

    @Override
    public void onSelectedEntries(ArchiveAdapter.Selector selector, EntryTask entryTask, final Logger logger ) throws IOException {

        Enumeration<String> entries = archive.entries();
        while (entries.hasMoreElements()) {
            final String name = entries.nextElement();
            Entry entry = new Entry(name, archive.getEntrySize(name), false);
            if (selector.isSelected(entry)) {
                InputStream is = null;
                try {
                    try {
                        is = archive.getEntry(name);
                         entryTask.on(entry, is);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Exception while processing " + entry.name
                                + " inside " + archive.getName() + " of size " + entry.size, e);
                    }
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

                    final ReadableArchive subArchive = archive.getSubArchive(name);

                    // this is non sense, the subArchive should do this job for me but it does not.
                    // see the long comment from Tim on entries().
                    URI subURI=null;
                    try {
                        subURI = new URI(
                        "jar",
                        "file:" + uri.getSchemeSpecificPart() +
                            "!/" +
                            name,
                        null);
                    } catch(URISyntaxException e) {
                        logger.log(Level.FINE, e.getMessage(),e);
                    }
                    if (subArchive!=null) {

                        ReadableArchiveScannerAdapter adapter = new ReadableArchiveScannerAdapter(parser, subArchive, subURI);
                        parser.parse(adapter, new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    subArchive.close();
                                } catch (IOException e) {
                                    logger.log(Level.SEVERE, "Cannot close sub archive" + name,e);
                                }
                            }
                        });
                    }
                }
            }  
        }
    }

    @Override
    public void close() throws IOException {
    }
}
