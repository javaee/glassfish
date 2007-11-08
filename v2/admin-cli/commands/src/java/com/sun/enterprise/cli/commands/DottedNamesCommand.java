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

package com.sun.enterprise.cli.commands;

import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.admin.util.ClassUtil;
import com.sun.enterprise.admin.util.ArrayConversion;
import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.AttributeNotFoundException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;


import java.util.Vector;
import java.util.HashMap;
import java.util.Arrays;


/**
 *  This is class handles get, set and list commands
 *  @version  $Revision: 1.3 $
 */
public class DottedNamesCommand extends S1ASCommand
{

    private static final String GET_COMMAND = "get";
    private static final String SET_COMMAND = "set";
    private static final String LIST_COMMAND = "list";
    private static final String GET_OPERATION = "dottedNameGet";
    private static final String LIST_OPERATION = "dottedNameList";
    private static final String GET_MONITORING_OPERATION = "dottedNameMonitoringGet";
    private static final String LIST_MONITORING_OPERATION = "dottedNameMonitoringList";
    private static final String SET_OPERATION = "dottedNameSet";
    private static final String MONITOR_OPTION = "monitor";
    private static final String OBJECT_NAME = "com.sun.appserv:name=dotted-name-get-set,type=dotted-name-support";
    private static final String INTERVAL_OPTION = "interval";
    private static final String ITERATIONS_OPTION = "iterations";
    private static final String PROPERTY_STRING = "property|system-property";


    /**
     *  This method validates the options for this command
     *  @return boolean returns true if success else returns false
     *  @throws CommandValidationException
     */
    public boolean validateOptions() throws CommandValidationException
    {
        // if monitor option is specified for a get operation and 
        // the interval and iterations specified along with it, we need to make 
        // sure that the values specified for interval and iterations
        // are valid
        if(getOperation().equals(GET_MONITORING_OPERATION))
        {
            if(!(isIntervalValid() && isIterationValid()))
                return false;
        }
		return super.validateOptions();
    }

    private boolean isIntervalValid(){
        
        String interval = getOption(INTERVAL_OPTION);
        
        if(interval == null)
            return true;
        else if((interval != null) && (Integer.parseInt(interval) > 0))
            return true;
        else
        {
            printMessage(getLocalizedString("InvalidInterval", new Object[] {interval}));
            return false;
        }
    }
    
    private boolean isIterationValid() {
        
        String iterations = getOption(ITERATIONS_OPTION);
        
        if(iterations == null)
            return true;
        else if((iterations != null) && (Integer.parseInt(iterations) > 0))
            return true;
        else
        {
            printMessage(getLocalizedString("InvalidIterations", new Object[] {iterations}));
            return false;
        }
    }
    
	/*
		Compare Attributes (for sorting).  Attribute by itself doesn't work correctly.
	 */
	private final class AttributeComparator implements java.util.Comparator
	{
			public int
		compare( Object o1, Object o2 )
		{
			final Attribute	attr1	= (Attribute)o1;
			final Attribute	attr2	= (Attribute)o2;
			
			return( attr1.getName().compareTo( attr2.getName() ) ); 
		}
		
			public boolean
		equals( Object other )
		{
			return( other instanceof AttributeComparator );
		}
	}
	
	/*
		Extract all attributes from the results into one large array without duplicates.
	*/
		private Attribute []
	collectAllAttributes( Object [] results )
	{
		// use a HashMap to eliminate duplicates; use name as the key
		final HashMap	attrs	= new HashMap();
		
		for( int i = 0; i < results.length; ++i )
		{
			final Object	result	= results[ i ];
			
			if ( result instanceof Attribute )
			{
				attrs.put( ((Attribute)result).getName(), result );
			}
			else if ( result instanceof Attribute[] )
			{
				final Attribute[]	list	= (Attribute[])result;
				
				for( int attrIndex = 0; attrIndex < list.length; ++attrIndex )
				{
					final Attribute	attr	= list[ attrIndex ];
					
					attrs.put( attr.getName(), attr );
				}
			}
			else
			{
				assert( result instanceof Exception );
			}
		}
		
		final Attribute[]	attrsArray	= new Attribute[ attrs.size() ];
		attrs.values().toArray( attrsArray );
		Arrays.sort( attrsArray, new AttributeComparator() );
		
		return( attrsArray );
	}
	
    /**
     *  An abstract method that Executes the command
     *  @throws CommandException
     */
    public void runCommand() throws CommandException, CommandValidationException
    {
		if(!validateOptions())
            return;

		//use http connector
		final MBeanServerConnection mbsc = getMBeanServerConnection(getHost(), 
									    getPort(), 
									    getUser(), 
									    getPassword());
		final Object[] params = getDottedNamesParam(getName().equals(LIST_COMMAND)?
                                                    false:true);
		final String[] types = new String[] {STRING_ARRAY};
        final String operationName = getOperation();
        final String interval = getOption(INTERVAL_OPTION);
        final String iterations = getOption(ITERATIONS_OPTION);
        
		try
		{
			// we always invoke with an array, and so the result is always an Object []
		    final Object[] returnValues = (Object[])mbsc.invoke(new ObjectName(OBJECT_NAME), 
					      		operationName, params, types);
			if ( operationName.indexOf( "List" ) >= 0)
			{
				// a list operation, just print the String []
				displayResultFromList( (String [])returnValues );
			}
            else if(operationName.equals(this.GET_MONITORING_OPERATION) && (interval != null) && (iterations != null))
            {
                String[]	userArgs	= (String [])params[ 0 ];
				displayResultFromGetOrSet( userArgs, returnValues );
                long interval_millis = Integer.parseInt(interval) * 1000;
                int counter = Integer.parseInt(iterations);
                int i = 1;
                do {
                    printMessage("\n");
                    if (i < counter) {
                        try {
                            Thread.currentThread().sleep(interval_millis);
                            Object[] retVals = (Object[]) mbsc.invoke(new ObjectName(OBJECT_NAME), 
                                                                      operationName, 
                                                                      params, 
                                                                      types);
                            userArgs = (String []) params[0];
                            displayResultFromGetOrSet(userArgs, retVals);
                            ++i;
                        } catch (InterruptedException ie) {
                        }
                    }
                }while(i < counter);
            }
			else
			{
				final String[]	userArgs	= (String [])params[ 0 ];
				
				displayResultFromGetOrSet( userArgs, returnValues );
			}
		}
		catch(Exception e)
		{
			final String	msg	= getExceptionMessage( e );
		    if ( msg != null)
		    {
				CLILogger.getInstance().printDetailMessage( msg );
			}
			
		    throw new CommandException(getLocalizedString("CommandUnSuccessful",
								  new Object[] {name} ), e);
		}        
    }


    /**
     * get the dotted notation from the operand and convert it to a Object[]
     * @return Object[]
     */
    private Object[] getDottedNamesParam(boolean convertUnderscore)
    {
        final Vector dottedNames = getOperands();
        String [] dottedNamesArray = new String[dottedNames.size()];
        
        for (int ii=0; ii<dottedNames.size(); ii++)
        {
            if (convertUnderscore)
                dottedNamesArray[ii] = convertUnderscoreToHyphen((String)dottedNames.get(ii));
            else
                dottedNamesArray[ii] = (String)dottedNames.get(ii);
        }
        return new Object[]{dottedNamesArray};
        
        //return new Object[] {(String[])dottedNames.toArray(new String[dottedNames.size()])};
    }


    /**
     *  get the operation to invoke depending on the command name and option
     *  if command name is "set" then the operation is dottedNameSet
     *  if command name is "get" then the operation is dottedNameGet
     *  if the command name is "get" with --monitor option to true, then the
     *  operation is dottedNameMonitoringGet
     *  all others return null.
     *  @return name of th operation 
     */
    private String getOperation()
    {
        if (getName().equals(SET_COMMAND))
            return SET_OPERATION;
        else if (getName().equals(GET_COMMAND))
        {	    
            if (getBooleanOption(MONITOR_OPTION))
                return GET_MONITORING_OPERATION;
            else
                return GET_OPERATION;
        }
        else if (getName().equals(LIST_COMMAND))
        {	    
            if (getBooleanOption(MONITOR_OPTION))
                return LIST_MONITORING_OPERATION;
            else
                return LIST_OPERATION;
        }
        return null;
    }

	private void	printMessage( String msg )
	{
		CLILogger.getInstance().printMessage( msg );
	}

		private String
	getExceptionMessage( Exception e )
	{
		String msg = null;

		if (e instanceof RuntimeMBeanException) {
			RuntimeMBeanException rmbe = (RuntimeMBeanException) e;
			msg     = rmbe.getTargetException().getLocalizedMessage();
		} else if (e instanceof RuntimeOperationsException) {
			RuntimeOperationsException roe = (RuntimeOperationsException) e;
			msg	= roe.getTargetException().getLocalizedMessage();
		} else {
			msg	= e.getLocalizedMessage();
		}
		if ( msg == null || msg.length() == 0 )
		{
			msg	= e.getMessage();
		}
			
		if ( msg == null || msg.length() == 0 )
		{
			msg	= e.getClass().getName();
		}
		
		return( msg );
	}
	
  


	private static String INNER_ARRAY_DELIM	= ",";	// when an array is inside another
	private static String OUTER_ARRAY_DELIM	= System.getProperty("line.separator");	// top-level array
	
    /**
     *  figure out the returnValue type and call appropriate print methods
     *  @params returnval
     *  @throws CommandException if could not print AttributeList
     */
    	private void
    displayResultFromGetOrSet( final String[] inputs, final Object [] returnValues )
    	throws Exception
    {
    	if ( returnValues.length == 1 )
    	{
    		// there was a single string provided as input
    		final Object	result	= returnValues[ 0 ];
    		
	    	if ( result instanceof Exception )
	    	{
	    		throw (Exception) result;
	    	}
			else if ( result.getClass() == Attribute[].class )
			{
				// this must have been the result of a wildcard input
				final Attribute[]	attrs	= (Attribute[])result;
				
				if ( attrs.length == 0 ) 
				{
				    throw new AttributeNotFoundException( getLocalizedString("NoWildcardMatches") );
				}
				
				printMessage( stringify( attrs, OUTER_ARRAY_DELIM ) );
			}
			else
			{
				printMessage( stringify( result, OUTER_ARRAY_DELIM ) );
			}
    	}
    	else
    	{
    		// more than one input String; collect all the resulting Attributes
    		// into one big non-duplicate sorted list and print them.
			final Attribute[]	attrs	= collectAllAttributes( returnValues );
			
    		printMessage(  stringify( attrs, OUTER_ARRAY_DELIM ) );
    		
    		// tell about any failures
    		for( int i = 0; i < returnValues.length; ++i )
    		{
    			if ( returnValues[ i ] instanceof Exception )
    			{
    				final Exception	e	= (Exception)returnValues[ i ];
    				
    				final String msg	= getLocalizedString( "ErrorInGetSet",
    					new Object [] { inputs[ i ], getExceptionMessage( e ) } );
    					
    				printMessage( msg );
    			}
    		}
    	}
    	
		return;
    }
    
	
		private String
	stringifyArray( final Object [] a, String delim )
	{
		final StringBuffer	buf	= new StringBuffer();
	
		for( int i = 0; i < a.length; ++i )
		{
			buf.append( stringify( a[ i ], INNER_ARRAY_DELIM ) );
			if ( i != a.length - 1 )
			{
				buf.append( delim );
			}
		}
		return( buf.toString() );
	}
	
		private String
	stringify( Object o )
	{
		return( stringify( o, "\n" ) );
	}

	/*
		Turn the object into a String suitable for display to the user.
	 */
		private String
	stringify( Object o, String delim )
	{
		String	result	= null;
		
		if ( o == null )
		{
			result	= "";
		}
		else if ( o instanceof Attribute )
		{
			final Attribute	attr	= (Attribute)o;
			
			result	= attr.getName() + " = " +  stringify( attr.getValue(), INNER_ARRAY_DELIM );
		}
		else if ( ClassUtil.objectIsPrimitiveArray( o ) )
		{
		   final Object [] objectList = ArrayConversion.toAppropriateType( o );
		   
		   result	= stringifyArray( objectList, delim );
		}
		else if ( ClassUtil.objectIsArray( o ) )
		{
			result	= stringifyArray( (Object [])o, delim );
		}
		else if ( o instanceof Exception )
		{
			final Exception e	= (Exception)o;
			
			result	= getExceptionMessage( e );
		}
		else
		{
			result	= o.toString();
		}
		
		assert( result != null );
		return( result );
	}

	

	
    private void displayResultFromList( final String [] result )
    {
        if ( result.length == 0 )
        {
            //need to convert the operands to String for display

            final String displayOperands = stringify(getDottedNamesParam(true),
                                                     INNER_ARRAY_DELIM);
            printMessage(getLocalizedString("EmptyList", new Object[] {
                displayOperands }));
        }
        else
        {
            printMessage( stringify( result, OUTER_ARRAY_DELIM ) );
        }
    }


    /**
     *  This method will convert the attribute in the dotted name notation
     *  from underscore to hyphen.
     *  @param param - the dotted name to convert
     *  @return the converted string
     */
    public String convertUnderscoreToHyphen(String param)
    {
        int endIndex = param.indexOf('=');
        int begIndex = (endIndex>0)? param.lastIndexOf('.', endIndex):param.lastIndexOf('.');
        if(begIndex<1 || checkPropertyToConvert(param.substring(0,begIndex)))
           return param;
        if(endIndex>0)
           return param.substring(0,begIndex) + param.substring(begIndex,endIndex).replace('_', '-') + param.substring(endIndex);
        else
           return param.substring(0,begIndex) + param.substring(begIndex).replace('_', '-');
    }

    /**
     * This method checks if the element in the dotted name contains "property"
     * or "system-property".  If the element is "property" or "system-property"
     * then return true else return false.
     * @param param - dotted name
     * @return true if dotted name contains "property" or "system-property"
     *         false
     */
    public boolean checkPropertyToConvert(String param)
    {
        final int index = param.lastIndexOf('.');
        if (index < 0) return false;
        final String elementName = param.substring(index+1);
        if (elementName.matches(PROPERTY_STRING))
            return true;
        else
            return false;
    }
    
	
}
