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

