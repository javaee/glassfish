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
package org.glassfish.admin.amx.dotted.valueaccessor;


import org.glassfish.admin.amx.dotted.DottedNameStrings;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;
import javax.management.MBeanServerConnection;
import javax.management.ReflectionException;
import javax.management.MBeanException;
import javax.management.InstanceNotFoundException;
import javax.management.AttributeNotFoundException;
import javax.management.RuntimeOperationsException;
import javax.management.JMException;

import javax.management.MBeanServerConnection;

public abstract class PropertyValueAccessorBase extends PrefixedValueAccessorBase
{
    abstract String getGetPropertyMethodName();
	abstract String getSetPropertyMethodName();
	abstract String getGetPropertiesMethodName();
    
    
    public PropertyValueAccessorBase(MBeanServerConnection conn, String prefix)
	{
		super( conn, prefix);
	}
  
    public Attribute getValue( ObjectName objectName, String valueName )
		throws java.io.IOException, ReflectionException, InstanceNotFoundException,
		AttributeNotFoundException
	{
		Attribute	result	= null;
		try
		{
			final Object value	= getMBS().invoke( objectName,
				getGetPropertyMethodName(), new Object [] { valueName }, 
				new String [] { "java.lang.String" } );
			result	= new Attribute( valueName, value );
		}
		catch( MBeanException e )
		{
			// method doesn't exist
			throw new AttributeNotFoundException( DottedNameStrings.getString(DottedNameStrings.ATTRIBUTE_NOT_FOUND_KEY, valueName ));
		}
		catch( ReflectionException e )
		{
			// method doesn't exist
			throw new AttributeNotFoundException(  DottedNameStrings.getString(DottedNameStrings.ATTRIBUTE_NOT_FOUND_KEY, valueName ));
		}
		return( result );
	}
	
    public Attribute
	setValue( final ObjectName objectName, final Attribute attr ) throws Exception
	{
	// NOTE: -Djmx.invoke.getters must be set for setProperty() to succeed
	// as a method invocation (in the unit tests)
	
	// note that if attr.getValue() is null, the property will be removed
		getMBS().invoke( objectName,
			getSetPropertyMethodName(), new Object [] { attr }, 
			new String [] { "javax.management.Attribute" } );
		
		return( attr );
	}
	
	
	public String [] getAllPropertyNames(ObjectName objectName )
		throws java.io.IOException, ReflectionException, InstanceNotFoundException
    {
        return getAllPropertyNames( objectName, false );
    }
	public String [] getAllPropertyNames(ObjectName objectName, boolean bIncludingPrefix )
		throws java.io.IOException, ReflectionException, InstanceNotFoundException
	{
		String	[]	names	= null;
		
		try
		{
			final AttributeList	props	= (AttributeList)getMBS().invoke( objectName,
					getGetPropertiesMethodName(), null, 
					null );
			names	= new String [ props.size() ];
			for( int i = 0; i < names.length; ++i )
			{
				final Attribute	attr	= (Attribute)props.get( i );
				if(bIncludingPrefix)
                    names[ i ]	= getDottedNamePrefix() + attr.getName();
                else
                    names[ i ]	= attr.getName();
			}
		}
		// getProperties() does not exist--do not log this--it will always happen
		// on MBeans that don't have properties.  Certain bugs in S1As interceptor
		// cause the wrong types of exceptions to be thrown, so we have to catch
		// several of them.  The correct one is ReflectionException
		catch( MBeanException e )
		{
			names	= new String [ 0 ];
		}
		catch( RuntimeOperationsException e )
		{
			names	= new String [ 0 ];
		}
		catch( ReflectionException e )
		{
			names	= new String [ 0 ];
		}
		
		return( names );
	}
    
    
}


