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
package com.sun.appserv.management.config;

import com.sun.appserv.management.base.Singleton;
import com.sun.appserv.management.base.XTypes;

/**
	Base interface Configuration for an &lt;ssl&gt; element.
*/
@AMXCreateInfo(paramNames={"cert-nickname", "optional"})
public interface SSLConfig extends ConfigElement, Singleton
{
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE	= XTypes.SSL_CONFIG;
	
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