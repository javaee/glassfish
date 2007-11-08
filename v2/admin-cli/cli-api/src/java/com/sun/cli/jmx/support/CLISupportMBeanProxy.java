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
 
/*
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/support/CLISupportMBeanProxy.java,v 1.3 2005/12/25 03:45:46 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:46 $
 */
 
 
package com.sun.cli.jmx.support;

import javax.management.ObjectName;
import javax.management.NotificationListener;
import javax.management.NotificationFilter;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;

import com.sun.cli.jmx.support.AliasMgrHashMapImpl;
import com.sun.cli.jmx.support.AliasMgr;


/*
	Supplies the CLISupportMBean and AliasMgrMBean methods in a single proxy.
	
	Refer to CLISupportMBean and AliasMgrMBean for details.
 */
public class CLISupportMBeanProxy implements CLISupportMBean, AliasMgrMBean
{
	final AliasMgrMBean				mAliasMgrProxy;
	final CLISupportMBean			mCLIProxy;
	
	/*
		public static AliasMgrMBean
	createAliasMgrProxy( MBeanServerConnection conn ) throws Exception
	{
		final ObjectName aliasMgrObjectName		= new ObjectName( CLISupportStrings.ALIAS_MGR_TARGET );
		
		final AliasMgrMBean proxy		= (AliasMgrMBean)MBeanServerInvocationHandler.newProxyInstance( conn,
								aliasMgrObjectName, AliasMgrMBean.class, false );
		return( proxy );
	}
	
		public static CLISupportMBean
	createCLISupportProxy( MBeanServerConnection conn ) throws Exception
	{
		final ObjectName cliSupportObjectName	= new ObjectName( CLISupportStrings.CLI_SUPPORT_TARGET );
		
		final CLISupportMBean	proxy	= (CLISupportMBean)MBeanServerInvocationHandler.newProxyInstance( conn,
						cliSupportObjectName, CLISupportMBean.class, false );
						
		return( proxy );
	}
	*/
	
	/*
		CLISupport and AliasMgr are anywhere; precreated for this constructor
	 */
		public
	CLISupportMBeanProxy(
		AliasMgrMBean			aliasMgr,
		CLISupportMBean			cliSupport ) throws Exception
	{
		mAliasMgrProxy	= aliasMgr;
		mCLIProxy		= cliSupport;
	}
	
	

//------------------------------ CLI --------------------------------------------
	
		public ResultsForGetSet []
	mbeanGet( String attrs, String [] targets) throws Exception
	{
		return( mCLIProxy.mbeanGet( attrs, targets ) );
	}

		public ResultsForGetSet []
	mbeanSet( String attrs, String [] targets ) throws Exception
	{
		return( mCLIProxy.mbeanSet( attrs, targets ) );
	}

		public InvokeResult []
	mbeanInvoke(
		String	operation,
		String	args,
		String [] targets ) throws Exception
	{
		return( mCLIProxy.mbeanInvoke( operation, args, targets ) );
	}

		public InvokeResult []
	mbeanInvoke(
		String	operation,
		String [] targets ) throws Exception
	{
		return( mbeanInvoke( operation, null, targets ) );
	}
	
	
		public ObjectName []
	mbeanFind( String [] targets )
		throws Exception
	{
		return( mCLIProxy.mbeanFind( targets ) );
	}
	
		public ObjectName []
	mbeanFind( String target )
		throws Exception
	{
		return( mbeanFind( new String[] { target } ));
	}
	
		public ObjectName []
	mbeanFind( String [] targets, String regex)
		throws Exception
	{
		return( mCLIProxy.mbeanFind( targets, regex ) );
	}


		public InspectResult
	mbeanInspect( InspectRequest request, ObjectName name ) throws Exception
	{
		return( mCLIProxy.mbeanInspect( request, name ) );
	}
	
		public InspectResult []
	mbeanInspect( InspectRequest request, String [] targets ) throws Exception
	{
		return( mCLIProxy.mbeanInspect( request, targets ) );
	}
	
		public void
	mbeanCreate( String name, String theClass, String args ) throws Exception
	{
		mCLIProxy.mbeanCreate( name, theClass, args );
	}
	
		public void
	mbeanUnregister( String name ) throws Exception
	{
		mCLIProxy.mbeanUnregister( name );
	}
	
		public int
	mbeanCount( ) throws Exception
	{
		return( mCLIProxy.mbeanCount( ) );
	}
	
		public String []
	mbeanDomains( ) throws Exception
	{
		return( mCLIProxy.mbeanDomains( ) );
	}
	
		public void
	mbeanListen(
		boolean		start,
		String []	targets,
		NotificationListener listener,
		NotificationFilter filter,
		Object handback ) throws Exception
	{
		mCLIProxy.mbeanListen( start, targets, listener, filter, handback );
	}
	
	
		public ObjectName []
	resolveTargets( String [] targets ) throws Exception
	{
		return( mCLIProxy.resolveTargets( targets ) );
	}
	
	
//------------------------------ AliasMgr --------------------------------------------
	
	
		public void
	createAlias( String aliasName, String objectName ) throws Exception
	{
		mAliasMgrProxy.createAlias( aliasName, objectName );
	}
	
		public void
	deleteAlias( String aliasName ) throws Exception
	{
		mAliasMgrProxy.deleteAlias( aliasName );
	}
	
		public String
	resolveAlias( String aliasName ) throws Exception
	{
		return( (String)mAliasMgrProxy.resolveAlias( aliasName ) );
	}
	
		public String []
	listAliases( boolean showValues ) throws Exception
	{
		return( mAliasMgrProxy.listAliases( showValues ) );
	}
	
		public String []
	getAliases( ) throws Exception
	{
		return( listAliases( false ) );
	}
	
		public AliasMgrMBean
	getAliasMgr()
	{
		return( mAliasMgrProxy );
	}
}

