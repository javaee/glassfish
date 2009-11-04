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
@Scoped(PerLookup.class)

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
        fire1=fire2=fire3=0;
        start = System.nanoTime();
        try { howmany = Integer.parseInt(howmanyString); } 
        catch(Exception e) { /*ignore*/ }

        final ActionReport report = context.getActionReport();

        try {

            if(ppt == null || listenerOK == false ) {
                throw new RuntimeException("Registration/Listener Error");
            }

            for(int i = 0; i  < howmany; i++) {
                int which = randy.nextInt(3) + 1;

                switch(which) {
                    case 1: fire4(); break;
                    case 2: fire5(); break;
                    case 3: fire4(); break;
                }
            }

            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            report.setMessage(createMessage());
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
/*
    private void fire1() {
        ppt.method1("xxx", 50);
        ++fire1;
    }

    private void fire2() {
        ppt.method2("xxx", 2, 3);
        ++fire2;
    }
    private void fire3() {
        ppt.method3("xxx");
        ++fire3;
    }
 * */
    
    private void fire4() {
        if(ppt != null) {
            ppt.overload(50);
            ++fire4;
        }
    }

    private void fire5() {
        if(ppt != null) {
            ppt.overload("fire5!!");
            ++fire5;
        }
    }

    private String createMessage() {
        long time = System.nanoTime() - start;
        time /= 1000;   // microseconds

        return "mon-test successful with these calls:" +
        "method1 fired " + fire1 + "times\n" +
        "method2 fired " + fire2 + "times\n" +
        "method3 fired " + fire3 + "times\n" +
        "method4 fired " + fire4 + "times\n" +
        "method5 fired " + fire5 + "times\n" +
         "\n*** Time = " + time + " microseconds ***";
    }
    @Override
    public void postConstruct() {
        randy = new Random(System.nanoTime());

        try {
            ppt = probeProviderFactory.getProbeProvider(PPTester.class);
            System.out.println("SUCCESS!!  Created PPTester instance!!!");
        }
        catch(Exception e) {
            System.out.println(StringUtils.getStackTrace(e));
            System.out.println("@@@@@@ ERROR registering listener @@@@@@@");
            return;
        }
        try {
            listenerRegistrar.registerListener(new PPListener());
            listenerOK = true;
        }

        catch(Exception e) {
            System.out.println(StringUtils.getStackTrace(e));
            System.out.println("@@@@@@ ERROR registering listener @@@@@@@");
        }
    }

    private PPTester ppt = null;
    private Random randy;
    private static boolean listenerOK = false;

    String msg;
    int fire1;
    int fire2;
    int fire3;
    int fire4;
    int fire5;
    long start;
}

