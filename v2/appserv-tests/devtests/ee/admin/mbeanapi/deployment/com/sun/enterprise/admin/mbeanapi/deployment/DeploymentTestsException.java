/*
 * DeploymentTestsException.java
 *
 * Created on September 17, 2004, 11:42 AM
 */

package com.sun.enterprise.admin.mbeanapi.deployment;

/**
 *
 * @author  bnevins
 */
public class DeploymentTestsException extends Exception
{
	public DeploymentTestsException(String s)
	{
		super(s);
	}

	public DeploymentTestsException(Throwable t)
	{
		super(t);
	}

	public DeploymentTestsException(String s, Throwable t)
	{
		super(s, t);
	}
	
}
