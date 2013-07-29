/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.classmodel.reflect.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Archive adapter based on a single InputStream instance.
 * 
 * @author Jerome Dochez
 */
public class InputStreamArchiveAdapter extends AbstractAdapter {
    
    final private InputStream is;
    final private URI uri;
//    final private JarArchive parentArchive;

    public InputStreamArchiveAdapter(JarArchive parent, URI uri, InputStream is) {
        this.uri = uri;
        this.is = is;
//        this.parentArchive = parent;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public Manifest getManifest() throws IOException {
        throw new IOException("Not Implemented");
    }

    @Override
    public void onSelectedEntries(Selector selector, EntryTask task, Logger logger) throws IOException {
        byte[] bytes = new byte[52000];
        JarEntry ja;
        JarInputStream jis = new JarInputStream(new BufferedInputStream(is));
        
        try {
            while ((ja=jis.getNextJarEntry())!=null) {
                try {
                    Entry je = new Entry(ja.getName(), ja.getSize(), ja.isDirectory());
                    if (!selector.isSelected(je))
                        continue;
    
                    try {
                        if (ja.getSize()>bytes.length) {
                            bytes = new byte[(int) ja.getSize()];
                        }
                        if (ja.getSize()!=0) {
                            // beware, ja.getSize() can be equal to -1 if the size cannot be determined.
    
                            int read = 0;
                            int allRead=0;
                            do {
                                read = jis.read(bytes, allRead, bytes.length-allRead);
                                allRead+=read;
                                if (allRead==bytes.length) {
                                    // oh crap !
                                    bytes = Arrays.copyOf(bytes, bytes.length*2);
                                }
    
                            } while (read!=-1);
    
                            if (ja.getSize()!=-1 && ja.getSize()!=(allRead+1)) {
                                logger.severe("Incorrect file length while processing " + ja.getName() + " of size " + ja.getSize() + " got " + allRead);
                            }
    
                            // if the size was not known, let's reset it now.
                            if (je.size==-1) {
                                je = new Entry(ja.getName(), allRead+1);
                            }
                        }
                        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                        try {
                            task.on(je, bais);
                        } finally {
                            bais.close();
                        }
    
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Exception while processing " + ja.getName()
                                + " of size " + ja.getSize(), e);
                    }
                } finally {
                    // this is here to catch the spurious "java.io.EOFException:Unexpected end of ZLIB input stream"
                    try {
                      jis.closeEntry();
                    } catch (Exception e) {
                      logger.log(Level.FINE, "swallowing error", e);
                    }
                }
            }
        } finally {
          try {
            jis.close();
          } catch (Exception e) {
            logger.log(Level.FINE, "swallowing error", e);
          }
        }
    }

    @Override
    public void close() throws IOException {
        is.close();
    }
}
