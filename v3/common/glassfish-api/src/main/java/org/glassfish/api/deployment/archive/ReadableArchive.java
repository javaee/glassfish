/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.api.deployment.archive;

import org.jvnet.hk2.annotations.Contract;

import java.io.InputStream;
import java.io.IOException;
import java.net.URI;

/**
 * Interface for implementing read access to an underlying archive on a unspecified medium
 *
 * @author Jerome Dochez
 */
@Contract
public interface ReadableArchive extends Archive {

    /**
     * Returns the InputStream for the given entry name
     * The file name must be relative to the root of the module.
     *
     * @param name the file name relative to the root of the module.
     * @return the InputStream for the given entry name or null if not found.
     */
    public InputStream getEntry(String name) throws IOException;

    /**
     * Returns the existence of the given entry name
     * The file name must be relative to the root of the module.
     *
     * @param name the file name relative to the root of the module.
     * @return the existence the given entry name.
     */
    public boolean exists(String name) throws IOException;

    /**
     * Returns the entry size for a given entry name or 0 if not known
     *
     * @param name the entry name
     * @return the entry size
     */
    public long getEntrySize(String name);

    /**
     * Open an abstract archive
     *
     * @param uri path to the archive
     */
    public void open(URI uri) throws IOException;

    /**
     * Returns an instance of this archive abstraction for an embedded
     * archive within this archive.
     *
     * @param name is the entry name relative to the root for the archive
     * @return
     *      the Archive instance for this abstraction,
     *      or null if no such entry exists.
     */
    public ReadableArchive getSubArchive(String name) throws IOException;

    /**
     * @return true if this archive exists
     */
    public boolean exists();

    /**
     * deletes the archive
     */
    public boolean delete();

    /**
     * rename the archive
     *
     * @param name the archive name
     */
    public boolean renameTo(String name);

    /**
     * set the parent archive for this archive
     *
     * @param parentArchive the parent archive
     */
    public void setParentArchive(ReadableArchive parentArchive);

    /**
     * get the parent archive of this archive
     *
     * @return the parent archive
     */
    public ReadableArchive getParentArchive();
}
