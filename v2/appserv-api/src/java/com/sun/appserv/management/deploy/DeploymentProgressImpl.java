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
package com.sun.appserv.management.deploy;

import java.util.Locale;
import java.util.Map;
import java.io.Serializable;


import com.sun.appserv.management.deploy.DeploymentProgress;
import com.sun.appserv.management.base.MapCapableBase;

/**
	Use DeploymentSupport to create a new instance of this class.
 */
public final class DeploymentProgressImpl
	extends MapCapableBase
	implements DeploymentProgress
{	
	/**
		Create a new instance based on another.
	 */
		public
	DeploymentProgressImpl( final DeploymentProgress src )
	{
		this( src.asMap() );
	}
	
		protected boolean
	validate()
	{
		final byte	progressPercent	= getProgressPercent();
		return( progressPercent >= 0 && progressPercent <= 100 );
	}
	
	/**
		Create a new instance with explicit params
		
		@param progressPercent
		@param description
		@param other other values, see this( Map m )
	 */
		public <T extends Serializable>
	DeploymentProgressImpl(
		final byte		progressPercent,
		final String	description,
		final Map<String,T> other )
	{
		this( other, false );
		
		putField( PROGRESS_PERCENT_KEY, new Byte( (byte)progressPercent ) );
		putField( DESCRIPTION_KEY, description );
		
		validateThrow();
	}
	
	/**
		@param m	a Map representing a DeploymentProgress
		@param validate	true if should validate
	 */
		private <T extends Serializable>
	DeploymentProgressImpl( final Map<String,T> m, final boolean validate )
	{
		super( m, DEPLOYMENT_PROGRESS_CLASS_NAME);

		if ( validate )
		{
			validateThrow();
		}
	}
	
	/**
		@param data	a Map representing a DeploymentProgress
	 */
	/**
		Create a new instance.  The Map must contain the following
		keyed values:
		
		<ul>
		<li>{@link com.sun.appserv.management.base.MapCapable}.MAP_CAPABLE_TYPE_KEY with
			value DEPLOYMENT_PROGRESS_CLASS_NAME</li>
		<li>PROGRESS_PERCENT_KEY</li>
		<li>DESCRIPTION_KEY</li>
		</ul>
		<p>
		The map may contain also contain localized descriptions.
		See {@link DeploymentProgress}.LOCALIZED_DESCRIPTION_KEY_BASE.
		
		@param m	a Map representing a DeploymentProgress
	 */
		public <T extends Serializable>
	DeploymentProgressImpl( final Map<String,T> m )
	{
		super( m, DEPLOYMENT_PROGRESS_CLASS_NAME );
		checkValidType( m, DEPLOYMENT_PROGRESS_CLASS_NAME );
		
		validateThrow();
	}
	
		public String
	getMapClassName()
	{
		return( DEPLOYMENT_PROGRESS_CLASS_NAME );
	}
	
		public static String
	getLocalizedDescriptionKey( final Locale locale )
	{
		return( LOCALIZED_DESCRIPTION_KEY_BASE + "_" + locale.toString() );
	}
    
    	public byte
    getProgressPercent()
    {
    	return( getByte( PROGRESS_PERCENT_KEY ).byteValue() );
    }
    
    	public String
    getDescription()
    {
    	return( getString( DESCRIPTION_KEY ) );
    }
    
		public String
    getLocalizedDescription( final Locale locale)
    {
    	return( getString( getLocalizedDescriptionKey( locale ) ) );
    }
}



