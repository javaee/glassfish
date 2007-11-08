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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/test/CLISupportTestee.java,v 1.3 2005/12/25 03:45:52 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:52 $
 */
 
package  com.sun.cli.jmx.test;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

import java.util.Properties;
import java.util.ArrayList;
import javax.management.*;


import com.sun.cli.util.ArrayConversion;
import com.sun.cli.util.ClassUtil;

import com.sun.cli.util.stringifier.ArrayStringifier;
import com.sun.cli.util.stringifier.SmartStringifier;

import com.sun.cli.jmx.util.InvokeHelper;

/*
	A DynamicMBean used as a target for testing CLI
 */
public final class CLISupportTestee implements DynamicMBean
{
	final static String	VIRTUAL_OPERATION_NAME	= "virtualTest";
		
	final static String	D	= ",";	// delimiter

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
	
		public MBeanOperationInfo []
	createMBeanOperationInfos()
	{
		final Method []	allMethods	= this.getClass().getDeclaredMethods();
		
		final ArrayList	exportMethods	= new ArrayList();
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
		ArrayList	classes	= new ArrayList();
		
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
	createVirtualOperationInfo( String operationName, Class [] argClasses )
	{
		final int	numArgs	= Array.getLength( argClasses );
		
		// create a method for each supported class
		final ArrayList	paramInfos	= new ArrayList();
		
		for( int i = 0; i < numArgs; ++i )
		{
			final Class	theClass	= argClasses[ i ];
			
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
		
		ArrayList	ops	= new ArrayList();
		
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
			final MBeanAttributeInfo []		attributeInfo	= null;
			final MBeanConstructorInfo []		constructorInfo	= null;
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
    getAttribute(String attribute)
    	throws AttributeNotFoundException, MBeanException, ReflectionException
    {
    	throw new AttributeNotFoundException();
    }
	
    	public AttributeList
    getAttributes(String[] attributes)
    {
    	return new AttributeList();
    }
    
    
    	public AttributeList
    setAttributes(AttributeList attributes)
    {
    	return new AttributeList();
    }
    
    
    
		public void
	setAttribute(Attribute attribute)
		throws AttributeNotFoundException, InvalidAttributeValueException,
		MBeanException, ReflectionException
	{
    	throw new AttributeNotFoundException();
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
    		// p( "INVOKING HELPER: " + actionName + "(" + AutoStringifier.toString( signature ) + ")");
    		result	= mInvokeHelper.invoke( actionName, params, signature );
    		
    		// ignore result, always return signature of invoked method
    		result	= signature;
    		// p( "SUCCESS: " + actionName + "(" + AutoStringifier.toString( signature ) + ")");
    		
    	}
    	catch( Exception e )
    	{
    		// e.printStackTrace();
    		throw new MBeanException( e );
    	}
    	
    	return( result );
    }
}



















