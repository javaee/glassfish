/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/MBeanProxyHandlerIntf.java,v 1.1 2005/11/08 22:40:23 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2005/11/08 22:40:23 $
 */

package com.sun.cli.jmxcmd.util.jmx;

import java.util.logging.Logger;

import javax.management.ObjectName;
import javax.management.MBeanInfo;

/**
	Documents built-in supported operations of {@link MBeanProxyHandler}.
 */
public interface MBeanProxyHandlerIntf
{
	/**
		Get the MBeanInfo for the MBean this proxy represents.
		
		@return the MBeanInfo or null if not available or inappropriate
	 */
	public MBeanInfo	getMBeanInfo();
	
		
 	public ObjectName 	getTargetObjectName();
 	
	/**
		Set a Logger for this proxy.
		Proxies emit most log messages at FINE, FINER, FINEST levels only.
		@param logger
	 */
	public void	setProxyLogger( Logger logger );
	
	/**
		Get this proxy's Logger (if any)
	 */
	public Logger	getProxyLogger( );
}


