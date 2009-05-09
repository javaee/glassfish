/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admin.amx.intf.config;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.management.Descriptor;
import javax.management.MBeanAttributeInfo;
import org.glassfish.admin.amx.annotation.Stability;
import org.glassfish.admin.amx.annotation.Taxonomy;
import org.glassfish.admin.amx.config.AMXConfigProxy;
import static org.glassfish.admin.amx.config.AMXConfigConstants.*;

/**
 * Various utilities for working with AMX config MBeans.
 * @author llc
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
public final class AMXConfigHelper
{
    private AMXConfigHelper()
    {
    }

    
    /**
        match all attributes that have a Descriptor field with the specified value.
        (could be extended to use regexp for value and/or field name).
        @param amx the proxy
        @param fieldName name of the field in the Descriptor for each Attribute
        @param value value of the field in the Descriptor for each Attribute
     */
    public static Set<String> attributeNamesByDescriptorField(
        final AMXConfigProxy amx,
        final String fieldName, final String value)
    {
        final Set<String> attrNames = new HashSet<String>();
        for (final MBeanAttributeInfo attrInfo : amx.extra().mbeanInfo().getAttributes())
        {
            final Descriptor desc = attrInfo.getDescriptor();
            if (value.equals(desc.getFieldValue(fieldName)))
            {
                attrNames.add(attrInfo.getName());
            }
        }
        return attrNames;
    }
    
    public static Set<String> simpleAttributes(final AMXConfigProxy amx)
    {
        final String elementKind = org.jvnet.hk2.config.Element.class.getName();
        final Set<String> elementNames = attributeNamesByDescriptorField(amx, DESC_KIND, elementKind);
        final Set<String> remaining = amx.attributeNames();
        remaining.removeAll(elementNames);
        return remaining;
    }

    /**
        Get simple attributes; those that are not Element kind.
     */
    public static Map<String, Object> simpleAttributesMap(final AMXConfigProxy amx)
    {
        return amx.attributesMap( simpleAttributes(amx) );
    }
}


