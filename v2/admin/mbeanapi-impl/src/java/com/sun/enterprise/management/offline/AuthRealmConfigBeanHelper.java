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

import java.util.Set;

import javax.management.AttributeNotFoundException;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigException;

import com.sun.appserv.management.config.AuthRealmConfig;

import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.StringUtil;


final class AuthRealmConfigBeanHelper extends StdConfigBeanHelper
{
    private AuthRealmSupport  mSupport;
    
        public
    AuthRealmConfigBeanHelper(
        final ConfigContext configContext,
        final ConfigBean    configBean )
    {
        super( configContext, configBean );
        
        mSupport    = createSupport();
    }
    
	private static final String TEMPLATE_PREFIX = "${";
	
        private AuthRealmSupport
   createSupport()
   {
        AuthRealmSupport    support = null;
        
        if ( isStdFileRealm() )
        {
            String    file    = null;
            try
            {
                file    = getFile();
            }
            catch( Exception e )
            {
                // some realms are malformed
            }
            
            if ( file != null && file.indexOf( TEMPLATE_PREFIX ) < 0 )
            {
                support    = new AuthRealmSupport( this );
            }
            else if ( file != null )
            {
                // can't support it
            }
        }
        
        return support;
   }
    
        private boolean
   isStdFileRealm()
   {
        return AuthRealmConfig.DEFAULT_REALM_CLASSNAME.equals( getClassname() );
   }
    
        private void
    checkRealmType()
    {
        if ( ! isStdFileRealm() )
        {
            throw new IllegalArgumentException(
                "AuthRealm type " + getClassname() +" not supported." );
        }
    }
   
    private static final String USER_NAMES_ATTR = "UserNames";
    private static final String GROUP_NAMES_ATTR = "GroupNames";
    
        protected Set<String>
    _getAttributeNames()
    {
        final Set<String>   attrNames = super._getAttributeNames();
        
        if ( isStdFileRealm() )
        {
            attrNames.add( GROUP_NAMES_ATTR );
            attrNames.add( USER_NAMES_ATTR );
        }
        
        return attrNames;
    }
    
        protected Class
    getAttributeClass( final String attrName )
    {
        Class  result  = null;
        
        if ( isStdFileRealm() &&
            (   GROUP_NAMES_ATTR.equals( attrName ) ||
                USER_NAMES_ATTR.equals( attrName ) ) )
        {
            result  = String[].class;
        }
        else
        {
            result  = super.getAttributeClass( attrName );
	    }
	    return result;
    }
    
    	public Object
    getAttribute( final String attrName )
    	throws AttributeNotFoundException
    {
        Object  result  = null;
        
        if ( GROUP_NAMES_ATTR.equals( attrName ) )
        {
            checkRealmType();
            result  = mSupport.getGroupNames();
            //sdebug( "GroupNames: " + StringUtil.toString( (String[])result ) );
        }
        else if ( USER_NAMES_ATTR.equals( attrName ) )
        {
            checkRealmType();
            result  = mSupport.getUserNames();
            //sdebug( "UserNames: " + StringUtil.toString( (String[])result ) );
        }
        else
        {
            result  = super.getAttribute( attrName );
	    }
	    return result;
    }
    
    static private final Set<String> SUPPORTED_OPERATIONS =
        GSetUtil.newUnmodifiableStringSet(
            "getUserGroupNames", "addUser", "updateUser", "removeUser" );
    
        public Object
    handleInvoke(
    	String 		operationName,
    	Object[]	args,
    	String[]	types )
    {
        Object  result  = null;
        
        if(args == null)
        {
            unsupportedOperation( operationName, args, types );
        }

        final int   numArgs = args.length;
        
        if ( isStdFileRealm() &&
            SUPPORTED_OPERATIONS.contains( operationName ) && numArgs >= 1 )
        {
            final String    user    = (String)args[ 0 ];
                
            if (  operationName.equals( "getUserGroupNames" ) && numArgs == 1)
            {
                result  = mSupport.getUserGroupNames( user );
            }
            else if (  operationName.equals( "addUser" ) && numArgs == 3)
            {
                final String    password    = (String)args[ 1 ];
                final String[]    groupList    = (String[])args[ 2 ];
                mSupport.addUser( user, password, groupList );
            }
            else if (  operationName.equals( "updateUser" ) && numArgs == 3)
            {
                final String      password    = (String)args[ 1 ];
                final String[]    groupList    = (String[])args[ 2 ];
                mSupport.updateUser( user, password, groupList );
            }
            else if (  operationName.equals( "removeUser" ) && numArgs == 1)
            {
                mSupport.removeUser( user  );
            }
            else
            {
                unsupportedOperation( operationName, args, types );
            }
        }
        else
        {
            unsupportedOperation( operationName, args, types );
        }
        //sdebug( "handleInvoke: " + operationName + "(): " + result );
        
        return result;
    }
    
        public String
    getFile()
    {
        return getPropertyValue( "file" );
    }
    
    
        public String
    getClassname()
    {
        try
        {
            return (String)getAttribute( "Classname" );
        }
        catch( AttributeNotFoundException e )
        {
            e.printStackTrace();
            throw new RuntimeException( e );
        }
    }
}

















