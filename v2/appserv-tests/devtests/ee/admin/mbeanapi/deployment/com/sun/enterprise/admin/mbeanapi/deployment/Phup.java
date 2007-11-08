/*
 * Thup.java
 *
 * Created on September 18, 2004, 11:53 PM
 */

package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Properties;

/**
 * An abbreviation for PortHostUserPassword
 * It's annoying to pass it around all over the place as individual items,
 * so I created this trivial class.
 * @author  bnevins
 */
class Phup
{
	Phup(int port, String host, String user, String password)
	{
		this.port = port;
		this.host = host;
		this.user = user;
		this.password = password;
	}
	
	///////////////////////////////////////////////////////////////////////////

	Phup(String port, String host, String user, String password) throws DeploymentTestsException
	{
		this.port = string2int(port);
		this.host = host;
		this.user = user;
		this.password = password;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	Phup(Properties props) throws DeploymentTestsException
	{
		user		= props.getProperty("user");
		password	= props.getProperty("password");
		host		= props.getProperty("host");
		
		if(user == null || password == null || host == null)
			throw new DeploymentTestsException("Can't find user and/or password and/or host in Properties file.");

		// string2int validates...
		port = string2int(props.getProperty("port"));
	}
	
	//////////////////////////////////////////////////////////////////////////
	
	private static int string2int(String s) throws DeploymentTestsException
	{
		try
		{
			int i = Integer.parseInt(s);
			
			if(i <= 0 || i > 65535)
				throw new NumberFormatException();
			
			return i;
		}
		catch(NumberFormatException nfe)
		{
			throw new DeploymentTestsException("Bad port number: " + s);
		}
	}
	
	//////////////////////////////////////////////////////////////////////////
	
	// note that these have default scope...
	String user;
	String password;
	String host;
	int    port;
}
