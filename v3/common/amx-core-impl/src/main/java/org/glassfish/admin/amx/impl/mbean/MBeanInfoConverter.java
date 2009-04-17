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

package org.glassfish.admin.amx.impl.mbean;

import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.admin.amx.util.ClassUtil;
import org.glassfish.admin.amx.util.StringUtil;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
	Convert MBeanInfo which was derived from public client-side interface into
	MBeanInfo useable by server-side MBeans.
 */
public final class MBeanInfoConverter 
{
	private final Map<Class,MBeanInfo>	mConvertedInfos;
	private static MBeanInfoConverter	INSTANCE	= null;
	
		private
	MBeanInfoConverter( )
	{
		mConvertedInfos	= Collections.synchronizedMap( new HashMap<Class,MBeanInfo>() );
	}
	
		public static synchronized MBeanInfoConverter
	getInstance()
	{
		if ( INSTANCE == null )
		{
			INSTANCE	= new MBeanInfoConverter();
		}
		
		return( INSTANCE );
	}
	
    private static final Map<String,Class>  NAMES_TO_CLASSES =
        Collections.synchronizedMap( new HashMap<String,Class>() );
    
		protected static Class
	toClass( final String className )
	{
        Class theClass  = NAMES_TO_CLASSES.get( className );
        if ( theClass != null )
            return theClass;
            
		try
		{
			theClass    = ClassUtil.getClassFromName( className );
            NAMES_TO_CLASSES.put( className, theClass );
            return theClass;
		}
		catch( ClassNotFoundException e )
		{
			assert( false );
			throw new RuntimeException( e );
		}
	}
	
	private final static String	OBJECT_NAME_SUFFIX	= "ObjectName";
	private final static String	SET_SUFFIX	= "Set";
	private final static String	MAP_SUFFIX	= "Map";
	private final static String	LIST_SUFFIX	= "List";
	private final static String	OBJECT_NAME_MAP_SUFFIX	= OBJECT_NAME_SUFFIX + MAP_SUFFIX;
	private final static String	OBJECT_NAME_SET_SUFFIX	= OBJECT_NAME_SUFFIX + SET_SUFFIX;
	private final static String	OBJECT_NAME_LIST_SUFFIX	= OBJECT_NAME_SUFFIX + LIST_SUFFIX;
		
		
		protected void
	trace( Object o )
	{
		//System.out.println( org.glassfish.admin.amx.util.stringifier.SmartStringifier.toString( o ) );
	}
	
		private final MBeanAttributeInfo
	convert( final MBeanAttributeInfo info )
	{
		MBeanAttributeInfo	result	= info;
		final String	name	= info.getName();
		
		final Class	type	= toClass( info.getType() );
		
		String	newName	= null;
		Class	newType	= type;
		
		if ( AMXProxy.class.isAssignableFrom( type ) )
		{
			newName	= name + OBJECT_NAME_SUFFIX;
			newType	= ObjectName.class;
		}
		else if ( name.endsWith( SET_SUFFIX ) &&
		     ! name.endsWith( OBJECT_NAME_SET_SUFFIX ) ) 
		{
			newName	= convertMethodName( name, SET_SUFFIX, OBJECT_NAME_SET_SUFFIX );
		}
		else if ( name.endsWith( MAP_SUFFIX ) &&
		     ! name.endsWith( OBJECT_NAME_MAP_SUFFIX )
		    ) 
		{
			newName	= convertMethodName( name, MAP_SUFFIX, OBJECT_NAME_MAP_SUFFIX );
		}
		else if ( name.endsWith( LIST_SUFFIX ) &&
		     ! name.endsWith( OBJECT_NAME_LIST_SUFFIX )
		    ) 
		{
			newName	= convertMethodName( name, LIST_SUFFIX, OBJECT_NAME_LIST_SUFFIX );
		}
		
		if ( newName != null )
		{
			trace( ClassUtil.stripPackageName( type.getName() ) + " " + name +
				" => " + ClassUtil.stripPackageName( newType.getName() ) + " " + newName );
				
			result	= new MBeanAttributeInfo( newName, newType.getName(),
				info.getDescription(), info.isReadable(), info.isWritable(), info.isIs() );
		}
		
		return( result );
	}
	
		protected static String
	convertMethodName(
		final String srcName,
		final String srcSuffix,
		final String resultSuffix )
	{
		return( StringUtil.replaceSuffix( srcName, srcSuffix, resultSuffix ) );
	}
	
		private final MBeanOperationInfo
	convert( final MBeanOperationInfo info )
	{
		MBeanOperationInfo	result	= info;
		final String	name	= info.getName();
		
		final Class	returnClass	= toClass( info.getReturnType() );
		
		String	newName	= null;
		Class	newReturnClass	= returnClass;
		
		if ( AMXProxy.class.isAssignableFrom( returnClass ) )
		{
			/*
				Anything returning an AMX (or sub-interface) must necessarily
				return an ObjectName from the MBean.
			 */
			newReturnClass	= ObjectName.class;
			
			/*
				Except for create() and createAbc() methods, we tack on 
				OBJECT_NAME_SUFFIX to the MBean operation.  AMXProxyHandler
				expects this convention.
			 */
			if ( name.startsWith( "create" ) )
			{
				newName			= name;
			}
			else
			{
				newName			= name + OBJECT_NAME_SUFFIX;
			}
		}
		else if ( Map.class.isAssignableFrom( returnClass ) &&
			name.endsWith( MAP_SUFFIX ) &&
			! name.endsWith( OBJECT_NAME_MAP_SUFFIX ) )
		{
			newName			= convertMethodName( name, MAP_SUFFIX, OBJECT_NAME_MAP_SUFFIX );
		}
		else if ( Set.class.isAssignableFrom( returnClass ) &&
			name.endsWith( SET_SUFFIX ) &&
			! name.endsWith( OBJECT_NAME_SET_SUFFIX ))
		{
			newName			= convertMethodName( name, SET_SUFFIX, OBJECT_NAME_SET_SUFFIX );
		}
		else if ( Set.class.isAssignableFrom( returnClass ) &&
			name.endsWith( LIST_SUFFIX ) &&
			! name.endsWith( OBJECT_NAME_LIST_SUFFIX ))
		{
			newName			= convertMethodName( name, LIST_SUFFIX, OBJECT_NAME_LIST_SUFFIX );
		}
		
		if ( newName != null )
		{
			trace( ClassUtil.stripPackageName( returnClass.getName() ) + " " + name + "(...)" +
				" => " + ClassUtil.stripPackageName( newReturnClass.getName() ) + " " + newName + "(...)" );
				
			result	= new MBeanOperationInfo(
				newName,
				info.getDescription(),
				info.getSignature(),
				newReturnClass.getName(),
				info.getImpact() );
		}
		
		return( result );
	}

		private final MBeanAttributeInfo[]
	convertAttributes( final MBeanAttributeInfo[]	origInfos )
	{
		final MBeanAttributeInfo[]	infos	= new MBeanAttributeInfo[ origInfos.length ];
		
		for( int i = 0; i < infos.length; ++i )
		{
			infos[ i ]	= convert( origInfos[ i ] );
		}
		
		return( infos );
	}
	
		private final MBeanOperationInfo[]
	convertOperations( final MBeanOperationInfo[]	origInfos )
	{
		final MBeanOperationInfo[]	infos	= new MBeanOperationInfo[ origInfos.length ];
		
		for( int i = 0; i < infos.length; ++i )
		{
			infos[ i ]	= convert( origInfos[ i ] );
		}
		
		return( infos );
	}
	
	
		private final MBeanInfo
	find( final Class theInterface )
	{
		return( mConvertedInfos.get( theInterface ) );
	}
	
		public final MBeanInfo
	convert(
		final Class					theInterface,
		final MBeanAttributeInfo[]	extraAttributeInfos )
	{
		MBeanInfo	result	= null;
		
        result	= find( theInterface );
        if ( result == null )
        {
        // no big deal if this were threaded; we'd do it twice; last one wins
            final MBeanInfo	origInfo	= JMXUtil.interfaceToMBeanInfo( theInterface );
            
            final MBeanAttributeInfo[]	origAttrInfos	= origInfo.getAttributes();
            final MBeanAttributeInfo[]	attrInfos	= extraAttributeInfos == null ?
                origAttrInfos :
                JMXUtil.mergeMBeanAttributeInfos( origAttrInfos, extraAttributeInfos);
            
            result	= new MBeanInfo(
                    origInfo.getClassName(),
                    origInfo.getDescription(),
                    convertAttributes( attrInfos ),
                    origInfo.getConstructors(),
                    convertOperations( origInfo.getOperations() ),
                    origInfo.getNotifications() );
            
            mConvertedInfos.put( theInterface, result );
        }
        return( result );
    }
}








