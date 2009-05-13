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

import com.sun.appserv.management.DomainRoot;

import javax.management.NotificationEmitter;


/**
	Base interface implemented by all available interfaces part
	of the <b>A</b>pp<b>s</b>erver <b>A</b>pplication <b>P</b>rogramming
	<b>I</b>nterface (AMX).
	An AMX is actually a dynamic client-side proxy to a server-side MBean.
	It provides a strongly typed and convenient interface for accessing
	the server-side MBeans.
	<p>
	Certain conventions are followed when an AMX provides
	access to other AMX instances; when a single item is returned, the return
	type is strongly-typed. When a Map or Set is returned,
	the values found in the Map or Set must be cast appropriately.  The context
	should make it obvious what the appropriate cast is.
	It is always safe to cast to AMX, since this is base interface.
    <p>
    Additional information, such as {@link javax.management.MBeanInfo} for the target MBean,
    may be obtained via {@link Util#getExtra}
    <p>
	All AMX that emit Notifications place a Map within
	the userData field of a standard {@link javax.management.Notification}
	which may be obtained via {@link javax.management.Notification#getUserData}.
	Within the Map are zero or more items, which vary with the Notification type.
	Each Notification type, and data available within the Notification,
	is defined in its respective MBean or in an appropriate place.
    
	@see Util#getExtra
    @see com.sun.appserv.management.base.Extra
    @see com.sun.appserv.management.base.Container
    @see com.sun.appserv.management.base.StdAttributesAccess
    @see com.sun.appserv.management.config.PropertiesAccess
    @see com.sun.appserv.management.monitor.MonitoringStats
    @see com.sun.appserv.management.j2ee.J2EEDomain
    @deprecated  going away soon
 */
@Deprecated
public interface AMX extends NotificationEmitter, AMXMBeanLogging, PathnameSupport
{
    /**
        The prefix for all Notification types issued by AMX.
     */
    public static final String  NOTIFICATION_PREFIX  = "com.sun.appserv.management.";
    
    /**
        The JMX domain in which all AMX MBeans are located.
     */
    public static final String  JMX_DOMAIN  = "amx";
    
	/**
		Get the AMX which logically contains this one.  All AMX have
		a Container except for DomainRoot.
		
		@return the parent proxy for this one, possibly null.
		@see AMXAttributes#ATTR_CONTAINER_OBJECT_NAME
	 */
	public Container	getContainer();
	
	/**
		Get the DomainRoot.
		
		@return the Domain representing the Appserver Domain to which this  belongs
	 */
	public DomainRoot	getDomainRoot();
	
	/**
		The delimiter between parts of a fully-qualified type as returned
		by {@link #getFullType}.
	 */
	public static final String FULL_TYPE_DELIM	= ".";
	
	
	/**
		Value from {@link #getGroup} indicating that the AMX is a
		configuration MBean.
	 */
	public static final String	GROUP_CONFIGURATION	= "configuration";
	
	/**
		Value from {@link #getGroup} indicating that the AMX represents a monitoring MBean.
	 */
	public static final String	GROUP_MONITORING	= "monitoring";
	
	/**
		Value from {@link #getGroup} indicating that the AMX is a utility MBean.
	 */
	public static final String	GROUP_UTILITY	= "utility";
	
	/**
		Value from {@link #getGroup} indicating that the AMX is a JSR 77 MBean
		(J2EE Management) .
	 */
	public static final String	GROUP_JSR77	= "jsr77";
	
	/**
		Value from {@link #getGroup} indicating that the AMX is not one
		of the other types.
	 */
	public static final String	GROUP_OTHER	= "other";
	
	
	
	/**
		The ObjectName property key denoting the type of the MBean.
	 */
	public final static String	J2EE_TYPE_KEY			= "j2eeType";
	
	/**
		The ObjectName property key denoting the name of the MBean.
	 */
	public final static String	NAME_KEY			= "name";
	
	/**
		The name given to any MBean lacking a "real" name.  Certain
		MBeans are singletons within their scope, and while they have
		a "name" field within their ObjectName (property {@link #NAME_KEY}),
		they have no meaningful name.
	 */
	public final static String	NO_NAME			= "na";
	
	/**
		The name used when a FullType part refers to a <i>non-existent</i>
		parent eg a standalone ejb or web module that has a null J2EEApplication.
	 */
	public final static String	NULL_NAME			= "null";	// do not change this--it's standard!
	
	
	/**
		Format:<br>
		<pre>
			[[[<i>part</i>].]*]<i>j2eeType</i>
		</pre>
		Example for j2eeType=Servlet:
		<pre>
        type=J2EEServer.J2EEApplication.WebModule.Servlet
        </pre>
		@see AMXAttributes#ATTR_FULL_TYPE
	 */
	public String		getFullType();
	
	
	/**
		Possible values include:
		<ul>
		<li>{@link #GROUP_CONFIGURATION}</li>
		<li>{@link #GROUP_MONITORING}</li>
		<li>{@link #GROUP_UTILITY}</li>
		<li>{@link #GROUP_JSR77}</li>
		<li>{@link #GROUP_OTHER}</li>
		</ul>
		@return the group to which this AMX belongs. 
		@see AMXAttributes#ATTR_GROUP
	 */
	public String		getGroup();


	
	/**
		Get the j2eeType of this item.  Same as the value of the 'j2eeType' property
		within the ObjectName.  The ObjectNames of all AMX contain a property whose
		key is "j2eeType" ({@link #J2EE_TYPE_KEY}) and whose value is specific to the
		sub-interface the item represents.  Equivalent to the J2EE_TYPE field
		found in each AMX sub-interface.
		
		@return String which is the j2eeType
		@see XTypes
		@see com.sun.appserv.management.j2ee.J2EETypes
	 */
	public String	getJ2EEType();
	
	
	
	/**
		Get the name of this item.  Same as the value of the 'name' property
		within the ObjectName.
		
		@return the "name" property value as found inside the ObjectName
		@see #NAME_KEY
	 */
	public String	getName();
	
	/**
	    @return true if this MBean runs natively in DAS, or false if it's a proxy
	    to an MBean outside the DAS.
	 */
	public boolean  isDAS();
}



