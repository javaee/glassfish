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
package org.glassfish.deployment.admin;

import com.sun.enterprise.util.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.Cluster;
import org.glassfish.api.admin.RuntimeType;
import org.jvnet.hk2.annotations.Service;

import org.glassfish.api.ActionReport.ExitCode;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;


/**
 * Trivial example of deployment-for-instance command
 * @author hzhang_jn
 * @author tjquinn
 */
@Service(name="_deploy")
@Scoped(PerLookup.class)
@Cluster(value={RuntimeType.INSTANCE})
public class InstanceDeployCommand extends InstanceDeployCommandParameters implements AdminCommand {

    private final static String LS = System.getProperty("line.separator");
    @Override
    public void execute(AdminCommandContext ctxt) {


        final StringBuilder outputSB = new StringBuilder();
        outputSB.append(getClass().getName()).append(" is running").append(LS)
                .append("  path = " + path).append(LS)
                .append("  deploymentplan = ").append(deploymentplan).append(LS)
                .append("  generateddirs = ").append(LS);

        final List<File> generatedDirs = Arrays.asList(generatedejbdir, generatedjspdir, generatedpolicydir, generatedxmldir);
        for (File generatedDir : generatedDirs) {
            if (generatedDir != null) {
                listFile(outputSB, generatedDir, "    ");
            }

        }

        System.out.println(outputSB.toString());

        ctxt.report.setActionExitCode(ExitCode.SUCCESS);
        ctxt.report.setMessage("InstanceDeploy: path = " + path.getAbsolutePath() +
                ", plan = " + ((deploymentplan == null) ? "null" : deploymentplan.getAbsolutePath()));
        return;
    }

    private void listFile(final StringBuilder sb, final File file, String indentation) {
        sb.append(indentation).append(file.getAbsolutePath()).append(file.isDirectory() ? "/" : "").append(LS);
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                listFile(sb, subFile, indentation + "  ");
            }
        }
    }

    private File renameOrCopyFileParam(
            final File fileParam,
            final File newLocation,
            final Logger logger) throws IOException {
        if (fileParam == null) {
            return null;
        }
        File result = null;
        final boolean renameResult = FileUtils.renameFile(fileParam, newLocation);
        /*
         * If the rename failed then it could be because the new location is
         * on a different device, for example.  In that case, try copying
         * the file.
         */
        if (renameResult) {
            result = newLocation;
        } else {
            FileUtils.copyFile(fileParam, newLocation);
            result = newLocation;
        }
        return result;
    }
}
