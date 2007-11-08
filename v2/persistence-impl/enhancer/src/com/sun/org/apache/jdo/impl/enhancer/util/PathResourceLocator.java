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

import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.File;
import java.io.InputStream;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Searches resources within a path.
 */
public class PathResourceLocator
    extends ResourceLocatorBase
    implements ResourceLocator
{
    /**
     * The class loader for loading jdo resources.
     */
    final private URLClassLoader classLoader;

    /**
     * Returns a classloader initialized on the path provided to constructor.
     */
    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Creates an instance.
     */
    public PathResourceLocator(PrintWriter out,
                               boolean verbose,
                               String path)
        throws IOException
    {
        super(out, verbose);
        affirm(path != null);

        // convert path into list of URLs
        final List urls = new ArrayList();
        for (Enumeration e = new StringTokenizer(path, File.pathSeparator);
             e.hasMoreElements();) {
            final String s = (String)e.nextElement();

            // canonicalize file name
            final File file = new File(s).getCanonicalFile();
            final URL url = file.toURL();
            final String canonicalName = url.toString();
            affirm(canonicalName != null);

            // ensure path element is readable
            if (!file.canRead()) {
                final String msg
                    = getI18N("enhancer.cannot_read_resource",
                              file.toString());
                throw new IOException(msg);
            }

            // ensure path element is either directory or a jar/zip file
            final String l = s.toLowerCase();
            if (!(file.isDirectory()
                  || (file.isFile()
                      && (l.endsWith(".jar") || l.endsWith(".zip"))))) {
                final String msg
                    = getI18N("enhancer.illegal_path_element",
                              file.toString());
                throw new IOException(msg);
            }

            urls.add(url);
            printMessage(getI18N("enhancer.using_path_element",
                                 canonicalName));
        }

        // create class loader
        final URL[] urlArray = (URL[])urls.toArray(new URL[urls.size()]);
        classLoader = new URLClassLoader(urlArray, null);
        affirm(classLoader != null);
    }

    /**
     * Finds a resource with a given name.
     */
    public InputStream getInputStreamForResource(String resourceName)
    {
        //printMessage("PathResourceLocator.getInputStreamForResource() : resourceName = " + resourceName);

        affirm(resourceName != null);

        // not using getResourceAsStream() to catch IOExceptions
        final URL url = classLoader.findResource(resourceName);
        if (url == null) {
            printMessage(getI18N("enhancer.not_found_resource", resourceName));
            return null;
        }

        // return input stream
        final InputStream stream;
        try {
            stream = url.openStream();
        } catch (IOException ex) {
            // would be better to throw an IOException but currently
            // not supported by the JDOModel's JavaModel interface
            final String msg
                = getI18N("enhancer.io_error_while_reading_resource",
                          url.toString(), ex.getMessage());
            throw new RuntimeException(msg);
        }
        affirm(stream != null);
        printMessage(getI18N("enhancer.found_resource", resourceName));
        return stream;
    }
}
