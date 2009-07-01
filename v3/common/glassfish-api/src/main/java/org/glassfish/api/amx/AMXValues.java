/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.api.amx;

import javax.management.ObjectName;

/**
 * Important MBean values in AMX.
 * <p>
 * Methods instead of constants are used to avoid inlining of constants
 * that could change, and also to afford runtime calculation later/dynamically.
 */
public final class AMXValues
{
    /** constant for the name of the Parent attribute {@link AMXProxy#getParent} */
    public static final String ATTR_PARENT = "Parent";
    /** constant for the name of the Children attribute {@link AMXProxy#getChildren} */
    public static final String ATTR_CHILDREN = "Children";
    /** constant for the name of the Name attribute getName {@link AMXProxy#getChildren} */
    public static final String ATTR_NAME = "Name";
    
    
    /** ObjectName property for the type */
    public static final String TYPE_KEY = "type";

    /** ObjectName property for the name */
    public static final String NAME_KEY = "name";
    
    /** implied name for singletons when name proprety is not present */
    public static final String NO_NAME = "";

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
    
    
    /** Proxy interfaces may contain a type field denoting the type to be used in the ObjectName;
     * this is an alternative to an annotation that may be desirable to avoid
     * a dependency on the amx-core module.  Some proxy interfaces also represent
     * MBeans whose type and other metadata is derived not from the proxy interface,
     * but from another authoritative source; this allows an explicit
     * linkage that allows the AMXProxyHandler to deduce the correct type, given
     * the interface (and avoids any further complexity, the KISS principle).
     * eg public static final String AMX_TYPE = "MyType";
     * <p>
     * A good example of this is the config MBeans which use lower case types with dashes. Other
     * types may use classnames, or other variants; the proxy code can't assume any particular
     * mapping from a proxy interface to the actual MBean type.
     */
    public static final String TYPE_FIELD = "AMX_TYPE";
    
    
    /** prefix for AMX descriptor fields */
    public static final String DESC_PREFIX = "amx.";
    
    /** prefix for notification types */
    public static final String NOTIFICATION_PREFIX = DESC_PREFIX;
    
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
    public static final String DESC_GENERIC_INTERFACE_NAME = DESC_PREFIX + "genericInterfaceName";
    
    /**
    Descriptor value: whether the MBean is a singleton, in spite of having a name property in its ObjectName.
    This is mainly for compatibility; named singletons are strongly discouraged.

    Invariant by type: yes
     */
    public static final String DESC_IS_SINGLETON = DESC_PREFIX + "isSingleton";

    /**
    Descriptor value: whether the MBean is a global singleton eg whether in the AMX domain
    it can be looked up by its type and is the only MBean of that type.
    Invariant by type: yes, axiomatically
     */
    public static final String DESC_IS_GLOBAL_SINGLETON = DESC_PREFIX + "isGlobalSingleton";
    
    
    /**
    Descriptor value: Arbitrary string denoting the general classification of MBean.
    Predefined values include "configuration", "monitoring", "jsr77", "utility", "other".
    Invariant by type: yes
     */
    public static final String DESC_GROUP = DESC_PREFIX + "group";
    /**
    Descriptor value: whether new children may be added by code other than the implementation responsible for the MBean;
    this allows extension points within the hierarchy.
    Adding a new child means registering an MBean with an ObjectName that implies parentage via the ancestry type=name pairs.
     */
    public static final String DESC_SUPPORTS_ADOPTION = DESC_PREFIX + "supportsAdoption";
    /**
    Descriptor value: denotes the possible types of MBeans that children might be. If present, SHOULD include all possible and pre-known types.

    An empty array indicates that child MBeans might exist, but their types cannot be predicted.

    The key SHOULD NOT be present when amx.isLeaf=true, since it has no applicability.

    Invariant by type: no (allow for different implementations and/or subclassing).
     */
    public static final String DESC_SUB_TYPES = DESC_PREFIX + "subTypes";
    
    
    
    /**
    Group value indicating that the AMX is a configuration MBean.
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
    



    private AMXValues()
    {
    }

    /** The JMX domain in which all AMX MBeans are located. */
    public static String amxJMXDomain()
    {
        return "v3";
    }

    /** name of the Domain Admin Server (DAS) as found in an ObjectName */
    public static String dasName()
    {
        return "server";
    }
    
    /** name of the Domain Admin Server (DAS) &lt;config> */
    public static String dasConfig()
    {
        return dasName() + "-config";
    }

    public static ObjectName domainRoot()
    {
        return newObjectName("", "domain-root", amxJMXDomain());
    }

    /** ObjectName for top-level monitoring MBean (parent of those for each server) */
    public static ObjectName monitoringRoot()
    {
        return newObjectName("/", "mon", null);
    }

    /** ObjectName for top-level monitoring MBean for specified server */
    public static ObjectName serverMon(final String serverName)
    {
        return newObjectName("/mon", "server-mon", serverName);
    }

    public static ObjectName getMBeanServerDelegateObjectName()
    {
        return newObjectName("JMImplementation", "type=MBeanServerDelegate");
    }

    public static String prop(final String key, final String value)
    {
        return key + "=" + value;
    }

    /** Make a new AMX ObjectName with unchecked exception */
    public static ObjectName newObjectName(
            final String pp,
            final String type,
            final String name)
    {
        String props = prop(PARENT_PATH_KEY, pp) + "," + prop(TYPE_KEY, type);
        if (name != null)
        {
            props = props + "," + prop(NAME_KEY, name);
        }

        try
        {
            return new ObjectName(amxJMXDomain() + ":" + props);
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /** Make a new ObjectName with unchecked exception */
    public static ObjectName newObjectName(
            final String domain,
            final String props)
    {
        try
        {
            return new ObjectName(domain + ":" + props);
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
