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
package com.sun.appserv.management.base;

import java.util.Map;
import java.io.Serializable;

import com.sun.appserv.management.util.misc.ThrowableMapper;

/**
	Base class for OperationStatus.
 */

public class OperationStatusBase
	extends MapCapableBase
	implements OperationStatus
{
	
	/**
		Create a new instance which represents the appropriate class.
		
		@param m			a Map representing the appropriate class
		@param className	the class which the Map represents
	 */
		public <T extends Serializable>
	OperationStatusBase(
		final Map<String,T>		m,
		final String	className )
	{
		super( m, className );
	}
	
	
		protected boolean
	validate()
	{
		// nothing to do for now
		return( true );
	}
	
	/**
	    @return the status code from the operation
	 */
		public int
	getStatusCode()
	{
		int	statusCode	= STATUS_CODE_FAILURE;
		
		final Integer	code	= getInteger( STATUS_CODE_KEY );
		if ( code != null )
		{
			statusCode	= code.intValue();
		}
		else
		{
			statusCode	= getThrowable() != null ?
					STATUS_CODE_FAILURE : STATUS_CODE_SUCCESS;
		}
		
		return( statusCode );
	}
	
	/**
	    set the status code for the operation
	 */
		public void
	setStatusCode( final int statusCode )
	{
		putField( STATUS_CODE_KEY, new Integer( statusCode ) );
	}
	
	
	/**
	    @return a Throwable for the operation, or null if none
	 */
		public Throwable
	getThrowable()
	{
		return( (Throwable)getObject( THROWABLE_KEY ) );
	}
	
	
	/**
	    Set the Throwable for the operation, which may be null.
	 */
		public void
	setThrowable( final Throwable t)
	{
		final Throwable	conforming	= new ThrowableMapper( t ).map();
		
		putField( THROWABLE_KEY, conforming );
	}
	
	
}








