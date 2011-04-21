/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.vmcluster.runtime;

import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.SFTPv3FileAttributes;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.cluster.ssh.sftp.SFTPClient;
import org.glassfish.vmcluster.config.Template;
import org.glassfish.vmcluster.os.FileOperations;
import org.glassfish.vmcluster.spi.Machine;
import org.glassfish.vmcluster.util.RuntimeContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;

/**
 * Defines a local template, installed on *this* machine.
 * @author Jerome Dochez
 */
public class LocalTemplate extends VMTemplate {

    public LocalTemplate(String location, Template config) {
        super(location, config);
    }

    @Override
    public synchronized void copyTo(Machine destination, String destDir) throws IOException {

        String destPath = destDir + "/" + config.getName() + ".img";
        File source = new File(getLocation() + "/" + config.getName() + ".img");
        if (!source.exists()) {
            RuntimeContext.logger.severe("Cannot find template " + source.getAbsolutePath());
            throw new FileNotFoundException("Cannot find template " + source.getAbsolutePath());
        }

        FileOperations files = destination.getFileOperations();

        try {
            if (!files.exists(destPath)) {
                files.mkdir(destination.getConfig().getTemplatesLocation());
                RuntimeContext.logger.info("Copying template " + getDefinition().getName() + " on "
                    + destination.getName());
                files.copy(source, new File(destination.getConfig().getTemplatesLocation()));
                RuntimeContext.logger.info("Finished copying template " + getDefinition().getName() + " on "
                    + destination.getName());
            }
        } catch (IOException e) {
            RuntimeContext.logger.log(Level.SEVERE, "Cannot copy template on " + config.getName(),e);
            throw e;
        }

    }

    @Override
    public long getSize() throws IOException {
        File source = new File(getLocation() + "/" + config.getName() + ".img");
        return source.length();
    }

    @Override
    public boolean isLocal() {
        return true;
    }
}