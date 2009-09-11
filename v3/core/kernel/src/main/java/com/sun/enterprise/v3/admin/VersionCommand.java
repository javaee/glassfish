/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.admin;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.ActionReport.ExitCode;
import org.jvnet.hk2.annotations.Service;
import com.sun.appserv.server.util.Version;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.api.Param;
import org.jvnet.hk2.annotations.Inject;

/**
 * Return the version and build number
 *
 * @author Jerome Dochez
 */
@Service(name="version")
@I18n("version.command")
public class VersionCommand implements AdminCommand {
    
    @Param(optional=true, defaultValue="false", shortName = "v")
    Boolean verbose;
    
    @Inject
    Version version;

    final private static LocalStringManagerImpl strings = new LocalStringManagerImpl(VersionCommand.class);

    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        report.setActionExitCode(ExitCode.SUCCESS);
        StringBuilder vsb = new StringBuilder(strings.getLocalString("version", "Server version: {0}", version.getFullVersion()));
        if (verbose) {
            vsb.append(System.getProperty("line.separator"));
            vsb.append(strings.getLocalString("version.java", "Server's Java Runtime Environment version: {0}", System.getProperty("java.version")));
            vsb.append(System.getProperty("line.separator"));
            vsb.append(strings.getLocalString("version.more.0", "Use the command: 'list-modules' to see more information"));
        }
        report.setMessage(vsb.toString());
    }
    /* Implementation note: Currently (Aug 2008) the --verbose
     * option does not do anything special. Please see:
     * https://glassfish.dev.java.net/servlets/ReadMsg?list=arch&msgNo=21
     * for details. */
}
