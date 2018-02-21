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

package com.sun.enterprise.admin.mbeanapi.deployment;

import java.io.*;
import java.net.*;
import java.util.*;


/**
 * This class will deploy and undeploy every app found in the samples directory.
 * It will discover the name of every cluster and stand-alone instance and deploy
 * and undeploy all the samples to each.
 * In order to run the test, you should create the appropriate clusters and instances
 * and have a node-agent running.
 */

public class DeploymentTestsAuto
{
	public static void main(String[] args)
	{
		if(args.length > 0 && args[0].equals("ant"))
			calledFromAnt = true;
		
		bold("Deployment Tests Started", System.err);
		caller = System.getProperty("user.name");
		
		if(caller == null)
			caller = "unknown";
		
		DeploymentTestsAuto dta = null;
		
		try
		{
			dta = new DeploymentTestsAuto();
			dta.run();
			bold("RESULTS in " + dta.reportFile.getPath(), System.err);
			printErr("\n\n\n\n");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			usage();
		}
		finally
		{
			Tee.stop();
		}
		System.exit(0);
	}
	
	//////////////////////////////////////////////////////////////////////////
	
	public DeploymentTestsAuto() throws DeploymentTestsException
	{
		start = new Date();
		try
		{
			propertiesFile = Utils.safeGetCanonicalFile(new File(PROPERTIES_FILE));
			
			if(!propertiesFile.exists() && caller.equals("bnevins"))
				propertiesFile = Utils.safeGetCanonicalFile(new File("C:/jwsee/appserv-tests/devtests/ee/admin/mbeanapi/deployment/" + PROPERTIES_FILE));
			
			if(!propertiesFile.exists())
				throw new DeploymentTestsException("No properties file.  Expected it here: " +
				propertiesFile.getPath());
			
			printErr("Properties File: " + propertiesFile.getPath());
			printErr("");
			load();
			setupTee();
			getConstantArgs();
			getTargetList();
			getSampleList();
			getTestList();
			getOptions();
		}
		catch(DeploymentTestsException dte)
		{
			throw dte;
		}
		catch(Exception e)
		{
			throw new DeploymentTestsException("Unknown Exception caught in DeploymentTestsAuto.DeploymentTestsAuto", e);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public void run()
	{
		// note: NOW all exceptions are documented internally and
		// the run continues...
		
		for(Iterator it = tests.iterator(); it.hasNext(); )
		{
			Test test = (Test)it.next();
			
			try
			{
				DeployedAppInspector inspector = new DeployedAppInspector(
					test.target.ccmd, test.target.name);

				if(inspector.isDeployed(test.sample.name))
				{
					test.addComment("App was previously deployed before the test.  Undeploying before testing.\n");

					undeploy(test); 
					inspector.refresh();
					if(inspector.isDeployed(test.sample.name))
					{
						test.addComment("Could not undeploy App.  Aborted testing of this App.");
						break;
					}
				}
				
				deploy(test);
				inspector.refresh();
				
				if(!inspector.isDeployed(test.sample.name))
				{
					test.addComment("Inspector reported that App was not deployed.");
					break;
				}
				
				undeploy(test);
				inspector.refresh();

				if(inspector.isDeployed(test.sample.name))
				{
					test.addComment("Inspector reported that App was not undeployed.");
					break;
				}
				test.addComment("Successfully deployed and undeployed app");
				test.passed = true;
			}
			catch(DeploymentTestsException dte)
			{
				test.add(dte);
				continue;
			}
		}
		
		report();
	}
	
	///////////////////////////////////////////////////////////////////////////

	private void report()
	{
		end = new Date();
		long msec = end.getTime() - start.getTime();
		double sec = (double)msec / 1000.0;

		
		print(stars);
		bold("Test Results", System.out);
		print(stars);
		print("");

		print("Time Started: " + start);
		print("Time Ended:   " + end);
		print("Total Time:   " + sec + " seconds");
		print("");
		
		bold("Summary", System.out);
		print("");
		
		int numTests = tests.size();
		int numPassed = getNumPassed();
		int numFailed = numTests - numPassed;
		print("Total Tests: " + numTests);
		print("Passed:      " + numPassed);
		print("Failed:      " + numFailed);
		print("");
		
		bold("Details", System.out);
		print("");

		int which = 1;
		
		for(Iterator it = tests.iterator(); it.hasNext(); ++which)
		{
			Test test = (Test)it.next();
			
			print("Test " + which);
			print("");
			print(test.toString());
			print(stars);
			print("");
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void deploy(Test test) throws DeploymentTestsException
	{
		bold("Deploy " + test.sample.name + " to " + test.target.name, System.err);
		
		ConnectCmd ccmd = test.target.ccmd;
		DeployCmd deployCmd = cmdFactory.createDeployCmd(
			test.sample.file.getPath(),
			test.sample.name,
			null, //context-root
			true, // enable
			test.target.name);

		Cmd pipe = new PipeCmd(ccmd, deployCmd);
        
		try
		{
			pipe.execute();		
		}
		catch(Exception e)
		{
			throw new DeploymentTestsException("Error deploying.", e);
		}
		
		if(pauseAfterDeploy)
		{
			// if this program is called from Ant -- stdin won't work.
			// So use a dialog box...
			if(calledFromAnt)
				Utils.messageBox("Just deployed " + test.sample.name + "\nPress OK when ready to undeploy." , "Deployment Tests Pause");
			else
				Console.readLine(stars + "\n" + 
					boldify("Pausing after Deploy.  Hit Return to continue...") + 
					"\n" + stars + "\n");
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void undeploy(Test test) throws DeploymentTestsException
	{
		bold("UnDeploy " + test.sample.name + " from " + test.target.name, System.err);
		ConnectCmd ccmd = test.target.ccmd;
		UndeployCmd undeployCmd = cmdFactory.createUndeployCmd(
			test.sample.name,
			test.target.name);

		Cmd pipe = new PipeCmd(ccmd, undeployCmd);
        
		try
		{
			pipe.execute();		
		}
		catch(Exception e)
		{
			throw new DeploymentTestsException("Error undeploying.", e);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void getConstantArgs() throws DeploymentTestsException
	{
		phup = new Phup(props);
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void getTargetList() throws DeploymentTestsException
	{
		String err = "No targets specified in properties file.  Usage \"targets=foo,goo,hoo\"";
		String s = props.getProperty("targets");
		
		if(!Utils.ok(s))
			throw new DeploymentTestsException(err);
		
		String[] ss = s.split(",");
		
		if(ss == null || ss.length <= 0)
			throw new DeploymentTestsException(err);
		
		Set targetNames = new HashSet();
		//targetNames.add("server");
		
		for(int i = 0; i < ss.length; i++)
			targetNames.add(ss[i]);
		
		targets = new ArrayList();
		
		for(Iterator it = targetNames.iterator(); it.hasNext(); )
		{
			String targetName = (String)it.next();
			ConnectCmd ccmd = cmdFactory.createConnectCmd(phup);
			try
			{
				// note: it does NOT actually connect yet...
				ccmd.execute();
			}
			catch(Exception e)
			{
				throw new DeploymentTestsException("Couldn't connect to target: " + targetName, e);
			}
			
			targets.add(new Target(targetName, ccmd));
		}
		
		printErr("Targets:");
		int which = 1;
		for(Iterator it = targetNames.iterator(); it.hasNext(); ++which)
			printErr("   " + which + " " + it.next());
		
		printErr("");
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void getSampleList() throws DeploymentTestsException
	{
		SampleGetter getter = new SampleGetter(props);
		samples = getter.getSampleList();
		printErr("Samples:");
		
		int which = 1;
		for(Iterator it = samples.iterator(); it.hasNext(); ++which)
			printErr("   " + which + " " + ((Utils.Sample)it.next()).name );
		
		printErr("");	
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void getTestList()// throws DeploymentTestsException
	{
		tests = new ArrayList();
		
		for(Iterator itt = targets.iterator(); itt.hasNext(); )
		{
			Target target = (Target)itt.next();
			
			for(Iterator its = samples.iterator(); its.hasNext(); )
			{
				Utils.Sample sample = (Utils.Sample)its.next();
				tests.add(new Test(sample, target));
			}
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void getOptions()
	{
		String s = props.getProperty("pauseAfterDeploy");
		
		if(Utils.ok(s) && s.toLowerCase().equals("true"))
			pauseAfterDeploy = true;
	}

	//////////////////////////////////////////////////////////////////////////
	
	private void load() throws IOException
	{
		InputStream in = new FileInputStream(propertiesFile);
		props.load(in);
		in.close();
	}

	//////////////////////////////////////////////////////////////////////////
	
	private static void print(String s)
	{
		System.out.println(s);
	}
	
	//////////////////////////////////////////////////////////////////////////
	
	private static void printErr(String s)
	{
		System.err.println(s);
	}
	
	//////////////////////////////////////////////////////////////////////////
	
	private static void bold(String s, PrintStream out)
	{
		String message = boldify(s);
			
		out.println(stars);
		out.println(message);
		out.println(stars);
	}
	
	
	//////////////////////////////////////////////////////////////////////////
	
	private static String boldify(String s)
	{
		String message = s;
		int len = (s == null ? 0 : s.length());
		
		if(len < 60)
		{
			message = "     " + message + "     ";
			len += 10;
			
			if(len % 2 != 0)
			{
				message += " ";
				++len;
			}
			
			int numStars = (80 - len) / 2;
			StringBuffer sb = new StringBuffer();
			
			for(int i = 0; i < numStars; i++)
			{
				sb.append('*');
			}
			message = sb.toString() + message + sb.toString();
		}
		
		return message;
	}
	
	//////////////////////////////////////////////////////////////////////////
	
	private void setupTee() throws DeploymentTestsException
	{
		// where to save the report?
		// (1) in the properties file
		
		String fname = props.getProperty("report");
		
		if(!Utils.ok(fname))
		{
			// (2) -- the default
			fname = "./" + REPORT_FILE;
		}
		
		reportFile = Utils.safeGetCanonicalFile(new File(fname));
		
		try
		{
			Tee.start(reportFile.getPath());
		}
		catch(IOException e)
		{
			throw new DeploymentTestsException("Could not create report file: " + reportFile, e);
		}
	}
	
	//////////////////////////////////////////////////////////////////////////

	private int getNumPassed()
	{
		int numPassed = 0;
		
		for(Iterator it = tests.iterator(); it.hasNext(); )
		{
			Test test = (Test)it.next();
			
			if(test.passed)
				++numPassed;
		}
		
		return numPassed;
	}
	
	//////////////////////////////////////////////////////////////////////////

	private static void usage()
	{
		printErr("\n\n\n\n");
		bold("USAGE", System.err);
		
		for(int i = 0; i < help.length; i++)
		{
			printErr(help[i]);
		}

	}
	
	//////////////////////////////////////////////////////////////////////////
	
	private					CmdFactory	cmdFactory		= Env.getCmdFactory();
	private					File		propertiesFile;
	private					File		reportFile;
	private					Properties	props			= new Properties();
	private					Phup		phup;
	private					List		targets;
	private					List		samples;
	private					List		tests;
	private					Date		start;
	private					Date		end;
	private	static			String		stars;
	private static final	String		PROPERTIES_FILE	= "./DeploymentTests.properties";
	private static final	String		REPORT_FILE		= "./DeploymentTests.out";
	private static			String		caller;
	private					boolean		pauseAfterDeploy	= false;
	private static			boolean		calledFromAnt		= false;
	private static final	String[]	help =
	{
		"java -cp <classpath> -ea com.sun.enterprise.admin.mbeanapi.deployment.DeploymentTestsAuto",
		"",
		"where <classpath> must include: mbeanapi.jar, jmxri.jar and jmxremote.jar",
		"",
		"You must put instructions in a file named \"DeploymentTests.properties\"",
		"At a minimum, you must setup the following properties (example values included",
		"",
		"user=admin",
		"port=8686",
		"password=adminadmin",
		"host=iasengsol6.red.iplanet.com",
		"sampledir=C:/jwsee/appserv-tests/devtests/ee/admin/mbeanapi/deployment/samples",
		"targets=qbert,foo",
		"",
		"The report file path defaults to \"DeploymentTests.out\" in the current directory.",
		"You can specify any path you want like so:",
		"",
		"report=c:/jwsee/appserv-tests/devtests/ee/admin/mbeanapi/deployment/DeploymentTests.out",
		"",
		"",
		"targets is a comma-delimited list of clusters and stand-alone instances.",
		"sampledir points to a directory that you have filled with samples to be deployed and undeployed.",
		"To make manual testing possible you can set a property that will cause the program to pause and",
		"wait for you to type in a carriage return after every deployment.  This allows you to check the",
		"server, run the app, etc.  Here is the syntax:",
		"",
		"pauseAfterDeploy=true",
		"",
		"",
		"",
	};
	
	static
	{
		// note -- there must be a better way to do this, but I can't find it!!
		StringBuffer sb = new StringBuffer();

		for(int i = 0; i < 80; i++)
			sb.append('*');

		stars = sb.toString();
	}

	///////////////////////////////////////////////////////////////////////////
	////////////////  nested classes   ///////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	

	

	
	private static class Target
	{
		public String toString()
		{
			return "Target: " + name;
		}
		private Target(String name, ConnectCmd ccmd)
		{
			this.name = name;
			this.ccmd = ccmd;
		}
		private String		name;
		private ConnectCmd	ccmd;
	}
	
	private static class Test
	{
		public String toString()
		{
			StringBuffer sb = new StringBuffer();
			
			if(passed)
				sb.append("****** Passed").append('\n');
			else
				sb.append("****** Failed").append('\n');
			
			sb.append(sample).append('\n');
			sb.append(target).append('\n');

			if(comments.length() > 0)
				sb.append(comments).append('\n');
			
			if(!passed)
			{
				sb.append("Exceptions:").append('\n');
				int which = 1;
				for(Iterator it = exceptions.iterator(); it.hasNext(); ++which)
				{
					Exception e = (Exception)it.next();
					sb.append("Exception # " + which).append('\n');
					
					StringWriter sw = new StringWriter();
					PrintWriter  pw = new PrintWriter(sw, true);
					e.printStackTrace(pw);
					pw.close();
					
					sb.append(sw.toString());
				}
			}
			return sb.toString();
		}
		
		private Test(Utils.Sample sample, Target target)
		{
			this.sample = sample;
			this.target = target;
		}
		private void addComment(String s)
		{
			comments += s;
		}

		private void add(DeploymentTestsException dte)
		{
			exceptions.add(dte);
		}
		Utils.Sample	sample;
		Target	target;
		String	comments = new String();
		List	exceptions = new ArrayList();
		boolean passed = false;
	}
}

