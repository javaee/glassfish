/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.universal.glassfish;

import com.sun.enterprise.universal.collections.CollectionUtils;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A replacement for System Properties
 * This is designed so that each instance in a shared JVM can have its own
 * System Properties.
 * @author bnevins
 */
public final class GFSystemImpl {
    /**
     * Get the GFSystemImpl Properties
     * @return a snapshot copy of the current Properties
     */
    public final Map<String,String> getProperties()
    {
        return Collections.unmodifiableMap(props);
    }
    
    /**
     * Get a GF System Property
     * @param key the name of the property
     * @return the value of the property
     */
    public final String getProperty(String key)
    {
        return props.get(key);
    }

    /**
     * Set a GF System Property, null is acceptable for the name and/or value.
     * @param key the name of the property
     * @param value the value of the property
     */
    public final void setProperty(String key, String value)
    {
        props.put(key, value);
    }
    
    public GFSystemImpl() {
    }
    
    // initial props copy java.lang.System Properties
    private final ConcurrentMap<String,String> props = new ConcurrentHashMap<String, String>(
            CollectionUtils.propertiesToStringMap(System.getProperties()));
}
