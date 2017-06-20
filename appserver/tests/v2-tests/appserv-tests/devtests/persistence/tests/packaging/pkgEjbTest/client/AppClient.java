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

package client;

import javax.ejb.*;

import java.util.Collection;
import java.util.List;

import ejb.Test;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import java.util.*;
import java.text.*;


public class AppClient {

    @EJB(name="ejb/Test") 
    private static Test sb;
    private static String testSuiteID;
    private static SimpleReporterAdapter status =
        new SimpleReporterAdapter("appserv-tests");


    public static void main(String[] args) {
	System.out.println("args ->"+ args);
	System.out.println("args0 ->"+ args[0]);
	testSuiteID = args[0];	
        AppClient test = new AppClient();
	status.addDescription("Testing  JPA packaging scenarious.."+testSuiteID);
	test.runTest();
	status.printSummary(testSuiteID);
    }


    public void runTest() {

	
        // Persist all entities
	String testInsert = sb.testInsert();
        System.out.println("Inserting Customer and Orders... " + testInsert);
	if("OK".equals(testInsert)) {
		status.addStatus(testSuiteID + "-InsertCustomer", status.PASS);
	} else {
		status.addStatus(testSuiteID + "-InsertCustomer", status.FAIL);
        }

	String verInsert = sb.verifyInsert();
        // Test query and navigation
        System.out.println("Verifying that all are inserted... " + verInsert);
	if("OK".equals(verInsert)) {
                status.addStatus(testSuiteID + "-VerifyCustomerInsert", status.PASS);
        } else {
                status.addStatus(testSuiteID + "-VerifyCustomerInsert", status.FAIL);
        }

        // Get a detached instance 
        String c = "Joe Smith";
	String testDelete = sb.testDelete(c);

        // Remove all entities
        System.out.println("Removing all... " + testDelete);
	if("OK".equals(testDelete)) {
                status.addStatus(testSuiteID + "-DeleteCustomer", status.PASS);
        } else {
                status.addStatus(testSuiteID + "-DeleteCustomer", status.FAIL);
        }

	String verDelete = sb.verifyDelete();
        // Query the results
        System.out.println("Verifying that all are removed... " + verDelete);
	if("OK".equals(verDelete)) {
                status.addStatus(testSuiteID + "-VerifyDeleteCustomer", status.PASS);
        } else {
                status.addStatus(testSuiteID + "-VerifyDeleteCustomer", status.FAIL);
        }

    }
}
