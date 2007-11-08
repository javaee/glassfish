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

package com.sun.enterprise.admin.common;

import java.io.Serializable;
import java.util.*;


/**
	ParamList encapsulates a list of Param objects
	<p>
	The exact storage method is an implementation detail.  ParamList is
	intended to be used by building it, then using it; it is not intended
	for removal, sorting, etc operations.
	@author Lloyd Chambers
	@version 1.0
*/

public class ParamList implements Serializable
{
    	public static long serialVersionUID = 5433279640964105821L;

	private Vector mParams;

    private static final String		kDelimiterString	     = ", ";
	private static final int		kInitialCapacity         = 4;

	/**
		Constructs a new, empty ParamList with a small initial capacity.
	*/
	
	public	ParamList()
	{
		mParams	= new Vector( kInitialCapacity );
	}

	/**
		Adds a new paramater to the list. If the <it>same</it> 
                parameter is passed again, it will replace the existing one.

		@param param	the Param to be added to the list
	*/
        
	public void addParam( Param param )
	{
            //Assert.insist(param!=null, "null");
            int elementIndex =  mParams.indexOf( param );
            boolean paramExists = ( elementIndex != -1 );
            
            if( paramExists ) 
            {
                mParams.setElementAt ( param, elementIndex );
            }
            else
            {
                mParams.add( param );
            }
	}

	/**
            Adds a new paramater to the end of the list by creating it
            from the specified name and value.
            
            @param name		the name to be used to create the Param
            @param value	the value to be used to create the Param
	*/
	
	public void addParam( String name, Serializable value )
	{
		this.addParam( new Param (name, value ) );
	}

	/**
		Finds a parameter, given its name.  Names are case-sensitive.

		@param name		the name of the parameter

		@return		a Param if found, otherwise null
	*/
	
    public Param getParam( String name )
	{
		Param	resultParam	= null;

		final int	numItems	= mParams.size( );
		
                Iterator iter = getElements( );
                while ( iter.hasNext( ) )
                {
                    Param aParam = ( Param )iter.next( );
                    
                    if( name.equals( aParam.mName ) )
                    {
			resultParam = aParam;
			break;
                    }                    
                }

                return ( resultParam );
	}

	/**
		Returns an Iterator which can be used to iterate through all Params
		in the list.

		@return		an Iterator object
	*/
        
	public Iterator getElements( )
	{
		// rely on built-in capability of vector
		return ( mParams.iterator( ) );
	}

	/**
		Constructs a String representation of the ParamList.
		<p>
		The resulting String is of the form:<n>
			"name: value, name: value, ..."

		@return		an String represention of the list
	*/
	public String toString()
	{
		Iterator	iter	= getElements();

		StringBuffer	buf	= new StringBuffer( 256 );

		while ( iter.hasNext() )
		{
			Param	item	= (Param)iter.next();

			buf.append( item );
			buf.append( kDelimiterString );
		}
		// strip last trailing comma
		if ( buf.length() != 0 )
		{
			buf.setLength( buf.length() - kDelimiterString.length() );
		}

		return( new String( buf ) );
	}
}