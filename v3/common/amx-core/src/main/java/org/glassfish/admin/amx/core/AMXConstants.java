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

import org.glassfish.admin.mbeanserver.AMXLoader;

/**
Keys for AMX metadata found in the various javax.management.Desriptors found in MBeanInfo
and its contents.
 */
public final class AMXConstants {

    private AMXConstants() {
    }
    /** constant for the name of the Parent attribute {@link AMXProxy#getParent} */
    public static final String ATTR_PARENT = "Parent";
    /** constant for the name of the Children attribute {@link AMXProxy#getChildren} */
    public static final String ATTR_CHILDREN = "Children";
    /** constant for the name of the Name attribute getName {@link AMXProxy#getChildren} */
    public static final String ATTR_NAME = "Name";
    /** interfaces may contain a type field denoting the type to be used in the ObjectName;
     * this is an alternative to an annotation that may be desirable to avoid
     * a dependency on the amx-core module.  Some proxy interfaces also represent
     * MBeans whose type and other metadata is derived not from the proxy interface,
     * but from another authoritative source; this allows an explicit
     * linkage, albeit one that has to be maintained.
     * eg public static final String AMX_TYPE = "MyType";
     */
    public static final String TYPE_FIELD = "AMX_TYPE";
    /** implied name for singletons, used in ancestor type=name pairs */
    public static final String NO_NAME = "";
    /**
    The JMX domain in which all AMX MBeans are located.
    This will be "amx" after the switch, but is "amx3" until that time.
     */
    public static final String AMX_JMX_DOMAIN = AMXLoader.AMX_JMX_DOMAIN;
    private static final String P = "amx.";
    public static final String NOTIFICATION_PREFIX = P;
    /**
    Group value indicating that the AMX is a
    configuration MBean.
     */
    public static final String GROUP_CONFIGURATION = "configuration";
    /**
    Value indicating that the AMX represents a monitoring MBean.
     */
    public static final String GROUP_MONITORING = "monitoring";
    /**
    Value indicating that the AMX is a utility MBean.
     */
    public static final String GROUP_UTILITY = "utility";
    /**
    Value indicating that the AMX is a JSR 77 MBean
    (J2EE Management) .
     */
    public static final String GROUP_JSR77 = "jsr77";
    /**
    Value indicating that the AMX is not one
    of the other types.
     */
    public static final String GROUP_OTHER = "other";
    /**
    The ObjectName property key denoting the type of the MBean.
     */
    public final static String TYPE_KEY = "type";
    /**
    The ObjectName property key denoting the name of the MBean.
    See {@link #NO_NAME}.
     */
    public final static String NAME_KEY = "name";
    /**
    The ObjectName property key denoting the path of the MBean.
    This field serves to disambiguitate the ObjectName from others
    that might have the same type and name elsewhere in the hierarchy.
    While there is redundancy between type/name and that last part of the
    path, this eliminates algorithmic issues with the type and path
    being different, which is allowed, for example with DomainRoot
    having a path of simply "/".
    public static final String PATH_KEY= "path";
     */
    /**
    The ObjectName property key denoting the path of the parent MBean.
    This field serves to disambiguitate the ObjectName from others
    that might have the same type and name elsewhere in the hierarchy.
    While there is redundancy between type/name and that last part of the
    path, this eliminates algorithmic issues with the type and path
    being different, which is allowed, for example with DomainRoot
    having a path of simply "/".
     */
    public static final String PARENT_PATH_KEY = "pp";
    /**
    Defined by JMX standard: name of the interface for the MBean.
     */
    public static final String DESC_STD_IMMUTABLE_INFO = "immutableInfo";
    /**
    Defined by JMX standard: name of the interface for the MBean.
    public static final String DESC_CACHE_INFO_BY_TYPE = "cachMBeanInfoByType";
     */
    /**
    Defined by JMX standard: name of the interface for the MBean.
    Proxy code might not have access to this class because of module classloader.
    A proxy SHOULD specify a base interace.
     */
    public static final String DESC_STD_INTERFACE_NAME = "interfaceName";
    
    /**
    The generic AMX interface to be used if the class found in
    {@link #DESC_STD_INTERFACE_NAME}
    cannot be loaded.  The class specified here must reside in the amx-core
    module eg org.glassfish.admin.amx.core eg AMXProxy or AMXConfigProxy.
     */
    public static final String DESC_GENERIC_INTERFACE_NAME = P + "genericInterfaceName";
    /**
    Descriptor value: whether the MBean is a singleton, in spite of having a name property in its ObjectName.
    This is mainly for compatibility; named singletons are strongly discouraged.

    Invariant by type: yes
     */
    public static final String DESC_IS_SINGLETON = P + "isSingleton";
    /**
    Descriptor value: Overrides the type value for the purposes of a Pathname.
    For example the DomainConfig MBean uses eg "domain" instead of "DomainConfig".

    This value corresponds to the type (only); it MUST NOT include the name.

    Invariant by type: yes
     */
    public static final String DESC_PATH_PART = P + "pathPart";
    /**
    Descriptor value: Arbitrary string denoting the general classification of MBean.
    Predefined values include "configuration", "monitoring", "jsr77", "utility", "other".
    Invariant by type: yes
     */
    public static final String DESC_GROUP = P + "group";
    /**
    Descriptor value: whether new children may be added by code other than the implementation responsible for the MBean;
    this allows extension points within the hierarchy.
    Adding a new child means registering an MBean with an ObjectName that implies parentage via the ancestry type=name pairs.
     */
    public static final String DESC_SUPPORTS_ADOPTION = P + "supportsAdoption";
    /**
    Descriptor value: denotes the possible types of MBeans that children might be. If present, SHOULD include all possible and pre-known types.

    An empty array indicates that child MBeans might exist, but their types cannot be predicted.

    The key SHOULD NOT be present when amx.isLeaf=true, since it has no applicability.

    Invariant by type: no (allow for different implementations and/or subclassing).
     */
    public static final String DESC_SUB_TYPES = P + "subTypes";
}
