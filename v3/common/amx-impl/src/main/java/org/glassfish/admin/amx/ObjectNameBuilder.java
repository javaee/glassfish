package org.glassfish.admin.amx;

import javax.management.ObjectName;

/**
 * @author llc
 */
public interface ObjectNameBuilder {
    ObjectName getObjectName( String[] hints );
}
