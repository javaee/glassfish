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
package org.glassfish.admin.amx.core;
 
import java.util.Set;
import java.util.Map;

import javax.management.ObjectName;


/**
    The  interface presented by a dynamic proxy to an AMX MBean,
    implemented by {@link org.glassfish.admin.amx.core.proxy.AMXProxyHandler}.
    Interfaces representing AMX MBeans may extend or implement this interface, but in
    most cases it will not be appropriate or convenient to 'implement' the interface because
    it is for use by a proxy to the MBean, not the MBean itself.
    <p>
    A convention followed in AMXProxy is that getters do not use "get", in order to
    distinguish them from the usual getter pattern for MBean attributes.  For example,
    {@link #parent} returns and AMXProxy, but {@link #getParent} returns an ObjectName.  The same
    convention is followed for children()/getChildren().
 */
public interface AMXProxy extends AMX_SPI {
    /** MBean MUST return an ObjectName.  May be null for DomainRoot only. */
    public AMXProxy parent();
    
    /**
        Value of the name property of the ObjectName.  Could differ from getName(), which returns
        the internal name, which might not be legal in an ObjectName, or could have changed.
    */
    public String nameProp();
    
    /** The value of the {@link AMXConstants#PARENT_PATH_KEY} property in the ObjectName */
    public String parentPath();
    
    /** The value of the {@link AMXConstants#TYPE_KEY} property in the ObjectName */
    public String type();

	
	/**
		A proxy can become invalid if its corresponding MBean is unregistered.
        If currently marked as valid, a trip to the server is made to verify validity.
		@return true if this proxy is valid
	 */
	public boolean	valid();
    
    /** Get all existing children of all types. Returns null if the MBean is a leaf node (cannot have children) .*/
    public Set<AMXProxy> childrenSet();
    
    /**
        Get all children of a specified type, keyed by the name as found in the ObjectName.
     */
    public Map<String,AMXProxy> childrenMap(final String type);
    
    /** 
        Get all children of the same type.
        The Map is keyed by the name as found in the ObjectName.
        @param intf the proxy interface, type is deduced from it
    */
    public <T extends AMXProxy> Map<String,T> childrenMap(final Class<T> intf);
    
    /**
        Get Maps keyed by type, with a Map keyed by name.
     */
    public Map<String,Map<String,AMXProxy>> childrenMaps();
    
    /**
        Get a singleton child of the specified type.  An exception is thrown if the child is not
        a singleton.  If children do not exist, or there is no such child, then null is returned.
     */
    public AMXProxy child(final String type);
    
    /** Get a singleton child. Its type is derived from the interface using {@link Util#deduceType}. */
    public <T extends AMXProxy> T child(final Class<T> intf);
    
    /**
        Return a proxy implementing the specified interface. 
     */
    public <T extends AMXProxy> T as(Class<T> intf);
	 
	/**
		Get all available Attributes, keyed by name.
		@return Map keyed by Attribute name.
	 */
	public Map<String,Object>		attributesMap();
    
	/**
		Get all available Attributes names, no trip to server needed.
	 */
    public Set<String>  attributeNames();
    
    /** Get this MBean's pathname */
    public String path();
    
	/**
		The ObjectName of this MBean.
	 */
	public ObjectName		objectName();
    
    /** return a Java interface representing this MBean */
    public String java();
        
    /** additional capabilities */
    public Extra extra();
}









