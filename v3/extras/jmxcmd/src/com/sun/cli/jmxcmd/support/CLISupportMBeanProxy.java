/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/CLISupportMBeanProxy.java,v 1.5 2004/05/01 01:09:48 llc Exp $
 * $Revision: 1.5 $
 * $Date: 2004/05/01 01:09:48 $
 */
 
 
package com.sun.cli.jmxcmd.support;

import javax.management.ObjectName;
import javax.management.NotificationListener;
import javax.management.NotificationFilter;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;

import com.sun.cli.jmxcmd.support.AliasMgrHashMapImpl;
import com.sun.cli.jmxcmd.support.AliasMgr;


/**
	Supplies the CLISupportMBean and AliasMgrMBean methods in a single proxy.
	
	Refer to CLISupportMBean and AliasMgrMBean for details.
 */
public class CLISupportMBeanProxy implements CLISupportMBean, AliasMgrMBean
{
	final AliasMgr					mAliasMgrProxy;
	final CLISupportMBean			mCLIProxy;
	

	
	/**
		CLISupport and AliasMgr are anywhere (local or remote); precreated for this constructor
	 */
		public
	CLISupportMBeanProxy(
		AliasMgr				aliasMgr,
		CLISupportMBean			cliSupport ) throws Exception
	{
		mAliasMgrProxy	= aliasMgr;
		mCLIProxy		= cliSupport;
	}
	
	

//------------------------------ CLI --------------------------------------------
	
		public ResultsForGetSet []
	mbeanGet( String attrs, String [] targets) throws Exception
	{
		return( mCLIProxy.mbeanGet( attrs, targets ) );
	}

		public ResultsForGetSet []
	mbeanSet( String attrs, String [] targets ) throws Exception
	{
		return( mCLIProxy.mbeanSet( attrs, targets ) );
	}

		public InvokeResult []
	mbeanInvoke(
		String	operation,
		String	args,
		String [] targets ) throws Exception
	{
		return( mCLIProxy.mbeanInvoke( operation, args, targets ) );
	}

		public InvokeResult []
	mbeanInvoke(
		String	operation,
		String [] targets ) throws Exception
	{
		return( mbeanInvoke( operation, null, targets ) );
	}
	
	
		public ObjectName []
	mbeanFind( String [] targets )
		throws Exception
	{
		return( mCLIProxy.mbeanFind( targets ) );
	}
	
		public ObjectName []
	mbeanFind( String target )
		throws Exception
	{
		return( mbeanFind( new String[] { target } ));
	}
	
		public ObjectName []
	mbeanFind( String [] targets, String regex)
		throws Exception
	{
		return( mCLIProxy.mbeanFind( targets, regex ) );
	}


		public InspectResult
	mbeanInspect( InspectRequest request, ObjectName name ) throws Exception
	{
		return( mCLIProxy.mbeanInspect( request, name ) );
	}
	
		public InspectResult []
	mbeanInspect( InspectRequest request, String [] targets ) throws Exception
	{
		return( mCLIProxy.mbeanInspect( request, targets ) );
	}
	
		public void
	mbeanCreate( String name, String theClass, String args ) throws Exception
	{
		mCLIProxy.mbeanCreate( name, theClass, args );
	}
	
		public void
	mbeanUnregister( String name ) throws Exception
	{
		mCLIProxy.mbeanUnregister( name );
	}
	
		public int
	mbeanCount( ) throws Exception
	{
		return( mCLIProxy.mbeanCount( ) );
	}
	
		public String []
	mbeanDomains( ) throws Exception
	{
		return( mCLIProxy.mbeanDomains( ) );
	}
	
		public String
	mbeanGetDefaultDomain( ) throws Exception
	{
		return( mCLIProxy.mbeanGetDefaultDomain( ) );
	}
	
		public ObjectName[]
	mbeanListen(
		boolean		start,
		String []	targets,
		NotificationListener listener,
		NotificationFilter filter,
		Object handback ) throws Exception
	{
		return( mCLIProxy.mbeanListen( start, targets, listener, filter, handback ) );
	}
	
	
		public ObjectName []
	resolveTargets( String [] targets ) throws Exception
	{
		return( mCLIProxy.resolveTargets( targets ) );
	}
	
	
//------------------------------ AliasMgr --------------------------------------------
	
	
		public void
	createAlias( String aliasName, String objectName ) throws Exception
	{
		mAliasMgrProxy.createAlias( aliasName, objectName );
	}
	
		public void
	deleteAlias( String aliasName ) throws Exception
	{
		mAliasMgrProxy.deleteAlias( aliasName );
	}
	
		public String
	getAliasValue( String aliasName ) 
	{
		return( (String)mAliasMgrProxy.getAliasValue( aliasName ) );
	}
	
		public String []
	listAliases( boolean showValues ) throws Exception
	{
		return( mAliasMgrProxy.listAliases( showValues ) );
	}
	
		public String []
	getAliases( ) throws Exception
	{
		return( listAliases( false ) );
	}
	
		public AliasMgr
	getAliasMgr()
	{
		return( mAliasMgrProxy );
	}
}

