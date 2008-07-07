/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
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


package org.jvnet.hk2.osgiadapter;

import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.common_impl.DirectoryBasedRepository;
import org.osgi.framework.Constants;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;

/**
 * Only OSGi bundles are recognized as modules.
 * 
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiDirectoryBasedRepository extends DirectoryBasedRepository {

    public OSGiDirectoryBasedRepository(String name, File repository) {
        super(name, repository);
    }

    public OSGiDirectoryBasedRepository(String name, File repository, boolean isTimerThreadDaemon) {
        super(name, repository, isTimerThreadDaemon);
    }

    /**
     * This class overrides this mthod, because we don't support the following cases:
     * 1. external manifest.mf file for a jar file
     * 2. jar file exploded as a directory.
     * Both the cases are supported in HK2, but not in OSGi.
     *
     * @param jar bundle jar
     * @return a ModuleDefinition for this bundle
     * @throws IOException
     */
    @Override
    protected ModuleDefinition loadJar(File jar) throws IOException {
        assert (jar.isFile()); // no support for exploded jar
        Manifest m = new JarFile(jar).getManifest();
        if (m != null) {
            if (m.getMainAttributes().getValue(Constants.BUNDLE_SYMBOLICNAME) != null) {
                Logger.logger.logp(Level.FINE, "OSGiDirectoryBasedRepository", "loadJar",
                        "{0} is an OSGi bundle", new Object[]{jar});
                return newModuleDefinition(jar, null);
            }
        }
        return null;
    }

    @Override
    protected ModuleDefinition newModuleDefinition(File jar, Attributes attr) throws IOException {
        return new OSGiModuleDefinition(jar);
    }
}
