/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/CLISupportMBean.java,v 1.4 2004/01/10 02:57:20 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/01/10 02:57:20 $
 */
 

/*
	The MBean interface for CLI support.
 */
 
package com.sun.cli.jmxcmd.support;

import javax.management.*;
import java.io.IOException;



public interface CLISupportMBean
{
	
	/**
		Supports the get CLI command
		
		@param attrs	comma-separated list of attributes
		@param targets	space-separated list of targets
	 */
		public ResultsForGetSet []
	mbeanGet( String attrs, String [] targets) throws Exception;
	

	/**
		Supports the set CLI command
		
		@param attrs	comma-separated list of attributes/value pairs
		@param targets	space-separated list of targets
	 */
		public ResultsForGetSet []
	mbeanSet( String attrs, String [] targets ) throws Exception;

	 
	/**
		Supports the invoke CLI command
		
		@param operationName	name of operation to invoke
		@param argList			comma-separated list of operation arguments
		@param targets			space-separated list of targets
		
		@return array of InvokeResult[], one for each resolved target
	 */
		public InvokeResult []
	mbeanInvoke( String operationName, String argList, String [] targets ) throws Exception;
	
	
	
	/**
		Supports the find CLI command
		
		@param patterns	space-separated list of ObjectName patterns
		@return	array of ObjectName which match the pattern(s)
	 */
		public ObjectName []
	mbeanFind( String [] patterns )
		throws Exception;
		
	/**
		Supports the find CLI command with regular expressions
		
		@param patterns	space-separated list of ObjectName patterns
		@param regexList comma-separatde list of name/value regular expressions (eg nameexp=valueexp)
		@return	array of ObjectName which match the pattern(s)
	 */
		public ObjectName []
	mbeanFind( String [] patterns, String regexList )
		throws Exception;


	/**
		Supports the inspect CLI command
		
		@param name	ObjectName which must resolve to single MBean ObjectName
	 */
		public InspectResult
	mbeanInspect( InspectRequest request, ObjectName name ) throws Exception;
	
	/**
		Supports the list CLI command
		
		@param request		the request describing what is wanted
		@param targets		array of targets, aliases, etc
	 */
		public InspectResult []
	mbeanInspect( InspectRequest request, String [] targets ) throws Exception;
	
	
	/**
		Supports the create CLI command
		
		@param name		the ObjectName for the newly created MBean
		@param theClass	the class of the MBean to instantiate
		@param args		optional argument list to choose a constructor
	 */
		public void
	mbeanCreate( String name, String theClass, String args ) throws Exception;
	
	/**
		Supports the delete CLI command
		
		@param name		the ObjectName for the MBean to unregister
	 */
		public void
	mbeanUnregister( String name ) throws Exception;
	
	/**
		Supports the count CLI command
	 */
		public int
	mbeanCount(  ) throws Exception;
	
	/**
		Supports the domains CLI command
	 */
		public String []
	mbeanDomains(  ) throws Exception;
	
	/**
		Supports the domains CLI command
	 */
		public String
	mbeanGetDefaultDomain(  ) throws Exception;
	
	/**
		Supports the listen CLI command
		
		@param start		whether to start or stop listening
		@param targets		array of targets, aliases, etc
		@param listener		listener object
		@param filter		optional filter
	 */
		public ObjectName[]
	mbeanListen(
		boolean	start,
		String [] targets,
		NotificationListener listener,
		NotificationFilter filter,
		Object handback ) throws Exception;
	
	
	/**
		Resolve a set of targets to their underlying ObjectNames.  Targets may
		be fully qualified, partially qualified or aliases.  Targets that can't be
		resolved are ignored.
		
		@param targets	array of targets to be resolved.
	 */
		public ObjectName []
	resolveTargets( final String [] targets) throws Exception;
}

