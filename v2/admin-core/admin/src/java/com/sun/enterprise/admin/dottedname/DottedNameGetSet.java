/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /cvs/glassfish/admin-core/admin/src/java/com/sun/enterprise/admin/dottedname/DottedNameGetSet.java,v 1.3 2005/12/25 03:47:31 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:47:31 $
 */
 

package com.sun.enterprise.admin.dottedname;

import javax.management.Attribute;
import javax.management.AttributeList;

/*
	Interface for supporting CLI 'get' and 'set' commands.
 */
public interface DottedNameGetSet
{
	/*
		Get multiple dotted-name values.  Input can be one or more names, any of which
		may be wildcarded.
		
		If there are N names in the input the resulting Object []
		will also be of length N.  Each element in the output Object [] will be:
		  - an Attribute		if the input value was a single dotted-name
		  - an Attribute []		if the input value was a wildcarded dotted-name (could be of any length)
		  - an Exception		if there was a problem (usually AttributeNotFoundException)
		
		
		When using  wildcarded name, the result will usually never be an Exception unless
		there was some low-level problem; an regex that matches nothing will return
		an empty AttributeList.
		
		@param name		dotted name of the Attribute
		@returns		AttributeList for dotted-names which were gotten.
	 */
	public Object []	dottedNameGet( String [] names ) ;
	
	/*
		Same as dottedNameGet( String [] ), but for a single name, which may be wildcarded.
		Completely equivalent to dottedNameGet( new String[] { name } )
		
		An exception may be returned; it will not be thrown.
	 */
	public Object 		dottedNameGet( String name );
	
	/*
		Set multiple dotted-name values.  Input must be one or more names. Wildcarding
		is disallowed.
		
		If there are N names in the input the resulting Object []
		will also be of length N.  Each entry in the output Object [] will be:
		  - an Attribute		if it was successfully set
		  - an Exception		if there was a problem
		
		@param name		dotted name of the Attribute
		@returns		AttributeList for dotted-names which were set.
	 */
	public Object []	dottedNameSet( String [] names );
	public Object		dottedNameSet( String name );

	/*
		Same as dottedNameGet, but for monitoring.
	 */
	public Object []	dottedNameMonitoringGet( String [] names ) ;
	public Object 		dottedNameMonitoringGet( String name ) ;	
	
	/*
		Return a Set of String (dotted names) that match the name prefixes for
		non-monitoring dotted names.
		
		@param namePrefixes array of dotted-name prefixes (may be wildcarded)
	 */
	public String []		dottedNameList( String [] namePrefixes);
	
	/*
		Return a Set of String (dotted names) that match the name prefixes,
		but only for monitoring dotted names.
		
		@param namePrefixes array of dotted-name prefixes (may be wildcarded)
	 */
	public String []		dottedNameMonitoringList( String [] namePrefixes );
}
