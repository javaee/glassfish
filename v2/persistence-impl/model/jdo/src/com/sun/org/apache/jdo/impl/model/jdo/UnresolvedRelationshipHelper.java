/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.impl.model.jdo;

import java.util.Map;
import java.util.HashMap;

import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOField;

/**
 * A helper class to manage unresolved relationship information. 
 * The class maps the mappedBy name to all JDOField instances using this name
 * (which might denote fields from different classes) as the mapped by name. 
 * To ease the access the list of JDOField instances is organized as a map
 * using the declaring JDOClass as key.
 */
class UnresolvedRelationshipHelper extends HashMap 
{
    /** 
     * Stores an unresolved relationship entry. The specified JDOField uses
     * the specified field name in its mappedBy clause. The specified
     * mappedByName denotes the field on the owning side of the relationship. 
     * @param mappedByName the field name used in the mappedBy clause.
     * @param jdoField the jdoField instance using the specified field name as
     * its mappedBy name.
     */
    public synchronized void register(String mappedByName, JDOField jdoField) {
        Map fieldMap = (Map) get(mappedByName);
        if (fieldMap == null) {
            // new entry for field name
            fieldMap = new HashMap();
            put(mappedByName, fieldMap);
        }
        // store jdoField
        fieldMap.put(jdoField.getDeclaringClass(), jdoField);
    }

    /**
     * Look for a JDOField in the unresolved relationship entry having the
     * specified mappedByName as its mappedBy name. The JDOField must be 
     * declared by the specified jdoClass instance. This allows the owning
     * side to find the JDOField using the name of the owning side in its
     * mappedBy clause.
     * @param mappedByName the field name used as mappedBy name.
     * @param jdoClass the declaring JDOClass of the field to be returned.
     * @return a JDOField declared by the specified jdoClass using the
     * specified mappedByName as its mappedBy name. 
     */
    public synchronized JDOField resolve(String mappedByName, JDOClass jdoClass) {
        JDOField field = null;
        Map fieldMap = (Map) get(mappedByName);
        if (fieldMap != null) {
            // Get JDOField instance for specified JDOClass instance and 
            // remove it directly since it is resolved.
            // Please note, remove returns the removed entry, so we do not
            // need an extra get for the instance to be returned. 
            field = (JDOField) fieldMap.remove(jdoClass);
            // remove the map if it was the last entry
            if (fieldMap.isEmpty()) {
                remove(mappedByName);
            }
        }
        return field;
    }

    /**
     * Removes the specified JDOField from this unresolved relationship
     * helper.
     * @param mappedByName the field name used in the mappedBy clause.
     * @param jdoField the jdoField instance using the specified field name as
     * its mappedBy name.
     */
    public void remove(String mappedByName, JDOField jdoField) {
        // We can delegate to resolve here, because it will remove the
        // resolved entry from the helper and we simply ignore the returned
        // value from resolve. 
        resolve(mappedByName, jdoField.getDeclaringClass());
    }

}
