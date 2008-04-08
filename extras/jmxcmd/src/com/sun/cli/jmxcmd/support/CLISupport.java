/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

