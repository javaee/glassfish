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

import com.sun.appserv.management.DomainRoot;

import com.sun.appserv.management.j2ee.*;

/**
	Map all types from J2EE Types to their respective MBean interfaces.
 */
public final class J2EETypesMapper extends TypesMapper
{
	private static J2EETypesMapper	INSTANCE	= null;
	
	
		public static synchronized J2EETypesMapper
	getInstance()
	{
		if ( INSTANCE == null )
		{
			INSTANCE	= new J2EETypesMapper();
		}
		
		return( INSTANCE );
	}
	
	
		private
	J2EETypesMapper( )
	{
		super( MBEANINTERFACES );
	}

	
	/**
		The classes for which we need mapping (all MBeans must be included)
	 */
	private static final Object[] MBEANINTERFACES	=
	{
		J2EEDomain.J2EE_TYPE, J2EEDomain.class,
		J2EEServer.J2EE_TYPE, J2EEDomain.class,
		J2EECluster.J2EE_TYPE, J2EEDomain.class,
		//J2EEStandaloneServer.J2EE_TYPE, J2EEStandaloneServer.class,
		J2EEApplication.J2EE_TYPE, J2EEDomain.class,
		AppClientModule.J2EE_TYPE, AppClientModule.class,
		EJBModule.J2EE_TYPE, EJBModule.class,
		WebModule.J2EE_TYPE, WebModule.class,
		ResourceAdapterModule.J2EE_TYPE, ResourceAdapterModule.class,
		ResourceAdapter.J2EE_TYPE, ResourceAdapter.class,
		EntityBean.J2EE_TYPE, EntityBean.class,
		StatefulSessionBean.J2EE_TYPE, StatefulSessionBean.class,
		StatelessSessionBean.J2EE_TYPE, StatelessSessionBean.class,
		MessageDrivenBean.J2EE_TYPE, MessageDrivenBean.class,
		Servlet.J2EE_TYPE, Servlet.class,
		JavaMailResource.J2EE_TYPE, JavaMailResource.class,
		JCAResource.J2EE_TYPE, JCAResource.class,
		JCAConnectionFactory.J2EE_TYPE, JCAConnectionFactory.class,
		JCAManagedConnectionFactory.J2EE_TYPE, JCAManagedConnectionFactory.class,
		JDBCResource.J2EE_TYPE, JDBCResource.class,
		JDBCDataSource.J2EE_TYPE, JDBCDataSource.class,
		JDBCDriver.J2EE_TYPE, JDBCDriver.class,
		JMSResource.J2EE_TYPE, JMSResource.class,
		JNDIResource.J2EE_TYPE, JNDIResource.class,
		JTAResource.J2EE_TYPE, JTAResource.class,
		RMIIIOPResource.J2EE_TYPE, RMIIIOPResource.class,
		URLResource.J2EE_TYPE, URLResource.class,
		JVM.J2EE_TYPE, JVM.class,
		WebServiceEndpoint.J2EE_TYPE, WebServiceEndpoint.class,
	};
	
}
