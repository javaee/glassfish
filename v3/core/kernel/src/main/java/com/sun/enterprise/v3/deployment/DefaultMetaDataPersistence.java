/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.v3.deployment;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import com.sun.enterprise.v3.contract.ApplicationMetaDataPersistence;
import com.sun.enterprise.v3.server.ServerEnvironment;
import com.sun.logging.LogDomains;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Default persistence scheme that use the file system
 */
@Service
public class DefaultMetaDataPersistence implements ApplicationMetaDataPersistence {

    @Inject
    ServerEnvironment env;

    public void save(String name, Properties moduleProps) {

        File generatedAppRoot = new File(env.getApplicationStubPath(), name);

        OutputStream os=null;
        File propFile = null;
        try {
            if (!generatedAppRoot.exists()) {
                generatedAppRoot.mkdirs();
            }

            propFile = new File(generatedAppRoot, "glassfish.props");
            os = new BufferedOutputStream(new FileOutputStream(propFile));
            moduleProps.store(os, "GlassFish container properties");
        } catch(IOException ioe) {
            LogDomains.getLogger(LogDomains.DPL_LOGGER).severe("IOException while saving module properties : " + ioe.getMessage());

            try {
                if (os!=null) {
                    os.close();
                }
                if (propFile.exists()) {
                    propFile.delete();
                }
                if (generatedAppRoot.exists()) {
                    generatedAppRoot.delete();
                }
            } catch(IOException e) {
                // ignore
            }
        } finally {
            try {
                if (os!=null) {
                 os.close();
                }
            } catch(IOException ioe) {
               // ignre
            }
        }
    }

    public Properties load(String appName) {

        File appRoot = new File(env.getApplicationStubPath(), appName);
        // do we have our glassfish.props ?
        File propertiesFile = new File(appRoot, "glassfish.props");

        if (propertiesFile.exists()) {
            // bingo
            Properties props = new Properties();
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(propertiesFile));
                props.load(is);
            } catch(IOException ioe) {
                LogDomains.getLogger(LogDomains.DPL_LOGGER).log(Level.SEVERE,
                        "Cannot load appserver meta information about " + appRoot.getName(), ioe);
                return null;
            } finally {
                try {
                    if (is!=null) {
                        is.close();
                    }
                } catch(IOException e) {
                    // ignore
                }
            }
            return props;
        }
        return null;
    }
}
