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

import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.Singleton;
import com.sun.appserv.management.base.XTypes;

import java.util.Map;


/**
    Configuration for the &lt;applications&gt; element; it is an internal "node" which
    groups all resources under itself.
    @since Glassfish V3
*/
public interface ApplicationsConfig
	extends ConfigElement, Container, ConfigCreator, ConfigRemover, ConfigCollectionElement, Singleton
{
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE	= XTypes.APPLICATIONS_CONFIG;
    
    	
	/**
        Glassfish V3 prefers {@link #getApplicationConfigMap}.
        
		@return Map, keyed by name of {@link J2EEApplicationConfig}
		@see #getEJBModuleConfigMap
		@see #getWebModuleConfigMap
		@see #getAppClientModuleConfigMap
		@see #getRARModuleConfigMap
		@see #getLifecycleModuleConfigMap
		@see #getConnectorModuleConfigMap
		@see #getExtensionModuleConfigMap
        @Deprecated
	 */
	public Map<String,J2EEApplicationConfig>			getJ2EEApplicationConfigMap();
    
    
	/**
		@return Map, keyed by name of {@link J2EEApplicationConfig}
		@see #getEJBModuleConfigMap
		@see #getWebModuleConfigMap
		@see #getAppClientModuleConfigMap
		@see #getRARModuleConfigMap
		@see #getLifecycleModuleConfigMap
		@see #getConnectorModuleConfigMap
		@see #getExtensionModuleConfigMap
		@see #getJ2EEApplicationConfigMap
        @since Appserver V3
	 */
	public Map<String,ApplicationConfig>    getApplicationConfigMap();
	
	
	/**
		@return Map, keyed by name of {@link EJBModuleConfig}.
		@see #getJ2EEApplicationConfigMap
		@see #getWebModuleConfigMap
		@see #getAppClientModuleConfigMap
		@see #getRARModuleConfigMap
		@see #getLifecycleModuleConfigMap
		@see #getConnectorModuleConfigMap
		@see #getExtensionModuleConfigMap
	 */
	public Map<String,EJBModuleConfig>			getEJBModuleConfigMap( );
	
	/**
		@return Map, keyed by name of {@link WebModuleConfig}.
		@see #getJ2EEApplicationConfigMap
		@see #getEJBModuleConfigMap
		@see #getAppClientModuleConfigMap
		@see #getRARModuleConfigMap
		@see #getLifecycleModuleConfigMap
		@see #getConnectorModuleConfigMap
		@see #getExtensionModuleConfigMap
	 */
	public Map<String,WebModuleConfig>			getWebModuleConfigMap( );
	
	/**
		@return Map, keyed by name of {@link RARModuleConfig}.
		@see #getJ2EEApplicationConfigMap
		@see #getWebModuleConfigMap
		@see #getEJBModuleConfigMap
		@see #getAppClientModuleConfigMap
		@see #getLifecycleModuleConfigMap
		@see #getConnectorModuleConfigMap
		@see #getExtensionModuleConfigMap
	 */
	public Map<String,RARModuleConfig>			getRARModuleConfigMap();
	
	/**
		@return Map, keyed by name of {@link AppClientModuleConfig}.
		@see #getJ2EEApplicationConfigMap
		@see #getWebModuleConfigMap
		@see #getEJBModuleConfigMap
		@see #getRARModuleConfigMap
		@see #getLifecycleModuleConfigMap
		@see #getConnectorModuleConfigMap
		@see #getExtensionModuleConfigMap
	 */
	public Map<String,AppClientModuleConfig>			getAppClientModuleConfigMap();
	
	/**
		@return Map, keyed by name of {@link LifecycleModuleConfig}.
		@see #getJ2EEApplicationConfigMap
		@see #getWebModuleConfigMap
		@see #getEJBModuleConfigMap
		@see #getRARModuleConfigMap
		@see #getAppClientModuleConfigMap
		@see #getConnectorModuleConfigMap
		@see #getExtensionModuleConfigMap
	 */
	public Map<String,LifecycleModuleConfig>			getLifecycleModuleConfigMap();
	
	/**
		@return Map, keyed by name of {@link LifecycleModuleConfig}.
		@see #getJ2EEApplicationConfigMap
		@see #getWebModuleConfigMap
		@see #getEJBModuleConfigMap
		@see #getRARModuleConfigMap
		@see #getAppClientModuleConfigMap
		@see #getConnectorModuleConfigMap
	 */
	public Map<String,ExtensionModuleConfig>			getExtensionModuleConfigMap();
    

	/**
		@return Map, keyed by name of {@link J2EEApplicationConfig}
		@see #getJ2EEApplicationConfigMap
		@see #getWebModuleConfigMap
		@see #getEJBModuleConfigMap
		@see #getRARModuleConfigMap
		@see #getAppClientModuleConfigMap
		@see #getLifecycleModuleConfigMap
		@see #getExtensionModuleConfigMap
	 */
	public Map<String,ConnectorModuleConfig> getConnectorModuleConfigMap();
    
//-----------------------------------------------------------------------------------------
   /**
		Create a new {@link CustomMBeanConfig}.
		The 'implClassname' must specify a valid classname. If invalid,
		the CustomMBeanConfig will still be created, but of course the MBean
		will not be loaded.
		<p>
		Any number of properties may be included by adding them to the
		Map 'optional'. See {@link PropertiesAccess} for details.
		<p>
		See {@link CustomMBeanConfig} for details on valid values
		for the 'objectNameProperties' parameter, and for details on
		the ObjectName with which the MBean will be registered.
		<p>
        <b>Questions</b>
        <ul>
        <li>
            Where do you put the jar file for the mbean so that it can
            be loaded?
        </li>
        <li>
            What is the behavior if the user
            creates a CustomMBeanConfig specifying 'objectName' with the following:<br>
            "", "user:", "name=foo", "amx:name=foo", "name=foo,type=bar",":".
            <p>
            What are the resulting ObjectNames produced by the above, and how 
            are they obtained?
        </li>
        </ul>

		@param name the display name, will be the name used in the ObjectName 'name' property
		@param implClassname    the implementing class
		@param objectName the partial ObjectName used when registering the MBean
		new module
		@param enabled whether the MBean should load
		@param reserved reserved
	*/
	public CustomMBeanConfig    createCustomMBeanConfig(
                        	        String name,
                        	        String implClassname,
                        	        String objectName,
                        	        boolean enabled,
                        	        Map<String,String> reserved );
                        	        
    /** 
        Remove a CustomMBeanConfig.  All references to it are also removed.
        <p>
        <b>Questions</b>
        <ul>
        <li>Are running MBeans first stopped?</li>
        </ul>
        @param name    name as returned by {@link CustomMBeanConfig#getName}
     */
	public void                  removeCustomMBeanConfig( String name );
	    
	
	/**
		@return Map, keyed by name of {@link CustomMBeanConfig}.
	 */
	public Map<String,CustomMBeanConfig>    getCustomMBeanConfigMap();
    
    
//-----------------------------------------------------------------------------------------
    
    
/**
		Create a new lifecycle module.  A Lifecycle Module must
		implement the interface
		<code>com.sun.appserv.server.LifecycleListener</code>,
		which is outside the scope of AMX, see the product
		documentation.
		<p>
		The 'loadOrder' parameter must be a positive integer value
		(eg >= 1) can be used to force the order in which    
        deployed lifecycle modules are loaded at server start up.     
        Smaller numbered modules get loaded sooner. Order is          
        unspecified if two or more lifecycle modules have the same    
        load-order value.
		<p>                                  
        If 'isFailureFatal' is true,server startup will fail when
        this module does not load properly.                                                   

		@param name the name for the new lifecycle module
		@param description optional description
		@param classname the classname associated with this lifecycle module
		@param classpath optional additioinal classpath
		@param loadOrder integer value to force loading order of LifecycleModules
		@param isFailureFatal if true, server startup will fail when
		        this module does not load properly.   
		@param enabled  whether to load the module at startup
		@return a LifecycleModuleConfig
	*/
	public LifecycleModuleConfig	createLifecycleModuleConfig( String name,
	                            String description,
	                            String classname,
	                            String classpath, 
	                            String loadOrder,
	                            boolean	isFailureFatal,
	                            boolean enabled,
	                            Map<String,String>		reserved );

	/**
		Removes an existing lifecycle module.
		    
		@param name the name of the lifecycle module to be removed.
	*/
	public void			removeLifecycleModuleConfig( String name );


}







