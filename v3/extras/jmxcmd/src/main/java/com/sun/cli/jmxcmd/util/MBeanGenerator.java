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
package com.sun.cli.jmxcmd.util;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Arrays;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import com.sun.cli.jcmd.util.misc.ClassUtil;

import org.glassfish.admin.amx.util.jmx.MBeanAttributeInfoComparator;



import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.admin.amx.util.jmx.MBeanOperationInfoComparator;


/**
	Generate an MBean ".java" file.
 */
public class MBeanGenerator implements MBeanGeneratorHook
{
	boolean					mEmitComments;
	Map<String,Integer>     mCounts;
	AttributeNameMapper		mMapper;
	
		public 
	MBeanGenerator( )
	{
		mCounts			= null;
		mEmitComments	= true;
	}
	
	private final static String	TAB		= "\t";
	private final static String	NEWLINE	= "\n";
	private final static String	PARAM_DELIM	= ", ";
	public final static String	FINAL_PREFIX	= "final ";
	public final static int		IMPORT_THRESHOLD	= 2;
	
	
	private static final String	BRACKETS	= "[]";
	
		static String
	stripBrackets( String name )
	{
		String result	= name;
		
		while( result.endsWith( BRACKETS ) )
		{
			result	= result.substring( 0, result.length() - BRACKETS.length() );
		}
		
		return( result );
	}
				
	
		private static void
	countType( Map<String,Integer> counts, String typeIn )
	{
		final String	type	= stripBrackets( ClassUtil.getFriendlyClassname( typeIn ) );
		
		Integer	count	= counts.get( type );
		if ( count == null )
		{
			count	= new Integer( 1 );
		}
		else
		{
			count	= new Integer( count.intValue() + 1 );
		}
		
		counts.put( type, count );
	}
	
	/**
		Count how many times an Attribute type is used.
	 */
		public static void
	countTypes( Map<String,Integer> counts, MBeanAttributeInfo[]	infos )
	{
		for( int i = 0; i < infos.length; ++i )
		{
			countType( counts, infos[ i ].getType() );
		}
	}
	
	/**
		Count how many times the return type and parameter types are used.
	 */
		private static void
	countTypes( Map<String,Integer> counts, MBeanOperationInfo[]	infos )
	{
		for( int i = 0; i < infos.length; ++i )
		{
			countType( counts, infos[ i ].getReturnType() );
			
			final MBeanParameterInfo[]	params	= infos[ i ].getSignature();
			for( int p = 0; p < params.length; ++p )
			{
				countType( counts, params[ p ].getType() );
			}
		}
	}
	
	
	
		String
	getCodeClassname( String classname )
	{
		String	name	= ClassUtil.getFriendlyClassname( classname );
		
		if ( typeMayBeAbbreviated( name ) )
		{
			name	= ClassUtil.stripPackagePrefix( name );
		}
		
		return( name );
	}
	
	
		private Map<String,Integer>
	countAllTypes( MBeanInfo	info )
	{
		final Map<String,Integer>	counts	= new HashMap<String,Integer>();
		final MBeanAttributeInfo[]	attrInfos		= info.getAttributes();
		final MBeanOperationInfo[]	operationInfos	= info.getOperations();
		if ( attrInfos != null )
		{
			countTypes( counts, attrInfos );
		}
		if ( operationInfos != null )
		{
			countTypes( counts, operationInfos );
		}
		
		return( counts );
	}
	
		private String
	getImportBlock( Map<String,Integer> counts )
	{
		final StringBuffer	buf		= new StringBuffer();
		final Iterator		iter	= counts.keySet().iterator();
		
		while ( iter.hasNext() )
		{
			final String	key	= (String)iter.next();
			final Integer	count	= (Integer)counts.get( key );
			
			// if used twice or more, generate an import statement
			if ( count.intValue() >= IMPORT_THRESHOLD && ! isUnqualifiedType( key ) )
			{
				buf.append( "import " + key + ";" + NEWLINE );
			}
		}
		
		return( buf.toString() );
	}
	
		protected boolean
	isUnqualifiedType( String type )
	{
		return( type.indexOf( "." ) < 0 );
	}
	
	/**
		type must be the "friendly" name.
	 */
		protected boolean
	typeMayBeAbbreviated( String type )
	{
		final Integer	count	= mCounts.get( type );
		if ( count == null )
		{
			return( false );
		}
		
		return( count.intValue() >= IMPORT_THRESHOLD );
	}
	
	
		public String
	generate( final MBeanInfo	info, boolean emitComments )
	{
		mEmitComments	= emitComments;
		
		final StringBuffer	buf	= new StringBuffer();
		
		if ( mEmitComments )
		{
			buf.append( getHeaderComment( info ) + NEWLINE + NEWLINE );
		}
		
		buf.append( "package " + getPackageName( info ) + ";" + NEWLINE );
		
		mCounts	= countAllTypes( info );
		buf.append( NEWLINE + getImportBlock( mCounts ) + NEWLINE );
		
		if ( mEmitComments )
		{
			buf.append( getInterfaceComment( info ) + NEWLINE + NEWLINE );
		}
		String	interfaceName	= getClassname( info );
		buf.append( "public interface " + interfaceName + " \n{\n" );
		
		
		final MBeanAttributeInfo[]	attrInfos		= info.getAttributes();
		final MBeanOperationInfo[]	operationInfos	= info.getOperations();
		if ( attrInfos != null )
		{
			Arrays.sort( attrInfos, MBeanAttributeInfoComparator.INSTANCE );
			buf.append( generateAttributes( attrInfos ) );
		}
		if ( operationInfos != null )
		{
			if ( operationInfos.length != 0 )
			{
				Arrays.sort( operationInfos, MBeanOperationInfoComparator.INSTANCE );
			
				buf.append( NEWLINE + "// -------------------- Operations --------------------" + NEWLINE );
				buf.append( generateOperations( operationInfos ) );
			}
		}
		
		
		buf.append( "\n}" );
		
		return( buf.toString() );
	}
	
	
	
		protected String
	indent( String contents, String prefix)
	{
		final StringBuffer	buf	= new StringBuffer();
		if ( contents.length() != 0 )
		{
			final String[]		lines	= contents.split( NEWLINE );
			
			for( int i = 0; i < lines.length; ++i )
			{
				buf.append( prefix + lines[ i ] + NEWLINE);
			}
			
			buf.setLength( buf.length() - 1 );
		}
		
		return( buf.toString() );
	}
	
		protected String
	indent( String contents )
	{
		return( indent( contents, TAB ) );
	}
	
		protected String
	makeJavadocComment( String contents )
	{
		return( "/**" + NEWLINE + indent( contents ) + NEWLINE + "*/" );
	}
	
	
		protected String
	formMethod( String returnType, String name, String[] params, String[] names )
	{
		final String	begin	= "public " + getCodeClassname( returnType ) + TAB + name + "(";
		String			paramsString	= "";
		if ( params != null && params.length != 0 )
		{
			final StringBuffer	buf	= new StringBuffer();
			
			buf.append( " " );
			for( int i = 0; i < params.length; ++i )
			{
				buf.append( FINAL_PREFIX );
				buf.append( getCodeClassname( params[ i ] ) );
				buf.append( " " + names[ i ] );
				buf.append( PARAM_DELIM );
			}
			
			buf.setLength( buf.length() - PARAM_DELIM.length() );	// strip last ","
			buf.append( " " );
			paramsString	= buf.toString();
		}
		
		
		return( begin + paramsString + ");" );
	}
	
	/**
		Return a comment regarding the Attribute name if it was mapped to a different
		Java name.
	 */
		protected String
	getAttributeNameComment( String attributeName, String javaName )
	{
		String	comment	= "";
		
		if ( ! attributeName.equals( javaName ) )
		{

		}
		return( comment );
	}
	
		protected String
	generateAttributes( MBeanAttributeInfo[]	infos )
	{
		final StringBuffer	buf	= new StringBuffer();
		
		final String[]	typeTemp	= new String[ 1 ];
		final String[]	nameTemp	= new String[ 1 ];
		
		mMapper	= new AttributeNameMapperImpl( JMXUtil.getAttributeNames( infos ) );
		
		for( int i = 0; i < infos.length; ++i )
		{
			final MBeanAttributeInfo	info	= infos[ i ];
			
			final String	attributeName	= info.getName();
			final String	type			= info.getType();
			String	comment	= "";
			
			final String	javaName	= mMapper.originalToDerived( attributeName );
			
			if ( info.isReadable() )
			{
				if ( mEmitComments )
				{
					comment	= getGetterComment( info, javaName );
					if ( comment.length() != 0 )
					{
						buf.append( indent( comment )  + NEWLINE );
					}
				}
				buf.append( indent( formMethod( type, "get" + javaName, null, null) ) );
			}
			
			if ( info.isWritable() )
			{
				buf.append( NEWLINE );
				if ( mEmitComments )
				{
					comment	= getSetterComment( info, javaName );
					if ( comment.length() != 0 )
					{
						buf.append( indent( comment )  + NEWLINE );
					}
				}
				
				typeTemp[ 0 ]	= type;
				nameTemp[ 0 ]	= "value";
				
				buf.append( indent( formMethod( "void", "set" + javaName, typeTemp, nameTemp ) ) );
			}
			
			buf.append( NEWLINE + NEWLINE  );
		}
		
		return( buf.toString() );
	}
	
		protected String
	generateOperations( MBeanOperationInfo[]	infos )
	{
		final StringBuffer	buf	= new StringBuffer();
		
		for( int i = 0; i < infos.length; ++i )
		{
			final MBeanOperationInfo	info	= infos[ i ];
			final String	name		= info.getName();
			final String	returnType	= info.getReturnType();
			final MBeanParameterInfo[]	paramInfos	= info.getSignature();
			final int		impact		= info.getImpact();
			
			final String[]	paramTypes	= new String[ paramInfos.length ];
			for( int p = 0; p < paramInfos.length; ++p )
			{
				paramTypes[ p ]	= paramInfos[ p ].getType();
			}
			
			final String[]	paramNames	= getParamNames( info );
			
			if ( mEmitComments )
			{
				final String comment	= getOperationComment( info, paramNames );
				if ( comment.length() != 0 )
				{
					buf.append( NEWLINE + indent( comment )  + NEWLINE );
				}
			}
			
			final String method	= formMethod( returnType, name, paramTypes, paramNames );
			buf.append( indent( method ) + NEWLINE );
		}
		
		return( buf.toString() );
	}
	
		protected boolean
	isBoilerplateDescription( String description )
	{
		return( description == null || description.length() == 0 ||
			description.indexOf( "Attribute exposed for management" ) >= 0 ||
			description.indexOf( "Operation exposed for management" ) >= 0 ||
			description.indexOf( "No Description was available" ) >= 0);
	}
	
		public String[]
	getParamNames( MBeanOperationInfo info )
	{
		final MBeanParameterInfo[]	params	= info.getSignature();
		
		final String[]	names	= new String[ params.length ];
		
		for( int i = 0; i < params.length; ++i )
		{
			names[ i ]	= params[ i ].getName();
		}
		
		return( names );
	}
	
	
		public String
	getGetterSetterComment( MBeanAttributeInfo info, String actualName )
	{
		String	description	= info.getDescription() == null ? "" : info.getDescription();
		if ( isBoilerplateDescription( description ) )
		{
			description	= "";
		}
		
		final String	nameComment	= getAttributeNameComment( info.getName(), actualName );
		String			result	= null;
		
		if ( description.length() == 0 && nameComment.length() == 0 )
		{
			result	= "";
		}
		else
		{
			result	= description;
			if ( nameComment.length() != 0 )
			{
				if ( description.length() != 0 )
				{
					result	= result + NEWLINE;
				}
				result	= result + nameComment;
			}
			
			result	= makeJavadocComment( result );
		}

		return( result );
	}
	
		public String
	getGetterComment( MBeanAttributeInfo info, String actualName )
	{
		return( getGetterSetterComment( info, actualName ) );
	}
	
		public String
	getSetterComment( MBeanAttributeInfo info, String actualName )
	{
		return( getGetterSetterComment( info, actualName ) );
	}
	
		public String
	getOperationComment( MBeanOperationInfo info, final String[] paramNames )
	{
		final String	description	= info.getDescription();
		
		if ( description == null || isBoilerplateDescription( description ) )
		{
			return( "" );
		}
		
		final StringBuffer	buf	= new StringBuffer();
		
		final MBeanParameterInfo[]	signature	= info.getSignature();
		for( int i = 0; i < paramNames.length; ++i )
		{
			final String paramDescription	= signature[i].getDescription();
			
			buf.append( "@param " + paramNames[ i ] + TAB + paramDescription + NEWLINE );
		}
		
		final String	returnType	= getCodeClassname( info.getReturnType() );
		if ( ! returnType.equals( "void" ) )
		{
			buf.append( "@return " + returnType + NEWLINE );
		}

		return( makeJavadocComment( buf.toString() ) );
	}
	
		public String
	getHeaderComment( final MBeanInfo info )
	{
		return( makeJavadocComment( "" ) );
	}
		public String
	getInterfaceComment( final MBeanInfo info )
	{
		final String	comment	= "Implementing class was: " + info.getClassName();
		
		return( makeJavadocComment( comment ) );
	}
	
		public String
	getPackageName( final MBeanInfo info )
	{
		return( "mbeans" );
	}
	
	private static int	sCounter	= 0;
		private static synchronized final int
	getCounter()
	{
		return( sCounter++ );
	}

		public String
	getClassname( final MBeanInfo info )
	{
		// mangle the ObjectName into a class name
		return( "Interface" + getCounter() );
	}
	
		public String
	getExceptions( final MBeanOperationInfo info )
	{
		return( "" );
	}
}






