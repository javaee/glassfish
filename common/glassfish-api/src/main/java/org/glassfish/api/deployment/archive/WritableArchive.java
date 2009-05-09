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

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * Interface for implementing write access to an underlying archive on a unspecified medium
 *
 * @author Jerome Dochez
 */
@Contract
public interface WritableArchive extends Archive {

    /**
     * creates a new abstract archive with the given path
     *
     * @param uri the path to create the archive
     */
    public void create(URI uri) throws IOException;

    /**
     * Close a previously returned sub archive
     *
     * @param subArchive output stream to close
     * @link Archive.getSubArchive}
     */
    public void closeEntry(WritableArchive subArchive) throws IOException;

    /**
     * Create a new entry in the archive
     *
     * @param name the entry name
     * @returns an @see java.io.OutputStream for a new entry in this
     * current abstract archive.
     */
    public OutputStream putNextEntry(String name) throws java.io.IOException;

    /**
     * closes the current entry
     */
    public void closeEntry() throws IOException;

    /**
     * Returns an instance of this archive abstraction for an embedded
     * archive within this archive.
     *
     * @param name is the entry name relative to the root for the archive
     * @return the Archive instance for this abstraction
     */
    public WritableArchive createSubArchive(String name) throws IOException;
}
