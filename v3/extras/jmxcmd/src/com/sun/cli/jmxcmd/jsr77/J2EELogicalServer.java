/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/J2EELogicalServer.java,v 1.1 2004/10/14 19:07:03 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/10/14 19:07:03 $
 */
package com.sun.cli.jmxcmd.jsr77;

import java.util.*;
import javax.management.ObjectName;


/**
	Base interface only (for cluster and standalone server)
 */
public interface J2EELogicalServer extends J2EEManagedObject, StateManageable
{
	/**
	 * Start the application on this Server.
	 *
	 * @param appID The application ID
	 * @param optional Optional parameters supplied as name-value pairs
	 * The parameters are documented here:
	 * {@link com.sun.appserv.management.deploy.DeploymentMgr#startDeploy}
	 */
	public void startApp(String appID, Map optional);

	/**
	 * Stop the application on this Server.
	 *
	 * @param appID The application ID
	 * @param optional Optional parameters supplied as name-value pairs
	 * The parameters are documented here:
	 * {@link com.sun.appserv.management.deploy.DeploymentMgr#startDeploy}
	 */
	public void stopApp(String appID, Map optional);
}

