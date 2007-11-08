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
package com.sun.appserv.management.base;

import java.util.Map;
import java.io.IOException;

import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.client.ConnectionSource;


/**
	Extra information available about each {@link AMX}.  Most
	of these fields are for advanced use and/or direct use of JMX.
	
	@see com.sun.appserv.management.base.Util#getExtra
	@see AMX
 */
public interface Extra extends StdAttributesAccess
{
	/*
		*******************************************************
		CAUTION: if any Attribute is added that is not a "real"
		Attribute, be sure to update AMXImplBase.EXTRA_REMOVALS
		*******************************************************
	 */
	
	/**
		Get the names of all available Attributes as found in MBeanInfo.
		
		@return String[] of names
	 */
	public String[]			getAttributeNames( );
	 
	/**
		@return the ObjectName of the MBean targeted by the AMX
	 */
	public ObjectName		getObjectName();
	
	/**
		@return the MBeanInfo for the MBean targeted by the AMX
	 */
	public MBeanInfo		getMBeanInfo();
	
	
	/**
		@return true if the MBean targeted by the AMX has invariant MBeanInfo
	 */
	public boolean			getMBeanInfoIsInvariant();
	
	/**
		Return the Java classname of the interface that this 
		implements.  This classname may then subsequently be used
		to create a standard JMX proxy if desired.  If no such
		classname is appropriate, then null will be returned.
		
		@return the classnames of all implemented  interfaces (usually 1) or null
	 */
	public String			getInterfaceName();
	
	/**
		@return the {@link ProxyFactory} that created the {@link AMX}
	 */
	public ProxyFactory		getProxyFactory();
	
	/**
		@return the ConnectionSource used by the {@link AMX}
	 */
	public ConnectionSource	getConnectionSource();
	
	 
	/**
		Get all available Attribute values, keyed by name.  Available from client only;
		not a "real" Attribute.
		
		@return Map keyed by Attribute name.
	 */
	public Map<String,Object>		getAllAttributes();
	
	/**
		A proxy can become invalid if its corresponding MBean is unregistered. For
		example, if monitoring is disabled, most monitoring MBeans are unregistered
		from the MBeanServer and no longer exist.
		
		@return true if this proxy is valid
	 */
	public boolean	checkValid();

}
