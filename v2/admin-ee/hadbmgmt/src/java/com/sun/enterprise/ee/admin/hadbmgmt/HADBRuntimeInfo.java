/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

/*
 * HADBRuntimeInfo.java
 *
 * Created on May 10, 2005, 12:30 AM
 */

package com.sun.enterprise.ee.admin.hadbmgmt;

import com.sun.enterprise.util.StringUtils;
import java.io.*;
import java.util.*;

/**
 * This class is responsible for getting and setting database attributes, and getting
 * status.
 * @author bnevins
 * @since 9.0
 */
public class HADBRuntimeInfo 
{
	/**
	 * Creates an instance.  The instance does nothing until you call a method
	 * @param info Container of all info
	 */
	public HADBRuntimeInfo(HADBInfo info) 
	{
		this.info = info;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * Get all of the database attributes for a given database(cluster).
	 * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If there is a runtime error getting the configuration.
	 * @return A Properties object withh all of the attributes for a database.  
	 * Note that some attributes are <I>read-only</I>.
	 */
	public Properties getAttributes() throws HADBSetupException 
	{
		// here is the output from hadbm:
		// ./hadbm get --all foobar
		// Attribute Value
		// jdbcURL   jdbc:sun:hadb:bulldozer.red.iplanet.com:15205,bulldozer.red.iplanet.com:15225

		if(allAtts != null)
			return allAtts;
		
		allAtts = new Properties();
		if(HADBUtils.noHADB())
		{
			allAtts.setProperty("foo", "goo");
			return allAtts;
		}
		
		String[] commands = info.getGetAttributesCommands();
		HADBMExecutor exec = new HADBMExecutor(info.getExecutable(), commands);
		//int exitValue = exec.exec(new File("C:/bnbin/spitargs.exe"), commands);
		int exitValue = exec.exec();

		if(exitValue != 0)
		{
			String out = exec.getStdout();
			String err = exec.getStderr();
			String msg = StringHelper.get("hadbmgmt-res.getAttributesFailed",
				new Object[]{"" + exitValue, out, err} );
			
			if(exec.isHadbmError(22005))
			{
				msg = StringHelper.get("hadbmgmt-res.AuthError") + " -- " + msg;
			}
				
			throw new HADBSetupException(msg);
		}

		LoggerHelper.fine("***** get --all STDOUT\n" + exec.getStdout());
		LoggerHelper.fine("***** get --all STDERR\n" + exec.getStderr());

		String s = exec.getStdout();
		
		if(!StringUtils.ok(s))
			return allAtts;
		
		String[] ss = s.split("\\s+");

		// make sure we got a non-empty array with pairs
		if(ss == null || ss.length == 0 || (ss.length % 2) != 0)
		{
			throw new HADBSetupException("hadbmgmt-res.gdtAttributesFailed",
			new Object[]
			{"" + exitValue, s, exec.getStderr()} );
			
		}
		// skip the first header line
		for(int i = 2; i < ss.length; i += 2)
		{
			allAtts.setProperty(ss[i], ss[i + 1]);
		}
		
		return allAtts;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * Get all of the read-only database attributes for a given database(cluster).
	 * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If there is a runtime error getting the configuration.
	 * @return A Properties object withh all of the read-only attributes for a database.  
	 */
	public Properties getReadOnlyAttributes() throws HADBSetupException 
	{
		separateAttributes();
		//System.out.println("READONLY: " + readOnlyAtts);
		return readOnlyAtts;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * Get all of the read-write database attributes for a given database(cluster).
	 * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If there is a runtime error getting the configuration.
	 * @return A Properties object withh all of the read-write attributes for a database.  
	 */
	public Properties getReadWriteAttributes() throws HADBSetupException 
	{
		separateAttributes();
		
		//System.out.println("READWRITE: " + readWriteAtts);
		return readWriteAtts;
	}

	///////////////////////////////////////////////////////////////////////////
	
	public void setAttributes(String sprops) throws HADBSetupException 
	{
		//Input String: foo=goo:hoo=ioo
		//hadbmgmt-res.BadProperties
		// Example of code that is 95% error-checking.  Remember the input is coming
		// directly from a user!
		
		// get the frequently used error message ready to go...
		String errmsg = StringHelper.get("hadbmgmt-res.BadProperties", sprops);
		
		if(sprops == null)
			throw new HADBSetupException(errmsg);
		if(sprops.indexOf('=') < 0)
			throw new HADBSetupException(errmsg);

		Properties props = new Properties();
		String[] pairs = sprops.split(":");
		
		// if the argument == ":", then we will have an array of zero-length!
		if(pairs == null || pairs.length <= 0)
			throw new HADBSetupException(errmsg);
		
		for(String pair : pairs)
		{
			// pair looks like this: "foo=goo"
			// paranoid about NPE
			if(pair == null)
				throw new HADBSetupException(errmsg);
	
			String[] splitpair = pair.split("=");
			
			if(splitpair == null || splitpair.length != 2)
				throw new HADBSetupException(errmsg);
	
			if(!StringUtils.ok(splitpair[0]) || !StringUtils.ok(splitpair[1]))
				throw new HADBSetupException(errmsg);
			
			props.setProperty(splitpair[0], splitpair[1]);
		}
		
		if(props.size() < 1)
			throw new HADBSetupException(errmsg);
		
		setAttributes(props);
	}

	///////////////////////////////////////////////////////////////////////////
	
	public void setAttributes(Properties props) throws HADBSetupException 
	{
		if(HADBUtils.noHADB())
		{
			return;
		}
		
		if(props == null || props.size() <= 0)
		{
			throw new HADBSetupException("hadbmgmt-res.NothingToDo");
		}
		
		String[] commands = info.getSetAttributesCommands(props);
		HADBMExecutor exec = new HADBMExecutor(info.getExecutable(), commands);
		int exitValue = exec.exec();

		if(exitValue != 0)
		{
			throw new HADBSetupException("hadbmgmt-res.setAttributesFailed",
			new Object[]
			{"" + exitValue, exec.getStdout(), exec.getStderr()} );
		}

		LoggerHelper.fine("***** 'hadbm set' STDOUT\n" + exec.getStdout());
		LoggerHelper.fine("***** 'hadbm set' STDERR\n" + exec.getStderr());
	}

	///////////////////////////////////////////////////////////////////////////
	
	public String getOtherInfo() throws HADBSetupException 
	{
		final String stars = "  ********  ";
		StringBuilder sb = new StringBuilder();
		sb.append(stars).append(StringHelper.get("hadbmgmt-res.NodeListHeader")).append(stars).append('\n').append('\n');
		sb.append(info.getNodeList()).append('\n').append('\n');
		sb.append(stars).append(StringHelper.get("hadbmgmt-res.PackagesListHeader")).append(stars).append('\n').append('\n');
		sb.append(info.getPackagesList()).append('\n');
		sb.append(stars).append(StringHelper.get("hadbmgmt-res.dbStatusHeader")).append(stars).append('\n').append('\n');
		sb.append(getDatabaseStatus()).append('\n');

		return sb.toString();
	}
	
	///////////////////////////////////////////////////////////////////////////

	public String getDatabaseStatus() throws HADBSetupException
	{
		// typical stdout --> "Database Status\r\nfoo      FaultTolerant\r\n"

		String[]		commands	= info.getExistsCommands();        
		HADBMExecutor	r			= new HADBMExecutor(info.getExecutable(), commands);
		int				exitValue	= r.exec();

		String err = StringHelper.get("hadbmgmt-res.getDBStatusFailed", 
			new Object[] { info.getClusterName(), exitValue, r.getStdout(), r.getStderr() } );

		if(exitValue != 0)
			throw new HADBSetupException(err);
		
		String out = r.getStdout();
		
		if(out == null || out.length() < 2)
			throw new HADBSetupException(err);
		

		try
		{
			Scanner scanner = new Scanner(out);

			// skip the first line: Database Status\r\n
			scanner.nextLine();	

			// the next word should be the db-name (cluster-name)
			if(!scanner.next().equals(info.getClusterName()))
				throw new HADBSetupException(err);

			return scanner.next();
		}
		catch(HADBSetupException he)
		{
			throw he;
		}
		catch(Exception e)
		{
			throw new HADBSetupException(err);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////

	boolean isRunning() throws HADBSetupException
	{
		/*
		 * HAFaultTolerant,FaultTolerant,Operational,NonOperational,Stopped,Unknown
		 */
		String status = getDatabaseStatus();

		if(status.equals("NonOperational") || status.equals("Stopped") || status.equals("Unknown"))
		{
			return false;
		}
		
		return true;
	}
	
	///////////////////////////////////////////////////////////////////////////

	public String[] getNodeList() throws HADBSetupException
	{
		// here is the output from hadbm:
		// hadbm status --nodes c1
		// NodeNo HostName   Port  NodeRole NodeState MirrorNode
		// 0      iasengsol6 17200 active   running   1
		// 1      iasengsol8 17220 active   running   0
		//
		// send this output back as:
		// ret[0] = "iasengsol6,running"
		// ret[1] = "iasengsol8,running"
		
		String raw = info.getNodeList();
		String err = StringHelper.get("hadbmgmt-res.getNodeListFailedB", raw);

		if(!StringUtils.ok(raw))
			throw new HADBSetupException(err);
		try
		{
			List<String> ss = new ArrayList<String>();
			BufferedReader reader = new BufferedReader(new StringReader(raw));

			// get rid of first line...
			reader.readLine(); 

			for(String line = reader.readLine(); line != null; line = reader.readLine())
			{
				String[] words = line.split("\\s");
				List<String> wordlist = stripEmpty(words);
				
				if(wordlist.size() < 6)
					throw new HADBSetupException(err);
				ss.add(wordlist.get(1) + "," + wordlist.get(4));
			}
			
			return ss.toArray(new String[ss.size()]);
		}
		catch(Exception e)
		{
			
		}

		throw new HADBSetupException(err);
	}

	///////////////////////////////////////////////////////////////////////////

	private List<String> stripEmpty(String[] ss)
	{
		List<String> list = new ArrayList<String>();

		for(String s : ss)
		{
			if(s != null && s.length() > 0)
				list.add(s);
		}
		return list;
	}

	///////////////////////////////////////////////////////////////////////////
	
	private void separateAttributes() throws HADBSetupException 
	{
		if(readOnlyAtts != null)
			return; // alrady done!
		
		getAttributes();
		ConfigAttributeManager cam = new ConfigAttributeManager(allAtts);
		readOnlyAtts	= cam.getReadOnlyAttributes();
		readWriteAtts	= cam.getReadWriteAttributes();
	}

	///////////////////////////////////////////////////////////////////////////
	
	private					HADBInfo	info;
	private					Properties	allAtts;
	private					Properties	readOnlyAtts;
	private					Properties	readWriteAtts;
}

