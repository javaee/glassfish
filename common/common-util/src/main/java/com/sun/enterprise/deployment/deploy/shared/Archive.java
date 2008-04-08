/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.enterprise.deployment.deploy.shared;

import java.util.Enumeration;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.jar.Manifest;


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
    public Enumeration entries(); 

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
     * Returns the InputStream for the given entry name 
     * The file name must be relative to the root of the module. 
     * 
     * @param name the file name relative to the root of the module. 
     * 
     * @return the InputStream for the given entry name or null if not found. 
     */ 
    public InputStream getEntry(String name) throws IOException;
    
    /**
     * Returns an instance of this archive abstraction for an embedded 
     * archive within this archive.
     *
     * @param name is the entry name relative to the root for the archive
     * 
     * @return the Archive instance for this abstraction
     */
    public Archive getSubArchive(String name) throws IOException;
    
    /**
     * Returns the manifest information for this archive
     * @return the manifest info
     */
    public Manifest getManifest() throws IOException;
    
    /**
     * (Optional) 
     * Returns the path for this archive. If the archive is implemented
     * as a jar file for instance, it will return the full path to the 
     * jar file as jar url like
     *   jar://foo/bar/archive.jar
     * <p>
     * If the archive is implemented as a directory structure 
     * with each entry a separate file, it will return the root directory 
     * as a file url like
     *   file://foo/bar/archive
     * <p>
     * Some implementation of the archive which are not based on the file
     * system will not be able to return a value.
     *
     * @return the url path for this archive. 
     */
    public URI getURI();    
    
    /**
     * Returns the size of the archive.
     * @return long indicating the size of the archive
     */
    public long getArchiveSize() throws NullPointerException, SecurityException;
}
