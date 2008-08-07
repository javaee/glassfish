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

import com.sun.appserv.management.base.XTypes;


/**
	Configuration for custom MBean.
	<p>
	When a Custom MBean is loaded, the object name specified via
	the 'ObjectNameInConfig' Attribute is used.  The JMX Domain
	will always be {@link #JMX_DOMAIN}.
	<p>
	If there is a name specified in  'ObjectNameInConfig' eg
	"name=name1", that name is used and the name as returned
	by {@link #getName} is not used within the ObjectName.
	
	<p> Note the following:
	<ul>
	  <li>The MBean is dynamically registered or deregistered upon enabling it. </li>
	  <li>If an MBean is created with a certain ObjectName, a property "server=&lt;server-name&gt;"
	      is added to the ObjectName's representation, when the MBean is being registered with
	      the MBeanServer. The "server-name" above refers to the name of the application server instance.
	      If you deploy it to the admin server ifself, then the property added is: "server=server".</li>
	   <li>If an ObjectName is not specified, then an ObjectName is created by the server.</li>
	   <li>The MBean domain name for a custom MBean is <b> user </b>. </li>
	</ul>
    <b>Questions</b>
    <ul>
    <li>
        When setEnabled( true/false ) is called, are custom MBeans dynamically
        loaded and unloaded?  Document this here.
    </li>
    <li>
        How to obtain the ObjectName for the runtime MBean that
        this config specifies?
    </li>
    <li>
        Are runtime MBeans loaded only in the DAS, or in each server?
        What about the Node Agent?  If they are loaded per server, 
        what is put into the ObjectName to distinguish them?
    </li>
    <li>
        All these either/or cases are confusing.  We should restrict
        the object-name Attribute to properties, prohibit a 'name'
        property, and require a 'type' property--my opinion--Lloyd
    </li>
    </ul>
	
	@see com.sun.appserv.management.config.ResourcesConfig#getCustomMBeanConfigMap
 */
public interface CustomMBeanConfig
    extends NamedConfigElement, Enabled, PropertiesAccess, ObjectType,
    DeployedItemRefConfigReferent, Description
{
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE	= XTypes.CUSTOM_MBEAN_CONFIG;
	
	/**
	    The JMX domain in which all custom MBeans are registered by default.
	 */
	public static final String  JMX_DOMAIN   = "user";
	
	/**
	    Get the ObjectName as configured (which could differ from the ObjectName
	    with which the MBean is registered--TBD).
	    This Attribute may not be changed; it is read-only.
	 */
	public String getObjectNameInConfig();
	
	/**
	    Get the implementation class.
	    This Attribute is read-only.
	 */
	public String getImplClassname();
}











