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

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.ActionReport.ExitCode;


import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.management.mbeanserver.GlassfishMBeanServerFactory;

/**
 * Return the version and build number
 *
 * @author llc
 */
@Service(name="amx")
@I18n("amx.command")
// perhaps scope should be persistent if this command is to initialize
@Scope(PerLookup.class)
public class AMXCommand implements AdminCommand {
    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(AMXCommand.class);
    
//@Inject(name=GlassfishMBeanServerFactory.GLASSFISH_MBEANSERVER)
    private  MBeanServer mMBeanServer;
    
    public AMXCommand() {
        System.out.println( "AMXCommand.AMXCommand" );
        mMBeanServer = ManagementFactory.getPlatformMBeanServer();
        
        try {
           GlassfishMBeanServerFactory.getMBeanServer();
        }
        catch ( Throwable t ) {
            System.out.println( t );
        }
    
        //mMBeanServer = ManagementFactory.getPlatformMBeanServer();
    }
    
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        report.setActionExitCode(ExitCode.SUCCESS);
        report.setMessage( "AMXCommand " + localStrings.getLocalString("amx.command","amx") +
            " executed successfully at: " + new java.util.Date() );
    }
}






