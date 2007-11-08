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

import java.util.*;

/**
 *
 * @author  bnevins
 */
class HADBCreateDB
{
	HADBCreateDB(HADBCreateDBInfo info)
	{
		this.info = info;
	}
	
	///////////////////////////////////////////////////////////////////////////

	void create() throws HADBSetupException
	{
		//testPortBase();
		info.validate();
		String[] commands = info.getCreateCommands();
		HADBMExecutor exec = new HADBMExecutor(info.getExecutable(), commands);		

		int exitValue = exec.exec();

		if(exitValue != 0)
		{
			throw new HADBSetupException("hadbmgmt-res.CreateFailed", 
				new Object[] {"" + exitValue, exec.getStdout(), exec.getStderr()} );
		}

		else
		{
			LoggerHelper.fine("hadbmgmt-res.CreateDBOutput", exec.getStdout(), exec.getStderr());
		}
	}
	
    void delete() throws HADBSetupException
	{		

        //First stop the database. For now we ignore the result (since the 
        //database may already be in a stopped state). 
        String[] commands = info.getStopCommands();        
		HADBMExecutor exec = new HADBMExecutor(info.getExecutable(), commands);		
        int exitValue = exec.exec();
                        
        //Now that the database is stopped, delete it
        commands = info.getDeleteCommands();
		exec = new HADBMExecutor(info.getExecutable(), commands);		
		exitValue = exec.exec();
		
		if(exitValue != 0)
		{
			throw new HADBSetupException("hadbmgmt-res.DeleteFailed", 
				new Object[] {"" + exitValue, exec.getStdout(), exec.getStderr()} );
		}

		else
		{
			LoggerHelper.fine("hadbmgmt-res.DeleteDBOutput", exec.getStdout(), exec.getStderr());
		}
	}
    
	///////////////////////////////////////////////////////////////////////////
	
	boolean exists() throws HADBSetupException
	{
        String[] commands = info.getExistsCommands();        
		HADBMExecutor r = new HADBMExecutor(info.getExecutable(), commands);
		int exitValue = r.exec();

		if(exitValue == 0)
		{
			info.setDBPreExists();
			return true;
		}
		
		 return false;
	}
    
	///////////////////////////////////////////////////////////////////////////
	
	/** doesn't work because the port is UDP !!
	private void testPortBase() throws HADBSetupException
	{
		// make sure the port base isn't in use -- most likely by another DB
		int port = info.getPortBase();
		
		try
		{
			java.net.ServerSocket ss = new java.net.ServerSocket(port);
			
			// over-engineering.  We will ALWAYS get an Exception if it is already
			// bound -- but what the heck!
			if(ss.isBound())
			{
				ss.close();
				return;
			}
			ss.close();	// this will never be executed...
		}
		catch(Exception e)
		{
			// fall through...
		}
		// port is in use -- we get here via an Exception --OR-- if isBound() returned false
		String mesg = StringHelper.get("hadbmgmt-res.DBPortInUse", "" + port);
		LoggerHelper.severe(mesg);
		throw new HADBSetupException(mesg);
	}
	*/
	///////////////////////////////////////////////////////////////////////////

	private HADBCreateDBInfo info;
}
