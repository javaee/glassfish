
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

package org.glassfish.maven;

import java.io.*;

import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.EmbeddedFileSystem;


public  class Util {

    public static Server getServer(String serverID, String installRoot, String instanceRoot, String configFile) throws IOException {
        Server.Builder builder = new Server.Builder(serverID);

        EmbeddedFileSystem efs = getFileSystem(installRoot, instanceRoot, configFile);
        Server server;
        if (efs != null) {
            server = builder.setEmbeddedFileSystem(efs).build();
        }
        else {
            server = builder.build();
        }
        return server;
    }

    public static EmbeddedFileSystem getFileSystem(String installRoot, String instanceRoot, String configFile) {
        if (installRoot == null && instanceRoot == null && configFile == null)
            return null;

        System.out.println("InstallRoot = " + installRoot);
        System.out.println("InstanceRoot = " + instanceRoot);
        if (instanceRoot == null && installRoot != null) {
            instanceRoot = installRoot + "/domains/domain1";
        }

        System.out.println("InstanceRoot = " + instanceRoot);
        if (configFile == null && instanceRoot != null) {
            configFile = instanceRoot + "/config/domain.xml";
        }

        EmbeddedFileSystem.Builder efsb = new EmbeddedFileSystem.Builder();
        if (installRoot != null)
            efsb.setInstallRoot(new File(installRoot), true);
        if (instanceRoot != null)
            efsb.setInstanceRoot(new File(instanceRoot));
        if (configFile != null)
            efsb.setConfigurationFile(new File(configFile));

        return efsb.build();
    }



}
