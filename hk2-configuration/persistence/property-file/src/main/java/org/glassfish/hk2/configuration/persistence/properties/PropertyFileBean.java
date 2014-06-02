/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.glassfish.hk2.configuration.persistence.properties;

import java.util.HashMap;
import java.util.Map;

/**
 * This bean configures the PropertyFileService itself.  An implementation
 * of this bean can be added directly to the Hub, or the utility method
 * {@link PropertyFileService#addPropertyFileBean(PropertyFileBean)} can
 * be used to add this bean to the Hub
 * 
 * @author jwells
 *
 */
public class PropertyFileBean {
    /** The name of the type under which this bean should be placed */
    public final static String TYPE_NAME = "PropertyFileServiceBean";
    
    /** The name of the single instance of this bean */
    public final static String INSTANCE_NAME = "DEFAULT";
    
    private final HashMap<String, Class<?>> mapping = new HashMap<String, Class<?>>();
    
    /**
     * A null constructor for creating an empty PropertyFileBean
     */
    public PropertyFileBean() {
    }
    
    /**
     * This method will create a deep copy of the passed in PropertyFileBean
     * 
     * @param copyMe The non-null bean to copy
     */
    public PropertyFileBean(PropertyFileBean copyMe) {
        mapping.putAll(copyMe.getTypeMapping());
    }
    
    /**
     * Gets the mapping from type name to bean class
     * 
     * @return A copy of the type name to bean class mapping
     */
    public Map<String, Class<?>> getTypeMapping() {
        synchronized (mapping) {
            return new HashMap<String, Class<?>>(mapping);
        }
    }
    
    /**
     * Adds a type mapping to the set of type mappings
     * 
     * @param typeName The name of the type.  May not be null
     * @param beanClass The bean class to which this type should be mapped.
     * May not be null
     */
    public void addTypeMapping(String typeName, Class<?> beanClass) {
        synchronized (mapping) {
            mapping.put(typeName, beanClass);
        }
    }
    
    /**
     * Removes the type mapping with the given name
     * 
     * @param typeName removes the type mapping of the given name.  May
     * not be null
     * @return The class associated with the type name, or null if there
     * was no type mapping with the given name
     */
    public Class<?> removeTypeMapping(String typeName) {
        synchronized (mapping) {
            return mapping.remove(typeName);
        }
    }
    
    /**
     * Gets the type mapping with the given name
     * 
     * @param typeName the type mapping to search for.  May
     * not be null
     * @return The class associated with the type name, or null if there
     * was no type mapping with the given name
     */
    public Class<?> getTypeMapping(String typeName) {
        synchronized (mapping) {
            return mapping.get(typeName);
        }
    }

}
