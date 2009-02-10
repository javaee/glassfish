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

package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.Repository;
import com.sun.enterprise.module.RepositoryChangeListener;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;

/**
 * {@link URLClassLoader} that listens to {@link Repository} changes and add those jar files automatically.
 *
 * @author Sahoo
 */
final class ExtensibleClassLoader extends URLClassLoader implements RepositoryChangeListener {
    ExtensibleClassLoader(URL[] urls, ClassLoader parent, List<Repository> repos) {
        super(urls, parent);
        for (Repository repo : repos) {
            // Add all the existing libraries to classloader
            for(URI uri : repo.getJarLocations()) {
                if (addURI(uri)) {
                    LOGGER.info("Added " + uri + " to shared classpath");
                }
            }
            repo.addListener(this);
        }
    }

    public void jarAdded(java.net.URI uri) {
        added(uri);
     }

    public void jarRemoved(java.net.URI uri) {
        removed(uri);
     }

    public void added(URI uri) {
        File file =new File(uri);
        if (file.isDirectory()) {
            LOGGER.info("directory not support, please contribute");
        }
        if (addURI(uri)) {
            LOGGER.info("Added " + uri + " to shared classpath, no need to restart appserver");
        }
    }

    public void removed(URI uri) {
    }

    public void moduleAdded(ModuleDefinition moduleDefinition) {
    }

    public void moduleRemoved(ModuleDefinition moduleDefinition) {
    }

    private boolean addURI(URI uri) {
        boolean success = false;
        try {
            super.addURL(uri.toURL());
            success = true;
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, "Cannot add new added library to shared classpath", e);
        }
        return success;
    }

    private static final Logger LOGGER = Logger.getLogger(ExtensibleClassLoader.class.getName());
}
