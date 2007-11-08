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
 * HADBCreateDB.java
 *
 * Created on April 10, 2004, 12:37 PM
 */

package com.sun.enterprise.ee.admin.hadbmgmt;


import com.sun.enterprise.util.SystemPropertyConstants;

import java.io.File;

public class HADBCreateSchema
{
	public HADBCreateSchema(HADBInfo info) throws HADBSetupException
	{
		this.info = info;
		jdbcURL = info.getJdbcURL();
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public Object[] create() throws HADBSetupException
	{
		// October 2004 bnevins
		// No RPC Exception is observed anymore.
		
		//FIXTHIS: You might ask why we are exec'ing this command in a new java
		//VM process, when the HADBSessionStoreUtil command is here at our disosal.
		//The reason is that when invoked within the DAS, an RPC exception is
		//always thrown from the HADB jdbc driver. When this is resolved, we
		//can get rid of the call to execHADBSessionStoreUtil and replace
		//it with the code below.
		
		//public HADBSessionStoreUtil( String user, String password, String url,
		//	String systemUser, String systemPassword)
		/**
		 * HADBSessionStoreUtil u = new HADBSessionStoreUtil(info.getUser(), info.getPassword(),
		 * jdbcURL, info.getUser(), info.getPassword());
		 * u.createSessionStore();
		 **/

		if(HADBUtils.nativeSchema())
		{
			try
			{
				nativeCreate();
				return info.prepMsgs();
			}
			catch(Exception e)
			{
				// fall through
			}
		}
		
		execHADBSessionStoreUtil("create");
		
		return info.prepMsgs();
	}
	
	public Object[] clear() throws HADBSetupException
	{
		//FIXTHIS: You might ask why we are exec'ing this command in a new java
		//VM process, when the HADBSessionStoreUtil command is here at our disosal.
		//The reason is that when invoked within the DAS, an RPC exception is
		//always thrown from the HADB jdbc driver. When this is resolved, we
		//can get rid of the call to execHADBSessionStoreUtil and replace
		//it with the code below.
		
		//public HADBSessionStoreUtil( String user, String password, String url,
		//	String systemUser, String systemPassword)
		/**
		 * HADBSessionStoreUtil u = new HADBSessionStoreUtil(info.getUser(), info.getPassword(),
		 * jdbcURL, info.getUser(), info.getPassword());
		 * u.clearSessionStore();
		 **/

		if(HADBUtils.nativeSchema())
		{
			try
			{
				nativeClear();
				return info.prepMsgs();
			}
			catch(Exception e)
			{
				// fall through
			}
		}
		
		execHADBSessionStoreUtil("clear");
		return info.prepMsgs();
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void execHADBSessionStoreUtil(String command) throws HADBSetupException
	{
		PasswordManager systemPW	= new PasswordManager(info, Constants.SCHEMASYSTEMPASSWORD, 
			info.getSystemPassword());
		PasswordManager dbPW		= new PasswordManager(info, Constants.SCHEMADBPASSWORD, 
			info.getDatabasePassword());
		try
		{
			String[] args = new String[9];
			args[0] = "-cp";
			String classpath = System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY) +
			"/lib/appserv-se.jar" + File.pathSeparator;
			classpath += System.getProperty(SystemPropertyConstants.HADB_ROOT_PROPERTY) +
			"/lib/hadbjdbc4.jar";
			args[1] = classpath;
			args[2] = "com.sun.enterprise.ee.admin.hadbmgmt.HADBSessionStoreUtil";
			args[3] = command;
			args[4] = jdbcURL;
			args[5] = info.getDatabaseUser();
			args[6] = dbPW.getFile().getAbsolutePath();
			args[7] = info.getSystemUser();
			args[8] = systemPW.getFile().getAbsolutePath();;
			String javaCmd = info.getJavaRoot() + "/bin/java";
			//usage: java HADBSessionStoreUtil [create|clear] url user password systemuser systempassword
			HADBMExecutor exec = new HADBMExecutor(new File(javaCmd), args);

			// bug: the first few times it may fail...
			// com.sun.hadb.jdbc.DbException: HADB-S-00224: The operation timed out
			for(int i = 0; i < 10; i++)
			{
				int exitValue = exec.exec();
				//int exitValue = 0;
				//String[] args2 = new String[] { args[3], args[4], args[5], args[6], args[7],args[8] };
				//HADBSessionStoreUtil.main(args2);
				
				if(exitValue == 0)
				{
					String s = StringHelper.get("hadbmgmt-res.SchemaCreationMessage",
					StringHelper.get("hadbmgmt-res.SUCCEEDED"), "" + (i+1), "" + 10);
					LoggerHelper.fine(s);
					//info.addMsg(s);
					break;
				}
				if(i >= 9)
				{
					throw new HADBSetupException("hadbmgmt-res.SchemaCreateFailed",
					new Object[]
					{"Ten Tries Attempted" + exitValue, exec.getStdout(), exec.getStderr()});
				}

				String s = StringHelper.get("hadbmgmt-res.SchemaCreationMessage",
				StringHelper.get("hadbmgmt-res.FAILED"), "" + (i+1), "" + 10);
				LoggerHelper.info(s);
				info.addMsg(s);
			}
		}
		finally
		{
			systemPW.delete();
			dbPW.delete();
		}
		
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void nativeClear() throws HADBSetupException
	{
		for(int i = 0; i < 10; i++)
		{
			try 
			{             
				createUtil();
				tableUtil.clearSessionStore();
				String s = StringHelper.get("hadbmgmt-res.SchemaClearMessage",
					StringHelper.get("hadbmgmt-res.SUCCEEDED"), "" + (i+1), "" + 10);
				LoggerHelper.fine(s);
				info.addMsg(s);
				break;
			}         
			catch (Exception e) 
			{
				if(i >= 9)
					throw new HADBSetupException("hadbmgmt-res.SchemaClearFailed", e);
				
			}    
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void nativeCreate() throws HADBSetupException
	{
			//public HADBSessionStoreUtil( String user, String password, String url,
			//String systemUser, String systemPassword)
		for(int i = 0; i < 10; i++)
		{
			try 
			{             
				createUtil();
				tableUtil.createSessionStore();
				String s = StringHelper.get("hadbmgmt-res.SchemaCreationMessage",
					StringHelper.get("hadbmgmt-res.SUCCEEDED"), "" + (i+1), "" + 10);
				LoggerHelper.fine(s);
				info.addMsg(s);
				break;
			}         
			catch (Exception e) 
			{
				if(i >= 9)
					throw new HADBSetupException("hadbmgmt-res.SchemaCreateFailedNative", e);
			}    
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void createUtil() throws HADBSetupException
	{
		tableUtil = new HADBSessionStoreUtil(
			info.getDatabaseUser(),
			info.getDatabasePassword(),
			jdbcURL,
			info.getSystemUser(),
			info.getSystemPassword());
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private HADBInfo			info;
	private String				jdbcURL;
	private	HADBSessionStoreUtil tableUtil;
}
