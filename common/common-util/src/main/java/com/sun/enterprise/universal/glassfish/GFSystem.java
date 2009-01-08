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

/**
 * A replacement for System Properties
 * An InheritableThreadLocal is used to store the "impl".  This means that the
 * initial thread that uses this class -- and all its sub-threads will get the
 * same System Properties.
 * Make sure that you don't create it from the main Thread -- otherwise all instances
 * will get the same props.
 * E.g.
 * main thread creates instance1-thread and instance2-thread
 * The 2 created threads should each call init() -- but the main thread should not.
 * In the usual case where there is just one instance in the JVM -- this class is also
 * perfectly usable.  Just call any method when you need something.
 * 
 * @author bnevins
 */
public final class GFSystem {
    public final static void init() {
        // forces creation
        getProperty("java.lang.separator");
    }
    
    /**
     * Get the GFSystem Properties
     * @return a snapshot copy of the dcurrent Properties
     */
    public final static Map<String,String> getProperties()
    {
        return gfsi.get().getProperties();
    }
    
    /**
     * Get a GF System Property
     * @param key the name of the property
     * @return the value of the property
     */
    public final static String getProperty(String key)
    {
        return gfsi.get().getProperty(key);
    }

    /**
     * Set a GF System Property, null is acceptable for the name and/or value.
     * @param key the name of the property
     * @param value the value of the property
     */
    public final static void setProperty(String key, String value)
    {
        gfsi.get().setProperty(key, value);
    }
    
    private static final InheritableThreadLocal<GFSystemImpl> gfsi = 
         new InheritableThreadLocal<GFSystemImpl>() {
             @Override 
             protected GFSystemImpl initialValue() {
                 return new GFSystemImpl();
         }
     };
}
