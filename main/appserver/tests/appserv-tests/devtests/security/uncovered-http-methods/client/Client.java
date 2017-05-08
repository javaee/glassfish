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

package org.glassfish.jacc.test.uncoveredmethods;

import java.net.*;
import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
	private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");
	private static String testSuite = "Security::UncoveredHTTPMethods";
	private static String contextPathOpen = "/open";
	private static String contextPathDeny = "/deny";
	
	private String host;
	private String port;
	private String username;
	private String password;

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
		System.out.println("      Host: " + host);
		System.out.println("      Port: " + port);
		System.out.println("  Username: " + username);
	}

	public void doTests() {
		testExample1();
		testExample1Put();
		testExample2();
		testExample2Delete();
		testExample3a();
		testExample3aPut();
		testExample3bPost();
		testExample3bDelete();
		testCovered1Post();
		testCovered1Put();
		testCovered2();
		testCovered2Put();
		testCovered3aPost();
		testCovered3aDelete();
		testCovered3b();
		testCovered3bPut();
	}

	public void testExample1() {
		String servlet = "/Example1";
		String descriptionOpen = contextPathOpen+servlet;
		String descriptionDeny = contextPathDeny+servlet;

		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, null, 302, username, contextPathOpen, output);
		int index = output.indexOf("https://");
		if (success && (index != -1)) {
			stat.addStatus(descriptionOpen, stat.PASS);
		}
		else stat.addStatus(descriptionOpen, stat.FAIL);

		output = new StringBuffer();
		success = doIndividualTest(servlet, null, 302, username, contextPathDeny, output);
		if (success) stat.addStatus(descriptionDeny, stat.PASS);
		else stat.addStatus(descriptionDeny, stat.FAIL);
	}

	public void testExample1Put() {
		String servlet = "/Example1";
		String method = "PUT";
		String descriptionOpen = contextPathOpen+servlet+"-"+method;
		String descriptionDeny = contextPathDeny+servlet+"-"+method;

		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, method, 200, username, contextPathOpen, output);
		if (success) {
			stat.addStatus(descriptionOpen, stat.PASS);
		}
		else stat.addStatus(descriptionOpen, stat.FAIL);

		output = new StringBuffer();
		success = doIndividualTest(servlet, method, 403, username, contextPathDeny, output);
		if (success) stat.addStatus(descriptionDeny, stat.PASS);
		else stat.addStatus(descriptionDeny, stat.FAIL);
	}

	public void testExample2() {
		String servlet = "/Example2";
		String descriptionOpen = contextPathOpen+servlet;
		String descriptionDeny = contextPathDeny+servlet;

		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, null, 200, username, contextPathOpen, output);
		if (success) {
			stat.addStatus(descriptionOpen, stat.PASS);
		}
		else stat.addStatus(descriptionOpen, stat.FAIL);

		output = new StringBuffer();
		success = doIndividualTest(servlet, null, 403, username, contextPathDeny, output);
		if (success) stat.addStatus(descriptionDeny, stat.PASS);
		else stat.addStatus(descriptionDeny, stat.FAIL);
	}

	public void testExample2Delete() {
		String servlet = "/Example2";
		String method = "DELETE";
		String descriptionOpen = contextPathOpen+servlet+"-"+method;
		String descriptionDeny = contextPathDeny+servlet+"-"+method;

		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, method, 403, username, contextPathOpen, output);
		if (success) {
			stat.addStatus(descriptionOpen, stat.PASS);
		}
		else stat.addStatus(descriptionOpen, stat.FAIL);

		output = new StringBuffer();
		success = doIndividualTest(servlet, method, 403, username, contextPathDeny, output);
		if (success) stat.addStatus(descriptionDeny, stat.PASS);
		else stat.addStatus(descriptionDeny, stat.FAIL);
	}

	public void testExample3a() {
		String servlet = "/Example3a";
		String descriptionOpen = contextPathOpen+servlet;
		String descriptionDeny = contextPathDeny+servlet;

		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, null, 200, username, contextPathOpen, output);
		if (success) {
			stat.addStatus(descriptionOpen, stat.PASS);
		}
		else stat.addStatus(descriptionOpen, stat.FAIL);

		output = new StringBuffer();
		success = doIndividualTest(servlet, null, 200, username, contextPathDeny, output);
		if (success) stat.addStatus(descriptionDeny, stat.PASS);
		else stat.addStatus(descriptionDeny, stat.FAIL);
	}

	public void testExample3aPut() {
		String servlet = "/Example3a";
		String method = "PUT";
		String descriptionOpen = contextPathOpen+servlet+"-"+method;
		String descriptionDeny = contextPathDeny+servlet+"-"+method;

		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, method, 200, username, contextPathOpen, output);
		if (success) {
			stat.addStatus(descriptionOpen, stat.PASS);
		}
		else stat.addStatus(descriptionOpen, stat.FAIL);

		output = new StringBuffer();
		success = doIndividualTest(servlet, method, 403, username, contextPathDeny, output);
		if (success) stat.addStatus(descriptionDeny, stat.PASS);
		else stat.addStatus(descriptionDeny, stat.FAIL);
	}

	public void testExample3bPost() {
		String servlet = "/Example3b";
		String method = "POST";
		String descriptionOpen = contextPathOpen+servlet+"-"+method;
		String descriptionDeny = contextPathDeny+servlet+"-"+method;

		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, method, 200, username, contextPathOpen, output);
		if (success) {
			stat.addStatus(descriptionOpen, stat.PASS);
		}
		else stat.addStatus(descriptionOpen, stat.FAIL);

		output = new StringBuffer();
		success = doIndividualTest(servlet, method, 200, username, contextPathDeny, output);
		if (success) stat.addStatus(descriptionDeny, stat.PASS);
		else stat.addStatus(descriptionDeny, stat.FAIL);
	}

	public void testExample3bDelete() {
		String servlet = "/Example3b";
		String method = "DELETE";
		String descriptionOpen = contextPathOpen+servlet+"-"+method;
		String descriptionDeny = contextPathDeny+servlet+"-"+method;

		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, method, 200, username, contextPathOpen, output);
		if (success) {
			stat.addStatus(descriptionOpen, stat.PASS);
		}
		else stat.addStatus(descriptionOpen, stat.FAIL);

		output = new StringBuffer();
		success = doIndividualTest(servlet, method, 403, username, contextPathDeny, output);
		if (success) stat.addStatus(descriptionDeny, stat.PASS);
		else stat.addStatus(descriptionDeny, stat.FAIL);
	}

	public void testCovered1Post() {
		String servlet = "/Covered1";
		String method = "POST";
		String descriptionOpen = contextPathOpen+servlet+"-"+method;
		String descriptionDeny = contextPathDeny+servlet+"-"+method;

		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, method, 302, username, contextPathOpen, output);
		int index = output.indexOf("https://");
		if (success && (index != -1)) {
			stat.addStatus(descriptionOpen, stat.PASS);
		}
		else stat.addStatus(descriptionOpen, stat.FAIL);

		output = new StringBuffer();
		success = doIndividualTest(servlet, method, 302, username, contextPathDeny, output);
		if (success) stat.addStatus(descriptionDeny, stat.PASS);
		else stat.addStatus(descriptionDeny, stat.FAIL);
	}

	public void testCovered1Put() {
		String servlet = "/Covered1";
		String method = "PUT";
		String descriptionOpen = contextPathOpen+servlet+"-"+method;
		String descriptionDeny = contextPathDeny+servlet+"-"+method;

		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, method, 302, username, contextPathOpen, output);
		int index = output.indexOf("https://");
		if (success && (index != -1)) {
			stat.addStatus(descriptionOpen, stat.PASS);
		}
		else stat.addStatus(descriptionOpen, stat.FAIL);

		output = new StringBuffer();
		success = doIndividualTest(servlet, method, 302, username, contextPathDeny, output);
		if (success) stat.addStatus(descriptionDeny, stat.PASS);
		else stat.addStatus(descriptionDeny, stat.FAIL);
	}

	public void testCovered2() {
		String servlet = "/Covered2";
		String descriptionOpen = contextPathOpen+servlet;
		String descriptionDeny = contextPathDeny+servlet;

		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, null, 302, username, contextPathOpen, output);
		int index = output.indexOf("https://");
		if (success && (index != -1)) {
			stat.addStatus(descriptionOpen, stat.PASS);
		}
		else stat.addStatus(descriptionOpen, stat.FAIL);

		output = new StringBuffer();
		success = doIndividualTest(servlet, null, 302, username, contextPathDeny, output);
		if (success) stat.addStatus(descriptionDeny, stat.PASS);
		else stat.addStatus(descriptionDeny, stat.FAIL);
	}

	public void testCovered2Put() {
		String servlet = "/Covered2";
		String method = "PUT";
		String descriptionOpen = contextPathOpen+servlet+"-"+method;
		String descriptionDeny = contextPathDeny+servlet+"-"+method;

		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, method, 403, username, contextPathOpen, output);
		if (success) {
			stat.addStatus(descriptionOpen, stat.PASS);
		}
		else stat.addStatus(descriptionOpen, stat.FAIL);

		output = new StringBuffer();
		success = doIndividualTest(servlet, method, 403, username, contextPathDeny, output);
		if (success) stat.addStatus(descriptionDeny, stat.PASS);
		else stat.addStatus(descriptionDeny, stat.FAIL);
	}

	public void testCovered3aPost() {
		String servlet = "/Covered3a";
		String method = "POST";
		String descriptionOpen = contextPathOpen+servlet+"-"+method;
		String descriptionDeny = contextPathDeny+servlet+"-"+method;

		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, method, 200, username, contextPathOpen, output);
		if (success) {
			stat.addStatus(descriptionOpen, stat.PASS);
		}
		else stat.addStatus(descriptionOpen, stat.FAIL);

		output = new StringBuffer();
		success = doIndividualTest(servlet, method, 200, username, contextPathDeny, output);
		if (success) stat.addStatus(descriptionDeny, stat.PASS);
		else stat.addStatus(descriptionDeny, stat.FAIL);
	}

	public void testCovered3aDelete() {
		String servlet = "/Covered3a";
		String method = "DELETE";
		String descriptionOpen = contextPathOpen+servlet+"-"+method;
		String descriptionDeny = contextPathDeny+servlet+"-"+method;

		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, method, 200, username, contextPathOpen, output);
		if (success) {
			stat.addStatus(descriptionOpen, stat.PASS);
		}
		else stat.addStatus(descriptionOpen, stat.FAIL);

		output = new StringBuffer();
		success = doIndividualTest(servlet, method, 200, username, contextPathDeny, output);
		if (success) stat.addStatus(descriptionDeny, stat.PASS);
		else stat.addStatus(descriptionDeny, stat.FAIL);
	}

	public void testCovered3b() {
		String servlet = "/Covered3b";
		String descriptionOpen = contextPathOpen+servlet;
		String descriptionDeny = contextPathDeny+servlet;

		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, null, 302, username, contextPathOpen, output);
		int index = output.indexOf("https://");
		if (success && (index != -1)) {
			stat.addStatus(descriptionOpen, stat.PASS);
		}
		else stat.addStatus(descriptionOpen, stat.FAIL);

		output = new StringBuffer();
		success = doIndividualTest(servlet, null, 302, username, contextPathDeny, output);
		if (success) stat.addStatus(descriptionDeny, stat.PASS);
		else stat.addStatus(descriptionDeny, stat.FAIL);
	}

	public void testCovered3bPut() {
		String servlet = "/Covered3b";
		String method = "PUT";
		String descriptionOpen = contextPathOpen+servlet+"-"+method;
		String descriptionDeny = contextPathDeny+servlet+"-"+method;

		StringBuffer output = new StringBuffer();
		boolean success = doIndividualTest(servlet, method, 302, username, contextPathOpen, output);
		int index = output.indexOf("https://");
		if (success && (index != -1)) {
			stat.addStatus(descriptionOpen, stat.PASS);
		}
		else stat.addStatus(descriptionOpen, stat.FAIL);

		output = new StringBuffer();
		success = doIndividualTest(servlet, method, 302, username, contextPathDeny, output);
		if (success) stat.addStatus(descriptionDeny, stat.PASS);
		else stat.addStatus(descriptionDeny, stat.FAIL);
	}

	private boolean doIndividualTest(String servlet, String method, int code, String user, String context, StringBuffer output) {
		boolean result = false;
		try {
			int rtncode;
			String url = "http://" + host + ":" + port + context + servlet;
			System.out.println("\nInvoking servlet at " + url);
			rtncode = invokeServlet(url, method, user, output);
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

	private int invokeServlet(String url, String method, String user, StringBuffer output) throws Exception {
		String httpMethod = "GET";
		if ((method != null) && (method.length() > 0)) httpMethod = method;
		System.out.println("Invoking servlet with HTTP method: " + httpMethod);
		URL u = new URL(url);
		HttpURLConnection c1 = (HttpURLConnection) u.openConnection();
		c1.setRequestMethod(httpMethod);
		if ((user != null) && (user.length() > 0)) {
			// Add BASIC header for authentication
			String auth =  user + ":" + password;
			String authEncoded = new sun.misc.BASE64Encoder().encode(auth.getBytes());
			c1.setRequestProperty("Authorization", "Basic " + authEncoded);
		}
		c1.setUseCaches(false);

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
					//System.out.println(line);
				}
			}
			finally {
				try { if (is != null) is.close(); }
				catch (Exception exc) {}
				try { if (input != null) input.close(); }
				catch (Exception exc) {}
			}
		}
		else if (code == HttpURLConnection.HTTP_MOVED_TEMP) {
			URL redir = new URL(c1.getHeaderField("Location"));
			String line = "Servlet redirected to: " + redir.toString();
			output.append(line);
			System.out.println(line);
		}
		return code;
	}
}
