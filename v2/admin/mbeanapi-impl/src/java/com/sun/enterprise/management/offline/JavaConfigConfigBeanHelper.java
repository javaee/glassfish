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
import java.util.List;
import java.util.Arrays;

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;

import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.misc.GSetUtil;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigException;


final class JavaConfigConfigBeanHelper extends StdConfigBeanHelper
{
    static private final String JVM_OPTIONS_ATTR   = "JvmOptions";
    
        public
    JavaConfigConfigBeanHelper(
        final ConfigContext configContext,
        final ConfigBean    configBean )
    {
        super( configContext, configBean );
        
    }
    
    
       private boolean
    hasJVMOptions()
    {
        return hasValue( JVM_OPTIONS_ATTR );
    }
    
        protected Set<String>
    _getAttributeNames()
    {
        final Set<String>   attrNames = super._getAttributeNames();
        
        if ( hasJVMOptions() )
        {
            attrNames.add( JVM_OPTIONS_ATTR );
        }
        
        return attrNames;
    }
    
      
        protected Class
    getAttributeClass( final String attrName )
    {
        Class  result  = null;
        
        if ( JVM_OPTIONS_ATTR.equals( attrName ) )
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
        
        if ( JVM_OPTIONS_ATTR.equals( attrName ) )
        {
            result  = getJVMOptions();
        }
        else
        {
            result  = super.getAttribute( attrName );
	    }
	    return result;
    }
    
      	public void
    setAttribute( final String name, final Object value )
    	throws AttributeNotFoundException, InvalidAttributeValueException
    {
        if ( JVM_OPTIONS_ATTR.equals( name ) )
        {
            setJVMOptions( (String[])value );
        }
        else
        {
    	    super.setAttribute( name, value );
    	}
    }
    
    
        private com.sun.enterprise.config.serverbeans.JavaConfig
    getJavaConfigConfigBean()
    {
        return (com.sun.enterprise.config.serverbeans.JavaConfig)
            getConfigBean();
    }
    
        public String[]
    getJVMOptions()
    {
        // presumably spaces are not allowed in a JVM option
        final String[]  result  = getJavaConfigConfigBean().getJvmOptions();
        
        return result;
    }
    
        public void
    setJVMOptions( final String[] optionsIn )
    {
        // ensure no duplicates
        final Set<String>   optionsSet    = GSetUtil.newUnmodifiableStringSet( optionsIn );
        final String[]  options = GSetUtil.toStringArray( optionsSet );
        Arrays.sort( options );
        
        getJavaConfigConfigBean().setJvmOptions( options );
    }
    
}

















