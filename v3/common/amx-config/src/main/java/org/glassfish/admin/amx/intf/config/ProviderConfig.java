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




import java.util.Map;

/**
	 Configuration for the &lt;provider-config&gt; element.
*/

public interface ProviderConfig
	extends NamedConfigElement, PropertiesAccess
{
    public static final String AMX_TYPE = "provider-config";
    
	/** Key for use with {@link #createRequestPolicyConfig} and {@link #createResponsePolicyConfig}*/
	public static final String	AUTH_SOURCE_KEY		=	"AuthSource";
	
	/** Key for use with {@link #createRequestPolicyConfig} and {@link #createResponsePolicyConfig}*/
	public static final String	AUTH_RECIPIENT_KEY	=	"AuthRecipient";

    /**  */
    public static final String PROVIDER_TYPE_CLIENT = "client";
    
    /**  */
    public static final String PROVIDER_TYPE_SERVER = "server";
    
	public String	getClassName();
	public void	setClassName( final String value );

	public String	getProviderId();

    /** Returns {@link #PROVIDER_TYPE_CLIENT} or {@link #PROVIDER_TYPE_SERVER} */
	public String	getProviderType();
    
    /** Use {@link #PROVIDER_TYPE_CLIENT} or {@link #PROVIDER_TYPE_SERVER} */
	public void	setProviderType( final String value );

// -------------------- Operations --------------------
// 	/**
// 		Creates new request-policy-config element.
// 
// 		@param optional Map of optional attributes whose keys are defined here.
// 		(eg:- AUTH_RECIPIENT_KEY)
// 		@return A proxy to the RequestPolicyConfig MBean.
// 	 */
// 	public RequestPolicyConfig	createRequestPolicyConfig( Map<String,String> optional );
// 
// 	/**
// 		Removes request-policy-config element.
// 	 */
// 	public void					removeRequestPolicy();

	/**
		Get the RequestPolicyConfig MBean.
	 */
	RequestPolicyConfig	getRequestPolicy();

// 	/**
// 		Creates new response-policy-config element.
// 
// 		@param optional Map of optional attributes whose keys are defined here.
// 		(eg:- AUTH_RECIPIENT_KEY)
// 		@return A proxy to the ResponsePolicyConfig MBean.
// 	 */
// 	public ResponsePolicyConfig		createResponsePolicyConfig( Map<String,String> optional );
// 
// 	/**
// 		Removes response-policy-config element.
// 	 */
// 	public void						removeResponsePolicy();

	/**
		Get the ResponsePolicyConfig MBean.
	 */
	ResponsePolicyConfig			getResponsePolicy();



}
