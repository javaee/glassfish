/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jmxcmd/test/mbeans/CLISupportTestee.java,v 1.5 2004/10/14 19:06:47 llc Exp $
 * $Revision: 1.5 $
 * $Date: 2004/10/14 19:06:47 $
 */
 
package  com.sun.cli.jmxcmd.test.mbeans;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

import java.util.Properties;
import java.util.ArrayList;
import java.util.List;
import javax.management.*;
import org.glassfish.admin.amx.util.ClassUtil;
import org.glassfish.admin.amx.util.StringUtil;




/*
	A DynamicMBean used as a target for testing CLI
 */
public final class CLISupportTestee implements DynamicMBean
{
	final static String	VIRTUAL_OPERATION_NAME	= "virtualTest";
		
	final static String	D	= ",";	// delimiter
	
	final Map<String,Object>	mAttributes;

		private void
	p( Object o )
	{
		System.out.println( o.toString() );
	}
    
	final InvokeHelper	mInvokeHelper;
	
	
		public
	CLISupportTestee()
	{
		mInvokeHelper	= new InvokeHelper( this );
		
		mAttributes		= new HashMap<String,Object>();
	}
   
	
//-----------------------------------------------------------------------------

		public String []
	virtualTest()
	{
		return( new String [ 0 ] );
	}
	
	
		public String []
	testFoo( long [][] p1 )
	{
		return( new String [  ] { "[[C", "char" } );
	}
	

    public String testString( final String s )
    {
        return s;
    }

//-----------------------------------------------------------------------------


		public void
	testNamed( java.util.Properties props )
	{
	}


		public void
	testNamed( String p1)
	{
	}


		public void
	testNamed( String p1, String p2 )
	{
	}

		public void
	testNamed( String p1, String p2, String p3)
	{
	}

		public void
	testNamed( String p1, String p2, String p3, Properties p4)
	{
	}
//-----------------------------------------------------------------------------

		
		public void
   	testPropertiesOnly( Properties p1 )
    {
    }
    
		public void
    testProperties1Arg( String p1, Properties p2 )
    {
    }
    
		public void
    testProperties2Args( String p1, Properties p2, String p3)
    {
    }
    
//-----------------------------------------------------------------------------
	
	private MBeanInfo	mMBeanInfo	= null;
	
	
		public MBeanParameterInfo []
	createMBeanParameterInfos( Method m)
	{
		final Class []	parameterClasses	= m.getParameterTypes();
		
		final int					numParams	= Array.getLength( parameterClasses );
		final MBeanParameterInfo	infos []	= new MBeanParameterInfo[ numParams ];
		
		for( int i = 0; i < numParams; ++i )
		{
			// use parameter names of p1, p2, p3, etc
			final String	parameterName	= "p" + (i+1);
			
			final MBeanParameterInfo	info	=
				new MBeanParameterInfo( parameterName,
										parameterClasses[ i ].getName(),
										"parameter " + i + 1
										);
			
			infos[ i ]	= info;
		}
		
		return( infos );
	}
	
	public static String[]	ATTRIBUTE_DATA =
	{
		"Boolean^Value.Boolean",
		"boolean^value.Boolean",
		"Character&Value.Character",
		"character&value.Character",
		"Short*Value.Short",
		"short*value.Short",
		"Integer+Value.Integer",
		"integer+value.Integer",
		"Long!Value.Long",
		"long!value.Long",
		"Float@Value.Float",
		"float@value.Float",
		"Double#Value.Double",
		"double#value.Double",
		"String.Value.String",
		"string.value.String",
		
		"0Boolean^Value.Boolean",// illegal start char
		"1Boolean^Value.Boolean",// illegal start char
		"2Boolean^Value.Boolean",// illegal start char
		"3Boolean^Value.Boolean",// illegal start char
		"4Boolean^Value.Boolean",// illegal start char
	};
	
		public MBeanAttributeInfo[]
	createAttributeInfos()
	{
		final MBeanAttributeInfo[]	attributeInfos	= new MBeanAttributeInfo[ ATTRIBUTE_DATA.length ];
		
		for( int i = 0; i < ATTRIBUTE_DATA.length; ++i )
		{
			final String	data	= ATTRIBUTE_DATA[ i ];
			final int		delimIndex	= data.lastIndexOf( "." );
			final String	attributeClass	= "java.lang." +
								data.substring( delimIndex + 1, data.length() );
			final String	attributeName			= data.substring( 0, delimIndex );
			final String	attributeDescription	= "test attribute " + StringUtil.quote( attributeName );
			
			attributeInfos[ i ]	= new MBeanAttributeInfo(
				attributeName, attributeClass, attributeDescription, true, true, false );
			
			try
			{
				final Class	theClass	= ClassUtil.getClassFromName( attributeClass);
				mAttributes.put( attributeName, ClassUtil.InstantiateDefault( theClass ) );
			}
			catch( Exception e )
			{
				assert( false );
			}
		}
		
		return( attributeInfos );
	}
	
		public MBeanOperationInfo []
	createMBeanOperationInfos()
	{
		final Method []	allMethods	= this.getClass().getDeclaredMethods();
		
		final List<MBeanOperationInfo>	exportMethods	= new ArrayList<MBeanOperationInfo>();
		final int 		allCount	= Array.getLength( allMethods );
		for( int i = 0; i < allCount; ++i )
		{
			final Method	m	= allMethods[ i ];
			final String	name	= m.getName();
			
			if ( name.startsWith( "test" ) )
			{
				final String		description	= "tests '" + name + "' operation";
				final MBeanParameterInfo	parameterInfo []	= createMBeanParameterInfos( m );
				
				// our generic invoker will return the same type of result for each--
				// the signature that was invoked
				// final String		returnTypeString	= m.getReturnType().getName();
				final String		returnTypeString	= String [].class.getName();
				
				final MBeanOperationInfo	info	= new MBeanOperationInfo( name,
												 description,
												 parameterInfo,
												 returnTypeString,
												 MBeanOperationInfo.INFO );
				
				exportMethods.add( info );
			}
		}
		
		
		// add the virtual methods
		final MBeanOperationInfo []	virtualOperations	= createVirtualOperationInfos();
		for( int i = 0; i < Array.getLength( virtualOperations ); ++i )
		{
			exportMethods.add( virtualOperations[ i ] );
		}
		
		MBeanOperationInfo []	infos	= new MBeanOperationInfo[ exportMethods.size() ];
		
		infos	= (MBeanOperationInfo [])exportMethods.toArray( infos );
		
		
		return( infos );
	}
	

	/*
		All the base types we support
	 */
	private final static Class []	BASE_CLASSES	= 
	{
		char.class,
		Character.class,
		boolean.class,
		Boolean.class,
		short.class,
		Short.class,
		int.class,
		Integer.class,
		long.class,
		Long.class,
		float.class,
		Float.class,
		double.class,
		Double.class,
		Object.class,
		String.class,
		Properties.class,
		
		Number.class,
		java.math.BigInteger.class,
		java.math.BigDecimal.class,
		
		java.net.URI.class,
		java.net.URL.class
	};
	private final static int	NUM_BASE_CLASSES	= Array.getLength( BASE_CLASSES );
	
		public Class []
	getSupportedClassList()
	{
		ArrayList<Class<?>>	classes	= new ArrayList<Class<?>>();
		
		for( int i = 0; i < NUM_BASE_CLASSES; ++i )
		{
			Class	derivedClass	= BASE_CLASSES[ i ];
			
			classes.add( derivedClass );
			if ( derivedClass == Properties.class )
				continue;
			
			// 1D array
			derivedClass	= Array.newInstance( derivedClass, 0 ).getClass();
			classes.add( derivedClass );
			
			// 2D array
			derivedClass	= Array.newInstance( derivedClass, 0 ).getClass();
			classes.add( derivedClass );
			
			// we support any dimension arryas, but 3 is probably excessive from 
			// a testing standpoint
			/*
			// 3D array
			derivedClass	= Array.newInstance( derivedClass, 0 ).getClass();
			classes.add( derivedClass );
			*/
		}
		
		Class []	result	= new Class[ classes.size() ];
		classes.toArray( result );
		return( result );
	}
	
	
		public MBeanOperationInfo
	createVirtualOperationInfo( String operationName, Class<?> [] argClasses )
	{
		final int	numArgs	= Array.getLength( argClasses );
		
		// create a method for each supported class
		final List<MBeanParameterInfo>	paramInfos	= new ArrayList<MBeanParameterInfo>();
		
		for( int i = 0; i < numArgs; ++i )
		{
			final Class<?>	theClass	= argClasses[ i ];
			
			// create single parameter with name of "pX", where X is the index of the param 1,2,3, etc
			final String	parameterName	= "p" + (i + 1);
			final String	parameterDescription	= "parameter " + (i+1);
			final MBeanParameterInfo	param	= new MBeanParameterInfo( parameterName, theClass.getName(), parameterDescription );
			
			paramInfos.add( param );
		}
		
		MBeanParameterInfo []	infos	= new MBeanParameterInfo [ paramInfos.size() ];
		paramInfos.toArray( infos );
		
		// create operation with return type of String [] (which will return the signature)
		final String		returnTypeString	= String [].class.getName();
		final String		description	= "virtual operation";
		final MBeanOperationInfo	info	= new MBeanOperationInfo( operationName,
													 description,
													 infos,
													 returnTypeString,
													 MBeanOperationInfo.INFO );
		return( info );
	
	}
	
	
	
		public MBeanOperationInfo []
	createVirtualOperationInfos()
	{
		final String	operationName	= VIRTUAL_OPERATION_NAME;
		
		ArrayList<MBeanOperationInfo>	ops	= new ArrayList<MBeanOperationInfo>();
		
		/* create a method for each supported class consisting of a single parameter
		 */
		final Class []	classes		= getSupportedClassList();
		final int		numClasses	= classes.length;
		for( int i = 0; i < numClasses; ++i )
		{
			final Class []	classList	= new Class [] { classes[ i ] };
			final MBeanOperationInfo	info	= createVirtualOperationInfo( operationName, classList );
			ops.add( info );
		}
		
		// for each primitive type, create a signature with 3 parameters, each of which can vary between
		// the primitive type and the Object form.
		for( int i = 0; i < numClasses; ++i )
		{
			final Class	theClass	= classes[ i ];
			if ( ! ClassUtil.IsPrimitiveClass( theClass ) )
				continue;
				
			final Class objectClass	= ClassUtil.PrimitiveClassToObjectClass( theClass );
			
			// generate all 8 variants
			final Class []	both	= new Class [] { theClass, objectClass };
			for( int p1 = 0; p1 < 2; ++p1 )
			{
				for( int p2 = 0; p2 < 2; ++p2 )
				{
					for( int p3 = 0; p3 < 2; ++p3 )
					{
						final Class []	classList	= new Class [] { both[ p1 ], both[ p2 ], both[ p3 ] };
						MBeanOperationInfo	info	= createVirtualOperationInfo( operationName, classList );
						ops.add( info );
					}
				}
			}
		}
		
		
		// Create all method signature for depth of 2 with all variants of all supported types
		// CAUTION: this generates (num classes)^2 operations, about 7000 or so.
		final int	depth	= 2;
		final int	numCombinations	= numClasses * numClasses;	// must match depth
		for( int i = 0; i < numCombinations; ++i )
		{
			final Class []	classList	= new Class [ depth ];
		
			// number of assignments must match depth
			classList[ 0 ]	= classes[ i % numClasses ];
			classList[ 1 ]	= classes[ (i / numClasses) % numClasses ];
			
			if ( classList[ 0 ] == Properties.class || classList[ 1 ] == Properties.class )
			{
				// don't generate any methods with Properties; that is only for named invocation
				continue;
			}
			assert( classList[ 0 ] != Properties.class && classList[ 1 ] != Properties.class );
			
			final MBeanOperationInfo	info	= createVirtualOperationInfo( operationName + i, classList );
			ops.add( info );
		}
			
		
		MBeanOperationInfo []	infos	= new MBeanOperationInfo[ ops.size() ];
		infos	= (MBeanOperationInfo [])ops.toArray( infos );
		return( infos );
	}
	
		public synchronized MBeanInfo
	getMBeanInfo()
	{
		if ( mMBeanInfo == null )
		{
			final MBeanAttributeInfo []		attributeInfo	= createAttributeInfos();
			final MBeanConstructorInfo []	constructorInfo	= null;
			final MBeanNotificationInfo []	notificationInfo	= null;
			final MBeanOperationInfo []		operationInfo	= createMBeanOperationInfos();
			
			mMBeanInfo	= new MBeanInfo( this.getClass().getName(),
				"Test MBean for the CLI support code",
				attributeInfo,
				constructorInfo,
				operationInfo,
				notificationInfo );
		}
		
		return( mMBeanInfo );
	}
	
	
    	public Object
    getAttribute( String attributeName )
    	throws AttributeNotFoundException, MBeanException, ReflectionException
    {
    	final Object	result	= mAttributes.get( attributeName );
    	if ( result == null )
    	{
    		throw new AttributeNotFoundException( attributeName );
    	}
    	
    	//System.out.println( "### getAttribute: " + attributeName + "class = " + result.getClass().getName() );
    	
    	return( result );
    }
	
    	public AttributeList
    getAttributes(String[] attributes)
    {
    	final AttributeList	attrs	= new AttributeList();
    	
    	for( int i = 0; i < attributes.length; ++i )
    	{
    		final Object	value	= mAttributes.get( attributes[ i ] );
    		
    		if ( value != null )
    		{
    			attrs.add( new Attribute( attributes[ i ], value ) );
    		}
    	}
    	
    	return( attrs );
    }
    
    
    	public AttributeList
    setAttributes(AttributeList attributes)
    {
    	final AttributeList	successes	= new AttributeList();
    	
    	for( int i = 0; i < attributes.size(); ++i )
    	{
    		final Attribute	attr	= (Attribute)attributes.get( i );
    		
    		if ( mAttributes.containsKey( attr.getName() ) )
    		{
    			mAttributes.put( attr.getName(), attr.getValue() );
    			successes.add( attr );
    		}
    	}
    	
    	return( successes );
    }
    
    
    
		public void
	setAttribute(Attribute attribute)
		throws AttributeNotFoundException, InvalidAttributeValueException,
		MBeanException, ReflectionException
	{
    	if ( mAttributes.containsKey( attribute.getName() ) )
    	{
    		mAttributes.put( attribute.getName(), attribute.getValue() );
    	}
    	else
    	{
    		throw new AttributeNotFoundException( attribute.getName() );
    	}
    	
	}
    
        public Object
    invoke(
    	String	actionName,
    	Object	params[],
    	String	signature[] )
    	throws MBeanException, ReflectionException
    {
    	Object	result	= null;
    	
    	if ( actionName.startsWith( VIRTUAL_OPERATION_NAME ) )
    	{
    		return( signature );
    	}
    	
    	try
    	{
    		//p( "INVOKING HELPER: " + actionName + "(" + SmartStringifier.toString( params ) + ")");
    		result	= mInvokeHelper.invoke( actionName, params, signature );
    		
    		// ignore result, always return signature of invoked method
    		result	= signature;
    		//p( "SUCCESS: " + actionName + "(" + SmartStringifier.toString( params ) + ")");
    		
    	}
    	catch( Exception e )
    	{
    		e.printStackTrace();
    		throw new MBeanException( e );
    	}
    	
    	return( result );
    }
}



















