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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/spi/JMXServiceURLConnectorProvider.java,v 1.3 2005/11/15 20:59:54 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/15 20:59:54 $
 */
 
package com.sun.cli.jmxcmd.spi;

import java.util.Map;
import java.util.Set;
import java.io.IOException;

import javax.management.remote.JMXConnector;
import org.glassfish.admin.amx.util.SetUtil;



/**
	Supports connectivity via a JMXServiceURL.
	<p>
	Supported protocols are {@link #URL_PROTOCOL}
 */
public final class JMXServiceURLConnectorProvider
	extends JMXConnectorProviderBase
{
		public
	JMXServiceURLConnectorProvider()
	{
	}
	
	static final class Info implements JMXConnectorProviderInfo
	{
		private static final String	DESCRIPTION	=
			"Implements connectivity via a JMXServiceURL";
		private static final String	USAGE	=
			"connect --protocol|-r url " +
			"--url <url> " +
			"[--user|-u <user> --password-file|-f <path> ] "  +
			"[connection-name]";
		
			public String
		getDescription()
		{
			return( DESCRIPTION );
		}
			public String
		getUsage()
		{
			return( USAGE );
		}
	}
	
		public static JMXConnectorProviderInfo
	getInfo()
	{
		return( new Info() );
	}
	
		public JMXConnector
	connect( java.util.Map<String,String> params )
		throws IOException
	{
		final String	urlString		= (String)params.get( URL );
		
		requireParam( urlString, "url" );
		
		final Map<String,Object>		env	= initEnv( params );
				
		return( connect( urlString, env ) );
	}
	
	public final static String	URL_PROTOCOL		= "url";
	public static final Set<String>	SUPPORTED_PROTOCOLS	=
		SetUtil.newUnmodifiableStringSet( URL_PROTOCOL );
	
		protected Set<String>
	getSupportedProtocols()
	{
		return( SUPPORTED_PROTOCOLS );
	}
}




