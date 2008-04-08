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
package org.glassfish.admin.amx.j2ee;

import java.util.Set;
import java.util.Collections;


import javax.management.ObjectName;
import javax.management.AttributeNotFoundException;


import com.sun.appserv.management.j2ee.J2EEDeployedObject;
import com.sun.appserv.management.j2ee.StateManageable;

import com.sun.appserv.management.util.misc.GSetUtil;
import org.glassfish.admin.amx.mbean.Delegate;

/**
 */
public class J2EEDeployedObjectImplBase extends J2EEManagedObjectImplBase
	// implements J2EEDeployedObject
{
		public
	J2EEDeployedObjectImplBase(
        final String j2eeType,
        final String fullType,
        final ObjectName parentObjectName,
        final Class<? extends J2EEDeployedObject> theInterface,
        final Delegate delegate )
	{
		super( j2eeType, fullType, parentObjectName, theInterface, delegate );
	}
    	
		public String
	getdeploymentDescriptor()
	{
		return( (String)delegateGetAttributeNoThrow( "deploymentDescriptor" ) );
	}
	
		public String
	getserver()
	{
		return( getServerObjectName().toString() );
	}
	
	private final static Set<String>	DONT_MAP_SET =
	    GSetUtil.newUnmodifiableStringSet("deploymentDescriptor", "server" );
	
		protected Set<String>
	getDontMapAttributeNames()
	{
		return( Collections.unmodifiableSet(
		    GSetUtil.newSet( DONT_MAP_SET, super.getDontMapAttributeNames() ) ));
	}
	
	/** jsr77 StateManageable impl. */

		public boolean
	isstateManageable()
	{
		return( false );
	}

		public void	
	start()
	{
		checkstateManageable();
		getDelegate().invoke( "start", null, null );
		setstartTime( System.currentTimeMillis() );
	}

		public void	
	startRecursive()
	{
 		start();
	}

		public void	
	stop()
	{
		checkstateManageable();
		getDelegate().invoke( "stop", null, null );
		setstartTime( 0 );
	}

		private void
	checkstateManageable()
	{
		if ( !isstateManageable() )
		{
			throw new UnsupportedOperationException( "stateManageable is false" );
		}
	}
}




