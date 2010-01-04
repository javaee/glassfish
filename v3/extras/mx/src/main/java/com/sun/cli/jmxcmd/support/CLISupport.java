/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2009 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/CLISupport.java,v 1.3 2004/01/10 02:57:20 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2004/01/10 02:57:20 $
 */
 

 
package com.sun.cli.jmxcmd.support;
 
import javax.management.*;
import java.io.IOException;

/**
	Proxy class for accessing functionality of the CLISupportMBean
	
	For now, this MBean is run locally, it it could be run remotely,
	in which case this class would take care of looking it up and forwarding
	appropriately.
 */
public final class CLISupport implements CLISupportMBean
{
	private CLISupportMBeanImpl		mImpl;
    

	/**
		Constructor takes the MBeanServerConnection which should be used to implement
		its functionality.  This *need not be* the same MBeanServerConnection as the
		one in which this MBean itself is registered.
		
		@param conn			the MBeanServerConnection
		@param aliasMgr		an AliasMgr
	 */
		public
	CLISupport( MBeanServerConnection conn, AliasMgr aliasMgr )
		throws Exception
	{
		mImpl	= new CLISupportMBeanImpl( conn, aliasMgr );
	}
	
		public ResultsForGetSet []
	mbeanGet( String attrs, String [] targets) throws Exception
	{
		final ResultsForGetSet []	result	= mImpl.mbeanGet( attrs, targets );
		
		return( result );
	}
	

		public ResultsForGetSet []
	mbeanSet( String attrs, String [] targets ) throws Exception
	{
		final ResultsForGetSet []	result	= mImpl.mbeanSet( attrs, targets );
		
		return( result );
	}
	
	 
		public InvokeResult []
	mbeanInvoke( String operationName, String args, String [] targets )
		throws Exception
	{
		final InvokeResult []	result	= mImpl.mbeanInvoke( operationName, args, targets );
		
		return( result );
	}


		public InspectResult
	mbeanInspect( InspectRequest request, ObjectName name )
		throws Exception
	{
		final InspectResult	result	= (InspectResult)mImpl.mbeanInspect( request, name );
		
		return( result );
	}
	
		public InspectResult []
	mbeanInspect( InspectRequest request, String [] targets )
		throws Exception
	{
		final InspectResult []	result	= mImpl.mbeanInspect( request, targets );
		
		return( result );
	}
	
		public ObjectName []
	mbeanFind( String [] patterns  )
		throws Exception
	{
		final ObjectName []	result	= mImpl.mbeanFind( patterns );
		
		return( result );
	}
	
	
		public ObjectName []
	mbeanFind( String [] patterns, String regexList)
		throws Exception
	{
		final ObjectName []	result	= mImpl.mbeanFind( patterns, regexList );
		
		return( result );
	}
	
	
		public ObjectName []
	resolveTargets( final String [] targets ) throws Exception
	{
		final ObjectName []	resolved	= mImpl.resolveTargets( targets );
		
		return( resolved );
	}
	
		public void
	mbeanCreate( String name, String theClass, String args ) throws Exception
	{
		mImpl.mbeanCreate( name, theClass, args );
	}
	
		public void
	mbeanUnregister( String name ) throws Exception
	{
		mImpl.mbeanUnregister( name );
	}
	
		public int
	mbeanCount( ) throws Exception
	{
		return( mImpl.mbeanCount( ) );
	}
	
		public String []
	mbeanDomains( ) throws Exception
	{
		return( mImpl.mbeanDomains( ) );
	}
	
		public String
	mbeanGetDefaultDomain( ) throws Exception
	{
		return( mImpl.mbeanGetDefaultDomain( ) );
	}
	
		public ObjectName[]
	mbeanListen(
		boolean	start,
		String [] targets,
		NotificationListener listener,
		NotificationFilter filter,
		Object handback ) throws Exception
	{
		return( mImpl.mbeanListen( start, targets, listener, filter, handback ) );
	}
	
}

