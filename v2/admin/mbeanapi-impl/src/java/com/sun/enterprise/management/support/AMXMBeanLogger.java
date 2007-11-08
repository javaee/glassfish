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
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/support/AMXMBeanLogger.java,v 1.4 2006/03/09 20:30:46 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2006/03/09 20:30:46 $
 */

package com.sun.enterprise.management.support;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.management.ObjectName;




import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.AMXLoggerBase;
import com.sun.appserv.management.base.LoggerSupport;
import com.sun.appserv.management.base.Util;

/**
	The logger class for all MBean loggers.
	<p>
	When constructing an MBean Logger,
	we derive its name from the ObjectName
	of the MBean.  The Javadoc for the logger class states:
	<i>Logger names can be arbitrary strings..."</i>.  This convention
	is followed in spirit; the logger name for an MBean is followed by
	forming a dotted string which consists of a common prefix followed by
	the hierarchy of parents of the form
	<j2eeType> or <j2eeType>:<name>, depending on whether the
	MBean has a name or not.
 */
public final class AMXMBeanLogger extends AMXLoggerBase
{
	private final ObjectName	mObjectName;
	
		private static String
	mangle( final String s )
	{
		// don't allow the '.' in a name part
		return( s.replaceAll( "\\.", "_" ) );
	}
	
	private static final String	TYPE_NAME_DELIM	= ":";
	
		private static String
	formTypeName(
		final String	j2eeType,
		final String	name )
	{
	    String  result  = null;
	    
	    if ( j2eeType == null )
	    {
	        result  = name;
	    }
	    else
	    {
    		String	pair	= mangle( j2eeType );
    		
    		if ( ! name.equals( ObjectNames.getSingletonName( j2eeType ) ) )
    		{
    			pair	= pair + TYPE_NAME_DELIM + mangle( name );
    		}
    		
    		result  = pair;
		}
		
		return( result );
	}
	
	
	/** 
		Derive a Logger name consistent with the package hierarchy.
	 */
		private static String
	createLoggerName( final ObjectName	objectName )
	{
		final String	j2eeType	= Util.getJ2EEType( objectName );
		final String	name		= Util.getName( objectName );
		final TypeInfos	infos	= TypeInfos.getInstance();
		
		final StringBuffer	buf	= new StringBuffer();
		buf.append( LoggerSupport.AMX_MBEAN_LOGGER_PREFIX  );
		buf.append( AMX.FULL_TYPE_DELIM + formTypeName( j2eeType, name ) );
		
		final String[]		typeChain	= infos.getJ2EETypeChain( objectName );
		for( int i = 0; i < typeChain.length - 1; ++i )
		{
			final String	type	= typeChain[ i ];
			String	value	= objectName.getKeyProperty( type );
			if ( value == null )
			{
				value	= AMX.NO_NAME;
			}
			
			buf.append( AMX.FULL_TYPE_DELIM + formTypeName( type, value ) );
		}

		return( buf.toString() );
	}
	
		private
	AMXMBeanLogger(
		final ObjectName	objectName )
	{
		this( objectName, null );
		throw new IllegalArgumentException();	// don't use this routine
	}
	
		private
	AMXMBeanLogger(
		final ObjectName		objectName,
		final String			resourceBundleName )
	{
		super( createLoggerName( objectName ), resourceBundleName );
		
		mObjectName	= objectName;
		throw new IllegalArgumentException();	// don't use this routine
	}
	
		public static Logger
	createNew( final ObjectName objectName )
	{
		final String	loggerName	= createLoggerName( objectName );
		
		return( Logger.getLogger( loggerName ) );
	}
	
	
	/**
		Override so that we can include the ObjectName in every LogRecord.
	 */
	public static final String	OBJECT_NAME_KEY	= "ObjectName";
		public void
	log( final LogRecord record )
	{
		final Object[]	existing	= record.getParameters();
		final int		numExisting	= existing == null ?  0 : existing.length;
		
		final Object[]		params	= new Object[ 1 + numExisting ];
		if ( existing != null )
		{
			System.arraycopy( existing, 0, params, 0, existing.length );
		}
		
		// follow convention by placing a Map with our stuff in the last position
		final Map<String,ObjectName> m	= new HashMap<String,ObjectName>();
		m.put( OBJECT_NAME_KEY, mObjectName );
		params[ params.length - 1 ]	= m;
		
		record.setParameters( params );
		
		super.log( record );
	}
}
















