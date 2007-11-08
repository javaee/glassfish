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

import java.util.Set;
import java.util.Collections;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanRegistrationException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;


import com.sun.enterprise.management.support.oldconfig.OldProps;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.stringifier.ArrayStringifier;
import com.sun.appserv.management.j2ee.J2EETypes;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.config.ConfigContext;


/**
	Loads MBeans.
 */
final class LoaderOfOldMonitor extends LoaderOfOld
{
	LoaderOfOldMonitor( final Loader loader )
	{
		super( loader );
	}

		public Set<ObjectName>
	findAllOldCandidates()
	{
		final ObjectName	pattern	= JMXUtil.newObjectName( "com.sun.appserv:category=monitor,*" );
		final Set<ObjectName> all	= JMXUtil.queryNames( getMBeanServer(), pattern, null );
		
		return( all );
	}
	

	
		private static void
	mySleep( final long millis )
	{
		try
		{
			Thread.sleep( millis );
		}
		catch( InterruptedException e )
		{
		}
	}
	
		private final String
	formApplicationAndServerProps( final ObjectName oldObjectName )
	{
		final String	serverName	= oldObjectName.getKeyProperty( "server" );
		final String serverProp	= Util.makeProp( XTypes.SERVER_ROOT_MONITOR, serverName );
		
		String	props	= serverProp;
		
		String	applicationName	= oldObjectName.getKeyProperty( "application" );
		if ( applicationName == null )
		{
			applicationName	= AMX.NULL_NAME;
		}
		final String	applicationProp	= Util.makeProp( XTypes.APPLICATION_MONITOR, applicationName );
		
		props	= Util.concatenateProps( props, applicationProp );
		
		return( props );
	}
	
	/**
		Finding the monitoring peer for a WebModule is very complex as of version 8.1, due
		to the inconsistent names used for JSR 77 MBeans versus their monitoring equivalents.
		Mix in virtual servers, and context root versus module name, and it's a real 
		witches-brew of naming--Lloyd Chambers 27 Oct 2004
		
		Here are some facts:
		(1) the name property of a j2eeType=WebModule ObjectName
		is formed as //<virtual-server-name><context-root>.
		<context-root> always starts with "/".  <context-root> by default is the same
		as the name of the web module, but need not be.  A typical name is thus:
		//server/MyModule
		
		(2) the name property of a type=standalone-web-module ObjectName is  always the module name, never
		the context root. The name property of a type=web-module (embedded war) is the module name + ".war".
		The virtual server is not specified, since a type=standalone-web-module or type=web-module MBean
		is only a holder for webmodule-virtual-server mbeans.  Thus, the usual parallel structure is malformed.
		in the case of monitoring mbeans for web modules.
		
		Examples:
		
		(a) j2eeType=WebModule,name=//server/bookstore,J2EEServer=server,J2EEApplication=null
		(b) type=standalone-web-module,name=bookstore,category=monitor,server=server
		(c) type=web-module,name=logging-helloworld.war,application=logging-helloworld,category=monitor,server=server
		
		(3) type=servlet is a monitoring mbean which contains properties standalone-web-module or
		web-module and the webmodule-virtual-server in which it resides.  For example:
		
		(a) type=servlet,name=LoggingServlet,application=logging-helloworld,category=monitor,
			server=server,web-module=logging-helloworld.war,webmodule-virtual-server=server
			
		(b) type=servlet,name=jsp,category=monitor,server=server,standalone-web-module=bookstore,webmodule-virtual-server=server
		
		@see ServletImpl #getMonitoringPeerObjectName
	 */
	 
		protected ObjectName
	oldToNewObjectName( final ObjectName oldObjectName )
	{
		final String	oldType	= oldObjectName.getKeyProperty( "type" );
		final String	domainName	= mLoader.getAMXJMXDomainName();
		
		ObjectName	newObjectName	= null;
		
		/*
		if ( oldType.equals( "webmodule-virtual-server" ) )
		{
		*
			Make the peer of a WebModule be a WebModuleVirtualServerMonitor
		 *
			final String	virtualServerName	= oldObjectName.getKeyProperty( "name" );
			final String	webModuleName		= WebModuleSupport.getWebModuleName( oldObjectName );
			final String compositeName	= WebModuleSupport.formCompositeName( virtualServerName, webModuleName );
			
			final String requiredProps	=
				Util.makeRequiredProps( XTypes.WEB_MODULE_VIRTUAL_SERVER_MONITOR, compositeName );
			
			final String	containmentProps	= formApplicationAndServerProps( oldObjectName );
			final String	props	= Util.concatenateProps( requiredProps, containmentProps );
			
			newObjectName	= JMXUtil.newObjectName( domainName, props );
		}
		else
		*/if ( oldType.equals( "servlet" ) )
		{
		    // A ServletMonitor is contained by its WebModuleVirtualServerMonitor
			// a monitor for a Servlet
			final String	webModuleVirtualServerMonitorName	= oldObjectName.getKeyProperty( "webmodule-virtual-server" );
			
			final String requiredProps	=
				Util.makeRequiredProps( XTypes.SERVLET_MONITOR, oldObjectName.getKeyProperty( "name" ) );
			
			final String containerProp  =
			    Util.makeProp( XTypes.WEB_MODULE_VIRTUAL_SERVER_MONITOR, webModuleVirtualServerMonitorName );
			final String	containmentProps	= formApplicationAndServerProps( oldObjectName );
			
			final String	props	= Util.concatenateProps( requiredProps, containerProp, containmentProps);
			
			newObjectName	= JMXUtil.newObjectName( domainName, props );
		}
		else
		if ( oldType.equals( "webservice-endpoint" ))
		{
			// a monitor for a web service endpoint
            // first look for a EJB web service endpoint
            String ejbModName =
            oldObjectName.getKeyProperty("standalone-ejb-module");
            if ( ejbModName == null)
            {
                // try if this is a ejb module in an application
                ejbModName = oldObjectName.getKeyProperty("ejb-module");
            }
            if ( ejbModName != null)
            {
                final String requiredProps	=
                    Util.makeRequiredProps( XTypes.WEBSERVICE_ENDPOINT_MONITOR, oldObjectName.getKeyProperty( "name" ) );
			
                final String modProp = Util.makeProp( XTypes.EJB_MODULE_MONITOR, ejbModName );
                final String containmentProps = formApplicationAndServerProps( oldObjectName );
			
                final String props	=
                    Util.concatenateProps( requiredProps, modProp, containmentProps);
			
                newObjectName	= JMXUtil.newObjectName( domainName, props );
            }
            else 
            {
                /*
                String	virtualServerName	= oldObjectName.getKeyProperty( "webmodule-virtual-server" );
                if ( virtualServerName == null)
                {
                    virtualServerName = "server";
                }

                final String	webModuleName= WebModuleSupport.getWebModuleName( oldObjectName );
                
                final String 	vsMonitorCompositeName	=
                    WebModuleSupport.formCompositeName( virtualServerName, webModuleName );
			
                final String requiredProps	=
                    Util.makeRequiredProps( XTypes.WEBSERVICE_ENDPOINT_MONITOR, oldObjectName.getKeyProperty( "name" ) );
			
                final String	virtualServerProp	= Util.makeProp( XTypes.WEB_MODULE_VIRTUAL_SERVER_MONITOR, vsMonitorCompositeName );
                final String	containmentProps	= formApplicationAndServerProps( oldObjectName );
			
                final String	props	= Util.concatenateProps( requiredProps, virtualServerProp, containmentProps);
			
                newObjectName	= JMXUtil.newObjectName( domainName, props );
                */
                final String requiredProps	=
                    Util.makeRequiredProps( XTypes.WEBSERVICE_ENDPOINT_MONITOR, oldObjectName.getKeyProperty( "name" ) );
			
                final String webModuleVirtualServerMonitorName	=
                    oldObjectName.getKeyProperty( "webmodule-virtual-server" );
                final String modProp =
                    Util.makeProp( XTypes.WEB_MODULE_VIRTUAL_SERVER_MONITOR, webModuleVirtualServerMonitorName );
                final String containmentProps = formApplicationAndServerProps( oldObjectName );
			
                final String props	=
                    Util.concatenateProps( requiredProps, modProp, containmentProps);
			
                newObjectName	= JMXUtil.newObjectName( domainName, props );
            }

        }
        else 
		{
			final OldTypeToJ2EETypeMapper	mapper	= OldMonitorTypes.getInstance();
			final OldProps	oldProps	= new OldProps( oldObjectName, mapper);

			String	props	= oldProps.getNewProps();
			newObjectName	=  JMXUtil.newObjectName( domainName, props );

			/*
				If it's containment hierarchy includes an APPLICATION_MONITOR, then if one
				is not present (eg a standalone web or ejb monitor),
				then insert one with name AMX.NULL_NAME.
			 */
			final String	j2eeType	= newObjectName.getKeyProperty( AMX.J2EE_TYPE_KEY );
			final String[]	fullType	= TypeInfos.getInstance().getJ2EETypeChain( newObjectName );
			for( int i = 0; i < fullType.length - 1; ++i )
			{
				if ( fullType[ i ].equals( XTypes.APPLICATION_MONITOR ) &&
					newObjectName.getKeyProperty( XTypes.APPLICATION_MONITOR ) == null )
				{
					final String	prop	= Util.makeProp( XTypes.APPLICATION_MONITOR, AMX.NULL_NAME );
					newObjectName	= JMXUtil.newObjectName( newObjectName, prop );
					break;
				}
			}
		} 		
		return( newObjectName );
	}


        // remove the forward slash
        private String
    getContextRoot( final String webModuleName ) {

        ConfigBean cb = null;
        ConfigContext cCtx = AdminService.getAdminService().
                getAdminContext().getAdminConfigContext();

        try {
            cb = ApplicationHelper.findApplication(cCtx, webModuleName);
        } catch(Exception e) {
            // ignore
            return null;
        }
        if (cb instanceof WebModule) {
            String ctxRoot = ((WebModule) cb).getContextRoot();
            if ((ctxRoot != null) && (ctxRoot.length() > 0)) {
               if (ctxRoot.charAt(0) == '/'){
                    ctxRoot = ctxRoot.substring(1,ctxRoot.length()) ;
               }
            }
            return ctxRoot;
        } else {
            return null;
        }
    }

		private boolean
	isOldMonitorObjectName( final ObjectName objectName )
	{
		boolean	isOldMonitor	= false;
		
		if ( objectName.getDomain().equals( "com.sun.appserv" ) &&
				"monitor".equals( objectName.getKeyProperty( "category" ) ) )
		{
			final String	type	= objectName.getKeyProperty( "type" );
			
			isOldMonitor	= ! getIgnoreTypes().contains( type );
		}
		
		return( isOldMonitor );
	}

	/**
		Do not attempt to create corresponding "new" mbeans for these types
	 */
	private static final Set<String> IGNORE_TYPES	= GSetUtil.newUnmodifiableStringSet(
			"connection-factories",
			"connector-modules",
			"connector-service",
			"connection-factory",
			"standalone-connector-module",
			"connection-pools",
			"resources",
			"thread-pools",
			"applications",
			"orb",
			"connection-managers",
			"bean-methods",
			
			// we deal with these two specially
			"web-module",
			"standalone-web-module",
			
			// these are JDK 5.0 MBeans.  See bug (RFE) 
			"operating-system",
			"threadinfo",
			"memory",
			"thread-system",
			"garbage-collectors",
			"garbage-collector",
			"runtime",
			"compilation-system",
			"class-loading-system",
			
			// native web core types that have not yet been removed
			"request",
			"pwc-thread-pool",
			
			"jndi", //completely non-functional as of 27 June 2005.
			
			"jms-service",
			"jndi",
			
			"GMSClientMBean",
			
			// support these at some point?
		    "DomainDiagnostics",
		    "JVMInformation",
		    "JVMInformationCollector",
		    "work-management"
		);

	/**
		These types need to be supported.
	 */
	private static final Set<String> NEEDS_SUPPORT	= GSetUtil.newUnmodifiableStringSet(
		    // not for 9.0
		    // "DomainDiagnostics",
		    // "JVMInformation",
		    // "JVMInformationCollector",
		    // "work-management",
	);


		protected Set<String>
	getNeedsSupport()
	{
		return( NEEDS_SUPPORT );
	}

		protected Set<String>
	getIgnoreTypes()
	{
		return( IGNORE_TYPES );
	}


		public boolean
	isOldMBean( final ObjectName oldObjectName )
	{
		return isOldMonitorObjectName( oldObjectName );
	}
	
}








