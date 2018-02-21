/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.s1asdev.appclient.appcpath.client;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1asdev.appclient.appcpath.lib.LibUtil;
import java.net.URL;
import javax.sound.midi.SysexMessage;

/**
 *
 * @author tjquinn
 */
public class TestAPPCPATH {
    
    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    /**
     * Creates a new instance of TestAPPCPATH
     */
    public TestAPPCPATH() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.err.println(TestAPPCPATH.class.getClassLoader().getClass().getName());
        int status = 1;
        try {
            stat.addDescription("Testing APPCPATH");
            new TestAPPCPATH().run(args);
            status = 0;
        } catch (Throwable thr) {
            System.err.println("Client detected the following error:");
            thr.printStackTrace();
        } finally {
            stat.printSummary("APPCPATH");
            System.exit(status);
        }
        
    }
    
    private void run(String[] args) throws Throwable {
        /*
         *Use a class in the other jar file.  The APPCPATH env. var.
         *should point to this jar file as the client is run.
         */
        System.err.println("Attempting to instantiate LibUtil...");
        boolean passExpected = (args[0].equalsIgnoreCase("pass"));
        /*
         *args[0] will be either PASS or FAIL, depending on the expected outcome.
         */
        try {
            LibUtil lu = new LibUtil();
            stat.addStatus("APPCPATH test", passExpected ? stat.PASS : stat.FAIL);
        } catch (Throwable thr) {
            stat.addStatus("APPCPATH test", passExpected ? stat.FAIL : stat.PASS);
            throw thr;
        }
}

}
