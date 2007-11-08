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
 * PasswordManager.java
 *
 * Created on June 28, 2004, 11:17 AM
 */

package com.sun.enterprise.ee.admin.hadbmgmt;

import java.io.*;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.StringUtils;
import java.util.*;

/**
 *
 * @author  bnevins
 */
class PasswordManager
{
	PasswordManager(HADBInfo info, String name, String password) throws HADBSetupException
	{
		this.info = info;
		this.name = name;
		this.varName = makePropName(name);
		this.filenameArg = name + "file";
		this.password = password;
		file = new File(HADBUtils.getPasswordFileDir(info), name);
		createFile();
		protectFile();
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	void delete()
	{
		if(HADBUtils.noDelete())
			return;
		
		if(!file.delete())
			file.deleteOnExit();
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	String getArg()
	{
		return "--" + filenameArg + "=" + file.getAbsolutePath();
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	static String makePropName(String name)
	{
		// e.g. adminpassword --> HADBM_ADMINPASSWORD
		return prepend + name.toUpperCase();
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	File getFile()
	{
		return file;
	}
	
	///////////////////////////////////////////////////////////////////////////
	///////        private     ////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	
	private void createFile() throws HADBSetupException
	{
		try
		{
			// note: FileWriter will overwrite with no errors if it already exists
			FileWriter fw = new FileWriter(file);
			fw.write(varName + "=" + password);
			fw.close();
		}
		catch(Exception e)
		{
			throw new HADBSetupException(e);
		}
	}
	
	
	/**
	 * helper method.  It will read the props out of the file and set passwords appropriately
	 */
	
	final static void setPasswords(HADBInfo sinfo, String s) throws HADBSetupException
	{
		// fetch it out and write it to another file
		File f = null;
		
		try
		{
			if(!ok(s))
				return;

			f = new File(s);
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
			Properties props = new Properties();
			props.load(in);
			in.close();
			
			
			String apwName = makePropName(Constants.ADMINPASSWORD);
			String dpwName = makePropName(Constants.DBPASSWORD);
			String spwName = makePropName(Constants.SYSTEMPASSWORD);
			
			Set allowedKeys = new HashSet();
			
			allowedKeys.add(apwName);
			allowedKeys.add(dpwName);
			allowedKeys.add(spwName);
			
			checkProps(sinfo, props, allowedKeys);

			String apw = props.getProperty(apwName);
			String dpw = props.getProperty(dpwName);
			String spw = props.getProperty(spwName);
			
			if(ok(apw))
			{
				sinfo.setAdminPassword(apw);
				sinfo.addMsg(StringHelper.get("hadbmgmt-res.ReadPasswordFromFile", apwName, f));
			}
			
			if(ok(dpw))
			{
				sinfo.setDatabasePassword(dpw);
				sinfo.addMsg(StringHelper.get("hadbmgmt-res.ReadPasswordFromFile", dpwName, f));
			}

			if(ok(spw))
			{
				sinfo.setSystemPassword(dpw);
				sinfo.addMsg(StringHelper.get("hadbmgmt-res.ReadPasswordFromFile", spwName, f));
			}
		}
		catch(IOException ioe)
		{
			throw new HADBSetupException("hadbmgmt-res.BadPasswordFile", ioe, new Object[] { f, ioe} );
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private void protectFile() throws HADBSetupException
	{
		if(FileUtils.protect(file) == false)
		{
			LoggerHelper.warning("hadbmgmt-res.CantProtectPasswordFile", file);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private static boolean ok(String s)
	{
		return StringUtils.ok(s);
	}
	
	///////////////////////////////////////////////////////////////////////////

	final static void checkProps(HADBInfo sinfo, Properties props, Set allowedKeys)
	{
		// look at all the props and see if there is anything other than the allowed
		// properties.  If so, log a warning and add the message for CLI
		// using cool Collections frameworks techniques!
		
		// have to clone it -- o/w the original props will get munged up
		Properties p = (Properties)props.clone();
		
		Set propsKeys	= p.keySet();

		// remove the allowed keys from the props.  If there is anything left,
		// issue a warning
		
		propsKeys.removeAll(allowedKeys);
		
		if(propsKeys.size() <= 0)
			return;
		
		String warn = StringHelper.get("hadbmgmt-res.FunkyEntries");
		
		boolean firstTime = true;
		for(Iterator it = propsKeys.iterator(); it.hasNext(); )
		{
			String badProp = (String)it.next();
			
			if(firstTime)
				firstTime = false;
			else
				warn += ", ";
			
			warn += badProp;
		}
		LoggerHelper.warning(warn);
		sinfo.addMsg(warn);
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private static final	String		prepend = "HADBM_";
	private					HADBInfo	info;
	private					String		name;
	private					String		varName;
	private					String		filenameArg;
	private					String		password;
	private					File		file;
}
