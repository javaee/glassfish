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

/*
 * @(#) ClassLoaderUtils.java
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with iPlanet/Sun Microsystems.
 */
package com.sun.enterprise.loader;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.StringTokenizer;

import com.sun.enterprise.util.io.FileUtils;

import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;


/**
 * Contains utility methods to create class loaders.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public class ClassLoaderUtils {

    /** logger for this class */
    private static Logger _logger =
        LogDomains.getLogger(LogDomains.LOADER_LOGGER);

    /**
     * Returns a new class loader based on the given directory paths and 
     * the jar files & zip files found under jar directory.  
     *
     * @param    dirs     array of directory path names
     * @param    jarDirs  array of path name to directories that contains
     *                    JAR & ZIP files.
     * @param    parent   parent class loader for the new class loader
     *
     * @return   a new class loader based on the urls from the given params
     *
     * @throws  IOException  if an i/o error while constructing the urls
     */
    public static ClassLoader getClassLoader(File[] dirs, File[] jarDirs, 
            ClassLoader parent) throws IOException {

        URLClassLoader loader  = null;
        URL[] urls             = getUrls(dirs, jarDirs);

        if (urls != null) {
            if (parent != null) {
                loader = new URLClassLoader(urls, parent); 
            } else {
                loader = new URLClassLoader(urls);
            }
        }

        return loader;
    }

    /**
     * Returns an array of urls that contains ..
     * <pre>
     *    i.   all the valid directories from the given directory (dirs) array
     *    ii.  all jar files from the given directory (jarDirs) array
     *    iii. all zip files from the given directory (jarDirs) array
     * </pre>
     *
     * @param    dirs     array of directory path names
     * @param    jarDirs  array of path name to directories that contains
     *                    JAR & ZIP files.
     * @return   an array of urls that contains all the valid dirs, 
     *           *.jar & *.zip
     *
     * @throws  IOException  if an i/o error while constructing the urls
     */
    public static URL[] getUrls(File[] dirs, File[] jarDirs) 
            throws IOException {

        URL[] urls  = null;
        List list   = new ArrayList();

        // adds all directories
        if (dirs != null) {
            for (int i=0; i<dirs.length; i++) {
                File dir = dirs[i];
                if (dir.isDirectory() || dir.canRead()) {
                    URL url = dir.toURI().toURL();
                    list.add(url);

                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.log(Level.FINE, 
                            "Adding directory to class path:" + url.toString());
                    }
                }
            }
        } 

        // adds all the jars
        if (jarDirs != null) {
            for (int i=0; i<jarDirs.length; i++) {
                File jarDir =  jarDirs[i];

                if (jarDir.isDirectory() || jarDir.canRead()) {
                    File[] files = jarDir.listFiles();

                    for (int j=0; j<files.length; j++) {
                        File jar = files[j];

                        if ( FileUtils.isJar(jar) || FileUtils.isZip(jar) ) {
                            list.add(jar.toURI().toURL());

                            if (_logger.isLoggable(Level.FINE)) {
                                _logger.log(Level.FINE, 
                                    "Adding jar to class path:" + jar.toURL());
                            }
                        }
                    }
                }
            }
        }

        // converts the list to an array
        if (list.size() > 0) {
            urls = new URL[list.size()];
            urls = (URL[]) list.toArray(urls);
        }

        return urls;
    }

    /**
     * Returns a list of urls that contains ..
     * <pre>
     *    i.   all the valid directories from the given directory (dirs) array
     *    ii.  all jar files from the given directory (jarDirs) array
     *    iii. all zip files from the given directory (jarDirs) array
     * </pre>
     *
     * This is similar to getUrls(File[], File[])
     *
     * @param    dirs     array of directory path names
     * @param    jarDirs  array of path name to directories that contains
     *                    JAR & ZIP files.
     * @return   a list of urls that contains all the valid dirs, 
     *           *.jar & *.zip; the obj representing the paths are
     *           of type java.lang.String. It returns an empty list
     *           if no valid dir, jar or zip present.
     *
     * @throws  IOException  if an i/o error while constructing the urls
     *
     * @see #getUrls(File[], File[]);
     */
    public static List getUrlList(File[] dirs, File[] jarDirs) 
        throws IOException {
        return getUrlList(dirs, jarDirs, false); 
    }

    /**
     * Returns a list of urls that contains ..
     * <pre>
     *    i.   all the valid directories from the given directory (dirs) 
     *         array    
     *    ii.  all jar files from the given directory (jarDirs) array
     *    iii. all zip files from the given directory (jarDirs) array if
     *         not ignoring zip file (ignoreZip is false).
     * 
     * </pre>
     * 
     * @param    dirs     array of directory path names
     * @param    jarDirs  array of path name to directories that contains
     *                    JAR & ZIP files.
     * @return   a list of urls that contains all the valid dirs,
     *           *.jar & *.zip if not ignoring zip file, the obj 
     *           representing the paths are of type java.lang.String. 
     *           It returns an empty list if no valid dir, jar or zip 
     *           present.
     *
     * @throws  IOException  if an i/o error while constructing the urls
     *
     */
    public static List getUrlList(File[] dirs, File[] jarDirs,
        boolean ignoreZip) throws IOException {

        List list   = new ArrayList();

        // adds all directories
        if (dirs != null) {
            for (int i=0; i<dirs.length; i++) {
                File dir = dirs[i];
                if (dir.isDirectory() || dir.canRead()) {
                    list.add( dir.getCanonicalPath() );

                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.log(Level.FINE, 
                            "Adding directory to class path:" 
                            + dir.getCanonicalPath());
                    }
                }
            }
        } 

        // adds all the jars
        if (jarDirs != null) {
            for (int i=0; i<jarDirs.length; i++) {
                File jarDir =  jarDirs[i];

                if (jarDir.isDirectory() || jarDir.canRead()) {
                    File[] files = jarDir.listFiles();

                    for (int j=0; j<files.length; j++) {
                        File jar = files[j];

                        if ( FileUtils.isJar(jar) || 
                            (!ignoreZip && FileUtils.isZip(jar)) ) {
                            list.add( jar.getCanonicalPath() );

                            if (_logger.isLoggable(Level.FINE)) {
                                _logger.log(Level.FINE, 
                                    "Adding jar to class path:" 
                                    + jar.getCanonicalPath());
                            }
                        }
                    }
                }
            }
        }

        return list;
    }

    private static final URL[] EMPTY_URL_ARRAY = new URL[0];
    
    /**
     * get URL[] from classpath
     * catches exception for wrong files
     */
    public static URL[] getUrlsFromClasspath(String classpath) {
        final List<URL> urls  = new ArrayList<URL>();
        
        if (classpath == null) {
            return EMPTY_URL_ARRAY;
        }
        
        // tokenize classpath
        final StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);
        while (st.hasMoreTokens()) {
            try {
                File f = new File(st.nextToken());
                urls.add(f.toURI().toURL());
            } catch(Exception e) {
                  _logger.log(Level.WARNING,
				  			"loader.unexpected_error_while_creating_urls",e);
            }
        }
         
        // converts the list to an array
        URL[] ret;
        if (urls.size() > 0) {
            ret = urls.toArray(new URL[]{});
        } else {
            ret = new URL[0];
        }

        return ret;
     }

    /**
     * Unit test code.
     */
    public static void main(String[] args) {

        try {
            URL[] urls = getUrls(new File[] {new File(args[0])}, 
                     new File[] {new File(args[1])});
            for (int i=0; i<urls.length; i++) {
                System.out.println(urls[i]);
            }

            URLClassLoader loader = (URLClassLoader) 
                    getClassLoader(new File[] {new File(args[0])},
                        new File[] {new File(args[1])}, null);

            //Class c = Class.forName(args[2], true, loader);
            Class c = loader.loadClass(args[2]);
            System.out.println("Loaded: " + c.getName());
            System.out.println("Loaded class has the following methods...");
            java.lang.reflect.Method[] m = c.getDeclaredMethods();
            for (int i=0; i<m.length; i++) {
                System.out.println(m[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
