/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.enterprise.module.bootstrap.Which;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.bootstrap.PlatformMain;

import java.io.*;
import java.util.logging.Logger;
import java.net.URI;

/**
 * Top level abstract main class
 *
 * @author Jerome Dochez
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public abstract class AbstractMain extends PlatformMain {

    final File bootstrapFile;

    protected ASMainHelper helper;

    final protected File glassfishDir; // glassfish/

    protected File domainDir; // default is glassfish/domains/domain1    

    abstract Logger getLogger();

    protected abstract String getPreferedCacheDir();

    AbstractMain() {
        this.bootstrapFile = findBootstrapFile();
        System.setProperty(StartupContext.ROOT_PROP, bootstrapFile.getParent());
        glassfishDir = bootstrapFile.getParentFile().getParentFile(); //glassfish/
        System.setProperty("com.sun.aas.installRoot",glassfishDir.getAbsolutePath());
    }

    public void start(String[] args) throws Exception {
        helper = new ASMainHelper(logger);
        helper.parseAsEnv(glassfishDir);
        run(logger, args);
    }

    protected void run(Logger logger, String... args) throws Exception {
        this.logger = logger;
        StartupContext sc = getContext(StartupContext.class);
        if (sc!=null) {
            domainDir = sc.getUserDirectory();
        }
        if (domainDir==null) {
            domainDir = helper.getDomainRoot(new StartupContext(bootstrapFile, args));
            helper.verifyAndSetDomainRoot(domainDir);
        }

        File cacheProfileDir = new File(domainDir, getPreferedCacheDir());
        // This is where inhabitants cache is located
        System.setProperty("com.sun.enterprise.hk2.cacheDir", cacheProfileDir.getAbsolutePath());
        setUpCache(bootstrapFile.getParentFile(), cacheProfileDir);
    }

    protected void setSystemProperties() throws Exception {
       /* Set a system property called com.sun.aas.installRootURI.
         * This property is used in felix/conf/config.properties and possibly
         * in other OSGi framework's config file to auto-start some modules.
         * We can't use com.sun.aas.installRoot,
         * because that com.sun.aas.installRoot is a directory path, where as
         * we need a URI.
         */
        String installRoot = System.getProperty("com.sun.aas.installRoot");
        URI installRootURI = new File(installRoot).toURI();
        System.setProperty("com.sun.aas.installRootURI", installRootURI.toString());
        String instanceRoot = System.getProperty("com.sun.aas.instanceRoot");
        URI instanceRootURI = new File(instanceRoot).toURI();
        System.setProperty("com.sun.aas.instanceRootURI", instanceRootURI.toString());        
    }

    protected abstract void setUpCache(File sourceDir, File cacheDir) throws IOException;

    protected File findBootstrapFile() {
        try {
            return Which.jarFile(getClass());
        } catch (IOException e) {
            throw new RuntimeException("Cannot get bootstrap path from "
                    + getClass() + " class location, aborting");
        }
    }


}
