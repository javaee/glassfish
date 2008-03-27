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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/stringifier/ObjectNameStringifier.java,v 1.5 2005/11/15 20:21:50 llc Exp $
 * $Revision: 1.5 $
 * $Date: 2005/11/15 20:21:50 $
 */
 
package com.sun.cli.jmxcmd.util.jmx.stringifier;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.Iterator;
import java.util.Arrays;

import javax.management.ObjectName;

import com.sun.cli.jcmd.util.stringifier.Stringifier;
import com.sun.cli.jcmd.util.misc.ListUtil;
import com.sun.cli.jcmd.util.misc.StringUtil;


/**
	Stringifier for an ObjectName which sorts the properties in the ObjectName
	for more consistent and readable output.
 */
public final class ObjectNameStringifier implements Stringifier
{
	public final static ObjectNameStringifier DEFAULT = new ObjectNameStringifier();
	
	private static List	PROPS	= null;
	
		private synchronized static List
	getPROPS()
	{
		if ( PROPS == null )
		{
		PROPS	= Collections.unmodifiableList( ListUtil.newListFromArray( new String[]
			{
				"j2eeType","type",
				"name",
				
				"J2EEDomain",
				"J2EEServer",
				"JVM",
				"Node",
				"J2EEApplication",
				
				"AppClientModule",
				
				"EJBModule",
				"EntityBean",
				"StatefulSessionBean",
				"StatelessSessionBean",
				"MessageDrivenBean",
				
				"WebModule", "Servlet",
				
				"ResourceAdapterModule",
				"JavaMailResource",
				"JCAResource",
				"JCAConnectionFactory",
				"JCAManagedConnectionFactory",
				"JDBCResource",
				"JDBCDataSource",
				"JDBCDriver",
				"JMSResource",
				"JNDIResource",
				"JTAResource",
				"RMI_IIOPResource",
				"URL_Resource",
				
			} ));
		}
		return( PROPS );
	}
	
	
	private List	mOrderedProps;
	private boolean	mOmitDomain;
	
		public
	ObjectNameStringifier()
	{
		this( getPROPS() );
	}
	
		public
	ObjectNameStringifier( final List props )
	{
		mOrderedProps	= props;
		mOmitDomain	= false;
	}
	
		public
	ObjectNameStringifier( final String[] props )
	{
		this( ListUtil.newListFromArray( props ) );
	}
	
	
	    private String
	makeProp( final String name, final String value )
	{
	    return( name + "=" + value );
	}

		public String
	stringify( Object o )
	{
		if ( o == null )
		{
			return( "null" );
		}


		final ObjectName	on	= (ObjectName)o;
		
		final StringBuffer	buf	= new StringBuffer();
		if ( ! mOmitDomain )
		{
			buf.append( on.getDomain() + ":" );
		}
		
		final Map<String,String>	props	= on.getKeyPropertyList();
		
		final List<String>	ordered	= new ArrayList( mOrderedProps );
		ordered.retainAll( props.keySet() );
		
		// go through each ordered property, and if it exists, emit it
		final Iterator	iter	= ordered.iterator();
		while ( iter.hasNext() && props.keySet().size() >= 2 )
		{
			final String	key		= (String)iter.next();
			final String	value	= (String)props.get( key );
			if ( value != null )
			{
				buf.append( makeProp( key, value ) + "," );
				props.remove( key );
			}
		}
		
		// emit all remaining properties in order
		final Set<String>		remainingSet = props.keySet();
		final String[]	remaining	= new String[ remainingSet.size() ];
		remainingSet.toArray( remaining );
		Arrays.sort( remaining );
		
		for( int i = 0; i < remaining.length; ++i )
		{
			final String	key		= remaining[ i ];
			final String	value	= (String)props.get( key );
			buf.append( makeProp( key, value )  + "," );
		}
		
		final String	result	= StringUtil.stripSuffix( buf.toString(), "," );

		return( result );
	}
	
		public List
	getProps()
	{
		return( mOrderedProps );
	}
	
		public void
	setProps( final List props )
	{
		mOrderedProps	= props;
	}
	
	
		public boolean
	getOmitDomain()
	{
		return( mOmitDomain );
	}
	
		public void
	setOmitDomain( final boolean omit )
	{
		mOmitDomain	= omit;
	}
}
























