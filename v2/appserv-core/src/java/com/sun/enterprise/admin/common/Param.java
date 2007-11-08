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


/**
	Encapsulates a name/value pair.
	<p>
	The name of a Param is required to be a string, but its value may be
	any Serializable object.   Param does not alter value at any time. Thus
    Param is an immutable class.
	@author Lloyd Chambers
	@version 1.0
*/

public class Param implements Serializable
{
    public static long serialVersionUID = -6783475004108829145L;

	public String		mName;
	public Serializable	mValue;

	/**
		Constructs a new Param with name and value.
		<p>
		@param name		non-null String specifying parameter name
		@param value	any Serializable (may be null)
	 */
	public Param( String name, Serializable value )
	{
		//Assert.assert( (name != null), "null name" );

		mName	= name;
		mValue	= value;
	}

	/**
		Generates a string of the form: "name: value".
	 */
	public String toString()
	{
		if ( mValue != null )
		{
			return( mName + ": " + mValue );
		}
		return( mName + ": <null>" );
	}
        
        /**
            Defines the logical equality of Param instance with any other
            Object. Note that this method <strong> does not </strong> obey
            the general contract of the java.lang.Object.equls() method.
            An instance of Param is equal to other Object iff
            <li> it is also an instance of Param &&
            <li> it has the same name as that of this instance.
            It is clear that this method does not take into account the value
            of the Param.
         
            @param other instance of object to be compared
            @return boolean false if the objects are "equal" false otherwise
        */
        
        public boolean equals(Object other)
        {
            boolean isSame = false;
            if (other instanceof Param)
            {
                isSame = this.mName.equals(((Param)other).mName);
            }
            
            return isSame;
        }
        
        /**
            Generates the hashcode for this param. Since equals() method takes
            only name into account, this method also takes into account only
            the name.
            
            @return integer hashcode
        */
        public int hashCode()
        {
            return (mName.hashCode ());
        }
}