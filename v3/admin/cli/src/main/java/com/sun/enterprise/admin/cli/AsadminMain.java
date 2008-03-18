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
 */
package com.sun.enterprise.admin.cli;

import com.sun.enterprise.cli.framework.*;

/**
 * my v3 main, basically some throw away code
 */
public class AsadminMain {

    public static void main(String[] args) {
        try {
            new com.sun.enterprise.cli.framework.CLIMain().invokeCommand(args);
        }
        catch (InvalidCommandException ice) {
            if (TRACE) {
                System.out.println("REMOTE COMMAND!!!");
            }
            RemoteCommand rc = RemoteCommand.getInstance();
            rc.handleRemoteCommand(args);
        }
        catch(NoClassDefFoundError ncdfe)
        {
            CLILogger.getInstance().printError(ncdfe.toString());
            System.exit(1);
        }
        catch (Throwable ex) {
            CLILogger.getInstance().printExceptionStackTrace(ex);
            CLILogger.getInstance().printError(ex.getLocalizedMessage());
            System.exit(1);
        }
    }
    public static final boolean TRACE = Boolean.getBoolean("trace");

    private final static boolean foundClass(String s) {
        try {
            Class.forName(s);
            return true;
        }
        catch (Throwable t) {
            System.out.println("Can not find class: " + s);
            return false;
        }
    }
    private final static String[] requiredClassnames =
            {
        // one from launcher jar        
        "com.sun.enterprise.admin.launcher.GFLauncher",
        // one from universal jar
        "com.sun.enterprise.universal.xml.MiniXmlParser",
        // one from cli-framework jar
        "com.sun.enterprise.cli.framework.CLIMain",
        // one from glassfish bootstrap jar
        "com.sun.enterprise.glassfish.bootstrap.Main",
        // one from stax-api
        "javax.xml.stream.XMLInputFactory",
        // one from server-mgmt
        "com.sun.enterprise.admin.servermgmt.RepositoryException",
        // one from common-utils
        "com.sun.enterprise.util.net.NetUtils",
        // one from admin/util
        "com.sun.enterprise.admin.util.TokenValueSet",
        // here's one that server-mgmt is dependent on
        "com.sun.enterprise.security.auth.realm.file.FileRealm",
        // dol
        "com.sun.enterprise.deployment.PrincipalImpl",
        // kernel
        "com.sun.appserv.server.util.Version",
    };
    static {
        // check RIGHT NOW to make sure all the classes we need are
        // available
        long start = System.currentTimeMillis();
        boolean gotError = false;
        for (String s : requiredClassnames) {
            if(!foundClass(s))
                gotError = true;
        }
        // final test -- see if sjsxp is available
        try {
            javax.xml.stream.XMLInputFactory.newInstance().getXMLReporter();
        }
        catch(Throwable t) {
            gotError = true;
            System.out.println("Can't access STAX classes");
        }
        if(gotError) {
            // messages already sent to stdout...
            System.exit(1);
        }
        long stop = System.currentTimeMillis();
        System.out.println("Time to pre-load classes = " + (stop-start) + " msec");
    }
}


