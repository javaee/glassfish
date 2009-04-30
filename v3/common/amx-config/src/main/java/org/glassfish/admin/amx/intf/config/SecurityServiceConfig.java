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


import java.util.Map;

/**
	 Configuration for the &lt;security-service&gt; element.
 */
public interface SecurityServiceConfig extends
			PropertiesAccess, ConfigElement, Singleton
{
    public static final String AMX_TYPE = "security-service";
    
    @ResolveTo(Boolean.class)
	public String	getAuditEnabled();
	public void	setAuditEnabled( String value );

	public String	getAuditModules();
	public void	setAuditModules( String value );

	public String	getDefaultPrincipalPassword();
	public void	setDefaultPrincipalPassword( String value );

	public String	getDefaultPrincipal();
	public void	setDefaultPrincipal( String value );

	public String	getDefaultRealm();
	public void	setDefaultRealm( String value );

	public String	getJACC();
	public void	setJACC( String value );

    /**                                               
        This attribute is used to customize the                       
        java.security.Principal implementation class used in the      
        default principal to role mapping. This attribute is          
        optional. When it is not specified,                           
        com.sun.enterprise.deployment.Group implementation of         
        java.security.Principal is used. The value of this attribute  
        is only relevant when the activate-default                    
        principal-to-role-mapping attribute is set to true.
        @since AppServer 9.0
     */
    public String  getMappedPrincipalClass();
    
    /**
        @see #getMappedPrincipalClass
        @since AppServer 9.0
     */
    public void    setMappedPrincipalClass( String theClass );
    
    /**                                                     
        Causes the appserver to apply a default principal to role     
        mapping, to any application that does not have an application 
        specific mapping defined. Every role is mapped to a           
        same-named (as the role) instance of a                        
        java.security.Principal implementation class (see             
        mapped-principal-class). This behavior is similar to that of  
        Tomcat servlet container. It is off by default.    
        @since AppServer 9.0
     */
    public String  getActivateDefaultPrincipalToRoleMapping();
    /**
        @see #getActivateDefaultPrincipalToRoleMapping
        @since AppServer 9.0
     */
    public void     setActivateDefaultPrincipalToRoleMapping( String enabled );

// -------------------- Operations --------------------

	/**
		Calls Container.getContaineeMap( XTypes.JACC_PROVIDER_CONFIG ).

		@return Map of JACCProviderConfig MBean proxies , keyed by name.
		@see org.glassfish.admin.amx.base.Container#getContaineeMap
	 */
	public Map<String,JACCProviderConfig>	getJACCProvider();
	
// 	/**
// 		Create a new &lt;jacc-provider>
// 		
// 		@param name			name of the &lt;jacc-provider>
// 		@param policyProvider				a classname
// 		@param policyConfigurationFactoryProvider	a classname
// 		@param reservedForFutureUse		reserved for future use
// 		@return A proxy to the JACCProviderConfig MBean.
// 	 */
// 	public JACCProviderConfig	createJACCProviderConfig(
// 							String	name,
// 							String	policyProvider,
// 							String	policyConfigurationFactoryProvider,
// 							Map 	reservedForFutureUse );
// 
// 	/**
// 		Removes a jacc-provider element.
// 
// 		@param name The name (id) of the jacc-provider to be removed.
// 	 */
// 	public void		removeJACCProviderConfig( String name );
	
	
	/**
		Calls Container.getContaineeMap( XTypes.AUTH_REALM_CONFIG ).

		@return Map of AuthRealmConfig MBean proxies, keyed by name.
		@see org.glassfish.admin.amx.base.Container#getContaineeMap
	 */
	public Map<String,AuthRealmConfig>	getAuthRealm();
	
	
// 	/**
// 		Create a new AuthRealmConfig.  If you are creating a File Realm using
// 		{@link AuthRealmConfig#DEFAULT_REALM_CLASSNAME}, you will also want to specify the
// 		property {@link AuthRealmConfig#KEY_FILE_PROPERTY_KEY}, typically with a template String
// 		such as {@link AuthRealmConfig#KEY_FILE_PREFIX}my-key-file.
// 
// 		@param name		name of the &lt;auth-realm>
// 		@param classname	implementing class, eg {@link AuthRealmConfig#DEFAULT_REALM_CLASSNAME}
// 		@param optional optional parameters (properties)
// 		@return Returns a proxy to the created AuthRealmConfig MBean.
// 	*/
// 	public AuthRealmConfig	createAuthRealmConfig( String name, 
//             String classname, Map<String,String> optional );
//             
// 	/**
// 		Removes an auth-realm element.
// 
// 		@param name     name of the auth-realm
// 	 */
// 	public void		removeAuthRealmConfig( String name );


	/**
		Calls Container.getContaineeMap( XTypes.AUDIT_MODULE_CONFIG ).

		@return Map of AuditModuleConfig MBean proxies, keyed by name.
		@see org.glassfish.admin.amx.base.Container#getContaineeMap
	 */
	public Map<String,AuditModuleConfig>	getAuditModule();
	
	
// 	/**
// 		Create a new &lt;audit-module>.  Note that the {@link AuditModuleConfig#AUDIT_ON_PROPERTY}
//         can be specified in the map as<br>
//         PropertiesAccess.PROPERTY_PREFIX + AuditModuleConfig.AUDIT_ON_PROPERTY .
// 		
// 		@param name		name of the &lt;audit-module
// 		@param className	implementing class
// 		@param reservedForFutureUse		reserved for future use
//         @since GlassFish V3
// 	 */
// 	public AuditModuleConfig	createAuditModuleConfig( String name, String className, Map<String,String> reservedForFutureUse );
// 
// 	/**
// 		Removes an &lt;audit-module&gt element.
// 
// 		@param name		name of the audit-module
// 	 */
// 	public void		removeAuditModuleConfig( String name );
	
	/**
		Calls Container.getContaineeMap( XTypes.MESSAGE_SECURITY_CONFIG ).

		@return Map of MessageSecurityConfig MBean proxies, keyed by name.
		@see org.glassfish.admin.amx.base.Container#getContaineeMap
	 */
	public Map<String,MessageSecurityConfig>	getMessageSecurity();
	
        
//     /**
// 		Create a new &lt;message-security-config&gt;.  Because there must exist at least one
//         {@link ProviderConfig}, you must specify the parameters for one such item.  Additional
//         providers may be created using {@link MessageSecurityConfig#createProviderConfig}.
//         <p>
//         Optional values include {@link MessageSecurityConfigKeys#DEFAULT_PROVIDER_KEY} and
//         {@link MessageSecurityConfigKeys#DEFAULT_CLIENT_PROVIDER_KEY}.
//         <p>
//         See {@link MessageSecurityConfig#createProviderConfig} for more details on parameters
// 
// 		@param authLayer eg {@link MessageLayerValues#SOAP} or {@link MessageLayerValues#HTTP_SERVLET}
//         @param providerID         choose a self-explanatory name for the provider
//         @param providerType       either {@link ProviderConfig#PROVIDER_TYPE_CLIENT} or {@link ProviderConfig#PROVIDER_TYPE_SERVER}
//         @param providerClassname  classname for the provider
// 		@param optional
// 		@return A proxy to the MessageSecurityConfig MBean.
// 		@see MessageSecurityConfigKeys
// 	*/
// 	public MessageSecurityConfig	createMessageSecurityConfig(
// 		String	authLayer,
//         String  providerID,
//         String  providerType, 
// 	    String  providerClassname,
// 		Map<String,String>		optional );
// 
// 	/**
// 		Removes message-security-config element.
// 
// 		@param authLayer
// 	 */
// 	public void		removeMessageSecurityConfig( String authLayer );
}




