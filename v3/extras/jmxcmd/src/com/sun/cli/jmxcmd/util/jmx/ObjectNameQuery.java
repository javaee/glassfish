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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/ObjectNameQuery.java,v 1.2 2005/11/08 22:40:24 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2005/11/08 22:40:24 $
 */
package com.sun.cli.jmxcmd.util.jmx;

import java.util.Set;

import javax.management.ObjectName;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;

public interface ObjectNameQuery
{
	/**
		Return the ObjectNames of all MBeans whose properties match all the specified
		regular expressions.  Both property names and values may be searched.
		
		A starting set may be specified by using an ObjectName pattern.
		This can greatly improve the performance of the search by restricting the
		set of MBeans which are examined; otherwise all registered MBeans must be examined.
		
		The regexNames[ i ] pattern corresponds to regexValues[ i ].  A value of null
		for any item is taken to mean "match anything".  Thus specifing null for 
		'regexNames' means "match any name" and specifying regexNames[ i ] = null means
		to match only based on regexValues[ i ] (and vice versa).
		
		@param startingSet 			optional ObjectName pattern for starting set to search
		@param regexNames			optional series of regular expressions for Property names
		@param regexValues			optional series of regular expressions for Property values
		@return 					array of ObjectName (may be of zero length)
	 */
	Set<ObjectName>	matchAll( Set<ObjectName> startingSet, String [] regexNames, String [] regexValues );
				
	/**
		Return the ObjectNames of all MBeans whose properties match any of the specified
		regular expressions.  Both property names and values may be searched.
		
		A starting set may be specified by using an ObjectName pattern.
		This can greatly improve the performance of the search by restricting the
		set of MBeans which are examined; otherwise all registered MBeans must be examined.
		
		
		The regexNames[ i ] pattern corresponds to regexValues[ i ].  A value of null
		for any item is taken to mean "match anything".  Thus specifing null for 
		'regexNames' means "match any name" and specifying regexNames[ i ] = null means
		to match only based on regexValues[ i ] (and vice versa).
		
		@param startingSet 			optional ObjectName pattern for starting set to search
		@param regexNames			optional series of regular expressions for Property names
		@param regexValues			optional series of regular expressions for Property values
		@return 					array of ObjectName (may be of zero length)
	 */
	Set<ObjectName>	matchAny( Set<ObjectName> startingSet, String [] regexNames, String [] regexValues );
}






