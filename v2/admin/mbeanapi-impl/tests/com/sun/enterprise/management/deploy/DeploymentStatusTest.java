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
package com.sun.enterprise.management.deploy;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.io.Serializable;

import com.sun.appserv.management.deploy.DeploymentStatus;

import com.sun.appserv.management.deploy.DeploymentStatusImpl;


import com.sun.enterprise.management.AMXTestBase;
import com.sun.enterprise.management.Capabilities;
import com.sun.appserv.management.util.misc.IteratorUtil;

/**
 */
public final class DeploymentStatusTest extends junit.framework.TestCase
{
		public
	DeploymentStatusTest( )
	{
	}
	
	
	
		private DeploymentStatusImpl
	createDeploymentStatus( final Object deployID )
	{
		final DeploymentStatusImpl	ds	=
			new DeploymentStatusImpl( 
			0,
			"success",
			"description",
			null );
		
		final Throwable	t	= new Exception( "test", new Throwable( "test2" ) );
		
		ds.setStageThrowable( t );
		assert( ds.getStageThrowable() == t );
		
		return( ds );
	}
	
		public void
	testCreateDeploymentStatus()
	{
		createDeploymentStatus( "dummy" );
	}
	
	/**
		An alternative impl, to ensure the logic doesn't assume a particular impl.
	 */
	private static final class DeploymentStatusFoo
	    implements DeploymentStatus, Serializable
	{
	    public static final long    serialVersionUID    = 983629292;
	    
		final ArrayList<DeploymentStatus>	mSubStages;
		final Map<String,Serializable>	mMap;
		final Exception	mException;
		DeploymentStatus	mParent;
		
			public
		DeploymentStatusFoo()
		{
			mSubStages	= new ArrayList<DeploymentStatus>();
			mException	= new Exception();
			mParent		= null;
			
			mMap	= new HashMap<String,Serializable>();
			putField( STAGE_STATUS_KEY, new Integer( 0 ) );
			putField( STAGE_STATUS_MESSAGE_KEY, "status message" );
			// putField( STAGE_THROWABLE_KEY, mException );
			putField( STAGE_DESCRIPTION_KEY, "description" );
		}
			public void
		addSubStage( DeploymentStatus subStage )
		{
			mSubStages.add( subStage );
			subStage.setParent( this );
		}
		
			public String
		getMapClassName()
		{
			return( DEPLOYMENT_STATUS_CLASS_NAME );
		}
		
			public Map<String,Serializable>
		asMap()
		{
			final Map<String,Serializable>	m	= new HashMap<String,Serializable>();
			
			m.putAll( mMap );
			
			if ( mSubStages.size() != 0 )
			{
				final DeploymentStatus[]	s	= new DeploymentStatus[ mSubStages.size() ];
				mSubStages.toArray( s );
				m.put( SUB_STAGES_KEY, s );
			}
			
			m.put( MAP_CAPABLE_CLASS_NAME_KEY, getMapClassName() );
			
			return( m );
		}
	
			public void
		putField( final String key, final Serializable value )
		{
			mMap.put( key, value );
		}

			public Iterator<Map<String,Serializable>>
		getSubStages()
		{
			if ( mSubStages.size() == 0 )
			{
				return( null );
			}
			
			final List<Map<String,Serializable>> maps   = new ArrayList<Map<String,Serializable>>();
			for( final DeploymentStatus ds : mSubStages )
			{
			    maps.add( ds.asMap() );
			}
			
			return( maps.iterator() );
		}
		
			public List<DeploymentStatus>
		getSubStagesList()
		{
			return( mSubStages );
		}

		public int getStageStatus()	{ return( getStatusCode() ); }
		public String getStageStatusMessage()	{ return( "status message" ); }
		
		public Throwable getThrowable()			{ return( mException ); }
		public Throwable getStageThrowable()	{ return( getThrowable() ); }
		public int getStatusCode()
			{ return( getStageThrowable() == null ? STATUS_CODE_SUCCESS : STATUS_CODE_FAILURE ); }

		public String getStageDescription()	{ return( "stage description" ); }

	    public DeploymentStatus getParent()		{ return( mParent ); }
	    
		public Map<String,Serializable> getAdditionalStatus()
		{
			return( null );
		}
		
			public void
		setParent( DeploymentStatus parent )
		{
			mParent	= parent;
		}
	    
	    	public boolean
	    equals( Object rhs )
	    {
	    	return( new DeploymentStatusImpl( this ).equals( rhs ) );
	    }
	    
	    	public String
	    toString()
	    {
	    	return( new DeploymentStatusImpl( this.asMap() ).toString() );
	    }
	}
	
		public void
	testDeploymentStatusFromMap()
	{
		final DeploymentStatusImpl	ds	= createDeploymentStatus( "dummy" );
		final DeploymentStatusFoo	stage1	= new DeploymentStatusFoo();
		ds.addSubStage( stage1 );
		
		final Map<String,Serializable>	data	= ds.asMap();
		
		final DeploymentStatusImpl ds2	= new DeploymentStatusImpl( data );
		
		assert( ds2.equals( ds ) );
	}
	
	
		public void
	testDeploymentStatusAsMap()
	{
		final DeploymentStatusImpl	ds	= createDeploymentStatus( "dummy" );
		
		final Map<String,Serializable>	m	= ds.asMap();
	}
	
	
		public void
	testCreateDeploymentStatusFromDeploymentStatus()
	{
		final DeploymentStatusFoo	foo	= new DeploymentStatusFoo();
		final DeploymentStatusImpl	ds	= new DeploymentStatusImpl( foo );
		
		assert( foo.equals( ds ) );
		assert( ds.equals( foo ) );
	}
	
		public void
	testDeploymentStatusSubStages()
	{
		final DeploymentStatusFoo	stage1	= new DeploymentStatusFoo();
		final DeploymentStatusFoo	stage2	= new DeploymentStatusFoo();
		final DeploymentStatusFoo	stage1_1	= new DeploymentStatusFoo();
		final DeploymentStatusFoo	stage2_1	= new DeploymentStatusFoo();
		
		final DeploymentStatusImpl	root	= createDeploymentStatus( "root" );
		assert( stage1.getParent() == null );
		root.addSubStage( stage1 );
		assert( stage1.getParent() == root );
		root.addSubStage( stage2 );
		assert( stage2.getParent() == root );
		stage1.addSubStage( stage1_1 );
		assert( stage1_1.getParent() == stage1 );
		stage2.addSubStage( stage2_1 );
		assert( stage2_1.getParent() == stage2 );
		
		final List<DeploymentStatus> subStages = root.getSubStagesList();
		assert( subStages.get(0) == stage1 );
		assert( subStages.get(1) == stage2 );
	}
	
	private static final class MyException extends Exception
	{
	    public static final long    serialVersionUID    = 833629292;
	    
		public MyException( Throwable cause )
		{
			super( cause );
		}
		
		public MyException( )
		{
		}
	}
	
		public void
	testIllegalThrowableDetected()
	{
		final DeploymentStatusImpl	root	= createDeploymentStatus( "root" );
		
		final Throwable	t	= new MyException( new MyException() );
		root.setStageThrowable( t );
		assert( root.getStageThrowable() != t );
	}
	
}


























