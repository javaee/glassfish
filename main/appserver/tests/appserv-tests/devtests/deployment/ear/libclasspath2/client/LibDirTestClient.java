/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2017 Oracle and/or its affiliates. All rights reserved.
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

package libclasspath2.client;

import java.io.IOException;
import javax.ejb.EJB;
import libclasspath2.ResourceHelper;
import libclasspath2.ejb.LookupSBRemote;

/**
 *
 * @author tjquinn
 */
public class LibDirTestClient {

    private static @EJB() LookupSBRemote lookup;
    
    /** Creates a new instance of LibDirTestClient */
    public LibDirTestClient() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new LibDirTestClient().run(args);
        } catch (Throwable thr) {
            thr.printStackTrace(System.err);
            System.exit(-1);
        }
    }
    
    private void run(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("result=-1");
            System.out.println("note=You must specify the expected result string as the command-line argument");
            System.exit(1);
        }
        String expected = args[0];

        /*
         *Ask the EJB to find the required resources and property values.
         */
        ResourceHelper.Result serverResult = lookup.runTests(args, ResourceHelper.TestType.SERVER);
        
        /*
         *Now try to get the same results on the client side.
         */
        StringBuilder clientResults = new StringBuilder();
        ResourceHelper.Result clientResult = ResourceHelper.checkAll(args, ResourceHelper.TestType.CLIENT);
        
        if (serverResult.getResult() && clientResult.getResult()) {
            System.out.println("result=0");
            System.out.println("note=Received expected results");
        } else {
            System.out.println("result=-1");
            dumpResults(serverResult.getResult(), serverResult.getResults().toString(), "server");
            dumpResults(clientResult.getResult(), clientResult.getResults().toString(), "client");
            System.exit(1);
        }
    }

    private void dumpResults(boolean result, String results, String whereDetected) {
        if ( ! result) {
            System.out.println("note=Error(s) on the " + whereDetected + ":");
            for (String error : results.split("@")) {
                System.out.println("note=  " + error);
            }
            System.out.println("note=");
        }
        
    }
}
