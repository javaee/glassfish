/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

package org.glassfish.embed;

import  com.sun.enterprise.util.ObjectAnalyzer;
import org.glassfish.embed.impl.ReadableArchiveAdapter;
import org.glassfish.api.deployment.archive.ReadableArchive;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.Manifest;

/**
 * {@link ReadableArchive} representation of an application that
 * is not assembled into the canonical WAR/JAR/RAR format.
 *
 * @author Kohsuke Kawaguchi
 * @author Byron Nevins
 */

public class ScatteredArchive extends ReadableArchiveAdapter {
    /**
     *
     * @param name Application name. Among other things, this is used by default as the context path
     *      when you deploy this scattered war file.
     * @param resources Directory where static resources like JSPs live.  WEB-INF/web.xml may be hre as well.
     * @param webXml if null, defaults to {@code WEB-INF/web.xml} under {@code resources}.
     * @param classes A Collection of classpath URLs for this application
     */
    public ScatteredArchive(String name, File resources, File webXml, Collection<URL> classes) {
        this.name = name;
        this.resources = resources;
        if(webXml==null)
            webXml = new File(resources,"WEB-INF/web.xml");
        this.webXml = webXml;
        this.classpath = classes;
    }

    /**
     * Get the classpath URLs
     * @return A read-only copy of the classpath URL Collection
     */
    public Iterable<URL> getClassPath() {
        return Collections.unmodifiableCollection(classpath);
    }

    /**
     *
     * @return The resources directory
     */
    public File getResourcesDir() {
        return resources;
    }


    ///////////////////////////////////////////////////////////////////////////
    //////      public methods that implement the Archive interface
    ///////////////////////////////////////////////////////////////////////////

  /**
     * Returns the InputStream for the given entry name
     * The file name must be relative to the root of the module.
     *
     * @param name the file name relative to the root of the module.
     * @return the InputStream for the given entry name or null if not found.
     */
    
    public InputStream getEntry(String name) throws IOException {
        File f = getFile(name);
        if(f.exists())  return new FileInputStream(f);
        return null;
    }
   /**
     * Returns whether or not a file by that name exists
     * The file name must be relative to the root of the module.
     *
     * @param name the file name relative to the root of the module.
     * @return does the file exist?
     */

    public boolean exists(String name) throws IOException {
        return getFile(name).exists();
    }

   /**
     * Returns an enumeration of the module file entries.  All elements
     * in the enumeration are of type String.  Each String represents a
     * file name relative to the root of the module.
     * <p><strong>Currently under construction</strong>
     * @return an enumeration of the archive file entries.
     */
    public Enumeration<String> entries() {
        // TODO: abstraction breakage. We need file-level abstraction for archive
        // and then more structured abstraction.
        return EMPTY_ENUMERATOR;
    }

    /**
     * Returns the manifest information for this archive
     * @return the manifest info
     */
    public Manifest getManifest() throws IOException {
        // TODO: we can support manifest.
        // for now I'm not doing this because it seems like the value of this is limited for webapps.
        return null;
    }
    /**
     * Returns the path used to create or open the underlying archive
     *
     * <p>
     * TODO: abstraction breakage:
     * Several callers, most notably {@link DeploymentContext#getSourceDir()}
     * implementation, assumes that this URI is an URL, and in fact file URL.
     *
     * <p>
     * If this needs to be URL, use of {@link URI} is misleading. And furthermore,
     * if its needs to be a file URL, this should be {@link File}.
     *
     * @return the path for this archive.
     */
    public URI getURI() {
        return resources.toURI();
    }

    /**
     * Returns the name of the archive.
     * <p>
     * Implementations should not return null.
     * @return the name of the archive
     */
    public String getName() {
        return name;
    }
    /**
     * Returns an enumeration of the module file entries with the
     * specified prefix.  All elements in the enumeration are of
     * type String.  Each String represents a file name relative
     * to the root of the module.
     * <p><strong>Currently Not Supported</strong>
     * @param prefix the prefix of entries to be included
     * @return an enumeration of the archive file entries.
     * @throws UnsupportedOperationException always
     */

    public Enumeration<String> entries(String s) {
        throw new UnsupportedOperationException(unsupported("entries(String)"));
    }

    public String toString() {
        return ObjectAnalyzer.toString(this);
    }


    ///////////////////////////////////////////////////////////////////////////
    //////             End of public API     //////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////


    /**
     * Maps the resource within the war into physical file.
     *
     * <p>
     * This method creates an illusion that we actually have a fully assembled war,
     * when in reality we don't. The illusion is partial because we can't
     * emulate WEB-INF/classes and WEB-INF/lib.
     *
     * @param name Relative path from within the canonical war format.
     * @return concrete location.
     */
    private File getFile(String name) {
        if(name.equals("WEB-INF/web.xml"))
            return webXml;
        return new File(resources, name);
    }

    private String unsupported(String s) {
        s = getClass().getName() + "." + s;
        s += " is not supported yet.";
        return s;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private final           File                    resources;  // Static resources, JSP, etc.
    private final           File                    webXml;     // Location of web.xml
    private final           Collection<URL>         classpath;  // Classes and jar files
    private final           String                  name;
    private static final    Enumeration<String>     EMPTY_ENUMERATOR = new Vector<String>().elements();
}
