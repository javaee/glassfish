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
package org.glassfish.admin.amx.intf.config;

import org.glassfish.admin.amx.base.Singleton;


/**
	Base interface Configuration for an &lt;ssl&gt; element.
*/
@AMXCreateInfo(paramNames={"cert-nickname", "optional"})
public interface SslConfig extends ConfigElement, Singleton
{
    public static final String AMX_TYPE = "ssl";
    
	/**
		Value of class java.lang.Boolean
	 */
	public static final String	CLIENT_AUTH_ENABLED_KEY	= "ClientAuthEnabled";
	
	/**
		This key has a value which is a String containing cipher suite names.
		It must be in comma-separated list.  A "+" preceeding a cipher means
		to enable it and a "-" means to not enable it. Candidates include:
	 */
	public static final String	SSL_2_CIPHERS_KEY		= "SSL2Ciphers";
	/**
		This key has a value which is a String containing cipher suite names.
		It must be in comma-separated list.  A "+" preceeding a cipher means
		to enable it and a "-" means to not enable it. Candidates include:
	 */
	public static final String	SSL_3_TLS_CIPHERS_KEY	= "SSL3TLSCiphers";
	
	/**
		Value of class java.lang.Boolean
	 */
	public static final String	SSL_3_ENABLED_KEY		= "SSL3Enabled";
	
	/**
		Value of class java.lang.Boolean
	 */
	public static final String	SSL_2_ENABLED_KEY		= "SSL2Enabled";
	
	/**
		Value of class java.lang.Boolean
	 */
	public static final String	TLS_ENABLED_KEY			= "TLSEnabled";
	
	/**
		Value of class java.lang.Boolean
	 */
	public static final String	TLS_ROLLBACK_ENABLED_KEY= "TLSRollbackEnabled";
	
	/**
		This key has a value which is a String containing cipher suite names.
		It must be in comma-separated list.  A "+" preceeding a cipher means
		to enable it and a "-" means to not enable it. Candidates include:
		<ul>
			<li>rsa_rc4_128_md5</li>
			<li>rsa3des_sha</li>
			<li>rsa_des_sha</li>
			<li>rsa_rc4_40_md5</li>
			<li>rsa_rc2_40_md5</li>
			<li>rsa_null_md5</li>
			<li>rsa_des_56_sha</li>
			<li>rsa_rc4_56_sha</li>
		</ul>
	 */
	public final static String	SSL3_TLS_CIPHERS_KEY		= "ssl3-tls-ciphers";
    
    
	public String	getCertNickname();
	public void	setCertNickname( String value );

    @ResolveTo(Boolean.class)
	public String	getClientAuthEnabled();
	public void	setClientAuthEnabled( String value );

	public String	getSSL2Ciphers();
	public void	setSSL2Ciphers( String value );

    @ResolveTo(Boolean.class)
	public String	getSSL2Enabled();
	public void	setSSL2Enabled( String value );

    @ResolveTo(Boolean.class)
	public String	getSSL3Enabled();
	public void	setSSL3Enabled( String value );

	public String	getSSL3TLSCiphers();
	public void	setSSL3TLSCiphers( String value );

    @ResolveTo(Boolean.class)
	public String	getTLSEnabled();
	public void	setTLSEnabled( String value );

    @ResolveTo(Boolean.class)
	public String	getTLSRollbackEnabled();
	public void	setTLSRollbackEnabled( String value );

}