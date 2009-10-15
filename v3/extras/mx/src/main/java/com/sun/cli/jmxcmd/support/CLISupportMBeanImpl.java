/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.cli.jmxcmd.support;
 
import java.lang.reflect.Array;
import java.lang.ClassNotFoundException;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Pattern;
import java.io.IOException;
import javax.management.*;

import org.glassfish.admin.amx.util.ArrayConversion;
import org.glassfish.admin.amx.util.ClassUtil;
import org.glassfish.admin.amx.util.stringifier.*;
import org.glassfish.admin.amx.util.ExceptionUtil;
import org.glassfish.admin.amx.util.RegexUtil;
import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.admin.amx.util.jmx.ObjectNameQueryImpl;
import org.glassfish.admin.amx.util.jmx.ReadWriteAttributeFilter;
import org.glassfish.admin.amx.util.jmx.stringifier.MBeanOperationInfoStringifier;

final class CLISupportMBeanImpl implements CLISupportMBean
{

    interface ClassQuery
    {
        public Class<?>	getClassForName( String argName )
            throws ClassNotFoundException;
    }

    final class AttributeClassQuery implements ClassQuery
    {

        final MBeanAttributeInfo []	mAttributeInfo;
        final int					mNumInfos;
            
        AttributeClassQuery( final MBeanAttributeInfo [] attributeInfo )
        {
            mAttributeInfo	= attributeInfo;
            mNumInfos		= Array.getLength( attributeInfo );
        }
        
            public Class<?>
        getClassForName( final String attributeName )
            throws ClassNotFoundException
        {
            Class<?>	theClass	= Object.class;
            
            for( int i = 0; i < mNumInfos; ++i )
            {
                final MBeanAttributeInfo	info	= mAttributeInfo[ i ];
                
                // System.out.println( "attr info: " + SmartStringifier.toString( info ) );
                
                if ( attributeName.equals( info.getName() ) )
                {
                    theClass	= ClassUtil.getClassFromName( info.getType() );
                    break;
                }
            }
            return( theClass );
        }
    }






    private static void debug( final Object o ) { System.out.println( SmartStringifier.toString(o) ); }
    
	static private final String		WILDCARD_PATTERN=",*";
	static private final String		WILDCARD_PATTERN_NOPROPS="*";
	
	private final static int	WILDCARD_NONE		= 0;	// "*"
	private final static int	WILDCARD_ALL		= 1;	// "*"
	private final static int	WILDCARD_READABLE	= 2;	// "*r"
	private final static int	WILDCARD_WRITEABLE	= 3;	// "*w"
	private final static int	WILDCARD_MIXED		= 4;	// expression
	
	private final static char	ALIASES_DELIM	= ' ';	// space
	
	final MBeanServerConnection	mConnection;
	final AliasMgr				mAliasMgr;
	
	final ObjectName		mAliasMgrName;
	
	final ArgParser			mParser;
	
	
	
	CLISupportMBeanImpl( final MBeanServerConnection  conn, AliasMgr aliasMgr )
		throws Exception
	{
		mConnection	= conn;
		mAliasMgr	= aliasMgr;
		
		mParser	= new ArgParserImpl();
		
		mAliasMgrName	= new ObjectName( CLISupportStrings.ALIAS_MGR_TARGET );
	}

		private static final int
	GetWildcardType( final String s )
	{
		int	type	= WILDCARD_NONE;
		
		if ( s.equals( "*" ) )
			type	= WILDCARD_ALL;
		else if ( s.equals( "*r" ) )
			type	= WILDCARD_READABLE;
		else if ( s.equals( "*w" ) )
			type	= WILDCARD_WRITEABLE;
		else if ( s.indexOf( "*" ) >= 0 )
			type	= WILDCARD_MIXED;
		else
			type	= WILDCARD_NONE;
		
		return( type );
	}
	
		private final boolean
	containsWildcard( final String [] strings )
	{
		boolean	containsWildcard	= false;
		
		for ( int i = 0; i < strings.length; ++i )
		{
			if ( GetWildcardType( strings[ i ] ) != WILDCARD_NONE )
			{
				containsWildcard	= true;
				break;
			}
		}
		return( containsWildcard );
	}
	
		private static void
	dm( Object o )
	{
		System.out.println( SmartStringifier.toString( o ) );
	}


		static AttributeList
	ParseAttributes(
		final String		input,
		final String		delim,
		final ClassQuery	classQuery)
		throws NoSuchMethodException, ClassNotFoundException, ArgParserException
	{
		final ArgParser	parser	= new ArgParserImpl();
		
		final ParseResult []	parsedArgs	= parser.Parse( input, true  );
		final int				numArgs	= Array.getLength( parsedArgs );
		
		final AttributeList	attrList	= new AttributeList();
		
		for( int i = 0; i < numArgs; ++i )
		{
			final ParseResult	arg	= parsedArgs[ i ];
			
			//dm( "arg " + i + " = " + arg );
		
			final String	argName		= arg.getName();
			
			try
			{
				//dm( "arg " + i + " argName = " + argName );
				final Class	theClass	= classQuery.getClassForName( argName );
				
				//dm( "arg " + i + " class = " + theClass.getName() );
				
				// theClass will be null if the attribute doesn't exist
				if ( theClass != null )
				{
					//dm( "matching arg " + i + " against " + theClass.getName() );
					final ParsedObject	parseResult	= MatchArg( arg, theClass );
					//dm( "matched arg " + i + ": " + SmartStringifier.toString( parseResult ) );
					
					final Object	value	= parseResult.mObject;
					final Attribute	attr	= new Attribute( argName, value );
					attrList.add( attr );
					//dm( "added Attribute " + i + ": " + SmartStringifier.toString( attr ) );
				}
			}
			catch( Exception e )
			{
			}
			
		}
		
		return( attrList );
	}

	
		private static ParsedObject []
	ParseArrayAsType( final ParseResult pr, final Class elementType )
		throws Exception
	{
		//debug( "ParseArrayAsType: parsing args as " + ClassUtil.getFriendlyClassname( elementType ) );
		
		final ParseResult []	elems	= (ParseResult [])pr.getData();
		
		// convert each argument into the appropriate type of object
		final int		numArgs	= Array.getLength( elems );

		final ParsedObject []	results	= new ParsedObject[ numArgs ];
		for ( int i = 0; i < numArgs; ++i )
		{
			// allow all exceptions to be thrown
			results[ i ]	= MatchArg( elems[ i ], elementType );
		}
		
		return( results );
	}


		private static void
	checkAssignable( Class<?> requiredClass, Class<?> assignee )
	{
		if ( ! requiredClass.isAssignableFrom( assignee ) )
		{
			final String	str	= requiredClass.getName() +
									" is NOT compatible with cast of type " + assignee.getName();
			//p( str );
			throw new IllegalArgumentException( str );
		}
	}
	
		private static void
	checkIsArray( Class theClass )
	{
		if ( ! ClassUtil.classIsArray( theClass ) )
		{
			throw new IllegalArgumentException( "does not accept an array" );
		}
	}
	
		private static Object
	populatePrimitiveArray( Class primitiveClass, ParsedObject [] parsedData )
	{
		final int	numItems	= Array.getLength( parsedData );
		Object		theArray	= Array.newInstance( primitiveClass, numItems );
		
		for( int i = 0; i < numItems; ++i )
		{
			final Object	value	= parsedData[ i ].mObject;
			
			switch( ClassUtil.getPrimitiveArrayTypeCode( theArray.getClass() ) )
			{
				default:	throw new IllegalArgumentException( "not a simple array" );
				
				case 'Z':	Array.setBoolean( theArray, i, ((Boolean)value).booleanValue() );	break;
				case 'B':	Array.setByte( theArray, i, ((Byte)value).byteValue() );		break;
				case 'C':	Array.setChar( theArray, i, ((Character)value).charValue() );	break;
				case 'S':	Array.setShort( theArray, i, ((Short)value).shortValue() );		break;
				case 'I':	Array.setInt( theArray, i, ((Integer)value).intValue() );		break;
				case 'J':	Array.setLong( theArray, i, ((Long)value).longValue() );		break;
				case 'F':	Array.setFloat( theArray, i, ((Float)value).floatValue() );		break;
				case 'D':	Array.setDouble( theArray, i, ((Double)value).doubleValue() );	break;
			}
		}
		
		return( theArray );
	}
	
	/*
		The only checks that need to be here are those that would allow instantiation of the class
		in spite of invalid values (from CLI point of view).  No checks need go here that will fail
		while the instantiation is being attempted.
	 */
		private static void
	CheckValidInstantiation( final Class theClass, String value )
	{
		// only allow booleans that are "true" or "false"
		if ( theClass == Boolean.class || theClass == boolean.class )
		{
			if ( ! (value.equalsIgnoreCase( "true"  ) || value.equalsIgnoreCase( "false" )) )
			{
				throw new IllegalArgumentException( "not a Boolean/boolean: " + value );
			}
		}
		
		// for numeric types, out of range checks will fail anyway, so don't bother checking here...
	}
		
		private static boolean
	isReservedWord_null( String s)
	{
		return( s != null && s.equalsIgnoreCase( "null" ) );
	}
	
	/**
		Parse an argument.
		
		@param arg				the parsed argument
		@param requiredClass	class specifying required Class of the argument (specify Object if no requirement)
		
		@return an object of the required type, or an exception
	 */
		static ParsedObject
	MatchArg( final ParseResult arg, final Class requiredClass )
		throws Exception
	{
		// important: this variable should contain the desired class of the resulting parsed object
		// It may be *different* than the actual type if logical class is a primitive type, because the parsed
		// object will be stored as an Object (non-primitive type).
		Class	logicalClass	= requiredClass;
		
		
		/* p( "MatchArg: \"" + arg + "\" against class " +
			ClassUtil.getFriendlyClassname( requiredClass.getName() ) ); */
		
		Object	resultingObject	= null;
		boolean	isNullArg		= arg.getData() == null;
		
		final int		argType		= arg.getType();
		
		// first check for quoted string
		if ( argType == ParseResult.LITERAL_STRING )
		{
			// extract the quoted string
			resultingObject	= arg.getData();
			logicalClass	= String.class;
		}
		else
		{
			Class	castClass	= null;
			
			final boolean haveCast	= arg.getTypeCast() != null;
			if ( haveCast )
			{
				//p( "MatchArg: cast = " + arg.getTypeCast() );
				// extract the class name
				final String	className	= ClassUtil.ExpandClassName( arg.getTypeCast() );
				castClass		= ClassUtil.getClassFromName( className );
				
				// don't check castClass here for compatibility; it could be an array
				//p( "MatchArg: castclass = " + arg.getTypeCast() );
			}
			assert( (haveCast && castClass != null) || (! haveCast && castClass == null ) );
			
			if ( argType == ParseResult.OTHER )
			{
				// not an array, it must be a simple type
				final String argString	= (String)arg.getData();
				
				if ( haveCast )
				{
					checkAssignable( requiredClass, castClass );
					logicalClass	= castClass;
				}
				
				// check for special case of a null object "null" or "NULL".
				// A cast is significant as it allows selection of a specific signature.
				if ( isReservedWord_null( argString )  )
				{
					if ( ClassUtil.IsPrimitiveClass( logicalClass ) )
					{
						throw new IllegalArgumentException( "primitive class '" +
							logicalClass.getName() + "' cannot take a null value" );
					}
					
					resultingObject	= null;
					isNullArg		= true;
				}
				else
				{
					if ( ! haveCast )
					{
						CheckValidInstantiation( logicalClass, argString );
					}
					
					resultingObject	= ClassUtil.InstantiateFromString( logicalClass, argString );
					//p( "ParseArg: parsed regular object: " + resultingObject.toString() + " as " + logicalClass );
				}
			}
			else if ( argType == ParseResult.ARRAY )
			{
				checkIsArray( requiredClass );
				
				/*
				  All arrays have a type for each element. Examples:
				  	type		element type	inner type
				  	int []		int					int
				  	int[][]		int[]				int
				  	int[][][]	int[][]				int
				  	
				  	If a cast is present, then the cast means to change the inner element type, so long as it is compatible
				  	with the required type.
				 */
				final Class elementClass		= ClassUtil.getArrayElementClass( requiredClass );
				final Class innerElementClass	= ClassUtil.getInnerArrayElementClass( requiredClass );
				
				logicalClass	= elementClass;		// by default, it's as-required
				
				/*
				p( "element class = " + elementClass.getName() );
				p( "inner element class = " + innerElementClass.getName() );
				p( "cast class = " + ((castClass == null) ? "null" : castClass.getName()) );
				p( "required class = " + requiredClass.getName() );
				*/
				
				// If the element type is also an array, alter it's type to reflect the cast.
				if ( haveCast)
				{
					checkAssignable( innerElementClass, castClass );
					
					if ( ClassUtil.classIsArray( elementClass )  )
					{
						if ( ! ClassUtil.IsPrimitiveClass( innerElementClass ) )
						{
							logicalClass	= ClassUtil.convertArrayClass( elementClass, castClass );
						}
					}
					else
					{
						logicalClass	= castClass;
					}
				}

				//p( "MatchArg: array element must be of type " + ClassUtil.getFriendlyClassname( logicalClass ) );
					
				final ParsedObject[] 	parsedData	= ParseArrayAsType( arg, logicalClass );
				
				// convert ParsedObjects into an array of proper type
				final int numItems	= Array.getLength( parsedData );
				
				if ( ClassUtil.IsPrimitiveClass( logicalClass ) )
				{
					resultingObject	= populatePrimitiveArray( logicalClass, parsedData );
				}
				else
				{
					final Object []	realArray	=
						ArrayConversion.createObjectArrayType( logicalClass, numItems );
					for( int i = 0; i < numItems; ++i )
					{
						realArray[ i ]	= parsedData[ i ].mObject;
						//p( "elem " + i + " = " + realArray[ i ].getClass().getName() );
					}
					
					resultingObject	= realArray;
				}
				logicalClass	= resultingObject.getClass();
			}
			else
			{
				assert( false );
			}
		}
		
		assert( (! isNullArg && resultingObject != null) || (isNullArg && resultingObject == null) );
		
		checkAssignable( requiredClass, logicalClass );
		
		final boolean	exactMatch		= logicalClass == requiredClass && arg.getTypeCast() != null;
		
		final ParsedObject	result	= new ParsedObject( resultingObject, logicalClass, exactMatch );
				
		return( result );
	}
	
    
        private static boolean
    isJavaIdentifier( final String s )
    {
        boolean isJava = false;
        
        if ( s.length() > 0 )
        {
            int codePoint = Character.codePointAt(s, 0);
            
            if ( Character.isJavaIdentifierStart(codePoint) )
            {
                isJava = true;
                
                final int codePointCount = Character.codePointCount(s,0, s.length());
                for( int i = 1; i < codePointCount; ++i )
                {
                    codePoint = Character.codePointAt(s, i);
                    if ( ! Character.isJavaIdentifierPart(codePoint) )
                    {
                        isJava = false;
                        break;
                    }
                }
            }
        }
        
        return isJava;
    }

		private ParseResult []
	ParseArguments(
			final String 	input,
			final Class		requiredType )
		throws ArgParserException
	{
		// it's legal to have no arguments; this is the invocation of an operation with no arguments
		if ( input == null )
		{
			return( new ParseResult [0] );
		}
		
		final ArgParser		parser	= new ArgParserImpl( );
		
		ParseResult []	results	= null;
		
		/*
		 	If it appears to be a named invocation, first try parsing that way.
		 	If that fails, just parse as an ordered invocation.
		 	The '=' must occur after the first character (or there would be no name).
		 	There are other syntactic restrictions of course, but this should eliminate
		 	all the cases where no '=' is present (probably 99.9% of the cases).
		 	The rest of the time a failed initial parse will detect any other issue.
		 */
		 
		final int idx = input.indexOf( "=" );
        boolean namedInvocation = false;
        if ( idx > 0 )
        {
            final String namePortion = input.substring(0, idx);
            namedInvocation = isJavaIdentifier(namePortion);
        }
        
		if ( namedInvocation )
		{
			try
			{
				results	= parser.Parse( input, true );
			}
			catch( Exception e )
			{
				// OK, fall through and parse as Ordered invocation
			}
		}
		
		if ( results == null )
		{
			results	= parser.Parse( input, false );
		}
		
		return( results );
	}
	

	
	/*
		Match operation based on all parsedArgs being named and matching names of the operation
		parameters.
	 */
		private int
	GetNamedParamIndex(
		final String		name,
		final MBeanParameterInfo []	infos)
	{
		int	paramIndex	= -1;
		
		final int	numParams	= Array.getLength( infos );
		for( int i = 0; i < numParams; ++i )
		{
			if ( infos[ i ].getName().equals( name ) )
			{
				paramIndex	= i;
				break;
			}
		}
		
		return( paramIndex );
	}
	
	
	/*
		Match operation based on all parsedArgs being named and matching names of the operation
		parameters.
	 */
		private int
	MatchNamedOperation(
		final MBeanOperationInfo	info,
		final ParseResult []		parsedArgs,
		final ParsedObject []		argsOut )
	{
		final int					numArgs	= Array.getLength( parsedArgs );
		final MBeanParameterInfo []	paramInfos	= info.getSignature();
		
		int	matchType	= MATCH_NONE;
		
		final ParseResult []	reorderedResults	= new ParseResult [ numArgs ];
		
		if ( Array.getLength( paramInfos ) == numArgs )
		{
			matchType	= MATCH_OK;
			
			for( int i = 0; i < numArgs; ++i )
			{
				final String	paramName	= parsedArgs[ i ].getName();

				if ( paramName == null || paramName.length() == 0 )
				{
					matchType	= MATCH_NONE;
					break;
				}
				
				final int	paramIndex	= GetNamedParamIndex( paramName, paramInfos );
				
				if ( paramIndex < 0 )
				{
					matchType	= MATCH_NONE;
					break;
				}
				
				reorderedResults[ paramIndex ]	= parsedArgs[ i ];
			}
			
			// names have now been used to place the arguments in the proper order.
			// See if there is a match based on type
			matchType	= MatchOperation( info.getSignature(), reorderedResults, argsOut );
		}
		
		
		return( matchType );
	}


	private static final int	MATCH_NONE	= 0;
	private static final int	MATCH_EXACT	= 1;
	private static final int	MATCH_OK	= 2;
	
	/*
		Match string arguments against possible operation signatures and return the arguments
		in an ParsedObject [].
	 */
		private int
	MatchOperation(
		final MBeanParameterInfo []	paramInfo,
		final ParseResult []		parsedArgs,
		final ParsedObject []		argsOut )
	{
		final int	numArgs	= Array.getLength( parsedArgs );
		
		int			matchType		= MATCH_NONE;
		
		if ( paramInfo.length == numArgs )
		{
			matchType		= MATCH_OK;
			
			try
			{
				boolean	exactMatch	= true;
				
				for ( int i = 0; i < numArgs; ++i )
				{
					final String	paramClassname	= paramInfo[ i ].getType();
					final Class		paramClass		= ClassUtil.getClassFromName( paramClassname );
					
					//p( "MatchOperation: matching " + ClassUtil.getFriendlyClassname(paramClassname ));
					
					argsOut[ i ]	= MatchArg( parsedArgs[ i ], paramClass );
					argsOut[ i ].mClass	= paramClass;	// in case it was a primitive class
					
					if ( ! argsOut[ i ].mExactMatch )
					{
						exactMatch	= false;
					}
				}
				if ( exactMatch )
				{
					matchType		= MATCH_EXACT;
				}
				
			}
			catch( Exception e )
			{
				matchType	= MATCH_NONE;
			}
		}
		
		if ( matchType != MATCH_NONE )
		{
			/* p( "MatchOperation: matched " + AutoStringifier.toString( info ) + " against " +
				ArrayStringifier.stringify( parsedArgs, ",", new ParseResultStringifier() )  ); */
		}
		
		return( matchType );
	}
	
	
	/*
		Find all matching operations by name.
		
		If there are case-sensitive matches, return those,
		and do not return any case-insensitive matches.
		
		Otherwise, return the case-insensitive matches.
	 */
		private MBeanOperationInfo []
	GetMatchingMBeanOperationInfo(
		final String					operationName,
		final MBeanOperationInfo []		operationInfos)
	{
		final List<MBeanOperationInfo>	caseSensitiveMatches	= new ArrayList<MBeanOperationInfo>();
		final List<MBeanOperationInfo>	caseInsensitiveMatches	= new ArrayList<MBeanOperationInfo>();
		
		final int	numOps	= operationInfos.length;
		for ( int i = 0; i < numOps; ++i )
		{
			final MBeanOperationInfo	operationInfo		= operationInfos[ i ];
			final String				operationInfoName	= operationInfo.getName();
			
			if ( operationInfoName.equals( operationName ) )
			{
				caseSensitiveMatches.add( operationInfo );
			}
			else if ( operationInfoName.equalsIgnoreCase( operationName ) )
			{
				//p( "GetMatchingMBeanOperationInfo: added: " + operationInfoName + " for " + operationName);
				caseInsensitiveMatches.add( operationInfo );
			}
		}
		
		// preferentially return the case-sensitive matches
		final List<MBeanOperationInfo>	opList	= caseSensitiveMatches.size() != 0 ?
									caseSensitiveMatches : caseInsensitiveMatches;
		
		return opList.toArray( new MBeanOperationInfo [ opList.size() ] );
	}
	
	
		MBeanOperationInfo
	FindPropertiesOperation( final MBeanOperationInfo [] candidates )
	{
		final int	numCandidates	= Array.getLength( candidates );
		MBeanOperationInfo	info	= null;
		
		for( int i = 0; i < numCandidates; ++i )
		{
			final MBeanParameterInfo []	paramInfos	= candidates[ i ].getSignature();
			
			if ( paramInfos.length == 1 &&
					paramInfos[ 0 ].getType().equals( "java.util.Properties" ) )
			{
				info	= candidates[ i ];
				break;
			}
		}
		
		return( info );
	}
	
	
		private int
	MatchOperation(
		final boolean				namedInvocation,
		final MBeanOperationInfo	info,
		final ParseResult []		parsedArgs,
		final ParsedObject []		argsOut )
	{
		int	matchType	= MATCH_NONE;
		
		if ( namedInvocation )
		{
			matchType	= MatchNamedOperation( info, parsedArgs, argsOut );
		}
		else
		{
			matchType	= MatchOperation( info.getSignature(), parsedArgs, argsOut );
		}
		return( matchType );
	}
	
	private static class MatchedOperation
	{
		public MBeanOperationInfo	mOperationInfo;
		public ParsedObject []		mArgs;
		
			private
		MatchedOperation( MBeanOperationInfo info, ParsedObject [] args )
		{
			mOperationInfo	= info;
			mArgs			= args;
		}
	}

		private MatchedOperation []
	MatchOperations(
		final String				requestedOperationName,
		final boolean				namedInvocation,
		final ParseResult []		parsedArgs,
		final MBeanOperationInfo []	candidates )
	{
		final int	numArgs			= parsedArgs.length;
		final int	numCandidates	= candidates.length;
		
		final List<MatchedOperation>	opList	= new ArrayList<MatchedOperation>();
				
		for ( int i = 0; i < numCandidates; ++i )
		{
			final ParsedObject []		objectArgs	=  new ParsedObject [ numArgs ];
			final MBeanOperationInfo	info	= candidates[ i ];
			
			final int	matchType	= MatchOperation( namedInvocation, info, parsedArgs, objectArgs );
			if ( matchType != MATCH_NONE )
			{
				final MatchedOperation	matched = new MatchedOperation( info, objectArgs );
				
				if ( matchType == MATCH_EXACT &&
					requestedOperationName.equals( info.getName() ) )
				{
					//p( "MatchOperations: EXACT MATCH" );
					opList.clear();
					opList.add( matched );
					break;
				}
				else
				{
					opList.add( matched );
					// keep going; could be more than one operation that matches
				}
			}
		}
			
		if ( namedInvocation )
		{
			if ( opList.size() == 0 )
			{
				// see if there is an operation which takes a java.util.properties
				final MBeanOperationInfo	propertiesOperation	= FindPropertiesOperation( candidates );
				
				if ( propertiesOperation != null )
				{
					final Properties	props	= new Properties();
					
					// add all name/value args to the Properties
					for( int i = 0; i < numArgs; ++i )
					{
						final ParseResult	arg	= parsedArgs[ i ];
						props.setProperty( arg.getName(), arg.getData().toString() );
					}
					
					final ParsedObject	arg	= new ParsedObject( props, Properties.class, false);
					final ParsedObject []	args	= new ParsedObject [ ]	{ arg };
					
					final MatchedOperation	matched = new MatchedOperation( propertiesOperation, args );
					opList.add( matched );
				}
			}
		}
		
		final MatchedOperation []	matches	= new MatchedOperation [ opList.size() ];
		opList.toArray( matches );
		
		return( matches );
	}
	
	
	private class ParsedObjectArrayStringifier implements Stringifier
	{
		final String	mOperationName;
		
		ParsedObjectArrayStringifier( String operationName )
		{
			mOperationName	= operationName;
		}
		
		
			 public String
		stringify( Object o )
		{
			final ParsedObject []	signature	= (ParsedObject [])o;
			final String			signatureStr	= ArrayStringifier.stringify( signature, "," );
			
			return( mOperationName + "(" + signatureStr + ")" );
		}
	}
		
		void
	throwMultiplePossibleOperationsException( final MatchedOperation [] matches )
	{
		// opList is an ArrayList of ParsedObject []
		final String opStr	= ArrayStringifier.DEFAULT.stringify( matches, ",");
		
		final String	baseStr	= "Ambiguous invocation (" + matches.length + " possible operations):\n";
		throw new IllegalArgumentException( baseStr + opStr +"\n");
	}
	
	
		void
	throwNoSuchMethodException(
		final String				operationName,
		final ParseResult []		parsedArgs,
		final MBeanOperationInfo[]	candidates )
		throws NoSuchMethodException
	{
		final String baseMsg	= "No matching signature found for " +
			operationName + "(" + ArrayStringifier.stringify( parsedArgs, "," ) + ")" +
			"\nPossible candidates:\n";
		
		String	msg	= baseMsg;
		
		if ( candidates.length == 0 )
		{
			msg	= msg + "<none>";
		}
		else
		{
			final StringBuffer	extra	= new StringBuffer();
			for( int i = 0; i < candidates.length; ++i )
			{
				final String	candidateString	= MBeanOperationInfoStringifier.DEFAULT.stringify( candidates[ i ] );
				
				extra.append( candidateString + "\n" );
			}
			msg	= baseMsg + extra.toString();
		}
		
		throw new NoSuchMethodException( msg );
	}
						
						
		private OperationData
	ParseOperation(
		final String	requestedOperationName,
		final String	argList,
		final MBeanOperationInfo [] operationInfos )
		throws Exception
	{
		final ParseResult []	parsedArgs	= ParseArguments( argList, Object.class );
		
		/*p( "\nParseOperation: " +
			operationName + "(" +
			ArrayStringifier.stringify( parsedArgs, "|", new ParseResultStringifier( '|')) + ")" );*/
			
		
		// select all the operations with matching names
		final MBeanOperationInfo []		candidates	=
			GetMatchingMBeanOperationInfo( requestedOperationName, operationInfos );
		if ( candidates.length != 0 )
		{
			// winnow it down to only those that match
			final boolean	namedInvocation	= parsedArgs.length != 0 &&
									parsedArgs[ 0 ].getName() != null;
									
			final MatchedOperation []	matches			= MatchOperations(
						requestedOperationName, namedInvocation, parsedArgs, candidates );


			// now see if we have exactly one match
			if ( matches.length != 1 )
			{
				if ( matches.length == 0 )
				{
					throwNoSuchMethodException( requestedOperationName, parsedArgs, candidates );
				}
				else
				{
					throwMultiplePossibleOperationsException( matches );
				}
			}
		
			final OperationData	data	=
					new OperationData( matches[ 0 ].mOperationInfo.getName(), matches[ 0 ].mArgs );
				
			return( data );
		}
		else
		{
			throw new NoSuchMethodException( "No operation with the name \"" + requestedOperationName + "\" exists." );
		}
		
	}
	
		private Set<String>
	resolveAttributeName(
		final MBeanAttributeInfo[]	infos,
		final String[]				allAttrNames,
		final String				attrExpr ) throws Exception
	{
		final int	wildcardType	= GetWildcardType( attrExpr );
		Set<String>			result	= null;
		
		if ( wildcardType == WILDCARD_ALL )
		{
			result	= ArrayConversion.arrayToSet( allAttrNames );
		}
		else if ( wildcardType == WILDCARD_READABLE )
		{
			final ReadWriteAttributeFilter	filter	= ReadWriteAttributeFilter.READABLE_FILTER;
			
			final MBeanAttributeInfo[]	filtered	= JMXUtil.filterAttributeInfos( infos, filter );
			result	= ArrayConversion.arrayToSet( JMXUtil.getAttributeNames( filtered  ) );
		}
		else if ( wildcardType == WILDCARD_WRITEABLE )
		{
			final ReadWriteAttributeFilter	filter	= ReadWriteAttributeFilter.WRITEABLE_FILTER;
			
			final MBeanAttributeInfo[]	filtered	= JMXUtil.filterAttributeInfos( infos, filter );
			result	= ArrayConversion.arrayToSet( JMXUtil.getAttributeNames( filtered  ) );
		}
		else if ( wildcardType == WILDCARD_MIXED )
		{
			result	= new HashSet<String>();
			
			// pattern matching, dude
			final Pattern	pattern	= Pattern.compile( RegexUtil.wildcardToJavaRegex( attrExpr ) );
			//System.out.println( "matching pattern: " + attrExpr );
			for( int i = 0; i < allAttrNames.length; ++i )
			{
				if ( pattern.matcher( allAttrNames[ i ] ).matches() )
				{
					result.add( allAttrNames[ i ] );
				}
			}
		}
		else if ( wildcardType == WILDCARD_NONE )
		{
			result	= Collections.singleton( attrExpr );
		}
		else
		{
			assert( false );
		}
		
		return( result );
	}
	
		private String []
	resolveAttributeNames(
		final ObjectName		objectName,
		final String[]			attrExprs ) throws Exception
	{
		final Set<String>		resolvedNames	= new HashSet<String>();
		final MBeanAttributeInfo[]	attrInfos	=
			mConnection.getMBeanInfo( objectName ).getAttributes();
		final String[]				attrNames	= JMXUtil.getAttributeNames( attrInfos );
		
		for( int i = 0; i < attrExprs.length; ++i )
		{
			final Set<String>	tempSet	= resolveAttributeName( attrInfos, attrNames, attrExprs[ i ] );
			resolvedNames.addAll( tempSet );
		}
		
		return resolvedNames.toArray( new String[ resolvedNames.size() ] );
	}


	/**
		Supports the mbean-get CLI command
		
		@param attrs	comma-separated list of attributes
		@param targets	space-separated list of targets
	 */
		 public ResultsForGetSet []
	mbeanGet( final String attrs, final String [] targets) throws Exception
	{
		if ( attrs == null || targets == null )
		{
			throw new IllegalArgumentException();
		}

		final ObjectName []			objects	= resolveTargets( targets );
		final String []				attrNames	= mParser.ParseNames( attrs );
		final ResultsForGetSet []	results	= new ResultsForGetSet[ objects.length ];
		final boolean				hasWildcard	= containsWildcard( attrNames );
		
		for ( int objectIndex = 0; objectIndex < objects.length; ++objectIndex )
		{
			final ObjectName	objectName	= objects[ objectIndex ];
			
			String []	actualAttrList	= null;

			// Each object will resolve differently
			if ( hasWildcard )
			{
				actualAttrList	= resolveAttributeNames( objectName, attrNames );
			}
			else
			{
				actualAttrList	= attrNames;
			}
			
			AttributeList 	idxResults	= null;
			final Set<String>	problemNames	= new HashSet<String>();
			try
			{
				/*p( "mbeanGet: getting for: " + objectName.toString() +
					", attrs = " + ArrayStringifier.stringify( actualAttrList, ", "));*/
				idxResults	= JMXUtil.getAttributesRobust( mConnection, objectName, actualAttrList, problemNames);
				assert( idxResults != null );
			}
			catch( java.io.IOException e )
			{
				// An IOException is a complete failure; get out
				throw e;
			}
			catch( InstanceNotFoundException e )
			{
				// failure is OK; return empty list as result
				idxResults	= new AttributeList();
			}
			
			results[ objectIndex ]	= new ResultsForGetSet( objectName, idxResults, problemNames);
		}
		
		return( results );
	}
	

	
	/**
		Supports the mbean-set CLI command
		
		@param attrs	comma-separated list of attributes/value pairs
		@param targets	space-separated list of targets
	 */
		 public ResultsForGetSet []
	mbeanSet( final String attrs, final String [] targets ) throws Exception
	{
		final ObjectName [] objects	= resolveTargets( targets );
		
		final int	numObjects	= Array.getLength( objects );
		
		final ResultsForGetSet []	results	= new ResultsForGetSet[numObjects ];
		
		for ( int objectIndex = 0; objectIndex < numObjects; ++objectIndex )
		{
			final ObjectName			objectName	= objects[ objectIndex ];
			final MBeanInfo				info		= mConnection.getMBeanInfo( objectName );
			final AttributeClassQuery	query		= new AttributeClassQuery( info.getAttributes() );
			final AttributeList			attrList	= ParseAttributes( attrs, ",", query);
			
			//System.out.println( "mbeanSet: attrs = " + SmartStringifier.toString( attrList ) );
			
			AttributeList idxResults	= null;
			try
			{
				idxResults	= mConnection.setAttributes( objectName, attrList );
			}
			catch( java.io.IOException e )
			{
				// An IOException is a complete failure; get out
				e.printStackTrace();
				throw e;
			}
			catch( Exception e )
			{
				e.printStackTrace();
				// failure is OK; return empty list as result
				idxResults	= new AttributeList();
			}
			//System.out.println( "mbeanSet: result attrs = " + SmartStringifier.toString( idxResults ) );
			
			results[ objectIndex ]	= new ResultsForGetSet( objectName, idxResults );
		}
		
		return( results );
	}
	

	/**
		We allow omitting the domain prefix and the ":" from an ObjectName.
		Supply the domain if not already present.
		
		This routine's job is to construct a valid ObjectName, not to see if the
		MBean actually exists.
	 */
		ObjectName
	createObjectName( String target )
		throws MalformedObjectNameException
	{
		if ( target.length() == 0 )
		{
			throw new IllegalArgumentException( "empty string is not a legal ObjectName" );
		}
		ObjectName	result	= null;
		
		try
		{
			// rather than try to interpret it to see if it's an ObjectName, just try it
			result	= new ObjectName( target );
		}
		catch( MalformedObjectNameException e )
		{
			// if no domain was specified, supply "*:" (all domains)
			// and supply ",*"
			final int	colonIndex	= target.indexOf( ':' );
			if ( colonIndex < 0 )
			{
				// no domain specified, try with our abbreviation
				final String	modifiedName	= "*:" + target;
				
				result	= convertToPattern( new ObjectName( modifiedName ) );
			}
			else if ( colonIndex == target.length() -1 )
			{
				// colon is last character; try adding "*" to the end.
				// no domain specified, try with our abbreviation
				final String	modifiedName	= target + "*";
				
				result	= new ObjectName( modifiedName );
			}
			else
			{
				// already contains a ":", so assume it's a domain preceeding it
				throw e;
			}
		}
		 	
		return( result );
	}
	
	/*
	
			if ( results.size() == 0 )
			{
				// special hack for S1AS8 which won't show an MBean in a query unless
				// GetMBeanInfo is done
				try
				{
					final MBeanInfo	info	= mConnection.getMBeanInfo( name );
					// if we got here, there is an MBean with this name
					results.add( name );
				}
				catch( Exception e )
				{
					// ignore
				}
			}
	*/
	
		boolean
	exists( final ObjectName name )
		throws java.io.IOException
	{
		return( mConnection.isRegistered( name ) );
	}
	
		ObjectName
	convertToPattern( ObjectName input )
		throws MalformedObjectNameException
	{
		ObjectName		newName		= input;
		final String	nameString	= input.toString();
		
		if ( ! nameString.endsWith( WILDCARD_PATTERN ) && 
			( ! nameString.endsWith( WILDCARD_PATTERN_NOPROPS ) ) )
		{
			final String	newNameString = nameString + WILDCARD_PATTERN;
			
			newName	= new ObjectName( newNameString );
		}
		else
		{
			// already a pattern, and it didn't previously match
		}
		
		return( newName );
	}
	
	/**
		Convert an ObjectName to a set of names for MBeans that actually exist.
		If the pattern is a fully qualified name,
		then the set will consist of a single item (if it exists).  If the pattern is a true
		pattern, then there may be 1 or more ObjectNames returned.
	 */
		 private Set<ObjectName>
	findObjects( final ObjectName name )
		throws MalformedObjectNameException, IOException
	{
		Set<ObjectName>	results	= Collections.emptySet();
		
		if ( name.isPattern())
		{
			// no choice, we must do a query
			results	= mConnection.queryNames( name, null );
			assert( results != null );
		}
		else
		{
			// it's not a pattern, see if it exists as a fully-qualified name
			if ( exists( name ) )
			{
				results	= Collections.singleton( name );
			}
			else
			{
				// doesn't exist, interpret it as a pattern
				final ObjectName	alt	= convertToPattern( name );
				
				if ( ! alt.equals( name ) )
				{
					results	= mConnection.queryNames( alt, null );
					assert( results != null );
				}
			}
		}
		
		return( results );
	}
	
 
	
	/**
		This character, when preceeding a target, logically nots the resolution of a target eg
		it causes the target resolution to select all ObjectNames not selected by the target.
	 */
	static final public char	NOT_CHAR	= '!';
	
	 /**
	 	Recursively resolve the target into 1 or more ObjectNames.
	 	The 'target' may be an alias, an ObjectName, or an abbreviated ObjectName.
	 	
	 	@param target	target to resolve
	  */
	 	Set<ObjectName>
	 resolveTarget( final String targetIn )
	 	throws MalformedObjectNameException, MBeanException, ReflectionException,
	 	InstanceNotFoundException, IOException
	 {
		Set<ObjectName>		objectNames		= new HashSet<ObjectName>();
		String	actualTarget	= targetIn;
		
		boolean	isNegation	= false;
		if ( targetIn.indexOf( NOT_CHAR ) == 0 )
		{	
			// it's a negation of the alias
			actualTarget	= targetIn.substring( 1, targetIn.length() );
			isNegation	= true;
		}
		
		if ( AliasMgrImpl.isValidAlias( actualTarget ) )
		{
			String	value	= null;
			
			try
			{
		 		value	= getAliasValue( actualTarget );
		 	}
		 	catch( Exception e )
		 	{
		 		// squelch
		 	}
		 	
		 	if ( value != null )
		 	{
			 	// could be an alias consisting of other aliases, so split it first
		 		final String []	components	= value.split( "" + ALIASES_DELIM );
		 		
		 		final int numComponents	= Array.getLength( components );
		 		for( int i = 0; i < numComponents; ++i )
		 		{
		 			objectNames.addAll( resolveTarget( components[ i ] ) );
		 		}
	 		}
	 	}
		else if ( targetIn.length() != 0 )
		{
			// not an alias, so it must be an ObjectName or pattern
			// OR an *abbreviated form* that we allow or a pattern
			final ObjectName	objectName	= createObjectName( actualTarget );
			final Set<ObjectName>			nameSet	= findObjects( objectName );
 			objectNames.addAll( nameSet );
		}
		
		if ( isNegation )
		{
			// get all MBeans, and remove the ones we've just resolved
			final Set<ObjectName>	tempSet	= resolveTarget( "*" );
			tempSet.removeAll( objectNames );
			objectNames	= tempSet;
		}
 		
 		return( objectNames );
	 }
	 /**
	 	Resolve the specified targets, which may be a mix of aliases and/or ObjectNames. An ObjectName in turn
	 	may be abbreviated, fully qualified, or an ObjectName pattern.
	 	The returned list will not contain any duplicates.
	 	
	 	@param targets	array of targets to resolve
	  */
	 	 public ObjectName []
	 resolveTargets( final String [] targets )
	 	throws MalformedObjectNameException, IOException,
	 		MBeanException, ReflectionException, InstanceNotFoundException
	 {
	 	final int	numTargets	= targets.length;
	 	
		final Set<ObjectName>	objectNames			= new HashSet<ObjectName>();
			
	 	for( int i = 0; i < numTargets; ++i )
	 	{
	 		String	name	= targets[ i ];
	 		
	 		final Set<ObjectName>	values	= resolveTarget( name );
	 		objectNames.addAll( values );
	 	}
	 	
	 	// make sure there are no duplicate names
		final ObjectName []	objectNamesArray	= new ObjectName[ objectNames.size() ];
		objectNames.toArray( objectNamesArray );
			
		return( objectNamesArray );
	 }

 	
 	
	 
	/**
		Supports the mbean-invoke CLI command
		
		@param operationName	name of operation to invoke
		@param argList			comma-separated list of operation arguments
		@param targets			space-separated list of targets
		
		@return array of InvokeResult[], one for each resolved target
	 */
		 public InvokeResult []
	mbeanInvoke(
		final String		operationName,
		final String		argList,
		final String []		targets )
		throws Exception
	{
		//debug( "CLISupportMBeanImpl.mbeanInvoke()" + operationName + "(" + argList + ") on " + SmartStringifier.toString( targets ));
	
		final ObjectName [] objectNames	= resolveTargets( targets );

		final int	numTargets	= objectNames.length;
		
		final InvokeResult	results[]	= new InvokeResult [ numTargets ];
		
		for( int i = 0; i < numTargets; ++i )
		{
			final ObjectName	objectName	= objectNames[ i ];
			final MBeanInfo		mbeanInfo	= mConnection.getMBeanInfo( objectName );
			boolean				parsedOK	= true;
		
			OperationData	data	= null;
			results[ i ]	= new InvokeResult( objectName, null, null );
			
			try
			{
				// parse anew for each object as data types could vary
				data	= ParseOperation( operationName, argList, mbeanInfo.getOperations() );
			}
			catch( NoSuchMethodException e )
			{
				// ignore, this object won't be processed
				// p( "mbeanInvoke: no such method: " + operationName + "()" );
				parsedOK	= false;
				results[ i ].mThrowable	= e;
			}
			catch( Exception e )
			{
				/* p( "mbeanInvoke: exception parsing \"" + operationName + "()\"" +
					" object = \"" + objectName.toString() + "\"" ); */
				results[ i ].mThrowable	= e;
				parsedOK	= false;
			}
		
			if ( parsedOK )
			{
				final String []		signature	= data.getSignature();
				final Object []		args		= data.getArgs();
				
				try
				{
					//debug( "mbeanInvoke: invoking operation: " + data.mName + "(" + ArrayStringifier.stringify( signature, ",", new ClassNameStringifier()) + ")" );
					
					results[ i ].mResult	= mConnection.invoke( objectName, data.mName, args, signature);
				}
				catch( java.io.IOException e )
				{
					// An IOException is a complete failure; get out
					throw e;
				}
				catch( Exception e )
				{
					results[ i ].mThrowable	= ExceptionUtil.getRootCause( e );
				}
			}
		}
		return( results );
	}
	
	/**
		Produce a new array of MBeanFeatureInfo containing only items
		found in the list of names
		
		Supports the wildcard character '*'
	 */
		private List<MBeanFeatureInfo>
	FilterInfoByName( final MBeanFeatureInfo [] infos, final String [] names)
	{
		final int numNames	= names.length;
		final int numInfos	= infos.length;
	
		final List<MBeanFeatureInfo>	filteredInfo	= new ArrayList<MBeanFeatureInfo>();
		
		// optimize for the "all" case; it has the most to lose in terms of speed
		if ( containsWildcard( names ) && names.length >= 1 && names[ 0 ] == "*" )
		{
			for( int i = 0; i < numInfos; ++i )
			{
				assert( infos[ i ] != null );
				filteredInfo.add( infos[ i ] );
			}
		}
		else
		{
			final Pattern[]	patterns	= RegexUtil.exprsToPatterns( names, Pattern.CASE_INSENSITIVE );
		
			for( int infoIndex = 0; infoIndex < numInfos; ++infoIndex )
			{
				final MBeanFeatureInfo	item	= infos[ infoIndex ];
				final String			name	= item.getName();
					
				for( int patternIndex = 0; patternIndex < numNames; ++patternIndex )
				{
					if ( patterns[ patternIndex ].matcher( name ).matches() )
					{
						filteredInfo.add( item );
						break;
					}
				}
			}
		}
		
		return( filteredInfo );
	}
	
	

	/*
		Supports the inspect CLI command
		
		@param patterns	space-separated list of ObjectName patterns
	 */
		 public InspectResult
	mbeanInspect( final InspectRequest request, final ObjectName name )
		throws Exception
	{
		final MBeanInfo			info		= mConnection.getMBeanInfo( name );
		final ObjectInstance	instance	= mConnection.getObjectInstance( name );
		
		final InspectResult	result	= new InspectResult( instance );
		result.includeDescription	= request.includeDescription;
		
		result.mbeanInfo	= info;
		
		final MBeanAttributeInfo []		attrs			= info.getAttributes();
		final MBeanOperationInfo []		ops				= info.getOperations();
		final MBeanConstructorInfo []	constructors	= info.getConstructors();
		final MBeanNotificationInfo []	notifs			= info.getNotifications();
		
		for( int i = 0; i < ops.length; ++i )
		{
			assert( ops[ i ] != null ) : "mbeanInspect: MBeanOperationInfo [] has null entries";
		}
		
		String	summary	= null;
		
		if ( request.includeSummary )
		{
			String description	= "";
			
			if ( request.includeDescription )
			{
				description	= "Description: " + info.getDescription() + "\n";
			}
			
			final String	nameDelim	= "-----";
			
			summary	= description +
						"Class: " + instance.getClassName() + "\n" +
						"Summary: attributes " + Array.getLength( attrs ) +
						", operations " + Array.getLength( ops ) +
						", constructors " + Array.getLength( constructors ) +
						", notifications " + Array.getLength( notifs );
		}
		result.summary	= summary;
		
		if ( request.attrs != null )
		{
			final String []		names	= mParser.ParseNames( request.attrs );
			
			final List<MBeanFeatureInfo> filteredInfo	= FilterInfoByName( attrs, names );
			
			final MBeanAttributeInfo []	results	= new MBeanAttributeInfo[ filteredInfo.size() ];
			result.attrInfo	= filteredInfo.toArray( results );
		}
		
		if ( request.operations != null )
		{
			final String []		names	= mParser.ParseNames( request.operations  );
			
			final List<MBeanFeatureInfo>	filteredInfo	= FilterInfoByName( ops, names );
			
			final MBeanOperationInfo []	results	= new MBeanOperationInfo[ filteredInfo.size() ];
			result.operationsInfo	= (MBeanOperationInfo [])filteredInfo.toArray( results );
		}
		
		if ( request.constructors )
		{
			result.constructorsInfo	= constructors;
		}
		
		if ( request.notifications != null )
		{
			result.notificationsInfo	= notifs;
		}
		
		return( result );
	}
	
		 public InspectResult []
	mbeanInspect( final InspectRequest request, final String [] targets )
		throws Exception
	{
		final ObjectName []		objectNames	= resolveTargets( targets );
		
		final int numObjects	= Array.getLength( objectNames );
		InspectResult []	results	= new InspectResult[ numObjects ];
		
		for( int i = 0; i < numObjects; ++i )
		{
			results[ i ]	= mbeanInspect( request, objectNames[ i ] );
		}
		
		return( results );
	}
	
	
		public ObjectName []
	mbeanFind( final String [] patterns )
		throws MalformedObjectNameException, IOException, MBeanException,
		ReflectionException, InstanceNotFoundException
	{
		final ObjectName []	names	= resolveTargets( patterns );
	
		return( names );
	}
	
	
		public ObjectName []
	mbeanFind( final String [] patterns, String regexList )
		throws MalformedObjectNameException, IOException, MBeanException,
		ReflectionException, InstanceNotFoundException
	{
		if ( regexList == null )
		{
			return( mbeanFind( patterns ) );
		}
		
		final ObjectName []	candidates = resolveTargets( patterns );
		
		
		final String []	pairs	= regexList.split( "," );
		
		final String []	regexNames	= new String[ pairs.length ];
		final String []	regexValues	= new String[ pairs.length ];
		
		for( int i = 0; i < pairs.length; ++i )
		{
			final String	pair	= pairs[ i ];
			final int	delimIndex	= pair.indexOf( '=' );
			
			if ( delimIndex < 0 )
			{
				throw new IllegalArgumentException( "Malformed regex pair: " + pair );
			}
			
			regexNames[ i ]		= pair.substring( 0, delimIndex );
			regexValues[ i ]	= pair.substring( delimIndex + 1, pair.length() );
		}
		
		final Set<ObjectName>		startingSet	= new HashSet<ObjectName>();
		startingSet.addAll( java.util.Arrays.asList( candidates ) );
		
		final ObjectNameQueryImpl	query	= new ObjectNameQueryImpl();
		final Set<ObjectName>	resultSet	= query.matchAll( startingSet, regexNames, regexValues );
		
		final ObjectName []	results	= new ObjectName [ resultSet.size() ];
		
		resultSet.toArray( results );
		
		return( results );
	}
	
	
		public void
	mbeanCreate( String name, String theClass, String argString ) throws Exception
	{
		final ObjectName	objectName	= new ObjectName( name );
		
		if ( argString != null )
		{
			throw new UnsupportedOperationException( "no support yet for constructors with arguments" );
		}
		
		mConnection.createMBean( theClass, objectName );
	}
	
	
	
		public void
	mbeanUnregister( String name ) throws Exception
	{
		final ObjectName	objectName	= new ObjectName( name );
		
		mConnection.unregisterMBean( objectName );
	}
	
		public int
	mbeanCount(  ) throws Exception
	{
		final Integer	mbeanCount	= mConnection.getMBeanCount( );
		
		return( mbeanCount.intValue() );
	}
	
		public String []
	mbeanDomains(  ) throws Exception
	{
		return( mConnection.getDomains() );
	}
	
	
		public String
	mbeanGetDefaultDomain(  ) throws Exception
	{
		return( mConnection.getDefaultDomain() );
	}
	
	
	
		public ObjectName[]
	mbeanListen(
		boolean	start,
		String [] targets,
		NotificationListener listener,
		NotificationFilter filter,
		Object handback ) throws Exception
	{
		final ObjectName []	names	= resolveTargets( targets );
		final List<ObjectName>		broadcasters	= new ArrayList<ObjectName>();
		
		for( int i = 0; i < targets.length; ++i )
		{
			try
			{
				if ( start )
				{
					// if no handback is specified, make it be the ObjectName being listened to
					final Object	actualHandback	= handback == null ? names[ i ] : handback;
					
					mConnection.addNotificationListener( names[ i ], listener, filter,
						 actualHandback );
				}
				else
				{
					mConnection.removeNotificationListener( names[ i ], listener  );
				}
				broadcasters.add( names[ i ] );
			}
			catch( java.io.IOException e )
			{
				// An IOException is a complete failure; get out
				throw e;
			}
			catch( Exception e )
			{
				//System.out.println( "Can't listen/stop listening to: " + names[ i ] + " (" + e.getMessage() + ")" );
			}
		}
		
		return( (ObjectName[])broadcasters.toArray( new ObjectName[ broadcasters.size() ]  ) );
	}

	
//-------------- access to AliasMgrMBean ------------------------------


	
		 String
	getAliasValue( String aliasName )
		throws Exception
	{
		final String result	= mAliasMgr.getAliasValue( aliasName );
		return( result );
	}
	
	
}







