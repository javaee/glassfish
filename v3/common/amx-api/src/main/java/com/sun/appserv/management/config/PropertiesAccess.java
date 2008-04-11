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

import java.util.Map;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.Container;


/**
	All MBeans that have Properties must extend this interface.
	<p>
	Properties are always Strings.  Property names are required to be unique.
	<p>
	To specify properties when creating any type of {@link AMXConfig} which
	(the AMXConfig must extend PropertiesAccess), add them to the optional
	Map when creating it:
<pre><code>
    final Map<String,String> optional    = new HashMap<String,String>();
    optional.put( PropertiesAccess.PROPERTY_PREFIX + "prop1", prop1Value );
    optional.put( PropertiesAccess.PROPERTY_PREFIX + "prop2", prop2Value );
    ...
</code></pref>
    <p>
    New for Glassfish V3:  properties are first-class MBeans and so this
    interface extends Container.
 */
public interface PropertiesAccess extends Container
{
	/**
		When a key is required for a property in a Map,
		its name must consist of this prefix plus the actual name.  When
		accessing a property directly, this prefix must not be used.
	 */
    final static String PROPERTY_PREFIX = "property.";
    
//----------------------------------------------------------------------------------
    /**
		Get the names of all properties.
        @deprecated use the PropertyConfig MBeans from {@link #getPropertyConfigMap}
	 */
	public String[]	getPropertyNames( );
	
	/**
		@return Map containing all properties, keyed by name
        @deprecated use the PropertyConfig MBeans from {@link #getPropertyConfigMap}
	 */
	public Map<String,String>	getProperties();
	
	/**
		Get the value of a property.
		
		@param propertyName	the name of the property
        @deprecated use the PropertyConfig MBeans from {@link #getPropertyConfigMap}
	 */
	public String	getPropertyValue( String propertyName );
					
	/**
		Set the value of a property.  The property must already exist.
		The existing description is retained.
		
		@param propertyName	the name of the property
		@param propertyValue	the value of the property
        @deprecated use the PropertyConfig MBeans from {@link #getPropertyConfigMap}
	 */	
	public void		setPropertyValue( String propertyName, String propertyValue );
						
	/**
		Return true if any properties exist with the specified name.
		
		@param propertyName	the name of the property
        @deprecated use the PropertyConfig MBeans from {@link #getPropertyConfigMap}
	 */
	public boolean	existsProperty( String propertyName );
	
	/**
		Create a new property.
		
		@param propertyName		the name of the property
		@param propertyValue	the value of the property
        @deprecated use {@link #createPropertyConfig}
	 */
	public void		createProperty( String propertyName, String propertyValue);
	
	/**
        @deprecated use {@link #removePropertyConfig}
	 */
	public void		removeProperty( String propertyName );
    
//----------------------------------------------------------------------------------
    
    /**
       @since Glassfish V3
     */
	public PropertyConfig createPropertyConfig( String propertyName, String propertyValue);
    
    /**
       @since Glassfish V3
     */
	public void            removePropertyConfig( String propertyName );
    
	/**
		Return all PropertyConfig MBeans, keyed by property name. 
        @since Glassfish V3
	 */
    public Map<String,PropertyConfig>  getPropertyConfigMap();
}






