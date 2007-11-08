/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

package com.sun.s1asdev.appclient.appcpath.client;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1asdev.appclient.appcpath.lib.LibUtil;

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
