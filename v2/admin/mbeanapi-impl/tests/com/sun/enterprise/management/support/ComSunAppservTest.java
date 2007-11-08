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
package com.sun.enterprise.management.support;

import javax.management.ObjectName;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import java.io.IOException;

import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.BoundaryStatistic;
import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.BoundedRangeStatistic;
import javax.management.j2ee.statistics.TimeStatistic;

import com.sun.appserv.management.util.j2ee.J2EEUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.base.Util;

import com.sun.appserv.management.j2ee.statistics.*;


import com.sun.enterprise.management.AMXTestBase;
import com.sun.enterprise.management.Capabilities;

/**
    Base class for testing the com.sun.appserv MBeans relied
    upon by AMX.
 */
public class ComSunAppservTest
	extends AMXTestBase
{
    	public
    ComSunAppservTest()
    {
    }
	
    /**
        @return Map<String,ObjectName>, keyed by value of 'type' property from ObjectName
     */
        public Map<String,ObjectName>
    getAllComSunAppservCategory( final String category )
        throws IOException
    {
        final ObjectName    pattern =
            Util.newObjectName( "com.sun.appserv:category=" + category + ",*");
        final Set<ObjectName> objectNames =
            JMXUtil.queryNames( getMBeanServerConnection(), pattern, null );
        
        final HashMap<String,ObjectName>   m   = new HashMap<String,ObjectName>();
        for( final ObjectName objectName : objectNames )
        {
            final String    type    = objectName.getKeyProperty( "type" );
            if ( type != null )
            {
                m.put( type, objectName );
            }
        }
        
        return m;
    }
            
        public Map<String,ObjectName>
    getAllComSunAppservMonitor()
        throws java.io.IOException
    {
        return getAllComSunAppservCategory( "monitor" );
    }
    
        public Map<String,ObjectName>
    getAllComSunAppservConfig()
        throws java.io.IOException
    {
        return getAllComSunAppservCategory( "config" );
    }
  
	
	
}






