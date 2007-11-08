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
package com.sun.enterprise.util;

// import java.util.zip.*;
// import java.util.*;
// import java.io.InputStream;
// import java.io.BufferedInputStream;

import java.io.IOException;
import java.io.File;
import java.util.Set;
import java.util.HashSet;

import java.net.URL;
import com.sun.enterprise.util.FileUtil;

//START OF IASRI 4660742
import java.util.logging.*;
import com.sun.logging.*;
//END OF IASRI 4660742

// IASRI 4709925 - updated ejb class loader
import com.sun.enterprise.loader.EJBClassLoader;

public final class JarClassLoader extends EJBClassLoader {

    // START OF IASRI 4660742
    static final Logger _logger=LogDomains.getLogger(LogDomains.UTIL_LOGGER);
    // END OF IASRI 4660742

    // START OF IASRI 4679641
    // private static final boolean debug = false;
    private static final boolean debug = com.sun.enterprise.util.logging.Debug.enabled;
    // END OF IASRI 4679641
  
    public JarClassLoader() {
        super(ConnectorClassLoader.getInstance());
    }

    public JarClassLoader(String jarName) throws IOException { 
        super(ConnectorClassLoader.getInstance());
        addJar(jarName); 
    } 
    
    /**
     * Add a JAR to the list of JARs we search for a class's bytecodes.
     */
    
    public synchronized void addJar(String jarName) throws IOException {
	// The URL is OS specific. On Solaris, file:/xyz and file:///xyz work.
	// On NT, file://c:/xyz and file:/c:/xyz work.  
	// So we let the java.io.File create a URL for us.
	File file = new File(FileUtil.getAbsolutePath(jarName));
	/** IASRI 4660742
	if(debug) 
	    System.err.println("JarClassLoader.addJar: url=" + file.toURL());
	**/
	//START OF IASRI 4660742
	if(debug && _logger.isLoggable(Level.FINE)) {
	    _logger.log(Level.FINE,"JarClassLoader.addJar: url=" + file.toURL());
  }
	//END OF IASRI 4660742

	appendURL(file);
    }

    public void addDir(URL url) throws IOException {
	/** IASRI 4660742
	if(debug) 
	    System.err.println("JarClassLoader.addDir: url=" + url);
	**/
	//START OF IASRI 4660742
	if(debug && _logger.isLoggable(Level.FINE)) {
	    _logger.log(Level.INFO,"JarClassLoader.addDir: url=" + url);
  }
	//END OF IASRI 4660742
        appendURL(url);
    }

    /**
     @deprecated use getClasspath()
     */
    public String getClassPath() {
        return getClasspath();
    }
    
    public final String getClasspath() {
	String cpath = "";
	URL[] urls = getURLs();
	String sep = File.pathSeparator;

	for(int i=0; i < urls.length; ++i) {
	    cpath = cpath + sep + urls[i].getFile(); 
	}

	return cpath;
    }
}





