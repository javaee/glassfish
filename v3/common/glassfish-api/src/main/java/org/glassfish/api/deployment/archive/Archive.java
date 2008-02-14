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

import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.Manifest;
import java.net.URI;


/**
 * This interface is an abstraction for accessing a module archive. 
 *
 * @author Jerome Dochez
 */
public interface Archive {
    
    /**
     * closes this archive and releases all resources
     */
    public void close() throws IOException;
    
    /** 
     * Returns an enumeration of the module file entries.  All elements 
     * in the enumeration are of type String.  Each String represents a 
     * file name relative to the root of the module. 
     * 
     * @return an enumeration of the archive file entries. 
     */ 
    public Enumeration<String> entries(); 

    /** 
     * Returns an enumeration of the module file entries with the
     * specified prefix.  All elements in the enumeration are of 
     * type String.  Each String represents a file name relative 
     * to the root of the module. 
     * 
     * @param prefix the prefix of entries to be included
     * @return an enumeration of the archive file entries. 
     */ 
    public Enumeration entries(String prefix);

    
    /**
     * Returns the manifest information for this archive
     * @return the manifest info
     */
    public Manifest getManifest() throws IOException;
    
    /**
     * Returns the path used to create or open the underlying archive
     *
     * @return the path for this archive. 
     */
    public URI getURI();
    
    /**
     * Returns the size of the archive.
     * @return long indicating the size of the archive
     */
    public long getArchiveSize() throws NullPointerException, SecurityException;
    
    /**
     * Returns the name of the archive.
     * <p>
     * Implementations should not return null.
     * @return the name of the archive
     */
    public String getName();
}
