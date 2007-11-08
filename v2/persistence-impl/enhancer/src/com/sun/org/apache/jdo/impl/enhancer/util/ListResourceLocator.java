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

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.impl.enhancer.util;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.FileInputStream;

import java.net.URL;


/**
 * Searches resources among a set of files.
 */
public class ListResourceLocator
    extends ResourceLocatorBase
    implements ResourceLocator
{
    /**
     * The map of jdo files.
     */
    final Map files = new HashMap();

    /**
     * Creates an intsance.
     */
    public ListResourceLocator(PrintWriter out,
                               boolean verbose,
                               List fileNames)
        throws IOException
    {
        super(out, verbose);
        affirm(fileNames != null);
        
        // hash the file objects by their canonical name
        for (Iterator i = fileNames.iterator(); i.hasNext();) {
            final String s = (String)i.next();

            // canonicalize file name
            final File file = new File(s).getCanonicalFile();
            final URL url = file.toURL();
            final String canonicalName = url.toString();
            affirm(canonicalName != null);

            // ensure file is readable
            if (!file.canRead()) {
                final String msg
                    = getI18N("enhancer.cannot_read_resource",
                              file.toString());
                throw new IOException(msg);
            }

            // hash file by its canonicalized resource name
            files.put(canonicalName, file);
            printMessage(getI18N("enhancer.using_file",
                                 canonicalName));
        }
    }
    
    /**
     * Finds a resource with a given name.
     */
    public InputStream getInputStreamForResource(String resourceName)
    {
        //printMessage("ListResourceLocator.getInputStreamForResource() : resourceName = " + resourceName);

        affirm(resourceName != null);
        
        final Set entries = files.entrySet();
        for (Iterator i = entries.iterator(); i.hasNext();) {
            final Map.Entry entry = (Map.Entry)i.next();
            final String fileName = (String)entry.getKey();
            if (!fileName.endsWith(resourceName)) {
                continue;
            }
            final File file = (File)entry.getValue();
            
            final InputStream stream;
            try {
                stream = new FileInputStream(file);
            } catch (FileNotFoundException ex) {
                // would be better to throw an IOException but currently
                // not supported by the JDOModel's JavaModel interface
                final String msg
                    = getI18N("enhancer.io_error_while_reading_resource",
                              file.toString(), ex.getMessage());
                throw new RuntimeException(msg);
            }
            affirm(stream != null);
            printMessage(getI18N("enhancer.found_resource", resourceName));
            return stream;
        }
        printMessage(getI18N("enhancer.not_found_resource", resourceName));
        return null;
    }
}
