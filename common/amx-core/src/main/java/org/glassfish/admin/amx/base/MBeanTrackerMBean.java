

package org.glassfish.admin.amx.base;
import javax.management.ObjectName;

import java.util.Set;
import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.api.amx.AMXMBeanMetadata;

/**
    MBean providing server-side support for AMX eg for efficiency or other
    reasons.
 */
@AMXMBeanMetadata(leaf=true, singleton=true)
public interface MBeanTrackerMBean
{
    /**
        Get all children of the specified MBean.  An empty set is returned
        if no children are found.
    */
    @ManagedOperation
    public Set<ObjectName> getChildrenOf(final ObjectName parent);
    
    @ManagedOperation
    public ObjectName getParentOf(final ObjectName child);
}











