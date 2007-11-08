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
package com.sun.enterprise.management.config;

import javax.management.ObjectName;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import java.io.IOException;

import javax.management.*;

import com.sun.appserv.management.util.j2ee.J2EEUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.misc.TypeCast;
import com.sun.appserv.management.util.stringifier.ArrayStringifier;
import com.sun.appserv.management.util.jmx.JMXUtil;

import com.sun.appserv.management.base.Util;

import com.sun.enterprise.management.support.ComSunAppservTest;

/**
    Unit test for the com.sun.appserv monitoring MBeans relied
    upon by AMX.
 */
public final class ComSunAppservConfigTest extends ComSunAppservTest
{
    	public
    ComSunAppservConfigTest()
    {
    }


        private boolean
    checkConfig(final ObjectName   objectName )
        throws Exception
    {
        boolean worksOK = true;
        
        final MBeanServerConnection conn    = getMBeanServerConnection();
        
        final MBeanInfo mbeanInfo    = conn.getMBeanInfo( objectName );
        assert( mbeanInfo != null );
        final MBeanAttributeInfo[]  attrInfos   = mbeanInfo.getAttributes();
        for( final MBeanAttributeInfo attrInfo : attrInfos )
        {
            try
            {
                final Object    value   = conn.getAttribute( objectName, attrInfo.getName() );
            }
            catch( Exception e )
            {
			    final Throwable rootCause	= ExceptionUtil.getRootCause( e );
			
                warning( "MBean " + StringUtil.quote( objectName ) +
                    " threw an exception trying to get Attribute " +
                    StringUtil.quote( attrInfo.getName() ) + ": " + rootCause );
                worksOK = false;
            }
        }
        
        final String[]  attrNames   = JMXUtil.getAttributeNames( attrInfos );
        try
        {
           // System.out.println( objectName + " {" + ArrayStringifier.stringify( attrNames, ", " ) + "}");
            final AttributeList attrs   = conn.getAttributes( objectName, attrNames );
            assert( attrs.size() == attrInfos.length );
            
            // verify that Attribute names match what was returned
            final Set<String> attrNamesSet  = GSetUtil.newSet( attrNames );
            assert( attrNamesSet.size() == attrNames.length );  // should all be unique
            final List<Attribute> typed = TypeCast.asList( attrs );
            for( final Attribute attr : typed )
            {
                assert( attrNamesSet.contains( attr.getName() ) );
            }
        }
        catch( Exception e )
        {
			final Throwable rootCause	= ExceptionUtil.getRootCause( e );
			    
            warning( "MBean " + StringUtil.quote( objectName ) +
                " threw an exception trying to get getAttributes(" +
                ArrayStringifier.stringify( attrNames, ", " ) + "): " + rootCause );
            worksOK = false;
        }
        
        return( worksOK );
    }
    
    private static final Set<String> COM_SUN_APPSERV_EXCLUDED_TYPES  =
        Collections.unmodifiableSet( GSetUtil.newSet( new String[]
        {
            "servers",
            "applications",
            "configs",
            "resources",
            "transactions-recovery",
            "synchronization",
        } ));
        
    
    
        public void
    testAllConfig()
        throws Exception
    {
        final Map<String,ObjectName>    m   = getAllComSunAppservConfig();
        
        final Collection<ObjectName>   objectNames = m.values();
        final Set<ObjectName>   defective   = new HashSet<ObjectName>();
        int testedCount = 0;
        for( final ObjectName objectName : objectNames )
        {
            final String type   = objectName.getKeyProperty( "type" );
            if ( COM_SUN_APPSERV_EXCLUDED_TYPES.contains( type ) )
            {
                continue;
            }
            
            ++testedCount;
            if ( ! checkConfig( objectName ) )
            {
                defective.add( objectName );
            }
        }
        
        printVerbose( "ComSunAppservConfigTest.testAllConfig: checked " +
            testedCount + " com.sun.appserv:category=config MBeans for basic functionality, " +
            defective.size() + " failures." );
        
        if ( defective.size() != 0 )
        {
            failure( "The following MBeans are defective:\n" +
                CollectionUtil.toString( defective, "\n") );
        }
    }
    
  
	
}






