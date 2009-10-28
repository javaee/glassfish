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
package com.sun.enterprise.v3.admin;

import com.sun.enterprise.util.StringUtils;
import java.util.*;
import org.glassfish.api.Param;
import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.server.ServerEnvironmentImpl;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.ActionReport;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.PerLookup;
import com.sun.enterprise.universal.Duration;
import org.glassfish.flashlight.provider.ProbeProviderFactory;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Singleton;


/**
 * uptime command
 * Reports on how long the server has been running.
 * 
 */

@Service(name = "mon-test")
@Scoped(Singleton.class)

public class MonTest implements AdminCommand, PostConstruct{
    @Inject
    ServerEnvironmentImpl env;

    @Inject
    protected ProbeProviderFactory probeProviderFactory;

    @Inject
    ProbeClientMediator listenerRegistrar;

    @Param(optional=true, defaultValue="10", primary=true)
    String howmanyString;

    public void execute(AdminCommandContext context) {
        int howmany = 10;
        msg = "";
        try { howmany = Integer.parseInt(howmanyString); } 
        catch(Exception e) { /*ignore*/ }

        final ActionReport report = context.getActionReport();

        try {
            for(int i = 0; i  < howmany; i++) {
                int which = randy.nextInt(3) + 1;

                switch(which) {
                    case 1: fire1(); break;
                    case 2: fire2(); break;
                    case 3: fire3(); break;
                }
            }

            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            msg = "Monitoring Test Here!";
            msg += "howmany= " + howmany;
            report.setMessage(msg);
        }
        catch(Exception e) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            report.setMessage("ERROR! " + e.getMessage() + "\n" + StringUtils.getStackTrace(e));
        }
        finally {
            howmanyString = "10";
        }
    }

    private void fire1() {
        ppt.method1("xxx", 50);
        msg += "fire1\n";
    }

    private void fire2() {
        ppt.method2("xxx", 2, 3, new Date());
        msg += "fire2\n";
    }
    private void fire3() {
         ppt.method3("xxx");
        msg += "fire3\n";
    }

    @Override
    public void postConstruct() {
        try {
            ppt = probeProviderFactory.getProbeProvider(PPTester.class);
            System.out.println("SUCCESS!!  Created PPTester instance!!!");
        }
        catch(Exception e) {
            System.out.println(StringUtils.getStackTrace(e));
            System.out.println("@@@@@@ ERROR registering listener @@@@@@@");
        }

        try {
            listenerRegistrar.registerListener(new PPListener());
        }
        catch(Exception e) {
            System.out.println(StringUtils.getStackTrace(e));
            System.out.println("@@@@@@ ERROR registering listener @@@@@@@");

        }
        
        randy = new Random(System.nanoTime());
    }

    private PPTester ppt = null;
    private Random randy;
    String msg;
}

