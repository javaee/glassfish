/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.security.embedded;

import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.util.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.types.Property;

/**
 *
 * @author nithyasubramanian
 */
public class EmbeddedSecurityUtil {

    public static void copyConfigFiles(Habitat habitat, String fromInstanceDir, String toInstanceDir) throws IOException {
        //For security reasons, permit only an embedded server instance to carry out the copy operations
        ServerEnvironment se = habitat.getComponent(ServerEnvironment.class);
        if (!isEmbedded(se)) {
            return;
        }

        if ((fromInstanceDir == null) || (toInstanceDir == null)) {
            throw new IllegalArgumentException("Null inputs");
        }

        File fileFromInstanceDir = new File(fromInstanceDir);
        File fileToInstanceDir = new File(toInstanceDir);

        List<String> fileNames = new ArrayList<String>();


        //Add FileRealm keyfiles to the list

        SecurityService securityService = habitat.getComponent(SecurityService.class);
        fileNames.addAll(getKeyFileNames(securityService));

        //Add keystore and truststore files

        // For the embedded server case, will the system properties be set in case of multiple embedded instances?
        //Not sure - so obtain the other files from the usual locations instead of from the System properties

        String keyStoreFileName = fromInstanceDir + File.separator + "config" + File.separator + "keystore.jks";
        String trustStoreFileName = fromInstanceDir + File.separator + "config" + File.separator + "cacerts.jks";

        fileNames.add(keyStoreFileName);
        fileNames.add(trustStoreFileName);

        //Add login.conf and security policy

        String loginConf = fromInstanceDir + File.separator + "config" + File.separator + "login.conf";
        String secPolicy = fromInstanceDir + File.separator + "config" + File.separator + "security.policy";

        fileNames.add(loginConf);
        fileNames.add(secPolicy);

        File toConfigDir = new File(fileToInstanceDir, "config");
        if (!toConfigDir.exists()) {
            toConfigDir.mkdir();
        }

        //Copy files into new directory

        for (String fileName : fileNames) {

            FileUtils.copyFile(new File(fileName), new File(toConfigDir, parseFileName(fileName)));
        }


    }

    public static List<String> getKeyFileNames(SecurityService securityService) {
        List<String> keyFileNames = new ArrayList<String>();

        List<AuthRealm> authRealms = securityService.getAuthRealm();
        for (AuthRealm authRealm : authRealms) {
            String className = authRealm.getClassname();
            if ("com.sun.enterprise.security.auth.realm.file.FileRealm".equals(className)) {
                List<Property> props = authRealm.getProperty();
                for (Property prop : props) {
                    if ("file".equals(prop.getName())) {
                        keyFileNames.add(prop.getValue());
                    }
                }
            }
        }

        return keyFileNames;
    }

    public static String parseFileName(String fullFilePath) {
        if (fullFilePath == null) {
            return null;
        }
        int beginIndex = fullFilePath.lastIndexOf(File.separator);
        return fullFilePath.substring(beginIndex + 1);
    }

    public static boolean isEmbedded(ServerEnvironment se) {
        if (se.getRuntimeType() == RuntimeType.EMBEDDED) {
            return true;
        }
        return false;
    }
}
