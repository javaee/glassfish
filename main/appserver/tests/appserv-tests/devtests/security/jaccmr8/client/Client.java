/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package org.glassfish.jacc.test.mr8;

import java.net.*;
import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
	private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");
	private static String testSuite = "Security::JACCMR8";
	private static String contextPath = "/jaccmr8";
	
	private String host;
	private String port;
	private String username;
	private String password;
	private String rolename;
	private String otheruser;
	private String ejbmode = "None";

	public static void main(String[] args) {
		stat.addDescription(testSuite);
		Client client = new Client(args);
		client.doTests();
		stat.printSummary();
	}

	public Client(String[] args) {
		host = args[0];
		port = args[1];
		username = args[2];
		password = args[3];
		rolename = args[4];
		otheruser = args[5];
		System.out.println("      Host: " + host);
		System.out.println("      Port: " + port);
		System.out.println("  Username: " + username);
		System.out.println("  Rolename: " + rolename);
		System.out.println("Other User: " + otheruser);
	}

	public void doTests() {
		// Use the stateful EJB inside the servlet
		// The stateful EJB uses annotations to protect the EJB
		ejbmode = "stateful";
		testAnyAuthUser();
		testAnyAuthUserOther();
		testAnyAuthUserNone();
		testDenyUncovered();
		testDenyUncoveredOther();
		testDenyUncoveredNone();
		testStar();
		testStarOther();
		testStarNone();
		testServlet();
		testServletOther();
		testServletNone();
		testAuthUser();
		testAuthUserOther();
		testAuthUserNone();

		// Use the stateless EJB inside the servlet
		// The stateless EJB uses the deployment descriptor to protect the EJB
		// Only repeat tests that actually can invoke the servlet
		ejbmode = "stateless";
		testAnyAuthUser();
		testAnyAuthUserOther();
		testStar();
		testAuthUser();
		testServlet();
		testServletOther();
		testServletNone();
	}

	public void testAnyAuthUser() {
		String servlet = "/anyauthuser";
		String description = servlet+"-"+username+"-"+ejbmode;
		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, 200, username, ejbmode, output);
		if (!success) {
			stat.addStatus(description, stat.FAIL);
			return;
		}

		// Check results in output
		success = checkResults(output,true,true,true,true,true,true);
		if (!success) {
			System.out.println("Incorrect results:" + description);
			stat.addStatus(description, stat.FAIL);
			return;
		}

		stat.addStatus(description, stat.PASS);
	}

	public void testAnyAuthUserOther() {
		String servlet = "/anyauthuser";
		String description = servlet+"-"+otheruser+"-"+ejbmode;
		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, 200, otheruser, ejbmode, output);
		if (!success) {
			stat.addStatus(description, stat.FAIL);
			return;
		}

		// Check results in output
		success = checkResults(output,false,true,true,false,false,true);
		if (!success) {
			System.out.println("Incorrect results:" + description);
			stat.addStatus(description, stat.FAIL);
			return;
		}

		stat.addStatus(description, stat.PASS);
	}

	public void testAnyAuthUserNone() {
		String servlet = "/anyauthuser";
		String description = servlet+"--"+ejbmode;
		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, 401, null, ejbmode, output);
		if (!success) {
			stat.addStatus(description, stat.FAIL);
			return;
		}

		// No results to check!
		stat.addStatus(description, stat.PASS);
	}

	public void testAuthUser() {
		String servlet = "/authuser";
		String description = servlet+"-"+username+"-"+ejbmode;
		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, 200, username, ejbmode, output);
		if (!success) {
			stat.addStatus(description, stat.FAIL);
			return;
		}

		// Check results in output
		success = checkResults(output,true,true,true,true,true,true);
		if (!success) {
			System.out.println("Incorrect results:" + description);
			stat.addStatus(description, stat.FAIL);
			return;
		}

		stat.addStatus(description, stat.PASS);
	}

	public void testAuthUserOther() {
		String servlet = "/authuser";
		String description = servlet+"-"+otheruser+"-"+ejbmode;
		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, 403, otheruser, ejbmode, output);
		if (!success) {
			stat.addStatus(description, stat.FAIL);
			return;
		}

		// No results to check!
		stat.addStatus(description, stat.PASS);
	}

	public void testAuthUserNone() {
		String servlet = "/authuser";
		String description = servlet+"--"+ejbmode;
		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, 401, null, ejbmode, output);
		if (!success) {
			stat.addStatus(description, stat.FAIL);
			return;
		}

		// No results to check!
		stat.addStatus(description, stat.PASS);
	}

	public void testStar() {
		String servlet = "/star";
		String description = servlet+"-"+username+"-"+ejbmode;
		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, 200, username, ejbmode, output);
		if (!success) {
			stat.addStatus(description, stat.FAIL);
			return;
		}

		// Check results in output
		success = checkResults(output,true,true,true,true,true,true);
		if (!success) {
			System.out.println("Incorrect results:" + description);
			stat.addStatus(description, stat.FAIL);
			return;
		}

		stat.addStatus(description, stat.PASS);
	}

	public void testStarOther() {
		String servlet = "/star";
		String description = servlet+"-"+otheruser+"-"+ejbmode;
		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, 403, otheruser, ejbmode, output);
		if (!success) {
			stat.addStatus(description, stat.FAIL);
			return;
		}

		// No results to check!
		stat.addStatus(description, stat.PASS);
	}

	public void testStarNone() {
		String servlet = "/star";
		String description = servlet+"--"+ejbmode;
		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, 401, null, ejbmode, output);
		if (!success) {
			stat.addStatus(description, stat.FAIL);
			return;
		}

		// No results to check!
		stat.addStatus(description, stat.PASS);
	}

	public void testServlet() {
		String servlet = "/servlet";
		String description = servlet+"-"+username+"-"+ejbmode;
		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, 200, username, ejbmode, output);
		if (!success) {
			stat.addStatus(description, stat.FAIL);
			return;
		}

		// Check results in output
		success = checkResults(output,false,false,false,false,false,false);
		if (!success) {
			System.out.println("Incorrect results:" + description);
			stat.addStatus(description, stat.FAIL);
			return;
		}

		stat.addStatus(description, stat.PASS);
	}

	public void testServletOther() {
		String servlet = "/servlet";
		String description = servlet+"-"+otheruser+"-"+ejbmode;
		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, 200, otheruser, ejbmode, output);
		if (!success) {
			stat.addStatus(description, stat.FAIL);
			return;
		}

		// Check results in output
		success = checkResults(output,false,false,false,false,false,false);
		if (!success) {
			System.out.println("Incorrect results:" + description);
			stat.addStatus(description, stat.FAIL);
			return;
		}

		stat.addStatus(description, stat.PASS);
	}

	public void testServletNone() {
		String servlet = "/servlet";
		String description = servlet+"--"+ejbmode;
		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, 200, null, ejbmode, output);
		if (!success) {
			stat.addStatus(description, stat.FAIL);
			return;
		}

		// Check results in output
		success = checkResults(output,false,false,false,false,false,false);
		if (!success) {
			System.out.println("Incorrect results:" + description);
			stat.addStatus(description, stat.FAIL);
			return;
		}

		stat.addStatus(description, stat.PASS);
	}

	public void testDenyUncovered() {
		String servlet = "/denyuncoveredpost";
		String description = servlet+"-"+username+"-"+ejbmode;
		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, 403, username, ejbmode, output);
		if (!success) {
			stat.addStatus(description, stat.FAIL);
			return;
		}

		// No results to check!
		stat.addStatus(description, stat.PASS);
	}

	public void testDenyUncoveredOther() {
		String servlet = "/denyuncoveredpost";
		String description = servlet+"-"+otheruser+"-"+ejbmode;
		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, 403, otheruser, ejbmode, output);
		if (!success) {
			stat.addStatus(description, stat.FAIL);
			return;
		}

		// No results to check!
		stat.addStatus(description, stat.PASS);
	}

	public void testDenyUncoveredNone() {
		String servlet = "/denyuncoveredpost";
		String description = servlet+"--"+ejbmode;
		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, 403, null, ejbmode, output);
		if (!success) {
			stat.addStatus(description, stat.FAIL);
			return;
		}

		// No results to check!
		stat.addStatus(description, stat.PASS);
	}

	// Validate that all the passed in results are as expected
	// If any value is not as expected the overall results are false
	private boolean checkResults(StringBuffer results,
			boolean EJBisCallerInRole, boolean EJBisUserInAnyAuthUserRole,
			boolean EJBInvokeAnyAuthUser, boolean EJBInvokeAuthUser,
			boolean WEBisUserInRole, boolean WEBisUserInAnyAuthUserRole) {
		int index;
		boolean result = true;

		if (EJBisCallerInRole)
			index = results.indexOf("EJB isCallerInRole: true");
		else
			index = results.indexOf("EJB isCallerInRole: false");
		if (index == -1)
			result = false;

		if (!result) return result;

		if (EJBisUserInAnyAuthUserRole)
			index = results.indexOf("EJB isUserInAnyAuthUserRole: true");
		else
			index = results.indexOf("EJB isUserInAnyAuthUserRole: false");
		if (index == -1)
			result = false;

		if (!result) return result;

		if (WEBisUserInRole)
			index = results.indexOf("WEB isUserInRole: true");
		else
			index = results.indexOf("WEB isUserInRole: false");
		if (index == -1)
			result = false;

		if (!result) return result;

		if (WEBisUserInAnyAuthUserRole)
			index = results.indexOf("WEB isUserInAnyAuthUserRole: true");
		else
			index = results.indexOf("WEB isUserInAnyAuthUserRole: false");
		if (index == -1)
			result = false;

		if (!result) return result;

		index = results.indexOf("EJB Invoke AnyAuthUser: Yes");
		if (EJBInvokeAnyAuthUser)
			result = (index != -1);
		else
			result = (index == -1);

		if (!result) return result;

		index = results.indexOf("EJB Invoke AuthUser: Yes");
		if (EJBInvokeAuthUser)
			result = (index != -1);
		else
			result = (index == -1);

		return result;
	}

	private boolean doIndividualTest(String servlet, int code, String user, String mode, StringBuffer output) {
		boolean result = false;
		try {
			int rtncode;
			String url = "http://" + host + ":" + port + contextPath + servlet;

			Hashtable ht = new Hashtable();
			ht.put("mode", URLEncoder.encode(mode,"UTF-8"));
			ht.put("name", URLEncoder.encode(rolename,"UTF-8"));

			System.out.println("\nInvoking servlet at " + url);
			rtncode = invokeServlet(url, ht, user, output);
			System.out.println("The servlet return code: " + rtncode);
			if (rtncode != code) {
				System.out.println("Incorrect return code, expecting: " + code);
			}
			else result = true;
		} catch (Exception ex) {
			System.out.println("Exception: " + ex.toString());
			//ex.printStackTrace();
		}
		return result;
	}

	private int invokeServlet(String url, Hashtable contentHash, String user, StringBuffer output) throws Exception {
		URL u = new URL(url);
		HttpURLConnection c1 = (HttpURLConnection) u.openConnection();
		c1.setAllowUserInteraction(true);
		if ((user != null) && (user.length() > 0)) {
			// Add BASIC header for authentication
			String auth =  user + ":" + password;
			String authEncoded = new sun.misc.BASE64Encoder().encode(auth.getBytes());
			c1.setRequestProperty("Authorization", "Basic " + authEncoded);
		}
		c1.setDoOutput(true);
		c1.setUseCaches(false);

		// get the output stream to POST to.
		DataOutputStream out;
		out = new DataOutputStream(c1.getOutputStream());
		String content = "";

		// Create a single String value to be POSTED from the parameters passed
		// to us. This is done by making "name"="value" pairs for all the keys
		// in the Hashtable passed to us.
		Enumeration e = contentHash.keys();
		boolean first = true;
		while (e.hasMoreElements()) {
			// For each key and value pair in the hashtable
			Object key = e.nextElement();
			Object value = contentHash.get(key);

			// If this is not the first key-value pair in the hashtable,
			// concantenate an "&" sign to the constructed String
			if (!first)
				content += "&";

			// append to a single string. Encode the value portion
			content += (String) key + "=" + URLEncoder.encode((String) value,"UTF-8");

			first = false;
		}

		// Write out the bytes of the content string to the stream.
		out.writeBytes(content);
		out.flush();
		out.close();

		// Connect and get the response code and/or output to verify
		c1.connect();
		int code = c1.getResponseCode();
		if (code == HttpURLConnection.HTTP_OK) {
			InputStream is = null;
			BufferedReader input = null;
			String line = null;
			try {
				is = c1.getInputStream();
				input = new BufferedReader(new InputStreamReader(is));
				while ((line = input.readLine()) != null) {
					output.append(line);
					System.out.println(line);
				}
			}
			finally {
				try { if (is != null) is.close(); }
				catch (Exception exc) {}
				try { if (input != null) input.close(); }
				catch (Exception exc) {}
			}
		}
		return code;
	}
}
