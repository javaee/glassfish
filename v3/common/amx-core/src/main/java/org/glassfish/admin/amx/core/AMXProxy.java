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
import org.glassfish.admin.amx.annotation.Stability;
import org.glassfish.admin.amx.annotation.Taxonomy;
import org.glassfish.admin.amx.core.proxy.AMXProxyHandler;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;

/**
The  interface presented by a dynamic proxy to an AMX MBean,
implemented by {@link AMXProxyHandler}.
Interfaces representing AMX MBeans may extend this interface, but in
most cases it will not be appropriate or convenient for MBean implementors to
'implement' the interface because it is for use by a <i>proxy to the MBean</i>, not the MBean itself.
<p>
A convention followed in AMXProxy is that convenience "getter" methods
(implement by the proxy handler) do not use the "get" prefix,
in order to distinguish them from the usual getter pattern for real MBean attributes.
For example, {@link #parent} returns an AMXProxy, but {@link #getParent} returns the value of the
{@code Parent} attribute (an ObjectName).
The same convention is followed for {@link #childrenSet}, etc / {@link #getChildren}.

<p>
Proxy interfaces should not be considered authoritative, meaning that an underlying MBean
<i>implementation</i> determines what the MBean actually provides, generally without awareness of
the proxy interface.  Therefore, it is possible for the proxy interface
to completely misrepresent the actual MBean functionality.  For this reason, sub-interfaces
of {@code AMXProxy} might omit specific getter/setter methods, and instead focus on the <i>containment
relationships</i>, which form the core of usability of navigating the hierarchy, looking up
children, etc.  Only at runtime would errors between the interface and the MBean would emerge.
<p>
 * MBean clients can accomplish all objectives using only this generic AMXProxy interface, but it
 * can be more convenient to use sub-interfaces in Java clients.
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
public interface AMXProxy extends AMX_SPI
{
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
    public boolean valid();

    /** Get all existing children of all types. Returns null if the MBean is a leaf node (cannot have children) .*/
    public Set<AMXProxy> childrenSet();

    /**
    Get all children of a specified type, keyed by the name as found in the ObjectName.
     */
    public Map<String, AMXProxy> childrenMap(final String type);

    /** 
    Get all children of the same type.
    The Map is keyed by the name as found in the ObjectName.
    @param intf the proxy interface, type is deduced from it
     */
    public <T extends AMXProxy> Map<String, T> childrenMap(final Class<T> intf);

    /**
    Get Maps keyed by type, with a Map keyed by name.
     */
    public Map<String, Map<String, AMXProxy>> childrenMaps();

    /**
    Get a singleton child of the specified type.  An exception is thrown if the child is not
    a singleton.  If children do not exist, or there is no such child, then null is returned.
     */
    public AMXProxy child(final String type);

    /** Get a singleton child. Its type is derived from the interface using {@link Util#deduceType}. */
    public <T extends AMXProxy> T child(final Class<T> intf);

    /**
     Return a proxy implementing the specified interface.  Clients with access to
     a sub-interface of {@link AMXProxy} can specialized it with this method; the proxy
     by default will implement only the base {@link AMXProxy} interface.
     */
    public <T extends AMXProxy> T as(Class<T> intf);

    /**
    Get a Map keyed by Attribute name of all Attribute values.
     */
    public Map<String, Object> attributesMap();

    /**
    Get a Map keyed by Attribute name of the specified Attribute values.
     */
    public Map<String, Object> attributesMap(final Set<String> attrNames);

    /**
    Get all available Attributes names, no trip to server needed.
     */
    public Set<String> attributeNames();

    /** Get this MBean's pathname.  Its parent path can be obtained by calling {@code path}
     on {@link #parent} */
    public String path();

    /**
    The ObjectName of this MBean.
     */
    public ObjectName objectName();

    /** Return a Java interface representing this MBean, suitable for display or compilation */
    public String java();

    /** additional capabilities, including direct JMX access */
    public Extra extra();

}









