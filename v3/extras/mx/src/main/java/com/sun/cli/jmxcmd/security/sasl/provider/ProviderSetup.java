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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/security/sasl/provider/ProviderSetup.java,v 1.3 2004/03/09 00:44:44 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2004/03/09 00:44:44 $
 */

package com.sun.cli.jmxcmd.security.sasl.provider;

import java.security.Provider;
import java.security.Security;

/**
 */
public final class ProviderSetup 
{
    private ProviderSetup()	{}
    private static boolean	sInited	= false;
    
	/**
		Not needed for JDK 1.5.
	 */
		private static void
	addSaslProvider()
	{
		final Provider	provider	= new com.sun.security.sasl.Provider();
		
		System.out.println( "provider = " + provider.getInfo() );
			
        Security.addProvider( provider );
	}
	
	/**
		JDK 1.5 does not implement a SASL/PLAIN server mechanism.
	 */
		private static void
	addSaslPLAINProvider()
	{
        Security.addProvider( new PLAINServerProvider() );
	}
	
	
    	public static void
    setup()
    {
    	if ( ! sInited )
    	{
    		sInited	= true;
    		addSaslProvider();
    		addSaslPLAINProvider();
    	}
    }
}
