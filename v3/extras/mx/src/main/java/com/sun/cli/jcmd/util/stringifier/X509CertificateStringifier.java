/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
package org.glassfish.admin.amx.util.stringifier;


import java.security.cert.X509Certificate;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.glassfish.admin.amx.util.StringUtil;



/**
	Stringifies an X509CertificateStringifier.
 */
 
public final class X509CertificateStringifier implements Stringifier
{
	public final static X509CertificateStringifier	DEFAULT	= new X509CertificateStringifier();
	
		public
	X509CertificateStringifier()
	{
	}
	
	
		private static byte[]
	getFingerprint( byte[] signature, String alg )
	{
		byte[]	result	= null;
		
		try
		{
			final MessageDigest md = MessageDigest.getInstance( alg );
			
			result	= md.digest( signature );
		}
		catch ( NoSuchAlgorithmException e )
		{
			result	= signature;
			e.printStackTrace();
		}
		
		return( result );
	}
	
	/**
		Static variant when direct call will suffice.
	 */
		public static String
	stringify( final X509Certificate cert )
	{
		final StringBuffer	buf	= new StringBuffer();
		final String		NL	= "\n";
		
		buf.append( "Issuer: " + cert.getIssuerDN().getName() + NL);
		buf.append( "Issued to: " + cert.getSubjectDN().getName()  + NL);
		buf.append( "Version: " + cert.getVersion()  + NL);
		buf.append( "Not valid before: " + cert.getNotBefore()  + NL);
		buf.append( "Not valid after: " + cert.getNotAfter()  + NL);
		buf.append( "Serial number: " + cert.getSerialNumber()  + NL);	
		buf.append( "Signature algorithm: " + cert.getSigAlgName()  + NL);	
		buf.append( "Signature algorithm OID: " + cert.getSigAlgOID()  + NL);
		
		buf.append( "Signature fingerprint (MD5): " );
		byte[]	fingerprint	= getFingerprint( cert.getSignature(), "MD5" );
		buf.append( StringUtil.toHexString( fingerprint, ":" ) + NL );
		
		buf.append( "Signature fingerprint (SHA1): " );
		fingerprint	= getFingerprint( cert.getSignature(), "SHA1" );
		buf.append( StringUtil.toHexString( fingerprint, ":" )  + NL );
		
		return( buf.toString() );
	}
	
		public String
	stringify( Object object )
	{
		return( stringify( (X509Certificate)object ) );
	}
}

