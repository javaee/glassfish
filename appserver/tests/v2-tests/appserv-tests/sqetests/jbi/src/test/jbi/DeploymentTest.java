/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2018 Oracle and/or its affiliates. All rights reserved.
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

package test.jbi;

import java.io.File;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;


/** 
 * This class tests deployemnt of a service assembly.
 *
 */
public class DeploymentTest implements JBIQuicklookTest 

{

    
    /**
     * used to store timing data
     */
    private long start, end;
    
    /**
     * success string
     */
    public static String DEPLOYMENT_STARTED = "Started service assembly";
    public static String FAILED = "failed";
    public static String ERROR = "error";
    
    
    public static SimpleReporterAdapter reporter = new SimpleReporterAdapter("appserv-tests"); 

    /**
     * this method is invoked for testing deployment
     */
    public String test(AdminCli adminCli) {
        String status = null;
        try {
            start = System.currentTimeMillis();
 
            testDeployment(adminCli);
            
            status = SimpleReporterAdapter.PASS;
        } catch(final Exception e) {
            status = SimpleReporterAdapter.FAIL;
            throw new RuntimeException(e);
        }
        finally {
            end = System.currentTimeMillis();
        }
        return ( status ) ;
    }

    /**
     * This method returns the name of the class for reporting 
     */
    public String getName() {
        final String name = "Testing deployment";
        return (name);
    }
    
    /**
     * This is the main method that does the real testing
     */
    private void testDeployment(AdminCli adminCli) throws RuntimeException 
    {
        String result = null;     
        File bc1 =  new File(System.getProperty("jbi.archives.dir"), "bc1.zip");
        String bc1Path = bc1.getAbsolutePath();

        File bc2 =  new File(System.getProperty("jbi.archives.dir"), "bc2.zip");
        String bc2Path = bc2.getAbsolutePath();
        
        File sa =  new File(System.getProperty("jbi.archives.dir"), "sa-for-bc1-and-bc2.zip");
        String saPath = sa.getAbsolutePath();
        
        String bc1Name = "manage-binding-1";
        String bc2Name = "manage-binding-2";

        String command = null;

        try {
            //install bc1
            command = "install-jbi-component";
            adminCli.setCommand(command);
            adminCli.setOperand(bc1Path);
            result = adminCli.execute();
            System.out.println(command + ": " + result);
                    
            //install bc2
            command = "install-jbi-component";
            adminCli.setCommand(command);
            adminCli.setOperand(bc2Path);
            result = adminCli.execute();
            System.out.println(command + ": " + result);
            
            //start bc1
            command = "start-jbi-component";
            adminCli.setCommand(command);
            adminCli.setOperand(bc1Name);
            result = adminCli.execute();
            System.out.println(command + ": " + result); 
 
            //start bc2
            command = "start-jbi-component";
            adminCli.setCommand(command);
            adminCli.setOperand(bc2Name);
            result = adminCli.execute();
            System.out.println(command + ": " + result);

            //deploy sa1
            command = "deploy-jbi-service-assembly";
            adminCli.setCommand(command);
            adminCli.setOperand(saPath);
            result = adminCli.execute();
            System.out.println(command + ": " + result);
            
            //start sa1
	    command = "start-jbi-service-assembly";
            adminCli.setCommand(command);
            adminCli.setOperand("esbadmin00089-sa");
            result = adminCli.execute();
            System.out.println(command + ": " + result);
            checkError(command, result);

	    // stop sa1
	    command = "stop-jbi-service-assembly";
	    adminCli.setCommand(command);
	    adminCli.setOperand("esbadmin00089-sa");
	    result = adminCli.execute();
	    System.out.println(command + ": " + result);
            checkError(command, result);

	    // shut down sa1
	    command = "shut-down-jbi-service-assembly";
	    adminCli.setCommand(command);
	    adminCli.setOperand("esbadmin00089-sa");
	    result = adminCli.execute();
	    System.out.println(command + ": " + result);
	    checkError(command, result);

            // undeploy sa1
            command = "undeploy-jbi-service-assembly";
            adminCli.setCommand(command);
            adminCli.setOperand("esbadmin00089-sa");
            result = adminCli.execute();
            System.out.println(command + ": " + result);
            checkError(command, result);

	    // stop bc1
	    command = "stop-jbi-component";
	    adminCli.setCommand(command);
	    adminCli.setOperand(bc1Name);
	    result = adminCli.execute();
            System.out.println(command + ": " + result);
            checkError(command, result);

	    // stop bc2
	    command = "stop-jbi-component";
	    adminCli.setCommand(command);
	    adminCli.setOperand(bc2Name);
	    result = adminCli.execute();
	    System.out.println(command + ": " + result);
            checkError(command, result);

	    // uninstall bc1
	    command = "uninstall-jbi-component";
	    adminCli.setCommand(command);
	    adminCli.setOperand(bc1Name);
	    result = adminCli.execute();
	    System.out.println(command + ": " + result);
            checkError(command, result);

	    // uninstall bc2
	    command = "uninstall-jbi-component";
	    adminCli.setCommand(command);
	    adminCli.setOperand(bc2Name);
	    result = adminCli.execute();
	    System.out.println(command + ": " + result);
            checkError(command, result);

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkError(String command, String result) throws Exception {
	if (result != null && (result.toLowerCase().indexOf(FAILED)!=-1 ||
					result.toLowerCase().indexOf(ERROR)!=-1))
	    throw new Exception(command + " failed: " + result);
    }

    /**
     * This method is used to return the execution time for this test
     */
    public long getExecutionTime() {
        return ( end - start );
    }
}


