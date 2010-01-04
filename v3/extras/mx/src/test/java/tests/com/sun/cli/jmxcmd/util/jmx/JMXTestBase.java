/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2009 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jmxcmd/util/jmx/JMXTestBase.java,v 1.2 2004/02/14 01:39:36 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2004/02/14 01:39:36 $
 */
 
package org.glassfish.admin.amx.util.jmx;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.NotCompliantMBeanException;
import javax.management.MBeanRegistrationException;


public class JMXTestBase extends junit.framework.TestCase
{
	private MBeanServer	mServer;
	
		public
	JMXTestBase(  )
	{
		mServer	= createAgent();
	}
	
		protected MBeanServer
	getServer()
	{
		return( mServer );
	}
	
		private MBeanServer
	createAgent(  )
	{
		// don't register it with the Factory
		return( MBeanServerFactory.newMBeanServer() );
	}
	
		protected void
	registerMBean( Object mbean, String name )
		throws MalformedObjectNameException, InstanceAlreadyExistsException,
		NotCompliantMBeanException, MBeanRegistrationException
	{
		mServer.registerMBean( mbean, new ObjectName( name ) );
	}
	
		public void
	setUp() throws Exception
	{
	}
	
		public void
	tearDown()
		throws Exception
	{
		JMXUtil.unregisterAll( mServer );
		mServer	= null;
	}

};

