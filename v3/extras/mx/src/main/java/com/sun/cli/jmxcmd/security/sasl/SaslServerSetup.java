/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2004-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.cli.jmxcmd.security.sasl;

import java.util.Map;
import java.io.File;
import java.io.IOException;
import org.glassfish.admin.amx.util.StringUtil;



public class SaslServerSetup extends SaslSetup
{
		public
	SaslServerSetup( final Map<String,Object> env, final boolean	useTLS )
	{
		super( env, useTLS );
	}
	
	/**
		the access level file used by the connector server to
		perform user authorization. The access level file is a properties
		based text file specifying username/access level pairs where
		access level is either "readonly" or "readwrite" access to the
		MBeanServer operations. This properties based access control
		checker has been implemented using the MBeanServerForwarder
		interface which wraps the real MBean server inside an access
		controller MBean server which performs the access control checks
		before forwarding the requests to the real MBean server.

		This property is implementation-dependent and might not be
		supported by all implementations of the JMX Remote API.
	*/
		public void
	setupAuthorization( File authorizationFile)
	{
		if ( authorizationFile != null )
		{
			put("jmx.remote.x.access.file", authorizationFile );
			printDebug( "authorization enabled using file " + StringUtil.quote( authorizationFile ));
		}
		else
		{
			printDebug( "authorization disabled" );
		}
	}
	
		public void
	setupAuthentication( File authenticationFile )
		throws IOException
	{
		if ( authenticationFile != null )
		{
			final PasswordFileCallbackHandler	handler	= new PasswordFileCallbackHandler( authenticationFile.toString() );
			put("jmx.remote.sasl.callback.handler", handler );
			printDebug( "authentication enabled using file " + StringUtil.quote( authenticationFile ));
		}
		else
		{
			printDebug( "authentication disabled" );
		}
	}
}


