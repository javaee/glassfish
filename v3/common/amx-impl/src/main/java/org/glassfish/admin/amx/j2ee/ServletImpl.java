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
package org.glassfish.admin.amx.j2ee;
 
import java.util.Map;
import java.util.Set;

import javax.management.ObjectName;
import javax.management.j2ee.statistics.ServletStats;

import com.sun.appserv.management.j2ee.Servlet;
import com.sun.appserv.management.j2ee.WebModule;
import com.sun.appserv.management.j2ee.J2EETypes;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.Util;


import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;

import org.glassfish.admin.amx.mbean.Delegate;

//import com.sun.appserv.management.monitor.WebModuleVirtualServerMonitor;
//import com.sun.appserv.management.monitor.ServletMonitor;

/**
 */
public final class ServletImpl
	extends J2EEManagedObjectImplBase
	//  implements Servlet
{
        public
	ServletImpl(
        final String fullType,
        final ObjectName parentObjectName,
        final Delegate delegate )
	{
		super( J2EETypes.SERVLET, fullType, parentObjectName, Servlet.class, delegate );
	}
	
		protected String
	getMonitoringPeerJ2EEType()
	{
		return( XTypes.SERVLET_MONITOR );
	}
	
		protected ObjectName
	queryMonitoringPeerFailed( final Map<String,String> propsMap )
	{
    /*
		final WebModule			webModule	= (WebModule)getContainer();
		
		debug( "queryMonitoringPeerFailed: expecting to find " +
		    MapUtil.toString( propsMap ) );
		    
		if ( webModule == null )
		{
		    logWarning(
		        "ServletImpl.queryMonitoringPeerFailed: " +
		        "Can't get containing WebModule, my ObjectName = " + getObjectName());
            return null;
		}
		
		ObjectName	result	= null;
		
		try
		{
			final WebModuleVirtualServerMonitor	webModuleVirtualServerMonitor	=
				(WebModuleVirtualServerMonitor)webModule.getMonitoringPeer();
			if ( webModuleVirtualServerMonitor == null )
			{
			    final ObjectName    objectName  = Util.getObjectName( webModule );
			    
			    final String msg = "ServletImpl.queryMonitoringPeerFailed: " +
			        "Can't get WebModuleVirtualServerMonitor for " +
			            quote( toString( objectName ));
			    debug( msg );
			    logFine( msg );
			}
			else
			{
    			final Map<String,ServletMonitor> servletMap	= 
    			    webModuleVirtualServerMonitor.getServletMonitorMap();
    			
    			final ServletMonitor	sm	= servletMap.get( getName() );
    			if ( sm != null )
    			{
    				result	= Util.getObjectName( sm );
    			}
    			else
    			{
    			    final String  servletMonitorNames = CollectionUtil.toString(
    			        Util.toObjectNames( servletMap ).values(), StringUtil.NEWLINE() );
    			    
    		        logWarning(
    		            "ServletImpl.queryMonitoringPeerFailed: " +
    		            "Can't find ServletMonitor, my ObjectName = " +
    		                quote( toString( getObjectName() ) ) +
    		                ", WebModuleVirtualServerMonitor " +
    		                quote( toString( Util.getObjectName(webModuleVirtualServerMonitor) )) +
    		                " ServletMonitor names:" + StringUtil.NEWLINE() + servletMonitorNames );
    			}
			}
		}
		catch( Exception e )
		{
		    logWarning( "ServletImpl.queryMonitoringPeerFailed: " + e);
		    debug( "ServletImpl.queryMonitoringPeerFailed: " + e + "\n" +
		        ExceptionUtil.getStackTrace( e ) );
		}
		
		return result;
    */
    return null;
	}
}





