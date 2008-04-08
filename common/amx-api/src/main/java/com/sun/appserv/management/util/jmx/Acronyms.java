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
 * $Header: /cvs/glassfish/appserv-api/src/java/com/sun/appserv/management/util/jmx/Acronyms.java,v 1.2 2007/05/05 05:31:03 tcfujii Exp $
 * $Revision: 1.2 $
 * $Date: 2007/05/05 05:31:03 $
 */


package com.sun.appserv.management.util.jmx;

import java.util.Map;
import java.util.HashMap;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.util.misc.MapUtil;



/**
 */
public final class Acronyms
{
	private static Map<String,String>	MAP	= null;
	
	private static final String[]	MAPPINGS	= new String[]
	{
		"Cmp", "CMP",
		"CmpContainer", "CMPContainer",
		"Corba", "CORBA",
		"EjbContainer", "EJBContainer",
		"HttpListeners", "HTTPListeners",
		"HttpService", "HTTPService",
		"Jacc", "JACC",
		"Jaxr", "JAXR",
		"Jaxrpc", "JAXRPC",
		"JdbcConnectionPool", "JDBCConnectionPool",
		"JDBCResourceJndiName", "JDBCResourceJNDIName",
		"Jdo", "JDO",
		"Jms", "JMS",
		"JndiName", "JNDIName",
		"Jta", "JTA",
		"Jts", "JTS",
		"JvmOptions", "JVMOptions",
		"MdbContainer", "MDBContainer",
		"Orb", "ORB",
		"RmicOptions", "RMICOptions",
		"Saaj", "SAAJ",
		"Ssl2Ciphers", "SSL2Ciphers",
		"Ssl2Enabled", "SSL2Enabled",
		"Ssl3Enabled", "SSL3Enabled",
		"Ssl3TlsCiphers", "SSL3TLSCiphers",
		"SystemJmxConnectorName", "SystemJMXConnectorName",
		"TlsEnabled", "TLSEnabled",
		"TlsRollbackEnabled", "TLSRollbackEnabled",
		"SfsbPersistenceType", "SFSBPersistenceType",
		"SfsbHaPersistenceType", "SFSBHAPersistenceType",
		"SfsbCheckpointEnabled", "SFSBCheckpointEnabled",
		"SfsbQuickCheckpointEnabled", "SFSBQuickCheckpointEnabled",
		"SfsbStorePoolName", "SFSBStorePoolName",
		"SsoFailoverEnabled", "SSOFailoverEnabled",
		"HttpSessionStorePoolName", "HTTPSessionStorePoolName",
		"DnsLookupEnabled", "DNSLookupEnabled",
		"SslEnabled", "SSLEnabled",
		"ThreadPoolIds", "ThreadPoolIDs",
		"JndiLookupName", "JNDILookupName",
		};
	
	/**
		Return a Map whose keys are acronyms (their case should be ignored),
		and whose values are the proper capitalization.
	 */
		public static synchronized Map<String,String>
	getMap()
	{
		if ( MAP == null )
		{
			MAP	= MapUtil.newMap( MAPPINGS );
		}
		return( MAP );
	}
}

