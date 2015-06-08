/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.classmodel.reflect;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.jar.Manifest;
import java.util.logging.Logger;

/**
 * adapter for reading archive style structure
 *
 * @author Jerome Dochez
 */
public interface ArchiveAdapter extends Closeable {

    /**
     * Returns the URI of the archive
     *
     * @return URI of the archive
     */
    public URI getURI();

    /**
     * Returns the manifest instance for the archive.
     *
     * @return the archive's manifest
     * @throws IOException if the manifest cannot be loaded.
     */
    public Manifest getManifest() throws IOException ;

    /**
     * defines the notion of an archive entry task which is a task
     * aimed to be run on particular archive entry.
     */
    public interface EntryTask {
        
        /**
         * callback to do some processing on an archive entry.
         *
         * @param e the archive entry information such as its name, size...
         * @param is the archive entry content.
         * @throws IOException if the input stream reading generates a failure
         */
        public void on(final Entry e, InputStream is) throws IOException;
    }

    public interface Selector {

        /**
         * callback to select an archive for processing
         * @param entry the archive entry information
         * @return true if the archive entry has been selected for processing
         */
        public boolean isSelected(final Entry entry);
    }
    
    /**
     * perform a task on each archive entry
     *
     * @param task the task to perform
     * @param logger for any logging activity
     * @throws IOException can be generated while reading the archive entries
     */
    public void onAllEntries(EntryTask task, Logger logger) throws IOException;

    /**
     * perform a task on selected archive entries
     *
     * @param selector implementation to select the archive archive entries on
     * which the task should be performed.
     * @param task the task to perform
     * @param logger for any logging activity
     * @throws IOException can be generated while reading the archive entries
     */
    public void onSelectedEntries(Selector selector, EntryTask task, Logger logger) throws IOException;


    /**
     * Definition of an archive entry
     */
    public final class Entry {
        final public String name;
        final public long size;

        /**
         * creates a new archive entry
         * @param name the entry name
         * @param size the entry size
         * @param isDirectory true if this entry is a directory
         * @deprecated Use the other constructor, isDirectory is not used
         */
        public Entry(String name, long size, boolean isDirectory) {
            this.name = name;
            this.size = size;
        }
        
        /**
         * creates a new archive entry
         * @param name the entry name
         * @param size the entry size
         */
        public Entry(String name, long size) {
            this(name, size, false);
        }
    }
}
