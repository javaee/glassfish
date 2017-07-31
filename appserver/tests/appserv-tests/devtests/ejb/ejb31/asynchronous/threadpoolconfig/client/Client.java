/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package test;

import javax.ejb.*;
import javax.annotation.*;
import javax.naming.*;
import java.util.concurrent.*;
import java.util.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");
    private static String appName;
    private static int numOfInvocations = 50;
    private static int maxPoolSize = 32;
    private static String threadNamePrefix = "__ejb-thread-pool";

    public static void main(String args[]) throws Exception {
	appName = args[0]; 
        if(args.length >= 2) {
            try {
                numOfInvocations = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {  //ignore
            }
        }
        if(args.length >= 3) {
            try {
                maxPoolSize = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {  //ignore
            }
        }
	stat.addDescription(appName);
	Client client = new Client();       
        client.doTest();	
        stat.printSummary(appName + "ID");
    }

    public void doTest() throws Exception {
        boolean failed = false;
        InitialContext ic = new InitialContext();
        Hello helloBean = (Hello) ic.lookup("java:global/" + appName + "/HelloBean");

        List<Future<String>> results = new ArrayList<Future<String>>();
        List<String> acceptableThreadNames = new ArrayList<String>();

        for(int i = 1; i <= maxPoolSize; i++) {
            acceptableThreadNames.add(threadNamePrefix + i);
        }
        for(int i = 0; i < numOfInvocations; i++) {
            results.add(helloBean.getThreadNameId());
        }
        
        for(Future<String> f : results) {
            String s = f.get();
            String threadName = s.split(" ")[0];
            if(acceptableThreadNames.contains(threadName)) {
                System.out.println("Thread name is in range: " + s);
            } else {
                failed = true;
                System.out.println("Thread name is NOT in range: " + s);
            }
        }
        System.out.println("Number of results: " + results.size());
        System.out.println("All " + acceptableThreadNames.size() + 
            " acceptable thread names: " + acceptableThreadNames);
        stat.addStatus(appName, (failed ? stat.FAIL: stat.PASS));
    }
}
