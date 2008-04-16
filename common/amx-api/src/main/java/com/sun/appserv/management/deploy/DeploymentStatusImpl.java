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

import com.sun.appserv.management.base.OperationStatusBase;
import com.sun.appserv.management.util.misc.TypeCast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
    <b>Not for public use.</b>  Use {@link DeploymentSupport} to
    create a DeploymentStatus from a Map.
 */
public final class DeploymentStatusImpl
	extends OperationStatusBase
	implements DeploymentStatus
{
	transient DeploymentStatus	mParent;
	
		public
	DeploymentStatusImpl( final DeploymentStatus src )
	{
		this( src.asMap(), true );
	}
	
		private <T extends Serializable>
	DeploymentStatusImpl(
		final Map<String,T>	m,
		final boolean	doValidate )
	{
		super( m, DEPLOYMENT_STATUS_CLASS_NAME );
		
		checkValidType( m, DEPLOYMENT_STATUS_CLASS_NAME );
		mParent	= null;
		
		convertSubStages();
		if ( doValidate && ! validate() )
		{
			throw new IllegalArgumentException( toString() );
		}
	}
	
	/**
		Create a new instance.  The Map must contain the following
		keyed values:
		
		<ul>
		<li>{@link com.sun.appserv.management.base.MapCapable}.MAP_CAPABLE_TYPE_KEY with
			value DEPLOYMENT_STATUS_CLASS_NAME</li>
		<li>STAGE_STATUS_KEY</li>
		<li>STAGE_STATUS_MESSAGE_KEY</li>
		</ul>
		
		The map may contain values keyed by any of the
		following:
		<ul>
		<li>SUB_STAGES_KEY</li>
		<li>STAGE_THROWABLE_KEY</li>
		<li>STAGE_DESCRIPTION_KEY</li>
		</ul>
		
		@param m	a Map representing a DeploymentStatus
	 */
		public <T extends Serializable>
	DeploymentStatusImpl( final Map<String,T> m )
	{
		this( m, true );
		convertSubStages();
	}
	
	/**
		Create a new instance.  The 'optional' Map may contain any of the value
		keys as found in this( Map m ).  Values supplied in the Map, if conflicting
		with other parameters, are overwritten in the resulting new DeploymentStatusImpl.
		
		@param stageStatus	
		@param stageStatusMessage	
		@param stageDescription
	 */
		public <T extends Serializable>
	DeploymentStatusImpl(
		final int		stageStatus,
		final String	stageStatusMessage,
		final String	stageDescription,
		final Map<String,T> optional )
	{
		super( null, DEPLOYMENT_STATUS_CLASS_NAME );

		putAll( optional );
		
		setStageStatus(  stageStatus );
		setStageStatusMessage( stageStatusMessage == null ? "" : stageStatusMessage );
		setStageDescription( stageDescription == null ? "" : stageDescription );
		
		convertSubStages();
		if ( ! validate() )
		{
			throw new IllegalArgumentException( );
		}
	}
	
	    private void
	convertSubStages()
	{
	    final List<?> value  = List.class.cast( getField( SUB_STAGES_KEY ) );
	    if ( value != null && value.size() != 0 )
	    {
    	    final List<DeploymentStatus> ssList = new ArrayList<DeploymentStatus>();
    	    
    	    for( final Object o : value )
    	    {
    	        DeploymentStatus ds = null;
    	        if ( o instanceof Map )
    	        {
    	            final Map<String,Serializable> m = TypeCast.asMap(o);
    	            ds   = new DeploymentStatusImpl(m,true);
    	        }
    	        else if ( o instanceof DeploymentStatus )
    	        {
	                 // not necessarily the same implementation + always make a copy
	                final DeploymentStatus in = (DeploymentStatus)o;
    	            ds  = new DeploymentStatusImpl( in );
    	        }
    	        else
    	        {
    	            throw new IllegalArgumentException();
    	        }
    	        ds.setParent(this);
    	        ssList.add( ds );
    	    }
    	    putField( SUB_STAGES_KEY, (Serializable)ssList );
	    }
	}
	
    /**
     */
     @Override
	    protected Serializable
	asMapHook( final String key, final Serializable value )
	{
		Serializable  result	= value;
	    
	    if ( SUB_STAGES_KEY.equals( key ) )
	    {
            // convert substages to Map as well
    	    final List<?> l  = List.class.cast( value );
    	    if ( l != null && l.size() != 0 )
    	    {
    	        final List<DeploymentStatus> lds =
    	            TypeCast.checkList( l, DeploymentStatus.class );
    	    
        	    final ArrayList<Map<String,Serializable>> maps =
        	        new ArrayList<Map<String,Serializable>>();
        	    
        	    for( final DeploymentStatus ds : lds )
        	    {
        	        maps.add( ds.asMap() );
        	    }
        	    result  = maps;
    	    }
	    }
	    else
	    {
	        result  = super.asMapHook(key, value );
	    }
	    
	    return result;
    }
	
		protected boolean
	validate()
	{
		boolean	valid	= getInteger( STAGE_STATUS_KEY ) != null;
		assert( valid ) : "STAGE_STATUS_KEY missing";
		if ( valid )
		{
		    if ( getString( STAGE_STATUS_MESSAGE_KEY ) == null )
		    {
		        putField( STAGE_STATUS_MESSAGE_KEY, "N/A" );
		    }
		}
		
		if ( valid )
		{
    	    final List<?> value  = List.class.cast( getField( SUB_STAGES_KEY ) );
    	    final List<DeploymentStatus>    subStages   = 
    		    TypeCast.checkList( value, DeploymentStatus.class );
		}
		
		return( valid );
	}
	
	
		public String
	getStageDescription()
	{
		return( getString( STAGE_DESCRIPTION_KEY ) );
	}
	
		public void
	setStageDescription( final String description )
	{
		putField( STAGE_DESCRIPTION_KEY, description );
	}
	
	
		public String
	getStageStatusMessage()
	{
		return( getString( STAGE_STATUS_MESSAGE_KEY ) );
	}
	
	
		public void
	setStageStatusMessage( final String message )
	{
		putField( STAGE_STATUS_MESSAGE_KEY, message );
	}
	
	
		public int
	getStageStatus()
	{
		return( getStatusCode() );
	}
	
		public void
	setStageStatus( int status )
	{
		setStatusCode( status );
	}
	
	    private List<DeploymentStatus>
	getDeploymentStatusField()
	{
	    final List<?> value  = List.class.cast( getField( SUB_STAGES_KEY ) );
	    final List<DeploymentStatus>    subStages   = 
		    TypeCast.checkList( value, DeploymentStatus.class );
		return subStages;
	}
	
		public void
	addSubStage( final DeploymentStatus status )
	{
		status.setParent( this );
		
	    List<DeploymentStatus> subStages  = getDeploymentStatusField();
		if ( subStages == null )
		{
			subStages	= new ArrayList<DeploymentStatus>();
			
			putField( SUB_STAGES_KEY, (Serializable)subStages );
		}
		
		subStages.add( status );
	}
	
		public Iterator<Map<String,Serializable>>
	getSubStages()
	{
	    List<Map<String,Serializable>>   maps = null;
	    
		final List<DeploymentStatus> subStages	= getSubStagesList();
		if ( subStages != null )
		{
	        maps    = new ArrayList<Map<String,Serializable>>();
    		for( final DeploymentStatus ds : subStages )
    		{
    		    maps.add( ds.asMap() );
    		}
		}
		else
		{
		    maps    = Collections.emptyList();
		}
		
		return maps.iterator();
	}
	
		public List<DeploymentStatus>
	getSubStagesList()
	{
		List<DeploymentStatus>	subStages	= getDeploymentStatusField();
		TypeCast.checkList( subStages, DeploymentStatus.class );
		
		if ( subStages == null )
		{
		    subStages   = Collections.emptyList();
		}
		else
		{
		    subStages   = Collections.unmodifiableList( subStages );
		}
		
		return subStages;
	}
	
		public DeploymentStatus
	getParent()
	{
		return( mParent );
	}
	
		public void
	setParent( final DeploymentStatus parent )
	{
		mParent	= parent;
	}
	
		public Throwable
	getStageThrowable()
	{
		return( getThrowable( ) );
	}
	
		public void
	setStageThrowable( Throwable t)
	{
		setThrowable( t );
	}


        public Map<String,Serializable>
    getAdditionalStatus()
    {
        return( getMap( ADDITIONAL_STATUS_KEY ) );
    }


        public void
    setAdditionalStatus( final Map<String,Serializable> additionalStatus )
    {
        if ( ! (additionalStatus instanceof Serializable) )
        {
            throw new IllegalArgumentException( "Class is not Serializable: " +
                additionalStatus.getClass().getName() );
        }

        putField( ADDITIONAL_STATUS_KEY, (Serializable)additionalStatus );
    }
 	
	
		public boolean
	equals( final Object	rhs)
	{
		boolean	equal	= false;
		
		if ( rhs instanceof DeploymentStatus && ! (rhs instanceof DeploymentStatusImpl) )
		{
			equal	= super.equals( new DeploymentStatusImpl( (DeploymentStatus)rhs ) );
		}
		else
		{
			equal	= super.equals( rhs );
		}
		
		return( equal );
	}
}








