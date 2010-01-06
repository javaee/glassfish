/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2010 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/security/sasl/provider/PLAINServerFactory.java,v 1.1 2004/03/09 00:44:44 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/03/09 00:44:44 $
 */
package com.sun.cli.jmxcmd.security.sasl.provider;


import java.util.Map;
import javax.security.auth.callback.*;
import javax.security.sasl.*;

/**
 * Plain SASL server factory. Used with PlainServer.java.
 */
public class PLAINServerFactory implements SaslServerFactory
{
    	public SaslServer
    createSaslServer(
    	final String			mechs,
		final String			protocol,
		final String			serverName,
		final Map				props,
		final CallbackHandler	cbh)
		throws SaslException
	{
		SaslServer	result	= null;
		
		if ( mechs.equals( PLAIN ) )
		{
		    result	=  new PLAINServer(cbh);
		}
		return result;
    }

	static private final String		TRUE	= "true";
	static public final String		PLAIN	= "PLAIN";
	static private final String[]	EMPTY	= new String[ 0 ];
	static private final String[]	PLAIN_ARRAY	= new String[] { PLAIN };
	
    /**
     * Simple-minded implementation that ignores props
     */
    	public String[]
    getMechanismNames(Map props)
    {
    	String[]	result	= null;
    	
		if ( props == null )
		{
			result	= PLAIN_ARRAY;
		}
		else if (
			TRUE.equals( props.get( Sasl.POLICY_NOPLAINTEXT ) ) ||
			TRUE.equals( props.get( Sasl.POLICY_NOACTIVE ) ) ||
			TRUE.equals( props.get( Sasl.POLICY_NODICTIONARY ) ) ||
			TRUE.equals( props.get( Sasl.POLICY_FORWARD_SECRECY ) )
			 )
		{
		System.out.println( "PLAINServerFactory: policy prohibits use" );
			result	= EMPTY;
		}
		else
		{
			result	= PLAIN_ARRAY;
		}
		
		return result;
    }
}
