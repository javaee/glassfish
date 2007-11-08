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

/*
 * SCOMap.java
 *
 * created September 20, 2001
 *
 * @author Marina Vatkina
 * @version 1.0.1
 */

package com.sun.org.apache.jdo.sco;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public interface SCOMap extends java.util.Map, SCO {
    /**
     * Resets removed and added lists after flush
     */
    void reset();

    /**
     * Associates the specified value with the specified key in this map 
     * without recording the event. Used internaly to initially populate 
     * the Map.
     */ 
    void putInternal(Object key, Object value);

    /**
     * Copies all of the mappings from the specified map to this one without 
     * recording the event. Used internaly to initially populate the Map.
     */ 
    void putAllInternal(Map t);


    /**
     * Clears Map without recording
     * the event. Used internaly to clear the Map
     */
    void clearInternal();


    /**
     * Removes mappings from the Map without recording
     * the event. Used internally to update the Map
     */
    void removeInternal(Object key);

    /**
     * Returns the Collection of added keys
     *
     * @return Collection of the added keys as java.util.Collection
     */
    Collection getAddedKeys();

    /**
     * Returns the Collection of added values
     *
     * @return Collection of the added values as java.util.Collection
     */
    Collection getAddedValues();

    /**
     * Returns the Collection of removed keys
     *
     * @return Collection of the removed keys as java.util.Collection
     */
    Collection getRemovedKeys();

    /**
     * Returns the Collection of removed values
     *
     * @return Collection of the removed values as java.util.Collection
     */
    Collection getRemovedValues();

    /**
     * Returns the type of the key assignment compatible with all
     * keys of this map.
     * 
     * @return the type of the key assignment compatible with all
     * keys.
     */
    Class getKeyType();

    /**
     * Returns the type of the value assignment compatible with all
     * values of this map.
     * 
     * @return the type of the value assignment compatible with all
     * values.
     */
    Class getValueType();

    /**
     * Returns whether nulls are permitted as keys or values.
     *   
     * @return true if nulls are permitted as keys or values. 
     */ 
    boolean allowNulls();

    /**
     * Set the contents of this Map from the frozen entries.
     * @since 1.0.1
     * @param entries the array of entries
     */
    void setFrozen(Map.Entry[] entries);
    
    /** Get an iterator regardless of whether the map is frozen.
     * If frozen, get a frozen iterator.
     * If thawed, get a regular iterator.
     * @since 1.0.1
     * @return an iterator over the map entries.
     */
    Iterator eitherIterator();
    
    /**
     * Get an iterator over the frozen elements of this map. This allows
     * iteration of the elements without thawing them, as is needed for
     * transcription.
     * @since 1.0.1
     * @return an iterator over the frozen map entries.
     */
    Iterator frozenIterator();
}
