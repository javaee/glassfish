/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.api.amx;

/**
 * Important MBean values in AMX.
 * All but one moved to org.glassfish.external.amx.AMX* in gmbal~gf_common.
 */
public final class AMXValues
{
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
    
    private AMXValues()
    {
    }
}



