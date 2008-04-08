
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
package com.sun.cli.jcmd.util.stringifier;


import java.security.cert.X509Certificate;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.sun.cli.jcmd.util.misc.StringUtil;


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

