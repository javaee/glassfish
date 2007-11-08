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
 

package com.sun.enterprise.management.offline;

import java.io.IOException;

import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;

import com.sun.enterprise.management.support.Delegate;
import com.sun.enterprise.management.support.DelegateBase;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigException;

//import com.sun.enterprise.config.serverbeans.*;

/**
 */
class ConfigDelegate extends DelegateBase
{
    private final ConfigBeanHelper    mHelper;

    ConfigDelegate(
        final ConfigContext configContext,
        final String        xPath )
        throws ConfigException
    {
        super( "ConfigDelegate", null );
        mHelper = ConfigBeanHelperFactory.getInstance( configContext ).getHelper( xPath );
    }
    
     ConfigDelegate(
        final ConfigContext configContext,
        final ConfigBean    configBean )
        throws ConfigException
    {
        super( "ConfigDelegate", null );
        mHelper = ConfigBeanHelperFactory.getInstance( configContext ).getHelper( configBean );
    }
	
    	public Object
    getAttribute( final String attrName )
    	throws AttributeNotFoundException
    {
    	final Object  result  = mHelper.getAttribute(attrName);
    	
    	return result;
    }

    	public void
    setAttribute( final Attribute attr )
    	throws AttributeNotFoundException, InvalidAttributeValueException
    {
    	mHelper.setAttribute( attr );
    	
    	flush();
    }

    	public MBeanInfo
    getMBeanInfo()
    {
        return mHelper.getMBeanInfo();
    }
    
    
    /**
        @see com.sun.enterprise.management.config.OldPropertiesImpl
     */
        public AttributeList
    getProperties()
    {
        return mHelper.getProperties();
    }
    
     /**
        @see com.sun.enterprise.management.config.OldPropertiesImpl
     */
        public String
    getPropertyValue( final String name )
    {
        return mHelper.getPropertyValue( name );
    }
    
     /**
        @see com.sun.enterprise.management.config.OldPropertiesImpl
     */
        public void
    setProperty( final Attribute attr )
    {
        mHelper.setProperty( attr );
    }
    
     
    /**
        @see com.sun.enterprise.management.config.OldPropertiesImpl
     */
        public AttributeList
    getSystemProperties()
    {
        return mHelper.getSystemProperties();
    }
    
    
     /**
        @see com.sun.enterprise.management.config.OldPropertiesImpl
     */
        public String
    getSystemPropertyValue( final String name )
    {
        return mHelper.getSystemPropertyValue( name );
    }
    
     /**
        @see com.sun.enterprise.management.config.OldPropertiesImpl
     */
        public void
    setSystemProperty( final Attribute attr )
    {
        mHelper.setSystemProperty( attr );
    }
    
    
        public String
    getDescription()
    {
        return mHelper.getDescription();
    }
    
        public void
    setDescription( final String description )
    {
        mHelper.setDescription( description );
    }
    
    	public final Object
    invoke(
    	String 		operationName,
    	Object[]	args,
    	String[]	types )
    {
        Object  result  = null;
        final int   numArgs = args == null ? 0 : args.length;
        
        if ( "getProperties".equals( operationName ) &&
            numArgs == 0 )
        {
            result  = getProperties();
        }
        else if ( "getPropertyValue".equals( operationName ) &&
            numArgs == 1 && types[0].equals( String.class.getName() ) )
        {
            result  = getPropertyValue( (String)args[ 0 ] );
        }
        else if ( "setProperty".equals( operationName ) &&
            numArgs == 1 && types[0].equals( Attribute.class.getName() ) )
        {
            setProperty( (Attribute)args[ 0 ] );
        }
        else if ( "getSystemProperties".equals( operationName ) &&
            numArgs == 0 )
        {
            result  = getSystemProperties();
        }
        else if ( "getSystemPropertyValue".equals( operationName ) &&
            numArgs == 1 && types[0].equals( String.class.getName() ) )
        {
            result  = getSystemPropertyValue( (String)args[ 0 ] );
        }
        else if ( "setSystemProperty".equals( operationName ) &&
            numArgs == 1 && types[0].equals( Attribute.class.getName() ) )
        {
            setSystemProperty( (Attribute)args[ 0 ] );
        }
        else
        {
            result  = mHelper.handleInvoke( operationName, args, types );
        }
        
        flush();
    	
        return result;
    }
                
        private void
    flush()
    {
        try
        {
            mHelper.getConfigContext().flush();
        }
        catch (ConfigException e)
        {
            throw new RuntimeException( e );
        }
    }                
}

















