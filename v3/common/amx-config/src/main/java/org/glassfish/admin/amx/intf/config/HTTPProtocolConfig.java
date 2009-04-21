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
Configuration for the &lt;http-protocol&gt; element.
     @deprecated  Grizzly variants now exist
*/

public interface HTTPProtocolConfig extends ConfigElement, Singleton
{
    public static final String AMX_TYPE = "http-protocol";
	
	/** http version */
	public static final String VERSION_KEY				=	"Version";
	public static final String DNS_LOOKUP_ENABLED_KEY	=	"DNSLookupEnabled";
	
	/**
		See {@link HTTPProtocolConfig#setForcedType}.
	 */
	public static final String FORCED_TYPE_KEY		=	"ForcedType";
    
	/**
		See {@link HTTPProtocolConfig#setDefaultType}.
	 */
	public static final String DEFAULT_TYPE_KEY		=	"DefaultType";
    
    
	/**
		See {@link HTTPProtocolConfig#setForcedResponseType} for legal values.
		The response type to be forced if the content served cannot   
        be matched by any of the MIME mappings for extensions.        
        Specified as a semi-colon delimited string consisting of      
        content-type, encoding, language, charset
        <p>
        Example: "text/html; charset=iso-8859-1"      
        @deprecated do not use      
	 */
	public static final String FORCED_RESPONSE_TYPE_KEY		=	"ForcedResponseType";
    
	/**
		See {@link #FORCED_RESPONSE_TYPE_KEY}.
        @deprecated do not use
	 */
	public static final String DEFAULT_RESPONSE_TYPE_KEY	=	"DefaultResponseType";
	public static final String SSL_ENABLED_KEY				=	"SSLEnabled";
    
    
    /**
        <b>DO NOT USE, use {@link #getDefaultType}</b>
        @deprecated
     */
    @ResolveTo(Integer.class)
	public String	getDefaultResponseType();
    
	/**
        <b>DO NOT USE, use {@link #setDefaultType}</b>
        @deprecated
	*/
	public void	setDefaultResponseType( final String value );

    @ResolveTo(Boolean.class)
	public String	getDNSLookupEnabled();
	public void	setDNSLookupEnabled( final String value );


	/**
        Example: "text/html; charset=iso-8859-1".
	*/
	public String	getForcedType();
	/**
        See {@link #getForcedType}.
	*/
    public void     setForcedType(String forcedType);
    
    
	/**
		@see HTTPProtocolConfigKeys#FORCED_RESPONSE_TYPE_KEY
	*/
	public String	getDefaultType();
    public void     setDefaultType(String defaultType);
    
	/**
        <b>DO NOT USE, use {@link #getDefaultResponseType}</b>
		@see HTTPProtocolConfigKeys#FORCED_RESPONSE_TYPE_KEY
        @deprecated
	*/
	public String	getForcedResponseType();
	/**
		@see HTTPProtocolConfigKeys#FORCED_RESPONSE_TYPE_KEY
        @deprecated
	*/
	public void	setForcedResponseType( final String value );

    @ResolveTo(Boolean.class)
	public String	getSSLEnabled();
	public void	setSSLEnabled( final String value );

	public String	getVersion();
	public void	setVersion( final String value );

}
