/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */



package com.sun.persistence.spi.deployment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

/**
 * This interface serves two purposes a) SJSAS has many different physical
 * representations of an archive, viz: memory, file, memory mapped file etc.
 * This interface hides these facts from persistence module.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @see #getEntries() b) The classloader for an archive largely depends on
 *      whether we are dealing with an enterprise archive or standard archive.
 *      We in persistence module do not want to know about the rules for class
 *      loading for an enterprise environment. Instead we expect SJSAS to tell
 *      us what classloader to use. Hence this interface.
 * @see #getClassLoader()
 */
public interface Archive {
    /**
     * Returns an enumeration of the module file entries. Each String represents
     * a file name relative to the root of the module.
     */
    Enumeration<String> getEntries() throws IOException;

    /**
     * Returns the InputStream for the given entry name The file name must be
     * relative to the root of the module.
     *
     * @param entryPath the file name relative to the root of the module.
     * @return the InputStream for the given entry name or null if not found.
     */
    InputStream getEntry(String entryPath) throws IOException;

    ClassLoader getClassLoader();
}
