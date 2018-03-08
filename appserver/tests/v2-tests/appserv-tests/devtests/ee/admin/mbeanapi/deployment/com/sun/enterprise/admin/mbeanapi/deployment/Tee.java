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

/**
 * @version 1.00 April 1, 2000
 * @author Byron Nevins
 */

package com.sun.enterprise.admin.mbeanapi.deployment;

import java.io.*;


public class Tee extends PrintStream 
{

	/////////////////////////////////////////////////////////////////

	private Tee(PrintStream ps) 
	{
		super(ps);
	}


	/////////////////////////////////////////////////////////////////

	// Starts copying stdout and 
	//stderr to the file f.
	public static void start(String f) throws IOException 
	{
		// Save old settings.
		oldStdout = System.out;
		//oldStderr = System.err;

		// Create/Open logfile.
		logfile = new PrintStream(new BufferedOutputStream(new FileOutputStream(f)));

		// Start redirecting the output.
		System.setOut(new Tee(System.out));
		//System.setErr(new Tee(System.err));
	}


	/////////////////////////////////////////////////////////////////

	// Restores the original settings.
	public static void stop() 
	{
		System.setOut(oldStdout);
		//System.setErr(oldStderr);
		
		try 
		{
			logfile.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}


	/////////////////////////////////////////////////////////////////

	// PrintStream override.
	public void write(int b) 
	{
		try 
		{
			logfile.write(b);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			setError();
		}
		super.write(b);
	}

	/////////////////////////////////////////////////////////////////

	// PrintStream override.
	public void write(byte buf[], int off, int len) 
	{
		try 
		{
			logfile.write(buf, off, len);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			setError();
		}
		super.write(buf, off, len);
	}

	/////////////////////////////////////////////////////////////////

	static OutputStream logfile;
	static PrintStream oldStdout;
	//static PrintStream oldStderr;

	/////////////////////////////////////////////////////////////////

	public static void main(String[] args) 
	{
		try 
		{
			// Start capturing characters 
			//into the log file.
			Tee.start("log.txt");

			// Test it.
			System.out.println(
			"Here's is some stuff to stdout.");
			System.err.println(
			"Here's is some stuff to stderr.");
			System.out.println(
			"Let's throw an exception...");
			new Exception().printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		finally 
		{
			// Stop capturing characters 
			//into the log file 
			// and restore old setup.
			Tee.stop();
		}
	}
}
