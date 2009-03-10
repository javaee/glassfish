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

import org.glassfish.api.deployment.archive.ReadableArchive;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.Manifest;

/**
 * {@link ReadableArchive} representation of an web application that
 * is not assembled into the canonical war format. 
 *
 * @author Kohsuke Kawaguchi
 */
public class ScatteredWar extends ReadableArchiveAdapter {
    /**
     *
     * @param name
     *      Application name. Among other things, this is used by default as the context path
     *      when you deploy this scattered war file.
     * @param webXml
     *      if null, defaults to {@code WEB-INF/web.xml} under {@code resources}.
     */
    public ScatteredWar(String name, File resources, File webXml, Collection<URL> classes) {
        this.name = name;
        this.resources = resources;
        if(webXml==null)
            webXml = new File(resources,"WEB-INF/web.xml");
        this.webXml = webXml;
        this.classpath = classes;
    }

    public Iterable<URL> getClassPath() {
        return Collections.unmodifiableCollection(classpath);
    }

    public File getResourcesDir() {
        return resources;
    }


    /**
     * Maps the resource within the war into physical file.
     *
     * <p>
     * This method creates an illusion that we actually have a fully assembled war,
     * when in reality we don't. The illusion is partial because we can't
     * emulate WEB-INF/classes and WEB-INF/lib.
     *
     * @param name
     *      Relative path from within the canonical war format.
     * @return
     *      concrete location.
     */
    private File getFile(String name) {
        if(name.equals("WEB-INF/web.xml"))
            return webXml;
        return new File(resources, name);
    }

    public InputStream getEntry(String name) throws IOException {
        File f = getFile(name);
        if(f.exists())  return new FileInputStream(f);
        return null;
    }
    
    public boolean isDirectory(String name) {
    	File f = getFile(name);
    	return f.isDirectory();
    }

    public boolean exists(String name) throws IOException {
        return getFile(name).exists();
    }
    
    public Collection<String> getDirectories() throws IOException {
    	return new Vector<String>();
   	}

    public Enumeration<String> entries() {
        // TODO: abstraction breakage. We need file-level abstraction for archive
        // and then more structured abstraction.
        return EMPTY_ENUMERATOR;
    }

    public Manifest getManifest() throws IOException {
        // TODO: we can support manifest.
        // for now I'm not doing this because it seems like the value of this is limited for webapps.
        return null;
    }

    public URI getURI() {
        return resources.toURI();
        // see my note on getURI javadoc. This isn't a URI
//        return URI.create("scattered-war:"+resources.getPath());
    }

    public String getName() {
        return name;
    }

    ///////////////////////////////////////////////////////////////////////////
    
    private final File resources;       // Static resources, JSP, etc.
    private final File webXml;          // Location of web.xml
    private final Collection<URL> classpath;  // Classes and jar files
    private final String name;
    private static final Enumeration EMPTY_ENUMERATOR = new Vector().elements();


    public Enumeration<String> entries(String arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
