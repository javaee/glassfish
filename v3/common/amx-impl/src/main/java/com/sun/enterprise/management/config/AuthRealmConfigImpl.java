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

package com.sun.enterprise.management.config;

import com.sun.enterprise.management.support.Delegate;


/**
	Configuration for the &lt;auth-realm&gt; element.
*/


public final class AuthRealmConfigImpl  extends AMXConfigImplBase
	//implements AuthRealmConfig
{
		public
	AuthRealmConfigImpl( final Delegate delegate )
	{
		super( delegate );
	}


    static private final String[]   STRING_SIG  = new String[] { String.class.getName() };
    static private final String[]   UPG_SIG  =
        new String[] { String.class.getName(), String.class.getName(), String[].class.getName() };
    
		public void
	addUser( 
	    final String user,
	    final String password,
	    final String[] groupList )
	{
		getDelegate().invoke(
		    "addUser",
		    new Object[] { user, password, groupList },
		    UPG_SIG );
	}
	
		public String[]
	getGroupNames()
	{
	    String[]  result  = null;
	    
	    // offline is implemented as an Attribute (as it should be)
	    // online is implemented (incorrectly) as an operation.
	    try
	    {
		    result  = (String[])getDelegate().invoke( "getGroupNames", null, null );
		}
		catch( Exception e )
		{
		    try
		    {
		        result  = (String[])delegateGetAttributeNoThrow( "GroupNames" );
		    }
		    catch( Exception ee )
		    {
		        // may not be any for this type of realm
		    }
		}
		
		return result;
	}
	
		public String[]
	getUserGroupNames( final String user )
	{
		return (String[])getDelegate().invoke(
		    "getUserGroupNames",
		    new Object[] { user },
		    STRING_SIG );
	}
	
		public String[]
	getUserNames()
	{
	    String[]  result  = null;
	    
	    // offline is implemented as an Attribute (as it should be)
	    // online is implemented (incorrectly) as an operation.
	    try
	    {
		    result  = (String[])getDelegate().invoke( "getUserNames", null, null );
		}
		catch( Exception e )
		{
		    try
		    {
		        result  = (String[])delegateGetAttributeNoThrow( "UserNames" );
		    }
		    catch( Exception ee )
		    {
		        // may not be any for this type of realm
		    }
		}
		
		return result;
	}

		public void
	removeUser( final String user )
	{
		getDelegate().invoke( "removeUser", new Object[] { user }, STRING_SIG );
	}
	
		public void
	updateUser(
	    final String user,
	    final String password,
	    final String[] groupList )
	{
		getDelegate().invoke( "updateUser",
		    new Object[] { user, password, groupList },
		    UPG_SIG );
	}
}










