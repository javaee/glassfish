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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/stringifier/HandshakeCompletedEventStringifier.java,v 1.4 2005/11/08 22:39:26 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2005/11/08 22:39:26 $
 */
 
package org.glassfish.admin.amx.util.stringifier;

import javax.net.ssl.HandshakeCompletedEvent;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

/**
	Stringifies a HandshakeCompletedEvent.
 */
 
public final class HandshakeCompletedEventStringifier implements Stringifier
{
	public final static HandshakeCompletedEventStringifier	DEFAULT	= new HandshakeCompletedEventStringifier();
	
		public
	HandshakeCompletedEventStringifier()
	{
	}
	
	/**
		Static variant when direct call will suffice.
	 */
		public static String
	stringify( final HandshakeCompletedEvent event )
	{
		final String	NL	= "\n";
			
		StringBuffer	buf	= new StringBuffer();
		buf.append( "Cipher suite: " + event.getCipherSuite() + NL);
		buf.append( "Address: " + event.getSocket().getRemoteSocketAddress().toString() + NL);
		
		try
		{
			buf.append( "Certificate chain:" + NL);
			final Certificate[]	certChain	= event.getPeerCertificates();
			for( int i = 0; i < certChain.length; ++i )
			{
				final X509Certificate	cert	= (X509Certificate)certChain[ i ];
				final String			certString	= X509CertificateStringifier.stringify( cert);
				
				buf.append( certString );
				buf.append( NL + "=>" + NL );
			}
		}
		catch ( javax.net.ssl.SSLPeerUnverifiedException e )
		{
			buf.append( "TLS PEER UNVERIFIED (no certificate)" );
		}
		
		return( buf.toString() );
	}
	
		public String
	stringify( Object object )
	{
		return( stringify( (X509Certificate)object ) );
	}
}

